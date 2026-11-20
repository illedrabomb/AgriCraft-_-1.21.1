# Main-mod preflight and deferred combined test gate (2026-11-19)

Scope: branch `integration/neoforge-1211-consolidation`, root `neoforge-port/`, Minecraft 1.21.1 / NeoForge. This is a **test plan, not evidence of a passed build**. User requested implementing and documenting all planned changes first; no Gradle/JUnit/Minecraft execution without explicit consent.

**Zentrale Abhakliste:** [`MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md`](MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md). Die vorliegenden Detailfälle dienen als Prüfanleitung; Ergebnisse nur in der Abhakliste mit Commit und Belegen als erledigt markieren.

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

**Fruchtmodell am aktuellen Branch (statischer Stand 20.11.2026):** `CropSticksBlock.randomTick` erzeugt für `GOURD` eine **interne** Frucht (`markInternalFruit`), keine neue physische Nachbarfrucht. `GourdFruits.tryPlace` hat im aktuellen Hauptmod-Wachstumspfad keinen Aufrufer. Die weiterhin vorhandenen `GeneticGourdFruitBlock`-/Besitzer- und Nachbarchunk-Routinen sind für **historisch gespeicherte physische Früchte** relevant. Für einen frischen Kürbis-/Melonensamen daher intern reifen Stamm, ersten Ertrag und zweiten Zyklus testen (`ER-04`); physische Frucht-/Token-Fälle ausschließlich in einer kontrolliert kopierten Altwelt (`CR-17`). Nicht wegen fehlender Nachbarfrucht fälschlich den aktuellen Normalfall als fehlgeschlagen markieren.

- Verify world start, datapack reload, Crop Sticks single/double placement, growth, mature detection and one-stack-per-product harvest cap.
- Normal wheat/carrot seed directly onto single sticks: both should initialize Growth/Gain/Strength = 1/1/1 and mature. Reproduce old `STK-04 / GEN-01` carrot crossing question with **known mature parents**, an empty double-stick target and repeated normal random ticks; keep growth speed unchanged for the fertility comparison. Record whether one-parent spreading is configured separately.
- Two/three/four mature **direct** parents around empty target; one continuous 3+ straight row; confirm expected relative likelihood without treating a single random miss as a deterministic failure.
- **Aktuelle Kreuzungswahrscheinlichkeiten (statischer Quellstand 20.11.2026):** `CrossFertilityRules` verwendet eine feste Grundchance von **30 %**, unabhängig vom sichtbaren Genwert. Bei Standard-Servermultiplikator `crossFertilityMultiplier=1` sind es pro **berechtigtem Ziel-Random-Tick** 30 % (2 direkte Eltern), 37,5 % (3), 45 % (4) und 15 % (durchgehende 3+-Reihe, genau ein direkter Eltern-Nachbar). Ein-Eltern-Klonen: 30 % × artspezifische `spreadChance` bei aktivierter `cloneable`-Regel. Diese Werte gelten **nicht pro Sekunde**, und die zusätzlichen Bedingungen (reife gültige Eltern, geeigneter Boden, Wetter/Umwelt/Modconfig) können den Versuch ganz verhindern. Der alte Bericht zum Fertilitätswert 10 % ist historisch; maßgeblich ist hier der aktuelle Code. Detailprotokoll `CR-18`.
- **GEN-02 – Elternzustand nach Laden/Reload:** Mit einem gereiften Elternfeld und einem leeren Doppel-Stick-Ziel speichern und neu laden; zusätzlich mit einer **Weltkopie** einen Datapack-Reload während der Reifephase durchlaufen. Nur wirklich bepflanzte, gereifte einfache Crop Sticks dürfen als Eltern dienen; leere Doppel-Sticks, unbepflanzte einfache Sticks und entfernte Blöcke niemals. Vier direkte Nachbarn weiterhin mit dem bereits festgelegten Bonus; keine neue Mutationsrate.
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


### Nachtrag: Seed-Analyzer-Rechtsklick und Journal-Bedienung (19.11.2026)

