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
- [x] ST-08: Rückgabe des Papiers beim Original-Seed-Rezept auf Erhalt sämtlicher Data Components umgestellt (`f5b3f067`); neuer JUnit-Quelltest (`9c1d80ea`) vorbereitet, nicht ausgeführt.
- [x] ST-09: JEI-Rezeptdarstellung auf fehlende Katalysator-/Papier-Restanzeige geprüft; wiederverwendbare Papier-Anleitung (`8ca2a7cb`, `b10e002c`, `f30a7c6b`) und Lokalisierungsquelltest (`4848de59`) ergänzt. Laufzeitprüfung offen.
- [x] ST-10: Wiederhergestellter Analyzer mit bereits eingelegtem Saatgut und Journal wird periodisch auf fehlende Pflanzenentdeckung und aktuelle Mutationshinweise abgeglichen (`80cc66ed`, `08247982`). Gemeinsam genutzte, idempotente Code-Logik statisch geprüft; Save/Reload-Test offen.
- [x] ST-11: Direkte Analyzer-Rechtsklicks verwenden für Client-Belegungsprognosen die öffentlichen Anzeige-Slots statt der nur serverseitig vollständigen Inventarslots (`311fce8e`). Statische Quellprüfung; Multiplayer-Laufzeit offen.
- [x] ST-12: Zweite Crop-Sticks-Lage wird nur bei bestätigtem Blockzustand verbraucht bzw. als Item ausgegeben (`88db8895`). Statisch geprüft; abgefangene/überschriebene Blockänderungen im Spiel noch offen.
- [x] ST-13: Seed-/Analyzer-/Chest-/Journal-/Papier-Datenpfade gemäß [`SEED_DATA_INTEGRITY_STATIC_AUDIT_2026-11-20.md`](SEED_DATA_INTEGRITY_STATIC_AUDIT_2026-11-20.md) gegen Quellstand `5254fbba` geprüft. **MITTEL-Risiko:** ein direkter übergroßer `Container.setItem`-Aufruf kürzt ohne Rest-Rückgabe; reguläre Slot-/Shift-Klick-Pfade weiter ungetestet. Keine Runtime-Prüfung.
- [x] ST-14: Handpflanzung und Kreuzungsnachwuchs bei fehlgeschlagenen/überschriebenen Blockzustandswechseln statisch abgesichert; kein Item-Verbrauch bzw. kein dauerhaft verstecktes Genom ohne bestätigtes `PLANTED` (`CR-14`). Ingame-Bestätigung offen.
- [x] ST-15: Wachstum, physische/interne Fruchtbildung und Standard-/interne Fruchternte behandeln einen nicht übernommenen Blockzustand als Fehlschlag; Wachstumsstufe/Fruchtmetadaten werden im betroffenen Pfad zurückgesetzt (`CR-15`). **Nur statischer Diff-/Quellreview.**
- [x] ST-16: Stage-Remap nach Reload, veraltete interne Gourd-Frucht und physische Fruchtbereinigung behalten bei abgewiesenem Blockupdate die vorherigen Wachstums-/Besitzmetadaten; spätere Wiederholung möglich (`CR-16`). **Statisch geprüft, nicht ausgeführt.**
- [x] ST-17: `CropSticksBlock.randomTick` auf `markInternalFruit` und fehlenden Aufruf von `GourdFruits.tryPlace` im aktiven Hauptmod-Wachstumspfad abgeglichen. Neue interne Frucht gegen historische physische Besitzer-Frucht in `CR-17` getrennt; kein Code-/Spieltest.
- [x] ST-18: `CrossBreeding`, `CrossFertilityRules`, `BreedingEngine` und Standardwerte von `CropBalanceConfig` auf vier direkte Eltern, 3+-Reihe und Ein-Eltern-Klonen abgeglichen; theoretische Einzel-Tick-Wahrscheinlichkeiten in `CR-18` dokumentiert. **Statisch, ohne Simulation oder Testausführung.**
- [x] ST-19: `SpeciesGeneLimits.apply` nach Auswahl der Kindart in `CrossBreeding.attempt` statisch geprüft: neue dritte Art maximal `4/4/4`, bereits beteiligte Ergebnisart höchstens eigener Bestwert je Gen oder 5; schwächere Werte bleiben erhalten. Art- und Standortvoraussetzungen anhand des gedeckelten Genoms geprüft; Same-Species-Kreuzung und Ein-Eltern-Klon unverändert. `SpeciesGeneLimitsTest` als JUnit-Quelle ergänzt, **nicht ausgeführt**.
- [x] ST-20: `BreedingEngine` auf robustheitsgewichtete vollständige Elternvorlage, 0–4-Punkte-Tabelle, +3-Grenze je Genwert, Mehrpunktverlust und 40-%-Rückgewinnung umgestellt. `CrossBreeding` gibt Reihenmodus gesondert weiter; bestehende Config-Multiplikatoren werden auf den neuen Tabellenfaktor normalisiert. `BalancedBreedingTest` neu, `RowEvolutionInheritanceTest` aktualisiert; **nur statische Quellprüfung, kein Build/Test/Simulation**.
- [x] ST-21: Doppelten Mutationsabschlag der 3+-Reihe im BAL-01-Code statisch entfernt: Normierung des historischen Reihen-Multiplikators `0,5` gegen Reihen-Baseline `0,5` statt gegen die Zahl der Eltern, während die 2-Eltern-Punktetabelle erhalten bleibt. `BalancedBreedingTest` um volle Vierpunkte-Reihe und reduzierten konfigurierten Reihen-Faktor ergänzt. **Quellreview, keine Testausführung**.
- [x] ST-22: `CrossBreeding.attempt` zählt maximal 20 unmittelbar aufeinanderfolgende reife Reiheneltern, behält höchstens vier als Genvorlagen und erhöht nur die Reihen-Entstehungschance; `CrossFertilityRules.rowFertilityMultiplier(int)` liefert 0,5 bei 3 bzw. 1,0 bei 20 Eltern und deckelt darüber. `CrossBreedingFertilityTest` um Grenzwerte erweitert. **Statische Prüfung; keine Testausführung**.
- [x] ST-23: Die reine Reihen-Auswahl in `StraightRowParents.scan` ausgelagert; `CrossBreeding.attempt` nutzt weiterhin `eligibleParent`, sodass fehlende/ungeladene Chunks, unreife Eltern und Lücken als `null` abbrechen. Elternlisten werden vor `Collections.shuffle` ausdrücklich veränderbar kopiert. `StraightRowParentsTest` für 3/4/20 Eltern, Abbruch an Distanz 2/5, maximale Probeentfernung 20 und exakt vier genetische Vorlagen **geschrieben, nicht ausgeführt**. Keine Änderung der Entstehungswahrscheinlichkeit gegenüber ST-22.

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
- [ ] GE-07: Hinzufügen und Entfernen der zweiten Crop-Sticks-Lage, insbesondere fehlgeschlagene bzw. von Schutz-Plugins verhinderte `setBlock`-Änderungen (`CR-13`).
- [ ] GE-08: Abgewiesene oder überschriebene Blockzustandsänderung bei Handpflanzung und Kreuzungsnachkomme ohne Seed-Verlust oder unsichtbares Genom (`CR-14`, nur kopierte Testwelt).
- [ ] GE-09: Misslungene Wachstumsschritte erhöhen weder Wachstumsstufe noch verbrauchen sie Knochenmehl oder Bodennährstoffe (`CR-15`).
- [ ] GE-10: Abgewiesenes Stage-Remap nach Reload bewahrt `GrowthStage` und `TotalGrowthStages`, späterer erfolgreicher Remap rechnet nur einmal um (`CR-16`).
- [ ] GE-11: Kreuzungschancen mit 2/3/4 direkten Eltern und gesonderter lückenloser 3-/4-/10-/20-Eltern-Reihe unter protokollierter Serverconfig und gültigen Ziel-Ticks prüfen; dabei Unreife, Lücke und nicht geladene Chunks als Abbruch sowie maximal vier tatsächliche Genvorlagen nachweisen. Baseline-Soll: 15 % bei 3er-Reihe, 30 % bei 20er-Reihe, 45 % bei 4 direkten Eltern. Einfache Stichproben nicht als empirischen Prozentbeleg ausgeben (`CR-18` historisch vor Längenbonus).

