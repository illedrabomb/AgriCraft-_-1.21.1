# Minecraft Mod – Test Tracker: Ablauf und Austauschformat

**Stand:** 17.11.2026 · **Projekt:** AgriCraft / CropGenetics, Minecraft 1.21.1, NeoForge · **Integrationsbranch:** `integration/neoforge-1211-consolidation`

**Allgemeines Tracker-Repository:** [Mod-Test-Tracker-Minecraft](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft) – allgemeiner Ablauf und bisher bekannte JSON-Importanforderung werden im [README](https://github.com/illedrabomb/Mod-Test-Tracker-Minecraft/blob/main/README.md) dokumentiert. Die Webseite selbst ist dort aktuell noch nicht versioniert; ihre genaue Schema-Implementierung ist noch zu prüfen.

## Zweck und Arbeitsablauf

Der **Minecraft Mod – Test Tracker** ist die vom Tester verwendete lokale Webseite für strukturierte **manuelle Ingame-Tests**. Die Testdefinition wird als JSON-Datei bereitgestellt; der Tester lädt sie in die Webseite, führt die beschriebenen Schritte in Minecraft aus, hakt die Fälle ab und notiert Beobachtungen. Anschließend stellt er eine **Ergebnisdatei** für die Auswertung im Entwicklungs-Chat bereit. Ziel ist, den Testablauf zwischen Tester und Entwickler/KI reproduzierbar zu halten, ohne jede Testanweisung einzeln im Chat durchzugehen.

**Vorgesehene Reihenfolge:**

1. Vorhandenen technischen Build-/JUnit-Nachweis prüfen; für diesen Testzyklus: Build erfolgreich, 36/36 JUnit-Tests bestanden.
2. Die zur Mod-Version passende JSON-Testsuite in den Tracker laden.
3. Minecraft 1.21.1 mit NeoForge, der aktuell gebauten Mod-JAR und den benötigten Testmods starten; tatsächlich getestete Modliste und Version dokumentieren.
4. Tests nach Bereich abarbeiten, Commands aus dem Testfall bei Bedarf ausführen; Status, Kommentar, tatsächliche Beobachtung und mögliche Belege (z. B. Screenshots, Koordinaten, Logs) erfassen.
5. Nach dem Test eine Ergebnisdatei exportieren/ablegen und dem Entwicklungs-Chat hochladen. **Das genaue Exportformat der Webseite wurde bislang nicht anhand einer echten Exportdatei verifiziert.**
6. Entwicklung/KI analysiert Fehler nach Test-ID, Erwartung, Beobachtung und Belegen; daraus werden Fixes und gezielte Nachtests abgeleitet. Nach Änderungen die automatische Validierung und die betroffenen manuellen Fälle wiederholen.

**Wichtig:** Das Abhaken in der Weboberfläche ist keine automatisierte Minecraft-Prüfung und kein Beweis für eine korrekte Spielfunktion. Tests dürfen nicht als bestanden markiert werden, bevor sie tatsächlich im Spiel durchgeführt wurden.

## Bekannte Importanforderungen (durch echten Fehler belegt)

Beim ersten Import der am 17.11.2026 erstellten Testdefinition zeigte der Tracker:

> Tests konnten nicht geladen werden: Ungültige tests.json: suiteId und nichtleere tests-Liste erforderlich.

Daher muss die importierte JSON-Datei mindestens eine **nichtleere `suiteId`** und eine **nichtleere `tests`-Liste** enthalten. Die erste Datei hatte zwar 30 Testfälle, aber **kein `suiteId`** und wurde abgewiesen. Die zweite, korrigierte Datei enthält:

```json
{
  "suiteId": "agricraft-neoforge-1211-manual-20261117",
  "format": "minecraft-mod-test-tracker",
  "schemaVersion": 1,
  "title": "AgriCraft / CropGenetics – manueller NeoForge-Test",
  "tests": [
    {
      "id": "PRE-01",
      "section": "Vorbereitung",
      "title": "Version und Mod-Ladeliste",
      "priority": "hoch",
      "steps": ["Minecraft 1.21.1 mit NeoForge und der frisch gebauten AgriCraft-JAR starten."],
      "expected": "AgriCraft/CropGenetics lädt ohne Startabbruch.",
      "commands": [],
      "status": "offen",
      "checked": false,
      "comment": "",
      "actual": "",
      "evidence": []
    }
  ]
}
```

**Das Beispiel ist gekürzt** und illustriert die bislang erstellte Dateistruktur, nicht das vollständig verifizierte interne Schema des Webseiten-Codes. Die konkrete Importvalidierung ist nur hinsichtlich `suiteId` und nichtleerer `tests`-Liste nachgewiesen. Ob die korrigierte Datei tatsächlich importiert werden kann, wurde vom Tester noch nicht bestätigt. Bei weiteren Importfehlern **nicht** blind Felder erfinden, sondern den Quellcode bzw. eine funktionierende `tests.json` der Webseite vergleichen.

## Aktuelle manuelle Testsuite (30 Fälle)

Am 17.11.2026 wurde eine Testsuite mit **30 offenen Fällen** in sechs Bereichen erstellt:

| Bereich | Anzahl | Schwerpunkt |
| --- | ---: | --- |
| Vorbereitung | 3 | Version/JAR/Modliste, Testwelt |
| Crop Sticks und Darstellung | 7 | vier Eckstäbe, Kreuzungen, Texturen, Stage 0, Pflanzenfarben/-höhe, Samen-Icons |
| Wachstum und Ernte | 6 | normale Reife, Knochenmehl, Welt-Neuladen, `/reload`, Folgeernte |
| Melonen und Kürbisse | 6 | erste und zweite Frucht, blockierte Nachbarfelder, Diagnosezähler |
| HUD, Identify und JEI | 5 | Overlay, Lesbarkeit, Identify, 16 Saatarten in JEI |
| Kompatibilität und Abschluss | 3 | Pam's Crops, parallele Mods, Logs/Screenshots |
| **Gesamt** | **30** | Alle initial als `offen` gesetzt |

Die Tests haben stabile IDs wie `PRE-01`, `VIS-01`, `GRO-01`, `FRU-02`, `UI-01` und `JEI-01`. Die JSON-Definition hat `statusOptions` = `offen`, `bestanden`, `fehlgeschlagen`, `blockiert`, `übersprungen`. Die Weboberfläche könnte ihre eigenen Zustände verwenden; deren genaue Implementierung wurde noch nicht eingesehen.

**Besonders wichtig:** Die früheren Ingame-Beobachtungen „Wachstumsstufe 0 wirkte gleich“ und „X-Anordnung bei beiden getesteten Samen“ sind **historische Befunde**, keine Bestätigung für den aktuellen Build. Alle entsprechenden Fälle müssen erneut im Client geprüft werden. Das Problem des zweiten Melonen-/Kürbis-Fruchtzyklus bleibt bis zu einem echten manuellen PASS offen.

### Beispiel für Diagnose-Command

An einer bekannten Crop-Sticks-Position einer reifen Melonen-/Kürbispflanze:

```mcfunction
/cropgenetics data gourd <x> <y> <z>
```

Bei Auswertung `matureFruitTicks`, `matureFruitTicksBlocked`, `fruitPlacementNoEligibleTarget` und `fruitPlacementEligibleFailed` vor/nach dem Beobachtungszeitraum vergleichen. Die Koordinaten in spitzen Klammern sind Platzhalter; echte Weltkoordinaten einsetzen.

## Austausch und Auswertung der Resultate

Bei jedem als fehlgeschlagen markierten Fall möglichst dokumentieren: **Test-ID**, Erwartung, tatsächliches Verhalten, Reproduktionsschritte, eingesetzte JAR/Commit und Mods, Koordinaten und Log-/Bildnachweise. Die Webseite ist der Ort zum Abarbeiten; der Chat wertet die exportierten Daten aus und dokumentiert die Findings im Integrationsbranch.

**Unbestätigte Aspekte:** Die Webseite bzw. ihre Dateien wurden nicht bereitgestellt. Daher sind Speichermethode (Browser/Datei), Exportknopf, konkrete JSON-Exportfelder und eventuelle Versionsmigrationen **nicht verifiziert**. Der hier beschriebene Ablauf entspricht dem vom Tester gewünschten Workflow und dem beobachteten Importfehler; er ist kein formaler Nachweis der gesamten Website-Implementierung.
