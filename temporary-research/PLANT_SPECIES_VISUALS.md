# Individuelle Pflanzenmodelle auf Crop Sticks
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation` | **Keine Builds und keine Tests durchgeführt**

## Implementierter Code
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropAppearance.java`: visuelle Enum-Kategorie `generic`, `wheat`, `carrot`, `potato`; Auswahl aus der originalen `Genome.plantId()`.
- `CropSticksBlock`: zusätzlicher `EnumProperty<CropAppearance> SPECIES` im Blockstate. Neue, unbepflanzte Stäbe starten als `generic`.
- `CropSticksBlockEntity.syncState`: beim Pflanzen, bei zufälligem Wachstum, nach der Ernte und bei einem neuen Kreuzungsnachkommen wird das Aussehen aus dem Genom synchronisiert; die Genetik selbst ändert sich dadurch nicht.
- `assets/cropgenetics/blockstates/crop_sticks.json`: neue **Multipart**-Definition. Basisstäbe und die sichtbare Pflanze bilden getrennte Render-Layer. Die Basis bleibt `crop_sticks` oder `crop_sticks_cross`, die Pflanze erscheint nur für `planted=true`.
- Weizen: `minecraft:block/wheat_stage0..7` (acht sichtbare Wachstumsphasen).
- Karotte: `minecraft:block/carrots_stage0..3` (vier Phasen, je zwei unserer Stufen).
- Kartoffel: `minecraft:block/potatoes_stage0..3` (vier Phasen, je zwei unserer Stufen).
- Unbekannte oder Mod-Pflanzen erhalten `cropgenetics:block/crop_generic`, ein neutrales Kreuzmodell mit Minecraft-Eichenlaubtextur. Die tatsächliche Pflanzenart und alle Allele bleiben im `Genome` erhalten.
- Die ursprünglichen grünen Platzhaltermodelle sind noch vorhanden, werden vom neuen Blockstate aber nicht mehr für Weizen/Karotten/Kartoffeln verwendet.

## Gründe für diese Architektur
Ein statischer Blockstate kann nicht sinnvoll jede potentielle Pflanze aus AgriPlants oder hunderten Modpacks als eigene Enum-Variante kodieren. Hier werden nur die 3 Vanilla-Beispiele visuell unterschieden. Für den späteren großen Pflanzenkatalog wird **ein datengetriebener Clientrenderer/BakedModel-Adapter** nötig, der die vorhandenen `texture.plant_textures` / `texture.render_type` aus AgriPlants nutzt. Das Species-Enum ist lediglich ein erster zuverlässiger Weg für die Vanilla-Beispiele, **keine allgemeine Plugin-API**.

## Noch zu beachten
- Das Modell-Rendering wurde **nicht im Client getestet**, auch der Quellcode nicht neu kompiliert (Nutzerwunsch). Ob die 1.21.1-Vanilla-Modellreferenzen und Render-Layer in allen Zuständen korrekt sind, ist nicht bestätigt.
- Alte Blockstates mit fehlender `species`-Eigenschaft müssen beim späteren Savegame-Migrationscheck berücksichtigt werden; das gespeicherte Genom ist maßgeblich.
- Für generische Modpflanzen derzeit ein gleichbleibendes Modell unabhängig von der Wachstumsstufe.
- Die alten `crop_sticks_planted`, `crop_sticks_plant_mid`, `crop_sticks_plant_tall` und `crop_sticks_cross_*` bleiben als unbenutzte Altmodelle im Asset-Ordner; Aufräumen erst nach Render-Validierung.
- Soll eine Pflanze als eigenständige Art erkennbar sein, ist die eindeutige Genome-ID wichtiger als der visuelle Enum-Blockstate.
- Keine Änderung an Wachstum, Ertrag, Züchtung, Genomdaten oder AgriPlants-JSON-Schema nötig.

## Als Nächstes
1. Pflanzentexturen und Render-Typ aus originalen AgriPlants-JSONs lesen, mit strenger Validierung.
2. Einen Client-Renderer einbauen, der beliebige registrierte Pflanzendefinitionen mit ihrer Textur rendert und generische Darstellung auf Dauer ersetzt.
3. Schrittweise alte Vanilla- und Mod-Pflanzendefinitionen migrieren. Bei Bedarf Compatibility-Overrides als Datapacks.
4. Falls der Nutzer irgendwann wieder Builds erlaubt: Compile, Client-Render-Check und Dedicated Server vor einer Releaseversion.
