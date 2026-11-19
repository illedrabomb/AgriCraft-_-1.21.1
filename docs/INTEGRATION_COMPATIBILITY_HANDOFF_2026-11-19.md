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
