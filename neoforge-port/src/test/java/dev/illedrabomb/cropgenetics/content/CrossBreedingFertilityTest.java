package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Cross fertility is now a gameplay rule, not an extra inherited trait. */
class CrossBreedingFertilityTest {
    @Test
    void allThreeStatGenomesUseOnePredictableFertilityRoll() {
        Genome basic = Genome.basic("minecraft:wheat_plant");
        Genome improved = new Genome("minecraft:wheat_plant", 10, 10, 10);
        assertEquals(0.30D, CrossFertilityRules.fertilityChance(basic), 1.0E-10);
        assertEquals(0.30D, CrossFertilityRules.pairFertilityChance(basic, basic), 1.0E-10);
        assertEquals(0.30D, CrossFertilityRules.pairFertilityChance(basic, improved), 1.0E-10);
    }
}
