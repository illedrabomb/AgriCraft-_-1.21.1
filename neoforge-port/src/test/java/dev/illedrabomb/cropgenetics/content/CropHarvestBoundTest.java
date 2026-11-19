package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CropHarvestBoundTest {
    private static PlantDefinition plant(int minimum, int maximum, int productCount) {
        List<PlantDefinition.HarvestProduct> products =
                java.util.stream.IntStream.range(0, productCount)
                        .mapToObj(ignored -> new PlantDefinition.HarvestProduct(
                                "minecraft:wheat", minimum, maximum, 1.0D))
                        .toList();
        return new PlantDefinition(
                "minecraft:wheat_plant", List.of(), List.of("minecraft:wheat_seeds"),
                List.of(0, 1), 1, 0.75D, 0.025D, true, 0.0D,
                products, new PlantRequirements(
                        0, 15, Optional.of("minecraft:farmland"), Optional.empty(), 0),
                HarvestMode.STANDARD, 0.0D);
    }

    @Test
    void normalHarvestStillUsesExpectedGainRolls() {
        var drops = CropHarvest.products(plant(1, 1, 1),
                Genome.basic("minecraft:wheat_plant"), RandomSource.create(123L));
        assertEquals(1, drops.size());
        assertEquals(1, drops.get(0).getCount());
    }

    @Test
    void extremeValidDatapackProductsCannotEmitUnboundedStacks() {
        var drops = CropHarvest.products(plant(4096, 4096, 4),
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(123L));
        assertEquals(CropHarvest.MAX_DROP_STACKS, drops.size());
        assertTrue(drops.stream().allMatch(stack -> stack.getCount() > 0));
    }

    @Test
    void wrongSpeciesNeverPaysOut() {
        assertTrue(CropHarvest.products(plant(1, 1, 1),
                Genome.basic("minecraft:carrot_plant"), RandomSource.create(42L)).isEmpty());
    }
}
