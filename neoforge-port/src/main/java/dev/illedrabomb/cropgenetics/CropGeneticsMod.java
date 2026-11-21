package dev.illedrabomb.cropgenetics;

import dev.illedrabomb.cropgenetics.command.CropDataCommand;
import dev.illedrabomb.cropgenetics.command.SoilCommand;
import dev.illedrabomb.cropgenetics.config.CropBalanceConfig;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropCreativeTabs;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropRecipes;
import dev.illedrabomb.cropgenetics.content.SoilChemistryAccess;
import dev.illedrabomb.cropgenetics.content.SoilDynamics;
import dev.illedrabomb.cropgenetics.content.SoilOverrideInvalidation;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropDataReloadListener;
import dev.illedrabomb.cropgenetics.data.SoilProfileRegistry;
import dev.illedrabomb.cropgenetics.data.SoilOverrideSavedData;
import dev.illedrabomb.cropgenetics.data.SoilProfileReloadListener;
import dev.illedrabomb.cropgenetics.data.SoilDynamicsReloadListener;
import dev.illedrabomb.cropgenetics.network.PlantConditionsSync;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(CropGeneticsMod.MOD_ID)
public final class CropGeneticsMod {
    public static final String MOD_ID = "cropgenetics";
    private static final AgriPlantsRegistry PLANTS = new AgriPlantsRegistry();
    private static final SoilProfileRegistry SOIL_PROFILES = new SoilProfileRegistry();

    public static AgriPlantsRegistry plants() { return PLANTS; }
    public static SoilProfileRegistry soilProfiles() { return SOIL_PROFILES; }

    public CropGeneticsMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, CropBalanceConfig.SPEC);
        CropComponents.register(modBus);
        CropBlocks.register(modBus);
        CropItems.register(modBus);
        CropRecipes.register(modBus);
        modBus.addListener(PlantConditionsSync::register);
        // Server datapack soil profiles (including imported originals) provide
        // explicit chemistry; dimension-local overrides take precedence.
        SoilChemistryAccess.install((level, soilPos) -> {
            if (!level.hasChunkAt(soilPos)) return java.util.Optional.empty();
            // Overrides are persisted per dimension and per soil block. A
            // missing override falls back to a static datapack soil profile.
            return SoilOverrideSavedData.get(level).sample(soilPos, level.getBlockState(soilPos))
                    .or(() -> SOIL_PROFILES.sample(level.getBlockState(soilPos)));
        });
        CropCreativeTabs.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
        NeoForge.EVENT_BUS.addListener(PlantConditionsSync::onDatapackSync);
        NeoForge.EVENT_BUS.addListener(SoilOverrideInvalidation::onBreak);
        NeoForge.EVENT_BUS.addListener(SoilOverrideInvalidation::onPlace);
        NeoForge.EVENT_BUS.addListener(SoilOverrideInvalidation::onMultiPlace);
        NeoForge.EVENT_BUS.addListener(SoilOverrideInvalidation::onLevelTickPost);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CropDataCommand.register(event, PLANTS);
        SoilCommand.register(event, SOIL_PROFILES);
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SoilProfileReloadListener(SOIL_PROFILES, mod -> ModList.get().isLoaded(mod)));
        event.addListener(new SoilDynamicsReloadListener(SoilDynamics::configure));
        event.addListener(new CropDataReloadListener(PLANTS, mod -> ModList.get().isLoaded(mod)));
    }
}
