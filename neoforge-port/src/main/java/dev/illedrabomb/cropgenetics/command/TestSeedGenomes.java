package dev.illedrabomb.cropgenetics.command;

import dev.illedrabomb.cropgenetics.genetics.Genome;

/** Explicitly distinguishes the legacy Gain shortcut from the three-stat form. */
final class TestSeedGenomes {
    private TestSeedGenomes() {}

    static Genome gainOnly(String speciesId, int gain) {
        return full(speciesId, 1, gain, 1);
    }

    static Genome full(String speciesId, int growth, int gain, int strength) {
        return new Genome(speciesId, growth, gain, strength);
    }
}
