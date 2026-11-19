package dev.illedrabomb.cropgenetics.data;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Does not need the foreign mod jar or an initialized item registry. */
class RegisteredSeedCropDiscoveryNamingTest {
    private static RegisteredSeedCropDiscovery.Policy pamPolicy() {
        return new RegisteredSeedCropDiscovery.Policy(
                "pamhc2crops", "seeditem", "item", "_plant",
                "auto_pam/", 512, 9, .75D, .025D, 5, Set.of(), Map.of());
    }

    @Test
    void pamSeedSuffixExtractsTheWholeOriginalCropName() {
        var policy = pamPolicy();
        assertEquals("amaranth",
                RegisteredSeedCropDiscovery.inferredCropName(
                        ResourceLocation.parse("pamhc2crops:amaranthseeditem"),
                        policy).orElseThrow());
        // 'mustardseeds' is the real crop name, not 'mustard'. It has an
        // extra 'seeds' prefix before the expected final 'seeditem'.
        assertEquals("mustardseeds",
                RegisteredSeedCropDiscovery.inferredCropName(
                        ResourceLocation.parse("pamhc2crops:mustardseedsseeditem"),
                        policy).orElseThrow());
    }

    @Test
    void unapprovedNamespaceOrNonSeedItemsCannotAutoDiscoverPlants() {
        var policy = pamPolicy();
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2trees:appleseeditem"), policy).isEmpty());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:amaranthitem"), policy).isEmpty());
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                ResourceLocation.parse("pamhc2crops:seeditem"), policy).isEmpty());
    }
}
