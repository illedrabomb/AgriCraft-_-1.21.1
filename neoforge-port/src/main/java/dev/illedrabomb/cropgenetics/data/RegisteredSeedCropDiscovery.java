package dev.illedrabomb.cropgenetics.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Shared, opt-in crop seed/produce pair discovery for a specifically approved
 * mod namespace. An adapter supplies the exact naming policy; this class
 * never scans every mod for arbitrary names containing "seed".
 *
 * Explicit species, seed and produce claims always win. A discovered pair
 * is generic Crop Sticks support, never a substitute for original genetics
 * or special growing conditions.
 */
public final class RegisteredSeedCropDiscovery {
    private RegisteredSeedCropDiscovery() {}

    public record ItemPair(String seed, String produce) {}

    public record Policy(String modId, String seedSuffix, String produceSuffix,
                         String plantSuffix, String diagnosticPrefix, int maxPlants,
                         int minLight, double growthChance, double growthBonus,
                         int harvestMaximum, Set<String> excludedCrops,
                         Map<String, ItemPair> itemPairs) {
        public Policy {
            if (!modId.matches("[a-z0-9_.-]+")
                    || !seedSuffix.matches("[a-z0-9_]+")
                    || !produceSuffix.matches("[a-z0-9_]+")
                    || !plantSuffix.matches("[a-z0-9_]+")
                    || !diagnosticPrefix.matches("[a-z0-9_/-]+"))
                throw new IllegalArgumentException("Invalid approved crop discovery policy");
            if (maxPlants < 0 || maxPlants > 512 || minLight < 0 || minLight > 15
                    || harvestMaximum < 1 || harvestMaximum > 64
                    || !Double.isFinite(growthChance) || growthChance < 0 || growthChance > 1
                    || !Double.isFinite(growthBonus) || growthBonus < 0 || growthBonus > 1)
                throw new IllegalArgumentException("Invalid crop fallback limits");
            excludedCrops = Set.copyOf(excludedCrops);
            itemPairs = Map.copyOf(itemPairs);
        }
    }

    private record Candidate(String crop, ResourceLocation seed,
                             ResourceLocation produce, boolean configured) {}

    private static boolean registeredPair(Candidate candidate) {
        return BuiltInRegistries.ITEM.containsKey(candidate.seed())
                && BuiltInRegistries.ITEM.containsKey(candidate.produce())
                && BuiltInRegistries.ITEM.get(candidate.seed()) != Items.AIR
                && BuiltInRegistries.ITEM.get(candidate.produce()) != Items.AIR;
    }

