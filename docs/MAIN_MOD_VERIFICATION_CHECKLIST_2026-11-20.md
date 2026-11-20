# AgriCraft NeoForge 1.21.1 – zentrale Hauptmod-Prüfliste (20.11.2026)

**Verbindlicher Status:** Die nachfolgenden Häkchen trennen **statische Quellprüfung** von tatsächlich **ausgeführten Tests**. Ein geschriebener JUnit-Test ist nicht bestanden. Seit den neueren Analyzer-/Journal-/Cross-/Harvest-Änderungen gibt es **keine** Build-, JUnit-, CI- oder Minecraft-Freigabe des Benutzers. Historische erfolgreiche Läufe vom 17.11.2026 zählen nicht als Bestätigung des aktuellen Branches.

**Geltungsbereich:** `integration/neoforge-1211-consolidation`, Hauptmod `neoforge-port/`. Add-ons sind bewusst ausgeschlossen. Bei jeder späteren Prüfung Datum, Commit-SHA, Testumgebung, Ergebnis, Log/Beleg und ggf. Fehlerticket ergänzen. `[x]` nur mit einem konkret überprüften Ergebnis setzen; `[-]` nicht benutzen, stattdessen Fehler im Nachweis dokumentieren.

## 1. Bereits erledigte **statische** Quell-/Dokumentprüfung (keine Laufzeittests)

- [x] ST-01: Branch und Ausgangsübergaben (Handoff, Test-Gate, Multiplayerfälle) abgeglichen; Quellstand seit `70c3ffb0` fortgeschrieben.
- [x] ST-02: Seed-Pflanzung und Original-Seed-Rückwandlung auf Verlust fremder Data Components überprüft; Schutzcode ergänzt (`4257d6a0`, `00733e03`).
- [x] ST-03: Analyzer + Seed Chest: begrenzte Lager-Slots, Blockabbau-/Menü-Gültigkeitsprüfungen im Quellcode nachvollzogen (`ddd0dd32`, `276acafe`).
- [x] ST-04: Vier direkte Eltern sowie die vorhandene 3+-Reihenregel und Eltern-Blockzustände statisch geprüft (`b4af0955`). Wahrscheinlichkeiten unverändert.
- [x] ST-05: Gourd→Standard-Datapackwechsel und veraltete interne Fruchtkennung in Reload-/Harvest-/Loot-Pfaden geprüft (`a135543f`, `02c23b24`).
- [x] ST-06: Gourd-Ernte auf echte Reife und fehlende physische Besitzerkennung abgesichert (`e437397d`, `6345716e`, `986b2b89`, Testquelle `2c8a48be`).
- [x] ST-07: Ein Stack pro unterschiedlichem Ernteprodukt in `CropHarvest.products` im Quellcode bestätigt; der bereits vorhandene JUnit-Test ist **nicht** frisch ausgeführt.

## 2. Ausführung ausstehend – zunächst ausdrücklich **gesperrt**

Freigabe durch den Benutzer erforderlich, **bevor** einer der folgenden Punkte ausgeführt oder abgehakt wird.

### Technisches Gate

- [ ] TG-01: Java 21 / NeoForge 1.21.1, exakte Branch-SHA und Modliste protokollieren.
- [ ] TG-02: Hauptmod `:test` über den freigegebenen Gradle-9.2.1-Wrapper ausführen; JUnit-Fallanzahl, Fehler und Report sichern.
- [ ] TG-03: Hauptmod `:build` ausführen; JAR, Ressourcen und Mod-Metadaten prüfen.
- [ ] TG-04: Dedizierten Server und Client starten; keine Lade-/Registry-/Classloaderfehler.
- [ ] TG-05: Datapack-Reload, Welt-Save/Restart und Chunk-Unload mit unveränderten Genen durchführen.

### Genetik, Pflanzung und Wachstum

