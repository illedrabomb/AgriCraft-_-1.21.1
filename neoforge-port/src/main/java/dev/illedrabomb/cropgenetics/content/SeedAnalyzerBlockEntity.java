package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

    // World watchers receive only the icon item ID and journal occupancy.
    // The original ItemStacks (including hidden genes and researched species)
    // are never broadcast to every nearby player by block-entity packets.
    private ItemStack clientDisplaySeed = ItemStack.EMPTY;
    private boolean clientDisplayJournal;

    public ItemStack displaySeed() {
        return level != null && level.isClientSide ? clientDisplaySeed : getItem(0);
    }

    public ItemStack displayJournal() {
        return level != null && level.isClientSide
                ? (clientDisplayJournal ? new ItemStack(CropItems.SEED_JOURNAL.get()) : ItemStack.EMPTY)
                : getItem(1);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("DisplayOnly", true);
        ItemStack seed = getItem(0);
        if (!seed.isEmpty()) {
            tag.putString("DisplaySeedId", BuiltInRegistries.ITEM.getKey(seed.getItem()).toString());
        }
        tag.putBoolean("DisplayJournal", !getItem(1).isEmpty());
        return tag;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.getBoolean("DisplayOnly")) {
            String itemId = tag.getString("DisplaySeedId");
            ResourceLocation parsed = ResourceLocation.tryParse(itemId);
            clientDisplaySeed = parsed == null || !BuiltInRegistries.ITEM.containsKey(parsed)
                    ? ItemStack.EMPTY : new ItemStack(BuiltInRegistries.ITEM.get(parsed));
            clientDisplayJournal = tag.getBoolean("DisplayJournal");
            // Never overwrite the client-side menu slots with the limited
            // visual packet. Inventory contents come from the container sync.
            return;
        }
        super.loadAdditional(tag, registries);
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
