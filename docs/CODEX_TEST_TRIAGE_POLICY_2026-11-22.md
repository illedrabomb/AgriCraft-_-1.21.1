# Codex-Testtriage: minimale Eigenkorrekturen, kostensparende Übergabe

**Vereinbarung 21.11.2026:** Codex soll vor dem persönlichen Test möglichst viel **deterministisch ausführen**, aber **nur eindeutig kritische Defekte selbst beheben**. Nichtkritische Fehler mit Logs an den gemeinsamen Chat zurückgeben; weitere Builds und Entscheidungen möglichst hier gemeinsam durchführen. Dies ersetzt **nicht** die Freigabe zum Teststart.

## Entscheidungsregel für Codex

| Stufe | Befund | Codex-Aktion |
| --- | --- | --- |
| **P0 kritisch** | Reproduzierbarer Verlust oder Duplikation von Seeds/Genomen/Inventar, beschädigter Save, unbeabsichtigtes Löschen, Sicherheits- oder Datenschutzproblem | Sofort Teststopp und Belege sichern. Eng begrenzten, kausal belegten Fix **nur an sicherer Kopie/Quellcode** vorbereiten, statisch prüfen, in einem Commit dokumentieren. Re-Test/Build nur bei entsprechender Ausführungsfreigabe. Bei unsicherer Ursache **nicht** auf Verdacht ändern. |
| **P1 schwer / Gate-Blocker** | Client- oder Dedicated-Server-Crash, fehlerhafte Registrierung, deterministisch scheiternder Core-Build, harter Login-/Netzwerkfehler | Bericht und Logs sichern. Nur **eindeutig lokalisierte, minimale** Fehlerursache selbst beheben, sofern nicht riskant und im Hauptmod. Alles andere ohne Änderung zurückgeben. Ein Build-Fehlschlag allein beweist noch keinen kritischen Spieldefekt. |
| **P2 normal** | Einzelne nicht-blockierende JUnit-Fehler, falsche Regel/Balance, unklare Gameplay-Abweichung, Übersetzungs-/JEI-Anzeige | **Nicht selbst korrigieren**, nur erwartetes/tatsächliches Ergebnis, Test-ID und Fundstelle dokumentieren. |
| **P3 gering** | Kosmetik, Text, Warnungen, Testkomfort, Performance-Verdacht ohne reproduzierbaren Schaden | **Nicht selbst korrigieren**, gesammelt melden. |
| **Umgebung** | Java/Gradle/Mod-JARs fehlen, Dependency nicht erreichbar, kein PC-Zugriff, unklarer Testaufbau | **Nicht als Modfehler klassifizieren**. Keine stillen Downloads, Plugin-Installationen, CI-Starts oder riskanten Systemänderungen; Blocker und Voraussetzungen melden. |

## Sparsame Ausführung

1. **Ein** ausdrücklich freigegebener, core-only automatisierter Lauf; zuerst `build/core-gate-reports/<UTC>-<SHA>/summary.md` und nur bei Bedarf die dort genannten frischen JUnit-XML- und Build-Logs lesen statt viele interaktive Einzelschritte und wiederholte Modellaufrufe. Die Kurzfassung ersetzt keine Ursachenanalyse.
2. Bei Fehlschlag **zuerst Fehler zusammenfassen und Ursache priorisieren**, nicht pauschal alle Tests wiederholen. Das Skript führt selbst keine Reparaturen aus.
3. Eigenkorrektur nur unter P0/P1-Bedingungen oben und nur mit reproduzierbarem, kausal verstandenem Defekt. Kein selbständiger Wechsel des Mod-Designs, der Genetik-/Balancewerte oder des Testplans. Keine Veränderungen an Originalwelten/Test-Mods ohne eigene Erlaubnis.
4. Nach einer minimalen Eigenkorrektur maximal einen gezielten Nachweis unter der **bereits erteilten Ausführungsfreigabe** durchführen; bei unklarer Reichweite vorher nachfragen. Niemals einen fehlgeschlagenen Test als bestanden markieren.
5. **P2/P3 in den Chat zurückgeben**. Der Nutzer möchte die nächsten Korrekturen und Builds dort gemeinsam durchführen. Normale Build-Kommandos verbrauchen selbst keine Sprachmodell-Tokens; die Analyse und Chatbegleitung können trotzdem Kontingent beanspruchen – keine Null-Token-Garantie.

## Kompakte Übergabevorlage

```text
Branch / GitHub-HEAD / JAR-SHA:
Ausführungsfreigabe und Testumfang:
Umgebung (Java, Gradle, NeoForge, installierte Testmods):
JUnit: bestanden / fehlgeschlagen / übersprungen / Belegdatei:
Build: Erfolg / Fehlschlag / JAR-Pfad:
P0/P1 (Ursache, minimaler Fix, Commit, neuer Nachweis oder Stopp):
P2/P3 (Test-ID, erwartet/ist, Logdatei, keine automatische Änderung):
Umgebungsblocker:
Offene Minecraft-/JEI-/Multiplayer-Abnahme:
Nächster Schritt für gemeinsamen Chat:
```

**Nicht automatisch starten:** Mineflayer/NeoForge-Bot, Minecraft-Client, Dedicated Server und die **89 manuellen Fälle der v9-Sonntagssuite** sind jeweils separat zu prüfen/freizugeben. Eine Bot-Verbindung ist noch nicht nachgewiesen. [Aktuelle Import-/Sicherungshinweise](test-tracker/README.md); ältere v5-Ergebnisse bleiben historisch.
