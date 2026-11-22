package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeedInventoryStackLimitsTest {
    @Test
    void geneticSeedCanFillChestStackButNotAnalyzerSlot() {
        ItemStack seed = new ItemStack(CropItems.GENETIC_SEED.get());
        assertEquals(64, SeedInventoryStackLimits.allowedCount(seed, 64));
        assertEquals(1, SeedInventoryStackLimits.allowedCount(seed, 1));
    }

    @Test
    void ordinarySeedsRetainNormalStackingInChest() {
        assertEquals(64, SeedInventoryStackLimits.allowedCount(
                new ItemStack(Items.WHEAT_SEEDS), 64));
    }

    @Test
    void analyzerIsStillLimitedToOneOrdinarySeed() {
        assertEquals(1, SeedInventoryStackLimits.allowedCount(
                new ItemStack(Items.WHEAT_SEEDS), 1));
    }

    @Test
    void itemLimitAlsoWinsAgainstLargerContainerCapacity() {
        assertEquals(16, SeedInventoryStackLimits.allowedCount(
                new ItemStack(Items.ENDER_PEARL), 64));
    }

    @Test
    void emptyStackCannotBeStored() {
        assertEquals(0, SeedInventoryStackLimits.allowedCount(ItemStack.EMPTY, 64));
    }
}
