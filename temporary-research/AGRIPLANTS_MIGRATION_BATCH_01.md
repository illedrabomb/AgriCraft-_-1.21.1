# AgriPlants-Migration: Erste kontrollierte Erweiterung
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Tatsächlich in diesem Schritt gespeichert
Originalquelle: `AgriCraft/AgriPlants`, Git-Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Minecraft 1.18.2.
Quellen:
- `minecraft/plants/beet_root_plant.json`
- `minecraft/plants/nether_wart_plant.json`

Neue NeoForge-Datensätze:
- `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/beetroot.json`
- `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/nether_wart.json`

Neue Client-Visuals:
- `neoforge-port/src/main/resources/assets/cropgenetics/cropgenetics/plant_visuals/beetroot.json`: Original-Vanilla-Texturliste `minecraft:block/beetroots_stage0..3`, auf 8 Wachstumsstufen gelegt.
- `neoforge-port/src/main/resources/assets/cropgenetics/cropgenetics/plant_visuals/nether_wart.json`: `minecraft:block/nether_wart_stage0..2`, 8 Stufen.

Beide Arten sind zusätzlich als genetische Anfangssamen im Kreativinventar registriert. Das vorhandene normal-Samen-Indexing kann `minecraft:beetroot_seeds` und `minecraft:nether_wart` jeweils über ihre neue PlantDefinition erkennen, sofern ihre Item-Interaktion vom Minecraft-Client tatsächlich an den CropStick-Block weitergereicht wird.

## Bewusst aus der Originaldatei übernommene Werte
| Feld | Rote Bete | Netherwarze |
|---|---|---|
| Plant-ID | `minecraft:beet_root_plant` | `minecraft:nether_wart_plant` |
| Saatgut | `minecraft:beetroot_seeds` | `minecraft:nether_wart` |
| Produkt | `minecraft:beetroot` | `minecraft:nether_wart` |
| Ernte-Min/Max | 1–3 | 1–3 |
| Produktchance | 0.95 | 0.95 |
| Basiswachstum | 0.75 | 0.65 |
| Wachstumsbonus | 0.025 | 0.025 |
| Stadien | 2,4,6,8,10,12,14,16 | 2,4,6,8,10,12,14,16 |
| After-harvest stage | 4 | 4 |
| Cloneable / spread | true / 0.1 | true / 0.1 |

## Absichtlich vereinfachte Umweltbedingungen
Original-AgriPlants enthält Bodenfeuchte, Säuregehalt, Nährstoffe, saisonale Regeln, Lichttoleranzfaktor, Biome/Dimensionen und mehr. **Diese Regeln wurden nicht 1:1 portiert.** Stattdessen:
- Rote Bete: `soil_block=minecraft:farmland`, Licht 10..15 (Original max=16 war anderer Zahlenraum; Original verlangt u.a. Herbst).
- Netherwarze: `soil_block=minecraft:soul_sand`, Licht 0..8; die ursprünglichen Chemiebedingungen werden dadurch nicht exakt abgebildet.
- Beide: `light_tolerance=0`; Original-`light_tolerance_factor` ist nicht dieselbe Kennzahl.
- Für beide sind im Source-Manifest die weggelassenen Legacy-Regeln ausdrücklich bestätigt und kommentiert. Das ist **bewusste NeoForge-Kompatibilitätsvereinfachung**, keine vollständige 1:1-Übernahme.

## Wiederverwendbare Importbasis
Neu:
- `neoforge-port/tools/migrate_agriplants.py`
- `neoforge-port/tools/agriplants_migration_manifest.json`

Der Konverter akzeptiert nur im Manifest explizit benannte Quell-Pflanzen, fordert ausdrücklich freigegebene Boden- und Lichtbedingungen und prüft grundlegende Textur-, Item- und Produktfelder. Er weist Sonderfälle zurück (nicht-Hash-Renderer, mehrschichtige Texturen, NBT-/Tag-Einträge, Custom Callbacks, Clipping-Produkte, ungültige Produktwahrscheinlichkeiten, fehlende Bestätigung alter Requirements).

Ausführen nach lokalem Checkout des Original-AgriPlants und des NeoForge-Port-Projekts:
```sh
cd neoforge-port
python3 tools/migrate_agriplants.py \
  --source /pfad/zum/AgriPlants \
  --output src/main/resources \
  --manifest tools/agriplants_migration_manifest.json
```
Die Quelle sollte auf den oben genannten Git-Commit gepinnt werden. **Das Werkzeug wurde hier nur geschrieben, nicht ausgeführt.** Automatischer Massenimport ist noch nicht erfolgt; die zwei JSON-Definitionen wurden explizit gespeichert.

Wichtig: Der Konverter kann Zielressourcen überschreiben. Vor einem echten Import Veränderungen im Git-Diff und Daten-/Ressourcennamen prüfen. `mods`, Biome, Spezialböden, Artenmutationen und Client-Texturverfügbarkeit müssen bei weiteren Pflanzen individuell bewertet werden.

## Was noch fehlt / was nicht behauptet wird
- Kein Build, kein Test, kein Client- oder Dedicated-Serverstart gemäß Nutzerwunsch.
- Keine echte Ingame-Bestätigung, dass Soul-Sand-CropSticks, die Netherwarzeninteraktion und der neue Client-Renderer korrekt funktionieren.
- Keine 1:1-Abbildung der ursprünglichen Bodenchemie, Jahreszeiten, Spezialrenderformen, OverridePlanting-Events oder Sonderprodukte.
- Es wurden **nur zwei zusätzliche Arten** migriert. Der übrige Originalkatalog benötigt eine Prüfung pro Pflanzenfamilie und ggf. neue Adapter. Aussagen wie „alle AgriPlants-Pflanzen unterstützt“ wären falsch.
- Der Importer selbst ist nicht als validiertes Tool zu bezeichnen, bis seine Ausgabe später geprüft wird.

## Nächste sinnvolle Migration
Weitere Pflanzen zunächst nach Render-Typ, eigener Texturabhängigkeit, Soil-/Fluid- und Produktsemantik klassifizieren. Für `gourd`, `plus`, Mehrlagen-, Sonder-Sand-/Wasser- und NBT-Pflanzen zuerst entsprechende Adapter ergänzen, statt inkompatible Daten unbemerkt zu degradieren.
