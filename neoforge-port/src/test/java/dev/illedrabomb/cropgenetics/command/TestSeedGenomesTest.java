package dev.illedrabomb.cropgenetics.command;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TestSeedGenomesTest {
    private static final String PUMPKIN = "minecraft:pumpkin_plant";

    @Test
    void legacyOneNumberAlwaysControlsOnlyGain() {
        assertEquals(new Genome(PUMPKIN, 1, 1, 1), TestSeedGenomes.gainOnly(PUMPKIN, 1));
        assertEquals(new Genome(PUMPKIN, 1, 5, 1), TestSeedGenomes.gainOnly(PUMPKIN, 5));
        assertEquals(new Genome(PUMPKIN, 1, 10, 1), TestSeedGenomes.gainOnly(PUMPKIN, 10));
    }

    @Test
    void threeNumbersAreAlwaysGrowthGainStrength() {
        assertEquals(new Genome(PUMPKIN, 10, 5, 3),
                TestSeedGenomes.full(PUMPKIN, 10, 5, 3));
        assertEquals(new Genome("minecraft:melon_plant", 1, 10, 1),
                TestSeedGenomes.full("minecraft:melon_plant", 1, 10, 1));
        assertEquals(new Genome(PUMPKIN, 10, 10, 10),
                TestSeedGenomes.full(PUMPKIN, 10, 10, 10));
    }

    @Test
    void invalidGenomeBoundsOrSpeciesAreRejected() {
        for (int invalid : new int[] { 0, 11 }) {
            assertThrows(IllegalArgumentException.class,
                    () -> TestSeedGenomes.gainOnly(PUMPKIN, invalid));
            assertThrows(IllegalArgumentException.class,
                    () -> TestSeedGenomes.full(PUMPKIN, invalid, 1, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> TestSeedGenomes.full(PUMPKIN, 1, invalid, 1));
            assertThrows(IllegalArgumentException.class,
                    () -> TestSeedGenomes.full(PUMPKIN, 1, 1, invalid));
        }
        assertThrows(IllegalArgumentException.class,
                () -> TestSeedGenomes.full("bad id", 1, 1, 1));
    }
}
