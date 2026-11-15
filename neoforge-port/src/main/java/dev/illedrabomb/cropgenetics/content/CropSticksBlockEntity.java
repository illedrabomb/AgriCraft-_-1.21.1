package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.genetics.GenomeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;

/** Owns the exact growth stage and the genome of a planted crop. */
public final class CropSticksBlockEntity extends BlockEntity {
    @Nullable private Genome genome;
    private int growthStage;

    public CropSticksBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.CROP_STICKS_ENTITY.get(), pos, state);
    }

    @Nullable public Genome genome() { return genome; }
    public int growthStage() { return growthStage; }

    public boolean plant(Genome seedGenome) {
        if (genome != null || seedGenome == null || level == null || level.isClientSide
                || getBlockState().getValue(CropSticksBlock.CROSS)) return false;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(seedGenome.plantId());
        if (plant == null) return false;
        genome = seedGenome;
        growthStage = 0;
        syncState(plant);
        return true;
    }

    public boolean advanceGrowth(PlantDefinition plant) {
        if (level == null || level.isClientSide || genome == null
                || !genome.plantId().equals(plant.id())
                || growthStage >= plant.stages().size() - 1) return false;
        growthStage++;
        syncState(plant);
        return true;
    }

    /**
     * Regrow from the stage configured in AgriPlants after a successful harvest.
     * No mutation of the stored genome occurs, and the block is not destroyed.
     */
    public boolean finishHarvest(PlantDefinition plant) {
        if (level == null || level.isClientSide || !isMature(plant)) return false;
        // A final-stage reset would allow unlimited repeated harvests without regrowth.
        growthStage = plant.harvestStage() >= plant.stages().size() - 1
                ? 0 : Math.max(0, plant.harvestStage());
        syncState(plant);
        return true;
    }

    public boolean isMature(PlantDefinition plant) {
        return genome != null && genome.plantId().equals(plant.id())
                && growthStage >= plant.stages().size() - 1;
    }

    private void syncState(PlantDefinition plant) {
        setChanged();
        if (level == null) return;
        BlockState previous = getBlockState();
        BlockState updated = previous.setValue(CropSticksBlock.PLANTED, genome != null)
                .setValue(CropSticksBlock.STAGE, CropGrowth.displayStage(growthStage, plant.stages().size()));
        if (!updated.equals(previous)) level.setBlock(worldPosition, updated, 3);
        level.sendBlockUpdated(worldPosition, previous, updated, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("GrowthStage", growthStage);
        if (genome != null) GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, genome)
                .result().ifPresent(value -> tag.put("Genome", value));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        growthStage = Math.max(0, tag.getInt("GrowthStage"));
        genome = null;
        if (tag.contains("Genome")) GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag.get("Genome"))
                .result().ifPresent(value -> genome = value);
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
