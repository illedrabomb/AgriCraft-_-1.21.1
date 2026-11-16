package dev.illedrabomb.cropgenetics.content;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * One genetic seed and one sheet of paper, in any two distinct grid slots,
 * produce exactly one plain original seed. The paper is returned unchanged.
 *
 * The source item is stored on the seed's components, so the result must be
 * assembled dynamically rather than specified as a fixed JSON item.
 */
public final class OriginalSeedRecoveryRecipe extends CustomRecipe {
    public OriginalSeedRecoveryRecipe(CraftingBookCategory category) {
        super(category);
    }

    private static ItemStack recoveryIngredient(CraftingInput input) {
        if (input.ingredientCount() != 2) return ItemStack.EMPTY;
        ItemStack geneticSeed = ItemStack.EMPTY;
        boolean foundPaper = false;

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(Items.PAPER)) {
                if (foundPaper) return ItemStack.EMPTY;
                foundPaper = true;
            } else if (stack.is(CropItems.GENETIC_SEED.get())) {
                if (!geneticSeed.isEmpty()) return ItemStack.EMPTY;
                geneticSeed = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return foundPaper ? geneticSeed : ItemStack.EMPTY;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        ItemStack seed = recoveryIngredient(input);
        return !seed.isEmpty() && CropItems.recoverOriginalSeed(seed).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack seed = recoveryIngredient(input);
        return seed.isEmpty() ? ItemStack.EMPTY
                : CropItems.recoverOriginalSeed(seed).orElse(ItemStack.EMPTY);
    }

    /**
     * The crafting menu consumes one item from each occupied slot. Return
     * exactly one paper in its original slot, offsetting that consumption.
     * Return nothing for the seed slot so exactly one genetic seed is spent.
     */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        if (recoveryIngredient(input).isEmpty()) return remaining;
        for (int i = 0; i < input.size(); i++) {
            if (input.getItem(i).is(Items.PAPER)) {
                remaining.set(i, new ItemStack(Items.PAPER));
            }
        }
        return remaining;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return CropRecipes.ORIGINAL_SEED_RECOVERY.get();
    }
}
