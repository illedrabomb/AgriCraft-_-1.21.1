package dev.illedrabomb.cropgenetics.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;

import static org.junit.jupiter.api.Assertions.*;

/** Original AgriCraft atlas must be carried into the NeoForge resources. */
class ClassicSeedAnalyzerGuiResourcesTest {
    @Test
    void originalAnalyzerTextureHasFullAtlasDimensions() throws Exception {
        try (var stream = getClass().getClassLoader().getResourceAsStream(
                "assets/cropgenetics/textures/gui/seed_analyzer_gui.png")) {
            assertNotNull(stream);
            var image = ImageIO.read(stream);
            assertNotNull(image);
            assertEquals(256, image.getWidth());
            assertEquals(256, image.getHeight());
        }
    }
    @Test
    void analyzerTextAndJeiStackingGuideMatchBothLanguages() throws Exception {
        JsonObject english = readLanguage("en_us");
        JsonObject german = readLanguage("de_de");
        for (String key : new String[] {"growth_short", "gain_short", "strength_short"}) {
            String translationKey = "screen.cropgenetics.analyzer." + key;
            assertTrue(english.has(translationKey), translationKey + " missing in en_us");
            assertTrue(german.has(translationKey), translationKey + " missing in de_de");
            assertTrue(english.get(translationKey).getAsString().contains("%s"));
            assertTrue(german.get(translationKey).getAsString().contains("%s"));
        }
        assertTrue(english.get("jei.cropgenetics.seed_analysis").getAsString()
                .contains("stack up to 64"));
        assertTrue(german.get("jei.cropgenetics.seed_analysis").getAsString()
                .contains("bis 64 stapelbar"));
    }

    private JsonObject readLanguage(String locale) throws Exception {
        String path = "assets/cropgenetics/lang/" + locale + ".json";
        try (var stream = getClass().getClassLoader().getResourceAsStream(path)) {
            assertNotNull(stream, path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        }
    }
}
