# Lokale Konsolidierung am 16.11.2026

## Ergebnis und Ausgangspunkte

Die Zusammenführung ist lokal abgeschlossen und technisch geprüft. **Keine produktive Freigabe**, keine Pushes und keine Übernahme nach entwicklung, testing oder master. Bereit für die nächste gezielte Gameplay-/Client-Testphase, nicht für eine Produktionsabnahme.

- Integrationsbranch: `integration/neoforge-1211-consolidation`.
- Ausgang Feature: `d2c5e45cd23d969e717b6f9bb6c92b27648fdd4d`, einschließlich der ungepushten API-, Kreativ-Tab- und Rezeptkorrekturen.
- Eingehender Entwicklungsstand: `ec0666a816116cd6db4f28c92bdf07867634cc83` aus origin/entwicklung; vor Arbeitsbeginn frisch gefetcht.
- Gemeinsame Basis: `cfca4d110b9c0934cf800693481491f1fafa5364`.
- Separater Worktree: `C:/Users/tinob/Documents/ChatGPT/AgriCraft-Integration-20261116`.
- Minecraft **1.21.1**, NeoForge **21.1.265**, Temurin **21.0.12.1+1**, Gradle **9.2.1**, ModDev **2.0.148**.
- Mod-Metadaten **0.2.14-alpha**, aus entwicklung übernommen. Kein Versionsbump auf entwicklung, weil dort kein Commit vorgenommen wurde. Die Versionsnummer bezeichnet hier einen lokalen Integrationsstand, keinen neuen Release.

## Merge-Ansatz und fachliche Konfliktlösungen

Echter Zwei-Eltern-Merge mit `git merge --no-commit --no-ff origin/entwicklung` auf Basis des geprüften Feature-Commits. Neun Dateien hatten Konflikte; jeder Bereich wurde einzeln ausgewertet. Keine pauschale ours/theirs-Übernahme. Bereits automatisch zusammengeführte Änderungen wurden zusätzlich geprüft.

| Konfliktdatei unter neoforge-port | Fachliche Lösung |
|---|---|
| CropGeneticsMod.java | Bodenchemie-Installation, Boden-Reconciliation, SoilCommand und alle drei Reload-Listener aus Feature erhalten. Genau eine Kreativ-Tab-Registrierung über die separate CropCreativeTabs-Klasse aus entwicklung. Die provisorische zweite Tab-Implementierung aus dem Feature entfällt zugunsten dieser lokalisierten, umfassenderen Implementierung. |
| CropConditions.java | Neue Standortregeln für Zuckerrohr, Kaktus und Pilze mit Biomen, Dimensionen, Flüssigkeit/Höhe, Saison, Bodenchemie und Boden-Wachstumsfaktor kombiniert. growthFactor verlangt beide Regelgruppen; keine Gruppe ersetzt die andere. |
| CropItems.java | Gleichwertige BlockItem-Typkorrekturen vereinheitlicht. Automatisch eingehende Ablehnung mehrdeutiger/falsch zugeordneter Saatgutrückgewinnung beibehalten. |
| CropSticksBlock.java | Präzise Block-Codecs; Analyzer-Interaktion und Bodenpflege in getrennten Pfaden; Standort-/Stützblockprüfung, Client-Prognose und geplante Support-Rechecks aus entwicklung erhalten. Frucht-Reconciliation läuft für vorhandene Fruchtmetadaten auch nach Moduswechsel. |
| CropSticksBlockEntity.java | Konservative STANDARD-Erntesperre aus entwicklung übernommen: kein Ertrag bei bestehender Frucht-/Richtungs-/Token-Zuordnung. Feature-Remapping und modusunabhängige Eigentumsprüfung bleiben erhalten. |
| CrossBreeding.java | Feature-canGrow-Prüfung beibehalten, die über das kombinierte growthFactor nun zusätzlich die neuen Standortregeln enthält. Mutationsfilter für Standort, Saison und Chemie bleiben aktiv; ungeeignete Kandidaten verbrauchen keinen Mutationswurf. |
| GeneticGourdFruitBlock.java | Gleichwertige Codec-Korrekturen mit importiertem MapCodec und konkretem Rückgabetyp vereinheitlicht. Eigentums-/Entfernungslogik erhalten. |
| plants/cactus.json | Erweiterbare cactus_soils aus entwicklung statt nur Sand; Vanilla-artig min_light=0, Toleranz=0; Kontaktstacheln aus Feature bleiben aktiv. |
| README.md | Historische Validierung erhalten und aktuellen Integrationsbranch/Konsolidierungsstand vorangestellt. Widersprüchliche Aussage zum Erntemodus korrigiert. |

