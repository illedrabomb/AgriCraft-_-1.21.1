package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared harvest-drop policy, both for right-click harvesting and block breaking.
 * AgriCraft's gain formula: (gain + 3) / 3 product rolls.
 */
public final class CropHarvest {
    private CropHarvest() {}

    public static List<ItemStack> products(PlantDefinition plant, Genome genome, RandomSource random) {
        if (!plant.id().equals(genome.plantId())) return List.of();

        // One visible fruit forms per gourd lifecycle, but Gain also affects
        // the harvested amount. Keep the entire payout within one item stack.
        if (plant.harvestMode() == HarvestMode.GOURD) {
            if (plant.id().equals("minecraft:melon_plant")) {
                // Vanilla melon gives slices; each Gain level contributes
                // one additional slice to the normal 3..7 payout.
                int slices = GourdHarvestRules.melonSlices(genome.gain(), random.nextInt(5));
                return List.of(new ItemStack(Items.MELON_SLICE,
                        Math.min(Items.MELON_SLICE.getDefaultMaxStackSize(), slices)));
            }
            if (plant.id().equals("minecraft:pumpkin_plant")) {
                // Gain 2: 90% one / 10% two; high Gain unlocks three/four.
                int pumpkins = GourdHarvestRules.pumpkinCount(genome.gain(), random.nextDouble());
                return List.of(new ItemStack(Items.PUMPKIN,
                        Math.min(GourdHarvestRules.MAX_DROP, pumpkins)));
            }
            return List.of();
        }

        int gain = genome.gain();
        int trials = (gain + 3) / 3;
        // Merge all gain rolls by actual item identity. Each distinct fruit
        // or produce item may yield at most ONE normal inventory stack per
        // harvest; multiple datapack product entries cannot bypass the limit.
        Map<Item, Integer> counts = new LinkedHashMap<>();

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
                if (count <= 0) continue;
                int stackLimit = Math.max(1, item.getDefaultMaxStackSize());
                counts.merge(item, Math.min(count, stackLimit),
                        (previous, added) -> Math.min(stackLimit, previous + added));
            }
        }
        List<ItemStack> drops = new ArrayList<>(counts.size());
        counts.forEach((item, count) -> drops.add(new ItemStack(item, count)));
        return drops;
    }
}
