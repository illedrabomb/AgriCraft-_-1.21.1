package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Original analyzer layout with server-owned slot restrictions.
 * The client uses synchronized dummy storage, not real world block entities.
 */
public final class SeedAnalyzerStandaloneMenu extends AbstractContainerMenu {
    private static final int ANALYZER_SLOTS = 9;
    private static final int PLAYER_SLOTS = 36;

    private final Container analyzer;

    public SeedAnalyzerStandaloneMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(ANALYZER_SLOTS));
    }

    SeedAnalyzerStandaloneMenu(int id, Inventory inventory, Container analyzer) {
        super(CropMenus.ANALYZER.get(), id);
        checkContainerSize(analyzer, ANALYZER_SLOTS);
        this.analyzer = analyzer;
        analyzer.startOpen(inventory.player);

        for (int index = 0; index < ANALYZER_SLOTS; index++) {
            final int slotIndex = index;
            addSlot(new Slot(analyzer, index,
                    index == 0 ? 26 : (index == 1 ? 26 : -1000),
                    index == 0 ? 38 : (index == 1 ? 71 : -1000)) {
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
        // Original analyzer inventory coordinates; keep slot order stable.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9,
                        13 + col * 18, 104 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, 13 + col * 18, 162));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return !(analyzer instanceof SeedAnalyzerBlockEntity live)
                || live.stillValid(player);
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
