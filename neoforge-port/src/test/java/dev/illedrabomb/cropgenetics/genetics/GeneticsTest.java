package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GeneticsTest {
    @Test
    void alleleOrderingAndDisplayModesMatchAgriCraft() {
        GenePair pair = new GenePair(3, 8);
        assertEquals(8, pair.dominant());
        assertEquals(3, pair.recessive());
        assertEquals(3, pair.expressed(GenePair.TraitMode.MIN));
        assertEquals(5, pair.expressed(GenePair.TraitMode.MEAN));
        assertEquals(8, pair.expressed(GenePair.TraitMode.MAX));
    }

    @Test
    void rejectsOutOfRangeAlleles() {
        assertThrows(IllegalArgumentException.class, () -> new GenePair(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new GenePair(1, 11));
    }

    @Test
    void visibleTenTenTenDoesNotImplyPerfectRecessives() {
        Genome genome = new Genome("minecraft:wheat",
            new GenePair(10, 1), new GenePair(10, 2),
            new GenePair(10, 3), new GenePair(5, 2), new GenePair(4, 1));
        assertTrue(genome.isPerfectVisible(GenePair.TraitMode.MAX));
        assertFalse(genome.isPerfectVisible(GenePair.TraitMode.MIN));
        assertEquals("10/10/10", genome.displayedStats(GenePair.TraitMode.MAX));
    }

    @Test
    void inheritanceStaysWithinOneToTen() {
        Genome a = new Genome("minecraft:wheat", new GenePair(10, 1),
            new GenePair(10, 1), new GenePair(10, 1),
            new GenePair(10, 1), new GenePair(10, 10));
        Genome b = Genome.basic("minecraft:carrot");
        Random seededRandom = new Random(42);
        for (int i = 0; i < 200; i++) {
            Genome child = BreedingEngine.combine(a, b, "minecraft:wheat", seededRandom);
            for (GenePair pair : new GenePair[]{
                child.growth(), child.gain(), child.strength(),
                child.fertility(), child.mutativity()}) {
                assertTrue(pair.recessive() >= 1);
                assertTrue(pair.dominant() <= 10);
            }
        }
    }
}
