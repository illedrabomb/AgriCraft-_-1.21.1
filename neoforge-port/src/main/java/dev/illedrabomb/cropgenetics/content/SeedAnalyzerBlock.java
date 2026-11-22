package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

/** Original-style placed seed analyzer with an attached seed-chest view. */
public final class SeedAnalyzerBlock extends BaseEntityBlock {
    public static final MapCodec<SeedAnalyzerBlock> CODEC = simpleCodec(SeedAnalyzerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = box(1, 0, 1, 15, 5, 15);

    public SeedAnalyzerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override @Nullable public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level,
                                            BlockPos pos, CollisionContext context) { return SHAPE; }
    @Override protected MapCodec<? extends SeedAnalyzerBlock> codec() { return CODEC; }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override @Nullable public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeedAnalyzerBlockEntity(pos, state);
    }

    /**
     * Recheck a retained journal after server datapack/world reloads, even
     * when no player moves items through the analyzer. This refreshes hints
     * and repairs a missing discovery for an already analyzed stored seed.
     * Every analyzer is staggered over five seconds to spread the work.
     */
    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, CropBlocks.SEED_ANALYZER_ENTITY.get(),
                (world, pos, blockState, analyzer) -> {
                    if ((world.getGameTime() + pos.asLong()) % 100 != 0) return;
                    analyzer.reconcileJournalMutations();
                });
    }

    /**
     * Original-style direct insertion: right-click with a seed to put one into
     * an empty analyzer. All components stay on the copied seed stack.
     * Other items fall back to opening the normal inventory interface.
     */
    @Override protected ItemInteractionResult useItemOn(ItemStack held, BlockState state,
            Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SeedAnalyzerBlockEntity analyzer))
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Original-style journal insertion. Sneak with the journal in hand,
        // even when the seed slot is occupied; never confuse it for a seed.
        if (player.isShiftKeyDown() && held.is(CropItems.SEED_JOURNAL.get())
                && analyzer.displayJournal().isEmpty()) {
            if (!level.isClientSide) {
                analyzer.setItem(1, held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // The client has no datapack crop registry. Analyzer.canPlaceItem
        // intentionally allows broad local menu placement prediction, but
        // that must not swallow a normal right-click with an unrelated item.
        // Otherwise the client reports SUCCESS although the server opens the
        // inventory instead of inserting anything.
        boolean insertable = level.isClientSide
                ? canPredictDirectSeed(held) : analyzer.canPlaceItem(0, held);
        if (!player.isShiftKeyDown() && analyzer.displaySeed().isEmpty()
                && insertable) {
            if (!level.isClientSide) {
                analyzer.setItem(0, held.copyWithCount(1));
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
            return ItemInteractionResult.SUCCESS;
        }
        // Let vanilla open the analyzer when the held item is not a seed.
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Narrow client-side direct-click prediction. Datapack-only seed items
     * still work on the authoritative server, but an unknown item must not
     * cause a false positive that hides the analyzer's inventory interaction.
     */
    private static boolean canPredictDirectSeed(ItemStack held) {
        if (held.isEmpty()) return false;
        return SeedAnalyzerInteractionRules.predictsSeed(
                BuiltInRegistries.ITEM.getKey(held.getItem()).toString(),
                held.is(CropItems.GENETIC_SEED.get()),
                CropItems.hasGenome(held),
                !held.getComponentsPatch().isEmpty());
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SeedAnalyzerBlockEntity analyzer)) return InteractionResult.PASS;
        if (player.isShiftKeyDown()) {
            // This callback can also follow an item interaction that passed
            // through to the block. Never extract the analyzed seed or journal
            // unless the player intentionally sneaks with empty hands.
            if (!player.getMainHandItem().isEmpty() || !player.getOffhandItem().isEmpty())
                return InteractionResult.PASS;
            if (!level.isClientSide) {
                // Extract the seed first, then the journal on the next action.
                int slot = !analyzer.getItem(0).isEmpty() ? 0
                        : (!analyzer.getItem(1).isEmpty() ? 1 : -1);
                if (slot >= 0) {
                    ItemStack removed = analyzer.removeItemNoUpdate(slot);
                    if (!removed.isEmpty() && !player.addItem(removed)) {
                        player.drop(removed, false);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide) {
            SeedChestBlockEntity chest = findChest(level, pos);
            Component title = Component.translatable(chest == null
                    ? "container.cropgenetics.seed_analyzer"
                    : "container.cropgenetics.seed_analyzer_with_chest");
            player.openMenu(new SimpleMenuProvider((id, inventory, viewer) ->
                    chest == null ? new SeedAnalyzerStandaloneMenu(id, inventory, analyzer)
                            : new SeedAnalyzerCombinedMenu(id, inventory, analyzer, chest), title));
        }
        return InteractionResult.SUCCESS;
    }

    @Nullable
    private SeedChestBlockEntity findChest(Level level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof SeedChestBlockEntity chest)
                return chest;
        }
        return null;
    }

    @Override protected void onRemove(BlockState state, Level level, BlockPos pos,
                                      BlockState replacement, boolean movedByPiston) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SeedAnalyzerBlockEntity analyzer)
            Containers.dropContents(level, pos, analyzer);
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }
}
