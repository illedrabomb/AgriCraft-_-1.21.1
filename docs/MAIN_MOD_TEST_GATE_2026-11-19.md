# Main-mod preflight and deferred combined test gate (2026-11-19)

Scope: branch `integration/neoforge-1211-consolidation`, root `neoforge-port/`, Minecraft 1.21.1 / NeoForge. This is a **test plan, not evidence of a passed build**. User requested implementing and documenting all planned changes first; no Gradle/JUnit/Minecraft execution without explicit consent.

## Static checks performed

1. `settings.gradle` includes `:pam-hc2` and `:farmers-delight` with distinct project directories. The root project's Gradle configuration still declares the main `cropgenetics` mod.
2. Both standalone add-on `build.gradle` files apply `java-library` and ModDev, use Java 21, `compileOnly project(':')`, and emit their own named JARs. Their independent generated metadata uses required dependencies on `cropgenetics`, NeoForge, Minecraft, and the corresponding source mod.
3. **Concrete configuration blockers fixed**: Groovy apostrophes in `mod_name` strings for Pam's and Farmer's now use double quotes (commits `797d3efe`, `2ee24a86`).
4. The add-on constructors register extensions only through public `CropDiscoveryAddonRegistry`, `CropSpeciesDisplayRegistry`, `CropDataCommandExtensions` and `CropSeedGuideRegistry` APIs. The add-ons retain external crop policies in their own Java/resources trees. Static inspection alone does not prove the separate runtime class loaders resolve their `compileOnly` host dependency.
5. Core `CrossBreeding.attempt`: empty double sticks check direct mature north/east/south/west parents, a continuous 3+ parent straight row when exactly one direct parent exists, a separate one-parent clone path, then one success roll for >=2 parents. Two/three/four direct parents use multipliers 1.0/1.25/1.5; row uses 0.5. No chance values changed during this review.
6. Plain unmodified registered seeds take `Genome.basic` when planted in *single* Crop Sticks; cross offspring is only accepted into *empty double* sticks. Past carrot-seed cross report remains an unresolved **gameplay reproduction** question, not a proven source regression.
7. Root-level projects' exact Gradle tasks and external mod metadata still need actual build and runtime checks. **Do not infer successful compilation from this static review.**

## Execution order after explicit user release

### A. Core only – first test gate

From PowerShell inside `neoforge-port`:

```powershell
& .\gradlew.bat :test :build --no-daemon --console=plain
```

Use explicit `:test :build`: unqualified `test build` may include subprojects. Collect the console log and HTML test report when failures occur. Inspect the JAR for accidental Pam/Farmer crop resources and class entries.

### B. Core-only Minecraft launch and manual test cases

Only core JAR with minimum required loader/vanilla dependencies, optionally JEI for UI. No Pam or Farmer add-on JARs.

- Verify world start, datapack reload, Crop Sticks single/double placement, growth, mature detection and one-stack-per-product harvest cap.
- Normal wheat/carrot seed directly onto single sticks: both should initialize Growth/Gain/Strength = 1/1/1 and mature. Reproduce old `STK-04 / GEN-01` carrot crossing question with **known mature parents**, an empty double-stick target and repeated normal random ticks; keep growth speed unchanged for the fertility comparison. Record whether one-parent spreading is configured separately.
- Two/three/four mature **direct** parents around empty target; one continuous 3+ straight row; confirm expected relative likelihood without treating a single random miss as a deterministic failure.
- Cross seed genotype persists after drop and replant; unanalysed genetic seeds do not merge and do not lose mutation; analyzer + adjacent Seed Chest + journal.
- JEI guide remains core-only; Pam/Farmer addon-specific descriptions, commands and species names must not appear.
- Multiplayer/dedicated server: config and seed/components survive save/reload, no client-only class loaded server-side.
- Record failed/blocked cases separately from passed ones in the external web tracker with explicit reproduction, expected/observed, mod list and screenshots.

### C. Optional modules – later independent gates

Only after main mod has passed and user releases add-on testing:

```powershell
& .\gradlew.bat :pam-hc2:build :farmers-delight:build --no-daemon --console=plain
```

Add-on build/launch verification is **not** a prerequisite to the core-only manual gameplay test; however, their build configuration is now parsed as part of the Gradle multi-project setup and must remain syntactically valid.

- Pam: installed core + `pamhc2crops` + Pam add-on. Confirm reviewed plant/produce mappings, explicit overrides, seed guide and admin command. Never claim unverified Pam variants are equivalent.
- MineColonies: future separate module (not included in the two current add-on builds).
- Farmer: installed core + Farmer + Farmer add-on. Confirm cabbage/tomato/onion only; native rice water/panicle parity remains deliberately unsupported.

## Blockers and priorities

- No actual Gradle execution or NeoForge launch has occurred after the add-on extraction. Do not release JARs based on static analysis.
- Priorities fixed by user: main AgriCraft core first; Pam's slightly above MineColonies; Farmer's Delight much later.
- Optional Ponder guidance is deferred even later.
- Keep progress in `docs/INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md` and `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md`.
