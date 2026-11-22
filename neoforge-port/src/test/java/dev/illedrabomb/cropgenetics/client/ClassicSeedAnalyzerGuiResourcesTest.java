package dev.illedrabomb.cropgenetics.client;

import org.junit.jupiter.api.Test;
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
}
