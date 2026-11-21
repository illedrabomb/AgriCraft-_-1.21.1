package dev.illedrabomb.cropgenetics.network;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.client.ClientPlantConditions;
import dev.illedrabomb.cropgenetics.data.PlantConditionsView;
import dev.illedrabomb.cropgenetics.data.PlantConditionsWire;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

/** Registration and server-authoritative login/reload broadcast. */
public final class PlantConditionsSync {
    private static final Logger LOGGER = LogUtils.getLogger();
    private PlantConditionsSync() {}

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(PlantConditionsPayload.TYPE,
                PlantConditionsPayload.STREAM_CODEC,
                (payload, context) -> ClientPlantConditions.receive(payload.data()));
    }

    public static void onDatapackSync(OnDatapackSyncEvent event) {
        try {
            // One consistent registry snapshot and one immutable wire image
            // for all players in this sync event.
            PlantConditionsView view = PlantConditionsView.from(CropGeneticsMod.plants().snapshot());
            PlantConditionsPayload payload = new PlantConditionsPayload(PlantConditionsWire.encode(view));
            event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, payload));
        } catch (IllegalArgumentException ex) {
            // Never send a truncated or invented catalogue. Existing clients
            // must regard this snapshot as unavailable (separate invalidation
            // is sent even if the real data exceeded its byte budget).
            LOGGER.warn("Unable to sync public plant conditions: {}", ex.getMessage());
            PlantConditionsPayload empty = new PlantConditionsPayload(new byte[0]);
            event.getRelevantPlayers().forEach(player -> PacketDistributor.sendToPlayer(player, empty));
        }
    }
}
