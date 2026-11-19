package dev.illedrabomb.cropgenetics.addon.pam;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PamSpeciesDisplayPolicyTest {
    @Test
    void retainsPluralSeedCropNames() {
        assertEquals("pamhc2crops:mustardseedsitem",
                PamSpeciesDisplayPolicy.produceForSpecies(
                        "pamhc2crops:mustardseeds_plant"));
        assertEquals("pamhc2crops:sesameseedsitem",
                PamSpeciesDisplayPolicy.produceForSpecies(
                        "pamhc2crops:sesameseeds_plant"));
    }

    @Test
    void refusesForeignAndInvalidSpecies() {
        assertNull(PamSpeciesDisplayPolicy.produceForSpecies("pamhc2trees:apple_plant"));
        assertNull(PamSpeciesDisplayPolicy.produceForSpecies("pamhc2crops:bad-name_plant"));
        assertNull(PamSpeciesDisplayPolicy.produceForSpecies("minecraft:wheat_plant"));
    }
}
