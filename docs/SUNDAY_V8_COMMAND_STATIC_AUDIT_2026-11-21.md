# DOC-35: statischer Befehlsabgleich für Core-Sonntag v8

**Status:** Quell-/JSON-Abgleich am 21.11.2026. **Kein** Minecraft-/Gradle-/JUnit-/CI-/Simulationslauf, kein bestätigter Testbefehl im Spiel. Gilt für die [v8-Suite](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v8.json) (`agricraft-core-neoforge-1211-20261122-v8`, 86 offene Fälle). Verbindlicher Teststand ist später die wirklich gebaute Git-SHA, nicht diese Dokumentationsprüfung.

## Befund

Die **33** benannten Kopierbefehle der v8 wurden gegen den Registrierungsbaum in `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/command/CropDataCommand.java` und gegen Vanilla-`/reload` gegengeprüft. Die vorhandenen Befehlsstrings benötigen **keine** Änderung. Sie werden vom Test-Tracker nur kopiert; die App führt sie nicht selbst aus.

| Testfälle | Befehlsform | Voraussetzung / Soll |
| --- | --- | --- |
| `TG-05`, `GE-10`, `GE-12b`, `ER-05`, `ER-10`, `UI-04`, `UI-08`, `MP-20` | `/reload` | Nur nach Freigabe, mit gesicherter Weltkopie bei manipulativen Reloads. Server-Datapack-Neuladen ist **kein** bestätigter Erfolg des jeweiligen Tests. |
| `ER-12`, `ER-15` | `/cropgenetics testseed <aktive-art> <gain>` | OP-Level 2, ausführender **Spieler** mit freiem Inventarslot, Gain 1..10; ergibt Genome `1/Gain/1`. Kein Konsolen-Inventar. |
| `ER-13`, `ER-15` | `/cropgenetics testseed <aktive-art> <growth> <gain> <strength>` | Derselbe Rechte-/Inventarcheck, drei Werte je 1..10; keine andere Reihenfolge annehmen. |
| `ER-12`–`ER-15` | `/cropgenetics harvestmultiplier`, `reset`, `<wert>` | OP-Level 2; Wert 0..64. Wirkung gilt nur für den laufenden Server; `reset` stellt den Konfigurationswert wieder her. Kein automatisches Persistieren in der Config. |
| `ER-15` | Unbekannte Art, Werte 0/11 und **zwei** Zahlen | **Absichtlich ungültig**: nicht als Testsuite- oder Buildfehler werten. Fehlende Parser-Branch/Fehlertext und unveränderter Seedbestand sind zu prüfen. |

## Vor einem späteren Lauf

1. Erst den automatischen Codex-Vorlauf und später den persönlichen Minecraft-Test **jeweils gesondert** freigeben. Diese Liste gibt keine Freigabe.
2. Die tatsächlich auf dem Server **aktiven** Pflanzen-IDs prüfen: `minecraft:pumpkin_plant` und `minecraft:melon_plant` sind die vorgesehenen Testdaten; eine abweichende Datapack-Lage kann die Fixture blockieren. Nicht mit erfundenen `give`-Data-Components umgehen.
3. Bei `testseed` Rechte, Quelle (Spieler versus Konsole), leeren Inventarplatz und Werte protokollieren. Eine absichtliche Parserablehnung benötigt **keinen** Servercrash und ist kein P0/P1-Befund.
4. Nach temporärem Erntemultiplikator **`reset`** und aktuellen Wert überprüfen; zwei Builds/Tests mit unterschiedlichem Wert nicht in denselben Beleg vermischen. Vor jedem `/reload` passende Weltkopie und Ausgangs-Datapack sichern.
5. Ergebnisse nur mit Fall-ID, Git-/JAR-SHA, tatsächlicher Ausgabe, Itemzahl und Screenshot/Log bewerten; die statische Syntaxprüfung ersetzt keine Ausführung.

**Grenze:** Der Brigadier-Baum wurde gelesen, nicht zur Laufzeit registriert. Ob der Client/Server die Befehle wirklich anbietet, ob Übersetzungen korrekt erscheinen und ob im Spiel die richtigen Items entstehen, bleibt unter `ER-12`–`ER-15` und `TG-04` offen. Die v8-JSON-Datei wurde **nicht** umgeschrieben; ältere Testresultate bleiben unangetastet.
