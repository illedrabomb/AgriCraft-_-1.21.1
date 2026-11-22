package dev.illedrabomb.cropgenetics.content;

import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Any hidden genotype must remain individual before and after analysis. */
class GeneticSeedStackingTest {
    @Test
    void defaultGeneticSeedIsAlwaysUnstackable() {
        GeneticSeedItem geneticSeed = CropItems.GENETIC_SEED.get();
        ItemStack seed = new ItemStack(geneticSeed);

        // An unanalyzed seed has no analyzed marker yet, but still cannot
        // merge with a different hidden genotype in a normal inventory.
        assertEquals(1, seed.getMaxStackSize());
    }
}
