# Fruchterzeugung, Explosionen, Kolben und Blockersatz

Stand: 2026-11-16 | Branch: `feature/neoforge-1211-foundation`

## Ausgangslage

Die genetische Melonen-/Kürbisfrucht ist ein eigenständiger Weltblock mit eigener BlockEntity und Eigentums-UUID. Der Stängel speichert dieselbe UUID und die Richtung. Wird einer der beiden Blöcke ersetzt, zerstört oder durch eine Explosion entfernt, müssen diese Beziehungen ohne zusätzliches Loot oder unberechtigte Eingriffe in Nachbarblöcke aktualisiert werden.

## Änderungen

1. **Kolbenschutz:** `CropBlocks` registriert genetische Crop Sticks, Melonen- und Kürbisfrüchte mit `BlockBehaviour.Properties.pushReaction(PushReaction.BLOCK)`. Damit sind die ortsgebundenen BlockEntity-Daten nicht regulär durch Kolbenbewegung verschiebbar. Die Methode ist in NeoForge 1.21.1 verfügbar: https://lexxie.dev/neoforge/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.Properties.html
2. **Stängelzuordnung vor Fruchtbindung:** `GeneticGourdFruitBlockEntity.bind` prüft zusätzlich, ob der Elternblock noch Crop Sticks ist, der Elternstängel nicht bereits eine Frucht besitzt und die physische Fruchtart zum gespeicherten Genom (`minecraft:melon_plant` oder `minecraft:pumpkin_plant`) passt. Fremde Pflanzenarten können keine neue Frucht an sich binden.
3. **Doppelte Abschlussprüfung:** `CropSticksBlockEntity.markPhysicalFruit` verwirft Eigentumsansprüche bei bereits entferntem Stängel, nicht mehr vorhandener oder ausgetauschter Eltern-BlockEntity, falschem Blocktyp oder Nicht-`GOURD`-Modus. Die bisherige Reife-, Richtungs- und Eigentumsprüfung bleibt erhalten.
4. **Vorsichtige Platzierung:** `GourdFruits.tryPlace` prüft vor dem Platzieren, ob die exakte Stamm-BlockEntity weiterhin vorhanden und reif ist und nicht bereits eine Frucht hält. `level.setBlock` kann Nachbar-/Blockereignisse auslösen; schlägt danach die Eigentumsbindung fehl, entfernt der Rollback den neuen Fruchtblock **nur**, wenn weiterhin der erwartete Block und dieselbe unmittelbar nach dem Platzieren beobachtete BlockEntity dort liegen. Er entfernt keine zwischenzeitlich anders ersetzten Blöcke.
5. **Explosion und Entfernen:** Bereits vorhandene `onRemove`-Hooks bleiben maßgeblich: Bei Fruchtentfernung wird der identische Eltern-Token geprüft; beim Stängelabbau wird eine noch vorhandene Frucht nur entkoppelt. Die Reihenfolge, in der beide Blöcke zerstört werden, ändert daran nichts, dass die Stängelernte bei gesetztem `fruitReady` keine zusätzlichen Produkte erzeugt. Die Vanilla-Frucht-Loot-Delegation bleibt unverändert.
6. **Chunkgrenzen:** Kein zusätzlicher Chunk-Force-Load. Die periodische Eigentumsprüfung aus `ORPHAN_GOURD_FRUIT_LIFECYCLE.md` bleibt für später wieder geladene Chunks zuständig.

## Warum kein zusätzliches Netzwerkpaket?

Die neuen Prüfungen verändern keine synchronisierten Datenstrukturen. Bei erfolgreicher Fruchtplatzierung und beim Entkoppeln werden weiterhin die bestehenden BlockEntity-Update-Mechanismen verwendet. Die explizite Blockreaktion verhindert bei regulären Kolbenvorgängen das Verschieben von Eigentumsdaten ohne ihre räumlichen Bezugspositionen.

## Zu überprüfen, sobald Builds/Tests wieder erlaubt sind

- Compile/API für `PushReaction.BLOCK`, `BlockEntity.isRemoved()` und BlockEntity-Lookups.
- Kolben mit Crop Sticks, genetischer Melone und genetischem Kürbis (auch klebrige Kolben).
- Explosion, die nur den Stängel, nur die Frucht oder beide Blöcke entfernt.
- Fruchtplatzierung, bei der ein Block-Callback den Zielblock oder Stängel austauscht.
- Neue Gourd-Frucht nahe einer Chunkgrenze, während ein Nachbarchunk entladen wird.
- Loot- und Eigentumsprüfung beim Wechsel zwischen `GOURD` und `STANDARD` nach `/reload`.

**Nicht ausgeführt:** Build, Tests, CI oder Minecraft-Start, entsprechend der vorhandenen Nutzeranweisung. Die Änderungen sind Quellcode-Absicherungen, nicht praktisch validiert.
