package dev.illedrabomb.cropgenetics.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conservative Pam's HarvestCraft 2 Crops registry discovery.
 *
 * Registered pairs under the historical <crop>seeditem / <crop>item schema
 * are recognized automatically. Administrators may also explicitly specify
 * uncommon pair IDs in a server datapack. Neither route guesses mutations,
 * crop blocks, textures, soil chemistry, seasons or other original gameplay.
 */
public final class PamCropAutoDiscovery {
    private static final String NAMESPACE = "pamhc2crops";
    private static final String SEED_SUFFIX = "seeditem";

    private record Candidate(String crop, ResourceLocation seed,
                             ResourceLocation produce, boolean configured) {}

    private PamCropAutoDiscovery() {}

    /** Validate against actual registered items before collision counting. */
    private static boolean registeredPair(Candidate pair) {
        return BuiltInRegistries.ITEM.containsKey(pair.seed())
                && BuiltInRegistries.ITEM.containsKey(pair.produce())
                && BuiltInRegistries.ITEM.get(pair.seed()) != Items.AIR
                && BuiltInRegistries.ITEM.get(pair.produce()) != Items.AIR;
    }

    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics,
                                  PamAutoDiscoverySettings settings) {
        if (!settings.enabled() || settings.maxPlants() == 0) return;
        Set<String> reservedSeeds = new HashSet<>(explicitSeedClaims);
        for (PlantDefinition plant : plants.values()) reservedSeeds.addAll(plant.seedItems());
        Set<String> reservedProducts = new HashSet<>(explicitProduceClaims);
        for (PlantDefinition plant : plants.values()) {
            for (PlantDefinition.HarvestProduct product : plant.harvestProducts())
                reservedProducts.add(product.itemId());
        }

        // Configured pairs come first: a special seed can also accidentally
        // match the generic suffix scheme, so explicit pairs win deterministically.
        List<Candidate> candidates = new ArrayList<>();
        settings.itemPairs().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> candidates.add(new Candidate(entry.getKey(),
                        ResourceLocation.parse(entry.getValue().seed()),
                        ResourceLocation.parse(entry.getValue().produce()), true)));

        // Registered standard pairs are eligible only when the crop has no
        // custom pair. Iteration order is stable regardless of registry order.
        List<ResourceLocation> keys = new ArrayList<>(BuiltInRegistries.ITEM.keySet());
        keys.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation seed : keys) {
            if (!NAMESPACE.equals(seed.getNamespace())
                    || !seed.getPath().endsWith(SEED_SUFFIX)) continue;
            String crop = seed.getPath().substring(0,
                    seed.getPath().length() - SEED_SUFFIX.length());
            if (crop.isEmpty() || !crop.matches("[a-z][a-z0-9_]*")
                    || settings.itemPairs().containsKey(crop)) continue;
            candidates.add(new Candidate(crop, seed,
                    ResourceLocation.fromNamespaceAndPath(NAMESPACE, crop + "item"), false));
        }

        // Do not let iteration order decide which crop owns a seed/produce
        // that multiple candidate species claim. Treat ALL claimants as
        // ambiguous, including configured-vs-standard naming collisions.
        Map<String, Integer> seedClaims = new HashMap<>();
        Map<String, Integer> produceClaims = new HashMap<>();
        for (Candidate candidate : candidates) {
            String speciesId = NAMESPACE + ":" + candidate.crop() + "_plant";
            if (settings.excludedCrops().contains(candidate.crop())
                    || explicitSpecies.contains(speciesId)
                    || plants.containsKey(speciesId)
                    || reservedSeeds.contains(candidate.seed().toString())
                    || reservedProducts.contains(candidate.produce().toString())
                    || reservedProducts.contains(candidate.seed().toString())
                    || reservedSeeds.contains(candidate.produce().toString())
                    || !registeredPair(candidate)) continue;
            seedClaims.merge(candidate.seed().toString(), 1, Integer::sum);
            produceClaims.merge(candidate.produce().toString(), 1, Integer::sum);
        }

        int added = 0;
        for (Candidate candidate : candidates) {
            String crop = candidate.crop();
            if (settings.excludedCrops().contains(crop)) continue;
            String speciesId = NAMESPACE + ":" + crop + "_plant";
            String seedId = candidate.seed().toString();
            if (explicitSpecies.contains(speciesId) || plants.containsKey(speciesId)
                    || reservedSeeds.contains(seedId)
                    || reservedProducts.contains(candidate.produce().toString())
                    || reservedProducts.contains(seedId)
                    || reservedSeeds.contains(candidate.produce().toString())) continue;
            if (seedClaims.getOrDefault(seedId, 0) > 1
                    || produceClaims.getOrDefault(candidate.produce().toString(), 0) > 1
                    || produceClaims.containsKey(seedId)
                    || seedClaims.containsKey(candidate.produce().toString())) {
                diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                        ResourceLocation.fromNamespaceAndPath("cropgenetics",
                                "auto_pam/" + crop + ".json"),
                        speciesId, CropReloadReport.Status.CONFLICT,
                        "Ambiguous auto-discovery seed/produce item (including cross-role reuse); candidate disabled"));
                continue;
            }

            if (!registeredPair(candidate)) {
                if (candidate.configured()) {
                    diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                            ResourceLocation.fromNamespaceAndPath("cropgenetics",
                                    "auto_pam/" + crop + ".json"),
                            speciesId, CropReloadReport.Status.WARNING,
                            "Configured Pam's seed/produce pair does not exist in item registry; skipped"));
                }
                continue;
            }

            if (added >= settings.maxPlants()) {
                diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                        ResourceLocation.fromNamespaceAndPath("cropgenetics", "auto_pam/discovery_limit"),
                        "", CropReloadReport.Status.WARNING,
                        "Pam's auto-discovery capped at " + settings.maxPlants()));
                break;
            }
            PlantDefinition plant = new PlantDefinition(
                    speciesId, List.of(NAMESPACE), List.of(seedId),
                    List.of(2, 4, 6, 8, 10, 12, 14, 16),
                    4, settings.growthChance(), settings.growthBonus(), true, 0.1D,
                    List.of(new PlantDefinition.HarvestProduct(
                            candidate.produce().toString(), 1, settings.harvestMaximum(), 0.9D)),
                    new PlantRequirements(settings.minLight(), 15,
                            java.util.Optional.of("minecraft:farmland"),
                            java.util.Optional.empty(), 0),
                    HarvestMode.STANDARD, 0.0D);
            List<String> errors = PlantRegistryValidator.errors(plant);
            if (!errors.isEmpty()) {
                diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                        ResourceLocation.fromNamespaceAndPath("cropgenetics",
                                "auto_pam/" + crop + ".json"),
                        speciesId, CropReloadReport.Status.WARNING,
                        "Auto-discovered Pam's crop failed registry validation: "
                                + String.join("; ", errors)));
                continue;
            }

            plants.put(speciesId, plant);
            reservedSeeds.add(seedId);
            reservedProducts.add(candidate.produce().toString());
            added++;
            diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                    ResourceLocation.fromNamespaceAndPath("cropgenetics",
                            "auto_pam/" + crop + ".json"),
                    speciesId, CropReloadReport.Status.WARNING,
                    (candidate.configured() ? "Configured item pair" : "Detected standard item pair")
                            + "; generic growth and farmland ONLY, no original AgriPlants parity"));
        }
    }
}
