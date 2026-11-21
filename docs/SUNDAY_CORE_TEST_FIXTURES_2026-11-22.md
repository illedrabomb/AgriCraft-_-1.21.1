# AgriCraft-Hauptmod: Testwelt-Aufbauten und Vorbereitungen für Sonntag

**Stand:** 21.11.2026 · **nur Vorbereitung, nichts gebaut oder ausgeführt**. Erst nach ausdrücklicher Freigabe des Projektbesitzers Gradle, JUnit, Minecraft, CI, Multiplayer oder Simulation starten. Ergänzt den [Testabend-Ablauf](SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md) und die [Tracker-v5-Suite mit 79 offenen Fällen](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v5.json); keine eigene Testsuite und keine heimlich abgehakten Fälle.

**Optionale F0/F1-Bauhilfe:** Ein [nicht ausgeführtes Vanilla-Datapack](SUNDAY_CORE_FIELD_DATAPACK_2026-11-22.md) kann nach späterer Freigabe die sechs Basis-/Kreuzungsflächen vorbereiten. Es räumt Terrain ab und ist daher **ausschließlich für eine neue Wegwerf-Testwelt** geeignet; Crop Sticks, Eltern, Genetik und Reife müssen weiterhin manuell geprüft werden.

## Vorher – gleiche Spielbedingungen für vergleichbare Ergebnisse

- [ ] Exakten **Git-Commit des gebauten Hauptmod-JARs**, Minecraft-/NeoForge-Version, Java 21, Modliste und Server-/Client-Logs aufschreiben. Keine Pam-/MineColonies-/Farmer's-Add-ons. JEI nur als ausdrücklich dokumentierte **optionale Anzeigehilfe** für die JEI-Fälle.
- [ ] Ein freies Areal in einer **neuen Core-Testwelt** auswählen; Weltname, Dimension, Biom, Startposition und **Koordinaten jeder Fläche** im Tracker notieren. Es werden **keine festen absoluten Koordinaten** vorausgesetzt.
- [ ] Server-Config zunächst unverändert lassen und die Werte für `crossFertilityMultiplier`, `rowCrossEvolution`, `rowEvolutionMutationMultiplier`, `fruitChanceMultiplier` und `randomTickSpeed` erfassen. Standardwerte nicht mit später veränderten Vergleichswerten mischen. `/gamerule randomTickSpeed` liest den aktuellen Wert nur aus.
- [ ] Eine **Backup-Kopie vor dem ersten Eingriff** anlegen; nach jedem Themenblock einen Tracker-Export und Screenshots sichern. Die alte 30er-/v1-/v2-/v3-/v4-`suiteId` ist nicht mit v5 kompatibel.
- [ ] Keine Seeds mit `/give` und frei erfundenen Data Components erzeugen. Genetische Seeds zunächst durch normales Pflanzen/Abbau oder gesichert bekannte Testgegenstände beschaffen. Für perfekte Werte und künstliche NBT-Zustände später gezielte, getrennte Fixtures vorbereiten, andernfalls den Sonderfall als `blocked` markieren.

## F0 – Basistest: einfache Crop Sticks und Pflanzbedingungen

1. **Zwei getrennte Pflanzplätze** auf geeignetem Ackerboden mit genau **einer** Crop-Sticks-Lage bauen: einer für gewöhnlichen Weizensamen, einer für gewöhnliche Karotte. Samen und Stufe 0 fotografieren.
2. Eine Pflanze normal wachsen lassen; Knochenmehl auf eine andere unreife Pflanze testen. Reife, Alter, Gene nach Analyse und Materialverbrauch pro Pflanze protokollieren.
3. **Eine separate Bodenversuchs-Reihe** mit mindestens einem nicht passenden Untergrund und einer passenden Fläche anlegen. Bei einem abgewiesenen Pflanzversuch `UI-10` Aktionsleistenmeldung, genaue `soil_block`-/`#soil_tag`-ID, Seedzahl vorher/nachher und Standort notieren.
4. Nur falls diese Arten im aktiven Datapack unterstützt sind: Zuckerrohr **mit und ohne Wasser direkt neben seinem Stützboden**, Pilz auf Myzel gegenüber hellem anderem Boden sowie eine erlaubte Kaktus-Pflanzfläche mit/ohne feste Nachbarblöcke. Nicht aus den Beispielen auf allgemeine Wasser-/Pilzregeln schließen.
5. Eine leere doppelte Kreuzlage vorbereiten; direktes Seed-Pflanzen verweigern lassen, dann mit leerer Hand obere Lage entfernen und genau **einen** zurückgegebenen Crop Stick zählen. Nachher wieder normal pflanzen.

**Zugeordnete IDs:** `GE-01`, `GE-06`, `GE-07`, `UI-05`, `UI-10`, teilweise `ER-02`. Frühere Karotten-/Stage-0-/X-Sticks-Befunde hier ausdrücklich wiederholen.

