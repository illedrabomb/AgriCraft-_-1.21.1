package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Old allele pairs are retained only as a migration representation. */
class GenePairDisplayContractTest {
    @Test
    void legacyPairsStillSortTheirValuesForMigration() {
        GenePair old = new GenePair(1, 3);
        assertEquals(3, old.dominant());
        assertEquals(1, old.recessive());
        Genome migrated = new Genome("minecraft:wheat_plant", old.dominant(), 1, 1);
        assertEquals("3/1/1", migrated.displayedStats());
    }

    @Test
    void freshVanillaAndGeneticSeedsStartWithTheSameThreeStats() {
        assertEquals(Genome.basic("minecraft:carrot_plant"),
                new Genome("minecraft:carrot_plant", 1, 1, 1));
    }
}
