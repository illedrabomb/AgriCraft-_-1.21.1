# Independent crop add-on module split — handoff (2026-11-19)

**Commit:** `d3ebfabf8e4777b62d54373aa8aecbe1ec1f83de` on `integration/neoforge-1211-consolidation`.

**Status: source/resource packaging split committed. Neither Gradle compilation nor Minecraft/JUnit tests have been run. Do not call this verified until the user requests the combined test.**

## User priorities (latest)

1. Complete, stabilize and test **AgriCraft / Crop Genetics core**.
2. **Pam's HarvestCraft 2 add-on**, minimally higher priority than MineColonies.
3. **MineColonies add-on**, also important.
4. **Farmer's Delight add-on**, very low priority; no native rice work now.

## What changed

- Added Gradle subprojects `:pam-hc2` and `:farmers-delight` under `neoforge-port/addons/`. Each has its own `build.gradle`, `@Mod` entrypoint, `neoforge.mods.toml` template and intended independent JAR.
- Added required dependency declarations: `cropgenetics` + `pamhc2crops` for Pam and `cropgenetics` + `farmersdelight` for Farmer. Absence of either add-on must not prevent the core from loading.
- Relocated **81 existing repository files** in one atomic Git commit (Java adapters, adapter tests, Pam plant and mutation JSON, policy JSON, Farmer rich-soil JSON). Original resource IDs and bytes preserved. Java packages were **changed to unique addon packages** to avoid JPMS split-package collisions.
- Core `CropDataReloadListener` no longer parses Pam settings nor directly calls `PamCropAutoDiscovery` or `FarmersDelightCropDiscovery`. It invokes independently registered `CropDiscoveryAddon` callbacks, preserving the generic datapack path.
- Pam adapter parses the same highest-priority `cropgenetics:cropgenetics/pam_auto_discovery.json` resource when installed, with invalid configuration failing closed and reported. No unverified Pam foods enabled by default.
- Farmer adapter keeps the three approved generic pairs cabbage/tomato/onion. Rice remains excluded.
- `RegisteredSeedCropDiscovery.inferredCropName` is now public because cross-module addon tests need the shared naming helper.

## Confirmed static Git tree inspection

- No `pamhc2_*.json`, `pam_auto_discovery.json`, Pam/Farmer adapter Java, or Farmer rich-soil files remain under **core** `neoforge-port/src/main/`.
- Both addon source trees, test trees, metadata and build scripts exist in the committed tree.
- The core's generic `PlantDefinition`, `AgriPlantsRegistry`, JSON parser, diagnostics, common discovery engine and addon extension point remain in the core.
- The main build must be invoked explicitly with `gradlew :test :build` when the user authorizes tests; invoking unqualified `gradlew test build` in a multi-project Gradle build would select **all** subprojects.

## Remaining important audit items (do not hide)

- **No actual Gradle/NeoForge compilation yet.** Subproject `compileOnly project(':')` integration, generated mod metadata and runtime module loading must be verified in the agreed final test round. Do not claim JARs are working until then.
- Several **display-only** compatibility traces remain in the core: `CropReloadReport`'s existing adapter-specific counters, and the advisory `SpecialCropCompatibilityWarnings` rule for explicitly data-packed Farmer rice. The core JEI class now obtains optional seed guide entries **only from installed add-on providers**; its former hardcoded Pam/Farmer policy has been removed. These introduce no external Java dependency, but further UI/diagnostics separation can move them into add-ons later. Core auto-discovery code and bundled Pam/Farmer datapack resources have been removed.
- Addon registration is executed on each add-on's `@Mod` construction; required target mod metadata governs whether the add-on can load. Missing add-ons leave the registry empty.
- Farmer's Delight and Pam's resources should only become active from their own JARs. Datapack path precedence and partial migration of old worlds require functional checks before distribution.
- Verify the built JAR contents (core has zero Pam/Farmer resource definitions, each addon contains its own) before considering release.

## Deferred validation checklist

1. From `neoforge-port`, core-only Gradle: `./gradlew :test :build --no-daemon --console=plain` (PowerShell `& .\gradlew.bat :test :build --no-daemon --console=plain`). **Do not execute yet.**
2. Build add-ons individually only after user approves: `./gradlew :pam-hc2:build :farmers-delight:build --no-daemon --console=plain`.
3. Check main-only mod launch with NeoForge/Minecraft, no Pam/Farmer installed; no automatic third-party crops/NoClassDefFoundError.
4. Check Pam+core+Pam-addon with exact registered item mappings, explicit overrides, self-plantable configured pairs and archived JSON.
5. Check Farmer+core+Farmer-addon with 3 approved generic candidates; rice remains excluded.
6. Inspect JAR contents, addon dependency metadata, optional JEI UX and diagnostics, localization, and server/client parity.

The current plan lives in `docs/ADDON_EXTRACTION_PLAN_2026-11-19.md`. Keep all checkpoints in `docs/INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md` if conversation context compacts.


