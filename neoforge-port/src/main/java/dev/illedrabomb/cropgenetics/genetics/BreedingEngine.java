package dev.illedrabomb.cropgenetics.genetics;

import java.util.List;
import java.util.Objects;
import java.util.random.RandomGenerator;

/** Inherit a complete parental genome and mutate the classic three visible stats. */
public final class BreedingEngine {
    private BreedingEngine() {}

    private static final int[][] POINT_WEIGHTS = {
            {35, 35, 20, 8, 2},
            {20, 30, 28, 16, 6},
            {10, 20, 30, 25, 15}
    };

    public static Genome combine(Genome first, Genome second, String childPlantId, RandomGenerator random) {
        return combine(first, second, childPlantId, random, 1.0D);
    }

    /** Compatibility overload for two parents. */
    public static Genome combine(Genome first, Genome second, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        return combine(List.of(first, second), childPlantId, random, mutationMultiplier);
    }

    /**
     * The caller's baseline mutation multiplier is 1/2/3 for 2/3/4 directly
     * adjacent parents. The point table already accounts for parent count,
     * so normalize the multiplier to avoid doubling this bonus. Rows retain
     * their weaker relative factor; zero disables mutation, not inheritance.
     */
    public static Genome combine(List<Genome> parents, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier) {
        return combine(parents, childPlantId, random, mutationMultiplier, false);
    }

    /** An extended straight row uses the weaker two-parent point distribution. */
    public static Genome combine(List<Genome> parents, String childPlantId,
                                 RandomGenerator random, double mutationMultiplier, boolean straightRow) {
        Objects.requireNonNull(parents);
        Objects.requireNonNull(random);
        if (parents.size() < 2 || parents.size() > 4 || parents.stream().anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Requires 2 to 4 non-null parents");
        if (!Double.isFinite(mutationMultiplier) || mutationMultiplier < 0D)
            throw new IllegalArgumentException("Invalid mutation multiplier");
        Genome template = chooseTemplate(parents, random);
        if (mutationMultiplier == 0D) return template.withPlantId(childPlantId);

        double scale = mutationMultiplier / (parents.size() - 1);
        int[] values = {template.growth(), template.gain(), template.strength()};
        int returnedPoints = 0;
        double lossChance = Math.min(1D, 0.025D * (11 - template.strength()) * scale);

        // Loss occurs before bonuses; only actually lost points can return.
        for (int stat = 0; stat < values.length; stat++) {
            if (random.nextDouble() >= lossChance) continue;
            int lossRoll = random.nextInt(100);
            int desiredLoss = lossRoll < 70 ? 1 : lossRoll < 95 ? 2 : 3;
            int loss = Math.min(desiredLoss, values[stat] - Genome.MIN);
            values[stat] -= loss;
            for (int point = 0; point < loss; point++) {
                if (random.nextDouble() < 0.40D) returnedPoints++;
            }
        }

        int points = rollBasePoints(random, straightRow ? 2 : parents.size());
        // At scale one there are no extra rolls and the table alone chooses
        // whether zero points are awarded. Config reductions thin the pool;
        // config increases can add points, never beyond four base points.
        if (scale < 1D) {
            int kept = 0;
            for (int i = 0; i < points; i++) if (random.nextDouble() < scale) kept++;
            points = kept;
        } else if (scale > 1D) {
            int extra = 0;
            for (int i = 0; i < points; i++)
                if (random.nextDouble() < Math.min(1D, scale - 1D)) extra++;
            points = Math.min(4, points + extra);
        }
        points += returnedPoints;

        int[] gained = new int[3];
        for (int point = 0; point < points; point++) {
            int stat = random.nextInt(3);
            if (gained[stat] >= 3) continue; // excess disappears, not redistributed
            gained[stat]++;
            values[stat] = Math.min(Genome.MAX, values[stat] + 1);
        }
        return new Genome(childPlantId, values[0], values[1], values[2]);
    }

    private static Genome chooseTemplate(List<Genome> parents, RandomGenerator random) {
        int total = 0;
        for (Genome parent : parents) total += parent.strength();
        int ticket = random.nextInt(total);
        for (Genome parent : parents) {
            ticket -= parent.strength();
            if (ticket < 0) return parent;
        }
        throw new IllegalStateException("Positive parental strength weights expected");
    }

    private static int rollBasePoints(RandomGenerator random, int parentCount) {
        int ticket = random.nextInt(100);
        int[] weights = POINT_WEIGHTS[parentCount - 2];
        for (int points = 0; points < weights.length; points++) {
            ticket -= weights[points];
            if (ticket < 0) return points;
        }
        throw new IllegalStateException("Point weights must total 100");
    }
}
