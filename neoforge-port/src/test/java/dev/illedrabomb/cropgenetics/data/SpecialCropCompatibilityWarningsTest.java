package dev.illedrabomb.cropgenetics.data;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SpecialCropCompatibilityWarningsTest {
    @Test
    void farmerRiceMustExplicitlyWarnAboutMissingOriginalMechanics() {
        String warning = SpecialCropCompatibilityWarnings.forPlant(
                "farmersdelight:rice_plant");
        assertNotNull(warning);
        assertTrue(warning.contains("source water"));
        assertTrue(warning.contains("panicle"));
        assertTrue(warning.contains("NOT implemented"));
    }

    @Test
    void supportedFarmerPilotsHaveNoSpecialWarning() {
        assertNull(SpecialCropCompatibilityWarnings.forPlant("farmersdelight:cabbage_plant"));
        assertNull(SpecialCropCompatibilityWarnings.forPlant("farmersdelight:tomato_plant"));
        assertNull(SpecialCropCompatibilityWarnings.forPlant("farmersdelight:onion_plant"));
    }

    @Test
    void vanillaAndPamCropsDoNotReceiveRiceWarning() {
        assertNull(SpecialCropCompatibilityWarnings.forPlant("minecraft:wheat_plant"));
        assertNull(SpecialCropCompatibilityWarnings.forPlant("pamhc2crops:rice_plant"));
    }
}
