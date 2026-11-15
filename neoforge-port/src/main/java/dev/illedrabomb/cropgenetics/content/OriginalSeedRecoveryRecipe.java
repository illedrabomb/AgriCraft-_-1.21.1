package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * One genetic seed anywhere in the 2x2/3x3 grid -> one plain source seed.
 *
 * A dynamic special recipe is required because the original item is encoded
 * on the ingredient ItemStack, rather than being a fixed JSON recipe result.
 */
public final class OriginalSeedRecoveryRecipe extends CustomRecipe {
    public OriginalSeedRecoveryRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack soleIngredient(CraftingInput input) {
        if (input.ingredientCount() != 1) return ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty()) return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack stack = soleIngredient(input);
        return !stack.isEmpty() && CropItems.recoverOriginalSeed(stack).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack stack = soleIngredient(input);
        return stack.isEmpty() ? ItemStack.EMPTY
                : CropItems.recoverOriginalSeed(stack).orElse(ItemStack.EMPTY);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= 1 && height >= 1;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CropRecipes.ORIGINAL_SEED_RECOVERY.get();
    }
}
