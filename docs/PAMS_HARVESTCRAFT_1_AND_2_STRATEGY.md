# Pam's HarvestCraft 1 und 2: Kompatibilitäts- und Migrationsstrategie

**Entscheidung:** 16.11.2026. **Priorität:** beide Generationen; Farmer's Delight nachrangig.  
**Branch:** `feature/neoforge-1211-foundation`. **Status:** Datapack-Quellcode, keine Builds oder Ingame-Tests.

## Unterschied zwischen den Generationen

**HarvestCraft 1 (Original):** Die offizielle Veröffentlichung endet bei Minecraft 1.12.2/Forge. Die Autorin stellt ausdrücklich klar, dass die erste Generation nicht über 1.12.2 hinaus aktualisiert wird. Es gibt keine offizielle NeoForge-1.21.1-Fassung, auf die der Port zur Laufzeit zugreifen könnte. Sie ist daher **Legacy-Migrationsquelle**, **keine tatsächliche NeoForge-Runtime-Integration**.

**HarvestCraft 2:** Separate moderne Mods. Das offizielle **Crops**-Modul für NeoForge 1.21.1 unterstützt die Mod-ID `pamhc2crops`; die originalen AgriPlants-1.18.2-Dateien enthalten kompatible Pflanzendefinitionen als Ausgangsdaten. Die Module **Trees**, **Food Core** und **Food Extended** haben separate Funktionsumfänge und werden nicht durch Crops-Datapacks repräsentiert.

Quellen:
- HarvestCraft 1: https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft
- HarvestCraft 2 Crops: https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops
- Originale AgriPlants-Crops: `AgriCraft/AgriPlants`, Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Verzeichnis `mod_pamhc2crops/plants/`.
- HarvestCraft-1-Quellgeschichte: https://github.com/MatrexsVigil/harvestcraft

## Zwei getrennte Pfade

### 1. Direkte HarvestCraft-2-Crop-Sticks-Kompatibilität

Die neu angelegten `pamhc2_*.json`-Serverdefinitionen nutzen den Mod-Guard `mods: ["pamhc2crops"]`. Sie sind im Spiel nur dann aktivierbar, wenn das echte Crops-Modul verfügbar ist und Saatgut- sowie Ernte-IDs im Ziel-Registry existieren. Die üblichen Crop-Sticks-Mechanismen der Mod `cropgenetics` können damit auf HarvestCraft-2-Saatgut angewendet werden.

Die erste Auswahl von **8 Pflanzen** (Tomate, Mais, Reis, Hafer, Roggen, Zwiebel, Kohl, Baumwolle) wurde am 16.11.2026 angelegt. Die zweite Auswahl von **14 Pflanzen** wurde aus weiteren verifizierten AgriPlants-Dateien übernommen: **Bean, Flax, Okra, Leek, Jute, Kiwi, Taro, Kale, Peas, Kenaf, Grape, Lettuce, Strawberry und Soybean**. Damit liegen **22 Pam's-2-Crop-Definitionen als Quellcode** vor. Alle sind unkompiliert/ungetestet.

Gemeinsame bekannte Grenzen: `overridePlanting=true` aus den Originaldaten wurde **nicht** übernommen; die Definitionen sind nur für Crop Sticks. Alte Lichtobergrenze 16 wird auf 15 beschränkt, `light_tolerance_factor` nicht erfunden, der historische `minecraft:empty`-Fluidfilter nicht blind zugeordnet. Saisonregeln greifen erst mit einem externen `SeasonAccess.Provider`. Client-Pflanzendarstellung benötigt eigene Prüfung. `Agave` und `Sisal` bleiben zurückgestellt, weil ihre originalen `arid`-Bedingungen mit einer pauschalen Farmland-Standortvorgabe zunächst keine saubere Originalparität garantieren.

### 2. HarvestCraft-1-Legacy-Kompatibilität

