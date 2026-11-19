package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import dev.illedrabomb.cropgenetics.compat.jei.CropSeedGuideRegistry;
import dev.illedrabomb.cropgenetics.command.CropDataCommandExtensions;
import dev.illedrabomb.cropgenetics.data.CropDiscoveryAddon;
import dev.illedrabomb.cropgenetics.data.CropDiscoveryAddonRegistry;
import dev.illedrabomb.cropgenetics.data.CropReloadReport;
import dev.illedrabomb.cropgenetics.data.ExplicitPlantClaims;
import dev.illedrabomb.cropgenetics.data.PlantDefinition;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import java.util.List;
import java.util.Map;

/** Three reviewed generic Crop Sticks pairs; rice is deliberately excluded. */
@Mod(FarmersDelightAddonMod.MOD_ID)
public final class FarmersDelightAddonMod implements CropDiscoveryAddon {
    public static final String MOD_ID = "cropgenetics_farmersdelight";

    public FarmersDelightAddonMod(IEventBus bus, ModContainer container) {
        CropDiscoveryAddonRegistry.register(this);
        CropDataCommandExtensions.register("farmersdelight", FarmersDelightDataCommands::show);
        CropSeedGuideRegistry.register(MOD_ID, () -> List.of(
                new CropSeedGuideRegistry.SeedPair(
                        "farmersdelight:cabbage_seeds", "farmersdelight:cabbage"),
                new CropSeedGuideRegistry.SeedPair(
                        "farmersdelight:tomato_seeds", "farmersdelight:tomato"),
                new CropSeedGuideRegistry.SeedPair(
                        "farmersdelight:onion", "farmersdelight:onion")));
    }

    @Override public String addonId() { return MOD_ID; }

    @Override public String requiredModId() { return "farmersdelight"; }

    @Override
    public void discover(ResourceManager resources, Map<String, PlantDefinition> plants,
                         ExplicitPlantClaims claims,
                         List<CropReloadReport.Entry> diagnostics) {
        FarmersDelightCropDiscovery.addMissing(
                plants, claims.species(), claims.seeds(), claims.produce(), diagnostics);
    }
}
