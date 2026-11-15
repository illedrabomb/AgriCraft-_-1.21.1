# Crop Genetics — NeoForge 1.21.1 implementation

**Development branch:** `feature/neoforge-1211-foundation`  
**Target:** Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21.  
**Working name/ID:** Crop Genetics / `cropgenetics`.

This source root is separate from the preserved original Forge 1.18.2 root. The mod is **not yet gameplay-ready or build-verified**.

## Implemented source
- NeoForge ModDevGradle project and `@Mod` entrypoint.
- Immutable `GenePair`, `Genome`, and `BreedingEngine`.
- Original AgriPlants 1.18.2 core plant/mutation parser; server datapack reload listener.
- Registered plant/mutation definitions accessible through `AgriPlantsRegistry`.
- Example carrot definition and carrot mutation data, plus JUnit parser/genetics tests.

## What data integration means now
Place data into `data/<namespace>/cropgenetics/plants/*.json` or `data/<namespace>/cropgenetics/mutations/*.json`. The old AgriPlants JSON shapes for **basic seed items, harvest items, growth, and simple unconditional mutations** are accepted. Only the example carrot entries are included so far. This **does not yet make carrots grow, mutate, or produce drops in-game**.

Current unsupported old JSON fields include soil/environment requirements, custom callbacks, special products, tag/NBT handling, special mutations, structure generation, rendering and advanced mod integrations. These must be deliberately implemented or rejected to avoid false compatibility. Vanilla/mod item IDs require later registry validation. Full AgriPlants import/conversion is pending.

## Still missing
Actual seed items and Data Components, Crop Sticks/BlockEntity, crop tick/harvest logic, mutation application, UI, village buildings, extensive modpack adapters, complete import, tests in-game. Automated tests are authored but **not yet executed**. A matching Gradle wrapper must be added before a reproducible build.

## Attribution
Derived from [AgriCraft](https://github.com/AgriCraft/AgriCraft) and AgriPlants/AgriCore, with their respective contributors and licenses. Original licensing/attribution notices must be retained for reused material. See `../docs/NEOFORGE_1.21.1_PLAN.md`.