## F1 – Reife Eltern für Kreuzung: Anordnungen nicht vermischen

Alle Eltern müssen **tatsächlich bepflanzt, reif und am Standort wachstumsfähig** sein. Das Ziel ist jeweils ein **leeres doppeltes** Crop-Sticks-Feld. Für jede Anordnung ein eigenes Ziel verwenden, damit zusätzliche Nachbarpflanzen nicht unbemerkt die Zahl der direkten Eltern erhöhen:

| Kennung | Aufbau um das Ziel | Erwartete Grundlage bei Config-Multiplikator 1 | Nachweis |
| --- | --- | --- | --- |
| F1-A | **Eine** reife Pflanze direkt neben dem Ziel | Eigener **Klonpfad**, kein vererbungsbedingter 2+-Eltern-Nachkomme | Genaues Elterngenom und unverändertes Kindgenom. |
| F1-B | **Zwei** reife Pflanzen auf zwei direkten Nachbarfeldern | **30 %** pro gültigem Ziel-Random-Tick | Gültige Ticks und erfolgreiche Kinder getrennt zählen. |
| F1-C | **Drei** direkte reife Nachbarn | **37,5 %** pro gültigem Ziel-Random-Tick | Alle drei Elternarten und Genome protokollieren. |
| F1-D | **Vier** direkte reife Nachbarn | **45 %** pro gültigem Ziel-Random-Tick | Genau vier mögliche genetische Vorlagen. |
| F1-E | Eine reife Pflanze direkt am Ziel, danach **ohne Lücke** 2, 3, 9 bzw. 19 weitere reife Pflanzen in **derselben Richtung** | **3-/4-/10-/20-Eltern-Reihe**: an den Endpunkten 15 % bzw. 30 % pro gültigem Tick; nur bis zu vier Vorlagen vererben Gene | Reihenlänge, Reife, Lücken, Zahl gültiger Ticks und Kinder zählen. |

**Wichtig:** Die langen Reihen dürfen **keinen zweiten direkten Elternnachbarn** am Zielfeld haben; sonst hat die direkte 2+-Regel Vorrang. Die Reihenlänge wird ab der ersten reifen Pflanze neben dem Ziel gezählt. Bei einer Lücke oder einem unreifen Segment endet die zusammenhängende Reihe.

**Zugeordnete IDs:** `GE-02`, `GE-03`, `GE-04`, `GE-05`, `GE-11`, später `BAL-01` mit eigenem statistischem Aufbau. **Ein einzelner Erfolg/Fehlschlag beweist keine Prozentangabe.** Nicht mehr als 70 erfolgreiche Kinder ist ein Balanceziel über Zuchtläufe, keine maximale Wartezeit in Minuten.

## F2 – Ernte, Früchte, Originalseed und wiederverwendbares Papier

1. Je eine Standardpflanze und zwei reife, unabhängig aufgebaute **Melonen-/Kürbis-Stängel** vorbereiten. Interne Frucht **innerhalb** der Crop Sticks ansehen; auf den **zweiten** gültigen Fruchtzyklus warten. Für Früchte Fruchtversuche/Status protokollieren, nicht bloß verstrichene Minuten.
2. Normale Rechtsklick-Ernte mit **leerer Hand** prüfen; unmittelbar wiederholten Klick ohne neue Reife als Duplikationskontrolle. Für jeden Produkttyp Anzahl pro Ernte festhalten: höchstens **ein Stack pro unterschiedlichem Produkt**.
3. Mit notiertem `1/1/1` und später höher entwickeltem Seed: Pflanzen → Abbau → Genome vergleichen. Original-Seed-Rückwandlung getrennt an **einem Blatt Papier**, einem **16er-Papierstapel** und individuell benanntem Papier durchführen; danach Shift-Klick/Seriencrafting. **Gesamtsumme Papier netto unverändert.**
4. Fremde Data Components, Datapack-Wechsel GOURD→STANDARD, alte physische Frucht und erzwungene fehlgeschlagene Ernte **nur in kopierter Sonderwelt**, sonst entsprechende ID `blocked`.

**Zugeordnete IDs:** `ER-01`–`ER-11`; sichere Standardfälle zuerst, invasive `ER-05`/`ER-06`/`ER-09`/`ER-10`/`ER-11` erst nach besonderer Vorbereitung.

## F3 – Analyzer, Seed Chest, Journal und optionale JEI-Sicht

