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


## Slot-Stapelgrößen in Analyzer und Seed Chest – 19.11.2026

- **Gefundene Schutzlücke:** `SeedInventoryBlockEntity.setItem` begrenzte einen eingehenden Stack bisher ausschließlich mit der Behälterkapazität (`getMaxStackSize`). Eine Seed Chest erlaubt 64; der Genetik-Seed selbst darf aber nur 1 pro Stapel enthalten. Normale Menüaktionen respektieren gewöhnlich das Itemlimit bereits, aber direkte `Container.setItem`-Aufrufe, ungewöhnliche Mod-Menüs oder korrupte Daten konnten die zentrale Inventarregel umgehen.
- `894ed6f0`: Der gemeinsame Setzpfad nutzt jetzt den **kleineren Wert** aus Behälterlimit und Item-eigener Maximalstapelgröße und arbeitet weiterhin auf einer **Kopie** des übergebenen Stacks. Damit bleibt die Chest für gewöhnliche Seeds stapelbar und der Analyzer auf einen Seed beschränkt; genetische Seeds sind auch in einer Chest strikt einzeln.
- `de94d125`: Kleine überprüfbare Regel `SeedInventoryStackLimits.allowedCount` ausgelagert. Leere Stacks ergeben 0, die Grenze ist für gültige Stacks positiv; defensive Behandlung unerwartet falscher Limits.
- `429de65b`: `SeedInventoryStackLimitsTest` enthält Fälle für genetischen Seed in 64er-Chest, normale Weizensamen, Analyzer-Einzel-Slot, 16er-Itemlimit sowie leerem Stack. **Nicht ausgeführt**.
- Wichtig: Die neue Regel **trunkiert** einen übergroßen, über `setItem` eingesetzten Stack wie schon zuvor; sie implementiert keine Overflow-Rückgabe für fehlerhafte externe Aufrufer. Direkte externe Container-Integrationen müssen gültige Mengen übergeben. Dieser Fix verändert weder Ernteprodukt-Stacks noch den Ein-Stapel-je-Frucht-pro-Ernte-Grundsatz.
- Sammeltest: Seed Chest mit 64 identischen normalen Weizensamen und mehreren individuell mutierten genetischen Seeds befüllen; genetische dürfen nicht zusammenstacken. Analyzer darf einen gewöhnlichen Stack nur **einzeln** einlegen und dessen Genom nicht verändern. Entnahme, angrenzende Analyzer-GUI, Welt speichern und laden und modifizierte Seed-Stacks prüfen. Build/JUnit/Minecraft absichtlich weiterhin offen.


## Provenienzschutz für individuell angepasste Fremd-Seeds – 19.11.2026

- **Gefundene Lücke:** `CropItems.seedWithGenome(genome, sourceSeed)` ist als optionaler Konvertierungs-Einstieg gedacht. Ein normaler Seed, auf dem andere Mods individuelle Data Components speichern, konnte diesem Hilfsaufruf übergeben werden, obwohl das genetische Ersatz-Item deren zusätzliche Komponenten **nicht** vollständig speichert. Das würde eine nur scheinbar verlustfreie Umwandlung erlauben.
- `d7753871`, `b9d73a9f`, `21ab3c56`: Wenn ein **noch nicht genetisch annotierter, normaler** Seed einen fremden Components-Patch trägt, gibt die quellbezogene Konvertierung jetzt explizit `ItemStack.EMPTY` zurück. Ein solcher Sonder-Seed bleibt für diese API unsupported, bis ein wirklich verlustfreier Adapter definiert ist. Die Quell-ItemStack-Instanz wird nicht mutiert.
- Bereits genotypisierte Seed-Stacks sind von dieser speziellen neuen Schranke ausgenommen; ihre bisherigen Same-Genome-/Provenienzprüfungen gelten weiterhin. **Nicht behaupten**, dass beliebige zusätzliche Komponenten auf bereits genotypisierten fremden Items vollständig kopiert werden: Die API speichert derzeit weiterhin nur die vorgesehenen genetischen und Herkunftsfelder.
- `38a0628c`: Isolierte JUnit-Regressionsfälle für unveränderten Vanilla-Seed, individuell umbenannten normalen Seed, bereits genotypisierten Seed und das eigene genetische Item. **Nicht ausgeführt.**
- Keine Änderung an der Storage-only-Ausnahme: Die Seed Chest kann eindeutig registrierte, fremd angepasste Stacks weiterhin **unverändert lagern**, ohne sie umzuwandeln; Analyzer und einfacher Crop-Sticks-Pflanzweg lehnen diese bewusst ab.
- Sammeltest nach allen Änderungen: angepasste Pam-/Farmer-Samen in Seed Chest lagern, Save/Reload und Komponenten vergleichen; Einsetzen in Analyzer und Crop Sticks darf den Stack nicht konsumieren oder stillschweigend vereinfachen; gewöhnliche und korrekt analysierte Samen funktionieren weiterhin. Erst nach einem expliziten Drittmod-Adapter darf die Konvertierung von Sonder-Komponenten als unterstützt gelten.


## Kreuzungen mit drei/vier Eltern: Pflanzenarten aus allen Elternpaaren – 19.11.2026

- **Nutzerregel:** Zwei bis vier direkt angrenzende, reife Eltern erhöhen die Kreuzungs- und sichtbare Gen-Evolutionschance. Eine zusammenhängende, geradlinige Züchterreihe mit mindestens drei Eltern und nur einem unmittelbaren Nachbarn bleibt die schwächste Variante.
- **Audit des bereits vorhandenen Codes:** `CrossFertilityRules` enthält die Fertilitätsfaktoren 2 Eltern = 1,0; 3 = 1,25; 4 = 1,5, bei Basis 30 % (vor Serverconfig): 30 % / 37,5 % / 45 %. Reihe: Faktor 0,5 = 15 %. Gen-Mutationsmultiplikatoren: 1 / 2 / 3 bzw. 0,5 für Reihe. `BreedingEngine.combine` wählt die drei einzelnen Stat-Werte bereits aus allen 2–4 Eltern aus.
- **Gefundene Lücke:** `CrossBreeding.chooseChildSpecies` wertete bisher nach einer zufälligen Reihenfolge **nur das erste Elternpaar** für Datapack-`MutationDefinition.matchesParents` aus. Bei 3–4 Eltern konnten Mutationsrezepte der übrigen Elternkombinationen übersehen werden. Auch die Fallback-Kindart wurde nur aus dem ersten Paar gewählt.
- `c52e01d5`: Jede Mutation-Definition wird nun gegen **sämtliche ungeordneten Paare unterschiedlicher Elternpositionen** aus der tatsächlich teilnehmenden Elternliste geprüft (2 Eltern = 1 Paar; 3 = 3 Paare; 4 = 6 Paare). Der Datapack-Mutationswurf und die bestehenden Standort-/Boden-/Saison-/Chemiefilter bleiben erhalten; ein Rezept wird weiterhin nur **einmal** gewürfelt, selbst wenn mehrere Paare passen. Ohne Mutation kann die Nachkommen-Art von jedem der beteiligten Eltern kommen, sofern sie am Ziel wächst.
- `2ae54273`: Paarprüfung als kleine pure `CrossParentPairRules.matchesAnyPair` ausgelagert.
- `8164170b`: Fünf JUnit-Fälle für Zweierpaar, Paar mit drittem Elternteil, Paar mit viertem Elternteil, nicht passendes Rezept und Einzel-Elternteil ohne Paar. **Nicht ausgeführt.**
- **Wichtig für Erwartungsmanagement:** Die zusätzlichen Eltern erhöhen weiterhin die **Fertilität** und die Chance auf eine **Verbesserung der drei Gene**. Sie verändern nicht automatisch die individuellen `MutationDefinition.chance`-Werte oder erfinden neue Rezepte; das Projekt muss diese Mutationsrezepte im Datapack definieren. Reihenzucht bleibt im Vergleich benachteiligt.
- Späterer Sammeltest: 2, 3, 4 reife direkte Eltern in verschiedenen Himmelsrichtungen mit mindestens zwei unterschiedlichen Arten; Kreuzungsfeld in der Mitte, Mutationsrezept nur für Elternpositionen 2+3 bzw. 1+4 und ein Versuchsaufbau in gerader Linie. Wiederholte Läufe für Wahrscheinlichkeiten und gleicher Genom-Vererbungspfad, ggf. mit fester Welt-/RNG-Sequenz. Build, JUnit und Minecraft weiterhin bewusst ausgesetzt.


## Komponentenverlust auch bei bereits genetischen Quellen verhindern – 19.11.2026

