package dev.illedrabomb.cropgenetics.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.illedrabomb.cropgenetics.content.CropSticksBlock;
import dev.illedrabomb.cropgenetics.content.CropSticksBlockEntity;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
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

        // The BlockState keeps only 0..7 for model compatibility. Use the
        // exact server-synced BlockEntity values for species with >8 stages.
        // Older world saves without TotalGrowthStages retain the old mapping.
        int totalStages = crop.totalGrowthStages();
        int stage = totalStages > 0
                ? Math.max(0, Math.min(crop.growthStage(), totalStages - 1))
                : crop.getBlockState().getValue(CropSticksBlock.STAGE);
        if (totalStages <= 0) totalStages = 8;

        PlantVisuals.Visual visual = PlantVisuals.INSTANCE.visual(genome.plantId());
        List<ResourceLocation> layers = visual.at(stage, totalStages);
        float height = Math.max(0.15F, (stage + 1.0F) / totalStages);

        drawLayer(layers.get(0), visual.geometry(), poses, buffers,
                0.08F, 0.92F, 0.02F, height, packedLight, packedOverlay);

        // Physical melon/pumpkin blocks are rendered by Minecraft itself.
        // Do not draw the old virtual fruit overlay on the stem.
    }

    private static void drawLayer(ResourceLocation source, PlantVisuals.Geometry geometry,
                                  PoseStack poses, MultiBufferSource buffers, float min, float max,
                                  float y0, float y1, int light, int overlay) {
        // Block textures belong to Minecraft's stitched atlas. The previous
        // entityCutoutNoCull("textures/...png") path bypassed atlas sprites
        // and would not reliably render vanilla or animated block textures.
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(source);
        VertexConsumer vertices = buffers.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poses.last();

        if (geometry == PlantVisuals.Geometry.PLUS) {
            plane(vertices, pose, sprite, 0.5F, y0, min, 0.5F, y1, max, light, overlay);
            plane(vertices, pose, sprite, min, y0, 0.5F, max, y1, 0.5F, light, overlay);
        } else {
            // Both CROSS and GOURD use crossed primary foliage.
            plane(vertices, pose, sprite, min, y0, min, max, y1, max, light, overlay);
            plane(vertices, pose, sprite, min, y0, max, max, y1, min, light, overlay);
        }
    }

    private static void plane(VertexConsumer consumer, PoseStack.Pose pose,
                              TextureAtlasSprite sprite,
                              float x0, float y0, float z0, float x1, float y1, float z1,
                              int light, int overlay) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float inverseLength = 1.0F / (float) Math.sqrt(dx * dx + dz * dz);
        float nx = dz * inverseLength;
        float nz = -dx * inverseLength;

        vertex(consumer, pose, sprite, x0, y1, z0, 0, 0, light, overlay, nx, nz);
        vertex(consumer, pose, sprite, x0, y0, z0, 0, 1, light, overlay, nx, nz);
        vertex(consumer, pose, sprite, x1, y0, z1, 1, 1, light, overlay, nx, nz);
        vertex(consumer, pose, sprite, x1, y1, z1, 1, 0, light, overlay, nx, nz);

        vertex(consumer, pose, sprite, x1, y1, z1, 0, 0, light, overlay, -nx, -nz);
        vertex(consumer, pose, sprite, x1, y0, z1, 0, 1, light, overlay, -nx, -nz);
        vertex(consumer, pose, sprite, x0, y0, z0, 1, 1, light, overlay, -nx, -nz);
        vertex(consumer, pose, sprite, x0, y1, z0, 1, 0, light, overlay, -nx, -nz);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               TextureAtlasSprite sprite,
                               float x, float y, float z, float u, float v,
                               int packedLight, int packedOverlay, float nx, float nz) {
        // The incoming UV coordinates are [0,1] relative to the sprite.
        float atlasU = sprite.getU0() + u * (sprite.getU1() - sprite.getU0());
        float atlasV = sprite.getV0() + v * (sprite.getV1() - sprite.getV0());
        consumer.addVertex(pose, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(atlasU, atlasV)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, 0, nz);
    }
}
