# Erweiterte Pflanzenformen: Cross, Plus, Gourd
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Tatsächlich umgesetzte Änderungen
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/client/PlantVisuals.java`: Reloadfähiges Ressourcenformat mit `geometry: "cross" | "plus" | "gourd"` und `stages`. Jede Stufe akzeptiert eine Textur-ID oder (nur bei GOURD) eine Liste aus höchstens zwei IDs. Die bisherige einfache `textures`-Liste bleibt abwärtskompatibel. Gespiegelte alte AgriPlants-`texture.render_type`-`hash/plus/gourd`- und `plant_textures`-Einträge werden ebenfalls gelesen, soweit sie maximal zwei Texturen pro Stufe und nur bei GOURD zwei enthalten.
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/client/CropSticksRenderer.java`: Das bisherige gekreuzte Doppelflächenmodell ist `CROSS`. Der neue `PLUS`-Modus nutzt zwei rechtwinklige Vertikalflächen. Im neuen `GOURD`-Modus erscheint zusätzlich zur gekreuzten Ranke eine zweite, kleinere gekreuzte Texturfläche als **vereinfachte Fruchtdarstellung**. Die originale AgriCraft-Geometrie mit Fruchtblock/Stem-Ausrichtung wird damit noch **nicht** reproduziert.
- `neoforge-port/tools/migrate_agriplants.py`: Recognizes `hash`, `plus` and `gourd`; outputs `geometry` plus `stages`. Die Originaltexturen im Namespace `agricraft:` werden standardmäßig abgelehnt, solange keine explizite `texture_replacements`-Zuordnung auf verfügbare Ressourcen oder eine `acknowledge_agricraft_assets`-Freigabe gesetzt wurde. Im originalen Forge-Subprojekt vorhandene Texturen sind **nicht automatisch** im neuen NeoForge-JAR enthalten.
- Drei **clientseitige Render-Beispiele** unter `assets/cropgenetics/cropgenetics/plant_visuals/`:
  - `melon.json`: GOURD, Vanilla-Melon-Stem mit zweiter Melonentextur in letzter Stufe
  - `pumpkin.json`: GOURD, Vanilla-Pumpkin-Stem mit zweiter Kürbistexur in letzter Stufe
  - `cornflower.json`: PLUS, Vanilla-Kornblumentextur

## Wichtige Einschränkung
Diese drei neuen JSON-Dateien sind **Visual-Ressourcen, keine aktivierten Pflanzendefinitionen**. Kürbis, Melone und Kornblume werden dadurch noch **nicht** als neue genetisch pflanzbare Arten registriert. Das wäre verfrüht: ursprüngliche Melonen/Kürbisse haben gourd-spezifische Fruchtmechaniken, weitere Pflanzen benötigen eigene Saatgut-/Soil-/Harvest-Adapter. Vor allem ist eine zweidimensionale Texturschicht **kein echter Melonen- oder Kürbisblock**.

Eine vollständige Original-AgriPlants-Migration von Spezialpflanzen inklusive Belohnungen, Füllung, Bedingungs-/Callback-Regeln und Geometrie steht weiterhin aus. Der vorhandene Vanilla-Katalog mit fünf vereinfachten Pflanzendefinitionen (Weizen, Karotten, Kartoffeln, Rote Bete, Netherwarzen) bleibt durch diesen Schritt unverändert.

## Formatbeispiel
```json
{
  "plant": "minecraft:melon_plant",
  "geometry": "gourd",
  "stages": [
    "minecraft:block/melon_stem",
    ["minecraft:block/melon_stem", "minecraft:block/melon_side"]
  ]
}
```
Die erste Textur jedes Stadiums bildet die Hauptpflanze, eine optionale zweite Textur die vereinfachte zusätzliche Fruchtschicht. Für andere Pflanzentypen ist pro Stadium aktuell nur eine Textur erlaubt.

## Offene Punkte
- **Keine Tests oder Builds durchgeführt**, wie ausdrücklich gewünscht. Renderer-Ereignisregistrierung, Blending, Normalen, Texturpfade, korrektes Laden aus Client-Ressourcen und Singleplayer-/Multiplayer-Darstellung sind nicht validiert.
- Der Source-Migrator wurde nur bearbeitet, **nicht ausgeführt**. Derzeit fehlt ein Pipeline-Report über sämtliche Originalpflanzen und die Abhängigkeiten ihrer Textur-Assets.
- GOURD sollte langfristig einen eigenen Blockmodell-/Frucht-Renderer bekommen, plus die serverseitige Fruchtbildung, Spezialernte und Mutation.
- Vor einer vollständigen Übernahme nicht vorhandene Textur-Ressourcen vermeiden und Client-Ressourcenpakete automatisch prüfen.
- Der alte CropAppearance-BlockState bleibt als Visual-Kompatibilitätsrest erhalten, aber das Aussehen kommt direkt aus der Genome-ID der synchronisierten BlockEntity.
