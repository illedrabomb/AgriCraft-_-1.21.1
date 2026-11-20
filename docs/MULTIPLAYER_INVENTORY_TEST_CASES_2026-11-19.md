# AgriCraft core – multiplayer, privacy and inventory scenarios

Status: **prepared, not executed** (2026-11-19). Prior agreement: implement all changes first, then one explicitly authorized test phase. Minecraft 1.21.1, NeoForge, core mod only; Pam/Farmer addons NOT installed.

## Setup for external tester

- One dedicated server and two independent clients, players **A** and **B** (not two windows logged into one account).
- Record exact JAR version / Git commit, Minecraft version, mod list, server logs, and whether JEI is present. Use a test world and keep backups before destructive block breaks.
- Prepare 1 standard wheat seed, 1 carrot, 2 genetic seeds of the same species but deliberately different Growth/Gain/Strength values, 1 Seed Analyzer, 1 Seed Chest, 1 Seed Journal, Crop Sticks.
- For uncertain random crosses, first reproduce with fully mature parent plants and unchanged randomTickSpeed. Do not report an individual failed chance roll as a defect.
- For each case: mark PASS/FAIL/BLOCKED in the web tracker, write actual vs. expected result, attach screenshots/logs of failures and exact steps.

## Tests

| ID | Why | Actions | Expected result |
|---|---|---|---|
| MP-01 | Only the correct player should receive discovered data | A analyzes a new seed with A's journal inserted; B watches nearby and holds a separate blank journal. | A's book records the species, B's book stays blank; world-view block packets do not replicate journal contents. |
| MP-02 | Hidden gene privacy | A plants an unanalysed 7/4/9 genetic seed in single Crop Sticks while B observes the crop HUD. Afterwards A moves a separate matching seed into the analyzer and B watches its block model. | B cannot read hidden numerical genes from an unanalysed planted crop; analyzer block visual may show species icon but not 7/4/9. |
| MP-03 | Analysis must not rewrite a genome | Insert known 7/4/9 seed, analyze, withdraw and compare to original. | It reads 7/4/9; no reroll, reset to 1/1/1, or new mutation. |
| MP-04 | No duplicate on simultaneous retrieval | With one seed in the analyzer, A and B both open its inventory and try to take it within the same second. | Exactly one seed total is received; the other player sees an empty slot after synchronization. |
| MP-05 | No lost journal on concurrent clicks | A and B both open analyzer and simultaneously move the single journal. | Only one final journal with its discoveries exists; no duplicated or erased discoveries. |
| MP-06 | Adjacent chest integration | Place one Seed Chest directly horizontally adjacent to analyzer, open analyzer. Then move chest one block diagonally and reopen. | Adjacent: 36 displayed storage slots: analyzer slots 0/1 accept seed/journal, seven filler slots reject placement, plus 27 functional chest slots. Diagonal: analyzer-only nine-slot menu with seven disabled filler slots. Directly opening chest still shows 27. |
| MP-07 | No item duplication across two UIs | A opens adjacent chest directly while B opens same chest through analyzer; move the same genetic seed at the same moment. | One authoritative item, correct final slot after both menus update. |
| MP-08 | Prevent menu stale references | While A has combined analyzer+chest menu open, B breaks the chest. | Menu closes or safely invalidates; stored items drop once, A cannot keep extracting from a detached phantom inventory. |
| MP-09 | Block break with two viewers | A opens analyzer; B breaks it with one seed and one journal stored. | The block drops its inventory exactly once, and the open menu cannot make duplicates. |
| MP-10 | One-item cap on hidden-genome seeds | Attempt to merge two same-item genetic seeds with different 1..10 genotypes in inventory, chest and analyzer. | Neither seed stacks; both genotypes remain separate through transfers. |
| MP-11 | Unmodified ordinary-seed stacks | Insert and remove 64 standard wheat seeds from Seed Chest; insert one into Analyzer. | Chest retains count and item identity; Analyzer holds only one and converts it to baseline analyzed 1/1/1, leaving 63 in player inventory if inserted directly. |
| MP-12 | Persistence over restart | Store seed with known mutation in chest, distinct seed in analyzer and researched journal, then stop and restart server cleanly. | Same contents, counts, genotypes, journal discoveries and analysis flags; no ghost or empty items. |
| MP-13 | Chunk unload during GUI | A leaves an analyzer menu open, B moves out of range or causes its chunk to unload by normal gameplay. | No crash; invalid menus close; server inventory remains authoritative. |
| MP-14 | Creative handling | A in creative inserts a seed and a journal by direct right-click; B watches. | Original held item remains as expected in creative, while placed copies do not create a survival-mode duplication exploit. |
| MP-15 | Prevent 63-item loss on combined-menu shift-click | Attach chest to analyzer. With analyzer seed slot empty and chest containing enough free space, A shift-clicks a fresh 64-count stack of ordinary wheat seeds from player inventory into the four-row combined menu. B watches both storage rows. | Exactly 64 seeds remain in total: at most 1 in analyzer (analyzed 1/1/1) and the remainder in chest/player inventory, never deleted. Chest slots still allow normal vanilla stacks. |
| MP-16 | Prevent overfilling analyzer with cursor / multiplayer | With combined menu open, A picks up a 64-count ordinary seed stack on the cursor and clicks the empty analyzer seed slot. Repeat while B opens the same menu and attempts another move. | Only 1 enters analyzer, the cursor or other slots retain all remaining items; neither duplication nor item loss. Disabled filler slots stay empty. |
| MP-17 | Stale combined menu after analyzer break | A opens combined analyzer/chest UI, B breaks **analyzer only**, leaving chest intact. A quickly shift-clicks from chest while the menu is closing. | The combined menu invalidates and no item is duplicated; the intact chest still holds exactly its previous items (minus any one legitimately transferred before closure). |
| MP-18 | Chest world-watch privacy | A stores a unique unanalyzed 7/4/9 genetic seed in the Seed Chest; separately keep a researched journal in the adjacent Analyzer (the chest itself accepts only seeds). B stands nearby without opening it; compare B's authorized open-container view only afterward. | B's ordinary world/blockentity update exposes **no** inventory ItemStacks, gene numbers, journal discoveries, or seed component data. An authorized chest menu still displays its real contents. |
| MP-19 | Sparse save snapshot must replace old contents | A fills two nonadjacent chest slots, saves/restarts, empties both slots, saves/restarts again. Repeat with Analyzer seed and Journal slots. | Previously occupied but now empty slots stay empty; no phantom seeds/journal reappear. Counts and components of remaining nonempty items survive unchanged. |
| MP-20 | Reload keeps a journal up to date without reinsertion | Place a journal in the Analyzer after unlocking a known mutation recipe, leave it in the block, then disable/change that recipe in a copied test datapack and run `/reload`. Wait at least six seconds, remove the same journal, open its book, restore the original datapack and repeat. | Species discoveries remain unchanged. Only mutation hints valid in the currently active datapack are shown after the analyzer's scheduled refresh; restoring the recipe restores its hint if all three species were already discovered. |

