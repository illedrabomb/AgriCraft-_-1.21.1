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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import javax.annotation.Nullable;
import java.util.UUID;

/** Owns the exact growth stage and the genome of a planted crop. */
public final class CropSticksBlockEntity extends BlockEntity {
    @Nullable private Genome genome;
    private boolean seedAnalyzed;
    @Nullable private String originalSeedId;
    private int growthStage;
    // Also synced to clients: the client cannot read server datapack stage counts.
    // Zero signals an older save that only has the 0..7 blockstate stage.
    private int totalGrowthStages;
    private boolean fruitReady;
    @Nullable private Direction fruitDirection;
    @Nullable private UUID fruitToken;
    // Server-only diagnostics for PT-02. Persist across save/reload so a
    // failed second fruit cycle can be examined without relying on log spam.
    private long matureFruitTicks;
    private long matureFruitTicksBlocked;
    private long fruitChanceRolls;
    private long fruitChancePassed;
    private long fruitPlacementSucceeded;
    private long fruitPlacementNoEligibleTarget;
    private long fruitPlacementEligibleFailed;

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
                && growthStage >= 0 && growthStage < totalGrowthStages
                && getBlockState().getValue(CropSticksBlock.PLANTED)
                && getBlockState().getValue(CropSticksBlock.STAGE)
                        == CropGrowth.displayStage(growthStage, totalGrowthStages)
                && getBlockState().getValue(CropSticksBlock.SPECIES)
                        == CropAppearance.fromPlantId(genome.plantId());
    }

    /**
     * Also called by random ticks: a chunk can load before datapacks finish
     * registering, so onLoad is not guaranteed to find this species yet.
     */
    public boolean reconcileLoadedStage(PlantDefinition plant) {
        if (!(level instanceof ServerLevel) || genome == null
                || !genome.plantId().equals(plant.id())) return false;
        int targetTotal = plant.stages().size();
        if (stageMetadataMatches(plant)) return false;

        // A datapack can change the number of stages while this crop is
        // planted. Preserve relative growth instead of treating the old
        // integer index as a stage in the new definition (8 -> 16, etc.).
        // Mature plants remain mature; physical fruit ownership is untouched.
        int adjustedStage = growthStage;
        if (totalGrowthStages > 0 && totalGrowthStages != targetTotal) {
            adjustedStage = CropGrowth.remapStage(growthStage, totalGrowthStages, targetTotal);
        }
        growthStage = Math.max(0, Math.min(adjustedStage, targetTotal - 1));
        // syncState updates TotalGrowthStages, marks the chunk dirty, and
        // sends the BlockEntity update even if the visual 0..7 state is equal.
        syncState(plant);
        return true;
    }

    @Nullable public Genome genome() { return genome; }
    public boolean seedAnalyzed() { return seedAnalyzed; }

    /** Safe presentation gate for future optional overlay integrations. */
    public boolean mayShowGenes() {
        return GeneticDisclosure.mayShowGenes(genome, seedAnalyzed);
    }

    /**
     * Inspect an already planted crop using the handheld analyzer.
     * Never modifies the immutable Genome; only records that this crop's
     * genotype was actually inspected. This state survives seed drops.
     */
    public boolean analyzePlantedCrop() {
        if (!(level instanceof ServerLevel) || genome == null) return false;
        if (!seedAnalyzed) {
            seedAnalyzed = true;
            setChanged();
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
        return true;
    }
    @Nullable public String originalSeedId() { return originalSeedId; }

    /** Uprooted genetic seed retains the exact parent's metadata. */
    public ItemStack seedDrop() {
        if (genome == null) return ItemStack.EMPTY;
        ItemStack result = CropItems.seedWithGenome(genome);
        if (originalSeedId != null) result.set(CropComponents.ORIGINAL_SEED.get(), originalSeedId);
        if (seedAnalyzed) result.set(CropComponents.ANALYZED.get(), true);
        return result;
    }
    public int growthStage() { return growthStage; }
    public int totalGrowthStages() { return totalGrowthStages; }
    public boolean fruitReady() { return fruitReady; }
    public long matureFruitTicks() { return matureFruitTicks; }
    public long matureFruitTicksBlocked() { return matureFruitTicksBlocked; }
    public long fruitChanceRolls() { return fruitChanceRolls; }
    public long fruitChancePassed() { return fruitChancePassed; }
    public long fruitPlacementSucceeded() { return fruitPlacementSucceeded; }
    public long fruitPlacementNoEligibleTarget() { return fruitPlacementNoEligibleTarget; }
    public long fruitPlacementEligibleFailed() { return fruitPlacementEligibleFailed; }

    /** Count mature gourd ticks before the environmental factor can reject a roll. */
    public void recordMatureFruitTick(boolean environmentEligible) {
        if (!(level instanceof ServerLevel) || genome == null) return;
        if (matureFruitTicks < Long.MAX_VALUE) matureFruitTicks++;
        if (!environmentEligible && matureFruitTicksBlocked < Long.MAX_VALUE)
            matureFruitTicksBlocked++;
        setChanged();
    }

    /** Count one mature-stem probability roll, then record whether it passed. */
    public void recordFruitChanceRoll(boolean passed) {
        if (!(level instanceof ServerLevel) || genome == null) return;
        if (fruitChanceRolls < Long.MAX_VALUE) fruitChanceRolls++;
        if (passed && fruitChancePassed < Long.MAX_VALUE) fruitChancePassed++;
        setChanged();
    }

    /** Only a fully bound physical fruit counts as placed. */
    public void recordFruitPlacementSuccess() {
        if (!(level instanceof ServerLevel) || !fruitReady) return;
        if (fruitPlacementSucceeded < Long.MAX_VALUE) fruitPlacementSucceeded++;
        setChanged();
    }

    /** Classify a passed fruit roll that failed placement without changing world state. */
    public void recordFruitPlacementFailure(boolean hadEligibleTarget) {
        if (!(level instanceof ServerLevel) || genome == null) return;
        if (hadEligibleTarget) {
            if (fruitPlacementEligibleFailed < Long.MAX_VALUE) fruitPlacementEligibleFailed++;
        } else {
            if (fruitPlacementNoEligibleTarget < Long.MAX_VALUE) fruitPlacementNoEligibleTarget++;
        }
        setChanged();
    }

    @Nullable public Direction fruitDirection() { return fruitDirection; }
    @Nullable public UUID fruitToken() { return fruitToken; }

    public boolean plant(Genome seedGenome) {
        return plant(seedGenome, ItemStack.EMPTY);
    }

    /** Accept the actual consumed seed stack to preserve origin and analysis. */
    public boolean plant(Genome seedGenome, ItemStack sourceSeed) {
        if (genome != null || seedGenome == null || level == null || level.isClientSide
                || getBlockState().getValue(CropSticksBlock.CROSS)) return false;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(seedGenome.plantId());
        if (plant == null) return false;
        // The seed's own genome is authoritative. Never store another
        // stack's analysis or OriginalSeed metadata on an unrelated crop.
        if (sourceSeed != null && !sourceSeed.isEmpty()) {
            // Planting eventually converts the source into our own genetic
            // seed drop. Never consume external components (custom name,
            // enchantments, addon data, etc.) that this conversion cannot
            // restore on uprooting. Match the explicit conversion safety rule.
            if (!CropSeedSourceSafety.canConvertWithoutExternalDataLoss(sourceSeed))
                return false;
            Genome attached = sourceSeed.get(CropComponents.GENOME.get());
            if (attached != null) {
                // The source must really be a recognised genetic seed of this
                // species. A forged genome component on an unrelated item
                // must never become a valid planting/provenance shortcut.
                if (!attached.equals(seedGenome)
                        || !CropItems.isGeneticSeedStack(sourceSeed)) return false;
            } else {
                // Direct callers of plant(...) need the same checks as the
                // block's right-click path: no unknown or customized items.
                // These may carry other mods' data which we cannot reproduce
                // in a one-item genetic seed drop.
                if (sourceSeed.is(CropItems.GENETIC_SEED.get())
                        || !sourceSeed.getComponentsPatch().isEmpty()) return false;
                String seedId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(sourceSeed.getItem()).toString();
                if (!CropGeneticsMod.plants().plantForSeed(seedId)
                        .map(candidate -> candidate.id().equals(seedGenome.plantId()))
                        .orElse(false)) return false;
            }
        }
        genome = seedGenome;
        seedAnalyzed = sourceSeed != null && CropItems.isAnalyzed(sourceSeed);
        // The actual item ID is authoritative for a Vanilla/mod seed.
        // A stale or forged ORIGINAL_SEED component on that stack must not
        // redirect later recovery to an unrelated item after addon removal.
        // Our dedicated genetic seed keeps its stored provenance instead.
        originalSeedId = null;
        if (sourceSeed != null && !sourceSeed.isEmpty()) {
            if (sourceSeed.is(CropItems.GENETIC_SEED.get())) {
                originalSeedId = sourceSeed.get(CropComponents.ORIGINAL_SEED.get());
            } else {
                originalSeedId = net.minecraft.core.registries.BuiltInRegistries.ITEM
                        .getKey(sourceSeed.getItem()).toString();
            }
        }
        growthStage = 0;
        totalGrowthStages = plant.stages().size();
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        matureFruitTicks = 0;
        matureFruitTicksBlocked = 0;
        fruitChanceRolls = 0;
        fruitChancePassed = 0;
        fruitPlacementSucceeded = 0;
        fruitPlacementNoEligibleTarget = 0;
        fruitPlacementEligibleFailed = 0;
        if (!syncState(plant)) {
            clearRejectedPlant();
            return false;
        }
        return true;
    }

    /** A denied block transition must not leave a hidden planted genome. */
    private void clearRejectedPlant() {
        genome = null;
        seedAnalyzed = false;
        originalSeedId = null;
        growthStage = 0;
        totalGrowthStages = 0;
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        matureFruitTicks = 0;
        matureFruitTicksBlocked = 0;
        fruitChanceRolls = 0;
        fruitChancePassed = 0;
        fruitPlacementSucceeded = 0;
        fruitPlacementNoEligibleTarget = 0;
        fruitPlacementEligibleFailed = 0;
        setChanged();
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
        // A new crossed genome has never been analyzed, even if parents were.
        seedAnalyzed = false;
        originalSeedId = null;
        growthStage = 0;
        totalGrowthStages = plant.stages().size();
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        matureFruitTicks = 0;
        matureFruitTicksBlocked = 0;
        fruitChanceRolls = 0;
        fruitChancePassed = 0;
        fruitPlacementSucceeded = 0;
        fruitPlacementNoEligibleTarget = 0;
        fruitPlacementEligibleFailed = 0;
        if (!syncState(plant)) {
            clearRejectedPlant();
            return false;
        }
        return true;
    }

    public boolean advanceGrowth(PlantDefinition plant) {
        if (level == null || level.isClientSide || genome == null
                || !genome.plantId().equals(plant.id())
                || growthStage >= plant.stages().size() - 1) return false;
        int previousStage = growthStage;
        growthStage++;
        if (!syncState(plant)) {
            growthStage = previousStage;
            setChanged();
            return false;
        }
        return true;
    }

    /**
     * Record the one physical fruit placed next to this mature stem.
     * This method must never create a second item or a virtual fruit.
     */
    public boolean markPhysicalFruit(PlantDefinition plant, Direction direction, UUID token) {
        if (!(level instanceof ServerLevel server) || isRemoved()
                || level.getBlockEntity(worldPosition) != this
                || !getBlockState().is(CropBlocks.CROP_STICKS.get())
                || plant.harvestMode() != HarvestMode.GOURD || !isMature(plant)
                || fruitReady || direction.getAxis().isVertical()
                || !GourdFruits.matches(server, worldPosition, direction, plant, token)) return false;
        fruitReady = true;
        fruitDirection = direction;
        fruitToken = token;
        if (!syncState(plant)) {
            fruitReady = false;
            fruitDirection = null;
            fruitToken = null;
            setChanged();
            return false;
        }
        return true;
    }

    /**
     * Reconcile a previously owned fruit even if a datapack reload changed
     * this species from GOURD to STANDARD. Never erase an owned physical
     * fruit's identity while its neighboring chunk is unavailable.
     */
    /** A newly formed gourd stays inside its own crop sticks instead of occupying a neighbor. */
    public boolean markInternalFruit(PlantDefinition plant) {
        if (!(level instanceof ServerLevel) || genome == null
                || !genome.plantId().equals(plant.id())
                || plant.harvestMode() != HarvestMode.GOURD || !isMature(plant)
                || fruitReady) return false;
        fruitReady = true;
        fruitDirection = null;
        fruitToken = null;
        if (!syncState(plant)) {
            fruitReady = false;
            setChanged();
            return false;
        }
        return true;
    }

    /** Pick an internal melon/pumpkin without destroying its mature parent crop. */
    public boolean harvestInternalFruit(PlantDefinition plant) {
        if (level == null || level.isClientSide || genome == null
                || !genome.plantId().equals(plant.id())
                || !CropFruitTransitionRules.canHarvestInternalGourd(plant.harvestMode(),
                        isMature(plant), fruitReady, fruitDirection != null,
                        fruitToken != null)) return false;
        fruitReady = false;
        // For an internal gourd fruit the stem stays mature. Only report
        // harvest success (and let the caller pay out items) after the
        // block's FRUIT=false transition really sticks.
        if (!syncState(plant)) {
            fruitReady = true;
            setChanged();
            return false;
        }
        return true;
    }

    public boolean reconcilePhysicalFruit(PlantDefinition plant) {
        if (!(level instanceof ServerLevel server) || genome == null
                || !genome.plantId().equals(plant.id()) || !fruitReady) return false;
        // A datapack may replace GOURD with STANDARD while an internal fruit
        // is ready. Clear only the obsolete fruit flag; keep the mature age
        // so the new normal harvest can still pay out once.
        if (fruitDirection == null && fruitToken == null) {
            if (!CropFruitTransitionRules.staleInternalFruit(plant.harvestMode(),
                    fruitReady, false, false)) return false;
            fruitReady = false;
            syncState(plant);
            return true;
        }
        if (fruitDirection != null && fruitToken != null) {
            BlockPos fruitPos = worldPosition.relative(fruitDirection);
            // An unloaded neighboring chunk does not mean the fruit was harvested.
            if (!server.hasChunkAt(fruitPos)) return false;
            if (GourdFruits.matchesOwned(server, worldPosition, fruitDirection,
                    plant.id(), fruitToken)) {
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
        if (plant == null) return false;
        clearFruit(plant);
        return true;
    }

    private void clearFruit(PlantDefinition plant) {
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        // Vanilla-like gourds keep their mature stem after fruit harvesting.
        // Resetting the stem to stage zero would force a complete second
        // growth cycle before the next fruit probability roll can even occur.
        // If a datapack changed the crop away from GOURD, retain the existing
        // ordinary-harvest regrowth behavior.
        if (plant.harvestMode() != HarvestMode.GOURD) {
            growthStage = CropGrowth.regrowthStage(plant);
        }
        syncState(plant);
    }

    public boolean readyForHarvest(PlantDefinition plant) {
        // Read-only: block loot can query this while the block is being
        // removed, so do not mutate block state as a side effect here.
        // Physical gourd blocks are harvested directly, not by right-clicking
        // the stem. Prevent duplicates through the stem's normal drop path.
        // Even if a legacy save or an unsupported datapack transition has
        // left ownership metadata behind, never pay a second stem harvest
        // while a physical fruit still owns this growth cycle.
        return CropFruitTransitionRules.canHarvestStandard(plant.harvestMode(),
                isMature(plant), fruitReady, fruitDirection != null, fruitToken != null);
    }

    /**
     * Regrow from the stage configured in AgriPlants after a successful harvest.
     * No mutation of the stored genome occurs, and the block is not destroyed.
     */
    public boolean finishHarvest(PlantDefinition plant) {
        if (level == null || level.isClientSide || !readyForHarvest(plant)) return false;
        int previousStage = growthStage;
        boolean previousFruitReady = fruitReady;
        Direction previousFruitDirection = fruitDirection;
        UUID previousFruitToken = fruitToken;
        fruitReady = false;
        fruitDirection = null;
        fruitToken = null;
        // A final-stage reset would allow unlimited repeated harvests without regrowth.
        growthStage = CropGrowth.regrowthStage(plant);
        if (!syncState(plant)) {
            growthStage = previousStage;
            fruitReady = previousFruitReady;
            fruitDirection = previousFruitDirection;
            fruitToken = previousFruitToken;
            setChanged();
            return false;
        }
        return true;
    }

    public boolean isMature(PlantDefinition plant) {
        return genome != null && genome.plantId().equals(plant.id())
                && CropGrowth.isMatureAtStage(growthStage, totalGrowthStages,
                        plant.stages().size());
    }

    private boolean syncState(PlantDefinition plant) {
        totalGrowthStages = plant.stages().size();
        setChanged();
        if (level == null) return false;
        BlockState previous = getBlockState();
        // Successful crossbreeding consumes the extra upper sticks: once a genome
                // exists, the block remains planted on its single lower stick layer.
        BlockState updated = previous.setValue(CropSticksBlock.CROSS,
                        CropCrossLayerRules.keepCrossLayer(previous.getValue(CropSticksBlock.CROSS), genome != null))
                .setValue(CropSticksBlock.PLANTED, genome != null)
                .setValue(CropSticksBlock.STAGE, CropGrowth.displayStage(growthStage, plant.stages().size()))
                .setValue(CropSticksBlock.FRUIT, fruitReady)
                .setValue(CropSticksBlock.FACING, fruitDirection == null ? Direction.NORTH : fruitDirection)
                .setValue(CropSticksBlock.SPECIES, genome == null
                        ? CropAppearance.GENERIC : CropAppearance.fromPlantId(genome.plantId()));
        if (!updated.equals(previous) && !level.setBlock(worldPosition, updated, 3))
            return false;
        // Do not claim a successful planting/cross if an event or another mod
        // has replaced the requested state, even when setBlock returned true.
        BlockState actual = level.getBlockState(worldPosition);
        if (!actual.equals(updated)) return false;
        level.sendBlockUpdated(worldPosition, previous, actual, 3);
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("GrowthStage", growthStage);
        tag.putBoolean("SeedAnalyzed", seedAnalyzed);
        if (originalSeedId != null) tag.putString("OriginalSeed", originalSeedId);
        tag.putInt("TotalGrowthStages", totalGrowthStages);
        tag.putBoolean("FruitReady", fruitReady);
        tag.putLong("MatureFruitTicks", matureFruitTicks);
        tag.putLong("MatureFruitTicksBlocked", matureFruitTicksBlocked);
        tag.putLong("FruitChanceRolls", fruitChanceRolls);
        tag.putLong("FruitChancePassed", fruitChancePassed);
        tag.putLong("FruitPlacementSucceeded", fruitPlacementSucceeded);
        tag.putLong("FruitPlacementNoEligibleTarget", fruitPlacementNoEligibleTarget);
        tag.putLong("FruitPlacementEligibleFailed", fruitPlacementEligibleFailed);
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
        seedAnalyzed = tag.getBoolean("SeedAnalyzed");
        originalSeedId = tag.contains("OriginalSeed")
                ? tag.getString("OriginalSeed") : null;
        int savedTotal = tag.getInt("TotalGrowthStages");
        totalGrowthStages = savedTotal >= 1 && savedTotal <= 64 ? savedTotal : 0;
        fruitReady = tag.getBoolean("FruitReady");
        matureFruitTicks = Math.max(0L, tag.getLong("MatureFruitTicks"));
        matureFruitTicksBlocked = Math.max(0L, tag.getLong("MatureFruitTicksBlocked"));
        fruitChanceRolls = Math.max(0L, tag.getLong("FruitChanceRolls"));
        fruitChancePassed = Math.max(0L, tag.getLong("FruitChancePassed"));
        fruitPlacementSucceeded = Math.max(0L, tag.getLong("FruitPlacementSucceeded"));
        fruitPlacementNoEligibleTarget = Math.max(0L, tag.getLong("FruitPlacementNoEligibleTarget"));
        fruitPlacementEligibleFailed = Math.max(0L, tag.getLong("FruitPlacementEligibleFailed"));
        fruitDirection = tag.contains("FruitDirection")
                ? Direction.byName(tag.getString("FruitDirection")) : null;
        if (fruitDirection != null && fruitDirection.getAxis().isVertical()) fruitDirection = null;
        fruitToken = tag.hasUUID("FruitToken") ? tag.getUUID("FruitToken") : null;
        genome = null;
        if (tag.contains("Genome")) GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag.get("Genome"))
                .result().ifPresent(value -> genome = value);
        // Old saves may have FruitReady but no owner token. Such data is
        // not an authoritative ownership claim; never keep a phantom fruit.
        // Both absent: new internal fruit. Both present: legacy owned neighbor fruit.
        // Mixed or plantless metadata is never a valid fruit claim.
        if (genome == null || !fruitReady || (fruitDirection == null) != (fruitToken == null)) {
            fruitReady = false;
            fruitDirection = null;
            fruitToken = null;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // World save is complete and authoritative. A nearby client only
        // needs growth/appearance metadata, never unanalysed gene values,
        // historical source seed identity or private diagnostic counters.
        CompoundTag tag = saveWithoutMetadata(registries);
        tag.remove("OriginalSeed");
        tag.remove("MatureFruitTicks");
        tag.remove("MatureFruitTicksBlocked");
        tag.remove("FruitChanceRolls");
        tag.remove("FruitChancePassed");
        tag.remove("FruitPlacementSucceeded");
        tag.remove("FruitPlacementNoEligibleTarget");
        tag.remove("FruitPlacementEligibleFailed");
        if (genome != null && !seedAnalyzed) {
            Genome publicGenome = CropGenomeSyncRules.forClient(genome, false);
            GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, publicGenome)
                    .result().ifPresent(value -> tag.put("Genome", value));
        }
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
