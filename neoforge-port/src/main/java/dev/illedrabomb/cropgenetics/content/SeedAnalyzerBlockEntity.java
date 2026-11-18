package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Seed slot (0), discovery journal (1), and seven disabled filler slots. */
public final class SeedAnalyzerBlockEntity extends SeedInventoryBlockEntity {
    public SeedAnalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.SEED_ANALYZER_ENTITY.get(), pos, state, 9);
    }

    @Override public int getMaxStackSize() { return 1; }
    @Override public boolean canPlaceItem(int slot, ItemStack stack) {
        // Client side allows placement prediction; the server only accepts
        // registered seeds or stacks already carrying genetic information.
        if (slot == 1) return stack.is(CropItems.SEED_JOURNAL.get());
        return slot == 0 && !stack.isEmpty()
                && (level == null || level.isClientSide
                        || CropItems.hasGenome(stack)
                        || CropGeneticsSeedRecognition.isRegisteredSeed(stack));
    }
    @Override protected void onStored(int slot, ItemStack stack) {
        // A genetic seed already owns its genome before analysis. This flag
        // only reveals that genome; it never creates a new mutation value.
        if (level == null || level.isClientSide) return;
        if (slot == 0 && !stack.isEmpty()) CropItems.analyzeSeed(stack);
        ItemStack seed = getItem(0);
        ItemStack journal = getItem(1);
        if (journal.is(CropItems.SEED_JOURNAL.get())) {
            boolean changed = false;
            if (CropItems.isAnalyzed(seed) && CropItems.hasGenome(seed)) {
                changed = SeedJournalItem.recordDiscovery(journal,
                        seed.get(CropComponents.GENOME.get()).plantId());
            }
            changed |= SeedJournalItem.refreshKnownMutations(
                    journal, CropGeneticsMod.plants().snapshot().mutations());
            if (changed) setChanged();
        }
    }
}
