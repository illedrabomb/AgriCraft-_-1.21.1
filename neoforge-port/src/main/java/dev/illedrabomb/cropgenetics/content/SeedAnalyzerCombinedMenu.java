package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Analyzer plus adjacent chest: original analyzer positions, separate 27-slot pane. */
public final class SeedAnalyzerCombinedMenu extends AbstractContainerMenu {
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

    private final Container analyzer;
    private final Container chest;

    public SeedAnalyzerCombinedMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(ANALYZER_SLOTS), new SimpleContainer(CHEST_SLOTS));
    }

    SeedAnalyzerCombinedMenu(int id, Inventory inventory,
                             Container analyzer, Container chest) {
        super(CropMenus.ANALYZER_CHEST.get(), id);
        checkContainerSize(analyzer, ANALYZER_SLOTS);
        checkContainerSize(chest, CHEST_SLOTS);
        this.analyzer = analyzer;
        this.chest = chest;

        analyzer.startOpen(inventory.player);
        chest.startOpen(inventory.player);

        for (int index = 0; index < ANALYZER_SLOTS; index++) {
            int x = index == 0 || index == 1 ? 26 : -1000;
            int y = index == 0 ? 38 : (index == 1 ? 71 : -1000);
            final int combinedIndex = index;
            this.addSlot(new Slot(analyzer, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stillValid(inventory.player) && analyzer.canPlaceItem(combinedIndex, stack)
                            && super.mayPlace(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return stillValid(player) && super.mayPickup(player);
                }

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
            int x = 199 + (index % 9) * 18;
            int y = 27 + (index / 9) * 18;
            final int combinedIndex = ANALYZER_SLOTS + index;
            this.addSlot(new Slot(chest, index, x, y) {
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return stillValid(inventory.player) && chest.canPlaceItem(combinedIndex - ANALYZER_SLOTS, stack)
                            && super.mayPlace(stack);
                }

                @Override
                public boolean mayPickup(Player player) {
                    return stillValid(player) && super.mayPickup(player);
                }

                @Override
                public int getMaxStackSize() {
                    return maxStorageSlotCount(combinedIndex);
                }
            });
        }

        // Original analyzer player-inventory positions, mirrored on client.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9,
                        13 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 13 + col * 18, 162));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        // When either block is removed or the player moves too far away,
        // stop all interaction with the now-stale combined inventory.
        return (!(analyzer instanceof SeedAnalyzerBlockEntity liveAnalyzer)
                || liveAnalyzer.stillValid(player))
                && (!(chest instanceof SeedChestBlockEntity liveChest)
                || liveChest.stillValid(player));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        // A slot-click packet can still arrive between block removal and
        // the next menu-validity tick. Never move items into or out of a
        // detached analyzer/chest during that short interval.
        if (!stillValid(player)) return original;
        if (index < 0 || index >= slots.size()) return original;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return original;

        ItemStack moving = slot.getItem();
        // A legacy chest may contain non-seed items from an older menu.
        // Block shift-merging more such items into that invalid stack.
        // Removal FROM storage remains allowed so players can recover it.
        if (index >= STORAGE_SLOTS
                && !analyzer.canPlaceItem(0, moving)
                && !analyzer.canPlaceItem(1, moving)
                && !SeedChestRestrictedMenu.canTransferIntoAnySlot(chest, moving))
            return ItemStack.EMPTY;
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
