package dev.illedrabomb.cropgenetics.genetics;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * Pure genetic combination layer for actual gameplay integration.
 * Species mutations and fertility-based parent selection are separate
 * responsibilities and are NOT implemented here yet.
 */
public final class BreedingEngine {
    private BreedingEngine() {}

    public static Genome combine(Genome first, Genome second, String childPlantId, RandomGenerator random) {
        return combine(first, second, childPlantId, random, 1.0D);
    }

    /**
     * Optional mutation tuning for a straight-row cross. The parents are never
     * changed, and the offspring always inherits its alleles from both.
     */
    public static Genome combine(Genome first, Genome second, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        Objects.requireNonNull(random);
        if (!Double.isFinite(mutationMultiplier) || mutationMultiplier < 0D)
            throw new IllegalArgumentException("Invalid mutation multiplier");
        return new Genome(childPlantId,
                inherit(first.growth(), second.growth(), first.mutativity(), second.mutativity(), random, mutationMultiplier),
                inherit(first.gain(), second.gain(), first.mutativity(), second.mutativity(), random, mutationMultiplier),
                inherit(first.strength(), second.strength(), first.mutativity(), second.mutativity(), random, mutationMultiplier),
                inherit(first.fertility(), second.fertility(), first.mutativity(), second.mutativity(), random, mutationMultiplier),
                inherit(first.mutativity(), second.mutativity(), first.mutativity(), second.mutativity(), random, mutationMultiplier));
    }

    private static GenePair inherit(GenePair a, GenePair b, GenePair ma, GenePair mb,
                                    RandomGenerator random, double mutationMultiplier) {
        int fromA = random.nextBoolean() ? a.dominant() : a.recessive();
        int fromB = random.nextBoolean() ? b.dominant() : b.recessive();
        // Mirrors the original AgriStatMutator with max=10 and expressed MAX.
        int mutationA = mutate(fromA, ma.expressed(GenePair.TraitMode.MAX), random, mutationMultiplier);
        int mutationB = mutate(fromB, mb.expressed(GenePair.TraitMode.MAX), random, mutationMultiplier);
        return new GenePair(mutationA, mutationB);
    }

    private static int mutate(int allele, int mutativity, RandomGenerator random,
                              double mutationMultiplier) {
        if (mutationMultiplier > 0D
                && random.nextDouble() < Math.min(1D, mutativity * mutationMultiplier / 10D)) {
            allele += random.nextInt(10) < (10 + mutativity) / 2 ? 1 : -1;
        }
        return Math.max(GenePair.MIN, Math.min(GenePair.MAX, allele));
    }
}
