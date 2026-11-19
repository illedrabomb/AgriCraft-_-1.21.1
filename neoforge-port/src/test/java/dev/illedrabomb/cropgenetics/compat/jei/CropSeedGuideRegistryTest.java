package dev.illedrabomb.cropgenetics.compat.jei;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CropSeedGuideRegistryTest {
    @Test
    void noThirdPartyItemsAreInferredWithoutAnAddonRegistration() {
        assertTrue(CropSeedGuideRegistry.availablePairs().stream()
                .noneMatch(pair -> pair.seedId().startsWith("unregistered_test_mod:")));
    }

    @Test
    void registeredAddonProvidesOnlyItsDeclaredCandidates() {
        CropSeedGuideRegistry.register("guide_test_alpha", () -> List.of(
                new CropSeedGuideRegistry.SeedPair(
                        "registered_test_mod:tomato_seeds",
                        "registered_test_mod:tomato")));
        assertTrue(CropSeedGuideRegistry.availablePairs().stream().anyMatch(pair ->
                pair.seedId().equals("registered_test_mod:tomato_seeds")
                        && pair.produceId().equals("registered_test_mod:tomato")));
    }

    @Test
    void duplicateAndInvalidProvidersAreRejected() {
        CropSeedGuideRegistry.register("guide_test_duplicate", List::of);
        assertThrows(IllegalArgumentException.class,
                () -> CropSeedGuideRegistry.register("guide_test_duplicate", List::of));
        assertThrows(IllegalArgumentException.class,
                () -> CropSeedGuideRegistry.register("Bad-Id", List::of));
    }

    @Test
    void brokenOptionalProviderDoesNotHideHealthyGuides() {
        CropSeedGuideRegistry.register("guide_test_broken", () -> {
            throw new IllegalStateException("Optional broken data");
        });
        assertDoesNotThrow(CropSeedGuideRegistry::availablePairs);
    }
}
