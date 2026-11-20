# AgriCraft NeoForge 1.21.1 – Schnelleinstieg für neue Chats

**Stand:** 20.11.2026. Diese Datei ist eine Navigationshilfe, kein Ersatz für den aktuellen Git-Stand.

## In 60 Sekunden starten
1. Repository `illedrabomb/AgriCraft-_-1.21.1`, Branch `integration/neoforge-1211-consolidation` öffnen. **HEAD aktuell abrufen** – hier keine unveränderliche HEAD-SHA behaupten.
2. `AGENTS.md` beachten; dann die betroffenen Quelldateien und nur benötigte Detaildokumente lesen.
3. Vor jedem Arbeitspaket letzte Commits und offene Tests prüfen; niemals alte Dokumentaussagen blind als aktuellen Codezustand übernehmen.

## Projekt und Entscheidungen
- Hauptmod: `neoforge-port/`; Minecraft 1.21.1 / NeoForge / Java 21.
- Zusammenhängende Entwicklungsarbeit bevorzugt. **Build, JUnit, CI-Ausführung, Minecraft-/Multiplayer-Lauf erst nach ausdrücklicher Testfreigabe des Nutzers.**
- Hauptmod-Testgate vor Add-on-Testgate; Pam's HarvestCraft 2 und Farmer's Delight optional, MineColonies später separates optionales Add-on.
- Genetik-/Erntesicherheit, Datenkomponenten von Seeds, Analyzer-/Chest-/Journal-Synchronisierung und Gourd-Fruchtzyklen nicht ohne Regression berücksichtigen.

## Derzeitiges Gate (aus Dokumentation, vor neuem Lauf verifizieren)
- [Zentrale Hauptmod-Prüfliste](MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md): ST-01 bis ST-28 als **statisch** geprüft; TG-/GE-/ER-/UI-Laufzeittests weiterhin offen. [GE-12-Fallback-/Artenmutationsmatrix](CROSSBREEDING_SPECIES_FALLBACK_TESTS_2026-11-20.md) vorbereitet, nicht ausgeführt. `ST-27` korrigiert den Drei-Gen-Mutations-Testquellfall; `ST-28` ergänzt Verlust- und Teilrückerstattungsfälle.
- Ältere lokale Erfolge vom 17.11.2026 belegen **nicht** den inzwischen veränderten Branch.
- Zuletzt dokumentierte Quelländerungen betreffen u. a. Journal-Nachtrag, Client-Belegungsprognose am Analyzer sowie abgesicherte Crop-Sticks-Lagenwechsel. **Nicht im Spiel bestätigt.**
- Der Pflanzungs-/Kreuzungsübergang verwirft ein neu gesetztes Genom wieder, falls `PLANTED` nicht als Blockzustand übernommen wird (`CR-14` / `GE-08`); die Interaktion bleibt bis zum späteren Test unbestätigt.
- Auch Wachstum und die Standard-/internen Gourd-Fruchtübergänge bestätigen Erfolg nur nach übernommenem Blockzustand (`CR-15`, `GE-09`, `ER-09`); ingame unbestätigt. **Workflow:** gemäß `AGENTS.md` ein gemeinsamer Commit pro vollständigem Arbeitspaket (Code + Prüffälle + Doku).
- Save-/Datapack-Reload: Stage-Remap und Bereinigung alter Fruchtkennungen übernehmen Daten erst nach bestätigtem Blockzustand (`CR-16`, `GE-10`, `ER-10`). Nur statischer Befund.
- **Fruchtmodell:** Neue Kürbis-/Melonenfrüchte sind intern; physische Nachbarfrüchte mit Besitzer-Token betreffen alte Save-Stände. `CR-17` trennt beide Fälle für den Testabend. Kein Gameplay-Test.
- **Kreuzungstest:** `CR-18` enthält historische 2/3/4-Eltern-/Reihen-Baselines (30/37,5/45/15 %). **Neuer Reihen-Längenbonus:** 3 lückenlose reife Eltern 15 %, bis 20 Eltern 30 %; vier direkte Eltern 45 % bei Config-Multiplikator 1. Nur vier Reiheneltern vererben Gene, weitere steigern nur die Entstehungschance (`ST-22`, `GE-11`). Robustheit beeinflusst im aktuellen Code **nicht** die Kind-Entstehungschance. Keine Laufzeitprüfung.
- **Offene Balance-Aufträge [`BAL-01`/`BAL-02`](CLASSIC_THREE_STAT_GENETICS_2026-11-18.md):** Vier `1/1/1`-Eltern mit Beste-Kinder-Auslese → statistisch ≤ 70 Kinder bis `10/10/10`; 0–4 Mutationspunkte, max. +3/Wert, Mehrpunkt-Verluste und 40-%-Punkterückgewinnung. Hohe Robustheit bevorzugt die **vollständige Elternvorlage** und senkt Mutationsverluste. **Artübergreifende Schranke:** Pro Kindwert höchstens **5, außer ein Elternteil derselben Kindart besitzt bereits mehr**; eine neu entstehende dritte Art höchstens **4/4/4**, auch bei perfekten Eltern. Das ist **keine Mindestgenetik**: Verluste und Verschiebungen können zu niedrigeren Werten führen. Erst nach Mutationen deckeln, Überschuss verfällt; frühere +5/`6/6/6`-Idee **überholt**. **Arten-Deckel nun in `SpeciesGeneLimits.apply`/`CrossBreeding.attempt` eingebaut**; JUnit-Quellfälle vorbereitet. [BAL-01-Punktetabelle und direkte Robustheitsgewichtung](BAL01_GENETICS_PROPOSAL_2026-11-20.md) nun im `BreedingEngine`-Quellcode implementiert, einschließlich Reihen-/Config-Skalierung. **Unbewiesene Balance:** ≤ 70 erfolgreiche Kinder bis `10/10/10`; doppelte Reihen-Punktbremse statisch beseitigt (Reihen-Baseline 0,5); tatsächliche Reihen-/Config-Balance beim Test prüfen. **Keine Simulation, kein JUnit-/Minecraft-Test ausgeführt.**
- Ein [statisches Seed-/Inventar-/NBT-Audit](SEED_DATA_INTEGRITY_STATIC_AUDIT_2026-11-20.md) zu Quellstand `5254fbba` dokumentiert einen **mittleren Integrationsrisikofall** bei direktem, übergroßem `Container.setItem`; reguläre Spielermenüs sind dadurch nicht als fehlerhaft nachgewiesen. Kein Laufzeittest.
- Nächste Aktion: aktuellen HEAD und offene Quellarbeit abgleichen; nur noch für das aktuelle Arbeitspaket notwendige Änderungen durchführen. Für Tests gesondert Freigabe einholen.
- Gemeinsamer Core-only-Test ist für **Sonntagabend, 22.11.2026** ins Auge gefasst; der [kurze Testabend-Ablauf](SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md) und das [Hauptmod-Testgate](MAIN_MOD_TEST_GATE_2026-11-19.md) sind an die neue 3–20er-Reihe angeglichen (`DOC-07`). Basistest, GE-12-Testwelt und statistische BAL-01-Abnahme bleiben getrennte Nachweisschritte, **nicht freigegeben oder ausgeführt**. Fehlende Freigabe bleibt Blocker für jede Testausführung.
- **Webtool-Vorbereitung `DOC-08`:** [Neue Core-Testsuite Etappe 1](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v1.json) mit 16 offenen Fällen und eigener `suiteId` vorbereitet; alte 30er-Testdaten niemals ohne Export ersetzen. Weitere Fälle vor dem Testabend in einer gesondert versionierten Suite ergänzen; Import oder Testausführung bislang nicht bestätigt.
- **Webtool-Vorbereitung `DOC-09`:** [Erweiterte Core-Testsuite v2](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v2.json) enthält 51 unabhängig beschriebene Fälle inklusive `MP-01`–`MP-23`; [Bedienanleitung für externe Tester](test-tracker/README.md). v1 bleibt historisch erhalten, bestehende Tracker-Ergebnisse vor Suiteimport exportieren. Noch keine Ausführung oder Browser-Importbestätigung, weitere Spezialfälle bleiben offen.
- **Webtool-Vorbereitung `DOC-10`:** [Aktuelle vollständige deutsche Core-Testsuite v3](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v3.json) mit **68 Fällen** und [externer Anleitung](test-tracker/README.md) ist für den Testabend bevorzugt. v1/v2 bleiben Historie; **vor Suiteimport alte Ergebnisse exportieren**. Sämtliche Ausführungen/Browserimport und Sonder-Testweltvorbereitungen bleiben unbestätigt.
- **Letzter Playtest / neue UX-Aufträge (`DOC-11`):** [Statischer Review vom 21.11.](LAST_PLAYTEST_REVIEW_AND_SEED_CONDITIONS_2026-11-21.md) trennt den zuletzt dokumentierten 18.11.-Export (27 Fälle: 25 pass, `GEN-05` fail, `HUD-03` blocked) von älteren 17.11.-Befunden. **Original-Ergebnis-JSON fehlt** im Repository; vollständiger 27-Fälle-Einzelreview benötigt erneute Bereitstellung. Neu offen: **`UX-01`/`UI-10`** konkrete serverseitige Pflanzablehnungs-Meldung und **`UX-02`/`UI-11`** serverkorrekte JEI-Pflanzenstandort-Bedingungen. **Noch nicht implementiert**; v3-Tracker enthält diese neuen IDs noch nicht, künftige v4 separat versionieren.

