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

- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropGeneticsSeedRecognition.java`: ein Saatgut wird nur erkannt, wenn `plants().plantForSeed(itemId)` erfolgreich und unzweideutig ist. Dies ist **kein generischer Scan aller Items, Pflanzenblöcke und Sonderpflanzungen**. Die Zuordnung selbst wird in `AgriPlantsRegistry.replaceAll` bereits kollisionssicher erzeugt.
- `content/CropSticksBlock.java`: Crop Sticks akzeptieren vorab genetische Seeds mit `GENOME`, oder **normale Seeds ohne Data-Component-Patch** mit eindeutiger Registry-Zuordnung. Die extra Prüfung `stack.getComponentsPatch().isEmpty()` blockiert absichtlich angepasste Mod-Stacks, damit keine individuellen Daten verloren gehen. Diese Sperre nicht pauschal entfernen.
- `content/CropItems.java`: `analyzeSeed` akzeptiert basale registrierte Seeds, lehnt fremde Component-Patches ohne bestehendes Genom ab; `seedWithGenome` merkt sich bei eindeutiger Zuordnung die Herkunft als Item-ID, **nicht** die vollständigen individuellen Originalkomponenten. `recoverOriginalSeed` gibt ein normales einzelnes Original-Item ohne Gene aus. Eine vermeintlich verlustfreie Rückkonvertierung von komplexen Mod-Stacks ist damit **nicht implementiert**.
- `data/PlantDefinition.java`: Datapack-Definition enthält `requiredMods`, `seedItems`, `stages`, Harvest- und Bodendaten. Pflanzen-IDs und Seed-IDs werden validiert, Gourd-Spezialmodus ist ausdrücklich nur für Vanilla-Melone/Kürbis freigegeben.
- `content/CropSticksBlock.java`: generisches Pflanzen-/Wachstumsverhalten ist unabhängig von mod-eigenen Spezialblöcken; dabei können Speziallogik, besondere Harvest-Events, Texture-Stages oder Wasserregeln abweichen. **Keine automatische Gleichwertigkeit behaupten.**
- **Korrektur nach tieferem Audit:** `data/CropDataReloadListener.java` enthält bereits eine **optionale Pam's-Autoerkennung** (`PamCropAutoDiscovery` → `RegisteredSeedCropDiscovery`), aktiv nur wenn `pamhc2crops` geladen ist. Kandidaten werden über registrierte `seeditem`/`item`-Paare oder geprüfte manuelle `item_pairs` bestimmt. Explizite Species-/Seed-/Produce-Claims haben Vorrang; Konflikte, fehlende Items und Validierungsfehler werden protokolliert. Eine `pam_auto_discovery.json`-Policy begrenzt die Anzahl/Regeln. Das ist **implementierter Code, aber keine nachgewiesene Kompatibilität mit der konkreten Pam-1.21.1-JAR**.
- `data/AgriPlantsRegistry.java` veröffentlicht ein konsistentes Snapshot inklusive ambiger Seed-IDs; Mehrdeutigkeiten werden ausgeschlossen.
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
- Die schon vorhandene Pam-Autoerkennung zuerst mit einer **konkreten** NeoForge-1.21.1-JAR/Quellversion prüfen (aktuell gemeldet 1.0.9). Mod-ID, `seeditem`/`item`-Namenskonvention, Policy und sämtliche Registry-IDs aus *dieser* Version abgleichen. Autoerkennung ggf. korrigieren, nicht neu erfinden.
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
4. **Pam's Auto-Discovery ist bereits im Code vorhanden;** ihre tatsächliche Abdeckung ist ungetestet. **Farmer's Delight** ist noch nicht als eigenständige Integration bestätigt. Nie beide pauschal als »fertig« markieren.
5. Das bestehende `docs/NEOFORGE_1.21.1_PLAN.md` ist historisch; frühere Aussage „noch keine Portierung“ wurde am 19.11.2026 auf den realen Branch-Stand korrigiert.

## Handoff: Nächster konkreter Arbeitsschritt

1. Tatsächliche Pam-1.21.1-JAR/Quell-Registry und Farmer's-1.21.1-Quelle prüfen.
2. Aktive Datapack-Pflanzendefinitionen im Branch inventarisieren und bestehenden generischen Adapter gegen die obigen Akzeptanzbedingungen vergleichen.
3. Erst danach korrekte optionale Datapack-Definitionen und spezielle Adapter samt Regressionstests implementieren.
4. Am Ende jeder größeren Änderung in diesem Dokument **Commit-SHA, erledigte Punkte, offene Risiken und noch nicht ausgeführte Tests** ergänzen.

## Änderungsprotokoll 19.11.2026

- `ae7def8c`: Projektplan um verbindliche Pakete allgemeine Kompatibilität, Pam's und Farmer's Delight erweitert; veraltete Plan-Statuszeile berichtigt.
- `4be6102b`: dieses dauerhafte Quellcode-Audit mit Primärquellen, Versionsgrenzen, Risiken, Akzeptanzkriterien und Chat-/AI-Handoff erstellt.
- **Implementierungsstand dieser Runde:** bestehenden Pam-Adapter im Code wiedergefunden und als bereits vorhanden dokumentiert. Es wurde in dieser Runde **kein neuer Pam-/Farmer's-Adapter-Code erstellt**, **keine** Testausführung. Diese Trennung bei Folgeberichten unbedingt beibehalten.

### Ergänztes Audit: bestehende Pam-Autoerkennung (19.11.2026)

Die frühe Beschreibung, Pam's sei ausschließlich geplant, war unvollständig. Der Branch besitzt schon:

- `data/PamCropAutoDiscovery.java`: zugelassener Namespace `pamhc2crops`, Muster `<crop>seeditem` / `<crop>item`, generische Fallback-Pflanzendefinition.
- `data/PamAutoDiscoverySettings.java`: standardmäßig aktiviert, mit Limits, Ausschlüssen und verifizierbaren benutzerdefinierten `item_pairs`; Konfiguration aus `cropgenetics/cropgenetics/pam_auto_discovery.json`.
- `data/RegisteredSeedCropDiscovery.java`: stabile Kandidatenauswahl, Vermeidung fremder Mods und mehrdeutiger Seed-/Produce-Rollen, priorisierte explizite Datapacks, Diagnosen.
- `data/CropDataReloadListener.java`: registriert Pam nur bei geladenem Mod und aktiver Policy, verhindert Autoerkennung bei fehlerhaften expliziten Pflanzenclaims.
- `data/AgriPlantsRegistry.java`: eliminiert Seed-Zuordnungskollisionen, ohne stillschweigend einen Gewinner zu wählen.

**Wesentliche Validierungslücke:** Ob die zugrunde gelegte `seeditem`/`item`-Benennung noch zu Pam's 1.21.1 passt, ist ungeprüft. Autoentdeckte Pflanzen erhalten generische Acker-/Erntebedingungen und sind keine originalgetreuen Spezialcrop-Adapter. Farmer's Delight benötigt wegen eigener Samen und Reisregeln einen gesonderten Ansatz.

## Farmer's Delight Pilot – Code ergänzt am 19.11.2026

- `be0becca`: `data/FarmersDelightCropDiscovery.java` neu. Verwendet den bestehenden, validierenden `RegisteredSeedCropDiscovery` mit **ausschließlich** zwei expliziten Paaren: `farmersdelight:cabbage_seeds` → `farmersdelight:cabbage` und `farmersdelight:tomato_seeds` → `farmersdelight:tomato`.
- `52bd5c7d`: `data/CropDataReloadListener.java` aktiviert den Pilot **nur bei geladener Mod `farmersdelight`**, sammelt auch Claims deaktivierter expliziter Datapacks und sperrt die automatische Ergänzung bei fehlerhaften/unlesbaren Claims. Die beiden Pflanzen erhalten **generische** Farmland-/Harvest-Bedingungen; Sonderlogik der Originalmod wird nicht kopiert.
- **Nicht implementiert:** Zwiebeln, Reis (Sonderpflanzung), originale Farmer's-Delight-Spezialernte und umfassende grafische Darstellung. Die beiden Pilotpflanzen sind noch **nicht** durch Java-21-Build, JUnit oder Minecraft 1.21.1 verifiziert. Vor einer Freigabe muss die tatsächliche Item-Registry der installierten Version geprüft werden.
- **Pam's:** vorhandener Auto-Adapter bleibt aktiv und unverändert; diese Runde hat dessen zuvor übersehenen Code nur auditiert. Auch er ist nicht gegen die aktuelle Pam-JAR verifiziert.
- **Nächster Arbeitsschritt:** Datapack-/Java-Validierung und JUnit für explizite Claim-Kollisionen, fehlende Mod, Datenreload; dann gezielte Modpack-Spieltests.

## Regressionstest-Paket – 19.11.2026 (noch NICHT ausgeführt)

- `39b01c1c`: Farmer's-Delight-Pilotpolicy in `FarmersDelightCropDiscovery.policy()` für reine Konfigurationstests isoliert; Produktionsverhalten unverändert.
- `65eccf4f`: neuer JUnit-Test `FarmersDelightCropDiscoveryTest` prüft exakt zugelassene Kohl-/Tomaten-Paare, Ausschluss von Reis/Zwiebeln, Fremd-Namespace und identische Seed-/Produce-Rollen.
- `c7583ff6`: neuer JUnit-Test `AgriPlantsRegistrySeedMappingTest` prüft beide Konfliktfälle: doppelt beanspruchter Seed wird für **beide** Pflanzen gesperrt; disjunkte Seed-IDs bleiben zuordenbar.
- **Nicht getestet:** `test`, `build`, Data-Reload mit echter Farmer's-/Pam's-JAR, Menü-/Welt-/Multiplayerfunktionen. Die Tests sind als Quellcode vorhanden; ihre Ausführung ist ein Pflichtpunkt des nächsten Gesamt-Builds.
- **Weitere Tests offen:** tatsächliche Farmer's-Registrierungswerte, Vorrang deaktivierter Datapack-Claims im ReloadListener, fehlende optional installierte Mods und Reis-Spezialadapter.

## Farmer's Delight Zwiebel-Pilot – 19.11.2026

- `8c9e0d9`: `FarmersDelightCropDiscovery` unterstützt jetzt neben Kohl und Tomate **Zwiebeln** mit demselben Item `farmersdelight:onion` als Saatgut und Ernteprodukt. Die generische „zwei verschiedene Items“-Erkennung bleibt bewusst streng; Zwiebel ist eine ausdrücklich zugelassene Ausnahme.
- Bei explizit beanspruchter Pflanzen-ID, Seed-ID oder Produkt-ID unterbleibt die Ergänzung. Die Item-ID muss im tatsächlichen Minecraft-Registry existieren; fremde Mods sind keine Pflichtabhängigkeit.
- `07e01979`: reine JUnit-Fälle prüfen, dass ein Zwiebel-Fallback nur ohne konkurrierende explizite Ansprüche erlaubt ist.
- **Risiko:** Dies ist eine **generische** Pflanze auf Farmland, nicht zwingend identisch zur vollständigen originalen Farmer's-Delight-Wachstums-/Erntelogik. Build, Weltstart, Rendering, tatsächliche Item-Registry und Spielverhalten noch **NICHT getestet**.
- **Reis bleibt bewusst offen:** benötigt Wasser-/zweiteiliges Pflanzverhalten und besondere Ernteprodukte; nicht auf gewöhnlichen Farmland-Crop reduzieren.

## Pam's 1.21.1 Namenskonvention und Reis-Sonderfall – 19.11.2026

**Verifizierbare Primärquellen:**
- Pam's offizielles Quellprojekt, grass loot: https://github.com/MatrexsVigil/phc2crops/blob/master/src/main/resources/data/pamhc2crops/loot_tables/blocks/grass_drops.json (enthält z.B. `pamhc2crops:mustardseedsseeditem`).
- Pam's offizielles Garden-Loot: https://github.com/MatrexsVigil/phc2crops/blob/master/src/main/resources/data/pamhc2crops/loot_tables/blocks/windygarden.json (`pamhc2crops:amaranthitem`, `pamhc2crops:mustardseedsitem`).
- Farmer's Delight originaler 1.21-Quellstand, Loot-Definitionen: https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/data/loot/FDBlockLoot.java . Ernte von `RICE_CROP` unterscheidet sich von `RICE_CROP_PANICLES`; reife Rispen liefern abhängig von Messer/Alter Reis oder Rispen. Onion verwendet eine eigene seedless crop loot definition.

**Quellcode und Regressionstests:**
- `3fe1b6e2`: bestehende Pam-Suffixerkennung in `RegisteredSeedCropDiscovery.inferredCropName` als reine, testbare Funktion isoliert. Verhalten unverändert; fremde Namespaces bleiben ausgeschlossen.
- `b35431cd`: `RegisteredSeedCropDiscoveryNamingTest`: `amaranthseeditem` → `amaranth`, `mustardseedsseeditem` → `mustardseeds`, fremde Tree-Namespace und Produce-Items werden abgewiesen. Noch nicht ausgeführt.

**Reis: konkrete nächste Architekturentscheidung:**
1. `HarvestMode` unterstützt bislang `STANDARD` und `GOURD`; ein Standard-Farmland-Adapter kann Reis nicht originalgetreu darstellen.
2. Quellversion und tatsächliche Block-/Item-Registries prüfen: unterer wassergepflanzter Stängel, obere Reisrispen mit separatem Reifegrad und Drops, Messer-/Normalernte, Wiederwuchs.
3. Keine automatisch erfundene `farmersdelight:rice_plant`-Definition und keinen Fall-back auf Farmland registrieren.
4. Prüfen, ob ein eigener Rice-Harvest-Adapter mit zweitem Blockteil innerhalb unserer Crop-Sticks möglich ist, ohne fremde Farmer's-Delight-Blockklassen zur Pflichtabhängigkeit zu machen. Erst dann implementieren und separat testen.
5. Gemeinsam testen: Anpflanzen mit/ohne Wasser, volle Reife, Messerernte, normale Ernte, wiederholte Ernten, Save/Reload, dedizierter Server, entfernte Farmer's-Delight-Mod.

**Status:** Die offiziellen Quellen stützen die generelle Namenskonvention, ersetzen aber nicht den zwingenden Abgleich gegen die tatsächliche NeoForge-1.21.1-Pam-JAR. Die Tests liegen im Git, wurden wegen der vereinbarten Testreihenfolge noch nicht ausgeführt.

## Inventarsicherheit: Genom ist kein Saatgut-Nachweis (19.11.2026)

- `6ff7c9ba`: `SeedAnalyzerBlockEntity.canPlaceItem` akzeptiert serverseitig nun `CropItems.isGeneticSeedStack(stack)` statt nur `hasGenome(stack)`.
- `60c9b3ef`: dieselbe Prüfung in `SeedChestBlockEntity.canPlaceItem`.
- Warum: Ein fremdes Item mit manipuliertem/verwaistem `GENOME`-Datenkomponent ist nicht automatisch Saatgut. Für einen gewöhnlichen Mod-Seed muss die aktive, eindeutige `plantForSeed`-Zuordnung passen; `cropgenetics:genetic_seed` darf seinen Genomwert behalten, auch wenn die optionale Ursprungsmod später fehlt.
- **Client bleibt prädiktiv permissiv:** Er kennt die vollständige serverseitige Pflanzenliste nicht; die definitive Entscheidung fällt im Servercontainer.
- **Nächste Tests:** (a) echtes genetisches Saatgut; (b) unmodifiziertes und analysiertes Vanilla-/Pam-/FD-Saatgut; (c) Nicht-Saatgut mit künstlichem Genom; (d) ordinary seed mit absichtlich falscher Pflanzen-ID; (e) gespeicherter genetischer Seed nach Entfernen des optionalen Mod-Adapters. Alle ohne Itemverlust oder unerlaubte Containeraufnahme.
- Nicht kompiliert oder im Spiel ausgeführt; Verhalten des Menü-Slotfilters bleibt zu prüfen.

## Mod-Entfernung / Datapack-Abhängigkeiten – 19.11.2026

- `9ec758e2`: `CropModDependencyRules` als reine, testbare Funktion für das `mods`-Feld von Pflanzen-/Mutationsdefinitionen eingeführt; `agricraft` bleibt bewusst akzeptierter Alt-Fork-Alias.
- `3cf0af9e`: `CropDataReloadListener.missingMod` nutzt diese Funktion, statt eigene Regeln zu duplizieren. **Kein beabsichtigter Verhaltenswechsel** beim Reload.
- `09d1b18e`: JUnit-Fälle für fehlendes Farmer's Delight, fehlendes Pam's, geladene optionale Mods, AgriCraft-Alt-Alias, Definition ohne `mods` und fehlerhaftes `mods`-Feld.
- Diese Tests wurden ausdrücklich **nicht** ausgeführt; abgelehnte Definitionen müssen im späteren Spieltest über `/reload` und Diagnosebericht kontrolliert werden.
- Bei Mod-Entfernung bereits gespeicherte genetische Seeds/Weltpflanzen nicht stillschweigend löschen. Bestehende Fallback- und Dormanzregeln bleiben separat zu testen.

## Mod entfernen und wieder installieren – Dormanz/Save-Fix (19.11.2026)

**Quell-Audit vor Änderung:**
- `CropSticksBlockEntity` persistiert `Genome`, originale Seed-ID, Analyseflag, exakte Wachstumsstufe und Stufenzahl ohne Zugriff auf den aktiven Pflanzenkatalog. Bei fehlender Pflanzendefinition überspringen `onLoad`, Server-Tick und Random-Tick das Wachstum, statt Gene zu löschen.
- `CropSticksBlock.getDrops` gibt beim Abbau eines bepflanzten Crop-Stick-Blocks den Gen-Seed über `seedDrop()` zurück, auch wenn die Pflanzenart momentan nicht registriert ist. Ohne aktive Definition werden keine erfundenen Ernterohstoffe erzeugt.
- **Gefundene Lücke:** `CropSticksBlock.canSurvive` prüfte bei abwesender Art alle *anderen* aktiven Bodenregeln. Eine auf besonderem Mod-Boden gespeicherte inaktive Pflanze konnte dadurch bei Blockupdates fälschlich als ungültig gelten.

**Änderungen:**
- `fdeea1b7`: `UnavailableModGenomePersistenceTest` (noch nicht ausgeführt): neue dreiwertige Genetik mit unbekannter Mod-ID codec-roundtrip, Legacy-Genom-Migration ohne Pflanzenregistrierung, Ablehnung ungültiger Werte.
- `c960a7c4`: `CropSticksBlock.canSurvive` erhält eine bereits eingepflanzte Art mit vorhandenen Genome-Daten, deren Definition fehlt, solange die physische Bodenunterlage nicht Luft ist. Keine Sonderböden für **neue** Pflanzungen dadurch freigeben. Periodischer Tick kann weiterhin physisch fehlende Stütze brechen.
- **Noch nicht getestet:** Save/Reload bestehender Welt, Deinstallation und Reinstallation einer Pam's-/Farmer's-Delight-JAR, /reload, zufällige Ticks, Boden-Nachbaränderungen, Genwerte nach erneuter Registrierung und Entity-/Item-Deserialisierung in dediziertem Server. Für Test immer Welt-Backup vor Mod-Entfernung erstellen.

## Sichere Herkunft genetischer Seeds – 19.11.2026

- `47e9ec40`: `CropItems.seedWithGenome(genome, sourceSeed)` übernimmt beim normalen, nicht-genetischen Ausgangsseed die `ORIGINAL_SEED`-ID **nur noch**, wenn der serverseitige, eindeutige `plantForSeed`-Index die Quelle genau der im Genom angegebenen Art zuordnet. Frühere Logik speicherte die ID jedes beliebigen Nicht-Luft-Items, was bei fehlerhaften Drittmod-Integrationen z.B. ein Weizen-Genom als Karotten-Ursprung markieren konnte.
- Bei bereits genetischem Source-Seed bleiben die bisherigen strengen Genome-Vergleiche und die vorher gespeicherte Herkunft unverändert. Bei unbekanntem Item wird kein neuer falscher Ursprung erfunden; bestehende rechtmäßig gespeicherte Herkunft bleibt bei Save/Reload erhalten.
- **Explizit nicht geändert:** die Wiederherstellung bereits gespeicherter `ORIGINAL_SEED`-IDs ohne aktiven Adapter. Diese ist nötig, um ursprüngliche Seeds bei vorübergehend deaktivierter Mod nicht vollständig unrettbar zu machen; ein allgemeiner Schutz gegen absichtlich gefälschte Data Components wäre ein separates Sicherheitskonzept.
- **Testfälle beim gemeinsamen Build:** Weizen-Genom + Weizen-Seed = gültige Herkunft; Weizen-Genom + Karotten-Seed = keine Karotten-Herkunft; fremder oder mehrdeutiger Mod-Seed = keine neue Herkunft; bereits genetischer Source-Seed mit identischem/abweichendem Genome; ursprüngliche Herkunft nach Mod-/Datapack-Entfernung.
- Noch **nicht** gebaut oder ausgeführt.


## Reload-Diagnose und Testabdeckung – 19.11.2026

- `19f3cf8c`: `CropReloadReport.farmersDelightStats()` zählt Farmer's-Delight-Pilotarten getrennt von Pam's: gefundene Arten, Namens-/Claim-Konflikte, fehlende registrierte Items, sonstige Warnungen und Sperrmarker. Herkunft: virtuelle Einträge `cropgenetics:auto_farmers_delight/...` aus dem letzten Server-Datapack-Reload.
- Zusätzlich implementiert in `command/CropDataCommand.java`: `/cropgenetics data farmersdelight` (Berechtigungsstufe 2), inklusive Status und Geltungsbereich **Kohl, Tomate und Zwiebel**; Reis ausdrücklich noch nicht unterstützt. `/cropgenetics data summary` nennt Farmer's-Delight- und Pam's-Anzahlen gesondert. Fehlerdetails: `/cropgenetics data errors [page]`; vollständig `/cropgenetics data all [page]`.
- `d3a0dec2`: `FarmersDelightDiagnosticsTest` prüft getrennte Zählung gegenüber Pam's, Konflikte, fehlende Items, die Discovery-Sperre und leere Resultate ohne optionale Mod.
- `33557f62`: Sperrmarker wird nicht zusätzlich als allgemeine Warnung gezählt; `suspended` meldet ihn separat.
- **Wichtig: Die Tests sind als Quellcode angelegt, aber noch nicht ausgeführt.** Auch neue Befehle, tatsächliche optional-mod-Registrierungen, Data-Reload, JEI/Journal, Pflanzenwachstum und Spielerinteraktionen sind noch nicht in Minecraft bestätigt. Die letzte erfolgreich gemeldete Benutzer-Buildphase datiert vor diesem großen Integrationspaket.
- Beim gemeinsamen Test nach Abschluss sämtlicher Änderungen: einmal ohne optionale Mods starten (keine Crashs, beide Adapter-Zähler null), danach mit Pam's und Farmer's Delight separat und gemeinsam; Reload-Berichte auf Plausibilität prüfen; gegebenenfalls die tatsächlichen Items der 1.21.1-JARs nachführen. Keine Kompatibilitätsfreigabe allein aufgrund eines JUnit-Erfolgs.

### Unveränderte nächste Baustellen

1. Reale Pam's-1.21.1-JAR-/Registry-Verifikation (`seeditem`/`item`-Muster) und Farmer's-Delight-1.21.1-Registryabgleich.
2. Reis-Spezialpflanzung von Farmer's Delight (nicht über generische Farmland-Regeln vortäuschen).
3. Sonderernte, Drops, Wachstum, Duplikationsschutz und vollständige Original-Stack-Komponenten für Seeds mit fremden Data-Component-Patches.
4. Gemeinsames Build-/JUnit-/Minecraft-Regressionstestfenster **erst nach dem vereinbarten gesamten Änderungspaket**; externe Tester-Datei für den Minecraft Mod – Test Tracker danach aktualisieren.


## Weiteres Diagnostikpaket – 19.11.2026

- `60976bab`: Fehlende Registrierung von `farmersdelight:onion` wird nun als `AUTO_MISSING_ITEM` erfasst; ein nicht validierbarer Onion-Fallback erhält `WARNING`. Ein absichtlich belegter Item-/Species-Claim bleibt dagegen ohne künstliche Fehlermeldung ausgeschlossen (Datapack-Priorität).
- `27de4e77`: `CropDataReloadListener` schreibt die Farmer's-Delight-Zahlen bei jedem Server-Datapack-Reload zusätzlich in das Serverlog, parallel zur bereits vorhandenen Pam's-Zusammenfassung. So werden die Fälle auch ohne Command-Eingabe nachvollziehbar.
- Diagnosen sind ausschließlich lesend; die neue Meldung schaltet keine zusätzliche Pflanzenart frei. Das System bleibt optional, ohne Link-Abhängigkeit auf Farmer's-Delight-Klassen.
- **Noch ungetestet** (kein Gradle/JUnit, kein Minecraft-Lauf seit diesen Änderungen); der Nutzer möchte den späteren Sammeltest nach allen Änderungen. Farmer's Delight Reis bleibt weiterhin bewusst ausgeschlossen und benötigt einen eigenen Spezialadapter mit verifiziertem 1.21.1-Verhalten.


## Quellabgleich Seed-IDs – 19.11.2026

### Pam's HarvestCraft 2 Crops

- Öffentliches Autoren-Repository, Seed-Loot-Tabelle: https://github.com/MatrexsVigil/phc2crops/blob/master/src/main/resources/data/pamhc2crops/loot_tables/blocks/grass_drops.json
- Dort sind u. a. `pamhc2crops:tomatoseeditem`, `pamhc2crops:riceseeditem`, `pamhc2crops:mustardseedsseeditem` und `pamhc2crops:sesameseedsseeditem` belegt. Der Namensparser muss bei wiederholtem `seeds` die ganze Basis (`mustardseeds`, `sesameseeds`) erhalten.
- `47601bb6`: JUnit `PamSeedNamingContractTest` mit Normalfällen, wiederholtem Seeds-Wort und fremdem Namespace ergänzt.
- **Versionsgrenze:** Die eingesehene Autorenquelle ist `master` und belegt für sich allein **nicht**, dass die NeoForge-1.21.1-JAR dieselben Item-IDs oder sämtliche passenden `<crop>item`-Produkte enthält. Die vorhandene Autoerkennung prüft die tatsächlich geladenen Item-Registrierungen zur Laufzeit und überspringt fehlende Paare. Vor einer Freigabe weiterhin die konkrete JAR überprüfen.

### Farmer's Delight

- Offizielles Quellprojekt `1.21` und seine Block-Drops: https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/data/loot/FDBlockLoot.java
- Der Quellstand nennt `CABBAGE_SEEDS` für Kohl, `TOMATO_SEEDS` für Tomaten, `ONION` als Produkt der samenlosen Zwiebelernte sowie `RICE` / `RICE_PANICLE` mit werkzeugabhängigen Drops. Die Pilot-Zuordnungen passen zu den Quellnamen; die originale Erntemechanik wird **nicht** von unserem generischen Crop-Sticks-Pilot nachgebildet.
- **Reis-Sonderfall:** `rice` wird nicht einfach als Standardackerpflanze autoentdeckt; originale Wasserpflanzung und die Knife-/Panicle-Ernte bleiben ein separates Arbeitspaket. Auch das `1.21`-Branch-Material ist noch kein beweiskräftiger Test gegen genau die verwendete NeoForge-1.21.1-JAR.

### Akzeptanz für den späteren Integrationslauf

1. Exakte Dateinamen/Checksums der installierten Pam's- und Farmer's-Delight-NeoForge-1.21.1-JARs dokumentieren.
2. Nach `/reload`: `/cropgenetics data pam`, `/cropgenetics data farmersdelight`, `/cropgenetics data errors` prüfen; Warnungen mit den tatsächlich registrierten Items vergleichen.
3. Repräsentative Seeds aller Adapter mit Crop Sticks, Analyzer, Journal, Save/Reload und Ernte durchspielen; gerade bei Zwiebel Selbst-Saatgut und bei namensauffälligem Pam's `mustardseeds`.
4. Kein anderes Ernteverhalten (speziell Reis oder Tomatenreben) als kompatibel markieren, bevor der Spieltest es bestätigt. **Noch keine Tests ausgeführt.**


## Schutz der Samenherkunft beim Einpflanzen – 19.11.2026

- `cd0c91c6`: `CropSticksBlockEntity.plant(Genome, ItemStack)` validiert jetzt den tatsächlichen **Quell-ItemStack**, auch wenn ein zukünftiger Adapter die Methode direkt aufruft und nicht den gewöhnlichen Spieler-Rechtsklick benutzt.
- Seed mit bereits vorhandenem `GENOME`: Genom muss dem zu pflanzenden Genom **exakt** entsprechen und `CropItems.isGeneticSeedStack` muss die Saat als legitime genetische Saat erkennen. Ein frei erfundenes `GENOME` auf irgendeinem Item genügt nicht.
- Gewöhnlicher Seed ohne `GENOME`: nur unmodifizierter Stack, keine genetische Seed-Item-Leerhülle, eindeutige registrierte Zuordnung zu genau der gewünschten Pflanzenart; Items mit fremden Data-Component-Patches werden weiterhin nicht stillschweigend verbraucht.
- `plant(Genome)` ohne Itemquelle bleibt für interne/gesonderte Aufrufer bestehen, allerdings ohne behauptete ursprüngliche Item-Herkunft. Die normale UI-Interaktion war bereits strenger; die untere Ebene zieht diese Regeln nun nach.
- Beabsichtigtes Sicherheitsverhalten: Unbekannte gepatchte Mod-Samen werden **nicht** heimlich konvertiert. Eine spätere echte verlustfreie Drittmod-Komponentenübernahme benötigt einen gezielten Adapter und entsprechende Tests.
- **Status:** Codeänderung auf Integrationsbranch, noch kein Gradle/JUnit/Minecraft-Test. Beim Sammeltest zusätzlich prüfen: normaler Vanilla-/Mod-Seed; analysierter Seed mit gültigem Genom; manipulierter/falscher Genome-Component-Stack; Falsch-Spezies aus fremdem Adapter; Save/Reload der `ORIGINAL_SEED`-Zuordnung.


## Erntemengen-Stabilität – 19.11.2026

- `5ab6b920`: `CropHarvest.products` begrenzt die erzeugten **Item-Stapel pro Ernte auf 256**. Bei gültigen, aber übergroßen Datapack-Erntedefinitionen (bis 64 Produkttypen à bis 4096 Items, wiederholt durch hohen Gain) konnte ein einzelner Rechtsklick/Blockabbau sonst extrem viele einzelne Item-Entities auslösen. Normale Erntemengen bleiben unverändert. Extremfälle werden an der Grenze abgeschnitten.
- Beim Aufteilen eines Item-Produkts wird eine nichtpositive, fehlerhafte Default-Max-Stackgröße defensiv auf 1 normalisiert, damit die Schleife nicht endlos läuft.
- `9e09f333`: `CropHarvestBoundTest` als JUnit-Regression: normales Gain-1-Ergebnis, hohe Produktausbeute mit 256er-Stapelgrenze, falsche Pflanzenart ohne Drops. **Angelegt, noch nicht ausgeführt.**
- Die Grenze gilt sowohl für Rechtsklick-Ernte als auch beim Blockabbau, da beide denselben Produktgenerator verwenden. Erzeugte genetische **Saatgut-Drops** bei Blockabbau sind davon getrennt und werden nicht begrenzt oder gelöscht.
- **Keine Verhaltensparität mit fremden Mods behaupten:** Farmer's-Delight-Tomatenreben, Zwiebel-Eigenvermehrung, Reis-Panicles/Knife-Drops und Pam's-Sonderernten müssen im späteren Gameplay-Test einzeln bestätigt werden. Beim nächsten Sammeltest insbesondere nach Rechtsklick und Blockabbau prüfen, dass Genom und `ORIGINAL_SEED` des ausgerissenen genetischen Samens identisch bleiben.


## Verbindliche neue Ernteobergrenze: EIN Stapel je Produkt – 19.11.2026

**Nutzerentscheidung (ersetzt ausdrücklich die vorherige 256-Stapel-Regel):** Bei einer einzelnen Ernte darf **jedes unterschiedliche Ernteprodukt maximal einen normalen ItemStack** erzeugen. Beispielsweise Weizen + Weizensamen ⇒ je höchstens ein Stapel; zweimal `minecraft:wheat` in den Datapack-Produkten wird zusammengeführt, nicht als zwei Stapel ausgegeben. Das Maximum je Produkt richtet sich nach dessen individueller Minecraft-Stapelgröße (z. B. 64 bei Weizen). Genetik-Gain 1..10 erhöht weiter die gewürfelte Stückzahl, aber nie über diese Grenze. Überhang aus extremen Einstellungen wird abgeschnitten.

- `f45a1d74`: `CropHarvest.products` aggregiert wiederholte Gain-Rolls und wiederholte Product-IDs in einer nach erster Nennung geordneten `LinkedHashMap<Item,Integer>`; begrenzt jede Produktmenge auf `item.getDefaultMaxStackSize()`; gibt danach genau einen Stapel je Item aus. Gilt für **Rechtsklick-Ernte und Blockabbau**, da beide den gemeinsamen Generator verwenden.
- **Wichtig:** Die ältere 256-Stapel-Schutzgrenze aus `5ab6b920` gilt **nicht mehr**. Sie wurde auf expliziten Nutzerwunsch durch die strengere produktspezifische Ein-Stapel-Regel ersetzt. Bei Datapacks mit mehrfach deklariertem identischem Produkt werden Counts nun zusammengeführt.
- `2195562d`: Regressionstests angepasst/erweitert: Standard-Gain-1, Gain-10 innerhalb eines Stacks, extreme mehrfach eingetragene gleiche Früchte nur ein Stapel, zwei unterschiedliche Produkte je ein Stapel, andere Species ohne Drops.
- Gourd-Fruit-Spezialmodus bleibt bei einer Frucht je Reifezyklus. Das beim **Abbau separat** zurückgegebene genetische Saatgut ist **kein HarvestProduct** und wird durch diese Regel nicht mit anderen Drops verschmolzen oder auf ein Produkt-Limit umgerechnet.
- **Noch ungetestet** (Gradle, JUnit, Minecraft). Beim Sammeltest mindestens Weizen+Samen, Farmer's-Delight-Onion, Pam's-Crops und Gain-10-Kulturen auch mit wiederholten gleichen Product-IDs prüfen.


## Analyzer-Slot und Pflanzbestätigung – 19.11.2026

- `5c3c38df`: `SeedAnalyzerBlockEntity.canPlaceItem` akzeptiert auf dem Server im Seed-Slot jetzt nur **gültige genetische Samen** oder **eindeutig registrierte, unmodifizierte normale Samen**. Ein gewöhnlicher Seed mit unbekanntem Data-Component-Patch wurde vorher durch die Slot-Prüfung angenommen, dann aber von `CropItems.analyzeSeed` zurecht nicht verändert: scheinbar funktionsloser Analyzer. Nun scheitert diese Eingabe bereits an der Slot-Prüfung, damit keine falsche Analyse-Erwartung entsteht.
- Die vorhandene clientseitige Einlege-Vorhersage bleibt unverändert offen; die serverseitige Containerprüfung bleibt maßgeblich. Ob das Menü bei abgelehnten Stacks sauber zurückspringt, muss im Minecraft-Test bestätigt werden.
- `47a7fb99`: `CropSticksBlock.useItemOn` meldet bei `cropgenetics:genetic_seed` auf dem Server erst dann Erfolg, wenn `crop.plant(genome, stack)` wirklich erfolgreich war. Bei einem abgelehnten direkten Pflanzversuch wird kein Seed konsumiert und kein falscher Erfolg gemeldet.
- **Weiterhin offen:** Ein expliziter Adapter für fremde, bereits geänderte ItemStacks kann später die vollen externen Komponenten in einem definierten Format übernehmen; dieser generische Pfad speichert **keine** beliebigen Fremdkomponenten. Gezielte manuelle Tests: normaler Seed, analysierter Vanilla-/Mod-Seed, Custom-Name-/Komponenten-Seed, genetischer Seed mit falscher Species, voller Analyzer-Slot, Client/Server-Multiplayer.
- Änderungen sind auf dem Integrationsbranch dokumentiert, aber **noch nicht gebaut oder in Minecraft getestet** (vereinbarter Sammeltest).


## Einheitlicher Pfad für einfache Saaten – 19.11.2026

- `b807d152`: neue gemeinsame Prüfung `CropGeneticsSeedRecognition.isUnmodifiedRegisteredSeed`: nur nichtleere, noch nicht genetisch beschriftete, **unmodifizierte** gewöhnliche Seeds mit eindeutiger Zuordnung in der aktiven Pflanzenregistry. Geschützte vorhandene Fremdkomponenten bleiben ausgeschlossen.
- `50e8cd5f`: Seed Analyzer benutzt diese Prüfung zusätzlich zum existierenden `CropItems.isGeneticSeedStack` für bereits genetische Samen.
- `1f39851d`: Crop Sticks benutzen dieselbe Prüfung für den 1/1/1-Pflanzweg. Bereits analysierte Seeds mit gültigem gespeichertem `GENOME` bleiben auf ihrem **gesonderten** Weg erlaubt und behalten ihr tatsächliches Genom. Dies ist eine Umstrukturierung der bisherigen Akzeptanzregeln, keine neue automatische Fremdmod-Konvertierung.
- **Absichtliche Unterscheidung:** Seed Chest speichert auch angepasste, eindeutig registrierte Seeds ohne Konvertierung, damit individuelle Item-Komponenten beim bloßen Lagern erhalten bleiben; Analyzer und Pflanzweg dürfen solche extern gepatchten Stacks dagegen ohne echten Lossless-Adapter nicht umschreiben oder verbrauchen.
- **Ungetestet:** späteren Sammeltest für (a) unveränderte Vanilla-/Pam-/Farmer-Seeds, (b) vorher analysierte gleiche Seeds, (c) extra komponierte Mod-Seeds, (d) originale Chest-Stack-Daten nach Save/Reload und (e) klientseitige Slot-Rückmeldung durchführen. Noch kein Build/JUnit/Minecraft-Lauf.


## Externe Regressionstests für Analyzer und Seed Chest – 19.11.2026

- `56d93786`: `docs/SEED_ANALYZER_CHEST_REGRESSION_2026-11-19.md` ergänzt. Neun manuelle Testfälle (SC-01 bis SC-09) mit Vorbereitung, Handlung, Grund und exakt prüfbarem Soll-Ergebnis: 64er-Seeds beim Analyzer, Erhalt existierender Genwerte, Fremdkomponenten, kombinierte Chest-GUI, Save/Reload, volle Inventare, doppelte Crop Sticks, Samenherkunft beim Abbau und optional-mod-Entfernung in Sicherungskopie.
- **Code-Audit dieses Durchgangs:** `SeedAnalyzerBlock.useItemOn` kopiert bei direktem Einlegen 1 Seed, `SeedInventoryBlockEntity.setItem` erstellt eine Kopie des übergebenen Stacks, `SeedAnalyzerBlock.useWithoutItem` versucht bei Entnahme erst das Spielerinventar und danach ein Welt-Drop, `SeedChestBlock` benutzt 27 persistent gespeicherte Slots. Es wurde **keine zusätzliche ungeprüfte Inventar-Umbauänderung** vorgenommen.
- Wichtiger Unterschied bleibt: Seed Chest ist reine sichere Lagerung auch für registrierte Seeds mit fremden Komponenten; Analyzer verweigert ihre automatische Analyse, solange keine verlustfreie Komponenten-Übernahme implementiert ist.
- **Alle Fälle offen**; sie sind Anleitung für den späteren gemeinsamen Test oder einen externen Tester, kein automatisch durchlaufener Minecraft-Nachweis.


## JEI-Hinweise für optionale Pflanzenmods – 19.11.2026

- `7943f424`: `compat/jei/CropGeneticsJeiPlugin.registerRecipes` verknüpft die bereits vorhandene Crop-Sticks-/Genetik-Einführungsseite jetzt auch mit **vorhandenen** Farmer's-Delight-Seeds (`cabbage_seeds`, `tomato_seeds`, `onion`) und nach dem registrierten Item-Namensschema passenden Pam's-HarvestCraft-2-Paaren (`<crop>seeditem` + `<crop>item`). Erwartete Ernteprodukte müssen jeweils wirklich in der Item-Registry vorhanden sein; Pam's-Scan auf 128 registrierte Seed-Kandidaten begrenzt.
- Farmer's-Delight-`rice` wird wegen eigener Wasserpflanzungs-/Knife-Erntemechanik **bewusst nicht** als normaler Crop-Sticks-Seed verlinkt. Andere Namespace-Items werden niemals allein wegen des Wortes `seed` erkannt.
- Wichtig: **JEI zeigt nur einen Informations-Link**, kein garantiert nutzbares Rezept und keine neue Crop-Registrierung. JEI kennt auf der Clientseite den effektiven Server-Pflanzenkatalog nicht zwingend. Ob ein Seed in Crop Sticks gesetzt werden darf, prüft weiterhin die aktive Server-Pflanzendefinition. Daher ist auch bei vorhandener Mod-JAR und einem JEI-Hinweis ein Datapack-Disable möglich.
- `78beee21`, `57f7c39e`: `en_us.json` / `de_de.json` erklären diesen Unterschied ausdrücklich auf der JEI-Seite.
- Noch **nicht gebaut oder in Minecraft geprüft**. Beim späteren gemeinsamen Test JEI mit und ohne jede optionale Mod prüfen, Hilfeseite auf Kohl-/Tomatensamen/Zwiebel und mindestens einem Pam's-Samen suchen; Reis darf nicht als normaler Seed suggeriert werden. Bei deaktiviertem Server-Datapack die Information als rein erklärenden Link verstehen.


## Genetische Seeds: Stapeln vor Analyse – 19.11.2026

- Code-Audit: `content/GeneticSeedItem.java` verwendet schon `properties.stacksTo(1)`. **Jeder** `cropgenetics:genetic_seed` ist damit unabhängig vom Analyseflag maximal 1 Item pro Stapel. `CropComponents.GENOME` ist persistent und wird über den Netzwerkcodec synchronisiert. Der Analyzer markiert nur `ANALYZED`, ohne Gene neu zu erzeugen.
- `526efd20`: `GeneticSeedStackingTest` als Regression ergänzt, damit die Ein-Seed-Stapelgrenze später nicht versehentlich aufgehoben wird. **Test noch nicht ausgeführt.**
- **Wichtige Unterscheidung:** Normale Vanilla- oder Mod-Samen können vor dem Pflanzen weiterhin stapelbar sein. Werden sie analysiert, erhalten sie denselben registrierten 1/1/1-Ausgangsgenotyp samt Item-Komponenten; die wirklich individuell verbesserten gekreuzten Saaten entstehen als separater genetischer Seed mit Stapelgröße 1. Eine hypothetische Fremdmod, die individuelle Mutationswerte auf normalen stapelbaren Seeds speichert, benötigt einen expliziten verlustfreien Adapter und wird momentan nicht pauschal umgewandelt.
- Beim Sammeltest: zwei unterschiedlich verbesserte **nicht analysierte** genetische Seeds in Inventar, Chest und beim Aufsammeln vergleichen; sie dürfen nicht zu einem Stapel verschmelzen und beim Analyzer keine Gene verändern.


## Lesender Offline-JAR-Audit für optionale Pflanzenmods – 19.11.2026

- `e47149b7`: `neoforge-port/scripts/inspect-optional-crop-jars.ps1` hinzugefügt. Nimmt ein oder mehrere explizite lokale `.jar`-Dateipfade, gibt Dateiname, SHA-256 und vorhandene Metadatadateien aus. Es durchsucht **nur ZIP-Eintragsnamen**, insbesondere `assets/pamhc2crops/models/item/*.json` und `assets/farmersdelight/models/item/*.json`, und vergleicht Pam's `seeditem`/`item`-Kandidaten sowie Farmer's-Delight-`cabbage_seeds`, `tomato_seeds`, `onion`, `rice`, `rice_panicle`.
- PowerShell-Aufruf **später nach Abschluss aller Änderungen**, aus `neoforge-port`:
  ```powershell
  $jarFiles = @(
      'C:\Pfad\zur\pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar',
      'C:\Pfad\zur\farmersdelight-neoforge-1.21.1.jar'
  )
  & '.\scripts\inspect-optional-crop-jars.ps1' -JarPaths $jarFiles
  ```
  Die beiden Pfade oben sind **Platzhalter**: vorhandene JAR-Dateien auswählen, nicht den Namen als tatsächliche Installation voraussetzen.
- **Interpretationsgrenze:** Ein fehlender oder vorhandener Item-Modell-Asset-Name beweist **nicht**, dass eine Item-Registry-ID fehlt oder existiert. Die echte Freigabe erfolgt erst später durch die Laufzeit-Registry und den Minecraft-Test mit `/cropgenetics data pam` bzw. `farmersdelight`. Das Skript ist ein schneller statischer Vorabvergleich und **verändert keine JARs und keine Weltdaten**.
- Skript wurde erstellt und im Branch dokumentiert, aber noch nicht in Windows PowerShell ausgeführt.


## Sicherheitsfix: Crop-BlockEntity-Netzwerksynchronisierung – 19.11.2026

- **Gefundene Lücke:** `CropSticksBlockEntity.getUpdateTag` lieferte zuvor `saveWithoutMetadata` vollständig aus. Dadurch erreichte das vollständige, noch nicht analysierte `Genome` alle Clients, die den Block beobachteten, obwohl Tooltip/Overlay Gene vor der Analyse verbergen. Außerdem wurden historische `OriginalSeed`-Daten und serverseitige Frucht-Diagnosezähler unnötig gesendet.
- `5cef6825`: `CropGenomeSyncRules.forClient` als pure Funktion eingeführt: Bei unanalysiertem Genom wird nur die **Pflanzenart** mit neutralen 1/1/1-Platzhalterwerten dargestellt. Bei analysierter Pflanze dürfen die echten sichtbaren Gene gesendet werden. Die serverseitige Originalinstanz wird nicht verändert.
- `47405156`: `getUpdateTag` verschickt jetzt dieses redigierte Genome bei unanalysierten Pflanzen und entfernt `OriginalSeed` sowie private Frucht-Diagnosezähler **nur aus dem Update-Paket**. `saveAdditional` bleibt vollständig; Welt- und Chunk-Speicherung enthalten weiterhin echte Genetik und Original-Samen-Herkunft. GrowthStage, Pflanzenart, Darstellungs-/Fruchtstatus bleiben für Clients erhalten.
- `859a76e0`: JUnit `CropGenomeSyncRulesTest` prüft versteckte Werte, unveränderte Serverdaten, analysierte tatsächliche Werte und leere Crop Sticks. **Tests noch nicht ausgeführt.**
- **Wichtige Semantik:** Der globale `seedAnalyzed`-Status einer bereits analysierten Pflanze ist keine pro-Spieler-Freischaltung; nach einer Analyse dürfen Beobachter die sichtbaren Werte erhalten. Wer strikt private, spielerbezogene Analysedaten möchte, benötigt eine andere Berechtigungs-/Paketarchitektur.
- Sammeltest: zwei Clients im Multiplayer, versteckte 8/9/10-Seedwerte vor Analyse nur auf Server bzw. Seed gespeichert; Render- und Jade/Identify-Anzeige ohne Gene; nach Analyse echte Werte sichtbar; Blockabbau ergibt immer noch 8/9/10 und ursprüngliche Seed-ID; Welt-Neustart ohne Genetikverlust. Separat prüfen, dass Fruchtanimation und Wachstum nicht durch die reduzierten Pakete beeinträchtigt werden.


## Seed-Journal-Darstellung für optionale Mods – 19.11.2026

- `fd5d6e2b`: `CropSpeciesNames.preferredProduceItem` kennt nur die **bereits freigegebenen** optionalen Namensmuster: Pam's `pamhc2crops:<crop>_plant` → `pamhc2crops:<crop>item`, Farmer's Delight `cabbage_plant`/`tomato_plant`/`onion_plant` → entsprechende Ernteitems. **Farmer's Delight rice bleibt ausdrücklich ausgeschlossen.**
- Existiert das Item in der aktiven Registry, wird dessen lokalisierter Mod-Item-Name im Journal, in der Seed-Beschriftung und HUD-Namensauflösung bevorzugt. Fehlt es, bleibt der bisherige einfache Pflanzen-ID-Fallback. Es werden weder neue Pflanzen registriert noch neue Saatgut-Daten erzeugt.
- `45b785bb`: `SeedJournalScreen.iconFor` verwendet für diese Mod-Pflanzen bei vorhandener Item-Registrierung das tatsächliche Ernteitem als Icon; fehlt die Mod/Registry, bleibt der genetische Seed als Ersatzsymbol.
- `cb768ff7`: `CropSpeciesNamesTest` ergänzt: alle drei Farmer-Pilotpflanzen, ausgeschlossener Reis, Pam's `mustardseeds` sowie fremde/ungültige IDs. **Noch nicht ausgeführt**.
- Beim Sammeltest Journal mit Farmer's und Pam's öffnen, Namen auf Deutsch/Englisch und Icons vergleichen; fehlende optionale Mod muss mit dem generischen Fallback ohne Crash weiter funktionieren. Die tatsächlich ausgerissenen genetischen Seeds bleiben unabhängig von diesem reinen Anzeige-Fallback erhalten.
