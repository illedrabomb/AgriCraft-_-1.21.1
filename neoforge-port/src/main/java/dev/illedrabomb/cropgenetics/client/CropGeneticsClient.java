package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;

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
    public static void registerResources(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(PlantVisuals.INSTANCE);
    }
}
