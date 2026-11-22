# Codex Core-Gate – automatisch extrahierter Befund

> Rohdaten-Auswertung, **keine** Ursachenanalyse oder P0/P1-Einstufung.

## Beleglage – keine automatische Testfreigabe

**WARNUNG: Unvollständiger oder fehlerhafter Gate-Nachweis.**

- Gradle-Exitcode ist nicht 0; Aufgabenlauf fehlgeschlagen oder unklar.
- Keine frischen JUnit-XML-Dateien vorhanden.
- Keine ausgeführten JUnit-Testfälle nachgewiesen.

## Rohdaten

- Git-Commit aus Laufmanifest: `077d70f70334b87acd3aaaaa0ec4ade854779ca2` (JAR-Zuordnung zusätzlich prüfen)
- Entwicklungsbranch: `integration/neoforge-1211-consolidation`
- Java: `openjdk version "21.0.12.1" 2026-08-18 LTS`
- Laufumfang: `core-only :test :build (no Minecraft run, no addon tasks)`
- Gradle-Exitcode: `1` (0 bedeutet nur Task-Erfolg)
- Frische JUnit-XML-Dateien: **0**
- JUnit-Nachweis: **FEHLT** (kein automatischer Pass-Status)
- JUnit: 0 Tests, 0 Failures, 0 Errors, 0 Skipped
- Während dieses Laufs erzeugte JAR-Dateien: **1**
- Fehlende oder nicht frisch erzeugte XML/JAR-Dateien sind **kein Erfolgsnachweis**.

## JAR-Artefakte

- `cropgenetics-0.2.14-alpha.jar` – 385545 Bytes, SHA-256: `9e272b6c9ddf97d8a96dbee64ec27b595caea704bef68c79760f0ad0e41da27a`

## Fehlgeschlagene JUnit-Fälle (ungekürzt in XML)

- Keine Failure/Error-Einträge in den gefundenen frischen XML-Dateien.

## Weiteres Vorgehen

Codex prüft `gradle.log` und die oben verlinkten XML-Dateien auf Ursachen.
Nur eindeutig kritische P0/P1-Funde minimal selbst beheben; P2/P3 im Chat melden.
Minecraft-/Mineflayer-/Multiplayer-Tests bleiben gesondert freigabepflichtig.
