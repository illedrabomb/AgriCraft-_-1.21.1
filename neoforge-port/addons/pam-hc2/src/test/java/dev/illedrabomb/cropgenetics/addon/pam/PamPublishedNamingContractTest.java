package dev.illedrabomb.cropgenetics.addon.pam;

import dev.illedrabomb.cropgenetics.data.*;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PamPublishedNamingContractTest {
    @Test
    void simpleSeedItemMapsToItsCropName() {
        var policy = PamAutoDiscoverySettings.DEFAULT;
        var pairPolicy = new RegisteredSeedCropDiscovery.Policy(
                "pamhc2crops", "seeditem", "item", "_plant", "auto_pam/",
                policy.maxPlants(), policy.minLight(), policy.growthChance(),
                policy.growthBonus(), policy.harvestMaximum(),
                policy.excludedCrops(), java.util.Map.of());
        assertEquals("carrot", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:carrotseeditem"), pairPolicy).orElseThrow());
        assertEquals("mustardseeds", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:mustardseedsseeditem"), pairPolicy).orElseThrow());
        assertEquals("sesameseeds", RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:sesameseedsseeditem"), pairPolicy).orElseThrow());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2trees:carrotseeditem"), pairPolicy).isEmpty());
    }
}
