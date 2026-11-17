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
    public static final java.util.List<String> DEFAULT_SPECIES = java.util.List.of(
            "minecraft:wheat_plant",
            "minecraft:carrot_plant",
            "minecraft:potato_plant",
            "minecraft:beet_root_plant",
            "minecraft:nether_wart_plant",
            "minecraft:melon_plant",
            "minecraft:pumpkin_plant",
            "minecraft:cornflower_plant",
            "minecraft:poppy_plant",
            "minecraft:dandelion_plant",
            "minecraft:cactus_plant",
            "minecraft:sugar_cane_plant",
            "minecraft:bamboo_plant",
            "minecraft:sweet_berry_plant",
            "minecraft:brown_mushroom_plant",
            "minecraft:red_mushroom_plant");

    private static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CropGeneticsMod.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CROP_GENETICS =
            TABS.register("crop_genetics", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cropgenetics.crop_genetics"))
                    .icon(() -> CropItems.seedWithGenome(Genome.basic("minecraft:wheat_plant")))
                    .displayItems((parameters, output) -> {
                        output.accept(CropItems.CROP_STICKS.get());
                        output.accept(CropItems.SEED_ANALYZER.get());
                        for (String species : DEFAULT_SPECIES) {
                            // Each displayed item is a usable seed with its own genome.
                            output.accept(CropItems.seedWithGenome(Genome.basic(species)));
                        }
                    }).build());

    public static void register(IEventBus bus) {
        TABS.register(bus);
    }
}
