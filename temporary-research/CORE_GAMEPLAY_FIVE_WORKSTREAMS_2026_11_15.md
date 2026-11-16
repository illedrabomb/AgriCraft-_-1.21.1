# Fünf zusammenhängende Gameplay-Bereiche – Fortschritt
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Auf ausdrücklichen Wunsch wurden keine Builds, Tests, GitHub Actions, Minecraft-Starts oder sonstigen Testläufe ausgeführt.** Sämtliche Aussagen beziehen sich nur auf Quellcodeänderungen und statische Durchsicht.

## 1. Bereits geladene Chunks nach Datapack-Reload

`CropSticksBlock` registriert nun einen **serverseitigen BlockEntityTicker**. Alle 100 Spielticks pro Position (mittels `(gameTime + pos.asLong()) % 100` auf verschiedene Ticks verteilt) vergleicht er das gespeicherte `TotalGrowthStages` und `GrowthStage` mit der derzeitigen `PlantDefinition`. Bei Abweichung wird über die bereits vorhandene idempotente `reconcileLoadedStage(plant)`-Methode normalisiert und synchronisiert.

Diese Prüfung wirkt auch in **bereits geladenen Chunks** und bei `randomTickSpeed=0`. Die bisherigen Reparaturwege `onLoad` und `randomTick` bleiben bestehen. Pflanzen mit momentan fehlender Art bleiben samt Genetik unangetastet.

Die zusätzlichen Tick-Aufrufe verursachen bei sehr vielen Pflanzen einen stetigen, wenn auch gedrosselten Overhead. Diese Performance muss vor einem Release in einer großen Welt gemessen werden.

Die eingesetzte API entspricht dem dokumentierten `EntityBlock#getTicker` plus `BaseEntityBlock.createTickerHelper` von NeoForge. Sie wurde aber nicht kompiliert: https://docs.neoforged.net/docs/1.20.6/blockentities/ und https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/world/level/block/baseentityblock

## 2. Wachstum und Ernte

`CropSticksBlockEntity.stageMetadataMatches(plant)` ist eine kleine, reine Konsistenzprüfung. Beim normalen **Rechtsklick-Ernten** aktualisiert der Server zuerst veraltete Wachstumsmetadaten und prüft erst dann die Ernteberechtigung.

`readyForHarvest` bleibt ausdrücklich **ohne Seiteneffekte**, da es auch während der Erzeugung von Blockdrops aufgerufen wird. Die in `CropHarvest` bestehenden Formeln, Gourd-Sonderfälle und Loot-Policies wurden nicht umgestellt.

## 3. Kreuzungen und Mutationen

`CrossBreeding.chooseChildSpecies` prüft jetzt die aktuell gültigen Bodenbedingungen des Kreuzungsziels, bevor eine mögliche Mutation ausgewählt wird. Mutationen für Arten, die am Zielboden nicht wachsen können, werden vor dem Zufallsentscheid herausgefiltert.

Ohne erfolgreiche Mutation wird eine Elternart bevorzugt, die auf dem Zielboden überhaupt wachsen kann. Die bestehenden genetischen Vererbungsregeln, Fertilitätsfilter und Endprüfung mit Licht/Strength bleiben erhalten. Eine Kreuzung kann weiterhin wegen schlechter Beleuchtung oder Genetik fehlschlagen; hier werden keine garantierten Erfolge behauptet.

## 4. Pflanzendarstellung

`PlantVisuals` deaktiviert jetzt **alle doppelten visuellen Definitionen derselben Pflanzen-ID**, statt willkürlich die zuerst eingelesene zu behalten. Für diese Pflanzen wird der bereits vorhandene, explizite Fallback `block/oak_leaves` verwendet.

Damit sind Client-Resource-Pack-Konflikte deterministisch. Die bereits zuvor implementierte exakte Wachstumsstufendarstellung und das Client-Ressourcen-Snapshot-Verhalten bleiben erhalten.

Weder fehlende Blockatlas-Sprites noch die Anzeige aller Frames auf einem tatsächlichen Client wurden hier nachgewiesen.

## 5. Melonen- und Kürbisfrüchte

`GeneticGourdFruitBlockEntity.bind` liefert nun `boolean` und lässt ausschließlich einen tatsächlich geladenen, horizontal direkt benachbarten CropSticks-Stängel mit vorhandenem Genome als Parent zu. `GourdFruits.tryPlace` betrachtet die Bindung nur bei Erfolg als gültig; bei einer fehlgeschlagenen Bindung entfernt die bestehende Rollback-Logik den gerade gesetzten Fruchtblock ohne Beute.

UUID-Besitz, direktionale Zuordnung, Block-Loot-Delegation und bisherige Nachreconciliation wurden nicht aufgeweicht.

## Wichtige offene Arbeiten

- Keine Kompilierung, kein Gameplay-Test, keine Performance-Messung.
- Der neue Ticker muss in einer großen Pflanzenfarm auf CPU-/Tick-Auswirkungen geprüft werden.
- Melonen-/Kürbis-Loot mit Silk Touch und Fortune wurde nicht im Spiel verifiziert.
- Clienttexturen sind aus lokalen Resource-Packs, nicht aus dem Server-Datapack synchronisiert.
- Ein allgemeiner, direkt auf `/reload` reagierender globaler Chunk-Scan wurde bewusst **nicht** implementiert, um unbeabsichtigte Chunkloads zu vermeiden. Der verteilte Ticker gleicht geladene Pflanzen stattdessen innerhalb seiner Prüfintervalle ab.
- Keine der fünf Baustellen kann ohne Ausführung von Build und Ingame-Szenarien als vollständig abgenommen gelten.
