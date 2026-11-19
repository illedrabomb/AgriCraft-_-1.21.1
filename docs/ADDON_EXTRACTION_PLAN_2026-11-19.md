# Add-on extraction and revised release priorities (2026-11-19)

Status: **approved architecture; migration in progress, NOT yet complete**. Do not describe the addons as separated until source, resources, Gradle outputs and main-only test isolation have been verified.

## User decisions (binding)

1. **Priority 1: AgriCraft / Crop Genetics main mod** — finish all vanilla/core features, fix core bugs and test **only the main mod** in the next coordinated gameplay round.
2. **Priority 2: Pam's HarvestCraft 2 add-on** — separate add-on; important and slightly ahead of MineColonies after the main mod. Preserve and continue the reviewed code/datapacks after extraction.
3. **Priority 3: MineColonies integration** — important separate add-on, directly after Pam's.
4. **Priority 4: Farmer's Delight add-on: very low** — preserve written adapter code/tests through extraction but defer all Farmer-specific feature work (especially native rice) to the distant future.
5. Retain generic, user-authored datapack plant/mutation loading in the main mod for any third-party mod. Do not auto-recognize other mods in the main mod.
6. One repository with independent Gradle modules/JAR files initially, no mandatory extra repositories.
7. Do not initiate Gradle/JUnit/Minecraft gameplay runs before the user requests the combined main-mod test. Static review is allowed.

## Target repository topology

```text
neoforge-port/
  settings.gradle
  build.gradle                    # core-only mod: cropgenetics
  src/main/...                    # vanilla/core, extensible datapacks, public addon API
  addons/
    pam-hc2/
      build.gradle                # separate mod id + jar; requires cropgenetics and pamhc2crops
      src/main/java/...           # PamCropAutoDiscovery, PamAutoDiscoverySettings, addon entrypoint
      src/main/resources/...      # Pam's plant/mutation JSON, settings, metadata
      src/test/...                # moved Pam-specific tests
    farmers-delight/
      build.gradle                # separate mod id + jar; requires cropgenetics and farmersdelight
      src/main/java/...           # FarmersDelightCropDiscovery and addon entrypoint
      src/main/resources/...      # separate FD-specific resources and metadata
      src/test/...                # moved Farmer-specific tests
    minecolonies/                 # planned only; do not activate yet
```

Gradle project naming/paths can vary if technically necessary; behavior and isolation requirements are authoritative.

## Mandatory core/addon boundary

- Main mod owns `PlantDefinition`, `MutationDefinition`, `AgriPlantsRegistry`, generic JSON format, registry validation, reload reporting and the shared reviewed `RegisteredSeedCropDiscovery` engine.
- Addons own **their own policy/adapter**, configuration, registrations and bundled datapack resources. In particular, the main code must no longer reference `PamCropAutoDiscovery`, `PamAutoDiscoverySettings`, or `FarmersDelightCropDiscovery` at compile time.
- Provide a small public addon registration interface with deterministic registration and reload order. Avoid scanning every loaded mod or using fragile reflection. The main mod must start and reload with zero addons.
- Preserve the fail-closed security invariants: explicit datapack species, seeds and produce win; missing optional dependencies do not crash the main mod; malformed explicit claims cannot be silently overwritten; adapter diagnostics remain attributable.
- Add-on resource paths may still use the core `cropgenetics` data namespace, because core reload loads all active datapacks; class/package layout and JAR ownership must be separated.
- Preserve the original Pam explicit plant/mutation definitions and discovery policy, including self-plantable reviewed same-item pairs, compatibility warnings, and all regression tests.
- Preserve Farmer's three reviewed candidates cabbage/tomato/onion. Rice requires separate native implementation and stays excluded.
- The optional Farmer's Delight rich-soil JSON profiles should be packaged in that add-on if their loading requires Farmer's Delight. Explicit core generic datapack support for third-party soils stays available.
- No changes to genetics, harvest stack limits, seed analyzer/chest, journal, crop sticks or species behavior during migration.

## Inventory to relocate

- Java: `data/PamCropAutoDiscovery.java`, `data/PamAutoDiscoverySettings.java`, `data/FarmersDelightCropDiscovery.java`.
- Core reload listener currently parses `cropgenetics/pam_auto_discovery.json` and calls both adapters directly. Replace with generic addon hookup; remove adapter-specific parse and diagnostics from the core listener.
- Main JAR currently embeds approximately 40+ `pamhc2_*.json` plant definitions, ~35+ mutation definitions, `pam_auto_discovery.json`, and two Farmer's rich-soil files. Verify exact inventory from Git, do not rely on approximate counts.
- Move Pam/Farmer-specific unit tests to corresponding add-on test sets; main test set should only depend on core classes and generic engine.
- Distinguish diagnostics API (shared generic record/report) from user-facing addon-specific commands/messages; do not accidentally break the main command's report formatter.

## Acceptance criteria before calling the extraction complete

