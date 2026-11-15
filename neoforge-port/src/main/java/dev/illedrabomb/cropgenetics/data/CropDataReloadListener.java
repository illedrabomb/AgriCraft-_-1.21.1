package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;

import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Both resource folders are parsed from one ResourceManager snapshot and
 * published together. A mutation never sees an older plant catalogue.
 */
public final class CropDataReloadListener extends
        SimplePreparableReloadListener<CropDataReloadListener.Prepared> {
    private static final Logger LOG = LogUtils.getLogger();
    private static final String PLANTS = "cropgenetics/plants";
    private static final String MUTATIONS = "cropgenetics/mutations";

    private final AgriPlantsRegistry registry;
    private final Predicate<String> loadedMod;

    public CropDataReloadListener(AgriPlantsRegistry registry, Predicate<String> loadedMod) {
        this.registry = registry;
        this.loadedMod = loadedMod;
    }

    /** Resource I/O only; this phase never writes the active registry. */
    @Override
    protected Prepared prepare(ResourceManager manager, ProfilerFiller profiler) {
        return new Prepared(readJson(manager, PLANTS), readJson(manager, MUTATIONS));
    }

    private static Map<ResourceLocation, JsonElement> readJson(ResourceManager manager, String root) {
        Map<ResourceLocation, JsonElement> values = new HashMap<>();
        // ResourceManager already applies pack priority to each resource key.
        manager.listResources(root, id -> id.getPath().endsWith(".json"))
                .entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                    try (Reader reader = entry.getValue().openAsReader()) {
                        values.put(entry.getKey(), JsonParser.parseReader(reader));
                    } catch (Exception exception) {
                        LOG.error("Skipping unreadable {}: {}", entry.getKey(), exception.getMessage());
                    }
                });
        return Map.copyOf(values);
    }

    @Override
    protected void apply(Prepared input, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, PlantDefinition> plants = new HashMap<>();
        Set<String> duplicates = new HashSet<>();

        input.plants().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                if (!AgriPlantsParser.enabled(json) || !modsAvailable(json)) return;
                PlantDefinition plant = AgriPlantsParser.plant(json);
                if (duplicates.contains(plant.id())) return;
                if (plants.putIfAbsent(plant.id(), plant) != null) {
                    plants.remove(plant.id());
                    duplicates.add(plant.id());
                    LOG.error("Conflicting plant ID {} at {}: disabling the species",
                            plant.id(), entry.getKey());
                }
            } catch (RuntimeException ex) {
                LOG.error("Skipping invalid plant {}: {}", entry.getKey(), ex.getMessage());
            }
        });

        Map<String, MutationDefinition> mutations = new HashMap<>();
        input.mutations().entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            try {
                JsonObject json = entry.getValue().getAsJsonObject();
                if (!AgriPlantsParser.enabled(json) || !modsAvailable(json)) return;
                MutationDefinition mutation = AgriPlantsParser.mutation(json);
                if (!plants.containsKey(mutation.parentA())
                        || !plants.containsKey(mutation.parentB())
                        || !plants.containsKey(mutation.childPlantId())) {
                    LOG.warn("Skipping mutation {}: unknown or disabled parent/child species",
                            entry.getKey());
                    return;
                }
                mutations.put(entry.getKey().toString(), mutation);
            } catch (RuntimeException ex) {
                LOG.error("Skipping invalid mutation {}: {}", entry.getKey(), ex.getMessage());
            }
        });

        // Exactly one volatile publication for plants, mutations and seed index.
        registry.replaceAll(plants, mutations);
        AgriPlantsRegistry.Snapshot active = registry.snapshot();
        if (!duplicates.isEmpty())
            LOG.warn("Disabled {} conflicting plant IDs: {}", duplicates.size(), duplicates);
        if (!active.ambiguousSeedItems().isEmpty())
            LOG.warn("Ambiguous seed mappings disabled: {}", active.ambiguousSeedItems());
        LOG.info("Crop datapack loaded: {} plants, {} mutations",
                active.plants().size(), active.mutations().size());
    }

    private boolean modsAvailable(JsonObject json) {
        if (!json.has("mods")) return true;
        for (JsonElement mod : json.getAsJsonArray("mods")) {
            String id = mod.getAsString();
            // AgriCraft data's self-dependency is now supplied by this mod.
            if (!"agricraft".equals(id) && !loadedMod.test(id)) return false;
        }
        return true;
    }

    public record Prepared(Map<ResourceLocation, JsonElement> plants,
                           Map<ResourceLocation, JsonElement> mutations) {}
}
