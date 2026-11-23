# AgriCraft NeoForge 1.21.1 – Übergabe an einen Chat ohne Projektkontext

**Aktueller technischer Nachweis (23.11.2026):** Nach erneuter ausdrücklicher Freigabe besteht Quellcommit `e0670fae3ee63ec0552625a8750de0d38d57ca78` den gezielten ST-63-Test (1/1) und das Hauptmod-Gate **220/220 JUnit, :test :build erfolgreich**. [Bericht, Logs und JAR-Hash](test-results/core-regression-2026-11-23-st63/README.md). Keine Quelländerung im Nachlauf. Gameplay/GUI/Kompatibilität/Multiplayer und Beta-Freigabe bleiben offen; weitere Läufe separat freigeben.

Die folgende Übergabe bleibt als historische Momentaufnahme **vor diesem Nachtest** erhalten.

**Stand:** 23.11.2026 · **Status:** Hauptmod in Integration, **kein erfolgreicher aktueller Core-Gate-Abschluss und keine Beta-Freigabe**. Diese Datei ist eine Momentaufnahme; **GitHub-HEAD vor jeder Folgearbeit neu abrufen**.

## Sofortiger Start im neuen Chat

> Wir setzen die AgriCraft-Portierung auf NeoForge 1.21.1 fort. Bitte lies im GitHub-Repository zuerst `AGENTS.md` und `docs/AI_START_HERE.md`, prüfe den tatsächlichen Stand des Integrationsbranches sowie den neuesten Testbericht und bearbeite dann das nächste zusammenhängende **Hauptmod**-Arbeitspaket. Melde dich kurz auf Deutsch. Ohne meine jeweils ausdrückliche Freigabe darfst du **keine Gradle-, JUnit-, CI-, Minecraft-Tests oder Simulationen ausführen**. Testquellen schreiben und statisch prüfen ist erlaubt. Änderungen inklusive Dokumentation gebündelt auf dem Integrationsbranch committen. Keine Änderungen auf `master`. Historische Testresultate nicht als aktuellen Erfolg ausgeben.

- **GitHub:** https://github.com/illedrabomb/AgriCraft-_-1.21.1
- **Repository:** `illedrabomb/AgriCraft-_-1.21.1`
- **Aktiver Branch:** `integration/neoforge-1211-consolidation` (nicht `master`)
- **Hauptmod:** `neoforge-port/` · Minecraft **1.21.1** · **NeoForge 21.1.265** · **Java 21**
- **Aktuellen Commit immer direkt über GitHub bestimmen.** Vor dem ST-63-Fix lag der Branch bei `17806c7d694f2575119f18f44bced1d97d820733`; dieser historische SHA ist **nicht** der HEAD nach ST-63.
- **Orientierung:** `docs/AI_START_HERE.md`, `docs/MAIN_MOD_VERIFICATION_CHECKLIST_2026-11-20.md`, `docs/CORE_BETA_READINESS_AUDIT_2026-11-23.md`, `docs/CODEX_TEST_TRIAGE_POLICY_2026-11-22.md`.

## Konkreter Fehler aus dem jüngsten Codex-Bericht

Beleg: `docs/test-results/core-regression-2026-11-23/README.md` und dort referenzierte Logs.

