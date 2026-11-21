package dev.illedrabomb.cropgenetics.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestSeedLocalizationContractTest {
    private static final Map<String, Integer> PLACEHOLDERS = Map.of(
            "command.cropgenetics.testseed.unknown_species", 1,
            "command.cropgenetics.testseed.inventory_full", 0,
            "command.cropgenetics.testseed.insert_failed", 0,
            "command.cropgenetics.testseed.received", 4);

    @Test
    void testSeedFeedbackHasCompleteLocalizedArguments() throws Exception {
        for (String locale : Set.of("de_de", "en_us")) {
            String path = "assets/cropgenetics/lang/" + locale + ".json";
            try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(stream, "Missing language resource: " + path);
                JsonObject translations = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                PLACEHOLDERS.forEach((key, count) -> {
                    assertTrue(translations.has(key), "Missing " + locale + ": " + key);
                    String value = translations.get(key).getAsString();
                    assertFalse(value.isBlank());
                    assertEquals(count, value.split("%s", -1).length - 1, key);
                });
            }
        }
    }
}
