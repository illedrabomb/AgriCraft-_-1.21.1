# AgriCraft-Hauptmod – Testdateien für externe Tester

## Welche Datei laden?

- **Für den Test bevorzugt:** [`AgriCraft-Core-Sonntag-2026-11-22-v5.json`](AgriCraft-Core-Sonntag-2026-11-22-v5.json) – **79 deutschsprachige Testfälle**, einschließlich `UI-10`, `UI-11` und neun gesonderten JEI-/Sync-Unterfällen (`UI-11a`–`UI-11i`). Alle Fälle noch offen, keine Ausführung bestätigt.
- [`AgriCraft-Core-Sonntag-2026-11-22-v4.json`](AgriCraft-Core-Sonntag-2026-11-22-v4.json) – 69 Fälle, für bisherige Ergebnisse unverändert erhalten.
- [`AgriCraft-Core-Sonntag-2026-11-22-v3.json`](AgriCraft-Core-Sonntag-2026-11-22-v3.json) – 68 Fälle, historisch unverändert für alte Ergebnisimporte.
- [`AgriCraft-Core-Sonntag-2026-11-22-v2.json`](AgriCraft-Core-Sonntag-2026-11-22-v2.json) – Zwischenstand mit 51 Fällen, wegen anderer `suiteId` nicht mit v3-Ergebnissen mischen.
- [`AgriCraft-Core-Sonntag-2026-11-22-v1.json`](AgriCraft-Core-Sonntag-2026-11-22-v1.json) – historisch vorbereitete erste Etappe mit 16 Fällen, nicht mit v2-Ergebnissen mischen.
- Die alte 30-Fälle-Datei im getrennten Tracker-Repository ist eine andere Suite und enthält historische Buildangaben. Nicht als aktuellen Testnachweis behandeln.

## Vorbereitungen

Für die konkrete Gestaltung der Testflächen zuerst die [F0–F5-Aufbauanleitung im AgriCraft-Repository](../SUNDAY_CORE_TEST_FIXTURES_2026-11-22.md) öffnen. Sie trennt sichere Gameplay-Tests von Weltkopien mit NBT-/Datapack-/Blockschutz-Versuchen. **Keine dieser Flächen ist bereits gebaut oder getestet.**

1. **Ohne ausdrückliche Freigabe des Projektbesitzers weder Gradle/JUnit noch Minecraft starten.** Die JSON-Datei selbst ist nur eine Anleitung.
2. Minecraft 1.21.1, Java 21, passende NeoForge-Version, dedizierten Testserver und die für den jeweiligen Fall benötigten ein bis zwei Testspieler bereithalten. **Nur AgriCraft-Hauptmod**, keine Pam-/MineColonies-/Farmer's-Add-ons.
3. Branch `integration/neoforge-1211-consolidation` und **genaue Git-Commit-ID des tatsächlich gebauten JARs** dokumentieren. Nicht den Vorbereitungscommit als bereits getesteten Build ausgeben.
4. Eine neue Welt oder gesicherte **Weltkopie** verwenden. Datapack-/NBT-Manipulation, Blockschutz, gleichzeitiger Blockabbau und andere riskante Szenarien **niemals am einzigen Save** durchführen.
5. [Mod-Test-Tracker](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft) lokal öffnen. **Vor „Tests laden“ bestehende Ergebnisse exportieren** – das Laden einer Suite ersetzt die bisherigen Testergebnisse im Browser.
6. **v5-Datei** unter **„Tests laden“** wählen. Die Suite-ID `agricraft-core-neoforge-1211-20261122-v5` ist bewusst anders als bei v1–v4 und der alten 30er-Suite. **Import ist nicht bestätigt; erst nach Freigabe und Export bisheriger Ergebnisse vornehmen.**

