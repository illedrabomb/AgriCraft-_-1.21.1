# HarvestCraft 1 → HarvestCraft 2: belegte Crop-ID-Zuordnung

**Stand:** 16.11.2026 · **Branch:** `feature/neoforge-1211-foundation`  
**Status:** Quellcode/JSON im Repository. **Keine Ausführung, Builds, Tests, Minecraft-Starts oder echte Migration.**

## Quellenlage

1. **HarvestCraft 1, Minecraft 1.12.2:** `MatrexsVigil/harvestcraft`, `src/main/java/com/pam/harvestcraft/blocks/CropRegistry.java`. Der Originalcode definiert `CROP_BLOCK_NAME = "pam{0}crop"`, `ITEM_NAME = "{0}item"`, `SEED_ITEM_NAME = "{0}seeditem"` und registriert diese Gegenstände für die Einträge von `cropNames`. Die Datei wurde direkt eingesehen. Wichtig: Kaffee, Senf und Tee haben im Original ausdrücklich abweichende Saatgutnamen!
2. **HarvestCraft 2:** `AgriCraft/AgriPlants`, gepinnter Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Dateien `mod_pamhc2crops/plants/<name>_plant.json`. Diese enthalten die jeweilige species-, Saatgut- und Ernte-ID. Zusätzlich wurde ein HarvestCraft-2-Crops-`ItemRegistry.java`-Ausschnitt eingesehen; das ist noch **kein vollständiger Nachweis der NeoForge-1.21.1-Registry**.
3. **Lizenz:** Keine Bild-, Modell- oder Java-Dateien der HarvestCraft-Mod wurden ins Mod-Runtime übernommen. Der Crosswalk enthält nur eigene strukturelle ID-Zuordnungsdaten.

### Manifest (30 bewusst eingeschränkte Fälle)

`neoforge-port/tools/data/pams_harvestcraft_1_to_2_crops.json` enthält **30 explizite Einträge**: tomato, corn, rice, oats, rye, onion, cabbage, cotton, bean, flax, okra, leek, jute, kiwi, taro, kale, peas, kenaf, grape, lettuce, strawberry, soybean, barley, celery, broccoli, sweetpotato, radish, scallion, cantaloupe, blueberry.

Für jeden Fall sind diese Quell-/Zielgruppen festgehalten:

| HarvestCraft 1 (1.12.2) | HarvestCraft 2 (NeoForge-Ziel) |
|---|---|
| `harvestcraft:<crop>seeditem` | `pamhc2crops:<crop>seeditem` |
| `harvestcraft:<crop>item` | `pamhc2crops:<crop>item` |
| `harvestcraft:pam<crop>crop` | **nur semantische Zuordnung** zu `pamhc2crops:<crop>_plant` |

Die Notation `<crop>` steht nur für die **30 ausdrücklich enthaltenen** Crop-Namen und darf **nicht** automatisch auf alle HarvestCraft-Gegenstände angewendet werden. Block-ID zu Pflanzen-Art-ID ist **keine direkte Blockkonvertierung**. Pam's-1-Fruchtbäume, Gartenblöcke, Rezepte, NBT, Metadata und andere Pflanzen außerhalb des Manifests bleiben unzugeordnet.

Als nachgewiesene **Namensausnahmen** wurden notiert:
- `coffeebean` → `harvestcraft:coffeeseeditem`
- `mustardseeds` → `harvestcraft:mustardseeditem`
- `tealeaf` → `harvestcraft:teaseeditem`

Diese Ausnahmen besitzen aktuell **keine freigegebenen HarvestCraft-2-Zielzuordnungen** im Manifest.

## Rein lesendes Werkzeug

`neoforge-port/tools/pam_legacy_crosswalk.py` bietet zwei Funktionen:

- Es überprüft beim **ausdrücklichen Start** die Manifeststruktur, gültige Namespaces, eindeutige Source-/Target-IDs sowie das Verbot automatischer Zuordnungen.
- Mit `--lookup harvestcraft:tomatoseeditem` kann es einen exakten Kandidaten für die neue Saatgut-ID anzeigen; `--metadata` und `--has-nbt` verhindern eine Zuordnung für unbekannte Legacy-Daten.

**Das Skript wurde nicht ausgeführt**, entsprechend der Nutzeranweisung. Es liest nur das Manifest, gibt JSON auf stdout aus und verändert keinerlei Dateien.

Die Auskunft hat den Status `candidate_not_verified_in_target_registry`. Selbst bei einem Treffer bedeutet dies **keine automatische Freigabe**, kein gültiges 1.21.1-Item-Objekt und keine Sicherheitsgarantie für eine Weltmigration.

## Nachtrag: aktuelle 1.21.1-Version und Prüfschranke (16.11.2026)

Die offizielle CurseForge-Veröffentlichung bestätigt die NeoForge-1.21.1-Variante `pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar` vom **09.05.2026**:
https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops

WICHTIG: **Veröffentlichung bestätigt nicht einzelne Item-IDs**. Der alte H1-`CropRegistry.java`-Code und die gepinnten AgriPlants-1.18.2-Pflanzen sind überprüfte historische Quellen; die tatsächlichen Item-Registrierungen der 1.21.1-1.0.9-JAR wurden bislang **nicht** überprüft. Es wäre falsch, die 30 Kandidaten bereits als vollständig migrationsfähig zu deklarieren.

