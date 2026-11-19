package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Small, optional Farmer's Delight compatibility pilot.
 *
 * Cabbage and tomato use distinct seed and produce items. Onion and rice are
 * intentionally excluded: onion has no equivalent seed-item pair, and rice
 * requires special water planting. No Farmer's Delight classes are linked.
 */
public final class FarmersDelightCropDiscovery {
    private FarmersDelightCropDiscovery() {}

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics) {
        Map<String, RegisteredSeedCropDiscovery.ItemPair> pairs = Map.of(
                "cabbage", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                "tomato", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato"));

        // The suffix is deliberately never used for bulk inference. Only the
        // two reviewed explicit pairs above may become generic Crop Sticks.
        RegisteredSeedCropDiscovery.Policy policy =
                new RegisteredSeedCropDiscovery.Policy(
                        "farmersdelight", "__explicit_pairs_only__", "item",
                        "_plant", "auto_farmers_delight/",
                        2, 9, 0.75D, 0.025D, 3,
                        Set.of("onion", "rice"), pairs);
        RegisteredSeedCropDiscovery.addMissing(plants,
                explicitSpecies, explicitSeedClaims, explicitProduceClaims,
                diagnostics, policy);
    }
}
