package dev.illedrabomb.cropgenetics.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conservative Pam's HarvestCraft 2 Crops registry discovery.
 *
 * Only a pair of registered item IDs ("<crop>seeditem" and "<crop>item")
 * qualifies. This is a generic Crop Sticks fallback, NOT an AgriPlants
 * original definition. Explicit datapack species and their seed claims
 * always take precedence, including disabled/invalid explicit files.
 * No mutation, crop block, texture, season or soil chemistry is inferred.
 */
public final class PamCropAutoDiscovery {
    private static final String NAMESPACE = "pamhc2crops";
    private static final String SEED_SUFFIX = "seeditem";

    private PamCropAutoDiscovery() {}

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  List<CropReloadReport.Entry> diagnostics,
                                  PamAutoDiscoverySettings settings) {
        if (!settings.enabled() || settings.maxPlants() == 0) return;
        Set<String> reservedSeeds = new HashSet<>(explicitSeedClaims);
        for (PlantDefinition plant : plants.values()) reservedSeeds.addAll(plant.seedItems());

        List<ResourceLocation> keys = new ArrayList<>(BuiltInRegistries.ITEM.keySet());
        keys.sort(Comparator.comparing(ResourceLocation::toString));
        int added = 0;
        for (ResourceLocation seedKey : keys) {
            if (!NAMESPACE.equals(seedKey.getNamespace())
                    || !seedKey.getPath().endsWith(SEED_SUFFIX)) continue;
            String crop = seedKey.getPath().substring(0,
                    seedKey.getPath().length() - SEED_SUFFIX.length());
            if (crop.isEmpty() || !crop.matches("[a-z][a-z0-9_]*")
                    || settings.excludedCrops().contains(crop)) continue;

            String speciesId = NAMESPACE + ":" + crop + "_plant";
            String seedId = seedKey.toString();
            if (explicitSpecies.contains(speciesId) || plants.containsKey(speciesId)
                    || reservedSeeds.contains(seedId)) continue;

            ResourceLocation productKey = ResourceLocation.fromNamespaceAndPath(
                    NAMESPACE, crop + "item");
            // Registered paired items only. Never manufacture a missing
            // product, use AIR or infer a world crop block from naming.
            if (!BuiltInRegistries.ITEM.containsKey(productKey)
                    || BuiltInRegistries.ITEM.get(seedKey) == Items.AIR
                    || BuiltInRegistries.ITEM.get(productKey) == Items.AIR) continue;

            if (added >= settings.maxPlants()) {
                diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                        ResourceLocation.fromNamespaceAndPath("cropgenetics",
                                "auto_pam/discovery_limit"),
                        "", CropReloadReport.Status.WARNING,
                        "Pam's auto-discovery capped at " + settings.maxPlants()));
                break;
            }
            PlantDefinition plant = new PlantDefinition(
                    speciesId, List.of(NAMESPACE), List.of(seedId),
                    List.of(2, 4, 6, 8, 10, 12, 14, 16),
                    4, settings.growthChance(), settings.growthBonus(), true, 0.1D,
                    List.of(new PlantDefinition.HarvestProduct(
                            productKey.toString(), 1, settings.harvestMaximum(), 0.9D)),
                    new PlantRequirements(settings.minLight(), 15,
                            java.util.Optional.of("minecraft:farmland"),
                            java.util.Optional.empty(), 0), HarvestMode.STANDARD, 0.0D);
            List<String> errors = PlantRegistryValidator.errors(plant);
            if (!errors.isEmpty()) continue;

            plants.put(speciesId, plant);
            reservedSeeds.add(seedId);
            added++;
            diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                    ResourceLocation.fromNamespaceAndPath("cropgenetics",
                            "auto_pam/" + crop + ".json"),
                    speciesId, CropReloadReport.Status.WARNING,
                    "Auto-discovered registered Pam's seed/produce pair; "
                            + "configurable fallback farmland/growth values, NOT original AgriPlants parity"));
        }
    }
}
