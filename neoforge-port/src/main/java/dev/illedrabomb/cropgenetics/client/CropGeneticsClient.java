package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import dev.illedrabomb.cropgenetics.content.SeedSpeciesCatalog;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropMenus;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.minecraft.world.level.block.Blocks;

/** Physical client only: no renderer classes loaded by a dedicated server. */
@EventBusSubscriber(modid = CropGeneticsMod.MOD_ID,
        bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class CropGeneticsClient {
    public static final KeyMapping TOGGLE_CROP_OVERLAY = new KeyMapping(
            "key.cropgenetics.toggle_overlay", InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_F10, "key.categories.misc");

    private CropGeneticsClient() {}

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CROP_OVERLAY);
    }

    @SubscribeEvent
    public static void registerSeedItemVariants(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(
                CropItems.GENETIC_SEED.get(),
                ResourceLocation.fromNamespaceAndPath(CropGeneticsMod.MOD_ID, "seed_species"),
                (stack, level, entity, seed) -> {
                    Genome genome = stack.get(CropComponents.GENOME.get());
                    if (genome == null) return 0.0F;
                    int index = SeedSpeciesCatalog.DEFAULT_SPECIES.indexOf(genome.plantId());
                    return index < 0 ? 0.0F
                            : (float) index / SeedSpeciesCatalog.DEFAULT_SPECIES.size();
                }));
    }

    @SubscribeEvent
    public static void registerMenuScreens(RegisterMenuScreensEvent event) {
        event.register(CropMenus.ANALYZER.get(),
                (menu, inventory, title) -> new ClassicSeedAnalyzerScreen<>(menu, inventory, title, false));
        event.register(CropMenus.ANALYZER_CHEST.get(),
                (menu, inventory, title) -> new ClassicSeedAnalyzerScreen<>(menu, inventory, title, true));
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(CropBlocks.CROP_STICKS_ENTITY.get(), CropSticksRenderer::new);
        event.registerBlockEntityRenderer(CropBlocks.SEED_ANALYZER_ENTITY.get(), SeedAnalyzerRenderer::new);
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
