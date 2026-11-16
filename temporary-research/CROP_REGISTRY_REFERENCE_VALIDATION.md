# Crop Genetics – Item- und Blockreferenzen in Datapacks
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Umfang

Der neue `PlantRegistryValidator` in `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/` vergleicht explizite Ressourcen-IDs von aktivierten Pflanzen mit den tatsächlichen Minecraft-/NeoForge-Registries:

- `seed_items[].object` → `BuiltInRegistries.ITEM.getOptional(id)`
- `products.products[].object` → `BuiltInRegistries.ITEM.getOptional(id)`
- `cropgenetics_requirements.soil_block` / unterstützte Legacy-`soil_block` → `BuiltInRegistries.BLOCK.getOptional(id)`

Der Validator liefert eine unveränderliche Liste lesbarer Fehler, z.B.:

```text
Unknown seed item: examplemod:missing_seeds
Unknown harvest item: examplemod:missing_fruit
Unknown soil block: examplemod:missing_soil
```

Im gemeinsamen `CropDataReloadListener.apply` wird diese Liste **nach dem JSON-Parsing, vor der Aufnahme in den Pflanzenkatalog** ausgewertet. Eine fehlerhafte Pflanze bekommt den Diagnose-Status `INVALID`, einschließlich aller gefundenen Referenzfehler. Die Art wird nicht registriert.

Mutationen werden anschließend gegen denselben neuen Pflanzenkatalog geprüft. Fehlt eine zuvor wegen Referenzfehlern ausgeschlossene Pflanze, erhält die Mutation entsprechend `MISSING_SPECIES`. Alle Einträge liegen weiter im atomaren `AgriPlantsRegistry.Snapshot` und können per Operator-Befehl `/cropgenetics data errors` angezeigt werden.

## Beispiele

Eine Pflanze mit `"seed_items":[{"type":"item","object":"examplemod:nonexistent_seed"}]` wird nach erfolgreichem Schema-Parsing nicht mehr aktiviert, wenn das Item nicht registriert ist. Auch eine Vanilla-Frucht mit falsch geschriebenem Itemnamen (z.B. `minecraft:melonn`) wird abgewiesen. Die Engine erzeugt keine still leeren Harvest-Drops mehr für solche ungültigen Referenzen.

## Bewusste Grenzen

- **Block-Tags:** `soil_tag` wird vom Parser bereits syntaktisch geprüft, aber nicht über `BuiltInRegistries.BLOCK` auf Existenz kontrolliert. Tags sind durch Datapacks dynamisch und können auch leer sein. Eine zuverlässige Prüfung von Tag-Bindungen und Mitgliedern erfordert den zum Reload gehörenden Registry-/Tag-Datenstand und ist hier noch nicht implementiert.
- **Pflanzen ohne Seed-Items:** Leere `seed_items`-Listen bleiben erlaubt. Solche Pflanzen können künftig nur über Kreuzung oder einen speziellen Adapter erhältlich sein; die neue Referenzprüfung erfindet kein Saatgut.
- **Mod-Abhängigkeiten:** Fehlende Pflicht-Mods werden weiterhin **vor** der Registry-Prüfung als `MISSING_MOD` behandelt, damit der Fehler verständlich bleibt.
- **Ressourcen/Modelle:** Client-Texturen, Modellvarianten, Loot-Tabellen und Blockatlas-Sprites sind **keine Item-/Block-Registry-Einträge**. Deren Existenz wird von dieser Änderung nicht behauptet.
- **Die Welt-NBT bleibt erhalten:** Genetische Samen bereits vorhandener, derzeit ungültiger Arten werden nicht automatisch umgeschrieben. Ohne aktuelle Pflanzenregistrierung können sie nicht weiterwachsen.
- **Keine Builds oder Tests durchgeführt** (auf Nutzerwunsch). Alle Änderungen sind Quellcode-Commits und müssen vor einer Veröffentlichung kompiliert und im Spiel geprüft werden.

## Nächste Schritte

Eine separate serverseitige Tag-Validierung für `soil_tag` und ein Ressourcenaudit für vorhandene Vanilla-/Mod-Texturen wären die nächste Stufe. Danach empfiehlt sich die erste ausdrücklich genehmigte Kompilierung mit Minecraft 1.21.1 und NeoForge 21.1.x, bevor weitere Spezialpflanzen importiert werden.
