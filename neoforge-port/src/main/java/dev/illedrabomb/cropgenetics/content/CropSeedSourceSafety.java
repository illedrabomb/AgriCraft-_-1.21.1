package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

/**
 * Conversion only carries our three seed-specific components. Never pretend
 * that arbitrary external component patches survive conversion to another item.
 */
final class CropSeedSourceSafety {
    private CropSeedSourceSafety() {}

    static boolean canConvertWithoutExternalDataLoss(ItemStack source) {
        if (source == null || source.isEmpty()) return true;
        for (var entry : source.getComponentsPatch().entrySet()) {
            DataComponentType<?> type = entry.getKey();
            if (type != CropComponents.GENOME.get()
                    && type != CropComponents.ANALYZED.get()
                    && type != CropComponents.ORIGINAL_SEED.get()) {
                return false;
            }
        }
        return true;
    }
}
