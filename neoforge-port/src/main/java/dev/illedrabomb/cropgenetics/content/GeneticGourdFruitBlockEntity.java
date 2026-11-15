package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
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

    public void bind(BlockPos parent, UUID token) {
        if (level == null || level.isClientSide || parentPos != null || fruitToken != null) return;
        parentPos = parent.immutable();
        fruitToken = token;
        setChanged();
        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public boolean ownedBy(BlockPos parent, UUID token) {
        return parentPos != null && parentPos.equals(parent) && token.equals(fruitToken);
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