- **Nachprüfung des vorherigen Provenienzschutzes:** Die erste Regel aus `21ab3c56` war absichtlich auf *noch nicht genotypisierte* Fremd-Samen begrenzt. Ein bereits analysierter normaler Seed oder das genetische Seed-Item konnte aber zusätzlich einen Custom Name, Mod-Component oder eine andere ItemStack-Patchänderung tragen, die `seedWithGenome(genome, sourceSeed)` nicht übernimmt. Für diesen allgemeinen Konvertierungs-Einstieg wäre das weiterhin Datenverlust.
- `a088e312`: `CropSeedSourceSafety.canConvertWithoutExternalDataLoss` prüft jetzt **jeden geänderten Data-Component-Typ** aus `getComponentsPatch().entrySet()`. Nur `CropComponents.GENOME`, `ANALYZED` und `ORIGINAL_SEED` sind als von diesem Konvertierungsweg bewusst unterstützte Komponenten freigegeben. Andere Komponenten – auch z. B. `CUSTOM_NAME` und Fremdmod-Eigenschaften – führen zu einer ausdrücklichen Ablehnung.
- `534f2094`: Die strengere Prüfung gilt jetzt auch für schon genotypisierte normale Seeds und eigene genetische Seeds. Bei abgelehnter Konvertierung: `ItemStack.EMPTY` zurückgeben und **den Quellstack unverändert lassen**. Die bestehenden normalen Crop-Sticks-/Analyzer-Aktionen, Chest-Aufbewahrung und die Ernteerzeugung verwenden jeweils ihre eigenen unveränderten Pfade.
- `92667ceb`: JUnit `CropSeedSourceSafetyTest` auf unveränderten Seed, modifizierten unanalysierten Seed, modifizierten analysierten Seed, modifizierten genetischen Seed und leeren Stack ausgebaut. **Tests nicht ausgeführt.**
- **Wichtige API-Regel für spätere Adapter:** Ein `ItemStack.EMPTY` aus der quellbezogenen Konvertierung bedeutet *nicht konvertiert*. Aufrufer dürfen dann **keinen** Originalstack konsumieren und müssen ggf. einen lossless Adapter anbieten. Die normale `seedWithGenome(genome)`-Erzeugung für echte neue Kreuzungsnachkommen bleibt davon getrennt.
- Der Daten-Component-Patch wird gegen die drei erlaubten *Typen* geprüft. Das ist eine konservative Schranke, keine pauschal garantierte Migration von mod-eigenen Item-Daten. Bei entfernten Mods oder Data-Component-Schemaänderungen im Gameplay-Test erneut prüfen. Weiterhin kein Gradle-/JUnit-/Minecraft-Lauf bis zum gemeinsamen Test.


## Mod-Autoerkennung: Gründe für übersprungene Paare – 19.11.2026

- **Audit-Ergebnis:** `RegisteredSeedCropDiscovery.addMissing` sicherte bereits den Vorrang expliziter Datapack-Pflanzen und belegter Seed-/Ernte-Items. Für **konfigurierte** `item_pairs` wurde der Kandidat bei solchen bereits vorhandenen Claims jedoch stillschweigend übersprungen. Bei Pam's-Versionen mit abweichenden IDs sieht der Pack-Autor dann nicht, *warum* die gewünschte Art nicht auto-registriert wurde.
- `30c0e4e6`: `reservedClaimReason` liefert den konkreten Grund: Pflanzenart bereits definiert, Saatgut/Ernteprodukt bereits reserviert oder vertauschte Item-Rollen belegt. Für explizit konfigurierte Paare wird jetzt ein `DISABLED`-Diagnoseeintrag mit diesem Grund geschrieben. Die zuvor sichere Vorrangregel ändert sich **nicht**. Für tausende rein automatisch inferierte Paare werden diese absichtlichen Überspring-Gründe weiterhin nicht massenhaft protokolliert.
- `d2ce0cd8`: Fünf JUnit-Fälle für freie Kandidaten, explizite Pflanzen-ID, bereits aktive Pflanzen-ID, getrennte Seed-/Produktbelegung und rollenübergreifende Belegung. **Nicht ausgeführt.**
- `c7d04bcb`: Farmer's Delights spezieller Onion-Fallback meldet nun ebenfalls `DISABLED`, falls eine explizite Pflanze bzw. ein expliziter Itemclaim Vorrang hat, oder `CONFLICT` mit der vorhandenen Plant-ID, wenn eine andere aktive Pflanzenart die Zwiebel bereits als Seed/Ernteprodukt verwendet. Der Onion-Einzelsaatgutpfad bleibt unverändert; **Reis bleibt absichtlich ausgeschlossen**.
- **Abnahme später:** Mit und ohne Pam's/Farmer's jeweils `/cropgenetics data pam` und `/cropgenetics data farmersdelight` prüfen. Eine gewollte explizite Pflanze darf weiterhin nicht überschrieben werden, soll aber bei einem betroffenen konfigurierten Fallback als nachvollziehbarer Skip auftauchen. Echte doppelte Item-Claims dürfen keine zweite Pflanze erzeugen. Keine Gradle-/JUnit-/Minecraft-Ausführung in dieser Runde.


## Datapack-Reload: Warnstufen und Snapshot-Regressionen – 19.11.2026

- **Audit:** `CropDataReloadListener.apply` baut alle Pflanzen, Mutationen und Diagnosen zunächst lokal und veröffentlicht sie dann über `AgriPlantsRegistry.replaceAll` als einen neuen `volatile Snapshot`. Ambige Seed-IDs werden aus der Rückwärtszuordnung entfernt. Dieser Kern muss für das aktuelle Paket **nicht** umgebaut werden.
- **Gefundene Diagnose-Verwirrung:** Der Reload-Logger gab bisher *alle* Einträge außer `LOADED` und `AUTO_DISCOVERED` als `LOG.warn` aus. Auch `DISABLED` (`enabled=false` bzw. gewünschter Datapack-Vorrang) und `MISSING_MOD` (nicht installierte optionale Mod) erschienen dadurch als Warnung.
- `8f39cede`, `56f776bb`: `CropReloadLogSeverity` ordnet `DISABLED` und `MISSING_MOD` zu **INFO**, aktive Fehler `INVALID`, `CONFLICT`, `MISSING_SPECIES`, `AUTO_MISSING_ITEM`, `WARNING` weiterhin zu **WARN**. Erfolgreiche `LOADED`/`AUTO_DISCOVERED`-Einträge bleiben aus dem Einzel-Log heraus und erscheinen weiterhin in der Zusammenfassung. **Die Statuswerte im eigentlichen Report bleiben unverändert.**
- `42836cf8`: JUnit `CropReloadLogSeverityTest` prüft alle neun Statusarten und ihre Logklassen. **Nicht ausgeführt.**
- `d35f77c3`: JUnit `AgriPlantsRegistrySnapshotTest` neu. Prüft, dass zwei Pflanzen mit derselben Seed-ID **keine willkürliche Seed-Zuordnung** erhalten, und dass ein anschließender Reload sowohl alte Claims als auch Ambiguitäten ersetzt. Ein zuvor erhaltener Snapshot bleibt unabhängig und unveränderlich. **Nicht ausgeführt.**
- Sammeltest: `/reload` mit gültigem Pack, bewusst `enabled=false`, fehlender Pam-/Farmer-Mod und explizit konfliktbehafteten Plant-IDs. Erwartung: modlose/deaktivierte Einträge nur INFO, echte Konflikte WARN, neue aktive Samenbelegung ohne Restzuordnung aus dem alten Pack; alte genetische Welt-/Seed-Daten dürfen dadurch nicht umgeschrieben werden.


## JEI-Kandidaten für optionale Saaten zentral abgesichert – 19.11.2026

- **Audit:** Die vorhandene JEI-Hilfeseite ist ausdrücklich eine Informationsseite und prüft nicht die serverseitige aktuelle Datapack-Aktivierung. Deutsch/Englisch erklären diese Einschränkung bereits korrekt. Es wurde **kein** neues pflanzbares Item und kein vorgetäuschtes Crafting-Rezept eingeführt.
- `b3e28c9e`: `compat/jei/OptionalSeedGuidePolicy.produceForSeed` als beschränkte, testbare Zuordnung. Nur die drei Farmer's-Delight-Einstiege `cabbage_seeds`, `tomato_seeds`, `onion` sowie Pam's-`pamhc2crops:<crop>seeditem` → `<crop>item` werden Kandidaten für die **JEI-Erklärhilfe**. Fremde Mods, generische `*_seeds`-Namensvermutungen und Farmer's-Delight-`rice`/`rice_panicle` sind ausgeschlossen.
- `3a35ddea`: `CropGeneticsJeiPlugin.addOptionalModSeedInfo` benutzt die gemeinsame Policy, behält aber die bestehende Registryprüfung auf tatsächlich vorhandene Seed- und Ernteitems und die 128er-Obergrenze des Pam's-Scans.
- `e98d677d`: JUnit `OptionalSeedGuidePolicyTest` für drei Farmer-Pflanzen, abgewiesene Reissamen/Panicles, Pam's-Namenskonvention, Pam's Trees und beliebige fremde Samen. **Nicht ausgeführt.**
- **Interpretation:** Der JEI-Link autorisiert **keinen** Crop-Sticks-Pflanzvorgang. Der Server kann diese Pflanzen durch Datapack-Regeln weiterhin deaktivieren. Pam's-Sonderkulturen und Farmer's-Delight-Reis benötigen nach wie vor separate Originalmechanik-Prüfungen. Diese Arbeit ist Regelabsicherung für die JEI-Darstellung, kein bestätigter Kompatibilitätsnachweis.
- Sammeltest: JEI-Rezept-/Verwendungsansicht bei Weizen, Farmer's Cabbage/Tomato/Onion und Pam's-Samen; bei Rice/Rice Panicle darf die generische Crop-Sticks-Hilfe nicht als Pflanzenunterstützung erscheinen. Mit fehlender optionaler Mod darf JEI ohne NoClassDefFoundError starten. Build und Minecraft weiterhin ausgesetzt.


## Pam's öffentliches Quellprojekt: Registry-Namen und Food-Seeds – 19.11.2026

