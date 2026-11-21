# AgriCraft-Hauptmod – Testdateien für externe Tester

**Tracker-Oberfläche:** Stand 21.11.2026 ist die externe App [Mod-Test-Tracker v2.3](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft). Maßgeblich für Import und Export ist deren [JSON_FORMAT.md](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/docs/JSON_FORMAT.md). Die neue v7-Testsuite nutzt `schemaVersion: 2`, `category` und eine kurze `summary` je Test. Die ältere v6 (`schemaVersion: 1`) bleibt importierbar, zeigt eingeklappt aber noch keine Erklärung an.

## Verbindliche Namen für Testdateien

Dateinamen nur mit ASCII-Buchstaben, Ziffern und Bindestrichen, ohne Leerzeichen. `YYYY-MM-DD` ist bei Suiten das **geplante Testdatum**, bei Ergebnissen das **tatsächliche Ausführungsdatum**. `NN` ist eine zweistellige, pro Tag und Testbuild aufsteigende Durchlaufnummer; `SHA8` sind die ersten acht Zeichen des **tatsächlich getesteten Git-Commits** (nicht des Vorbereitungscommits).

| Art | Muster | Beispiel |
| --- | --- | --- |
| Unveränderliche Testsuite | `AgriCraft-Core-Sonntag-YYYY-MM-DD-vN.json` | `AgriCraft-Core-Sonntag-2026-11-22-v7.json` |
| Vollständiges Testergebnis inklusive Bildern | `AgriCraft-Core-Ergebnis-YYYY-MM-DD-Lauf-NN-SHA8.zip` | `AgriCraft-Core-Ergebnis-2026-11-22-Lauf-01-1234abcd.zip` |
| JSON-Ergebnis ohne Bilddaten (nur optional) | `AgriCraft-Core-Ergebnis-YYYY-MM-DD-Lauf-NN-SHA8.json` | `AgriCraft-Core-Ergebnis-2026-11-22-Lauf-01-1234abcd.json` |
| Zusatzbeleg | `AgriCraft-Core-YYYY-MM-DD-Lauf-NN-TESTID-Kurzbeschreibung.ext` | `AgriCraft-Core-2026-11-22-Lauf-01-UI-14-Tastenbelegung.png` |

Die `SHA8`-Beispiele sind **Platzhalter**, keine getesteten Builds. Bei unbekanntem Git-Stand `SHA8` durch `unbekannt` ersetzen und im Bericht als Blocker erfassen; keine SHA erfinden. Eine neue Suite-Version `vN` und neue `suiteId` nur bei geänderten Testdefinitionen; alte Dateien und Exporte nicht umbenennen oder überschreiben. Der **Dateiname allein** belegt weder JAR-Herkunft noch Testerfolg: vollständige Git-SHA, JAR-Name und SHA-256 im Tracker-Kommentar/technischen Bericht notieren. Core und Add-ons bekommen getrennte Dateinamen und Testläufe.

## Welche Datei laden?

- **Für den Test bevorzugt:** [`AgriCraft-Core-Sonntag-2026-11-22-v7.json`](AgriCraft-Core-Sonntag-2026-11-22-v7.json) – **86 deutschsprachige Testfälle**, alle mit kurzer Erklärung für eingeklappte Karten. Bestehende IDs, Testschritte, Erwartungen und Commands aus v6 sind unverändert. Alle Fälle offen, kein Import/Lauf bestätigt.
- [`AgriCraft-Core-Sonntag-2026-11-22-v6.json`](AgriCraft-Core-Sonntag-2026-11-22-v6.json) – 86 Fälle inklusive `ER-12`–`ER-15`, `UI-12` (Jade), `UI-13` (Identify) und `UI-14` (HUD-Taste/Journal); historische Ausgangssuite für eigene ältere Exporte.
- [`AgriCraft-Core-Sonntag-2026-11-22-v5.json`](AgriCraft-Core-Sonntag-2026-11-22-v5.json) – 79 Fälle inklusive `UI-10`, `UI-11` und neun JEI-/Sync-Unterfällen; historisch unverändert.
- [`AgriCraft-Core-Sonntag-2026-11-22-v4.json`](AgriCraft-Core-Sonntag-2026-11-22-v4.json) – 69 Fälle, für bisherige Ergebnisse unverändert erhalten.
- [`AgriCraft-Core-Sonntag-2026-11-22-v3.json`](AgriCraft-Core-Sonntag-2026-11-22-v3.json) – 68 Fälle, historisch unverändert für alte Ergebnisimporte.
- [`AgriCraft-Core-Sonntag-2026-11-22-v2.json`](AgriCraft-Core-Sonntag-2026-11-22-v2.json) – Zwischenstand mit 51 Fällen, wegen anderer `suiteId` nicht mit v3-Ergebnissen mischen.
- [`AgriCraft-Core-Sonntag-2026-11-22-v1.json`](AgriCraft-Core-Sonntag-2026-11-22-v1.json) – historisch vorbereitete erste Etappe mit 16 Fällen, nicht mit v2-Ergebnissen mischen.
- Die alte 30-Fälle-Datei im getrennten Tracker-Repository ist eine andere Suite und enthält historische Buildangaben. Nicht als aktuellen Testnachweis behandeln.

