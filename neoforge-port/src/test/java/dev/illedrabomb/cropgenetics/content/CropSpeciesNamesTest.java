package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropSpeciesNamesTest {
    @Test
    void coreDoesNotInferOptionalProduceItemFromNamespaceAlone() {
        assertNull(CropSpeciesNames.preferredProduceItem("pamhc2crops:mustardseeds_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("farmersdelight:cabbage_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("farmersdelight:rice_plant"));
    }

    @Test
    void registeredAddonCanSupplyDisplayOnlyProduceItem() {
        CropSpeciesDisplayRegistry.register("display_test_alpha", species ->
                species.equals("testaddon:sample_plant") ? "testaddon:sample" : null);
        assertEquals("testaddon:sample",
                CropSpeciesNames.preferredProduceItem("testaddon:sample_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("minecraft:wheat_plant"));
    }

    @Test
    void invalidAndDuplicateMappingsFailClosed() {
        CropSpeciesDisplayRegistry.register("display_test_duplicate", species -> null);
        assertThrows(IllegalArgumentException.class,
                () -> CropSpeciesDisplayRegistry.register(
                        "display_test_duplicate", species -> "testaddon:a"));
        assertThrows(IllegalArgumentException.class,
                () -> CropSpeciesDisplayRegistry.register(
                        "Invalid-Id", species -> "testaddon:a"));
        CropSpeciesDisplayRegistry.register("display_test_bad_item", species ->
                species.equals("testaddon:bad_plant") ? "INVALID ITEM ID" : null);
        assertNull(CropSpeciesNames.preferredProduceItem("testaddon:bad_plant"));
    }
}
