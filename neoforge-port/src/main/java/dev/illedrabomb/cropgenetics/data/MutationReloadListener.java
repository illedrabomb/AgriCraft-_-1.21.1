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

/** Reads data/<namespace>/cropgenetics/mutations/*.json at server reload. */
public final class MutationReloadListener extends SimpleJsonResourceReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    private final AgriPlantsRegistry registry;

    public MutationReloadListener(AgriPlantsRegistry registry) {
        super(new Gson(), "cropgenetics/mutations");
        this.registry = registry;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, ResourceManager manager, ProfilerFiller profiler) {
        Map<String, MutationDefinition> result = new HashMap<>();
        resources.forEach((key, element) -> {
            try {
                JsonObject json = element.getAsJsonObject();
                if (!AgriPlantsParser.enabled(json)) return;
                MutationDefinition mutation = AgriPlantsParser.mutation(json);
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
