package dev.illedrabomb.cropgenetics.data;

import java.util.Set;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RegisteredSeedClaimDiagnosticsTest {
    private static final String PLANT = "pamhc2crops:mustard_plant";
    private static final String SEED = "pamhc2crops:mustardseeditem";
    private static final String PRODUCE = "pamhc2crops:mustarditem";

    private static String reason(Set<String> explicitSpecies, Set<String> activeSpecies,
                                 Set<String> seeds, Set<String> produce) {
        return RegisteredSeedCropDiscovery.reservedClaimReason(
                PLANT, SEED, PRODUCE, explicitSpecies, activeSpecies, seeds, produce);
    }

    @Test
    void unclaimedPairHasNoSkipReason() {
        assertNull(reason(Set.of(), Set.of(), Set.of(), Set.of()));
    }

    @Test
    void explicitSpeciesWinsEvenIfItemsAreFree() {
        assertTrue(reason(Set.of(PLANT), Set.of(), Set.of(), Set.of())
                .contains("species already defined"));
    }

    @Test
    void activeSpeciesWinsAfterOtherAutoDiscovery() {
        assertTrue(reason(Set.of(), Set.of(PLANT), Set.of(), Set.of())
                .contains("species already defined"));
    }

    @Test
    void seedAndProduceClaimsAreReportedSeparately() {
        assertTrue(reason(Set.of(), Set.of(), Set.of(SEED), Set.of())
                .contains("seed item already claimed"));
        assertTrue(reason(Set.of(), Set.of(), Set.of(), Set.of(PRODUCE))
                .contains("produce item already claimed"));
    }

    @Test
    void crossRoleClaimsAlsoPreventUnsafeDiscovery() {
        assertTrue(reason(Set.of(), Set.of(), Set.of(), Set.of(SEED))
                .contains("used as produce"));
        assertTrue(reason(Set.of(), Set.of(), Set.of(PRODUCE), Set.of())
                .contains("used as seed"));
    }
}
