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
- [Zentrale Hauptmod-Prüfliste](MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md): ST-01 bis ST-17 als **statisch** geprüft; TG-/GE-/ER-/UI-Laufzeittests weiterhin offen.
- Ältere lokale Erfolge vom 17.11.2026 belegen **nicht** den inzwischen veränderten Branch.
- Zuletzt dokumentierte Quelländerungen betreffen u. a. Journal-Nachtrag, Client-Belegungsprognose am Analyzer sowie abgesicherte Crop-Sticks-Lagenwechsel. **Nicht im Spiel bestätigt.**
- Der Pflanzungs-/Kreuzungsübergang verwirft ein neu gesetztes Genom wieder, falls `PLANTED` nicht als Blockzustand übernommen wird (`CR-14` / `GE-08`); die Interaktion bleibt bis zum späteren Test unbestätigt.
- Auch Wachstum und die Standard-/internen Gourd-Fruchtübergänge bestätigen Erfolg nur nach übernommenem Blockzustand (`CR-15`, `GE-09`, `ER-09`); ingame unbestätigt. **Workflow:** gemäß `AGENTS.md` ein gemeinsamer Commit pro vollständigem Arbeitspaket (Code + Prüffälle + Doku).
- Save-/Datapack-Reload: Stage-Remap und Bereinigung alter Fruchtkennungen übernehmen Daten erst nach bestätigtem Blockzustand (`CR-16`, `GE-10`, `ER-10`). Nur statischer Befund.
- **Fruchtmodell:** Neue Kürbis-/Melonenfrüchte sind intern; physische Nachbarfrüchte mit Besitzer-Token betreffen alte Save-Stände. `CR-17` trennt beide Fälle für den Testabend. Kein Gameplay-Test.
- Ein [statisches Seed-/Inventar-/NBT-Audit](SEED_DATA_INTEGRITY_STATIC_AUDIT_2026-11-20.md) zu Quellstand `5254fbba` dokumentiert einen **mittleren Integrationsrisikofall** bei direktem, übergroßem `Container.setItem`; reguläre Spielermenüs sind dadurch nicht als fehlerhaft nachgewiesen. Kein Laufzeittest.
- Nächste Aktion: aktuellen HEAD und offene Quellarbeit abgleichen; nur noch für das aktuelle Arbeitspaket notwendige Änderungen durchführen. Für Tests gesondert Freigabe einholen.
- Gemeinsamer Core-only-Test ist für **Sonntagabend, 22.11.2026** ins Auge gefasst; der [kurze Testabend-Ablauf](SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md) ist vorbereitet, **nicht freigegeben oder ausgeführt**. Fehlende Freigabe bleibt Blocker für jede Testausführung.

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
