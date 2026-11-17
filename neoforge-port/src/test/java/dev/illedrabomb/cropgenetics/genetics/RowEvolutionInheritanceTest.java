package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Same-species, two-parent rows can evolve through the existing inheritance
 * engine; one-parent clones keep the exact original genome.
 */
class RowEvolutionInheritanceTest {
    private static final class ImprovementRolls extends Random {
        @Override public boolean nextBoolean() { return true; }
        @Override public int nextInt(int bound) { return 0; }
    }

    @Test
    void twoSameSpeciesParentsCanImproveAllAlleles() {
        Genome baseline = Genome.basic("minecraft:wheat_plant");
        Genome child = BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new ImprovementRolls());

        assertEquals(baseline.plantId(), child.plantId());
        GenePair improved = new GenePair(2, 2);
        assertEquals(improved, child.growth());
        assertEquals(improved, child.gain());
        assertEquals(improved, child.strength());
        assertEquals(improved, child.fertility());
        assertEquals(improved, child.mutativity());
        // Neither parent genome is modified by inheritance.
        assertEquals(new GenePair(1, 1), baseline.growth());
    }

    @Test
    void mutationNeverExceedsMaximumAllele() {
        GenePair maximum = new GenePair(10, 10);
        Genome parent = new Genome("minecraft:wheat_plant",
                maximum, maximum, maximum, maximum, maximum);
        Genome child = BreedingEngine.combine(parent, parent,
                parent.plantId(), new ImprovementRolls());
        assertEquals(parent, child);
    }
}
