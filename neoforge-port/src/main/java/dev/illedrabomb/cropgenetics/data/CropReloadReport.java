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
     * A source-category count from the immutable report, not a guessed total
     * from JSON files or raw Pam's registry items. Auto-discovery entries use
     * a virtual resource path; no extra mutable counters are required.
     */
    public record PamDiscoveryStats(int explicitLoaded, int autoDiscovered,
                                    int autoConflicts, int autoMissingItems,
                                    int autoWarnings, boolean suspended) {}

    public PamDiscoveryStats pamDiscoveryStats() {
        int explicit = 0;
        int discovered = 0;
        int conflicts = 0;
        int missing = 0;
        int warnings = 0;
        boolean suspended = false;
        for (Entry entry : entries) {
            if (entry.kind() != Kind.PLANT) continue;
            boolean auto = entry.source().getNamespace().equals("cropgenetics")
                    && entry.source().getPath().startsWith("auto_pam/");
            if (auto) {
                switch (entry.status()) {
                    case AUTO_DISCOVERED -> discovered++;
                    case AUTO_MISSING_ITEM -> missing++;
                    case CONFLICT -> conflicts++;
                    default -> warnings++;
                }
            } else if (entry.source().getPath().startsWith("cropgenetics/plants/")
                    && (entry.status() == Status.LOADED
                        || entry.status() == Status.WARNING)) {
                explicit++;
            } else if (entry.source().toString().equals(
                    "cropgenetics:cropgenetics/pam_auto_discovery.json")
                    && ((entry.status() == Status.INVALID)
                        || entry.detail().startsWith("Pam's automatic discovery disabled"))) {
                suspended = true;
            }
        }
        return new PamDiscoveryStats(explicit, discovered, conflicts, missing, warnings, suspended);
    }

    /** Per-adapter diagnostics; Pam's historic summary remains unchanged. */
    public record AdapterDiscoveryStats(int discovered, int conflicts, int missingItems,
                                        int otherWarnings, boolean suspended) {}

    public AdapterDiscoveryStats farmersDelightStats() {
        int discovered = 0;
        int conflicts = 0;
        int missing = 0;
        int warnings = 0;
        boolean suspended = false;
        for (Entry entry : entries) {
            if (entry.kind() != Kind.PLANT
                    || !entry.source().getNamespace().equals("cropgenetics")
                    || !entry.source().getPath().startsWith("auto_farmers_delight/"))
                continue;
            if (entry.source().getPath().equals(
                    "auto_farmers_delight/discovery_suspended")) {
                suspended = true;
                continue;
            }
            switch (entry.status()) {
                case AUTO_DISCOVERED -> discovered++;
                case CONFLICT -> conflicts++;
                case AUTO_MISSING_ITEM -> missing++;
                default -> warnings++;
            }
        }
        return new AdapterDiscoveryStats(discovered, conflicts, missing, warnings, suspended);
    }

    public Map<Status, Integer> totals() {
        Map<Status, Integer> totals = new EnumMap<>(Status.class);
        for (Status status : Status.values()) totals.put(status, 0);
        for (Entry entry : entries) totals.merge(entry.status(), 1, Integer::sum);
        return Map.copyOf(totals);
    }
}
