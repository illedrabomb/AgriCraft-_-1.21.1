package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

/**
 * Restoring a serialized inventory replaces its previous snapshot, including
 * slots which have no entry in the incoming NBT list.
 */
final class SeedInventoryRestoreRules {
    private SeedInventoryRestoreRules() {}

    static void clearBeforeLoad(NonNullList<ItemStack> slots) {
        for (int slot = 0; slot < slots.size(); slot++) {
            slots.set(slot, ItemStack.EMPTY);
        }
    }
}
