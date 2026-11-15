package dev.illedrabomb.cropgenetics.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Resource-pack plant visuals, independently reloadable from server crop data.
 * Supports cross / plus / gourd-style vegetation with up to two texture layers.
 * These are approximations, not the original AgriCraft model renderer.
 */
public final class PlantVisuals extends SimpleJsonResourceReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    public static final PlantVisuals INSTANCE = new PlantVisuals();
    private static final ResourceLocation FALLBACK =
            ResourceLocation.withDefaultNamespace("block/oak_leaves");

    public enum Geometry { CROSS, PLUS, GOURD }

    public record Visual(Geometry geometry, List<List<ResourceLocation>> stages) {
        public Visual {
            stages = stages.stream().map(List::copyOf).toList();
        }

        public List<ResourceLocation> at(int stage, int totalStages) {
            int last = stages.size() - 1;
            int index = totalStages <= 1 ? last
                    : Math.min(last, (int) ((long) Math.max(0, stage) * last / (totalStages - 1)));
            return stages.get(index);
        }
    }

    private volatile Map<String, Visual> visuals = Map.of();

    private PlantVisuals() {
        super(new Gson(), "cropgenetics/plant_visuals");
    }

    public Visual visual(String plantId) {
        return visuals.getOrDefault(plantId,
                new Visual(Geometry.CROSS, List.of(List.of(FALLBACK))));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> sources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<String, Visual> next = new HashMap<>();
        // Stable duplicate handling regardless of resource manager map order.
        sources.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(source -> {
            try {
                JsonObject json = source.getValue().getAsJsonObject();
                String plantId = json.has("plant") ? json.get("plant").getAsString()
                        : json.get("id").getAsString();
                if (ResourceLocation.tryParse(plantId) == null) {
                    throw new IllegalArgumentException("Invalid plant ID");
                }

                JsonObject legacy = json.has("texture") ? json.getAsJsonObject("texture") : null;
                String geometryName = legacy != null
                        ? legacy.get("render_type").getAsString()
                        : json.has("geometry") ? json.get("geometry").getAsString() : "cross";
                Geometry geometry = switch (geometryName) {
                    case "cross", "hash" -> Geometry.CROSS;
                    case "plus" -> Geometry.PLUS;
                    case "gourd" -> Geometry.GOURD;
                    default -> throw new IllegalArgumentException("Unsupported geometry: " + geometryName);
                };

                JsonArray entries = legacy == null
                        ? json.has("stages") ? json.getAsJsonArray("stages")
                                : json.getAsJsonArray("textures")
                        : legacy.getAsJsonArray("plant_textures");
                if (entries == null || entries.size() < 1 || entries.size() > 64) {
                    throw new IllegalArgumentException("Expected 1..64 stages");
                }
                List<List<ResourceLocation>> stages = new ArrayList<>();
                for (JsonElement entry : entries) {
                    List<ResourceLocation> layers = new ArrayList<>();
                    if (entry.isJsonArray()) {
                        for (JsonElement layer : entry.getAsJsonArray()) {
                            layers.add(parseTexture(layer.getAsString()));
                        }
                    } else {
                        layers.add(parseTexture(entry.getAsString()));
                    }
                    if (layers.isEmpty() || layers.size() > 2) {
                        throw new IllegalArgumentException("Expected 1 or 2 texture layers per stage");
                    }
                    if (geometry != Geometry.GOURD && layers.size() != 1) {
                        throw new IllegalArgumentException("Second layer only supported for gourd geometry");
                    }
                    stages.add(List.copyOf(layers));
                }
                if (next.putIfAbsent(plantId, new Visual(geometry, stages)) != null) {
                    LOG.warn("Duplicate plant visuals ID {} from {}", plantId, source.getKey());
                }
            } catch (RuntimeException ex) {
                LOG.warn("Skipping invalid plant visuals {}: {}", source.getKey(), ex.getMessage());
            }
        });
        visuals = Map.copyOf(next);
        LOG.info("Loaded {} plant visual definitions", visuals.size());
    }

    private static ResourceLocation parseTexture(String id) {
        ResourceLocation parsed = ResourceLocation.tryParse(id);
        if (parsed == null || !(parsed.getPath().startsWith("block/")
                || parsed.getPath().startsWith("plant/"))) {
            throw new IllegalArgumentException("Expected namespaced block/ or plant/ texture: " + id);
        }
        return parsed;
    }
}
