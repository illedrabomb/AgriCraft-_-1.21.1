package dev.illedrabomb.cropgenetics.data;

import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure configuration tests: no Farmer's Delight jar or Minecraft client is
 * needed just to verify that the pilot never auto-registers special crops.
 */
class FarmersDelightCropDiscoveryTest {
    @Test
    void pilotContainsExactlyTwoReviewedSeedProducePairs() {
        var policy = FarmersDelightCropDiscovery.policy();
        assertEquals("farmersdelight", policy.modId());
        assertEquals(2, policy.maxPlants());
        assertEquals(Map.of(
                "cabbage", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                "tomato", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato")),
                policy.itemPairs());
        assertTrue(policy.excludedCrops().contains("rice"));
        assertTrue(policy.excludedCrops().contains("onion"));
    }

    @Test
    void onionSameItemNeedsItsOwnUnclaimedIdentity() {
        // The generic seed/produce pair policy excludes onion, while the
        // explicit onion exception permits one same-item source/harvest.
        assertTrue(FarmersDelightCropDiscovery.canAddOnion(
                Set.of(), Set.of(), Set.of()));
        assertFalse(FarmersDelightCropDiscovery.canAddOnion(
                Set.of("farmersdelight:onion_plant"), Set.of(), Set.of()));
        assertFalse(FarmersDelightCropDiscovery.canAddOnion(
                Set.of(), Set.of("farmersdelight:onion"), Set.of()));
        assertFalse(FarmersDelightCropDiscovery.canAddOnion(
                Set.of(), Set.of(), Set.of("farmersdelight:onion")));
    }

    @Test
    void theGenericPolicyRejectsForeignItemsAndDuplicateRoles() {
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
                p.excludedCrops(), Map.of("cabbage",
                        new RegisteredSeedCropDiscovery.ItemPair(
                                "farmersdelight:cabbage", "farmersdelight:cabbage"))));
    }
}
