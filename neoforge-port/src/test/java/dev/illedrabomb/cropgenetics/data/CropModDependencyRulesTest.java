package dev.illedrabomb.cropgenetics.data;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

/** Guard optional Pam's/Farmer's Delight definitions and imported crop packs. */
class CropModDependencyRulesTest {
    private static com.google.gson.JsonObject crop(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    @Test
    void unknownOrUninstalledOptionalModKeepsDefinitionDisabled() {
        var plant = crop("{\"mods\":[\"cropgenetics\",\"farmersdelight\"]}");
        assertEquals("farmersdelight", CropModDependencyRules.firstMissing(
                plant, Set.of("cropgenetics")::contains));
        assertNull(CropModDependencyRules.firstMissing(
                plant, Set.of("cropgenetics", "farmersdelight")::contains));
    }

    @Test
    void pamsCropsOnlyActivatesWhenInstalled() {
        var plant = crop("{\"mods\":[\"pamhc2crops\"]}");
        assertEquals("pamhc2crops",
                CropModDependencyRules.firstMissing(plant, unused -> false));
        assertNull(CropModDependencyRules.firstMissing(
                plant, "pamhc2crops"::equals));
    }

    @Test
    void legacyAgriCraftAliasDoesNotRequireOldForgeMod() {
        var legacy = crop("{\"mods\":[\"agricraft\"]}");
        assertNull(CropModDependencyRules.firstMissing(legacy, unused -> false));
        assertNull(CropModDependencyRules.firstMissing(crop("{}"), unused -> false));
    }

    @Test
    void malformedDependencyListFailsClosed() {
        var invalid = crop("{\"mods\":\"farmersdelight\"}");
        assertThrows(RuntimeException.class,
                () -> CropModDependencyRules.firstMissing(invalid, unused -> true));
    }
}
