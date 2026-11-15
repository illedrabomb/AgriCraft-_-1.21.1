package dev.illedrabomb.cropgenetics;

import dev.illedrabomb.cropgenetics.content.CropComponents;
import dev.illedrabomb.cropgenetics.content.CropItems;
import dev.illedrabomb.cropgenetics.data.AgriPlantsRegistry;
import dev.illedrabomb.cropgenetics.data.MutationReloadListener;
import dev.illedrabomb.cropgenetics.data.PlantReloadListener;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

/** Entry point for the AgriCraft-derived NeoForge implementation. */
@Mod(CropGeneticsMod.MOD_ID)
public final class CropGeneticsMod {
    public static final String MOD_ID = "cropgenetics";
    private final AgriPlantsRegistry plants = new AgriPlantsRegistry();

    public CropGeneticsMod(IEventBus modBus, ModContainer container) {
        CropComponents.register(modBus);
        CropItems.register(modBus);
        modBus.addListener(this::addCreativeSeeds);
        NeoForge.EVENT_BUS.addListener(this::registerReloadListeners);
    }

    private void addCreativeSeeds(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(CropItems.seedWithGenome(Genome.basic("minecraft:carrot_plant")));
        }
    }

    private void registerReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new PlantReloadListener(plants, mod -> ModList.get().isLoaded(mod)));
        event.addListener(new MutationReloadListener(plants));
    }
}
