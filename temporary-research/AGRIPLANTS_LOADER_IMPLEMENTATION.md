# Implementierter AgriPlants-Loader – Hand-off
Stand 2026-11-15 | Branch `feature/neoforge-1211-foundation` | Entwicklungsstand: Quellcode geschrieben, **kein Build/Game-Test durchgeführt**.

## Implementierte Dateien
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/PlantDefinition.java`: immutable Pflanzen-Definition inkl. Saaten, Stufen, Chance, Produkte.
- `.../MutationDefinition.java`: Kind-/Elternarten, Chance, symmetrische Elternprüfung.
- `.../AgriPlantsParser.java`: Parser für ausgewählte AgriPlants-JSON-Felder ohne alte AgriCore-Runtime.
- `.../AgriPlantsRegistry.java`: publizierte Maps aus Reload-Daten.
- `.../PlantReloadListener.java` und `.../MutationReloadListener.java`: `SimpleJsonResourceReloadListener` für Server-Datapacks.
- `.../CropGeneticsMod.java`: registriert beide Loader auf `AddReloadListenerEvent` des NeoForge-Game-Bus.
- Zwei Beispiel-Datensätze aus dem AgriPlants-Schema, auf wesentliche Felder reduziert:
  - `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/carrot.json`
  - `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/mutations/carrot.json`
- `neoforge-port/src/test/java/dev/illedrabomb/cropgenetics/data/AgriPlantsParserTest.java`: Parser-/Mutations-/Validierungstests.

## Erreichte Funktion
Der neue Code kann einfache AgriPlants-kompatible JSON-Daten in Java-Objekte überführen. Die Mod registriert einen Reload-Listener für Pflanzen und Mutationen, damit Datapacks Daten zur Laufzeit bereitstellen können.

Achtung: **Daten werden noch nicht mit Crop-Sticks/Genetik-/Wachstumssystem verbunden**. Die Mutation definiert lediglich eine Regel; sie wird im Spiel noch nicht angewendet.

## Annahmen und bekannte Einschränkungen
- Alle Ressourcen unter `data/<namespace>/cropgenetics/plants/*.json` bzw. `data/<namespace>/cropgenetics/mutations/*.json`. Die **alten Ordner der AgriPlants-Submodule werden nicht automatisch übernommen**.
- Einfache `seed_items` mit `type: item` und `useTag: false`, `products` des gleichen Typs, acht oder mehr Stufen nicht erzwungen, sowie unbedingte Mutationen.
- Nicht umgesetzt/teils beim Einlesen übergangen: Boden- und Umweltanforderungen, `callbacks`, `structure_generation`, Grafiken/Modelle, manche Mod-/Tag-/NBT-Saatmechaniken, spezielle Ernte-/Produkttypen. Diese Daten **dürfen nicht als spielmechanisch vollständig unterstützt gelten**.
- Der Parser überspringt bei ungültigen Einträgen die Datei und loggt Fehler. Schutz vor ungültigen Produkt-Mengen/Chance eingebaut.
- Mod-Voraussetzungen werden bei Pflanzen rudimentär geprüft (`agricraft` wird als Legacy-Selbstbezug akzeptiert); bei Mutationen müssen `mods` und Elternreferenzen noch validiert werden.
- Pflanzen-/Mutations-Maps sind derzeit getrennt aktualisiert. Bei Datapack-Reload noch keine atomare gemeinsam validierte Snapshot-Transaktion. Vor gameplay integration die Datenreferenzen prüfen.
- Die Parser-Version ist absichtlich begrenzt statt stillschweigend Vollkompatibilität zu versprechen.
- Java-Version für den neuen Source-Root: 21. NeoForge-Target laut `gradle.properties`: 21.1.265. Gradle-Wrapper im neuen Unterprojekt noch nicht hinzugefügt. Kein `gradle build` ausgeführt.
- Im neuen Unterprojekt wird das alte Forge-1.18.2-Root nicht kompiliert. Lizenz-/Attributionshinweise der tatsächlichen übernommenen Source-/Assets sind vor Packaging vollständig zu prüfen.

## Technischer nächster Schritt
1. **Build herstellen und ausführen** (Gradle-Wrapper Java 21 sowie GitHub Actions), mögliche API-/Mappingfehler korrigieren; dann automatisierte Tests.
2. Genom auf Item Data Components persistieren inkl. Codec und Netzwerkcodec (besonders Mutationen/Rezepte).
3. `CropStickBlock`/`BlockEntity` mit serverseitigem Wachstum und Seed-Konvertierung aus dem Loader verbinden.
4. Ernte-Adapter ohne Dupe-/Loot-Bypass; danach Datapack-Regeln für besondere Pflanzen.
5. Ressourcen aus AgriPlants strukturiert importieren/konvertieren und echte Registry-IDs gegen 1.21.1 prüfen; aktuell nur Karotten-Beispieldaten.
6. Separate Module/Addons für Bewässerung, Gewächshaus und MineColonies.

## Übergabeempfehlung für eine neue KI
Nicht neu entwickeln, was schon als Quellcode im Branch liegt. Zuerst PR #2, `neoforge-port/`, `GENETICS_DEEP_DIVE.md` und diese Datei lesen. Danach durch einen echten Build bestätigen, welche Teile kompilieren. UI-/Runtime-Funktionalität noch nicht behaupten.
