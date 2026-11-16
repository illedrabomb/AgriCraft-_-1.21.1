package dev.illedrabomb.cropgenetics.data;

import java.util.Optional;

/**
 * Optional exact environmental requirements. Unlike AgriPlants' historical
 * fluid predicates, fluidId matches the fluid state at the crop position.
 * Height is the crop block's Y coordinate, with inclusive bounds.
 */
public record EnvironmentFilter(Optional<Integer> minY, Optional<Integer> maxY,
                                Optional<String> fluidId) {
    public static final EnvironmentFilter UNRESTRICTED =
            new EnvironmentFilter(Optional.empty(), Optional.empty(), Optional.empty());

    public EnvironmentFilter {
        if (minY == null || maxY == null || fluidId == null)
            throw new IllegalArgumentException("Environmental optionals must be non-null");
        if (minY.isPresent() && (minY.get() < -2048 || minY.get() > 2048)
                || maxY.isPresent() && (maxY.get() < -2048 || maxY.get() > 2048))
            throw new IllegalArgumentException("Height limits must be within -2048..2048");
        if (minY.isPresent() && maxY.isPresent() && minY.get() > maxY.get())
            throw new IllegalArgumentException("min_y exceeds max_y");
        fluidId.ifPresent(id -> {
            if (id.length() > 128 || !id.matches("[a-z0-9_.-]+:[a-z0-9_./-]+"))
                throw new IllegalArgumentException("Invalid fluid ID: " + id);
        });
    }
}
