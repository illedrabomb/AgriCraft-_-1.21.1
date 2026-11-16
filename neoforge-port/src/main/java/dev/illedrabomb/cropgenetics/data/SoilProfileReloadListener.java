package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Server datapack profiles: data/<namespace>/cropgenetics/soil_profiles/*.json
 * Each record targets exactly one block ID OR block tag for chemistry and growth_modifier.
 * Duplicate target blocks are disabled rather than resolved arbitrarily.
 */
public final class SoilProfileReloadListener
        extends SimplePreparableReloadListener<Map<ResourceLocation, JsonElement>> {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String ROOT = "cropgenetics/soil_profiles";
    private final SoilProfileRegistry registry;

    public SoilProfileReloadListener(SoilProfileRegistry registry) {
        this.registry = registry;
    }

    @Override
    protected Map<ResourceLocation, JsonElement> prepare(ResourceManager manager,
                                                         ProfilerFiller profiler) {
        Map<ResourceLocation, JsonElement> resources = new HashMap<>();
        manager.listResources(ROOT, key -> key.getPath().startsWith(ROOT + "/")
                && key.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    try (Reader reader = entry.getValue().openAsReader()) {
                        resources.put(entry.getKey(), JsonParser.parseReader(reader));
                    } catch (Exception ex) {
                        LOG.warn("Unreadable soil profile {}: {}", entry.getKey(), ex.getMessage());
                    }
                });
        return Map.copyOf(resources);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> input, ResourceManager manager,
                         ProfilerFiller profiler) {
        Map<String, SoilProfileRegistry.Profile> profiles = new HashMap<>();
        Map<String, SoilProfileRegistry.Profile> tagged = new HashMap<>();
        Set<String> conflicting = new HashSet<>();
        Set<String> conflictingTags = new HashSet<>();
        input.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                Set<String> requiredKeys = Set.of("humidity", "acidity", "nutrients");
                Set<String> allowedKeys = Set.of("block", "block_tag", "humidity", "acidity",
                        "nutrients", "growth_modifier");
                if (!json.keySet().containsAll(requiredKeys)
                        || !allowedKeys.containsAll(json.keySet())
                        || json.has("block") == json.has("block_tag"))
                    throw new IllegalArgumentException(
                            "Expected exactly one of block/block_tag, plus humidity/acidity/nutrients");
                boolean isTag = json.has("block_tag");
                String block = json.get(isTag ? "block_tag" : "block").getAsString();
                ResourceLocation key = ResourceLocation.tryParse(block);
                if (key == null || (!isTag && (BuiltInRegistries.BLOCK.getOptional(key).isEmpty()
                        || BuiltInRegistries.BLOCK.getOptional(key).orElseThrow() == Blocks.AIR)))
                    throw new IllegalArgumentException("Invalid block/tag ID: " + block);
                SoilChemistryAccess.Sample sample = new SoilChemistryAccess.Sample(
                        SoilChemistryRule.parse(SoilChemistryRule.Humidity.class,
                                json.get("humidity").getAsString()),
                        SoilChemistryRule.parse(SoilChemistryRule.Acidity.class,
                                json.get("acidity").getAsString()),
                        SoilChemistryRule.parse(SoilChemistryRule.Nutrients.class,
                                json.get("nutrients").getAsString()));
                double modifier = json.has("growth_modifier")
                        ? json.get("growth_modifier").getAsDouble() : 1.0D;
                SoilProfileRegistry.Profile profile = new SoilProfileRegistry.Profile(sample, modifier);
                Map<String, SoilProfileRegistry.Profile> target = isTag ? tagged : profiles;
                Set<String> duplicates = isTag ? conflictingTags : conflicting;
                if (duplicates.contains(block)) return;
                if (target.putIfAbsent(block, profile) != null) {
                    target.remove(block);
                    duplicates.add(block);
                }
            } catch (RuntimeException ex) {
                LOG.warn("Invalid soil profile {}: {}", entry.getKey(), ex.getMessage());
            }
        });
        for (String block : conflicting)
            LOG.warn("Conflicting soil profile declarations for block {}, disabled", block);
        for (String tag : conflictingTags)
            LOG.warn("Conflicting soil profile declarations for tag #{}, disabled", tag);
        registry.replaceProfiles(profiles, tagged);
        LOG.info("Loaded {} soil chemistry profiles ({} conflicting blocks, {} conflicting tags)",
                registry.size(), conflicting.size(), conflictingTags.size());
    }
}
