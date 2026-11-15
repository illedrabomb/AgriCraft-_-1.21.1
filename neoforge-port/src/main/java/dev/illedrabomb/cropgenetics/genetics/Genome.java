package dev.illedrabomb.cropgenetics.genetics;

import java.util.Objects;

/**
 * Production domain model for a genetic seed. The three display attributes
 * follow the original 10/10/10 concept (growth/gain/strength).
 *
 * Fertility and mutativity remain independent inherited traits, since the
 * original AgriCraft breeding algorithm uses both. Weed resistance is
 * deliberately not included in the core and may be added by an addon.
 */
public record Genome(
        String plantId,
        GenePair growth,
        GenePair gain,
        GenePair strength,
        GenePair fertility,
        GenePair mutativity
) {
    public Genome {
        if (plantId == null || plantId.length() > 128
                || !plantId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Expected namespaced plant ID");
        }
        Objects.requireNonNull(growth);
        Objects.requireNonNull(gain);
        Objects.requireNonNull(strength);
        Objects.requireNonNull(fertility);
        Objects.requireNonNull(mutativity);
    }

    public static Genome basic(String plantId) {
        GenePair initial = new GenePair(1, 1);
        return new Genome(plantId, initial, initial, initial, initial, initial);
    }

    public boolean isPerfectVisible(GenePair.TraitMode mode) {
        return growth.expressed(mode) == 10
                && gain.expressed(mode) == 10
                && strength.expressed(mode) == 10;
    }

    public String displayedStats(GenePair.TraitMode mode) {
        return growth.expressed(mode) + "/" + gain.expressed(mode) + "/" + strength.expressed(mode);
    }
}
