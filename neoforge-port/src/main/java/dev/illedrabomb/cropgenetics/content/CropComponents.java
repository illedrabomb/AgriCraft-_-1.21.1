package dev.illedrabomb.cropgenetics.content;

import com.mojang.serialization.Codec;
import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.genetics.Genome;
import dev.illedrabomb.cropgenetics.genetics.GenomeCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registered, persistent genetic item state. */
public final class CropComponents {
    private CropComponents() {}

    private static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, CropGeneticsMod.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Genome>> GENOME =
            COMPONENTS.registerComponentType("genome",
                    builder -> builder.persistent(GenomeCodecs.GENOME)
                            .networkSynchronized(GenomeCodecs.STREAM_CODEC));

    /** The analyzer sets this on the specific seed stack, never on the species. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ANALYZED =
            COMPONENTS.registerComponentType("analyzed",
                    builder -> builder.persistent(Codec.BOOL));

    public static void register(IEventBus bus) {
        COMPONENTS.register(bus);
    }
}
