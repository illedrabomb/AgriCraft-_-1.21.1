package dev.illedrabomb.cropgenetics.data;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Naming checks based on Pam's public seed loot table. These checks cover
 * name parsing only, not whether a specific NeoForge 1.21.1 jar registers
 * the corresponding produce item or supports generic Crop Sticks growth.
 */
class PamSeedNamingContractTest {
    private static final RegisteredSeedCropDiscovery.Policy POLICY =
            new RegisteredSeedCropDiscovery.Policy(
                    "pamhc2crops", "seeditem", "item", "_plant", "auto_pam/",
                    128, 9, 0.75D, 0.025D, 5, Set.of(), Map.of());

    @Test
    void ordinarySeedItemNameBecomesExpectedCrop() {
        assertEquals("tomato", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:tomatoseeditem"), POLICY).orElseThrow());
        assertEquals("rice", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:riceseeditem"), POLICY).orElseThrow());
    }

    @Test
    void repeatedSeedsWordInNameMustNotBeSimplifiedOrLost() {
        // Official seed loot table contains this exact registry path.
        assertEquals("mustardseeds", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:mustardseedsseeditem"),
                POLICY).orElseThrow());
        assertEquals("sesameseeds", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:sesameseedsseeditem"),
                POLICY).orElseThrow());
    }

    @Test
    void foreignNamespaceAndWrongSuffixAreNeverRecognized() {
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("farmersdelight:tomatoseeditem"), POLICY).isEmpty());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:tomatoitem"), POLICY).isEmpty());
    }
}
