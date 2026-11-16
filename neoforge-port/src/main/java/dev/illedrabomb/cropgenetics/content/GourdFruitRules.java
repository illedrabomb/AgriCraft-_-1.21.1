package dev.illedrabomb.cropgenetics.content;

/**
 * Pure probability policy shared by physical fruit growth and diagnostics.
 * One random tick is a single chance to attempt placement, not a guaranteed fruit.
 */
public final class GourdFruitRules {
    private GourdFruitRules() {}

    /**
     * Keep the AgriCraft-inspired gain bonus and soil/environment multiplier.
     * Bad/nonfinite factors never produce a positive fruit probability.
     */
    public static double chance(double baseChance, int expressedGain, double growthFactor) {
        if (!Double.isFinite(baseChance) || !Double.isFinite(growthFactor)
                || baseChance <= 0D || growthFactor <= 0D) return 0D;
        return Math.max(0D, Math.min(1D,
                baseChance * (1D + (expressedGain - 1) / 10.0D) * growthFactor));
    }
}
