package dev.illedrabomb.cropgenetics.compat.jei;

import dev.illedrabomb.cropgenetics.client.ClientPlantConditions;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.data.PlantConditionsView;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** JEI-only bridge. Never referenced unless JEI is actually installed. */
public final class LivePlantConditionsJei {
    /** JEI 19.x can hide but not remove registered recipes at runtime. */
    static final int MAX_CACHED_RECIPES = PlantConditionsView.MAX_PLANTS * 8;
    private static final RecipeType<Info> TYPE = RecipeType.create(
            CropGeneticsMod.MOD_ID, "plant_conditions", Info.class);
    private static IJeiRuntime runtime;
    private static final Map<String, Info> CACHE = new HashMap<>();
    private static List<Info> visible = List.of();
    private static Info status;
    private static boolean cacheLimitReached;
    private LivePlantConditionsJei() {}

    public static void registerCategories(IRecipeCategoryRegistration registration) {
        IDrawable icon = registration.getJeiHelpers().getGuiHelper()
                .createDrawableItemStack(CropItems.GENETIC_SEED.get().getDefaultInstance());
        registration.addRecipeCategories(new Category(icon));
    }

    public static void available(IJeiRuntime jei) {
        runtime = jei;
        CACHE.clear();
        visible = List.of();
        status = null;
        cacheLimitReached = false;
        var ingredient = jei.getIngredientManager().createTypedIngredient(
                VanillaTypes.ITEM_STACK, CropItems.GENETIC_SEED.get().getDefaultInstance(), false);
        ingredient.ifPresent(value -> {
            status = new Info("", "", List.of(value));
            jei.getRecipeManager().addRecipes(TYPE, List.of(status));
        });
        refresh();
    }

    public static void unavailable() {
        runtime = null;
        CACHE.clear();
        visible = List.of();
        status = null;
        cacheLimitReached = false;
    }

    /** Call on the physical client's main thread after each new rules snapshot. */
    public static void refresh() {
        if (runtime == null) return;
        var manager = runtime.getRecipeManager();
        if (!visible.isEmpty()) manager.hideRecipes(TYPE, visible);
        List<Info> next = new ArrayList<>();
        List<Info> newlyAdded = new ArrayList<>();
        cacheLimitReached = false;
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
                String signature = signature(plant);
                Info info = CACHE.get(signature);
                if (info == null) {
                    if (CACHE.size() >= MAX_CACHED_RECIPES) {
                        // Do not keep registering hidden recipes indefinitely.
                        // This species is omitted rather than falsely linked
                        // to an older seed mapping. The status page warns.
                        cacheLimitReached = true;
                        continue;
                    }
                    info = new Info(plant.plantId(), signature, ingredients);
                    CACHE.put(signature, info);
                    newlyAdded.add(info);
                }
                next.add(info);
            }
        });
        if (!newlyAdded.isEmpty()) manager.addRecipes(TYPE, newlyAdded);
        // Reused recipe objects are reactivated after reload; their text is
        // resolved against the current snapshot at display time.
        if (!next.isEmpty()) manager.unhideRecipes(TYPE, next);
        visible = List.copyOf(next);
    }

    private static String signature(PlantConditionsView.Entry entry) {
        return entry.plantId() + "|" + String.join("|", entry.seedItems());
    }

    private record Info(String plantId, String seedSignature,
                        List<ITypedIngredient<?>> ingredients) {
        public List<FormattedText> description() {
            if (plantId.isEmpty())
                return cacheLimitReached
                        ? List.of(Component.translatable("jei.cropgenetics.conditions.cache_limit"))
                        : List.of(Component.translatable(ClientPlantConditions.current().isPresent()
                                ? "jei.cropgenetics.conditions.ready" : "jei.cropgenetics.conditions.pending"));
            return ClientPlantConditions.current().stream()
                    .flatMap(view -> view.plants().stream())
                    // JEI may keep a previously opened page alive after an
                    // update. Do not attach the new conditions to an old
                    // seed-to-species link that the server has removed.
                    .filter(entry -> seedSignature.equals(signature(entry)))
                    .findFirst()
                    .<List<FormattedText>>map(entry -> PlantConditionsText.lines(entry).stream()
                            .map(line -> (FormattedText) line).toList())
                    .orElseGet(() -> List.of(Component.translatable(
                            ClientPlantConditions.current().isPresent()
                                    ? "jei.cropgenetics.conditions.stale"
                                    : "jei.cropgenetics.conditions.pending")));
        }
    }

    /** Own public JEI extension point, not JEI's @NonExtendable information recipe. */
    private record Category(IDrawable icon) implements IRecipeCategory<Info> {
        @Override
        public RecipeType<Info> getRecipeType() { return TYPE; }

        @Override
        public Component getTitle() {
            return Component.translatable("jei.cropgenetics.conditions.title");
        }

        @Override
        public int getWidth() { return 170; }

        @Override
        public int getHeight() { return 125; }

        @Override
        public IDrawable getIcon() { return icon; }

        @Override
        public void setRecipe(IRecipeLayoutBuilder builder, Info recipe, IFocusGroup focuses) {
            var input = builder.addInputSlot(77, 1).setStandardSlotBackground();
            var output = builder.addInvisibleIngredients(mezz.jei.api.recipe.RecipeIngredientRole.OUTPUT);
            for (ITypedIngredient<?> ingredient : recipe.ingredients()) {
                input.addTypedIngredient(ingredient);
                output.addTypedIngredient(ingredient);
            }
        }

        @Override
        public void createRecipeExtras(IRecipeExtrasBuilder builder, Info recipe, IFocusGroup focuses) {
            builder.addScrollBoxWidget(170, 103, 0, 22).setContents(recipe.description());
        }
    }
}