## Dokumentenwegweiser
- [Hauptmod-Testgate](MAIN_MOD_TEST_GATE_2026-11-19.md) – Testaufbau und Prüfverfahren.
- [Sonntags-Testabend](SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md) – Reihenfolge, Stoppsignale und Ergebnis-Nachweise für den gemeinsamen Core-only-Lauf.
- [Multiplayer-/Inventar-Testfälle](MULTIPLAYER_INVENTORY_TEST_CASES_2026-11-19.md) – MP-01 bis MP-23.
- [Integration/Kompatibilität – historische Übergabe](INTEGRATION_COMPATIBILITY_HANDOFF_2026-11-19.md) – technische Entscheidungsdetails, Pam/Farmer's und ältere Commits.
- [Projektstatus – historisches Entwicklungsjournal](PROJECT_STATUS.md) – ältere Änderungen und Testergebnisse.
- [Manueller Test-Tracker](MANUAL_TEST_TRACKER.md) – Bedienung des externen Testtools.

## Dokumentationsumstellung: Fehlerquellen-Hinweis
- **Änderungspaket 20.11.2026:** Einführung dieser Kurzdatei und `AGENTS.md`, zunächst **ohne** Löschen oder Umschreiben der alten Dokumentation.
- **Einführungskommit AGENTS.md:** `d4b0f4e5a0c18e49d4b2191fd889d5f6dbcee19e`. Den Commit dieser Kurzdatei in der Git-Historie über den Betreff `docs: add fast-start guide for new chats` finden.
- Bei abweichender Assistenzentscheidung, übersehener Aufgabe oder falschem Wiedereinstieg **auch diese beiden Commits als mögliche Ursache prüfen** und mit dem Stand davor vergleichen. Keine Behauptung, dass diese reinen Dokumentationsänderungen einen Minecraft-Laufzeitfehler verursachen.
- Ältere Dokumente und Git-Historie bleiben als Rückfalloption erhalten.
