package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Server-side four-row analyzer/chest menu with real per-slot limits.
 *
 * The standard GENERIC_9x4 client screen draws the identical 36 storage
 * slots in the identical order. A CompoundContainer alone loses the
 * analyzer's one-item slot limit when its combined inventory is presented
 * as a generic chest: generic slots may then try to insert a 64-item stack,
 * although the underlying analyzer stores only one item.
 */
final class SeedAnalyzerCombinedMenu extends AbstractContainerMenu {
    static final int ANALYZER_SLOTS = 9;
    static final int CHEST_SLOTS = 27;
    static final int STORAGE_SLOTS = ANALYZER_SLOTS + CHEST_SLOTS;
    private static final int PLAYER_SLOTS = 36;

    /**
     * Slot capacity differs between the analyzer's top row and the chest.
     * Keep the mapping explicit so shift-click and cursor placement agree.
     */
    static int maxStorageSlotCount(int combinedIndex) {
        if (combinedIndex < 0 || combinedIndex >= STORAGE_SLOTS)
            throw new IllegalArgumentException("Invalid combined storage slot " + combinedIndex);
        return combinedIndex < ANALYZER_SLOTS ? 1 : 64;
    }

    private final SeedAnalyzerBlockEntity analyzer;
    private final SeedChestBlockEntity chest;

    SeedAnalyzerCombinedMenu(int id, Inventory inventory,
                             SeedAnalyzerBlockEntity analyzer, SeedChestBlockEntity chest) {
        super(MenuType.GENERIC_9x4, id);
        checkContainerSize(analyzer, ANALYZER_SLOTS);
        checkContainerSize(chest, CHEST_SLOTS);
        this.analyzer = analyzer;
        this.chest = chest;

        analyzer.startOpen(inventory.player);
        chest.startOpen(inventory.player);

        for (int index = 0; index < ANALYZER_SLOTS; index++) {
            int x = 8 + (index % 9) * 18;
            int y = 18;
            final int combinedIndex = index;
            this.addSlot(new Slot(analyzer, index, x, y) {
                @Override
                public int getMaxStackSize() {
                    // Slot zero: seed; slot one: journal; remaining slots
                    // disabled by analyzer.canPlaceItem. Never accept 64
                    // from a shift-click or a carried stack here.
                    return maxStorageSlotCount(combinedIndex);
                }
            });
        }
        for (int index = 0; index < CHEST_SLOTS; index++) {
            int x = 8 + (index % 9) * 18;
            int y = 36 + (index / 9) * 18;
            final int combinedIndex = ANALYZER_SLOTS + index;
            this.addSlot(new Slot(chest, index, x, y) {
                @Override
                public int getMaxStackSize() {
                    return maxStorageSlotCount(combinedIndex);
                }
            });
        }

        // Match vanilla ChestMenu.fourRows exactly. The client receives the
        // standard MenuType.GENERIC_9x4 with no new menu registration.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        8 + col * 18, 103 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 161));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // When either block is removed or the player moves too far away,
        // stop all interaction with the now-stale combined inventory.
        return analyzer.stillValid(player) && chest.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        if (index < 0 || index >= slots.size()) return original;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return original;

        ItemStack moving = slot.getItem();
        original = moving.copy();
        if (index < STORAGE_SLOTS) {
            if (!moveItemStackTo(moving, STORAGE_SLOTS,
                    STORAGE_SLOTS + PLAYER_SLOTS, true)) return ItemStack.EMPTY;
        } else {
            if (!moveItemStackTo(moving, 0, STORAGE_SLOTS, false))
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
        chest.stopOpen(player);
    }
}