- **`eece57c7`, `d74a5d1d`, `8e4c595a`:** Konkrete Client-/Server-Abweichung beim platzierten Seed Analyzer behoben. `SeedAnalyzerBlockEntity.canPlaceItem` lässt im Client zur Vorhersage der **Menü-Slots** bisher beliebige Items passieren, weil der Client den Server-Datapack-Katalog nicht besitzt. Der direkte `useItemOn`-Rechtsklick verwendete dieselbe breite Prüfung: Mit z. B. einem Diamanten oder einem Journal in der Hand konnte der Client erfolgreiches Seed-Einlegen melden, obwohl der Server stattdessen das Menü öffnen musste. Für den **direkten Block-Rechtsklick** gilt nun `SeedAnalyzerInteractionRules`: erkennbarer Vanilla-Samen oder eigener genetischer Samen mit Genom; modifizierte normale Items und andere Gegenstände werden nicht lokal als Einlegen verbraucht. Unbekannte optionale Mod-Samen bleiben serverautoritativ; ihre direkte Einlage funktioniert weiterhin auf dem Server, auch ohne Client-Vorhersage. Die tolerante Menü-Vorhersage für optional registrierte Samen bleibt unverändert.
- **`18c28119`:** Neuer isolierter `SeedAnalyzerInteractionRulesTest` deckt Diamant, Stock, Journal, Vanilla-Samen, veränderte Vanilla-Stacks, genetische Samen mit/ohne Genom sowie fremde Datapack-Samen ab. Reine Regeln; kein Client/World-Setup, bisher **nicht ausgeführt**.
- **`4e1ee659`:** `SeedJournalScreen` markiert entdeckte Pflanzeneinträge nun beim Darüberfahren. Hervorhebungs- und Klickrechteck stimmen überein. Lange lokalisierte Namen werden auf die Buchspalte begrenzt und erscheinen bei Hover vollständig als Tooltip; auch die Pflanzen-Detailüberschrift wird begrenzt. Es werden weiterhin **nur freigeschaltete** Forschungs-/Mutationsdaten gezeigt.
- **`ca830917`:** Ein versehentliches literales `\\n` in einem Kommentar der Seed Chest bereinigt (keine Funktionsänderung).
- Nach Freigabe im Client prüfen: Analyzer leer + Rechtsklick mit Diamant/Stock/Journal (muss Menü ermöglichen, nicht scheinbar einsetzen); Rechtsklick mit Vanilla-Saatgut und mit einem eigenen genetischen Seed (echte Einlage); Sneak + Journal (Journal-Slot); unbekannter Mod-Samen (Server entscheidet korrekt); Analyzermenü mit/ohne angrenzende Seed Chest; Forschungseinträge mit Maushover/Klick und lange durch Datapack eingeführte Pflanzennamen. Besonders testen, ob ein Mod-Samen ohne lokale Vorhersage trotzdem korrekt serverseitig eingelegt wird.
- Weiterhin **keine** Gradle-/JUnit-/Minecraft-Ausführung und kein allgemeiner Release/Test-Freigabewechsel. Die Visual- und Tooltip-Anpassung ist nicht im laufenden Spiel bestätigt.


### Nachtrag: Kombiniertes Analyzer-/Chest-Menü mit getrennten Stacklimits (19.11.2026)

