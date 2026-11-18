package dev.illedrabomb.cropgenetics.genetics;

import java.util.Objects;
import java.util.random.RandomGenerator;

/** Inherit and optionally mutate the three classic 1..10 crop stats. */
public final class BreedingEngine {
    private BreedingEngine() {}

    public static Genome combine(Genome first, Genome second, String childPlantId, RandomGenerator random) {
        return combine(first, second, childPlantId, random, 1.0D);
    }

    /**
     * One inherited value per stat, chosen from either parent; mutation may
     * increase or decrease it by one. Fertility and mutativity are no longer
     * heritable, hidden genes. Parents remain unchanged.
     */
    public static Genome combine(Genome first, Genome second, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        Objects.requireNonNull(random);
        if (!Double.isFinite(mutationMultiplier) || mutationMultiplier < 0D)
            throw new IllegalArgumentException("Invalid mutation multiplier");
        return new Genome(childPlantId,
                inherit(first.growth(), second.growth(), random, mutationMultiplier),
                inherit(first.gain(), second.gain(), random, mutationMultiplier),
                inherit(first.strength(), second.strength(), random, mutationMultiplier));
    }

    private static int inherit(int first, int second, RandomGenerator random, double multiplier) {
        int inherited = random.nextBoolean() ? first : second;
        // 10% baseline per stat at multiplier=1. Higher row-evolution values
        // increase the chance, without introducing invisible genetic traits.
        if (multiplier > 0D && random.nextDouble() < Math.min(1D, multiplier / 10D)) {
            inherited += random.nextBoolean() ? 1 : -1;
        }
        return Math.max(Genome.MIN, Math.min(Genome.MAX, inherited));
    }
}