## Additional crop-interaction cases for the later combined core test

| ID | Why | Actions | Expected result |
|---|---|---|---|
| CR-01 | A failed soil treatment must not become an accidental harvest | On ordinary unprofiled farmland, right-click a mature normal Crop Sticks plant while holding bone meal. Repeat with a water bucket. | Neither item is consumed for a nonexistent soil treatment; the crop is not harvested by the fallback empty-hand interaction. |
| CR-02 | Soil treatment remains available when configured | On explicitly profiled soil below Crop Sticks, right-click with bone meal (nutrients) and a water bucket (humidity). Check behavior at maximum values. | Server-authorized change consumes the item in survival only; already-maxed soil changes nothing and consumes nothing. |
| CR-03 | Normal harvest remains possible | With **both hands empty**, harvest a mature standard plant by right-clicking. Try the same on an immature plant. | Mature crop drops at most one stack per distinct produce item and regrows below maturity; immature crop does not pay out. |
| CR-04 | Gourd fruit harvest and next fruit | Mature melon/pumpkin in Crop Sticks, wait for its internal fruit, right-click empty-handed, then continue random ticks. | One fruit item drops; stem stays mature; a later fruit can form without replanted seeds. |
| CR-05 | Empty cross upper layer removal | Double empty Crop Sticks, right-click with both hands empty, then repeat while holding a non-crop item. | Empty-hand action returns the extra Crop Stick; holding an unrelated item cannot silently dismantle the cross. |

