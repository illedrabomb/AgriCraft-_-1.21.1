package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GeneticDisclosureTest {
    @Test
    void unanalysedSeedOrPlantedCropNeverDisclosesGeneValues() {
        Genome genome = Genome.basic("minecraft:sweet_berry_plant");
        assertFalse(GeneticDisclosure.mayShowGenes(genome, false));
        assertFalse(GeneticDisclosure.mayShowGenes(null, true));
        assertFalse(GeneticDisclosure.mayShowGenes(null, false));
    }

    @Test
    void onlyAnalysedPresentGenomeMayBeShown() {
        Genome genome = Genome.basic("minecraft:melon_plant");
        assertTrue(GeneticDisclosure.mayShowGenes(genome, true));
        assertFalse(GeneticDisclosure.mayShowGenes(genome, false));
    }
}
