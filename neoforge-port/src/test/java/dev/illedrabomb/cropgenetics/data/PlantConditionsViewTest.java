package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlantConditionsViewTest {
    private static PlantDefinition plant(String id, String... seeds) {
        return new PlantDefinition(id, List.of(), List.of(seeds),
                List.of(0, 1), 1, 0.5, 0, true, 0,
                List.of(new PlantDefinition.HarvestProduct("minecraft:wheat", 1, 1, 1)),
                new PlantRequirements(0, 12, Optional.empty(),
                        Optional.of("cropgenetics:fungal_soils"), 2),
                HarvestMode.STANDARD, 0);
    }

    @Test
    void snapshotProjectionIsSortedAndIgnoresAmbiguousSeedItems() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        registry.replaceAll(Map.of(
                "minecraft:sugar_cane_plant", plant("minecraft:sugar_cane_plant", "minecraft:sugar_cane", "minecraft:wheat_seeds"),
                "minecraft:brown_mushroom_plant", plant("minecraft:brown_mushroom_plant", "minecraft:brown_mushroom", "minecraft:wheat_seeds")),
                Map.of(), CropReloadReport.empty());
        PlantConditionsView view = PlantConditionsView.from(registry.snapshot());

        assertEquals(List.of("minecraft:brown_mushroom_plant", "minecraft:sugar_cane_plant"),
                view.plants().stream().map(PlantConditionsView.Entry::plantId).toList());
        assertEquals(List.of("minecraft:brown_mushroom"), view.plants().get(0).seedItems());
        assertEquals(PlantConditionsView.SiteRule.MUSHROOM_DARKNESS_EXEMPTION,
                view.plants().get(0).siteRule());
        assertEquals(PlantConditionsView.SiteRule.SUGAR_CANE_ADJACENT_WATER,
                view.plants().get(1).siteRule());
        assertEquals(12, view.plants().get(1).requirements().maxLight());
        assertThrows(UnsupportedOperationException.class, () -> view.plants().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> view.plants().get(0).seedItems().clear());
    }

    @Test
    void capturedViewDoesNotChangeAfterAnotherReload() {
        AgriPlantsRegistry registry = new AgriPlantsRegistry();
        registry.replaceAll(Map.of("minecraft:cactus_plant", plant("minecraft:cactus_plant", "minecraft:cactus")),
                Map.of(), CropReloadReport.empty());
        PlantConditionsView old = PlantConditionsView.from(registry.snapshot());
        registry.replaceAll(Map.of(), Map.of(), CropReloadReport.empty());

        assertEquals(PlantConditionsView.SiteRule.CACTUS_CLEAR_SIDES,
                old.plants().get(0).siteRule());
        assertTrue(PlantConditionsView.from(registry.snapshot()).plants().isEmpty());
    }

    @Test
    void failsClosedForOversizedCatalogueInsteadOfTruncating() {
        List<PlantConditionsView.Entry> entries = java.util.Collections.nCopies(
                PlantConditionsView.MAX_PLANTS + 1,
                new PlantConditionsView.Entry("minecraft:wheat_plant", List.of(),
                        PlantRequirements.defaultFarmland(), PlantConditionsView.SiteRule.NONE));
        assertThrows(IllegalArgumentException.class, () -> new PlantConditionsView(entries));
    }
}
