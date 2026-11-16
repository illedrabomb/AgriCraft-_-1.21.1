# NeoForge 1.21.1 – Statische API-/Persistenzprüfung, Runde 2

Datum: 2026-11-15  
Branch: `feature/neoforge-1211-foundation`

**Auf ausdrücklichen Nutzerwunsch keine Builds, Tests, CI-Ausführungen oder Spielstarts.**
Die folgenden Aussagen betreffen Quellcode-Änderungen und öffentlich dokumentierte API-Signaturen, **keine erfolgreiche Kompilierung**.

## Im Repository korrigiert

1. **Legacy-/Teil-NBT beim Gourd-Tracking:** `CropSticksBlockEntity.loadAdditional` normalisiert widersprüchliche `FruitReady`-/`FruitDirection`-/`FruitToken`-Einträge. Liegt nicht das komplette Besitzer-Tripel vor oder fehlt das Genom, wird keine aktive Frucht mehr behauptet. Altwelten bleiben auf ihre physisch vorhandenen Blöcke angewiesen; ohne UUID werden fremde Früchte nicht beansprucht.
2. **Keine erzwungenen Chunk-Loads bei Fruchtentfernung:** `GeneticGourdFruitBlock.onRemove` prüft vor dem Nachschlagen der Mutter-BlockEntity `ServerLevel.hasChunkAt(parentPos)`. Wenn der Eltern-Chunk nicht geladen ist, erhält der Elternteil kein unmittelbares Event, korrigiert die Beziehung aber beim nächsten RandomTick über `reconcilePhysicalFruit`.
3. **Begrenzte Genom-Pflanzen-ID im Netzwerk:** `GenomeCodecs` nutzt `writeUtf(id, 128)` und `readUtf(128)`. Der `Genome`-Konstruktor lehnt zugleich Pflanzen-IDs mit mehr als 128 Zeichen ab. Dies hält Persistenzdomäne und Netzwerklimit konsistent, statt große frei definierbare Strings zu versenden.
4. **Pflanzenflächen im Client:** `CropSticksRenderer` berechnet horizontale Normalen für die beiden Seiten einer vertikalen Pflanzenfläche aus deren Endpunkten. Zuvor wiesen sämtliche Quad-Normalen nach oben, unabhängig von ihrer tatsächlichen Orientierung. Die Methoden sind nur im Clientcode vorhanden.

## Geprüfte / nicht geänderte APIs

- `LootParams.Builder#getLevel()` und `getOptionalParameter(LootContextParam<T>)` existieren in 1.21.1: Der vorhandene `CropSticksBlock.getDrops`-Aufruf `builder.getLevel().getRandom()` ist deshalb **kein nachgewiesener API-Fehler**. Javadoc: https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/world/level/storage/loot/lootparams.builder
- `BlockEntityRenderer#render(BlockEntity,float,PoseStack,MultiBufferSource,int,int)` entspricht der Version 1.21.1: https://docs.neoforged.net/docs/1.21.1/blockentities/ber/
- `CompoundTag#putUUID/getUUID/hasUUID` existiert in Minecraft 1.21.1 (diese Signaturen sind **erst in späteren Minecraft-Versionen** weggefallen): https://mappings.dev/1.21.1/net/minecraft/nbt/CompoundTag.html
- `VertexConsumer#addVertex`/Fluent-Attribute ist ein 1.21-kompatibles Rendering-Muster: https://github.com/mezz/neoforged.github/blob/main/primers/1.21/index.md

## Noch nicht abschließend geklärt

- Die dynamischen Pflanzentexturen verwenden `RenderType.entityCutoutNoCull` mit einer direkten `textures/<path>.png`-Ressource, anstatt Minecraft-Blockatlas-Sprites zu verwenden. Das kann beim späteren Client-Test visuelle Unterschiede oder fehlende Texturen verursachen.
- Bei Vanilla-Melon-/Pumpkin-Loot delegiert der Owner-Fruchtblock die Drops an `vanilla.getDrops(builder)`; ob sämtliche Lootkontext-Parameter bei eigener BlockEntity/BlockState auch bei Silk Touch, Fortune, Explosionszerstörung und Mod-Lootmodifikatoren korrekt übertragen werden, ist im Spiel noch zu prüfen.
- Die Synchronisierung der `Genome`-BlockEntity bei Client-Reload, Join in bereits vorhandene Chunks und Datapack-Replacement ist noch nicht spielgetestet.
- Das `species`-/`facing`-BlockState-Schema kann in vorhandenen Welt-Saves Migration erfordern.
- Der externe Importer `tools/migrate_agriplants.py` und dessen Ausgabedaten wurden weiterhin nicht ausgeführt oder gegen eine große Auswahl echter Originaldefinitionen validiert.
- Eine persistente `FruitDirection` mit UUID vermeidet fremde/ausgetauschte Früchte; sie ist aber noch kein globaler Eigentümerindex. Bei entladenen Chunks kann die Benachrichtigung zeitversetzt sein.

## Empfehlung

Vor neuen Spezialpflanzen einen weiteren Quellcode-Audit auf Probleme in Datenreload/Mutation, klientseitigem Texturatlas und Custom-Fruit-Loot durchführen. Danach bei ausdrücklicher Freigabe einen vollständigen Build und Client-/Dedicated-Server-Test starten; bis dahin keine Release-Freigabe behaupten.
