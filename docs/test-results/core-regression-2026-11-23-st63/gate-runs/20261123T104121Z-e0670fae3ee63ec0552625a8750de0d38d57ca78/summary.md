# Codex Core-Gate – automatisch extrahierter Befund

> Rohdaten-Auswertung, **keine** Ursachenanalyse oder P0/P1-Einstufung.

## Beleglage – keine automatische Testfreigabe

**Nur formale Belege vorhanden – noch kein geprüfter Pass-Status.**


## Rohdaten

- Git-Commit aus Laufmanifest: `e0670fae3ee63ec0552625a8750de0d38d57ca78` (JAR-Zuordnung zusätzlich prüfen)
- Entwicklungsbranch: `integration/neoforge-1211-consolidation`
- Java: `openjdk version "21.0.12.1" 2026-08-18 LTS`
- Gradle: `9.2.1`
- Laufumfang: `core-only :test :build (no Minecraft run, no addon tasks)`
- Gradle-Exitcode: `0` (0 bedeutet nur Task-Erfolg)
- Frische JUnit-XML-Dateien: **62**
- JUnit-Nachweis: **VORHANDEN** (kein automatischer Pass-Status)
- JUnit: 220 Tests, 0 Failures, 0 Errors, 0 Skipped
- Während dieses Laufs erzeugte JAR-Dateien: **1**
- Fehlende oder nicht frisch erzeugte XML/JAR-Dateien sind **kein Erfolgsnachweis**.

## JAR-Artefakte

- `cropgenetics-0.2.14-alpha.jar` – 403199 Bytes, SHA-256: `313fb7466b110cdf5e5c255196e387e2f4393049500705a4830b22a93f79a08e`

## Fehlgeschlagene JUnit-Fälle (ungekürzt in XML)

- Keine Failure/Error-Einträge in den gefundenen frischen XML-Dateien.

## Weiteres Vorgehen

Codex prüft `gradle.log` und die oben verlinkten XML-Dateien auf Ursachen.
Nur eindeutig kritische P0/P1-Funde minimal selbst beheben; P2/P3 im Chat melden.
Minecraft-/Mineflayer-/Multiplayer-Tests bleiben gesondert freigabepflichtig.
