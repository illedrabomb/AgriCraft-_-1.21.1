package dev.illedrabomb.cropgenetics;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/** NeoForge entry point. Registration of crop content follows in subsequent commits. */
@Mod(CropGeneticsMod.MOD_ID)
public final class CropGeneticsMod {
    public static final String MOD_ID = "cropgenetics";

    public CropGeneticsMod(IEventBus modBus, ModContainer container) {
        // No hard dependency on the old InfinityLib or AgriCore runtime.
    }
}