### Bewusste Entscheidung: Erntemodus bei Melone/Kürbis

entwicklung verbot pauschal den Wechsel dieser Arten zu STANDARD. Feature enthält inzwischen eine umfassendere, tokengebundene Migration für bereits existierende physische Früchte. Das pauschale Verbot würde diese implementierte Funktion entfernen. Deshalb bleibt die Feature-Migration erhalten, kombiniert mit dem strengeren Ernteguard aus entwicklung. Neue physische Früchte benötigen weiterhin GOURD. Existierende Früchte sperren zusätzliche Stamm-Ernte unabhängig vom aktuellen Modus. Nach ihrer Entfernung wird auf die aktuelle Regrowth-Stufe zurückgesetzt.

Der konkrete Konflikt wurde auf dem Dedicated Server mit einem echten geladenen Überschreibungs-Datapack geprüft: vorhandene Melonenfrucht blieb nach Wechsel zu STANDARD zugeordnet; Stamm-Loot enthielt nur Sticks und genetischen Samen, keine Melone. Nach Fruchtentfernung wurde die Zuordnung gelöscht und Stufe 4 hergestellt. Separates Wiederherstellen der Reife erlaubte danach STANDARD-Melonenloot und bestätigt, dass das Test-Datapack tatsächlich wirkte. Das ist ein begrenzter Regressionstest, keine vollständige Loot-/Gameplay-Abnahme.

Unabhängige Schutzregeln aus entwicklung bleiben: mindestens zwei Wachstumsstufen und Pflanzen-IDs höchstens 128 Zeichen.

## Funktionsabgleich

| Herkunft | Übernommene Funktionen / Prüfgrenze |
|---|---|
| Beide Ausgangsstände | Crop Sticks, Double Sticks, Genom/Vererbung, Wachstum, Kreuzungen, Mutationen, Ernte, Saatgut, Analyzer, Datenkomponenten, Melonen/Kürbisse und Diagnosen. Quellpfade erhalten; nicht alle Spielerinteraktionen praktisch abgenommen. |
| Feature | Atomare Pflanzen-/Mutations-Snapshots; Pam-Crops-Autodiscovery und Konfiguration; explizite Pam-Pflanzen/-Mutationen und Migrationstools; Saison-Schnittstellen, Biome/Dimensionen/Höhen/Flüssigkeiten; Bodenprofile, Chemie, Dynamik/Pflege und gespeicherte Overrides; genaue Stufen-/Reload-Migration; Fruchteigentum, Entkopplung und Piston-Schutz. |
| entwicklung | Bambus, Süßbeeren, braune/rote Pilze mitsamt Daten/Visuals; erweiterbare Bodentags und Vanilla-Standortregeln; Stützblock-Rechecks; Client-Pflanzprognose; maintains_farmland-Tag; Analyse gepflanzter Kulturen; eindeutige Seed-Recovery; eigener lokalisierter Kreativ-Tab mit 16 Startgenomen; Attached-Stem-Modelle/Färbung und Tooltip-Hinweis; zusätzliche Datengrenzen. |
| Lokales d2c5e45 | Drei Compilerkorrekturen und korrigiertes Analyzer-Rezept bleiben wirksam. Der eigene Tab wird durch genau eine umfassendere Implementierung bereitgestellt. |

