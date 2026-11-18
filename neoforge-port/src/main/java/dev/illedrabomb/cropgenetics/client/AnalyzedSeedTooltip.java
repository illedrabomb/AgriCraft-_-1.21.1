package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.GeneticDisclosure;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Client-only Shift display; avoids referencing client classes in seed item logic. */
@EventBusSubscriber(modid = CropGeneticsMod.MOD_ID, value = Dist.CLIENT)
public final class AnalyzedSeedTooltip {
    private AnalyzedSeedTooltip() {}

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        // The same genome can be stored on an ordinary Vanilla seed after
        // an external analyzer has inspected it. These also need a tooltip.
        if (!CropItems.hasGenome(stack)) return;
        boolean customItem = stack.is(CropItems.GENETIC_SEED.get());
        if (!customItem) {
            Genome attached = stack.get(CropComponents.GENOME.get());
            if (attached == null) return;
            event.getToolTip().add(Component.translatable(
                    "tooltip.cropgenetics.plant", attached.plantId()));
            event.getToolTip().add(Component.translatable(CropItems.isAnalyzed(stack)
                    ? "tooltip.cropgenetics.analyzed_hint"
                    : "tooltip.cropgenetics.unanalyzed"));
            event.getToolTip().add(Component.translatable(
                    "tooltip.cropgenetics.plant_on_sticks"));
        }
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (!GeneticDisclosure.mayShowGenes(genome, CropItems.isAnalyzed(stack))
                || !Screen.hasShiftDown()) return;
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.stats",
                genome.displayedStats()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.growth", genome.growth()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.gain", genome.gain()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.strength", genome.strength()));
    }
}