### Nachgelagerte Gameplay-Balance (bewusst offen)

- [ ] **BAL-01:** Vier `1/1/1`-Eltern mit Beste-Kinder-Auslese sollen `10/10/10` statistisch in **höchstens 70 Kindern** erreichen. **Im Quellcode** wird jetzt eine vollständige Elternvorlage direkt nach Robustheit gewichtet gewählt. Danach **0–4 Mutationspunkte** zufällig verteilen, höchstens **+3 pro Wert** (Überlauf verfällt). Negative Mutationen dürfen **mehr als 1 Punkt** kosten; je tatsächlich verlorenem Punkt **40 % Chance auf einen positiven Poolpunkt**, sonst endgültig verloren. **Hohe Robustheit senkt die Verlustchance**, und weniger Eltern sollen länger bis `10/10/10` benötigen. [Konkrete Verteilung, **direkte Robustheitsgewichtung statt Quadrat**, keine doppelte Nullpunkt-Chance, +3 pro Wert **relativ zur gewählten Elternvorlage** sowie Auswahlstrategie als **erste Quellimplementierung mit offenem Balancetest** dokumentiert](BAL01_GENETICS_PROPOSAL_2026-11-20.md); Parameter sind erste Balancewerte; spätere Nachjustierung abhängig von Testfreigabe und Ergebnissen. [Vollständige Aufgabenbeschreibung](CLASSIC_THREE_STAT_GENETICS_2026-11-18.md) unter BAL-01. **Implementiert und JUnit-Quellfälle ergänzt, aber nicht gebaut, simuliert oder getestet; 70-Kinder-Ziel nicht belegt.**
- [ ] **BAL-02 (Arten-Deckel + robustheitsgewichtete Vorlage implementiert; Test offen):** **Robustheitsgewichtete vollständige Elternvorlage** mit direktem Strength-Gewicht (`1/1/3` häufiger als `2/2/1`, `3/2/1`, `4/1/1`) ersetzt im Quellcode die alte unabhängige Genwert-Vererbung. **Artübergreifende Obergrenze nach Spielerwunsch ist im Code umgesetzt:** Pro Genwert maximal **5**, außer ein **Elternteil derselben Ergebnisart** besitzt bereits einen höheren Wert; wiederholte Kreuzung einer Kartoffel `5/5/5` mit einer perfekten Karotte darf sie nicht weiter verbessern. Eine neu mutierte **dritte Art** erhält unabhängig von Elternwerten höchstens **4/4/4 (festgelegt)**; durch negative Mutationen/Punkteverschiebungen können ihre Werte **niedriger** sein. Deckel **nach der derzeitigen Vererbung/Mutation**, Überlauf verfällt; neue Mutationspunkt- und Verlustlogik aus BAL-01 ist im Code, aber noch nicht ausgeführt/verifiziert. Normale same-species-Zucht und Ein-Eltern-Klon nicht pauschal deckeln. Die frühere **+5/6er-Regel ist überholt**. [Beispiele und Testmatrix](CLASSIC_THREE_STAT_GENETICS_2026-11-18.md) unter BAL-02. **Artenbegrenzung statisch geprüft, neue JUnit-Testfälle geschrieben, nicht ausgeführt; Laufzeit-/Balanceabnahme offen.**

