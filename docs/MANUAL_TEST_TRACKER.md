# Minecraft Mod – Test Tracker: verifizierter Ablauf für AgriCraft

**Stand:** 17.11.2026 · **Minecraft:** 1.21.1 · **Loader:** NeoForge · **Branch:** `integration/neoforge-1211-consolidation`

**Verbindliche technische Quelle:** [JSON_FORMAT.md im Test-Tracker-Repository](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/docs/JSON_FORMAT.md). Die Angaben hier wurden mit den jetzt versionierten Dateien [`index.html`](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/index.html), [`README.md`](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/README.md) und den Beispiel-JSON-Dateien des Trackers abgeglichen. **Bei Änderungen am Tracker hat dessen JSON_FORMAT.md Vorrang.**

## Unmittelbar zu verwendende Testdatei

**[AgriCraft-30-Tests-Tracker-kompatibel.json herunterladen/ansehen](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/AgriCraft-30-Tests-Tracker-kompatibel.json)**

Diese Datei wurde im Tracker-Repository aus dem ursprünglichen 30-Fälle-Testplan in das tatsächlich erwartete JSON-Format übertragen. Sie enthält die **30 offenen Tests**, richtige Feldnamen (`category` statt `section`) und die Suite-ID `agricraft-neoforge-1211-manual-30-20261117`. Sie ist nach der im eingecheckten JavaScript definierten Importvalidierung strukturell kompatibel; eine tatsächliche erfolgreiche Browser-Importbestätigung des Testers liegt noch nicht vor.

**Nicht importieren:** `AgriCraft-Manueller-Test-Tracker-2026-11-17.json` (historischer Testplan, anderes Schema). Ebenso nicht die frühere lediglich um `suiteId` ergänzte Variante als kanonische Testsuite verwenden. Die 10-Fälle-Datei `tests.json` ist nur eine separate Smoke-Testsuite.

## Gebrauch der lokalen Offline-Webseite

1. Lokal `index.html` aus dem Tracker-Repository in einem modernen Browser öffnen; kein Webserver und keine Internetverbindung erforderlich.
2. **Tests laden** → `AgriCraft-30-Tests-Tracker-kompatibel.json` wählen. **Achtung: Das Laden einer Suite löscht die bisher im Tracker gespeicherten Testergebnisse! Vorher exportieren.**
3. Minecraft mit dem getesteten AgriCraft-Build und den passenden Zusatzmods öffnen, die 30 Tests abarbeiten, pro Test den Status wählen und Beobachtungen unter **Kommentar / Fehlerbeschreibung** schreiben.
4. Commands werden nur als kopierbare Schaltflächen bereitgestellt. Sie werden **nicht** automatisch in Minecraft oder PowerShell ausgeführt. Bei Platzhaltern (`<x> <y> <z>`) die realen Koordinaten einsetzen.
5. Im Feld **Tester / Testbuild** möglichst den JAR-Namen und Git-Commit eintragen.
6. **Ergebnisse exportieren** → `mod-test-results-YYYY-MM-DD.json` sichern und im Entwicklungs-Chat zur Auswertung hochladen. Falls erforderlich `latest.log`, Screenshots oder Crash-Reports getrennt mitgeben.
7. Beim späteren Fortsetzen **zuerst dieselbe Testsuite**, dann über **Ergebnisse importieren** den Export laden. Die `suiteId` muss exakt übereinstimmen. Ein Import überschreibt den aktuellen Bearbeitungsstand.

Die Webseite speichert Suite, Status, Kommentare und Testerfeld im Browser-`localStorage` unter `minecraft-mod-test-tracker-v1`. Das ist **keine sichere Datensicherung**: Vor Testpausen und Suitewechseln stets exportieren. Es gibt weder GitHub-Synchronisation noch automatische Ausführung der Tests.

Die Oberfläche bietet Suche, Filter, Status, Kommentare, Fortschrittsanzeige, kopierbare Commands und ein Zurücksetzen mit Bestätigungsdialog. Die Suche umfasst ID, Titel, Kategorie, Erwartungen, Schritte und Commands – **nicht** Kommentare.

## Verbindliches Testsuite-JSON-Schema (Auszug)

