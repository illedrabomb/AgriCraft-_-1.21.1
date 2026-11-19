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


### Nachtrag: Rezeptkorrektur und Saatgutpfad (19.11.2026)

- `639b5b79`: Das Seed-Analyzer-Rezept enthielt noch zwei Vanilla-Stöcke, Glasscheibe, Steinplatte und Holz. Gemäß der früheren Vorgabe wurde es auf **genau sechs Crop Sticks und keine weiteren Zutaten** reduziert (förmiges 3×3-Muster mit sechs Belegungen).
- Das Seed-Chest-Rezept nutzt bereits acht Crop Sticks um eine normale Truhe und enthält keine Planken. Es wurde deshalb nicht geändert.
- Statische Überprüfung von `CropItems`, `CropSticksBlockEntity`, `SeedAnalyzerBlockEntity`, `SeedChestBlockEntity` und `CropHarvest`: individuelle Genetik wird über `GENOME` in den genetischen Saatgutstapeln/Blockdaten gehalten; genetische Seeds sind auf Stackgröße 1 begrenzt; die Ernte bündelt Produkte auf höchstens einen normalen Itemstack pro Produkttyp. **Diese Feststellungen sind Quelltextbefunde, kein Gameplay-Test.**
- Keine Gradle-, JUnit- oder Minecraft-Ausführung; als spätere Prüfpunkte Analyzer-Rezeptanzeige in JEI und tatsächliches Crafting aufnehmen.


### Nachtrag: Rezeptverträge, Analyzer/Chest/Journal-Integration (19.11.2026)

- `679e3408`: Neuer JUnit-Ressourcentest `CropCraftingRecipeContractsTest`: sichert dauerhaft die tatsächlichen Rezeptdateien (nicht duplizierte Hardcoded-Rezepte), einschließlich sechs Crop Sticks als einzigen Analyzer-Zutaten, acht Crop Sticks + eine Truhe für die Seed Chest, sechs Crop-Sticks-Ausgabe aus vier Vanilla-Sticks sowie Journal aus Buch + Crop Stick.
- Quelltext-Audit: `SeedAnalyzerBlock` verbindet seine neun Inventarslots mit den 27 Slots der **ersten direkt horizontal angrenzenden** `SeedChestBlockEntity` zu einem 36-Slot-`CompoundContainer`; ohne Kiste bleibt ein 9-Slot-Menü. Die Seed Chest kann eigenständig geöffnet werden. Dies erfasst bewusst nur *eine* Nachbarkiste, keinen Verbund mehrerer Kisten.
- Server-Seite `SeedAnalyzerBlockEntity.onStored`: Der Seed wird bei Aufnahme analysiert, das konkrete `Genome` bleibt unangetastet; ein eingelegtes Journal erhält die entdeckte Spezies und bekannten Mutationstripel, sofern deren Arten vollständig erforscht sind. Der Journal-Datenbestand reist mit dem Itemstack statt globaler Spieler-Speicherung.
- Inventar-/Droppfade statisch gelesen: Seed Analyzer und Seed Chest droppen ihre Inhalte bei Blockabbau. Anwenden von `setItem` kopiert den Inputstack, bevor die Stackgrenze durchgesetzt wird. Menu-/Block-Interaktion und Persistenz müssen beim späteren Spieltest geprüft werden.
- **Kein** Gradle/JUnit/Minecraft ausgeführt. Den neuen Ressourcentest erst mit der genehmigten Core-Testphase starten. Bekannte offene Reproduktion: normales Karotten-Crossbreeding und Analyser-Journal-Netzwerkverhalten.


### Nachtrag: Welt-NBT, Inventar- und Journal-Persistenz (19.11.2026)

