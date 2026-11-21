# CodeQL-Workflow: Fehleranalyse und vorbereitete Korrektur (21.11.2026)

## Nachgewiesener Fehler

- GitHub-Läufe [44908781544](https://github.com/illedrabomb/AgriCraft-_-1.21.1/actions/runs/44908781544) (21.11.) und [43755312935](https://github.com/illedrabomb/AgriCraft-_-1.21.1/actions/runs/43755312935) (15.11.) liefen auf **`master`**, Commit `fe111987fb50759aa9607fc5b68f79430c124441`.
- Beide Jobs stoppten bei **Initialize CodeQL**; Autobuild und Analyse wurden übersprungen. Der Log nennt `Resource not accessible by integration` beim Abrufen von Code-Scanning-Features bzw. Status und verwendet `github/codeql-action@v1` (veraltet).
- Dies ist **kein** CodeQL-Fund in AgriCraft und **kein** Nachweis eines Buildfehlers im Entwicklungsbranch.

## Vorbereitung ausschließlich auf dem Entwicklungsbranch

- `.github/workflows/codeql-analysis.yml`: Checkout v4, CodeQL v4, explizit `security-events: write`, `contents: read`, `actions: read`.
- Java-Analyse mit `build-mode: none` statt `autobuild`, damit **keine** Gradle-Tasks über diesen Workflow gestartet werden. Die source-only-Analyse ist weniger vollständig als ein instrumentierter Build (vor allem bei generiertem/externem Code).
- Bisherige Auslöser (`master`-Push, PR gegen `master`, Samstag-Schedule) bleiben erhalten. **Auf dem Integrationsbranch wird deshalb kein CodeQL-Lauf ausgelöst.** Keine Änderungen an `master` oder Workflow-Ausführung durch dieses Arbeitspaket.
- Auch nach einer Übernahme in `master` muss die GitHub-Code-Scanning-Funktion bzw. Berechtigung geprüft werden. Ein bloßes Action-Upgrade garantiert nicht, dass der beobachtete 403-Fehler behoben ist. Vor einem erneuten Lauf ist die separate CI-Freigabe des Nutzers einzuholen.

## Noch offen

1. Repository-Einstellungen / Code Scanning und Token-Berechtigungen durch berechtigte Person prüfen.
2. Nur nach ausdrücklicher Genehmigung die Workflow-Änderung nach `master` übernehmen und einen kontrollierten CodeQL-Lauf erlauben.
3. Erst anhand eines neuen Logs beurteilen, ob die 403-Fehler beseitigt sind. Nicht als bestanden markieren, solange `Initialize CodeQL` nicht erfolgreich abgeschlossen ist.

**Teststatus:** Nur Quell-/Log-Review; keine CI, Gradle-, JUnit-, Minecraft- oder sonstige Testausführung.
