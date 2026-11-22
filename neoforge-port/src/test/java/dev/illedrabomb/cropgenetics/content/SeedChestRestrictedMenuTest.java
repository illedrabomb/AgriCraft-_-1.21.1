package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Regression for server-side slot filtering in the standalone Seed Chest. */
class SeedChestRestrictedMenuTest {
    @Test
    void wrapperPreservesOriginalChestPositionAndIndex() {
        SimpleContainer inventory = new SimpleContainer(27);
        Slot original = new Slot(inventory, 17, 44, 72);
        Slot guarded = SeedChestRestrictedMenu.restrictPlacement(original);
        assertSame(inventory, guarded.container);
        assertEquals(17, guarded.getContainerSlot());
        assertEquals(44, guarded.x);
        assertEquals(72, guarded.y);
    }

    @Test
    void wrapperDelegatesEveryPlacementToContainerRules() {
        // The menu must honor the container's answer, rather than Vanilla's
        // permissive default Slot.mayPlace (the UI-01 regression pattern).
        SimpleContainer inventory = new SimpleContainer(27) {
            @Override
            public boolean canPlaceItem(int index, ItemStack stack) {
                return index == 5 && stack.is(Items.WHEAT_SEEDS);
            }
        };
        Slot allowed = SeedChestRestrictedMenu.restrictPlacement(new Slot(inventory, 5, 8, 18));
        Slot blocked = SeedChestRestrictedMenu.restrictPlacement(new Slot(inventory, 6, 26, 18));

        assertTrue(allowed.mayPlace(new ItemStack(Items.WHEAT_SEEDS)));
        assertFalse(allowed.mayPlace(new ItemStack(Items.STICK)));
        assertFalse(blocked.mayPlace(new ItemStack(Items.WHEAT_SEEDS)));
    }
}
