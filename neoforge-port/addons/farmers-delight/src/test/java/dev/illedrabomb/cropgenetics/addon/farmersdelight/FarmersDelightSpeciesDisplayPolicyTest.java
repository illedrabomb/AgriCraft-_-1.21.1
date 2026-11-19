package dev.illedrabomb.cropgenetics.addon.farmersdelight;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FarmersDelightSpeciesDisplayPolicyTest {
    @Test
    void namesOnlyTheThreeReviewedPlants() {
        assertEquals("farmersdelight:cabbage",
                FarmersDelightSpeciesDisplayPolicy.produceForSpecies(
                        "farmersdelight:cabbage_plant"));
        assertEquals("farmersdelight:tomato",
                FarmersDelightSpeciesDisplayPolicy.produceForSpecies(
                        "farmersdelight:tomato_plant"));
        assertEquals("farmersdelight:onion",
                FarmersDelightSpeciesDisplayPolicy.produceForSpecies(
                        "farmersdelight:onion_plant"));
        assertNull(FarmersDelightSpeciesDisplayPolicy.produceForSpecies(
                "farmersdelight:rice_plant"));
        assertNull(FarmersDelightSpeciesDisplayPolicy.produceForSpecies(
                "minecraft:cabbage_plant"));
    }
}
