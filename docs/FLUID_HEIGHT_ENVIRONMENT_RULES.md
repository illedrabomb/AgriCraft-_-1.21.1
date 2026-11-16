# AgriCraft NeoForge 1.21.1 – Fluid- und Höhenfilter

Stand: 2026-11-16 | Entwicklungsbranch: `feature/neoforge-1211-foundation`  
**Umsetzungsstatus: ausschließlich Quellcode, nicht kompiliert und nicht getestet.**

## Neue optionale Bedingungen

Ein gemeinsames `EnvironmentFilter`-Datenmodell speichert optionale `minY`, `maxY` und `fluidId`. Ohne Angaben gelten keine neuen Einschränkungen. Die Höhenwerte sind **inklusive** und messen die Y-Koordinate der **Pflanzenposition**, nicht des Bodenblocks. Werte sind auf -2048 bis 2048 begrenzt, `minY <= maxY` wird geprüft. Eine `fluidId` ist eine exakte namespaced Registry-ID des **FluidState am Pflanzenblock**, beispielsweise `minecraft:empty`. Keine Fluid-Tags, Fließrichtung, Nachbarflüssigkeiten oder Legacy-Fluidregeln.

In `cropgenetics_requirements` einer Pflanze sind zusätzlich optional:
```json
{
  "min_y": 40,
  "max_y": 120,
  "fluid_id": "minecraft:empty"
}
```

Bei Mutationen sind unabhängig von den Regeln der Zielpflanze diese Top-Level-Felder optional:
```json
{
  "cropgenetics_min_y": 40,
  "cropgenetics_max_y": 120,
  "cropgenetics_fluid_id": "minecraft:empty"
}
```

Dies sind **Feldbeispiele**, keine vollständigen Datapack-Dateien.

## Einbindung in die Spielmechanik

- `CropConditions.growthFactor` prüft den neuen Filter zusätzlich zu Boden, Biomen, Dimensionen und Licht. Dadurch wirken die neuen Anforderungen bei Saatgutannahme, Pflanzenwachstum, Prüfung der Kreuzungseltern und Eignung einer Nachkommenart.
- `CrossBreeding.chooseChildSpecies` prüft den **eigenen** Mutations-`EnvironmentFilter` am Ziel vor der zufälligen Mutationsauswahl. Die eigenständige `CropConditions.canGrow`-Prüfung für die Zielpflanze bleibt erhalten.
- Die ursprünglichen Konstruktoren von `PlantRequirements` und `MutationDefinition` bleiben als Überladungen bestehen und verwenden `EnvironmentFilter.UNRESTRICTED`.
- `data/PlantRegistryValidator.java`
- `data/CropDataReloadListener.java`
- `tools/migrate_agriplants.py` kann explizite neue Werte `min_y`, `max_y`, `fluid_id` aus dem Manifest übernehmen. Aus dem Legacy-AgriPlants-`requirement.fluid`-Objekt wird ausdrücklich nichts automatisch abgeleitet.

## Wichtige Grenzen

1. **Fluidbedingungen sind noch kein Wasserpflanzen-Adapter:** Crop Sticks unterstützen hiermit nicht automatisch Waterlogging, Wasser-Platzierung oder Minecraft-Fluidphysik. Eine `minecraft:water`-Bedingung kann deshalb für einen gewöhnlichen Crop-Sticks-Block unerfüllbar sein; das ist kein Nachweis funktionierender Wasserpflanzen.
2. **Keine ursprüngliche Bodenchemie:** `soil_humidity`, `soil_acidity`, `soil_nutrients`, Jahreszeiten und Legacy-Toleranzformeln bleiben weiterhin offen.
3. **Fluid-ID-Referenzen werden geprüft:** `PlantRegistryValidator` meldet unbekannte Flüssigkeiten in Pflanzenanforderungen; `CropDataReloadListener` weist Mutationen mit unbekannter Fluid-ID als ungültig zurück. Die Prüfung wurde nur im Code ergänzt und bislang nicht ausgeführt oder kompiliert.
4. **Keine Build-/Gameplay-Bestätigung:** Java-Signaturen, Reload-Verhalten, dedicated server und Mod-Kompatibilität noch verifizieren.
5. Die Unterstützung ist optional, datengesteuert und verändert keine mitgelieferten Vanilla-Pflanzendefinitionen, solange keine neuen Filter eingetragen werden.

## Geänderte Dateien

- `data/EnvironmentFilter.java` (neu)
- `data/PlantRequirements.java`
- `data/MutationDefinition.java`
- `data/AgriPlantsParser.java`
- `content/CropConditions.java`
- `content/CrossBreeding.java`
- `tools/migrate_agriplants.py`

Alle Java-Pfade sind relativ zu `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`; der Importer liegt unter `neoforge-port/tools/`.

**Nächster Entwicklungsblock:** Das ursprüngliche Bodenchemie-Modell und einen expliziten saisonalen Adapter konzipieren und implementieren. Testläufe weiterhin nur auf Nutzerfreigabe.
