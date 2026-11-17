package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regression for the exclusive one-stick planting / two-stick breeding rule. */
class CropCrossLayerStateTest {
    @Test
    void emptyCrossSticksRemainAvailableForBreeding() {
        assertTrue(CropSticksBlockEntity.keepCrossLayer(true, false));
    }

    @Test
    void newlyGrownOffspringConsumesOnlyTheExtraCrossLayer() {
        assertFalse(CropSticksBlockEntity.keepCrossLayer(true, true));
    }

    @Test
    void singleSticksNeverBecomeCrossedSimplyBecauseCropGrows() {
        assertFalse(CropSticksBlockEntity.keepCrossLayer(false, false));
        assertFalse(CropSticksBlockEntity.keepCrossLayer(false, true));
    }
}
