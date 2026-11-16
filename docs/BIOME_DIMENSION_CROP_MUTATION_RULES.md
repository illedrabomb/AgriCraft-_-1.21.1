# Standortbedingungen für Pflanzen und Mutationen (NeoForge 1.21.1)

**Stand:** 2026-11-16  
**Branch:** `feature/neoforge-1211-foundation`  
**Nachweis:** Code im Entwicklungsbranch, **kein Build, Test, Minecraft-Start oder Importer-Aufruf**.

## Umgesetzte Funktionalität

Die bestehenden Bodenblock-/Bodentag-/Lichtbedingungen bleiben erhalten. `PlantRequirements` erhält vier **optionale exakte Registry-ID-Listen**: `allowedBiomes`, `blockedBiomes`, `allowedDimensions`, `blockedDimensions`.

`CropConditions.growthFactor` wertet diese Regeln zusätzlich zu Boden und Licht aus. Dadurch wirken sie auf Saatgut-Pflanzung (über `canGrow`), fortlaufendes Wachstum, die Verfügbarkeit von Kreuzungseltern und die Standortzulässigkeit der Nachkommen. Ohne konfigurierte Listen ist das Verhalten unverändert. Ein nicht auflösbares Biome-ID/Level wird bei *konfigurierter* Standortbedingung nicht positiv interpretiert. Strength kann nur den konfigurierten Lichtdefizit-Spielraum beeinflussen; **Biome/Dimensionen und Boden sind harte Bedingungen**.

`MutationDefinition` enthält unabhängig davon dieselben vier Listen. `CrossBreeding.chooseChildSpecies` filtert eine Mutation anhand dieser *zusätzlichen* Bedingungen am Kreuzungsziel, bevor deren Zufallsauswahl ausgeführt wird. Damit können Mutationen standortgebunden sein, während die Zielpflanze grundsätzlich auch anderswo wächst.

**Eingabeformat für Pflanzen:** innerhalb von `cropgenetics_requirements`:

```json
{
  "soil_block": "minecraft:farmland",
  "min_light": 9,
  "max_light": 15,
  "light_tolerance": 0,
  "allowed_biomes": ["minecraft:plains"],
  "blocked_dimensions": ["minecraft:the_nether"]
}
```

**Eingabeformat für Mutationen:** optionale Felder auf oberster Ebene der Mutation-JSON:

```json
{
  "cropgenetics_allowed_biomes": ["minecraft:plains"],
  "cropgenetics_blocked_dimensions": ["minecraft:the_nether"]
}
```

*Die Beispiele zeigen nur die **neuen Felder**, nicht vollständige Datapack-Definitionen.*

**Semantik:**
- Leere oder fehlende Allow-Listen: keine positive Einschränkung.
- Nichtleere Allow-Listen: aktuelle Registry-ID muss enthalten sein.
- Block-Listen: aktuelle Registry-ID darf nicht enthalten sein.
- Überschneidungen zwischen Allow-/Block-Liste derselben Kategorie werden abgelehnt.
- Höchstens 64 eindeutige, namespaced IDs pro Liste (max. 128 Zeichen je ID).
- **Exakte IDs**, keine Biome-Tags, keine Seasons, keine automatische Übernahme des 1.18.2-`ignoreFromStrength`-Mechanismus.

## Migration alter AgriPlants-Definitionen

`tools/migrate_agriplants.py` kann die vier Listen ausdrücklich aus dem per-Art-Manifest übernehmen. Er **leitet keine** Listen aus alten `requirement.biomes`/`requirement.dimensions`-Objekten ab, weil die damalige `blacklist`-/Strength-Semantik anders ist. Bei Originalanforderungen muss nach wie vor `acknowledge_legacy_requirements: true` gesetzt sein. Der Importer wurde nicht ausgeführt.

## Betroffene Implementierungen

- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/PlantRequirements.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/MutationDefinition.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/AgriPlantsParser.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropConditions.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CrossBreeding.java`
- `neoforge-port/tools/migrate_agriplants.py`

Zur Kompatibilität mit bisherigen Java-Aufrufstellen besitzen beide erweiterten Records weiterhin Konstruktoren mit der alten Parameterliste. Diese setzen Standortlisten leer.

## Was noch **nicht** implementiert oder verifiziert ist

1. Originale Bodenchemie: Feuchtigkeit, Säure, Nährstoffe und die zugehörigen Toleranzfaktoren.
2. Seasons-/Wetter-/Fluid- und komplexe originale Mutationsprädikate.
3. AgriPlants-`ignoreFromStrength` für Biome/Dimensionen, Biome-Tags und weitere dynamische Umweltregeln.
4. Prüfung, ob alle Java-/NeoForge-Signaturen kompiliert werden und ob Client/Server/Datapack-Reload exakt wie beschrieben funktionieren.
5. Negative Fälle für modded Registry-IDs, veraltete Saved-Data und Mutationen nach Datapack-Änderungen; Performance in großen Farmen.

**Nächster Schritt:** Statische API-Prüfung und anschließend eigenständige Datenmodelle für weitere Bedingungen (zuerst explizite Fluid-/Saisonregeln und danach konkrete Bodenchemie), bevor Alt-Daten importiert werden. Builds/Tests erst nach Nutzerfreigabe.
