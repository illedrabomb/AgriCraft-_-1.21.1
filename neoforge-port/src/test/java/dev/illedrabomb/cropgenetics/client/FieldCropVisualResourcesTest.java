package dev.illedrabomb.cropgenetics.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Guards the four-sided field crop visuals added after the November playtest.
 * Resource-only validation: no Minecraft client or renderer is initialized.
 */
class FieldCropVisualResourcesTest {
    @Test
    void wheatAndRootCropsUseEightFourSidedVisualFrames() throws Exception {
        for (String crop : List.of("wheat", "carrot", "potato", "beetroot")) {
            String path = "assets/cropgenetics/cropgenetics/plant_visuals/" + crop + ".json";
            try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(stream, "Missing plant visual: " + crop);
                JsonObject visual = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                assertEquals("hash", visual.get("geometry").getAsString(), crop);
                assertEquals(8, visual.getAsJsonArray("textures").size(), crop);
                for (var texture : visual.getAsJsonArray("textures")) {
                    assertTrue(texture.getAsString().startsWith("minecraft:block/"),
                            crop + ": expected a vanilla block atlas sprite");
                }
            }
        }
    }

    @Test
    void wheatUsesDistinctFirstAndLastFrames() throws Exception {
        String path = "assets/cropgenetics/cropgenetics/plant_visuals/wheat.json";
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream);
            JsonObject visual = JsonParser.parseReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            var textures = visual.getAsJsonArray("textures");
            assertEquals("minecraft:block/wheat_stage0", textures.get(0).getAsString());
            assertEquals("minecraft:block/wheat_stage7", textures.get(7).getAsString());
        }
    }
}
