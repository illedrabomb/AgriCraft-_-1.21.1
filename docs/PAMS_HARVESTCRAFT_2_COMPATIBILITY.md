# Pam's HarvestCraft 2 – priorisierte NeoForge-1.21.1-Kompatibilität

**Stand:** 16.11.2026. **Branch:** `feature/neoforge-1211-foundation`.  
**Status:** Acht optionale Crop-Sticks-Datapack-Definitionen gespeichert. **Nicht kompiliert, nicht getestet und nicht im Spiel bestätigt.**

## Priorität und verfügbare Module

Auf Nutzerwunsch ist **Pam's HarvestCraft 2** gegenüber Farmer's Delight die vorrangige Kompatibilitätsarbeit.

Die offiziellen CurseForge-Modseiten listen passende NeoForge-Dateien für Minecraft 1.21.1:
- **Crops:** `pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar` (09.05.2026); Mod-ID `pamhc2crops`.
- **Trees:** `pamhc2trees-NEOFORGE-1.21.1-1.0.9.jar` (30.03.2026).
- **Food Core:** `pamhc2foodcore-NEOFORGE-1.21.1-1.0.4.jar` (23.08.2025).

Primärquellen:
- https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops
- https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-trees
- https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-food-core

Pam's HarvestCraft 2 Crops enthält laut Projektbeschreibung viele Kulturpflanzen und Welt-Gärten. Unser Port erweitert **nur die Crop-Sticks-Bewirtschaftung**; er ersetzt keine Pam's-Gärten, eigenen Pflanzblöcke oder Vanilla-Drops.

## Originale AgriPlants-Definitionen

Als Datengrundlage dienen acht Originaldateien aus `AgriCraft/AgriPlants`, gepinnter Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Pfad `mod_pamhc2crops/plants/<name>_plant.json`. Für den Port wurden **neue, eigenständige JSON-Datapacks** geschrieben; es werden keine Originaltexturen oder sonstigen proprietären Pam's-Assets kopiert.

Die ausgewählten Namen und Original-Anbauzeiten:

| Pflanze | Original-Pflanzen-ID | Original-Saison | Saatgut-ID | Ernte-ID |
|---|---|---|---|---|
| Tomate | `pamhc2crops:tomato_plant` | Summer | `pamhc2crops:tomatoseeditem` | `pamhc2crops:tomatoitem` |
| Mais | `pamhc2crops:corn_plant` | Summer, Autumn | `pamhc2crops:cornseeditem` | `pamhc2crops:cornitem` |
| Reis | `pamhc2crops:rice_plant` | Summer | `pamhc2crops:riceseeditem` | `pamhc2crops:riceitem` |
| Hafer | `pamhc2crops:oats_plant` | Spring, Autumn | `pamhc2crops:oatsseeditem` | `pamhc2crops:oatsitem` |
| Roggen | `pamhc2crops:rye_plant` | Autumn | `pamhc2crops:ryeseeditem` | `pamhc2crops:ryeitem` |
| Zwiebel | `pamhc2crops:onion_plant` | Spring, Summer, Autumn | `pamhc2crops:onionseeditem` | `pamhc2crops:onionitem` |
| Kohl | `pamhc2crops:cabbage_plant` | Spring | `pamhc2crops:cabbageseeditem` | `pamhc2crops:cabbageitem` |
| Baumwolle | `pamhc2crops:cotton_plant` | Summer | `pamhc2crops:cottonseeditem` | `pamhc2crops:cottonitem` |

Gemeinsame Originalwerte, die übernommen wurden: acht Altersstufen `[2,4,6,8,10,12,14,16]`, `harvestStage=4`, `growth_chance=0.75`, `growth_bonus=0.025`, `cloneable=true`, `spread_chance=0.1`, Ernteprodukt Menge 1–5 mit 0.9-Chance.

Chemieanforderungen der meisten Pflanzen: `wet` Feuchtigkeit (Faktor 0.15), `slightly_acidic` Säure (Faktor 0.2), `high` oder höher Nährstoffe (Faktor 0.1). **Reis ist eine Ausnahme:** `watery` oder feuchter; das Original nennt Nährstoffstufe `average`, welche im neuen Port ausdrücklich als `medium` oder höher interpretiert wurde. `soil_chemistry` ist mit dem tatsächlich vererbten Strength-Wert verbunden.

## Modernes Datapack und optionale Mod

Ablage: `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/pamhc2_<name>.json`.

