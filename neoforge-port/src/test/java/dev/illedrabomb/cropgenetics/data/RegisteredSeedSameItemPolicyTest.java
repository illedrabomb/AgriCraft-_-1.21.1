package dev.illedrabomb.cropgenetics.data;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RegisteredSeedSameItemPolicyTest {
    private static RegisteredSeedCropDiscovery.Policy policy(
            Map<String, RegisteredSeedCropDiscovery.ItemPair> pairs) {
        return new RegisteredSeedCropDiscovery.Policy("pamhc2crops",
                "seeditem", "item", "_plant", "auto_pam/", 16, 9,
                .75D, .025D, 5, Set.of(), pairs);
    }

    @Test
    void explicitlyReviewedSelfPlantableProduceIsAllowed() {
        var pair = new RegisteredSeedCropDiscovery.ItemPair(
                "pamhc2crops:flaxitem", "pamhc2crops:flaxitem");
        assertEquals(pair, policy(Map.of("flax", pair)).itemPairs().get("flax"));
    }

    @Test
    void otherCropCannotReuseSameSeedOrProduce() {
        var first = new RegisteredSeedCropDiscovery.ItemPair(
                "pamhc2crops:flaxitem", "pamhc2crops:flaxitem");
        var second = new RegisteredSeedCropDiscovery.ItemPair(
                "pamhc2crops:otherseeditem", "pamhc2crops:flaxitem");
        assertThrows(IllegalArgumentException.class,
                () -> policy(Map.of("flax", first, "other", second)));
    }

    @Test
    void inferredNamesRemainSeparateFromExplicitFoodPairs() {
        var valid = policy(Map.of("flax",
                new RegisteredSeedCropDiscovery.ItemPair(
                        "pamhc2crops:flaxitem", "pamhc2crops:flaxitem")));
        assertTrue(RegisteredSeedCropDiscovery.inferredCropName(
                net.minecraft.resources.ResourceLocation.parse("pamhc2crops:flaxitem"),
                valid).isEmpty());
        assertEquals("flax", RegisteredSeedCropDiscovery.inferredCropName(
                net.minecraft.resources.ResourceLocation.parse("pamhc2crops:flaxseeditem"),
                valid).orElseThrow());
    }
}
