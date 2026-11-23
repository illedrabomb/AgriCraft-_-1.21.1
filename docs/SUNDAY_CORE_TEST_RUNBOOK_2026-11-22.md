# AgriCraft-Hauptmod – gemeinsamer Testabend am Sonntag, 22.11.2026

**Status: vorbereitet, nicht ausgeführt.** Dieses Dokument ist eine Bedienreihenfolge, kein Testbeleg und keine Freigabe. **Vor Gradle, JUnit, CI, Minecraft-Start und Multiplayer ausdrücklich den Benutzer um Testfreigabe bitten.** Den dann tatsächlich getesteten Branch-HEAD erst am Testabend eintragen.

**Bevorzugte Webtool-Testsuite für weitere offene Prüfungen:** [`test-tracker/AgriCraft-Core-Regression-2026-11-23-v1.1.json`](test-tracker/AgriCraft-Core-Regression-2026-11-23-v1.1.json) mit **85 offenen Definitionen (83 Core, 2 nachgelagerte Mod-Support-Fälle)**, 23 Multiplayer-Fällen, 31 Kopierbefehlen und der `suiteId` `agricraft-core-neoforge-1211-regression-20261123-v1.1`. Die bereits belegten IDs `UI-01`, `UI-09`, `UI-12`, `UI-13`, `UI-16` und `ER-13` fehlen absichtlich; `ER-12` und `UI-11` bleiben partial/offen. Regression 1.0 bleibt historische Ausgangsliste. **Kein weiterer Import oder Test ist automatisch freigegeben.**

**Historie ausschließlich für alte Ergebnisse:** v1 (16), v2 (51), v3 (68), v4 (69), v5 (79), v6/v7/v8 (je 86) behalten eigene `suiteId` und bleiben im Repository erhalten; Regression 1.0 ist kein stilles Update eines bisherigen Testlaufs. **Vor „Tests laden“ oder „Ergebnisse laden“ den vorhandenen Lauf als ZIP mit Bildern exportieren.** Unterschiedliche `suiteId`/`suiteFingerprint` niemals als direkt vergleichbare Läufe behandeln. Der [v5-Abdeckungsreview](SUNDAY_CORE_V5_COVERAGE_AUDIT_2026-11-21.md) erklärt historische JEI-Sonderfälle, ist aber keine bevorzugte Importdatei.

**Aufbauplan für externe Tester:** [F0–F6-Testwelt-Fixtures mit direkten Anordnungen, 3/4/10/20er-Reihen, Früchten, Papier, Analyzer/Chest, Weltkopien und Stoppsignalen](SUNDAY_CORE_TEST_FIXTURES_2026-11-22.md) vor dem Test lesen. Diese Dokumentation beschreibt nur Vorbereitungen, **keine bereits gebauten Welten oder ausgeführten Prüfungen**.

## 0. Vor dem ersten Befehl (gemeinsam abgleichen)

- [ ] Ausdrückliche Freigabe für den gemeinsamen Test liegt vor; Uhrzeit und Tester notieren.
- [ ] Aktuellen SHA von `integration/neoforge-1211-consolidation` notieren; keine uncommitteten Änderungen oder fremde JARs als geprüften Stand ausgeben.
- [ ] Java 21, Minecraft 1.21.1, NeoForge-Version, Betriebssystem, Modliste und Testwelt-/Backup-Pfad notieren. Für **Core only** keine Pam-, MineColonies- oder Farmer's-Add-on-JARs laden.
- [ ] Vorhandene Spielwelt und Tracker-Export sichern. Riskante NBT-/Datapack-/Blockschutz-Versuche **nur in einer Kopie** durchführen.
- [ ] Einen gemeinsamen Ergebnisort festlegen (Test-Tracker-Export plus Logs/Screenshots). Keine geänderte Testsuite mit altem Export und identischer `suiteId` vermischen; siehe [`MANUAL_TEST_TRACKER.md`](MANUAL_TEST_TRACKER.md).

## 1. Technisches Gate – nur nach Freigabe

