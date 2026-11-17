package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Protects two-parent breeding from accidental multiplication of fertility rolls. */
class CrossBreedingFertilityTest {
    private static Genome withFertility(int first, int second) {
        GenePair one = new GenePair(1, 1);
        return new Genome("minecraft:wheat_plant", one, one, one,
                new GenePair(first, second), one);
    }

    @Test
    void twoBaselineParentsUseOneTenPercentRollNotTwoOnePercentRolls() {
        assertEquals(0.1D, CrossBreeding.pairFertilityChance(
                withFertility(1, 1), withFertility(1, 1)), 1.0E-10);
    }

    @Test
    void highFertilityParentsHaveHigherSuccessProbability() {
        assertEquals(1.0D, CrossBreeding.pairFertilityChance(
                withFertility(10, 10), withFertility(10, 10)), 1.0E-10);
        assertEquals(0.55D, CrossBreeding.pairFertilityChance(
                withFertility(1, 1), withFertility(10, 10)), 1.0E-10);
    }

    @Test
    void expressedDominantFertilityIsUsed() {
        assertEquals(0.65D, CrossBreeding.pairFertilityChance(
                withFertility(8, 2), withFertility(5, 1)), 1.0E-10);
    }
}
