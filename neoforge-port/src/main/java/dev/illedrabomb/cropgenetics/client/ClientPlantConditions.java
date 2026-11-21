package dev.illedrabomb.cropgenetics.client;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantConditionsView;
import dev.illedrabomb.cropgenetics.data.PlantConditionsWire;
import java.util.Optional;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

/** Physical-client-only cache; never uses server registries or player NBT. */
@EventBusSubscriber(modid = CropGeneticsMod.MOD_ID, value = Dist.CLIENT)
public final class ClientPlantConditions {
    private static volatile PlantConditionsView current;
    private ClientPlantConditions() {}

    public static Optional<PlantConditionsView> current() { return Optional.ofNullable(current); }

    /** Called by the main-thread payload handler. Empty means unavailable. */
    public static void receive(byte[] bytes) {
        if (bytes.length == 0) {
            current = null;
            return;
        }
        try {
            current = PlantConditionsWire.decode(bytes);
        } catch (IllegalArgumentException ex) {
            current = null;
        }
    }

    @SubscribeEvent
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) { current = null; }
}
