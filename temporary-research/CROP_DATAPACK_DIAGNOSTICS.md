# Crop Genetics – Datapack-Diagnosebericht
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Implementiert
Der gemeinsame `CropDataReloadListener` sammelt für jede gefundene Pflanzen- und Mutationsressource einen Status. Auch Dateien, deren JSON nicht lesbar ist, erscheinen im Bericht. Das gesamte Ergebnis wird über `AgriPlantsRegistry.replaceAll(plants, mutations, report)` **atomar mit dem aktiven Pflanzenkatalog** veröffentlicht. Ein Bericht kann über `CropGeneticsMod.plants().reloadReport()` bzw. `CropGeneticsMod.plants().snapshot().report()` von zukünftigem Server-UI/Command-Code abgerufen werden.

Neuer Datentyp: `data/CropReloadReport.java`.
Jeder Eintrag enthält `Kind` (`PLANT` / `MUTATION`), die exakte ResourceLocation, die vorhandene Plant-ID beziehungsweise Child-ID, den Status und eine lesbare Begründung.

### Statuswerte
| Status | Bedeutung |
|---|---|
| `LOADED` | Definition erfolgreich geladen und aktiviert |
| `DISABLED` | Die JSON-Datei enthält `enabled=false` |
| `MISSING_MOD` | Mindestens eine benötigte Mod ist nicht geladen |
| `INVALID` | Datei nicht lesbar, fehlerhaftes JSON, Parser-/Schemafehler oder ungültige Werte |
| `CONFLICT` | Mehrere unterschiedliche Ressourcen enthalten dieselbe `plant.id`; **alle** betroffenen Definitionen werden deaktiviert |
| `MISSING_SPECIES` | Mutation verweist auf Kind oder Elternteil, die im neuen gültigen Pflanzenkatalog fehlen |

## Server-Log
Nach jedem vollständigen Reload gibt der Listener eine Zusammenfassung aus, etwa:

```text
Crop datapack reload: 7 plants, 1 mutations, 9 resources ({INVALID=1, ...})
```

Alle von `LOADED` abweichenden Ressourcen erzeugen eine zusätzliche Warnung mit Status, Ressource und Begründung:

```text
Crop data PLANT example:cropgenetics/plants/broken.json [INVALID]: ...
Crop data MUTATION example:cropgenetics/mutations/example.json [MISSING_SPECIES]: Unknown or disabled species: ...
```

Die Zahlen hier sind **nur illustrative Meldungsbeispiele**, keine Ergebnisse einer ausgeführten Mod. Eine einzelne Plant-ID kann mehrere Report-Einträge besitzen, wenn mehrere Dateien kollidieren.

## Modpack-Workflow
1. Server-Log nach `Crop datapack reload:` durchsuchen.
2. Alle Meldungen `Crop data` mit Status ungleich `LOADED` prüfen.
3. Bei `CONFLICT` **alle** betroffenen Ressourcendateien bzw. widersprüchlichen `plant.id`-Werte betrachten.
4. Bei `MISSING_SPECIES` zunächst die betroffenen Pflanzen-Logs kontrollieren; z.B. kann ein `MISSING_MOD`-Eintrag eine Folgefehlermeldung auslösen.
5. Nach Anpassung der Datapacks die normalen Minecraft-Server-/reload-Prozeduren benutzen und das Log erneut überprüfen. Keine Testresultate behaupten, bevor der Lauf tatsächlich stattgefunden hat.

## Technische Entscheidungen
- Die Diagnosen sind eine unveränderliche Liste im selben `Snapshot` wie Pflanzen, Mutationen und umgekehrter Seed-Index. So kann eine UI/ein Befehl später den *gleichen* Datenstand auslesen.
- JSON-Einlesefehler werden bereits in `prepare` gesammelt; einzelne fehlerhafte Dateien stoppen den gesamten Reload nicht.
- Doppelte Plant-IDs führen dazu, dass **auch die zuerst eingelesene Datei** nachträglich als `CONFLICT` gilt. Stabil sortierte Dateischlüssel machen das reproduzierbar.
- Fehlende Mod-Abhängigkeiten führen zum Überspringen der jeweiligen Definition. `agricraft`-Self-Dependency bleibt speziell unterstützt.
- Nur Abweichungen von `LOADED` werden pro Datei ins Server-Log geschrieben. Dadurch bleibt das Log bei großen Datapacks übersichtlicher; alle geladenen Dateinamen bleiben über `reloadReport().entries()` zugänglich.
- Unterschiedliche Ressourcen mit **identischem Dateipfad** werden bereits vom `ResourceManager` anhand Ressourcenpaket-Priorität zusammengeführt; der Listener sieht nur die jeweils ausgewählte Variante und kann Überschreibungen identischer Pfade nicht als separate Conflict-Dateien melden.

## Offen
- **Kein Build, kein Test, kein Minecraft-Spielstart und keine CI-Ausführung**, wie ausdrücklich gewünscht. Der Code wurde committed, seine Minecraft-/NeoForge-API-Konformität muss vor Release überprüft werden.
- Noch keine grafische Diagnoseoberfläche oder Brigadier-Befehle. Der Bericht ist aktuell via Server-Log und Programmierschnittstelle zugänglich.
- Eine fehlerhafte externe `Predicate<String> loadedMod`-Implementierung oder unerwartete Fehler in der zentralen `replaceAll`-Routine könnten weiterhin den Reload scheitern lassen, statt nur eine Definition zu überspringen.
- Die Diagnose erfasst Datenstruktur- und Referenzvalidierung, prüft aber **nicht**, ob alle referenzierten Mod-Items, Tags, Clienttexturen und Modelle zur Laufzeit vorhanden sind.
