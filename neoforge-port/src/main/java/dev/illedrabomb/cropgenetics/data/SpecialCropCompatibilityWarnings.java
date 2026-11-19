package dev.illedrabomb.cropgenetics.data;

import javax.annotation.Nullable;

/**
 * Warn about known special crop mechanics not replicated by basic Crop Sticks.
 * Does not disable explicit third-party datapacks or introduce hard dependencies.
 */
final class SpecialCropCompatibilityWarnings {
    private SpecialCropCompatibilityWarnings() {}

    @Nullable
    static String forPlant(String speciesId) {
        if ("farmersdelight:rice_plant".equals(speciesId)) {
            return "Farmer's Delight rice is an explicit GENERIC Crop Sticks override only; "
                    + "its 1.21.1 native crop requires source water, a separate upper "
                    + "panicle block and tool-dependent harvests. Original rice parity "
                    + "is NOT implemented.";
        }
        return null;
    }
}
