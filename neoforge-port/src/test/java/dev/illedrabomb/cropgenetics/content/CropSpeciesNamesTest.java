package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CropSpeciesNamesTest {
    @Test
    void farmerPilotUsesDocumentedProduceNames() {
        assertEquals("farmersdelight:cabbage",
                CropSpeciesNames.preferredProduceItem("farmersdelight:cabbage_plant"));
        assertEquals("farmersdelight:tomato",
                CropSpeciesNames.preferredProduceItem("farmersdelight:tomato_plant"));
        assertEquals("farmersdelight:onion",
                CropSpeciesNames.preferredProduceItem("farmersdelight:onion_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("farmersdelight:rice_plant"));
    }

    @Test
    void pamNamingPreservesRepeatedSeedsWord() {
        assertEquals("pamhc2crops:mustardseedsitem",
                CropSpeciesNames.preferredProduceItem("pamhc2crops:mustardseeds_plant"));
    }

    @Test
    void unrelatedSpeciesDoNotInventProduceItems() {
        assertNull(CropSpeciesNames.preferredProduceItem("minecraft:wheat_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("example:tomato_plant"));
        assertNull(CropSpeciesNames.preferredProduceItem("pamhc2crops:invalid-name_plant"));
    }
}