| Reihenfolge | Fälle | Aktion und Abbruchbedingung |
| --- | --- | --- |
| 1 | TG-01 | Versionen, SHA und Modliste festhalten. |
| 2 | TG-02, TG-03 | Ausschließlich die Hauptmod-Gradle-Aufgaben `:test` und `:build` gemäß [`MAIN_MOD_TEST_GATE_2026-11-19.md`](MAIN_MOD_TEST_GATE_2026-11-19.md) ausführen. Testanzahl, Ergebnis, Build-JAR und Konsolenausgabe protokollieren. Bei Fehler erst Ursache klären; keinen Ingame-Erfolg unterstellen. |
| 3 | TG-04 | Dedizierten Server und Client mit minimaler Core-Modliste starten. Crash/Registry-/Classloaderfehler sichern und das Gate stoppen. |
| 4 | TG-05 | Save/Restart und `/reload` mit bekannten Seed-Genen; vor manipulativen Versuchen Weltkopie anlegen. |

## 2. Gameplay-Blöcke – zusammenhängend, nicht quer springen

| Block | Fälle der zentralen Liste | Konkreter Schwerpunkt und Soll |
| --- | --- | --- |
| A. Grundpflanzen | GE-01, GE-06, GE-07 | Weizen/Karotte 1/1/1, Wachstumsstufen, Knochenmehl/Boden; einfache/gekreuzte Crop Sticks, genau ein Stick je bestätigtem Wechsel. Schutz-Plugin-Fehlpfad `CR-13` nur falls separat vorbereitet. |
| B. Genetik | GE-02–GE-05, GE-11, GE-12 | Reife direkte Eltern 2/3/4, Ein-Eltern-Klon und 3+-/20er-Reihe; unbekannte Genwerte nicht preisgeben. Artenmutation/Fallback und gedeckelte Kind-Gene nach [GE-12-Detailmatrix](CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) **nur mit vorbereitetem Datapack in Weltkopie** prüfen, sonst als `blocked` notieren. Zufallsversuche mehrfach beobachten und Anzahl/Randbedingungen notieren, keinen einzelnen Fehlschlag als Regelbruch werten. |
| C. Ernte & Früchte | ER-01–ER-06, ER-12–ER-15 | Nur gültige reife Ernte; ein Stack pro unterschiedlichem Produkt; Kürbis/Melone mit **zweitem** Fruchtzyklus. Gain-abhängige Kürbis-/Melonen-Ausbeute, Drop-Multiplikator und Operator-`testseed` getrennt prüfen; HG-01/HG-02 nur in Weltkopie. |
| D. Seeds & Papier | ER-07, ER-08 | Genom/Herkunft vor/nach Pflanzung, Abbau, Rückwandlung. Besonders `CR-12`: **ein Blatt bleibt beim Crafting im Feld**, danach neuer Seed; 16er-Papierstapel und benannte Komponenten netto unverändert; Shift-Klick keine Duplikate. `CR-11` mit fremden Seed-Daten muss die Rückwandlung verweigern. |
| E. Analyzer & Journal | UI-01, UI-03–UI-05, UI-07–UI-14 | Direkte Einlage, 1er-Slotgrenze, 64er-Chest-Stack, Journal-Entdeckungen und Live-Tastenbelegung (Standard F10), Pflanzablehnungen `UI-10`, JEI-/Serverbedingungen `UI-11` mit Unterfällen, Jade/Identify `UI-12`/`UI-13`, Save/Reload und Gen-Datenschutz. Optionale Mods jeweils in getrennten Clientprofilen; `MP-20`, `MP-22` und invasive JEI-Fälle nur mit vorbereiteter Weltkopie. |
| F. Zwei Spieler | UI-02, UI-06 und MP-01–MP-23 | Beide Spieler auf dediziertem Server: konkurrierende Klicks, gleichzeitiges Entfernen von Analyzer/Chest, Privatsphäre, Cursor-/Shift-Klick. Exakte Fälle aus [`MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`](MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) ablesen. Nur vorhandene zwei Accounts/Spieler benutzen, sonst als **blockiert** melden. |

**Zeitplanung:** A–D zuerst, danach E und F. Kein künstliches Zeitlimit und kein Überspringen ungeprüfter Fälle als „bestanden“; bei wenig Zeit Blöcke sauber als offen lassen und später fortsetzen. Die IDs aus der zentralen Prüfliste sind verbindlich; diese Tabelle erzeugt keine neuen Testfälle.

