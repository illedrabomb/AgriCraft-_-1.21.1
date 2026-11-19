package dev.illedrabomb.cropgenetics.compat.jei;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.SeedSpeciesCatalog;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IExtraIngredientRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.constants.RecipeTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import java.util.ArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import java.util.List;

/**
 * JEI is an optional runtime integration: only the compile-only API is required
 * for compiling this class, and JEI itself discovers the annotation if installed.
 */
@JeiPlugin
public final class CropGeneticsJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CropGeneticsMod.MOD_ID, "jei");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(CropItems.GENETIC_SEED.get(),
                new ISubtypeInterpreter<ItemStack>() {
                    @Override
                    public Object getSubtypeData(ItemStack stack, UidContext context) {
                        Genome genome = stack.get(CropComponents.GENOME.get());
                        // The ingredient browser groups by plant species, never by
                        // unanalysed gene values or arbitrary per-stack metadata.
                        return genome == null ? null : genome.plantId();
                    }

                    @Override
                    @SuppressWarnings("removal")
                    public String getLegacyStringSubtypeInfo(ItemStack stack, UidContext context) {
                        Genome genome = stack.get(CropComponents.GENOME.get());
                        return genome == null ? "" : genome.plantId();
                    }
                });
    }

    /**
     * Original seed recovery is a SpecialRecipe whose output depends on the
     * input genome and original item. JEI cannot infer that dynamic output.
     * Advertise concrete examples ONLY for species that the runtime recovery
     * logic can actually convert, without changing the vanilla recipe book.
     */
    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        List<RecipeHolder<CraftingRecipe>> displays = new ArrayList<>();
        for (String id : SeedSpeciesCatalog.DEFAULT_SPECIES) {
            ItemStack genetic = CropItems.seedWithGenome(Genome.basic(id));
            // JEI runs on the client, where the authoritative plant catalogue
            // is not guaranteed to be loaded. Use only known vanilla source
            // item IDs as display candidates, then ask the real recovery logic
            // whether that conversion is allowed in the current context.
            String original = SeedSpeciesCatalog.vanillaSeedForSpecies(id);
            if (original == null) continue;
            genetic.set(CropComponents.ORIGINAL_SEED.get(), original);
            var recovered = CropItems.recoverOriginalSeed(genetic);
            if (recovered.isEmpty()) continue;
            NonNullList<Ingredient> inputs = NonNullList.withSize(2, Ingredient.EMPTY);
            inputs.set(0, Ingredient.of(genetic));
            inputs.set(1, Ingredient.of(Items.PAPER));
            // This recipe is a JEI display only. The actual registered custom
            // recipe retains its reusable paper behavior and exact metadata.
            var display = new ShapelessRecipe("", CraftingBookCategory.MISC,
                    recovered.get(), inputs);
            ResourceLocation recipeId = ResourceLocation.fromNamespaceAndPath(
                    CropGeneticsMod.MOD_ID,
                    "jei_recover_" + id.replace(':', '_'));
            displays.add(new RecipeHolder<>(recipeId, display));
        }
        if (!displays.isEmpty()) {
            registration.addRecipes(RecipeTypes.CRAFTING, displays);
        }

        // An information page, not a fake crafting recipe: supported ordinary
        // seeds become genetic crops when planted on SINGLE crop sticks. The
        // actual genetic seed is obtained on breaking the planted sticks.
        // Link the same guide from the known vanilla starters, the genetic
        // seed and the crop-stick item in JEI's uses/info browser.
        List<ItemStack> entryPoints = new ArrayList<>();
        for (String species : SeedSpeciesCatalog.DEFAULT_SPECIES) {
            String source = SeedSpeciesCatalog.vanillaSeedForSpecies(species);
            if (source == null) continue;
            ResourceLocation key = ResourceLocation.tryParse(source);
            if (key != null) BuiltInRegistries.ITEM.getOptional(key)
                    .ifPresent(item -> entryPoints.add(new ItemStack(item)));
        }
        // Optional mod entry points: visible JEI help should be reachable
        // from seeds supplied by Pam's and Farmer's Delight as well. These
        // are only candidate item pairs; the SERVER datapack registry remains
        // authoritative about whether planting is actually enabled.
        addOptionalModSeedInfo(entryPoints);
        entryPoints.add(CropItems.GENETIC_SEED.get().getDefaultInstance());
        entryPoints.add(CropItems.CROP_STICKS.get().getDefaultInstance());
        registration.addItemStackInfo(entryPoints,
                Component.translatable("jei.cropgenetics.seed_conversion"));

        registration.addItemStackInfo(List.of(
                CropItems.SEED_ANALYZER.get().getDefaultInstance(),
                CropItems.SEED_CHEST.get().getDefaultInstance(),
                CropItems.SEED_JOURNAL.get().getDefaultInstance()),
                Component.translatable("jei.cropgenetics.seed_analysis"));
    }

    /** Only registered addon providers contribute third-party JEI links. */
    private static void addOptionalModSeedInfo(List<ItemStack> entryPoints) {
        // Neither Pam's nor Farmer's Delight is inferred in the core. In
        // particular, merely installing a source mod without its addon must
        // never claim Crop Sticks planting support in JEI.
        for (CropSeedGuideRegistry.SeedPair pair : CropSeedGuideRegistry.availablePairs()) {
            addSeedPair(entryPoints, pair.seedId(), pair.produceId());
        }
    }

    private static void addSeedPair(List<ItemStack> entryPoints,
                                    String seedId, String produceId) {
        ResourceLocation seed = ResourceLocation.tryParse(seedId);
        ResourceLocation produce = ResourceLocation.tryParse(produceId);
        if (seed == null || produce == null) return;
        var seedItem = BuiltInRegistries.ITEM.getOptional(seed);
        var productItem = BuiltInRegistries.ITEM.getOptional(produce);
        if (seedItem.isEmpty() || productItem.isEmpty()
                || seedItem.get() == Items.AIR || productItem.get() == Items.AIR)
            return;
        entryPoints.add(new ItemStack(seedItem.get()));
    }

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> seeds = SeedSpeciesCatalog.DEFAULT_SPECIES.stream()
                .map(id -> CropItems.seedWithGenome(Genome.basic(id)))
                .toList();
        registration.addExtraItemStacks(seeds);
    }
}
