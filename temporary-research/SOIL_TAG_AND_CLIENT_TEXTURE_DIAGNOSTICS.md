# Boden-Tags und Client-Texturen: Ressourcen-Diagnose

Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Nur Quellcodeänderungen; keine Builds, Tests, Spielstarts oder CI-Ausführungen.**

## Serverseitige Boden-Tags

- `PlantRegistryValidator.tagWarnings(PlantDefinition, ResourceManager)` prüft bei Pflanzen mit `cropgenetics_requirements.soil_tag` das aktuelle Datapack-Ressourcenangebot auf die erwartete Tag-Datei:
  `data/<namespace>/tags/block/<path>.json`.
- **Minecraft 1.21.1 verwendet `tags/block` im Singular**. Legacy-Pfade `tags/blocks` würden in 1.21.1 nicht mehr als Block-Tags verarbeitet.
- Eine nicht gefundene Datei wird als **nicht fatale `WARNING`** im `CropReloadReport` des selben Reload-Snapshots gespeichert. Die Pflanze bleibt aktiviert. Das ist absichtlich konservativ, denn die Datei-Präsenz ist nicht gleichbedeutend mit dem tatsächlich verfügbaren Taginhalt; NeoForge oder andere Datenmechanismen können ebenfalls Tags definieren.
- Der Operatorbefehl `/cropgenetics data errors` listet `WARNING` bereits mit auf, da er nur `LOADED` filtert. In `/cropgenetics data` wird die Warnungsanzahl zusammengefasst.
- Item-/Block-Registry-Prüfungen bleiben wie zuvor **harte `INVALID`-Fehler**. Die Boden-Tag-Warnung tritt erst nach einer erfolgreichen Definition auf.
- Ein leeres, fehlerhaftes oder zur Laufzeit nicht gebundenes Tag wird **nicht** von dieser Dateiprüfung erkannt. Daraus darf weder ein garantierter Bodenmatch noch ein zwingender Datenfehler gefolgert werden.

## Clientseitige Pflanzentexturen

- `PlantVisuals.apply` prüft jede über ihre JSON-Definition referenzierte Textur gegen die aktiven Client-Ressourcen:
  `assets/<namespace>/textures/<sprite_path>.png`.
- Mehrfach referenzierte PNGs werden pro Pflanzen-ID nur einmal protokolliert. Die Abwesenheit wird mit Plant-ID, Ursprungs-JSON und fehlenden PNG-Pfaden in das **Client-Log** geschrieben.
- Die öffentlich abrufbare, unveränderliche Map `PlantVisuals.INSTANCE.missingTextureFiles()` gibt für jede Pflanze die nicht als PNG auffindbaren ResourceLocations zurück.
- **Kein harter Ablehnungsgrund**: Manche Sprites entstehen durch Atlas-Quellen, die keine separat sichtbare PNG-Ressource liefern. Der Renderer darf außerdem seinen Missing-Sprite-Fallback verwenden. Die bestehende Client-Pflanzendefinition bleibt aktiv; Warnungen sind keine Beweise für sichtbare Renderfehler.
- Diese Daten liegen ausschließlich auf dem **Client**. Das Server-Datapack-Log und der Server-Adminbefehl können Ressourcendateien eines fremden Client-Resource-Packs nicht zuverlässig sehen und behaupten deshalb nicht, sie geprüft zu haben.
- Die neue Diagnose bestätigt weder Animationen noch Pixel-Layout/Transparenz oder Modellkompatibilität.

## Betroffene Dateien

- `data/PlantRegistryValidator.java`: Dateipräsenz-Warnung für Boden-Tags.
- `data/CropReloadReport.java`: zusätzlicher Status `WARNING`.
- `data/CropDataReloadListener.java`: tagbasierte Warnungen pro ansonsten gültiger Pflanze; weiterhin ein gemeinsamer atomarer Snapshot.
- `client/PlantVisuals.java`: Client-ResourceManager-PNG-Diagnose, Warnlogs und abrufbare Diagnostic-Map.

## Nächste Schritte

Bei späterer ausdrücklicher Testfreigabe: NeoForge-1.21.1-Kompilierung, Datapack-Test mit gültigem/fehlendem/leerem Boden-Tag, zwei Resource-Packs mit überlagernden Texturen, Client-Ressourcen-Neuladen, Multiplayer. Ohne Testfreigabe wäre eine vorsichtige statische Prüfung der Blockatlas-/ResourceManager-APIs und Datapack-Reload-Verträge sinnvoll.