```json
{
  "schemaVersion": 1,
  "suiteId": "agricraft-neoforge-1211-manual-30-20261117",
  "title": "AgriCraft / CropGenetics – manueller NeoForge-Test",
  "environment": { "minecraft": "1.21.1", "loader": "NeoForge" },
  "tests": [
    {
      "id": "PRE-01",
      "category": "Vorbereitung",
      "title": "Version und Mod-Ladeliste",
      "steps": ["Minecraft mit dem vorgesehenen NeoForge-Testbuild starten."],
      "expected": "Die Mod wird geladen.",
      "commands": []
    }
  ]
}
```

Dies ist ein **verkürztes Strukturbeispiel**, nicht die vollständige 30-Fälle-Datei. Erforderlich sind `suiteId` als nichtleerer String und `tests` als nichtleeres Array. Für jeden Test muss `id` ein nichtleerer, innerhalb der Suite eindeutiger String sein, `title` ein String und `steps` ein String-Array. `commands` muss, falls vorhanden, ebenfalls ein String-Array sein. `category`, `expected`, `title` der Suite, `description`, `environment` und `schemaVersion` sind optional; `schemaVersion` wird vom Importer derzeit nicht strikt geprüft.

**Testergebnisse gehören nicht in die Testsuite.** Die früheren Felder `status`, `checked`, `actual`, `evidence` innerhalb eines `tests[]`-Elements werden vom aktuellen Tracker nicht als Bearbeitungsstand verwendet. Ist-/Reproduktionsinformationen im `comment` des separat exportierten Resultats dokumentieren; Logdateien und Screenshots separat bereitstellen.

## Exportformat und Statuswerte

Der echte JavaScript-Export enthält `schemaVersion`, `suiteId`, `suiteTitle`, `exportedAt`, `tester`, `environment` und `results`. Unter `results` existiert für **jede Test-ID**, auch bei offenen Fällen, ein Objekt mit `status` und `comment`.

```json
{
  "schemaVersion": 1,
  "suiteId": "agricraft-neoforge-1211-manual-30-20261117",
  "suiteTitle": "AgriCraft / CropGenetics – manueller NeoForge-Test",
  "exportedAt": "2026-11-17T12:00:00.000Z",
  "tester": "Testbuild / Commit hier",
  "environment": { "minecraft": "1.21.1", "loader": "NeoForge" },
  "results": {
    "PRE-01": { "status": "pass", "comment": "Modliste geprüft; Start erfolgreich." },
    "PRE-02": { "status": "open", "comment": "" }
  }
}
```

Nur diese internen Werte sind erlaubt:

| Wert | Anzeige | Bedeutung |
| --- | --- | --- |
| `open` | Offen | Noch nicht abgeschlossen |
| `pass` | Bestanden | Erwartetes Verhalten erfüllt |
| `fail` | Fehlgeschlagen | Reproduzierbare Abweichung |
| `blocked` | Blockiert | Test derzeit nicht sinnvoll durchführbar |

Es gibt **keinen** Status `skipped` und keine deutschen Statuswerte im Export. Die Webseite setzt unbekannte Statuswerte beim Import auf `open`. Der interne `updatedAt` wird beim Export nicht ausgegeben. Der Ergebnisimport ignoriert Test-IDs, die in der aktuell geladenen Suite fehlen, und vergleicht **nur `suiteId`**, nicht den Inhalt der Tests.

## Neuer Testbefund: obere Kreuzungs-Crop-Sticks bleiben nach erfolgreicher Kreuzung bestehen (17.11.2026)

**Quelle:** Testerbericht mit zwei Ingame-Screenshots und Ergebnisexport `mod-test-results-2026-11-17 (2).json` (`suiteId: agricraft-neoforge-1211-manual-30-20261117`, Export 19:36:44 UTC). Der Screenshot einer bepflanzten Melonen-/Kürbisfläche und der Screenshot mit sichtbaren Kreuzverstrebungen auf leeren Crop-Stick-Feldern sind als externe Testbelege im Chat vorhanden; sie sind nicht ins Git eingecheckt.

