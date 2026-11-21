package dev.illedrabomb.cropgenetics.content;

/** Pure gain-to-yield rules for one visible melon or pumpkin fruit. */
public final class GourdHarvestRules {
    public static final int MAX_DROP = 64;

    private GourdHarvestRules() {}

    /** Interpolate exactly between the approved Gain-yield anchor points. */
    private static double between(int gain, int firstGain, double first,
                                  int lastGain, double last) {
        return first + (last - first) * (gain - firstGain) / (lastGain - firstGain);
    }

    /** Probability of at least two pumpkins: early Gains now matter more. */
    public static double secondPumpkinChance(int gain) {
        int g = Math.clamp(gain, 1, 10);
        if (g <= 2) return between(g, 1, 0.0D, 2, 0.19D);
        if (g <= 5) return between(g, 2, 0.19D, 5, 0.62D);
        if (g <= 8) return between(g, 5, 0.62D, 8, 0.86D);
        return between(g, 8, 0.86D, 10, 0.90D);
    }

    /** At least three pumpkins grow to 55% at Gain 10. */
    public static double thirdPumpkinChance(int gain) {
        int g = Math.clamp(gain, 1, 10);
        if (g <= 3) return 0.0D;
        if (g <= 5) return between(g, 3, 0.0D, 5, 0.16D);
        if (g <= 8) return between(g, 5, 0.16D, 8, 0.47D);
        return between(g, 8, 0.47D, 10, 0.55D);
    }

    /** Four pumpkins remain rare: 5% at Gain 8, 15% at Gain 10. */
    public static double fourthPumpkinChance(int gain) {
        int g = Math.clamp(gain, 1, 10);
        if (g <= 7) return 0.0D;
        if (g <= 8) return 0.05D;
        return between(g, 8, 0.05D, 10, 0.15D);
    }

    /** One uniform roll, mutually exclusive outcomes from one visible fruit. */
    public static int pumpkinCount(int gain, double roll) {
        if (!Double.isFinite(roll) || roll < 0.0D || roll >= 1.0D)
            throw new IllegalArgumentException("Pumpkin roll must be in [0, 1)");
        if (roll < fourthPumpkinChance(gain)) return 4;
        if (roll < thirdPumpkinChance(gain)) return 3;
        if (roll < secondPumpkinChance(gain)) return 2;
        return 1;
    }

    /** Vanilla-style 3..7 slices plus one per additional Gain level. */
    public static int melonSlices(int gain, int baseRoll) {
        if (baseRoll < 0 || baseRoll > 4)
            throw new IllegalArgumentException("Melon base roll must be 0..4");
        return Math.min(MAX_DROP, 3 + baseRoll + Math.clamp(gain, 1, 10) - 1);
    }
}
