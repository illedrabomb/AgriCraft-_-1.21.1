# AgriCraft-Hauptmod – gemeinsamer Testabend am Sonntag, 22.11.2026

**Status: vorbereitet, nicht ausgeführt.** Dieses Dokument ist eine Bedienreihenfolge, kein Testbeleg und keine Freigabe. **Vor Gradle, JUnit, CI, Minecraft-Start und Multiplayer ausdrücklich den Benutzer um Testfreigabe bitten.** Den dann tatsächlich getesteten Branch-HEAD erst am Testabend eintragen.

**Webtool-Testsuite, Etappe 1:** [`test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v1.json`](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v1.json) enthält **16 noch offene Fälle** (`TG-01` bis `TG-05`, `GE-01` bis `GE-04`, `GE-11`, `GE-12`, `ER-07`, `ER-08`, `UI-01`, `UI-08`, `UI-09`) für den [Mod-Test-Tracker](MANUAL_TEST_TRACKER.md). Diese Suite ist **neu**, ersetzt die ältere 30er-Suite nicht rückwirkend und hat eine andere `suiteId`. **Vor „Tests laden“ alte Ergebnisse exportieren:** Das Laden einer Suite löscht den bisherigen Tracker-Arbeitsstand. Fehlende Fälle in Etappe 1 sind **nicht bestanden**, sondern noch nicht in dieser Suite erfasst; die zentrale Hauptmod-Prüfliste bleibt vollständig verbindlich. Spätere Ergänzungen erhalten wegen des inhaltsgebundenen Ergebnisimports eine eigene Suite-Version/`suiteId`.

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
| C. Ernte & Früchte | ER-01–ER-06 | Nur gültige reife Ernte; ein Stack pro unterschiedlichem Produkt; Kürbis/Melone mit **zweitem** Fruchtzyklus. HG-01/HG-02 nur in Weltkopie. |
| D. Seeds & Papier | ER-07, ER-08 | Genom/Herkunft vor/nach Pflanzung, Abbau, Rückwandlung. Besonders `CR-12`: **ein Blatt bleibt beim Crafting im Feld**, danach neuer Seed; 16er-Papierstapel und benannte Komponenten netto unverändert; Shift-Klick keine Duplikate. `CR-11` mit fremden Seed-Daten muss die Rückwandlung verweigern. |
| E. Analyzer & Journal | UI-01, UI-03–UI-05, UI-07–UI-09 | Direkte Einlage, 1er-Slotgrenze, 64er-Chest-Stack, Journal-Entdeckungen, JEI DE/EN, Save/Reload, Anzeige-/Datenschutz. `MP-20`, `MP-22` mit Datapack-/NBT-Vorbereitung nur in Weltkopie. |
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
