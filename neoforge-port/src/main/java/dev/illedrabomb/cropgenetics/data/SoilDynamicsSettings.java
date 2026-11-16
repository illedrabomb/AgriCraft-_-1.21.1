package dev.illedrabomb.cropgenetics.data;

/**
 * Integer denominators for random-tick soil changes.
 * 0 disables the corresponding mechanism; 1 means every eligible event.
 * These are port-specific defaults, not original AgriCore probabilities.
 */
public record SoilDynamicsSettings(int rainTickChance, int growthNutrientChance) {
    public static final SoilDynamicsSettings DEFAULT = new SoilDynamicsSettings(16, 8);
    private static final int MAX_DENOMINATOR = 100_000;

    public SoilDynamicsSettings {
        if (rainTickChance < 0 || rainTickChance > MAX_DENOMINATOR
                || growthNutrientChance < 0 || growthNutrientChance > MAX_DENOMINATOR)
            throw new IllegalArgumentException(
                    "Soil dynamics denominators must be 0..100000; 0 disables");
    }
}
