# Datapack-Reload-, Mutations- und BlockEntity-Audit
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Nur statische Quellcodeprüfung. Keine Builds, Tests, GitHub Actions oder Spielstarts durchgeführt, wie vom Nutzer verlangt.**

## In diesem Arbeitsschritt committet

1. `data/MutationDefinition.java` prüft Kind- und beide Eltern-Plant-IDs nun mit dem gleichen namespaced Format wie `Genome` und dem 128-Zeichen-Limit der Netzwerkdarstellung. Damit führen ungültige IDs zu einem Parserfehler statt zu still unbrauchbaren Mutationen.
2. `data/MutationReloadListener.java` verwirft Mutationen mit nicht registriertem/disabled Kind oder Elternteil. Fehlende `mods`-Abhängigkeiten werden ebenso berücksichtigt (das historische `agricraft`-Self-Dependency gilt als durch Crop Genetics erfüllt). Andere gültige Mutationen bleiben ladbar.
3. `CropGeneticsMod.java` liefert die reale `ModList.get().isLoaded(mod)`-Predicate auch an `MutationReloadListener`.
4. `data/PlantReloadListener.java` verarbeitet die Ressourcenschlüssel in stabiler Reihenfolge und **deaktiviert** Pflanzen-IDs mit mehreren konkurrierenden Definitionen. Vorher blieb die zuerst angetroffene Definition aktiv, obwohl ein Duplicate nur als Fehler geloggt wurde. Der Seed-Reverse-Index wird anschließend aus den wirklich aktiven Pflanzen aufgebaut.
5. `content/CrossBreeding.java` prüft `ServerLevel.hasChunkAt` vor Zugriff auf eine Nachbar-BlockEntity. Eine zufällige Kreuzung darf keine Chunks neu laden, nur um Nachbarn zu suchen.

## Bewusst nicht verändert
- Die `CropSticksBlockEntity` serialisiert `Genome`, `GrowthStage`, `FruitReady`, `FruitDirection` und `FruitToken` bereits in einem BlockEntity-NBT. Der Client bekommt über `getUpdateTag` und `getUpdatePacket` entsprechende Daten. Bei Gameplay-Änderungen ruft `syncState` `setChanged`, `level.setBlock` (sofern BlockState anders) und `sendBlockUpdated` auf. Diese Mechanik wurde hier rein statisch geprüft, aber **nicht in Minecraft nachgewiesen**.
- Die `AgriPlantsRegistry` publiziert bereits einen unveränderlichen Pflanzenkatalog samt Reverse-Seed-Index unter einer einzelnen `volatile`-Referenz, wodurch die Seed-Zuordnung während eines Plant-Reloads konsistent bleibt.
- Die `AgriPlantsParser` lehnt unimplementierte Legacy-Soil-/Season-/Fluidregeln weiter explizit ab. Neue Datapacks nutzen die unterstützte `cropgenetics_requirements`-Syntax.
- Unbedingte Mutationen bleiben die derzeit einzige unterstützte Form; spezielle Biome/Dimension-/Saison-Bedingungen werden **nicht** als erfolgreich interpretiert.

## Offene technische Risiken
- **Reload-Reihenfolge:** `MutationReloadListener` prüft gegen den aktuell publizierten Pflanzenkatalog. Die Mod registriert erst Plant-, dann Mutation-Listener im selben `AddReloadListenerEvent`, doch die exakte NeoForge-Reload-Ausführungsreihenfolge und Parallelität muss bei späterer Ausführung geprüft werden. Wenn das Lifecycle-API keinen garantierten sequenziellen Apply-Zugriff gibt, sollte ein gemeinsamer Reload-Listener oder ein atomarer Datensnapshot eingeführt werden. Bis dahin ist die Reihenfolge eine Architekturannahme.
- **Datenfehler und schon gespeicherte Genetik:** Wenn ein Datapack eine bisher existierende Pflanzenart entfernt, bleiben deren Genome in der Welt gespeichert. Diese dürfen nicht automatisch einer anderen Pflanze zugeordnet werden; der Code führt keine solche Migration aus. Das Ernte-/Wachstumsverhalten bleibt für nicht registrierte Arten deaktiviert.
- **Doppelte Daten-IDs:** Das fail-closed-Verhalten bei mehrfacher `plant.id`-Definition ist gewollt. Die dabei betroffene Art erscheint erst wieder, wenn der Konflikt behoben wurde. Unterschiedliche Ressourcen mit identischer ResourceLocation werden bereits von Minecraft-Ressourcenprioritäten behandelt und erscheinen nicht als zwei Einträge in der Listener-Map.
- **Speicherzustand nach altem Save:** Beim Laden von `CropSticksBlockEntity` wird das NBT gelesen; wenn sichtbarer BlockState und NBT aus einer älteren Version divergieren, gibt es aktuell noch keinen dokumentierten vollständigen Migration-/Reconcile-Hook, der automatisch das Modell repariert. Keine zusätzlichen riskanten Änderungen im NBT-Load-Event ohne Test.
- **Client-Resource-Pack und serverseitige Datapacks** sind getrennt. Mutations-/Plant-Reload überträgt weiterhin keine Texturdateien an entfernte Clients.
- **Netzwerk-IDs und Worldsave:** Das bestehende Limit von 128 Zeichen für Genome-IDs kann besonders lange modded IDs verhindern. Diese Grenze ist beabsichtigt und sollte bei umfangreichen Packs berücksichtigt werden.
- Die Source-Korrekturen wurden **nicht kompiliert oder ausgeführt**. Syntax/API-Verträglichkeit und tatsächliches Spielverhalten sind noch unbestätigt.

## Nächster sinnvoller Schritt
Eine einheitliche, atomare Datapack-Reload-Pipeline für Pflanzen und Mutationen würde die noch bestehende Reload-Reihenfolge-Abhängigkeit eliminieren. Danach können Fehlerlogs, vorhandene Genome und Ressourcenpacks konsistenter behandelt werden. Keine weiteren Massenimporte bis die Datenbasis stabil ist.
