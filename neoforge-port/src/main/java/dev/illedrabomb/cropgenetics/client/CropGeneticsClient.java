package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.minecraft.world.level.block.Blocks;

/** Physical client only: no renderer classes loaded by a dedicated server. */
@EventBusSubscriber(modid = CropGeneticsMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CropGeneticsClient {
    private CropGeneticsClient() {}

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CropBlocks.CROP_STICKS_ENTITY.get(), CropSticksRenderer::new);
    }

    @SubscribeEvent
    public static void registerCropColors(RegisterColorHandlersEvent.Block event) {
        // Our attached-stem models inherit Vanilla tintindex=0 but do not
        // inherit Vanilla's BlockColors registration automatically.
        event.register((state, level, pos, tintIndex) -> {
            if (tintIndex < 0 || !state.getValue(
                    dev.illedrabomb.cropgenetics.content.CropSticksBlock.FRUIT)) return -1;
            // Reuse Minecraft's own attached-stem tint value, including any
            // available Vanilla customization of the color handler.
            return net.minecraft.client.Minecraft.getInstance().getBlockColors()
                    .getColor(Blocks.ATTACHED_MELON_STEM.defaultBlockState(),
                            level, pos, tintIndex);
        }, CropBlocks.CROP_STICKS.get());
    }

    @SubscribeEvent
    public static void registerResources(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(PlantVisuals.INSTANCE);
    }
}
