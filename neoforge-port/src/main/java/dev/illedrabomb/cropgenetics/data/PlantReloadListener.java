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

/** Reads data/<namespace>/cropgenetics/plants/*.json at server datapack reload. */
public final class PlantReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    private final AgriPlantsRegistry registry;
    private final Predicate<String> loadedMod;

    public PlantReloadListener(AgriPlantsRegistry registry, Predicate<String> loadedMod) {
        super(new Gson(), "cropgenetics/plants");
        this.registry = registry;
        this.loadedMod = loadedMod;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, PlantDefinition> result = new HashMap<>();
        resources.forEach((key, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                if (!AgriPlantsParser.enabled(json)) return;
                PlantDefinition plant = AgriPlantsParser.plant(json);
                // "agricraft" is the original data-pack self-dependency; now provided by this mod.
                if (plant.requiredMods().stream().anyMatch(mod -> !mod.equals("agricraft") && !loadedMod.test(mod))) return;
                if (result.putIfAbsent(plant.id(), plant) != null)
                    throw new IllegalArgumentException("Duplicate crop ID: " + plant.id());
            } catch (RuntimeException ex) {
                LOG.error("Skipping invalid plant definition {}: {}", key, ex.getMessage());
            }
        });
        registry.replacePlants(result);
        LOG.info("Loaded {} crop definitions", result.size());
    }
}
