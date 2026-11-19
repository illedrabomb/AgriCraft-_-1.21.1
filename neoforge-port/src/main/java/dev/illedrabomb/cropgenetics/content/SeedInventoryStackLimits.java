package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.item.ItemStack;

/** Preserve the stricter of the inventory capacity and the item's own limit. */
final class SeedInventoryStackLimits {
    private SeedInventoryStackLimits() {}

    static int allowedCount(ItemStack stack, int containerLimit) {
        if (stack.isEmpty()) return 0;
        return Math.max(1, Math.min(Math.max(1, containerLimit),
                Math.max(1, stack.getMaxStackSize())));
    }
}
