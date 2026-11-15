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

    public static int displayStage(int age, int totalStages) {
        if (totalStages <= 1) return 7;
        return Math.max(0, Math.min(7, (int) ((long) age * 7 / (totalStages - 1))));
    }
}
