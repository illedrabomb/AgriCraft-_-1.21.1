# Codex Core-Gate – automatisch extrahierter Befund

> Rohdaten-Auswertung, **keine** Ursachenanalyse oder P0/P1-Einstufung.

## Beleglage – keine automatische Testfreigabe

**WARNUNG: Unvollständiger oder fehlerhafter Gate-Nachweis.**

- Gradle-Exitcode ist nicht 0; Aufgabenlauf fehlgeschlagen oder unklar.
- JUnit meldet Fehler oder fehlgeschlagene Testfälle.

## Rohdaten

- Git-Commit aus Laufmanifest: `1c1b7cd4f990b84577537e1d3a549713f10576c5` (JAR-Zuordnung zusätzlich prüfen)
- Entwicklungsbranch: `integration/neoforge-1211-consolidation`
- Java: `openjdk version "21.0.12.1" 2026-08-18 LTS`
- Gradle: `9.2.1`
- Laufumfang: `core-only :test :build (no Minecraft run, no addon tasks)`
- Gradle-Exitcode: `1` (0 bedeutet nur Task-Erfolg)
- Frische JUnit-XML-Dateien: **62**
- JUnit-Nachweis: **VORHANDEN** (kein automatischer Pass-Status)
- JUnit: 220 Tests, 1 Failures, 0 Errors, 0 Skipped
- Während dieses Laufs erzeugte JAR-Dateien: **1**
- Fehlende oder nicht frisch erzeugte XML/JAR-Dateien sind **kein Erfolgsnachweis**.

## JAR-Artefakte

- `cropgenetics-0.2.14-alpha.jar` – 403187 Bytes, SHA-256: `7408c8226446f85af40299df7161eae96c6c2c11be635f2b3350e42869308a2c`

## Fehlgeschlagene JUnit-Fälle (ungekürzt in XML)

- `dev.illedrabomb.cropgenetics.content.SeedChestRestrictedMenuTest.wrapperDelegatesEveryPlacementToContainerRules()` (failure): org.opentest4j.AssertionFailedError: expected: <true> but was: <false> – `TEST-dev.illedrabomb.cropgenetics.content.SeedChestRestrictedMenuTest.xml`

## Weiteres Vorgehen

Codex prüft `gradle.log` und die oben verlinkten XML-Dateien auf Ursachen.
Nur eindeutig kritische P0/P1-Funde minimal selbst beheben; P2/P3 im Chat melden.
Minecraft-/Mineflayer-/Multiplayer-Tests bleiben gesondert freigabepflichtig.
