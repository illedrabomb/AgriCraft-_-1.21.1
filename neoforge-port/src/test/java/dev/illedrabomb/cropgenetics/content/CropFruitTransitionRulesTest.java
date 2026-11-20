package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.HarvestMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropFruitTransitionRulesTest {
    @Test
    void internalGourdFruitDoesNotBlockStandardHarvestAfterReload() {
        assertTrue(CropFruitTransitionRules.staleInternalFruit(
                HarvestMode.STANDARD, true, false, false));
        assertTrue(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.STANDARD, true, true, false, false));
        assertFalse(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.STANDARD, false, true, false, false));
    }

    @Test
    void physicalFruitOwnershipStillBlocksDuplicateStandardHarvest() {
        assertFalse(CropFruitTransitionRules.staleInternalFruit(
                HarvestMode.STANDARD, true, true, true));
        assertFalse(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.STANDARD, true, true, true, true));
        assertFalse(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.STANDARD, true, false, true, true));
    }

    @Test
    void normalGourdLifecycleAndOrdinaryCropsRemainDistinct() {
        assertFalse(CropFruitTransitionRules.staleInternalFruit(
                HarvestMode.GOURD, true, false, false));
        assertFalse(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.GOURD, true, true, false, false));
        assertTrue(CropFruitTransitionRules.canHarvestStandard(
                HarvestMode.STANDARD, true, false, false, false));
    }
}
