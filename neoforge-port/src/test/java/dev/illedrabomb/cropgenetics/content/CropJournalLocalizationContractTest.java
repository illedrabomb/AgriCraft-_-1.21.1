package dev.illedrabomb.cropgenetics.content;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** The seed journal must never render untranslated dynamic guide page keys. */
class CropJournalLocalizationContractTest {
    private static JsonObject language(String locale) throws Exception {
        String path = "assets/cropgenetics/lang/" + locale + ".json";
        try (InputStream input = CropJournalLocalizationContractTest.class
                .getClassLoader().getResourceAsStream(path)) {
            assertNotNull(input, "Missing localization: " + path);
            return JsonParser.parseReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
        }
    }

    private static void assertNonEmpty(JsonObject entries, String key) {
        assertTrue(entries.has(key), "Missing translation: " + key);
        assertFalse(entries.get(key).getAsString().isBlank(), "Empty translation: " + key);
    }

    @Test
    void englishAndGermanHaveIdenticalTranslationKeys() throws Exception {
        JsonObject english = language("en_us");
        JsonObject german = language("de_de");
        assertEquals(english.keySet(), german.keySet(),
                "All player-visible messages should be localized equally");
    }

    @Test
    void everyDynamicJournalGuideLineExistsInBothLanguages() throws Exception {
        for (String locale : Set.of("en_us", "de_de")) {
            JsonObject entries = language(locale);
            for (int page = 0; page < 3; page++) {
                for (int line = 0; line < 6; line++) {
                    assertNonEmpty(entries, "screen.cropgenetics.guide_" + page + "_" + line);
                }
            }
            for (String suffix : Set.of("intro", "genetics", "crossing",
                    "journal_count", "journal_empty", "journal_back",
                    "journal_plant_detail", "journal_unlocked",
                    "journal_seed_values", "journal_analyzer_values",
                    "journal_mutations", "journal_no_mutations",
                    "journal_conditions", "journal_related_mutations")) {
                String key = suffix.startsWith("journal_")
                        ? "screen.cropgenetics." + suffix
                        : "screen.cropgenetics.journal_" + suffix;
                assertNonEmpty(entries, key);
            }
            assertNonEmpty(entries, "screen.cropgenetics.journal");
            assertNonEmpty(entries, "jei.cropgenetics.seed_conversion");
            assertNonEmpty(entries, "jei.cropgenetics.seed_recovery");
            assertNonEmpty(entries, "jei.cropgenetics.seed_analysis");
        }
    }
}