- **Fund durch statische Prüfung:** Das alte angrenzende 4-Reihen-Menü wurde über `ChestMenu.fourRows(..., new CompoundContainer(analyzer, chest))` gebaut. Der Analyzer speichert maximal **1** Item pro Slot, die Seed Chest normale Stacks bis zum Item-Maximum. Im zusammengefassten Container ließen sich diese unterschiedlichen *Slot*-Grenzen nicht explizit abbilden: Insbesondere Shift-Klick eines 64er-Normalsamenstapels in den oberen Analyzer-Slot konnte dazu führen, dass das Menü mehr als ein Item transferieren will, die Blockentity beim Speichern aber auf eins begrenzt. Das ist ein **identifiziertes Verlustrisiko**, noch kein reproduzierter Spieltest-Fehler.
- **`a75c8f41`, `8a2cb522`, `ddd0dd32`:** Neues serverseitiges `SeedAnalyzerCombinedMenu` verwendet den bekannten Vanilla-Menütyp `GENERIC_9x4` und exakt das bestehende 36-Slot-Layout. Es legt die ersten neun Slots einzeln am Analyzer mit Maximalgröße **1** an (tatsächlich nur Seedslot 0 und Journalslot 1 befüllbar; sieben Füllslots bleiben gesperrt). Die weiteren 27 Slots sind direkt an die Seed Chest gebunden und erlauben reguläre **64** bzw. das strengere jeweilige Itemmaximum. Die Spieler-Slots entsprechen dem Vanilla-ChestMenu-Layout. Der neue Menütyp braucht clientseitig keine eigene GUI-Registrierung. `quickMoveStack` bewegt Stacks mit den jeweiligen Slotgrenzen; `stillValid` verlangt gleichzeitig gültige Analyzer- und Chest-Blockentities.
- **`cbeecb46`:** Neuer `SeedAnalyzerCombinedMenuSlotTest` prüft die 9×1-/27×64-Struktur und ungültige Slotindizes. Nur geschrieben, noch **nicht ausgeführt**.
- **`cf4f06b8`:** Externes Multiplayer-Testdokument präzisiert die sieben deaktivierten Analyzer-Füllslots und ergänzt **MP-15 bis MP-17**: 64er-Shift-Klick ohne Itemverlust, Cursor-Klick und gleichzeitiger Zweispielerzugriff, blockabgebauter Analyzer während kombinierter Menübenutzung. Alle erwarteten Mengen inklusive Reststapel explizit festhalten.
- **Wichtige Verifikationsgrenze:** Der Server nutzt seine eigenen gebundenen Slots, der Client zeichnet weiterhin die standardmäßige Vanilla-Vier-Reihen-Oberfläche. Kurzzeitige lokale Vorhersageeffekte beim Drag-/Shift-Klick sind deshalb möglich und müssen im späteren Gameplay-Test beobachtet werden. Ebenso ungeprüft: Menüschluss beim Abbau genau während anderer Spieler klicken. Kein Gradle/JUnit/Minecraft gestartet.


### Nachtrag: Seed-Chest-Datenschutz und NBT-Snapshot-Ersatz (19.11.2026)

- **`e3918534`:** `SeedChestBlockEntity.getUpdateTag` explizit auf einen **leeren** öffentlichen Blockentity-Update-Tag beschränkt. Die Chest hat keinen Inhalt-Renderer und muss weder Genome noch Journal-/Inventar-Komponenten an normale Chunk-/Blockzuschauer übertragen. Nur Spieler, die das Inventar tatsächlich öffnen, erhalten dessen Slot-Inhalte über die autorisierte Vanilla-Menüsynchronisierung. Der **persistente** Welt-Speicherpfad `saveAdditional` bleibt vollständig und unverändert. Dies adressiert einen statisch erkannten potenziellen Privacy-Leak; ein tatsächlicher Netzwerk-Sniff wurde nicht durchgeführt.
- **`21941676`, `4ef62f22`, `4389a297`:** `SeedInventoryBlockEntity.loadAdditional` entfernt vor `ContainerHelper.loadAllItems` alle vorherigen Werte aus der bestehenden `NonNullList`. Der NBT-Helper stellt nur serialisierte belegte Slots wieder her; beim Laden eines neuen/sparsamen Snapshots in eine wiederverwendete Blockentity dürfen nicht erwähnte Slots keine alten Seeds oder Journale behalten. Die Rücksetzung erfolgt nur im Ladepfad; Menü-Entnahme, Welt-Save und `getUpdateTag` bleiben getrennt.
- **`6eeae2d6`:** Neuer `SeedInventoryRestoreRulesTest` überprüft das vollständige Leeren vorher gefüllter 27er-Chest- und 9er-Analyzer-Listen bei unveränderter Größe. Test ergänzt, **nicht ausgeführt**.
- **`12129e3a`, `146c82ac`:** Externe Testdokumentation auf **19** Multiplayer-/Inventar-Fälle erweitert, darunter MP-18 (kein Zuschauerzugriff auf Chest-Item-Komponenten ohne Öffnen) und MP-19 (entleerte Slots nach wiederholtem Weltneustart bleiben leer). Testanleitung hält fest: Seed Chest speichert Seeds, nicht Journale; Journal separat im Analyzer.
- Verifikation nach expliziter Freigabe: Zwei Spieler + Packet-/NBT-Inspektion bzw. Privacy-Sichtprüfung, gefüllte und geleerte Slots über zwei kontrollierte Neustarts, optional entfernte Mod-Arten, gespeicherte/genetische Seed-Daten und Forschungsjournal. **Weder Gradle/JUnit noch Minecraft gestartet.** Kein Nachweis über tatsächlich gesendete Pakete oder laufzeitliches Verhalten.


