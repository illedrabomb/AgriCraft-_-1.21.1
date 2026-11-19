package dev.illedrabomb.cropgenetics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.illedrabomb.cropgenetics.content.SeedAnalyzerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Shows the actual stored seed and journal on the classic analyzer block.
 * The block model stays visible even when both slots are empty.
 */
public final class SeedAnalyzerRenderer implements BlockEntityRenderer<SeedAnalyzerBlockEntity> {
    public SeedAnalyzerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(SeedAnalyzerBlockEntity analyzer, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        ItemStack seed = analyzer.displaySeed();
        ItemStack journal = analyzer.displayJournal();
        if (seed.isEmpty() && journal.isEmpty()) return;

        if (!seed.isEmpty()) {
            // Lay the seed flat on the analyzer's top surface (the analyzer block is 5/16 blocks tall).
            draw(analyzer, seed, 0.50D, 0.325D, 0.43D, 0.38F,
                    poses, buffers, packedLight, packedOverlay);
        }
        if (!journal.isEmpty()) {
            draw(analyzer, journal, 0.73D, 0.327D, 0.70D, 0.32F,
                    poses, buffers, packedLight, packedOverlay);
        }
    }

    private static void draw(SeedAnalyzerBlockEntity analyzer, ItemStack stack,
                             double x, double y, double z, float scale,
                             PoseStack poses, MultiBufferSource buffers,
                             int light, int overlay) {
        poses.pushPose();
        poses.translate(x, y, z);
        poses.mulPose(Axis.XP.rotationDegrees(90.0F));
        poses.scale(scale, scale, scale);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                stack, ItemDisplayContext.FIXED, light, overlay,
                poses, buffers, analyzer.getLevel(), 0);
        poses.popPose();
    }
}
