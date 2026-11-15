# Exakte Wachstumsstufe im Client-Renderer
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Ziel
Der Port speichert bis zu 64 Wachstumsstufen pro Pflanze im BlockEntity, nutzte zum Rendern bisher jedoch nur `CropSticksBlock.STAGE` (0..7). Bei Pflanzen mit mehr als acht Wachstumsstufen verloren dadurch viele unterschiedliche Alterswerte ihre eigenständigen Texturframes.

## Implementierte Änderungen
- `CropSticksBlockEntity` stellt zusätzlich zum existierenden `growthStage` die Zahl `totalGrowthStages` bereit. Sie wird beim Pflanzen, beim Annehmen eines Kreuzungsnachkommens und in `syncState(plant)` aus `plant.stages().size()` übernommen.
- `saveAdditional` speichert die Zahl unter NBT `TotalGrowthStages` (neben `GrowthStage`). `loadAdditional` liest sie mit Gültigkeitsbereich 1..64.
- Die bereits bestehenden BlockEntity-Mechanismen `getUpdateTag`, `getUpdatePacket` und `syncState` mit `setChanged`/`sendBlockUpdated` übertragen dadurch **auch** die exakte Wachstumsstufe und die Gesamtzahl an Clients. Es wird kein eigenes Netzwerkpaket eingeführt.
- `CropSticksRenderer` verwendet `crop.growthStage()` und `crop.totalGrowthStages()` für die Auswahl von `PlantVisuals.Visual.at(stage,totalStages)` und die Pflanzenhöhe. Der BlockState bleibt achtstufig für Vanilla-Blockmodelle und Legacy-Kompatibilität.
- Falls `TotalGrowthStages` in einem alten Save fehlt, wird `0` als Kennzeichen gespeichert und der Renderer fällt auf die bisherige `BlockState.STAGE`-Skala von acht Stufen zurück. Eine weitere Spielzustandsänderung über `syncState(plant)` setzt die korrekte Stufenzahl.

## Beispiel
Eine Pflanze mit 16 Wachstumsstufen und 16 Clienttexturframes kann nun für die exakten Indizes 0..15 ihre entsprechenden 16 Texturen wählen, **sofern der Client diese Texturen als Ressourcen besitzt**. Der BlockState unterscheidet weiterhin nur die acht Modellstufen 0..7, was für die Quad-Darstellung nicht mehr relevant ist.

Die Clienttexturen werden weiterhin aus dem lokalen Resource-Pack geladen, nicht aus serverseitigen Datapacks gesendet. Stimmt die Anzahl der visuellen Frames nicht mit den serverseitigen Wachstumsstufen überein, verteilt `Visual.at` die vorhandenen Frames proportional über die genaue Alters-/Stufenanzahl.

## Grenzen
- Die Synchronisation wird über die bestehende BlockEntity-NBT-Aktualisierung durchgeführt; tatsächliche Paket-Zustellung und Neuzeichnen bei unverändertem BlockState müssen **im Spiel** geprüft werden.
- Ältere gespeicherte Pflanzen erhalten bis zur nächsten synchronisierten Serveränderung den bisherigen achtstufigen Renderer-Fallback. Es erfolgt hier keine invasive On-Load-Migration.
- Die Server-Definition gibt nur die **Anzahl** der Stufen an den Client weiter, nicht die numerischen AgriPlants-Stufenschwellen oder die Datapack-Definition selbst.
- Gourd-Früchte bleiben physische Weltblöcke; das vorhandene angehängte Vanilla-Stem-Modell wird weiterhin über den BlockState gesteuert.
- **Kein Build, keine Tests, keine CI-Ausführung und kein Spielstart.** Die Quellcodeänderung ist committed, aber noch nicht praktisch bestätigt.

## Nächste Schritte
Bei späterer ausdrücklicher Genehmigung: Compile, Multiplayer-Chunk-Neueintritt, Pflanze mit 16/32 Texturframes, Wachstum innerhalb einer unveränderten BlockState-Stufe sowie Speichern/Neuladen alter Save-Daten gezielt prüfen.

## Ergänzung: BlockState-Konsistenz bei älteren Chunks (2026-11-15)

Die inzwischen vorhandene Migration über `onLoad()`, `reconcileLoadedStage()` und den periodischen `BlockEntityTicker` repariert fehlende `TotalGrowthStages`-Daten. Die Prüfung `stageMetadataMatches(plant)` wurde zusätzlich erweitert: Sie vergleicht nun `PLANTED`, den auf 0..7 normalisierten `STAGE` und `SPECIES` mit dem gespeicherten Genom und der exakten Stufe. Dadurch werden auch alte oder inkonsistente BlockStates korrigiert, obwohl die beiden NBT-Stufenwerte bereits gültig sind. `reconcileLoadedStage` ruft dafür dieselbe bestehende `syncState(plant)`-Routine auf; keine neuen Pakete und keine Änderung an der genetischen Wachstumslogik.

**Nicht ausgeführt:** Build, Tests, CI oder Minecraft. Die korrekte Aktualisierung beim Chunk-Neuladen ist weiterhin ingame zu verifizieren.
