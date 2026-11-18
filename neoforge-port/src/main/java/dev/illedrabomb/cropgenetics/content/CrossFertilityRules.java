package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;

/** Fertility is a transparent gameplay probability, not a hidden heritable stat. */
final class CrossFertilityRules {
    private CrossFertilityRules() {}

    // Three mature-parent crop stats no longer include inherited Fertility.
    // Players can adjust overall breeding speed in the SERVER config.
    private static final double BASE_CHANCE = 0.30D;

    static double fertilityChance(Genome genome) {
        return BASE_CHANCE;
    }

    /** A two-parent row has the lowest bonus; more neighbors increase success. */
    static double parentCountMultiplier(int parentCount) {
        if (parentCount < 2 || parentCount > 4)
            throw new IllegalArgumentException("Expected 2..4 parents");
        return 1.0D + (parentCount - 2) * 0.25D;
    }

    /** Extra parents also increase the probability of visible stat mutation. */
    static int mutationMultiplier(int parentCount) {
        if (parentCount < 2 || parentCount > 4)
            throw new IllegalArgumentException("Expected 2..4 parents");
        return parentCount - 1;
    }

    /** An extended straight row is deliberately weaker than any adjacent pair. */
    static double rowFertilityMultiplier() { return 0.50D; }

    /** A row also has less chance to improve visible traits. */
    static double rowMutationMultiplier() { return 0.50D; }

    /** One success roll for the mature pair; no independent parent rolls. */
    static double pairFertilityChance(Genome first, Genome second) {
        return BASE_CHANCE;
    }
}
