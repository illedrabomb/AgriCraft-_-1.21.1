package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;

/** Growth calculations derived from AgriCraft; soil multipliers follow later. */
public final class CropGrowth {
    private CropGrowth() {}

    public static double chance(PlantDefinition plant, Genome genome) {
        int growth = genome.growth().expressed(GenePair.TraitMode.MAX);
        return Math.max(0D, Math.min(1D, plant.growthChance() + growth * plant.growthBonus()));
    }

    /**
     * Transfer growth progress between stage-count revisions of a species.
     * Both endpoints are kept exact; interior stages use nearest rounding.
     * An older single-stage species was always mature.
     */
    public static int remapStage(int age, int oldTotal, int newTotal) {
        if (newTotal <= 1) return 0;
        if (oldTotal <= 1) return newTotal - 1;
        int boundedAge = Math.max(0, Math.min(age, oldTotal - 1));
        return (int) (((long) boundedAge * (newTotal - 1)
                + (oldTotal - 1) / 2L) / (oldTotal - 1));
    }

    /**
     * A harvested crop must resume below maturity. This is shared by
     * ordinary harvests and removal of an owned physical melon/pumpkin.
     * If a datapack chooses its last stage as harvestStage, start at zero
     * rather than allowing an immediate second harvest without regrowth.
     */
    public static int regrowthStage(PlantDefinition plant) {
        int matureStage = plant.stages().size() - 1;
        return plant.harvestStage() >= matureStage
                ? 0 : Math.max(0, plant.harvestStage());
    }

    public static int displayStage(int age, int totalStages) {
        if (totalStages <= 1) return 7;
        return Math.max(0, Math.min(7, (int) ((long) age * 7 / (totalStages - 1))));
    }
}
