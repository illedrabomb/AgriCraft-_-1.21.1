package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regression for the exclusive one-stick planting / two-stick breeding rule. */
class CropCrossLayerStateTest {
    @Test
    void emptyCrossSticksRemainAvailableForBreeding() {
        assertTrue(CropCrossLayerRules.keepCrossLayer(true, false));
    }

    @Test
    void newlyGrownOffspringConsumesOnlyTheExtraCrossLayer() {
        assertFalse(CropCrossLayerRules.keepCrossLayer(true, true));
    }

    @Test
    void singleSticksNeverBecomeCrossedSimplyBecauseCropGrows() {
        assertFalse(CropCrossLayerRules.keepCrossLayer(false, false));
        assertFalse(CropCrossLayerRules.keepCrossLayer(false, true));
    }
}
