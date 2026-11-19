package dev.illedrabomb.cropgenetics.command;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CropDataCommandExtensionsTest {
    @Test
    void addonCommandsAreOnlyRegisteredByExplicitAddonEntrypoints() {
        assertTrue(CropDataCommandExtensions.registered().stream()
                .noneMatch(ext -> ext.name().equals("unregistered_addon_test")));
        CropDataCommandExtensions.register("registered_addon_test", (source, registry) -> 1);
        assertTrue(CropDataCommandExtensions.registered().stream()
                .anyMatch(ext -> ext.name().equals("registered_addon_test")));
    }

    @Test
    void duplicateOrInvalidAddonCommandCannotReplaceExistingHandler() {
        CropDataCommandExtensions.register("command_duplicate_test", (source, registry) -> 1);
        assertThrows(IllegalArgumentException.class,
                () -> CropDataCommandExtensions.register(
                        "command_duplicate_test", (source, registry) -> 2));
        assertThrows(IllegalArgumentException.class,
                () -> CropDataCommandExtensions.register(
                        "Bad-Name", (source, registry) -> 2));
    }
}
