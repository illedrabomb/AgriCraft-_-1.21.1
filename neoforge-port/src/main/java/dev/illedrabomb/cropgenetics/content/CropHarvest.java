package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/**
 * Shared harvest-drop policy, both for right-click harvesting and block breaking.
 * AgriCraft's gain formula: (gain + 3) / 3 product rolls.
 */
public final class CropHarvest {
    private CropHarvest() {}

    public static List<ItemStack> products(PlantDefinition plant, Genome genome, RandomSource random) {
        if (!plant.id().equals(genome.plantId())) return List.of();

        int gain = genome.gain().expressed(GenePair.TraitMode.MAX);
        int trials = (gain + 3) / 3;
        List<ItemStack> drops = new ArrayList<>();

        for (int trial = 0; trial < trials; trial++) {
            for (PlantDefinition.HarvestProduct product : plant.harvestProducts()) {
                if (random.nextDouble() >= product.chance()) continue;

                ResourceLocation id = ResourceLocation.tryParse(product.itemId());
                if (id == null) continue;
                Item item = BuiltInRegistries.ITEM.getOptional(id).orElse(null);
                if (item == null) continue;

                int count = product.minimum();
                int spread = product.maximum() - product.minimum();
                if (spread > 0) count += random.nextInt(spread + 1);
                while (count > 0) {
                    int part = Math.min(count, item.getDefaultMaxStackSize());
                    drops.add(new ItemStack(item, part));
                    count -= part;
                }
            }
        }
        return drops;
    }
}
