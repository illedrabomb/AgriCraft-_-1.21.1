package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Objects;

/** Server-defined crop with namespaced IDs compatible with AgriPlants records. */
public record PlantDefinition(
        String id, List<String> requiredMods, List<String> seedItems, List<Integer> stages,
        int harvestStage, double growthChance, double growthBonus,
        boolean cloneable, double spreadChance, List<HarvestProduct> harvestProducts,
        PlantRequirements requirements, HarvestMode harvestMode, double fruitChance,
        boolean contactThorns
) {
    /** Preserve the existing constructor for older callers and data fixtures. */
    public PlantDefinition(
            String id, List<String> requiredMods, List<String> seedItems, List<Integer> stages,
            int harvestStage, double growthChance, double growthBonus,
            boolean cloneable, double spreadChance, List<HarvestProduct> harvestProducts,
            PlantRequirements requirements, HarvestMode harvestMode, double fruitChance) {
        this(id, requiredMods, seedItems, stages, harvestStage, growthChance, growthBonus,
                cloneable, spreadChance, harvestProducts, requirements, harvestMode, fruitChance, false);
    }

    public PlantDefinition {
        Objects.requireNonNull(id);
        requiredMods = List.copyOf(requiredMods);
        seedItems = List.copyOf(seedItems);
        stages = List.copyOf(stages);
        harvestProducts = List.copyOf(harvestProducts);
        Objects.requireNonNull(requirements);
        Objects.requireNonNull(harvestMode);
        // Bound imported lists before the registry publishes a plant. Huge
        // datapack arrays must not multiply later harvest/breeding work.
        if (requiredMods.size() > 64 || seedItems.size() > 64
                || stages.size() > 64 || harvestProducts.size() > 64) {
            throw new IllegalArgumentException(
                    "Too many required mods, seed items, growth stages or harvest products (max 64 each)");
        }
        if (stages.stream().anyMatch(stage -> stage < 0 || stage > 4096)) {
            throw new IllegalArgumentException("Growth stage values must be within 0..4096");
        }
        if (!Double.isFinite(fruitChance) || fruitChance < 0 || fruitChance > 1)
            throw new IllegalArgumentException("Invalid fruit chance");
        // A one-stage STANDARD crop would be mature again immediately after
        // every harvest, allowing unlimited drops without any growth ticks.
        if (stages.size() < 2) {
            throw new IllegalArgumentException(
                    "Harvestable crops require at least two growth stages");
        }
        if (harvestMode == HarvestMode.GOURD
                && !id.equals("minecraft:melon_plant")
                && !id.equals("minecraft:pumpkin_plant"))
            throw new IllegalArgumentException("Physical gourd adapter only supports vanilla melon/pumpkin");
        if (harvestMode == HarvestMode.GOURD && (fruitChance <= 0 || stages.size() < 2))
            throw new IllegalArgumentException("Gourd requires positive fruit chance and regrowth stages");
        if (harvestMode != HarvestMode.GOURD && fruitChance != 0)
            throw new IllegalArgumentException("Fruit chance is only defined for gourds");
        if (harvestMode == HarvestMode.GOURD && (harvestProducts.size() != 1
                || harvestProducts.get(0).minimum() != 1 || harvestProducts.get(0).maximum() != 1
                || harvestProducts.get(0).chance() != 1))
            throw new IllegalArgumentException("Gourd requires one guaranteed single fruit product");
        // Keep the datapack registry within the same ID bound as Genome.
        // Long IDs must be diagnosed at reload, not throw later on seed use.
        if (id.length() > 128 || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Invalid plant ID (max 128 characters): " + id);
        }
        if (seedItems.stream().anyMatch(seed -> seed == null || !seed.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")))
            throw new IllegalArgumentException("Invalid seed item ID");
        if (stages.isEmpty() || harvestStage < 0 || harvestStage >= stages.size())
            throw new IllegalArgumentException("Invalid growth stages");
        if (!Double.isFinite(growthChance) || !Double.isFinite(growthBonus) || !Double.isFinite(spreadChance) || growthChance < 0 || growthBonus < 0 || spreadChance < 0 || spreadChance > 1)
            throw new IllegalArgumentException("Invalid growth/spread chance");
    }

    public record HarvestProduct(String itemId, int minimum, int maximum, double chance) {
        public HarvestProduct {
            Objects.requireNonNull(itemId);
            if (!itemId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+") ||
                minimum < 0 || maximum < minimum || maximum > 4096 || !Double.isFinite(chance) || chance < 0 || chance > 1)
                throw new IllegalArgumentException("Invalid harvest product");
        }
    }
}
