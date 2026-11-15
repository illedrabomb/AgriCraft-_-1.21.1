package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Reads the supported core of original AgriPlants 1.18.2 JSON without its Forge runtime. */
public final class AgriPlantsParser {
    private AgriPlantsParser() {}

    public static PlantDefinition plant(JsonObject json) {
        String id = requiredString(json, "id");
        List<String> mods = strings(json.getAsJsonArray("mods"));
        List<String> seeds = new ArrayList<>();
        JsonArray seedItems = json.getAsJsonArray("seed_items");
        if (seedItems != null) for (JsonElement element : seedItems) {
            JsonObject item = element.getAsJsonObject();
            if (!"item".equals(optionalString(item, "type", "item")) ||
                    item.has("useTag") && item.get("useTag").getAsBoolean())
                throw new JsonParseException(id + ": tag/non-item seeds need a crop adapter");
            seeds.add(requiredString(item, "object"));
        }
        List<Integer> stages = new ArrayList<>();
        JsonArray stageData = json.getAsJsonArray("stages");
        if (stageData == null) throw new JsonParseException(id + ": stages missing");
        for (JsonElement element : stageData) stages.add(element.getAsInt());
        if (stages.stream().anyMatch(x -> x < 0)) throw new JsonParseException(id + ": negative stage");
        List<PlantDefinition.HarvestProduct> products = new ArrayList<>();
        JsonObject productList = json.getAsJsonObject("products");
        if (productList != null && productList.getAsJsonArray("products") != null) {
            for (JsonElement element : productList.getAsJsonArray("products")) {
                JsonObject entry = element.getAsJsonObject();
                if (!"item".equals(optionalString(entry, "type", "item")) ||
                        entry.has("useTag") && entry.get("useTag").getAsBoolean())
                    throw new JsonParseException(id + ": non-item product needs a crop adapter");
                products.add(new PlantDefinition.HarvestProduct(requiredString(entry, "object"),
                        entry.get("min").getAsInt(), entry.get("max").getAsInt(),
                        entry.get("chance").getAsDouble()));
            }
        }
        return new PlantDefinition(id, mods, seeds, stages, json.get("harvestStage").getAsInt(),
                json.get("growth_chance").getAsDouble(), json.get("growth_bonus").getAsDouble(),
                json.get("cloneable").getAsBoolean(), json.get("spread_chance").getAsDouble(),
                products, requirements(json));
    }

    private static PlantRequirements requirements(JsonObject json) {
        // The original 1.18.2 soil chemistry, seasons, biome and dimension rules
        // cannot yet be represented. Do not pretend they are satisfied.
        JsonObject legacy = json.getAsJsonObject("requirement");
        if (legacy != null) {
            for (String key : new String[]{"soil_humidity", "soil_acidity", "soil_nutrients",
                    "biomes", "dimensions", "seasons", "conditions", "fluid"}) {
                if (legacy.has(key)) {
                    throw new JsonParseException("Legacy requirement '" + key
                            + "' requires a migration/compatibility adapter");
                }
            }
        }
        JsonObject modern = json.getAsJsonObject("cropgenetics_requirements");
        if (modern != null && legacy != null) {
            throw new JsonParseException("Do not mix legacy and new crop requirements");
        }
        JsonObject input = modern == null ? legacy : modern;
        if (input == null) return PlantRequirements.defaultFarmland();
        for (String key : input.keySet()) {
            if (!key.equals("min_light") && !key.equals("max_light")
                    && !key.equals("soil_block") && !key.equals("soil_tag")) {
                throw new JsonParseException("Unsupported crop requirement: " + key);
            }
        }

        int minLight = input.has("min_light") ? input.get("min_light").getAsInt() : 9;
        int maxLight = input.has("max_light") ? input.get("max_light").getAsInt() : 15;
        Optional<String> soilBlock = input.has("soil_block")
                ? Optional.of(input.get("soil_block").getAsString()) : Optional.empty();
        Optional<String> soilTag = input.has("soil_tag")
                ? Optional.of(input.get("soil_tag").getAsString()) : Optional.empty();
        if (soilBlock.isEmpty() && soilTag.isEmpty()) {
            soilBlock = Optional.of("minecraft:farmland");
        }
        return new PlantRequirements(minLight, maxLight, soilBlock, soilTag);
    }

    public static MutationDefinition mutation(JsonObject json) {
        if (json.has("conditions") && json.getAsJsonArray("conditions").size() > 0)
            throw new JsonParseException("Conditional mutations need a dedicated adapter");
        return new MutationDefinition(requiredString(json, "child"),
                requiredString(json, "parent1"), requiredString(json, "parent2"),
                json.get("chance").getAsDouble());
    }

    public static boolean enabled(JsonObject json) {
        return !json.has("enabled") || json.get("enabled").getAsBoolean();
    }

    private static List<String> strings(JsonArray array) {
        if (array == null) return List.of();
        List<String> strings = new ArrayList<>();
        for (JsonElement element : array) strings.add(element.getAsString());
        return strings;
    }

    private static String optionalString(JsonObject json, String field, String fallback) {
        return json.has(field) ? json.get(field).getAsString() : fallback;
    }

    private static String requiredString(JsonObject json, String field) {
        if (!json.has(field)) throw new JsonParseException("Missing required field: " + field);
        return json.get(field).getAsString();
    }
}
