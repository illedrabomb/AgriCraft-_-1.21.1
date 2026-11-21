package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GourdHarvestRulesTest {
    @Test
    void pumpkinQuantityThresholdsGrowWithGain() {
        for (int gain = 1; gain <= 10; gain++) {
            assertEquals((gain - 1) / 10.0D,
                    GourdHarvestRules.secondPumpkinChance(gain), 1.0E-10);
        }
        assertEquals(0.0D, GourdHarvestRules.secondPumpkinChance(0));
        assertEquals(0.9D, GourdHarvestRules.secondPumpkinChance(100));
        assertEquals(0.0D, GourdHarvestRules.thirdPumpkinChance(4));
        assertEquals(0.1D, GourdHarvestRules.thirdPumpkinChance(5));
        assertEquals(0.6D, GourdHarvestRules.thirdPumpkinChance(10));
        assertEquals(0.0D, GourdHarvestRules.fourthPumpkinChance(7));
        assertEquals(0.1D, GourdHarvestRules.fourthPumpkinChance(8));
        assertEquals(0.3D, GourdHarvestRules.fourthPumpkinChance(10));
    }

    @Test
    void pumpkinCountHasExactBoundariesWithoutRandomSampling() {
        assertEquals(1, GourdHarvestRules.pumpkinCount(1, 0.0D));
        assertEquals(1, GourdHarvestRules.pumpkinCount(2, 0.1D));
        assertEquals(2, GourdHarvestRules.pumpkinCount(2, 0.099D));
        assertEquals(4, GourdHarvestRules.pumpkinCount(10, 0.299D));
        assertEquals(3, GourdHarvestRules.pumpkinCount(10, 0.3D));
        assertEquals(2, GourdHarvestRules.pumpkinCount(10, 0.6D));
        assertEquals(1, GourdHarvestRules.pumpkinCount(10, 0.9D));
        assertThrows(IllegalArgumentException.class,
                () -> GourdHarvestRules.pumpkinCount(10, -0.01D));
        assertThrows(IllegalArgumentException.class,
                () -> GourdHarvestRules.pumpkinCount(10, 1.0D));
    }

    @Test
    void melonSlicesIncreaseEveryGainWithoutExceedingOneStack() {
        assertEquals(3, GourdHarvestRules.melonSlices(1, 0));
        assertEquals(7, GourdHarvestRules.melonSlices(1, 4));
        assertEquals(4, GourdHarvestRules.melonSlices(2, 0));
        assertEquals(12, GourdHarvestRules.melonSlices(10, 0));
        assertEquals(16, GourdHarvestRules.melonSlices(10, 4));
        assertTrue(GourdHarvestRules.melonSlices(999, 4) <= GourdHarvestRules.MAX_DROP);
        assertThrows(IllegalArgumentException.class,
                () -> GourdHarvestRules.melonSlices(1, -1));
        assertThrows(IllegalArgumentException.class,
                () -> GourdHarvestRules.melonSlices(1, 5));
    }
}
