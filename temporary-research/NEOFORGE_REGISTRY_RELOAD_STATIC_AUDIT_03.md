# NeoForge 1.21.1 – Registry- und Reload-Audit, Runde 3
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Arbeitsmodus
**Keine Builds, Tests, CI-Läufe oder Spielstarts.** Quellcodeprüfung des aktuellen NeoForge-Ports und Abgleich ausgewählter API-Signaturen mit 1.21.1-Dokumentation. Implementierte Änderungen wurden direkt in den GitHub-Branch committet, sind jedoch nicht kompiliert.

## Statisch geprüfte APIs
- `SimplePreparableReloadListener<T>` stellt `prepare(ResourceManager, ProfilerFiller)` und `apply(T, ResourceManager, ProfilerFiller)` bereit: https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/server/packs/resources/simplepreparablereloadlistener
- `BlockBehaviour.Properties.ofFullCopy(BlockBehaviour)` existiert in 1.21.1: https://lexxie.dev/neoforge/1.21.1/net/minecraft/world/level/block/state/BlockBehaviour.Properties.html
- `Sheets.cutoutBlockSheet()` existiert in 1.21.1: https://aldak.netlify.app/javadoc/1.21.1-21.1.x/net/minecraft/client/renderer/sheets
- `CropAppearance` enthält die Enumwerte `MELON` und `PUMPKIN` samt Mapping für `minecraft:melon_plant` und `minecraft:pumpkin_plant`. Damit ist der zuvor vermutete Enum-Mismatch im **aktuellen** Quellcode nicht vorhanden.

## Korrekturen

### Unsinnige, aber registrierte Vanilla-Inhalte
`PlantRegistryValidator.errors` prüfte bislang nur, ob Item- und Block-IDs im Registry-Katalog vorhanden sind. `minecraft:air` ist ein registrierter Spezialwert und würde damit fälschlicherweise als Saatgut, Ernteprodukt oder Pflanzenboden akzeptiert.

Jetzt werden `Items.AIR` als Saatgut/Ernteprodukt und `Blocks.AIR` als expliziter Bodenblock zurückgewiesen. Der Datapack-Diagnosebericht gibt den Fehlertyp zusammen mit der referenzierten ID aus. Dies verhindert insbesondere leere Ausgaben und nicht sinnvolle Bodenbedingungen. Andere vorhandene, aber nicht für Pflanzen geeignete Mod-Blöcke können weiterhin als Boden definiert werden; dafür ist ein Mod-Adapter oder eine spielseitige Prüfung nötig.

### Dateisuche nur im vorgesehenen Ordner
`CropDataReloadListener.readJson` filtert die von `ResourceManager.listResources` gelieferten Dateien jetzt zusätzlich explizit auf `root + "/"`. Dadurch erscheinen JSON-Dateien mit nur ähnlich beginnendem Verzeichnisnamen nicht als Pflanzen- oder Mutationsdatei. Die `.json`-Dateiendung bleibt Voraussetzung.

## Keine Änderungen erforderlich
- Der gemeinsame Server-Reload verwendet bereits eine `SimplePreparableReloadListener`-Instanz für Pflanzen/Mutationen und publiziert einen atomaren Registry-Snapshot.
- Die durch frühere Arbeit eingefügten `CropAppearance.MELON`- und `PUMPKIN`-Konstanten stimmen mit der multipart-Blockstate-Datei überein.
- Die Client-Blockatlas-Rendering-API `Sheets.cutoutBlockSheet()` ist dokumentiert vorhanden. Das beweist noch nicht die Darstellung unter Resource-Reload oder als echter BlockEntityRenderer.

## Noch offen
- Eine vollständige Java-21-Kompilierung und Laufzeitvalidierung mit NeoForge 21.1.265 steht aus.
- Unbekannte historische AgriPlants-Tags, Saatgut-Metadaten und Mod-Bodenregeln benötigen weiterhin Adapter.
- Der Renderer und die Owned-Gourd-Loot-Delegation sind nicht im Spiel geprüft (insbesondere Texturatlas, Silk Touch und Fortune).
- Das `prepare`-Lesen der gesamten Dateisammlung hat keine spezifischen Größenlimits; bei sehr großen oder beschädigten Packs ist zusätzlicher Schutz denkbar.
- Die Registrierung von Vanilla- und Mod-BlockEntitytypen unter konkreten Weltbedingungen ist noch nicht getestet.

## Empfohlener nächster Schritt
Die Datapack-Parsergrenzen (insbesondere große `stages`-Listen, Produktzahl, Seiteneffekte unter Mod-Daten) und die Client-Pflanzenstufen statisch abgleichen; erst mit ausdrücklicher Erlaubnis Builds/Tests starten.
