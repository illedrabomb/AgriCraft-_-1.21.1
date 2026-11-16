# Branch-Strategie: AgriCraft NeoForge 1.21.1

**Beschluss:** 16.11.2026. **Status:** `testing` wurde von `entwicklung` abgezweigt. Es wurden **keine Tests gestartet**.

## Zuständigkeiten

1. **`feature/*`**: isolierte Implementierung einzelner größerer Features oder Featureserien. Aktuell u. a. `feature/neoforge-1211-foundation` und `feature/pflanzen-mutationen`. Ohne Freigabe keine Builds/Tests.
2. **`entwicklung`**: Integrations- und laufender Entwicklungsstand. Features werden erst nach gezielter Sichtung hierhin übernommen. Die zuvor schriftlich festgelegte Versionsregel gilt weiterhin: **bei jedem Commit auf `entwicklung`** `mod_version` im selben Commit um einen Patch erhöhen, inklusive Dokumentationscommits; `-alpha` bleibt bestehen. Integration impliziert noch keine bestandenen Tests.
3. **`testing`**: gesonderter Test-/Abnahmebranch, erstellt am 16.11.2026 **auf Grundlage von `entwicklung`**. Ein klar identifizierter Entwicklungscommit wird für einen Testzyklus nach `testing` übernommen. Hier dürfen Builds, Minecraft-Starts, CI, Importer und Gameplay-Tests **erst nach ausdrücklicher Nutzerfreigabe** ausgeführt werden. Fehlerkorrekturen primär auf Feature-/Entwicklungszweigen erarbeiten, dann erneut zur Abnahme übernehmen. Beim Veröffentlichen von Testresultaten stets SHA und Testumfang dokumentieren.
4. **`master`**: stabiler, freigegebener Stand. Ein getesteter, eindeutig identifizierter Commit darf erst nach bestätigter Abnahme hierhin gelangen. Weder Feature- noch Integrations- oder Test-Branches werden automatisch nach `master` übernommen.
5. **`docs/*`**: Planung/Dokumentation ohne Freigabe von Gameplay-Features.

## Ablauf

`feature/*` → Code-Review/Zusammenführen → `entwicklung` → Abnahmekandidat in `testing` → **autorisierte** Build-/Gameplay-Tests und Fehlerbehebung → ausdrückliche Bestätigung → `master`.

## Wichtig für die aktuelle Portierung

Der sehr große, noch nicht kompilierte NeoForge-Code liegt aktuell auf `feature/neoforge-1211-foundation` und wurde **nicht automatisch** nach `entwicklung`, `testing` oder `master` übertragen. Die Existenz des Testbranches allein berechtigt **keinen** Testlauf.

Dauerhafte Übergabepunkte: `docs/PROJECT_STATUS.md`, `neoforge-port/README.md`. Vor Integrationsaktionen Branch-HEADs prüfen und keine bestehende Version versehentlich überschreiben.
