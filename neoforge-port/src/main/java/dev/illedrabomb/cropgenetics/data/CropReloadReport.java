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
    public enum Status { LOADED, DISABLED, MISSING_MOD, INVALID, CONFLICT, MISSING_SPECIES }

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

    public Map<Status, Integer> totals() {
        Map<Status, Integer> totals = new EnumMap<>(Status.class);
        for (Status status : Status.values()) totals.put(status, 0);
        for (Entry entry : entries) totals.merge(entry.status(), 1, Integer::sum);
        return Map.copyOf(totals);
    }
}
