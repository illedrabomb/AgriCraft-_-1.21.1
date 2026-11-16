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
import java.util.HashSet;
import java.util.Set;
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
            if (stages == null || stages.isEmpty() || stages.size() > 64
                    || stages.stream().anyMatch(layers -> layers == null
                            || layers.isEmpty() || layers.size() > 2)) {
                throw new IllegalArgumentException("Expected 1..64 visual stages with 1..2 layers");
            }
            stages = stages.stream().map(List::copyOf).toList();
        }

        public List<ResourceLocation> at(int stage, int totalStages) {
            // BlockState.STAGE is a normalized 0..7 visual scale. Clamp
            // malformed/out-of-range values before mapping onto the
            // resource-pack-specific number of texture frames.
            int last = stages.size() - 1;
            int maximum = Math.max(0, totalStages - 1);
            int clampedStage = Math.max(0, Math.min(stage, maximum));
            int index = totalStages <= 1 ? last
                    : (int) ((long) clampedStage * last / maximum);
            return stages.get(index);
        }
    }

    /** One client resource generation: visuals and warnings never diverge. */
    public record Snapshot(Map<String, Visual> visuals,
                           Map<String, List<ResourceLocation>> missingTextureFiles) {
        public Snapshot {
            visuals = Map.copyOf(visuals);
            missingTextureFiles = Map.copyOf(missingTextureFiles);
        }
    }

    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of());

    private PlantVisuals() {
        super(new Gson(), "cropgenetics/plant_visuals");
    }

    public Visual visual(String plantId) {
        return snapshot.visuals().getOrDefault(plantId,
                new Visual(Geometry.CROSS, List.of(List.of(FALLBACK))));
    }

    /** Client resource lookup diagnostics for configured texture paths. */
    public Map<String, List<ResourceLocation>> missingTextureFiles() {
        return snapshot.missingTextureFiles();
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> sources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<String, Visual> next = new HashMap<>();
        Map<String, List<ResourceLocation>> missing = new HashMap<>();
        // Conflicting visual IDs should not depend on which resource sorts first.
        Set<String> ambiguous = new HashSet<>();
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
                if (ambiguous.contains(plantId)) {
                    LOG.warn("Ignoring visual {} from {}: species ID already ambiguous",
                            plantId, source.getKey());
                    return;
                }
                if (next.putIfAbsent(plantId, new Visual(geometry, stages)) != null) {
                    next.remove(plantId);
                    missing.remove(plantId);
                    ambiguous.add(plantId);
                    LOG.error("Conflicting visual species ID {} at {}: using fallback for this species",
                            plantId, source.getKey());
                } else {
                    // Inspect the original PNGs in this active client resource
                    // manager. Missing files are advisory: generated or
                    // atlas-only sprite sources may still render correctly.
                    Set<ResourceLocation> absent = new HashSet<>();
                    for (List<ResourceLocation> layers : stages) for (ResourceLocation sprite : layers) {
                        ResourceLocation png = ResourceLocation.fromNamespaceAndPath(
                                sprite.getNamespace(), "textures/" + sprite.getPath() + ".png");
                        if (resourceManager.getResource(png).isEmpty()) absent.add(png);
                    }
                    if (!absent.isEmpty()) {
                        List<ResourceLocation> ordered = absent.stream().sorted().toList();
                        missing.put(plantId, ordered);
                        LOG.warn("Plant visuals {} from {} reference {} PNG files not found in "
                                + "active client resources: {} (atlas-generated sprites may still work)",
                                plantId, source.getKey(), ordered.size(), ordered);
                    }
                }
            } catch (RuntimeException ex) {
                LOG.warn("Skipping invalid plant visuals {}: {}", source.getKey(), ex.getMessage());
            }
        });
        // One volatile assignment prevents mixed generations on resource reload.
        Snapshot updated = new Snapshot(next, missing);
        snapshot = updated;
        LOG.info("Loaded {} plant visual definitions; {} missing-PNG reports, {} ambiguous IDs",
                updated.visuals().size(), updated.missingTextureFiles().size(),
                ambiguous.size());
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
