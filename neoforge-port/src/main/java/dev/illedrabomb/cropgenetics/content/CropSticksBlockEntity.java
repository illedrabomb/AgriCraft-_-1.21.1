package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.genetics.GenomeCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import java.util.UUID;

/** Owns the exact growth stage and the genome of a planted crop. */
public final class CropSticksBlockEntity extends BlockEntity {
    @Nullable private Genome genome;
    private int growthStage;
    // Also synced to clients: the client cannot read server datapack stage counts.
    // Zero signals an older save that only has the 0..7 blockstate stage.
    private int totalGrowthStages;
    private boolean fruitReady;
    @Nullable private Direction fruitDirection;
    @Nullable private UUID fruitToken;

    public CropSticksBlockEntity(BlockPos pos, BlockState state) {
        super(CropBlocks.CROP_STICKS_ENTITY.get(), pos, state);
    }

    /**
     * Repair old stage metadata when a saved chunk enters the server world.
     * Skip species that are currently unavailable (e.g. disabled datapack);
     * their exact genome and age must remain intact until the species returns.
     */
    @Override
    public void onLoad() {
        super.onLoad();
        if (!(level instanceof ServerLevel) || genome == null) return;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant != null) reconcileLoadedStage(plant);
    }

    /**
     * Read-only check used by the renderer and ticking code to determine
     * whether the BlockEntity metadata matches the current crop catalogue.
     * A reload may have changed stage count while this chunk stayed loaded.
     */
    public boolean stageMetadataMatches(PlantDefinition plant) {
        return genome != null && genome.plantId().equals(plant.id())
                && totalGrowthStages == plant.stages().size()
                && growthStage >= 0 && growthStage < totalGrowthStages;
    }

    /**
     * Also called by random ticks: a chunk can load before datapacks finish
     * registering, so onLoad is not guaranteed to find this species yet.
     */
    public boolean reconcileLoadedStage(PlantDefinition plant) {
        if (!(level instanceof ServerLevel) || genome == null
                || !genome.plantId().equals(plant.id())) return false;
        int targetTotal = plant.stages().size();
        int clampedStage = Math.max(0, Math.min(growthStage, targetTotal - 1));
        if (totalGrowthStages == targetTotal && growthStage == clampedStage) return false;

        // Never silently delete the identity of an owned physical fruit.
        // Age normalization is safe; fruit ownership is reconciled separately.
        growthStage = clampedStage;
        // syncState updates TotalGrowthStages, marks the chunk dirty, and
        // sends the BlockEntity update even if the visual 0..7 state is equal.
        syncState(plant);
        return true;
    }

    @Nullable public Genome genome() { return genome; }
    public int growthStage() { return growthStage; }
    public int totalGrowthStages() { return totalGrowthStages; }
    public boolean fruitReady() { return fruitReady; }
    @Nullable public Direction fruitDirection() { return fruitDirection; }
    @Nullable public UUID fruitToken() { return fruitToken; }

    public boolean plant(Genome seedGenome) {
        if (genome != null || seedGenome == null || level == null || level.isClientSide
                || getBlockState().getValue(CropSticksBlock.CROSS)) return false;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(seedGenome.plantId());
        if (plant == null) return false;
        genome = seedGenome;
        growthStage = 0;
        totalGrowthStages = plant.stages().size();
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
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
        totalGrowthStages = plant.stages().size();
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
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
     * Record the one physical fruit placed next to this mature stem.
     * This method must never create a second item or a virtual fruit.
     */
    public boolean markPhysicalFruit(PlantDefinition plant, Direction direction, UUID token) {
        if (!(level instanceof ServerLevel server) || !isMature(plant)
                || fruitReady || direction.getAxis().isVertical()
                || !GourdFruits.matches(server, worldPosition, direction, plant, token)) return false;
        fruitReady = true;
        fruitDirection = direction;
        fruitToken = token;
        syncState(plant);
        return true;
    }

    /**
     * Called from stem random ticks. A broken/removed fruit is harvested via
     * vanilla block drops; the stem only restarts its growth cycle.
     */
    public boolean reconcilePhysicalFruit(PlantDefinition plant) {
        if (!(level instanceof ServerLevel server) || !isMature(plant)
                || plant.harvestMode() != HarvestMode.GOURD || !fruitReady) return false;
        if (fruitDirection != null && fruitToken != null) {
            BlockPos fruitPos = worldPosition.relative(fruitDirection);
            // An unloaded neighboring chunk does not mean the fruit was harvested.
            if (!server.hasChunkAt(fruitPos)) return false;
            if (GourdFruits.matches(server, worldPosition, fruitDirection, plant, fruitToken)) {
                return false;
            }
        }
        clearFruit(plant);
        return true;
    }

    /** Immediately react to removal of only our own exact physical fruit. */
    public boolean onOwnedFruitRemoved(ServerLevel server, BlockPos fruitPos, UUID token) {
        if (level != server || !fruitReady || fruitDirection == null || fruitToken == null
                || !fruitToken.equals(token) || !worldPosition.relative(fruitDirection).equals(fruitPos)
                || genome == null) return false;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant == null || plant.harvestMode() != HarvestMode.GOURD) return false;
        clearFruit(plant);
        return true;
    }

    private void clearFruit(PlantDefinition plant) {
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        growthStage = plant.harvestStage() >= plant.stages().size() - 1
                ? 0 : Math.max(0, plant.harvestStage());
        syncState(plant);
    }

    public boolean readyForHarvest(PlantDefinition plant) {
        // Read-only: block loot can query this while the block is being
        // removed, so do not mutate block state as a side effect here.
        // Physical gourd blocks are harvested directly, not by right-clicking
        // the stem. Prevent duplicates through the stem's normal drop path.
        return isMature(plant) && plant.harvestMode() != HarvestMode.GOURD;
    }

    /**
     * Regrow from the stage configured in AgriPlants after a successful harvest.
     * No mutation of the stored genome occurs, and the block is not destroyed.
     */
    public boolean finishHarvest(PlantDefinition plant) {
        if (level == null || level.isClientSide || !readyForHarvest(plant)) return false;
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
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
        totalGrowthStages = plant.stages().size();
        setChanged();
        if (level == null) return;
        BlockState previous = getBlockState();
        BlockState updated = previous.setValue(CropSticksBlock.PLANTED, genome != null)
                .setValue(CropSticksBlock.STAGE, CropGrowth.displayStage(growthStage, plant.stages().size()))
                .setValue(CropSticksBlock.FRUIT, fruitReady)
                .setValue(CropSticksBlock.FACING, fruitDirection == null ? Direction.NORTH : fruitDirection)
                .setValue(CropSticksBlock.SPECIES, genome == null
                        ? CropAppearance.GENERIC : CropAppearance.fromPlantId(genome.plantId()));
        if (!updated.equals(previous)) level.setBlock(worldPosition, updated, 3);
        level.sendBlockUpdated(worldPosition, previous, updated, 3);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("GrowthStage", growthStage);
        tag.putInt("TotalGrowthStages", totalGrowthStages);
        tag.putBoolean("FruitReady", fruitReady);
        if (fruitReady && fruitDirection != null && fruitToken != null) {
            tag.putString("FruitDirection", fruitDirection.getName());
            tag.putUUID("FruitToken", fruitToken);
        }
        if (genome != null) GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, genome)
                .result().ifPresent(value -> tag.put("Genome", value));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        growthStage = Math.max(0, tag.getInt("GrowthStage"));
        int savedTotal = tag.getInt("TotalGrowthStages");
        totalGrowthStages = savedTotal >= 1 && savedTotal <= 64 ? savedTotal : 0;
        fruitReady = tag.getBoolean("FruitReady");
        fruitDirection = tag.contains("FruitDirection")
                ? Direction.byName(tag.getString("FruitDirection")) : null;
        if (fruitDirection != null && fruitDirection.getAxis().isVertical()) fruitDirection = null;
        fruitToken = tag.hasUUID("FruitToken") ? tag.getUUID("FruitToken") : null;
        genome = null;
        if (tag.contains("Genome")) GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag.get("Genome"))
                .result().ifPresent(value -> genome = value);
        // Old saves may have FruitReady but no owner token. Such data is
        // not an authoritative ownership claim; never keep a phantom fruit.
        if (genome == null || fruitDirection == null || fruitToken == null || !fruitReady) {
            fruitReady = false;
            fruitDirection = null;
            fruitToken = null;
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
