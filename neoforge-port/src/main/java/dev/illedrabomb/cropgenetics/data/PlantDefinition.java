package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Objects;

/** Server-defined crop with namespaced IDs compatible with AgriPlants records. */
public record PlantDefinition(
        String id, List<String> requiredMods, List<String> seedItems, List<Integer> stages,
        int harvestStage, double growthChance, double growthBonus,
        boolean cloneable, double spreadChance, List<HarvestProduct> harvestProducts
) {
    public PlantDefinition {
        Objects.requireNonNull(id);
        requiredMods = List.copyOf(requiredMods);
        seedItems = List.copyOf(seedItems);
        stages = List.copyOf(stages);
        harvestProducts = List.copyOf(harvestProducts);
        if (!id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) throw new IllegalArgumentException("Invalid plant ID: " + id);
        if (stages.isEmpty() || harvestStage < 0 || harvestStage >= stages.size())
            throw new IllegalArgumentException("Invalid growth stages");
        if (growthChance < 0 || growthBonus < 0 || spreadChance < 0 || spreadChance > 1)
            throw new IllegalArgumentException("Invalid growth/spread chance");
    }

    public record HarvestProduct(String itemId, int minimum, int maximum, double chance) {
        public HarvestProduct {
            Objects.requireNonNull(itemId);
            if (!itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") ||
                minimum < 0 || maximum < minimum || chance < 0 || chance > 1)
                throw new IllegalArgumentException("Invalid harvest product");
        }
    }
}
