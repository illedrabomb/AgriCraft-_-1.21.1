package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** One seed slot plus eight disabled GUI cells to fit a vanilla 1-row menu. */
public final class SeedAnalyzerBlockEntity extends SeedInventoryBlockEntity {
    public SeedAnalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.SEED_ANALYZER_ENTITY.get(), pos, state, 9);
    }

    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == 0 && !stack.isEmpty();
    }
    @Override protected void onStored(int slot, ItemStack stack) {
        // A genetic seed already owns its genome before analysis. This flag
        // only reveals that genome; it never creates a new mutation value.
        if (slot == 0 && !stack.isEmpty() && level != null && !level.isClientSide)
            CropItems.analyzeSeed(stack);
    }
}
