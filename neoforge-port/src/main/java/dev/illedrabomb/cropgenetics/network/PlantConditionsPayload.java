package dev.illedrabomb.cropgenetics.network;

import dev.illedrabomb.cropgenetics.CropGeneticsMod;
import dev.illedrabomb.cropgenetics.data.PlantConditionsWire;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Server to client only; version is embedded in the bounded wire document. */
public record PlantConditionsPayload(byte[] data) implements CustomPacketPayload {
    public static final Type<PlantConditionsPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CropGeneticsMod.MOD_ID, "plant_conditions_v1"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlantConditionsPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> buffer.writeByteArray(payload.data),
                    buffer -> new PlantConditionsPayload(buffer.readByteArray(PlantConditionsWire.MAX_BYTES)));

    public PlantConditionsPayload {
        if (data == null || data.length > PlantConditionsWire.MAX_BYTES)
            throw new IllegalArgumentException("Invalid plant conditions payload size");
        data = data.clone();
    }

    @Override
    public byte[] data() { return data.clone(); }

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
