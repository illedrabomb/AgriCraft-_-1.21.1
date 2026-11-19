package dev.illedrabomb.cropgenetics.addon.pam;

import dev.illedrabomb.cropgenetics.data.*;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.Map;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PamAutoDiscoverySettingsTest {
    @Test void rejectsTreesAndForeignNamespaces() {
        assertThrows(IllegalArgumentException.class, () -> new PamAutoDiscoverySettings.ItemPair(
                "pamhc2trees:apple_sapling", "pamhc2trees:appleitem"));
        assertThrows(IllegalArgumentException.class, () -> new PamAutoDiscoverySettings.ItemPair(
                "minecraft:wheat_seeds", "pamhc2crops:wheatitem"));
    }
    @Test void rejectsCrossRoleConflicts() {
        var pairs = Map.of("first", new PamAutoDiscoverySettings.ItemPair("pamhc2crops:a", "pamhc2crops:b"),
                "second", new PamAutoDiscoverySettings.ItemPair("pamhc2crops:b", "pamhc2crops:c"));
        assertThrows(IllegalArgumentException.class, () -> new PamAutoDiscoverySettings(
                true, 512, 9, .75, .025, 5, Set.of(), pairs));
    }
    @Test void acceptsExplicitSelfPlantableFoodButNotCrossCropRoleCollisions() {
        var selfPlantable = new PamAutoDiscoverySettings.ItemPair(
                "pamhc2crops:flaxitem", "pamhc2crops:flaxitem");
        var settings = new PamAutoDiscoverySettings(true, 512, 9, .75, .025,
                5, Set.of(), Map.of("flax", selfPlantable));
        assertEquals(selfPlantable, settings.itemPairs().get("flax"));

        var ambiguous = Map.of(
                "flax", selfPlantable,
                "different", new PamAutoDiscoverySettings.ItemPair(
                        "pamhc2crops:otherseeditem", "pamhc2crops:flaxitem"));
        assertThrows(IllegalArgumentException.class, () -> new PamAutoDiscoverySettings(
                true, 512, 9, .75, .025, 5, Set.of(), ambiguous));
    }

    @Test void rejectsUnknownConfigurationInsteadOfIgnoringTypos() {
        assertThrows(RuntimeException.class, () -> PamAutoDiscoverySettings.parse(
                JsonParser.parseString("{\"max_plant\":5}").getAsJsonObject()));
    }
    @Test void rejectsNonFiniteProbabilities() {
        assertThrows(IllegalArgumentException.class, () -> new PamAutoDiscoverySettings(
                true, 512, 9, Double.NaN, .025, 5, Set.of()));
    }
}
