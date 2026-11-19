package dev.illedrabomb.cropgenetics.compat.jei;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OptionalSeedGuidePolicyTest {
    @Test
    void farmerDelightThreePilotsHaveOnlyReviewedGuides() {
        assertEquals("farmersdelight:cabbage",
                OptionalSeedGuidePolicy.produceForSeed("farmersdelight:cabbage_seeds"));
        assertEquals("farmersdelight:tomato",
                OptionalSeedGuidePolicy.produceForSeed("farmersdelight:tomato_seeds"));
        assertEquals("farmersdelight:onion",
                OptionalSeedGuidePolicy.produceForSeed("farmersdelight:onion"));
    }

    @Test
    void riceAndSpecialWaterCropsCannotAppearAsOrdinaryCrops() {
        assertNull(OptionalSeedGuidePolicy.produceForSeed("farmersdelight:rice"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("farmersdelight:rice_panicle"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("farmersdelight:rice_seeds"));
    }

    @Test
    void pamSeedsUseOnlyApprovedNamespaceAndSuffix() {
        assertEquals("pamhc2crops:mustarditem",
                OptionalSeedGuidePolicy.produceForSeed("pamhc2crops:mustardseeditem"));
        // In Pam's older published registry, the crop name itself can end
        // in 'seeds': mustardseedsseeditem -> mustardseedsitem.
        assertEquals("pamhc2crops:mustardseedsitem",
                OptionalSeedGuidePolicy.produceForSeed("pamhc2crops:mustardseedsseeditem"));
        assertEquals("pamhc2crops:sesameseedsitem",
                OptionalSeedGuidePolicy.produceForSeed("pamhc2crops:sesameseedsseeditem"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("pamhc2trees:mustardseeditem"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("pamhc2crops:mustardseeds"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("pamhc2crops:seeditem"));
    }

    @Test
    void noGenericForeignSeedInference() {
        assertNull(OptionalSeedGuidePolicy.produceForSeed("minecraft:wheat_seeds"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("othermod:tomato_seeds"));
        assertNull(OptionalSeedGuidePolicy.produceForSeed("farmersdelight:potato_seeds"));
    }
}
