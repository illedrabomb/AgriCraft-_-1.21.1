package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure tests of the exact fruit chance used by randomTick and diagnostics.
 * These tests do not replace player-triggered harvest and second-cycle tests.
 */
class GourdFruitRulesTest {
    @Test
    void baselineChanceMatchesDatapackAtNeutralGainAndSoil() {
        assertEquals(0.2D, GourdFruitRules.chance(0.2D, 1, 1D), 1E-12);
    }

    @Test
    void gainAndSoilModifierAffectFruitChanceBeforeClamping() {
        assertEquals(0.38D, GourdFruitRules.chance(0.2D, 10, 1D), 1E-12);
        assertEquals(0.19D, GourdFruitRules.chance(0.2D, 10, 0.5D), 1E-12);
        assertEquals(1D, GourdFruitRules.chance(0.8D, 10, 2D), 1E-12);
    }

    @Test
    void BlockedOrInvalidEnvironmentCannotCreateFruit() {
        assertEquals(0D, GourdFruitRules.chance(0.2D, 10, 0D));
        assertEquals(0D, GourdFruitRules.chance(0.2D, 10, -1D));
        assertEquals(0D, GourdFruitRules.chance(0.2D, 10, Double.NaN));
        assertEquals(0D, GourdFruitRules.chance(0.2D, 10, Double.POSITIVE_INFINITY));
        assertEquals(0D, GourdFruitRules.chance(Double.NaN, 10, 1D));
        assertEquals(0D, GourdFruitRules.chance(0D, 10, 1D));
    }
}
