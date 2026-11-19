package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Server-side recognition of ordinary seeds without inventing genotypes. */
final class CropGeneticsSeedRecognition {
    private CropGeneticsSeedRecognition() {}

    /**
     * Plain item route shared by Crop Sticks and the analyzer. Customized
     * stacks are storage-only until a dedicated lossless adapter exists.
     */
    static boolean isUnmodifiedRegisteredSeed(ItemStack stack) {
        return stack != null && !stack.isEmpty()
                && !stack.is(CropItems.GENETIC_SEED.get())
                && !CropItems.hasGenome(stack)
                && stack.getComponentsPatch().isEmpty()
                && isRegisteredSeed(stack);
    }

    static boolean isRegisteredSeed(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        return CropGeneticsMod.plants().plantForSeed(id).isPresent();
    }
}
