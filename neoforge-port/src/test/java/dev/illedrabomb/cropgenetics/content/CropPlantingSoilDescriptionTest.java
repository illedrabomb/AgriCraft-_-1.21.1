package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.data.PlantRequirements;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Soil denial must name the actual rule rather than guess a specific block. */
class CropPlantingSoilDescriptionTest {
    @Test
    void exactSoilRuleNamesRequiredBlock() {
        PlantRequirements farmland = PlantRequirements.defaultFarmland();
        assertEquals("minecraft:farmland", CropPlantingFeedback.requiredSoil(farmland));
    }

    @Test
    void tagRuleKeepsTagMarkerInsteadOfInventingABlock() {
        PlantRequirements soils = new PlantRequirements(0, 15,
                Optional.empty(), Optional.of("cropgenetics:soils/mushrooms"), 0);
        assertEquals("#cropgenetics:soils/mushrooms",
                CropPlantingFeedback.requiredSoil(soils));
    }
}
