# Analyzer-Integrität und Journal-Direkteinlage (Core)

**22.11.2026 – damalige P2-Quellkorrektur, weiterhin ohne neuen Build/Spieltest auf dem aktuellen HEAD.** Die ursprüngliche Menüphase wird durch die spätere klassische GUI-Portierung ergänzt. Anlass: [Phase-2-Livebefunde](test-results/gameplay-core-2026-11-22-phase2/README.md) `UI-01` (bestätigt: Seed/Stick in falschen Analyzer-Slots) und `UI-09` (Sneak + Journal landet nicht im Slot). Die frühere 212/212-JUnit-Abnahme gilt **nicht** für diese Änderungen.

## Korrektur

- Kombinierter Analyzer/Seed-Chest: `SeedAnalyzerCombinedMenu` konsultiert jetzt ausdrücklich `analyzer.canPlaceItem` bzw. `chest.canPlaceItem` bei **jeder** Server-Slot-Einlage. `Slot.mayPlace` allein prüft die Containerbeschränkungen nicht. Füllslots 2–8 bleiben leer; Slot 0 ist Seed, Slot 1 Journal; nur die Chest darf normale Stacks halten.
- Einzel-Analyzer: `SeedAnalyzerStandaloneMenu` schützt serverseitig Slot 0 (Seed), Slot 1 (Journal) und sieben gesperrte Füllslots, auch bei Shift-Klick. **Historie:** Die erste Korrektur nutzte noch Vanilla-`GENERIC_9x1`. **Aktueller Quellstand seit `c93f7a6`:** eigener Menütyp `CropMenus.ANALYZER` mit `ClassicSeedAnalyzerScreen` auf dem Client und Original-Analyzer-Koordinaten. `SeedAnalyzerBlockEntity.canPlaceItem` schließt das Journal im Seed-Slot aus.
- Journal-Direkteinlage: `SeedJournalItem.useOn` behandelt **Sneak + Journal + Analyzer-Zielblock** als Item-on-Block, weil Minecraft 1.21.1 den normalen Block-`useItemOn`-Pfad bei Secondary-Use überspringt. Serverseitig wird nur ein Journal in den leeren Slot kopiert und der Handstack nur in Survival um eins verkleinert. Bei belegtem Slot wird keine Einlage vorgenommen. Normale Journal-Rechtsklick-Nutzung bleibt das Buch.
- **Historischer Paketschnitt:** Dieses P2-Sicherheitspaket enthielt selbst noch keine neue GUI. Das nachfolgende, weiterhin ungetestete Paket `c93f7a6` führte die [klassische Analyzer-Oberfläche](CLASSIC_ANALYZER_GUI_PORT_2026-11-22.md) mit eigenen Menütypen ein, ohne globale Vanilla-Truhen-Screen-Overrides.

## Noch ausstehende Abnahme (gebündeltes Testgate)

1. Standalone + kombinierter Analyzer: Slot 0/1 mit passendem/unpassendem Item, Füllslot 2–8 mit Seed/Journal/Stick, Cursor + Shift-Klick, Stack 64 identischer genetischer Seeds, Chest 27 Slots; **Gesamtmengen und alle Komponenten** vor/nach vergleichen.
2. Journal mit bestätigtem Hotbar-Slot und echter Sneak-Taste, per MCPFabric-`useItemOn` und nativer Maus, bei leerem/belegtem Slot, mit Seed im Slot 0, mit GameMode Survival/Creative; keine Phantomaktion, keine Duplikate.
3. Save/Reload, Menüentfernung während Klicks, zwei Spieler erst nach separater Multiplayer-Freigabe.
4. Neue `SeedAnalyzerSlotRulesTest`-JUnit-Quelle prüft die zulässigen Slottypen und 1er-Grenze; **nicht ausgeführt**. Vorheriger grüner Build gilt nur für älteren Commit.

Historische `UI-01`/`UI-09`-Fehler im [v8-Live-Testbeleg](test-results/gameplay-core-2026-11-22-phase2/README.md) unverändert lassen. Für neue Tests die [Core-Regression 1.0](test-tracker/AgriCraft-Core-Regression-2026-11-22-v1.0.json) mit denselben IDs verwenden und deren Status nur auf dem tatsächlich neuen Build erfassen.

## ST-61 – Standalone Seed Chest: Slotfilter auch im Menü (ungetestet)

**Statischer Fund, Priorität mittel:** `SeedChestBlock.useWithoutItem` öffnete bislang `ChestMenu.threeRows`. Wie bei `UI-01` fragen die Vanilla-`Slot`-Instanzen dabei den Container-Filter `SeedChestBlockEntity.canPlaceItem` nicht selbst ab. Damit war eine nicht zum Saatgut passende Cursor-/Shift-Klick-Einlage in die **allein geöffnete** Seed Chest voraussichtlich möglich; **kein neuer Live-Nachweis**.

