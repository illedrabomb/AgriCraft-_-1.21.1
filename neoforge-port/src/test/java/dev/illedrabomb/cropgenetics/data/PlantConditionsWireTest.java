package dev.illedrabomb.cropgenetics.data;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlantConditionsWireTest {
    @Test
    void roundTripPreservesPublicRequirementsAndSpecialRules() {
        PlantRequirements requirements = new PlantRequirements(3, 12,
                Optional.empty(), Optional.of("cropgenetics:fungal_soils"), 2,
                List.of("minecraft:swamp"), List.of("minecraft:desert"),
                List.of("minecraft:overworld"), List.of(),
                new EnvironmentFilter(Optional.of(-20), Optional.of(100), Optional.empty()),
                new SeasonRule(List.of("autumn")),
                new SoilChemistryRule(Optional.of(new SoilChemistryRule.Condition("wet",
                        SoilChemistryRule.Comparison.EQUAL_OR_HIGHER, 0.2)),
                        Optional.empty(), Optional.empty()));
        PlantConditionsView view = new PlantConditionsView(List.of(
                new PlantConditionsView.Entry("minecraft:brown_mushroom_plant",
                        List.of("minecraft:brown_mushroom"), requirements,
                        PlantConditionsView.SiteRule.MUSHROOM_DARKNESS_EXEMPTION)));

        assertEquals(view, PlantConditionsWire.decode(PlantConditionsWire.encode(view)));
    }

    @Test
    void malformedAndFuturePayloadsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> PlantConditionsWire.decode(new byte[0]));
        assertThrows(IllegalArgumentException.class,
                () -> PlantConditionsWire.decode(new byte[PlantConditionsWire.MAX_BYTES + 1]));
        assertThrows(IllegalArgumentException.class,
                () -> PlantConditionsWire.decode(new byte[] {0, 0, 0, 2, 0, 0, 0, 0}));
        assertThrows(IllegalArgumentException.class,
                () -> PlantConditionsWire.decode(new byte[] {0, 0, 0, 1, 0, 0, 2, 1}));
    }
}
