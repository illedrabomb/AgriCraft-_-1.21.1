package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Deterministic BAL-01 branch cases; not a statistical tuning simulation. */
class BalancedBreedingTest {
    private static Genome g(int growth, int gain, int strength) {
        return new Genome("minecraft:wheat", growth, gain, strength);
    }

    private static final class FixedRolls extends Random {
        private final int[] integers;
        private final double[] doubles;
        private int integerIndex;
        private int doubleIndex;

        FixedRolls(int[] integers, double... doubles) {
            this.integers = integers;
            this.doubles = doubles;
        }

        @Override public int nextInt(int bound) {
            return Math.floorMod(integers[integerIndex++], bound);
        }

        @Override public double nextDouble() {
            return doubles[doubleIndex++];
        }
    }

    @Test
    void selectsWholeParentByStrengthIncludingWeakerSevenOfThirtySevenTickets() {
        List<Genome> parents = List.of(g(10, 10, 10), g(10, 10, 10),
                g(10, 10, 10), g(5, 6, 7));
        Genome strong = BreedingEngine.combine(parents, "minecraft:wheat",
                new FixedRolls(new int[]{29}), 0D);
        Genome weak = BreedingEngine.combine(parents, "minecraft:wheat",
                new FixedRolls(new int[]{30}), 0D);
        assertEquals(g(10, 10, 10), strong);
        assertEquals(g(5, 6, 7), weak);
    }

    @Test
    void zeroPointRollDoesNotNeedASecondMutationActivationRoll() {
        Genome baseline = g(1, 1, 1);
        // Select template, three no-loss rolls, roll zero pool.
        assertEquals(baseline, BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new FixedRolls(new int[]{0, 0}, 1D, 1D, 1D)));
    }

    @Test
    void fourPointPoolCannotGiveMoreThanThreeToOneStat() {
        Genome baseline = g(5, 6, 7);
        assertEquals(g(8, 6, 7), BreedingEngine.combine(List.of(baseline, baseline,
                        baseline, baseline), baseline.plantId(),
                new FixedRolls(new int[]{0, 99, 0, 0, 0, 0}, 1D, 1D, 1D), 3D));
    }

    @Test
    void actualLostPointsCanReturnButNeverBelowOne() {
        Genome baseline = g(3, 1, 1);
        // Growth loses three requested points, but only two actual points.
        // Both are returned (40% roll succeeds), then point allocation
        // to Gain can increase it by two. Other stats cannot lose below one.
        assertEquals(g(1, 3, 1), BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new FixedRolls(new int[]{0, 99, 0, 1, 1},
                        0D, 0D, 0D, 1D, 1D), 1D));
    }

    @Test
    void highStrengthPreventsLossAtTheSameRoll() {
        Genome sturdy = g(3, 3, 10);
        assertEquals(sturdy, BreedingEngine.combine(sturdy, sturdy,
                sturdy.plantId(), new FixedRolls(new int[]{0, 0}, 0.20D, 0.20D, 0.20D)));
    }

    @Test
    void weakerRowsUseTheTwoParentPointPool() {
        Genome baseline = g(1, 1, 1);
        // 15/100 is a zero-point roll for the two-parent tier, but a
        // one-point roll in the four-parent direct tier.
        List<Genome> parents = List.of(baseline, baseline, baseline, baseline);
        assertEquals(baseline, BreedingEngine.combine(parents,
                baseline.plantId(), new FixedRolls(new int[]{0, 15}, 1D, 1D, 1D), 0.5D, true));
        assertEquals(g(2, 1, 1), BreedingEngine.combine(parents,
                baseline.plantId(), new FixedRolls(new int[]{0, 15, 0}, 1D, 1D, 1D), 3D, false));
    }

    @Test
    void straightRowDoesNotThinItsAlreadyWeakerPointTableAgain() {
        Genome baseline = g(1, 1, 1);
        List<Genome> parents = List.of(baseline, baseline, baseline, baseline);
        // At the historical default row multiplier 0.5, the 99-ticket is
        // four points from the weak two-parent table. It must not be thinned
        // by an additional row penalty; all four allocations hit Growth,
        // with the fourth discarded by the +3 limit.
        assertEquals(g(4, 1, 1), BreedingEngine.combine(parents,
                baseline.plantId(), new FixedRolls(new int[]{0, 99, 0, 0, 0, 0},
                        1D, 1D, 1D), 0.5D, true));
    }

    @Test
    void customRowMultiplierCanStillReduceMutation() {
        Genome baseline = g(1, 1, 1);
        List<Genome> parents = List.of(baseline, baseline, baseline);
        // Half the original row baseline: each of four generated points
        // fails its 0.5 keep roll, so the child inherits without gain.
        assertEquals(baseline, BreedingEngine.combine(parents,
                baseline.plantId(), new FixedRolls(new int[]{0, 99},
                        1D, 1D, 1D, 0.9D, 0.9D, 0.9D, 0.9D), 0.25D, true));
    }

    @Test
    void maximumConfigMultiplierStillHonorsThreePointLimitAndTenCap() {
        Genome baseline = g(8, 10, 10);
        List<Genome> parents = List.of(baseline, baseline, baseline, baseline);
        // Four base points, four extra-point rolls, then every point targets
        // Growth. The gain stays capped at 10 even at config multiplier 5.
        assertEquals(g(10, 10, 10), BreedingEngine.combine(parents,
                baseline.plantId(), new FixedRolls(new int[]{0, 99, 0, 0, 0, 0},
                        1D, 1D, 1D, 0D, 0D, 0D, 0D), 15D));
    }

    @Test
    void zeroMultiplierPreservesOneFullTemplateWithoutAnyMutation() {
        Genome weaker = g(5, 6, 7);
        Genome stronger = g(10, 10, 10);
        assertEquals(weaker, BreedingEngine.combine(List.of(stronger, weaker),
                weaker.plantId(), new FixedRolls(new int[]{10}), 0D));
    }

    @Test
    void rejectsInvalidConfigMutationMultipliers() {
        Genome genome = g(1, 1, 1);
        List<Genome> parents = List.of(genome, genome);
        assertThrows(IllegalArgumentException.class, () -> BreedingEngine.combine(
                parents, genome.plantId(), new Random(1), -1D));
        assertThrows(IllegalArgumentException.class, () -> BreedingEngine.combine(
                parents, genome.plantId(), new Random(1), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> BreedingEngine.combine(
                parents, genome.plantId(), new Random(1), Double.POSITIVE_INFINITY));
    }
}