### Nachtrag: Journal-Mutationshinweise nach Datapack-Reload aktuell halten (20.11.2026)

- **Ausgangslage:** `SeedAnalyzerBlockEntity.onStored` aktualisierte die bekannten Mutationshinweise eines Forschungsjournals nur dann, wenn Seed oder Journal in einen Analyzer-Slot gesetzt wurden. Nach einem Server-`/reload` konnte ein **bereits eingelegtes** Journal deswegen noch Hinweise auf entfernte/angepasste Mutationsdefinitionen behalten, bis es erneut eingelegt wurde.
- **`66fd3073`, `38a5e596`:** `SeedAnalyzerBlock` besitzt jetzt einen ausschließlich serverseitigen, über Position und Spielzeit versetzten Ticker (100 Ticks). `SeedAnalyzerBlockEntity.reconcileJournalMutations` gleicht bei einem vorhandenen Journal nur die bekannten Mutationshinweise mit dem aktuellen Datapack-Snapshot ab. `setChanged` erfolgt nur bei tatsächlich abweichender Hinweisliste. Der Samengenotyp, Analyzer-Slot, Journal-Speziesentdeckungen und Gene werden **nicht** neu berechnet oder gelöscht.
- **`14653a27`:** `JournalMutationRulesTest` erweitert: Entfernen einer Mutation durch Datapack-Änderung entfernt ihren aktiven Hinweis, aber nicht die bereits entdeckten drei Pflanzen; das Wiedereinspielen der Definition macht den vollständig bekannten Hinweis wieder sichtbar. Test geschrieben, **nicht ausgeführt**.
- **`dd7956e1`:** Externes Inventar-Testdokument ergänzt **MP-20**: Forschungsjournal im Analyzer belassen, Datapack-Mutation ändern, `/reload`, mindestens sechs Sekunden warten, Journal entnehmen, Buch prüfen und Datapack wiederherstellen. Eine Weltkopie und Sicherung der Datapacks sind für den späteren Test nötig.
- **Einschränkung:** Außerhalb des Analyzers befindliche Journale erhalten absichtlich keine periodischen Inventar-Mutationen; die Rekonstruktion geschieht beim nächsten Einlegen. Keine automatischen oder Minecraft-Tests ausgeführt. Multiplayer-Synchronisation und tatsächliches Datapack-Reload-Verhalten bleiben unbestätigt.


### Crop-Sticks-/Ernte-Gate ergänzt (20.11.2026)

- **`39686096`**: Knochenmehl-/Wassereimerbehandlung wird serverseitig auf vorhandenes Bodenprofil und tatsächlich geänderte Werte geprüft; der Client meldet nicht mehr unbesehen Erfolg. Fehlgeschlagene Behandlung darf keine Survival-Items verbrauchen.
- **`eae893b4`**: Manuelle Rechtsklick-Ernte sowie das Entfernen der zweiten, leeren Crop-Stick-Lage setzen nun ausdrücklich **beide Hände leer** voraus. Ein gehaltenes Item, dessen Bodenpflege fehlschlägt, darf nicht versehentlich ernten. Hierfür ist später die Interaktion aus Nutzersicht zu prüfen.
- Im externen Testfallkatalog stehen **CR-01 bis CR-05** (Bodenpflege Fehlpfad/Erfolgspfad, normales Ernten mit Ein-Stack-pro-Produkt-Grenze, Gourd-Frucht, leeres Kreuz). Bei Client-Server-Abweichungen Screenshot plus `latest.log` erfassen.
- **Noch nicht freigegeben oder ausgeführt:** Gradle, JUnit, Minecraft sowie der gesamte kombinierte Core-only-Gate-Test.


### Nachtrag: Boden-Nährstoffabrechnung auf erfolgreiche Wachstumsereignisse begrenzt (20.11.2026)

