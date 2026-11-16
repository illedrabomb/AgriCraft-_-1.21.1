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
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        Objects.requireNonNull(random);
        return new Genome(childPlantId,
                inherit(first.growth(), second.growth(), first.mutativity(), second.mutativity(), random),
                inherit(first.gain(), second.gain(), first.mutativity(), second.mutativity(), random),
                inherit(first.strength(), second.strength(), first.mutativity(), second.mutativity(), random),
                inherit(first.fertility(), second.fertility(), first.mutativity(), second.mutativity(), random),
                inherit(first.mutativity(), second.mutativity(), first.mutativity(), second.mutativity(), random));
    }

    private static GenePair inherit(GenePair a, GenePair b, GenePair ma, GenePair mb, RandomGenerator random) {
        int fromA = random.nextBoolean() ? a.dominant() : a.recessive();
        int fromB = random.nextBoolean() ? b.dominant() : b.recessive();
        // Mirrors the original AgriStatMutator with max=10 and expressed MAX.
        int mutationA = mutate(fromA, ma.expressed(GenePair.TraitMode.MAX), random);
        int mutationB = mutate(fromB, mb.expressed(GenePair.TraitMode.MAX), random);
        return new GenePair(mutationA, mutationB);
    }

    private static int mutate(int allele, int mutativity, RandomGenerator random) {
        if (random.nextInt(10) < mutativity) {
            allele += random.nextInt(10) < (10 + mutativity) / 2 ? 1 : -1;
        }
        return Math.max(GenePair.MIN, Math.min(GenePair.MAX, allele));
    }
}
