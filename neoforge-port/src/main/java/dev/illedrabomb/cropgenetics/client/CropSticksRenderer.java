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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import java.util.List;

/**
 * Client-only plant mesh. Crop sticks themselves remain ordinary block models.
 * Supports CROSS, PLUS and four-sided HASH plane arrangements for stems and leaves.
 * New gourd fruits render inside their Crop Sticks alongside the mature stem;\n * legacy physical fruits keep their separate world block render path.
 */
public final class CropSticksRenderer implements BlockEntityRenderer<CropSticksBlockEntity> {
    public CropSticksRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(CropSticksBlockEntity crop, float partialTick, PoseStack poses,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        Genome genome = crop.genome();
        if (genome == null || !crop.getBlockState().getValue(CropSticksBlock.PLANTED)) return;
        // New fruits grow inside the crop sticks. Render a compact vanilla fruit
        // block on the same tile, without replacing the plant or a neighbor.
        boolean isGourd = genome.plantId().equals("minecraft:melon_plant")
                || genome.plantId().equals("minecraft:pumpkin_plant");
        if (isGourd && crop.getBlockState().getValue(CropSticksBlock.FRUIT)
                && crop.fruitDirection() == null && crop.fruitToken() == null) {
            poses.pushPose();
            poses.translate(0.13D, 0.05D, 0.13D);
            poses.scale(0.74F, 0.74F, 0.74F);
            Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
                    genome.plantId().equals("minecraft:melon_plant")
                            ? Blocks.MELON.defaultBlockState()
                            : Blocks.PUMPKIN.defaultBlockState(),
                    poses, buffers, packedLight, packedOverlay);
            poses.popPose();
            // Do not stop rendering here: the mature stem remains present
            // beneath/around its internal fruit until the next harvest.
        }

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
        // Only the compact four-sided field crops get the new height curve.
        // Preserve the established visuals for gourds, stalks and special crops.
        float growth = totalStages <= 1 ? 1.0F : (float) stage / (totalStages - 1);
        float height = visual.geometry() == PlantVisuals.Geometry.HASH
                ? 0.28F + 0.50F * growth
                : Math.max(0.15F, (stage + 1.0F) / totalStages);

        // The BER renders atlas sprites directly. Unlike vanilla block
        // models, they do not receive tintindex colors automatically.
        // Match vanilla's biome-independent stem age tint and oak foliage
        // fallback tint, rather than leaving gray/white pixels uncolored.
        int tint = vanillaTint(layers.get(0), genome.plantId(), stage,
                totalStages, crop);
        drawLayer(layers.get(0), visual.geometry(), poses, buffers,
                0.08F, 0.92F, 0.02F, height, packedLight, packedOverlay, tint);

