package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Dedicated inventory section for crop genetics tools and plant species. */
public final class CropCreativeTabs {
    private CropCreativeTabs() {}

    /** Client-visible catalog for the built-in seed variants and JEI extras. */
    public static final java.util.List<String> DEFAULT_SPECIES = SeedSpeciesCatalog.DEFAULT_SPECIES;

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CropGeneticsMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CROP_GENETICS =
            TABS.register("crop_genetics", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cropgenetics.crop_genetics"))
                    .icon(() -> CropItems.seedWithGenome(Genome.basic("minecraft:wheat_plant")))
                    .displayItems((parameters, output) -> {
                        output.accept(CropItems.CROP_STICKS.get());
                        output.accept(CropItems.SEED_ANALYZER.get());
                        output.accept(CropItems.SEED_CHEST.get());
                        for (String species : DEFAULT_SPECIES) {
                            // Each displayed item is a usable seed with its own genome.
                            output.accept(CropItems.seedWithGenome(Genome.basic(species)));
                        }
                    }).build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
