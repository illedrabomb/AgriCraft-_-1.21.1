package dev.illedrabomb.cropgenetics.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Optional seed-to-produce JEI guide candidates registered by installed addons.
 * No source-mod-specific item rules belong to the core JEI plugin.
 */
public final class CropSeedGuideRegistry {
    public record SeedPair(String seedId, String produceId) {
        public SeedPair {
            Objects.requireNonNull(seedId);
            Objects.requireNonNull(produceId);
        }
    }

    private static final Map<String, Supplier<List<SeedPair>>> ADDONS = new HashMap<>();

    private CropSeedGuideRegistry() {}

    public static synchronized void register(String addonId,
                                              Supplier<List<SeedPair>> provider) {
        Objects.requireNonNull(addonId);
        Objects.requireNonNull(provider);
        if (!addonId.matches("[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException("Invalid seed guide addon ID");
        if (ADDONS.containsKey(addonId))
            throw new IllegalArgumentException("Duplicate seed guide addon: " + addonId);
        ADDONS.put(addonId, provider);
    }

    /** A broken optional guide cannot prevent the rest of JEI from loading. */
    public static List<SeedPair> availablePairs() {
        List<Map.Entry<String, Supplier<List<SeedPair>>>> providers;
        synchronized (CropSeedGuideRegistry.class) {
            providers = ADDONS.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey()).toList();
        }
        List<SeedPair> result = new ArrayList<>();
        for (var provider : providers) {
            try {
                List<SeedPair> pairs = provider.getValue().get();
                if (pairs != null) {
                    for (SeedPair pair : pairs) {
                        if (pair != null) result.add(pair);
                    }
                }
            } catch (RuntimeException ignored) {
                // Optional display-only provider: retain all healthy guides.
            }
        }
        return List.copyOf(result);
    }
}
