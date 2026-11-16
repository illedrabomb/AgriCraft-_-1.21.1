package dev.illedrabomb.cropgenetics.content;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CropGrowthRegressionTest {
    @Test void stageReloadPreservesEndpointsAndRelativeProgress() {
        assertEquals(0, CropGrowth.remapStage(0, 8, 16));
        assertEquals(15, CropGrowth.remapStage(7, 8, 16));
        assertEquals(6, CropGrowth.remapStage(3, 8, 16));
        assertEquals(3, CropGrowth.remapStage(6, 16, 8));
    }
    @Test void exactGrowthCanChangeWithinSameBlockState() {
        assertEquals(CropGrowth.displayStage(1, 16), CropGrowth.displayStage(2, 16));
        assertEquals(7, CropGrowth.displayStage(15, 16));
    }
    @Test void legacyAndOutOfRangeSavedStagesAreBounded() {
        assertEquals(7, CropGrowth.remapStage(0, 1, 8));
        assertEquals(0, CropGrowth.remapStage(-5, 8, 16));
        assertEquals(15, CropGrowth.remapStage(99, 8, 16));
    }
}