- **Beobachtung:** Nach Weiterentwicklung/Kreuzung und Entstehung einer Pflanze verschwinden die **oberen/zusätzlichen Kreuzungs-Crop-Sticks** nicht bzw. werden nicht verbraucht. Kreuzförmige Verstrebungen bleiben auch an leeren Positionen sichtbar. Das ist ein vom Tester gemeldeter Ingame-Fehler; die Screenshots allein belegen nicht, ob die interne Kreuzungszustandsvariable ebenfalls bestehen bleibt.
- **Zusammenhang mit `VIS-02: fail`:** Im Testexport steht: `entfernen der Kreuzung durch weiteres Rechtklick nicht vorhande. versucht darüber zu platzieren und nicht den gekreuten Stick zu entfernen`. Das ist eine zweite, möglicherweise zusammenhängende Interaktionsabweichung.
- **Verbindliches Sollverhalten (Testerentscheidung):** Zwei Crop Sticks / `CROSS=true` sind ausschließlich für Kreuzung bestimmt: Solange das Feld gekreuzt ist, darf ein direkt gesetzter Seed **nicht** gepflanzt werden. Sobald dort durch Kreuzung ein Nachkomme wächst, wird die zusätzliche obere Stick-Ebene **immer** verbraucht (`CROSS=false`); die untere Stick-Ebene und die Pflanze bleiben. Zum manuellen Setzen eines Seeds muss man zuerst die obere Ebene entfernen (leere Hand, Rechtsklick), erst danach ist das Einpflanzen erlaubt. Es gibt damit keinen Zustand „direkt gepflanzter Seed auf gekreuzten Sticks“.
- **Automatisierte Regression (neu, noch nicht ausgeführt):** `CropCrossLayerStateTest` prüft die vier Kombinationen aus Kreuzungszustand und vorhandenem Genom über `CropSticksBlockEntity.keepCrossLayer`. Damit ist das reine Zustandsprinzip abgesichert; Ein-/Ausgabe, Item-Drops und tatsächliche Rechtsklick-Ereignisse benötigen weiterhin Ingame-Tests.
- **Reproduktion/Nachtest:** Zwei Crop-Stick-Felder mit Elternpflanzen und angrenzende gekreuzte/leere Sticks vorbereiten; Ausgangszustand (normal/gekreuzt) notieren; auf erfolgreiche Übertragung warten und Zielpflanze überprüfen; danach Blockmodell, Rechtsklick-Interaktion und gegebenenfalls Stick-Drops/Inventar prüfen. Falls nötig Welt neu laden und auf Client-Server-Zustandsabweichungen prüfen.
- **Implementierung vorbereitet:** `CropSticksBlockEntity.syncState` setzt `CROSS=false`, sobald ein Nachkomme eine Pflanze auf dem gekreuzten Feld erzeugt (Commit `7fa00bf`). `CropSticksBlock.useWithoutItem` entfernt bei leerer Hand und leerem Kreuzungsfeld die obere Ebene und gibt einen Crop Stick zurück (Commits `c113050`, `45d7108`). Bereits vorhandene Seed-Interaktionspfade prüfen `!CROSS` und sperren direktes Pflanzen. **Noch nicht per Gradle gebaut oder im Spiel nachgetestet.** Dem vorhandenen `VIS-02`-Fehler zugeordnet; bei Einführung einer eigenen Test-ID die Tracker-Suite konsistent versionieren, statt IDs im bestehenden Ergebnisexport stillschweigend zu verändern.

## Fruchtzyklus Melonen/Kürbisse: Codebefund und Nachtest (17.11.2026)

Im hochgeladenen Ergebnisexport stehen `FRU-01`, `FRU-02` und `FRU-04` auf `fail`; die Kommentare melden u. a. einen ausbleibenden zweiten Melonenertrag nach rund 20 Minuten. Dass die **Frucht** neben dem in Crop Sticks befindlichen Stängel wächst, ist das vorgesehene vanilla-nahe Verhalten und **allein kein Fehler**. Die wiederholte Fruchtbildung bleibt dagegen ein relevantes Problem.

**Konkreter Quellcodebefund:** `CropSticksBlockEntity.clearFruit` wurde beim Entfernen einer zugehörigen Melonen-/Kürbisfrucht aufgerufen und setzte bisher auch den reifen Stängel über `CropGrowth.regrowthStage(plant)` zurück, häufig auf Stufe 0. Bis zur erneuten Reife waren dadurch keine weiteren Frucht-Chance-Würfe möglich. **Änderung in Commit `357a619`:** Für aktiv als `GOURD` konfigurierte Arten bleibt die Stängelstufe beim Fruchtabbau unverändert; entfernt werden nur physische Fruchtbindung und `fruitReady`. Für nach einem Datapack-Reload anders konfigurierte Erntearten bleibt der bisherige Regrowth-Zweig erhalten. Platzierung neben dem Stängel, Chance und Bodenregeln wurden **nicht** verändert.