- [ ] GE-01: Weizen und Karotte als einfache 1/1/1-Saat pflanzen, Reifestufen und Reife prüfen (`STK-04/GEN-01` ausdrücklich offen).
- [ ] GE-02: Zwei, drei und vier direkte reife Eltern; steigende Kreuzungschance und Genvererbung dokumentieren.
- [ ] GE-03: Ein-Eltern-Klonen und gesonderte 3+-Geradlinigen-Regel prüfen.
- [ ] GE-04: Mutation, Samenabbau, Analyse, Replanting und Gene 1/1/1 bis 10/10/10 prüfen.
- [ ] GE-05: Elternzustände und verwaiste Ziel-Blockentities nach Save/Reload prüfen (`GEN-02`).
- [ ] GE-06: Knochenmehl, normale Random-Ticks, Nährstoffverbrauch und Bodenvoraussetzungen (`CR-01`, `CR-02`, `CR-06`, `CR-07`).

### Ernte, Frucht und Item-Datensicherheit

- [ ] ER-01: Normales Rechtsklick-Ernten nur mit leeren Händen; keine Doppelernte, Regrowth unter Reife (`CR-03`).
- [ ] ER-02: Crop-Sticks-Abbau: genau ein genetischer Seed mit unverändertem Genom plus zulässige reife Produkte.
- [ ] ER-03: Maximal ein Stack je tatsächlich unterschiedlichem Produkt, auch bei hohem Gain und mehrfachen Datapack-Einträgen.
- [ ] ER-04: Kürbis/Melone – interne Frucht, zweiter Zyklus und kein Früchtestapel (`CR-04`, `PT-02`).
- [ ] ER-05: Wechsel GOURD→STANDARD mit bereits fertiger interner Frucht, unmittelbar danach Rechtsklick und Blockabbau (`HG-01`).
- [ ] ER-06: Gespeicherte interne Frucht mit nicht reifem Stamm darf weder per Rechtsklick noch beim Blockabbau Produkte ausgeben (`HG-02`, nur in kopierter Testwelt mit gezieltem Datapack-/NBT-Teststand).
- [ ] ER-07: Seeds mit externen Komponenten, OriginalSeed-Provenienz und geschützte Rückwandlung (`CR-08` bis `CR-11`).

### Analyzer, Chest, Journal, Client und Multiplayer

- [ ] UI-01: Analyzer/Chest-Slots, Shift-Klick von 64 Seeds, Journal und Inventargrenzen (`MP-01` bis `MP-17`).
- [ ] UI-02: Gleichzeitige Spieler, Entfernen von Analyzer/Chest während Klicks (`MP-08`, `MP-09`, `MP-17`, `MP-21`).
- [ ] UI-03: Seed-/Journal-Komponenten vor Zuschauern geschützt; Save/Reload leerer Slots (`MP-18`, `MP-19`).
- [ ] UI-04: Journal-Mutationen nach `/reload` ohne Buch-Neueinlage (`MP-20`).
- [ ] UI-05: Tooltip, verdeckte/analysierte Gene, Seed-Stapel, JEI, HUD und Stage-0-Visuals.
- [ ] UI-06: Dedizierter Server und zwei Spieler: gespeicherte Gene, Slot-Sync und Mod-Entfernung ohne Datenverlust.

## 3. Dokumentation und Abschluss

- [x] DOC-01: Test-Gate dokumentiert: [`MAIN_MOD_TEST_GATE_2026-11-19.md`](MAIN_MOD_TEST_GATE_2026-11-19.md).
- [x] DOC-02: Detailfälle dokumentiert: [`MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`](MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md).
- [x] DOC-03: Chat-/AI-Übergabe dokumentiert: [`INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md`](INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md).
- [ ] DOC-04: Jede ausgeführte Prüfung mit Commit, Ergebnis und Nachweis ergänzen; Fehler separat als offen markieren.
- [ ] DOC-05: Gemeinsamen Hauptmod-Test vollständig abnehmen; **erst danach** eigene Add-on-Testgates freigeben.

**Nächste Aktion:** weitere Hauptmod-Quellarbeit ohne Testausführung. Nach ausdrücklicher Freigabe mit TG-01 beginnen und diese Datei schrittweise fortführen.
