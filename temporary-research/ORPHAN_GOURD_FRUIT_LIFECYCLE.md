# Verwaiste physische Melonen- und Kürbisfrüchte

Stand: 2026-11-16 | Branch: `feature/neoforge-1211-foundation`

## Problem

Ein genetischer Melonen- oder Kürbis-Fruchtblock ist ein echter Weltblock mit einer eigenen BlockEntity. Die Frucht speichert `ParentX/Y/Z` und `FruitToken`; der Stamm speichert seine `fruitDirection` und denselben Token.

Wird der Stängel zerstört, kann seine Frucht noch in einem anderen (ggf. ungeladenen) Chunk stehen. Bisher blieb die gespeicherte Eigentumsreferenz der Frucht dauerhaft bestehen, auch wenn der Stängel weg war oder inzwischen eine andere Pflanze an seiner Stelle stand. Der Fruchtblock selbst soll dabei nicht plötzlich verschwinden: Er wurde bereits physisch erzeugt und hat eigene Vanilla-Loot-Regeln.

## Implementierte Lebenszyklus-Regeln

1. **Stängel wird zerstört, Frucht-Chunk geladen:** `CropSticksBlock.onRemove` liest die vorhandene Stamm-BlockEntity vor dem Entfernen und überprüft den exakten Frucht-Ort und Eigentums-Token. Nur wenn die Frucht im bereits geladenen Nachbarchunk existiert und die Werte passen, ruft der Stängel `detachIfOwnedBy` auf.
2. **Stängel wird zerstört, Frucht-Chunk nicht geladen:** Der Stängel lädt keinen Chunk nach. Der Fruchtblock behält die Referenz vorläufig; bei später geladenem Frucht-Chunk wird sie per periodischem `BlockEntityTicker` überprüft.
3. **Frucht-Chunk geladen, Stängel-Chunk nicht geladen:** Keine Änderung. `server.hasChunkAt(parentPos)` schützt vor einem falsch erkannten verwaisten Zustand.
4. **Beide Chunks geladen, Stängel vorhanden und exakt passend:** Die Referenz bleibt erhalten. Validiert werden Stamm-Position, Richtung, Art des Melonen-/Kürbisblocks, `fruitReady` und gemeinsamer Eigentums-Token; Datapack-Wechsel `GOURD` -> `STANDARD` ändern diese Eigentumsprüfung nicht.
5. **Beide Chunks geladen, Stängel entfernt/ausgetauscht/inkonsistent:** Die Frucht verliert nur ihre **alte Eigentumsreferenz**. Die Frucht bleibt als echter, normal aberntbarer Weltblock stehen. Es werden weder Blöcke ersetzt noch Gegenstände gedroppt.
6. **Stammblock vorhanden, aber BlockEntity während eines Chunk-Ladevorgangs noch nicht zugänglich:** Die Prüfung wird vertagt, nicht vorschnell entkoppelt.

## Technische Details

- `GeneticGourdFruitBlockEntity.detachIfOwnedBy` löst nur ein exakt passendes Eltern-Token.
- `GeneticGourdFruitBlockEntity.detachIfOrphaned` prüft nur serverseitig und ohne Chunk-Force-Load.
- `detachOwner` setzt `parentPos` und `fruitToken` auf `null`, markiert die Frucht-BlockEntity mit `setChanged()` zum Speichern und sendet das bestehende BlockEntity-Update. Keine neuen Netzwerkpakete.
- `GeneticGourdFruitBlock.getTicker` prüft gestaffelt alle 100 Ticks, auch bei `randomTickSpeed=0`. Eine bereits entkoppelte Frucht verursacht dann keine Eltern-Blockzugriffe mehr.
- `GeneticGourdFruitBlock.getDrops` delegiert weiterhin an den Vanilla-Melonen-/Kürbis-Loot; der neue Lifecycle erzeugt keine weiteren Drops.
- `CropSticksBlock.onRemove` greift nicht bei normalen BlockState-Änderungen derselben Crop-Sticks-Blockart, z. B. Wachstum, Fruchtreife oder Client-Synchronisierung.

## Bewusst gewählte Grenze

Eine verwaiste Frucht **verschwindet nicht automatisch**. Damit gehen Spielern bereits erzeugte Früchte nicht durch das Entladen von Chunks, das Zerstören des Stängels oder Reloads verloren. Automatisches Despawnen würde eine separate Produktentscheidung und Loot-Strategie benötigen.

Wenn der alte Stamm nach einem Reload noch ein `fruitReady`-Flag trägt, aber nicht mehr Eigentümer einer Frucht ist, übernimmt wie bisher die regelmäßige Stamm-Reconciliation die Korrektur; auch hier werden keine neuen Drops erzeugt.

## Nicht verifiziert

Keine Builds, Tests, CI oder Minecraft-Starts ausgeführt. Vor Freigabe insbesondere folgende Szenarien im Spiel prüfen: Stängelabbau bei geladenem Nachbarchunk; Abbau mit späterem Frucht-Chunk-Load; korrektes Überleben der Frucht beim wiederholten Chunkwechsel; Korruption bzw. Austausch des Stängels; physische Fruchtdrops mit und ohne passenden Stamm; `/reload` zwischen `GOURD` und `STANDARD`.
