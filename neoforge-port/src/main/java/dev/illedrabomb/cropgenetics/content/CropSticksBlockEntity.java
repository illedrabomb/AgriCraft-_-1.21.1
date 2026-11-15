package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.HarvestMode;
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
    private boolean fruitReady;

    public CropSticksBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.CROP_STICKS_ENTITY.get(), pos, state);
    }

    @Nullable public Genome genome() { return genome; }
    public int growthStage() { return growthStage; }
    public boolean fruitReady() { return fruitReady; }

    public boolean plant(Genome seedGenome) {
        if (genome != null || seedGenome == null || level == null || level.isClientSide
                || getBlockState().getValue(CropSticksBlock.CROSS)) return false;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(seedGenome.plantId());
        if (plant == null) return false;
        genome = seedGenome;
        growthStage = 0;
        fruitReady = false;
        syncState(plant);
        return true;
    }

    /**
     * Double crop sticks accept offspring generated from neighboring parents.
     * Never replace an existing genome, even when random ticks race with a player.
     */
    public boolean acceptCrossOffspring(Genome child, PlantDefinition plant) {
        if (level == null || level.isClientSide || genome != null || child == null
                || !getBlockState().getValue(CropSticksBlock.CROSS)
                || !child.plantId().equals(plant.id())) return false;
        genome = child;
        growthStage = 0;
        fruitReady = false;
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

    /** Mature gourd stems form a single separate fruit on later random ticks. */
    public boolean tryFormFruit(PlantDefinition plant) {
        if (level == null || level.isClientSide || plant.harvestMode() != HarvestMode.GOURD
                || !isMature(plant) || fruitReady) return false;
        fruitReady = true;
        syncState(plant);
        return true;
    }

    public boolean readyForHarvest(PlantDefinition plant) {
        return isMature(plant)
                && (plant.harvestMode() != HarvestMode.GOURD || fruitReady);
    }

    /**
     * Regrow from the stage configured in AgriPlants after a successful harvest.
     * No mutation of the stored genome occurs, and the block is not destroyed.
     */
    public boolean finishHarvest(PlantDefinition plant) {
        if (level == null || level.isClientSide || !readyForHarvest(plant)) return false;
        fruitReady = false;
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
                .setValue(CropSticksBlock.STAGE, CropGrowth.displayStage(growthStage, plant.stages().size()))
                .setValue(CropSticksBlock.FRUIT, fruitReady)
                .setValue(CropSticksBlock.SPECIES, genome == null
                        ? CropAppearance.GENERIC : CropAppearance.fromPlantId(genome.plantId()));
        if (!updated.equals(previous)) level.setBlock(worldPosition, updated, 3);
        level.sendBlockUpdated(worldPosition, previous, updated, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("GrowthStage", growthStage);
        tag.putBoolean("FruitReady", fruitReady);
        if (genome != null) GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, genome)
                .result().ifPresent(value -> tag.put("Genome", value));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        growthStage = Math.max(0, tag.getInt("GrowthStage"));
        fruitReady = tag.getBoolean("FruitReady");
        genome = null;
        if (tag.contains("Genome")) GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag.get("Genome"))
                .result().ifPresent(value -> genome = value);
        if (genome == null) fruitReady = false;
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
