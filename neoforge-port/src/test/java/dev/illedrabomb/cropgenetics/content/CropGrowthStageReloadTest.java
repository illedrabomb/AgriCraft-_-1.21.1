package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropGrowthStageReloadTest {
    @Test
    void lootMaturityUsesRelativeProgressAfterStageCountChanges() {
        // A previous eight-stage crop at age 4 is not instantly mature
        // after a datapack replaces its definition with four stages.
        assertFalse(CropGrowth.isMatureAtStage(4, 8, 4));
        assertTrue(CropGrowth.isMatureAtStage(7, 8, 4));
        assertFalse(CropGrowth.isMatureAtStage(2, 4, 8));
        assertTrue(CropGrowth.isMatureAtStage(3, 4, 4));
    }

    @Test
    void legacyAgeWithoutSavedStageCountRetainsOldFallback() {
        assertTrue(CropGrowth.isMatureAtStage(7, 0, 8));
        assertFalse(CropGrowth.isMatureAtStage(3, 0, 8));
        assertFalse(CropGrowth.isMatureAtStage(0, 8, 0));
    }

    @Test
    void matureEndpointStaysMatureAndFreshSeedStaysImmature() {
        for (int newTotal : new int[]{2, 4, 8, 16, 64}) {
            assertTrue(CropGrowth.isMatureAtStage(7, 8, newTotal));
            assertFalse(CropGrowth.isMatureAtStage(0, 8, newTotal));
        }
    }
}
