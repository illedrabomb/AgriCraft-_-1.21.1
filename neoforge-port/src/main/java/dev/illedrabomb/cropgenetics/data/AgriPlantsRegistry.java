package dev.illedrabomb.cropgenetics.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Immutable server crop snapshot. Plant definitions, mutation definitions and
 * the reverse seed index are published in a single volatile assignment.
 */
public final class AgriPlantsRegistry {
    public record Snapshot(Map<String, PlantDefinition> plants,
                           Map<String, MutationDefinition> mutations,
                           Map<String, String> seedToPlant,
                           Set<String> ambiguousSeedItems,
                           CropReloadReport report) {
        public Snapshot {
            plants = Map.copyOf(plants);
            mutations = Map.copyOf(mutations);
            seedToPlant = Map.copyOf(seedToPlant);
            ambiguousSeedItems = Set.copyOf(ambiguousSeedItems);
            java.util.Objects.requireNonNull(report);
        }

        public Optional<PlantDefinition> plantForSeed(String itemId) {
            String id = seedToPlant.get(itemId);
            return id == null ? Optional.empty() : Optional.ofNullable(plants.get(id));
        }
    }

    private volatile Snapshot snapshot = new Snapshot(Map.of(), Map.of(), Map.of(), Set.of(), CropReloadReport.empty());

    /** Capture once when one operation needs several related collections. */
    public Snapshot snapshot() { return snapshot; }

    public Map<String, PlantDefinition> plants() { return snapshot.plants(); }
    public Map<String, MutationDefinition> mutations() { return snapshot.mutations(); }
    public Optional<PlantDefinition> plantForSeed(String itemId) {
        return snapshot.plantForSeed(itemId);
    }
    public Set<String> ambiguousSeedItems() { return snapshot.ambiguousSeedItems(); }
    public CropReloadReport reloadReport() { return snapshot.report(); }

    /**
     * Construct everything locally, then publish it all at once. References
     * to disabled plants cannot be activated by a stale mutation reload.
     */
    public void replaceAll(Map<String, PlantDefinition> inputPlants,
                           Map<String, MutationDefinition> inputMutations,
                           CropReloadReport report) {
        Map<String, PlantDefinition> plants = Map.copyOf(inputPlants);
        Map<String, MutationDefinition> mutations = Map.copyOf(inputMutations);
        // The combined reload listener has already validated references and
        // recorded their diagnostics. Never silently remove a mutation here:
        // that would make the published report disagree with the catalogue.
        mutations.forEach((id, mutation) -> {
            if (!plants.containsKey(mutation.parentA())
                    || !plants.containsKey(mutation.parentB())
                    || !plants.containsKey(mutation.childPlantId())) {
                throw new IllegalArgumentException("Mutation " + id
                        + " references a species absent from the new crop snapshot");
            }
        });

        Map<String, String> reverse = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();
        plants.values().stream()
                .sorted(java.util.Comparator.comparing(PlantDefinition::id))
                .forEach(plant -> {
                    for (String itemId : plant.seedItems()) {
                        if (ambiguous.contains(itemId)) continue;
                        String prior = reverse.putIfAbsent(itemId, plant.id());
                        if (prior != null && !prior.equals(plant.id())) {
                            reverse.remove(itemId);
                            ambiguous.add(itemId);
                        }
                    }
                });
        snapshot = new Snapshot(plants, mutations, reverse, ambiguous, report);
    }
}
