package dev.illedrabomb.cropgenetics.genetics;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
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
    void currentThreeStatGenomeSurvivesActualWorldNbtFormat() {
        Genome original = new Genome("minecraft:carrot_plant", 3, 7, 10);
        var encoded = GenomeCodecs.GENOME.encodeStart(NbtOps.INSTANCE, original)
                .result().orElseThrow();
        assertInstanceOf(CompoundTag.class, encoded);
        CompoundTag tag = (CompoundTag) encoded;

        assertEquals("minecraft:carrot_plant", tag.getString("plant"));
        assertEquals(3, tag.getInt("growth"));
        assertEquals(7, tag.getInt("gain"));
        assertEquals(10, tag.getInt("strength"));
        assertFalse(tag.contains("fertility"));
        assertFalse(tag.contains("mutativity"));

        Genome restored = GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, tag)
                .result().orElseThrow();
        assertEquals(original, restored);
    }

    @Test
    void legacyFivePairNbtMigratesWithoutLosingExpressedValues() {
        CompoundTag legacy = new CompoundTag();
        legacy.putString("plant", "minecraft:wheat_plant");
        for (String trait : new String[] {
                "growth", "gain", "strength", "fertility", "mutativity" }) {
            CompoundTag pair = new CompoundTag();
            pair.putInt("dominant", trait.equals("growth") ? 8
                    : trait.equals("gain") ? 6 : trait.equals("strength") ? 4 : 1);
            pair.putInt("recessive", 1);
            legacy.put(trait, pair);
        }

        Genome restored = GenomeCodecs.GENOME.parse(NbtOps.INSTANCE, legacy)
                .result().orElseThrow();
        assertEquals(new Genome("minecraft:wheat_plant", 8, 6, 4), restored);
        // A subsequent world save must use only the supported three-stat model.
        CompoundTag rewritten = (CompoundTag) GenomeCodecs.GENOME
                .encodeStart(NbtOps.INSTANCE, restored).result().orElseThrow();
        assertFalse(rewritten.contains("fertility"));
        assertFalse(rewritten.contains("mutativity"));
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
