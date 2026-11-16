package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registration for the original AgriCraft-style growing substrate. */
public final class CropBlocks {
    private CropBlocks() {}

    private static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(CropGeneticsMod.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, CropGeneticsMod.MOD_ID);

    public static final DeferredBlock<CropSticksBlock> CROP_STICKS =
            BLOCKS.registerBlock("crop_sticks", CropSticksBlock::new,
                    BlockBehaviour.Properties.of().noCollission().strength(0.5F)
                            .sound(SoundType.WOOD).pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<GeneticGourdFruitBlock> GENETIC_MELON_FRUIT =
            BLOCKS.registerBlock("genetic_melon_fruit", GeneticGourdFruitBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.MELON)
                            .pushReaction(PushReaction.BLOCK));

    public static final DeferredBlock<GeneticGourdFruitBlock> GENETIC_PUMPKIN_FRUIT =
            BLOCKS.registerBlock("genetic_pumpkin_fruit", GeneticGourdFruitBlock::new,
                    BlockBehaviour.Properties.ofFullCopy(net.minecraft.world.level.block.Blocks.PUMPKIN)
                            .pushReaction(PushReaction.BLOCK));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CropSticksBlockEntity>>
            CROP_STICKS_ENTITY = BLOCK_ENTITIES.register("crop_sticks",
                    () -> BlockEntityType.Builder.of(CropSticksBlockEntity::new, CROP_STICKS.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GeneticGourdFruitBlockEntity>>
            GOURD_FRUIT_ENTITY = BLOCK_ENTITIES.register("gourd_fruit",
                    () -> BlockEntityType.Builder.of(GeneticGourdFruitBlockEntity::new,
                            GENETIC_MELON_FRUIT.get(), GENETIC_PUMPKIN_FRUIT.get()).build(null));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        BLOCK_ENTITIES.register(bus);
    }
}
