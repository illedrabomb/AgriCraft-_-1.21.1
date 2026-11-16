# Vanilla-Texturen als Standard und analysierte Saatgut-Tooltips

Stand: 2026-11-15 | NeoForge 1.21.1 · Branch `feature/neoforge-1211-foundation`

**Keine Builds, Tests, CI oder Spielstarts ausgeführt.**

## 1. Vanilla-Texturen bevorzugen

Die Standard-Visual-JSONs der Vanilla-Pflanzen `minecraft:melon_plant` und `minecraft:pumpkin_plant` referenzieren wieder Vanilla-Sprites:
- `minecraft:block/melon_stem` und als zweite Gourd-Lage im letzten Frame `minecraft:block/melon_side`
- `minecraft:block/pumpkin_stem` und `minecraft:block/pumpkin_side`

Die anderen vorhandenen Vanilla-Pflanzen-JSONs (Wheat, Carrot, Potato, Beetroot, Nether Wart) bleiben unverändert. Originale AgriCraft-PNGs von Melone und Kürbis bleiben weiterhin im NeoForge-Port als **optionale Assets**; ihre bloße Existenz im Resource-Pack-Ordner überschreibt keine Vanilla-Texturen.

Die Crop-Sticks behalten die historische AgriCraft-Holztextur, da es kein direkt entsprechendes Vanilla-Crop-Sticks-Asset gibt.

**Wichtig:** Es handelt sich um die **Standardkonfiguration**, nicht um eine feste Prioritätsregel über beliebige Benutzer-Resource-Packs hinweg. Ein höher priorisiertes Resource-Pack darf die Visual-JSONs wie gewohnt überschreiben.

## 2. Analysezustand am genetischen Seed

Neuer persistenter Data Component `cropgenetics:analyzed` mit Boolean-Wert; bestehende genetische Daten liegen weiterhin in `cropgenetics:genome`. Der Analysezustand wird **pro ItemStack** gespeichert und mit dem Item synchronisiert (persistent `Codec.BOOL`).

Die API für ein Analyse-Item ist:
```java
boolean success = CropItems.markAnalyzed(seedStack);
```
Sie gibt `false` zurück, wenn das Item kein `cropgenetics:genetic_seed` mit Genome ist, und setzt andernfalls `cropgenetics:analyzed` auf `true` auf dem übergebenen Stack. Auf Serverseite nach erfolgreicher Analyse aufrufen, niemals nur als lokalen Tooltip-Effekt.

`CropItems.isAnalyzed(seedStack)` ist für Tooltip und spätere Benutzeroberfläche verfügbar.

Unanalysierte Saat zeigt ihre Pflanzen-ID und `Nicht analysiert`. Analysierte Saat zeigt Pflanzen-ID und `Analysiert – Shift halten für Genwerte`. **Nur bei gedrückter Shift-Taste auf dem Client** fügt `AnalyzedSeedTooltip` fünf Allelpaare und das bisherige Kurzformat für Wachstum/Ertrag/Robustheit hinzu. Der Client-only Handler benutzt `ItemTooltipEvent` auf dem Game Event Bus und `Screen.hasShiftDown()`, damit keine Client-Klasse aus `GeneticSeedItem` oder anderen Common-Klassen referenziert wird.

Nach Analyse bleiben beide Genome-Allele und der Analysemarker beim Speichern, Laden und durch die ItemStack-Synchronisierung erhalten. Bei neu erzeugten Samen wird kein Analysemarker gesetzt; sie sind zunächst unanalysiert. Wird ein Stack kopiert, übernimmt die Kopie seine Komponenten, einschließlich des Analysemarkers.

## 3. Anschluss an das neue Analyse-Item

Das im lokalen Codex-Schritt erwähnte zusätzliche Analyse-Item ist in der aktuellen **GitHub-Branch-Kopie** nicht zu finden. Der zuständige Handler muss nach dem tatsächlichen Scan `CropItems.markAnalyzed(seedStack)` aufrufen. Falls dieser Handler lokal schon existiert, die neue API dort integrieren, **keinen zweiten Analyzer registrieren**.

Die Shift-Anzeige und der Marker sind daher jetzt implementiert, der Aufruf durch das möglicherweise nur lokal vorhandene neue Item aber **noch nicht verifiziert oder verbunden**.

## 4. Noch offen

- Keine Kompilierung und keine Tests auf Client/Dedicated Server.
- Nach Merge mit lokalen Codex-Änderungen Analyse-Item-Hook verbinden.
- Tooltip für analysierte und unanalysierte Seeds, Shift-Tastenbehandlung sowie Multiplayer-ItemStack-Synchronisierung praktisch prüfen.
- Minecraft-Texturen sind Default, Ressource-Pack-Overrides können bewusst andere Texturen wählen.
