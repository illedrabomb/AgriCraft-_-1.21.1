# Strength: Lichttoleranz und Wachstumsfaktor
Stand: 2026-11-15 | Branch `feature/neoforge-1211-foundation` | **Keine Builds/Tests ausgeführt** gemäß Nutzerwunsch.

## Implementiert
- `PlantRequirements`: zusätzliches Pflicht-Datenfeld `lightTolerance` (0..15). Ohne Definition standardmäßig 0, sodass bestehende Pflanzen mit strengem Lichtbereich unverändert bleiben.
- `AgriPlantsParser`: liest `cropgenetics_requirements.light_tolerance` als optionalen Zahlenwert; toleriert das neue Feld ausdrücklich, weist unbekannte und unimplementierte Altbedingungen weiterhin zurück.
- `CropConditions`: neue `growthFactor(level, pos, plant, genome)`-Funktion (Wert 0..1). **Bodenbedingungen bleiben strikt** – auch Strength 10 erlaubt keine fremden/ungültigen Spezialböden.
- Licht-Defizit ist Abstand vom konfigurierten `min_light..max_light`-Fenster. `Strength` wird wie die sichtbaren Werte im bisherigen Mod-Code mit `GenePair.TraitMode.MAX` ausgewertet.
- Zulässiges Defizit = `floor(light_tolerance * (strength-1)/9)`. Bei Defizit 0 ist der Faktor 1. Bei Defizit > zulässiges Defizit wird Wachstum komplett verhindert. Innerhalb des Toleranzfensters ist der Multiplikator `(allowed + 1 - deficit)/(allowed + 1)`. Dadurch bedeutet Toleranz schlechtere Wachstumsbedingungen, keine automatische Beschleunigung.
- `CropSticksBlock.randomTick`: verwendet `CropGrowth.chance(plant, genome) * CropConditions.growthFactor(...)` für den Server-Wachstumstick. Das erzeugt einen echten Einfluss von Strength auf das Wachstum.
- `CropSticksBlock.useItemOn`: genetische und normale Samen können nur eingesetzt werden, wenn der Wachstumsfaktor für **ihr tatsächliches Genom** > 0 ist. Normale Samen beginnen mit Strength 1.
- `CrossBreeding`: Eltern werden mit ihrem eigenen Genom und Nachkommen mit dem frisch erzeugten Genom auf Licht/Boden geprüft; Klone nutzen das Originalgenom.
- Beispielkarotte: `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/carrot.json` hat jetzt `light_tolerance: 3`, `min_light: 9`, `max_light: 15`, `soil_block: minecraft:farmland`.

## Beispiel
- Stärke 1, Karotte: Wachstum nur bei Licht 9..15; Defizit > 0 blockiert das Pflanzen/Wachsen.
- Stärke 10, Karotte: 3 Lichtpunkte außerhalb des Bereichs toleriert; bei Licht 8 beträgt der Faktor 3/4, bei Licht 6 der Faktor 1/4. Bei Licht <=5 bleibt Wachstum aus.
- Der Faktor wird auf die schon vorhandene growth-bezogene Chance multipliziert.

## Bewusst nicht übernommen
- Originales AgriPlants `light_tolerance_factor` ist eine andere Einstellung; wir **konvertieren sie nicht stillschweigend** in `light_tolerance`.
- Originale Soilchemie-Modelle, Nährstoffe, Luftfeuchtigkeit, Saisons, Blockstate-Bedingungen, Fluidbedingungen und spezielle Mod-Böden brauchen separate Adapter/Definitionsmöglichkeiten.
- Eine Schutzwirkung von Strength gegen den *falschen Bodentyp* ist nicht implementiert und absichtlich nicht pauschal versprochen.
- Noch kein Config-Multiplier und kein Vergleich mit dem Originalbalancing. `Strength` verwendet aktuell die vorhandene MAX-Dominanz des Genoms.

## Entwicklungsrisiken
- **Kein Compile, kein Test und kein Ingame-Start** seit der Nutzerentscheidung zum Pausieren von Tests. APIs bleiben unbestätigt.
- Die Beispielkarotte verwendet 1.21.1-Lichtwerte 0..15; der ältere AgriPlants-Datensatz enthielt max_light=16. Wer Altdaten übernimmt, muss den alten Zahlenraum ausdrücklich migrieren.
- Das neue `PlantRequirements`-Record hat ein zusätzliches Feld. Ein bestehender Java-Aufrufer, der den alten 4-Argumente-Konstruktor benutzt, muss aktualisiert werden.
- Crop Sticks werden bei geänderten Bodendefinitionen möglicherweise nach Datapack-Reload instabil; Save-/Reload-Migration ist weiter offen.

## Nächster Arbeitsschritt
1. Das vorhandene Datenschema für echte Pflanzenmodelle und Texturen erweitern, statt nur Eichenlaub-Platzhalter darzustellen.
2. Die AgriPlants-Ressourcen nach echten Vanilla-/Mod-Namespaces kontrolliert migrieren; besondere Requirements weiter ausdrücklich abweisen.
3. Vor einer spielbaren Releaseversion die verbleibenden API-Aufrufe durch einen Build und mindestens einen dedizierten Serverstart validieren, sobald der Nutzer Tests wieder zulässt.
