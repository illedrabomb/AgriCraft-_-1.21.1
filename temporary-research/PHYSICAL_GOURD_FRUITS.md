# Echte Kürbis- und Melonenblöcke neben Crop Sticks
Stand: 2026-11-15 | Branch `feature/neoforge-1211-foundation` | **Auf Nutzerwunsch keine Tests/Builds**

## Implementierung
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/GourdFruits.java`: physische Fruchtplatzierung nur für die explizit unterstützten Pflanzenarten `minecraft:melon_plant` und `minecraft:pumpkin_plant`; echte Vanilla-Blöcke `Blocks.MELON` bzw. `Blocks.PUMPKIN`. Nur leere horizontale Nachbarpositionen oberhalb von Farmland, Dirt, Grass Block, Coarse Dirt oder Podzol werden berücksichtigt. Reihenfolge der vier möglichen Nachbarn wird pro Versuch zufällig gewählt.
- `CropSticksBlockEntity`: neue persistente `FruitDirection`-Angabe (Direction NORTH/SOUTH/EAST/WEST). `FruitReady` bedeutet nun: ein tatsächlicher Fruchtblock wurde in dieser Richtung platziert. Setzen erfolgt **nach erfolgreicher Blockplatzierung**, und das Eltern-Genome bleibt unverändert. `reconcilePhysicalFruit` erkennt bei späteren Random-Ticks, wenn der echte Block fehlt oder die alte Richtung aus Altsaves nicht bekannt ist, löscht `FruitReady` und setzt die Wachstumstufe auf `harvestStage` zurück.
- `CropSticksBlock.randomTick`: bei reifer Ranke mit passender Umwelt wird die aus Gain, `fruitChance` und Licht/Bodenfaktor berechnete Fruchtchance benutzt, um `GourdFruits.tryPlace` auszulösen. Ist die Frucht noch vorhanden, wird keine zweite erzeugt. Nach Entfernung setzt ein späterer Random-Tick die Ranke zurück.
- `CropSticksBlock.useWithoutItem` sowie `getDrops` benutzen `readyForHarvest`. Dieses erlaubt Standard-Ernte nur bei Standardpflanzen, **nicht bei Gourd**. Fruchtblock und seine Vanilla-Drops werden damit ausschließlich durch den tatsächlichen Vanilla-Fruchtblock gesteuert; der Crop-Stick-Block gibt beim Abbau nur Stäbe und den Genom-Samen zurück.
- `CropSticksRenderer`: die veraltete virtuelle Fruchtschicht wurde aus dem Renderer entfernt. Die Ranke wird weiterhin vom BER, die reale Frucht vom Minecraft-Blockrenderer dargestellt.
- `PlantDefinition`: `GOURD`-Mode ist bislang nur für explizite Vanilla-Melon/Pumpkin-IDs erlaubt. Andere Arten benötigen einen eigenen Spezialadapter, statt stillschweigend ohne Frucht zu wachsen.

## Spielablauf im **Quellcode**
1. Melonen- oder Kürbissamen auf leere Crop Sticks pflanzen (nach den Boden-/Lichtregeln).
2. Aufwachsende Ranke erreicht Reife.
3. Nach zusätzlichen Server-Random-Ticks entsteht bei erfolgreichem `fruitChance`-Wurf ein Block **direkt nördlich, südlich, östlich oder westlich**, wenn der Platz leer ist und unter ihm geeignetes Standarderdreich liegt.
4. Der Spieler erntet die Frucht durch normalen Abbau des Vanilla-Melon-/Pumpkin-Blocks. Der Block bestimmt die Drops (z.B. Vanilla-Melon-Slices und Kürbisblock gemäß Vanilla-Loot), **nicht** die `CropHarvest`-Produkte.
5. Beim nächsten Random-Tick erkennt die Mutterpflanze die fehlende Frucht und setzt ihre Wachstumsstufe zurück; anschließend kann eine neue Frucht entstehen.
6. Wenn die Mutterpflanze während vorhandener Frucht abgebaut wird, verbleibt der physische Fruchtblock als normaler Block in der Welt und kann separat abgebaut werden.

## Wesentliche Abweichungen / Risiken
- **Nicht kompiliert, nicht getestet**. Die konkrete NeoForge/Minecraft-1.21.1-API-Signaturen, Registry-Zuweisungen, Synchronisierung und Rendering wurden nicht praktisch verifiziert.
- Das Frucht-Tracking geschieht per **gespeicherter Richtung und Blocktyp**, nicht über einen eigenen Fruchtblock/BlockEntity mit eindeutiger Parent-ID. Wenn eine andere Mod oder ein Spieler den fehlenden Nachbarblock durch einen **identischen Fruchtblock ersetzt**, gilt dieser als vorhandene Frucht. Eine strengere eindeutige Ownership-Verknüpfung würde einen eigenen Fruchtblock samt BE oder ein globales Tracking benötigen.
- Entfernte Früchte werden erst **auf einem zukünftigen Random-Tick** erkannt, nicht sofort beim Nachbar-Update. Damit ist die visuelle `FRUIT`-Statusproperty eventuell kurzzeitig noch true; die Frucht selbst ist aber schon weg.
- Die Ranke ist aktuell nicht passend zur Frucht ausgerichtet; normale Vanilla-Fruchtblöcke nutzen ihre standardmäßige Darstellung ohne besondere Stiel-Verbindung.
- Beim Fruchtblock-Bruch gelten Vanilla-Lootregeln und Werkzeuge statt unserer `gain`-Erntemengen; Gain steuert ausschließlich die Fruchtbildungschance.
- Wird keine freie Nachbarposition mit geeignetem Untergrund gefunden, entsteht keine Frucht; die Ranke versucht es bei späteren Random-Ticks erneut.
- Veraltete Saves mit `FruitReady=true` ohne `FruitDirection` werden beim nächsten relevanten Random-Tick korrigiert.
- Keine speziellen Bodentags, Farmwerkzeug-Boni, automatischen Farmer, serverseitigen Konfigurationsmöglichkeiten, echten AgriCraft-Gourd-Blöcke oder kompatiblen Rezeptadapter.
- Bei Referenzproblemen/unkompatiblen Minecraft-Versionen gilt: Build- und Ingame-Validierung muss **vor** einer Veröffentlichung stattfinden, sobald Tests wieder erlaubt werden.

## Nächste Schritte
1. Wenn wirklich eindeutige Parent-Ownership, Sofort-Ernte-Erkennung und spezifische Mod-Gourds benötigt werden: eigenen `GeneticGourdFruitBlock` plus BlockEntity mit Parent-Pos und Parent-ID einführen, oder eine sichere Welt-Datenindexierung.
2. Vanilla-Stem-Ausrichtung zur vorhandenen Frucht ergänzen; keine Kollision mit FruitBlock.
3. Block-/Loot-Signaturen, Client/Server und Savegame-Stabilität beim späteren Testfreigabe prüfen.
