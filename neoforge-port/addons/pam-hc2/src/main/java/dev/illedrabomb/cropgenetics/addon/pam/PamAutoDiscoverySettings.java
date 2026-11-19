package dev.illedrabomb.cropgenetics.addon.pam;

import dev.illedrabomb.cropgenetics.data.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Optional server datapack policy for unknown Pam's HarvestCraft 2 seeds.
 * Original per-plant datapacks always take precedence over this fallback.
 */
public record PamAutoDiscoverySettings(boolean enabled, int maxPlants,
                                       int minLight, double growthChance,
                                       double growthBonus, int harvestMaximum,
                                       Set<String> excludedCrops,
                                       Map<String, ItemPair> itemPairs) {
    public static final PamAutoDiscoverySettings DEFAULT =
            new PamAutoDiscoverySettings(true, 512, 9, 0.75D, 0.025D, 5, Set.of(), Map.of());

    /** Keep previous settings constructor for callers that do not use custom pairs. */
    public PamAutoDiscoverySettings(boolean enabled, int maxPlants, int minLight,
                                    double growthChance, double growthBonus,
                                    int harvestMaximum, Set<String> excludedCrops) {
        this(enabled, maxPlants, minLight, growthChance, growthBonus,
                harvestMaximum, excludedCrops, Map.of());
    }

    /** Explicitly reviewed item identities, never inferred from crop names. */
    public record ItemPair(String seed, String produce) {
        public ItemPair {
            checkPamItem(seed);
            checkPamItem(produce);
        }
    }

    private static void checkPamItem(String id) {
        if (id == null || id.length() > 128
                || !id.matches("pamhc2crops:[a-z0-9_./-]+"))
            throw new IllegalArgumentException("Expected Pam's Crops item ID: " + id);
    }

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
        itemPairs = Map.copyOf(itemPairs);
        if (itemPairs.size() > 128 || itemPairs.keySet().stream().anyMatch(
                crop -> !crop.matches("[a-z][a-z0-9_]*") || crop.length() > 80))
            throw new IllegalArgumentException("item_pairs: up to 128 valid crop names");
        // If a seed or product is claimed by more than one configured
        // species, no unambiguous fallback can be inferred. Reject it.
        if (itemPairs.values().stream().map(ItemPair::seed).distinct().count() != itemPairs.size())
            throw new IllegalArgumentException("item_pairs contains duplicate seed IDs");
        if (itemPairs.values().stream().map(ItemPair::produce).distinct().count() != itemPairs.size())
            throw new IllegalArgumentException("item_pairs contains duplicate produce IDs");
        // A crop explicitly configured with seed == produce (for example
        // a verified self-plantable Pam food) is valid. An item shared by
        // DIFFERENT configured crops is still ambiguous and must fail closed.
        for (var first : itemPairs.entrySet()) {
            for (var second : itemPairs.entrySet()) {
                if (first.getKey().equals(second.getKey())) continue;
                if (first.getValue().seed().equals(second.getValue().produce())
                        || first.getValue().produce().equals(second.getValue().seed())) {
                    throw new IllegalArgumentException(
                            "item_pairs seed/produce IDs overlap across different crops");
                }
            }
        }
    }

    public static PamAutoDiscoverySettings parse(JsonObject json) {
        Set<String> allowed = Set.of("enabled", "max_plants", "min_light",
                "growth_chance", "growth_bonus", "harvest_maximum", "excluded_crops", "item_pairs");
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
        Map<String, ItemPair> pairs = new HashMap<>();
        if (json.has("item_pairs")) {
            JsonObject input = json.getAsJsonObject("item_pairs");
            if (input.size() > 128) throw new JsonParseException("Too many item_pairs");
            for (String name : input.keySet()) {
                JsonObject pair = input.getAsJsonObject(name);
                if (!pair.keySet().equals(Set.of("seed", "produce")))
                    throw new JsonParseException("item_pairs." + name
                            + " requires exactly seed and produce");
                pairs.put(name, new ItemPair(
                        pair.get("seed").getAsString(), pair.get("produce").getAsString()));
            }
        }
        return new PamAutoDiscoverySettings(
                bool(json, "enabled", DEFAULT.enabled()),
                integer(json, "max_plants", DEFAULT.maxPlants()),
                integer(json, "min_light", DEFAULT.minLight()),
                number(json, "growth_chance", DEFAULT.growthChance()),
                number(json, "growth_bonus", DEFAULT.growthBonus()),
                integer(json, "harvest_maximum", DEFAULT.harvestMaximum()),
                exclusions, pairs);
    }

    private static boolean bool(JsonObject json, String key, boolean fallback) {
        if (!json.has(key)) return fallback;
        var value = json.get(key);
        if (!value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
            throw new JsonParseException(key + " must be boolean");
        return value.getAsBoolean();
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
