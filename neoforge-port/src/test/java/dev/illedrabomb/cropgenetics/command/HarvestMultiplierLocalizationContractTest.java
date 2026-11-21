package dev.illedrabomb.cropgenetics.command;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarvestMultiplierLocalizationContractTest {
    private static final Set<String> KEYS = Set.of(
            "command.cropgenetics.harvestmultiplier.show",
            "command.cropgenetics.harvestmultiplier.temporary",
            "command.cropgenetics.harvestmultiplier.config",
            "command.cropgenetics.harvestmultiplier.set");

    @Test
    void adminMultiplierFeedbackIsLocalizedInEnglishAndGerman() throws Exception {
        for (String locale : Set.of("de_de", "en_us")) {
            String path = "assets/cropgenetics/lang/" + locale + ".json";
            try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(stream, "Missing language resource: " + path);
                JsonObject entries = JsonParser.parseReader(
                        new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
                for (String key : KEYS) {
                    assertTrue(entries.has(key), "Missing translation " + key + " in " + locale);
                    assertFalse(entries.get(key).getAsString().isBlank());
                }
                assertEquals(2, occurrences(entries.get(
                        "command.cropgenetics.harvestmultiplier.show").getAsString(), "%s"));
                assertEquals(1, occurrences(entries.get(
                        "command.cropgenetics.harvestmultiplier.set").getAsString(), "%s"));
            }
        }
    }

    private static int occurrences(String value, String fragment) {
        return (value.length() - value.replace(fragment, "").length()) / fragment.length();
    }
}
