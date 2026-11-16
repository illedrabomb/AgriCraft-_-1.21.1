# Pam's HarvestCraft 2: Rosenkohl, Legacy-Vanilla-Aliasse und 11 weitere Originalmutationen

**Stand:** 16.11.2026. **Aktiver Feature-Branch:** `feature/neoforge-1211-foundation`.  
**Validierung:** NUR Source-/Datapack-Änderungen. **Keine Builds, Tests, Minecraft-Starts, Skript- oder CI-Ausführung.**

## Originalquellen

Die Original-AgriPlants-Dateien wurden aus `AgriCraft/AgriPlants`, Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, verwendet:
- `mod_pamhc2crops/plants/brusselsprout_plant.json`
- `mod_pamhc2crops/mutations/{onion,radish,tomato,rice,rye,barley,celery,sweetpotato,scallion,cantaloupe,bean}_mutation.json`.

**Rosenkohl** ist unter `data/cropgenetics/cropgenetics/plants/pamhc2_brusselsprout.json` ergänzt. Original: ID `pamhc2crops:brusselsprout_plant`, Seed `pamhc2crops:brusselsproutseeditem`, Ernte `pamhc2crops:brusselsproutitem`, acht Stufen, harvestStage 4, growthChance 0.75, growthBonus 0.025, cloneable true, spreadChance 0.1, Produkt 1–5 mit 0.9, `wet/slightly_acidic/high` mit Toleranzen 0.15/0.2/0.1, Frühling. Wie bei unseren übrigen Pam's-Crops ersetzen wir den Original-Override des normalen Pflanzens **nicht**: Nutzung nur über Crop Sticks; historisches maxLight 16 wird für den NeoForge-Port auf 15 begrenzt, ursprüngliche Fluid-/Licht-Toleranz-Sonderregeln werden nicht behauptet.

## Original-Mutationen nach der Rosenkohl-Ergänzung

| Ergebnis | Eltern | Originalchance |
|---|---|---|
| `onion` | `celery` + `brusselsprout` | 0.5 |
| `radish` | `tomato` + `brusselsprout` | 0.5 |

Beide wurden mit unveränderten Original-Eltern-/Kind-Art-IDs übernommen und mit `mods: ["pamhc2crops"]` als optionale Server-Datapacks gesichert.

## Explizite 1.18-AgriPlants-Vanilla-Aliasse

Die gepinnten älteren Mutationdateien benutzen den Namespace `vanilla:`. Unser aktueller NeoForge-Port verwendet bei den bereits existierenden Pflanzen `minecraft:`. Alle sechs folgenden **Einzelzuordnungen** wurden gegen die tatsächlich auf dem Feature-Branch vorhandenen `plants/*.json`-Dateien geprüft:

| Alte Original-Art | Neue Port-Art |
|---|---|
| `vanilla:carrot_plant` | `minecraft:carrot_plant` |
| `vanilla:potato_plant` | `minecraft:potato_plant` |
| `vanilla:wheat_plant` | `minecraft:wheat_plant` |
| `vanilla:sugarcane_plant` | `minecraft:sugar_cane_plant` |
| `vanilla:melon_plant` | `minecraft:melon_plant` |
| `vanilla:pumpkin_plant` | `minecraft:pumpkin_plant` |

Die Alias-Liste liegt unter `neoforge-port/tools/data/agriplants_vanilla_species_aliases.json`. Sie ist eine **begrenzt zugelassene Source-Migrationsreferenz**, **kein** zur Laufzeit geladener globaler Namespace-Übersetzer und keine Weltmigration. Unbekannte `vanilla:`-Art-IDs werden **nicht automatisch umgeschrieben**.

## Neun durch explizite Aliasse mögliche Originalmutationen

Bei diesen neun AgriPlants-Originalmutationen wurden ausschließlich vorhandene `vanilla:`-Eltern durch exakt zugeordnete `minecraft:`-Art-IDs ersetzt. Die anderen Eltern, Kind-Art und Mutationsgrundchance 0.5 blieben erhalten. Die Dateien liegen unter `data/cropgenetics/cropgenetics/mutations/pamhc2_*.json`.

| Kind | Eltern in neuer Portnotation | Chance |
|---|---|---|
| Tomate | Süßkartoffel + Karotte | 0.5 |
| Reis | Roggen + Zuckerrohr | 0.5 |
| Roggen | Gerste + Weizen | 0.5 |
| Gerste | Weizen + Zuckerrohr | 0.5 |
| Sellerie | Weizen + Sojabohne | 0.5 |
| Süßkartoffel | Kartoffel + Zuckerrohr | 0.5 |
| Frühlingszwiebel | Karotte + Zuckerrohr | 0.5 |
| Cantaloupe | Melone + Erdbeere | 0.5 |
| Bohne | Kürbis + Kartoffel | 0.5 |

Alle aufgeführten Elternspezies wurden als vorhandene `PlantDefinition`-IDs im eigenen Repository geprüft. Die vorhandene Registry-/Reload-Logik kann später ungültige Mutationen wegen fehlender Species zurückweisen; dies wurde **nicht ausgeführt**.

**Wichtig für die Spielmechanik:** Der Port liefert die **originale Basis-Chance** und Elternkombination, aber allgemeine Kreuzungs-, Licht-, Frucht-, Umwelt- und Genetiklogik kann die tatsächliche Erfolgswahrscheinlichkeit weiter beeinflussen. Die Sonderfälle von Vanilla-Kürbis/Melone/Zuckerrohr wurden noch nicht im Spiel validiert; ein technischer Merge/Release ist nicht mit einer Gameplay-Abnahme gleichzusetzen.

## Neuer Gesamtstand

- **31** optionale Pam's HarvestCraft 2 Crop-Sticks-Pflanzen als JSON-Quellcode (vorher 30).
- **25** originale Pam's-2-Mutationsdatensätze (vorher 14; **2 + 9** neu).
- **31** historisch belegte Pam's-HarvestCraft-1-zu-2-ID-Migrationskandidaten (vorher 30); nach Rosenkohl ergänzt.
- Keine komplette HarvestCraft-1-Laufzeitintegration (die Originalmod endet bei 1.12.2).
- Keine sichere Aussage über das aktuelle Registry-Inventar der Pam's-2-NeoForge-1.21.1-JAR, solange nicht direkt geprüft.

## Weiteres Vorgehen

1. Weitere HarvestCraft-2-Crops und belegte Originalmutationen inventarisieren, u. a. noch nicht migrierte Eltern-/Kinderarten.
2. Pam's-Crops-NeoForge-1.21.1-JAR-Registries **vor** einer tatsächlichen Item- oder Spielstands-Konvertierung mit dem Crosswalk abgleichen.
3. Die Besonderheiten der Vanilla-Spezies bei Kreuzung und Vererbung in einem **später ausdrücklich genehmigten** Build-/Testdurchlauf validieren; ebenso Season-Provider, Rendering und Crop-Sticks-Ernte.
4. Pam's Trees und den späteren MineColonies-Addon-Pfad getrennt weiterentwickeln.

Andere Branches (`entwicklung`, `testing`, `master`) bleiben unverändert.
