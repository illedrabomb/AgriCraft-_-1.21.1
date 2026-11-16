package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import java.io.Reader;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Server datapack settings at data/cropgenetics/cropgenetics/soil_dynamics.json.
 * A missing file restores documented defaults on reload.
 */
public final class SoilDynamicsReloadListener
        extends SimplePreparableReloadListener<SoilDynamicsSettings> {
    private static final Logger LOG = LogUtils.getLogger();
    private static final ResourceLocation FILE =
            ResourceLocation.fromNamespaceAndPath("cropgenetics", "cropgenetics/soil_dynamics.json");
    private final Consumer<SoilDynamicsSettings> publish;

    public SoilDynamicsReloadListener(Consumer<SoilDynamicsSettings> publish) {
        this.publish = java.util.Objects.requireNonNull(publish);
    }

    @Override
    protected SoilDynamicsSettings prepare(ResourceManager manager, ProfilerFiller profiler) {
        var resource = manager.getResource(FILE);
        if (resource.isEmpty()) return SoilDynamicsSettings.DEFAULT;
        try (Reader reader = resource.get().openAsReader()) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            if (!Set.of("rain_tick_chance", "growth_nutrient_chance").containsAll(root.keySet()))
                throw new IllegalArgumentException("Unknown soil dynamics setting");
            int rain = readInt(root, "rain_tick_chance", SoilDynamicsSettings.DEFAULT.rainTickChance());
            int nutrient = readInt(root, "growth_nutrient_chance",
                    SoilDynamicsSettings.DEFAULT.growthNutrientChance());
            return new SoilDynamicsSettings(rain, nutrient);
        } catch (Exception ex) {
            LOG.warn("Invalid soil dynamics datapack {}: {}; using documented defaults",
                    FILE, ex.getMessage());
            return SoilDynamicsSettings.DEFAULT;
        }
    }

    private static int readInt(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        var value = json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("0|[1-9][0-9]*"))
            throw new IllegalArgumentException(key + " must be an integer 0..100000");
        return Integer.parseInt(value.getAsString());
    }

    @Override
    protected void apply(SoilDynamicsSettings settings, ResourceManager manager,
                         ProfilerFiller profiler) {
        publish.accept(settings);
        LOG.info("Soil dynamics: rain=1/{}, growth nutrient depletion=1/{} (0=disabled)",
                settings.rainTickChance(), settings.growthNutrientChance());
    }
}
