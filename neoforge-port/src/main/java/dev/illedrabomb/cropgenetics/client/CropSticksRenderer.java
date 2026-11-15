package dev.illedrabomb.cropgenetics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.illedrabomb.cropgenetics.content.CropSticksBlock;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws two crossed, double-sided cutout planes for any plant visuals ID.
 * Crop-stick wood is still drawn by the static block model.
 */
public final class CropSticksRenderer implements BlockEntityRenderer<CropSticksBlockEntity> {
    public CropSticksRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CropSticksBlockEntity crop, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Genome genome = crop.genome();
        if (genome == null || !crop.getBlockState().getValue(CropSticksBlock.PLANTED)) return;

        // Client state is synchronized by the block entity update packet.
        // The display stage is 0..7 and independent of the exact genome.
        int stage = crop.getBlockState().getValue(CropSticksBlock.STAGE);
        ResourceLocation atlasKey = PlantVisuals.INSTANCE.texture(genome.plantId(), stage, 8);
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                atlasKey.getNamespace(), "textures/" + atlasKey.getPath() + ".png");

        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        float size = 0.42F;
        float y0 = 0.02F;
        float y1 = Math.max(0.12F, (stage + 1) / 8.0F);
        PoseStack.Pose pose = poses.last();

        // Cross-plane vegetation as in vanilla crop block models.
        plane(consumer, pose, 0.5F - size, y0, 0.5F - size,
                0.5F + size, y1, 0.5F + size, packedLight, packedOverlay);
        plane(consumer, pose, 0.5F - size, y0, 0.5F + size,
                0.5F + size, y1, 0.5F - size, packedLight, packedOverlay);
    }

    private static void plane(VertexConsumer consumer, PoseStack.Pose pose,
                              float x0, float y0, float z0, float x1, float y1, float z1,
                              int light, int overlay) {
        // One face and the reverse face, so the texture is visible from all sides.
        vertex(consumer, pose, x0, y1, z0, 0, 0, light, overlay);
        vertex(consumer, pose, x0, y0, z0, 0, 1, light, overlay);
        vertex(consumer, pose, x1, y0, z1, 1, 1, light, overlay);
        vertex(consumer, pose, x1, y1, z1, 1, 0, light, overlay);

        vertex(consumer, pose, x1, y1, z1, 0, 0, light, overlay);
        vertex(consumer, pose, x1, y0, z1, 0, 1, light, overlay);
        vertex(consumer, pose, x0, y0, z0, 1, 1, light, overlay);
        vertex(consumer, pose, x0, y1, z0, 1, 0, light, overlay);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               float x, float y, float z, float u, float v,
                               int packedLight, int packedOverlay) {
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, 0, 1, 0);
    }
}
