package dev.illedrabomb.cropgenetics.content;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** No client window, datapack registry or world instance is needed. */
class SeedAnalyzerInteractionRulesTest {
    @Test
    void rightClickWithUnrelatedItemOpensMenuInsteadOfPredictingInsertion() {
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:diamond", false, false, false));
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:stick", false, false, false));
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "cropgenetics:seed_journal", false, false, false));
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                null, false, false, false));
    }

    @Test
    void plainVanillaSeedsAreRecognizedButCustomizedItemsAreNot() {
        assertTrue(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:wheat_seeds", false, false, false));
        assertTrue(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:carrot", false, false, false));
        assertTrue(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:sweet_berries", false, false, false));
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "minecraft:wheat_seeds", false, false, true));
    }

    @Test
    void geneticSeedsRequireAnAttachedGenomeBeforeDirectInsertion() {
        assertTrue(SeedAnalyzerInteractionRules.predictsSeed(
                "cropgenetics:genetic_seed", true, true, true));
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "cropgenetics:genetic_seed", true, false, false));
    }

    @Test
    void unknownDatapackSeedsRemainServerAuthoritative() {
        assertFalse(SeedAnalyzerInteractionRules.predictsSeed(
                "othermod:custom_seed", false, false, false));
    }
}
