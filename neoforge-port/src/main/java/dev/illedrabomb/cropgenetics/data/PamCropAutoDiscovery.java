package dev.illedrabomb.cropgenetics.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The only registered integration for the generic seed-pair engine.
 * Pam's HarvestCraft 2 Crops uses an explicit item naming contract.
 * No other mod, Pam's Trees or saplings are scanned by this adapter.
 */
public final class PamCropAutoDiscovery {
    private PamCropAutoDiscovery() {}

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics,
                                  PamAutoDiscoverySettings settings) {
        if (!settings.enabled() || settings.maxPlants() == 0) return;

        Map<String, RegisteredSeedCropDiscovery.ItemPair> pairs = new HashMap<>();
        settings.itemPairs().forEach((crop, pair) ->
                pairs.put(crop, new RegisteredSeedCropDiscovery.ItemPair(
                        pair.seed(), pair.produce())));

        RegisteredSeedCropDiscovery.Policy policy =
                new RegisteredSeedCropDiscovery.Policy(
                        "pamhc2crops", "seeditem", "item", "_plant", "auto_pam/",
                        settings.maxPlants(), settings.minLight(),
                        settings.growthChance(), settings.growthBonus(),
                        settings.harvestMaximum(), settings.excludedCrops(), pairs);

        RegisteredSeedCropDiscovery.addMissing(plants, explicitSpecies,
                explicitSeedClaims, explicitProduceClaims, diagnostics, policy);
    }
}
