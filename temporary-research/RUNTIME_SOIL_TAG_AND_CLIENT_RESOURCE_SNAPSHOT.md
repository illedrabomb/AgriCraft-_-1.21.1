# Laufzeit-Boden-Tags und konsistente Client-Ressourcen
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Nur GitHub-Quellcodeänderungen; keine Builds, Tests, CI-Ausführungen oder Spielstarts.**

## 1. Tatsächlich gebundene Boden-Tags

Neu im `CropDataCommand`:

- `/cropgenetics data tags`
- `/cropgenetics data tags 2` (weitere Seiten, 8 Zeilen pro Seite)

Nur für Quellen mit `hasPermission(2)` (Serveroperatoren/Konsole). Die Abfrage ist **rein lesend**, startet keinen Datapack-Reload und verändert kein Pflanzenwachstum.

Für jede aktuell aktive Pflanze mit `requirements.soilTag()` wird der Tag als `TagKey<Block>` unter `Registries.BLOCK` aus der realen `BuiltInRegistries.BLOCK`-Tag-Bindung abgefragt. Im Befehl erscheint:

- `#namespace:tag (N registered blocks)`, wenn der Tag aktuell gebunden ist und N Mitglieder besitzt.
- `EMPTY #namespace:tag`, wenn ein gebundener Tag keine Mitglieder hat.
- `UNBOUND #namespace:tag`, wenn keine Bindung besteht.

Die Liste wird nach Pflanzen-ID sortiert und paginiert. Dabei wird ein einzelner `AgriPlantsRegistry.Snapshot` erfasst, damit die geprüften Pflanzen dem gleichen Datensatz angehören.

### Unterschied zur bereits vorhandenen Dateiwarnung

Der Server-Reload-Report gibt weiterhin höchstens eine **advisory**-Warnung aus, wenn die Datei `data/<namespace>/tags/block/<path>.json` nicht in der aktuellen ResourceManager-Auswahl auftaucht. **Das sagt nicht aus, dass der Tag zur Laufzeit ungebunden ist:** Tags können über andere Datenquellen, Mod-Generierung oder abweichende Ressourcenauflösungen bereitgestellt werden.

Der neue Adminbefehl fragt dagegen das tatsächlich gebundene Registry-Tag im Serverzustand nach Abschluss des normalen Reloads ab. Die Abfrage ist absichtlich **nicht** Teil der vorbereitenden Datapack-Validierung; vor Abschluss des vollständigen Server-Reloads könnten Tags noch nicht final gebunden sein.

Eine leere/fehlende Boden-Tag-Bindung deaktiviert die Pflanze derzeit nicht automatisch. `CropConditions.matchesSoil` verwendet weiterhin `BlockState.is(TagKey)`, sodass ungebundene Tags natürlicherweise keine Bodenblöcke matchen.

## 2. Client-Ressourcen als konsistenter Snapshot

`PlantVisuals` publizierte zuvor `visuals` und `missingTextureFiles` über zwei voneinander unabhängige `volatile`-Felder. Während eines Client-Resource-Reloads konnten andere Clientpfade kurzzeitig unterschiedliche Generationen sehen.

Neu:

- Unveränderliches `PlantVisuals.Snapshot(visuals, missingTextureFiles)`.
- **Eine** volatile Referenz `snapshot`; sie wird erst nach Abschluss der Ressourcenprüfung ausgetauscht.
- `visual(plantId)` liest nur diesen Snapshot, und `missingTextureFiles()` nur dessen Diagnostic-Map.
- Vorhandene Logs zu fehlenden `textures/<sprite_path>.png`-Ressourcen bleiben erhalten. Es handelt sich weiterhin ausschließlich um **Warnungen**, nicht um Beweise für fehlende Atlas-Sprites.
- Server-Datapacks können damit keine Client-Packs prüfen, die ein Spieler lokal aktiviert hat; die Clientdiagnose ist bewusst separat.

## API-Basis
- Minecraft/NeoForge 1.21.1: `BuiltInRegistries.BLOCK.getTag(TagKey<Block>)` liefert eine optionale HolderSet-Bindung für aktuelle Block-Tags; die `size()` gibt deren Mitgliederzahl an.
- Die Tags der Version 1.21.1 werden in `data/<namespace>/tags/block/` statt `tags/blocks/` abgelegt.
- Das Client-Reload verwendet weiterhin `SimpleJsonResourceReloadListener.apply(...)`; die Veröffentlichung des Snapshots erfolgt erst nach Durchlauf des `ResourceManager`.

## Grenzen
- **Nicht kompiliert oder ingame geprüft**. Die genauen Signaturen und das Verhalten auf dem verwendeten NeoForge-Build müssen später durch die ausdrücklich freigegebene Kompilierung und einen Server-Test bestätigt werden.
- `getTag` bezieht sich auf die Registrierungsbindung des laufenden Servers. Diese Abfrage ist keine Traversierung eines beliebigen, noch nicht angewendeten Datapacks.
- In frühen Server-Startphasen steht möglicherweise noch kein Reload-Bericht bereit; der Befehl informiert in diesem Fall entsprechend.
- Das Ermitteln tatsächlich gestitchter Blockatlas-Sprites sowie das Prüfen animierter Texturen ist nicht durch PNG-Dateiexistenz alleine abgedeckt.

## Nächster Schritt
Statisches Audit für die Transaktionsreihenfolge bei Datapack-Duplikaten und Load-Fehlern; später (mit Genehmigung) Build, Client-/Dedicated-Server-Test und Test eines Boden-Tags, das nur durch Datenquellen statt einer einzelnen JSON-Datei bereitgestellt wird.