**Nachtest für beide Arten:** Frucht einmal wachsen lassen; `/cropgenetics data gourd <x> <y> <z>` vor Abbau protokollieren; physische Frucht entfernen; unmittelbar prüfen, ob der Stängel reif bleibt und `fruitReady=false` ist; danach wiederholte Frucht-Chance-Würfe und eine zweite physische Frucht auf frei geeignetem Nachbarfeld nachweisen. Wenn weiterhin lange nichts wächst, die Werte `fruitChanceRolls`, `fruitChancePassed`, `fruitPlacementNoEligibleTarget` und `fruitPlacementEligibleFailed` vergleichen. Damit unterscheiden wir geringe Tick-/Chance-Häufigkeit von echter Platzierungsblockade.

**Status:** Quellcodefix committed, **nicht gebaut und nicht im Minecraft-Client nachgetestet**. Eine garantierte Frucht innerhalb einer festen Minutenzahl ist damit nicht behauptet.

## UI- und Saatgutbefunde aus dem 30-Test-Export (17.11.2026)

- **`UI-01` / `UI-03`:** Der Nutzer möchte Pflanzeninformationen in einem Look-at-Overlay wie Identify/WAILA; das separate `CropInfoOverlay` läuft, aber Identify 0.4.0 zeigt noch keine Crop-Genetics-Daten. Der bestehende Stand hat **keine Identify-spezifische Plugin-Integration**; ein unterstützter öffentlicher Hook wurde bislang nicht nachgewiesen. **Nicht als behoben markieren.** Keine fragile Verknüpfung über private Identify-Klassen oder ein vorgetäuschtes API hinzugefügt. Bei einer künftigen Integration müssen unanalysierte Genwerte weiter ausgeblendet bleiben.
- **`VIS-07`:** Der Seed-Itemname war bisher generell `Genetic Seed`; die Art stand nur in der Tooltip-Zeile. Jetzt verwendet `GeneticSeedItem.getName(ItemStack)` die synchronisierte `Genome.plantId()` und übersetzte Artbezeichnungen aus `CropSpeciesNames` (z. B. `Wheat Genetic Seed` oder deutsch `Weizen-Saatgut (genetisch)`). Unbekannte Datapack-Arten erhalten einen lesbaren ID-Fallback. Die Crop-Info-Zeile nutzt dieselbe Namensquelle. Der redundante Crafting-Grid-Hinweis wurde aus dem generischen Seed-Tooltip entfernt. **Noch nicht gebaut/ingame bestätigt**; das eigentliche JEI-Rezept-Thema bleibt offen.
- **Nachtest:** Verschiedene genetische Seeds (Weizen, Karotte, Melone) in Inventar und JEI auf Artname prüfen; bei unanalysierten Seeds dürfen keine Genpaare sichtbar werden; mit Analyzer + Shift die Sichtbarkeit erneut kontrollieren. Identify weiterhin separat als offenes Integrationsziel erfassen.

## Kreuzungsgeschwindigkeit – nachvollziehbarer Codefix (17.11.2026)

**Testerbefund:** In `END-01` des 30-Fälle-Exports wurden nur etwa zwei von vier besetzten Nachbar-Kreuzungsfeldern nach 20 Minuten gemeldet. Das ist ein Beobachtungswert, noch keine allgemeine zeitliche Erfolgsgarantie.

**Ursache:** `CrossBreeding.attempt` zog zuvor bei zwei reifen Nachbarpflanzen für **jede Elternpflanze separat** eine Fertilitätsprobe. Bei Basisgenom 1/1 (= 0,1 pro Probe) war die Wahrscheinlichkeit, im selben Ziel-Random-Tick **beide** Eltern zu erhalten, nur `0,1 × 0,1 = 0,01` (= 1 %). Andernfalls fiel die Logik häufig auf den Ein-Eltern-Klonpfad zurück. Darauf kamen die ohnehin seltenen Minecraft-Random-Ticks.

**Änderung (Commit `a88fcff`):** Erst alle berechtigten reifen Nachbarn ermitteln, danach bei mindestens zwei Nachbarn **eine gemeinsame Fruchtbarkeitsprobe** anhand des Mittelwerts der ausgedrückten Eltern-Fertilität (1/1 + 1/1 ⇒ 10 % pro Ziel-Random-Tick). Eltern werden weiterhin zufällig ausgewählt; Mutation, Biome, Boden, Gene, Kandidatenfilter und Ziel-Validierung bleiben bestehen. Bei nur einem reifen Nachbarn bleibt Klonen separat und erfordert Fertilität × `spreadChance`. Ein vollständiges Gameplay-Balancing oder eine garantierte Mindestdauer folgt daraus nicht.

