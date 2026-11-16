package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.HashSet;
import java.util.Set;

/**
 * Optional server datapack policy for unknown Pam's HarvestCraft 2 seeds.
 * Original per-plant datapacks always take precedence over this fallback.
 */
public record PamAutoDiscoverySettings(boolean enabled, int maxPlants,
                                       int minLight, double growthChance,
                                       double growthBonus, int harvestMaximum,
                                       Set<String> excludedCrops) {
    public static final PamAutoDiscoverySettings DEFAULT =
            new PamAutoDiscoverySettings(true, 512, 9, 0.75D, 0.025D, 5, Set.of());

    public PamAutoDiscoverySettings {
        if (maxPlants < 0 || maxPlants > 512)
            throw new IllegalArgumentException("max_plants must be 0..512");
        if (minLight < 0 || minLight > 15)
            throw new IllegalArgumentException("min_light must be 0..15");
        if (!Double.isFinite(growthChance) || growthChance < 0 || growthChance > 1)
            throw new IllegalArgumentException("growth_chance must be 0..1");
        if (!Double.isFinite(growthBonus) || growthBonus < 0 || growthBonus > 1)
            throw new IllegalArgumentException("growth_bonus must be 0..1");
        if (harvestMaximum < 1 || harvestMaximum > 64)
            throw new IllegalArgumentException("harvest_maximum must be 1..64");
        excludedCrops = Set.copyOf(excludedCrops);
        if (excludedCrops.size() > 512 || excludedCrops.stream().anyMatch(
                crop -> !crop.matches("[a-z][a-z0-9_]*") || crop.length() > 80))
            throw new IllegalArgumentException("excluded_crops: up to 512 valid crop names");
    }

    public static PamAutoDiscoverySettings parse(JsonObject json) {
        Set<String> allowed = Set.of("enabled", "max_plants", "min_light",
                "growth_chance", "growth_bonus", "harvest_maximum", "excluded_crops");
        if (!allowed.containsAll(json.keySet()))
            throw new JsonParseException("Unknown Pam's discovery setting");
        Set<String> exclusions = new HashSet<>();
        if (json.has("excluded_crops")) {
            var values = json.getAsJsonArray("excluded_crops");
            if (values.size() > 512) throw new JsonParseException("Too many excluded crops");
            values.forEach(element -> {
                String crop = element.getAsString();
                if (!exclusions.add(crop))
                    throw new JsonParseException("Duplicate excluded crop: " + crop);
            });
        }
        return new PamAutoDiscoverySettings(
                json.has("enabled") ? json.get("enabled").getAsBoolean() : DEFAULT.enabled(),
                integer(json, "max_plants", DEFAULT.maxPlants()),
                integer(json, "min_light", DEFAULT.minLight()),
                number(json, "growth_chance", DEFAULT.growthChance()),
                number(json, "growth_bonus", DEFAULT.growthBonus()),
                integer(json, "harvest_maximum", DEFAULT.harvestMaximum()),
                exclusions);
    }

    private static int integer(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        var value = json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()
                || !value.getAsString().matches("0|[1-9][0-9]*"))
            throw new JsonParseException(key + " must be a nonnegative integer");
        return Integer.parseInt(value.getAsString());
    }

    private static double number(JsonObject json, String key, double fallback) {
        if (!json.has(key)) return fallback;
        var value = json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
            throw new JsonParseException(key + " must be a finite number");
        return value.getAsDouble();
    }
}
