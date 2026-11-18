package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** 27 persisted storage slots displayed below an adjacent analyzer seed slot. */
public final class SeedChestBlockEntity extends SeedInventoryBlockEntity {
    public SeedChestBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.SEED_CHEST_ENTITY.get(), pos, state, 27);
    }

    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        // Preserve all custom genome components; slots store full ItemStacks.
        return CropItems.hasGenome(stack) || stack.is(CropItems.GENETIC_SEED.get())
                || CropGeneticsSeedRecognition.isRegisteredSeed(stack);
    }
}
