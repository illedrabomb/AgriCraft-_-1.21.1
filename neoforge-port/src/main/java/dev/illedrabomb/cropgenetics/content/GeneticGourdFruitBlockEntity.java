package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import java.util.UUID;
import javax.annotation.Nullable;

/** Each physical fruit remembers its actual parent and a unique fruit token. */
public final class GeneticGourdFruitBlockEntity extends BlockEntity {
    @Nullable private BlockPos parentPos;
    @Nullable private UUID fruitToken;

    public GeneticGourdFruitBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.GOURD_FRUIT_ENTITY.get(), pos, state);
    }

    /**
     * Bind only a horizontally adjacent, loaded crop stem. Returning false
     * allows placement to roll back instead of leaving an ownerless fruit.
     */
    public boolean bind(BlockPos parent, UUID token) {
        if (level == null || level.isClientSide || parent == null || token == null
                || parentPos != null || fruitToken != null
                || !level.hasChunkAt(parent)
                || parent.getY() != worldPosition.getY()
                || Math.abs(parent.getX() - worldPosition.getX())
                        + Math.abs(parent.getZ() - worldPosition.getZ()) != 1
                || !(level.getBlockEntity(parent) instanceof CropSticksBlockEntity stem)
                || stem.genome() == null) return false;
        parentPos = parent.immutable();
        fruitToken = token;
        setChanged();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
        return true;
    }

    public boolean ownedBy(BlockPos parent, UUID token) {
        return parentPos != null && parentPos.equals(parent) && token.equals(fruitToken);
    }

    /**
     * When a stem is broken, leave its existing fruit as an ordinary
     * harvestable world block, but remove the stale parent reference.
     * Tokens must match so an unrelated/new stem cannot detach this fruit.
     */
    public boolean detachIfOwnedBy(BlockPos parent, UUID token) {
        if (!(level instanceof ServerLevel) || !ownedBy(parent, token)) return false;
        detachOwner();
        return true;
    }

    /**
     * Check ownership after both chunks are available. Never force a chunk
     * load or detach while the parent chunk is absent; the fruit may still
     * have a perfectly valid stem across the chunk boundary.
     */
    public boolean detachIfOrphaned(ServerLevel server) {
        if (level != server || parentPos == null || fruitToken == null
                || !server.hasChunkAt(parentPos)) return false;

        if (server.getBlockState(parentPos).is(CropBlocks.CROP_STICKS.get())) {
            // A crop-stick BE may not yet be available during chunk loading.
            // Wait for a subsequent check rather than making a false orphan.
            if (!(server.getBlockEntity(parentPos) instanceof CropSticksBlockEntity stem))
                return false;
            if (stem.fruitReady() && fruitToken.equals(stem.fruitToken())
                    && stem.fruitDirection() != null && stem.genome() != null
                    && parentPos.relative(stem.fruitDirection()).equals(worldPosition)
                    && GourdFruits.matchesOwned(server, parentPos, stem.fruitDirection(),
                            stem.genome().plantId(), fruitToken)) return false;
        }

        detachOwner();
        return true;
    }

    private void detachOwner() {
        parentPos = null;
        fruitToken = null;
        setChanged();
        // This is a metadata-only change: no world-block replacement,
        // no loot and no attempt to mutate a different stem.
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Nullable public BlockPos parentPos() { return parentPos; }
    @Nullable public UUID fruitToken() { return fruitToken; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (parentPos != null && fruitToken != null) {
            tag.putInt("ParentX", parentPos.getX());
            tag.putInt("ParentY", parentPos.getY());
            tag.putInt("ParentZ", parentPos.getZ());
            tag.putUUID("FruitToken", fruitToken);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        parentPos = null;
        fruitToken = null;
        if (tag.contains("ParentX") && tag.contains("ParentY")
                && tag.contains("ParentZ") && tag.hasUUID("FruitToken")) {
            parentPos = new BlockPos(tag.getInt("ParentX"), tag.getInt("ParentY"), tag.getInt("ParentZ"));
            fruitToken = tag.getUUID("FruitToken");
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
