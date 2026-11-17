package dev.illedrabomb.cropgenetics.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Source-only model checks for the reported two-plates-versus-four-rods issue. */
class CropStickGeometryRegressionTest {
    private static JsonArray elements(String name) throws Exception {
        String path = "assets/cropgenetics/models/block/" + name + ".json";
        try (var stream = CropStickGeometryRegressionTest.class.getClassLoader()
                .getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonArray("elements");
        }
    }

    @Test
    void normalCropSticksHaveExactlyFourDistinctCornerRods() throws Exception {
        assertCornerRods(elements("crop_sticks"), 4);
    }

    @Test
    void crossedCropSticksRetainAllFourCornerRods() throws Exception {
        assertCornerRods(elements("crop_sticks_cross"), 8);
    }

    private static void assertCornerRods(JsonArray elements, int expectedCount) {
        assertEquals(expectedCount, elements.size());
        Set<String> foundCorners = new HashSet<>();
        for (int i = 0; i < 4; i++) {
            JsonObject rod = elements.get(i).getAsJsonObject();
            var from = rod.getAsJsonArray("from");
            var to = rod.getAsJsonArray("to");
            int x = from.get(0).getAsInt();
            int z = from.get(2).getAsInt();
            assertTrue(x == 3 || x == 12);
            assertTrue(z == 3 || z == 12);
            assertEquals(0, from.get(1).getAsInt());
            assertEquals(10, to.get(1).getAsInt());
            assertEquals(x + 1, to.get(0).getAsInt());
            assertEquals(z + 1, to.get(2).getAsInt());
            foundCorners.add(x + "," + z);
        }
        assertEquals(Set.of("3,3", "12,3", "3,12", "12,12"), foundCorners);
    }
}