        // Physical melon/pumpkin blocks are rendered by Minecraft itself.
        // Do not draw the old virtual fruit overlay on the stem.
    }

    private static int vanillaTint(ResourceLocation sprite, String plantId, int stage,
                                   int totalStages, CropSticksBlockEntity crop) {
        net.minecraft.world.level.block.state.BlockState vanilla = null;
        if (sprite.equals(ResourceLocation.withDefaultNamespace("block/oak_leaves"))) {
            vanilla = Blocks.OAK_LEAVES.defaultBlockState();
        } else if (sprite.equals(ResourceLocation.withDefaultNamespace("block/melon_stem"))
                && plantId.equals("minecraft:melon_plant")) {
            int age = totalStages <= 1 ? 7
                    : (int) ((long) stage * 7 / (totalStages - 1));
            vanilla = Blocks.MELON_STEM.defaultBlockState()
                    .setValue(StemBlock.AGE, Math.max(0, Math.min(7, age)));
        } else if (sprite.equals(ResourceLocation.withDefaultNamespace("block/pumpkin_stem"))
                && plantId.equals("minecraft:pumpkin_plant")) {
            int age = totalStages <= 1 ? 7
                    : (int) ((long) stage * 7 / (totalStages - 1));
            vanilla = Blocks.PUMPKIN_STEM.defaultBlockState()
                    .setValue(StemBlock.AGE, Math.max(0, Math.min(7, age)));
        }
        if (vanilla == null) return 0xFFFFFF;
        int color = Minecraft.getInstance().getBlockColors().getColor(
                vanilla, crop.getLevel(), crop.getBlockPos(), 0);
        return color == -1 ? 0xFFFFFF : color & 0xFFFFFF;
    }

    private static void drawLayer(ResourceLocation source, PlantVisuals.Geometry geometry,
                                  PoseStack poses, MultiBufferSource buffers, float min, float max,
                                  float y0, float y1, int light, int overlay, int tint) {
        // Block textures belong to Minecraft's stitched atlas. The previous
        // entityCutoutNoCull("textures/...png") path bypassed atlas sprites
        // and would not reliably render vanilla or animated block textures.
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(source);
        VertexConsumer vertices = buffers.getBuffer(Sheets.cutoutBlockSheet());
        PoseStack.Pose pose = poses.last();

        if (geometry == PlantVisuals.Geometry.HASH) {
            // Four parallel faces around a centered square, viewed as a # from above.
            // Unlike diagonal CROSS planes these keep foliage inside the crop sticks.
            float innerMin = 0.27F;
            float innerMax = 0.73F;
            plane(vertices, pose, sprite, innerMin, y0, innerMin, innerMax, y1, innerMin, light, overlay, tint);
            plane(vertices, pose, sprite, innerMin, y0, innerMax, innerMax, y1, innerMax, light, overlay, tint);
            plane(vertices, pose, sprite, innerMin, y0, innerMin, innerMin, y1, innerMax, light, overlay, tint);
            plane(vertices, pose, sprite, innerMax, y0, innerMin, innerMax, y1, innerMax, light, overlay, tint);
        } else if (geometry == PlantVisuals.Geometry.PLUS) {
            plane(vertices, pose, sprite, 0.5F, y0, min, 0.5F, y1, max, light, overlay, tint);
            plane(vertices, pose, sprite, min, y0, 0.5F, max, y1, 0.5F, light, overlay, tint);
        } else {
            // Both CROSS and GOURD use crossed primary foliage.
            plane(vertices, pose, sprite, min, y0, min, max, y1, max, light, overlay, tint);
            plane(vertices, pose, sprite, min, y0, max, max, y1, min, light, overlay, tint);
        }
    }

    private static void plane(VertexConsumer consumer, PoseStack.Pose pose,
                              TextureAtlasSprite sprite,
                              float x0, float y0, float z0, float x1, float y1, float z1,
                              int light, int overlay, int tint) {
        float dx = x1 - x0;
        float dz = z1 - z0;
        float inverseLength = 1.0F / (float) Math.sqrt(dx * dx + dz * dz);
        float nx = dz * inverseLength;
        float nz = -dx * inverseLength;

        vertex(consumer, pose, sprite, x0, y1, z0, 0, 0, light, overlay, nx, nz, tint);
        vertex(consumer, pose, sprite, x0, y0, z0, 0, 1, light, overlay, nx, nz, tint);
        vertex(consumer, pose, sprite, x1, y0, z1, 1, 1, light, overlay, nx, nz, tint);
        vertex(consumer, pose, sprite, x1, y1, z1, 1, 0, light, overlay, nx, nz, tint);

        vertex(consumer, pose, sprite, x1, y1, z1, 0, 0, light, overlay, -nx, -nz, tint);
        vertex(consumer, pose, sprite, x1, y0, z1, 0, 1, light, overlay, -nx, -nz, tint);
        vertex(consumer, pose, sprite, x0, y0, z0, 1, 1, light, overlay, -nx, -nz, tint);
        vertex(consumer, pose, sprite, x0, y1, z0, 1, 0, light, overlay, -nx, -nz, tint);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose,
                               TextureAtlasSprite sprite,
                               float x, float y, float z, float u, float v,
                               int packedLight, int packedOverlay, float nx, float nz, int tint) {
        // The incoming UV coordinates are [0,1] relative to the sprite.
        float atlasU = sprite.getU0() + u * (sprite.getU1() - sprite.getU0());
        float atlasV = sprite.getV0() + v * (sprite.getV1() - sprite.getV0());
        consumer.addVertex(pose, x, y, z)
                .setColor((tint >> 16) & 255, (tint >> 8) & 255, tint & 255, 255)
                .setUv(atlasU, atlasV)
                .setOverlay(packedOverlay)
                .setLight(packedLight)
                .setNormal(pose, nx, 0, nz);
    }
}
