package dev.illedrabomb.cropgenetics.compat.jei;

import net.minecraft.resources.ResourceLocation;
import javax.annotation.Nullable;

/**
 * Item registry candidates for JEI explanatory links only.
 * This must never be used to authorize planting on a server.
 */
final class OptionalSeedGuidePolicy {
    private OptionalSeedGuidePolicy() {}

    @Nullable
    static String produceForSeed(String seedId) {
        ResourceLocation key = ResourceLocation.tryParse(seedId);
        if (key == null) return null;
        if (key.getNamespace().equals("farmersdelight")) {
            return switch (key.getPath()) {
                case "cabbage_seeds" -> "farmersdelight:cabbage";
                case "tomato_seeds" -> "farmersdelight:tomato";
                // Onion itself is both a planting item and the crop.
                case "onion" -> "farmersdelight:onion";
                // Rice requires its own planting mechanics and is excluded.
                default -> null;
            };
        }
        if (!key.getNamespace().equals("pamhc2crops")
                || !key.getPath().endsWith("seeditem")) return null;
        String crop = key.getPath().substring(0,
                key.getPath().length() - "seeditem".length());
        if (!crop.matches("[a-z][a-z0-9_]*")) return null;
        return "pamhc2crops:" + crop + "item";
    }
}
