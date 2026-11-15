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
import java.util.List;

/**
 * Client-only plant mesh. Crop sticks themselves remain ordinary block models.
 * Supports CROSS and PLUS plane arrangements for stems and leaves.
 * Gourd fruit blocks are rendered separately as real world blocks.
 */
public final class CropSticksRenderer implements BlockEntityRenderer<CropSticksBlockEntity> {
    public CropSticksRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CropSticksBlockEntity crop, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Genome genome = crop.genome();
        if (genome == null || !crop.getBlockState().getValue(CropSticksBlock.PLANTED)) return;
        // The attached vanilla stem is drawn by the multipart blockstate.
        // Do not draw the cross-plane stem over it a second time.
        if (crop.getBlockState().getValue(CropSticksBlock.FRUIT)
                && (genome.plantId().equals("minecraft:melon_plant")
                || genome.plantId().equals("minecraft:pumpkin_plant"))) return;

        int stage = crop.getBlockState().getValue(CropSticksBlock.STAGE);
        PlantVisuals.Visual visual = PlantVisuals.INSTANCE.visual(genome.plantId());
        List<ResourceLocation> layers = visual.at(stage, 8);
        float height = Math.max(0.15F, (stage + 1) / 8.0F);

        drawLayer(layers.get(0), visual.geometry(), poses, buffers,
                0.08F, 0.92F, 0.02F, height, packedLight, packedOverlay);

        // Physical melon/pumpkin blocks are rendered by Minecraft itself.
        // Do not draw the old virtual fruit overlay on the stem.
    }

    private static void drawLayer(ResourceLocation source, PlantVisuals.Geometry geometry,
                                  PoseStack poses, MultiBufferSource buffers, float min, float max,
                                  float y0, float y1, int light, int overlay) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                source.getNamespace(), "textures/" + source.getPath() + ".png");
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose pose = poses.last();

        if (geometry == PlantVisuals.Geometry.PLUS) {
            plane(vertices, pose, 0.5F, y0, min, 0.5F, y1, max, light, overlay);
            plane(vertices, pose, min, y0, 0.5F, max, y1, 0.5F, light, overlay);
        } else {
            // Both CROSS and GOURD use crossed primary foliage.
            plane(vertices, pose, min, y0, min, max, y1, max, light, overlay);
            plane(vertices, pose, min, y0, max, max, y1, min, light, overlay);
        }
    }

    private static void plane(VertexConsumer consumer, PoseStack.Pose pose,
                              float x0, float y0, float z0, float x1, float y1, float z1,
                              int light, int overlay) {
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