### Ernte, Frucht und Item-Datensicherheit

- [ ] ER-01: Normales Rechtsklick-Ernten nur mit leeren Händen; keine Doppelernte, Regrowth unter Reife (`CR-03`).
- [ ] ER-02: Crop-Sticks-Abbau: genau ein genetischer Seed mit unverändertem Genom plus zulässige reife Produkte.
- [ ] ER-03: Maximal ein Stack je tatsächlich unterschiedlichem Produkt, auch bei hohem Gain und mehrfachen Datapack-Einträgen.
- [ ] ER-04: Kürbis/Melone – interne Frucht, zweiter Zyklus und kein Früchtestapel (`CR-04`, `PT-02`).
- [ ] ER-05: Wechsel GOURD→STANDARD mit bereits fertiger interner Frucht, unmittelbar danach Rechtsklick und Blockabbau (`HG-01`).
- [ ] ER-06: Gespeicherte interne Frucht mit nicht reifem Stamm darf weder per Rechtsklick noch beim Blockabbau Produkte ausgeben (`HG-02`, nur in kopierter Testwelt mit gezieltem Datapack-/NBT-Teststand).
- [ ] ER-07: Seeds mit externen Komponenten, OriginalSeed-Provenienz und geschützte Rückwandlung (`CR-08` bis `CR-11`).
- [ ] ER-08: **Ein Blatt** Papier bleibt nach einmaligem und wiederholtem Crafting im Gitter; Papierstapel behalten ihre genaue Menge und Komponenten; Shift-Klick erzeugt keine Duplikate oder Verluste (`CR-12`).
- [ ] ER-09: Misslungene Standard-/interne Gourd-Ernte erzeugt keine Produkte und lässt Frucht-/Altersdaten für einen später legitimen Versuch erhalten (`CR-15`).
- [ ] ER-10: Nach Datapack-Wechsel bzw. Entfernen physischer Frucht keine teilweise gelöschte interne Frucht-/Besitzkennung bei fehlgeschlagenem Update (`CR-16`).
- [ ] ER-11: Frisches internes Kürbis-/Melonenwachstum gegen historisch gespeicherte physische Nachbarfrucht abgrenzen; Altwelt ohne geeigneten Save als blockiert führen (`CR-17`).

