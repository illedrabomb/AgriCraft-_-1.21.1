# Originale AgriPlants-Bodenprofile und konfigurierbare Bodendynamik

**Stand:** 2026-11-16  
**Branch:** `feature/neoforge-1211-foundation`  
**Status:** Als Quellcode/Server-Datapacks committed; **nicht kompiliert, nicht ausgeführt und nicht getestet**.

## Quelle und Übertragungsgrenzen

Die folgenden Werte wurden direkt aus den **gepinnten AgriPlants-1.18.2-Dateien** des Repositories `AgriCraft/AgriPlants`, Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Ordner `minecraft/soils/`, entnommen. Für die Farmer's-Delight-Variante wurde `mod_farmersdelight/soils/rich_soil_soil.json` ebenfalls eingesehen, sie wird derzeit **nicht als obligatorische Integration ausgeliefert**.

| Original | NeoForge-Block-ID(s) | Feuchtigkeit | Säure | Nährstoffe | growth_modifier |
|---|---|---|---|---|---:|
| farmland_soil | `minecraft:farmland` | wet | slightly_acidic | high | 1.0 |
| sand_soil | `minecraft:sand`, `minecraft:red_sand` | arid | neutral | low | 0.8 |
| clay_soil | `minecraft:clay` | flooded | slightly_acidic | medium | 1.0 |
| podzol_soil | `minecraft:podzol` | damp | slightly_acidic | very_high | 1.1 |
| mycelium_soil | `minecraft:mycelium` | damp | neutral | low | 0.8 |
| soul_sand_soil | `minecraft:soul_sand` | arid | neutral | high | 0.8 |
| soul_soil_soil | `minecraft:soul_soil` | arid | neutral | very_high | 1.0 |
| rich_soil_soil **(nur untersucht)** | `farmersdelight:rich_soil` | damp | neutral | very_high | 1.8 |

**Wichtige Abweichung:** Das historische Sand-Profil verwendet den Forge-Blocktag `forge:sand`. Im neuen Port wurde es **bewusst in zwei Vanilla-Block-IDs aufgeteilt**, weil das bisherige Profil-Reload-System exakte Block-IDs erwartet. Diese zwei IDs können die Mitglieder eines historischen Tags nicht vollständig abbilden. Eine kompatible NeoForge-/Common-Tag-Auflösung und Erfassung weiterer Sandvarianten stehen noch aus.

Alle **acht Vanilla-Blockdateien** liegen unter `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/soil_profiles/` (inklusive `red_sand.json`). Kein originales AgriPlants-`stateDefinition` mit abweichenden Blockzuständen wurde übernommen: die geprüften Vanilla-Vorlagen enthalten leere `stateDefinition`-Listen. Nicht untersuchte Mod-Soils und NBT-basierte Varianten bleiben offen.

**Änderung gegenüber früheren Etappen:** Es werden ab jetzt **belegte Originalprofile** als Daten ausgeliefert, statt überhaupt keine Profile mitzuliefern. Die Aussage „kein automatisch zugewiesener Vanilla-Wert“ aus älteren Entwicklungsständen ist damit historisch und nicht mehr der aktuelle Stand. Dennoch werden keine willkürlichen Werte erfunden.

## growth_modifier im Wachstumspfad

`SoilProfileRegistry.Profile` speichert `SoilChemistryAccess.Sample` und `growthModifier` **im selben atomaren Registry-Snapshot**.

- In Profil-Dateien ist `growth_modifier` optional und standardmäßig `1.0`.
- Zulässig: endliche Zahlen zwischen **0 und 4**. Ungültige Einträge werden wie fehlerhafte Profile mit Warnung übersprungen; doppelte Deklarationen desselben Blocks in verschiedenen Dateien werden deaktiviert.
- `CropConditions.growthFactor` multipliziert den bisherigen Licht-/Umweltfaktor mit dem `growth_modifier` des **aktuellen Bodenblocktyps**, auch bei optimalem Licht. Somit wirken die Werte auf Wachstum, Fruchtchance und die Viabilitätsprüfung.
- Bestehende positionsbezogene Chemie-Overrides verändern **nur Feuchtigkeit, Säure und Nährstoffe**; der Wachstumsfaktor gehört weiterhin zum **Blockprofil**.
- Ohne passendes Bodenprofil beträgt der Wachstumsfaktor `1.0`. Falls die Pflanzenanforderung eine Bodenchemie-Regel enthält und kein Sample existiert, bleibt sie trotzdem unerfüllt.
- **Bodenprofil ≠ erlaubter Pflanzenstandort:** Der darunterliegende Block muss zusätzlich dem `soil_block`/`soil_tag` der Pflanze entsprechen. Ein neues Sandprofil allein lässt nicht automatisch Weizen auf Sand wachsen.

