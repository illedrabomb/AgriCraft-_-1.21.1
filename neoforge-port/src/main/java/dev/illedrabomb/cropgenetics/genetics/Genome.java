package dev.illedrabomb.cropgenetics.genetics;

/**
 * The classic AgriCraft-style three visible, heritable crop stats.
 * Every stat is one integer from 1 to 10. Legacy two-allele saves are
 * migrated by GenomeCodecs, preserving each previously expressed MAX value.
 */
public record Genome(String plantId, int growth, int gain, int strength) {
    public static final int MIN = 1;
    public static final int MAX = 10;

    public Genome {
        if (plantId == null || plantId.length() > 128
                || !plantId.matches("[a-z0-9_.-]+:[a-z0-9_./-]+")) {
            throw new IllegalArgumentException("Expected namespaced plant ID");
        }
        check("growth", growth);
        check("gain", gain);
        check("strength", strength);
    }

    private static void check(String name, int stat) {
        if (stat < MIN || stat > MAX)
            throw new IllegalArgumentException(name + " must be between 1 and 10");
    }

    public static Genome basic(String plantId) {
        return new Genome(plantId, 1, 1, 1);
    }

    public Genome withPlantId(String newPlantId) {
        return new Genome(newPlantId, growth, gain, strength);
    }

    public boolean isPerfectVisible() {
        return growth == MAX && gain == MAX && strength == MAX;
    }

    public String displayedStats() {
        return growth + "/" + gain + "/" + strength;
    }
}
