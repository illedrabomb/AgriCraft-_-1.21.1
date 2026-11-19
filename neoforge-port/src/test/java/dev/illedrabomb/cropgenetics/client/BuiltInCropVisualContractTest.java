package dev.illedrabomb.cropgenetics.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.illedrabomb.cropgenetics.content.SeedSpeciesCatalog;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Static contracts for the built-in atlas visuals. No Minecraft runtime,
 * graphics context or resource reload is required.
 */
class BuiltInCropVisualContractTest {
    private static final String VISUAL_ROOT =
            "assets/cropgenetics/cropgenetics/plant_visuals/";

    private static final Map<String, String> VISUAL_FILES = Map.ofEntries(
            Map.entry("minecraft:wheat_plant", "wheat"),
            Map.entry("minecraft:carrot_plant", "carrot"),
            Map.entry("minecraft:potato_plant", "potato"),
            Map.entry("minecraft:beet_root_plant", "beetroot"),
            Map.entry("minecraft:nether_wart_plant", "nether_wart"),
            Map.entry("minecraft:melon_plant", "melon"),
            Map.entry("minecraft:pumpkin_plant", "pumpkin"),
            Map.entry("minecraft:cornflower_plant", "cornflower"),
            Map.entry("minecraft:poppy_plant", "poppy"),
            Map.entry("minecraft:dandelion_plant", "dandelion"),
            Map.entry("minecraft:cactus_plant", "cactus"),
            Map.entry("minecraft:sugar_cane_plant", "sugar_cane"),
            Map.entry("minecraft:bamboo_plant", "bamboo"),
            Map.entry("minecraft:sweet_berry_plant", "sweet_berry"),
            Map.entry("minecraft:brown_mushroom_plant", "brown_mushroom"),
            Map.entry("minecraft:red_mushroom_plant", "red_mushroom"));

    @Test
    void eachBuiltInSeedHasExactlyOneEightStageVisualDefinition() throws Exception {
        assertEquals(SeedSpeciesCatalog.DEFAULT_SPECIES.size(), VISUAL_FILES.size());
        for (String plantId : SeedSpeciesCatalog.DEFAULT_SPECIES) {
            String visualName = VISUAL_FILES.get(plantId);
            assertNotNull(visualName, "Missing visual mapping for " + plantId);
            JsonObject visual = read(visualName);
            assertEquals(plantId, visual.get("plant").getAsString(), visualName);
            String geometry = visual.has("geometry")
                    ? visual.get("geometry").getAsString() : "cross";
            assertTrue(List.of("cross", "plus", "hash", "gourd").contains(geometry),
                    "Unsupported geometry for " + visualName);
            JsonArray frames = visual.has("stages")
                    ? visual.getAsJsonArray("stages")
                    : visual.getAsJsonArray("textures");
            assertNotNull(frames, "No visual stages for " + visualName);
            assertEquals(8, frames.size(), "Unexpected visual frame count for " + visualName);
            for (int index = 0; index < frames.size(); index++) {
                assertTrue(frames.get(index).isJsonPrimitive(),
                        "Built-in visuals must use one atlas sprite per stage: "
                                + visualName + " frame " + index);
                String sprite = frames.get(index).getAsString();
                assertTrue(sprite.startsWith("minecraft:block/"),
                        "Invalid vanilla atlas sprite " + visualName + ": " + sprite);
            }
        }
    }

    @Test
    void gourdStemsNeverEmbedASecondVirtualFruitTexture() throws Exception {
        for (String fruit : List.of("melon", "pumpkin")) {
            JsonObject visual = read(fruit);
            assertEquals("gourd", visual.get("geometry").getAsString());
            JsonArray stages = visual.getAsJsonArray("stages");
            for (int i = 0; i < stages.size(); i++) {
                assertEquals("minecraft:block/" + fruit + "_stem",
                        stages.get(i).getAsString(),
                        "Fruit meshes are owned by the Crop Sticks renderer, not visual layer " + i);
            }
        }
    }

    @Test
    void seedlessPlantsStillHaveAVisibleGrowthSequence() throws Exception {
        for (String crop : List.of("cactus", "sugar_cane", "bamboo",
                "sweet_berry", "brown_mushroom", "red_mushroom")) {
            JsonObject visual = read(crop);
            JsonArray stages = visual.has("stages")
                    ? visual.getAsJsonArray("stages")
                    : visual.getAsJsonArray("textures");
            assertEquals(8, stages.size(), crop);
            assertFalse(stages.get(0).getAsString().isBlank());
            assertFalse(stages.get(7).getAsString().isBlank());
        }
    }

    private static JsonObject read(String visualName) throws Exception {
        String path = VISUAL_ROOT + visualName + ".json";
        try (var stream = BuiltInCropVisualContractTest.class
                .getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, "Missing built-in visual: " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }
}
