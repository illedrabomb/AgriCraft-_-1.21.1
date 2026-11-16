package dev.illedrabomb.cropgenetics.data;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pure datapack-model guards. Real fruit ownership and the second world
 * fruit cycle still require an actual Minecraft server/client test.
 */
class GourdDefinitionRegressionTest {
    private static PlantDefinition plant(String id, HarvestMode mode, double fruitChance,
                                         List<PlantDefinition.HarvestProduct> products,
                                         int harvestStage) {
        return new PlantDefinition(
                id, List.of("minecraft"), List.of("minecraft:melon_seeds"),
                List.of(2, 4, 6, 8, 10, 12, 14, 16),
                harvestStage, 0.7, 0.025, true, 0.1, products,
                PlantRequirements.defaultFarmland(), mode, fruitChance);
    }

    private static List<PlantDefinition.HarvestProduct> oneMelon() {
        return List.of(new PlantDefinition.HarvestProduct("minecraft:melon", 1, 1, 1.0));
    }

    @Test
    void bothVanillaGourdsAllowARegrowthStageBeforeMaturity() {
        for (String id : List.of("minecraft:melon_plant", "minecraft:pumpkin_plant")) {
            PlantDefinition gourd = plant(id, HarvestMode.GOURD, 0.2,
                    oneMelon(), 4);
            assertEquals(8, gourd.stages().size());
            assertEquals(4, gourd.harvestStage());
            assertTrue(gourd.harvestStage() < gourd.stages().size() - 1,
                    "Regrowth age must be below maturity for " + id);
        }
    }

    @Test
    void unsupportedPhysicalGourdSpeciesAreRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> plant("minecraft:wheat_plant", HarvestMode.GOURD, 0.2,
                        oneMelon(), 4));
    }

    @Test
    void fruitChanceMustBePositiveFiniteAndOnlyForGourds() {
        for (double chance : new double[]{0.0, Double.NaN, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, -0.1, 1.1}) {
            assertThrows(IllegalArgumentException.class,
                    () -> plant("minecraft:melon_plant", HarvestMode.GOURD, chance,
                            oneMelon(), 4), "Invalid gourd chance " + chance);
        }
        assertThrows(IllegalArgumentException.class,
                () -> plant("minecraft:melon_plant", HarvestMode.STANDARD, 0.2,
                        oneMelon(), 4));
    }

    @Test
    void physicalFruitDefinitionMustHaveExactlyOneGuaranteedProduct() {
        assertThrows(IllegalArgumentException.class,
                () -> plant("minecraft:melon_plant", HarvestMode.GOURD, 0.2,
                        List.of(), 4));
        assertThrows(IllegalArgumentException.class,
                () -> plant("minecraft:melon_plant", HarvestMode.GOURD, 0.2,
                        List.of(new PlantDefinition.HarvestProduct(
                                "minecraft:melon", 1, 2, 1.0)), 4));
        assertThrows(IllegalArgumentException.class,
                () -> plant("minecraft:melon_plant", HarvestMode.GOURD, 0.2,
                        List.of(new PlantDefinition.HarvestProduct(
                                "minecraft:melon", 1, 1, 0.5)), 4));
    }
}
