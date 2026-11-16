package dev.illedrabomb.cropgenetics.client;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import javax.imageio.ImageIO;
import java.io.InputStreamReader;
import static org.junit.jupiter.api.Assertions.*;

class CropStickModelTest {
    @Test void woodFacesNeverSampleTransparentLegacySpritePixels() throws Exception {
        var loader = getClass().getClassLoader();
        var sprite = ImageIO.read(loader.getResourceAsStream(
                "assets/cropgenetics/textures/block/crop_sticks_wood.png"));
        for (String model : new String[]{"crop_sticks", "crop_sticks_cross"}) {
            try (var input = loader.getResourceAsStream("assets/cropgenetics/models/block/" + model + ".json")) {
                assertNotNull(input);
                var json = JsonParser.parseReader(new InputStreamReader(input)).getAsJsonObject();
                for (var element : json.getAsJsonArray("elements")) {
                    for (var entry : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
                        var face = entry.getValue().getAsJsonObject();
                        if (!face.get("texture").getAsString().equals("#wood")) continue;
                        assertTrue(face.has("uv"), model + ": implicit geometry UV can hit transparent sprite area");
                        var uv = face.getAsJsonArray("uv");
                        int x0 = uv.get(0).getAsInt(), y0 = uv.get(1).getAsInt();
                        int x1 = uv.get(2).getAsInt(), y1 = uv.get(3).getAsInt();
                        assertTrue(x0 >= 0 && y0 >= 0 && x1 <= 16 && y1 <= 16 && x1 > x0 && y1 > y0);
                        for (int y = y0; y < y1; y++) for (int x = x0; x < x1; x++) {
                            assertEquals(255, sprite.getRGB(x * sprite.getWidth() / 16,
                                    y * sprite.getHeight() / 16) >>> 24, model + ": transparent wood texel");
                        }
                    }
                }
            }
        }
    }
}
