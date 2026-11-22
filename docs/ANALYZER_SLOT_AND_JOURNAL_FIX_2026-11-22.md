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
