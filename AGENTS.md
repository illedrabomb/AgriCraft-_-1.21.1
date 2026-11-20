# AgriCraft NeoForge 1.21.1 – Anweisungen für Entwicklungsassistenten

## Einstieg
1. Zuerst `docs/AI_START_HERE.md` lesen und Branch/HEAD in GitHub prüfen.
2. Danach nur die für das aktuelle Arbeitspaket verlinkten Dokumente und betroffenen Quelldateien lesen. Historische Protokolle nicht pauschal vollständig laden.
3. Aussagen der Kurzdatei stets gegen den aktuellen Code/Commit verifizieren, wenn sie die Änderung beeinflussen.

## Geltungsbereich
- Repository: `illedrabomb/AgriCraft-_-1.21.1`.
- Entwicklungsbranch: `integration/neoforge-1211-consolidation`; niemals ungefragt auf `master` arbeiten oder mergen.
- Hauptmod: `neoforge-port/`; Minecraft 1.21.1, NeoForge, Java 21.
- Pam's HarvestCraft / Farmer's Delight als optionale Integrationen bzw. Add-ons behandeln; MineColonies erst später als separates optionales Add-on.

## Umsetzung und Verifikation
- Zusammenhängende Arbeitspakete bevorzugen; Arbeiten und Risiken nachvollziehbar dokumentieren.
- Bestehende Vereinbarung: automatisierte Builds/JUnit, Minecraft- und Multiplayer-Tests erst nach ausdrücklicher Freigabe des Nutzers. Statische Analyse ist keine Testausführung.
- Niemals `Test geschrieben` mit `Test bestanden` gleichsetzen. Teststatus nur mit Commit, Datum, Umgebung, Ergebnis und Beleg als bestanden markieren.
- Hauptmod zunächst separat verifizieren; Add-ons erst nach vereinbartem Hauptmod-Gate testen.
- Keine ungefragten Löschungen oder Überschreibungen lokaler Test-Mods, Saves oder Testergebnisse.

## Dokumentation ohne Duplikate
- `docs/AI_START_HERE.md`: aktueller Einstieg, nächste Aktion, HEAD-Bezug und wesentliche Blocker (kurz halten).
- `docs/MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md`: verbindlicher Prüfstatus und IDs.
- `docs/MAIN_MOD_TEST_GATE_2026-11-19.md` sowie `docs/MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md`: ausführliche Testverfahren.
- `docs/INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md` und `docs/PROJECT_STATUS.md`: historische Detailprotokolle; nicht mehr für jede kleine Änderung dieselbe Zusammenfassung anhängen.
- Technische Entscheidungen in der betreffenden Fachdokumentation oder im Commit erklären; im Schnelleinstieg nur verlinken.
- Bestehende Historie nicht ohne gesonderte Prüfung löschen oder umstrukturieren.

## Fehlerzuordnung bei dieser Dokumentationsumstellung
- Die Einführung von `AGENTS.md` und `docs/AI_START_HERE.md` am 20.11.2026 ist als **mögliche Fehler-/Abweichungsursache** bei künftigem Chatwechsel, falschem Kontext oder übersehenen Anforderungen zu berücksichtigen.
- Zum Eingrenzen die Einführungskommit(s) dieser Dateien im Git-Verlauf mit dem vorherigen Branch-Stand vergleichen. Erst prüfen, dann gezielt revertieren; keine pauschalen Resets.
- Diese Änderung ist dokumentations-/prozessbezogen, keine nachgewiesene Änderung des Minecraft-Laufzeitverhaltens.
