package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import javax.annotation.Nullable;

/** Original-style placed seed analyzer with an attached seed-chest view. */
public final class SeedAnalyzerBlock extends BaseEntityBlock {
    public static final MapCodec<SeedAnalyzerBlock> CODEC = simpleCodec(SeedAnalyzerBlock::new);

    public SeedAnalyzerBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends SeedAnalyzerBlock> codec() { return CODEC; }
    @Override @Nullable public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeedAnalyzerBlockEntity(pos, state);
    }

    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SeedAnalyzerBlockEntity analyzer)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            SeedChestBlockEntity chest = findChest(level, pos);
            Container combined = chest == null ? analyzer : new CompoundContainer(analyzer, chest);
            Component title = Component.translatable(chest == null
                    ? "container.cropgenetics.seed_analyzer"
                    : "container.cropgenetics.seed_analyzer_with_chest");
            player.openMenu(new SimpleMenuProvider((id, inventory, viewer) ->
                    chest == null ? ChestMenu.oneRow(id, inventory, combined)
                            : ChestMenu.fourRows(id, inventory, combined), title));
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
