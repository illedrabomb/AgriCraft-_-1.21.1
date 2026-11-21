# DOC-30/31: optionale F0–F3-Feldvorbereitung per Vanilla-Datapack

**Nur vorbereitet, nicht installiert, nicht ausgeführt, nicht getestet.** Der Minecraft-1.21.1-Datapack-Quellordner liegt unter [`test-fixtures/AgriCraft-Core-Fields-1.21.1/`](test-fixtures/AgriCraft-Core-Fields-1.21.1/). Kein Mod-Code und keine Seed-/NBT-Daten werden verändert.

## Sicherheitsgrenzen

- Der Funktionsaufruf ist **destruktiv für das Gelände**: Er ersetzt in sechs klar umgrenzten Bereichen vorhandene Blöcke (Y der Ausführungsposition bis Y+3 durch Luft; Y−1 durch Ackerboden/Wasser). Ausschließlich in **neuer, entbehrlicher Testwelt mit Backup** verwenden. Nicht in einer bestehenden Save-Welt ausführen.
- Der gesamte Aufbau reicht vom Anker bis **+173 Blöcke in X**, **+8 in Z** sowie **−1 bis +3 in Y**. Das Areal muss vorher frei und passend sein; angrenzende Blöcke, Licht, Biom, Serverkonfiguration und Tickrate werden nicht automatisch angepasst.
- Das Datapack baut **nur Boden und Bewässerung für F0/F1/F2 sowie eine markierte Plattform für F3**. Es setzt keine Crop Sticks, Geräte, genetischen Seeds, reifen Eltern oder erwarteten Ergebnisse. F4/F5, Sonderwelten und Mehrspieler bleiben manuell.
- Diese Quelle ist für einen **später freigegebenen** Weltlauf gedacht. Weder jetzt noch durch das Einchecken wird eine Funktion ausgelöst.

## Nach Freigabe: Durchführung durch eine Testperson

1. Neue entbehrliche Java-1.21.1-Core-Testwelt erstellen und vor dem ersten Bau sichern. Datapack-Ordner `AgriCraft-Core-Fields-1.21.1` in `datapacks` dieser Testwelt kopieren (keine vorhandenen Datapacks überschreiben). Laden/`/reload` ist erst nach ausdrücklicher Minecraft-Testfreigabe erlaubt.
2. Mit Operatorrechten an einem **frei gewählten Ausgangspunkt** auf der späteren Gehfläche stehen. Feste Ankerkoordinaten und Dimension sowie den Commit des getesteten Mod-JARs notieren. Vorher prüfen, dass 174×9 Blöcke nach Osten frei sind und darunter ein tragfähiger Bereich liegt.
3. Erst nach Backup und Freigabe den folgenden Befehl bewusst ausführen:

   ```mcfunction
   /execute at @s run function cropgenetics_test:build_core_fields
   ```

4. Sichtprüfung: F0 bei X+0..12; F1-A bei X+20..28, F1-B +36..44, F1-C +52..60, F1-D +68..76, F1-E +84..110. Das **Zielfeld** liegt jeweils bei relativ `(24|40|56|72|88, 0, 4)`. Die lange Reihe verläuft vom letzten Ziel **ostwärts** auf X+89..108.
5. F2 liegt bei X+120..145: drei unabhängige Ernteversuche für normale Produkte, Melonen und Kürbisse mit dokumentiertem Gain. F3 liegt bei X+155..173: **blau (159,4)** markiert den Analyzer, **grün (160,4)** die direkte Chest; **rot (169,4)** den zweiten Analyzer und **gelb (170,5)** die diagonale Negativkontrolle. Koordinatenpaare sind **X-/Z-Offsets**, Markierungen befinden sich auf Y−1. Die eigentlichen Geräte **von Hand auf die Markierungen** stellen.
6. Jetzt erst die im [F0–F3-Plan](SUNDAY_CORE_TEST_FIXTURES_2026-11-22.md) beschriebenen Crop Sticks, Eltern, Geräte, Gene und Reifestufen manuell herstellen. Reife und gültige Boden-/Lichtbedingungen ausdrücklich prüfen. Die F1-E-Reihe nach 3/4/10/20 Eltern getrennt vermessen; keine zusätzlichen direkten Nachbarn am Ziel zulassen.
7. Screenshots und Koordinaten im Test-Tracker erfassen; **keinen Test als bestanden markieren**, solange das Verhalten nicht wirklich beobachtet wurde. Für neue Versuche Zustände separat sichern bzw. wiederherstellen.

## Einordnung

`pack_format: 48` und der `data/<namespace>/function/`-Pfad sind für Minecraft Java 1.21.1 vorgesehen. Die Funktionssyntax und der echte Terrainaufbau sind **noch nicht durch Minecraft bestätigt**. Misslingt das Laden oder sind die Felder ungeeignet, den Aufbau abbrechen, Ausgangswelt aus Backup zurückholen und den Fehler samt Log dokumentieren. Keine automatische Reparatur eines Saves.
