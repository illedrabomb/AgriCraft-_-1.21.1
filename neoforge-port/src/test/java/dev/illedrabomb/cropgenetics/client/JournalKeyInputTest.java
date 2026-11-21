package dev.illedrabomb.cropgenetics.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.junit.jupiter.api.Test;
import org.lwjgl.glfw.GLFW;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JournalKeyInputTest {
    @Test
    void normalKeysRetainTheirKeySymbol() {
        assertEquals(InputConstants.Type.KEYSYM,
                JournalKeyInput.resolve(GLFW.GLFW_KEY_F10, 0).getType());
    }

    @Test
    void unknownKeyWithScanCodeBecomesBindAble() {
        assertEquals(InputConstants.Type.SCANCODE,
                JournalKeyInput.resolve(GLFW.GLFW_KEY_UNKNOWN, 42).getType());
    }

    @Test
    void unknownKeyWithoutScanCodeRemainsUnbound() {
        assertEquals(InputConstants.UNKNOWN,
                JournalKeyInput.resolve(GLFW.GLFW_KEY_UNKNOWN, 0));
    }
}
