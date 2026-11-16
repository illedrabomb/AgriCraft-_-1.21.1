# Eindeutige Zuordnung von Kürbis-/Melonenfrüchten zu ihrer Ranke
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`
**Keine Builds, Tests oder GitHub-Actions ausgeführt**, entsprechend der Vorgabe.

## Was neu im NeoForge-Port liegt
- `content/GeneticGourdFruitBlock.java`: eigener physischer Fruit-Block als `BaseEntityBlock` für Kürbis/Melone. Das Blockmodell nutzt die entsprechenden Vanilla-Minecraft-Blockmodelle. `getDrops` reicht die Ernte an die Vanilla-Melon-/Pumpkin-Lootlogik weiter; keine zusätzlichen Erntedrops über die Mutterpflanze. `onRemove` informiert die zugehörige Ranke **sofort beim Entfernen oder Ersetzen durch einen anderen Block**.
- `content/GeneticGourdFruitBlockEntity.java`: persistente Elternposition `ParentX/Y/Z` und individuelles `FruitToken` (UUID). Die Methode `ownedBy(parent,token)` ist erforderlich für jede Zugehörigkeitsprüfung.
- `content/CropBlocks.java`: zwei Blockregistrierungen `genetic_melon_fruit`, `genetic_pumpkin_fruit` sowie der gemeinsame `gourd_fruit`-BlockEntityType.
- `content/GourdFruits.java`: setzt nur unsere eigenen Fruit-Blöcke, erstellt pro versuchtem Fruchtansatz ein UUID-Token, bindet den neuen Block an Parent-Pos/Token und übergibt dieselben Daten an die Ranke. Wenn das Setzen oder Zuordnen scheitert, wird der neue Block ohne Drops verworfen. Weiterhin nur vier horizontale Felder und geeigneter Standardboden.
- `content/CropSticksBlockEntity.java`: speichert neben `FruitReady` und `FruitDirection` nun auch `FruitToken`. `reconcilePhysicalFruit` erkennt einen Block nur noch als **eigen**, wenn Blocktyp, Richtung, Elternposition und UUID stimmen. `onOwnedFruitRemoved(server, pos, token)` setzt beim Entfernen der richtigen Frucht unmittelbar `FruitReady` zurück, löscht Richtung/Token, setzt die Wachstumsstufe auf `harvestStage` zurück und aktualisiert den sichtbaren BlockState. Unbeteiligte Früchte (auch identisch aussehende) lösen keinen Reset aus.
- `content/CropSticksBlock.java`: zusätzliche `FACING`-BlockState-Eigenschaft, wird bei Fruchtbindung auf die Fruchtrichtung gesetzt, ohne Frucht nach Norden zurückgesetzt.
- `content/CropAppearance.java`: MELON/PUMPKIN eigene Visual-Kategorien.
- `assets/cropgenetics/blockstates/crop_sticks.json`: Bei `fruit=true` kombiniert der Multipart-Blockstate die normalen Crop-Sticks mit den Vanilla-`attached_melon_stem`-/`attached_pumpkin_stem`-Modellen; Rotation je nach Richtung.
- `assets/cropgenetics/blockstates/genetic_melon_fruit.json` und `genetic_pumpkin_fruit.json`: echte Früchte verwenden Vanilla-Blockmodelle.
- `client/CropSticksRenderer.java`: Der reguläre BER zeichnet **keinen weiteren Ranken-Overlay**, wenn der Blockstate schon das ausgerichtete Vanilla-Stielmodell rendert.

## Serverablauf
1. Die schon vorhandenen AgriPlants-Definitionen für Melone/Kürbis wachsen wie bisher.
2. Bei ausreichend gereifter Ranke würfelt das System eine Gain-abhängige Fruchtchance.
3. Ein leerer Platz mit passendem Boden wird ausgewählt, dort wird ein `GeneticGourdFruitBlock` gesetzt; dessen BlockEntity bekommt die Mutterposition und ein neues UUID-Token.
4. Die Mutter-BlockEntity akzeptiert diese Bindung nur dann, wenn derselbe Block unter genau derselben Elternposition und Token auffindbar ist; andernfalls Rollback ohne Drops.
5. Wird der eigene Fruchtblock abgebaut oder ersetzt, ruft der Fruit-Block `onOwnedFruitRemoved` bei der richtigen Ranke auf und setzt ihre Nachwuchsphase umgehend zurück. Der echte Fruchtblock wird genau einmal über sein Loot-Verhalten geerntet, nicht zusätzlich durch `CropHarvest`.
6. Ein späterer Random-Tick dient weiterhin als Rückfallsicherung für Fälle, in denen die Ranke nicht geladen war oder die Referenz beschädigt ist.

## Grenzen und technische Risiken
- **Noch nicht kompiliert oder im Spiel getestet.** NeoForge-1.21.1-Signaturen für Block-/BlockEntity-Events, Model-Varianten und Loot-Delegierung sind deshalb nicht praktisch bestätigt.
- Die Fruit-BlockEntity besitzt nur eine Elternposition und UUID, keine Versionsprüfung und keine Dimensionskennung. Da die Referenz innerhalb derselben ServerLevel-Instanz entsteht, ist dimensionsübergreifendes Tracking derzeit nicht vorgesehen.
- Beim **Austausch eines Fruchtblocks durch denselben Blocktyp** ohne vollständigen Block-/BE-Austausch gibt es eventuell keinen `onRemove`-Event. Ein späterer Random-Tick prüft die Eigentümerschaft anhand der UUID.
- Bei alten Welten, in denen noch Vanilla-Melon-/Pumpkin-Fruchtblöcke neben Crop Sticks stehen, fehlt der neue Owner-Token. Solche früheren Früchte werden **nicht automatisch gelöscht** (Schutz fremder/playergesetzter Blöcke). Die Mutter vergleicht diese nicht mehr als ihre neue Frucht und korrigiert ihren alten FruitReady-Status beim nächsten Random-Tick.
- Bei Kolbenbewegung, Chunk-Grenzen und gleichzeitiger Weltbearbeitung kann die unmittelbare Parent-Benachrichtigung ausfallen; das nächste Random-Tick-Reconcile ist der Fallback.
- Loot benutzt die Vanilla-Fruchtloot-Tabelle indirekt; die tatsächliche Weitergabe des Loot-Kontexts (insbesondere Silk Touch) muss später geprüft werden. Beim Pflanzen von normalem Saatgut können andere Mods die Right-Click-Interaktion beeinflussen.
- Gourd-Gain regelt Fruchtbildung, nicht zusätzliche physische Fruchtmenge. Normale Crop-Sticks-Drops liefern bei Gourd keine zweite Frucht.
- Nur Vanilla-Melone und -Kürbis in der Gourd-Methode. Andere Mod-Früchte brauchen eigene Blockregistrierungen/Adapter.
- Keine automatischen Farm-/MineColonies-Aktionen oder Angabe des Besitzers im Tooltip.

## Nächstes
Wichtiger als weitere ungetestete Spezialfeatures ist eine **statische API-/Quellcodeprüfung** auf veraltete NeoForge-Mappings und Loot-Kontexte; diese kann auf Nutzerwunsch weiterhin ohne Build/Test erfolgen. Wenn Tests freigegeben werden: Java 21-Compile, Testwelt mit zwei benachbarten Ranken, Kolben-/Ernte-/Chunkgrenzen sowie Dedicated Server.
