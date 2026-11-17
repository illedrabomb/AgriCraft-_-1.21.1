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

    @Override
    public void registerExtraIngredients(IExtraIngredientRegistration registration) {
        List<ItemStack> seeds = SeedSpeciesCatalog.DEFAULT_SPECIES.stream()
                .map(id -> CropItems.seedWithGenome(Genome.basic(id)))
                .toList();
        registration.addExtraItemStacks(seeds);
    }
}
