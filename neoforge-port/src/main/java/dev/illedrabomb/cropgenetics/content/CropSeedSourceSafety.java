package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import javax.annotation.Nullable;

/** Rules for preserving unknown item components during seed conversion. */
final class CropSeedSourceSafety {
    private CropSeedSourceSafety() {}

    static boolean canConvertWithoutExternalDataLoss(ItemStack source,
            @Nullable Genome attachedGenome, Item geneticSeed) {
        if (source == null || source.isEmpty()) return true;
        return source.is(geneticSeed) || attachedGenome != null
                || source.getComponentsPatch().isEmpty();
    }
}
