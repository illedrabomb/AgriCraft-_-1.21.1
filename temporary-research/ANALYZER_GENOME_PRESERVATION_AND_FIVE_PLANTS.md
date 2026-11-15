# Saatgut-Analyse, Genomerhalt und fünf neue Vanilla-Pflanzen

Stand: 2026-11-15. Branch: `feature/neoforge-1211-foundation`.

**Nur Quellcode-/Ressourcenänderungen. Keine Builds, Tests, CI oder Minecraft-Starts.**

## 1. Ein benutzbares Analysewerkzeug

Im derzeitigen GitHub-Branch existierte kein registriertes Analyse-Item aus der zwischenzeitlichen lokalen Codex-Arbeit. Deshalb ist nun ein einfaches eigenständiges `cropgenetics:seed_analyzer` enthalten.

- Kreativmodus: Ingredient-Tab
- Survival-Rezept: Glasplatte und Stock, diagonal im 2×2/3×3-Craftingfeld
- Bedienung: Analysator in der **Haupthand**, ein unterstütztes Saatgut in der **Nebenhand**, dann Rechtsklick
- Saatgut und Analysator werden nicht verbraucht.
- Bei bereits genetischem Saatgut wird das vorhandene Genome **nicht** überschrieben. `CropItems.markAnalyzed` setzt nur den Analysemarker.
- Bei einem unveränderten Vanilla-/Mod-Saatgut ohne Genome, für das der aktive Katalog eine **eindeutige** Art kennt, wird zuerst der bereits etablierte Standard-Genotyp `Genome.basic(plantId)` (alle Allele 1/1) angehängt und danach analysiert. Das ist die definierte Baseline beim normalen Pflanzen dieses Saatguts, keine Schätzung unbekannter Gene.
- Nicht registrierte, mehrdeutige oder bereits anderweitig angepasste Seeds ohne Genome werden **nicht** verändert und melden einen Hinweis. Ein Seed mit vorhandenen, aber zur Itemart unpassenden Genen wird nicht analysiert.
- Shift-Tooltip nutzt die vorhandene clientseitige `AnalyzedSeedTooltip`-Klasse. Angezeigt werden Growth/Gain/Strength/Fertility/Mutativity mit beiden Allelen.
- Das Item nutzt die schon im Original vorhandene AgriCraft-Lupentextur, unverändert in den Port übernommen; Originallizenz liegt im Port.

**Lokaler Codex-Abgleich:** Wenn es lokal bereits einen anders implementierten Analyzer gibt, vor der nächsten Synchronisation dessen Funktion und Item-ID vergleichen und die beiden Systeme zusammenführen, statt beide zu registrieren.

## 4. Genome und Herkunft bei Samen erhalten

`CropSticksBlockEntity` speichert neben dem unveränderten `Genome` jetzt auch:
- `SeedAnalyzed` (Boolean)
- `OriginalSeed` (String-ID, sofern beim Pflanzen bekannt)

Der neue Pfad `plant(genome, sourceSeed)` liest diese Werte **bevor** der Quell-ItemStack beim Pflanzen verbraucht wird. Bei unbearbeitetem Vanilla-/Mod-Saatgut wird die ursprüngliche Item-ID direkt aus dem tatsächlichen Item übernommen.

Die Methode `seedDrop()` erstellt beim Ausgraben eine neue genetische Saatgut-ItemStack mit **identischem** Genome und übertragenem Analysemarker sowie Original-Item-ID. Bisher wurde ein neuer genetischer Seed nur aus dem Genome erzeugt; der Analysemarker und die exakte Ursprungsitem-ID gingen dabei verloren.

Bei **neuen Kreuzungsnachkommen** wird `SeedAnalyzed=false` gesetzt, da das Kind-Genome noch nicht untersucht wurde. Vorhandene Gene werden über `BreedingEngine` vererbt, aber die Analyse eines Elternteils wird nicht als Analyse des Kindes ausgegeben.

`CropItems.seedWithGenome(genome)` wählt nur dann eine spätere Ursprungs-Seed-ID aus dem Datenkatalog, wenn genau **eine** registrierte Itemart für die Art existiert. Gibt es mehrere mögliche Original-Seed-Items, wird keine Wahl geraten: eine verlustbehaftete Rückwandlung wird damit verhindert, sofern keine konkrete Quelle am Seed gespeichert ist.

Die bereits implementierte Crafting-Rückumwandlung **genetischer Seed + Papier → normales Saatgut, Papier zurück** bleibt unverändert. Die zurückverwandelte Saat besitzt ausdrücklich **keine** Genom- oder Analysekomponenten.

## 5. Neue kompatible Vanilla-Arten

Die neuen Definitionen liegen in `data/cropgenetics/cropgenetics/plants/`. Zuordnung und Client-Texturen:

| Art-ID | Saatgut/Produkt | Boden unter Crop Sticks | Darstellung |
|---|---|---|---|
| `minecraft:cornflower_plant` | `minecraft:cornflower` | Farmland | `minecraft:block/cornflower` |
| `minecraft:poppy_plant` | `minecraft:poppy` | Farmland | `minecraft:block/poppy` |
| `minecraft:dandelion_plant` | `minecraft:dandelion` | Farmland | `minecraft:block/dandelion` |
| `minecraft:cactus_plant` | `minecraft:cactus` | Sand | `minecraft:block/cactus_side` |
| `minecraft:sugar_cane_plant` | `minecraft:sugar_cane` | Dirt | `minecraft:block/sugar_cane` |

**Das sind Datapack-Crop-Sticks-Varianten, keine vollständige Emulation der Vanilla-Spezialblöcke.** Insbesondere Zuckerrohr braucht im Vanilla-Spiel Wasser neben geeignetem Boden; der bisherige Crop-Sticks-Adapter prüft Wasser noch nicht. Kaktus-Geometrie und die Darstellung von hohen Pflanzen sind vorläufige gekreuzte Sprites. Ohne dedizierten Adapter erfolgt das Wachstum rein über den normalen Crop-Sticks-Ticker und die definierte Licht-/Bodenbedingung. Vanilla-Pflanzen-/Blocktexturen bleiben Standard.

Es sind **keine** Fremdmod-Arten mit unbelegten Item-IDs importiert worden. Der Parser akzeptiert Modpflanzen nur bei vorhandenem Mod und registrierten Items/Soil-Blöcken. Fehlende Kompatibilität führt weiter zu per-Datei-Diagnosen.

## Spätere explizite Prüfungen

- Analyzer im Survival herstellen; Haupthand/Nebenhand benutzen; analysierte und unanalysierte Tooltips mit Shift prüfen.
- Genome auf Vanilla-Saatgut durch Analyse, Pflanzen, Ausgraben und erneutes Pflanzen erhalten; die Rückwandlung mit Papier prüfen.
- Neue Kreuzungsnachkommen auf `SeedAnalyzed=false` prüfen und Vererbung alle fünf Allelpaare vergleichen.
- Persistenz der NBT-Felder `SeedAnalyzed` und `OriginalSeed` nach Welt-Speichern/Neuladen sowie Multiplayer-Synchronisierung prüfen.
- Neue Pflanzen auf gültigen und ungültigen Bodenvarianten prüfen, besonders Sand/Dirt und die vorläufige Abweichung von Zuckerrohr-Vanilla-Regeln.
- Client-Itemmodell, Original-Lupen-PNG im Atlas, dedizierter Server, Rezeptdaten und Registernamen kompilieren und ingame verifizieren.
