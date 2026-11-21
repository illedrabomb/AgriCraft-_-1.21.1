package dev.illedrabomb.cropgenetics.compat.jei;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Source-only language contract; it does not execute JEI or Minecraft. */
class PlantConditionsLocalizationContractTest {
    private static final Set<String> KEYS = Set.of(
            "title", "pending", "ready", "cache_limit", "plant", "soil_block", "soil_tag", "light", "tolerance",
            "sugar_cane", "cactus", "mushroom", "biomes_allowed", "biomes_blocked",
            "dimensions_allowed", "dimensions_blocked", "min_y", "max_y", "fluid",
            "seasons", "humidity", "acidity", "nutrients", "server_note");

    @Test
    void everyLiveJeiRuleHasGermanAndEnglishTranslation() throws Exception {
        for (String locale : Set.of("de_de", "en_us")) {
            String path = "assets/cropgenetics/lang/" + locale + ".json";
            try (var input = getClass().getClassLoader().getResourceAsStream(path)) {
                assertNotNull(input, "Missing language resource " + path);
                JsonObject translations = JsonParser.parseReader(
                        new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
                for (String key : KEYS) {
                    String fullKey = "jei.cropgenetics.conditions." + key;
                    assertTrue(translations.has(fullKey), "Missing " + fullKey + " in " + locale);
                    assertFalse(translations.get(fullKey).getAsString().isBlank());
                }
            }
        }
    }
}
