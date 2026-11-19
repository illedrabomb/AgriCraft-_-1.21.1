# Übergabe / Arbeitspaket: Pflanzen anderer Mods (19.11.2026)

> Diese Datei ist ausdrücklich als dauerhafter Arbeitsstand für einen neuen Chat oder eine andere AI gedacht. Keine Annahme, dass alle genannten Integrationen schon laufen.

## Repository und Spielumgebung

- Repository: `illedrabomb/AgriCraft-_-1.21.1`
- Entwicklungsbranch: `integration/neoforge-1211-consolidation` (nicht `main`).
- Modul: `neoforge-port/`; Ziel Minecraft 1.21.1 / NeoForge, Java 21.
- Letzte vom Benutzer gemeldete erfolgreiche lokale Buildphase liegt **vor** den jüngsten Analyzer-/Journal-Codeänderungen. Build und Minecraft-Lauf wurden seither bewusst zurückgestellt.
- Lokales Windows-JDK des Benutzers: `D:\guthub repositorys\Minecraft_mods\Lena\jdk-21.0.12.1+1\bin\java.exe` (JAVA_HOME = Ordner *vor* `bin`).
- Gradle 9.2.1: Wrapper `D:\guthub repositorys\Minecraft_mods\Lena\villagerfamilytree_1.21.1\gradlew.bat`, Wrapper-Properties verweisen auf `gradle-9.2.1-bin.zip`. **Nicht** den AgriCraft-Root-Wrapper benutzen.
- Windows-Checkout des Benutzers: `C:\Users\tinob\Documents\ChatGPT\AgriCraft-Integration-20261116\neoforge-port`. Vor Pull lokale Änderungen prüfen; untracked `Test-Mods/` nicht löschen.
- Spielerentscheidung: **Zuerst größere, zusammenhängende Entwicklungsänderungen fertigstellen; erst danach gemeinsam Gradle/JUnit und Minecraft testen.** Nicht jede kleine Änderung einzeln zum Test ausgeben.

## Verbindliche Priorität

1. Generische sichere Integration für **Datapack-registrierte Mod-Pflanzen**.
2. **Pam's HarvestCraft 2: Crops** ausdrücklich auf NeoForge 1.21.1.
3. **Farmer's Delight** einschließlich Sonderpflanzung von Reis.
4. Integrations-/Regressionstests und JEI-/Journal-Abdeckung.
5. Erst dann gemeinsames manueller Gameplay-Gate; spätere Dorf-Züchterhaus-/Villager-Projekte und optionale Addons getrennt halten.
6. MineColonies später als *separates optionales Addon*. Ponder sehr viel später und optional.

## Quellcode-Audit: Was heute vorhanden ist

- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropGeneticsSeedRecognition.java`: ein Saatgut wird nur erkannt, wenn `plants().plantForSeed(itemId)` erfolgreich und unzweideutig ist. Dies ist **kein generischer Scan aller Items, Pflanzenblöcke und Sonderpflanzungen**.
- `content/CropSticksBlock.java`: Crop Sticks akzeptieren vorab genetische Seeds mit `GENOME`, oder **normale Seeds ohne Data-Component-Patch** mit eindeutiger Registry-Zuordnung. Die extra Prüfung `stack.getComponentsPatch().isEmpty()` blockiert absichtlich angepasste Mod-Stacks, damit keine individuellen Daten verloren gehen. Diese Sperre nicht pauschal entfernen.
- `content/CropItems.java`: `analyzeSeed` akzeptiert basale registrierte Seeds, lehnt fremde Component-Patches ohne bestehendes Genom ab; `seedWithGenome` merkt sich bei eindeutiger Zuordnung die Herkunft als Item-ID, **nicht** die vollständigen individuellen Originalkomponenten. `recoverOriginalSeed` gibt ein normales einzelnes Original-Item ohne Gene aus. Eine vermeintlich verlustfreie Rückkonvertierung von komplexen Mod-Stacks ist damit **nicht implementiert**.
- `data/PlantDefinition.java`: Datapack-Definition enthält `requiredMods`, `seedItems`, `stages`, Harvest- und Bodendaten. Pflanzen-IDs und Seed-IDs werden validiert, Gourd-Spezialmodus ist ausdrücklich nur für Vanilla-Melone/Kürbis freigegeben.
- `content/CropSticksBlock.java`: generisches Pflanzen-/Wachstumsverhalten ist unabhängig von mod-eigenen Spezialblöcken; dabei können Speziallogik, besondere Harvest-Events, Texture-Stages oder Wasserregeln abweichen. **Keine automatische Gleichwertigkeit behaupten.**
- JEI und Journal sind bereits für eigene/genetische Seeds vorhanden; neue Pflanzenarten müssen im aktiven Datapack und bei Darstellung/JEI einzeln nachgewiesen werden.

## Recherche zu externen Mods (Primärquellen)

- Pam's HarvestCraft 2 – Crops, offizielle Projektseite des Autors: https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops . Die Beschreibung nennt 96 Früchte/Gemüse, die aus essbaren Items gepflanzt werden können, sowie Garden Bushes als Bezugsquelle. Verfügbarer NeoForge-1.21.1-Build (Stand Recherche 19.11.2026): `pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar`. Dort ausdrücklich bestätigte Registry-IDs und Kompatibilität fehlen uns noch.
- Quellprojekt: https://github.com/MatrexsVigil/phc2crops . Dort existiert bereits eine Feature-Anfrage zur AgriCraft-Unterstützung auf 1.21.1 (Issue #85). Das ist **keine bestätigte Integration**.
- Farmer's Delight dokumentiert Kohl, Tomaten, Zwiebeln und Reis; Reis hat eine eigene Pflanzmechanik und darf nicht blind wie ein normales Farmland-Saatgut behandelt werden: https://jexxme.github.io/FarmersDelight-Neoforged/mechanics/crops/ . Konkrete 1.21.1-Registry-IDs und Version müssen mit dem jeweiligen offiziellen 1.21.1-Quellstand/Mod-JAR abgeglichen werden; keine Ableitung aus aktueller 26.x-Dokumentation ohne Versionsabgleich.

## Größeres Implementierungspaket: Reihenfolge und Akzeptanzbedingungen

### Paket A – generische Kompatibilität
- Über alle aktiven `PlantDefinition`s eine stabile Zuordnung Seed-Item → exakt eine Pflanzen-ID bilden. Bei Kollision: registrierte Mehrdeutigkeit loggen und Item **nicht** stillschweigend umwandeln.
- Bei Registrierung fehlender Mods: deren Definitionen überspringen, Kernmod vollständig startbar lassen.
- Keine Duplikate beim Einsetzen/Abbauen/Ernten. Kein automatischer Rückschreibverlust bei customized ItemStacks. Für komplexe Originalkomponenten entweder expliziten Adapter samt nachvollziehbarer Komponenten-Kopie entwickeln oder Registrierung als unsupported erklären.
- Datapack-Reload, Save/Reload, Server/Client, Multiplayersynchronisierung und `recoverOriginalSeed` mit entfernter optionaler Mod testen.
- JEI soll Umwandlung **auf einzelnen Crop Sticks** erklären; doppelte Sticks bleiben Kreuzungsfeld.

### Paket B – Pam's HarvestCraft 2
- Eine **konkrete** NeoForge-1.21.1-JAR/Quellversion als Referenz festlegen (aktuell gemeldet 1.0.9). Mod-ID und sämtliche für Pilotkulturen verwendeten Item-/Block-IDs aus *dieser* Version auslesen.
- Mit 3–5 repräsentativen Kulturen beginnen: gewöhnliche Feldfrucht, Gartenstrauch-Quelle, Sonderernte, unterschiedlich hohe Stufen; erst nach erfolgreichem Nachweis die restlichen unterstützten Kulturen abdecken.
- Food-Items als Pflanzgut explizit mappen; nicht jedes essbare Item pauschal als Seed interpretieren.
- Wachstum, Drops, Tooltips, Analyzer/Chest, Journal, JEI und genetische 1/1/1 → 10/10/10-Weiterzüchtung abnehmen.
- Keine Hard-Dependency; bei fehlendem Pam niemals Klassen aus diesem Mod initialisieren.

### Paket C – Farmer's Delight
- Exakte NeoForge-1.21.1-Version und Registry-IDs prüfen. Kohl-/Tomatensamen sowie Zwiebeln getrennt abbilden.
- Reis als **Spezialadapter** behandeln, nicht die normalen Wasser-/Boden- und Harvest-Regeln überschreiben.
- Mod-eigene Ernteprodukte und eventuelle Messer-/Rechtsklick-Spezialernte nicht unbemerkt durch generische Vanilla-Drops ersetzen.

### Paket D – Testsystem
- JUnit: eindeutige Zuordnung, Kollisionen, fehlende optionale Mod, save/load mit `ORIGINAL_SEED`, genetiktreue Mehrfachkreuzung.
- Manuell jeweils ohne und mit der optionalen Mod (Client **und** dedizierter Server), drei Samenarten pro Mod, Save/Reload, JEI und Journal.
- Externe Tester erhalten eine importierbare Datei für den `Minecraft Mod – Test Tracker` mit Zweck, Vorbereitung, Aktion, Soll-Ergebnis und Beweisanforderung je Fall.
- Keine Aussage »kompatibel« allein auf Basis eines erfolgreichen Java-Builds.

## Offene Risiken / Entscheidungen für nachfolgende AI

1. **Wichtigste technische Lücke:** `getComponentsPatch().isEmpty()` verweigert angepasste Mod-Seeds. Das ist zunächst Absicherung, kein per se zu entfernender Bug.
2. Genetische Seeds speichern eine einfache Herkunfts-ID und keine vollständigen Original-Itemdaten. Original-Items aus Food-Crops müssen bei der Konvertierung ohne Verlust besonderer Komponenten behandelt werden.
3. Bei Spezialpflanzen sind Stufen, Rendering, Boden und Harvest-Logik separat abzuklären; zwei gleiche Item-IDs dürfen nie implizit zwei Pflanzen zugeordnet werden.
4. Pam's/Farmer's noch **nicht** als implementiert oder spielgetestet markieren. Diese Übergabe dokumentiert Audit und nächsten sicheren Umsetzungspfad.
5. Das bestehende `docs/NEOFORGE_1.21.1_PLAN.md` ist historisch; frühere Aussage „noch keine Portierung“ wurde am 19.11.2026 auf den realen Branch-Stand korrigiert.

## Handoff: Nächster konkreter Arbeitsschritt

1. Tatsächliche Pam-1.21.1-JAR/Quell-Registry und Farmer's-1.21.1-Quelle prüfen.
2. Aktive Datapack-Pflanzendefinitionen im Branch inventarisieren und bestehenden generischen Adapter gegen die obigen Akzeptanzbedingungen vergleichen.
3. Erst danach korrekte optionale Datapack-Definitionen und spezielle Adapter samt Regressionstests implementieren.
4. Am Ende jeder größeren Änderung in diesem Dokument **Commit-SHA, erledigte Punkte, offene Risiken und noch nicht ausgeführte Tests** ergänzen.
