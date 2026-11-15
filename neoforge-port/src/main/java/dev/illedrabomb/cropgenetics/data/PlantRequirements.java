package dev.illedrabomb.cropgenetics.data;

import java.util.Objects;
import java.util.Optional;

/**
 * Minimal explicit planting conditions for the NeoForge port.
 * Old AgriPlants soil chemistry/seasons are NOT silently interpreted as vanilla farmland.
 */
public record PlantRequirements(int minLight, int maxLight, Optional<String> soilBlock,
                                Optional<String> soilTag, int lightTolerance) {
    public PlantRequirements {
        Objects.requireNonNull(soilBlock);
        Objects.requireNonNull(soilTag);
        if (lightTolerance < 0 || lightTolerance > 15) {
            throw new IllegalArgumentException("Light tolerance must be within 0..15");
        }
        if (minLight < 0 || maxLight > 15 || minLight > maxLight) {
            throw new IllegalArgumentException("Light range must be within 0..15");
        }
        if (soilBlock.isPresent() && soilTag.isPresent()) {
            throw new IllegalArgumentException("Specify either soil_block or soil_tag, not both");
        }
        soilBlock.ifPresent(PlantRequirements::checkId);
        soilTag.ifPresent(PlantRequirements::checkId);
    }

    private static void checkId(String value) {
        if (!value.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Invalid soil identifier: " + value);
        }
    }

    public static PlantRequirements defaultFarmland() {
        return new PlantRequirements(9, 15, Optional.of("minecraft:farmland"), Optional.empty(), 0);
    }
}
