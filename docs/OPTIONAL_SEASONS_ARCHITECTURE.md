# Saisonale Pflanzenregeln und optionaler Provider

**Stand:** 2026-11-16. **Zweig:** `feature/neoforge-1211-foundation`.  
**Status: Implementierter Quellcode, nicht kompiliert, nicht ausgeführt, nicht getestet.**

## Architekturentscheidung

Die NeoForge-Portierung erhält **keinen eigenen Jahreszeitenkalender**. Saisonale Bedingungen sind optional: Ohne registrierten Saison-Provider werden sie bewusst ignoriert. Ein kompatibler Adapter (z. B. für eine nachweislich passende Serene-Seasons-Version) ist **noch nicht implementiert** und darf erst nach API-/Versionsprüfung hinzugefügt werden. Es besteht keine Pflichtabhängigkeit auf einen Jahreszeiten-Mod.

Die neue `SeasonAccess`-Schnittstelle nimmt `Provider.currentSeason(ServerLevel, BlockPos)` entgegen. Adapter dürfen sich über `install` anmelden und bei Deinstallation/Abschaltung über `uninstall` entfernen; die Referenz ist volatil. Falls ein registrierter Provider am Server eine unbekannte Jahreszeit (`Optional.empty()`) liefert, werden eingeschränkte Pflanzen/Mutationen **nicht** zugelassen (fail closed). Ohne Provider werden saisonale Regeln vollständig ignoriert. Clientseitig wird keine Saison lokal simuliert; der Server bleibt autoritativ.

## Datenformat

`cropgenetics_requirements` einer Pflanze kann zusätzlich enthalten:

```json
"allowed_seasons": ["spring", "summer", "autumn"]
```

Eine Mutation kann unabhängig von ihrer Zielpflanze das Top-Level-Feld enthalten:

```json
"cropgenetics_allowed_seasons": ["winter"]
```

Nur `spring`, `summer`, `autumn`, `winter` sind zugelassen, keine Duplikate und höchstens vier Einträge. Eine leere/fehlende Liste lässt Wachstum bzw. Mutation unabhängig von der Jahreszeit zu.

`AgriPlantsParser` liest diese Felder, `PlantRequirements` und `MutationDefinition` tragen `SeasonRule`. Die bisherigen Konstruktoren bleiben für ältere Aufrufstellen erhalten. `CropConditions.growthFactor` prüft die Pflanzenliste über `SeasonAccess`; damit wirkt sie auf Pflanzung, Wachstum, Elternauswahl und die Eignung von Nachkommen. `CrossBreeding.chooseChildSpecies` prüft die zusätzliche Mutationsliste.

Der Offline-Importer `tools/migrate_agriplants.py` akzeptiert saisonale Vorgaben nur bei ausdrücklich angegebenem `allowed_seasons` im Manifest. Er interpretiert nicht automatisch die originalen `requirement.seasons`-Einträge. Die vorhandenen Beispielpflanzen erhalten dadurch **noch keine** neuen saisonalen Einschränkungen.

## Bodenchemie: geplanter Adapter (noch nicht implementiert)

Die alte AgriPlants-Definition beschreibt u. a. `soil_humidity`, `soil_acidity` und `soil_nutrients` einschließlich qualitativer Vergleichsoperatoren und Toleranzfaktoren. Vanilla-Minecraft enthält dafür keine äquivalente Bodenchemie-Registry. Eine direkte Übernahme als einfache Blockliste oder Hydration-Bedingung wäre sachlich falsch.

Die Portierung sollte hierfür ein **separates Bodenprobe-Datenmodell** und eine optionale serverseitige Bodenwert-Quelle erhalten. Vor einem Gameplay-Anschluss sind zu klären: Wertebereiche, Bodenposition, Aktualisierung und Speicherung, Abhängigkeit von Farmblöcken/Anreicherungen, Strength-Toleranzen, Verhalten bei fehlenden Mod-Integrationen und Abgrenzung zu Vanilla-Hydration. Bis dahin werden alte Chemiebedingungen weiter explizit vom direkten Parser abgelehnt bzw. nur mit Manifest-Freigabe bewusst vereinfacht.

## Betroffene Dateien

- `data/SeasonRule.java` (neu)
- `content/SeasonAccess.java` (neu)
- `data/PlantRequirements.java`
- `data/MutationDefinition.java`
- `data/AgriPlantsParser.java`
- `content/CropConditions.java`
- `content/CrossBreeding.java`
- `tools/migrate_agriplants.py`

Java-Pfade liegen unter `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`.

## Validierungsbedarf und nächste Schritte

1. Java-/NeoForge-Signaturen kompilieren und die Routen server/client bei Seed-Platzierung/Datapack-Reload prüfen (**noch nicht freigegeben**).
2. Serene-Seasons-Version/API passend zur Minecraft-1.21.1-NeoForge-Basis recherchieren; optionales Modul ohne harte Runtime-Abhängigkeit umsetzen.
3. Saisonwechsel, nicht auflösbare Saison, Biome und Mutation unter einer echten Provider-Integration überprüfen.
4. Bodenchemie-Datenmodell/Provider fachlich aus dem gepinnten AgriCraft-/AgriPlants-Quellcode herleiten.
