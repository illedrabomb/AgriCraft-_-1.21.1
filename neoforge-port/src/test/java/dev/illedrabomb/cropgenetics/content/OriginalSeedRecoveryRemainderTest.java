package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OriginalSeedRecoveryRemainderTest {
    @Test
    void namedPaperIsReturnedWithoutLosingComponentsOrCount() {
        ItemStack ingredient = new ItemStack(Items.PAPER, 16);
        ingredient.set(DataComponents.CUSTOM_NAME, Component.literal("Research notes"));

        ItemStack remainder = OriginalSeedRecoveryRecipe.paperRemainder(ingredient);

        assertEquals(1, remainder.getCount());
        assertEquals(ingredient.getComponents(), remainder.getComponents());
        assertEquals(16, ingredient.getCount());
        assertNotSame(ingredient, remainder);
    }

    @Test
    void unrelatedIngredientCannotBecomeFreePaper() {
        assertTrue(OriginalSeedRecoveryRecipe.paperRemainder(
                new ItemStack(Items.DIAMOND)).isEmpty());
    }
}