Die Originalquellen bestätigen die Zahlen, aber die **exakte Multiplikationsreihenfolge des historischen AgriCore-Runtimes** und Interaktionen mit sämtlichen Spezialpflanzen wurden noch nicht vollständig verifiziert. Die gegenwärtige Einbindung ist eine ausdrücklich gekennzeichnete Portierung.

Beispiel:
```json
{
  "block": "minecraft:podzol",
  "humidity": "damp",
  "acidity": "slightly_acidic",
  "nutrients": "very_high",
  "growth_modifier": 1.1
}
```

## Datapack-konfigurierbare Regen- und Nährstoffraten

Neues Server-Datapack-Format: `data/cropgenetics/cropgenetics/soil_dynamics.json`

```json
{
  "rain_tick_chance": 16,
  "growth_nutrient_chance": 8
}
```

- Beide Zahlen sind **Ganzzahlen von 0 bis 100000**.
- **0 deaktiviert** die jeweilige Mechanik.
- **N > 0** bedeutet eine Änderungswahrscheinlichkeit von **1/N pro geeignetem Ereignis**.
- `rain_tick_chance`: Nur bei Regen oberhalb einer **bepflanzten** Crop-Sticks-Position während eines Server-Random-Ticks; +1 Feuchtigkeitsstufe.
- `growth_nutrient_chance`: Nach erfolgreichem Fortschritt einer Pflanze um eine Stufe; -1 Nährstoffstufe.
- Höchst-/Tiefstgrenzen bleiben: Feuchtigkeit bis `flooded`, Nährstoffe bis `none`.
- Die Quoten sind **bewusst neue Port-Einstellungen**, keine aus dem historischen AgriCore belegten Wahrscheinlichkeiten. Die Datei wird mit den bisherigen Defaultwerten ausgeliefert.
- Fehlende Datei bei Datapack-Reload → Default `16` und `8`; ungültige Datei → Warnung und ebenfalls Defaultwerte. Unbekannte Keys, negative Zahlen, Bruchteile, nichtnumerische Werte und zu große Zahlen werden abgelehnt.
- `SoilDynamicsReloadListener` liest die JSON im Ressourcen-Reload; `SoilDynamics.configure` veröffentlicht ein unveränderliches `SoilDynamicsSettings`-Objekt per `volatile` Referenz.

### Einschränkungen

Datapack-Priorität überschreibt **gleiche Ressourcenpfade**. Eigene Bodenprofile unter anderem Dateinamen, die dieselbe Block-ID wie eingebaute Profile deklarieren, zählen dagegen als **widersprüchliche Einträge** und deaktivieren dieses Blockprofil. Ein Modpack sollte die eingebauten Dateipfade überschreiben, wenn es einen Vanilla-Wert ändern möchte.

Für Mods (u. a. Farmer's Delight), modded soil_tags, BlockState-/NBT-basierte Profile, Regen über Gewächshäusern, automatische Verdunstung, Nährstoffregeneration, Bewässerungsleitungen und das spätere MineColonies-Addon bleibt weitere Arbeit.

Das ganze Paket ist **nicht kompiliert oder getestet**. Vor Veröffentlichung müssen NeoForge-1.21.1-Signaturen, Server-Reload, Resources-Priorität, Blockregistrierung, Persistenz, Performance und die reale Wachstumsrate geprüft werden.

## Dateien

- `data/SoilProfileRegistry.java`, `data/SoilProfileReloadListener.java`
- `data/SoilDynamicsSettings.java`, `data/SoilDynamicsReloadListener.java` (neu)
- `content/CropConditions.java`, `content/SoilDynamics.java`
- `CropGeneticsMod.java`
- `src/main/resources/data/cropgenetics/cropgenetics/soil_profiles/{farmland,sand,red_sand,clay,podzol,mycelium,soul_sand,soul_soil}.json`
- `src/main/resources/data/cropgenetics/cropgenetics/soil_dynamics.json`

Java-/Ressourcenpfade relativ zu `neoforge-port/`.
