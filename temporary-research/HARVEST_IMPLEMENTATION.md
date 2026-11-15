# Erntesystem: Implementierungsstand
Stand: 2026-11-15 | Branch `feature/neoforge-1211-foundation` | Keine Tests/Builds auf Nutzerwunsch.

## Konkret geschrieben
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropHarvest.java`: berechnet Ernteprodukte anhand der AgriPlants-`PlantDefinition.harvestProducts` und des genetischen `Gain`-Werts; verwendet wie das Original `(gain + 3) / 3` Produktdurchläufe. Jeder definierte Drop respektiert `chance`, `min` und `max`. Mod-/Vanilla-Item-IDs werden über `BuiltInRegistries.ITEM` aufgelöst, unbekannte IDs werden übersprungen. Größere Mengen werden in ItemStacks unterteilt.
- `CropSticksBlock.useWithoutItem`: reife, eingepflanzte Pflanzen können über Standard-Rechtsklick/Leerhand geerntet werden; Server erzeugt Items in der Welt.
- `CropSticksBlockEntity.finishHarvest`: Genom bleibt stehen, Wachstumsstadium wird auf `harvestStage` aus der Pflanzendefinition zurückgesetzt und synchronisiert. Falls die Definition als „nach Ernte“ die letzte Wachstumsstufe nennt, wird sicherheitshalber Stufe 0 gewählt, um unbegrenztes sofortiges Wiederernten zu vermeiden.
- `CropSticksBlock.getDrops`: beim normalen Blockabbau werden ein/zwei Crop Sticks, ein genetischer Samen mit dem vollständigen Genom sowie bei reifer Pflanze die normalen Ernteprodukte mit derselben Gain-Logik gesammelt.
- `PlantDefinition`: überprüft nun endliche Wahrscheinlichkeiten und beschränkt die maximale Menge pro Ernteproduktdefinition auf 4096.

## Absichtliche Designentscheidungen
- Gleiche Gain-Formel für normale Ernte **und** Zerstörung (anders als die ältere AgriCraft-Implementierung, die bei Blockbruch nur einmal `getHarvestProducts` aufruft).
- Nach der normalen Ernte wird **kein zusätzlicher genetischer Samen ausgegeben**; der Samen bleibt als Pflanze erhalten. Genetisches Saatgut wird beim normalen Blockbruch zurückgegeben.
- Ernte beim Blockbruch ist destruktiv; Rücksetzen des Stadiums geschieht nur beim Ernten ohne Abbau.
- Einziges bisher unterstütztes Produktschema: einfache `type:item`-Definitionen; Spezialfälle erfordern Adapter.

## Unbestätigte technische Details / wichtige nächste Arbeit
- **Nicht kompiliert, nicht getestet und kein Client-/Server-Start durchgeführt** (ausdrücklicher Nutzerwunsch); Minecraft-API-Signaturen und Loot-Hooks daher nicht verifiziert. Vor Veröffentlichung prüfen, ob `LootParams.Builder.getLevel` und `Item.getDefaultMaxStackSize` exakt in der gewählten 1.21.1-Version verfügbar sind.
- `useWithoutItem`/Item-Interaktionspriorität im NeoForge 1.21.1-Client noch zu validieren. Ein Seed/Stab in der Hand hat Vorrang; sonst sollte der Standard-Rechtsklick die Ernte auslösen.
- Ernte-Daten berücksichtigen bislang keine komplexen `required`-Produkte, NBT/Data Components, Mod-Drop-Events, Pflanzen-Callbacks, saisonale Bedingungen, Fortune/Silk Touch, sonstige Werkzeug- und Bodenmechaniken. Das kann nicht als vollständige Fremdmod-Kompatibilität gelten.
- Keine visuelle Art-Unterscheidung der Pflanzen; nur Platzhaltermodelle.
- Direkter Block-Loot statt Loot-Table verlangt besondere Sorgfalt für Explosionen, Creative und Spezialabbau; noch nicht geprüft.
- Vor produktivem Nutzen ungültige gespeicherte Wachstumsstufen bei Datapack-Wechsel begrenzen; die BE hält bisher die bestehende Stufe.
- Nachweis der Performance/Fehlerfreiheit und korrekter Mod-Registrierungen offen.

## Nächster Entwicklungsschritt
1. Kreuzstäbe: Eltern aus vier Nachbarn, Speziesmutationen nach `MutationDefinition`, genetische Nachkommen über `BreedingEngine`.
2. Daten-/Speziesvalidierung bei Kreuzung und Wachstum (fehlende/elternlose PlantDefinition, SeedItems).
3. Sichere Vanilla-/Mod-Samenkonvertierung mit Datenkomponenten; dann Soil-Regeln und besondere Ernteadapter.
4. Erst bei erneutem Nutzerwunsch Builds/Tests durchführen. GitHub-CI bleibt manuell.

## Herkunft
Weiterhin Fork von AgriCraft; ursprüngliche Autoren, Lizenz und Datenquellen anerkennen. Die ursprüngliche 1.18.2-Codebasis bleibt unangetastet.