In 1.12.2 gespeicherte Items, Samen, Ernteprodukte, die Crop-Arten und die entsprechenden Registry-/Metadata-IDs sind **nicht einfach die `pamhc2crops`-IDs**. Eine direkte Kopie würde in 1.21.1 ungültige oder falsch zugeordnete Saaten erzeugen. Geplant ist deshalb ein **separater, versionierter, expliziter Crosswalk**:

- **Source:** altes HarvestCraft-1-`harvestcraft`-Item oder Crop-Identität, inklusive ggf. Metadata/NBT, nur nach Source-Verifikation.
- **Target:** moderne `pamhc2crops`-Art, sofern belegt und in der konkreten NeoForge-Version vorhanden; sonst bewusster Eintrag **nicht migrierbar** oder eine später unabhängig entwickelte, rechtlich erlaubte Alternative.
- **Version und Provenienz:** Quelle der alten ID, Zielversion, dokumentierte Semantik sowie Warnung bei Mehrdeutigkeit.
- **Kein Auto-Import:** Keine stillschweigende Eins-zu-eins-Übertragung nach bloßem deutschen oder englischen Pflanzennamen; keine Konvertierung unbekannter NBT-/Weltstände.
- **Spielwelten:** Alte 1.12.2-Welten sind nicht automatisch in der neuen 1.21.1-Version lauffähig. Minecrafts Versions-/Mod-Migration und mögliche fehlende Blockzustände müssen getrennt geplant und später autorisiert getestet werden.
- **Assets/Rechte:** Keine Originaltexturen, Modelle oder ungeprüften alten Java-Klassen in die neue Mod kopieren; die HarvestCraft-Mod ist als `All Rights Reserved` veröffentlicht.

Die HarvestCraft-1-Migration ist **noch nicht implementiert**. Dieser Unterschied ist absichtlich Teil der Projektanforderung, nicht ein stillschweigender Verzicht auf Generation 1.

## Update 16.11.2026: erste Legacy-Zuordnung umgesetzt

Die `CropRegistry.java` der Originalmod HarvestCraft 1 (1.12.2) wurde unmittelbar gelesen und mit 22 bereits vorhandenen HarvestCraft-2-`AgriPlants`-Pflanzen verglichen. Die daraus abgeleiteten **22 eindeutigen, bewusst nur als Kandidaten bezeichneten ID-Maps** liegen unter `neoforge-port/tools/data/pams_harvestcraft_1_to_2_crops.json`. Das lesende Tool `neoforge-port/tools/pam_legacy_crosswalk.py` wurde hinzugefügt, aber **nicht ausgeführt**. Es gibt weiterhin **keine Weltmigration**. Besonders wichtig: alte Crop-Blöcke haben keine direkte Ziel-Block-ID; der Crosswalk nennt bei ihnen nur die zugehörige H2-Species. Weitere Details und die Kaffee-/Senf-/Tee-Namensausnahmen: `docs/PAMS_HARVESTCRAFT_1_LEGACY_ID_CROSSWALK.md`.

## Nächste konkrete Arbeit

1. Aus den tatsächlichen HarvestCraft-1-1.12.2-Quelldaten eine **belegte Tabelle alter Crop-/Item-IDs** aufbauen; mit HarvestCraft-2-Crops-Registry abgleichen.
2. Nur eindeutig belegte 1→2-Mappings als prüfbare, optionale JSON-Migrationsdefinitionen anlegen; nie erfundene IDs verwenden.
3. Die restlichen AgriPlants-`mod_pamhc2crops/plants/`-Pflanzen und vorhandene Mutationen weiter katalogisieren.
4. Für Trees/Fruit Trees den eigenen Wachstums-/Fruchtadapter entwerfen (keine Gleichsetzung mit normalen Crop Sticks).
5. Nach ausdrücklicher Freigabe Kompilierung, Item-/Block-Registry-Checks und Ingame-Tests auf `testing`.

`entwicklung`, `testing` und `master` bleiben bis zur ausdrücklich gewünschten Integration/Abnahme unverändert.
