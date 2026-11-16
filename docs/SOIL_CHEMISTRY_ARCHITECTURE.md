# Bodenchemie – datengesteuertes Kernmodell und Adaptergrenze

**Datum:** 2026-11-16. **Branch:** `feature/neoforge-1211-foundation`.  
**Stand:** Quellcode implementiert, statisch auf Anschlussstellen geprüft, **nicht kompiliert und nicht im Spiel getestet**.

## Hintergrund

Die gepinnten Originaldefinitionen aus `AgriCraft/AgriPlants` (Commit `9444e75828f66bd40adabb4aeca2544765408f8a`) enthalten Bodenbedingungen für:
- `soil_humidity` (z. B. Weizen: `wet`, Kaktus: `arid`);
- `soil_acidity` (z. B. Weizen: `slightly_acidic`, Kaktus: `neutral`);
- `soil_nutrients` (z. B. Weizen: `high`, Kaktus: `low`, mit `equal_or_higher`).

Die Originaleinträge besitzen außerdem **`tolerance_factor`**. Deren historische Berechnung, Strength-Abhängigkeit und die tatsächlichen Bodenwertquellen sind **nicht** durch das neue Modell abgedeckt. Dieser Arbeitsschritt ist daher **keine 1:1-Portierung der Legacy-Bodenchemie**.

## Datengesteuerte Regeln

Das neue `SoilChemistryRule` verwendet drei optionale Bedingungen. Wertebereiche des **eigenständigen neuen Modells**:

| Eigenschaft | Aufsteigende Werte |
|---|---|
| `humidity` | `arid`, `dry`, `normal`, `wet`, `saturated` |
| `acidity` | `acidic`, `slightly_acidic`, `neutral`, `slightly_alkaline`, `alkaline` |
| `nutrients` | `low`, `medium`, `high` |

Für jeden Wert kann `comparison` die Ausprägung `equal`, `equal_or_higher` oder `equal_or_lower` haben. **Die Kategorien und die Reihenfolge sind eine vorläufige 1.21.1-Modellsprache**, kein verifiziertes Kopieren der ursprünglichen AgriCore-Ordinalwerte. Speziell für Säure ist „höher“ hier nur die oben dokumentierte Kategorie-Reihenfolge, kein chemischer pH-Vergleich.

### Pflanzen-Datapack

Optionale Eigenschaft innerhalb des bereits vorhandenen `cropgenetics_requirements`-Objekts:

```json
"soil_chemistry": {
  "humidity": {"category": "wet", "comparison": "equal"},
  "acidity": {"category": "slightly_acidic", "comparison": "equal"},
  "nutrients": {"category": "high", "comparison": "equal_or_higher"}
}
```

### Mutations-Datapack

Eigenständige optionale Bedingung auf Mutation-Top-Level:

```json
"cropgenetics_soil_chemistry": {
  "humidity": {"category": "arid", "comparison": "equal"}
}
```

Diese Ausschnitte sind bewusst keine vollständigen Pflanzen- oder Mutationsdefinitionen. Ungültige Kategorien/Vergleiche oder unbekannte Felder werden beim Einlesen zurückgewiesen. Fehlt die Chemiebedingung, ändert sich das Verhalten bestehender Pflanzen nicht.

## Bodenprobe und Spielmechanik

`SoilChemistryAccess.Provider` bietet `Optional<Sample> sample(ServerLevel, BlockPos soilPos)`. Der übergebene Block ist **die Bodenposition direkt unter den Crop Sticks**. Das Sample enthält eine qualitative Feuchtigkeits-, Säure- und Nährstoffstufe.

**Wichtig: Es wurde noch kein konkreter Bodenprobe-Provider registriert.**

- Ohne `soil_chemistry`-Regel: Die übrigen Umweltbedingungen gelten wie zuvor, **auch wenn kein Provider existiert**.
- Mit Chemiebedingung, aber **ohne Provider oder ohne Sample**: Die Regel gilt serverseitig als **nicht erfüllt**, statt Werte zu erfinden.
- Mit Provider und Sample: Die drei optionalen Vergleiche werden gemeinsam geprüft (logisches UND). Dies ist an `CropConditions.growthFactor` angeschlossen und beeinflusst dadurch Pflanzung, Wachstum, Auswahl von Kreuzungseltern und Eignung von Nachkommen.
- `MutationDefinition` besitzt unabhängig von der Zielpflanze dieselbe optionale Bedingung; `CrossBreeding` prüft sie **vor** dem Mutationszufallswurf.
- Der Server entscheidet. Ohne Zugriff auf einen `ServerLevel` wird clientseitig kein historischer Bodenwert simuliert.
- `PlantRequirements` und `MutationDefinition` behalten Konstruktoren ohne Chemieparameter, die `UNRESTRICTED` einsetzen.

### Offline-Migration

`neoforge-port/tools/migrate_agriplants.py` erlaubt ein **ausdrücklich** eingetragenes `soil_chemistry`-Objekt im per-Pflanze-Manifest, einschließlich strikter Kategorien-/Vergleichsprüfung. Alte `soil_humidity`, `soil_acidity`, `soil_nutrients` samt `tolerance_factor` werden **nicht automatisch kopiert oder interpretiert**. Die bisher verlangte Freigabe für Legacy-Umweltvereinfachung bleibt bestehen. Importer wurde nicht ausgeführt.

## Offene Arbeitspakete

1. Die originalen AgriCore-Typen, Werte und Toleranz-/Strength-Formeln aus der gepinnten Quelle analysieren; ordinales Verhalten danach nötigenfalls ändern.
2. Reale Bodenproben bereitstellen: datengesteuerte Bodenprofile, ggf. eigener Boden-/Nährstoffspeicher und Änderung durch Bewässerung/Dünger. Vanilla-Hydration darf nicht stillschweigend als vollständige Chemie gelten.
3. Serverseitige Probe bei Datapack-Reload, Welt-/Chunkwechsel und großen Farmen effizient behandeln. Anbieter-Lifecycle und mögliche externe Integrationen festlegen.
4. Die Core-Logik und Gson/NeoForge-APIs kompilieren sowie Client, Dedicated Server, Multiplayer und Kreuzungs-Gameplay prüfen – **erst nach ausdrücklicher Testfreigabe**.
5. Die originale AgriPlants-Landschaft/Mutationen weiter klassifizieren. Das geplante MineColonies-Addon soll später eine saubere Schnittstelle zum Bodensystem verwenden können.

## Geänderte Quelldateien

- `data/SoilChemistryRule.java` (neu)
- `content/SoilChemistryAccess.java` (neu)
- `data/PlantRequirements.java`
- `data/MutationDefinition.java`
- `data/AgriPlantsParser.java`
- `content/CropConditions.java`
- `content/CrossBreeding.java`
- `neoforge-port/tools/migrate_agriplants.py`

Java-Pfade sind relativ zu `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`.