- **`eff009e4`**: `CropSticksBlock.randomTick` ruft `SoilDynamics.onGrowth` nur noch auf, wenn `crop.advanceGrowth` tatsächlich eine Stufe hinzugefügt hat. Ein bestandener Zufallswurf allein reicht nicht aus, um Boden-Nährstoffe zu verbrauchen.
- **`2a74451f`**: Sowohl der direkte Knochenmehlpfad `useItemOn` als auch `BonemealableBlock.performBonemeal` berücksichtigen bei einem **erfolgreichen** Stufenanstieg dieselbe konfigurierte Nährstoff-Dynamik. Fehlversuche, Reifestufe und fehlende Wachstumsbedingungen belasten den Boden nicht. Direkte Knochenmehl-Interaktion konsumiert weiterhin nur bei Erfolg genau ein Item in Survival.
- Der spätere manuelle Testkatalog enthält **CR-06** (natürlicher Tick/Nährstoffabzug) und **CR-07** (Knochenmehl und mod-kompatible Wachstumstools). Für deterministische Versuche wird im **kopierten** Datapack `data/cropgenetics/cropgenetics/soil_dynamics.json` `growth_nutrient_chance` auf `1` gesetzt; danach Originalkonfiguration wiederherstellen.
- **Noch ungeprüft:** tatsächliche Client-/Server-Interaktion, automatischer Bodenabzug, Knochenmehl-Verbrauch und Kompatibilität externer Wachstumstools. Keine Gradle-/JUnit-/Minecraft-Ausführung, Testfreigabe steht aus.


### Nachtrag: Verlustfreie Seed-Pflanzung und belastbare Herkunftskennung (20.11.2026)

- **`4257d6a0`**: `CropSticksBlockEntity.plant` prüft nun auch beim Einpflanzen `CropSeedSourceSafety.canConvertWithoutExternalDataLoss`. Eine Vanilla-/Mod-Saat oder dedizierte genetische Saat mit fremden, nicht rekonstruierbaren Data Components (z. B. Custom Name oder Addon-Komponenten) wird **nicht** konsumiert. Denn späteres Ausgraben konvertiert zwingend in `cropgenetics:genetic_seed` und könnte diese Fremdkomponenten sonst verlieren.
- **`d62a95f7`**: Bei einer gültigen normalen Vanilla-/Mod-Saat ist für `OriginalSeed` ausschließlich die **tatsächlich eingesetzte Item-ID** maßgeblich. Ein auf dem Quellstack hinterlegter abweichender, veralteter oder manipulierter `ORIGINAL_SEED`-Wert darf die spätere Rückwandlung nicht auf ein fremdes Item lenken. Nur unser eigener dedizierter genetischer Seed bewahrt seinen explizit gespeicherten historischen Ursprung.
- **`87eb04c0`**: Die Client-Erfolgsprognose für bereits genetisch markierte Seeds berücksichtigt die gleiche Quellkomponenten-Prüfung. Sonst würde der Client eine Pflanzung bestätigen, die der Server zu Recht ablehnt.
- **`9ee7a48e`**: Ergänzte externe Testfälle **CR-08** (fremde Item-Komponenten überleben abgewiesene Pflanzung), **CR-09** (Herkunft gewöhnlicher Mod-Seeds entspricht tatsächlich eingesetztem Item), **CR-10** (dedizierter genetischer Seed behält seinen expliziten historischen Ursprung, auch nach Mapping-Entfernung).
- Einschränkung: Das Vorbereiten von Seeds mit manipulierten Komponenten gehört ausschließlich in eine **kopierte Testwelt**. Die serverseitigen Mapping-/Provenienzregeln wurden statisch überprüft, **nicht** durch Gradle, JUnit, Minecraft oder Multiplayer ausgeführt.

### Nachtrag: Rückwandlung schützt zusätzliche Seed-Komponenten (20.11.2026)

- **`00733e03`:** `CropItems.recoverOriginalSeed` verweigert die Papier-Rückwandlung, wenn der genetische Seed fremde oder ausdrücklich entfernte Data Components im Patch trägt. Das bestehende Rezept liefert dann kein Resultat und verbraucht weder Seed noch Papier. Die absichtliche Umwandlung eines unveränderten genetischen Seeds in ein gewöhnliches Original-Item bleibt erlaubt und entfernt weiterhin bewusst die genetischen Angaben.
- `CropSeedSourceSafetyTest` enthält nun auch einen Quellcode-Testfall für das Entfernen eines fremden Standard-Components; vorhandene Fälle für benannte Stacks decken die genutzte Sicherheitsregel ab. **Tests wurden nicht gestartet.**
- Externer manueller Fall **CR-11** im Multiplayer-/Inventarkatalog. Verbleibende Risiken: Laufzeitvalidierung des Rezeptgitters und dessen Papier-Rückgabe sowie gesamter Core-only-Gate weiterhin offen.

