package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.network.chat.Component;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Vanilla item-component equality, including hidden genetics, controls stacking. */
class GeneticSeedStackingTest {
    private static ItemStack seed(String species, int growth, int gain, int strength) {
        ItemStack stack = new ItemStack(CropItems.GENETIC_SEED.get());
        stack.set(CropComponents.GENOME.get(), new Genome(species, growth, gain, strength));
        return stack;
    }

    @Test
    void matchingGenomesCanShareAStackRegardlessOfParentHistory() {
        ItemStack first = seed("minecraft:wheat_plant", 4, 7, 9);
        ItemStack second = seed("minecraft:wheat_plant", 4, 7, 9);
        assertEquals(64, first.getMaxStackSize());
        // No parent-identity component is recorded on genetic seeds.
        assertTrue(ItemStack.isSameItemSameComponents(first, second));
    }

    @Test
    void differentSpeciesOrAnyGeneRemainSeparate() {
        ItemStack seed = seed("minecraft:wheat_plant", 4, 7, 9);
        assertFalse(ItemStack.isSameItemSameComponents(seed,
                seed("minecraft:carrot_plant", 4, 7, 9)));
        assertFalse(ItemStack.isSameItemSameComponents(seed,
                seed("minecraft:wheat_plant", 4, 8, 9)));
        assertFalse(ItemStack.isSameItemSameComponents(seed,
                seed("minecraft:wheat_plant", 5, 7, 9)));
        assertFalse(ItemStack.isSameItemSameComponents(seed,
                seed("minecraft:wheat_plant", 4, 7, 10)));
    }

    @Test
    void analysisProvenanceAndExternalComponentsStayDistinct() {
        ItemStack base = seed("minecraft:wheat_plant", 4, 7, 9);
        ItemStack analyzed = base.copy();
        analyzed.set(CropComponents.ANALYZED.get(), true);
        assertFalse(ItemStack.isSameItemSameComponents(base, analyzed));

        ItemStack recoveredFromOtherSeed = base.copy();
        recoveredFromOtherSeed.set(CropComponents.ORIGINAL_SEED.get(), "minecraft:beetroot_seeds");
        assertFalse(ItemStack.isSameItemSameComponents(base, recoveredFromOtherSeed));

        ItemStack named = base.copy();
        named.set(DataComponents.CUSTOM_NAME, Component.literal("special"));
        assertFalse(ItemStack.isSameItemSameComponents(base, named));
    }
}
