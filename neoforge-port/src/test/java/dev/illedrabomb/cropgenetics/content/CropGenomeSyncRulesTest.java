package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CropGenomeSyncRulesTest {
    @Test
    void unanalyzedCropSharesSpeciesButNotPrivateGeneValues() {
        Genome real = new Genome("minecraft:wheat_plant", 8, 9, 10);
        Genome visible = CropGenomeSyncRules.forClient(real, false);
        assertEquals("minecraft:wheat_plant", visible.plantId());
        assertEquals(1, visible.growth());
        assertEquals(1, visible.gain());
        assertEquals(1, visible.strength());
        assertEquals(8, real.growth()); // no mutation of authoritative data
    }

    @Test
    void analyzedCropCanDisplayItsActualValues() {
        Genome real = new Genome("minecraft:wheat_plant", 8, 9, 10);
        assertEquals(real, CropGenomeSyncRules.forClient(real, true));
    }

    @Test
    void emptyCropRemainsEmptyOnClient() {
        assertNull(CropGenomeSyncRules.forClient(null, false));
    }
}
