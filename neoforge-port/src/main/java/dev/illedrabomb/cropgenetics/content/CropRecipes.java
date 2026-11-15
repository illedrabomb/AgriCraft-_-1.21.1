package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Recipes whose results depend on the genetic input ItemStack. */
public final class CropRecipes {
    private CropRecipes() {}

    private static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CropGeneticsMod.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<OriginalSeedRecoveryRecipe>>
            ORIGINAL_SEED_RECOVERY = SERIALIZERS.register("original_seed_recovery",
                    () -> new SimpleCraftingRecipeSerializer<>(OriginalSeedRecoveryRecipe::new));

    public static void register(IEventBus bus) {
        SERIALIZERS.register(bus);
    }
}
