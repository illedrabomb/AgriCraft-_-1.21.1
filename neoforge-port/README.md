# Crop Genetics — NeoForge 1.21.1 port

**Development branch:** `entwicklung`  
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
- AgriPlants-compatible server datapack definitions for wheat, carrot, potato, beetroot, nether wart, **melon and pumpkin**, plus five new Vanilla crop-stick species (cornflower, poppy, dandelion, cactus, sugar cane) and a carrot species mutation. Cactus/sugar cane currently approximate Vanilla growth rules; special block adapters remain future work.
- **Client-side resource-pack crop renderer**: custom BlockEntityRenderer draws crop texture overlays keyed by Genome.plantId and BlockState.STAGE. A client reload listener reads `assets/<namespace>/cropgenetics/plant_visuals/*.json`. Included visuals for wheat/carrots/potatoes/beetroot/nether wart; unknown species use a fallback texture. The client visual loader supports `cross`, `plus`, and approximated `gourd` geometry; visuals and missing-PNG warnings now publish as one client resource snapshot. Plant quads now sample sprites from the Minecraft block atlas, and `assets/minecraft/atlases/blocks.json` declares `textures/plant/` atlas sources. Physical gourd fruits use their own block models, not the old virtual fruit texture. Selected old AgriPlants `texture.plant_textures` entries can also be loaded if mirrored as client resources.

- Original AgriCraft asset reuse: wooden crop-stick block/item textures, legacy melon/pumpkin frames as **optional assets**, and the original magnifying-glass analyzer icon are retained. Vanilla plants, including melons and pumpkins, use Minecraft sprites by default. The original MIT license is retained at `LICENSE`. See `../temporary-research/ORIGINAL_AGRICRAFT_TEXTURES_PORT.md`.

## Versioning and branch policy

- `master`: pinned to commit `fe111987fb50759aa9607fc5b68f79430c124441`, the **0.1.0-alpha source revision reported as thoroughly tested locally with Codex**. A subsequently built and booted local analyzer variant was not identified by its own commit SHA and is **not** asserted to be present on `master`.
- `entwicklung`: ongoing feature work; the current mod version is increased with each commit (starting with `0.2.1-alpha`), and new code remains unverified until explicitly tested.
- **Minor** milestone (`0.1.0-alpha` → `0.2.0-alpha`): increase the middle number when starting a substantial feature batch, resetting the patch number to zero.
- **Every commit on `entwicklung` increments the third number** (`0.2.0-alpha` → `0.2.1-alpha` → `0.2.2-alpha`); include the updated `mod_version` in the SAME commit. This applies to docs, fixes and features alike, without an extra version-only commit. The `-alpha` suffix is preserved, and a version bump does not imply a successful build or test.
- Only promote a specific, identified and verified commit from `entwicklung` to `master` after the agreed validation gate. Keep the working branch for ongoing changes.

## 0.2.8-alpha: NeoForge 1.21.1 API compile fixes (Codex H1)

- `CropSticksBlock` and `GeneticGourdFruitBlock`, both derived from
  `BaseEntityBlock`, now implement its required `codec()` method using
  `MapCodec` plus the Vanilla `simpleCodec(Properties constructor)` helper.
- The `CropItems.CROP_STICKS` registration is typed
  `DeferredItem<BlockItem>` to match NeoForge's `registerSimpleBlockItem`
  return type, instead of the incompatible `DeferredItem<Item>`.
- No gameplay or resource behavior was intentionally changed in this commit.
- **Source-level fix only**: no compile, tests or game launch were authorized.
  Further compile issues, if any, remain to be found by a later authorized build.

## 0.2.7-alpha: client-side survival prediction for specialty soils

Confirmed that `PlantVisuals` accepts **both** the `textures` schema
and the `stages` schema used by bamboo, berries and mushrooms. The files
are not malformed merely because the newer crops use `stages`.

Fixed the client-side `CropSticksBlock.canSurvive` path: its crop catalogue
is server-side and can be empty on the client. A client is therefore
permissive for non-farmland supports rather than predicting that valid
sand, mud, mycelium or modded soil placement must fail. The logical
server retains the authoritative registry-based soil and planting-site
validation and rejects unsupported positions. This affects prediction,
not the allowed server-side crop locations.

Source-only change; no build, client launch or gameplay test performed.

## 0.2.6-alpha: brown and red mushroom crops

