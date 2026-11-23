# Neuer Core-Build – Testlauf 23.11.2026

**Gate nicht bestanden. Nach P1-Minimalfix 219/220 JUnit-Tests erfolgreich, ein P2-Fehler offen. Minecraft nicht gestartet.**

GitHub-Ausgang: `4eddb68c8d45c614b98f45ccb285cde13dd7f7f3`, `integration/neoforge-1211-consolidation`. Neuer sauberer Checkout unter `D:\guthub repositorys\AgriCraft-core-regression-2026-11-23`. Vorhandene manuelle Instanz/Saves/Mods unverändert, keine laufenden Minecraft-Prozesse/Ports gefunden. Nutzerfreigabe: neuer Build im aktuellen Chat ausdrücklich zum Testen freigegeben. Scope nur Hauptmod, keine CI-/Add-on-/Multiplayer-Tests.

Java Temurin 21.0.12.1+1, Gradle 9.2.1, Windows. Der neue Gradle-Preflight bestätigt die tatsächlich verwendete Version. Keine Installation. Lokal erzeugte Python-Caches/work-Verzeichnisse nur über .git/info/exclude ausgeschlossen; keine Projektdatei dafür geändert.

## Ergebnisse

1. Parser-Auswertung: 11 Python-Unittests erfolgreich, Exitcode 0. Die unabhängige Parser-Prüfung erzeugte __pycache__, worauf ein erster Core-Preflight vor jedem Gradle-Task am Clean-Gate stoppte. Cache lokal ausgeschlossen, danach echter Tasklauf; kein Testfehler des Hauptmods daraus abgeleitet.
2. Erster Core-Tasklauf auf `4eddb68c`: Exitcode 1, zwei compileJava-Typinferenzfehler bei den Screen-Registrierungen in CropGeneticsClient (Zeilen 57/59 im Ausgangsstand). Kein JUnit und kein JAR in diesem Lauf.
3. Minimaler P1-Fix: bei beiden event.register-Aufrufen Menütyp und ClassicSeedAnalyzerScreen<Menütyp> explizit angeben. Keine Screen-, Slot-, NBT-, ItemStack- oder Spielverhaltensänderung. Statischer Diff geprüft; Java-Kompilierung dient als direkter Typnachweis, kein Implementierung spiegelnder Test ergänzt.
4. Einmaliger Folgelauf auf lokalem Zwischencommit `1c1b7cd4f990b84577537e1d3a549713f10576c5`: Haupt-/Testquellen kompilieren, neues JAR erzeugt. **220 Tests: 219 erfolgreich, 1 failure, 0 errors, 0 skipped.** Task :test scheitert, :build nicht abgeschlossen. Kein weiterer Lauf.

JAR-Kandidat: `cropgenetics-0.2.14-alpha.jar`, **403187 Bytes**, SHA-256 `7408c8226446f85af40299df7161eae96c6c2c11be635f2b3350e42869308a2c`. Kein freigegebenes/abgenommenes Spielartefakt: Core-Gate rot. Zwischencommit 1c1b7cd4 bleibt lokal, Veröffentlichungscommit bündelt identischen Quellfix mit Bericht; Git-/JAR-Zuordnung nicht mit einem Pass verwechseln.

## Offener P2-Befund – Seed-Chest-Slotfilter

Fehlgeschlagen: `SeedChestRestrictedMenuTest.wrapperDelegatesEveryPlacementToContainerRules()`, Testzeile 53. Fake-Container akzeptiert Wheat-Seeds ausschließlich in Container-Slot 5. `allowed.mayPlace(WHEAT_SEEDS)` liefert false statt true.

Fundstelle: `SeedChestRestrictedMenu.restrictPlacement`, anonyme Slot-Unterklasse. Der lokale Wert `index = original.getContainerSlot()` wird im Methodenrumpf von dem geerbten öffentlichen `Slot.index` verdeckt. `mayPlace` liest daher den Menüindex statt den Container-Slotindex. Die statische Bytecode-Auswertung in slot-filter-bytecode.txt zeigt ausdrücklich `getfield index:I`; kein eingefangener lokaler Slotindex. Der isolierte Testslot wurde nicht per addSlot ins Menü eingetragen und hat Menüindex 0, während sein Container-Slot 5 ist. Im normalen 3x9-Menü können beide Indizes zufällig übereinstimmen; ein konkreter Gameplay-/Datenverlustfehler ist damit noch nicht bewiesen.

Korrekturvorschlag für gemeinsamen Chat: in mayPlace explizit `getContainerSlot()` oder einen eindeutig anders benannten eingefangenen Container-Slotindex nutzen. Anschließend betroffenen Test und Core-Gate sowie UI-16-Livepfad prüfen. **P2 nicht selbst korrigiert; keine Assertion abgeschwächt oder Test deaktiviert.**

Zwei bestehende Deprecation-Warnungen zu EventBusSubscriber.Bus bleiben P3, ohne Änderung.

## Belege und nächster Schritt

[gate-runs/](gate-runs/) enthält beide Manifest-/Gradle-/Exitcode-/Parser-Berichte. [junit/](junit/) enthält frische XMLs, [html/](html/) den vollständigen JUnit-Bericht. Die 11 Parser-Tests sind separat protokolliert. Kein Minecraft-/Regression-1.0-Fall als bestanden markiert. Keine Client-/Serverprozesse gestartet; vorbereitete leere Testverzeichnisse bleiben lokal.

Zuerst P2-Korrektur gemeinsam entscheiden und erneut testen. Die frühere 212/212-Abnahme und Gameplay-Ergebnisse gehören zu älteren Commits und werden nicht auf den neuen Build übertragen.
