# AgriCraft-Hauptmod – Testdateien für externe Tester

## Welche Datei laden?

- **Aktueller Arbeitsstand:** [`AgriCraft-Core-Sonntag-2026-11-22-v2.json`](AgriCraft-Core-Sonntag-2026-11-22-v2.json) – **51 Testfälle**, noch keiner ausgeführt; einschließlich aller `MP-01`–`MP-23`-Fälle.
- [`AgriCraft-Core-Sonntag-2026-11-22-v1.json`](AgriCraft-Core-Sonntag-2026-11-22-v1.json) – historisch vorbereitete erste Etappe mit 16 Fällen, nicht mit v2-Ergebnissen mischen.
- Die alte 30-Fälle-Datei im getrennten Tracker-Repository ist eine andere Suite und enthält historische Buildangaben. Nicht als aktuellen Testnachweis behandeln.

## Vorbereitungen

1. **Ohne ausdrückliche Freigabe des Projektbesitzers weder Gradle/JUnit noch Minecraft starten.** Die JSON-Datei selbst ist nur eine Anleitung.
2. Minecraft 1.21.1, Java 21, passende NeoForge-Version, dedizierten Testserver und die für den jeweiligen Fall benötigten ein bis zwei Testspieler bereithalten. **Nur AgriCraft-Hauptmod**, keine Pam-/MineColonies-/Farmer's-Add-ons.
3. Branch `integration/neoforge-1211-consolidation` und **genaue Git-Commit-ID des tatsächlich gebauten JARs** dokumentieren. Nicht den Vorbereitungscommit als bereits getesteten Build ausgeben.
4. Eine neue Welt oder gesicherte **Weltkopie** verwenden. Datapack-/NBT-Manipulation, Blockschutz, gleichzeitiger Blockabbau und andere riskante Szenarien **niemals am einzigen Save** durchführen.
5. [Mod-Test-Tracker](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft) lokal öffnen. **Vor „Tests laden“ bestehende Ergebnisse exportieren** – das Laden einer Suite ersetzt die bisherigen Testergebnisse im Browser.
6. v2-Datei unter **„Tests laden“** wählen. Die Suite-ID `agricraft-core-neoforge-1211-20261122-v2` ist bewusst anders als bei v1 und der alten 30er-Suite.

## Jeden Test gleich durchführen

1. Test-ID lesen, alle genannten Vorbedingungen herstellen. Fehlende Weltkopie, fehlender zweiter Spieler oder fehlendes Test-Datapack: **`blocked`**, nicht `pass`.
2. Schritte in der Reihenfolge durchführen, Ausgangs- und Endzustand fotografieren bzw. zählen. Bei Wahrscheinlichkeiten **gültige Random-Ticks** von erfolgreich entstandenen Kindern trennen.
3. Das Feld **„Erwartet“** mit der tatsächlichen Beobachtung vergleichen. **`pass`** nur bei nachgewiesenem Soll, **`fail`** bei Abweichung, **`open`** bei noch nicht begonnenem Fall.
4. Kommentar im Tracker immer nach diesem Muster ausfüllen: `Commit: …; Tester/Server: …; Ausgangslage: …; Ist: …; Anzahl/Ticks: …; Belege: Screenshot-Datei/latest.log: …`.
5. Bei Itemduplikation, Datenverlust, sichtbaren geheimen Genen oder Crash sofort stoppen, Welt/Log sichern und **`fail`** mit kleinstem Reproduktionsweg erfassen.
6. Nach jedem abgeschlossenen Themenblock **„Ergebnisse exportieren“** benutzen und JSON plus Screenshots/Logs sichern. Der Browser-Status allein ist keine belastbare Sicherung.

## Grenzen der aktuellen v2

- 51 Fälle sind **nicht die gesamte** verbindliche [`Hauptmod-Prüfliste`](../MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md). Weitere ER-/UI-/Balance-Spezialfälle kommen in einer nächsten versionierten Suite; kein fehlender Fall wird still als bestanden markiert.
- Die 23 Multiplayerfälle sind aus der vorhandenen [detaillierten Multiplayer-Matrix](../MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) übernommen. Für ihre parallelen Aktionen werden zwei Personen beziehungsweise Accounts benötigt.
- `GE-12` und einige Daten-/Blockschutz-Szenarien benötigen eine vorbereitete Weltkopie und kontrollierte Testdaten. Ohne solche Vorbedingungen **`blocked`** dokumentieren, nicht wahllos die produktive Testwelt verändern.
- Bisher fand **weder eine JUnit-/Minecraft-Ausführung noch ein bestätigter Browserimport** dieser Suite statt. Die JSON-Struktur wird lediglich statisch geprüft.

**Versionsregel:** Bei geänderten Tests oder zusätzlichen Fällen eine neue `suiteId` und neue Dateiversion verwenden. Bereits exportierte Resultate bleiben dem alten Suite-Stand zugeordnet.
