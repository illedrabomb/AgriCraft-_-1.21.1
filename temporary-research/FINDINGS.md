# Bestandsanalyse AgriCraft-Fork – Forschungsstand 2026-11-15

## 1. Gesicherter Repository-Zustand
Repository: https://github.com/illedrabomb/AgriCraft-_-1.21.1 ; Default-Branch `master`.
- `build.gradle`: ForgeGradle 5.1+, Forge `1.18.2-40.2.4`, Java 17, Integrationen auf MC 1.18.2.
- `mod.properties`: `version_minecraft = 1.18.2`; AgriCraft 3.1.6.
- `gradle/wrapper/gradle-wrapper.properties`: Gradle 7.4.
- `src/main/resources/META-INF/mods.toml`: `loaderVersion="[40,)"`, Forge-/InfinityLib-Abhängigkeit.
- `settings.gradle` und `.gitmodules`: eingebundene AgriCore-/InfinityLib-Submodule; `AgriPlants`-Submodule.
- GitHub-Git-Tree: 375 Java-Dateien unter `src/main/java` (ohne Submodule); keine Aussage darüber, wie viel automatisch kompatibel ist.
- `src/main/java/com/infinityraider/agricraft/AgriCraft.java` erbt von `com.infinityraider.infinitylib.InfinityMod`, nutzt InfinityLib-Netzwerk und Modell-Loader.

## 2. Konkrete Ankerpunkte (Quellcode eingesehen)
Alle Pfade relativ zu `src/main/java/com/infinityraider/agricraft/`:
- `impl/v1/genetics/AgriCrossBreedEngine.java`: filtert gültige, ausgewachsene und ggf. fruchtbare Nachbarn; sortiert nach Fertilität; bei einem geeigneten Elternteil Klon, bei zwei Elternteilen Kombination; Gene/Allele werden vererbt oder mutiert. **Wertvolle Referenzlogik.**
- `impl/v1/genetics/AgriGenome.java`: Genom als Map von Gene-Paaren, dominant/rezessiv, serialisiert in NBT-Listen; erzeugt Seed-Stacks und abgeleitete Stats. **Logik portierbar; Speicherung für NeoForge 1.21.1 über geeignete moderne Serialisierung und Item Data Components neu entwerfen.**
- `impl/v1/genetics/AgriMutationHandler.java`: separate Mutatoren für Pflanzenart und Zahlenstatistik; Mutationstrigger mit PASS/FORBID/FORCE, Erfolgswahrscheinlichkeit; Stat-Mutation hängt von Mutativity ab. **Nicht stillschweigend auf drei Stats reduzieren, ohne Balancing zu prüfen.**
- `content/core/TileEntityCrop.java`: Crop-BlockEntity für Genom/Wachstum/Weeds, Nachbarschafts-Cache, Forge `LazyOptional`, altes ModelData; **stark API-gebunden, größtenteils Anpassung/Neuentwurf.**
- `handler/VanillaSeedConversionHandler.java`: Vanilla-Saatkonvertierung über `AgriApi.getGenomeAdapterizer()`, Ausnahme-Set, Rechtsklick-Event, optionales Überschreiben der normalen Farmmechanik. **Konzept für Hybrid-Strategie wiederverwenden.**
- `plugins/minecraft/SeedWrapper.java`: erkennt vorhandene AgriCraft-Samen oder registrierte Pflanzensamen `plant.isSeedItem(stack)`; erzeugt Standardgenom. **Bestehende Adapter-Idee, aber keine nachgewiesene universelle Kompatibilität.**
- `content/world/village/WorkAtCropSticks.java`: vorhandene Villager-Workstation-Logik, entfernt Weeds. **Nicht mit einem fertigen Züchterhaus/Handelsfeature gleichsetzen.**
Weitere wichtige Kandidaten (im Tree identifiziert, noch NICHT im Detail geprüft): `impl/v1/genetics/AgriGenePair.java`, `GeneStat.java`, `GeneSpecies.java`, `AgriMutationRegistry.java`, `content/core/BlockCrop.java`, `ItemDynamicAgriSeed.java`, `ItemCropSticks.java`, `api/v1/stat/IAgriStatsMap.java`, `plugins/*`.