## Vorbereitungen

**Jade/Identify:** `UI-12` und `UI-13` sind **getrennte Clientprofile** innerhalb der Hauptmod-Kompatibilitätsabnahme, keine Pflichtmods und keine Freigabe für andere AgriCraft-Add-ons. Erst Core ohne HUD-Mod prüfen, dann Jade und Identify jeweils einzeln hinzufügen. Die Tests unterscheiden „gemeinsam lauffähig“ von „zusätzliche AgriCraft-Informationen im fremden Overlay vorhanden“. Identify besitzt nach dem bisherigen Playtest keine bestätigte Crop-Genetics-Integration; fehlende Zusatztexte sind kein Nachweis eines neu eingeführten Crashs. Private/unanalysierte Gene dürfen dennoch nie angezeigt werden.

**ST-47:** Das kleine eigenständige AgriCraft-Art-/Stufen-Overlay wird durch die bloße Installation von Jade oder WTHIT nicht mehr abgeschaltet; Identify hatte diesen Ausschluss ohnehin nicht. Eine spezielle Jade-/WTHIT-/Identify-Integration ist damit **nicht** implementiert. Beim Test nach überlappenden HUD-Elementen schauen und Screenshot/GUI-Skalierung dokumentieren.

**ST-48/49:** Standardtaste **F10** (statt ursprünglich H) schaltet nur die eigene AgriCraft-Art-/Stufenzeile ein/aus, solange kein Menü geöffnet ist. Die Belegung ist in **Optionen → Steuerung** änderbar; nach einem Neustart beginnt das HUD wieder sichtbar. Das Saatgut-Journal enthält eine vierte Anleitungsseite (DE/EN). `UI-14` prüft Taste, Menüschutz, Umbelegung und die optionale parallele Anzeige mit Jade/Identify. Das ist keine Steuerung der HUDs fremder Mods.

**ST-50:** Auf der vierten Journal-Seite steht **die gerade tatsächlich belegte Taste**. Durch Klick auf den Belegungs-Knopf und anschließenden Tastendruck wird sie direkt geändert; **Esc** bricht ab, **Delete/Backspace** entfernt die Belegung. Die Änderung verwendet Minecrafts eigene Tastenoptionen und soll beim nächsten Start erhalten bleiben. Der Text „Standard F10“ ist nur die Ausgangsvorgabe, nicht die aktuelle Anzeige. `UI-14` prüft den Abgleich mit dem Steuerungsmenü.

**Gain-Testdaten (ST-42, nur nach Testfreigabe):** Ein Spieler mit Operatorrechten und einem freien Inventarslot erhält über `/cropgenetics testseed minecraft:pumpkin_plant 10` einen nicht analysierten genetischen Kürbissamen mit `Growth 1 / Gain 10 / Strength 1`; für Melonen `minecraft:melon_plant`, für andere aktive Arten die genaue Arten-ID verwenden. Gain nur 1–10, keine freie NBT-Eingabe. Die Art muss im geladenen Datapack vorhanden sein. Serverkonsole, normale Spieler, unbekannte Arten und volles Inventar erhalten keine Samen. **Dieser Befehl ist eine Testhilfe, kein Nachweis, dass Pflanzenreife oder Drop-Balance funktionieren.** Die erzeugten Samen sind echte Items und können im Weltstand gespeichert werden: deshalb ausschließlich in Testweltkopien verwenden. ER-12 bis ER-15 verwenden die Hilfe und prüfen ihre Zugriffsgrenzen.

