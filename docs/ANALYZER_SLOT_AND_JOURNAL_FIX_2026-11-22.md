# Analyzer-Integrität und Journal-Direkteinlage (Core)

**22.11.2026 – Quellkorrektur vorbereitet, kein neuer Build/Spieltest.** Anlass: [Phase-2-Livebefunde](test-results/gameplay-core-2026-11-22-phase2/README.md) `UI-01` (bestätigt: Seed/Stick in falschen Analyzer-Slots) und `UI-09` (Sneak + Journal landet nicht im Slot). Die frühere 212/212-JUnit-Abnahme gilt **nicht** für diese Änderungen.

## Korrektur

- Kombinierter Analyzer/Seed-Chest: `SeedAnalyzerCombinedMenu` konsultiert jetzt ausdrücklich `analyzer.canPlaceItem` bzw. `chest.canPlaceItem` bei **jeder** Server-Slot-Einlage. `Slot.mayPlace` allein prüft die Containerbeschränkungen nicht. Füllslots 2–8 bleiben leer; Slot 0 ist Seed, Slot 1 Journal; nur die Chest darf normale Stacks halten.
- Einzel-Analyzer: eigener serverseitiger `SeedAnalyzerStandaloneMenu` mit exakt dem Vanilla-`GENERIC_9x1`-Slot-/Layout-Protokoll. Der Client erhält weiter das bekannte Vanilla-Truhenmenü; der Server schützt die zwei aktiven Analyzer-Slots und 7 gesperrten Füllslots auch bei Shift-Klick. Kein Paket-/GUI-Registrierungswechsel. `SeedAnalyzerBlockEntity.canPlaceItem` schließt Journal im Seed-Slot auch bei clientseitiger Vorhersage aus.
- Journal-Direkteinlage: `SeedJournalItem.useOn` behandelt **Sneak + Journal + Analyzer-Zielblock** als Item-on-Block, weil Minecraft 1.21.1 den normalen Block-`useItemOn`-Pfad bei Secondary-Use überspringt. Serverseitig wird nur ein Journal in den leeren Slot kopiert und der Handstack nur in Survival um eins verkleinert. Bei belegtem Slot wird keine Einlage vorgenommen. Normale Journal-Rechtsklick-Nutzung bleibt das Buch.
- **Kein Ersatz der klassischen GUI in diesem Sicherheitspaket**: Die beschlossene Original-Analyzer-Ansicht ist ein separates, anschließendes Client-/Menüpaket und braucht einen eigenen Test; keinesfalls einen ungetesteten globalen Vanilla-Truhen-Screen-Override.

## Noch ausstehende Abnahme (gebündeltes Testgate)

1. Standalone + kombinierter Analyzer: Slot 0/1 mit passendem/unpassendem Item, Füllslot 2–8 mit Seed/Journal/Stick, Cursor + Shift-Klick, Stack 64 identischer genetischer Seeds, Chest 27 Slots; **Gesamtmengen und alle Komponenten** vor/nach vergleichen.
2. Journal mit bestätigtem Hotbar-Slot und echter Sneak-Taste, per MCPFabric-`useItemOn` und nativer Maus, bei leerem/belegtem Slot, mit Seed im Slot 0, mit GameMode Survival/Creative; keine Phantomaktion, keine Duplikate.
3. Save/Reload, Menüentfernung während Klicks, zwei Spieler erst nach separater Multiplayer-Freigabe.
4. Neue `SeedAnalyzerSlotRulesTest`-JUnit-Quelle prüft die zulässigen Slottypen und 1er-Grenze; **nicht ausgeführt**. Vorheriger grüner Build gilt nur für älteren Commit.

Ergebnisse getrennt zu `UI-01`/`UI-09` im v8-Tracker und dem Live-Testbeleg eintragen, keine alten Rohdaten überschreiben.

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