### Analyzer, Chest, Journal, Client und Multiplayer

- [ ] UI-01: Analyzer/Chest-Slots, Shift-Klick von 64 Seeds, Journal und Inventargrenzen (`MP-01` bis `MP-17`).
- [ ] UI-02: Gleichzeitige Spieler, Entfernen von Analyzer/Chest während Klicks (`MP-08`, `MP-09`, `MP-17`, `MP-21`).
- [ ] UI-03: Seed-/Journal-Komponenten vor Zuschauern geschützt; Save/Reload leerer Slots (`MP-18`, `MP-19`).
- [ ] UI-04: Journal-Mutationen nach `/reload` ohne Buch-Neueinlage (`MP-20`).
- [ ] UI-05: Tooltip, verdeckte/analysierte Gene, Seed-Stapel, JEI, HUD und Stage-0-Visuals.
- [ ] UI-07: JEI zeigt die ausdrückliche Erklärung „Papier ist wiederverwendbar“ für genetische Seeds in Deutsch und Englisch; die vereinfachte Rezeptvorschau wird nicht als tatsächlich papierverbrauchend missverstanden.
- [ ] UI-06: Dedizierter Server und zwei Spieler: gespeicherte Gene, Slot-Sync und Mod-Entfernung ohne Datenverlust.
- [ ] UI-08: Bereits eingelegtes analysiertes Saatgut trägt nach Save/Reload fehlende Arten im Journal nach, ohne Entdeckungen oder Gene zu löschen (`MP-22`).
- [ ] UI-09: Belegter/leergeräumter Analyzer sagt direkte Seed- und Journal-Einlage auf Client/Server übereinstimmend voraus (`MP-23`).

## 3. Dokumentation und Abschluss

- [x] DOC-01: Test-Gate dokumentiert: [`MAIN_MOD_TEST_GATE_2026-11-19.md`](MAIN_MOD_TEST_GATE_2026-11-19.md).
- [x] DOC-02: Detailfälle dokumentiert: [`MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`](MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md).
- [x] DOC-03: Chat-/AI-Übergabe dokumentiert: [`INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md`](INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md).
- [x] DOC-06: Mobil lesbarer, nach Fall-IDs gegliederter Ablauf für den gemeinsam geplanten **Sonntagabend** vorbereitet: [`SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md`](SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md). **Nur Dokumentation; kein Test gestartet oder bestanden.**
- [ ] DOC-04: Jede ausgeführte Prüfung mit Commit, Ergebnis und Nachweis ergänzen; Fehler separat als offen markieren.
- [ ] DOC-05: Gemeinsamen Hauptmod-Test vollständig abnehmen; **erst danach** eigene Add-on-Testgates freigeben.

**Nächste Aktion:** weitere Hauptmod-Quellarbeit ohne Testausführung. Nach ausdrücklicher Freigabe mit TG-01 beginnen und diese Datei schrittweise fortführen.
