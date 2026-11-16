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
 * Each record assigns exactly one known BLOCK ID to a qualitative soil sample.
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
        Map<String, SoilChemistryAccess.Sample> profiles = new HashMap<>();
        Set<String> conflicting = new HashSet<>();
        input.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                Set<String> allowedKeys = Set.of("block", "humidity", "acidity", "nutrients");
                if (!json.keySet().equals(allowedKeys))
                    throw new IllegalArgumentException(
                            "Expected exactly block, humidity, acidity and nutrients");
                String block = json.get("block").getAsString();
                ResourceLocation key = ResourceLocation.tryParse(block);
                if (key == null || BuiltInRegistries.BLOCK.getOptional(key).isEmpty()
                        || BuiltInRegistries.BLOCK.getOptional(key).orElseThrow() == Blocks.AIR)
                    throw new IllegalArgumentException("Unknown or air block: " + block);
                SoilChemistryAccess.Sample sample = new SoilChemistryAccess.Sample(
                        SoilChemistryRule.parse(SoilChemistryRule.Humidity.class,
                                json.get("humidity").getAsString()),
                        SoilChemistryRule.parse(SoilChemistryRule.Acidity.class,
                                json.get("acidity").getAsString()),
                        SoilChemistryRule.parse(SoilChemistryRule.Nutrients.class,
                                json.get("nutrients").getAsString()));
                if (conflicting.contains(block)) return;
                if (profiles.putIfAbsent(block, sample) != null) {
                    profiles.remove(block);
                    conflicting.add(block);
                }
            } catch (RuntimeException ex) {
                LOG.warn("Invalid soil profile {}: {}", entry.getKey(), ex.getMessage());
            }
        });
        for (String block : conflicting)
            LOG.warn("Conflicting soil profile declarations for block {}, disabled", block);
        registry.replaceAll(profiles);
        LOG.info("Loaded {} explicit soil chemistry profiles ({} conflicts)",
                registry.size(), conflicting.size());
    }
}