**Korrektur:** `SeedChestRestrictedMenu` erbt das Vanilla-3x9-Menü samt unverändertem Menütyp, Slotindex und Bildschirm. Nur auf dem Server ersetzt es die 27 Chest-Slots während `addSlot` durch Slots, deren `mayPlace` die bestehende Containerregel abfragt. Spieler-Slots bleiben unverändert. Client benutzt weiterhin die Vanilla-Truhenansicht. Kein neues Netzwerkprotokoll, keine ItemStack-Konvertierung, keine NBT-Migration, keine Änderung an bestehenden Brust-Inhalten.

**Geschriebene Regression (nicht ausgeführt):** `SeedChestRestrictedMenuTest` prüft Index/Position und einen Fake-Container, der nur Saatgut in einem ausgewählten Slot akzeptiert. Diese reine Quellprüfung beweist nicht den vollständigen Minecraft-Click-Dispatch.

**Gebündelter Spieltest (neuer Unterfall zu UI-01):**
1. Seed Chest **ohne** angrenzenden Analyzer öffnen. Stick, Journal, Stein oder fremdes Nicht-Saatgut per Cursor, Shift-Klick und Hotbar-Tausch versuchen; darf serverseitig nicht in die Chest gelangen.
2. Normale registrierte und genetische Seeds passend hineinlegen; identische Genwerte bis 64 zusammenführen; unterschiedliche Data Components getrennt halten.
3. Seed Chest an Analyzer anschließen, gemeinsam öffnen, erneut dieselben Positiv-/Negativfälle prüfen. 27 Slotpositionen und Gesamtmenge vor/nach festhalten.
4. Gesicherten Weltstand neu laden; bereits vorhandene Items bleiben erhalten. Bei kurzzeitig falscher clientseitiger Vanillavorhersage muss der Server korrigieren; keine Ghost-/Dupe-/Verlustannahme ohne Logs.
5. Bei weggebrochener Chest und altem geöffnetem Menü weitere Klicks versuchen; Menügültigkeit/Serverzustand protokollieren. Mehrspieler erst nach separater Freigabe.

**Restrisiko:** Vanilla-Client prüft ohne eigenen Menütyp die Seed-only-Regel nicht lokal voraus. Ob Fehlklicks visuell kurz „springen“, bleibt im Spiel zu beurteilen. Rückbau durch Revert des ST-61-Commits möglich.

## ST-62 – Shift-Klick-Vorprüfung für Seed Chest (ungetestet)

**Statischer Risikofund, Priorität mittel:** Der serverseitige `Slot.mayPlace`-Schutz aus ST-61 verhindert normale neue Einlagen. Eine bislang falsch befüllte Seed Chest kann aber **alte Fremditems** enthalten. Für den geerbten `ChestMenu.quickMoveStack`-Mergepfad und das kombinierte Analyzer-Menü darf nicht angenommen werden, dass eine einzelne neue `mayPlace`-Regel alle Merge-Randfälle abdeckt. Kein erneuter Live-Nachweis und keine behauptete neue Duplikation.

**Defensive Korrektur:** Vor jedem Shift-Klick **vom Spieler in die Chest** prüft `SeedChestRestrictedMenu`, ob wenigstens ein Chest-Zielslot das Item laut `Container.canPlaceItem` akzeptiert. Das kombinierte Menü prüft dieselbe Chest-Regel sowie die zwei gültigen Analyzer-Slots vor seiner bestehenden Transferlogik. Auch ein unzulässiger Altstapel darf dadurch nicht weiter aufgefüllt werden. Shift-Klick **aus** der Chest zum Spieler bleibt erlaubt, damit alte Fremditems gerettet werden können. Standalone prüft außerdem `stillValid` vor dem geerbten Transfer, um Klicks nach Blockentfernung zu unterbinden. Kein neuer Menütyp, kein Client-Screen-Wechsel, kein ItemStack-Split und keine Datenmigration.

**Regressionstestquelle:** `SeedChestRestrictedMenuTest.quickTransferPreflightRespectsEveryDestinationSlot` benutzt einen gefilterten Fake-Container und prüft zulässige Seeds, abgewiesene Sticks und leere Stacks. **Nur geschrieben, nicht ausgeführt**; vollständige Vanilla-/NeoForge-Click- und Race-Semantik bleibt im späteren Spieltest zu bestätigen.

**Regressionsrunde 1.0:** `UI-01` und `UI-16` prüfen zusätzlich ausdrücklich eine gesicherte Weltkopie mit schon vor dem Fix enthaltenem Fremditem: Shift-Klick eines weiteren gleichen Fremditems aus dem Spielerinventar darf die Chest **nicht** auffüllen; bereits enthaltenes Fremditem muss weiterhin entnommen werden können. Nach Blockabbau bei noch offenem Menü keine späte Verschiebung oder Doppel-Drops; Gesamtbestand mit Serverdaten vergleichen. Multiplayer-Rennen nur bei separater Freigabe.

**Restrisiken:** Client-Vanilla-Menü sagt mögliche Einlagen kurzfristig voraus; serverseitige Korrektur kann zu sichtbarem Springen führen. Exakte `ChestMenu`-/`moveItemStackTo`-Mergepfade und Save-/Reconnect-Belege sind ohne freigegebene Minecraft-/JUnit-Ausführung unbestätigt.
