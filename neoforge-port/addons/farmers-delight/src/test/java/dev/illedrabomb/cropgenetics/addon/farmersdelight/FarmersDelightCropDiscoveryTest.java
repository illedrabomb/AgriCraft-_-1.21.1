package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import dev.illedrabomb.cropgenetics.data.*;

import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure policy tests: no Farmer's Delight jar or Minecraft client is needed
 * to verify that the adapter only exposes reviewed ordinary crop pairs.
 */
class FarmersDelightCropDiscoveryTest {
    @Test
    void pilotContainsExactlyThreeReviewedSeedProducePairs() {
        var policy = FarmersDelightCropDiscovery.policy();
        assertEquals("farmersdelight", policy.modId());
        assertEquals(3, policy.maxPlants());
        assertEquals(Map.of(
                "cabbage", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                "tomato", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato"),
                "onion", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:onion", "farmersdelight:onion")),
                policy.itemPairs());
        assertTrue(policy.excludedCrops().contains("rice"));
        assertFalse(policy.excludedCrops().contains("onion"));
    }

    @Test
    void onionIsExplicitlySelfPlantableWithoutBroadSeedInference() {
        var policy = FarmersDelightCropDiscovery.policy();
        assertEquals("farmersdelight:onion",
                policy.itemPairs().get("onion").seed());
        assertEquals("farmersdelight:onion",
                policy.itemPairs().get("onion").produce());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("farmersdelight:rice_seeds"), policy).isEmpty());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("farmersdelight:arbitrary_seeds"), policy).isEmpty());
    }

    @Test
    void theGenericPolicyRejectsForeignItemsAndCrossCropRoleConflicts() {
        var p = FarmersDelightCropDiscovery.policy();
        assertThrows(IllegalArgumentException.class, () -> new RegisteredSeedCropDiscovery.Policy(
                p.modId(), p.seedSuffix(), p.produceSuffix(), p.plantSuffix(),
                p.diagnosticPrefix(), p.maxPlants(), p.minLight(),
                p.growthChance(), p.growthBonus(), p.harvestMaximum(),
                p.excludedCrops(), Map.of("cabbage",
                        new RegisteredSeedCropDiscovery.ItemPair(
                                "minecraft:wheat_seeds", "farmersdelight:cabbage"))));
        assertThrows(IllegalArgumentException.class, () -> new RegisteredSeedCropDiscovery.Policy(
                p.modId(), p.seedSuffix(), p.produceSuffix(), p.plantSuffix(),
                p.diagnosticPrefix(), p.maxPlants(), p.minLight(),
                p.growthChance(), p.growthBonus(), p.harvestMaximum(),
                p.excludedCrops(), Map.of(
                        "onion", new RegisteredSeedCropDiscovery.ItemPair(
                                "farmersdelight:onion", "farmersdelight:onion"),
                        "other", new RegisteredSeedCropDiscovery.ItemPair(
                                "farmersdelight:other_seeds", "farmersdelight:onion"))));
    }
}
