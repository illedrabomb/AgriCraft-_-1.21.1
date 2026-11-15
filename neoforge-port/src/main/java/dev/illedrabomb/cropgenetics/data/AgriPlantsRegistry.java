package dev.illedrabomb.cropgenetics.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Server-side loaded datapack plant registry. The plant catalogue and its
 * reverse seed index are published together, avoiding stale seed mappings.
 */
public final class AgriPlantsRegistry {
    private volatile PlantCatalogue catalogue = new PlantCatalogue(Map.of(), Map.of(), Set.of());
    private volatile Map<String, MutationDefinition> mutations = Map.of();

    public Map<String, PlantDefinition> plants() { return catalogue.plants(); }
    public Map<String, MutationDefinition> mutations() { return mutations; }

    /**
     * Return the plant mapped unambiguously to a plain vanilla/mod item.
     * If multiple plant definitions claim the same item, refuse conversion
     * rather than silently selecting the wrong species.
     */
    public Optional<PlantDefinition> plantForSeed(String itemId) {
        PlantCatalogue current = catalogue;
        String plantId = current.seedToPlant().get(itemId);
        return plantId == null ? Optional.empty() : Optional.ofNullable(current.plants().get(plantId));
    }

    public Set<String> ambiguousSeedItems() { return catalogue.ambiguousSeedItems(); }

    public void replacePlants(Map<String, PlantDefinition> next) {
        Map<String, PlantDefinition> plants = Map.copyOf(next);
        Map<String, String> reverse = new HashMap<>();
        Set<String> ambiguous = new HashSet<>();

        // Sort for reproducibility; an ambiguous mapping is always excluded.
        plants.values().stream().sorted(java.util.Comparator.comparing(PlantDefinition::id))
                .forEach(plant -> {
                    for (String itemId : plant.seedItems()) {
                        if (ambiguous.contains(itemId)) continue;
                        String previous = reverse.putIfAbsent(itemId, plant.id());
                        if (previous != null && !previous.equals(plant.id())) {
                            reverse.remove(itemId);
                            ambiguous.add(itemId);
                        }
                    }
                });
        catalogue = new PlantCatalogue(plants, Map.copyOf(reverse), Set.copyOf(ambiguous));
    }

    public void replaceMutations(Map<String, MutationDefinition> next) { mutations = Map.copyOf(next); }

    private record PlantCatalogue(Map<String, PlantDefinition> plants,
                                  Map<String, String> seedToPlant,
                                  Set<String> ambiguousSeedItems) { }
}
