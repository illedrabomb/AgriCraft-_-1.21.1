package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Both server menu layouts must delegate insertion to analyzer.canPlaceItem. */
class SeedAnalyzerSlotRulesTest {
    @Test
    void onlySeedAndJournalSlotsAcceptTheirMatchingItems() {
        SeedAnalyzerBlockEntity analyzer = new SeedAnalyzerBlockEntity(
                BlockPos.ZERO, CropBlocks.SEED_ANALYZER.get().defaultBlockState());
        ItemStack seed = new ItemStack(CropItems.GENETIC_SEED.get(), 64);
        ItemStack journal = new ItemStack(CropItems.SEED_JOURNAL.get());

        assertTrue(analyzer.canPlaceItem(0, seed));
        assertFalse(analyzer.canPlaceItem(0, journal));
        assertTrue(analyzer.canPlaceItem(1, journal));
        assertFalse(analyzer.canPlaceItem(1, seed));
        for (int slot = 2; slot < analyzer.getContainerSize(); slot++) {
            assertFalse(analyzer.canPlaceItem(slot, seed));
            assertFalse(analyzer.canPlaceItem(slot, journal));
            assertFalse(analyzer.canPlaceItem(slot, new ItemStack(Items.STICK)));
        }
        assertEquals(1, analyzer.getMaxStackSize());
    }
}
