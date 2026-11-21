package dev.illedrabomb.cropgenetics.data;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlantConditionsWireTest {
    @Test
    void emptyCatalogueUsesCompleteVersionedFrame() {
        var empty = new PlantConditionsView(List.of());
        byte[] bytes = PlantConditionsWire.encode(empty);
        assertArrayEquals(new byte[] {0, 0, 0, 1, 0, 0, 0, 0}, bytes);
        assertEquals(empty, PlantConditionsWire.decode(bytes));
    }

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

    @Test
    void rejectsTrailingBytesAndTruncatedPublishedSnapshot() {
        byte[] valid = PlantConditionsWire.encode(new PlantConditionsView(List.of(
                new PlantConditionsView.Entry("minecraft:wheat_plant", List.of("minecraft:wheat_seeds"),
                        PlantRequirements.defaultFarmland(), PlantConditionsView.SiteRule.NONE))));

        assertThrows(IllegalArgumentException.class,
                () -> PlantConditionsWire.decode(Arrays.copyOf(valid, valid.length + 1)));
        assertThrows(IllegalArgumentException.class,
                () -> PlantConditionsWire.decode(Arrays.copyOf(valid, valid.length - 1)));
        assertEquals(1, PlantConditionsWire.decode(valid).plants().size());
    }

    @Test
    void rejectsInvalidSiteRuleWithoutAcceptingPartialCatalogue() throws IOException {
        byte[] invalid = PlantConditionsWire.encode(new PlantConditionsView(List.of(
                new PlantConditionsView.Entry("minecraft:cactus_plant", List.of("minecraft:cactus"),
                        PlantRequirements.defaultFarmland(), PlantConditionsView.SiteRule.CACTUS_CLEAR_SIDES))));
        var input = new DataInputStream(new ByteArrayInputStream(invalid));
        input.readInt(); // protocol version
        input.readInt(); // entry count
        input.readUTF(); // plant ID
        int seedCount = input.readUnsignedByte();
        for (int i = 0; i < seedCount; i++) input.readUTF();
        int siteRuleOffset = invalid.length - input.available();
        invalid[siteRuleOffset] = (byte) 127;

        assertThrows(IllegalArgumentException.class, () -> PlantConditionsWire.decode(invalid));
    }

    @Test
    void rejectsMalformedResourceIdsFromTheWire() throws IOException {
        byte[] original = PlantConditionsWire.encode(new PlantConditionsView(List.of(
                new PlantConditionsView.Entry("minecraft:wheat_plant", List.of("minecraft:wheat_seeds"),
                        PlantRequirements.defaultFarmland(), PlantConditionsView.SiteRule.NONE))));

        // Modified-UTF ASCII first byte after the two-byte string length.
        byte[] invalidPlant = original.clone();
        invalidPlant[10] = (byte) '?';
        assertThrows(IllegalArgumentException.class, () -> PlantConditionsWire.decode(invalidPlant));

        var input = new DataInputStream(new ByteArrayInputStream(original));
        input.readInt(); // version
        input.readInt(); // plant count
        input.readUTF(); // plant ID
        assertEquals(1, input.readUnsignedByte());
        int seedStart = original.length - input.available();
        byte[] invalidSeed = original.clone();
        invalidSeed[seedStart + 2] = (byte) '?';
        assertThrows(IllegalArgumentException.class, () -> PlantConditionsWire.decode(invalidSeed));
    }

    @Test
    void refusesOversizedSeedListsAndIdsBeforeSending() {
        var oversizedSeeds = java.util.Collections.nCopies(65, "minecraft:wheat_seeds");
        // The public projection now rejects these before the encoder sees them.
        assertThrows(IllegalArgumentException.class, () -> new PlantConditionsView.Entry(
                "minecraft:wheat_plant", oversizedSeeds, PlantRequirements.defaultFarmland(),
                PlantConditionsView.SiteRule.NONE));
        assertThrows(IllegalArgumentException.class, () -> new PlantConditionsView.Entry(
                "x".repeat(129), List.of(), PlantRequirements.defaultFarmland(),
                PlantConditionsView.SiteRule.NONE));
    }

    @Test
    void malformedWireCatalogueWithDuplicateSpeciesIsRejected() {
        var requirements = PlantRequirements.defaultFarmland();
        byte[] encoded = PlantConditionsWire.encode(new PlantConditionsView(List.of(
                new PlantConditionsView.Entry("minecraft:wheat_plant", List.of(), requirements,
                        PlantConditionsView.SiteRule.NONE),
                new PlantConditionsView.Entry("minecraft:melon_plant", List.of(), requirements,
                        PlantConditionsView.SiteRule.NONE))));
        // Both identifiers have the same UTF-8 length, so changing only the
        // second species name preserves the wire layout and checks uniqueness.
        byte[] original = "minecraft:melon_plant".getBytes(StandardCharsets.UTF_8);
        byte[] replacement = "minecraft:wheat_plant".getBytes(StandardCharsets.UTF_8);
        assertEquals(original.length, replacement.length);
        int matches = 0;
        for (int i = 0; i <= encoded.length - original.length; i++) {
            boolean found = true;
            for (int j = 0; j < original.length; j++) {
                if (encoded[i + j] != original[j]) { found = false; break; }
            }
            if (found) {
                System.arraycopy(replacement, 0, encoded, i, replacement.length);
                matches++;
                i += original.length - 1;
            }
        }
        assertEquals(1, matches);
        assertThrows(IllegalArgumentException.class, () -> PlantConditionsWire.decode(encoded));
    }
}
