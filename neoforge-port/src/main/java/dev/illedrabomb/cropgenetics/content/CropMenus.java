package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Distinct analyzer menus keep every ordinary Vanilla chest screen intact. */
public final class CropMenus {
    private CropMenus() {}

    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, CropGeneticsMod.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<SeedAnalyzerStandaloneMenu>> ANALYZER =
            MENUS.register("seed_analyzer", () ->
                    new MenuType<>(SeedAnalyzerStandaloneMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final DeferredHolder<MenuType<?>, MenuType<SeedAnalyzerCombinedMenu>> ANALYZER_CHEST =
            MENUS.register("seed_analyzer_chest", () ->
                    new MenuType<>(SeedAnalyzerCombinedMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void register(IEventBus bus) {
        MENUS.register(bus);
    }
}
