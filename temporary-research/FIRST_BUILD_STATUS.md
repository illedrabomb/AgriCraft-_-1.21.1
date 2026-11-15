# Erster CI-Build und technische Validierung
Stand: 2026-11-15.

## CI eingerichtet
- Workflow: `.github/workflows/neoforge-1211.yml`
- Workflow-Run: https://github.com/illedrabomb/AgriCraft-_-1.21.1/actions/runs/43705289444
- Job: Java 21 (Temurin), Gradle 9.2.1 via setup-gradle, `gradle --no-daemon --stacktrace build` in `neoforge-port/`.
- Bei diesem Dokumentationsstand: GitHub-Run `in_progress` (Setup/Checkout/Java/Gradle erfolgreich, Build-Schritt läuft).
- **Noch kein nachgewiesener Build-Erfolg, kein Test-Ergebnis und keine Client-/Server-Tests.**
- CI-Workflow ist bewusst auf den Featurebranch beschränkt, damit altes Forge-1.18.2-Root nicht versehentlich gebaut wird.

## Wo suchen, wenn der Run fehlschlägt?
1. Build-Link öffnen und Compile-/Test-Fehler aus dem Schritt `Compile and run unit tests` erfassen.
2. Gradle-Pluginversion, NeoForge 21.1.265, Parchment-Konfiguration, serverseitige AddReloadListenerEvent-Signatur und Mapping der SimpleJsonResourceReloadListener-Klasse prüfen.
3. `AgriPlantsParserTest` und `GeneticsTest` unterscheiden: Parser-JUnit-Tests vs. Java-Compiler-/Gradle-Konfigurationsfehler.
4. Änderungen im selben Branch vornehmen; erneuter `push` startet neue CI.
5. Erst nach grünem Run den Code als „kompiliert und Unit-Tests bestanden“ markieren. Minecraft-Ingame-Test separat.

## GitHub API Snapshot
Am 2026-11-15 läuft ein Push-Run mit ID `43705289444`; Job-ID `132784938911`.
