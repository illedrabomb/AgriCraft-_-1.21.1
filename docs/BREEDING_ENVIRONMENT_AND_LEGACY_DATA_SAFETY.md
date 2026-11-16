# AgriCraft NeoForge 1.21.1 – Zuchtumgebung und Legacy-Datapack-Sicherheit

Datum: 2026-11-16 | Entwicklungsbranch: `feature/neoforge-1211-foundation`  
**Status: im Quellcode implementiert, nicht kompiliert und nicht getestet.**

## Vererbte Eigenschaften vor Artenentscheidung

**Betroffene Dateien**
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/genetics/Genome.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CrossBreeding.java`

Vorher: `CrossBreeding.chooseChildSpecies` prüfte beim Selektieren einer möglichen Mutation nur `CropConditions.matchesSoil`. Die endgültige Nachkommen-Genetik wurde erst nach dieser Entscheidung ausgewürfelt. Eine nach Boden geeignete Mutation konnte darum vor der abschließenden Licht-/Strength-Prüfung ausgewählt werden, obwohl der tatsächliche Nachkomme am Ziel nicht wachsen könnte.

Jetzt:
1. Die Genetik eines Nachkommen wird **einmal** aus beiden Eltern über `BreedingEngine.combine(...)` vererbt. Dabei wird temporär eine gültige Eltern-Art-ID verwendet.
2. `chooseChildSpecies` berücksichtigt für Mutationen und alternative Elternarten die **tatsächlichen geerbten Eigenschaften** in `CropConditions.canGrow(level, target, plant, inherited)`. Damit werden Boden, Helligkeit und toleriertes Lichtdefizit berücksichtigt.
3. Nach Auswahl der Art ändert `Genome.withPlantId(...)` nur die Art-ID. Alle geerbten Allele (Growth, Gain, Strength, Fertility und Mutativity) bleiben unverändert. Es gibt keine zweite genetische Ziehung.
4. Der bestehende finale `CropConditions.canGrow`-Check bleibt zur Absicherung erhalten.

**Grenzen:** Bei unverträglichen Umweltbedingungen aller Kandidaten kann die Zucht weiterhin fehlschlagen. Mutationen werden weiter in nach Datapack-ID sortierter Reihenfolge geprüft; dies ist keine gewichtete Gesamtlotterie und keine Nachbildung aller AgriPlants-1.18.2-Mutationsprädikate. Das serverseitige RNG-Verhalten ändert sich nicht nur im Ergebnis, sondern ggf. bei der Anzahl konsumierter Mutations-Zufallswerte (für nun ausgefilterte Kandidaten). Kein Build/Test.

## Originaldaten bei direktem Datapack-Load nicht stillschweigend ignorieren

**Betroffene Datei**
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/AgriPlantsParser.java`

Der Parser lehnt nun zusätzlich direkt im Server-Datapack übernommene, noch nicht portierte Legacy-Semantiken ausdrücklich ab:

- nichtleere `callbacks`, `structure_generation`, `particle_effects`;
- nichtleere `clip_products.products`;
- von den erwarteten Legacy-Defaults abweichende `grass_drop_chance`, `seed_drop_chance`, `seed_drop_bonus`;
- nichtleeres Custom-NBT in Seed- oder Harvest-Item-Einträgen;
- `overridePlanting=true` für Saatgut;
- optionale/bedingte Harvest-Produkte mit `required=false`.

**Grund:** Der Offline-Importer `tools/migrate_agriplants.py` schützt solche Fälle bereits teilweise; beim direkten Laden alter Dateien gab es eine Lücke. Der neue Schutz ist eine bewusste Kompatibilitätsgrenze, **kein neuer Gameplay-Adapter**. In `tools/agriplants_migration_manifest.json` ausdrücklich erlaubte Vereinfachungen werden weiterhin in bereits konvertierten JSONs berücksichtigt; die Konvertierung entfernt die entsprechenden Legacy-Felder. Alte unbearbeitete AgriPlants-Dateien können und sollen mit klarer Diagnose abgelehnt werden.

**Noch offen:** weitere Legacy-JSON-Felder (z. B. Seed-Drop-/Textur-/Journal-Metadaten) vollständig evaluieren, eventuelle ungewollte False Positives und Error-Handling prüfen, Kaktus-Thorns und echte Ökologie-/Mutationsadapter portieren.

## Validierungsbedarf (bis zur Nutzerfreigabe nicht ausführen)

1. Java-21-/NeoForge-Build inkl. Abgleich der Gson-API.
2. Kreuzungen bei Zielboden passend/unpassend, unter normalem und grenzwertigem Licht, Strength 1/10, verschiedenen Elternarten und Mutationskandidaten.
3. Negativfälle für direkte Legacy-JSONs, atomarer Reload bei zurückgewiesenen Arten und Diagnosen.
4. Prüfen, ob mehrfache Mutationsdefinitionen mit unterschiedlicher Chance entsprechend des gewünschten Originalmodells ausgewählt werden.

## Nächster Entwicklungsblock

Echte Adapter für priorisierte Legacy-Pflanzeneigenschaften entwerfen und in die vorhandene CropGenetics-Datapack-Architektur integrieren (Kaktus-Thorns, spezialisierte Boden-/Klima- und Mutationsbedingungen); anschließend mit Referenzdefinitionsdateien dokumentieren und vor Release testen.
