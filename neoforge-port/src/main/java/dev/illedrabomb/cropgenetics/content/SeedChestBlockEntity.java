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
        // The client does not own the active datapack plant catalogue.
        // Predict slot acceptance locally; the server checks real species.
        return !stack.isEmpty() && (level == null || level.isClientSide
                || CropItems.hasGenome(stack)
                || CropGeneticsSeedRecognition.isRegisteredSeed(stack));
    }
}