1. Ein auf dem **PC des Nutzers ausdrücklich freigegebener** Core-Testlauf am **23.11.2026**, Ausgangscommit `4eddb68c`, meldete zwei Java-Kompilierfehler wegen generischer Typinferenz bei der GUI-Menüregistrierung. Der lokale Codex hat diesen **P1-Fix** in `CropGeneticsClient` bereits angewandt/committet; der nächste Lauf kompilierte die Java-Quellen.
2. Der Folgelauf auf lokalem Zwischenstand `1c1b7cd4` erreichte **219 von 220 JUnit-Tests erfolgreich, 0 übersprungen, 1 fehlgeschlagen**: `SeedChestRestrictedMenuTest.wrapperDelegatesEveryPlacementToContainerRules` (Zeile 53). Daher scheiterte `:test`, `:build` wurde **nicht** erfolgreich beendet.
3. Ursache: `SeedChestRestrictedMenu.restrictPlacement` hat im anonymen `Slot`-Wrapper in `mayPlace` versehentlich `Slot.index` (Menüindex) für `container.canPlaceItem(...)` verwendet. Im Test unterschied sich der Menüindex 0 vom tatsächlichen Containerindex 5. **ST-63 korrigiert den Produktionscode** auf `container.canPlaceItem(getContainerSlot(), stack)`, die bestehende Regressionstestquelle bleibt unverändert.
4. **Wichtig:** Für **ST-63** wurde noch **kein** erneuter Build, JUnit-, Minecraft- oder CI-Lauf durchgeführt. Das Problem ist statisch plausibel korrigiert, aber der Fix nicht durch Tests bestätigt. Es gibt kein grünes neues Testgate.
5. Die zusätzlich dokumentierten **11 Python-Parser-Tests** bestanden auf dem lokalen PC mit Java **21.0.12.1+1** und Gradle **9.2.1**. Zwei veraltete EventBusSubscriber.Bus-Warnungen gelten als P3, nicht als bewiesener Beta-Blocker.
6. Ein erzeugtes Kandidaten-JAR `cropgenetics-0.2.14-alpha.jar` (403187 Bytes, SHA-256 `7408c8226446f85af40299df7161eae96c6c2c11be635f2b3350e42869308a2c`) ist **nicht freigegeben** und nach dem fehlgeschlagenen Gate **keine** aktuelle Beta-Basis.

**Testbefugnis:** Die Freigabe des Nutzers für den früheren lokalen Lauf überträgt sich **nicht automatisch** auf diesen oder einen neuen Chat. Vor einem neuen `:test`, `:build`, CI- oder Spieltest wieder explizit fragen und die genehmigten Schritte begrenzen. Keine Tests starten, nur weil ein Skript bereitliegt.

## Weitere belegte Historie

- Der ältere freigegebene Core-Lauf vom **22.11.2026** auf `5ec9c3c7`: **212/212 JUnit**, `:test :build` erfolgreich. Dieser Nachweis gilt **nicht** für den inzwischen geänderten Branch.
- Gameplay-Core-Etappe 2 am **22.11.**: **4 pass, 2 partial, 2 P2-fail**. Betroffen `UI-01` (Analyzer/Chest-Slots) und `UI-09` (Journal-Direkteinlage). Anschließende Quellfixes sind vorbereitet, auf neuerem HEAD **noch nicht** durch Gameplay bestätigt.
- Kein erfolgreicher aktueller Multiplayer-Gesamttest, keine vollständige neue GUI-/JEI-/Mod-Kompatibilitätsabnahme; alte Teilresultate nicht hochrechnen.

## Zusammenhängende Hauptmod-Änderungen, die noch Laufzeitnachweise brauchen

- **Genetik/Seed-Stacks:** Gleiche Art + gleiche Genwerte + gleiche relevante Komponenten stapeln bis 64 **unabhängig von der Elternherkunft**; abweichende Gene/Komponenten nicht. Befehl `/cropgenetics testseed` mit Arten-Tabvervollständigung und Ganzgenom-Fall.
- **GUI:** Klassischer Seed-Analyzer mit eigenem Menü, lokalisierter Gen-/Artanzeige; separat geöffnete Seed Chest mit gefiltertem 3×9-Vanilla-Menü; kombinierte Chest-/Analyzer-Slots und Verschieben; Journal-Direkteinlage. Die umfangreichere GUI-Neugestaltung wurde vorerst **hinter die vollständigen Tests** gestellt; falls diese viele Fehler zeigen, Priorität neu bewerten.
- **HUD/Journal:** F10 als Standard zum Umschalten des eigenen Crop-HUD; im Journal wird die **aktuelle** belegte Taste aus der Clientkonfiguration angezeigt; sie kann dort durch Klick geändert werden.
- **Ernte:** Gain-basierte Kürbis- und Melonendrops (Melonenscheiben, Kürbis bis 4 je Zufallsverteilung), einstellbarer Drop-Multiplikator **0..64** mit Adminbefehl, finaler Stack-Grenze 64.
- **Server/JEI:** Synchronisierte Pflanzbedingungen mit Reload-/Client-Lifecycle; DE/EN-Hinweise; Dedicated-Server-Grenzen und JEI-Anzeige müssen im tatsächlichen Spiel geprüft werden.
- **Kompatibilität:** Jade und Identify explizit testen; WTHIT als optionaler Overlay-Kontext. Fremde Overlays dürfen keine unanalysierten Gene offenlegen.
- **Genetik-Balance:** `BAL-01` und `BAL-02` sind noch nicht statistisch abgenommen; **Simulationen nur nach Freigabe**.