### Nachtrag: Schließendes Analyzer-/Chest-Menü gegen späte Klicks absichern (20.11.2026)

- **`276acafe`:** Die gemeinsame `SeedAnalyzerCombinedMenu`-Ansicht prüft jetzt zusätzlich bei jedem Shift-Klick sowie vor Einlegen und Entnehmen in ihren 36 Lager-Slots, dass sowohl Analyzer als auch Seed Chest unverändert vorhanden und für den Spieler erreichbar sind. Das bisherige `stillValid` zum regulären Schließen bleibt bestehen.
- Das reduziert das statisch identifizierte Zeitfenster zwischen Blockabbau und automatischem Menüschluss: ankommende Lager-Klicks dürfen kein bereits abgetrenntes Inventar mehr verändern. Der normale Standalone-Analyzer bzw. die Standalone-Chest sowie Spielerinventar-Slots sind nicht umgebaut worden.
- **MP-21** erweitert die spätere Zwei-Spieler-Abnahme um Cursor-, Pickup-, Hotbar- und Shift-Klicks während des Abbaus beider Blöcke. **Kein Gradle, JUnit, Minecraft oder CI ausgeführt**; keine Behauptung eines nachgewiesenen Duplikationsfehlers oder einer vollständigen Netzwerkrace-Verifikation.

### Nachtrag: Interne Gourd-Frucht nach Harvest-Mode-Reload (20.11.2026)

- **Problem:** Ein Kürbis-/Melonenstamm mit intern fertiger Frucht (`FruitReady=true`, ohne physische Besitzerkennung) konnte nach einem Datapack-Wechsel `GOURD` → `STANDARD` nicht mehr regulär geerntet werden. Der Reife-/Erntepfad sperrte jedes `FruitReady`, während die regelmäßige Fruchtbereinigung interne Früchte unangetastet ließ.
- **`a135543f`, `02c23b24`:** Eine veraltete interne Fruchtkennung wird beim Reload-Abgleich zurückgesetzt, **ohne** die Reife zu löschen. Vor dem Abgleich kann der sichere read-only Erntepfad eine solche reife Pflanze bereits einmalig als normale Frucht ernten – auch beim Blockabbau. Physische Früchte mit Besitzerkennung bleiben ausdrücklich gesperrt, bis deren Eigentumsverhältnis geklärt ist. Die normale Gourd-Fruchtfolge bleibt unverändert.
- **JUnit-Quelltest `7ec6d263`:** `CropFruitTransitionRulesTest` für die drei Fälle `GOURD`-Normalzyklus, `STANDARD` mit internem Reststatus und physische Besitzerkennung; Tests noch **nicht ausgeführt**.
- **HG-01 (späterer manueller Test):** In einer Kopie der Welt einen reifen Kürbis-/Melonenstamm mit intern sichtbarer Frucht erzeugen. Datapack-Pflanzendefinition vorübergehend von `GOURD` zu `STANDARD` ändern, `/reload`, ohne Warten einmal mit leeren Händen ernten; alternativ im separaten Versuch vor dem Tick abbauen. Einmal normaler Produktauswurf und Erhalt des Genoms beziehungsweise Seed-Drop beim Abbau, keine Duplikate; nach 100 Ticks muss der Blockzustand reconciled sein. Ursprungsdatapack anschließend wiederherstellen.
- **Nicht durchgeführt:** Gradle, JUnit, Minecraft, CI. Produktauswahl nach einem absichtlich geänderten Datapack folgt dessen **aktiver** Definition, nicht dem früheren Gourd-Modus.

### Nachtrag: Nur reife interne Früchte dürfen auszahlen (20.11.2026)

