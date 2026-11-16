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
        // Legacy-only gameplay rules must not disappear merely because a
        // datapack was loaded directly instead of through the offline importer.
        for (String field : new String[]{"callbacks", "structure_generation",
                "particle_effects"}) {
            JsonArray legacyRules = json.getAsJsonArray(field);
            if (legacyRules != null && legacyRules.size() > 0)
                throw new JsonParseException(id + ": unsupported legacy " + field
                        + " requires a compatibility adapter");
        }
        JsonObject clips = json.getAsJsonObject("clip_products");
        if (clips != null && clips.has("products")
                && clips.getAsJsonArray("products").size() > 0)
            throw new JsonParseException(id + ": legacy clipping products require an adapter");
        if ((json.has("grass_drop_chance")
                && json.get("grass_drop_chance").getAsDouble() != 0D)
                || (json.has("seed_drop_chance")
                && json.get("seed_drop_chance").getAsDouble() != 1D)
                || (json.has("seed_drop_bonus")
                && json.get("seed_drop_bonus").getAsDouble() != 0D))
            throw new JsonParseException(id + ": nondefault legacy seed drops require an adapter");
        List<String> mods = strings(json.getAsJsonArray("mods"));
        List<String> seeds = new ArrayList<>();
        JsonArray seedItems = json.getAsJsonArray("seed_items");
        if (seedItems != null && seedItems.size() > 64)
            throw new JsonParseException(id + ": too many seed items (max 64)");
        if (seedItems != null) for (JsonElement element : seedItems) {
            JsonObject item = element.getAsJsonObject();
            if (!"item".equals(optionalString(item, "type", "item")) ||
                    item.has("useTag") && item.get("useTag").getAsBoolean())
                throw new JsonParseException(id + ": tag/non-item seeds need a crop adapter");
            if (item.has("nbt") && !item.get("nbt").getAsString().isEmpty())
                throw new JsonParseException(id + ": custom seed NBT requires an adapter");
            if (item.has("overridePlanting") && item.get("overridePlanting").getAsBoolean())
                throw new JsonParseException(id + ": legacy seed planting override requires an adapter");
            seeds.add(requiredString(item, "object"));
        }
        List<Integer> stages = new ArrayList<>();
        JsonArray stageData = json.getAsJsonArray("stages");
        if (stageData == null) throw new JsonParseException(id + ": stages missing");
        if (stageData.size() < 1 || stageData.size() > 64)
            throw new JsonParseException(id + ": expected 1..64 growth stages");
        for (JsonElement element : stageData) stages.add(element.getAsInt());
        if (stages.stream().anyMatch(x -> x < 0)) throw new JsonParseException(id + ": negative stage");
        List<PlantDefinition.HarvestProduct> products = new ArrayList<>();
        JsonObject productList = json.getAsJsonObject("products");
        if (productList != null && productList.getAsJsonArray("products") != null) {
            if (productList.getAsJsonArray("products").size() > 64)
                throw new JsonParseException(id + ": too many harvest products (max 64)");
            for (JsonElement element : productList.getAsJsonArray("products")) {
                JsonObject entry = element.getAsJsonObject();
                if (!"item".equals(optionalString(entry, "type", "item")) ||
                        entry.has("useTag") && entry.get("useTag").getAsBoolean())
                    throw new JsonParseException(id + ": non-item product needs a crop adapter");
                if (entry.has("nbt") && !entry.get("nbt").getAsString().isEmpty())
                    throw new JsonParseException(id + ": custom harvest NBT requires an adapter");
                if (entry.has("required") && !entry.get("required").getAsBoolean())
                    throw new JsonParseException(id + ": optional harvest product requires an adapter");
                products.add(new PlantDefinition.HarvestProduct(requiredString(entry, "object"),
                        entry.get("min").getAsInt(), entry.get("max").getAsInt(),
                        entry.get("chance").getAsDouble()));
            }
        }
        return new PlantDefinition(id, mods, seeds, stages, json.get("harvestStage").getAsInt(),
                json.get("growth_chance").getAsDouble(), json.get("growth_bonus").getAsDouble(),
                json.get("cloneable").getAsBoolean(), json.get("spread_chance").getAsDouble(),
                products, requirements(json),
                HarvestMode.parse(optionalString(json, "cropgenetics_harvest_mode", "standard")),
                json.has("cropgenetics_fruit_chance")
                        ? json.get("cropgenetics_fruit_chance").getAsDouble() : 0.0D,
                json.has("cropgenetics_contact_thorns")
                        && json.get("cropgenetics_contact_thorns").getAsBoolean());
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
                    && !key.equals("soil_block") && !key.equals("soil_tag")
                    && !key.equals("light_tolerance")
                    && !key.equals("allowed_biomes") && !key.equals("blocked_biomes")
                    && !key.equals("allowed_dimensions") && !key.equals("blocked_dimensions")
                    && !key.equals("min_y") && !key.equals("max_y")
                    && !key.equals("fluid_id")) {
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
        int tolerance = input.has("light_tolerance") ? input.get("light_tolerance").getAsInt() : 0;
        return new PlantRequirements(minLight, maxLight, soilBlock, soilTag, tolerance,
                strings(input.getAsJsonArray("allowed_biomes")),
                strings(input.getAsJsonArray("blocked_biomes")),
                strings(input.getAsJsonArray("allowed_dimensions")),
                strings(input.getAsJsonArray("blocked_dimensions")),
                environment(input, ""));
    }

    public static MutationDefinition mutation(JsonObject json) {
        if (json.has("conditions") && json.getAsJsonArray("conditions").size() > 0)
            throw new JsonParseException("Conditional mutations need a dedicated adapter");
        return new MutationDefinition(requiredString(json, "child"),
                requiredString(json, "parent1"), requiredString(json, "parent2"),
                json.get("chance").getAsDouble(),
                strings(json.getAsJsonArray("cropgenetics_allowed_biomes")),
                strings(json.getAsJsonArray("cropgenetics_blocked_biomes")),
                strings(json.getAsJsonArray("cropgenetics_allowed_dimensions")),
                strings(json.getAsJsonArray("cropgenetics_blocked_dimensions")),
                environment(json, "cropgenetics_"));
    }

    /** Parse only explicit 1.21.1 environmental filters, never legacy fluid objects. */
    private static EnvironmentFilter environment(JsonObject input, String prefix) {
        String min = prefix + "min_y";
        String max = prefix + "max_y";
        String fluid = prefix + "fluid_id";
        return new EnvironmentFilter(
                input.has(min) ? Optional.of(input.get(min).getAsInt()) : Optional.empty(),
                input.has(max) ? Optional.of(input.get(max).getAsInt()) : Optional.empty(),
                input.has(fluid) ? Optional.of(input.get(fluid).getAsString()) : Optional.empty());
    }

    public static boolean enabled(JsonObject json) {
        return !json.has("enabled") || json.get("enabled").getAsBoolean();
    }

    private static List<String> strings(JsonArray array) {
        if (array == null) return List.of();
        if (array.size() > 64) throw new JsonParseException("Too many required mods (max 64)");
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
