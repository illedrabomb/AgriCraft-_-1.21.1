package dev.illedrabomb.cropgenetics.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

/** Normalizes keyboard input for journal key rebinding, including scan-code-only keys. */
public final class JournalKeyInput {
    private JournalKeyInput() {}

    public static InputConstants.Key resolve(int keyCode, int scanCode) {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN && scanCode <= 0) {
            return InputConstants.UNKNOWN;
        }
        return InputConstants.getKey(keyCode, scanCode);
    }
}