- Jedes JSON enthält `mods: ["pamhc2crops"]`; `CropDataReloadListener` überspringt es, wenn Pam's Crops **nicht** geladen ist. Keine neue harte Mod-Abhängigkeit oder direkte Java-Referenz auf Pam's.
- Der Loader validiert beim Server-Reload die tatsächlich registrierten Saatgut- und Ernte-Item-IDs. Fehlende/umbenannte Items deaktivieren **nur** die betreffende Pflanzendefinition mit Diagnose.
- Es wurden **keine Pam's-Zuchtmutationen erfunden**: Neue Kreuzungsrezepte sollen nur aus belegten AgriPlants-Originalen stammen.
- Die Server-Pflanzen nutzen den generischen Crop-Sticks-Pflanzkörper sowie den vorhandenen visuellen Fallback. Spezifische Pam's-Wachstums-/Pflanzen-Texturen werden **nicht** als eigene Assets kopiert oder ohne bestätigte Render-Registrierung behauptet.

### Konkrete bekannte Abweichungen zur ursprünglichen 1.18.2-Definition

1. **`overridePlanting=true`:** Die ursprünglichen AgriPlants-Pam's-Dateien übersteuern Pam's normales Pflanzenverhalten. Dieser besondere Hook wird aktuell **nicht** portiert. Die zusätzlichen Definitionen sind auf das **explizite Benutzen auf Crop Sticks** beschränkt; normale Pam's-Pflanzen bleiben unverändert.
2. **Original-Lichtobergrenze 16:** Der NeoForge-Port verwendet `max_light: 15`, da `LevelReader.getRawBrightness` in der Zielversion typischerweise 0–15 liefert. Die originale `light_tolerance_factor: 0.5` ist **noch nicht 1:1 auf den neuen `light_tolerance`-Wert übertragen**. Zur Vermeidung einer erfundenen Umrechnung wurde kein zusätzlicher Licht-Toleranzwert gesetzt (Standard 0).
3. **Originales `fluid=minecraft:empty`:** Nicht nach `fluid_id` konvertiert, weil die Ziel-Nutzung eines leeren Fluid-Registryschlüssels auf Minecraft 1.21.1 nicht bestätigt wurde. Vanilla/Mod-Flüssigkeiten müssen als separates Kompatibilitätspaket behandelt werden.
4. **Saisonregeln:** Sie sind als `allowed_seasons` vorhanden; ohne installierten `SeasonAccess.Provider` werden saisonale Sperren bewusst ignoriert. Es gibt noch **keinen Serene-Seasons-Adapter**.
5. **Boden:** Alle acht verwenden als erste Crop-Sticks-Portvorgabe `soil_block: "minecraft:farmland"`. Das ist eine **engere/konkrete Port-Einschränkung**, keine vollständige Reproduktion aller ursprünglichen erlaubten Bodenvarianten. Reis braucht auf diesem Boden ggf. Regen/Bewässerung, bis `watery` erreicht wird.
6. **Pam's Crop-Mod-Registrierung:** Mod-ID und viele Saatgutnamen sind aus vorhandenen AgriPlants-Originalen und Pam's-Projektquellen belegt. **Die Ziel-JAR 1.0.9 wurde nicht in einer NeoForge-Laufzeit inspiziert**, daher bleiben endgültige Item-IDs/Client-Renders unbestätigt.

## Weitere Arbeit (Priorität)

1. **Restliche originalen `mod_pamhc2crops/plants/`-Definitionen** systematisch übernehmen, mit Originalbedingungen, bestehenden Seed-/Crop-ID-Prüfungen und sauberer Diagnose. Der vorhandene Offline-Importer kann dies nach späterer expliziter Freigabe unterstützen; er wurde **nicht ausgeführt**.
2. Original-Pam's-Crop-Mutationsdefinitionen `mod_pamhc2crops/mutations/` klassifizieren und nur portierbare Bedingungen übernehmen.
3. **Pam's Trees:** getrennte Architektur prüfen, da Fruchtbäume nicht einfach die Genetik gewöhnlicher Crop Sticks übernehmen sollten. Keine bloße Scheinunterstützung durch generische Pflanzdefinitionen.
4. Farm-Minecraft-1.21.1-Pflanzung: Dual-Use-Saatgut, unmodifizierte ItemStacks, Analyzer, Crop-Sticks-Wachstum, Dünger, Ertrag sowie zurückgewandeltes Original-Saatgut prüfen.
5. Keine Builds, Tests, Actions, Minecraft-Starts oder Importer-Läufe ohne Zustimmung. `entwicklung`, `testing` und `master` nicht ungefragt verändern.

## Rechts-/Assetgrenze

Die Originalmod Pam's HarvestCraft 2 ist laut CurseForge **All Rights Reserved**. Die Portierung referenziert nur Item-IDs und eigene neue Datapack-Definitionen; **keine Texturen, Modelle, Java-Klassen oder längeren Original-Datensätze** aus Pam's werden als Assets kopiert.