- **`d6b6c3d2`:** Vorhandenen `UnavailableModGenomePersistenceTest` um zwei wichtige Regressionen erweitert: `GenomeCodecs.GENOME` muss Growth/Gain/Strength (1..10) samt Pflanzen-ID im **tatsächlichen NBT-Weltspeicherformat** unverändert roundtrippen; alte fünf-Paar-NBT-Genotypen müssen auf die drei sichtbaren Werte migriert und beim erneuten Speichern ohne Fertility/Mutativity geschrieben werden. Tests nur hinzugefügt, **nicht ausgeführt**.
- Statischer Welt-Save-Pfad `CropSticksBlockEntity.saveAdditional/loadAdditional`: setzt `GrowthStage`, `SeedAnalyzed`, `OriginalSeed`, `TotalGrowthStages`, Genom-NBT, Fruchtbereitschaft/Owner-Token sowie diagnostische Counter; das Client-Update-Tag entfernt die privaten Quell-/Diagnosedaten und maskiert unanaly­sierte Genwerte. Der tatsächliche Server-Neustart bleibt als späterer Gameplay-Test offen.
- `SeedInventoryBlockEntity` verwendet `ContainerHelper.saveAllItems/loadAllItems` für Analyzer und Chest. `SeedJournalItem` speichert Entdeckungen/Mutationstipps als Datenkomponenten am konkreten Buchstack; die Client-Buchseite liest eine Kopie der übermittelten Itemdaten, ohne eigenen Server-State.
- Erkenntnis für Spieltest: Journal in Analyzer einlegen, analysierte Seeds nacheinander verwenden, Journal entnehmen, Welt schließen/neu öffnen und Ergebnisse vergleichen. Ebenso einzelnes Seed-Stack samt verdecktem Genom vor/nach Neustart und bei Abbau der Crop Sticks kontrollieren. Keines dieser Szenarien ist aktuell gespielt worden.


### Nachtrag: Multiplayer-/Inventar-Überprüfung vorbereitend (19.11.2026)

- `7062c32b`, korrigiert in `434a50dc`: Neuer detaillierter, für externe Tester ausführbarer Katalog `docs/MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md` mit 14 Fällen zu Zweispieler-Zugriff, Analyzerslot/Journal, benachbarter Chest, zeitgleichem Entnehmen, Blockabbau bei geöffnetem Menü, Weltneustart, Gen-Privatsphäre und normalen/gezüchteten Seeds.
- Statischer Codepfad geprüft: Analyzer/Chest verwenden Vanilla-`ChestMenu`, `CompoundContainer`, `stillValid` mit Blockentity-Identität/Entfernung, serverseitiges Inventar und `Containers.dropContents` beim Abbau. Die world-watch-Pakete des Analyzers sind auf Item-/Pflanzenidentität und Belegungszustand begrenzt; echte Genwerte bleiben in serverseitig gespeicherten Itemstacks. Crop Sticks maskieren unanaly­sierte Stats für Client-Watcher.
- **Keine** Multiplayer-, Gradle- oder JUnit-Ausführung; eventuelle Duplikation bei simultanen Spielern und invalidierte Menüs nach Blockabbau sind ausdrücklich *noch unbestätigte Risiken*, keine als behoben gemeldeten Fehler.


### Nachtrag: Hauptmod-UI-Sprachschlüssel und Core-Backlog (19.11.2026)

- `31085815`: Neuer `CropJournalLocalizationContractTest` liest **die echten** `en_us.json`- und `de_de.json`-Ressourcen, vergleicht den vollständigen Schlüsselbestand und prüft alle dynamisch zusammengesetzten Journal-Guide-Texte (3 Seiten × 6 Zeilen), Journal-Detail-/Mutationsseiten und JEI-Infotexte auf vorhandene nichtleere Übersetzungen. Test geschrieben, **nicht ausgeführt**.
- Statische Sprachprüfung vor diesem Commit: beide Locale-Dateien hatten 63 gleichnamige Einträge; Journal- und JEI-Schlüssel vorhanden. Ein späterer echter Client-Lauf muss Lesbarkeit/Zeilenumbrüche, Textlängen und dynamische Journal-Seitennavigation prüfen.
- Kern-Quelltriage: `CrossBreeding` entspricht weiterhin den vereinbarten 2-/3-/4-direkt-Eltern- und 3+-Reihenregeln. Ohne reproduzierbaren Gegenbefund keine Zufallswahrscheinlichkeiten umgestellt. Original-Testbefund `STK-04/GEN-01` (normale Karottensamen) bleibt offen. Bereits dokumentierte visuelle Nachtests: Stage-0, Hud-Overlay/Identify, Seed-Icons, Kürbis-/Melonenfruchtzyklus.
- Keine Add-on-Features aktiviert, keine Gradle-/JUnit-/Minecraft-Ausführung. Nächste Schritte erst nach Freigabe des gebündelten Hauptmod-Testlaufs anhand des bestehenden Plans.


