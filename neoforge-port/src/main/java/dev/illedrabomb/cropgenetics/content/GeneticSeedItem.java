package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import java.util.List;

/** A seed whose genetic alleles are persisted on its ItemStack. */
public final class GeneticSeedItem extends Item {
    public GeneticSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context,
                                List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        Genome genome = stack.get(CropComponents.GENOME.get());
        if (genome != null) {
            tooltip.add(Component.translatable("tooltip.cropgenetics.plant", genome.plantId()));
            tooltip.add(Component.translatable("tooltip.cropgenetics.stats",
                    genome.displayedStats(GenePair.TraitMode.MAX)));
        }
    }
}