Dateibaumvergleich gegen beide Eltern: keine Dateien entfernt. Methoden-/Diff-Sichtung ergab keine versehentlich entfernten Funktionspfade. Absichtliche Ersetzungen: provisorischer Tab zugunsten CropCreativeTabs, engere Kaktus-Bodenangabe zugunsten Tag, pauschales Modusverbot zugunsten geprüfter Eigentums-Reconciliation. Dies belegt den Quellabgleich, keine vollständige funktionale Gleichwertigkeit im Client.

## Validierung und Ergebnisse

Neue separate Welt `neoforge-port/run/validation-server/integration-test-world`, Seed 16112026; localhost:25587/RCON:25588. Keine alten Welten oder Testdaten geändert. Testserver anschließend mit `stop` sauber beendet. Nur das NeoForge-Unterprojekt gebaut; alte Forge-Codebasis unverändert.

| Prüfung | Schritte / Ergebnis |
|---|---|
| Vollständiger Build | **PASS**: Java 21 explizit, Gradle build; erster vollständiger Integrationsbuild erfolgreich. |
| Alle Unit-Tests | **PASS**: 16 Tests, 0 failures/errors. Genetics 4, Parser 5, Pam-Settings 4, CropGrowth-Regressions 3. |
| Neue Regressionen | **PASS**: Stufenremapping 8↔16 und Endpunkte, Legacy-/Grenzwerte, feinere Altersstufen innerhalb eines BlockState, Einstufen-Ernteexploit und zu lange Pflanzen-ID. Die BlockState-Rechnung ist kein Client-Sichttest. |
| Dedicated Server | **PASS**: Erststart `Done (8.976s)`, Neustart `Done (1.888s)`; keine Client-Classloading-, Registry- oder Lade-Exceptions. |
| Pflanzen/Mutationen | **PASS**: 16 Pflanzen, 1 Mutation, 87 Ressourcen; vier neue Arten zusätzlich zum Feature. 70 MISSING_MOD-Diagnosen betreffen absichtlich nicht installiertes optionales Pam. |
| Analyzer-Rezept | **PASS**: Start und Reload laden 1292 Rezepte ohne Analyzer-Parsefehler. Herstellung/Bedienung nicht gespielt. |
| Adminbefehle | **PASS**: data, data pam, data errors, data all, data tags; soil show/set/clear. Ausgabe der gebundenen Bodentags und gespeicherter Bodenwerte geprüft. Nur privilegierte RCON-Ausführung, keine Berechtigungsabnahme mit Nicht-Operator-Spieler. |
| Genetik-Persistenz | **PASS**: NBT-Fixture mit allen fünf Genpaaren 9/2, Stufe 3/8 und Analyseflag; save-all/stop/Neustart; Werte unverändert. Bodenoverride damp/neutral/high ebenfalls erhalten. Kein Seed-Item-Netzwerktest. |
| Einfacher Datapack-Reload | **PASS**: bestehendes Genom und unveränderter Katalog erhalten; keine Rezept-/Ladefehler. |
| Widersprüchliche Pflanzen | **PASS**: zweite explizite wheat_plant-Definition blockiert beide Claims und abhängige Mutation; konsistenter Katalog 15 Pflanzen/0 Mutationen. Geladener Weizen bleibt mit Genom dormant. Nach Entfernen des Konflikts 16/1 wiederhergestellt. |
| Geänderte Stufenzahl | **PASS**: im tickenden geladenen Chunk, randomTickSpeed=0, 8→16 remappt Stufe3→6, zurück 16→8 auf3; Genpaare bleiben erhalten. |
| Melonen-Moduswechsel | **PASS, begrenzte Fixture**: aktive STANDARD-Override-Datei, Fruchteigentum und Loot-Sperre, Entfernung/Regrowth und anschließender STANDARD-Loot wie oben. Keine komplette Duplikations-/Fortune-/Silk-Touch-Abnahme. |
| Client/Rendering/Kreativmenü | **Nicht durchgeführt**. Quellcode/Modelle und Client-Trennung geprüft, aber keine tatsächliche Darstellung/Bedienung abgenommen. |
| Vollständiges Gameplay/Lasttest | **Nicht durchgeführt**: Spieler-Pflanzung, Kreuzung/Mutation, Wachstum aller Arten, Analyzer-UI, Umweltsimulation und große Farmen benötigen nächste Testphase. |
| Echte Pam-Crops-Integration | **Nicht durchgeführt**: Code und explizite Ressourcen erhalten, Settings-Regressionen bestehen; realer Pam-Mod nicht installiert. /data pam nur ohne Pam geprüft. Trees ausgeschlossen. |

