package dev.illedrabomb.cropgenetics.content;

/** Pure gain-to-yield rules for one visible melon or pumpkin fruit. */
public final class GourdHarvestRules {
    public static final int MAX_DROP = 64;

    private GourdHarvestRules() {}

    /** Gain 1 -> 0% chance of two pumpkins; Gain 10 -> 90%. */
    public static double secondPumpkinChance(int gain) {
        return (Math.clamp(gain, 1, 10) - 1) / 10.0D;
    }

    /** Vanilla-style 3..7 slices plus one per additional Gain level. */
    public static int melonSlices(int gain, int baseRoll) {
        if (baseRoll < 0 || baseRoll > 4)
            throw new IllegalArgumentException("Melon base roll must be 0..4");
        return Math.min(MAX_DROP, 3 + baseRoll + Math.clamp(gain, 1, 10) - 1);
    }
}
