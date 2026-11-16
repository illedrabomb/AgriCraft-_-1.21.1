# Lokale Validierung 16.11.2026: Freigabe gestoppt

## Stand und Isolation

Feature-Ausgang: `7ebf33de5f6842c944bde77ccb12de8577582ac4`, Branch `feature/neoforge-1211-foundation`. Mod-Version laut Metadaten **0.1.0-alpha**, nicht der abweichende Stand 0.2.14-alpha auf entwicklung. Minecraft **1.21.1**, NeoForge **21.1.265**, Temurin **21.0.12.1+1**, Gradle **9.2.1**, ModDev **2.0.148**.

Frischer Vollklon: `C:/Users/tinob/Documents/ChatGPT/AgriCraft-Validation-20261116`. Der bestehende Arbeitsklon `Agricraft FOrk` und seine lokalen Änderungen blieben unverändert. Ausschließlich `neoforge-port/` gebaut, keine Änderung am alten Forge-Projekt. Neue Welt: `neoforge-port/run/validation-server/validation-world`. Server ausschließlich localhost:25585, RCON localhost:25586; Offline-Modus nur für diese Testumgebung. Bestehende lokale EULA-Zustimmung übernommen. Keine produktiven Welten verwendet.

## Integrationssperre

Schreibfreie Merge-Vorschau mit `git merge-tree` und Basis `cfca4d110b9c0934cf800693481491f1fafa5364` zeigt Konfliktmarker zwischen Feature und entwicklung. Betroffen sind unter anderem CropGeneticsMod, CropConditions, CropSticksBlock, CropSticksBlockEntity, CrossBreeding und die Kaktusdefinition. Beispiel: Feature installiert Boden-/Chemie-Listener, die Gegenseite ersetzt denselben Konstruktorabschnitt durch ihre Kreativ-Tab-Registrierung. Lokales vollständiges Preview: `merge-preview.txt` im Klon-Root.

Gemäß Auftrag wurde keine Integration ausgeführt. Fachliche Konfliktauflösung und vollständige Abnahme bleiben erforderlich.

| Remote-Branch zu Beginn | SHA |
|---|---|
| feature/neoforge-1211-foundation | 7ebf33de5f6842c944bde77ccb12de8577582ac4 |
| entwicklung | ec0666a816116cd6db4f28c92bdf07867634cc83 |
| testing | ec0666a816116cd6db4f28c92bdf07867634cc83 |
| master | fe111987fb50759aa9607fc5b68f79430c124441 |

## Fehler und Änderungen

Alle Java-Dateien liegen unter `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`.

| Ursache | Korrektur | Nachprüfung |
|---|---|---|
| CropSticksBlock und GeneticGourdFruitBlock implementieren den abstrakten BaseEntityBlock.codec() nicht | Beide content-Klassen: simpleCodec und codec-Override | Kompilierung und Serverregistrierung PASS |
| registerSimpleBlockItem liefert DeferredItem<BlockItem>, nicht DeferredItem<Item> | content/CropItems.java korrekt typisiert | Build PASS |
| Feature besitzt keinen eigenen Kreativ-Tab, Items nur unter Zutaten | CropGeneticsMod.java registriert eigenen Tab mit Sticks, Analyzer und zwölf Startgenomen | Build/Serverregistrierung PASS; Client-Sichtprüfung nicht durchgeführt |
| Analyzer-Rezept verwendet String-Zutaten, 1.21.1 erwartet Ingredient-Objekte | resources/data/cropgenetics/recipe/seed_analyzer.json verwendet item-Objekte | Server-Nachprüfung unten |
| Standard-Run-Verzeichnis trennt Client und Server nicht explizit | neoforge-port/build.gradle setzt separate validation-Verzeichnisse | Neue Serverwelt erzeugt |

Ursprünglicher Laufzeitfehler:

```text
Parsing error loading recipe cropgenetics:seed_analyzer
JsonParseException: Map entry 'G': Failed to parse either.
First: Not a json array: "minecraft:glass_pane";
Second: Not a JSON object: "minecraft:glass_pane"
```

## Testmatrix

Ergebnisse gelten nur für den Feature-Ausgang plus dokumentierte lokale Korrekturen, nicht für testing/master. Nach erkannter Integrationssperre wurde die vollständige Abnahme angehalten.

