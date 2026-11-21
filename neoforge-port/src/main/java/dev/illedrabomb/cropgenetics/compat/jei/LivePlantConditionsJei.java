package dev.illedrabomb.cropgenetics.compat.jei;

import dev.illedrabomb.cropgenetics.client.ClientPlantConditions;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** JEI-only bridge. Never referenced unless JEI is actually installed. */
public final class LivePlantConditionsJei {
    private static IJeiRuntime runtime;
    private static final Map<String, Info> CACHE = new HashMap<>();
    private static List<IJeiIngredientInfoRecipe> visible = List.of();
    private static Info status;
    private LivePlantConditionsJei() {}

    public static void available(IJeiRuntime jei) {
        runtime = jei;
        CACHE.clear();
        visible = List.of();
        status = null;
        var ingredient = jei.getIngredientManager().createTypedIngredient(
                VanillaTypes.ITEM_STACK, CropItems.GENETIC_SEED.get().getDefaultInstance(), false);
        ingredient.ifPresent(value -> {
            status = new Info("", List.of(value));
            jei.getRecipeManager().addRecipes(RecipeTypes.INFORMATION, List.of(status));
        });
        refresh();
    }

    public static void unavailable() {
        runtime = null;
        CACHE.clear();
        visible = List.of();
        status = null;
    }

    /** Call on the physical client's main thread after each new rules snapshot. */
    public static void refresh() {
        if (runtime == null) return;
        var manager = runtime.getRecipeManager();
        if (!visible.isEmpty()) manager.hideRecipes(RecipeTypes.INFORMATION, visible);
        List<IJeiIngredientInfoRecipe> next = new ArrayList<>();
        List<IJeiIngredientInfoRecipe> newlyAdded = new ArrayList<>();
        ClientPlantConditions.current().ifPresent(view -> {
            for (var plant : view.plants()) {
                // Include only unambiguous ordinary seed sources and the
                // genetic seed for this exact species; never claim that an
                // arbitrary installed mod's seeds are active.
                List<ItemStack> stacks = new ArrayList<>();
                stacks.add(CropItems.seedWithGenome(Genome.basic(plant.plantId())));
                for (String seed : plant.seedItems()) {
                    ResourceLocation key = ResourceLocation.tryParse(seed);
                    if (key != null) BuiltInRegistries.ITEM.getOptional(key)
                            .filter(item -> item != Items.AIR)
                            .ifPresent(item -> stacks.add(new ItemStack(item)));
                }
                List<ITypedIngredient<?>> ingredients = stacks.stream()
                        .map(stack -> runtime.getIngredientManager()
                                .createTypedIngredient(VanillaTypes.ITEM_STACK, stack, false))
                        .flatMap(java.util.Optional::stream)
                        .map(item -> (ITypedIngredient<?>) item).toList();
                if (ingredients.isEmpty()) continue;
                String signature = plant.plantId() + "|" + String.join("|", plant.seedItems());
                Info info = CACHE.get(signature);
                if (info == null) {
                    info = new Info(plant.plantId(), ingredients);
                    CACHE.put(signature, info);
                    newlyAdded.add(info);
                }
                next.add(info);
            }
        });
        if (!newlyAdded.isEmpty()) manager.addRecipes(RecipeTypes.INFORMATION, newlyAdded);
        // Reused recipe objects are reactivated after reload; their text is
        // resolved against the current snapshot at display time.
        if (!next.isEmpty()) manager.unhideRecipes(RecipeTypes.INFORMATION, next);
        visible = List.copyOf(next);
    }

    private record Info(String plantId, List<ITypedIngredient<?>> ingredients)
            implements IJeiIngredientInfoRecipe {
        @Override
        public List<ITypedIngredient<?>> getIngredients() { return ingredients; }

        @Override
        public List<FormattedText> getDescription() {
            if (plantId.isEmpty())
                return List.of(Component.translatable(ClientPlantConditions.current().isPresent()
                        ? "jei.cropgenetics.conditions.ready" : "jei.cropgenetics.conditions.pending"));
            return ClientPlantConditions.current().stream()
                    .flatMap(view -> view.plants().stream())
                    .filter(entry -> entry.plantId().equals(plantId))
                    .findFirst()
                    .<List<FormattedText>>map(entry -> PlantConditionsText.lines(entry).stream()
                            .map(line -> (FormattedText) line).toList())
                    .orElseGet(() -> List.of(Component.translatable("jei.cropgenetics.conditions.pending")));
        }
    }
}