Das Prüfwerkzeug `neoforge-port/tools/pam_legacy_crosswalk.py` kann zusätzlich mit `--verify-own-definitions` die **lokalen neuen `pamhc2_*.json`** auf exakte Übereinstimmung mit den gespeicherten 30 Crosswalk-Ziel-IDs prüfen. Es liest lediglich eigene Datapack-Dateien, prüft Spezies-, Saatgut-, Produkt-ID und `mods: ["pamhc2crops"]`, meldet fehlende/ungültige Werte und scheitert bei Fehlern. **Diese Prüfung wurde nicht ausgeführt**; selbst bei Erfolg wäre ausschließlich die interne Datapack-Konsistenz bestätigt, **nicht** die reale NeoForge-Item-Registry.

Ein `lookup`-Ergebnis enthält jetzt für alle Typen explizit `direct_item_replacement: false`. Unbekannte Metadaten (ungleich 0) oder NBT führen weiterhin zu keinem Kandidaten. Ein Pflanzenblock-Mapping beschreibt weiterhin nur eine **Artbeziehung**, keinen Zielblock. Es gibt weiterhin **keine Welt- oder Item-Konvertierung** und keine heimliche Änderung von Spielständen.

### Erweiterung um acht eindeutig belegte Crop-Namen (16.11.2026)

Die nachträglich eingefügten Originalnamen `barley`, `celery`, `broccoli`, `sweetpotato`, `radish`, `scallion`, `cantaloupe`, `blueberry` sind in HarvestCraft 1s ursprünglicher `CropRegistry.cropNames`-Liste verzeichnet und haben jeweils passende Original-AgriPlants-Crop-Definitionen für HarvestCraft 2. Sie wurden deshalb als weitere acht **source-grounded Kandidaten** in die JSON-Crosswalk-Datei aufgenommen. Das rein lesende Skript erwartet dadurch bei einem späteren `--verify-own-definitions`-Lauf **30** eigene Crop-JSON-Dateien. **Kein Skript wurde ausgeführt**; die tatsächlichen Item-IDs der modernen NeoForge-Pam's-2-JAR bleiben weiterhin unbestätigt. Auch hier wird keinerlei automatische Item- oder Blockkonvertierung ausgelöst.

## Integrationsentscheidung

- **HarvestCraft 2:** direkte optionale NeoForge-Laufzeitkompatibilität über die bereits angelegten `pamhc2_*.json`-Pflanzen.
- **HarvestCraft 1:** historische Quelle zur Migration von Spielern/Modpacks, **keine** offizielle HarvestCraft-1-NeoForge-1.21.1-Laufzeitmod.
- **Keine Weltkonvertierung ohne eigene Implementierung und Zustimmung.** Die Migration von 1.12.2-NBT, Minecraft-BlockIDs, DataFixer/ItemStack-Komponenten und möglicherweise gelöschten IDs ist ein eigener, besonders sorgfältig zu behandelnder Arbeitsbereich.
- **Unbekannte oder doppeldeutige Datensätze werden nicht geraten**; spätere Vergleichsdaten sollen versioniert und geprüft werden.

## Nachtrag 16.11.2026: 31 explizite Legacy-Kandidaten

Ein weiterer Originalname **brusselsprout** wurde auf Basis von HarvestCraft-1-`CropRegistry.cropNames` und der dazugehörigen AgriPlants-2-Pflanzendefinition in das H1→H2-Manifest aufgenommen. Die Crosswalk-Datei enthält somit **31** statt 30 Einträge. Sämtliche zuvor genannten Zahlen mit 22/30 waren historische Zwischenstände. Die Abfragen bleiben **rein lesend und nicht ausgeführt**. Ziel-JAR-Registrierungen sind unbestätigt; `direct_item_replacement: false` bleibt gesetzt und die H1-World-Migration ist weiterhin nicht implementiert. Separates Alias-/Mutationspapier: `docs/PAMS_VANILLA_SPECIES_ALIASES_AND_BRUSSELSPROUT.md`.

## Nächste Schritte

1. Weitere originale HarvestCraft-1-Kulturen einschließlich Namensausnahmen prüfen und in die Quell-/Zielmatrix einordnen; nur bestätigte Zielarten aktivieren.
2. Die konkrete Pam's-Crops-NeoForge-1.21.1-Item-Registry mit den aktuell vorhandenen 30 Art- und Seed-IDs abgleichen, ohne ungefragt eine Mod-JAR zu installieren oder Spieltests auszuführen.
3. Separaten Datentyp für einen späteren, ausdrücklich freizugebenden Migrationslauf entwerfen; niemals direkt ganze Welt-Dateien überschreiben.
4. Pam's 2 Trees/Obstbäume fachlich getrennt von Crop Sticks behandeln.

## Branch-/Testregeln

Nur `feature/neoforge-1211-foundation` geändert. `entwicklung`, `testing` und `master` bleiben unberührt. Builds, Tests, Spielstarts, Actions oder Skript-Ausführungen erst nach ausdrücklicher Zustimmung.
