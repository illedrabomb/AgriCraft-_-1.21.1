package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.genetics.GenomeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

/**
 * The genome is owned by the crop block entity while planted, and by the
 * seed data component while in an inventory.
 */
public final class CropSticksBlockEntity extends BlockEntity {
    @Nullable private Genome genome;

    public CropSticksBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.CROP_STICKS_ENTITY.get(), pos, state);
    }

    @Nullable
    public Genome genome() {
        return genome;
    }

    public boolean plant(Genome seedGenome) {
        if (genome != null || seedGenome == null) return false;
        this.genome = seedGenome;
        setChanged();
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(CropSticksBlock.PLANTED, true), 3);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (genome != null) {
            GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, genome)
                    .result().ifPresent(value -> tag.put("Genome", value));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        genome = null;
        if (tag.contains("Genome")) {
            GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag.get("Genome"))
                    .result().ifPresent(value -> genome = value);
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
