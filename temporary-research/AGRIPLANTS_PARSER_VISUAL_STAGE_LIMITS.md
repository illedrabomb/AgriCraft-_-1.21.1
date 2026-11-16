# Parser-Grenzen und visuelle Wachstumsstufen
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Quellcodeänderungen, keine Builds, Tests, CI-Ausführungen oder Spielstarts.**

## Datenimport: neue Obergrenzen

Die bisherige `AgriPlantsParser.plant`-Implementierung las beliebig lange JSON-Listen für Mods, Saatgut, Stufen und Ernteprodukte. Solche Dateien waren nicht zwingend syntaktisch ungültig, konnten aber den späteren Reload oder häufige Harvest-Berechnungen belasten.

Der Parser verwirft nun Datensätze mit:
- mehr als **64** `mods`-Einträgen,
- mehr als **64** `seed_items`,
- weniger als **1** oder mehr als **64** `stages`,
- mehr als **64** Ernteprodukten unter `products.products`.

Der unveränderliche `PlantDefinition`-Konstruktor erzwingt die gleichen Listenobergrenzen unabhängig vom Parser und akzeptiert für die Werte innerhalb der Stufenliste nur **0..4096**. Bereits vorhandene Prüfungen für `harvestStage`, Chance-Parameter und Harvest-Produktwerte bleiben erhalten.

Die Obergrenzen sind **konservative Port-Schutzgrenzen**, keine Behauptung über die maximalen Listenlängen des ursprünglichen AgriPlants-Projekts. Ein späterer Massenimport muss inkompatible Sonderfälle ausdrücklich protokollieren und bei begründetem Bedarf die Grenzen gezielt anpassen.

## Client: visuelle Stufenauswahl

Minecraft `CropSticksBlock.STAGE` bietet derzeit acht visuelle Zustände (0..7), während ein genetisches Gewächs im `CropSticksBlockEntity.growthStage` seine exakte serverseitige Wachstumsstufe speichert.

- `CropGrowth.displayStage(age,totalStages)` normalisiert den **serverseitigen** Wachstumsindex auf 0..7.
- `CropSticksRenderer` benutzt diese acht Zustände und fragt `PlantVisuals.Visual.at(stage,8)` nach dem passenden Sprite.
- `Visual.at` begrenzt den angeforderten Stage-Index nun vor dem Frame-Mapping auf die gültige Spanne `0..totalStages-1`. Negative und zu hohe Werte können keine unerwartete Frame-Auswahl bewirken.
- Der `Visual`-Record prüft seine eigene Frame-Struktur: 1..64 Stufen mit jeweils 1..2 Texturebenen, also kein leerer Frame und keine leere Frame-Liste.
- Ressourcenpack-Sprites mit mehr als acht Stufen werden weiterhin **auf acht sichtbare Blockzustände quantisiert**. Es gibt dabei keine direkte 1:1-Abbildung zu 16 oder 32 Bildern. Wer diese Präzision benötigt, braucht künftig eine synchronisierte exakte Wachstumsstufe für den Renderer und ein darauf abgestimmtes Client-Schema.

## Verbleibende Aufgaben
- Das historische AgriPlants-`stages`-Array enthält numerische Stufenwerte, deren Bedeutungen je nach Originalpaket variieren können. Der aktuelle Port verwendet **Anzahl und Index**, nicht die genauen numerischen Schwellenwerte, für seine Wachstumslogik.
- Die zweite Gourd-Texturlage bleibt für das physische Fruchtmodell außerhalb des bisherigen Quad-Renderers; separate Gourd-Blöcke werden als Weltblöcke gerendert.
- Die Prüfung von übergroßen **JSON-Dateien insgesamt** ist noch keine Dateigrößen-/Streamingbegrenzung. Die JSON-Datei muss bereits von Gson eingelesen werden, bevor Listengrenzen geprüft werden können.
- Sämtliche Korrekturen sind **nicht kompiliert und nicht ingame geprüft**; Builds/Tests erfolgen erst bei ausdrücklicher Freigabe.
