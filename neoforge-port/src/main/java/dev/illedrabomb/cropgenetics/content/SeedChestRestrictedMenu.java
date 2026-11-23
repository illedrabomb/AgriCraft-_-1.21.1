package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Keeps the vanilla 9x3 chest protocol/screen while enforcing the Seed Chest's
 * own item filter for cursor clicks, hotbar swaps and shift-click transfers.
 *
 * The client still creates the matching vanilla ChestMenu; the authoritative
 * server instance alone has to reject illegal writes.
 */
final class SeedChestRestrictedMenu extends ChestMenu {
    SeedChestRestrictedMenu(int id, Inventory inventory, SeedChestBlockEntity chest) {
        super(MenuType.GENERIC_9x3, id, inventory, chest, 3);
    }

    @Override
    protected Slot addSlot(Slot slot) {
        // ChestMenu invokes addSlot from its constructor. This override must
        // not depend on subclass fields that have not been initialized yet.
        if (slot.container instanceof SeedChestBlockEntity) {
            return super.addSlot(restrictPlacement(slot));
        }
        return super.addSlot(slot);
    }

    private static final int CHEST_SLOTS = 27;

    /**
     * Vanilla's quick transfer can attempt merging into an occupied slot.
     * Refuse unrelated player items before delegating, even if a legacy chest
     * still contains an item inserted before its slot filter existed.
     * Taking an old invalid item OUT of the chest is still allowed.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size())
            return ItemStack.EMPTY;
        Slot source = slots.get(index);
        if (index >= CHEST_SLOTS && source.hasItem()
                && !canTransferIntoAnySlot(getContainer(), source.getItem()))
            return ItemStack.EMPTY;
        return super.quickMoveStack(player, index);
    }

    /** Shared shift-click preflight; each destination checks its own filter. */
    static boolean canTransferIntoAnySlot(Container container, ItemStack stack) {
        if (stack.isEmpty()) return false;
        for (int index = 0; index < container.getContainerSize(); index++) {
            if (container.canPlaceItem(index, stack)) return true;
        }
        return false;
    }

    /** Preserve Vanilla slot indices and coordinates; only change mayPlace. */
    static Slot restrictPlacement(Slot original) {
        Container container = original.container;
        int index = original.getContainerSlot();
        return new Slot(container, index, original.x, original.y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return container.canPlaceItem(index, stack) && super.mayPlace(stack);
            }
        };
    }
}
