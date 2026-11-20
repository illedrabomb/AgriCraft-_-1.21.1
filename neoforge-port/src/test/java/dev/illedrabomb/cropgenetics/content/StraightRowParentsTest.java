package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Deterministic row boundaries: no Minecraft server or world required. */
class StraightRowParentsTest {
    private static Genome genome(int growth) {
        return new Genome("minecraft:wheat", growth, 1, 1);
    }

    @Test
    void uninterruptedTwentyParentRowUsesOnlyFirstFourAsGeneticTemplates() {
        Genome first = genome(1);
        AtomicInteger furthestProbe = new AtomicInteger();
        StraightRowParents.Selection result = StraightRowParents.scan(first, distance -> {
            furthestProbe.set(distance);
            return genome(Math.min(distance, 10));
        });

        assertEquals(20, result.length());
        assertEquals(20, furthestProbe.get());
        assertEquals(List.of(genome(1), genome(2), genome(3), genome(4)),
                result.geneticParents());
        assertEquals(4, result.geneticParents().size());
        assertEquals(1D, CrossFertilityRules.rowFertilityMultiplier(result.length()), 1E-10);
        assertFalse(result.geneticParents().contains(genome(10)));
    }

    @Test
    void gapOrUnloadedChunkEndsRowWithoutLookingPastIt() {
        AtomicInteger furthestProbe = new AtomicInteger();
        StraightRowParents.Selection result = StraightRowParents.scan(genome(1), distance -> {
            furthestProbe.set(distance);
            return distance == 5 ? null : genome(distance);
        });

        assertEquals(4, result.length());
        assertEquals(5, furthestProbe.get());
        assertEquals(List.of(genome(1), genome(2), genome(3), genome(4)),
                result.geneticParents());
    }

    @Test
    void secondPositionMissingDoesNotMakeAValidRow() {
        AtomicInteger lookups = new AtomicInteger();
        StraightRowParents.Selection result = StraightRowParents.scan(genome(1), distance -> {
            lookups.incrementAndGet();
            return null;
        });

        assertEquals(1, result.length());
        assertEquals(List.of(genome(1)), result.geneticParents());
        assertEquals(1, lookups.get());
        assertTrue(result.length() < 3);
    }

    @Test
    void onlyThreeContiguousParentsAlreadyQualifyAsRow() {
        StraightRowParents.Selection result = StraightRowParents.scan(genome(1),
                distance -> distance <= 3 ? genome(distance) : null);
        assertEquals(3, result.length());
        assertEquals(3, result.geneticParents().size());
    }
}