1. **Analyzer allein** sowie **Analyzer mit genau einer waagerecht benachbarten Seed Chest** aufbauen. Diagonal aufgestellte Chest als Negativkontrolle verwenden. Vor dem Öffnen alle Inventarplätze leer lassen.
2. Normale 64er-Samenstapel, **zwei verschiedene genetische Seeds mit notierten Genen** sowie ein gesondertes Journal bereithalten. Analyzer-Seed-/Journal-Slots (je **1**) von Chest-Slots (**27 × bis 64**) unterscheiden; Shift-Klick und Cursor-Transfer zählen.
3. Journal bei analysiertem Seed einlegen, Entdeckungen notieren, gespeichert lassen, dann **normalen** Save/Restart durchführen. `/reload` und gezielte fehlende Journal-Einträge erst in gesicherter Sonderwelt. Private Gene dürfen bloßen Zuschauern nicht sichtbar werden.
4. Nur wenn JEI ausdrücklich im freigegebenen Testprofil vorhanden ist: genetische Seed-Varianten, Papier-Katalysator-Hinweis und DE/EN-Text prüfen. `UI-11a/b/g` vergleichen `soil_block`/`#soil_tag`, Licht, Umwelt und Sonderregeln mit der **wirklichen Serverdefinition**. Die Quellimplementierung von `UI-11` ist **noch nicht im Spiel bestätigt**.
5. Für `UI-11c` zwei gültige Datapackstände, für `UI-11e` zwei getrennte Server und für `UI-11h` eine gesicherte A/B/C-Reloadfolge vorbereiten. `UI-11d` erfordert zwei Spieler; `UI-11f` ein Core-Profil ohne JEI. `UI-11i` ohne sicheres Netzwerk-Harness als **blocked** dokumentieren, keine Pakete in einen echten Server injizieren.

**Zugeordnete IDs:** `UI-01`, `UI-03`–`UI-11i`, `MP-06`, `MP-10`–`MP-12`, `MP-15`–`MP-16`, `MP-19`–`MP-23`. Gene niemals durch einen rein optischen Vergleich als gleich behaupten – berechtigter Spieler muss sie am einzelnen Seed prüfen.

## F4 – Zwei Spieler und gefährliche Zustandswechsel: Weltkopie

- [ ] Dedizierten Server mit **zwei echten Testspielern A/B** bereitstellen; bei nur einem Spieler die konkurrierenden `MP`-Fälle als `blocked` notieren.
- [ ] Eindeutig unterscheidbare Seeds/Journal-Inhalte zählen. A öffnet Analyzer-/Chest-Menü, B öffnet zweiten Zugang oder baut einen Block ab. Erst nach Startzustands-Screenshot die konkurrierende Aktion auf gemeinsames Signal durchführen.
- [ ] **Analyzer-Abbau** und **Chest-Abbau** in **zwei getrennten wiederhergestellten Kopien** durchspielen. Intakte Chest/Analyzer-Inventare müssen ihren Bestand behalten; entferntes Inventar darf **nur einmal** droppen.
- [ ] Gezielt abgelehnte Blockupdates, alter Fruchtbesitz, NBT-Manipulation und `GE-12a`–`GE-12f` **nicht frei improvisieren**. [Sondermatrix GE-12](CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) verlangt eine tatsächlich gültige Mutationsregel, vorbereitete Datapacks und dokumentierte Elternwerte. Ohne geeignete Fixture als `blocked` behandeln.

**Zugeordnete IDs:** `MP-01`–`MP-23`, `GE-08`–`GE-10`, `GE-12a`–`GE-12f`, `ER-05`/`ER-06`/`ER-09`–`ER-11`, `UI-02`/`UI-04`/`UI-06`/`UI-08`/`UI-09`. Mehrspieler-/Inventar-Datenverlust ist ein **Stoppsignal** für den betroffenen Block.

## F5 – Nachgelagerte Balance, nicht zwischen Standardtests mischen

`BAL-01` verlangt dokumentierte **erfolgreich entstandene Kinder** bis `10/10/10` mit konsequenter Beste-Kinder-Auslese und ausreichend unabhängige Zuchtläufe; `BAL-02` braucht kontrollierte artübergreifende Eltern und ggf. ein Datapack. Ohne gesonderte statistische Vorbereitung/Freigabe `blocked`/`open`, **niemals** aus einer kurzen Testreihe `pass` ableiten. Keine Simulation gestartet.

## Minimaler Ergebniszettel pro Fläche

`Fixture-ID; Fall-ID; Commit/JAR; Datum/Uhrzeit; Dimension/Biom/Koordinaten; Boden/Licht; Elternarten und Gene; Ausgangs-/Endinventar; Serverconfig/randomTickSpeed; Anzahl gültiger Ticks/Kinder; erwartetes Ergebnis; tatsächliches Ergebnis; pass/fail/blocked/open; Screenshot/Log/Tracker-Export`.

**Abschluss:** Jeden Themenblock im [Tracker-v5](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v5.json) dokumentieren und Ergebnisse exportieren. Nur beobachtete Fälle `pass`; fehlende Voraussetzungen `blocked`, nicht still überspringen. Vor erneutem Test der nächsten Kategorie Weltzustand sichern. **Alle Häkchen in diesem Dokument sind Vorbereitungs-Checkpoints, derzeit nicht ausgeführt.**