### Reihenfolge innerhalb des Genetikblocks und notwendige Vorbereitung

1. **Zunächst ohne Test-Datapack:** GE-01–GE-04 mit derselben Standard-Serverconfig spielen: echte Elternreife und reinen Ein-Eltern-Klon feststellen; dann 2/3/4 direkte Eltern, kurze Reihe und 20er-Reihe. Für jede Formation Anzahl **gültiger Ziel-Random-Ticks** und entstandene Kinder getrennt erfassen. Keinen einzelnen zufälligen Fehlschlag als Fehler werten.
2. **Grenzwerte ohne Balanceversprechen:** Mit bereits bekannten Seed-Genen prüfen, dass neue Pflanzen nach Artenmutation höchstens `4/4/4` besitzen, dass niedrigere Werte erlaubt sind und dass gleichartige Eltern ihre eigene Art über 5 hinaus weiterentwickeln können. Für nicht natürlich auftretende Situationen `GE-12` nutzen.
3. **Danach nur in einer Kopie mit gesichertem Test-Datapack:** `GE-12a` bis `GE-12f` gemäß [Detailmatrix](CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) durchführen. Mutation zu einer dritten Art **niemals allein aus dem Elternpaar ableiten**; eine tatsächlich passende Datapack-Regel und passende Standortbedingungen sind nötig. Ist diese Vorbereitung nicht vorhanden, `blocked` notieren, keinen regulären Save manipulieren.
4. **BAL-01 – statistische Zuchtziel-Abnahme getrennt:** Vier `1/1/1`-Eltern mit konsequenter Beste-Kinder-Auslese, Zahl der **erfolgreich entstandenen** Kinder bis `10/10/10` dokumentieren. Ein einzelner Zuchtlauf ist **kein Nachweis**, dass der Erwartungswert höchstens 70 beträgt. Für eine belastbare Stichprobe/Simulation ist separate ausdrückliche Freigabe erforderlich; bis dahin `BAL-01` offen lassen.

**Wichtiger Unterschied:** `GE-11` misst die Entstehungschance **pro gültigem Tick**, `BAL-01` die Genentwicklung **pro erfolgreich erzeugtem Kind**. Die separat einstellbaren Config-Werte und die Robustheit nicht als austauschbare Fertilitätswerte behandeln. Vor allem bei vielen Pflanzen können derselbe Seed und dieselbe Welt unterschiedliche Tickzahlen haben.

## 3. Dokumentation pro Fall

Ein Ergebnis enthält mindestens: **Fall-ID, pass/fail/blocked/open, exakte Commit-SHA, Datum/Uhrzeit, Einzel-/Mehrspieler, Modliste, erwartetes und tatsächliches Verhalten, Reproduktionsschritte, Belegpfad**. Für Zufalls-/Wachstumsfälle zusätzlich Anzahl der Versuche, beobachtete Ereignisse und Wartebedingungen. `pass` nur nach beobachtetem Soll; geschriebene JUnit-Quellen allein zählen nicht.

Bei **Itemverlust, Itemduplikation, Genom-/Journal-Verlust, Datenleck oder Crash** sofort den betroffenen Fall stoppen, keine weiteren mutierenden Aktionen an der Testwelt, Save/Log/Tracker-Zustand sichern und den Fehler als `fail` mit minimaler Reproduktion erfassen. Bei fehlender Testumgebung `blocked`, nicht `pass`.

Nach jedem vollständig durchgeführten Block den Tracker **Ergebnisse exportieren**, nicht nur im Browser speichern; Export, Screenshots und `latest.log` separat sichern. Erst nach Sichtung der Nachweise die Häkchen in [`MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md`](MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md) mit Datum und SHA ändern.

## 4. Abschlussregel

Die Hauptmod ist erst abgenommen, wenn das technische Gate und alle vereinbarten Core-Testfälle nachweisbar bestanden oder ausdrücklich als Blocker geklärt sind und der Benutzer die Abnahme bestätigt. **Pam's HarvestCraft 2, MineColonies, Farmer's Delight und Ponder nicht in diesen Testlauf mischen.**
