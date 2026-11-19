package dev.illedrabomb.cropgenetics.data;

import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.Map;

/**
 * Minimal opt-in extension point for separately packaged crop add-ons.
 *
 * The host owns generic datapack parsing, claim collection, validation and
 * persistence. Add-ons own external mod semantics and bundled data resources.
 * They must never override an explicit species, seed or produce claim.
 *
 * Add-on entrypoints register an implementation during mod construction.
 * The host invokes it during subsequent server datapack reloads.
 */
public interface CropDiscoveryAddon {
    /** Unique add-on mod ID, used for deterministic ordering and diagnostics. */
    String addonId();

    /** Mod ID whose presence is required before this adapter may run. */
    String requiredModId();

    /** Add only safe, missing definitions and append diagnostics. */
    void discover(ResourceManager resources, Map<String, PlantDefinition> plants,
                  ExplicitPlantClaims claims,
                  List<CropReloadReport.Entry> diagnostics);
}
