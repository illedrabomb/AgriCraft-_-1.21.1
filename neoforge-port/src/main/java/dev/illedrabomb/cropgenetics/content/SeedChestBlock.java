package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

/** A standalone 27-slot seed chest, also accessible through an adjacent analyzer. */
public final class SeedChestBlock extends BaseEntityBlock {
    public static final MapCodec<SeedChestBlock> CODEC = simpleCodec(SeedChestBlock::new);

    public SeedChestBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends SeedChestBlock> codec() { return CODEC; }
    @Override @Nullable public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SeedChestBlockEntity(pos, state);
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                                          Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SeedChestBlockEntity chest)) return InteractionResult.PASS;
        if (!level.isClientSide) player.openMenu(new SimpleMenuProvider(
                (id, inventory, viewer) -> ChestMenu.threeRows(id, inventory, chest),
                Component.translatable("container.cropgenetics.seed_chest")));
        return InteractionResult.SUCCESS;
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos,
                                      BlockState replacement, boolean movedByPiston) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level.getBlockEntity(pos) instanceof SeedChestBlockEntity chest)
            Containers.dropContents(level, pos, chest);
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }
}
