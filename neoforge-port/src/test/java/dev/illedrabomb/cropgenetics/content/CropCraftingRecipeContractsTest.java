package dev.illedrabomb.cropgenetics.content;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** Locks in agreed survival recipes without booting a Minecraft client. */
class CropCraftingRecipeContractsTest {
    private static JsonObject recipe(String name) throws Exception {
        String path = "data/cropgenetics/recipe/" + name + ".json";
        try (InputStream input = CropCraftingRecipeContractsTest.class
                .getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing recipe resource: " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    /** Resolves shaped pattern symbols into individual item counts. */
    private static Map<String, Integer> shapedIngredients(JsonObject recipe) {
        JsonObject key = recipe.getAsJsonObject("key");
        JsonArray pattern = recipe.getAsJsonArray("pattern");
        Map<String, Integer> counts = new HashMap<>();
        for (var row : pattern) {
            String symbols = row.getAsString();
            for (int i = 0; i < symbols.length(); i++) {
                String symbol = String.valueOf(symbols.charAt(i));
                if (" ".equals(symbol)) continue;
                assertTrue(key.has(symbol), "Undefined recipe symbol: " + symbol);
                JsonObject ingredient = key.getAsJsonObject(symbol);
                String id = ingredient.has("item")
                        ? ingredient.get("item").getAsString()
                        : "#" + ingredient.get("tag").getAsString();
                counts.merge(id, 1, Integer::sum);
            }
        }
        return counts;
    }

    private static void assertOutput(JsonObject recipe, String id, int count) {
        assertEquals(id, recipe.getAsJsonObject("result").get("id").getAsString());
        assertEquals(count, recipe.getAsJsonObject("result").get("count").getAsInt());
    }

    @Test
    void analyzerCostsExactlySixCropSticks() throws Exception {
        JsonObject analyzer = recipe("seed_analyzer");
        assertEquals("minecraft:crafting_shaped", analyzer.get("type").getAsString());
        assertEquals(Map.of("cropgenetics:crop_sticks", 6), shapedIngredients(analyzer));
        assertOutput(analyzer, "cropgenetics:seed_analyzer", 1);
    }

    @Test
    void seedChestUsesCropSticksInsteadOfWoodPlanks() throws Exception {
        JsonObject chest = recipe("seed_chest");
        assertEquals(Map.of("cropgenetics:crop_sticks", 8, "minecraft:chest", 1),
                shapedIngredients(chest));
        assertOutput(chest, "cropgenetics:seed_chest", 1);
    }

    @Test
    void cropStickCraftingYieldsSix() throws Exception {
        JsonObject sticks = recipe("crop_sticks");
        assertEquals(Map.of("minecraft:stick", 4), shapedIngredients(sticks));
        assertOutput(sticks, "cropgenetics:crop_sticks", 6);
    }

    @Test
    void journalUsesOneBookAndOneCropStick() throws Exception {
        JsonObject journal = recipe("seed_journal");
        assertEquals("minecraft:crafting_shapeless", journal.get("type").getAsString());
        Map<String, Integer> ingredientCounts = new HashMap<>();
        for (var ingredient : journal.getAsJsonArray("ingredients")) {
            String item = ingredient.getAsJsonObject().get("item").getAsString();
            ingredientCounts.merge(item, 1, Integer::sum);
        }
        assertEquals(Map.of("minecraft:book", 1, "cropgenetics:crop_sticks", 1),
                ingredientCounts);
        assertOutput(journal, "cropgenetics:seed_journal", 1);
    }
}
