package dev.illedrabomb.cropgenetics.content;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Display-only mapping for optional plant species names.
 *
 * Installed crop addons provide their own produce item mapping; the core
 * never needs to recognize specific third-party mod namespaces.
 */
public final class CropSpeciesDisplayRegistry {
    private static final Map<String, Function<String, String>> PROVIDERS = new HashMap<>();

    private CropSpeciesDisplayRegistry() {}

    public static synchronized void register(
            String addonId, Function<String, String> produceForSpecies) {
        Objects.requireNonNull(addonId);
        Objects.requireNonNull(produceForSpecies);
        if (!addonId.matches("[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException("Invalid crop display addon ID");
        if (PROVIDERS.containsKey(addonId))
            throw new IllegalArgumentException("Duplicate crop display addon: " + addonId);
        PROVIDERS.put(addonId, produceForSpecies);
    }

    /**
     * Conflicting providers never silently choose a product. Invalid or
     * temporarily unavailable optional providers cannot break the HUD.
     */
    public static String produceForSpecies(String species) {
        if (species == null) return null;
        List<Function<String, String>> providers;
        synchronized (CropSpeciesDisplayRegistry.class) {
            providers = PROVIDERS.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue).toList();
        }
        String result = null;
        for (Function<String, String> provider : providers) {
            try {
                String candidate = provider.apply(species);
                if (candidate == null) continue;
                if (!candidate.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                    continue;
                if (result != null && !result.equals(candidate))
                    return null;
                result = candidate;
            } catch (RuntimeException ignored) {
                // Display-only integration must not crash gameplay.
            }
        }
        return result;
    }
}
