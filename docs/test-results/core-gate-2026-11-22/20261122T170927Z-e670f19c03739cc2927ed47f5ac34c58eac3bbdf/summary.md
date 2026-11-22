# Codex Core-Gate – automatisch extrahierter Befund

> Rohdaten-Auswertung, **keine** Ursachenanalyse oder P0/P1-Einstufung.

## Beleglage – keine automatische Testfreigabe

**WARNUNG: Unvollständiger oder fehlerhafter Gate-Nachweis.**

- Gradle-Exitcode ist nicht 0; Aufgabenlauf fehlgeschlagen oder unklar.
- JUnit meldet Fehler oder fehlgeschlagene Testfälle.
- Kein frisch erzeugtes JAR-Artefakt nachgewiesen.

## Rohdaten

- Git-Commit aus Laufmanifest: `e670f19c03739cc2927ed47f5ac34c58eac3bbdf` (JAR-Zuordnung zusätzlich prüfen)
- Entwicklungsbranch: `integration/neoforge-1211-consolidation`
- Java: `openjdk version "21.0.12.1" 2026-08-18 LTS`
- Laufumfang: `core-only :test :build (no Minecraft run, no addon tasks)`
- Gradle-Exitcode: `1` (0 bedeutet nur Task-Erfolg)
- Frische JUnit-XML-Dateien: **59**
- JUnit-Nachweis: **VORHANDEN** (kein automatischer Pass-Status)
- JUnit: 212 Tests, 2 Failures, 0 Errors, 0 Skipped
- Während dieses Laufs erzeugte JAR-Dateien: **0**
- Fehlende oder nicht frisch erzeugte XML/JAR-Dateien sind **kein Erfolgsnachweis**.

## JAR-Artefakte

- Keine frisch erzeugten JARs nachgewiesen.

## Fehlgeschlagene JUnit-Fälle (ungekürzt in XML)

- `dev.illedrabomb.cropgenetics.content.GeneticSeedStackingTest.defaultGeneticSeedIsAlwaysUnstackable()` (failure): java.lang.IllegalStateException: Registry is already frozen – `TEST-dev.illedrabomb.cropgenetics.content.GeneticSeedStackingTest.xml`
- `dev.illedrabomb.cropgenetics.content.SeedInventoryStackLimitsTest.geneticSeedIsLimitedToOneEvenInChestWithSixtyFourCapacity()` (failure): java.lang.IllegalStateException: Registry is already frozen – `TEST-dev.illedrabomb.cropgenetics.content.SeedInventoryStackLimitsTest.xml`

## Weiteres Vorgehen

Codex prüft `gradle.log` und die oben verlinkten XML-Dateien auf Ursachen.
Nur eindeutig kritische P0/P1-Funde minimal selbst beheben; P2/P3 im Chat melden.
Minecraft-/Mineflayer-/Multiplayer-Tests bleiben gesondert freigabepflichtig.
