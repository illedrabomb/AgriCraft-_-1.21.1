# AgriCraft-Hauptmod – gemeinsamer Testabend am Sonntag, 22.11.2026

**Status: vorbereitet, nicht ausgeführt.** Dieses Dokument ist eine Bedienreihenfolge, kein Testbeleg und keine Freigabe. **Vor Gradle, JUnit, CI, Minecraft-Start und Multiplayer ausdrücklich den Benutzer um Testfreigabe bitten.** Den dann tatsächlich getesteten Branch-HEAD erst am Testabend eintragen.

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
| B. Genetik | GE-02–GE-05 | Reife direkte Eltern 2/3/4, Ein-Eltern-Klon und 3+-Reihe; unbekannte Genwerte nicht preisgeben. Zufallsversuche mehrfach beobachten und Anzahl/Randbedingungen notieren, keinen einzelnen Fehlschlag als Regelbruch werten. |
| C. Ernte & Früchte | ER-01–ER-06 | Nur gültige reife Ernte; ein Stack pro unterschiedlichem Produkt; Kürbis/Melone mit **zweitem** Fruchtzyklus. HG-01/HG-02 nur in Weltkopie. |
| D. Seeds & Papier | ER-07, ER-08 | Genom/Herkunft vor/nach Pflanzung, Abbau, Rückwandlung. Besonders `CR-12`: **ein Blatt bleibt beim Crafting im Feld**, danach neuer Seed; 16er-Papierstapel und benannte Komponenten netto unverändert; Shift-Klick keine Duplikate. `CR-11` mit fremden Seed-Daten muss die Rückwandlung verweigern. |
| E. Analyzer & Journal | UI-01, UI-03–UI-05, UI-07–UI-09 | Direkte Einlage, 1er-Slotgrenze, 64er-Chest-Stack, Journal-Entdeckungen, JEI DE/EN, Save/Reload, Anzeige-/Datenschutz. `MP-20`, `MP-22` mit Datapack-/NBT-Vorbereitung nur in Weltkopie. |
| F. Zwei Spieler | UI-02, UI-06 und MP-01–MP-23 | Beide Spieler auf dediziertem Server: konkurrierende Klicks, gleichzeitiges Entfernen von Analyzer/Chest, Privatsphäre, Cursor-/Shift-Klick. Exakte Fälle aus [`MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`](MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) ablesen. Nur vorhandene zwei Accounts/Spieler benutzen, sonst als **blockiert** melden. |

**Zeitplanung:** A–D zuerst, danach E und F. Kein künstliches Zeitlimit und kein Überspringen ungeprüfter Fälle als „bestanden“; bei wenig Zeit Blöcke sauber als offen lassen und später fortsetzen. Die IDs aus der zentralen Prüfliste sind verbindlich; diese Tabelle erzeugt keine neuen Testfälle.

## 3. Dokumentation pro Fall

Ein Ergebnis enthält mindestens: **Fall-ID, pass/fail/blocked/open, exakte Commit-SHA, Datum/Uhrzeit, Einzel-/Mehrspieler, Modliste, erwartetes und tatsächliches Verhalten, Reproduktionsschritte, Belegpfad**. Für Zufalls-/Wachstumsfälle zusätzlich Anzahl der Versuche, beobachtete Ereignisse und Wartebedingungen. `pass` nur nach beobachtetem Soll; geschriebene JUnit-Quellen allein zählen nicht.

Bei **Itemverlust, Itemduplikation, Genom-/Journal-Verlust, Datenleck oder Crash** sofort den betroffenen Fall stoppen, keine weiteren mutierenden Aktionen an der Testwelt, Save/Log/Tracker-Zustand sichern und den Fehler als `fail` mit minimaler Reproduktion erfassen. Bei fehlender Testumgebung `blocked`, nicht `pass`.

Nach jedem vollständig durchgeführten Block den Tracker **Ergebnisse exportieren**, nicht nur im Browser speichern; Export, Screenshots und `latest.log` separat sichern. Erst nach Sichtung der Nachweise die Häkchen in [`MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md`](MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md) mit Datum und SHA ändern.

## 4. Abschlussregel

Die Hauptmod ist erst abgenommen, wenn das technische Gate und alle vereinbarten Core-Testfälle nachweisbar bestanden oder ausdrücklich als Blocker geklärt sind und der Benutzer die Abnahme bestätigt. **Pam's HarvestCraft 2, MineColonies, Farmer's Delight und Ponder nicht in diesen Testlauf mischen.**
