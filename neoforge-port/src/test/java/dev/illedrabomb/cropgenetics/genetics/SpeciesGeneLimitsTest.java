package dev.illedrabomb.cropgenetics.genetics;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SpeciesGeneLimitsTest {
    private static Genome gene(String species, int growth, int gain, int strength) {
        return new Genome("minecraft:" + species, growth, gain, strength);
    }

    @Test
    void weakParentSpeciesCannotJumpPastFiveOrExploitRepeatedCrosses() {
        Genome carrot = gene("carrot", 10, 10, 10);
        Genome potato = gene("potato", 1, 1, 1);
        Genome inherited = gene("potato", 10, 10, 10);
        assertEquals(gene("potato", 5, 5, 5),
                SpeciesGeneLimits.apply(inherited, List.of(carrot, potato)));
        assertEquals(gene("potato", 5, 5, 5),
                SpeciesGeneLimits.apply(inherited,
                        List.of(carrot, gene("potato", 5, 5, 5))));
    }

    @Test
    void higherValuesAlreadyDevelopedInChildSpeciesRemainPossiblePerStat() {
        assertEquals(gene("potato", 7, 6, 8),
                SpeciesGeneLimits.apply(gene("potato", 10, 10, 10),
                        List.of(gene("carrot", 10, 10, 10), gene("potato", 7, 6, 8))));
        assertEquals(gene("potato", 9, 6, 8),
                SpeciesGeneLimits.apply(gene("potato", 10, 10, 10),
                        List.of(gene("carrot", 10, 10, 10), gene("potato", 7, 6, 8),
                                gene("potato", 9, 4, 3))));
    }

    @Test
    void thirdSpeciesCapsAtFourWithoutRaisingWeakGenes() {
        List<Genome> parents = List.of(gene("potato", 10, 10, 10), gene("carrot", 10, 10, 10));
        assertEquals(gene("pumpkin", 4, 4, 4),
                SpeciesGeneLimits.apply(gene("pumpkin", 10, 10, 10), parents));
        assertEquals(gene("pumpkin", 3, 4, 2),
                SpeciesGeneLimits.apply(gene("pumpkin", 3, 7, 2), parents));
    }

    @Test
    void newSpeciesAlsoCapsAtFourWhenEveryParentHasTheSameSpecies() {
        Genome perfectCarrot = gene("carrot", 10, 10, 10);
        List<Genome> twoSameParents = List.of(perfectCarrot, perfectCarrot);
        assertEquals(gene("pumpkin", 4, 4, 4),
                SpeciesGeneLimits.apply(gene("pumpkin", 10, 10, 10), twoSameParents));
        assertEquals(gene("pumpkin", 2, 4, 3),
                SpeciesGeneLimits.apply(gene("pumpkin", 2, 9, 3), twoSameParents));

        List<Genome> fourSameParents = List.of(perfectCarrot, perfectCarrot,
                perfectCarrot, perfectCarrot);
        assertEquals(gene("pumpkin", 4, 4, 4),
                SpeciesGeneLimits.apply(gene("pumpkin", 10, 10, 10), fourSameParents));
        // Same-species descendants keep their normal unrestricted evolution.
        assertEquals(perfectCarrot, SpeciesGeneLimits.apply(perfectCarrot, fourSameParents));
    }

    @Test
    void sameSpeciesBreedingAndExistingWeakChildrenAreUnchanged() {
        Genome perfect = gene("potato", 10, 10, 10);
        assertEquals(perfect, SpeciesGeneLimits.apply(perfect,
                List.of(gene("potato", 9, 9, 9), gene("potato", 9, 9, 9))));
        Genome weak = gene("potato", 2, 3, 1);
        assertEquals(weak, SpeciesGeneLimits.apply(weak,
                List.of(perfect, gene("carrot", 1, 1, 1))));
    }

    @Test
    void parentOrderAndFourParentExtensionCannotImportForeignSpeciesStrength() {
        Genome potato = gene("potato", 7, 3, 9);
        Genome carrot = gene("carrot", 10, 10, 10);
        Genome potatoTwo = gene("potato", 3, 8, 2);
        Genome beetroot = gene("beetroot", 10, 10, 10);
        Genome inherited = gene("potato", 10, 10, 10);
        Genome expected = gene("potato", 7, 8, 9);

        assertEquals(expected, SpeciesGeneLimits.apply(inherited,
                List.of(potato, carrot, potatoTwo, beetroot)));
        assertEquals(expected, SpeciesGeneLimits.apply(inherited,
                List.of(beetroot, potatoTwo, carrot, potato)));
        // Even four powerful foreign parents cannot lift a completely new species.
        assertEquals(gene("pumpkin", 4, 4, 4), SpeciesGeneLimits.apply(
                gene("pumpkin", 10, 10, 10), List.of(potato, carrot, potatoTwo, beetroot)));
    }

    @Test
    void weakChildrenAreNeverImprovedByCeilingsAcrossParentCounts() {
        Genome weak = gene("potato", 1, 2, 3);
        Genome potato = gene("potato", 10, 10, 10);
        Genome carrot = gene("carrot", 10, 10, 10);
        Genome beetroot = gene("beetroot", 10, 10, 10);
        assertEquals(weak, SpeciesGeneLimits.apply(weak, List.of(potato, carrot)));
        assertEquals(weak, SpeciesGeneLimits.apply(weak, List.of(potato, carrot, beetroot)));
        assertEquals(weak, SpeciesGeneLimits.apply(weak, List.of(potato, carrot, beetroot, carrot)));
        assertEquals(gene("pumpkin", 1, 2, 3), SpeciesGeneLimits.apply(
                gene("pumpkin", 1, 2, 3), List.of(potato, carrot)));
    }

    @Test
    void rejectsIncompleteOrOverfullParentSetsInsteadOfGuessingGenetics() {
        Genome child = gene("potato", 5, 5, 5);
        Genome parent = gene("carrot", 5, 5, 5);
        assertThrows(IllegalArgumentException.class,
                () -> SpeciesGeneLimits.apply(child, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> SpeciesGeneLimits.apply(child, List.of(parent)));
        assertThrows(IllegalArgumentException.class,
                () -> SpeciesGeneLimits.apply(child,
                        List.of(parent, parent, parent, parent, parent)));
        assertThrows(IllegalArgumentException.class,
                () -> SpeciesGeneLimits.apply(child, java.util.Arrays.asList(parent, null)));
    }
}
