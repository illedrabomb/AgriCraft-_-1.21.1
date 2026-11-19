package dev.illedrabomb.cropgenetics.data;

import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CropDiscoveryAddonRegistryTest {
    private static CropDiscoveryAddon addon(String id, String required) {
        return new CropDiscoveryAddon() {
            @Override public String addonId() { return id; }
            @Override public String requiredModId() { return required; }
            @Override public void discover(
                    ResourceManager resources, Map<String, PlantDefinition> plants,
                    ExplicitPlantClaims claims, List<CropReloadReport.Entry> diagnostics) {
                // Registration and dependency filtering need no live world.
            }
        };
    }

    @Test
    void onlyLoadedTargetModsExposeTheirDiscoveryAdapters() {
        CropDiscoveryAddonRegistry.register(addon("regtest_apple", "fakecrops"));
        CropDiscoveryAddonRegistry.register(addon("regtest_banana", "anothercrops"));

        List<CropDiscoveryAddon> loaded =
                CropDiscoveryAddonRegistry.loaded(id -> id.equals("fakecrops"));
        assertTrue(loaded.stream().anyMatch(a -> a.addonId().equals("regtest_apple")));
        assertFalse(loaded.stream().anyMatch(a -> a.addonId().equals("regtest_banana")));
    }

    @Test
    void repeatedOrInvalidRegistrationFailsClosed() {
        CropDiscoveryAddonRegistry.register(addon("regtest_duplicate", "fakecrops"));
        assertThrows(IllegalArgumentException.class,
                () -> CropDiscoveryAddonRegistry.register(
                        addon("regtest_duplicate", "fakecrops")));
        assertThrows(IllegalArgumentException.class,
                () -> CropDiscoveryAddonRegistry.register(
                        addon("INVALID-ID", "fakecrops")));
    }
}
