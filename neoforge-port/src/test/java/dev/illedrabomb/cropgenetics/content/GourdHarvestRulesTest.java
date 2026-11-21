package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GourdHarvestRulesTest {
    @Test
    void pumpkinChanceGrowsByTenPercentagePointsPerGain() {
        for (int gain = 1; gain <= 10; gain++) {
            assertEquals((gain - 1) / 10.0D,
                    GourdHarvestRules.secondPumpkinChance(gain), 1.0E-10);
        }
        assertEquals(0.0D, GourdHarvestRules.secondPumpkinChance(0));
        assertEquals(0.9D, GourdHarvestRules.secondPumpkinChance(100));
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
