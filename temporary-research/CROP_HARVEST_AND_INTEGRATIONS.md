# Analyse: Crop Sticks, Wachstum, Ernte und Mod-Integration
**Stand:** 2026-11-15. **Status:** Quelltextprüfung, kein NeoForge-Build und keine Laufzeittests.

## Nachgewiesene Quellstellen
Alle Java-Pfade relativ zu `src/main/java/com/infinityraider/agricraft/`:

- `content/core/BlockCrop.java` – ca. 979 Zeilen; Block-Interaktionen, Crop-Stick-Zustände, `randomTick`, Loot/Blockdrops.
- `content/core/TileEntityCrop.java` – ca. 778 Zeilen; Genom, Wachstumslogik, Kreuzungen, Ernte, Bodenbedingungen, altes InfinityLib-Syncing und Forge-Events.
- `content/core/ItemDynamicAgriSeed.java` – AgriCraft-Samen, noch über ItemStack-NBT (`setTag`) serialisiert; Forge- und InfinityLib-Schnittstellen.
- `util/CropHelper.java` – Ertrags-Logik in `executePlantHarvestRolls`.
- `api/v1/plant/IAgriPlant.java` – API für Pflanzenart, Wachstumsbedingungen, Ernte-, Scher- und Klonprodukte, Erntezustand.
- `impl/v1/crop/IncrementalGrowthLogic.java` – wiederverwendbare, stufenbasierte Wachstumsidee.
- `handler/VanillaSeedConversionHandler.java` und `plugins/minecraft/SeedWrapper.java` – vorhandene Umwandlung und Erkennung registrierter Saatgüter.

## Nachvollziehbarer Datenfluss (heutiger 1.18.2-Stand)
1. Ein kompatibler `ItemStack` wird über `AgriApi.getGenomeAdapterizer()` in ein `IAgriGenome` aufgelöst (`SeedWrapper` sucht eine registrierte Pflanze mit `isSeedItem(stack)`).
2. In `BlockCrop.runRightClickLogic` wird `crop.plantGenome(...)` aufgerufen, der Saatgutstack verbraucht und der Blockzustand angepasst. Die separate `VanillaSeedConversionHandler`-Route kann das Einpflanzen gewöhnlicher Samen abfangen.
3. `BlockCrop.randomTick` ruft `IAgriCrop.applyGrowthTick` auf; in `TileEntityCrop` verzweigt das entweder zu Weeds, Kreuzung auf Doppelstäben oder regulärem Wachstum mit Boden-/Wachstumsbedingungen.
4. Reguläres Wachstum verwendet `calculateGrowthRate()`: `soilFactor * (plantGrowthChanceBase + growthStat * plantGrowthChanceBonus * configGrowthMultiplier)`, gefolgt von RNG, Growth-Events und dem nächsten `IAgriGrowthStage`.
5. Kreuzung auf Doppelstäben delegiert an `AgriMutationHandler`/`AgriCrossBreedEngine`, deren Allel-/Klon-Regeln separat in `GENETICS_DEEP_DIVE.md` dokumentiert sind.
6. Ernte via `TileEntityCrop.harvest`: nach Berechtigungs-/Eventprüfung `CropHelper.executePlantHarvestRolls`, Wachstumszustand auf `plant.getGrowthStageAfterHarvest()` setzen, `plant.onHarvest`.
7. `CropHelper.executePlantHarvestRolls`: Schleife mit **(gain + 3) / 3** Aufrufen von `plant.getHarvestProducts(consumer, growthStage, stats, random)` (Ganzzahldivision). Für Gain 1 bis 10 sind das 1 bis 4 Aufrufe – die konkreten Itemmengen bleiben von der Pflanzenimplementierung abhängig.
8. Blockzerstörung in `BlockCrop.getDrops` ist ein **anderer Pfad**: ggf. Stäbe, dann **einmal** `getHarvestProducts` (nicht derselbe Gain-Loop) sowie ein genetischer Samen, wenn `growthStage.canDropSeed()` gilt. Das kann bei unterschiedlichen Erntemethoden zu unterschiedlichem Ertrag führen und benötigt bewusste Design-/Balancingtests.

## Wichtige Erkenntnisse für Modpack-Kompatibilität
- **Wiederverwendbares Design:** Die Pflanzendefinition ist selbst für die Ernteprodukte verantwortlich, die Genetik regelt die Anzahl von Ernteversuchen. Damit können passende fremde Items ausgegeben werden, ohne die Item-IDs zu ersetzen.
- **Kein universeller Automatismus nachgewiesen:** `SeedWrapper` erkennt aktuell nur Samen zu *bereits registrierten* `IAgriPlant`-Definitionen. Erkennung allein bedeutet nicht, dass jeder Mod-Pflanzenblock mit Spezialböden, Wachstum, Ernte-NBT, Multi-Block-Form oder Ressourcen-Logik korrekt funktioniert.
- Zwingend erforderlich: `CropAdapter`-Schnittstelle je Pflanzenfamilie, Registry-/Datapack-Definition pro Pflanze, explizite Integrationen für exotische Spezialpflanzen; automatische Aufnahme nur für sichere Standardfälle.
- Für fremde Drops sollten die tatsächlichen `ItemStack`-/Component-Daten und Speziallootbedingungen erhalten bleiben; eine simple `itemId -> dropId`-Map genügt nicht.
- Für Farmer's Delight, Mystical Agriculture usw. **keine konkrete NeoForge-1.21.1-Kompatibilität behaupten**, bevor passende Mod-Versionen und Ernteszenarien getestet sind.