**Regression:** `CrossBreedingFertilityTest` prüft 1/1 ⇒ 10 %, 10/10 ⇒ 100 %, gemischt ⇒ 55 % und Dominanz 8/5 ⇒ 65 %. **Tests noch nicht ausgeführt.**

**Späterer Nachtest:** Je ein Feld mit genau einem und mit genau zwei reifen Eltern vergleichen, beide auf zulässigem Boden; Start-/Endzustand und real verstrichene Zeit erfassen. Der echte Zufallstakt hängt auch von `randomTickSpeed`, geladenen Chunks und Pflanzenbedingungen ab. Die geplante optionale **Evolution bei Reihenanbau** ist eine **getrennte, noch nicht implementierte** Funktion und wird nicht mit dieser Kreuzungs-Chance vermischt.

## Reihen-Kreuzung und konfigurierbare Evolution (Testerentscheidung 17.11.2026)

**Verbindliche Regel des Testers:** **Keine zusätzlichen Bonus-Samen bei normalem Wachstum oder bei der Ernte.** Evolution betrifft ausschließlich den **einen Nachkommen**, der bei erfolgreicher Vermehrung auf einem doppelt gesteckten Kreuzungsfeld entsteht. Die beiden Eltern und ihr Genom bleiben unverändert. Ein direkt bepflanztes Feld mit einem Crop Stick produziert durch reines Reihenwachstum **keine** neue Saatgutgeneration.

Die bisherige Vererbungsfunktion `BreedingEngine.combine` kann beim Kreuzen Gene bereits mit Mutativity-abhängigen ±1-Schritten mutieren. Neu wird ein **gerader Reihen-Kreuzungsfall** gesondert erkannt: Zwei geeignete Eltern derselben Art stehen auf entgegengesetzten Seiten des gekreuzten Zielfelds (N/S oder E/W). Für **nur diesen Fall** kann die Mutationschance über eine NeoForge-Serverkonfiguration verändert oder ausgeschaltet werden. Andere zulässige Kreuzungen verwenden weiterhin die bisherige allgemeine Vererbungsregel; ein einzelner Elternteil bleibt auf dem separaten Klonpfad ohne neue Mutation.

### Servereinstellungen

`CropBalanceConfig` wird als `ModConfig.Type.SERVER` registriert. Die konkreten Einstellungen werden beim Start in der NeoForge-Serverkonfiguration der Welt verwaltet (typischerweise `<Welt>/serverconfig/cropgenetics-server.toml`).

| Einstellung | Standard | Bereich | Bedeutung |
| --- | ---: | ---: | --- |
| `breeding.rowCrossEvolution` | `true` | `true/false` | Mutationen beim geraden Kreuzungsfall gleicher Art aktivieren/deaktivieren |
| `breeding.rowEvolutionMutationMultiplier` | `1.0` | `0.0–5.0` | Mutation-Chance des Reihen-Nachkommens skalieren |
| `breeding.crossFertilityMultiplier` | `1.0` | `0.1–5.0` | Zwei-Eltern-Kreuzungshäufigkeit skalieren (auf 100 % begrenzt) |
| `gourds.fruitChanceMultiplier` | `1.0` | `0.1–5.0` | Fruchtversuche reifer Melonen-/Kürbispflanzen skalieren |

Mit Standardwerten bleiben die bisherige Mutations- und Fruchtchance erhalten; die bereits korrigierte einzelne Fruchtbarkeitsprobe für Elternpaare bleibt die Ausgangsbasis. Alle Gene sind weiterhin auf 1..10 begrenzt; der Erfolg eines Random Ticks wird nicht garantiert.

**Regression:** `RowEvolutionInheritanceTest` prüft ±1-Werte, Grenzen und ausgeschaltete/erhöhte Mutationschance. `CrossBreedingFertilityTest` prüft die unveränderte Basiswahrscheinlichkeit der Elternpaare. **Die Konfiguration, Compilation und Gameplaysimulation sind noch nicht gebaut/ausgeführt.** Beim späteren Test immer mit frischer geeigneter Konfiguration und beiden Elternpaar-Anordnungen (gerade vs. Ecke) vergleichen.

## Wasserkanne/Wachstumstools: Standard-Bonemeal-Schnittstelle (17.11.2026)