**Alle drei Gene (ST-43):** `/cropgenetics testseed minecraft:pumpkin_plant 10 5 3` erzeugt `Growth 10 / Gain 5 / Strength 3`. Reihenfolge immer **Growth Gain Strength**, jeder Wert 1–10. Die Ein-Zahl-Kurzform `/cropgenetics testseed minecraft:pumpkin_plant 5` bleibt als **Gain 5** mit Growth/Strength 1 erhalten. Genau zwei Zahlen sind ungültig; beide Befehlsformen bleiben Operator-only. `ER-13` nutzt die Vollform, `ER-15` prüft die Kompatibilität und Grenzfälle. Kein Test wurde ausgeführt.

Für die konkrete Gestaltung der Testflächen zuerst die [F0–F5-Aufbauanleitung im AgriCraft-Repository](../SUNDAY_CORE_TEST_FIXTURES_2026-11-22.md) öffnen. Sie trennt sichere Gameplay-Tests von Weltkopien mit NBT-/Datapack-/Blockschutz-Versuchen. **Keine dieser Flächen ist bereits gebaut oder getestet.**

1. **Ohne ausdrückliche Freigabe des Projektbesitzers weder Gradle/JUnit noch Minecraft starten.** Die JSON-Datei selbst ist nur eine Anleitung.
2. Minecraft 1.21.1, Java 21, passende NeoForge-Version, dedizierten Testserver und die für den jeweiligen Fall benötigten ein bis zwei Testspieler bereithalten. **Nur AgriCraft-Hauptmod**, keine Pam-/MineColonies-/Farmer's-Add-ons.
3. Branch `integration/neoforge-1211-consolidation` und **genaue Git-Commit-ID des tatsächlich gebauten JARs** dokumentieren. Nicht den Vorbereitungscommit als bereits getesteten Build ausgeben.
4. Eine neue Welt oder gesicherte **Weltkopie** verwenden. Datapack-/NBT-Manipulation, Blockschutz, gleichzeitiger Blockabbau und andere riskante Szenarien **niemals am einzigen Save** durchführen.
5. [Mod-Test-Tracker](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft) lokal öffnen. **Vor „Tests laden“ oder „Ergebnisse laden“ den aktiven Lauf über „ZIP mit Bildern exportieren“ extern sichern** – Imports ersetzen den aktuellen Bearbeitungsstand. Die lokale Testlauf-Historie ist kein externes Backup.
6. **v7-Datei** unter **„Tests laden“** wählen. Ihre `suiteId` lautet `agricraft-core-neoforge-1211-20261122-v7`; alte v6-Ergebnisexporte gehören weiterhin zur v6-Suite und dürfen nicht als v7-Ergebnisse importiert werden. **Import ist nicht bestätigt; erst nach Freigabe und Export bisheriger Ergebnisse vornehmen.**

**Windows-PowerShell, erst nach Freigabe und aus dem Hauptmod-Gradle-Projektverzeichnis:** Das bestehende technische Gate beschreibt `& .\gradlew.bat :test :build --no-daemon --console=plain`. Das ist ein **Anleitungstext**, hier wurde nichts ausgeführt. Vorher Java-21-Installation und den tatsächlichen Branch-HEAD prüfen; die korrekte Arbeitsverzeichnis-/Subprojektzuordnung steht im [`MAIN_MOD_TEST_GATE`](../MAIN_MOD_TEST_GATE_2026-11-19.md). Bei Fehlern Log und Testreport sichern und **nicht** die Spieltests als bestanden eintragen.

## Jeden Test gleich durchführen

