# Datengetriebener Pflanzenrenderer – NeoForge 1.21.1
Stand: 2026-11-15 | Entwicklungsbranch: `feature/neoforge-1211-foundation`

## Implementierter Quellcode (kein Build oder Ingame-Test auf Nutzerwunsch)
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/client/CropSticksRenderer.java`: clientseitiger `BlockEntityRenderer<CropSticksBlockEntity>`, der das Pflanzen-Genom aus der synchronisierten BlockEntity und die sichtbare Wachstumsstufe aus dem BlockState liest. Er zeichnet eine beidseitig sichtbare gekreuzte Pflanzenfläche über dem weiterhin statischen Holzmodell der Crop Sticks.
- `.../client/PlantVisuals.java`: Client-Resource-Pack-Reload-Listener (`SimpleJsonResourceReloadListener`), der eine Map `plant ID -> Texturliste` lädt. Reload ersetzt die Map vollständig; bei ungültigen Definitionen wird geloggt und die einzelne Ressource übersprungen. Fehlen Texturen, wird neutrales Eichenlaub benutzt.
- `.../client/CropGeneticsClient.java`: ausschließlich für `Dist.CLIENT` registrierte Mod-Bus-Events `EntityRenderersEvent.RegisterRenderers` und `RegisterClientReloadListenersEvent`. Keine Clientklasse im gemeinsamen `@Mod`-Entry-Point.
- `assets/cropgenetics/cropgenetics/plant_visuals/{wheat,carrot,potato}.json`: je 8 Stufen mit nativen Minecraft-Textur-IDs.
- `assets/cropgenetics/blockstates/crop_sticks.json`: jetzt nur noch Multipart-Grundmodelle für einfache/doppelte Holzstäbe. Das vorherige statische Pflanzen-Overlay wurde entfernt, um Doppelrendering zu vermeiden.

## Neues Client-Ressourcenformat
Ort: `assets/<resource_namespace>/cropgenetics/plant_visuals/<dateiname>.json` im Mod-JAR oder einem aktiven Ressourcenpaket.

Beispiel:
```json
{
  "plant": "minecraft:wheat_plant",
  "textures": [
    "minecraft:block/wheat_stage0",
    "minecraft:block/wheat_stage1",
    "minecraft:block/wheat_stage2",
    "minecraft:block/wheat_stage3",
    "minecraft:block/wheat_stage4",
    "minecraft:block/wheat_stage5",
    "minecraft:block/wheat_stage6",
    "minecraft:block/wheat_stage7"
  ]
}
```

Die Einträge referenzieren Texturressourcen ohne `textures/` und `.png`. Texturen können aus anderen Mods kommen, wenn deren **Client-Ressourcen** vorhanden sind. Für 4 Stufen kann die Liste 4 Einträge haben; sie werden auf 8 sichtbare Stufen skaliert. Ein anderer Pflanzenmod könnte etwa `"plant": "examplemod:pepper_plant"` und Texturen `"examplemod:block/pepper_stage0"` usw. definieren.

## Übernahme vorhandener AgriPlants-Texturabschnitte
`PlantVisuals` akzeptiert alternativ einen **als Client-Ressource gespiegelten** Original-Datensatz, soweit er `id`, `texture.render_type = "hash"` und `texture.plant_textures` (genau eine Textur pro Stufe) enthält. Das erlaubt erste Migration ohne händische Extraktion der Texturliste. Die Berechnung bleibt geometrisch eine vereinfachte Kreuzfläche, **keine exakte Umsetzung aller alten Render-Modi**.

Wichtig: Das alte AgriPlants-Submodule liegt unter `data` und anderen Verzeichnissen; **ein serverseitiger Datapack-Reload überträgt diese Texturen nicht automatisch an den Client**. Damit Modpacks eigene Pflanzen sehen, müssen zu den serverseitigen Pflanzen-JSONs auch die passenden Texturdefinitionsdateien und Texturassets als Mod-Ressourcen oder Client-Ressourcenpaket bereitgestellt werden. Für vollständige Datapack->Client-Synchronisierung ist später ein Netzwerkprotokoll nötig. Die `PlantVisuals`-Definition ist absichtlich clientseitig.

## Noch offen
- **Build, Render-Test, Client-/Server-Start nicht durchgeführt**. Die 1.21.1-APIs und direkte `RenderType.entityCutoutNoCull`-Texturbindung sind anhand der Dokumentation entworfen, aber nicht durch Ausführung verifiziert.
- Die Original-JSONs können spezielle Formen, mehrere übereinander liegende Texturen, Alphacutout-/Hash-Varianten, schwebende Teile, Biomeinfärbung und Modellreferenzen enthalten. Diese werden nicht vollständig unterstützt. Für Multi-Layer-Texturen wird beim Laden ausdrücklich eine Fehlermeldung ausgelöst.
- Resource-Packs auf entfernten Clients müssen die Texturen/Definitionen tatsächlich enthalten.
- Das frühere vierwertige `CropAppearance`-Enum bleibt vorerst im Blockstate zur Savegame-Kompatibilität, der Renderer nutzt es **nicht** als Quelle der Artenidentität.
- Die exakte Wachstumsstufe in der BlockEntity bleibt bestehen; die neuen Texturen verwenden die 0..7-BlockState-Stufe.
- Die neutrale Ersatztextur ist noch schlichtes Eichenlaub; individuelle Geometrie, Höhen, Cross-Plants mit mehreren Teilen und Vanilla-Atlasoptimierung folgen.
- Alter Code und bestehende Tests wurden nicht entfernt. GitHub CI bleibt manuell, Nutzer wollte ohne Tests weitermachen.

## Nächste priorisierte Schritte
1. Sichere automatische Konvertierung/massenhafter Import aus AgriPlants-Unterordnern (validieren, Log, Skip bei veralteten Ressourcennamen), inklusive Erzeugung der Client-Texturlisten.
2. Erweiterte Renderformen aus AgriPlants und Pflanzen mit mehreren Stufen-/Texturlayern, solange keine Mod-API-Erweiterungen nötig sind.
3. Synchronisation der definitionsgebundenen Pflanzendaten auf Client in Multiplayer für mehr als reine Optik.
4. Vor Release Build und Spielstart validieren, sobald Tests wieder freigegeben werden.

## Referenzen
NeoForge 1.21.1 Event- und Render-API: https://docs.neoforged.net/docs/1.21.1/blockentities/ber/ und https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/client/event/RegisterClientReloadListenersEvent.html.
