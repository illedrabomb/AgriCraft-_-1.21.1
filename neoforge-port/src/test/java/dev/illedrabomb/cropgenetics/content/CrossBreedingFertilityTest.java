package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Cross fertility is now a gameplay rule, not an extra inherited trait. */
class CrossBreedingFertilityTest {
    @Test
    void moreEligibleParentsIncreaseCrossingAndMutationChances() {
        assertEquals(1.0D, CrossFertilityRules.parentCountMultiplier(2));
        assertEquals(1.25D, CrossFertilityRules.parentCountMultiplier(3));
        assertEquals(1.5D, CrossFertilityRules.parentCountMultiplier(4));
        assertEquals(1, CrossFertilityRules.mutationMultiplier(2));
        assertEquals(2, CrossFertilityRules.mutationMultiplier(3));
        assertEquals(3, CrossFertilityRules.mutationMultiplier(4));
    }

    @Test
    void uninterruptedRowHasLowerOddsThanAllAdjacentCrosses() {
        assertTrue(CrossFertilityRules.rowFertilityMultiplier()
                < CrossFertilityRules.parentCountMultiplier(2));
        assertTrue(CrossFertilityRules.rowMutationMultiplier()
                < CrossFertilityRules.mutationMultiplier(2));
        assertEquals(0.15D, 0.30D * CrossFertilityRules.rowFertilityMultiplier(), 1.0E-10);
    }

    @Test
    void allThreeStatGenomesUseOnePredictableFertilityRoll() {
        Genome basic = Genome.basic("minecraft:wheat_plant");
        Genome improved = new Genome("minecraft:wheat_plant", 10, 10, 10);
        assertEquals(0.30D, CrossFertilityRules.fertilityChance(basic), 1.0E-10);
        assertEquals(0.30D, CrossFertilityRules.pairFertilityChance(basic, basic), 1.0E-10);
        assertEquals(0.30D, CrossFertilityRules.pairFertilityChance(basic, improved), 1.0E-10);
    }
}
