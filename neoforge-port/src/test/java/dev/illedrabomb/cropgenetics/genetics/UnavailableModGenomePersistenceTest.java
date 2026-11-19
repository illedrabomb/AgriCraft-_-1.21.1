package dev.illedrabomb.cropgenetics.genetics;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * A seed or planted crop keeps its genome even when a mod species is absent
 * from the currently loaded crop catalogue. Decoding must not consult it.
 */
class UnavailableModGenomePersistenceTest {
    @Test
    void unknownOptionalModSpeciesSurvivesCodecRoundTrip() {
        Genome original = new Genome("pamhc2crops:missing_mod_crop_plant", 9, 5, 7);
        var serialized = GenomeCodecs.GENOME.encodeStart(JsonOps.INSTANCE, original)
                .result().orElseThrow();
        Genome loaded = GenomeCodecs.GENOME.parse(JsonOps.INSTANCE, serialized)
                .result().orElseThrow();
        assertEquals(original, loaded);
    }

    @Test
    void legacyGenomeOfUnavailableSpeciesCanStillMigrate() {
        var source = JsonParser.parseString("""
                {
                  "plant":"farmersdelight:onion_plant",
                  "growth":{"dominant":6,"recessive":2},
                  "gain":{"dominant":4,"recessive":1},
                  "strength":{"dominant":9,"recessive":3},
                  "fertility":{"dominant":1,"recessive":1},
                  "mutativity":{"dominant":1,"recessive":1}
                }
                """);
        Genome loaded = GenomeCodecs.GENOME.parse(JsonOps.INSTANCE, source)
                .result().orElseThrow();
        assertEquals("farmersdelight:onion_plant", loaded.plantId());
        assertEquals(6, loaded.growth());
        assertEquals(4, loaded.gain());
        assertEquals(9, loaded.strength());
    }

    @Test
    void malformedModlessGenomeCannotReplaceASavedPlant() {
        var malformed = JsonParser.parseString("""
                {"plant":"farmersdelight:onion_plant","growth":50,"gain":1,"strength":1}
                """);
        assertTrue(GenomeCodecs.GENOME.parse(JsonOps.INSTANCE, malformed)
                .result().isEmpty());
    }
}
