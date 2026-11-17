package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * Standalone crop information line. Identify 0.4.0 reads only the block's
 * generic name and has no supported public crop-details hook. This HUD works
 * beside any optional look-at mod without trying to access its internals.
 *
 * Never render the stored genome's individual gene values here.
 */
@EventBusSubscriber(modid = CropGeneticsMod.MOD_ID, value = Dist.CLIENT)
public final class CropInfoOverlay {
    private CropInfoOverlay() {}

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.options.hideGui
                || !(minecraft.hitResult instanceof BlockHitResult hit)
                || hit.getType() != HitResult.Type.BLOCK
                || !minecraft.level.getBlockState(hit.getBlockPos())
                        .is(CropBlocks.CROP_STICKS.get())
                || !(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof CropSticksBlockEntity crop)) {
            return;
        }
        Genome genome = crop.genome();
        if (genome == null) return;

        String id = genome.plantId();
        String label = id.substring(id.indexOf(':') + 1).replace("_plant", "").replace('_', ' ');
        int total = crop.totalGrowthStages() > 0 ? crop.totalGrowthStages() : 8;
        int stage = Math.max(0, Math.min(crop.growthStage(), total - 1));
        Component line = Component.translatable("overlay.cropgenetics.crop", label, stage + 1, total);
        int x = 8;
        int y = event.getGuiGraphics().guiHeight() - 58;
        event.getGuiGraphics().drawString(minecraft.font, line, x, y, 0xFFFFFF, true);
    }
}