Added `minecraft:brown_mushroom_plant` and `minecraft:red_mushroom_plant`
as genetic Crop Sticks plants using the Vanilla mushroom items and block sprites.
Their planting-site adapter accepts mycelium, podzol and Nether nylium at any
light level, or dirt-family/farmland/moss at raw block brightness below 13.
The shared site check applies to planting, normal growth and crossbreeding.
Their datapack definitions use a 0..15 light range so the stricter light
rule only applies to ordinary soils, not specialty mushroom substrates.

The crops are intentionally **not** giant mushroom generators and do not
emulate Vanilla random mushroom spread: they use normal Crop Sticks growth,
genetic inheritance and harvesting. No builds or gameplay tests were run.

## 0.2.5-alpha: bamboo and sweet berry genetic crops

Two more Minecraft crop-stick plants have been added, using Vanilla items and sprite references:
`minecraft:bamboo_plant` (seed and harvest `minecraft:bamboo`) and
`minecraft:sweet_berry_plant` (seed and harvest `minecraft:sweet_berries`).
Their block-site restrictions are implemented by `CropConditions.matchesPlantingSite`,
shared by planting, growth and mutation selection. Bamboo accepts dirt-family,
sand, red sand, gravel, moss, mud and muddy mangrove roots. Sweet berry bushes
accept dirt-family, farmland, moss, mud and muddy mangrove roots.
Both require their defined minimum light level to grow. Berry visual frames
use the four default Vanilla bush stages; bamboo uses Vanilla bamboo sprites.
These remain *crop-stick crops*, not native bamboo columns or damaging bushes.
No external mod dependency is introduced. No build or test has run.

## 0.2.4-alpha: avoid incorrect source seed restoration

For original-seed recovery, a recorded `cropgenetics:original_seed` ID is **rejected** if the active plant catalogue maps that item to a different plant, or the item is marked ambiguous between species. If the original mod's integration has disappeared and the source item still exists without a conflicting mapping, its recorded ID remains usable. Legacy seeds lacking a recorded source only gain a fallback when exactly one registered, unambiguous seed item maps to their genome species. No genetic values are modified by the check.

Attached-genome Vanilla/mod seeds now display a tooltip reminder to plant them on **Crop Sticks**. Ordinary Vanilla placement outside Crop Sticks does not have a genetic BlockEntity and will not preserve this mod's inherited genes. These are source-only changes, not yet built or game-tested.

## 0.2.3-alpha: Vanilla-style growing sites for cactus and sugar cane

Crop-stick **sugar cane** now requires a Vanilla dirt-family block, sand/red sand or mud underneath and an adjacent water fluid (or frosted ice) at the **supporting soil's Y level**. Crop-stick **cactus** requires sand or red sand beneath and rejects solid horizontal neighbors or fluid in those spaces. This is checked when planting, during random-tick growth, and when selecting parent/mutation offspring sites. Neither crop now has an artificial minimum light requirement. These are crop-stick-site rules: they **do not** create multi-block Vanilla stems or implement every Vanilla survival/damage update. These source changes have not been built or game-tested.

## 0.2.2-alpha: in-place planted crop analysis

Use the `cropgenetics:seed_analyzer` on an occupied Crop Sticks block to mark the **planted** crop analyzed without harvesting it, altering its genome or consuming the analyzer. The BlockEntity saves and synchronizes the analysis state, so the seed dropped when the sticks are broken will display its genes with Shift. The analyzer still works on offhand seeds when used in the air. On clients, preexisting-genome ordinary seed stacks can predict placement without requiring the server-only crop catalogue; the server still authoritatively validates the species and growth conditions. Source-only changes; no build or gameplay test run.

## Crafting-only recovery of original seeds

One `cropgenetics:genetic_seed` **plus one paper** in any two crafting slots (2x2 inventory or 3x3 crafting table) yields exactly one plain original Minecraft/mod seed. The genetic seed is consumed; **the paper is returned as the crafting remainder** and is reusable. This is a special dynamic crafting recipe whose result is resolved from the seed's recorded `cropgenetics:original_seed` item ID, falling back to the current crop catalogue for older seeds. If the original item is unavailable or cannot be identified, the recipe does not match and nothing is consumed. There is no right-click recovery action. The returned plain seed intentionally loses all genetics and analysis data. See `src/main/java/dev/illedrabomb/cropgenetics/content/OriginalSeedRecoveryRecipe.java`. Source-only change; local crafting verification has not been performed.

## Vanilla visuals and analyzed genetic seed tooltips

Vanilla Minecraft textures remain the **default** for Vanilla species, including melon and pumpkin; the copied original AgriCraft gourd PNGs are optional assets rather than active replacements. The original AgriCraft crop-stick textures remain in use because Crop Sticks have no equivalent Vanilla asset.

