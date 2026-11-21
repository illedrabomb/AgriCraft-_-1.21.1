package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HarvestDropScalingTest {
    @Test
    void defaultsAndFractionsRoundToWholeItems() {
        assertEquals(3, HarvestDropScaling.count(3, 1.0D, 64));
        assertEquals(5, HarvestDropScaling.count(3, 1.5D, 64));
        assertEquals(1, HarvestDropScaling.count(3, 0.25D, 64));
        assertEquals(0, HarvestDropScaling.count(3, 0.0D, 64));
    }

    @Test
    void neverExceedsOneNormalStackEvenWithExtremeConfig() {
        assertEquals(64, HarvestDropScaling.count(16, 64.0D, 64));
        assertEquals(16, HarvestDropScaling.count(64, 64.0D, 16));
        assertEquals(1, HarvestDropScaling.count(64, 64.0D, 1));
        assertEquals(64, HarvestDropScaling.count(4096, 64.0D, 99));
        assertEquals(64, HarvestDropScaling.count(Long.MAX_VALUE, 64.0D, 64));
        assertEquals(64, HarvestDropScaling.count(Long.MAX_VALUE, 0.5D, 64));
    }

    @Test
    void invalidInputsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> HarvestDropScaling.count(-1, 1, 64));
        assertThrows(IllegalArgumentException.class, () -> HarvestDropScaling.count(1, -1, 64));
        assertThrows(IllegalArgumentException.class,
                () -> HarvestDropScaling.count(1, Double.NaN, 64));
        assertThrows(IllegalArgumentException.class,
                () -> HarvestDropScaling.count(1, Double.POSITIVE_INFINITY, 64));
        assertThrows(IllegalArgumentException.class, () -> HarvestDropScaling.count(1, 1, 0));
    }
}
