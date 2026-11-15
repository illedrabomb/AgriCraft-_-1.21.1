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
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Client resource-pack visuals (not server datapack definitions).
 *
 * assets/<namespace>/cropgenetics/plant_visuals/*.json:
 * { "plant": "minecraft:wheat_plant",
 *   "textures": ["minecraft:block/wheat_stage0", ...] }
 *
 * Each texture is an ordinary texture resource location WITHOUT
 * "textures/" and ".png". Never access the server's AgriPlants registry
 * from a dedicated client renderer.
 */
public final class PlantVisuals extends SimpleJsonResourceReloadListener {
    private static final Logger LOG = LogUtils.getLogger();
    public static final PlantVisuals INSTANCE = new PlantVisuals();

    private volatile Map<String, List<ResourceLocation>> visuals = Map.of();

    private PlantVisuals() {
        super(new Gson(), "cropgenetics/plant_visuals");
    }

    public ResourceLocation texture(String plantId, int stage, int totalStages) {
        List<ResourceLocation> stages = visuals.get(plantId);
        if (stages == null || stages.isEmpty()) {
            return ResourceLocation.withDefaultNamespace("block/oak_leaves");
        }
        int last = stages.size() - 1;
        int index = totalStages <= 1 ? last
                : Math.min(last, (int) ((long) Math.max(0, stage) * last / (totalStages - 1)));
        return stages.get(index);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> sources, ResourceManager resourceManager,
                         ProfilerFiller profiler) {
        Map<String, List<ResourceLocation>> next = new HashMap<>();
        for (Map.Entry<ResourceLocation, JsonElement> source : sources.entrySet()) {
            try {
                JsonObject json = source.getValue().getAsJsonObject();
                // Modern resource-pack format, or a mirrored 1.18.2 AgriPlants
                // entry with its original texture.plant_textures array.
                String plantId = json.has("plant") ? json.get("plant").getAsString()
                        : json.get("id").getAsString();
                if (ResourceLocation.tryParse(plantId) == null) {
                    throw new IllegalArgumentException("Invalid plant ID");
                }
                JsonObject legacyTexture = json.has("texture")
                        ? json.getAsJsonObject("texture") : null;
                if (legacyTexture != null && !"hash".equals(legacyTexture.get("render_type").getAsString())) {
                    throw new IllegalArgumentException("Only legacy render_type=hash can be approximated");
                }
                JsonArray entries = legacyTexture != null
                        ? legacyTexture.getAsJsonArray("plant_textures")
                        : json.getAsJsonArray("textures");
                if (entries == null || entries.isEmpty() || entries.size() > 64) {
                    throw new IllegalArgumentException("Expected 1..64 texture stages");
                }
                List<ResourceLocation> textures = new ArrayList<>();
                for (JsonElement value : entries) {
                    String id;
                    if (legacyTexture != null) {
                        JsonArray group = value.getAsJsonArray();
                        if (group.size() != 1) {
                            throw new IllegalArgumentException("Multi-layer textures require a dedicated renderer");
                        }
                        id = group.get(0).getAsString();
                    } else {
                        id = value.getAsString();
                    }
                    ResourceLocation parsed = ResourceLocation.tryParse(id);
                    if (parsed == null || !parsed.getPath().startsWith("block/")) {
                        throw new IllegalArgumentException("Expected texture ID namespace:block/name");
                    }
                    textures.add(parsed);
                }
                if (next.putIfAbsent(plantId, List.copyOf(textures)) != null) {
                    LOG.warn("Duplicate plant visuals ID {}, ignoring {}", plantId, source.getKey());
                }
            } catch (RuntimeException ex) {
                LOG.warn("Skipping invalid plant visuals {}: {}", source.getKey(), ex.getMessage());
            }
        }
        visuals = Map.copyOf(next);
        LOG.info("Loaded {} crop visual definitions", visuals.size());
    }
}