## Follow-up: JEI guidance owned by installed add-ons

- `86f50d10`: `CropSeedGuideRegistry` is a dependency-free core extension point for client-side JEI seed/product candidate pairs.
- `cf454d4f`: core JEI removed its implicit Pam/Farmer candidate scan. The help page now accepts only pairs explicitly registered by installed addons; the core still validates that registered items are present.
- `dd72644b` / `c82d0c43`: Pam and Farmer addon entrypoints register their own candidates. Pam keeps its bounded reviewed `seeditem` → `item` naming convention, Farmer only cabbage/tomato/onion. Native Farmer rice remains excluded.
- `3085f637`: generic JEI addon registration tests added; old hard-coded `OptionalSeedGuidePolicy` and its mixed test removed after equivalent generic + addon-policy coverage. Tests still **not run**.
- No-mod / source-mod-without-addon scenarios now must not display addon-only planting support. Confirm this in deferred game test; the build remains unverified.


## Static follow-up correction

- `0c141777`: the Gradle settings file was rechecked and its original `pluginManagement` / Foojay plugin / `rootProject.name` header restored alongside the new addon `include` declarations. This was caught and fixed by read-only Git inspection before any user-run build. **Compilation remains unverified.**


## Follow-up: addon-owned admin diagnostics

- `af5a67ad`: introduced generic `CropDataCommandExtensions` in the core, with deterministic extension registration and duplicate command rejection.
- `0c5392c7` / `d9bb1d39`: moved the former Pam and Farmer admin summary bodies into `PamDataCommands` and `FarmersDelightDataCommands` classes **inside their respective modules**.
- `32626726` / `d47f6bee`: the respective addon `@Mod` constructors register the original `pam` and `farmersdelight` subcommands. They no longer appear when only the core is loaded.
- `a684c6d9`: core `CropDataCommand` now contains only generic commands, appends contributed addon subcommands and reports a generic AUTO_DISCOVERED count. The original addon command names/output remain available when installed.
- `1b84ddec`: unit tests for contribution, duplicate detection and invalid command identifiers. **Not executed.**
- Static source inspection confirms the core `CropDataCommand` no longer contains Pam/Farmer literals; addon command classes and registration calls exist.
- Core still contains name fallback rules, addon-specific immutable report aggregates and the protective Farmer rice warning; these **do not directly activate any addon feature**. Assess whether to generalize them later without regressing explicit user datapacks.


## Follow-up: addon-owned crop display names

- `c02122a3`: generic, synchronized `CropSpeciesDisplayRegistry` in core; addon-provided species → produce item mapping, with deterministic conflict rejection and error containment.
- `859ed3ee`: `CropSpeciesNames.preferredProduceItem` no longer contains hard-coded Pam/Farmer namespace rules. Names for vanilla species remain covered by their existing translation switch.
- `7f6d7ae1` / `2d619211`: Pam and Farmer display-name policies moved into their own addon packages. Pam retains plural names like `mustardseeds`; Farmer allows only cabbage/tomato/onion, not rice.
- `e6b073ed` / `33a27dc9`: the owning addons register those display mappings on mod initialization; the core does not synthesize an optional crop name when their addon is absent.
- `86c569bd`, `919e76e5`, `9a78a8a9`: core-only and addon-specific regression tests added/updated. **No tests run**.
- Remaining optional-specific helpers in core are generic read-only report aggregation (used by add-on diagnostics) and the explicit Farmer rice compatibility warning for user-authored generic datapacks. Both can be generalized later; neither auto-registers external plants.


## Follow-up: generic report statistics, zero Pam/Farmer paths in core report

- **`55674380` (atomic commit, 5 files):** core `CropReloadReport` now provides only a general `AutoDiscoveryStats` record and `autoDiscoveryStats(prefix, configPath)` parser. It no longer contains the old `PamDiscoveryStats`, `farmersDelightStats` or specific `auto_pam` / `auto_farmers_delight` checks.
- Pam addon `PamDataCommands` supplies `auto_pam/` and config path, and computes the legacy "all mods explicit loaded" count from the immutable report entries. The previous player-visible command labels and counts remain conceptually intact.
- Farmer addon `FarmersDelightDataCommands` supplies `auto_farmers_delight/` for generic counting, including suspension sentinel.
- Refactored core `CropOptionalDiscoveryStatsTest` to use an unrelated synthetic `auto_example/` prefix, and adjusted Farmer addon diagnostics tests. Explicit override DISABLED remains informational; config errors and suspension markers are separately tracked.
- Static Git source inspection confirms the five edited files no longer reference the removed addon-specific report APIs. **No Gradle, JUnit or Minecraft test was executed.**
- The remaining **intentional** core-specific compatibility check is `SpecialCropCompatibilityWarnings` for Farmer native rice: it warns users who explicitly force a generic rice datapack that native water/panicle/knife parity is not implemented. Removing that without a generic replacement would suppress a useful warning; revisit only when a safe data-driven warning mechanism is ready.
