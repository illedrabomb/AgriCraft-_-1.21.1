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

## Source-level observations, not test results

- `SeedInventoryBlockEntity.setItem` copies the supplied stack and bounds stored count by item maximum and container limit. Analyzer-only and standalone chest use vanilla `ChestMenu`. The adjacent combined view now uses `SeedAnalyzerCombinedMenu` (standard 9×4 client layout) with per-slot server-side limits: analyzer row 1 item, Seed Chest rows 64 for ordinary seeds. This prevents silently truncating a shift-clicked 64-count stack to one item in the analyzer.
- `SeedAnalyzerBlockEntity.getUpdateTag` transmits **display-only** seed item ID, plant identity and journal-occupancy flag rather than actual seed/journal components to ordinary world watchers. GUI inventory transfer still uses the normal authorized menu synchronization for players who open it.
- `CropSticksBlockEntity.getUpdateTag` masks unanalysed genes using `CropGenomeSyncRules`. The authoritative world-save NBT keeps real genes.
- `SeedJournalItem` stores discoveries on each actual journal ItemStack, not on a global account. `SeedJournalScreen` opens from the player's synced held book.
- Block-break paths use `Containers.dropContents`, and `stillValid` checks block-entity identity and player distance. Runtime concurrency must still be verified.

## Escalation policy

If any duplication, loss, privacy disclosure or crash is observed, preserve the save/log first; stop that case and record a minimal reproduction. Do not dismiss it as random behavior. Do not introduce optional addon JARs until the core-only phase passes.