## Kommende gebündelte Tests (noch nicht alle ausgeführt)

- **Neue Testsuite heißt 1.0, nicht v9:** `docs/test-tracker/AgriCraft-Core-Regression-2026-11-22-v1.0.json`. Sie enthält **91 manuelle Fälle**, davon **89 Core** und **2 separat gesperrte optionale Mod-Support-Fälle**, sowie **35 Kopierbefehle**. Alte v8-Ergebnisse und Suiten separat aufbewahren.
- **Multiplayer** steht als eigener Schwerpunkt mit **23 Fällen**. **Mod-Support** ist ein weiterer eigener Schwerpunkt; `MOD-01` (Pam's HarvestCraft 2), `MOD-02` (Farmer's Delight) sind **nach** erfolgreichem Hauptmod-Gate vorgesehen. MineColonies später separat.
- **Pflichtregressionen nach ST-63:** zuerst `SeedChestRestrictedMenuTest.wrapperDelegatesEveryPlacementToContainerRules`, anschließend gesamtes freigegebenes Hauptmod-JUnit-/Build-Gate; danach erst autorisierte Gameplay-Fälle `UI-01`, `UI-09`, `UI-15`, `UI-16`, `GE-13`, `ER-15`, `UI-12` (Jade), `UI-13` (Identify), Journal/F10, JEI/Server, Save-Reload und Multiplayer.
- **Ablauf:** `docs/CODEX_SUNDAY_AUTOMATED_CORE_GATE_2026-11-22.md`, `docs/SUNDAY_CORE_TEST_RUNBOOK_2026-11-22.md`, `docs/test-tracker/README.md`. Skripte `neoforge-port/scripts/core-test-gate.ps1` (Windows) und `.sh`. Der `gradlew` im Repo-Root ist ein **alter Forge-7.4-Wrapper**, nicht der passende NeoForge-Wrapper; lokaler PC hatte systemweites Gradle 9.2.1. Nicht eigenmächtig installieren oder Tests starten.
- **Codex-Triage nach Freigabe:** ausschließlich klar ursächliche, kritische **P0/P1**-Fehler möglichst minimal selbst reparieren; P2/P3 mit Reproduktionsschritten und Logs an den gemeinsamen Chat melden. Erst wieder zusammenhängender Build nach Abstimmung.
- Testergebnisse **immer** mit Branch, Commit-SHA, Datum, Umgebung, JAR-Hash, Fall-ID und Logs dokumentieren; fehlerhafte historische Berichte nicht rückwirkend auf „pass“ setzen.

## Empfohlener nächster Schritt

1. Repository-HEAD, ST-63-Fix, Report und die offenen Testfälle gegen die echte GitHub-Quelle prüfen; auf doppelte/abweichende Commits achten.
2. Ohne separate Testfreigabe nur statische, eng begrenzte Folgearbeit durchführen und jeweils Code, Testquellen und Dokumentation konsistent in **einem Commit** abschließen.
3. Bevor ein weiterer technischer Core-Gate-Lauf gestartet wird, den Nutzer ausdrücklich um Freigabe bitten. Bei Erfolg Nachweis aktualisieren und erst dann persönliches GUI-/Gameplay-/MP-Gate sowie eine mögliche **erste öffentliche Beta** bewerten.

**Wichtig für neue Chats:** Diese Übergabe enthält Entscheidungs- und Historienkontext, ist aber keine Autorisierung für Tests und keine Behauptung, dass die beschriebenen Fixes schon zur Laufzeit funktionieren.
