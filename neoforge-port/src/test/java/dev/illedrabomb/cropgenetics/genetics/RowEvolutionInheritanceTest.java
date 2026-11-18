package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;
import java.util.Random;
import static org.junit.jupiter.api.Assertions.*;

/** Row breeding may improve the three visible traits, never hidden genes. */
class RowEvolutionInheritanceTest {
    private static final class ImprovementRolls extends Random {
        @Override public boolean nextBoolean() { return true; }
        @Override public int nextInt(int bound) { return 0; }
        @Override public double nextDouble() { return 0.0D; }
    }

    @Test
    void twoSameSpeciesParentsCanImproveAllThreeStats() {
        Genome baseline = Genome.basic("minecraft:wheat_plant");
        Genome child = BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new ImprovementRolls());
        assertEquals(new Genome(baseline.plantId(), 2, 2, 2), child);
        assertEquals(Genome.basic(baseline.plantId()), baseline);
    }

    @Test
    void mutationNeverExceedsMaximum() {
        Genome parent = new Genome("minecraft:wheat_plant", 10, 10, 10);
        Genome child = BreedingEngine.combine(parent, parent,
                parent.plantId(), new ImprovementRolls());
        assertEquals(parent, child);
    }

    @Test
    void disablingRowMutationStillInheritsButDoesNotImprove() {
        Genome baseline = Genome.basic("minecraft:wheat_plant");
        Genome child = BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new ImprovementRolls(), 0.0D);
        assertEquals(baseline, child);
    }

    @Test
    void configurableRowMutationChanceCanBeIncreased() {
        Genome baseline = Genome.basic("minecraft:wheat_plant");
        Genome child = BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new ImprovementRolls(), 5.0D);
        assertEquals(new Genome(baseline.plantId(), 2, 2, 2), child);
    }
}
