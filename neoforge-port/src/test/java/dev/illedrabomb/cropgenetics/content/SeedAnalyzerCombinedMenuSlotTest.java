package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Four-row combined menu shares Vanilla's 36 storage-slot layout on both
 * client and server, but must enforce different per-slot stack capacities.
 */
class SeedAnalyzerCombinedMenuSlotTest {
    @Test
    void analyzerTopRowHasOnlyOneItemPerSlot() {
        assertEquals(9, SeedAnalyzerCombinedMenu.ANALYZER_SLOTS);
        for (int slot = 0; slot < 9; slot++) {
            assertEquals(1, SeedAnalyzerCombinedMenu.maxStorageSlotCount(slot));
        }
    }

    @Test
    void adjacentChestRetainsNormalStacks() {
        assertEquals(27, SeedAnalyzerCombinedMenu.CHEST_SLOTS);
        assertEquals(36, SeedAnalyzerCombinedMenu.STORAGE_SLOTS);
        for (int slot = 9; slot < 36; slot++) {
            assertEquals(64, SeedAnalyzerCombinedMenu.maxStorageSlotCount(slot));
        }
    }

    @Test
    void invalidIndicesCannotBeMistakenForChestSlots() {
        assertThrows(IllegalArgumentException.class,
                () -> SeedAnalyzerCombinedMenu.maxStorageSlotCount(-1));
        assertThrows(IllegalArgumentException.class,
                () -> SeedAnalyzerCombinedMenu.maxStorageSlotCount(36));
    }
}