## Ressourcen und Weltgeneration
Git-Tree enthält echte Dorfstruktur-Dateien:
- `src/main/resources/data/agricraft/structures/village/plains/greenhouse.nbt`
- `src/main/resources/data/agricraft/structures/village/desert/greenhouse.nbt`
- `src/main/resources/data/agricraft/structures/village/savanna/greenhouse.nbt`
- `src/main/resources/data/agricraft/structures/village/snowy/greenhouse.nbt`
- `src/main/resources/data/agricraft/structures/village/taiga/greenhouse.nbt`
- zusätzliche `greenhouse_irrigated.nbt`-Varianten für einige Biome.
Die vorhandenen Gebäude können als **Anhaltspunkt** für das geplante Züchterhaus dienen. NBT-Strukturen, moderne Jigsaw-/Pool-Registrierung und enthaltene Block-IDs sind noch zu prüfen. Ein vorhandenes `greenhouse.nbt` beweist nicht, dass das neue Züchter-Villager-/Handelssystem schon implementiert ist. Bewässerungsblöcke müssen im Hauptmod vermieden werden, falls Bewässerung als separates Addon geplant ist.

## Technologie-/Abhängigkeitsrisiko
- `BlockCrop` leitet von InfinityLib `BlockBaseTile` ab und nutzt `InfProperty`/`IFluidLoggable`; eine direkte NeuForge-Portierung ist nicht nur eine Änderung der Imports.
- `TileEntityCrop` erbt InfinityLib `TileEntityBase` mit `AutoSyncedField`; Forge `LazyOptional`, `ModelDataMap` und `MinecraftForge.EVENT_BUS` erfordern Umstellung.
- `ItemDynamicAgriSeed` nutzt legacy ItemStack NBT; moderne NeoForge-Datenkomponenten und serialisierte ItemStack-Snapshots getrennt gestalten.
- `IAgriPlant` ist ein gutes Portierungs-Interface, könnte aber als kleine, reine Java-Domain und separate Minecraft-Adapter-Schnittstelle sinnvoller sein.

## Konkrete Testfälle für nächsten Coding-Schritt
1. Seed-Erkennung mit registriertem Vanilla-Seed, nicht registriertem Item, bereits genetischem Samen und ausgeschlossener Saat.
2. Beim Pflanzen wird exakt ein Seed (nicht in Creative) verbraucht und ein Genom samt Spezies gespeichert.
3. Cross-Sticks und einfache Stäbe haben getrennte Tick-Pfade; ohne verfügbare Eltern kein neues Genom.
4. Gene-Growth 1 vs. 10 beeinflusst Wachstumswahrscheinlichkeit; SoilFactor wirkt korrekt; ungültige Bedingungen blockieren Wachstum.
5. `Gain` 1/3/4/6/7/9/10 liefert anhand der Altformel 1/2/2/3/3/4/4 Harvest-Aufrufe; Drops hängen zusätzlich von Pflanzenregeln ab.
6. Rechtsklick-Ernte vs. Blockbruch getrennt testen und gewünschte Gleichheit/Unterschiede ausdrücklich entscheiden.
7. Nach Ernte kann der gewählte Pflanzenadapter den Stage zurücksetzen; keine Duplikation über mehrere Events.
8. Fremde Spezialpflanze liefert eigene Mod-Drops einschließlich Komponenten, ohne Mod-seitige Sonderbedingungen zu umgehen.
9. Vorhandene Village-NBTs mit aktuellen 1.21.1-Blöcken/Strukturformaten prüfen; Dorfgebäude werden nur generiert, wenn die Struktursysteme sie auch tatsächlich registrieren.
10. Dedicated-Server-Smoke-Test auf `21.1.265` gegen Client-only-Referenzen.

## Aufgaben / noch nicht geprüft
- `IAgriPlant`-Implementierungen für die Ernte von Fremdmods lokalisieren und klassifizieren.
- AgriPlants-Submodule und JSON-Mutations- / Soil-Definitions-Schema tatsächlich abrufen.
- InfinityLib/AgriCore-Submodule rekursiv auf direkt verwendete Abhängigkeiten auditieren.
- Die im Code gefundenen `greenhouse.nbt`-Ressourcen unter 1.21.1 inspizieren.
- Entscheiden, ob Genetik bei Break-Harvest und Rightclick-Harvest dieselbe Ertragsmultiplikation verwenden soll.