## 3. Ressourcen
Tree enthält vorhandene Modelle z.B. `assets/agricraft/models/block/crop/wood_cross.json`, `iron_cross.json`, `obsidian_cross.json`, Saat-/Analyzer-Modelle sowie zahlreiche Modelle für Pflanzen anderer Mods in `assets/agricraft/models/seed/`. Für 1.21.1 müssen Itemmodelle/Resource-Formate und tatsächliche Mod-Verfügbarkeit geprüft werden. `lib/AgriPlants` ist als Submodule referenziert; Inhalte noch nicht inventarisiert.

## 4. Wiederverwendung – vorsichtige qualitative Einschätzung
- **Hoch:** Zucht-, Vererbungs-, Mutationsregeln, Datendefinitionen, ggf. Texturen/Modelle, Lizenzkonforme Nutzung.
- **Mittel:** Crop Sticks, Saatgutlogik, Analyse/Aufbewahrung, Pflanzen-/Villager-Mechaniken – Spielmechanik erhalten, technische Integration wahrscheinlich neu.
- **Gering (direkt):** ForgeGradle-Konfiguration, InfinityLib-Basisklassen, alte Netzwerk-/Rendering-/Capability-Systeme und versionsgebundene Integrationen.
- Keine belastbare Prozentzahl oder Token-/Zeitkalkulation ohne Kompilierungsversuche und Klassifikation aller Abhängigkeiten.

## 5. Architekturentscheidung zur Prüfung
1. Native NeoForge 21.1.265-Registrierungen und Java 21; Kernmod unabhängig von portiertem InfinityLib entwerfen.
2. Genom/Werte als getrennte, testbare reine Java-Domäne modellieren, erst danach Bindung an BlockEntity, Item Data Components und Netzwerk.
3. Registry/Datapack für Pflanzendefinitionen und Mutationstabellen, Auto-Erkennung für reguläre Seeds, explizite Adapter für Spezialpflanzen.
4. Möglichst unveränderte Standard-Samen erhalten; konvertierbare genetische Zuchtsamen für Crop-Sticks. Cross-Mod-Blocks und Drops gründlich testen.
5. Optionales Addon-API für Irrigation und Greenhouse; keine harte Abhängigkeit der Hauptmod.

## 6. Offene Prüfungen / unmittelbare nächste Schritte
- Alle stat- und gene-bezogenen Klassen lesen; klären welche Stats sichtbar und welche intern für Kreuzung verwendet werden.
- Analysieren, welche `AgriCore`- und `InfinityLib`-Funktionen tatsächlich im Kern nötig sind (auch Submodule rekursiv).
- Die Pflanzen-/Mutation-/Soil-JSON-Schemas und AgriPlants-Daten inventarisieren.
- Alte Ressourcen-/Modelle auf Minecraft 1.21.1 prüfen.
- Dorfstruktur, Saatgut-Loot und Trades nachweisen; nicht ohne Fundstellen als bereits fertig vorhanden behandeln.
- JEI/Jade/Mystical Agriculture/Farmer's Delight sowie sonstige Zielmods nur gegen konkrete NeoForge-1.21.1-Versionen testen.
- Eine saubere NeoForge-Testumgebung erzeugen, Kompilierfehler klassifizieren, Tests für Vererbungswahrscheinlichkeiten/10-10-10 ausführen.
- README und Credits nach bestätigter Architektur aktualisieren; Ursprungsprojekt und MIT-Autorenschaft nicht entfernen.

## 7. Gültigkeit
Diese Datei fasst nur geprüfte Teilbereiche des Quellcodes und erkennbare Pfade zusammen. Keine vollständige Auditierung, kein funktionierender Port, keine Aussage „alle Seeds unterstützt“.
