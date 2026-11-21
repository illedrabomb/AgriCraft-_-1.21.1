package dev.illedrabomb.cropgenetics.config;

import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.WeakHashMap;

/** Non-persistent admin test override, scoped to one running server instance. */
public final class HarvestDropOverride {
    private static final Map<MinecraftServer, Double> SERVER_OVERRIDES = new WeakHashMap<>();

    private HarvestDropOverride() {}

    public static synchronized double effective(MinecraftServer server) {
        Double override = server == null ? null : SERVER_OVERRIDES.get(server);
        return override != null ? override : CropBalanceConfig.HARVEST_DROP_MULTIPLIER.get();
    }

    public static synchronized boolean isOverridden(MinecraftServer server) {
        return server != null && SERVER_OVERRIDES.containsKey(server);
    }

    public static synchronized void set(MinecraftServer server, double value) {
        if (server == null || !Double.isFinite(value) || value < 0 || value > 64)
            throw new IllegalArgumentException("Invalid server harvest multiplier override");
        SERVER_OVERRIDES.put(server, value);
    }

    public static synchronized void reset(MinecraftServer server) {
        if (server != null) SERVER_OVERRIDES.remove(server);
    }
}
