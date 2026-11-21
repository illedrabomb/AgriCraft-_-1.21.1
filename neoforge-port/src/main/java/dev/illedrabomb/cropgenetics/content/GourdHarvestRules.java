package dev.illedrabomb.cropgenetics.content;

/** Pure gain-to-yield rules for one visible melon or pumpkin fruit. */
public final class GourdHarvestRules {
    public static final int MAX_DROP = 64;

    private GourdHarvestRules() {}

    /** Probability of at least two pumpkins: Gain 1 -> 0%, Gain 10 -> 90%. */
    public static double secondPumpkinChance(int gain) {
        return (Math.clamp(gain, 1, 10) - 1) / 10.0D;
    }

    /** At least three pumpkins unlock from Gain 5 onward. */
    public static double thirdPumpkinChance(int gain) {
        return Math.max(0, Math.clamp(gain, 1, 10) - 4) / 10.0D;
    }

    /** Four pumpkins unlock from Gain 8 onward. */
    public static double fourthPumpkinChance(int gain) {
        return Math.max(0, Math.clamp(gain, 1, 10) - 7) / 10.0D;
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
