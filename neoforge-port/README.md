# Crop Genetics — NeoForge 1.21.1 implementation

**Development branch:** `feature/neoforge-1211-foundation`
**Target:** Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21.
**Temporary working title and mod ID:** Crop Genetics / `cropgenetics`; rename may be decided later.

This is the NEW source root and is intentionally separate from the old Forge 1.18.2 project in the repository root. The original project remains available as a reference. Do not mistake this initial foundation for a fully functioning farming mod.

## Current implemented scope
- NeoForge ModDevGradle build configuration and `@Mod` entrypoint.
- Immutable `GenePair`, `Genome`, and `BreedingEngine` production-domain code.
- Main visible traits Growth/Gain/Strength, with Fertility and Mutativity retained internally.
- Dominance, configurable display expression MIN/MEAN/MAX, inherited alleles and integer stat mutation rules.

## Not yet implemented
Data-component registration and persistence, seeds, Crop Sticks, growing crops, special species mutations, village buildings, modpack adapters, JSON importer, automated tests, integration tests and runtime validation.

## Building
Use a suitable Java 21 / Gradle installation to run `gradle build` from this directory. A matching Gradle wrapper has not yet been bundled here, and **this build has not been run or verified**. Establish a wrapper and CI before calling the port executable.

## Origin and attribution
This work is developed from the [AgriCraft](https://github.com/AgriCraft/AgriCraft) fork with its MIT license and contributors. Preserve original copyright/license information when moving code or assets. See `../docs/NEOFORGE_1.21.1_PLAN.md` and `../temporary-research/`.