    private static CropReloadReport.Entry diagnostic(
            Policy policy, String crop, String speciesId,
            CropReloadReport.Status status, String detail) {
        return new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                ResourceLocation.fromNamespaceAndPath("cropgenetics",
                        policy.diagnosticPrefix() + crop + ".json"),
                speciesId, status, detail);
    }

    /**
     * Called during the registry-accessible reload apply phase, after explicit
     * crops but before mutations. No snapshot is published by this method.
     */
    public static void addMissing(Map<String, PlantDefinition> plants,
                                  Set<String> explicitSpecies,
                                  Set<String> explicitSeedClaims,
                                  Set<String> explicitProduceClaims,
                                  List<CropReloadReport.Entry> diagnostics,
                                  Policy policy) {
        if (policy.maxPlants() == 0) return;

        Set<String> reservedSeeds = new HashSet<>(explicitSeedClaims);
        Set<String> reservedProducts = new HashSet<>(explicitProduceClaims);
        for (PlantDefinition plant : plants.values()) {
            reservedSeeds.addAll(plant.seedItems());
            for (PlantDefinition.HarvestProduct product : plant.harvestProducts())
                reservedProducts.add(product.itemId());
        }

        List<Candidate> candidates = new ArrayList<>();
        policy.itemPairs().entrySet().stream().sorted(Map.Entry.comparingByKey())
                .forEach(entry -> candidates.add(new Candidate(entry.getKey(),
                        ResourceLocation.parse(entry.getValue().seed()),
                        ResourceLocation.parse(entry.getValue().produce()), true)));

        List<ResourceLocation> keys = new ArrayList<>(BuiltInRegistries.ITEM.keySet());
        keys.sort(Comparator.comparing(ResourceLocation::toString));
        for (ResourceLocation seed : keys) {
            if (!policy.modId().equals(seed.getNamespace())
                    || !seed.getPath().endsWith(policy.seedSuffix())) continue;
            String crop = seed.getPath().substring(0,
                    seed.getPath().length() - policy.seedSuffix().length());
            if (crop.isEmpty() || !crop.matches("[a-z][a-z0-9_]*")
                    || policy.itemPairs().containsKey(crop)) continue;
            candidates.add(new Candidate(crop, seed, ResourceLocation.fromNamespaceAndPath(
                    policy.modId(), crop + policy.produceSuffix()), false));
        }

        // Only eligible registered pairs claim items. Reserve explicit and
        // already active claims, including their cross-role usage.
        Map<String, Integer> seedClaims = new HashMap<>();
        Map<String, Integer> produceClaims = new HashMap<>();
        for (Candidate candidate : candidates) {
            String speciesId = policy.modId() + ":" + candidate.crop() + policy.plantSuffix();
            if (policy.excludedCrops().contains(candidate.crop())
                    || explicitSpecies.contains(speciesId) || plants.containsKey(speciesId)
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
            if (policy.excludedCrops().contains(crop)) continue;
            String speciesId = policy.modId() + ":" + crop + policy.plantSuffix();
            String seedId = candidate.seed().toString();
            String produceId = candidate.produce().toString();
            if (explicitSpecies.contains(speciesId) || plants.containsKey(speciesId)
                    || reservedSeeds.contains(seedId) || reservedProducts.contains(produceId)
                    || reservedProducts.contains(seedId) || reservedSeeds.contains(produceId))
                continue;

            if (seedClaims.getOrDefault(seedId, 0) > 1
                    || produceClaims.getOrDefault(produceId, 0) > 1
                    || produceClaims.containsKey(seedId) || seedClaims.containsKey(produceId)) {
                diagnostics.add(diagnostic(policy, crop, speciesId,
                        CropReloadReport.Status.CONFLICT,
                        "Ambiguous auto-discovery seed/produce item; candidate disabled"));
                continue;
            }
            if (!registeredPair(candidate)) {
                if (candidate.configured()) {
                    diagnostics.add(diagnostic(policy, crop, speciesId,
                            CropReloadReport.Status.AUTO_MISSING_ITEM,
                            "Configured seed/produce pair missing in item registry; skipped"));
                }
                continue;
            }
            if (added >= policy.maxPlants()) {
                diagnostics.add(new CropReloadReport.Entry(CropReloadReport.Kind.PLANT,
                        ResourceLocation.fromNamespaceAndPath("cropgenetics",
                                policy.diagnosticPrefix() + "discovery_limit"),
                        "", CropReloadReport.Status.WARNING,
                        "Auto-discovery capped at " + policy.maxPlants()));
                break;
            }
            PlantDefinition plant = new PlantDefinition(
                    speciesId, List.of(policy.modId()), List.of(seedId),
                    List.of(2, 4, 6, 8, 10, 12, 14, 16),
                    4, policy.growthChance(), policy.growthBonus(), true, 0.1D,
                    List.of(new PlantDefinition.HarvestProduct(
                            produceId, 1, policy.harvestMaximum(), 0.9D)),
                    new PlantRequirements(policy.minLight(), 15,
                            Optional.of("minecraft:farmland"), Optional.empty(), 0),
                    HarvestMode.STANDARD, 0.0D);

            List<String> errors = PlantRegistryValidator.errors(plant);
            if (!errors.isEmpty()) {
                diagnostics.add(diagnostic(policy, crop, speciesId,
                        CropReloadReport.Status.WARNING,
                        "Auto-discovered crop failed registry validation: "
                                + String.join("; ", errors)));
                continue;
            }
            plants.put(speciesId, plant);
            reservedSeeds.add(seedId);
            reservedProducts.add(produceId);
            added++;
            diagnostics.add(diagnostic(policy, crop, speciesId,
                    CropReloadReport.Status.AUTO_DISCOVERED,
                    (candidate.configured() ? "Configured item pair" : "Detected standard item pair")
                            + "; generic growth and farmland ONLY, no original crop parity"));
        }
    }
}
