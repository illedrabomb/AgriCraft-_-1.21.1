package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.Items;

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
    void extremeRepeatedProductEntriesStillYieldOnlyOneStackPerItem() {
        var drops = CropHarvest.products(plant(4096, 4096, 4),
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(123L));
        assertEquals(1, drops.size());
        assertEquals(drops.get(0).getMaxStackSize(), drops.get(0).getCount());
    }

    @Test
    void gainStillIncreasesAmountWithinSingleStack() {
        var drops = CropHarvest.products(plant(1, 1, 1),
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(123L));
        assertEquals(1, drops.size());
        assertEquals(4, drops.get(0).getCount());
    }

    @Test
    void configuredDropMultiplierAppliesAfterGainWithOneStackCap() {
        var boosted = CropHarvest.products(plant(1, 1, 1),
                new Genome("minecraft:wheat_plant", 1, 10, 1),
                RandomSource.create(123L), 3.0D);
        var disabled = CropHarvest.products(plant(1, 1, 1),
                Genome.basic("minecraft:wheat_plant"), RandomSource.create(123L), 0.0D);
        var capped = CropHarvest.products(plant(4096, 4096, 4),
                new Genome("minecraft:wheat_plant", 1, 10, 1),
                RandomSource.create(123L), 64.0D);

        assertEquals(12, boosted.get(0).getCount());
        assertTrue(disabled.isEmpty());
        assertEquals(1, capped.size());
        assertEquals(64, capped.get(0).getCount());
    }

    @Test
    void fractionalMultiplierScalesRawHarvestBeforeApplyingStackLimit() {
        var fractional = CropHarvest.products(plant(128, 128, 1),
                Genome.basic("minecraft:wheat_plant"), RandomSource.create(123L), 0.5D);
        var tiny = CropHarvest.products(plant(4096, 4096, 1),
                Genome.basic("minecraft:wheat_plant"), RandomSource.create(123L), 0.01D);

        // Old order incorrectly produced 32 and 1 by clipping to 64 first.
        assertEquals(64, fractional.get(0).getCount());
        assertEquals(41, tiny.get(0).getCount());
    }

    @Test
    void twoDistinctProductsCanEachYieldOneStack() {
        var base = plant(1, 1, 1);
        var mixed = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:wheat", 1, 1, 1.0D),
                        new PlantDefinition.HarvestProduct("minecraft:wheat_seeds", 1, 1, 1.0D)),
                base.requirements(), base.harvestMode(), base.fruitChance(), base.contactThorns());
        var drops = CropHarvest.products(mixed,
                Genome.basic("minecraft:wheat_plant"), RandomSource.create(123L));
        assertEquals(2, drops.size());
        assertTrue(drops.stream().allMatch(stack -> stack.getCount() == 1));
    }

    @Test
    void sixteenItemStacksRespectTheirNativeLimitEvenWithRepeatedProducts() {
        var base = plant(1, 1, 1);
        var enderPearls = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:ender_pearl", 4096, 4096, 1.0D),
                        new PlantDefinition.HarvestProduct("minecraft:ender_pearl", 4096, 4096, 1.0D)),
                base.requirements(), base.harvestMode(), base.fruitChance(), base.contactThorns());

        var drops = CropHarvest.products(enderPearls,
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(97L), 64.0D);

        assertEquals(1, drops.size());
        assertEquals(Items.ENDER_PEARL, drops.get(0).getItem());
        assertEquals(16, drops.get(0).getCount());
    }

    @Test
    void zeroMultiplierSuppressesEveryProduceTypeWithoutPhantomStacks() {
        var base = plant(1, 1, 1);
        var mixed = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:wheat", 1, 1, 1.0D),
                        new PlantDefinition.HarvestProduct("minecraft:ender_pearl", 1, 1, 1.0D)),
                base.requirements(), base.harvestMode(), base.fruitChance(), base.contactThorns());

        assertTrue(CropHarvest.products(mixed,
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(97L), 0.0D)
                .isEmpty());
    }

    @Test
    void wrongSpeciesNeverPaysOut() {
        assertTrue(CropHarvest.products(plant(1, 1, 1),
                Genome.basic("minecraft:carrot_plant"), RandomSource.create(42L)).isEmpty());
    }

    @Test
    void oneVisibleGourdFruitCanPayMorePumpkinsOrMelonSlices() {
        var base = plant(1, 1, 1);
        var pumpkin = new PlantDefinition("minecraft:pumpkin_plant", base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:pumpkin", 1, 1, 1.0D)),
                base.requirements(), HarvestMode.GOURD, 0.2D, base.contactThorns());
        var melon = new PlantDefinition("minecraft:melon_plant", base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:melon_slice", 1, 1, 1.0D)),
                base.requirements(), HarvestMode.GOURD, 0.2D, base.contactThorns());
        var lowPumpkin = CropHarvest.products(pumpkin,
                new Genome("minecraft:pumpkin_plant", 1, 1, 1), RandomSource.create(17L));
        var highPumpkin = CropHarvest.products(pumpkin,
                new Genome("minecraft:pumpkin_plant", 1, 10, 1), RandomSource.create(17L));
        var highMelon = CropHarvest.products(melon,
                new Genome("minecraft:melon_plant", 1, 10, 1), RandomSource.create(17L));
        var boostedMelon = CropHarvest.products(melon,
                new Genome("minecraft:melon_plant", 1, 10, 1), RandomSource.create(17L), 64.0D);
        var noPumpkins = CropHarvest.products(pumpkin,
                new Genome("minecraft:pumpkin_plant", 1, 10, 1), RandomSource.create(17L), 0.0D);

        assertEquals(1, lowPumpkin.size());
        assertEquals(1, lowPumpkin.get(0).getCount());
        assertEquals(Items.PUMPKIN, lowPumpkin.get(0).getItem());
        assertEquals(1, highPumpkin.size());
        assertTrue(highPumpkin.get(0).getCount() >= 1 && highPumpkin.get(0).getCount() <= 4);
        assertEquals(1, highMelon.size());
        assertEquals(Items.MELON_SLICE, highMelon.get(0).getItem());
        assertTrue(highMelon.get(0).getCount() >= 12 && highMelon.get(0).getCount() <= 16);
        assertEquals(1, boostedMelon.size());
        assertEquals(64, boostedMelon.get(0).getCount());
        assertTrue(noPumpkins.isEmpty());
    }

    @Test
    void unregisteredAndZeroChanceProductsCannotProducePhantomDrops() {
        var base = plant(1, 1, 1);
        var invalid = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("cropgenetics:nonexistent_test_produce", 1, 1, 1.0D),
                        new PlantDefinition.HarvestProduct("minecraft:wheat", 1, 1, 0.0D)),
                base.requirements(), HarvestMode.STANDARD, base.fruitChance(), base.contactThorns());

        assertTrue(CropHarvest.products(invalid,
                new Genome("minecraft:wheat_plant", 1, 10, 1), RandomSource.create(17L)).isEmpty());
    }

    @Test
    void registeredAirProduceIsNotIncludedInTheDropList() {
        var base = plant(1, 1, 1);
        var withAir = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:air", 1, 1, 1.0D),
                        new PlantDefinition.HarvestProduct("minecraft:wheat", 1, 1, 1.0D)),
                base.requirements(), HarvestMode.STANDARD, base.fruitChance(), base.contactThorns());

        var drops = CropHarvest.products(withAir, Genome.basic("minecraft:wheat_plant"),
                RandomSource.create(17L));
        assertEquals(1, drops.size());
        assertEquals(Items.WHEAT, drops.get(0).getItem());
        assertTrue(drops.stream().noneMatch(net.minecraft.world.item.ItemStack::isEmpty));

        var onlyAir = new PlantDefinition(base.id(), base.requiredMods(), base.seedItems(),
                base.stages(), base.harvestStage(), base.growthChance(), base.growthBonus(),
                base.cloneable(), base.spreadChance(),
                List.of(new PlantDefinition.HarvestProduct("minecraft:air", 4096, 4096, 1.0D)),
                base.requirements(), HarvestMode.STANDARD, base.fruitChance(), base.contactThorns());
        assertTrue(CropHarvest.products(onlyAir, Genome.basic("minecraft:wheat_plant"),
                RandomSource.create(17L), 64.0D).isEmpty());
    }
}
