# Lokaler Core-Vorlauf – 22.11.2026

**Ergebnis: beide Läufe fehlgeschlagen, JUnit nicht erreicht, kein JAR erzeugt.**

Ausgangspunkt: `7288dbc7bd8e83585de2ac7094f1f03203e30c35`. Windows 11, Temurin 21.0.12.1+1, Gradle 9.2.1; ausdrückliche Nutzerfreigabe im lokalen Codex-Chat. Ausschließlich `:test :build --no-daemon --console=plain`, kein Minecraft-/CI-Lauf.

- [Lauf 1](20261122T165129Z-7288dbc7bd8e83585de2ac7094f1f03203e30c35/summary.md): `compileJava` scheitert an fehlenden schließenden Klammern in `CropDataCommand.register`.
- [Lauf 2](20261122T165251Z-cbe6f23a18e71bde5fdfedd8c53fa2f3395aeea7/summary.md): erste Korrektur unvollständig, gleicher Compilerfehler.
- Endkorrektur: je eine Klammer am `testseed`-Zweig und am Ende der Registrierung ergänzt. Nur statisch geprüft, **kein erfolgreicher Build oder Test nachgewiesen**. Weitere Ausführung nach Triage-Grenze gesondert freizugeben.

Die beiden Laufverzeichnisse enthalten unveränderte Rohlogs, Manifest, Exitcode, Startmarker und automatisch extrahierte Zusammenfassung. Die darin genannten lokalen Zwischencommits `cbe6f23a…` und im Bericht `48d5d37c…` wurden nicht einzeln veröffentlicht. Dieser gemeinsame Veröffentlichungscommit enthält beide Klammerkorrekturen, die Prüfstatus-Dokumentation und alle Belege. Der Textbericht beschreibt den Zustand vor Veröffentlichung; seine Angabe „nicht gepusht“ bezieht sich auf diese lokalen Zwischencommits.

Manuelle Minecraft-/Multiplayer-Abnahme und die 86 v8-Fälle bleiben offen. Keine JAR-Datei und daher kein JAR-SHA-256 vorhanden.

## Erneuter freigegebener Lauf und Folgelauf

- Ausgang `6862a993c0778171f1538947c97472ac6534ed60`: vier weitere Compilerfehler, JUnit nicht erreicht, kein frisches JAR. Siehe Laufordner `20261122T165839Z-*`.
- Minimal korrigiert: fehlende Reader-/JsonParser-Imports, expliziter JEI-Stream-Typparameter, öffentliche ChestMenu-Konstruktorsignatur für 1.21.1. Keine Balance-/NBT-Änderung. Zwei Deprecation-Warnungen bleiben unverändert.
- Lokaler Zwischencommit `077d70f70334b87acd3aaaaa0ec4ade854779ca2`: Hauptquellen und JAR erfolgreich, Gate dennoch Exitcode 1 wegen `compileTestJava` (100 ausgegebene Fehler: Minecraft-/LWJGL-/Serialization-Klassen fehlen). JUnit nicht ausgeführt. Test-Classpath-Konfiguration als Ursache untersuchen; nicht behoben, keine weiteren Läufe. Siehe Laufordner `20261122T170001Z-*`.
- JAR `cropgenetics-0.2.14-alpha.jar`: 385545 Bytes, SHA-256 `9e272b6c9ddf97d8a96dbee64ec27b595caea704bef68c79760f0ad0e41da27a`. Kein erfolgreicher Gate- oder Spielnachweis. JAR nicht als abgenommen bereitgestellt.
- Gemeinsamer Folge-Veröffentlichungscommit enthält die vier Compilerkorrekturen und beide neuen Berichte; `077d70f7` ist nur lokaler Zwischencommit. Der Abschnitt und Bericht oben dokumentieren die früheren Läufe.

## Zwei weitere ausdrücklich freigegebene Revisionen

1. `53ec07cdef11ffa11754648a37efe9b9e7bbe2cc`: ModDev-JUnit-Integration ergänzt, jedoch vor NeoForge-Version aktiviert; Gradle bricht beim Konfigurieren ab. Eigener Konfigurationsfehler, keine Testausführung.
2. `c9a5bf9cf19abf31c1e746241d2ebd4a98249d20`: Reihenfolge korrigiert, Konfiguration erfolgreich. Test-Klassenpfad scheitert bei Gson-Auflösung: explizite 2.11.0 gegen Minecraft `strictly 2.10.1`; laut Gradle drei weitere Fehler gleicher Ursache. Keine Testkompilierung/JUnit-Ausführung und kein frisches JAR.

Endkorrektur nach Lauf 2: Test-Gson ohne eigene Versionsvorgabe, sodass Minecraft die Version bestimmt. **Nicht erneut ausgeführt**, beide Freigaben verbraucht. ModDev-Einbindung gemäß https://github.com/neoforged/ModDevGradle#unit-testing-with-junit. Keine Testfälle ausgeschlossen oder Assertions geändert. Veröffentlichungscommit bündelt finale Build-Konfiguration, Status und beide vollständigen Laufprotokolle; genannte Zwischencommits bleiben lokal.

## Freigegebener Lauf ohne Codeänderungen

Ausgangspunkt `e670f19c03739cc2927ed47f5ac34c58eac3bbdf`, Windows, Java 21.0.12.1+1, Gradle 9.2.1. `:test :build` endet mit Exitcode 1: 212 Tests, 210 erfolgreich, 2 fehlgeschlagen, 0 übersprungen. Testkompilierung erfolgreich; `:build` nicht abgeschlossen, JAR nur UP-TO-DATE (kein frisches Artefakt).

Beide Fehler: `GeneticSeedStackingTest.defaultGeneticSeedIsAlwaysUnstackable` und `SeedInventoryStackLimitsTest.geneticSeedIsLimitedToOneEvenInChestWithSixtyFourCapacity`, jeweils Zeile 13. Test erzeugt `new GeneticSeedItem(new Item.Properties())` nach Registry-Freeze; `MappedRegistry.createIntrusiveHolder` wirft `IllegalStateException: Registry is already frozen`. Belegt ist ein Testaufbauproblem, kein nachgewiesener Gameplay-Stackfehler. Vorschlag für gemeinsame Korrektur: bereits registriertes genetisches Seed-Item verwenden statt nachträglicher Item-Erzeugung; hier nicht umgesetzt.

Keine Code-/Testquelländerung, keine Reparatur, kein weiterer Testlauf. Vollständige Gate-Logs, JUnit-XML und HTML-Berichte im Laufordner `20261122T170927Z-e670f19c03739cc2927ed47f5ac34c58eac3bbdf/`. Minecraft-Abnahme weiterhin offen.

## Korrektur der zwei JUnit-Fixture-Fehler (nach Lauf 17:09 UTC)

- `GeneticSeedStackingTest` und `SeedInventoryStackLimitsTest` verwenden jetzt `CropItems.GENETIC_SEED.get()` statt `new GeneticSeedItem(new Item.Properties())`. Das registrierte Mod-Item darf nach Minecraft-Registry-Freeze weiter in `ItemStack`s benutzt werden; eine neue `Item`-Instanz darf dann nicht mehr erzeugt werden.
- Die Assertions zum Maximalstapel (ein genetischer Seed) und zur Chest-Kapazität bleiben unverändert. **Keine Gameplay-, Registry- oder NBT-Änderung.**
- Nur statisch geprüft; ein neuer `:test :build` und die Minecraft-Abnahme benötigen gesonderte Freigabe. Die beiden bisherigen Fehlresultate bleiben unverändert archiviert und dürfen nicht rückwirkend als bestanden gelten.
