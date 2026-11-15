# Soil-/Lichtanforderungen – Entwicklungsstand
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation` | **Kein Build, keine Tests** gemäß Nutzerentscheidung.

## Implementiert im produktiven NeoForge-Quellcode
- `PlantRequirements.java`: eindeutige Bedingungen mit `minLight`/`maxLight` (0–15), **entweder** `soilBlock` **oder** `soilTag`, beide als namespaced ID.
- `PlantDefinition.java`: hat nun ein validiertes `PlantRequirements`-Feld. Nebenbei wurde ein schon vorher vorhandener offensichtlicher Syntaxfehler im HarvestProduct-Validator (fehlende Klammer) behoben, ohne einen Build zu starten.
- `AgriPlantsParser.java`: liest die neuen `cropgenetics_requirements` aus einer Datapack-Pflanzendefinition. Falls keine Regeln angegeben sind, gilt **Vanilla-Farmland, Licht 9–15**. Nur alte `requirement`-Objekte ohne unimplementierte Felder werden vorläufig verarbeitet. Alte AgriPlants-Felder für Bodenfeuchte, Säuregehalt, Nährstoffe, Biome, Dimensionen, Jahreszeiten, Bedingungen und Fluide werden **absichtlich abgelehnt**, statt unter falschen Voraussetzungen zu wachsen. Auch unbekannte Felder werden abgelehnt.
- `CropConditions.java`: prüft den tatsächlichen Block unter den Stäben via registrierter Block-ID oder Minecraft-Block-Tag und prüft lokale Helligkeit; fehlende Blöcke sind nicht zulässig.
- `CropSticksBlock.java`: Crop-Sticks können weiterhin auf Farmland stehen; zusätzlich auf Böden, die von geladenen Pflanzendefinitionen erlaubt sind. Beim Einpflanzen genetischer und normaler Samen sowie jedem Wachstumstick müssen die Bedingungen der jeweiligen Pflanze erfüllt sein.
- `CrossBreeding.java`: nur Eltern, die ihre Bedingungen erfüllen, nehmen teil; geklonte und gekreuzte Nachkommen werden nur auf einem für die Kindpflanze passenden Feld erzeugt.
- `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/carrot.json`: explizite Beispielkonfiguration `cropgenetics_requirements`.

## Beispiel neues Format
```json
"cropgenetics_requirements": {
  "min_light": 9,
  "max_light": 15,
  "soil_block": "minecraft:farmland"
}
```

Für Mod-Unterstützung alternativ z.B. `"soil_tag": "deinmod:plantable_soil"` (nur wenn dieser Tag wirklich existiert). **Die neue Block-Tag-Funktion ist keine vollständige Erfüllung der alten AgriPlants-Soilchemie.** Alte Fremdmod-Datensätze sind weiterhin zu migrieren.

## Noch nicht implementiert
- Originale AgriPlants-Toleranzfaktoren (Strength-Einfluss auf negative Bedingungen), Feuchte/Säure/Nährstoffqualität, Jahreszeiten, Biome, Dimensionen, Spezial-Blockstates, Flüssigkeiten, Mod-Crop-Adaptern.
- Eingebautes Warnsystem für nicht geladene Soil-IDs/Tags (aktuell ist die Bedingung einfach falsch).
- Einfluss der Anforderungen auf **Geschwindigkeit** statt nur erlaubte/verbotene Pflanzung und Wachstum.
- Synchronisation/Migration gespeicherter Pflanzensaves bei Datapack-Neuladen und Änderung der Bodenregeln.
- Ausführbare/Spieltests und Neukompilierung (auf Wunsch pausiert).

## Wichtiges technisches Risiko
`CropSticksBlock.canSurvive` prüft alle im Moment geladenen Definitionen für Sonderböden. Nach Datapack-Reload können Definitionsänderungen Stäbe destabilisieren. Die getrennten Reload-Listener für Pflanzen und Mutation bilden noch keinen gemeinsam validierten Snapshot.

## Nächster sinnvoller Schritt
1. Stärke/Strength-Toleranz für schlechte Licht- und Bodenbedingungen in das Wachstum einarbeiten (ohne originale Spezialbodendaten zu erfinden).
2. Die echten 1.18.2-AgriPlants-Dateien pro Mod migrieren und die jeweiligen IDs/Anforderungen auf 1.21.1 überprüfen.
3. Artabhängiges Rendering der Pflanzen einführen.
4. Später nach Nutzerfreigabe Build, Client und Dedicated Server prüfen.
