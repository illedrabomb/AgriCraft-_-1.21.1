package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GourdHarvestRulesTest {
    @Test
    void pumpkinQuantityThresholdsMatchApprovedBalanceAnchors() {
        assertEquals(0.0D, GourdHarvestRules.secondPumpkinChance(0));
        assertEquals(0.9D, GourdHarvestRules.secondPumpkinChance(100));
        assertEquals(0.19D, GourdHarvestRules.secondPumpkinChance(2), 1.0E-10);
        assertEquals(0.62D, GourdHarvestRules.secondPumpkinChance(5), 1.0E-10);
        assertEquals(0.86D, GourdHarvestRules.secondPumpkinChance(8), 1.0E-10);
        assertEquals(0.0D, GourdHarvestRules.thirdPumpkinChance(3));
        assertEquals(0.16D, GourdHarvestRules.thirdPumpkinChance(5), 1.0E-10);
        assertEquals(0.47D, GourdHarvestRules.thirdPumpkinChance(8), 1.0E-10);
        assertEquals(0.55D, GourdHarvestRules.thirdPumpkinChance(10), 1.0E-10);
        assertEquals(0.0D, GourdHarvestRules.fourthPumpkinChance(7));
        assertEquals(0.05D, GourdHarvestRules.fourthPumpkinChance(8), 1.0E-10);
        assertEquals(0.15D, GourdHarvestRules.fourthPumpkinChance(10), 1.0E-10);
        for (int gain = 1; gain <= 10; gain++) {
            assertTrue(GourdHarvestRules.fourthPumpkinChance(gain)
                    <= GourdHarvestRules.thirdPumpkinChance(gain));
            assertTrue(GourdHarvestRules.thirdPumpkinChance(gain)
                    <= GourdHarvestRules.secondPumpkinChance(gain));
        }
    }

    @Test
    void pumpkinCountHasExactBoundariesWithoutRandomSampling() {
        assertEquals(1, GourdHarvestRules.pumpkinCount(1, 0.0D));
        assertEquals(1, GourdHarvestRules.pumpkinCount(2, 0.19D));
        assertEquals(2, GourdHarvestRules.pumpkinCount(2, 0.189D));
        assertEquals(4, GourdHarvestRules.pumpkinCount(10, 0.149D));
        assertEquals(3, GourdHarvestRules.pumpkinCount(10, 0.15D));
        assertEquals(2, GourdHarvestRules.pumpkinCount(10, 0.55D));
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
