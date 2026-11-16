package dev.illedrabomb.cropgenetics.genetics;

/**
 * Immutable pair of alleles. The numerically stronger allele is dominant,
 * matching the original AgriCraft GeneStat behavior.
 */
public record GenePair(int dominant, int recessive) {
    public static final int MIN = 1;
    public static final int MAX = 10;

    public GenePair {
        if (dominant < MIN || dominant > MAX || recessive < MIN || recessive > MAX) {
            throw new IllegalArgumentException("Gene alleles must be between 1 and 10");
        }
        if (dominant < recessive) {
            int swap = dominant;
            dominant = recessive;
            recessive = swap;
        }
    }

    public int expressed(TraitMode mode) {
        return switch (mode) {
            case MIN -> recessive;
            case MEAN -> (dominant + recessive) / 2;
            case MAX -> dominant;
        };
    }

    public enum TraitMode { MIN, MEAN, MAX }
}
