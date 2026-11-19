package dev.illedrabomb.cropgenetics.data;

import net.minecraft.resources.ResourceLocation;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable diagnostics for one server datapack reload.
 * Resource locations are retained so pack authors can find problem files.
 */
public record CropReloadReport(List<Entry> entries) {
    public enum Kind { PLANT, MUTATION }
    public enum Status { LOADED, AUTO_DISCOVERED, AUTO_MISSING_ITEM, WARNING, DISABLED, MISSING_MOD, INVALID, CONFLICT, MISSING_SPECIES }

    public record Entry(Kind kind, ResourceLocation source, String plantId,
                        Status status, String detail) {
        public Entry {
            Objects.requireNonNull(kind);
            Objects.requireNonNull(source);
            Objects.requireNonNull(status);
            plantId = plantId == null ? "" : plantId;
            detail = detail == null ? "" : detail;
        }
    }

    public CropReloadReport {
        entries = List.copyOf(entries);
    }

    public static CropReloadReport empty() {
        return new CropReloadReport(List.of());
    }

    public long count(Kind kind, Status status) {
        return entries.stream().filter(e -> e.kind() == kind && e.status() == status).count();
    }

    /**
     * Generic, immutable discovery statistics for an addon-owned diagnostic
     * namespace. The core knows nothing about the source mod's identity.
     */
    public record AutoDiscoveryStats(int discovered, int conflicts,
                                     int missingItems, int otherWarnings,
                                     boolean suspended) {}

    /**
     * Summarize one addon's virtual resource prefix. A deliberately skipped
     * explicit claim is informational, not a warning or a new plant.
     *
     * @param prefix e.g. "auto_example/"
     * @param configPath optional relative resource path used for invalid
     *                   addon policy diagnostics; empty when none is used
     */
    public AutoDiscoveryStats autoDiscoveryStats(String prefix, String configPath) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(configPath);
        if (!prefix.matches("[a-z0-9_/-]+/")
                || (!configPath.isEmpty()
                    && !configPath.matches("[a-z0-9_./-]+")))
            throw new IllegalArgumentException("Invalid addon diagnostic path");

        int discovered = 0;
        int conflicts = 0;
        int missing = 0;
        int warnings = 0;
        boolean suspended = false;
        for (Entry entry : entries) {
            if (entry.kind() != Kind.PLANT
                    || !entry.source().getNamespace().equals("cropgenetics"))
                continue;
            String path = entry.source().getPath();
            if ((!configPath.isEmpty() && path.equals(configPath))
                    && (entry.status() == Status.INVALID
                        || entry.detail().contains("discovery disabled"))) {
                suspended = true;
                continue;
            }
            if (!path.startsWith(prefix)) continue;
            if (path.equals(prefix + "discovery_suspended")) {
                suspended = true;
                continue;
            }
            switch (entry.status()) {
                case AUTO_DISCOVERED -> discovered++;
                case CONFLICT -> conflicts++;
                case AUTO_MISSING_ITEM -> missing++;
                case DISABLED -> {}
                default -> warnings++;
            }
        }
        return new AutoDiscoveryStats(discovered, conflicts, missing, warnings, suspended);
    }

    public Map<Status, Integer> totals() {
        Map<Status, Integer> totals = new EnumMap<>(Status.class);
        for (Status status : Status.values()) totals.put(status, 0);
        for (Entry entry : entries) totals.merge(entry.status(), 1, Integer::sum);
        return Map.copyOf(totals);
    }
}
