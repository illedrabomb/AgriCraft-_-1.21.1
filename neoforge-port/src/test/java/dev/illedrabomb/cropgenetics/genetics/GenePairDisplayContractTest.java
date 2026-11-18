package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Clarifies that 3/1 denotes two alleles, never value/max. */
class GenePairDisplayContractTest {
    @Test
    void displayKeepsTheHigherAlleleOnTheLeft() {
        assertEquals(new GenePair(3, 1), new GenePair(1, 3));
        assertEquals(3, new GenePair(1, 3).dominant());
        assertEquals(1, new GenePair(1, 3).recessive());
    }

    @Test
    void eachAlleleHasAnIndependentMaximumOfTen() {
        assertEquals(10, new GenePair(10, 10).dominant());
        assertEquals(10, new GenePair(10, 10).recessive());
        assertThrows(IllegalArgumentException.class, () -> new GenePair(11, 1));
    }

    @Test
    void vanillaBaselineAndNewGeneticSeedShareTheSameStartingFertility() {
        Genome ordinary = Genome.basic("minecraft:carrot_plant");
        Genome genetic = Genome.basic("minecraft:carrot_plant");
        assertEquals(ordinary.fertility(), genetic.fertility());
        assertEquals(1, ordinary.fertility().dominant());
        assertEquals(1, ordinary.fertility().recessive());
    }
}
