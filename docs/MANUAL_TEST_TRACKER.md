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
