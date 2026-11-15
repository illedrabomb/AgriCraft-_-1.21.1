# Gemeinsames Datapack-Reload für Pflanzen und Mutationen
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Zweck
Zuvor hatten `PlantReloadListener` und `MutationReloadListener` getrennte Lade-/Apply-Schritte. Eine Mutation konnte damit gegen einen alten Pflanzenkatalog geprüft oder zeitweise getrennt von ihrem neuen Pflanzenbestand veröffentlicht werden.

## Jetzt im Repository umgesetzt
- `CropDataReloadListener extends SimplePreparableReloadListener<Prepared>` ist der **einzige** für Crop-Daten registrierte Server-Reload-Listener.
- `prepare` liest beide Verzeichnisse aus demselben `ResourceManager`:
  - `data/<namespace>/cropgenetics/plants/*.json`
  - `data/<namespace>/cropgenetics/mutations/*.json`
  Er liest die JSON-Ressourcen im Hintergrund ein, ohne den aktiven Pflanzenkatalog zu verändern.
- `apply` validiert erst Pflanzen (aktiviert, Mod-Abhängigkeiten verfügbar, Form korrekt, doppelte `plant.id`-Werte ausgeschlossen), anschließend Mutationen (aktiviert, Mod-Abhängigkeiten verfügbar, Kind und beide Eltern **im selben neuen Pflanzenstand** vorhanden). Fehlerhafte Einzeldefinitionen werden geloggt und übersprungen.
- `AgriPlantsRegistry.replaceAll(plants, mutations)` stellt einen neuen unveränderlichen `Snapshot` mit Pflanzen, Mutationen, eindeutigem Seed-Reverse-Index und mehrdeutigen Seed-IDs zusammen und veröffentlicht diesen über **eine** `volatile`-Referenz.
- `CropGeneticsMod` registriert nur `CropDataReloadListener`. Die alten separaten Loader wurden gelöscht, damit sie nicht versehentlich doch wieder unabhängig publizieren.
- `CrossBreeding.attempt` erfasst eine `AgriPlantsRegistry.Snapshot`-Referenz und benutzt innerhalb desselben Züchtungsversuchs den darin enthaltenen Pflanzen- und Mutationsstand. So kann eine Reload-Transition diesen Versuch nicht auf widersprüchliche Map-Versionen aufteilen.

## Verhalten bei Datenfehlern
- Die schon vorhandene, absichtlich restriktive AgriPlants-Parserlogik wird weiterverwendet. Nicht unterstützte Legacy-Regeln, bedingte Mutationen und Sonderprodukte bleiben ausgeschlossen.
- Zwei verschiedene Ressourcen mit derselben `plant.id` deaktivieren diese Art insgesamt. Eine Mutation, die sie referenziert, wird beim **gleichen Reload** ebenfalls entfernt.
- Mehrdeutige normale Seed-Item-Mappings werden nicht in den Reverse-Index aufgenommen. Genetische Samen speichern ihre konkrete Genome-ID weiter.
- Ein fehlender optionaler Mod deaktiviert die jeweilige Definition für diesen Reload; `agricraft` gilt als Self-Dependency, da die Quell-Daten aus dem alten AgriPlants-Projekt stammen.
- Eine bereits im Welt-NBT gespeicherte Pflanze einer vorübergehend deaktivierten Art behält ihre Genome-Daten; ihr Wachstum und die Ernte bleiben ausgesetzt, solange die Art im Katalog fehlt.

## API-Vertrag und Grenzen
- NeoForge 1.21.1 `SimplePreparableReloadListener` bietet `prepare(ResourceManager, ProfilerFiller)` für die asynchrone Vorbereitungsphase sowie `apply(T, ResourceManager, ProfilerFiller)` zum Anwenden. Siehe https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/server/packs/resources/simplepreparablereloadlistener
- Der `ResourceManager` bestimmt die Vorrangregel von Resource-Packs bei gleichen Ressourcen-Pfaden. Unterschiedliche Pfade mit gleicher `plant.id` bleiben dagegen Konflikte.
- **Atomar** bezeichnet die Veröffentlichung der *Katalogreferenz*. Andere Programmstellen, die `plants()` und `mutations()` getrennt aufrufen, könnten zwischen diesen Aufrufen noch unterschiedliche Generationen abfragen; Mehr-Map-Operationen sollen deshalb `snapshot()` erfassen. Die Kreuzungslogik tut dies jetzt.
- Ein Fehler in einer einzelnen Definition deaktiviert nur diese Definition; bei einem schwerwiegenden Fehler außerhalb der abgefangenen Parsefälle kann der gesamte Reload fehlschlagen und der alte Snapshot bleibt unverändert. Das ist beabsichtigtes Fail-Closed-Verhalten.
- Die Gesamtlaufzeit und das Lesen großer Modpacks wurden nicht benchmarked. Die Datenparser werden in `apply` ausgeführt; `prepare` übernimmt den Hintergrund-I/O-Teil.
- Clienttexturen bleiben separate Ressourcenpaket-Assets. Der neue Server-Daten-Snapshot überträgt sie **nicht** an entfernte Clients.
- **Keine Kompilierung, keine Tests, kein Spielstart, keine CI-Ausführung** wurden durchgeführt; Signaturen und Ablauf sind daher noch nicht praktisch verifiziert.

## Nächste Schritte
Nach ausdrücklicher Testfreigabe: Java-21-Build, Reload mit gültigen/ungültigen und doppelten Definitionsdateien, gleichzeitige Kreuzungsversuche sowie Multiplayer-Save-/Reload-Check. Vor weiterer Feature-Vergrößerung sollten Datapack-Validierungsberichte und optional Crop-Definitions-Synchronisierung zum Client geplant werden.