**Testerbefund `GRO-02`:** Direkt benutztes Knochenmehl ließ die AgriCraft-Pflanze wachsen, eine separate Gießkanne (im Test-Modset `Watering Cans 1.0.6`) dagegen nicht. Die genaue technische Implementation dieser Drittanbieter-Gießkanne ist bislang **nicht** verifiziert; nicht alle Mod-Gießkannen verwenden dieselbe API.

**Quellcode-Ursache/Änderung:** `CropSticksBlock` hatte bislang nur eine spezielle `Items.BONE_MEAL`-Rechtsklick-Behandlung, implementierte aber **nicht** `net.minecraft.world.level.block.BonemealableBlock`. Viele kompatible Wachstums-Items fragen diese Standard-Schnittstelle ab. Commit `e350da8` implementiert `isValidBonemealTarget`, `isBonemealSuccess` und `performBonemeal`: nur bepflanzte, nicht reife Sticks, serverseitig aktive Pflanzendefinition und alle Boden-/Umweltbedingungen; pro erfolgreicher Anwendung genau ein Stufenschritt. Bei falschem Boden, reifer Pflanze und leeren Kreuzungs-Sticks kein Wachstum. Keine Genomänderung oder zusätzliche Drops. Die vorhandene explizite Knochenmehl- und optionale Bodenpflege-Interaktion bleibt erhalten.

**Grenze:** Die Schnittstelle ist eine allgemeine Kompatibilitätsverbesserung, **keine nachgewiesene Fehlerbehebung für eine konkrete Watering-Cans-1.0.6-JAR**. Mods, die ausschließlich Vanilla-`CropBlock` unterstützen, benötigt eventuell einen separaten Adapter. Ebenso kann ein Mod nur Bodenbewässerung statt direktes Pflanzenwachstum anbieten.

**Nachtest:** Bone Meal, die konkrete Gießkanne und mindestens einen weiteren kompatiblen Bonemealable-Wachstumsgegenstand auf gleichen, noch nicht reifen Crop-Stick-Pflanzen prüfen; vor/nach Wachstumstufe, Itemverbrauch, zulässigen Boden und ausgereifte Pflanzen vergleichen. **Kein Build-/Minecraft-PASS für diesen Commit.**

## Die 30 vorgesehenen AgriCraft-Testfälle

| Bereich | Anzahl | Inhalte |
| --- | ---: | --- |
| Vorbereitung | 3 | Testbuild/JAR, Mods und Welt |
| Crop Sticks und Darstellung | 7 | vier Eckstäbe, Kreuzstruktur, Holztextur, Stage 0, Farben/Höhen, Samen-Icons |
| Wachstum und Ernte | 6 | natürliche Reife, Knochenmehl, Reload, Ernte und Folgezyklus |
| Melonen und Kürbisse | 6 | erste und zweite Frucht, blockierte Felder, Diagnose |
| HUD, Identify und JEI | 5 | Position/Lesbarkeit, Zusatzmods, 16 Saatarten |
| Kompatibilität und Abschluss | 3 | Pam's Crops, kombinierte Modliste, Belege |
| **Gesamt** | **30** | **Alle initial offen** |

Der Diagnosebefehl für einen Melonen-/Kürbis-Test lautet `/cropgenetics data gourd <x> <y> <z>`, mit echten Weltkoordinaten statt Platzhaltern. Für die Auswertung die Zähler `matureFruitTicks`, `matureFruitTicksBlocked`, `fruitPlacementNoEligibleTarget`, `fruitPlacementEligibleFailed` vergleichen.

**Historische Fehler sind nicht automatisch behoben:** identische Stage-0-Darstellung, X-Sticks bei beiden zuvor getesteten Samen und fehlende zweite Melonen-/Kürbisfrucht müssen im neuen Minecraft-Test ausdrücklich nachgeprüft werden. Der vorherige Build war erfolgreich und der JUnit-Bericht zeigte 36/36 bestandene Tests; das beweist keine Gameplay-Abnahme.

## Arbeitsteilung

- **Tracker-Repository:** Webseite, Importschema, Exportformat, allgemeine Vorlagen. Verbindliche Quelle ist [`docs/JSON_FORMAT.md`](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/docs/JSON_FORMAT.md).
- **AgriCraft-Repository:** konkrete Testanforderungen, Build- und Versionsstand, Fehlerbefunde und Regressionen.
- **Tester:** Durchführung in Minecraft, Status/Kommentare und Ergebnisexport.
- **Entwicklungs-Chat:** `results` anhand der exakt passenden `suiteId` und Test-IDs auswerten, Fixes dokumentieren und gezielte Nachtests erstellen.
