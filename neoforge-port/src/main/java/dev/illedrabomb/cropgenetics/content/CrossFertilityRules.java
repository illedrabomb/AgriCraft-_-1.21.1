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

    /** One success roll for the mature pair; no independent parent rolls. */
    static double pairFertilityChance(Genome first, Genome second) {
        return BASE_CHANCE;
    }
}