- **`e437397d`, `6345716e`, `986b2b89`:** Die persistente `FruitReady`-Markierung allein reicht nicht mehr als Auszahlungsbeweis. `CropFruitTransitionRules.canHarvestInternalGourd` verlangt zusätzlich aktive `GOURD`-Definition, tatsächliche Reife und das Fehlen einer physischen Besitzerkennung. Der gleiche Grenzwert wird bei Rechtsklick, Block-Loot und dem serverseitigen Fruchtabschluss verwendet.
- Der JUnit-Quelltest `CropFruitTransitionRulesTest` (`2c8a48be`) enthält zusätzlich Fälle für unreife Pflanze, physische Besitzerkennung und falschen aktiven Erntemodus. **Noch nicht ausgeführt.**
- **HG-02 (später, nur Testweltkopie):** An einem gespeicherten Stamm mit `FruitReady=true` die aktive Reifestufendefinition so ändern, dass er effektiv **unreif** ist; vor und nach `/reload` Rechtsklick und getrennt Blockabbau prüfen. Keine Früchte als Ernteprodukt, aber beim Abbau weiterhin den einen genetischen Seed erhalten. Anschließend Ursprungsdatapack und Testwelt wiederherstellen. Keine Behauptung, dass normale Spielmechanik von selbst diesen ungewöhnlichen Zustand erzeugt.

### Nachtrag: Original-Seed-Rezept behält Komponenten des zurückgegebenen Papiers (20.11.2026)

- **`f5b3f067`:** `OriginalSeedRecoveryRecipe.getRemainingItems` kopiert jetzt das eine zurückgegebene Blatt direkt aus dem tatsächlich eingesetzten Papier-Stack. Zuvor wurde neues Standardpapier erzeugt, wodurch z. B. ein individueller Name verloren ging. Der genetische Seed wird weiterhin verbraucht; bei ungültigem Rezept gibt es kein Rückgabe-Item.
- **`9c1d80ea`:** `OriginalSeedRecoveryRemainderTest` als noch nicht ausgeführter JUnit-Quelltest ergänzt. Manueller Test **CR-12** im Inventarkatalog; zentrale Abhakliste **ST-08/ER-08**. Der Test betrifft unveränderte Item-Komponenten sowie unveränderte Netto-Papiermenge.

### Nachtrag: JEI erklärt den wiederverwendbaren Rezept-Katalysator (20.11.2026)

- **`8ca2a7cb`, `b10e002c`, `f30a7c6b`:** JEI registriert das dynamische Original-Seed-Rezept als Beispiel-Shapeless-Rezept, das keine Restzutat anzeigen kann. Die neue separate JEI-Informationsseite `jei.cropgenetics.seed_recovery` erklärt in `de_de` und `en_us` ausdrücklich, dass ein Blatt Papier für mehrere Rückwandlungen genügt und individuelle Papier-Komponenten erhalten bleiben. Der alte JEI-Seed-Pflanzguide bleibt unverändert.
- **`4848de59`:** `CropJournalLocalizationContractTest` prüft künftig das Vorhandensein beider neuen Sprachtexte. Testquelle geschrieben, **nicht ausgeführt**.
- Manueller Fall **CR-12** erweitert um exakt ein Papier, Papierstapel, ungültiges Rezept und Shift-Klick; zentrale Prüfliste **ST-09** (statische Erklärung) und **UI-07** (tatsächliche JEI-Ansicht) ergänzt.

### Nachtrag: Journal-Entdeckungen nach Laden nachtragen (20.11.2026)

- **`80cc66ed`, `08247982`:** `SeedAnalyzerBlockEntity.reconcileJournalMutations` verwendet jetzt dieselbe idempotente Journal-Synchronisierung wie das Einlegen eines Seeds/Journals. Wenn bereits gespeicherte Slots nach einem Welt-/Chunk-Neuladen einen **analysierten** Seed mit noch nicht protokollierter Art enthalten, wird die Art nachgetragen und werden anschließend die gültigen Mutationshinweise erneuert. An anderen Entdeckungen, Seed-Genen und Analysemarkierungen ändert sich nichts. Der 100-Tick-Ticker bleibt bestehen und markiert nur tatsächlich geänderte Journale als dirty.
- Der neue manuelle Fall **MP-22** validiert Nachtragen, Wiederholung ohne Duplikate, nicht analysierte Seeds und vollständige bestehende Journale. Zentrale Prüfliste: **ST-10** statisch geprüft, **UI-08** noch nicht ausgeführt.
- Gradle, JUnit, Minecraft und CI bleiben ungestartet; es gibt bislang keinen echten Save-/Reload-Nachweis für diesen Pfad.
