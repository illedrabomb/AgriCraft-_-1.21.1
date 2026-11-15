# Crop Sticks – Implementierungsstand (vor Tests)
Stand: 2026-11-15. Branch: `feature/neoforge-1211-foundation`.

## Implementiert
- Neuer `CropSticksBlock` mit den Zuständen `CROSS` und `PLANTED`, nur auf Farmland platzierbar.
- Leere, einfache Pflanzenstäbe können durch einen zweiten Stab zu Kreuzstäben werden.
- Auf einfachen Stäben lässt sich ein `GeneticSeedItem` mit bestehender `cropgenetics:genome`-Data-Component einpflanzen.
- `CropSticksBlockEntity` speichert genetische Informationen über den vorhandenen `GenomeCodecs.GENOME`-Codec in BlockEntity-NBT und synchronisiert Änderungen per BlockEntity-Datenpaket.
- `getDrops` liefert beim normalen Lootpfad die Stäbe (1 oder 2) und ggf. ein genetisches Saatgut mit dem gespeicherten Genom.
- Primitive statische Modelle für einfache Stäbe, Doppelstäbe und den Zustand 'bepflanzt'; Modell ist Platzhalter, keine Art-spezifischen Pflanzenmodelle.
- Das Item erscheint im Kreativinventar; deutsche und englische Lokalisierung erweitert.

## Wichtige Grenzen
- **Kein neuer Build, kein Spieltest, keine Tests ausgeführt, wie vom Nutzer gewünscht.** Auch die Signaturen des Block-/BlockEntity-Codes sind noch nicht durch Kompilierung validiert.
- Pflanzenwachstum, Ernte, Mutationslogik und Nachbar-Erkennung fehlen. Es ist derzeit nur die Basis für das tatsächliche Pflanzen.
- **Noch keine Aufnahme normaler Vanilla-/Mod-Samen in Crop Sticks**, nur unser eigenes genetisches Saatgut.
- Für das Bepflanzen wird noch nicht überprüft, ob die Pflanzenart in der `AgriPlantsRegistry` registriert ist oder spezielle Bodenbedingungen erfüllt; unbedingt vor finaler Gameplay-Nutzung ergänzen.
- Die neuen Blockmodelle sind funktionale Platzhalter; ohne Renderer kann die angezeigte Pflanze nicht korrekt nach Art und Wachstumsstufe aussehen.
- BlockState `planted` ist redundant zur BlockEntity und braucht bei Desynchronisierung/Save-Migration konsistente Korrektur.
- Blockbruch über den normalen Lootpfad erhält das Genom; Creative/Explosionen und Sonder-Abbaumethoden müssen noch auf Duplikation oder Verlust geprüft werden.
- Der Block funktioniert ohne InfinityLib, AgriCore und AgriPlants als Runtime-Abhängigkeit.

## Folgende Arbeit
1. Zugriff auf geladene `PlantDefinition` aus dem CropSticks-Gameplay schaffen.
2. Serverseitige Wachstumsstufen/Tick-Logik und Pflanzenspezies-Visualisierung.
3. Nachbar-Crops auf Cross-Sticks erkennen, `BreedingEngine` und Mutationsregeln integrieren; Start-Saatgut/Herkunft der Mutationen validieren.
4. Ernten korrekt an Definitionen und Spezial-Crop-Adaptern orientieren; Drops nicht duplizieren.
5. Vanilla-/Mod-Samen sicher in genetische Seeds überführen.
6. Sobald Nutzer wieder testen möchte: Gradle-Build, Client- und Server-Start sowie Spieltests.
