package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

/**
 * Small, optional Farmer's Delight compatibility pilot.
 *
 * Cabbage and tomato use distinct seed and produce items. Onion and rice are
 * intentionally excluded: onion has no equivalent seed-item pair, and rice
 * requires special water planting. No Farmer's Delight classes are linked.
 */
public final class FarmersDelightCropDiscovery {
    private FarmersDelightCropDiscovery() {}

    /** Package-visible policy supports independent regression tests. */
    static RegisteredSeedCropDiscovery.Policy policy() {
        Map<String, RegisteredSeedCropDiscovery.ItemPair> pairs = Map.of(
                "cabbage", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                "tomato", new RegisteredSeedCropDiscovery.ItemPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato"));

        // The suffix is deliberately never used for bulk inference. Only the
        // two reviewed explicit pairs above may become generic Crop Sticks.
        return new RegisteredSeedCropDiscovery.Policy(
                        "farmersdelight", "__explicit_pairs_only__", "item",
                        "_plant", "auto_farmers_delight/",
                        2, 9, 0.75D, 0.025D, 3,
                        Set.of("onion", "rice"), pairs);
    }

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics) {
        RegisteredSeedCropDiscovery.addMissing(plants,
                explicitSpecies, explicitSeedClaims, explicitProduceClaims,
                diagnostics, policy());
        addOnion(plants, explicitSpecies, explicitSeedClaims,
                explicitProduceClaims, diagnostics);
    }

    // Farmer's Delight uses the onion itself as both seed and food. The
    // generic two-distinct-items discovery must reject same-item pairs.
    // Handle this one documented exception explicitly, never by guessing.
    private static final String ONION_PLANT = "farmersdelight:onion_plant";
    private static final String ONION_ITEM = "farmersdelight:onion";

    static boolean canAddOnion(Set<String> speciesClaims,
                               Set<String> seedClaims,
                               Set<String> produceClaims) {
        return !speciesClaims.contains(ONION_PLANT)
                && !seedClaims.contains(ONION_ITEM)
                && !produceClaims.contains(ONION_ITEM);
    }

    private static void addOnion(Map<String, PlantDefinition> plants,
                                 Set<String> explicitSpecies,
                                 Set<String> explicitSeedClaims,
                                 Set<String> explicitProduceClaims,
                                 List<CropReloadReport.Entry> diagnostics) {
        if (plants.containsKey(ONION_PLANT)
                || !canAddOnion(explicitSpecies, explicitSeedClaims, explicitProduceClaims))
            return;
        // Reserve onion if another valid plant already owns it. Be
        // conservative even when that plant only produces, but cannot plant,
        // the onion item.
        for (PlantDefinition existing : plants.values()) {
            if (existing.seedItems().contains(ONION_ITEM)
                    || existing.harvestProducts().stream()
                        .anyMatch(product -> ONION_ITEM.equals(product.itemId())))
                return;
        }
        ResourceLocation itemId = ResourceLocation.parse(ONION_ITEM);
        if (BuiltInRegistries.ITEM.getOptional(itemId)
                .filter(item -> item != Items.AIR).isEmpty()) {
            diagnostics.add(new CropReloadReport.Entry(
                    CropReloadReport.Kind.PLANT,
                    ResourceLocation.fromNamespaceAndPath(
                            "cropgenetics", "auto_farmers_delight/onion.json"),
                    ONION_PLANT, CropReloadReport.Status.AUTO_MISSING_ITEM,
                    "Onion item not registered in Farmer's Delight; skipped"));
            return;
        }

        PlantDefinition onion = new PlantDefinition(
                ONION_PLANT, List.of("farmersdelight"), List.of(ONION_ITEM),
                List.of(2, 4, 6, 8, 10, 12, 14, 16),
                4, 0.75D, 0.025D, true, 0.1D,
                List.of(new PlantDefinition.HarvestProduct(
                        ONION_ITEM, 1, 3, 0.9D)),
                new PlantRequirements(9, 15,
                        Optional.of("minecraft:farmland"), Optional.empty(), 0),
                HarvestMode.STANDARD, 0.0D);
        List<String> validationErrors = PlantRegistryValidator.errors(onion);
        if (!validationErrors.isEmpty()) {
            diagnostics.add(new CropReloadReport.Entry(
                    CropReloadReport.Kind.PLANT,
                    ResourceLocation.fromNamespaceAndPath(
                            "cropgenetics", "auto_farmers_delight/onion.json"),
                    ONION_PLANT, CropReloadReport.Status.WARNING,
                    "Onion fallback invalid: " + String.join("; ", validationErrors)));
            return;
        }
        plants.put(ONION_PLANT, onion);
        diagnostics.add(new CropReloadReport.Entry(
                CropReloadReport.Kind.PLANT,
                ResourceLocation.fromNamespaceAndPath(
                        "cropgenetics", "auto_farmers_delight/onion.json"),
                ONION_PLANT, CropReloadReport.Status.AUTO_DISCOVERED,
                "Onion as both seed and produce; generic farmland growth only"));
    }
}