**Windows-PowerShell, erst nach Freigabe und aus dem Hauptmod-Gradle-Projektverzeichnis:** Das bestehende technische Gate beschreibt `& .\gradlew.bat :test :build --no-daemon --console=plain`. Das ist ein **Anleitungstext**, hier wurde nichts ausgeführt. Vorher Java-21-Installation und den tatsächlichen Branch-HEAD prüfen; die korrekte Arbeitsverzeichnis-/Subprojektzuordnung steht im [`MAIN_MOD_TEST_GATE`](../MAIN_MOD_TEST_GATE_2026-11-19.md). Bei Fehlern Log und Testreport sichern und **nicht** die Spieltests als bestanden eintragen.

## Jeden Test gleich durchführen

1. Test-ID lesen, alle genannten Vorbedingungen herstellen. Fehlende Weltkopie, fehlender zweiter Spieler oder fehlendes Test-Datapack: **`blocked`**, nicht `pass`.
2. Schritte in der Reihenfolge durchführen, Ausgangs- und Endzustand fotografieren bzw. zählen. Bei Wahrscheinlichkeiten **gültige Random-Ticks** von erfolgreich entstandenen Kindern trennen.
3. Das Feld **„Erwartet“** mit der tatsächlichen Beobachtung vergleichen. **`pass`** nur bei nachgewiesenem Soll, **`fail`** bei Abweichung, **`open`** bei noch nicht begonnenem Fall.
4. Kommentar im Tracker immer nach diesem Muster ausfüllen: `Commit: …; Tester/Server: …; Ausgangslage: …; Ist: …; Anzahl/Ticks: …; Belege: Screenshot-Datei/latest.log: …`.
5. Bei Itemduplikation, Datenverlust, sichtbaren geheimen Genen oder Crash sofort stoppen, Welt/Log sichern und **`fail`** mit kleinstem Reproduktionsweg erfassen.
6. Nach jedem abgeschlossenen Themenblock **„Ergebnisse exportieren“** benutzen und JSON plus Screenshots/Logs sichern. Der Browser-Status allein ist keine belastbare Sicherung.

## Grenzen der aktuellen v5

- Die 79 Fälle decken TG-/GE-/ER-/UI-01–UI-11-/MP- und BAL-IDs der [`Hauptmod-Prüfliste`](../MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md) ab, einschließlich der sechs GE-12- und neun UI-11-Unterfälle. Die JEI-Anzeige liegt **als ungeprüfter Quellcode** vor. Ein Teil erfordert **separate, technisch vorbereitete Teststände**.
- Die 23 Multiplayerfälle sind aus der vorhandenen [detaillierten Multiplayer-Matrix](../MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) in ausführbare deutsche Prüfschritte übertragen. Für ihre parallelen Aktionen werden zwei Personen beziehungsweise Accounts benötigt.
- `GE-12` und einige Daten-/Blockschutz-Szenarien benötigen eine vorbereitete Weltkopie und kontrollierte Testdaten. Ohne solche Vorbedingungen **`blocked`** dokumentieren, nicht wahllos die produktive Testwelt verändern.
- `UI-11` benötigt ein ausdrücklich freigegebenes JEI-19.x-Profil; `UI-11c/e/g/h` verlangen zusätzliche Datapack-/Serverstände, `UI-11d` zwei Spieler. `UI-11i` verlangt ein sicheres Netzwerk-Harness: ohne solches **blocked**. Die Hauptmod muss auch **ohne JEI** starten (`UI-11f`). [Abdeckungsreview](../SUNDAY_CORE_V5_COVERAGE_AUDIT_2026-11-21.md).
- Bisher fand **weder eine JUnit-/Minecraft-Ausführung noch ein bestätigter Browserimport** dieser Suite statt. Die JSON-Struktur und eindeutige Test-IDs werden lediglich statisch geprüft. Diese Datei ist **nicht** die vollständige technische Vorbereitungsdatei für Manipulationsszenarien; für GE-12 steht die [separate Weltkopie-/Datapack-Matrix](../CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) bereit.

**Versionsregel:** Bei geänderten Tests oder zusätzlichen Fällen eine neue `suiteId` und neue Dateiversion verwenden. Bereits exportierte Resultate bleiben dem alten Suite-Stand zugeordnet.
