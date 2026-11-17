package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.GenePair;
import dev.illedrabomb.cropgenetics.genetics.Genome;

/** Pure genetic probability rules; no Minecraft world classes required. */
final class CrossFertilityRules {
    private CrossFertilityRules() {}

    static double fertilityChance(Genome genome) {
        return genome.fertility().expressed(GenePair.TraitMode.MAX) / 10.0D;
    }

    /** Average expressed fertility (1..10), one roll per mature parent pair. */
    static double pairFertilityChance(Genome first, Genome second) {
        return (fertilityChance(first) + fertilityChance(second)) / 2.0D;
    }
}
