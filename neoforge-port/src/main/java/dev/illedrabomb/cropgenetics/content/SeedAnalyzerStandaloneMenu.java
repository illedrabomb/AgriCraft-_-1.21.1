package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Vanilla one-row chest layout with the analyzer's real slot restrictions.
 * Client still uses GENERIC_9x1; only server-side slot validation differs.
 */
final class SeedAnalyzerStandaloneMenu extends AbstractContainerMenu {
    private static final int ANALYZER_SLOTS = 9;
    private static final int PLAYER_SLOTS = 36;

    private final SeedAnalyzerBlockEntity analyzer;

    SeedAnalyzerStandaloneMenu(int id, Inventory inventory, SeedAnalyzerBlockEntity analyzer) {
        super(MenuType.GENERIC_9x1, id);
        checkContainerSize(analyzer, ANALYZER_SLOTS);
        this.analyzer = analyzer;
        analyzer.startOpen(inventory.player);

        for (int index = 0; index < ANALYZER_SLOTS; index++) {
            final int slotIndex = index;
            addSlot(new Slot(analyzer, index, 8 + index * 18, 18) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stillValid(inventory.player)
                            && analyzer.canPlaceItem(slotIndex, stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return stillValid(player) && super.mayPickup(player);
                }

                @Override
                public int getMaxStackSize() {
                    return 1;
                }
            });
        }
        // Match ChestMenu.oneRow slot coordinates and packet slot order.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18, 49 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 8 + col * 18, 107));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return analyzer.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (!stillValid(player) || index < 0 || index >= slots.size())
            return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack moving = slot.getItem();
        ItemStack original = moving.copy();

        if (index < ANALYZER_SLOTS) {
            if (!moveItemStackTo(moving, ANALYZER_SLOTS,
                    ANALYZER_SLOTS + PLAYER_SLOTS, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(moving, 0, ANALYZER_SLOTS, false))
                return ItemStack.EMPTY;
        }
        if (moving.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
        else slot.setChanged();
        if (moving.getCount() == original.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, moving);
        return original;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        analyzer.stopOpen(player);
    }
}
