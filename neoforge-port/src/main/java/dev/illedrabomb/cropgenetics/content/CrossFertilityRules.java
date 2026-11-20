package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;

/** Fertility is a transparent gameplay probability, not a hidden heritable stat. */
final class CrossFertilityRules {
    private CrossFertilityRules() {}

    // Three mature-parent crop stats no longer include inherited Fertility.
    // Players can adjust overall breeding speed in the SERVER config.
    private static final double BASE_CHANCE = 0.30D;
    static final int MAX_ROW_LENGTH = 20;

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

    /**
     * Three contiguous mature parents start at 15% fertility; a full row of
     * twenty reaches 30% at default config, still below four direct parents.
     * Genes always come from at most four parents, regardless of row length.
     */
    static double rowFertilityMultiplier(int rowLength) {
        if (rowLength < 3) throw new IllegalArgumentException("Row requires at least three parents");
        int cappedLength = Math.min(MAX_ROW_LENGTH, rowLength);
        return 0.50D + 0.50D * (cappedLength - 3) / (MAX_ROW_LENGTH - 3);
    }

    /** A row also has less chance to improve visible traits. */
    static double rowMutationMultiplier() { return 0.50D; }

    /** One success roll for the mature pair; no independent parent rolls. */
    static double pairFertilityChance(Genome first, Genome second) {
        return BASE_CHANCE;
    }
}