| CR-06 | Soil nutrient cost only follows an actual growth stage change | In a copied test datapack, configure profiled soil and nutrient loss on each successful growth (`growth_nutrient_chance: 1` in `data/cropgenetics/cropgenetics/soil_dynamics.json`); let a crop naturally advance once, then try a blocked/failed growth roll at maximum maturity. | A successful stage advance debits exactly one nutrient level (if above NONE); a crop that did not advance never debits. No negative nutrients. |
| CR-07 | Bone meal uses the same nutrient accounting as random growth | With nutrient loss chance set to 1 and suitable profiled soil, apply bone meal to an immature crop. Repeat through an external BonemealableBlock-compatible growth tool where available. Finally try a mature crop and unsuitable conditions. | Each successful actual stage advance debits at most one nutrient level; failed or invalid applications do not debit. A successful direct bone meal action consumes exactly one bone meal in survival; creative remains unchanged. |

| CR-08 | Planting must not erase extra item data | Using a controlled test command, put a valid genome component **and** a custom display name or addon data on a registered Vanilla/mod seed. Attempt to plant it in single Crop Sticks, then compare stack count/components. Repeat with a custom-named dedicated genetic seed. | Planting is rejected; neither stack nor its extra components are consumed. Ordinary unmodified mapped seeds and dedicated genetic seeds **without foreign components** still plant normally. |
| CR-09 | Plain mod seed identity is the provenance authority | In a copied test world, create a registered mod seed with a valid matching genome but an incorrect stored OriginalSeed ID (still one of our known seed metadata components). Plant, break the crop and inspect the dropped genetic seed. | The stored original source identity is the **actual planted mod seed item**, never the stale/incorrect OriginalSeed value. Recovering its source must not create an unrelated seed. |
| CR-10 | Dedicated genetic seed keeps its genuine historical origin | Create a genetic seed with a valid genome, no foreign components, and a recorded original seed ID. Plant, break it, then (in a copied datapack) temporarily remove the optional source mapping. | The exact genome/analysis flag and the historical source ID survive the planting and uprooting; missing optional mappings do not fabricate a new origin. |

## Source-level observations, not test results

### CR-11 – Rückwandlung mit fremden Daten (späterer Core-only-Test)

- **Vorbereitung:** In einer kopierten Testwelt einen gültigen `cropgenetics:genetic_seed` mit Genom, `OriginalSeed` und zusätzlichem `CUSTOM_NAME` erzeugen; einen zweiten gleichartigen Seed ohne fremde Komponenten bereithalten.
- **Aktion:** Beide getrennt mit Papier im Rezept zur Rückwandlung in ein ursprüngliches Saatgut verwenden.
- **Erwartung:** Der benannte Seed liefert **kein** Rezeptresultat und bleibt einschließlich Genom, Herkunft und Name unverbraucht. Der unveränderte Seed lässt sich bei verfügbarer, eindeutiger Ursprungs-ID wie bisher zurückwandeln; genetische Komponenten werden nur in diesem ausdrücklich angeforderten Vorgang verworfen. Die Papier-Rückgabe bleibt korrekt.
- **Nachweis:** Screenshots beider Rezeptgitter und Inventare vor/nach dem Versuch; bei Abweichung `latest.log` sichern.


- `SeedInventoryBlockEntity.setItem` copies the supplied stack and bounds stored count by item maximum and container limit. Analyzer-only and standalone chest use vanilla `ChestMenu`. The adjacent combined view now uses `SeedAnalyzerCombinedMenu` (standard 9×4 client layout) with per-slot server-side limits: analyzer row 1 item, Seed Chest rows 64 for ordinary seeds. This prevents silently truncating a shift-clicked 64-count stack to one item in the analyzer.
- `SeedChestBlockEntity.getUpdateTag` now returns no inventory data for ordinary world watchers; authorized menus still synchronize their slots.
- `SeedInventoryBlockEntity.loadAdditional` clears stale in-memory slots before the sparse NBT Item list is applied, preventing phantom contents during replacement loads.
- `SeedAnalyzerBlockEntity.getUpdateTag` transmits **display-only** seed item ID, plant identity and journal-occupancy flag rather than actual seed/journal components to ordinary world watchers. GUI inventory transfer still uses the normal authorized menu synchronization for players who open it.
- `CropSticksBlockEntity.getUpdateTag` masks unanalysed genes using `CropGenomeSyncRules`. The authoritative world-save NBT keeps real genes.
- `SeedJournalItem` stores discoveries on each actual journal ItemStack, not on a global account. `SeedJournalScreen` opens from the player's synced held book.
- Block-break paths use `Containers.dropContents`, and `stillValid` checks block-entity identity and player distance. Runtime concurrency must still be verified.

## Escalation policy

If any duplication, loss, privacy disclosure or crash is observed, preserve the save/log first; stop that case and record a minimal reproduction. Do not dismiss it as random behavior. Do not introduce optional addon JARs until the core-only phase passes.
