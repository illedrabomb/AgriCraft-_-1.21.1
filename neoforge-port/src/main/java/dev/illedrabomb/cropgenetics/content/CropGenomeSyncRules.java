package dev.illedrabomb.cropgenetics.content;

import dev.illedrabomb.cropgenetics.genetics.Genome;
import javax.annotation.Nullable;

/** Pure rules for the public visual subset of a planted crop's genome. */
final class CropGenomeSyncRules {
    private CropGenomeSyncRules() {}

    /** Client visuals need the species, not undiscovered Growth/Gain/Strength. */
    @Nullable
    static Genome forClient(@Nullable Genome actual, boolean analyzed) {
        if (actual == null || analyzed) return actual;
        return Genome.basic(actual.plantId());
    }
}
