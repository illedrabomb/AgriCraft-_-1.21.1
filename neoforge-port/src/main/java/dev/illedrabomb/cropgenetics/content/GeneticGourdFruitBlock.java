package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

/**
 * A genuine world block whose parent identity cannot be confused with a
 * vanilla pumpkin/melon placed by a player. Vanilla block loot is delegated
 * to preserve ordinary Silk Touch and melon-slice behavior.
 */
public final class GeneticGourdFruitBlock extends BaseEntityBlock {
    public static final MapCodec<GeneticGourdFruitBlock> CODEC = simpleCodec(GeneticGourdFruitBlock::new);

    @Override
    protected MapCodec<? extends GeneticGourdFruitBlock> codec() {
        return CODEC;
    }

    public GeneticGourdFruitBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GeneticGourdFruitBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos,
                            BlockState replacement, boolean movedByPiston) {
        if (!state.is(replacement.getBlock()) && !level.isClientSide
                && level instanceof ServerLevel server
                && level.getBlockEntity(pos) instanceof GeneticGourdFruitBlockEntity fruit
                && fruit.parentPos() != null && fruit.fruitToken() != null
                // Never load the parent chunk just to notify removal.
                && server.hasChunkAt(fruit.parentPos())
                && level.getBlockEntity(fruit.parentPos()) instanceof CropSticksBlockEntity parent) {
            parent.onOwnedFruitRemoved(server, pos, fruit.fruitToken());
        }
        super.onRemove(state, level, pos, replacement, movedByPiston);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        // BlockState#getDrops is public in 1.21.1 (Block#getDrops protected).
        // Delegate to the real vanilla fruit's loot table with the original
        // loot builder; this retains the tool and loot modifiers.
        BlockState vanilla = state.is(CropBlocks.GENETIC_MELON_FRUIT.get())
                ? Blocks.MELON.defaultBlockState() : Blocks.PUMPKIN.defaultBlockState();
        // Vanilla's loot predicates must see the Vanilla block state, not
        // our owner-aware fruit's block state. Keep tool, origin and entity
        // context from the incoming builder unchanged.
        return vanilla.getDrops(builder.withParameter(
                net.minecraft.world.level.storage.loot.parameters.LootContextParams.BLOCK_STATE, vanilla));
    }
}