| Test/Ziel | Schritte | Ergebnis | Evidenz/Grenze |
|---|---|---|---|
| Kompilierung | Gradle build im Port, Java 21 explizit | PASS nach Korrektur | Erster Lauf FAIL mit drei Compilerfehlern; Folgelauf BUILD SUCCESSFUL |
| Bestehende Unit-Tests | GeneticsTest 4 und AgriPlantsParserTest 3 | PASS | 7 Tests, 0 Fehler; Allele, Vererbung, Originaldaten-Parser und Mutationen |
| Neue Pam-Konfigurationsregressionen | Fremd-/Trees-Namespace, Cross-Role-Kollision, unbekannte Option, NaN ablehnen | PASS | PamAutoDiscoverySettingsTest: 4 Tests, 0 Fehler; keine Registry-Integration |
| Mod-Build | jar/assemble/check/build | PASS | build/libs/cropgenetics-0.1.0-alpha.jar, keine Produktivfreigabe |
| Dedicated-Start/Client-Klassen | runServer bis Done mit neuer Welt | PASS | Done (10.377s); Zielversionen in Modliste, kein Client-Classloading-Abbruch |
| Analyzer-Rezept laden | Start und Reload, RecipeManager prüfen | Erstlauf FAIL, korrigiert | Obiger Parsefehler, Nachtest unten |
| Basiskatalog ohne Pam | RCON /cropgenetics data | PASS | 12 Pflanzen, 1 Mutation, 83 Ressourcen; 70 MISSING_MOD für absichtlich fehlendes optionales Pam |
| Pam-Diagnose ohne Pam | RCON /cropgenetics data pam | PASS nur Abwesenheitsfall | 0 Auto-Spezies/Konflikte, 12 Gesamtarten; keine echte Pam-Abnahme |
| Geladene Pflanze nach unverändertem Reload | randomTickSpeed=0, forceload, Weizen-NBT-Fixture mit allen fünf Paaren 9/2, Stufe 3/8; reload; NBT lesen | PASS begrenzter Fall | Genom, GrowthStage=3, TotalGrowthStages=8, SeedAnalyzed erhalten; Katalog unverändert |
| Speichern | save-all flush und stop | PASS | Saved the game; All dimensions are saved. Neustart separat unten |
| Client/Rendering/Kreativ-Tab | Nicht gestartet | BLOCKED, nicht durchgeführt | Abnahme nach Integrationssperre angehalten; keine Aussage über fehlende GUI-Unterstützung |
| Crop Sticks/Double Crop Sticks durch Spieler | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Admin-NBT-Fixture ersetzt keine Interaktion |
| Pflanzen/Wachstum/Stufen/Licht/Boden/Umwelt/Geschwindigkeit/Ertrag | Nicht durchgeführt | BLOCKED, nicht durchgeführt | randomTickSpeed im begrenzten Persistenztest bewusst 0 |
| Kreuzung/Mutation in Welt | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Reine Genetiktests ersetzen keine Weltintegration |
| Ernte/Saatgutrückgewinnung/Analyzer-UI | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Rezeptladen ersetzt keine Herstellung/Bedienung |
| Ungültige Datapacks/geänderte Pflanzen bei Reload | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Unit-Tests belegen keine vollständige Snapshot-Konsistenz |
| Wachstum innerhalb gleicher BlockState-Stufe sichtbar | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Client-Server-Sichttest fehlt |
| Melonen/Kürbisse ohne Duplikation | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Keine Abnahme der Eigentums-/Erntezustände |
| Große Farm/Performance | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Leere Testwelt ist kein Lasttest |
| Echtes Pam Crops/Autodiscovery/Registry-Konflikte | Nicht durchgeführt | BLOCKED, nicht durchgeführt | Offizieller Kandidat gefunden, nicht installiert |
| feature → entwicklung → testing → master | Merge-Vorschau | BLOCKED | Mehrere Konflikte; keine Integration/Pushes |

Pam-Kandidat: [offizielle Datei pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar, CurseForge 8064883](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops/files/8064883), als NeoForge/1.21.1 ausgewiesen. Bezugsquelle geprüft; tatsächliche JAR-Abhängigkeiten, Registry und Kompatibilität nicht getestet. Trees wurde nicht installiert.

## Reproduktion und Belege

Arbeitsverzeichnis immer neoforge-port. Nicht den Root-Wrapper des alten Forge-Projekts verwenden. Java 21 über JAVA_HOME explizit setzen; System-Java ist noch Java 8. Verwendet wurde der vorhandene Gradle-9.2.1-Cache unter `C:/Users/tinob/.gradle/wrapper/dists/gradle-9.2.1-bin/2t0n5ozlw9xmuyvbp7dnzaxug/gradle-9.2.1/bin/gradle.bat`, Aufruf `--no-daemon --console=plain build` bzw. `build runServer`.

Lokale Logs im Port: first-build.log, second-build.log, final-build.log, server-validation.log, server-retest.log. JUnit-XML: build/test-results/test. RCON-Protokolle: run/baseline-results.json und run/reload-results.jsonl. Welten/Logs werden nicht als Mod-Inhalt veröffentlicht.

## Offene Risiken und nächster Schritt

Keine produktive Freigabe. Zuerst die divergierenden Branches fachlich abgleichen; Konflikte kontrolliert lösen, dann einen eindeutig versionierten Kandidaten gemäß Branch-Workflow erstellen und sämtliche fehlenden Integrations-/Gameplay-/Pam-/Clienttests durchführen. Weitere Entwicklung ist möglich; Spielbereitschaft/Produktionsreife ist durch diese begrenzte Prüfung nicht belegt. Veraltete Client-EventBus-API-Warnungen bleiben bestehen. Kein Versionsbump auf entwicklung, da dort kein Commit angelegt wurde.

## Abgeschlossener Nachtest

- PASS: erneuter `build` mit elf fehlerfreien JUnit-Tests und korrigiertem Rezept.
- PASS: Dedicated-Neustart derselben separaten Testwelt, `Done (1.892s)`. Alle fuenf Genpaare 9/2, Wachstumsstufe 3/8 und Analyseflag nach Neustart erhalten. Dies testet eine NBT-Fixture, keine Saatgut-Item-Netzwerksynchronisation.
- PASS: Start und erneuter Reload laden je **1292 Rezepte** ohne ERROR/Exception im Nachtestlog; Analyzer-Parsefehler verschwunden. Erster Lauf hatte 1291 Rezepte.
- Server abschliessend mit RCON `stop` ordentlich beendet.
- Zusatzbeleg: `run/restart-results.jsonl`; kompakte, versionierte Evidenz in `docs/validation-evidence/2026-11-16.txt`.
- Nur lokaler Feature-Commit; keine Remote-Branches aktualisiert. Release **nicht** produktiv auf master freigegeben.
