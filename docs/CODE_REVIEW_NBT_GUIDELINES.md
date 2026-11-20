# AgriCraft – schlankes Code-Review und NBT-/Data-Component-Audit

**Gültig für:** Minecraft 1.21.1, NeoForge, Java 21, Branch `integration/neoforge-1211-consolidation`.

## Zweck und Grenzen
Nach jedem größeren zusammenhängenden Code-Arbeitspaket ein **statisches Review** der tatsächlich geänderten Pfade durchführen. Bei Änderungen an Save-/Load-, ItemStack-, Inventar-, Netzwerk- oder Rezeptlogik zusätzlich das Datenintegritäts-Audit anwenden. Kein Automatismus für Gradle, JUnit, Minecraft oder CI: Diese Ausführungen bleiben bis zur ausdrücklichen Nutzerfreigabe gesperrt.

Öffentliche Vorlage (als Prüfliste ausgewertet, **nicht** als Fremdskript installiert):
- https://github.com/jasonkneen/agent-skills/blob/main/.claude/skills/code-review/SKILL.md

**Technische Autorität bei versionsabhängigen Fragen:** Offizielle NeoForge-1.21.1-Dokumentation:
- https://docs.neoforged.net/docs/1.21.1/items/datacomponents/
- https://docs.neoforged.net/docs/1.21.1/items/
- https://docs.neoforged.net/docs/1.21.1/blockentities/

## A. Review jedes größeren Codepakets
- [ ] Aufgabenanforderung und tatsächlichen Diff vergleichen; Datei-/Commit-Kontext lesen.
- [ ] Korrektheit und Randfälle: null/leer/fehlende Registry-IDs, verwaiste Referenzen, Reload, entfernte optionale Mods.
- [ ] Client-/Server-Autorität, Mehrspieler-Zugriff und mögliche Duplikation oder Itemverluste prüfen.
- [ ] Änderungen an Events, Registern, Menüs und optionalen Modklassen mit exakter NeoForge-1.21.1-API abgleichen.
- [ ] Fehlerpfade, Performance (insbesondere Tick-/Reload-Pfade), unnötige Komplexität und ungewollte Verhaltensänderungen prüfen.
- [ ] Prüfen, ob vorhandene Regressionstests aktualisiert bzw. neue Tests **geschrieben** werden müssen; die tatsächliche Ausführung getrennt behandeln.

## B. Zusätzliches NBT-/Data-Component-Audit, wenn betroffen
- [ ] `ItemStack`-Kopien, Counts und unbekannte/fremde Components: kein unbeabsichtigtes `shrink`, Überschreiben, Duplizieren oder Erzeugen frischer Default-Stacks.
- [ ] Persistente `Codec`- und Netzwerk-`StreamCodec`-Wege getrennt prüfen; sensible/genetische Daten nicht unbeabsichtigt an bloße Zuschauer synchronisieren.
- [ ] `BlockEntity.saveAdditional/loadAdditional`, `super`-Aufrufe, `setChanged()`, Save/Reload und wiederverwendete leere Slots prüfen.
- [ ] Alte NBT-Versionen/Migration, fehlende optionale Mods und unbekannte/ungültige Pflanzen-IDs ohne Absturz oder stillen Datenverlust behandeln.
- [ ] Analyzer, Seed Chest, Journal, Crop Sticks, genetische Seeds und Original-Seed-Rückwandlung als zusammenhängende Datenpfade betrachten.
- [ ] Rezept-Restzutaten und wiederverwendbares Papier: Count und Komponenten bei Einzelcrafting und Shift-Klick berücksichtigen.
- [ ] Für unbewiesene Laufzeiteigenschaften einen konkreten JUnit- oder manuellen Testfall mit Soll-Ergebnis festhalten.

## Befundformat (knapp, überprüfbar)
| Feld | Inhalt |
| --- | --- |
| Priorität | BLOCKER / HOCH / MITTEL / NIEDRIG |
| Fundstelle | Pfad, Methode und nach Möglichkeit Zeile bzw. Commit |
| Ursache | Konkreter Codepfad und Auslöser |
| Auswirkung | Erwarteter Fehler, Datenverlust, Crash oder Performanceeffekt |
| Korrektur | Kleinste sichere Änderung bzw. überprüfbare Empfehlung |
| Nachweis | Statischer Fund / geschriebener Test / ausgeführter Test mit Commit und Log |

Keine unbelegten Pauschalaussagen (`sicher`, `getestet`, `kompatibel`). Keine rein stilistischen Kommentare ohne echten Nutzen als BLOCKER melden.

## Abschluss je Arbeitspaket
1. Kritische Befunde beheben oder ausdrücklich als Risiko und offene Aufgabe aufnehmen.
2. Geänderte Testfälle in `docs/MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md` eintragen, **ohne** ungestartete Laufzeittests abzuhaken.
3. Den kurzen Einstieg `docs/AI_START_HERE.md` nur aktualisieren, wenn sich aktueller Arbeitsstand, Blocker oder nächster Schritt ändern; keine identischen Chronikabsätze in mehreren Dateien.
4. Den jeweiligen Review-Commit selbst bei späteren Problemen als mögliche Fehlerquelle berücksichtigen. Alte Dokumentation und Git-Historie nicht löschen.
