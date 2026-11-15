package dev.illedrabomb.cropgenetics.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/** Reads data/<namespace>/cropgenetics/mutations/*.json at server reload. */
public final class MutationReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    private final AgriPlantsRegistry registry;
    private final Predicate<String> loadedMod;

    public MutationReloadListener(AgriPlantsRegistry registry, Predicate<String> loadedMod) {
        super(new Gson(), "cropgenetics/mutations");
        this.registry = registry;
        this.loadedMod = loadedMod;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, MutationDefinition> result = new HashMap<>();
        resources.forEach((key, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                if (!AgriPlantsParser.enabled(json)) return;
                // Original AgriPlants mutations declare required mods just as
                // plants do; never activate missing-mod recipes.
                if (json.has("mods")) {
                    for (JsonElement mod : json.getAsJsonArray("mods")) {
                        String modId = mod.getAsString();
                        if (!modId.equals("agricraft") && !loadedMod.test(modId)) {
                            LOG.info("Skipping mutation {}: missing mod {}", key, modId);
                            return;
                        }
                    }
                }
                MutationDefinition mutation = AgriPlantsParser.mutation(json);
                // Reject references missing from the current enabled plant
                // catalogue. Invalid datapack mutations must not silently
                // become active when their target is loaded later.
                Map<String, PlantDefinition> plants = registry.plants();
                if (!plants.containsKey(mutation.parentA())
                        || !plants.containsKey(mutation.parentB())
                        || !plants.containsKey(mutation.childPlantId())) {
                    LOG.warn("Skipping mutation {}: unknown/disabled plant reference ({}, {}, {})",
                            key, mutation.parentA(), mutation.parentB(), mutation.childPlantId());
                    return;
                }
                String id = key.toString();
                result.put(id, mutation);
            } catch (RuntimeException ex) {
                LOG.error("Skipping invalid mutation definition {}: {}", key, ex.getMessage());
            }
        });
        registry.replaceMutations(result);
        LOG.info("Loaded {} mutation definitions", result.size());
    }
}
