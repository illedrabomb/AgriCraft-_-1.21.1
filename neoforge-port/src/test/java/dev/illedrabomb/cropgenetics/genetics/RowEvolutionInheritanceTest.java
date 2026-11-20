package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;
import java.util.Random;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Full-genome inheritance and point-pool mutation use only the three visible traits. */
class RowEvolutionInheritanceTest {
    private static final class ImprovementRolls extends Random {
        @Override public int nextInt(int bound) { return bound == 100 ? 99 : 0; }
        @Override public double nextDouble() { return 1.0D; }
    }

    @Test
    void twoSameSpeciesParentsCanImproveAllThreeStats() {
        Genome baseline = Genome.basic("minecraft:wheat_plant");
        Genome child = BreedingEngine.combine(baseline, baseline,
                baseline.plantId(), new ImprovementRolls());
        assertEquals(new Genome(baseline.plantId(), 4, 1, 1), child);
        assertEquals(Genome.basic(baseline.plantId()), baseline);
    }

    @Test
    void threeAndFourParentsCanContributeTheirVisibleStats() {
        Random selectLast = new Random() {
            @Override public int nextInt(int bound) { return bound - 1; }
            @Override public double nextDouble() { return 1.0D; }
        };
        Genome low = Genome.basic("minecraft:wheat_plant");
        Genome high = new Genome("minecraft:wheat_plant", 7, 8, 9);
        Genome three = BreedingEngine.combine(List.of(low, low, high),
                low.plantId(), selectLast, 0D);
        Genome four = BreedingEngine.combine(List.of(low, low, low, high),
                low.plantId(), selectLast, 0D);
        assertEquals(high, three);
        assertEquals(high, four);
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
        assertEquals(new Genome(baseline.plantId(), 4, 1, 1), child);
    }
}
