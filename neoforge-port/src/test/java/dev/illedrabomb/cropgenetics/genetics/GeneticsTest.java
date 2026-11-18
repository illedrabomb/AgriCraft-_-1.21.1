package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

class GeneticsTest {
    @Test
    void classicThreeStatsAreVisibleAndBounded() {
        Genome genome = new Genome("minecraft:wheat", 3, 5, 8);
        assertEquals("3/5/8", genome.displayedStats());
        assertEquals(3, genome.growth());
        assertEquals(5, genome.gain());
        assertEquals(8, genome.strength());
        assertFalse(genome.isPerfectVisible());
        assertTrue(new Genome("minecraft:wheat", 10, 10, 10).isPerfectVisible());
    }

    @Test
    void rejectsInvalidStatValues() {
        assertThrows(IllegalArgumentException.class, () -> new Genome("minecraft:wheat", 0, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new Genome("minecraft:wheat", 1, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> new Genome("minecraft:wheat", 1, 1, 12));
    }

    @Test
    void inheritanceStaysWithinOneToTen() {
        Genome a = new Genome("minecraft:wheat", 10, 1, 10);
        Genome b = Genome.basic("minecraft:carrot");
        Random random = new Random(42);
        for (int i = 0; i < 200; i++) {
            Genome child = BreedingEngine.combine(a, b, "minecraft:wheat", random);
            assertTrue(child.growth() >= 1 && child.growth() <= 10);
            assertTrue(child.gain() >= 1 && child.gain() <= 10);
            assertTrue(child.strength() >= 1 && child.strength() <= 10);
        }
    }
}
