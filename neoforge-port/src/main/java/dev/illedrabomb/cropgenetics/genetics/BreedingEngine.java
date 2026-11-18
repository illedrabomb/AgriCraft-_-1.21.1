package dev.illedrabomb.cropgenetics.genetics;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Inherit and optionally mutate the three classic 1..10 crop stats. */
public final class BreedingEngine {
    private BreedingEngine() {}

    public static Genome combine(Genome first, Genome second, String childPlantId, RandomGenerator random) {
        return combine(first, second, childPlantId, random, 1.0D);
    }

    /** Compatibility overload for two parents. */
    public static Genome combine(Genome first, Genome second, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        return combine(List.of(first, second), childPlantId, random, mutationMultiplier);
    }

    /**
     * All 2..4 eligible parents contribute to the possible allele source
     * (one scalar per stat now). The caller decides the mutation probability
     * multiplier based on eligible parent count and server config.
     */
    public static Genome combine(List<Genome> parents, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        Objects.requireNonNull(parents);
        Objects.requireNonNull(random);
        if (parents.size() < 2 || parents.size() > 4 || parents.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Requires 2 to 4 non-null parents");
        if (!Double.isFinite(mutationMultiplier) || mutationMultiplier < 0D)
            throw new IllegalArgumentException("Invalid mutation multiplier");
        return new Genome(childPlantId,
                inherit(parents, Genome::growth, random, mutationMultiplier),
                inherit(parents, Genome::gain, random, mutationMultiplier),
                inherit(parents, Genome::strength, random, mutationMultiplier));
    }

    private static int inherit(List<Genome> parents, java.util.function.ToIntFunction<Genome> stat,
                               RandomGenerator random, double multiplier) {
        int inherited = stat.applyAsInt(parents.get(random.nextInt(parents.size())));
        // Baseline 10% mutation per stat for two parent crosses; three and four
        // mature same-species parents scale this chance up through the caller.
        if (multiplier > 0D && random.nextDouble() < Math.min(1D, multiplier / 10D)) {
            inherited += random.nextBoolean() ? 1 : -1;
        }
        return Math.max(Genome.MIN, Math.min(Genome.MAX, inherited));
    }
}
