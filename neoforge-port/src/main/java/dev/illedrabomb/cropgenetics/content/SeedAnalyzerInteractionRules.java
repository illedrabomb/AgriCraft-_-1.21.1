package dev.illedrabomb.cropgenetics.content;

/**
 * Client-safe direct-click prediction for the placed Seed Analyzer.
 *
 * Inventory slots can optimistically allow unknown datapack items, but the
 * block's right-click handler may only consume an interaction when the
 * client can identify a plausible seed without the server crop registry.
 */
final class SeedAnalyzerInteractionRules {
    private SeedAnalyzerInteractionRules() {}

    static boolean predictsSeed(String itemId, boolean ownGeneticSeed,
                                boolean hasGenome, boolean modifiedPlainItem) {
        if (ownGeneticSeed) return hasGenome;
        if (modifiedPlainItem || itemId == null) return false;
        return SeedSpeciesCatalog.DEFAULT_SPECIES.stream()
                .anyMatch(species -> itemId.equals(
                        SeedSpeciesCatalog.vanillaSeedForSpecies(species)));
    }
}
