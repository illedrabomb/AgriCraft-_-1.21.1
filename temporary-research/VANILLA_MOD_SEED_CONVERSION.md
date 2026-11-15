# Standard-Samen direkt in Crop Sticks – Implementierung
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Ergebnis
- `AgriPlantsRegistry` baut beim Laden der `PlantDefinition`-Map zusätzlich ein **rückwärtsgerichtetes Item-ID -> Pflanzen-ID-Verzeichnis**.
- Registry-Snapshot für Pflanzen und Saatgut-Index wird zusammen veröffentlicht, damit der Index nach einem Datapack-Reload nicht zur alten Pflanzenliste gehört.
- Wenn **zwei verschiedene Pflanzenarten dieselbe Saatgut-ID** beanspruchen, wird diese ID nicht automatisch zugeordnet; entsprechende Konflikte werden von `PlantReloadListener` geloggt. Es gibt keine stillschweigende zufällige Auswahl.
- `CropSticksBlock.useItemOn`: Auf einem einfachen, leeren Crop Stick kann ein normales Seed-Item platziert werden, sofern dessen registrierte Item-ID einer eindeutigen Pflanze entspricht und der ItemStack **keine modifizierten Data Components** enthält.
- Ein normales Item wird über `Genome.basic(plantId)` in einen Setzling mit 1/1/1-Startwerten und internen Startwerten umgesetzt. Im Survival wird genau ein Saatgut verbraucht; im Kreativmodus nicht.
- Das auf dem Crop Stick gespeicherte Genom hat danach denselben Persistenz-/Wachstums-/Ernte-/Kreuzungspfad wie ein genetisches Saatgut-Item.
- Unser eigenes `GeneticSeedItem` wird **zuerst separat verarbeitet**; seine bestehenden Allele werden nicht überschrieben, ungültige genetische Saaten werden nicht per Seed-ID neu erstellt.
- Saatgut-IDs in `PlantDefinition` werden auf gültige Namespaced-ID-Form geprüft.

## Aktuelle Vanilla-Beispiele aus dem Datapack
- `minecraft:wheat_seeds` -> `minecraft:wheat_plant`
- `minecraft:carrot` -> `minecraft:carrot_plant`
- `minecraft:potato` -> `minecraft:potato_plant`

## Modpack-Kompatibilität: wichtige Grenzen
- Normale Saaten aus Fremdmods funktionieren **nur wenn** die zugehörige Pflanzendefinition geladen ist, eindeutige `seed_items` enthält und das Item tatsächlich im Registry-System existiert.
- Keine universelle automatische Erkennung sämtlicher Mods. Die alten AgriPlants-Dateien sind noch nicht vollständig nach `data/<namespace>/cropgenetics/plants` portiert.
- Alle Saaten mit zusätzlicher Component-Patch (z.B. NBT-/Custom-Name-/modifizierte Daten) werden **bewusst nicht automatisch konvertiert**, damit keine Informationen verloren gehen. Solche Saaten und Tags brauchen später definierte Spezial-Adapter.
- Der Parser akzeptiert weiterhin nur einfache Item-Saaten (keine Tag-Saatgruppen, komplexe NBT-Bedingungen, Spezial-Crops oder Overrides).
- Nur direkter Rechtsklick auf bereits platzierte Pflanzenstäbe: **kein Inventar-Konvertierungsrezept**. Beim Abbau können genetische Samen mit den geerbten Daten zurückgegeben werden.
- Im Tooltip der **normalen Vanilla-Saaten** erscheinen vor dem Pflanzen keine Genwerte; der Zustand entsteht erst beim Einpflanzen.
- Noch keine Boden-/Licht-/Saison-Prüfungen, keine Crop-spezifischen Unterschiede bei Pflanzenmodellen.
- Interaktionsreihenfolge zwischen fremden Item-Overrides und `Block.useItemOn` ist **noch nicht in Client/Server verifiziert**.

## Stand und nächste Arbeit
- Keine Tests, kein Build, kein Spielstart – bewusst auf Wunsch des Nutzers. **Der Code ist nur geschrieben, nicht als funktionierend nachgewiesen.**
- Nächstes sinnvolles Feature: Boden- und Lichtanforderungen pro Pflanze einschließlich Kompatibilitäts-Adapter für Mod-Soil/Tag-Daten, bevor massenhaft AgriPlants-Definitionen importiert werden.
- Für Modpacks später optionale Konvertierungs-/Override-APIs bereitstellen; doppelte IDs und fremde Komponenten dürfen nicht unbemerkt zerstört werden.
- Vor Release: gezielte Build- und Dedicated-Server-Validierung, inklusive Data-Component-Erhalt und Abbau-Drops.
