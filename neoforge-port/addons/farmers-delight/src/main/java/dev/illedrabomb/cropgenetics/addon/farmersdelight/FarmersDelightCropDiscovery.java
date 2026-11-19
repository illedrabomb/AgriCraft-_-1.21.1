package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import dev.illedrabomb.cropgenetics.data.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reviewed Farmer's Delight 1.21.1 generic Crop Sticks candidates.
 *
 * Cabbage and tomato use distinct seed/produce items; onion is deliberately
 * an EXPLICIT same-item pair. Rice requires its native source-water and
 * separate panicle lifecycle, so it remains excluded. No Farmer's Delight
 * classes are loaded by this optional adapter.
 */
public final class FarmersDelightCropDiscovery {
    private FarmersDelightCropDiscovery() {}

    /** Package-visible policy supports independent regression tests. */
    public static RegisteredSeedCropDiscovery.Policy policy() {
        Map<String, RegisteredSeedCropDiscovery.ItemPair> pairs = Map.of(
                "cabbage", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                "tomato", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato"),
                "onion", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:onion", "farmersdelight:onion"));

        // Inference uses a deliberately impossible suffix: only these three
        // reviewed pairs may become generic Crop Sticks plants.
        return new RegisteredSeedCropDiscovery.Policy(
                "farmersdelight", "__explicit_pairs_only__", "item",
                "_plant", "auto_farmers_delight/",
                3, 9, 0.75D, 0.025D, 3, Set.of("rice"), pairs);
    }

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics) {
        RegisteredSeedCropDiscovery.addMissing(plants,
                explicitSpecies, explicitSeedClaims, explicitProduceClaims,
                diagnostics, policy());
    }
}