1. No `pamhc2_*` or Farmer-specific bundled datapack resources remain in the **main** output JAR.
2. The main source and test classpaths have no compile-time dependency on `pamhc2crops`, `farmersdelight` or addon adapter classes.
3. **Main-only** package runs with just NeoForge + Minecraft and exhibits no Pam/Farmer auto discovery; user-authored generic datapacks still work when they reference installed mods.
4. Pam add-on with core + Pam retains the existing item-pair discovery and all bundled plant/mutation definitions, including explicit override protections.
5. Farmer add-on with core + Farmer retains cabbage, tomato, onion discovery. Rice stays excluded; no false native-feature parity.
6. Add-on absent: no missing-class crash, duplicate species, missing-resource warning cascade, or required dependency on a third-party mod in main metadata.
7. Build/test execution deferred per user request, but static source/resource checks should be documented as soon as possible.

## Safe implementation order

**Phase A:** extract the public registration interface; decouple core listener from specific adapters while keeping behavior consistent through temporary in-repo registration. Do not ship a release at this intermediate stage.

**Phase B:** add two independent Gradle addon modules and their metadata/dependencies; relocate adapter Java, policies, tests, optional resources and permissions; remove duplicate core resources atomically.

**Phase C:** audit source references and resource ownership; inspect main-only Gradle task graph without starting tests if possible; create a focused manual main-only test matrix and handoff.

**Phase D:** focus exclusively on missing core AgriCraft features/bugs. After core acceptance prioritize Pam's **slightly ahead** of MineColonies; both are important. Farmer's Delight enhancements are deferred to the distant future.

## Constraints and cautions

- `CropDataReloadListener` currently includes two large, separate explicit-claim walks and the Pam settings parser; naive file moves will fail compilation.
- `CropReloadReport` has Pam/FD summary helpers still referenced by diagnostics. Shared report must either keep backward-compatible generic summary views or move dedicated formatters into addons in the same coordinated change.
- Existing JSON resource paths are a compatibility contract for external datapack overrides; preserve IDs even when changing packaging.
- No claimed build success until a real build is run by the user; no claimed add-on completion until main-only dependency isolation is checked.


## Migration implementation checkpoint — 2026-11-19 (not finished)

- `3411bc07`: added shared `ExplicitPlantClaims` in the core. It inventories all explicit species/seed/produce claims, including disabled datapack definitions, and fails closed on malformed/unreadable plant files.
- `9b3103c0`: replaced two independent, divergent claim-parsing loops in `CropDataReloadListener` with the shared claim inventory. This is a **preparatory refactor**; Pam/Farmer direct calls still exist and behavior is intended to stay unchanged.
- `3e37c833`: added `ExplicitPlantClaimsTest` for disabled declarations, malformed identities, unreadable plant resources and mutation-only diagnostics. **Tests not run** per user instruction.
- Next concrete step: introduce a generic core-to-add-on registration interface, then move both adapter classes/JSON into independent JAR modules as an atomic compatibility-safe migration. **Do not run the main-only Minecraft test until this work is complete.**


## Migration implementation checkpoint — generic addon registration (2026-11-19)

- `dcca2cc0`: introduced public `CropDiscoveryAddon` contract for separately packaged mods: `addonId`, `requiredModId` and the `discover` callback with shared claim inventory and diagnostics.
- `684075e2`: added deterministic, synchronized `CropDiscoveryAddonRegistry` with duplicate-registration rejection and required-mod filtering. External addons can register on their own NeoForge mod construction; no target-mod classes are loaded by the core.
- `f64180f0`: `CropDataReloadListener` now invokes registered extensions, with fail-closed invalid claim handling and transaction-like staging. If an adapter throws or attempts to replace an active definition, its staged plant additions are discarded and an INVALID diagnostic is recorded.
- `618ab1c2`: regression tests for registration, target dependency filtering and duplicate/invalid addon IDs. **Not executed**.
- **Current limitation:** Existing Pam/Farmer built-in discoverers and JSON resources are still present in the main JAR, and no separate module has been created yet. The new extension point is currently dormant until an add-on registers. Next package must relocate implementation, data resources and tests, and delete the old direct core calls in a coordinated edit.
- Main-only gameplay test is still blocked by this pending packaging migration; do not misreport the project as already split.


## Migration phase B committed — independent addon source/resource modules

- **Commit `d3ebfabf`:** created `:pam-hc2` and `:farmers-delight` Gradle modules with separate `@Mod` entrypoints and required dependencies on `cropgenetics` plus their original mod.
- Moved **81** Java source/test/datapack resources in one atomic Git tree update, preserving resource paths. All Pam plants/mutations/settings and Farmer rich soils are no longer embedded in the **main** source tree.
- Core `CropDataReloadListener` no longer parses Pam settings or directly calls either specific adapter. Add-on entrypoints register via `CropDiscoveryAddonRegistry`; independent source packages avoid JVM split-package problems.
- **Static tree verification:** zero old Pam/Farmer adapter/resource files remain under `neoforge-port/src/main/`. All module sources and metadata exist.
- **Do not claim builds/tests passed**. Gradle multi-project integration, runtime addon activation and resource overlays remain unverified, by user request.
- **Remaining cleanup:** optional JEI guide, localized name fallbacks, report summaries and advisory rice warnings still contain display-only references to Pam/Farmer in the core; no external class dependency. Relocate these when reviewing addon-facing UI to complete the logical boundary.
- Full restart-safe handoff and deferred test commands: [`docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md`](ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md).