Genetic seed stacks support a persistent `cropgenetics:analyzed` Boolean component. A playable `cropgenetics:seed_analyzer` is now registered (glass pane + stick), used from the main hand on an offhand seed. Existing alleles are preserved; unmodified registry-matched Vanilla seeds receive the established 1/1/1 baseline before being marked analyzed. Shift reveals Growth/Gain/Strength and Fertility/Mutativity alleles. The crop BlockEntity now also persists `SeedAnalyzed` and `OriginalSeed`, restoring them to the genetic seed when uprooted; new crossbreeding offspring start unanalyzed. If a locally developed analyzer already exists, compare and merge rather than duplicating item registrations. See `../temporary-research/VANILLA_TEXTURE_PRIORITY_AND_ANALYZED_SEED_SHIFT.md` and `../temporary-research/ANALYZER_GENOME_PRESERVATION_AND_FIVE_PLANTS.md`.

## Important data locations
- Server: `data/<namespace>/cropgenetics/plants/*.json`
- Server: `data/<namespace>/cropgenetics/mutations/*.json`
- Client: `assets/<namespace>/cropgenetics/plant_visuals/*.json`
- See `../temporary-research/DATA_DRIVEN_PLANT_RENDERER.md` and `../temporary-research/VANILLA_MOD_SEED_CONVERSION.md` for exact schema and tradeoffs.

The original AgriPlants directories are not automatically imported. Legacy soil chemistry, complex mutation predicates, special drops, model rendering, and modded crop hooks require dedicated migrations/adapters. The client visuals are **not automatically synchronized from server datapacks**.

## Five-workstream gameplay reliability update (2026-11-15)
The following source-only improvements are committed together: a staggered server BlockEntity ticker reconciles live crop stage metadata after datapack reload even with random ticks disabled; right-click harvest reconciles age before eligibility checking while block loot stays read-only; crossbreeding filters mutation species by actual destination soil and prefers a viable parent species if necessary; duplicate client visual species IDs are disabled with a predictable fallback; melon/pumpkin fruit placement now requires successful binding to an adjacent loaded parent crop. See `../temporary-research/CORE_GAMEPLAY_FIVE_WORKSTREAMS_2026_11_15.md`. **No builds or tests have run for these changes.** Ticker performance, API compilation, client visuals and fruit loot remain unverified.

## Validation state
An earlier Java 21/NeoForge compilation of the foundational source code succeeded on GitHub Actions; the subsequent test compilation failed due to Gson missing from the test classpath, which was corrected. All later genetics/gameplay/renderer changes **have not been built or game-tested**. On the user's explicit request, the GitHub workflow was changed to **manual dispatch only**, and no new builds or tests have been run during these feature implementations.

## Upcoming
- Conservative manifest-driven AgriPlants importer in `tools/migrate_agriplants.py` (written but not run). First two additional crops were ported manually; see `../temporary-research/AGRIPLANTS_MIGRATION_BATCH_01.md`. Further bulk migration and reference validation remain pending.
- Melon and pumpkin have a **separate one-fruit lifecycle**: a mature stem can place an adjacent *custom owned fruit block* that uses the Vanilla melon/pumpkin model and delegates loot to Vanilla. Gain affects fruit formation. The fruit BlockEntity stores parent position and UUID; the stem stores the same UUID and direction. Removing the owned fruit immediately resets its stem, with random-tick reconciliation as a fallback. The model adds an oriented attached stem. Cornflower now has its own server crop definition, and five new Vanilla species have been added; none emulate all special Vanilla planting mechanics. See `../temporary-research/OWNED_GOURD_FRUITS.md`.
- Vanilla fruit loot delegation now supplies the matching Vanilla BLOCK_STATE while retaining other loot context. Validate actual Silk Touch/Fortune/modded loot behavior, unique fruit ownership, stem direction and client atlas rendering with game tests when permitted. See `../temporary-research/RENDER_ATLAS_AND_GOURD_LOOT_AUDIT.md`.
- Improve advanced soil/climate rules and custom gourd support.
- Genetics inspection/storage UI, breeder villager/profession, complete plant catalog.
- Optional add-ons: irrigation, greenhouse, MineColonies.
- Compilation, gameplay smoke tests, client/dedicated-server validation before any release, when testing is re-enabled.

## License/credits
Based on the AgriCraft project and data subprojects. Preserve MIT license and original authorship/copyright information for all reused code/assets.