1. Test-ID lesen, alle genannten Vorbedingungen herstellen. Fehlende Weltkopie, fehlender zweiter Spieler oder fehlendes Test-Datapack: **`blocked`**, nicht `pass`.
2. Schritte in der Reihenfolge durchführen, Ausgangs- und Endzustand fotografieren bzw. zählen. Bei Wahrscheinlichkeiten **gültige Random-Ticks** von erfolgreich entstandenen Kindern trennen.
3. Das Feld **„Erwartet“** mit der tatsächlichen Beobachtung vergleichen. **`pass`** nur bei nachgewiesenem Soll, **`fail`** bei Abweichung, **`open`** bei noch nicht begonnenem Fall.
4. Kommentar im Tracker immer nach diesem Muster ausfüllen: `Commit: …; Tester/Server: …; Ausgangslage: …; Ist: …; Anzahl/Ticks: …; Belege: Screenshot-Datei/latest.log: …`.
5. Bei Itemduplikation, Datenverlust, sichtbaren geheimen Genen oder Crash sofort stoppen, Welt/Log sichern und **`fail`** mit kleinstem Reproduktionsweg erfassen.
6. Screenshots direkt beim aufgeklappten Test über das Screenshot-Feld mit **Strg+V** oder per Bilddatei hinzufügen und optional beschriften. Nach jedem Themenblock **„ZIP mit Bildern exportieren“** benutzen, mit dem oben festgelegten Namen sichern und Logs/Crash-Reports daneben ablegen. Ein bloßer JSON-Export enthält **nur Bildverweise, keine Bilddaten**. Zum Fortsetzen **„Ergebnisse laden“** mit dem vom Tracker erzeugten ZIP wählen: `suite.json` wird mitgeladen. Für reines Ergebnis-JSON vorher dieselbe Testsuite über **„Tests laden“** öffnen.

**Mehrere Builds:** Oben **„＋ Durchlauf hinzufügen“**, **„Umbenennen“** und **„Zum Durchlauf wechseln“** verwenden; vor einem Neubeginn **„Durchlauf sichern“** und zusätzlich ZIP exportieren. **„Neuen Durchlauf beginnen“** erzeugt leere Ergebnisse für dieselbe Suite. Der Build-Vergleich ist nur für identische `suiteId` **und** identischen `suiteFingerprint` aussagekräftig; geänderte Testsuiten nicht als direkte Regression vergleichen. Screenshots bleiben lokal im Browser, bis sie als ZIP gesichert werden. **Keine automatische Testausführung** durch die Webseite.

## Grenzen der aktuellen v7 (mit historischen v5-Spezialfällen)

- Die 79 Fälle decken TG-/GE-/ER-/UI-01–UI-11-/MP- und BAL-IDs der [`Hauptmod-Prüfliste`](../MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md) ab, einschließlich der sechs GE-12- und neun UI-11-Unterfälle. Die JEI-Anzeige liegt **als ungeprüfter Quellcode** vor. Ein Teil erfordert **separate, technisch vorbereitete Teststände**.
- Die 23 Multiplayerfälle sind aus der vorhandenen [detaillierten Multiplayer-Matrix](../MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) in ausführbare deutsche Prüfschritte übertragen. Für ihre parallelen Aktionen werden zwei Personen beziehungsweise Accounts benötigt.
- `GE-12` und einige Daten-/Blockschutz-Szenarien benötigen eine vorbereitete Weltkopie und kontrollierte Testdaten. Ohne solche Vorbedingungen **`blocked`** dokumentieren, nicht wahllos die produktive Testwelt verändern.
- `UI-11` benötigt ein ausdrücklich freigegebenes JEI-19.x-Profil; `UI-11c/e/g/h` verlangen zusätzliche Datapack-/Serverstände, `UI-11d` zwei Spieler. `UI-11i` verlangt ein sicheres Netzwerk-Harness: ohne solches **blocked**. Die Hauptmod muss auch **ohne JEI** starten (`UI-11f`). [Abdeckungsreview](../SUNDAY_CORE_V5_COVERAGE_AUDIT_2026-11-21.md).
- Bisher fand **weder eine JUnit-/Minecraft-Ausführung noch ein bestätigter Browserimport** dieser Suite statt. Die JSON-Struktur und eindeutige Test-IDs werden lediglich statisch geprüft. Diese Datei ist **nicht** die vollständige technische Vorbereitungsdatei für Manipulationsszenarien; für GE-12 steht die [separate Weltkopie-/Datapack-Matrix](../CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) bereit.

**Versionsregel:** Bei geänderten Tests oder zusätzlichen Fällen eine neue `suiteId` und neue Dateiversion verwenden. Bereits exportierte Resultate bleiben dem alten Suite-Stand zugeordnet.
