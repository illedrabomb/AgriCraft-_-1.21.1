package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/**
 * The server-authoritative analysis marker lives on this ItemStack. Detailed
 * genetics are displayed by the client-only Shift tooltip event.
 */
public final class GeneticSeedItem extends Item {
    public GeneticSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (genome == null) return super.getName(stack);
        return Component.translatable("item.cropgenetics.genetic_seed_named",
                CropSpeciesNames.label(genome.plantId()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (genome == null) return;
        tooltip.add(Component.translatable("tooltip.cropgenetics.plant", genome.plantId()));
        tooltip.add(Component.translatable(CropItems.isAnalyzed(stack)
                ? "tooltip.cropgenetics.analyzed_hint"
                : "tooltip.cropgenetics.unanalyzed"));
    }
}
