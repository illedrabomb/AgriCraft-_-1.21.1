# Lokaler Core-Vorlauf – 22.11.2026

**Ergebnis: beide Läufe fehlgeschlagen, JUnit nicht erreicht, kein JAR erzeugt.**

Ausgangspunkt: `7288dbc7bd8e83585de2ac7094f1f03203e30c35`. Windows 11, Temurin 21.0.12.1+1, Gradle 9.2.1; ausdrückliche Nutzerfreigabe im lokalen Codex-Chat. Ausschließlich `:test :build --no-daemon --console=plain`, kein Minecraft-/CI-Lauf.

- [Lauf 1](20261122T165129Z-7288dbc7bd8e83585de2ac7094f1f03203e30c35/summary.md): `compileJava` scheitert an fehlenden schließenden Klammern in `CropDataCommand.register`.
- [Lauf 2](20261122T165251Z-cbe6f23a18e71bde5fdfedd8c53fa2f3395aeea7/summary.md): erste Korrektur unvollständig, gleicher Compilerfehler.
- Endkorrektur: je eine Klammer am `testseed`-Zweig und am Ende der Registrierung ergänzt. Nur statisch geprüft, **kein erfolgreicher Build oder Test nachgewiesen**. Weitere Ausführung nach Triage-Grenze gesondert freizugeben.

Die beiden Laufverzeichnisse enthalten unveränderte Rohlogs, Manifest, Exitcode, Startmarker und automatisch extrahierte Zusammenfassung. Die darin genannten lokalen Zwischencommits `cbe6f23a…` und im Bericht `48d5d37c…` wurden nicht einzeln veröffentlicht. Dieser gemeinsame Veröffentlichungscommit enthält beide Klammerkorrekturen, die Prüfstatus-Dokumentation und alle Belege. Der Textbericht beschreibt den Zustand vor Veröffentlichung; seine Angabe „nicht gepusht“ bezieht sich auf diese lokalen Zwischencommits.

Manuelle Minecraft-/Multiplayer-Abnahme und die 86 v8-Fälle bleiben offen. Keine JAR-Datei und daher kein JAR-SHA-256 vorhanden.