### Nachtrag: Interne Gourd-Frucht und Mutterpflanze gemeinsam rendern (19.11.2026)

- **`8c063355`**: Konkreter Rendering-Fehler in `CropSticksRenderer` korrigiert. Sobald `FRUIT=true` und interne Frucht ohne Legacy-Nachbar-Owner vorlag, zeichnete der BER zwar die verkleinerte Melone/den Kürbis, beendete die Methode aber mit `return`, bevor die reife Mutterpflanze gezeichnet wurde. Dieses vorzeitige `return` wurde entfernt. Die Frucht wird weiterhin innerhalb der Sticks modelliert, anschließend wird der reife Stem/Plant-Layer gezeichnet.
- `df649998`, `3ee51fe8`: Begleitende Renderer-Kommentare für interne und ältere externe Gourd-Früchte an das tatsächliche Verhalten angepasst.
- Der vierbeinige Crop-Sticks-Block ist ein eigener Blockmodellpfad und bleibt unverändert. Es wurde **keine** Wachstumsgeschwindigkeit, Reife, Dropmenge, NBT-Ownership oder Fruchtchance verändert.
- Verifikation erst nach Testfreigabe: Gourd reifen lassen, erste interne Frucht sehen; gleichzeitige sichtbare reife Pflanze prüfen; Frucht mit Rechtsklick ernten; zweite Frucht abwarten; blockabbauen/Save-Restart bei fruchttragender Pflanze und alte physische Frucht-Saves prüfen. Die tatsächliche Frucht-Stängel-Überlagerung und Farbgebung bleiben visuell unbestätigt, kein Build/Client gestartet.


### Nachtrag: Seed-Icons und alle 16 Pflanzenvisuals statisch abgesichert (19.11.2026)

- **`af8f262f`, `279ac824`:** In `melon.json` und `pumpkin.json` die veraltete zweite Frucht-Texturschicht aus dem letzten Wachstumsframe entfernt. Seit der Umstellung auf echte verkleinerte Vanilla-Fruchtblöcke innerhalb der Crop Sticks zeichnet `CropSticksRenderer` nur noch den ersten Stem-Layer; die alten `melon_side`-/`pumpkin_side`-Einträge wurden nicht verwendet. Alle acht Frames zeigen weiterhin den Stem; die Frucht selbst erscheint ausschließlich bei `FRUIT=true`. **Keine** Genetik-, Wachstums- oder Ernteberechnung verändert.
- **`fe005b27`:** Neuer `BuiltInCropVisualContractTest` prüft alle 16 Saatgutarten gegen je eine vorhandene Pflanzenvisual-Datei, die korrekte Pflanzen-ID, eine erlaubte Geometrie, acht Stufen mit einer Vanilla-Atlas-Textur pro Stufe sowie die einfache Stem-Struktur von Melone/Kürbis. Damit bleiben Wachstumsstufen und das Gourd-Fruchtmodell später auseinandergehalten.
- **`c0e92aa2`:** `SeedSpeciesCatalogTest` um die konkreten 15 Item-Texturzuordnungen erweitert; die sechzehnte Sorte Weizen wird bereits im bestehenden Test über das Basismodell geprüft. Es wird bewusst die vorhandene Vanilla-Darstellung festgehalten: So hat etwa die Süßbeere als Item kein separates Vanilla-Samen-Icon und nutzt `minecraft:item/sweet_berries`. Ein ästhetisch neues Custom-Seed-Icon ist damit **nicht** umgesetzt.
- Visuelle Nachtests nach Freigabe: Je einen Seed aller 16 Arten im Inventar/JEI prüfen; besonders Beere, Pilze, Kaktus und Blumen von ihren Normalitems unterscheiden können; Weizen/Karotte/Kartoffel/Rote Bete in Stufe 0 und 7 vergleichen; bei Melone/Kürbis fruchtlosen und fruchttragenden reifen Stem beobachten. **Statisch geschrieben, Gradle/JUnit/Minecraft weiterhin nicht ausgeführt.**
