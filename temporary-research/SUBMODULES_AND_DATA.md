# Submodule-Analyse: AgriCore, InfinityLib, AgriPlants
Stand 2026-11-15 | Forschungsnotiz; noch kein Build oder Test auf Minecraft 1.21.1 / NeoForge 21.1.265.

## Analysierte festgepinnte Submodule
Aus `master` (Git-Tree, Mode 160000):
- `lib/AgriCore` -> `AgriCraft/AgriCore` @ `5601754f0ecdc3dacb00a9576b53166bb0a4f217`: 139 Tree-Einträge, 87 Java-Dateien (inkl. Tests).
- `lib/InfinityLib` -> `InfinityRaider/InfinityLib` @ `7bec1c3ded874453bd0dff63962015d9240042a4`: 296 Tree-Einträge, 215 Java-Dateien.
- `lib/AgriPlants` -> `AgriCraft/AgriPlants` @ `9444e75828f66bd40adabb4aeca2544765408f8a`: 901 Tree-Einträge, keine Java-Dateien. Anzahl ist nicht gleich Anzahl Pflanzen.

## AgriCore (Code geprüft)
- `src/main/java/com/agricraft/agricore/templates/AgriPlant.java` ist eine umfangreiche DTO/Pflanzendefinition mit ID, `mods`, `seed_items`, Wachstumsstufen, `harvestStage`, `growth_chance`, `growth_bonus`, Produkttabellen, Boden-/Umweltbedingungen, Rendering und Struktur-Generierungsregeln.
- `src/main/java/com/agricraft/agricore/json/AgriLoader.java` lädt JSON aus Dateiverzeichnissen über Gson/Dateisystem, prüft erforderliche Mods, unterstützt versionsabhängige Migration und optionales Zurückschreiben/Backups.
- Empfehlung: **Datenformat und Validierungs-/Versionsidee übernehmen**, aber für NeoForge 1.21.1 Ressourcen über Server-Datapack-/Reload-Mechanismen und belastbare Codices/Validierung verfügbar machen. Rückwärtskompatibilität mit AgriPlants mit Fixture-Tests beweisen; kein direktes 1:1-Compile versprochen.
- Weitere Kandidaten noch zu prüfen: `templates/AgriProductList.java`, `AgriSeed.java`, `AgriRequirement.java`, `registry/AgriPlants.java`, `registry/AgriMutations.java`, `json/AgriFileWalker.java`.

## AgriPlants (zwei echte Beispieldateien geprüft)
- `minecraft/plants/carrot_plant.json`: `version: "1.18.2"`, `id: "minecraft:carrot_plant"`, Vanilla-Karotte als Saat-Item `minecraft:carrot`, 8 Wachstumshöhen, `harvestStage: 4`, `growth_chance: 0.75`, `growth_bonus: 0.025`, Klonen erlaubt, Harvest-Produkt `minecraft:carrot` min 1 / max 4, Boden-/Lichtbedingungen und `structure_generation` mit Dorfgewächshäusern.
- `minecraft/mutations/carrot_mutation.json`: `child: "minecraft:carrot_plant"`, Eltern `minecraft:wheat_plant` und `minecraft:potato_plant`, `chance: 0.75`, keine Zusatzbedingungen.
- Ressourcen sind **sehr wertvoll** für Migration und Test-fixtures. Nicht blind als 1.21.1-kompatibel annehmen: Block-/Item-IDs, Rendering, Saison-Mods, Biome, JSON-Format und Erntebedingungen prüfen.
- Aus den 901 Tree-Einträgen keine pauschale Zahl unterstützter Mods oder Pflanzen ableiten. Nächster Schritt: tatsächliche JSON-Pfade nach Modnamespace und Typ zählen, fehlende Registry-IDs herausfiltern, aktuelle Modversionen prüfen.

## InfinityLib (Code geprüft)
- `src/main/java/com/infinityraider/infinitylib/InfinityMod.java` übernimmt Mod-Lifecycle, Config, Proxy, Registrierung, Netzwerk, Events, Client-Renderer.
- AgriCraft erbt in `src/main/java/com/infinityraider/agricraft/AgriCraft.java` von `InfinityMod`; `BlockCrop` erbt `BlockBaseTile`, `TileEntityCrop` erbt `TileEntityBase`, inklusive synchronisierter Felder.
- Vollständiger InfinityLib-Port nicht empfohlen: einzelne verwendete Funktionen in NeoForge-Standard-Registrierung, Konfiguration, Events, Packet-Sync und moderne BlockEntity-Datenmodelle umsetzen.
- **Offen**: exakter Umfang der InfinityLib-Imports/Vererbung über alle AgriCraft-Javaklassen, plus welche Funktionen sich nicht trivial ersetzen lassen. Ohne Import-Matrix keine verlässliche Aufwandsschätzung.

## Zielarchitektur (Empfehlung, kein implementierter Code)
- Plattformunabhängige Domain für `Genome`, `GenePair`, `CrossBreeding`, `MutationRule`, `PlantDefinition`, `HarvestProduct`.
- NeoForge-Anbindung für `CropStickBlock` / BlockEntity, Seed-Data-Components, Netzwerk und Datapack-Reload.
- `PlantAdapter` für Standardpflanzen und explizite Spezialfälle, unbekannte Pflanzen nicht ungeprüft integrieren.
- Separate optional installierbare Addons: Bewässerung, Gewächshaus und MineColonies.
- Ursprung AgriCraft/AgriCore/AgriPlants und MIT-/sonstige beteiligte Lizenzen vor Materialübernahme prüfen und Credits bewahren.

## Unmittelbare nächste Arbeit
1. Imports auf InfinityLib in allen Hauptmod-Quellen zählen und nach Paket/System aufteilen.
2. AgriPlants-Dateiliste nach Typ/Namespace auszählen (Tree) und 3-5 konkrete Mod-Beispieldaten auf alte IDs prüfen.
3. Datenübernahme-Prototyp: `carrot_plant.json` und `carrot_mutation.json` in ein modernes Schema konvertieren und validieren.
4. Testbare Genetiklogik unabhängig von Minecraft extrahieren und Unit-Tests für Dominanz/Vererbung/10-10-10 erstellen.
5. Den ersten echten NeoForge-21.1.265-Build erst als erledigt markieren, wenn kompilierbar und Client/Server getestet.
