package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Server-side recognition of ordinary seeds without inventing genotypes. */
final class CropGeneticsSeedRecognition {
    private CropGeneticsSeedRecognition() {}

    static boolean isRegisteredSeed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return CropGeneticsMod.plants().plantForSeed(id).isPresent();
    }
}
