package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.MapCodec;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.config.CropBalanceConfig;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Interactions: first stick is the placed block, apply a second stick on
 * empty crop sticks to enable crossing, or plant a genetic seed on single
 * sticks. Empty crossing sticks discover neighboring mature crops on server ticks.
 */
public final class CropSticksBlock extends BaseEntityBlock implements BonemealableBlock {
    public static final MapCodec<CropSticksBlock> CODEC = simpleCodec(CropSticksBlock::new);

    @Override
    protected MapCodec<? extends CropSticksBlock> codec() {
        return CODEC;
    }

    public static final BooleanProperty CROSS = BooleanProperty.create("cross");
    public static final BooleanProperty PLANTED = BooleanProperty.create("planted");
    public static final BooleanProperty FRUIT = BooleanProperty.create("fruit");
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 7);
    public static final EnumProperty<CropAppearance> SPECIES = EnumProperty.create("species", CropAppearance.class);

    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 12, 13);

    public CropSticksBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(CROSS, false).setValue(PLANTED, false).setValue(STAGE, 0)
                .setValue(SPECIES, CropAppearance.GENERIC).setValue(FRUIT, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(CROSS, PLANTED, STAGE, SPECIES, FRUIT, FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // Vanilla farmland remains the baseline. Explicit modded soil rules
        // can allow placement on other registered blocks/tags.
        if (level.getBlockState(pos.below()).is(Blocks.FARMLAND)) return true;
        // The plant catalogue is server-side. Do not let an empty client-side
        // registry reject legitimate placement or break predictions for
        // crop sticks on sand, mycelium, dirt, mud or modded soils.
        // The logical server still enforces the actual registered rules.
        if (level instanceof Level world && world.isClientSide) return true;
        // Preserve already planted crops when their optional mod or datapack
        // disappears. The per-crop genome is still saved in the BlockEntity;
        // do not reject its old support just because unrelated active plants
        // use different soil. Missing physical support still breaks normally.
        if (level instanceof Level world
                && world.getBlockEntity(pos) instanceof CropSticksBlockEntity existing
                && existing.genome() != null
                && !CropGeneticsMod.plants().plants().containsKey(
                        existing.genome().plantId())) {
            return !level.getBlockState(pos.below()).isAir();
        }
        return CropGeneticsMod.plants().plants().values().stream()
                .anyMatch(plant -> CropConditions.matchesPlantingSite(level, pos, plant));
    }

    /**
     * A removed or replaced supporting block schedules a single authoritative
     * recheck. Never destroy crops directly from updateShape: neighbor updates
     * can occur while a different block is still being modified.
     */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction,
                                     BlockState neighborState, LevelAccessor level,
                                     BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN) level.scheduleTick(pos, this, 1);
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // A prior update may have replaced this block; only process our own
        // still-present block and only while the supporting chunk is loaded.
        if (!level.getBlockState(pos).is(this) || !level.hasChunkAt(pos.below())) return;
        BlockState support = level.getBlockState(pos.below());
        // A physically absent support always breaks the sticks. A fluid
        // inside an otherwise valid waterloggable block is NOT an invalid
        // support by itself: configured soil IDs/tags decide what is allowed.
        if (support.isAir()) {
            level.destroyBlock(pos, true);
            return;
        }

        // A missing datapack species is dormant, not invalid: do not erase
        // its saved genotype merely because the crop registry was reloaded.
        if (level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop
                && crop.genome() != null) {
            PlantDefinition plant = CropGeneticsMod.plants().plants()
                    .get(crop.genome().plantId());
            if (plant != null && !CropConditions.matchesSoil(level, pos, plant.requirements())) {
                level.destroyBlock(pos, true);
            }
            return;
        }
        // Empty sticks have no species-specific soil. If any registered crop
        // accepts this support, preserve them; missing catalogues are not a
        // reason to break a solid supporting block.
        // Keep the same catalog-independent farmland baseline as canSurvive.
        // An otherwise restricted datapack must not uproot empty sticks
        // when farmland merely changes its moisture BlockState.
        if (!support.is(Blocks.FARMLAND)
                && !CropGeneticsMod.plants().plants().isEmpty()
                && CropGeneticsMod.plants().plants().values().stream()
                    .noneMatch(plant -> CropConditions.matchesSoil(level, pos, plant.requirements()))) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropSticksBlockEntity(pos, state);
    }

    /**
     * Periodic, staggered reconciliation for ALREADY LOADED chunks following
     * /reload. The random-tick path cannot repair crops when randomTickSpeed=0
     * or when a crop is not receiving random ticks.
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CropBlocks.CROP_STICKS_ENTITY.get(),
                (world, pos, current, crop) -> {
                    if ((world.getGameTime() + pos.asLong()) % 100 != 0) return;
                    Genome currentGenome = crop.genome();
                    if (currentGenome == null) return;
                    PlantDefinition currentPlant =
                            CropGeneticsMod.plants().plants().get(currentGenome.plantId());
                    if (currentPlant == null) return;
                    if (!crop.stageMetadataMatches(currentPlant)) {
                        crop.reconcileLoadedStage(currentPlant);
                    }
                    // Reconcile real fruit ownership even after a harvest-mode reload.
                    // No neighboring chunks are loaded by this check.
                    if (crop.fruitReady() || currentPlant.harvestMode() == HarvestMode.GOURD) {
                        crop.reconcilePhysicalFruit(currentPlant);
                    }
                });
    }

    /**
     * Datapack-opted cactus-like thorn contact. Only an actual planted crop
     * with an active registered species can hurt entities; empty sticks and
     * removed/disabled plant definitions never cause contact damage.
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !state.getValue(PLANTED)) return;
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) return;
        Genome genome = crop.genome();
        if (genome == null) return;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant != null && plant.contactThorns()) {
            entity.hurt(level.damageSources().cactus(), 1.0F);
        }
    }

    /**
     * Standard bonemeal interface used by Vanilla and compatible external
     * growth tools. The existing direct bone-meal interaction is retained;
     * implementations calling the interface now get the same server checks.
     */
    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        if (!state.is(this) || !state.getValue(PLANTED)) return false;
        if (!(level instanceof Level world)) return false;
        if (!(world.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)
                || crop.genome() == null) return false;
        // The client has no authoritative plant catalogue, so predict only
        // basic eligibility; the server applies all growth requirements.
        if (world.isClientSide) return state.getValue(STAGE) < 7;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(crop.genome().plantId());
        return plant != null && !crop.isMature(plant)
                && CropConditions.canGrow(world, pos, plant, crop.genome());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random,
                                     BlockPos pos, BlockState state) {
        // Growth tools should perform exactly one stage advance per valid
        // application; do not roll an additional hidden failure chance.
        return isValidBonemealTarget(level, pos, state);
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random,
                                BlockPos pos, BlockState state) {
        if (!isValidBonemealTarget(level, pos, level.getBlockState(pos))
                || !(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)
                || crop.genome() == null) return;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(crop.genome().plantId());
        if (plant == null) return;
        crop.reconcileLoadedStage(plant);
        if (!crop.isMature(plant) && CropConditions.canGrow(level, pos, plant, crop.genome())) {
            crop.advanceGrowth(plant);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        // Empty crosses must tick to discover parents; occupied crosses grow
        // exactly like single crop sticks.
        return state.getValue(CROSS) || state.getValue(PLANTED);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) return;
        Genome genome = crop.genome();
        if (genome == null) {
            if (state.getValue(CROSS)) CrossBreeding.attempt(level, pos, crop, random);
            return;
        }
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant == null) return;
        // Rain changes only explicitly profiled soil; do this BEFORE growth
        // requirements so rain can restore a previously unsuitable bed.
        SoilDynamics.onRandomTick(level, pos, random);
        // Retry migration if this chunk was loaded before the datapack
        // catalogue was available. Does not advance normal growth.
        crop.reconcileLoadedStage(plant);
        // Reconcile even under bad lighting so removed fruit never stays
        // "ready" indefinitely or gets duplicated through stem harvesting.
        if (crop.fruitReady() && crop.reconcilePhysicalFruit(plant)) return;
        double factor = CropConditions.growthFactor(level, pos, plant, genome);
        // Count mature gourd ticks even when the environment prevents a chance roll.
        // This distinguishes a blocked growth factor from absent random ticks in PT-02.
        if (plant.harvestMode() == HarvestMode.GOURD && !crop.fruitReady()
                && crop.isMature(plant)) {
            crop.recordMatureFruitTick(factor > 0D);
        }
        if (factor <= 0D) return;
        if (crop.isMature(plant)) {
            if (plant.harvestMode() == HarvestMode.GOURD && !crop.fruitReady()) {
                int gain = genome.gain();
                double fruitChance = GourdFruitRules.chance(
                        plant.fruitChance(), gain,
                        factor * CropBalanceConfig.GOURD_FRUIT_CHANCE_MULTIPLIER.get());
                boolean chancePassed = random.nextDouble() < fruitChance;
                crop.recordFruitChanceRoll(chancePassed);
                if (chancePassed && crop.markInternalFruit(plant)) {
                    crop.recordFruitPlacementSuccess();
                }
            }
            return;
        }
        if (random.nextDouble() < CropGrowth.chance(plant, genome) * factor) {
            crop.advanceGrowth(plant);
            // Never debit nutrients for failed rolls or mature idle crops.
            SoilDynamics.onGrowth(level, pos, random);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // Optional, player-driven soil care. Only soil with an explicitly
        // configured datapack profile or existing saved override is treatable.
        // Ordinary seeds, crop-stick stacking and harvesting remain unaffected.
        boolean water = stack.is(Items.WATER_BUCKET);
        boolean fertilizer = stack.is(Items.BONE_MEAL);
        // On a growing crop, bone meal advances the actual server-owned stage
        // before considering the separate, opt-in soil fertilizer mechanic.
        // Never advance a mature plant or consume an item for a failed action.
        if (fertilizer && crop.genome() != null) {
            PlantDefinition growing = CropGeneticsMod.plants().plants()
                    .get(crop.genome().plantId());
            if (growing != null && !level.isClientSide) {
                // A reload can change the species' stage count before the
                // periodic ticker has a chance to reconcile this plant.
                crop.reconcileLoadedStage(growing);
                // Bone meal skips the random chance, not hard planting,
                // climate, lighting or soil-chemistry requirements.
                if (!crop.isMature(growing)
                        && CropConditions.canGrow(level, pos, growing, crop.genome())
                        && crop.advanceGrowth(growing)) {
                    if (!player.isCreative()) stack.shrink(1);
                    level.levelEvent(1505, pos, 0);
                    return ItemInteractionResult.SUCCESS;
                }
                // No growth: allow the existing nutrient treatment fallback
                // to run, rather than consuming bone meal for no effect.
            }
        }
        if (water || fertilizer) {
            if (level instanceof ServerLevel server) {
                boolean changed = SoilTreatment.apply(server, pos.below(),
                        water ? SoilTreatment.Treatment.WATER
                              : SoilTreatment.Treatment.FERTILIZER);
                if (!changed) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                if (!player.isCreative()) {
                    if (water) {
                        // A water bucket is unstackable; avoid consuming the
                        // bucket itself as an ingredient.
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    } else {
                        stack.shrink(1);
                    }
                }
            }
            return ItemInteractionResult.SUCCESS;
        }

        // A second stick creates a cross-crop only when the position is empty.
        if (stack.is(CropItems.CROP_STICKS.get()) && !state.getValue(CROSS) && crop.genome() == null) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(CROSS, true), 3);
                if (!player.isCreative()) stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // Client prediction cannot consult the server-only plant catalogue.
        // Accept either attached-genome seed form locally; the server below
        // still checks seed identity, species, soil and growth conditions.
        if (level.isClientSide && !state.getValue(CROSS) && crop.genome() == null
                && CropItems.hasGenome(stack)) {
            return ItemInteractionResult.SUCCESS;
        }

        // An existing genetic seed keeps its full genome; never reset its alleles.
        if (stack.is(CropItems.GENETIC_SEED.get()) && !state.getValue(CROSS)) {
            Genome genome = stack.get(CropComponents.GENOME.get());
            PlantDefinition plant = genome == null ? null
                    : CropGeneticsMod.plants().plants().get(genome.plantId());
            if (plant == null || crop.genome() != null
                    || !CropConditions.canGrow(level, pos, plant, genome)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide) {
                if (!crop.plant(genome, stack)) {
                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
                if (!player.isCreative()) stack.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // A Vanilla/mod seed that was previously given its REAL genome
        // by an analyzer must preserve that genome when planted. Only the
        // species-matched, unambiguous seed mappings are accepted here.
        if (!state.getValue(CROSS) && crop.genome() == null
                && !stack.is(CropItems.GENETIC_SEED.get())
                && CropItems.isGeneticSeedStack(stack)) {
            Genome seedGenome = stack.get(CropComponents.GENOME.get());
            PlantDefinition plant = seedGenome == null ? null
                    : CropGeneticsMod.plants().plants().get(seedGenome.plantId());
            if (plant != null && CropConditions.canGrow(level, pos, plant, seedGenome)) {
                if (!level.isClientSide) {
                    if (!crop.plant(seedGenome, stack)) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                    if (!player.isCreative()) stack.shrink(1);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        // A vanilla/mod seed with *no custom component patch* starts as a 1/1/1
        // genome. Do not consume customized stacks or ambiguous modded seeds.
        if (!state.getValue(CROSS) && crop.genome() == null
                && CropGeneticsSeedRecognition.isUnmodifiedRegisteredSeed(stack)) {
            String seedId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            var plant = CropGeneticsMod.plants().plantForSeed(seedId);
            if (plant.isPresent() && CropConditions.canGrow(level, pos, plant.get(),
                    Genome.basic(plant.get().id()))) {
                if (!level.isClientSide) {
                    if (!crop.plant(Genome.basic(plant.get().id()), stack)) {
                        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                    }
                    if (!player.isCreative()) stack.shrink(1);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Empty-hand / default right click harvest. Genetic seed remains planted;
     * only the fruit products are dropped, and the growth stage resets.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level,
            BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) {
            return InteractionResult.PASS;
        }
        // Empty crossed sticks are reserved for breeding: remove the upper
        // layer with an empty-hand right-click before planting a seed.
        if (crop.genome() == null && state.getValue(CROSS)
                && player.getMainHandItem().isEmpty()) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(CROSS, false), 3);
                popResource(level, pos, new ItemStack(CropItems.CROP_STICKS.get()));
            }
            return InteractionResult.SUCCESS;
        }
        Genome genome = crop.genome();
        if (genome == null) return InteractionResult.PASS;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant == null) return InteractionResult.PASS;
        // A player can harvest immediately after /reload, before the next
        // staggered ticker. Normalize saved age only on the server.
        if (!level.isClientSide) crop.reconcileLoadedStage(plant);
        // A gourd's single fruit is now contained in the crop, not a neighboring block.
        // Old worlds with a legacy physical fruit keep their existing ownership flow.
        if (plant.harvestMode() == HarvestMode.GOURD && crop.fruitReady()
                && crop.fruitDirection() == null && crop.fruitToken() == null) {
            if (!level.isClientSide && crop.harvestInternalFruit(plant)) {
                for (ItemStack drop : CropHarvest.products(plant, genome, level.getRandom())) {
                    popResource(level, pos, drop);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!crop.readyForHarvest(plant)) return InteractionResult.PASS;

        if (!level.isClientSide && crop.finishHarvest(plant)) {
            for (ItemStack drop : CropHarvest.products(plant, genome, level.getRandom())) {
                popResource(level, pos, drop);
            }
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * A destroyed stem cannot own a physical fruit any longer. Detach only
     * the fruit with the exact stored position/token and only when its chunk
     * is already loaded. Unloaded fruit chunks recover on their own ticker.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState replacement, boolean movedByPiston) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server
                && server.getBlockEntity(pos) instanceof CropSticksBlockEntity stem
                && stem.fruitReady() && stem.fruitDirection() != null
                && stem.fruitToken() != null) {
            BlockPos fruitPos = pos.relative(stem.fruitDirection());
            if (server.hasChunkAt(fruitPos)
                    && server.getBlockEntity(fruitPos)
                            instanceof GeneticGourdFruitBlockEntity fruit) {
                fruit.detachIfOwnedBy(pos, stem.fruitToken());
            }
        }
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(CropItems.CROP_STICKS.get(), state.getValue(CROSS) ? 2 : 1));
        BlockEntity entity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof CropSticksBlockEntity crop && crop.genome() != null) {
            Genome genome = crop.genome();
            drops.add(crop.seedDrop());
            PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
            // Normal breaking is destructive: keep the seed, and if mature add
            // products. Repeated calls are avoided through the usual block loot path.
            if (plant != null && (crop.readyForHarvest(plant)
                    || (plant.harvestMode() == HarvestMode.GOURD && crop.fruitReady()
                            && crop.fruitDirection() == null && crop.fruitToken() == null))) {
                drops.addAll(CropHarvest.products(plant, genome, builder.getLevel().getRandom()));
            }
        }
        return drops;
    }
}
