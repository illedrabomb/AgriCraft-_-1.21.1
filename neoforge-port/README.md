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
- Atomic crop datapack reload: one `CropDataReloadListener` prepares plant and mutation JSON from the same resource manager, preflights conflicting active species IDs before detailed validation, then publishes plants, mutations and unambiguous seed mappings via one immutable `AgriPlantsRegistry.Snapshot`. Validation and soil-tag diagnostics complete before a plant enters the candidate map, while `replaceAll` rejects inconsistent mutation references rather than silently removing them. Cross-breeding captures one snapshot per attempt. See `../temporary-research/ATOMIC_CROP_DATAPACK_RELOAD.md` and `../temporary-research/CROP_RELOAD_CONFLICT_TRANSACTION_AUDIT.md`.
- Per-resource datapack diagnostics: each loaded, disabled, invalid or conflicting plant/mutation file is recorded in an immutable `CropReloadReport`, published in the same snapshot. The server log summarizes totals and explains every skipped definition. See `../temporary-research/CROP_DATAPACK_DIAGNOSTICS.md`.
- Item/block reference checks: after parsing, `PlantRegistryValidator` verifies registered seed items, harvest products and explicit soil blocks. Unknown IDs disable the plant with a specific `INVALID` diagnostic; dependent mutations are skipped. A nonfatal `WARNING` reports missing `tags/block/<id>.json` source files for configured soil tags. The client visual loader separately warns about missing PNG resources without disabling the visual. Runtime tag membership and stitched atlas correctness are not yet verified. See `../temporary-research/CROP_REGISTRY_REFERENCE_VALIDATION.md` and `../temporary-research/SOIL_TAG_AND_CLIENT_TEXTURE_DIAGNOSTICS.md`.
- Static registry/reload audit: explicit seed/product items and soil blocks reject the special `minecraft:air` entry, and server resource discovery is scoped to exact crop data subfolders. Core 1.21.1 reload, block-property and atlas-render API signatures were also checked. See `../temporary-research/NEOFORGE_REGISTRY_RELOAD_STATIC_AUDIT_03.md`.
- Import safety limits: the AgriPlants parser and `PlantDefinition` restrict required mod/seed/stage/product lists to at most 64 entries, with stage values in 0..4096. Client visual stages are independently validated and their selection clamped. The BlockEntity now syncs exact growth age and total stage count to the client renderer, enabling more than eight distinct visual frames; the blockstate still retains eight model stages. Legacy or out-of-range saved age metadata is repaired on server chunk load, with a random-tick fallback if the crop catalogue was not available at load time. See `../temporary-research/LEGACY_CROP_STAGE_CHUNK_LOAD_MIGRATION.md`. See `../temporary-research/AGRIPLANTS_PARSER_VISUAL_STAGE_LIMITS.md` and `../temporary-research/EXACT_CROP_STAGE_CLIENT_SYNC.md`.
- Operator-only server command: `/cropgenetics data [summary|errors [page]|all [page]|tags [page]]` displays the latest datapack report and can inspect actually bound soil-tag membership from the live block registry (eight lines per page). Requires permission level 2 and also works from the server console. See `../temporary-research/CROP_DATAPACK_COMMAND.md` and `../temporary-research/RUNTIME_SOIL_TAG_AND_CLIENT_RESOURCE_SNAPSHOT.md`.
- AgriPlants server datapack definitions for wheat, carrot, potato, beetroot, nether wart, **melon and pumpkin**, plus a carrot species mutation.
- **Client-side resource-pack crop renderer**: custom BlockEntityRenderer draws crop texture overlays keyed by Genome.plantId and BlockState.STAGE. A client reload listener reads `assets/<namespace>/cropgenetics/plant_visuals/*.json`. Included visuals for wheat/carrots/potatoes/beetroot/nether wart; unknown species use a fallback texture. The client visual loader supports `cross`, `plus`, and approximated `gourd` geometry; visuals and missing-PNG warnings now publish as one client resource snapshot. Plant quads now sample sprites from the Minecraft block atlas, and `assets/minecraft/atlases/blocks.json` declares `textures/plant/` atlas sources. Physical gourd fruits use their own block models, not the old virtual fruit texture. Selected old AgriPlants `texture.plant_textures` entries can also be loaded if mirrored as client resources.

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
- Vanilla fruit loot delegation now supplies the matching Vanilla BLOCK_STATE while retaining other loot context. Validate actual Silk Touch/Fortune/modded loot behavior, unique fruit ownership, stem direction and client atlas rendering with game tests when permitted. See `../temporary-research/RENDER_ATLAS_AND_GOURD_LOOT_AUDIT.md`.
- Improve advanced soil/climate rules and custom gourd support.
- Genetics inspection/storage UI, breeder villager/profession, complete plant catalog.
- Optional add-ons: irrigation, greenhouse, MineColonies.
- Compilation, gameplay smoke tests, client/dedicated-server validation before any release, when testing is re-enabled.

## License/credits
Based on the AgriCraft project and data subprojects. Preserve MIT license and original authorship/copyright information for all reused code/assets.
