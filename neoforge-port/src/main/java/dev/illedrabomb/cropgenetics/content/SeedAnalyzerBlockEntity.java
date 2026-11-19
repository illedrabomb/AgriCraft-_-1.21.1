package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Seed slot (0), discovery journal (1), and seven disabled filler slots. */
public final class SeedAnalyzerBlockEntity extends SeedInventoryBlockEntity {
    public SeedAnalyzerBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.SEED_ANALYZER_ENTITY.get(), pos, state, 9);
    }

    // Only the analyzer sends its compact occupied-slot view to watching clients.
    // Seed Chest inventories retain their normal server-only container behavior.
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel server) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, 3);
        }
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
