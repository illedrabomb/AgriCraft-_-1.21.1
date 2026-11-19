package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgriPlantsRegistrySnapshotTest {
    private static PlantDefinition plant(String species, String seed) {
        return new PlantDefinition(species, List.of(), List.of(seed),
                List.of(2, 4, 6, 8), 2, 0.5D, 0.01D, true, 0.1D,
                List.of(new PlantDefinition.HarvestProduct(
                        "minecraft:wheat", 1, 2, 1.0D)),
                new PlantRequirements(9, 15,
                        Optional.of("minecraft:farmland"), Optional.empty(), 0),
                HarvestMode.STANDARD, 0.0D);
    }

    @Test
    void sharedSeedIsDisabledInsteadOfChoosingArbitraryPlant() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        String sameSeed = "minecraft:wheat_seeds";
        registry.replaceAll(Map.of(
                "cropgenetics:first_plant", plant("cropgenetics:first_plant", sameSeed),
                "cropgenetics:second_plant", plant("cropgenetics:second_plant", sameSeed)),
                Map.of(), CropReloadReport.empty());

        assertTrue(registry.plantForSeed(sameSeed).isEmpty());
        assertTrue(registry.ambiguousSeedItems().contains(sameSeed));
    }

    @Test
    void newReloadReplacesAllSeedMappingsAndClearsOldAmbiguities() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        String shared = "minecraft:wheat_seeds";
        String carrot = "minecraft:carrot";
        registry.replaceAll(Map.of(
                "cropgenetics:first_plant", plant("cropgenetics:first_plant", shared),
                "cropgenetics:second_plant", plant("cropgenetics:second_plant", shared)),
                Map.of(), CropReloadReport.empty());
        AgriPlantsRegistry.Snapshot before = registry.snapshot();

        registry.replaceAll(Map.of(
                "cropgenetics:carrot_plant", plant("cropgenetics:carrot_plant", carrot)),
                Map.of(), CropReloadReport.empty());

        assertTrue(registry.ambiguousSeedItems().isEmpty());
        assertTrue(registry.plantForSeed(shared).isEmpty());
        assertEquals("cropgenetics:carrot_plant",
                registry.plantForSeed(carrot).orElseThrow().id());
        // Captured snapshots remain immutable and internally consistent.
        assertTrue(before.ambiguousSeedItems().contains(shared));
        assertTrue(before.plantForSeed(carrot).isEmpty());
        assertThrows(UnsupportedOperationException.class,
                () -> before.plants().clear());
    }
}
