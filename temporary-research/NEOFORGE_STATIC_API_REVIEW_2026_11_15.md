# Statische API- und Speicherprüfung – NeoForge 1.21.1
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Arbeitsmodus:** Quellcode- und Dokumentationsprüfung. **Kein Build, keine Tests, kein Ingame-Start**, wie vom Nutzer gewünscht.

## In diesem Review korrigiert
1. **Geschützte Block-Loot-Methode auf fremdem Block**. In `GeneticGourdFruitBlock.getDrops` wurde früher `vanilla.getBlock().getDrops(vanilla, builder)` verwendet. Der `BlockBehaviour#getDrops(BlockState, LootParams.Builder)`-Aufruf ist `protected`, daher ist dieser Ausdruck für eine andere Block-Instanz in einem fremden Package kein zulässiger Java-Zugriff. Die Änderung verwendet `vanilla.getDrops(builder)`; `BlockBehaviour.BlockStateBase#getDrops(LootParams.Builder)` ist laut 1.21.1-JavaDocs öffentlich. Die bestehende Loot-Builder-Instanz liefert weiter Tool-/Kontextparameter. Ob die Vanilla-Block-Loot-Table unter dem Custom-Blockstate-Kontext in allen Fällen genauso reagiert (Silk Touch/Fortune/Loot-Modifikatoren), bleibt **zu überprüfen**.
2. **Chunk-Grenzen bei Frucht-Tracking**. `CropSticksBlockEntity.reconcilePhysicalFruit` prüft den geladenen Nachbar-Chunk mit `ServerLevel.hasChunkAt`, bevor es eine fehlende Frucht als Ernte interpretiert. Ein aktuell nicht geladener Nachbar-Chunk darf den Stiel nicht zurücksetzen.
3. **Chunk-Grenzen bei Fruchtbildung und Zuordnung**. `GourdFruits.matches` und `GourdFruits.tryPlace` greifen nicht auf das Nachbarfeld zu, wenn dessen Chunk nicht geladen ist. So entstehen keine Fruchtblöcke in versehentlich neu geladenen oder noch nicht verfügbaren Chunks.

## Verifizierte 1.21.1-API-Kontrakte (nur Dokumentation)
- `BlockBehaviour#getDrops(BlockState, LootParams.Builder)` ist `protected`: https://lexxie.dev/neoforge/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.html
- `BlockBehaviour.BlockStateBase#getDrops(LootParams.Builder)` ist `public`: https://lexxie.dev/neoforge/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.BlockStateBase.html
- `Block#onRemove(..., movedByPiston)` wird in der 1.21.1-Dokumentation mit `Level` und `BlockState` übergeben: https://docs.neoforged.net/docs/1.21.1/
- Der Server muss den Client nach BE-NBT-Änderungen über `Level#sendBlockUpdated` informieren: https://docs.neoforged.net/docs/1.21.1/blockentities/

## Weiter offene Risiken
- **Wahrscheinlich breiteres API-Risiko:** `CropSticksBlock.getDrops` nutzt `builder.getLevel().getRandom()`; die genaue 1.21.1-Builder-API ist vor dem nächsten Release nachzuprüfen. Gleiches gilt für weitere ursprünglich ungeprüfte Methoden.
- Die direkte Delegation des Vanilla-Loots über `vanilla.getDrops(builder)` nutzt weiterhin den originalen Custom-Fruit-Loot-Builder; Modifikatoren und eventuelle `minecraft:block_state`-Prädikate könnten anders ausfallen als beim Abbau eines echten Vanilla-Fruchtblocks.
- Für die Client-Pflanzentexturen wird derzeit ein eigener `RenderType.entityCutoutNoCull(textures/...png)` verwendet. Vanilla-Blocktexturen werden in der Minecraft-Texturatlas-Ressource geladen; nicht jede Resource-ID muss als standalone PNG-RenderType die gleichen Ergebnisse liefern.
- Die `CropSticksBlockEntity.syncState`-Änderungen müssen als BlockState- und BE-Update zusammen ankommen; sichtbare Stielorientierung und Fruchtzuordnung sind noch nicht Ingame-geprüft.
- Die Prüfung `hasChunkAt` schützt gegen nicht verfügbare Chunk-Daten. Ob sie alle Übergänge bei entladenen Chunk-Tickets abdeckt, ist vor Release zu verifizieren.
- Eigene Fruit-Blöcke mit `BaseEntityBlock` behalten nicht automatisch jeden Vanilla-Block-Spezialfall (Piston, Silk Touch, Fortune, explosionsspezifische Drops). Hierfür ist eine spätere Ingame-Prüfung erforderlich.
- Noch keine automatisierte Konfliktauflösung beim Laden alter Welten; `FruitToken` in alten Saves fehlt. Die nächste RandomTick-Reconciliation resettiert den Stiel, lässt den alten Vanilla-Fruchtblock jedoch bewusst bestehen.

## Empfehlung für den nächsten Arbeitsschritt
1. Weitere **rein statische Java-API-Prüfung** im ganzen `neoforge-port` vornehmen (besonders LootParams.Builder, Client-Render-/Resource-Reload, NBT und NeoForge-Registrierung).
2. Serverseitiges und Client-Ressourcenformat vor einem großen AgriPlants-Import verfestigen.
3. Sobald Nutzer Build und Tests wieder erlaubt: Java 21/NeoForge Compile, Beuteprüfung mit Silk Touch/Fortune, Weltpersistenz, Chunkgrenzen, 2 benachbarte Pflanzen und Dedicated Server testen.