- **Quellprüfung:** Das öffentliche Herstellerprojekt `MatrexsVigil/phc2crops`, Datei `src/main/java/pam/pamhc2crops/init/ItemRegistry.java` (https://github.com/MatrexsVigil/phc2crops/blob/master/src/main/java/pam/pamhc2crops/init/ItemRegistry.java) führt Beispiele wie `agaveseeditem` und `agaveitem` auf. Für den Namen `mustardseeds` erscheinen **zwei getrennte IDs**: `mustardseedsseeditem` als Saatgut und `mustardseedsitem` als Ernte-/Grain-Item; bei `sesameseeds` analog `sesameseedsseeditem` und `sesameseedsitem`. Der bestehende `<crop>seeditem`-Algorithmus ist dafür grundsätzlich korrekt; das zusätzliche `s` im Pflanzenstamm darf nicht abgeschnitten werden.
- Der veröffentlichte Code zeigt außerdem `ItemPamFoodSeed` für einige **selbst pflanzbare Ernte-Items** wie `cottonitem`, `flaxitem`, `kenafitem`, `juteitem`, `sisalitem`, `coffeebeanitem`, `tealeafitem`, `agaveitem` und `gingeritem`. Diese Items sind nicht identisch mit den eigenständigen `seeditem`-IDs. **Entscheidung:** Nicht pauschal alle Lebensmittel als Crop-Sticks-Saatgut registrieren. Nur nach Kontrolle der konkreten 1.21.1-JAR optionale `item_pairs`-Definitionen/gezielte Food-Seed-Adapter ergänzen, wenn deren Semantik erhalten bleibt.
- **Versionsgrenze:** Das öffentlich einsehbare `master`-Java-Projekt zeigt noch alte `net.minecraftforge`-/`net.minecraft.item`-Imports, ist also **kein belastbarer Quellnachweis für den NeoForge-1.21.1-Build**. Die tatsächlichen Registry-IDs und Interaktionen müssen mit der lokalen 1.21.1-JAR und den Runtime-Diagnosekommandos gegengeprüft werden. Keine Veröffentlichung kopierter Hersteller-Assets; deren Repository nennt ausdrücklich Rechtevorbehalte.
- `3e524556`: `OptionalSeedGuidePolicyTest` prüft jetzt auch `mustardseedsseeditem` → `mustardseedsitem` und `sesameseedsseeditem` → `sesameseedsitem`.
- `5e74ece4`: `PamPublishedNamingContractTest` prüft die reine Pam's-Suffix-Inferenz für einfache Namen, `mustardseeds`/`sesameseeds` und das Verbot fremder Namespaces. **Tests nicht ausgeführt**.
- **Kein weiterer Auto-Adapter aktiviert:** Dieses Paket dokumentiert den Herstellerbefund und legt Regressionen an; es behauptet weder, dass die 1.21.1-JAR dieselben Namen trägt, noch dass selbst pflanzbare Pam's-Ernte-Items schon in Crop Sticks unterstützt werden.


## Offline-JAR-Prüfung für Pam's Sonderfälle erweitert – 19.11.2026

- `528128b0`: `neoforge-port/scripts/inspect-optional-crop-jars.ps1` meldet nun zusätzlich die **Anzahl der Seed-Item-Modellkandidaten ohne passendes Ernteitem-Modell**, die ausdrücklich plural benannten Pflanzen `mustardseeds` und `sesameseeds` sowie eine eng begrenzte Prüfliste historisch *selbst pflanzbarer* Pam's-Ernte-Items (`cotton`, `flax`, `kenaf`, `jute`, `sisal`, `coffeebean`, `tealeaf`, `agave`, `ginger`).
- Die Liste stammt aus dem älteren, öffentlichen `MatrexsVigil/phc2crops`-Quellprojekt; die **NeoForge-1.21.1-Version ist damit keineswegs verifiziert**. Der Report zeigt pro Kandidat lediglich, ob `assets/pamhc2crops/models/item/<item>.json` und ein dazugehöriges Seed-Modell als ZIP-Einträge vorkommen. **Ein Modell allein beweist weder einen registrierten Gegenstand noch dessen direkte Pflanzbarkeit oder Drop-/Wachstumslogik.**
- Kein neuer Crop-Sticks-Adapter und keine ungeprüfte Food-Item-Freigabe. Das Skript öffnet explizit übergebene lokale JARs **nur lesend**, gibt SHA-256 aus und verändert weder Mods noch Spielstände.
- Nach Abschluss der Codepakete auf Windows mit den tatsächlich installierten, versionsgenauen 1.21.1-JAR-Pfaden ausführen; Ausgabe als Text für den Handoff und die Test-Tracker-Fälle aufbewahren. Anschließend im Spiel mit `/cropgenetics data pam` Registry-Support und Originalmechanik vergleichen.
- **PowerShell-Skript und übrige Tests weiterhin nicht ausgeführt.**


## Korrigierte optionale Mod-Diagnosezahlen – 19.11.2026

- **Befund nach den letzten Diagnose-Ergänzungen:** `CropReloadReport.pamDiscoveryStats()` und `farmersDelightStats()` zählten neue `DISABLED`-Einträge für *gewollte* explizite Override-Pflanzen noch im Restposten „other warnings“. Sie erschienen bereits als INFO im Log, blähten aber die zusammengefasste Warnungszahl auf.
- `d6edbf8a`: `DISABLED` zählt in diesen beiden optionalen Auto-Discovery-Statistiken **nicht mehr als Warnung**. `CONFLICT`, `AUTO_MISSING_ITEM`, `WARNING` und andere tatsächliche Problemzustände behalten ihre bisherigen jeweiligen Zähler. Der vollständige `CropReloadReport.entries()` und `count(Status.DISABLED)` behalten die übersprungenen Fälle weiterhin – es geht **keine Diagnose verloren**.
- `f548c9bb`: Drei JUnit-Fälle prüfen Pam's- und Farmer's-Delight-Zusammenfassung getrennt sowie Sichtbarkeit von absichtlich übersprungenen Auto-Kandidaten im Rohreport. **Nicht ausgeführt.**
- Späterer Test: `/cropgenetics data pam` / `farmersdelight` mit expliziten Datapack-Overrides und tatsächlich fehlerhaften Kandidaten vergleichen. Gewollte Overrides müssen im Detailreport als `DISABLED` sichtbar bleiben, ohne „other warnings“ zu erhöhen; ein echtes fehlendes Item muss als `AUTO_MISSING_ITEM` zählen. Keine Builds oder Minecraft-Läufe vor Sammeltest.


## Farmer's Delight 1.21.1: Reis-Mechanik quellengeprüft – 19.11.2026

- **Versionsgenaue Originalquellen:** Hersteller-Branch `vectorwing/FarmersDelight` `1.21` enthält `gradle.properties` für **Minecraft 1.21.1, NeoForge 21.1.219, Mod-ID `farmersdelight`**. `RiceBlock.java` setzt Wasser-Quellblock am Reiskörper voraus (FluidTags.WATER, fluid amount 8) und erzeugt den separaten `RicePaniclesBlock` über der reifen unteren Pflanze. Dieser obere Block hat eine eigene Wachstums-/Erntestufe und bleibt an den unteren `RICE_CROP` gebunden. Changelog beschreibt unterschiedliche Knife-/Rice-/Panicle-/Straw-Ernten und Nachwachstum.
- **Kein generischer Ersatz:** Unser `CropSticksBlock` ist noch kein Fluid-Container und enthält keinen separaten oberen Reisblock. Deshalb bleibt `farmersdelight:rice` aus Farmer-Autoerkennung und JEI-Crop-Sticks-Hilfe **ausgeschlossen**. Nicht allein per Datapack als vollständige Kompatibilität deklarieren.
- `406a33cb`, `0804ea26`: Explizite Datapack-Pflanzen mit `id=farmersdelight:rice_plant` bekommen künftig einen deutlichen `WARNING`-Hinweis im Reload-Report, dass das nur ein **bewusst generischer Override** ohne Original-Wasser-/Panicle-/Knife-Parität ist. Der explizite Pack bleibt geladen; keine harte Mod-Abhängigkeit und keine Fremd-Pflanze wird ungefragt entfernt.
- `0175251d`: JUnit `SpecialCropCompatibilityWarningsTest` prüft Warnung für Reis und kein Warnen für normale Farmer-/Pam-/Vanilla-Arten. **Nicht ausgeführt.**
- `675b2bb8`: Eigenständiger, dauerhafter Entwurf `docs/FARMERS_DELIGHT_RICE_1211_ADAPTER_DESIGN.md` mit Herstellerlinks, technischen Anforderungen, Genetik-/Wasser-/Block-/Loot-Risiken und Pflichtfällen für den späteren echten Reisadapter.
- **Nächster Reis-Schritt:** 1.21.1-Runtime-JAR-Identitäten plus CropSticks-Fluid-/Zwei-Block-Architektur klären; erst danach genetiktreue Implementation. Dies ist ein substantieller Quellabgleich samt Warnschutz/Dokumentation, **kein** fertiger Reisadapter und **kein** durchgeführter Test.


## Pam's ausdrücklich konfigurierte Selbstpflanzer – 19.11.2026

- **Neues größeres Adapter-Paket:** Der bisherige `PamAutoDiscoverySettings.ItemPair`- und `RegisteredSeedCropDiscovery.Policy`-Validator untersagte grundsätzlich `seed == produce`. Historische Pam's-`ItemPamFoodSeed`-Items können jedoch absichtlich dieselbe Quelle zum Einpflanzen und Ernten verwenden, wie Farmer's Delight Zwiebeln. Ein generischer, unbeschränkter Lebensmittelscan wäre unsicher.
- `45052bac`, `5f3ea347`: `PamAutoDiscoverySettings` akzeptiert **nur im ausdrücklich konfigurierten `item_pairs`-Eintrag** jetzt gleiche Seed-/Produce-Item-IDs. Eine Item-ID in zwei **verschiedenen** konfigurierten Pflanzen, egal in welcher Rolle, bleibt unzulässig. Standardmäßig ist keine Selbstpflanzer-Art eingetragen.
- `2d5f37c1`: Allgemeine `RegisteredSeedCropDiscovery.Policy` und Kandidaten-Konfliktauswertung erlauben denselben expliziten Sonderfall, ohne den sicheren Cross-Crop-Kollisionsschutz, die Registry-/Datapack-Vorrangprüfungen oder die `<crop>seeditem`-Auto-Inferenz aufzuweichen. Beim expliziten Paar `flaxitem` → `flaxitem` überschreibt die Auto-Inferenz kein getrenntes `flaxseeditem` für denselben Pflanzennamen.
- `8a4647b6`, `8ea0a678`: JUnit für erlaubtes ausdrücklich konfiguriertes Food-Seed, abgelehnte Cross-Crop-ID-Überlappung und Unterscheidung zur suffixbasierten Auto-Inferenz. **Nicht ausgeführt.**
- `a3f2ab56`: Neue dauerhafte Anleitung `docs/PAM_SELF_PLANTABLE_SEED_ITEM_PAIRS.md` mit einem **nicht aktivierten** Flachs-Beispiel für `data/cropgenetics/cropgenetics/pam_auto_discovery.json`, Registry-/Game-Testfällen und Datenverlust-Schutzgrenzen.
- **Kritische Grenze:** Dass ein historisches Pam's-Food-Item selbst pflanzbar war, beweist dies **nicht** für die NeoForge-1.21.1-JAR. **Noch keine** Food-Item-Paare standardmäßig freigegeben und keine Original-Erntemechanik nachgebaut. Erst JAR-Abgleich, konkrete Nutzerentscheidung und gemeinsamer Test sollen einzelne zusätzliche Pflanzen aktivieren.


## Farmer's-Delight-Zwiebel: Sondercode durch gemeinsame Pair-Policy ersetzt – 19.11.2026

- **Ausgangslage:** `FarmersDelightCropDiscovery` behandelte Kohl/Tomate bereits über `RegisteredSeedCropDiscovery`, Zwiebel aber mit separatem `addOnion`, `canAddOnion` und eigenem Konflikt-/Registry-/PlantDefinition-Bau. Nach dem sicher eingegrenzten Pam's-Selbstpflanzer-Paket kann derselbe geprüfte Pfad beide Fälle abbilden.
- `6c946448`: `FarmersDelightCropDiscovery.policy()` enthält jetzt **drei explizite Paare**: `cabbage_seeds`→`cabbage`, `tomato_seeds`→`tomato`, `onion`→`onion`; `maxPlants=3`, nur `rice` ausgeschlossen. Der Suffix `__explicit_pairs_only__` bleibt absichtlich nicht inferierbar. Der manuelle Onion-Sonderweg wurde entfernt.
- Die allgemeine Discovery prüft weiterhin tatsächliche Item-Registrierung, explizite und aktive Species-/Seed-/Produce-Claims, Mehrdeutigkeiten, gültige Farmbedingungen und Datapack-Vorrang. Alle drei Pilotpflanzen haben bereits die gleichen 8 generischen Stufen und die gleichen Wachstums-/Harvest-Grenzen wie zuvor der Onion-Fallback.
- `13eff874`: `FarmersDelightCropDiscoveryTest` auf die **drei** freigegebenen Paare aktualisiert. Tests für explizite Zwiebel-Selbstpflanzung, verweigerte Reis-/beliebige Suffix-Inferenz, falschen Namespace und Cross-Crop-Rollenkollisionen. Der frühere Test, der gleiche Seed-/Produce-IDs generell verbot, war nach dem bewusst eingeführten Selbstpflanzer-Feature veraltet und wurde entsprechend ersetzt.
- **Wichtig:** Kein Farmer's-Delight-Reisadapter hinzugefügt; Reis ist weiter Spezialfall. Keine Garantie für vollständige Farmer's-Delight-Tomaten-/Kohl-Ernteparität ohne spätere Gameplay-Abnahme. Keine Gradle-, JUnit- oder Minecraft-Ausführung.
- Sammeltest: Zwiebel normal einpflanzen und ausreißen (Herkunft `farmersdelight:onion`), Ernte-Gain und Max-Stacklimit, Zwiebel mit konkurrierendem Datapack-Seedclaim, fehlender Farmer-Mod sowie `/cropgenetics data farmersdelight`-Einträge vor/nach Reload vergleichen. Es muss höchstens eine `farmersdelight:onion_plant` aktiv sein.


## 2026-11-19 — binding priority change and add-on extraction decision

**User-approved direction:** move already-developed Pam's HarvestCraft 2 and Farmer's Delight code/data out of the core into independent add-on JARs; next manual tests are **main mod only**. Main AgriCraft/Crop Genetics work comes first; Pam's HarvestCraft 2 comes **slightly ahead** of MineColonies, and both are important add-ons. Farmer's Delight has **very low priority** and is deferred to the distant future. Preserve the core's generic third-party datapack support. Do not run Gradle/JUnit/gameplay before user requests combined test.

The detailed, acceptance-criteria-driven migration plan is in [`docs/ADDON_EXTRACTION_PLAN_2026-11-19.md`](ADDON_EXTRACTION_PLAN_2026-11-19.md). Current state as of this note: **plan committed, extraction not yet implemented; adapters and bundled Pam JSON remain in core.** Never imply otherwise in future handoffs. Phase A (generic addon registration boundary) is the next source-edit batch; Phase B (Gradle modules and atomic relocation) must follow before main-only testing.


### Präzisierung der Add-on-Reihenfolge (verbindlich)

**1. AgriCraft-Hauptmod → 2. Pam's HarvestCraft 2 (minimal höher priorisiert) → 3. MineColonies (ebenfalls wichtig) → 4. Farmer's Delight (ganz unwichtig / sehr viel später).** Die Code-Auslagerung der bestehenden Integrationen bleibt Voraussetzung für den nächsten Hauptmod-Test. Die zuvor notierte Aussage, Pam's sei geparkt, ist durch diese Entscheidung aufgehoben. Siehe `docs/ADDON_EXTRACTION_PLAN_2026-11-19.md`.


## 2026-11-19 — eigenständige Pam-/Farmer-Add-on-Module in Git angelegt

- **`d3ebfabf`:** Ein atomarer Git-Commit verschob 81 Dateien aus dem Kern nach `neoforge-port/addons/pam-hc2/` und `neoforge-port/addons/farmers-delight/`, einschließlich Java-Adapter, Adapter-Tests, aller gebündelten Pam-Pflanzen/Mutationen/Auto-Policy und der Farmer-Rich-Soil-Datensätze. Beide Teilprojekte haben eigene `build.gradle`, `@Mod`-Entrypoints und NeoForge-Metadaten.
- Der Kern ruft weder `PamCropAutoDiscovery` noch `FarmersDelightCropDiscovery` direkt auf. Das allgemeine `CropDiscoveryAddonRegistry`-Hook ist verbunden; jeder Add-on-Entrypoint registriert sich bei der Mod-Initialisierung selbst.
- Per **statischer Git-Baumprüfung** liegen keine Pam/Farmer-Adapter- oder Datapack-Dateien mehr im Hauptmod-Quellressourcenordner. Keine Gradle-/JUnit-/Minecraft-Ausführung (Nutzerwunsch) — **kompilierbar/lauffähig NICHT bestätigt**.
- Noch core-seitige reine Anzeige-/Diagnose-Helfer mit Pam-/Farmer-ID-Strings: JEI Seed Guide, Artennamen-Fallback, Data-Command-Unterbefehle, Report-Statistiken und Farmer-Reiswarnung. Sie haben keine optionale Java-Klassenabhängigkeit; bei weiterer Bereinigung beachten.
- **Handoff bei Kontextwechsel:** `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md` mit Mapping, Abhängigkeiten, bekannten Risiken und erst später auszuführenden Build-/JAR-/Gameplay-Checks.
- Verbindliche Feature-Priorität bleibt: **AgriCraft-Kern > Pam's (knapp vor MineColonies) > MineColonies >>> Farmer's Delight.** Hauptmod isoliert testen erst nach vereinbarter Sammeltestfreigabe.


### Nachtrag: JEI-Anzeigen ebenfalls Add-on-gesteuert

- Hauptmod-JEI zeigte vorher die bekannten Pam-/Farmer-Saatgutkandidaten an, selbst wenn nur die **Originalmod ohne unser Add-on** installiert war. Das wäre nach der Auslagerung falsch.
- `86f50d10`, `cf454d4f`: generisches `CropSeedGuideRegistry` eingeführt; `CropGeneticsJeiPlugin` liest nur noch Kandidaten von tatsächlich registrierten Add-ons. Die bisherige harte `OptionalSeedGuidePolicy` und ihr Test wurden durch den Registry-Mechanismus und dessen neue Tests ersetzt.
- `dd72644b`, `c82d0c43`: Pam- bzw. Farmer-Add-on registrieren die eigenen JEI-Kandidaten aus ihrem Mod-Entrypoint. Farmer's Reis bleibt ausgeschlossen. Vorhandensein von Seed **und** Produkt wird im Kern weiterhin geprüft.
- **Keine Tests/Builds ausgeführt.** Details sowie verbleibende rein anzeigeseitige Core-IDs: `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md`.


### Nachtrag: Diagnosebefehle nur mit Add-on

- `af5a67ad`: `CropDataCommandExtensions` als generische, sichere Registrierung für zusätzliche `/cropgenetics data ...`-Unterbefehle in der Hauptmod angelegt.
- `0c5392c7`, `d9bb1d39`, `32626726`, `d47f6bee`: Die Pam- und Farmer-spezifischen Diagnoseausgaben und ihre Befehlsregistrierung leben jetzt in den **separaten Add-on-Modulen**. Sind die Add-ons nicht installiert, wird deren Unterbefehl nicht registriert.
- `a684c6d9`: Hauptmod-Befehl zeigt nur allgemeine Datenstatus-Informationen und registrierte Erweiterungen; statisch überprüft ohne Pam-/Farmer-Literale.
- `1b84ddec`: neue Unit-Tests für Add-on-Befehlsregistrierung, noch **nicht ausgeführt**.
- Ausführliche Übergabe: `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md`. Die bisherige Abmachung (keine Gradle-/JUnit-/Gameplay-Tests vor Freigabe) gilt weiterhin.


### Nachtrag: Mod-spezifische Artennamen aus dem Kern entfernt

- `c02122a3`, `859ed3ee`: Erweiterungsregister `CropSpeciesDisplayRegistry` hinzugefügt. `CropSpeciesNames` nutzt nur noch registrierte Add-on-Zuordnungen; keinerlei fest kodierte Pam-/Farmer-Namensfälle mehr.
- `7f6d7ae1`, `2d619211`: Die bisherigen Namensregeln (Pam inkl. `mustardseeds`, Farmer nur Kohl/Tomate/Zwiebel und ausdrücklich nicht Reis) liegen nun in eigenen Add-on-Klassen.
- `e6b073ed`, `33a27dc9`: Registrierung im jeweiligen Add-on-Entrypoint.
- `86c569bd`, `919e76e5`, `9a78a8a9`: passende JUnit-Regressionen ergänzt/angepasst; **nicht ausgeführt**.
- Handoff `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md` fortgeschrieben. Die Hauptmod bleibt durch die Split-Module auf AgriCraft-Kernfunktionen fokussiert. Prioritäten: AgriCraft > Pam's (knapp) > MineColonies >>> Farmer's Delight.


### Nachtrag: Report-Statistiken neutralisiert

- **`55674380`** (atomarer 5-Dateien-Commit): `CropReloadReport` nutzt jetzt allein `AutoDiscoveryStats` und `autoDiscoveryStats(prefix, configPath)` statt fest eingebauter Pam-/Farmer-Methoden oder spezieller Pfade.
- Die jeweiligen Add-on-Diagnoseklassen setzen ihre Präfixe selbst. Pam's behält die bisherige Anzahl explizit geladener Pflanzen aus allen Mods; Farmer's behandelt den bisherigen `discovery_suspended`-Marker.
- Kern-Regressionstests verwenden nun neutrale `auto_example/`-Daten; Farmer-Addon-Tests angepasst. Statische Prüfung der Referenzen erfolgt; **keine Ausführung** von Build/JUnit/Minecraft.
- Übrig bleibt absichtlich die schützende Reis-Warnung für explizit per Datapack angelegte Farmer's-Delight-Reiskulturen, solange keine Daten-basierte, modunabhängige Warnstrategie existiert. Details `docs/ADDON_MODULE_SPLIT_HANDOFF_2026-11-19.md`.


## Statische Buildskript-Korrektur – 19.11.2026

- `797d3efe`: Pam-Add-on-Metadaten verwenden nun einen korrekt quotierten Groovy-String für `Pam's HarvestCraft 2`.
- `2ee24a86`: Farmer's-Delight-Add-on-Metadaten verwenden nun einen korrekt quotierten Groovy-String für `Farmer's Delight`.
- Ursache: unescaped Apostrophe innerhalb eines Groovy-Strings mit einfachen Anführungszeichen hätten die Konfiguration der Add-on-Teilprojekte bereits beim Einlesen der `build.gradle`-Dateien abbrechen lassen.
- Dies ist ein **statisch bestätigter Syntaxfehler**, kein Ergebnis einer Gradle-Ausführung. Keine Tests oder Builds gestartet. Beim freigegebenen Gesamttest weiterhin zuerst Hauptmod `:test :build`, danach Add-on-Builds gesondert.
- Die alte Spieltest-Beobachtung zu normalen Karottensamen beim Crossbreeding bleibt **ungeklärt**. Codepfad für unveränderte registrierte Samen (`Genome.basic`), ausgereifte Eltern und Kreuzungswahrscheinlichkeit wurde angesehen, ohne daraus vorschnell eine Gameplay-Änderung abzuleiten.


### Nachtrag: Core-Preflight und Testfreigabe-Gate

- `7ec175d7`: Neuer reproduzierbarer, nach Modulen getrennter Prüfplan `docs/MAIN_MOD_TEST_GATE_2026-11-19.md` einschließlich konkreter normaler Karotten-/Weizensamen, 2-/3-/4-Eltern-Kreuzungen und der schwächeren Elternreihe, Analyzer, Seed Chest, Journal, Harvest-Limit, JEI sowie dediziertem Server.
- Öffentliche Add-on-Schnittstellen, Konstruktor-Registrierung, eigene Mod-Metadaten und `compileOnly`-Modulbezug wurden statisch geprüft. Dabei kein weiterer gesicherter Quellsyntaxfehler nach den Groovy-String-Korrekturen festgestellt; die tatsächliche Laufzeit-Klassenauflösung ist weiterhin ungetestet.
- Explizite Test-Gates: erst `:test :build` der Hauptmod, danach nur bei Freigabe separate `:pam-hc2:build :farmers-delight:build`. **Keine** Gradle-, JUnit- oder Minecraft-Ausführung in diesem Arbeitspaket.


### Nachtrag: Rezeptkorrektur und Saatgutpfad (19.11.2026)

- `639b5b79`: Das Seed-Analyzer-Rezept enthielt noch zwei Vanilla-Stöcke, Glasscheibe, Steinplatte und Holz. Gemäß der früheren Vorgabe wurde es auf **genau sechs Crop Sticks und keine weiteren Zutaten** reduziert (förmiges 3×3-Muster mit sechs Belegungen).
- Das Seed-Chest-Rezept nutzt bereits acht Crop Sticks um eine normale Truhe und enthält keine Planken. Es wurde deshalb nicht geändert.
- Statische Überprüfung von `CropItems`, `CropSticksBlockEntity`, `SeedAnalyzerBlockEntity`, `SeedChestBlockEntity` und `CropHarvest`: individuelle Genetik wird über `GENOME` in den genetischen Saatgutstapeln/Blockdaten gehalten; genetische Seeds sind auf Stackgröße 1 begrenzt; die Ernte bündelt Produkte auf höchstens einen normalen Itemstack pro Produkttyp. **Diese Feststellungen sind Quelltextbefunde, kein Gameplay-Test.**
- Keine Gradle-, JUnit- oder Minecraft-Ausführung; als spätere Prüfpunkte Analyzer-Rezeptanzeige in JEI und tatsächliches Crafting aufnehmen.


### Nachtrag: Rezeptverträge, Analyzer/Chest/Journal-Integration (19.11.2026)

- `679e3408`: Neuer JUnit-Ressourcentest `CropCraftingRecipeContractsTest`: sichert dauerhaft die tatsächlichen Rezeptdateien (nicht duplizierte Hardcoded-Rezepte), einschließlich sechs Crop Sticks als einzigen Analyzer-Zutaten, acht Crop Sticks + eine Truhe für die Seed Chest, sechs Crop-Sticks-Ausgabe aus vier Vanilla-Sticks sowie Journal aus Buch + Crop Stick.
- Quelltext-Audit: `SeedAnalyzerBlock` verbindet seine neun Inventarslots mit den 27 Slots der **ersten direkt horizontal angrenzenden** `SeedChestBlockEntity` zu einem 36-Slot-`CompoundContainer`; ohne Kiste bleibt ein 9-Slot-Menü. Die Seed Chest kann eigenständig geöffnet werden. Dies erfasst bewusst nur *eine* Nachbarkiste, keinen Verbund mehrerer Kisten.
- Server-Seite `SeedAnalyzerBlockEntity.onStored`: Der Seed wird bei Aufnahme analysiert, das konkrete `Genome` bleibt unangetastet; ein eingelegtes Journal erhält die entdeckte Spezies und bekannten Mutationstripel, sofern deren Arten vollständig erforscht sind. Der Journal-Datenbestand reist mit dem Itemstack statt globaler Spieler-Speicherung.
- Inventar-/Droppfade statisch gelesen: Seed Analyzer und Seed Chest droppen ihre Inhalte bei Blockabbau. Anwenden von `setItem` kopiert den Inputstack, bevor die Stackgrenze durchgesetzt wird. Menu-/Block-Interaktion und Persistenz müssen beim späteren Spieltest geprüft werden.
- **Kein** Gradle/JUnit/Minecraft ausgeführt. Den neuen Ressourcentest erst mit der genehmigten Core-Testphase starten. Bekannte offene Reproduktion: normales Karotten-Crossbreeding und Analyser-Journal-Netzwerkverhalten.


### Nachtrag: Welt-NBT, Inventar- und Journal-Persistenz (19.11.2026)

- **`d6b6c3d2`:** Vorhandenen `UnavailableModGenomePersistenceTest` um zwei wichtige Regressionen erweitert: `GenomeCodecs.GENOME` muss Growth/Gain/Strength (1..10) samt Pflanzen-ID im **tatsächlichen NBT-Weltspeicherformat** unverändert roundtrippen; alte fünf-Paar-NBT-Genotypen müssen auf die drei sichtbaren Werte migriert und beim erneuten Speichern ohne Fertility/Mutativity geschrieben werden. Tests nur hinzugefügt, **nicht ausgeführt**.
- Statischer Welt-Save-Pfad `CropSticksBlockEntity.saveAdditional/loadAdditional`: setzt `GrowthStage`, `SeedAnalyzed`, `OriginalSeed`, `TotalGrowthStages`, Genom-NBT, Fruchtbereitschaft/Owner-Token sowie diagnostische Counter; das Client-Update-Tag entfernt die privaten Quell-/Diagnosedaten und maskiert unanaly­sierte Genwerte. Der tatsächliche Server-Neustart bleibt als späterer Gameplay-Test offen.
- `SeedInventoryBlockEntity` verwendet `ContainerHelper.saveAllItems/loadAllItems` für Analyzer und Chest. `SeedJournalItem` speichert Entdeckungen/Mutationstipps als Datenkomponenten am konkreten Buchstack; die Client-Buchseite liest eine Kopie der übermittelten Itemdaten, ohne eigenen Server-State.
- Erkenntnis für Spieltest: Journal in Analyzer einlegen, analysierte Seeds nacheinander verwenden, Journal entnehmen, Welt schließen/neu öffnen und Ergebnisse vergleichen. Ebenso einzelnes Seed-Stack samt verdecktem Genom vor/nach Neustart und bei Abbau der Crop Sticks kontrollieren. Keines dieser Szenarien ist aktuell gespielt worden.


### Nachtrag: Multiplayer-/Inventar-Überprüfung vorbereitend (19.11.2026)

- `7062c32b`, korrigiert in `434a50dc`: Neuer detaillierter, für externe Tester ausführbarer Katalog `docs/MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md` mit 14 Fällen zu Zweispieler-Zugriff, Analyzerslot/Journal, benachbarter Chest, zeitgleichem Entnehmen, Blockabbau bei geöffnetem Menü, Weltneustart, Gen-Privatsphäre und normalen/gezüchteten Seeds.
- Statischer Codepfad geprüft: Analyzer/Chest verwenden Vanilla-`ChestMenu`, `CompoundContainer`, `stillValid` mit Blockentity-Identität/Entfernung, serverseitiges Inventar und `Containers.dropContents` beim Abbau. Die world-watch-Pakete des Analyzers sind auf Item-/Pflanzenidentität und Belegungszustand begrenzt; echte Genwerte bleiben in serverseitig gespeicherten Itemstacks. Crop Sticks maskieren unanaly­sierte Stats für Client-Watcher.
- **Keine** Multiplayer-, Gradle- oder JUnit-Ausführung; eventuelle Duplikation bei simultanen Spielern und invalidierte Menüs nach Blockabbau sind ausdrücklich *noch unbestätigte Risiken*, keine als behoben gemeldeten Fehler.


### Nachtrag: Hauptmod-UI-Sprachschlüssel und Core-Backlog (19.11.2026)

- `31085815`: Neuer `CropJournalLocalizationContractTest` liest **die echten** `en_us.json`- und `de_de.json`-Ressourcen, vergleicht den vollständigen Schlüsselbestand und prüft alle dynamisch zusammengesetzten Journal-Guide-Texte (3 Seiten × 6 Zeilen), Journal-Detail-/Mutationsseiten und JEI-Infotexte auf vorhandene nichtleere Übersetzungen. Test geschrieben, **nicht ausgeführt**.
- Statische Sprachprüfung vor diesem Commit: beide Locale-Dateien hatten 63 gleichnamige Einträge; Journal- und JEI-Schlüssel vorhanden. Ein späterer echter Client-Lauf muss Lesbarkeit/Zeilenumbrüche, Textlängen und dynamische Journal-Seitennavigation prüfen.
- Kern-Quelltriage: `CrossBreeding` entspricht weiterhin den vereinbarten 2-/3-/4-direkt-Eltern- und 3+-Reihenregeln. Ohne reproduzierbaren Gegenbefund keine Zufallswahrscheinlichkeiten umgestellt. Original-Testbefund `STK-04/GEN-01` (normale Karottensamen) bleibt offen. Bereits dokumentierte visuelle Nachtests: Stage-0, Hud-Overlay/Identify, Seed-Icons, Kürbis-/Melonenfruchtzyklus.
- Keine Add-on-Features aktiviert, keine Gradle-/JUnit-/Minecraft-Ausführung. Nächste Schritte erst nach Freigabe des gebündelten Hauptmod-Testlaufs anhand des bestehenden Plans.


### Nachtrag: Interne Gourd-Frucht und Mutterpflanze gemeinsam rendern (19.11.2026)

- **`8c063355`**: Konkreter Rendering-Fehler in `CropSticksRenderer` korrigiert. Sobald `FRUIT=true` und interne Frucht ohne Legacy-Nachbar-Owner vorlag, zeichnete der BER zwar die verkleinerte Melone/den Kürbis, beendete die Methode aber mit `return`, bevor die reife Mutterpflanze gezeichnet wurde. Dieses vorzeitige `return` wurde entfernt. Die Frucht wird weiterhin innerhalb der Sticks modelliert, anschließend wird der reife Stem/Plant-Layer gezeichnet.
- `df649998`, `3ee51fe8`: Begleitende Renderer-Kommentare für interne und ältere externe Gourd-Früchte an das tatsächliche Verhalten angepasst.
- Der vierbeinige Crop-Sticks-Block ist ein eigener Blockmodellpfad und bleibt unverändert. Es wurde **keine** Wachstumsgeschwindigkeit, Reife, Dropmenge, NBT-Ownership oder Fruchtchance verändert.
- Verifikation erst nach Testfreigabe: Gourd reifen lassen, erste interne Frucht sehen; gleichzeitige sichtbare reife Pflanze prüfen; Frucht mit Rechtsklick ernten; zweite Frucht abwarten; blockabbauen/Save-Restart bei fruchttragender Pflanze und alte physische Frucht-Saves prüfen. Die tatsächliche Frucht-Stängel-Überlagerung und Farbgebung bleiben visuell unbestätigt, kein Build/Client gestartet.


### Nachtrag: Seed-Icons und alle 16 Pflanzenvisuals statisch abgesichert (19.11.2026)

- **`af8f262f`, `279ac824`:** In `melon.json` und `pumpkin.json` die veraltete zweite Frucht-Texturschicht aus dem letzten Wachstumsframe entfernt. Seit der Umstellung auf echte verkleinerte Vanilla-Fruchtblöcke innerhalb der Crop Sticks zeichnet `CropSticksRenderer` nur noch den ersten Stem-Layer; die alten `melon_side`-/`pumpkin_side`-Einträge wurden nicht verwendet. Alle acht Frames zeigen weiterhin den Stem; die Frucht selbst erscheint ausschließlich bei `FRUIT=true`. **Keine** Genetik-, Wachstums- oder Ernteberechnung verändert.
- **`fe005b27`:** Neuer `BuiltInCropVisualContractTest` prüft alle 16 Saatgutarten gegen je eine vorhandene Pflanzenvisual-Datei, die korrekte Pflanzen-ID, eine erlaubte Geometrie, acht Stufen mit einer Vanilla-Atlas-Textur pro Stufe sowie die einfache Stem-Struktur von Melone/Kürbis. Damit bleiben Wachstumsstufen und das Gourd-Fruchtmodell später auseinandergehalten.
- **`c0e92aa2`:** `SeedSpeciesCatalogTest` um die konkreten 15 Item-Texturzuordnungen erweitert; die sechzehnte Sorte Weizen wird bereits im bestehenden Test über das Basismodell geprüft. Es wird bewusst die vorhandene Vanilla-Darstellung festgehalten: So hat etwa die Süßbeere als Item kein separates Vanilla-Samen-Icon und nutzt `minecraft:item/sweet_berries`. Ein ästhetisch neues Custom-Seed-Icon ist damit **nicht** umgesetzt.
- Visuelle Nachtests nach Freigabe: Je einen Seed aller 16 Arten im Inventar/JEI prüfen; besonders Beere, Pilze, Kaktus und Blumen von ihren Normalitems unterscheiden können; Weizen/Karotte/Kartoffel/Rote Bete in Stufe 0 und 7 vergleichen; bei Melone/Kürbis fruchtlosen und fruchttragenden reifen Stem beobachten. **Statisch geschrieben, Gradle/JUnit/Minecraft weiterhin nicht ausgeführt.**


### Nachtrag: Seed-Analyzer-Rechtsklick und Journal-Bedienung (19.11.2026)

- **`eece57c7`, `d74a5d1d`, `8e4c595a`:** Konkrete Client-/Server-Abweichung beim platzierten Seed Analyzer behoben. `SeedAnalyzerBlockEntity.canPlaceItem` lässt im Client zur Vorhersage der **Menü-Slots** bisher beliebige Items passieren, weil der Client den Server-Datapack-Katalog nicht besitzt. Der direkte `useItemOn`-Rechtsklick verwendete dieselbe breite Prüfung: Mit z. B. einem Diamanten oder einem Journal in der Hand konnte der Client erfolgreiches Seed-Einlegen melden, obwohl der Server stattdessen das Menü öffnen musste. Für den **direkten Block-Rechtsklick** gilt nun `SeedAnalyzerInteractionRules`: erkennbarer Vanilla-Samen oder eigener genetischer Samen mit Genom; modifizierte normale Items und andere Gegenstände werden nicht lokal als Einlegen verbraucht. Unbekannte optionale Mod-Samen bleiben serverautoritativ; ihre direkte Einlage funktioniert weiterhin auf dem Server, auch ohne Client-Vorhersage. Die tolerante Menü-Vorhersage für optional registrierte Samen bleibt unverändert.
- **`18c28119`:** Neuer isolierter `SeedAnalyzerInteractionRulesTest` deckt Diamant, Stock, Journal, Vanilla-Samen, veränderte Vanilla-Stacks, genetische Samen mit/ohne Genom sowie fremde Datapack-Samen ab. Reine Regeln; kein Client/World-Setup, bisher **nicht ausgeführt**.
- **`4e1ee659`:** `SeedJournalScreen` markiert entdeckte Pflanzeneinträge nun beim Darüberfahren. Hervorhebungs- und Klickrechteck stimmen überein. Lange lokalisierte Namen werden auf die Buchspalte begrenzt und erscheinen bei Hover vollständig als Tooltip; auch die Pflanzen-Detailüberschrift wird begrenzt. Es werden weiterhin **nur freigeschaltete** Forschungs-/Mutationsdaten gezeigt.
- **`ca830917`:** Ein versehentliches literales `\\n` in einem Kommentar der Seed Chest bereinigt (keine Funktionsänderung).
- Nach Freigabe im Client prüfen: Analyzer leer + Rechtsklick mit Diamant/Stock/Journal (muss Menü ermöglichen, nicht scheinbar einsetzen); Rechtsklick mit Vanilla-Saatgut und mit einem eigenen genetischen Seed (echte Einlage); Sneak + Journal (Journal-Slot); unbekannter Mod-Samen (Server entscheidet korrekt); Analyzermenü mit/ohne angrenzende Seed Chest; Forschungseinträge mit Maushover/Klick und lange durch Datapack eingeführte Pflanzennamen. Besonders testen, ob ein Mod-Samen ohne lokale Vorhersage trotzdem korrekt serverseitig eingelegt wird.
- Weiterhin **keine** Gradle-/JUnit-/Minecraft-Ausführung und kein allgemeiner Release/Test-Freigabewechsel. Die Visual- und Tooltip-Anpassung ist nicht im laufenden Spiel bestätigt.


### Nachtrag: Kombiniertes Analyzer-/Chest-Menü mit getrennten Stacklimits (19.11.2026)

- **Fund durch statische Prüfung:** Das alte angrenzende 4-Reihen-Menü wurde über `ChestMenu.fourRows(..., new CompoundContainer(analyzer, chest))` gebaut. Der Analyzer speichert maximal **1** Item pro Slot, die Seed Chest normale Stacks bis zum Item-Maximum. Im zusammengefassten Container ließen sich diese unterschiedlichen *Slot*-Grenzen nicht explizit abbilden: Insbesondere Shift-Klick eines 64er-Normalsamenstapels in den oberen Analyzer-Slot konnte dazu führen, dass das Menü mehr als ein Item transferieren will, die Blockentity beim Speichern aber auf eins begrenzt. Das ist ein **identifiziertes Verlustrisiko**, noch kein reproduzierter Spieltest-Fehler.
- **`a75c8f41`, `8a2cb522`, `ddd0dd32`:** Neues serverseitiges `SeedAnalyzerCombinedMenu` verwendet den bekannten Vanilla-Menütyp `GENERIC_9x4` und exakt das bestehende 36-Slot-Layout. Es legt die ersten neun Slots einzeln am Analyzer mit Maximalgröße **1** an (tatsächlich nur Seedslot 0 und Journalslot 1 befüllbar; sieben Füllslots bleiben gesperrt). Die weiteren 27 Slots sind direkt an die Seed Chest gebunden und erlauben reguläre **64** bzw. das strengere jeweilige Itemmaximum. Die Spieler-Slots entsprechen dem Vanilla-ChestMenu-Layout. Der neue Menütyp braucht clientseitig keine eigene GUI-Registrierung. `quickMoveStack` bewegt Stacks mit den jeweiligen Slotgrenzen; `stillValid` verlangt gleichzeitig gültige Analyzer- und Chest-Blockentities.
- **`cbeecb46`:** Neuer `SeedAnalyzerCombinedMenuSlotTest` prüft die 9×1-/27×64-Struktur und ungültige Slotindizes. Nur geschrieben, noch **nicht ausgeführt**.
- **`cf4f06b8`:** Externes Multiplayer-Testdokument präzisiert die sieben deaktivierten Analyzer-Füllslots und ergänzt **MP-15 bis MP-17**: 64er-Shift-Klick ohne Itemverlust, Cursor-Klick und gleichzeitiger Zweispielerzugriff, blockabgebauter Analyzer während kombinierter Menübenutzung. Alle erwarteten Mengen inklusive Reststapel explizit festhalten.
- **Wichtige Verifikationsgrenze:** Der Server nutzt seine eigenen gebundenen Slots, der Client zeichnet weiterhin die standardmäßige Vanilla-Vier-Reihen-Oberfläche. Kurzzeitige lokale Vorhersageeffekte beim Drag-/Shift-Klick sind deshalb möglich und müssen im späteren Gameplay-Test beobachtet werden. Ebenso ungeprüft: Menüschluss beim Abbau genau während anderer Spieler klicken. Kein Gradle/JUnit/Minecraft gestartet.


### Nachtrag: Seed-Chest-Datenschutz und NBT-Snapshot-Ersatz (19.11.2026)

- **`e3918534`:** `SeedChestBlockEntity.getUpdateTag` explizit auf einen **leeren** öffentlichen Blockentity-Update-Tag beschränkt. Die Chest hat keinen Inhalt-Renderer und muss weder Genome noch Journal-/Inventar-Komponenten an normale Chunk-/Blockzuschauer übertragen. Nur Spieler, die das Inventar tatsächlich öffnen, erhalten dessen Slot-Inhalte über die autorisierte Vanilla-Menüsynchronisierung. Der **persistente** Welt-Speicherpfad `saveAdditional` bleibt vollständig und unverändert. Dies adressiert einen statisch erkannten potenziellen Privacy-Leak; ein tatsächlicher Netzwerk-Sniff wurde nicht durchgeführt.
- **`21941676`, `4ef62f22`, `4389a297`:** `SeedInventoryBlockEntity.loadAdditional` entfernt vor `ContainerHelper.loadAllItems` alle vorherigen Werte aus der bestehenden `NonNullList`. Der NBT-Helper stellt nur serialisierte belegte Slots wieder her; beim Laden eines neuen/sparsamen Snapshots in eine wiederverwendete Blockentity dürfen nicht erwähnte Slots keine alten Seeds oder Journale behalten. Die Rücksetzung erfolgt nur im Ladepfad; Menü-Entnahme, Welt-Save und `getUpdateTag` bleiben getrennt.
- **`6eeae2d6`:** Neuer `SeedInventoryRestoreRulesTest` überprüft das vollständige Leeren vorher gefüllter 27er-Chest- und 9er-Analyzer-Listen bei unveränderter Größe. Test ergänzt, **nicht ausgeführt**.
- **`12129e3a`, `146c82ac`:** Externe Testdokumentation auf **19** Multiplayer-/Inventar-Fälle erweitert, darunter MP-18 (kein Zuschauerzugriff auf Chest-Item-Komponenten ohne Öffnen) und MP-19 (entleerte Slots nach wiederholtem Weltneustart bleiben leer). Testanleitung hält fest: Seed Chest speichert Seeds, nicht Journale; Journal separat im Analyzer.
- Verifikation nach expliziter Freigabe: Zwei Spieler + Packet-/NBT-Inspektion bzw. Privacy-Sichtprüfung, gefüllte und geleerte Slots über zwei kontrollierte Neustarts, optional entfernte Mod-Arten, gespeicherte/genetische Seed-Daten und Forschungsjournal. **Weder Gradle/JUnit noch Minecraft gestartet.** Kein Nachweis über tatsächlich gesendete Pakete oder laufzeitliches Verhalten.


### Nachtrag: Journal-Mutationshinweise nach Datapack-Reload aktuell halten (20.11.2026)

- **Ausgangslage:** `SeedAnalyzerBlockEntity.onStored` aktualisierte die bekannten Mutationshinweise eines Forschungsjournals nur dann, wenn Seed oder Journal in einen Analyzer-Slot gesetzt wurden. Nach einem Server-`/reload` konnte ein **bereits eingelegtes** Journal deswegen noch Hinweise auf entfernte/angepasste Mutationsdefinitionen behalten, bis es erneut eingelegt wurde.
- **`66fd3073`, `38a5e596`:** `SeedAnalyzerBlock` besitzt jetzt einen ausschließlich serverseitigen, über Position und Spielzeit versetzten Ticker (100 Ticks). `SeedAnalyzerBlockEntity.reconcileJournalMutations` gleicht bei einem vorhandenen Journal nur die bekannten Mutationshinweise mit dem aktuellen Datapack-Snapshot ab. `setChanged` erfolgt nur bei tatsächlich abweichender Hinweisliste. Der Samengenotyp, Analyzer-Slot, Journal-Speziesentdeckungen und Gene werden **nicht** neu berechnet oder gelöscht.
- **`14653a27`:** `JournalMutationRulesTest` erweitert: Entfernen einer Mutation durch Datapack-Änderung entfernt ihren aktiven Hinweis, aber nicht die bereits entdeckten drei Pflanzen; das Wiedereinspielen der Definition macht den vollständig bekannten Hinweis wieder sichtbar. Test geschrieben, **nicht ausgeführt**.
- **`dd7956e1`:** Externes Inventar-Testdokument ergänzt **MP-20**: Forschungsjournal im Analyzer belassen, Datapack-Mutation ändern, `/reload`, mindestens sechs Sekunden warten, Journal entnehmen, Buch prüfen und Datapack wiederherstellen. Eine Weltkopie und Sicherung der Datapacks sind für den späteren Test nötig.
- **Einschränkung:** Außerhalb des Analyzers befindliche Journale erhalten absichtlich keine periodischen Inventar-Mutationen; die Rekonstruktion geschieht beim nächsten Einlegen. Keine automatischen oder Minecraft-Tests ausgeführt. Multiplayer-Synchronisation und tatsächliches Datapack-Reload-Verhalten bleiben unbestätigt.


### Nachtrag: Crop-Sticks-Interaktion, Bodenbehandlung und manuelle Ernte (20.11.2026)

- **`39686096`:** In `CropSticksBlock.useItemOn` quittiert die Clientseite Knochenmehl/Wassereimer nicht mehr pauschal mit `SUCCESS`, wenn die tatsächliche Bodenbehandlung ausschließlich serverseitig geprüft wird. Nur der Server kann bei gültigem Bodenprofil und tatsächlicher Änderung eine erfolgreiche Behandlung melden und das Survival-Item verbrauchen. Bei fehlendem Profil beziehungsweise unveränderten Maximalwerten greift der normale `PASS_TO_DEFAULT_BLOCK_INTERACTION`-Pfad. Die Clientvorhersage muss im späteren integrierten Test beobachtet werden.
- **`eae893b4`:** `CropSticksBlock.useWithoutItem` verarbeitet manuelle Ernte und das Entfernen leerer Kreuz-Sticks nur noch, wenn **Haupt- und Nebenhand leer** sind. Damit löst ein abgewiesener Held-Item-Einsatz (zum Beispiel Knochenmehl/Wassereimer auf ungeeignetem Boden) nicht unbeabsichtigt die anschließende Ernte aus. Dies ist eine bewusst strengere UX-Bedingung und wird im Gameplay-Test auf Bedienbarkeit geprüft.
- Die bestehende Obergrenze **ein Itemstack je unterschiedlichem Ernteprodukt pro Erntevorgang** bleibt erhalten. Die Vererbungs-/Mutationswahrscheinlichkeit und Gourds-Wachstumszyklen wurden in diesem Paket nicht verändert.
- **CR-01 bis CR-05** im externen Fallkatalog `docs/MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md` dokumentieren Fehlbehandlung, gültige Bodenpflege, normale Ernte, Kürbis-/Melonenfrucht und das Entfernen eines leeren Kreuzes.
- **Status:** Nur statische Code-Änderungen und Testvorbereitung. Keine Gradle-, JUnit- oder Minecraft-Testausführung; Freigabe für einen gebündelten Durchlauf steht weiterhin aus.


### Nachtrag: Boden-Nährstoffabrechnung auf erfolgreiche Wachstumsereignisse begrenzt (20.11.2026)

- **`eff009e4`**: `CropSticksBlock.randomTick` ruft `SoilDynamics.onGrowth` nur noch auf, wenn `crop.advanceGrowth` tatsächlich eine Stufe hinzugefügt hat. Ein bestandener Zufallswurf allein reicht nicht aus, um Boden-Nährstoffe zu verbrauchen.
- **`2a74451f`**: Sowohl der direkte Knochenmehlpfad `useItemOn` als auch `BonemealableBlock.performBonemeal` berücksichtigen bei einem **erfolgreichen** Stufenanstieg dieselbe konfigurierte Nährstoff-Dynamik. Fehlversuche, Reifestufe und fehlende Wachstumsbedingungen belasten den Boden nicht. Direkte Knochenmehl-Interaktion konsumiert weiterhin nur bei Erfolg genau ein Item in Survival.
- Der spätere manuelle Testkatalog enthält **CR-06** (natürlicher Tick/Nährstoffabzug) und **CR-07** (Knochenmehl und mod-kompatible Wachstumstools). Für deterministische Versuche wird im **kopierten** Datapack `data/cropgenetics/cropgenetics/soil_dynamics.json` `growth_nutrient_chance` auf `1` gesetzt; danach Originalkonfiguration wiederherstellen.
- **Noch ungeprüft:** tatsächliche Client-/Server-Interaktion, automatischer Bodenabzug, Knochenmehl-Verbrauch und Kompatibilität externer Wachstumstools. Keine Gradle-/JUnit-/Minecraft-Ausführung, Testfreigabe steht aus.


### Nachtrag: Verlustfreie Seed-Pflanzung und belastbare Herkunftskennung (20.11.2026)

- **`4257d6a0`**: `CropSticksBlockEntity.plant` prüft nun auch beim Einpflanzen `CropSeedSourceSafety.canConvertWithoutExternalDataLoss`. Eine Vanilla-/Mod-Saat oder dedizierte genetische Saat mit fremden, nicht rekonstruierbaren Data Components (z. B. Custom Name oder Addon-Komponenten) wird **nicht** konsumiert. Denn späteres Ausgraben konvertiert zwingend in `cropgenetics:genetic_seed` und könnte diese Fremdkomponenten sonst verlieren.
- **`d62a95f7`**: Bei einer gültigen normalen Vanilla-/Mod-Saat ist für `OriginalSeed` ausschließlich die **tatsächlich eingesetzte Item-ID** maßgeblich. Ein auf dem Quellstack hinterlegter abweichender, veralteter oder manipulierter `ORIGINAL_SEED`-Wert darf die spätere Rückwandlung nicht auf ein fremdes Item lenken. Nur unser eigener dedizierter genetischer Seed bewahrt seinen explizit gespeicherten historischen Ursprung.
- **`87eb04c0`**: Die Client-Erfolgsprognose für bereits genetisch markierte Seeds berücksichtigt die gleiche Quellkomponenten-Prüfung. Sonst würde der Client eine Pflanzung bestätigen, die der Server zu Recht ablehnt.
- **`9ee7a48e`**: Ergänzte externe Testfälle **CR-08** (fremde Item-Komponenten überleben abgewiesene Pflanzung), **CR-09** (Herkunft gewöhnlicher Mod-Seeds entspricht tatsächlich eingesetztem Item), **CR-10** (dedizierter genetischer Seed behält seinen expliziten historischen Ursprung, auch nach Mapping-Entfernung).
- Einschränkung: Das Vorbereiten von Seeds mit manipulierten Komponenten gehört ausschließlich in eine **kopierte Testwelt**. Die serverseitigen Mapping-/Provenienzregeln wurden statisch überprüft, **nicht** durch Gradle, JUnit, Minecraft oder Multiplayer ausgeführt.

### Fortsetzung 20.11.2026 – Schutz beim Rückwandeln genetischer Seeds

- Quellcode gegen Branch-HEAD `70c3ffb044c861de5e9e8d64bcad876f171f8b9c` geprüft. `CropItems.recoverOriginalSeed` ließ zuvor fremde Data Components auf dem genetischen Seed ohne Verlustprüfung verschwinden, weil das Rezept ein neues Original-Item herstellt. Die Rückwandlung prüft nun dieselbe strenge Sicherheitsregel wie andere Seed-Konvertierungen und verweigert das Rezept bei fremden Komponenten.
- Regressionstestquelle `CropSeedSourceSafetyTest` um entfernte Komponenten erweitert; manueller Rezepttest **CR-11** hinzugefügt. Die absichtliche Entfernung von Genomdaten beim freiwilligen Rückwandeln einfacher genetischer Seeds bleibt unverändert.
- **Nicht ausgeführt:** Gradle, JUnit, Minecraft, CI. **Weiter offen:** das gesamte kombinierte Hauptmod-Test-Gate, echte Inventar-/Rezeptprüfungen und danach getrennte optionale Add-ons (Pam's, MineColonies, Farmer's Delight). Keine Kompatibilität eines Add-ons hiermit nachgewiesen.
- **GitHub-Commits:** `00733e03` (Rückwandlungsprüfung), `533ddd72` (JUnit-Testquelle), `bb79fe8f` (CR-11), `861973c1` (Core-Test-Gate), `244e2394` (Handoff).

### Fortsetzung 20.11.2026 – Schutz gegen veraltete kombinierte Inventarmenüs

- **Ausgangsstand:** GitHub-HEAD `4f6efc0d5b9e4ae5154cbfc26e2d447d89fc88f9`.
- **Code-Commit `276acafe`:** `SeedAnalyzerCombinedMenu` sperrt Shift-Klicks auf dem gesamten Menü und `mayPlace`/`mayPickup` auf den 36 Lager-Slots, sobald Analyzer oder angrenzende Chest nicht mehr `stillValid` sind. Das adressiert späte Pakete zwischen Blockabbau und Menüschluss, ohne Slotgrößen, Reihenfolge, normale Lagerregeln oder die Standalone-Menüs zu ändern.
- **Manueller Test:** **MP-21** in `docs/MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`; auch die bisherigen MP-08, MP-09 und MP-17 bleiben für beide Richtungen des Abbaus relevant.
- **Verifikationsgrenze:** nur Code-Review und Diff-Prüfung; keine Gradle-, JUnit-, Minecraft- oder CI-Ausführung. Ob der Client während des Races kurzzeitig eine abweichende Vorschau zeigt, bleibt ein Gameplay-Testpunkt. Add-ons weiterhin zurückgestellt.
