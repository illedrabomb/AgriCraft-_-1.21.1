# Migration der Wachstumsstufen beim Chunk-Laden
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Nur Quellcodeänderungen und statische Prüfung. Keine Builds, Tests, CI-Läufe oder Spielstarts.**

## Ausgangslage

Der Port hatte bereits exakte `GrowthStage`-Werte und den neuen NBT-Wert `TotalGrowthStages` in der `CropSticksBlockEntity`. Ältere Welten besaßen `TotalGrowthStages` noch nicht, weshalb `loadAdditional` die Zahl auf 0 setzte. Ohne weiteres Wachstum blieb dieser Legacy-Fallback dauerhaft aktiv: der Renderer zeigte nur die visuelle 0..7-Skala, auch wenn das vorhandene `GrowthStage`-Feld einen höheren exakten Wert enthielt.

## Implementierte Reparatur

- `CropSticksBlockEntity.onLoad()` ist als NeoForge-BlockEntity-Lifecycle-Hook überschrieben, ruft `super.onLoad()` auf und handelt **nur auf Serverseite**, wenn ein Genome gespeichert ist.
- Der Hook sucht die aktuelle `PlantDefinition` anhand der im Genome gespeicherten Pflanzenart. Fehlt die Art, bleibt die BlockEntity einschließlich ihrer Genetik und Wachstumsdaten unangetastet.
- Ist die Art vorhanden, berechnet `reconcileLoadedStage(plant)` die korrekte Gesamtzahl an Stufen aus `plant.stages().size()` und begrenzt einen außerhalb der gültigen Altersspanne liegenden gespeicherten `growthStage`-Index auf `0..(stages - 1)`.
- Nur bei tatsächlich unterschiedlicher `TotalGrowthStages`-Zahl oder ungültiger Altersposition wird `syncState(plant)` aufgerufen. Die Methode markiert den Chunk als geändert und nutzt die schon vorhandene BlockEntity-Update-Synchronisierung, auch wenn sich die normalisierte achtstufige BlockState-Anzeige nicht verändert.
- Ein `CropSticksBlock.randomTick` ruft die gleiche Reparatur vor Wachstumsbedingungen und Frucht-Reconciliation erneut auf. Damit können Pflanzen repariert werden, deren Chunk **vor der Verfügbarkeit eines aktivierten Pflanzenkatalogs** geladen wurde. Diese Wiederholung verändert das normale Wachstumsalter nicht, wenn alle Werte bereits gültig sind.

## Auswirkungen

- Alte genetische Pflanzen mit `GrowthStage` und ohne `TotalGrowthStages` erhalten nach einem Server-Chunk-Ladeereignis die korrekte Anzahl der aktuellen Pflanzenstufen. Ihre genaue Altersstufe bleibt erhalten, solange sie innerhalb der erlaubten Spanne liegt.
- Ein übergroßer NBT-Wert wird auf die letzte gültige Stufe begrenzt, statt dauerhaft ein fehlerhaftes Wachstumsverhalten zu verursachen.
- Verändert ein Datapack die Stufenzahl einer bestehenden Pflanzenart, wird gespeichertes Alter an den aktuellen Katalog angepasst, ohne Genome oder Allele neu zu generieren. Dies ist **Index-Clamping**, keine Zeit- oder Prozentkonvertierung: Ein Wechsel von 16 auf 8 Stufen kann ein zuvor teilweise gewachsenes Gewächs als reif erscheinen lassen.
- Ein nicht geladenes oder deaktiviertes Gewächs behält seine Originaldaten; der nächste Tick nach einer späteren erfolgreichen Datapack-Reaktivierung kann die Metadaten aktualisieren.
- Ein `onLoad`-Hook kann unter NeoForge in generierten bzw. erneut geladenen Chunks ausgelöst werden. Die Änderung ist idempotent und benötigt keine neue Tickerschleife.

## API-Belege

- NeoForge 1.21.1 `IBlockEntityExtension#onLoad`: https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/neoforged/neoforge/common/extensions/iblockentityextension
- NeoForge 1.21.1 BlockEntity-Update: `getUpdateTag`, `getUpdatePacket` und `Level#sendBlockUpdated`: https://docs.neoforged.net/docs/1.21.1/blockentities/

## Weiterhin offen

- Nicht kompiliert oder ingame geprüft. Insbesondere `onLoad`-Reihenfolge, `setBlock` während Chunk-Aktivierung und Paket-Auslieferung bei unverändertem normalisiertem BlockState müssen im Spiel erprobt werden, sobald Tests ausdrücklich erlaubt sind.
- Direkte, sofortige Migration **aller bereits geladenen Chunks bei /reload** ist nicht enthalten. Dort erfolgt die Aktualisierung beim nächsten zufälligen Pflanzentick. Ein Server-Neustart bzw. Chunk-Unload/-Reload löst den `onLoad`-Pfad aus.
- Auch beim Datapack-Reaktivieren zuvor fehlender Arten erfolgt die Reparatur beim nächsten Tick, nicht durch einen globalen Welt-Scan.
- Der Client bekommt weiterhin die genaue Anzahl der Stufen, aber keine vollständige serverseitige Pflanzen-/Texturdefinition.
