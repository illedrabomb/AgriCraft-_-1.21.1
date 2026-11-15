# Crop Genetics — NeoForge 1.21.1 port

**Development branch:** `feature/neoforge-1211-foundation`  
**Target:** Minecraft 1.21.1 / NeoForge 21.1.265 / Java 21  
**Temporary mod ID:** `cropgenetics`

This is the NEW codebase in `neoforge-port/`. The original Forge 1.18.2 root remains unchanged as the migration reference. This branch is **not a validated playable release**.

## Features currently implemented as source code
- Genetic genomes with dominant/recessive alleles, Growth/Gain/Strength and internal Fertility/Mutativity.
- Persistent genetic seed items via Data Components and crop-stick BlockEntity genome state.
- Crop sticks and double sticks: planting, server random tick growth, harvest products with Gain, offspring inheritance, simple mutation selection.
- Simple Vanilla/mod seed conversion using an unambiguous AgriPlants item ID mapping.
- Plant/environment requirements for explicit soil IDs/tags and light tolerance adjusted by Strength.
- AgriPlants server datapack definitions for wheat, carrot, potato, beetroot, nether wart, **melon and pumpkin**, plus a carrot species mutation.
- **Client-side resource-pack crop renderer**: custom BlockEntityRenderer draws crop texture overlays keyed by Genome.plantId and BlockState.STAGE. A client reload listener reads `assets/<namespace>/cropgenetics/plant_visuals/*.json`. Included visuals for wheat/carrots/potatoes/beetroot/nether wart; unknown species use a fallback texture. The client visual loader now supports `cross`, `plus`, and approximated `gourd` geometry, including a second texture layer for gourd fruits. Selected old AgriPlants `texture.plant_textures` entries can also be loaded if mirrored as client resources.

## Important data locations
- Server: `data/<namespace>/cropgenetics/plants/*.json`
- Server: `data/<namespace>/cropgenetics/mutations/*.json`
- Client: `assets/<namespace>/cropgenetics/plant_visuals/*.json`
- See `../temporary-research/DATA_DRIVEN_PLANT_RENDERER.md` and `../temporary-research/VANILLA_MOD_SEED_CONVERSION.md` for exact schema and tradeoffs.

The original AgriPlants directories are not automatically imported. Legacy soil chemistry, complex mutation predicates, special drops, model rendering, and modded crop hooks require dedicated migrations/adapters. The client visuals are **not automatically synchronized from server datapacks**.

## Validation state
An earlier Java 21/NeoForge compilation of the foundational source code succeeded on GitHub Actions; the subsequent test compilation failed due to Gson missing from the test classpath, which was corrected. All later genetics/gameplay/renderer changes **have not been built or game-tested**. On the user's explicit request, the GitHub workflow was changed to **manual dispatch only**, and no new builds or tests have been run during these feature implementations.

## Upcoming
- Conservative manifest-driven AgriPlants importer in `tools/migrate_agriplants.py` (written but not run). First two additional crops were ported manually; see `../temporary-research/AGRIPLANTS_MIGRATION_BATCH_01.md`. Further bulk migration and reference validation remain pending.
- Melon and pumpkin have a **separate one-fruit lifecycle**: a mature stem can place an adjacent *custom owned fruit block* that uses the Vanilla melon/pumpkin model and delegates loot to Vanilla. Gain affects fruit formation. The fruit BlockEntity stores parent position and UUID; the stem stores the same UUID and direction. Removing the owned fruit immediately resets its stem, with random-tick reconciliation as a fallback. The model adds an oriented attached stem. The cornflower JSON remains visual-only. See `../temporary-research/OWNED_GOURD_FRUITS.md`.
- Validate newly implemented unique fruit ownership, stem direction and removal behavior with actual game tests when permitted. Improve advanced soil/climate rules and custom gourd support.
- Genetics inspection/storage UI, breeder villager/profession, complete plant catalog.
- Optional add-ons: irrigation, greenhouse, MineColonies.
- Compilation, gameplay smoke tests, client/dedicated-server validation before any release, when testing is re-enabled.

## License/credits
Based on the AgriCraft project and data subprojects. Preserve MIT license and original authorship/copyright information for all reused code/assets.
