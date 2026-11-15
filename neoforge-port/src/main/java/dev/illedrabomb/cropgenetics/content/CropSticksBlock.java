package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import dev.illedrabomb.cropgenetics.data.HarvestMode;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
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
public final class CropSticksBlock extends BaseEntityBlock {
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
        return CropGeneticsMod.plants().plants().values().stream()
                .anyMatch(plant -> CropConditions.matchesSoil(level, pos, plant.requirements()));
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
                    if (currentPlant != null && !crop.stageMetadataMatches(currentPlant)) {
                        crop.reconcileLoadedStage(currentPlant);
                    }
                });
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
        // Retry migration if this chunk was loaded before the datapack
        // catalogue was available. Does not advance normal growth.
        crop.reconcileLoadedStage(plant);
        // Reconcile even under bad lighting so removed fruit never stays
        // "ready" indefinitely or gets duplicated through stem harvesting.
        if (plant.harvestMode() == HarvestMode.GOURD && crop.reconcilePhysicalFruit(plant)) return;
        double factor = CropConditions.growthFactor(level, pos, plant, genome);
        if (factor <= 0D) return;
        if (crop.isMature(plant)) {
            if (plant.harvestMode() == HarvestMode.GOURD && !crop.fruitReady()) {
                int gain = genome.gain().expressed(GenePair.TraitMode.MAX);
                double fruitChance = Math.min(1D,
                        plant.fruitChance() * (1D + (gain - 1) / 10.0D) * factor);
                if (random.nextDouble() < fruitChance) {
                    GourdFruits.tryPlace(level, pos, plant, crop, random);
                }
            }
            return;
        }
        if (random.nextDouble() < CropGrowth.chance(plant, genome) * factor) {
            crop.advanceGrowth(plant);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level,
            BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // A second stick creates a cross-crop only when the position is empty.
        if (stack.is(CropItems.CROP_STICKS.get()) && !state.getValue(CROSS) && crop.genome() == null) {
            if (!level.isClientSide) {
                level.setBlock(pos, state.setValue(CROSS, true), 3);
                if (!player.isCreative()) stack.shrink(1);
            }
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
            if (!level.isClientSide && crop.plant(genome) && !player.isCreative()) stack.shrink(1);
            return ItemInteractionResult.SUCCESS;
        }

        // A vanilla/mod seed with *no custom component patch* starts as a 1/1/1
        // genome. Do not consume customized stacks or ambiguous modded seeds.
        if (!state.getValue(CROSS) && crop.genome() == null
                && !stack.isEmpty() && !stack.is(CropItems.GENETIC_SEED.get())
                && stack.getComponentsPatch().isEmpty()) {
            String seedId = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
            var plant = CropGeneticsMod.plants().plantForSeed(seedId);
            if (plant.isPresent() && CropConditions.canGrow(level, pos, plant.get(),
                    Genome.basic(plant.get().id()))) {
                if (!level.isClientSide) {
                    if (!crop.plant(Genome.basic(plant.get().id()))) {
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
        Genome genome = crop.genome();
        if (genome == null) return InteractionResult.PASS;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant == null || !crop.readyForHarvest(plant)) return InteractionResult.PASS;

        if (!level.isClientSide && crop.finishHarvest(plant)) {
            for (ItemStack drop : CropHarvest.products(plant, genome, level.getRandom())) {
                popResource(level, pos, drop);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(CropItems.CROP_STICKS.get(), state.getValue(CROSS) ? 2 : 1));
        BlockEntity entity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (entity instanceof CropSticksBlockEntity crop && crop.genome() != null) {
            Genome genome = crop.genome();
            drops.add(CropItems.seedWithGenome(genome));
            PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
            // Normal breaking is destructive: keep the seed, and if mature add
            // products. Repeated calls are avoided through the usual block loot path.
            if (plant != null && crop.readyForHarvest(plant)) {
                drops.addAll(CropHarvest.products(plant, genome, builder.getLevel().getRandom()));
            }
        }
        return drops;
    }
}
