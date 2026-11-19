package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Covers replacement semantics before ContainerHelper loads sparse NBT slots. */
class SeedInventoryRestoreRulesTest {
    @Test
    void staleItemsAreClearedWhenLoadingAnEmptySnapshot() {
        NonNullList<ItemStack> slots = NonNullList.withSize(27, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.WHEAT_SEEDS, 64));
        slots.set(12, new ItemStack(Items.CARROT, 5));

        SeedInventoryRestoreRules.clearBeforeLoad(slots);

        assertEquals(27, slots.size());
        assertTrue(slots.stream().allMatch(ItemStack::isEmpty));
    }

    @Test
    void clearDoesNotShrinkAnalyzerInventory() {
        NonNullList<ItemStack> slots = NonNullList.withSize(9, ItemStack.EMPTY);
        slots.set(0, new ItemStack(Items.WHEAT_SEEDS));
        SeedInventoryRestoreRules.clearBeforeLoad(slots);

        assertEquals(9, slots.size());
        assertTrue(slots.get(0).isEmpty());
    }
}
