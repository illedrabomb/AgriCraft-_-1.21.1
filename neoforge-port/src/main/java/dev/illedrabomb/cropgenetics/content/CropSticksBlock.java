package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.core.BlockPos;
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
 * sticks. No growth or mutation ticks have been wired in yet.
 */
public final class CropSticksBlock extends BaseEntityBlock {
    public static final BooleanProperty CROSS = BooleanProperty.create("cross");
    public static final BooleanProperty PLANTED = BooleanProperty.create("planted");
    public static final IntegerProperty STAGE = IntegerProperty.create("stage", 0, 7);

    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 12, 13);

    public CropSticksBlock(Properties properties) {
        super(properties.randomTicks());
        registerDefaultState(stateDefinition.any().setValue(CROSS, false).setValue(PLANTED, false).setValue(STAGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(CROSS, PLANTED, STAGE);
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
        return level.getBlockState(pos.below()).is(Blocks.FARMLAND);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropSticksBlockEntity(pos, state);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(PLANTED) && !state.getValue(CROSS);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof CropSticksBlockEntity crop)) return;
        Genome genome = crop.genome();
        if (genome == null) return;
        PlantDefinition plant = CropGeneticsMod.plants().plants().get(genome.plantId());
        if (plant == null || crop.isMature(plant)) return;
        if (random.nextDouble() < CropGrowth.chance(plant, genome)) crop.advanceGrowth(plant);
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

        // Genetic seeds are consumed only if planting succeeded.
        if (stack.is(CropItems.GENETIC_SEED.get()) && !state.getValue(CROSS)) {
            Genome genome = stack.get(CropComponents.GENOME.get());
            if (genome == null || crop.genome() != null
                    || !CropGeneticsMod.plants().plants().containsKey(genome.plantId())) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide && crop.plant(genome) && !player.isCreative()) stack.shrink(1);
            return ItemInteractionResult.SUCCESS;
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
        if (plant == null || !crop.isMature(plant)) return InteractionResult.PASS;

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
            if (plant != null && crop.isMature(plant)) {
                drops.addAll(CropHarvest.products(plant, genome, builder.getLevel().getRandom()));
            }
        }
        return drops;
    }
}
