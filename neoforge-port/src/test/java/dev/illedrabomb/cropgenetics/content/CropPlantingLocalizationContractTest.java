package dev.illedrabomb.cropgenetics.content;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Static localization contract for all server-authoritative planting denials. */
class CropPlantingLocalizationContractTest {
    private static final Set<String> REASONS = Set.of(
            "cross", "occupied", "modified", "unknown", "soil", "water",
            "mushroom_light", "cactus_space", "location", "environment",
            "season", "chemistry", "dark", "bright", "blocked");

    @Test
    void allPlantingDenialReasonsHaveGermanAndEnglishText() throws Exception {
        for (String locale : Set.of("de_de", "en_us")) {
            String path = "assets/cropgenetics/lang/" + locale + ".json";
            try (InputStream input = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(input, "Missing language resource: " + path);
                JsonObject entries = JsonParser.parseReader(
                        new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                for (String reason : REASONS) {
                    String key = "message.cropgenetics.planting." + reason;
                    assertTrue(entries.has(key), "Missing " + locale + " translation for " + key);
                    assertFalse(entries.get(key).getAsString().isBlank(),
                            "Empty " + locale + " translation for " + key);
                }
            }
        }
    }
}
