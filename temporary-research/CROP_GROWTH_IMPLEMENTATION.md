# Serverseitiges Wachstum: Implementierungsstand
Stand: 2026-11-15. Branch: `feature/neoforge-1211-foundation`.

## Tatsächlich geschriebener Code
- `CropGeneticsMod.plants()` gibt Zugriff auf genau die Registry-Instanz der bestehenden Datapack-Reload-Listener.
- `CropGrowth.chance()` berechnet die im Original angelegte Grundformel `baseGrowthChance + growthGenValue * growthBonus` und begrenzt den Wert auf 0..1. Ein Soil-Faktor und konfigurierbarer Multiplikator fehlen.
- `CropSticksBlock`: Zustand `stage=0..7`; auf dem Server wird bei Random-Ticks eine registrierte PlantDefinition über `genome.plantId()` aufgelöst und ggf. eine Wachstumsstufe erhöht. Kreuzstäbe wachsen noch nicht, weil Kreuzungsmechanik fehlt.
- `CropSticksBlockEntity`: verwaltet `growthStage` als exakte interne Stufe (nicht nur 0..7) zusätzlich zur Genome-NBT-Persistierung. `plant()` validiert eine registrierte Pflanzen-ID, setzt das Stadium auf 0 und synchronisiert den BlockState. `advanceGrowth()` begrenzt gegen die letzte Stufe.
- `blockstates/crop_sticks.json` hat vollständige Varianten für `cross`, `planted` und `stage`; drei zusätzliche primitive Platzhaltermodelle simulieren zunehmende Pflanzenhöhe.

## Einschränkungen
- **Kein Build und kein Test dieser Änderung durchgeführt – ausdrücklicher Nutzerwunsch.** Diese Änderungen gelten als implementierter, aber unvalidierter Quellcode, nicht als nachgewiesen funktionierend.
- Die Wachstumsmodelle sind Platzhalter aus Oak-Leaves/Planks und noch nicht an Arten/Texturen gebunden.
- Nur genetische Samen werden eingesetzt; gewöhnliche Mod-/Vanilla-Samen-Konvertierung folgt später.
- Ernte, Gain-Ertragssteigerung, Soil-Regeln, Wachstumsausnahmen, saisonale Regeln, echte Speziesmutationen und Nachbar-Kreuzungen fehlen.
- Bei Datapack-Reload kann sich die Zahl der Stufen ändern; gespeicherte `growthStage`-Werte müssen neu begrenzt oder migriert werden.
- `BlockState.PLANTED` und BlockEntity-Genom könnten nach älteren Saves inkonsistent sein; Reparatur beim Laden ist noch offen.
- Die Render-/Block-Signaturen und das Verhalten auf Dedicated Server müssen geprüft werden, sobald Tests wieder erwünscht sind.

## Nächster Schritt
- Serverseitige Ernte reifer Pflanzen anhand `PlantDefinition.harvestProducts` und Genetikwert `Gain`, anschließend auf `harvestStage` zurücksetzen.
- Danach Kreuzungsmechanik: Eltern aus 4 Nachbarn, Fertility-Filter, Mutationsregel-Registry, Vererbung in `BreedingEngine`.
- Nutzerwunsch berücksichtigen: keine automatischen Tests oder Builds auslösen; GitHub-Workflow bleibt manuell.
