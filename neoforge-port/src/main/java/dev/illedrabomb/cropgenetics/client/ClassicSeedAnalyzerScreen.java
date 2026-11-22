package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropSpeciesNames;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

/**
 * Reuses the original AgriCraft 186x186 analyzer art and slot locations.
 * When a chest is attached, it is displayed in an independent right pane.
 */
public final class ClassicSeedAnalyzerScreen<T extends AbstractContainerMenu>
        extends AbstractContainerScreen<T> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            CropGeneticsMod.MOD_ID, "textures/gui/seed_analyzer_gui.png");
    private static final int PANEL_WIDTH = 186;
    private final boolean withChest;

    public ClassicSeedAnalyzerScreen(T menu, Inventory inventory, Component title, boolean withChest) {
        super(menu, inventory, title);
        this.withChest = withChest;
        imageWidth = withChest ? 374 : PANEL_WIDTH;
        imageHeight = 186;
        inventoryLabelX = 13;
        inventoryLabelY = 92;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, PANEL_WIDTH, 186, 256, 256);
        graphics.blit(TEXTURE, leftPos + 13, topPos + 25, 0, 186, 56, 56, 256, 256);
        graphics.blit(TEXTURE, leftPos + 25, topPos + 70, 186, 73, 18, 18, 256, 256);

        if (withChest) {
            // No inherited generic chest screen: the 27 slots are explicit.
            int x = leftPos + 188, y = topPos + 3;
            graphics.fill(x, y, x + 182, y + 87, 0xff35261d);
            graphics.fill(x + 3, y + 3, x + 179, y + 84, 0xffc5b592);
            for (int row = 0; row < 3; row++) {
                for (int col = 0; col < 9; col++) {
                    graphics.blit(TEXTURE, leftPos + 198 + col * 18,
                            topPos + 26 + row * 18, 186, 73, 18, 18, 256, 256);
                }
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, font.plainSubstrByWidth(title.getString(), 166),
                8, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle,
                inventoryLabelX, inventoryLabelY, 0x404040, false);
        if (withChest) {
            graphics.drawString(font, Component.translatable("container.cropgenetics.seed_chest"),
                    199, 9, 0x404040, false);
        }

        ItemStack seed = menu.slots.get(0).getItem();
        Genome genome = seed.get(CropComponents.GENOME.get());
        if (genome == null || !CropItems.isAnalyzed(seed)) {
            graphics.drawString(font, "?", 132, 42, 0x49413a, false);
            return;
        }

        // Genome is synchronized only to the menu viewer, not block watchers.
        String species = CropSpeciesNames.label(genome.plantId()).getString();
        graphics.drawString(font, font.plainSubstrByWidth(species, 88),
                87, 16, 0x4b3829, false);
        graphics.blit(TEXTURE, 88, 26, 186, 0, 19, 73, 256, 256);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.analyzer.growth_short", genome.growth()),
                110, 31, 0x3e5636, false);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.analyzer.gain_short", genome.gain()),
                110, 51, 0x76512b, false);
        graphics.drawString(font, Component.translatable("screen.cropgenetics.analyzer.strength_short", genome.strength()),
                110, 71, 0x49466c, false);
    }
}
