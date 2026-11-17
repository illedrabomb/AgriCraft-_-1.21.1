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
        Component label = speciesLabel(id);
        int total = crop.totalGrowthStages() > 0 ? crop.totalGrowthStages() : 8;
        int stage = Math.max(0, Math.min(crop.growthStage(), total - 1));
        // Report exact zero-based growth stages, matching /data diagnostics.
        Component line = Component.translatable("overlay.cropgenetics.crop", label, stage, total - 1);
        int x = 8;
        int y = event.getGuiGraphics().guiHeight() - 58;
        event.getGuiGraphics().drawString(minecraft.font, line, x, y, 0xFFFFFF, true);
    }

    private static Component speciesLabel(String id) {
        String key = switch (id) {
            case "minecraft:wheat_plant" -> "block.minecraft.wheat";
            case "minecraft:carrot_plant" -> "item.minecraft.carrot";
            case "minecraft:potato_plant" -> "item.minecraft.potato";
            case "minecraft:beet_root_plant" -> "item.minecraft.beetroot";
            case "minecraft:nether_wart_plant" -> "item.minecraft.nether_wart";
            case "minecraft:melon_plant" -> "block.minecraft.melon";
            case "minecraft:pumpkin_plant" -> "block.minecraft.pumpkin";
            case "minecraft:cornflower_plant" -> "block.minecraft.cornflower";
            case "minecraft:poppy_plant" -> "block.minecraft.poppy";
            case "minecraft:dandelion_plant" -> "block.minecraft.dandelion";
            case "minecraft:cactus_plant" -> "block.minecraft.cactus";
            case "minecraft:sugar_cane_plant" -> "item.minecraft.sugar_cane";
            case "minecraft:bamboo_plant" -> "item.minecraft.bamboo";
            case "minecraft:sweet_berry_plant" -> "item.minecraft.sweet_berries";
            case "minecraft:brown_mushroom_plant" -> "block.minecraft.brown_mushroom";
            case "minecraft:red_mushroom_plant" -> "block.minecraft.red_mushroom";
            default -> null;
        };
        if (key != null) return Component.translatable(key);
        // Datapack species without a Vanilla translation remain legible.
        return Component.literal(id.substring(id.indexOf(':') + 1)
                .replace("_plant", "").replace('_', ' '));
    }
}
