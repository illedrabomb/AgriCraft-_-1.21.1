# Statische Prüfung: Blockatlas und Gourd-Loot – NeoForge 1.21.1
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Keine Builds, keine Tests, kein Spielstart, kein CI-Workflow ausgeführt.** Alle Änderungen wurden ausschließlich anhand des Quellcodes und öffentlicher 1.21.1-API-Beschreibungen vorgenommen.

## Implementierte Korrekturen

### Client-Renderer
- `client/CropSticksRenderer.java` verwendet jetzt `Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(source)` zum Auflösen der Pflanzentexturen als `TextureAtlasSprite`.
- Die Vertex-Ausgabe benutzt `Sheets.cutoutBlockSheet()` statt einem separaten `RenderType.entityCutoutNoCull(textures/...png)`; UV-Werte werden aus `sprite.getU0()/getU1()/getV0()/getV1()` in echte Atlas-Koordinaten umgerechnet.
- Vertikale Pflanzenflächen behalten die zuvor korrigierten Normalen und werden doppelseitig als zwei Quads geschrieben.
- `assets/minecraft/atlases/blocks.json` ergänzt eine `directory`-Atlasquelle für `textures/plant/**.png` mit Sprite-Präfix `plant/`. Hierdurch lassen sich die zusätzlich für AgriPlants verwendeten `<namespace>:plant/...`-Texturen in den Standardblockatlas aufnehmen, wenn die tatsächlichen Dateien in Mod-/Resource-Pack-Assets liegen. Texturen unter `textures/block/` werden bereits über die Minecraft-Standardquelle gehandhabt.
- Resource-Pack-Atlasdefinitionen werden in Minecraft versionsabhängig als Quellen zusammengeführt; das Hinzufügen einer neuen Quelle ersetzt nicht absichtlich die Vanilla-Blocktexturen.
- Die `PlantVisuals`-JSON-Definitionen und das Reload-Event bleiben unverändert. Für fremde Ressourcen gilt weiterhin: Datei, erlaubter Sprite-Pfad und Client-Ressourcen müssen vorhanden sein.

**Wichtig:** Nicht alle ursprünglichen AgriPlants-Texturen werden mit dem NeoForge-Port geliefert. Die Atlas-Quelle ersetzt keine Texturdateien. Ohne Texturressource erhält ein Modell ggf. den fehlenden Sprite.

### Kürbis-/Melonen-Loot
- `GeneticGourdFruitBlock.getDrops` delegiert weiter an `Blocks.MELON` oder `Blocks.PUMPKIN`, nun aber mit explizit korrigiertem `LootContextParams.BLOCK_STATE` auf den Vanilla-Frucht-Blockzustand.
- `LootParams.Builder#withParameter` aktualisiert nur diesen Parameter; die anderen Kontextwerte (Tool, Ursprung, Spieler/Entität, Glück) stammen aus der ursprünglichen Block-Loot-Auswertung. Das verhindert mindestens den offensichtlichen Fehler, eine Vanilla-Loot-Tabelle mit `cropgenetics:genetic_..._fruit` als `BLOCK_STATE` auszuwerten.
- Die echte Frucht-BlockEntity behält die Zuordnung `ParentPos + FruitToken`; beim Abbauen gibt die Ranke keinen zusätzlichen Fruchtblock aus.
- **Restunsicherheit:** Mod-Loot-Modifikatoren können sich auch auf den tatsächlichen Custom-Blocknamen/BlockEntity beziehen; eine vollständige Vanilla-Gleichwertigkeit wird hier nicht behauptet.

## Dokumentationsgrundlagen
- NeoForge 1.21.1 `TextureAtlasSprite`, öffentliche UV-Methoden: https://lexxie.dev/neoforge/1.21.1/net/minecraft/client/renderer/texture/TextureAtlasSprite.html
- NeoForge 1.21.1 `Minecraft#getTextureAtlas(ResourceLocation)`: https://lexxie.dev/neoforge/1.21.1/net/minecraft/client/Minecraft.html
- NeoForge 1.21.1 `Material` und Atlas-Sprite: https://lexxie.dev/neoforge/1.21.1/net/minecraft/client/resources/model/Material.html
- Minecraft Atlas-Quellendateien und Merge-Verhalten: https://www.minecraft.net/en-us/article/minecraft-snapshot-22w46a
- `LootParams.Builder#withParameter`: https://mcstreetguy.github.io/ForgeJavaDocs/1.21-latest/net/minecraft/world/level/storage/loot/LootParams.Builder.html

## Noch offen
1. Die RenderType-/Buffer-Kompatibilität von `Sheets.cutoutBlockSheet()` in einem `BlockEntityRenderer` muss im Client praktisch geprüft werden. API-Signaturen reichen nicht als Render-Beweis.
2. Ressourcenneuladen mit mehreren Mods, fehlenden Sprites, Animations-Metadaten und Resource-Pack-Priorität muss später geprüft werden.
3. Loot-Drops unter Fortune, Silk Touch, Explosionen und NeoForge globalen Loot-Modifikatoren sind noch nicht im Spiel bestätigt.
4. `onRemove`, Chunk-Unload und Weltpersistenz beim Fruchtblock bleiben integrationskritisch.
5. Der NeoForge-Port ist ausdrücklich **nicht build- oder release-verifiziert**; bevor weitere große Feature-Schichten hinzukommen, sollte eine spätere Testfreigabe die verbleibenden API- und Laufzeitrisiken abdecken.

## Folgearbeit
Statisches Durchsehen der Datapack-Reload-Kette, Mutation-Validatoren und der BlockEntity-Sync-Verträge. Erst nach Nutzerfreigabe Kompilierung und Ingame-Tests.
