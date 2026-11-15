package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.genetics.GenePair;
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
        if (!stack.is(CropItems.GENETIC_SEED.get())
                || !CropItems.isAnalyzed(stack)
                || !Screen.hasShiftDown()) return;

        Genome genome = stack.get(CropComponents.GENOME.get());
        if (genome == null) return;
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.stats",
                genome.displayedStats(GenePair.TraitMode.MAX)));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.growth",
                genome.growth().dominant(), genome.growth().recessive()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.gain",
                genome.gain().dominant(), genome.gain().recessive()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.strength",
                genome.strength().dominant(), genome.strength().recessive()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.fertility",
                genome.fertility().dominant(), genome.fertility().recessive()));
        event.getToolTip().add(Component.translatable("tooltip.cropgenetics.mutativity",
                genome.mutativity().dominant(), genome.mutativity().recessive()));
    }
}
