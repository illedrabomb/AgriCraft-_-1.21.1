package dev.illedrabomb.cropgenetics.data;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Registration boundary between the core and independently packaged add-ons.
 * No external mod or adapter class is referenced from the host.
 */
public final class CropDiscoveryAddonRegistry {
    private static final List<CropDiscoveryAddon> ADDONS = new ArrayList<>();

    private CropDiscoveryAddonRegistry() {}

    /**
     * Register at add-on mod construction, before the first datapack reload.
     * Duplicate identities fail fast rather than running twice.
     */
    public static synchronized void register(CropDiscoveryAddon addon) {
        Objects.requireNonNull(addon, "addon");
        String id = Objects.requireNonNull(addon.addonId(), "addon ID");
        String required = Objects.requireNonNull(addon.requiredModId(), "target mod ID");
        if (!id.matches("[a-z][a-z0-9_]*") || !required.matches("[a-z][a-z0-9_]*"))
            throw new IllegalArgumentException("Invalid crop addon identity");
        if (ADDONS.stream().anyMatch(existing -> existing.addonId().equals(id)))
            throw new IllegalArgumentException("Duplicate crop addon registration: " + id);
        ADDONS.add(addon);
        ADDONS.sort(Comparator.comparing(CropDiscoveryAddon::addonId));
    }

    /** Snapshot for a reload, filtered to actually installed target mods. */
    public static synchronized List<CropDiscoveryAddon> loaded(Predicate<String> isLoaded) {
        return ADDONS.stream().filter(a -> isLoaded.test(a.requiredModId())).toList();
    }
}
