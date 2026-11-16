# Kürbis- und Melonenspielmechanik: Code-Implementierung
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## GitHub-Änderungen
- Neues `data/HarvestMode.java`: `STANDARD` oder `GOURD` als expliziter Harvest-Lifecycle.
- `data/PlantDefinition.java` hat optionale deklarative Parameter `harvestMode`, `fruitChance` und prüft einen einzelnen garantierten Fruchtblock als Gourd-Produkt sowie gültige Stufen, Chance und Regrowth.
- `data/AgriPlantsParser.java` liest `cropgenetics_harvest_mode` und `cropgenetics_fruit_chance` (Standard: `standard` bzw. 0).
- `content/CropSticksBlockEntity.java` speichert ein eigenes persistentes `fruitReady`-Flag zusätzlich zu Genom und Wachstumsstufe. Neue Pflanzen/Kindpflanzen beginnen ohne Frucht. Das Flag ist im NBT-Update enthalten und über die `FRUIT`-BlockState-Property an den Client gekoppelt.
- `content/CropSticksBlock.java` unterscheidet reife Ranke und vorhandene Frucht: Nach der letzten Wachstumsstufe würfelt der Server bei einem weiteren Random-Tick `min(1, fruit_chance * (1 + (gain-1)/10) * environment_factor)`. Erst bei Erfolg ist eine Gourd-Pflanze erntebereit. Das normale Wachstum und alle bisherigen Crops bleiben unverändert. Bei normaler Ernte wird die Frucht entfernt und die Ranke für erneutes Wachstum auf `harvestStage` zurückgesetzt; das Genom bleibt erhalten. Bei Blockzerstörung wird die vorhandene Frucht nur dann gedroppt, wenn `fruitReady` gesetzt ist.
- `content/CropHarvest.java`: aus einer gebildeten Gourd-Frucht entsteht exakt **ein** Fruchtblock; Gain vervielfacht nicht die Zahl der aus derselben Frucht gedroppten Blöcke.
- `client/CropSticksRenderer.java`: die bereits vorhandene zweite Gourd-Texturlage erscheint nur, wenn `FRUIT` wahr ist. Die eigentliche Frucht ist aktuell eine angenäherte Texturfläche, kein physischer Nachbarblock.
- `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/melon.json` und `pumpkin.json`: beide Pflanzen sind neu registriert und besitzen normale Saatgut-IDs, 8 Wachstumsstufen, je einen garantierten `minecraft:melon` / `minecraft:pumpkin`-Block als Frucht, `fruit_chance=0.2` und standardisierte Farmland-/Licht-Bedingungen.
- `CropGeneticsMod.java`: genetische Starter-Samen für Kürbis und Melone ins Kreativinventar aufgenommen.

## Spiellogik im Code
1. Normale `minecraft:melon_seeds` oder `minecraft:pumpkin_seeds` beziehungsweise genetische Samen auf **einfachen, leeren Crop Sticks** pflanzen.
2. Ranke wächst anhand von Growth, Boden, Licht und Strength; Genom und genauer Entwicklungsstand sind gespeichert.
3. Nach der Reife beginnt separat die Fruchtbildung. Der Gain-Wert beschleunigt die Chance, eine Frucht zu bilden. Eine Frucht kann nur ein einziges Mal gebildet werden, bis die bereits gebildete Frucht abgeerntet wurde.
4. Rechtsklick nach erfolgreicher Fruchtbildung droppt einen Fruchtblock und setzt das Stadium zurück; ohne reife Frucht keine Ernte.
5. Breaking droppt den genetischen Samen und bei gebildeter Frucht genau einen Fruchtblock.
6. Die vorher implementierte Kreuzungsmechanik kann aus reifen Pflanzen Eltern auswählen; die Nachkommen behalten ihre Genetik und wachsen wie die Elternart.

## Unterschied zum Original und bekannte Grenzen
- **Keine originalgetreue Gourd-Mechanik**: Die Frucht entsteht direkt als visuelle Fruchtschicht auf der Crop-Stick-Position. Noch keine echte Kürbis-/Melonenblock-Platzierung auf benachbarten Feldern, kein Stem-Facing, keine Frucht-Hitbox und keine vanilla-ähnliche Fruchtabtrennung.
- Alte AgriPlants-Produktmengen (z.B. bei Melonen 3–5) wurden hier absichtlich in **einen Block pro Frucht** umgewandelt. Diese Anpassung ist sichtbar in den NeoForge-Pflanzendefinitionen und kein 1:1-Datenimport.
- Originale Bodenchemie, Klimabedingungen, Jahreszeiten, Spezial-Biome und mögliche Mod-Callbacks sind nicht enthalten. Farmland und Licht 9..15 bilden eine **vereinfachte Version**.
- Die bestehenden Gourd-Client-Textur-JSONs für Melone/Kürbis sind jetzt mit den Server-Plant-IDs verknüpft; ob sämtliche Texturpfade in der finalen 1.21.1-Ressourcenauflösung verfügbar sind, muss im Client geprüft werden.
- Das erste Gourd-Rendering verwendet ein Kreuz aus zweidimensionalen Texturen, keine vollständig ausgearbeitete dreidimensionale Frucht.
- Bei vorhandenen alten gespeicherten Welten wird `FruitReady` für ältere Blöcke standardmäßig false; diese Reifepflanzen brauchen einen weiteren Random-Tick, um eine Frucht auszubilden.
- Keine serverseitigen Konfigurationsoptionen, kein Growth-Soil-Multiplikator, keine Vanilla-Farmer-Interaktion, kein zusätzliches Harvest-Event und keine echte Kontextprüfung auf Werkzeug/-Entnahme.
- **Auf Nutzerwunsch nicht kompiliert oder getestet.** Die APIs, World-Save-Integrität und Dedicated-Server-Interaktion müssen vor einer Veröffentlichung geprüft werden.
- Code/Definitionen wurden **direkt auf dem Branch committed**. Keine Builds/Tests/CI ausgelöst.

## Nächster sinnvoller Schritt
Entweder (A) physische Fruchtblöcke und Stem-Direction als echte Gourd-Mechanik einführen oder (B) den bereits entstandenen Code zunächst über Quellcode-/API-Inspektion stabilisieren (besonders Block-Drops/Render-/Reload-Hooks), ohne Tests zu starten. Bei weiteren Spezialarten ein eigenes Lifecycle-Interface statt Gourd-Sonderfällen in jeder Blockklasse planen.
