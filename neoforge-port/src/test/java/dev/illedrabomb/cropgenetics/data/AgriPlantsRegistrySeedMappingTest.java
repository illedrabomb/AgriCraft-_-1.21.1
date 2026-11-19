package dev.illedrabomb.cropgenetics.data;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

/** Duplicate ordinary seed IDs must never be assigned to an arbitrary crop. */
class AgriPlantsRegistrySeedMappingTest {
    private static PlantDefinition plant(String id, String seed) {
        return new PlantDefinition(id, List.of(), List.of(seed),
                List.of(2, 4, 6), 2, 0.75D, 0.025D, true, 0.1D,
                List.of(new PlantDefinition.HarvestProduct(
                        "minecraft:wheat", 1, 2, 1.0D)),
                new PlantRequirements(9, 15,
                        Optional.of("minecraft:farmland"), Optional.empty(), 0),
                HarvestMode.STANDARD, 0.0D);
    }

    @Test
    void ambiguousSeedIsRejectedForBothSpecies() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        String seed = "minecraft:wheat_seeds";
        registry.replaceAll(Map.of(
                "example:first", plant("example:first", seed),
                "example:second", plant("example:second", seed)),
                Map.of(), CropReloadReport.empty());
        assertTrue(registry.ambiguousSeedItems().contains(seed));
        assertTrue(registry.plantForSeed(seed).isEmpty());
    }

    @Test
    void distinctSeedMappingsRemainAvailable() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        registry.replaceAll(Map.of(
                "example:first", plant("example:first", "minecraft:wheat_seeds"),
                "example:second", plant("example:second", "minecraft:beetroot_seeds")),
                Map.of(), CropReloadReport.empty());
        assertEquals("example:first",
                registry.plantForSeed("minecraft:wheat_seeds").orElseThrow().id());
        assertEquals("example:second",
                registry.plantForSeed("minecraft:beetroot_seeds").orElseThrow().id());
        assertTrue(registry.ambiguousSeedItems().isEmpty());
    }
}
