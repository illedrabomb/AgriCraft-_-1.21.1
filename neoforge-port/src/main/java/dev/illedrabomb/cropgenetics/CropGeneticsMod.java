package dev.illedrabomb.cropgenetics;

import dev.illedrabomb.cropgenetics.command.CropDataCommand;
import dev.illedrabomb.cropgenetics.content.CropBlocks;
import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropCreativeTabs;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.content.CropRecipes;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.CropDataReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(CropGeneticsMod.MOD_ID)
public final class CropGeneticsMod {
    public static final String MOD_ID = "cropgenetics";
    private static final AgriPlantsRegistry PLANTS = new AgriPlantsRegistry();

    public static AgriPlantsRegistry plants() { return PLANTS; }

    public CropGeneticsMod(IEventBus modBus, ModContainer container) {
        CropComponents.register(modBus);
        CropBlocks.register(modBus);
        CropItems.register(modBus);
        CropRecipes.register(modBus);
        CropCreativeTabs.register(modBus);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);
        NeoForge.EVENT_BUS.addListener(this::registerCommands);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        CropDataCommand.register(event, PLANTS);
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new CropDataReloadListener(PLANTS, mod -> ModList.get().isLoaded(mod)));
    }
}