### Testablaufkorrekturen

Zwei ursprüngliche Harness-Annahmen wurden korrigiert, ohne den Mod zu ändern: soil show gibt Kategorien als Text statt `humidity=` aus; nach Neustart war der Fixture-Chunk nicht für Force-Loading markiert. Der erste Stufenwechselversuch ohne Tick-Ticket wurde deshalb nicht als Erfolg gewertet. Nach explizitem Force-Loading wurde der komplette Konflikt-/Wiederherstellungs-/Stufenzyklus erfolgreich wiederholt. Rohprotokolle und erfolgreiche Wiederholung sind getrennt erhalten.

## Belege und Reproduktion

Versionierte Belege unter `docs/validation-evidence/`: integration-2026-11-16.json (RCON), integration-restart-2026-11-16.json (Neustart und erster Remappingversuch), integration-reload-retest-2026-11-16.json (erfolgreiche Wiederholung) und integration-2026-11-16.txt (Build-/Server-/JUnit-Auszüge).

Lokale vollständige Logs im Port: integration-build.log, integration-server.log, integration-restart.log; JUnit-XML in build/test-results/test. Fixture-Skripte und generierte Datapacks ausschließlich im neuen run-Verzeichnis. Dort bleibt die Testwelt als Beleg erhalten.

Build reproduzieren: im Verzeichnis neoforge-port JAVA_HOME auf den vorhandenen Temurin-21-Pfad setzen und Gradle 9.2.1 mit `--no-daemon --console=plain build` aufrufen. Dedicated-Lauf: `runServer`, separate Testverzeichnisse sind im Build konfiguriert. Niemals den alten Root-Wrapper verwenden. Weltprüfungen verändern ausschließlich die beschriebene neue Fixture-Welt.

Getestetes Artefakt: `build/libs/cropgenetics-0.2.14-alpha.jar`, SHA256 `d4391ffca0693ba30437e377c2926c6eb80bb9869250d11617415da5ef2c0199`.

## Restliche Risiken und nächste Phase

Kein offener Compiler-/Serverstartfehler aus diesem Durchlauf. Veraltete EventBus-API-Warnungen und Entwicklungsumgebungswarnungen zu PerfOS/union-Ressourcen bleiben; sie führten nicht zum Abbruch. Rendering, kreative Inventarinhalte und echte Spielerabläufe sind nicht runtime-validiert. Die begrenzte Melonenprüfung ersetzt keine Prüfung aller Chunkgrenzen, Explosionen, Fremd-Lootmodifikationen und Früchte. Optionale Saison-/Mod-Provider und Pam müssen mit tatsächlich geladenen Mods getestet werden.

Nächster Schritt: gezielte Client-/Gameplay-Testphase auf genau diesem Integrationsstand. MineColonies-Addon wurde nicht implementiert. Keine Änderung an entwicklung/testing/master, keine Actions, kein Release und kein Push. Integrationscommit wird im abschließenden Commitnachweis dieses Berichts festgehalten; ein nachfolgender Dokumentationscommit ändert keine getesteten Mod-Dateien.

## Commitnachweis

Lokaler Integrations-/Mergecommit mit den getesteten Mod-Dateien: **`5790dae5295c9117420d4b682eb6a592d3154712`**. Eltern: `d2c5e45cd23d969e717b6f9bb6c92b27648fdd4d` und `ec0666a816116cd6db4f28c92bdf07867634cc83`. Der unmittelbar folgende reine Dokumentationscommit traegt diesen nachtraeglich ermittelten Commitnachweis ein; er aendert keine Mod-Dateien und ist der abschliessende lokale Branch-HEAD. Seine ID ist mit `git log -1 --format=%H integration/neoforge-1211-consolidation` abrufbar und wird im Abschlussbericht genannt.
