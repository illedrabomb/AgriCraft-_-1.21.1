# Core-Gate nach ST-63 – 23.11.2026

**Technisches Hauptmod-Gate bestanden: 220/220 JUnit, 0 Failures, 0 Errors, 0 Skipped; :test :build erfolgreich (Exitcode 0). Keine Beta-Freigabe.**

- Getesteter Quellcommit: `e0670fae3ee63ec0552625a8750de0d38d57ca78` auf `integration/neoforge-1211-consolidation`; vor dem Lauf identisch mit GitHub, sauberer Arbeitsbaum.
- Ausdrückliche Nutzerfreigabe in diesem Folgechat: „ja du hast meine Freigabe“ für zuerst den betroffenen Seed-Chest-Test, anschließend bei Erfolg das gesamte Hauptmod-Gate. Keine dauerhafte oder zusätzliche Spieltestfreigabe.
- Umgebung: Windows 11, Temurin 21.0.12.1+1, Gradle 9.2.1, Minecraft 1.21.1, NeoForge 21.1.265. Vorhandene Installationen verwendet; JAVA_HOME/PATH/GRADLE_USER_HOME nur im Testprozess gesetzt, Cache auf G:.
- Checkout: `D:\guthub repositorys\AgriCraft-core-regression-2026-11-23`; Hauptmod-Projekt `neoforge-port/`, nicht der historische Forge-Wrapper im Repository-Root.

## Nachweise

1. [Gezielter Lauf](targeted/): `:test --tests dev.illedrabomb.cropgenetics.content.SeedChestRestrictedMenuTest.wrapperDelegatesEveryPlacementToContainerRules --no-daemon --console=plain`, **1/1 bestanden**, Exitcode 0. XML vor dem Gesamtlauf separat gesichert.
2. [Gesamtlauf und Manifest](gate-runs/20261123T104121Z-e0670fae3ee63ec0552625a8750de0d38d57ca78/summary.md): freigegebener Starter `scripts/core-test-gate.ps1 -ApproveCoreTests`, **:test :build erfolgreich**, Beginn 10:41:21 UTC. 62 frische [JUnit-XML-Dateien](junit/) bestätigen **220/220**, keine Fehler/Skipped. Vollständiger [HTML-Bericht](html/index.html) und Gradle-Rohlog gesichert.
3. `:compileJava` lief im gezielten Test, im anschließenden Gesamtlauf UP-TO-DATE; `:test` wurde in beiden Läufen tatsächlich ausgeführt. `:jar` wurde im Gesamtlauf ausgeführt. Kein Clean-Rebuild behauptet.
4. Frisches `cropgenetics-0.2.14-alpha.jar`: **403199 Bytes**, SHA-256 `313fb7466b110cdf5e5c255196e387e2f4393049500705a4830b22a93f79a08e`. Lokaler Pfad: `neoforge-port/build/libs/cropgenetics-0.2.14-alpha.jar`. Hash unabhängig erneut gelesen. Kein Release hochgeladen oder in Spielinstanzen installiert.

## Bewertung und Grenzen

ST-63 ist jetzt durch den unveränderten gezielten Regressionstest und die vollständige JUnit-Suite bestätigt. Keine weitere Quellkorrektur nötig, keine Tests abgeschwächt/deaktiviert. Der Veröffentlichungscommit dieses Berichts ändert ausschließlich Dokumentation und Belege; getesteter Code bleibt dem oben genannten SHA zugeordnet.

Keine neuen P0/P1/P2-Befunde in diesen beiden Läufen. Die zwei bekannten P3-Deprecation-Warnungen aus dem früheren Lauf sind nicht korrigiert; die inkrementelle Kompilierung ist kein Nachweis ihrer Beseitigung. Python-Parser-Unittests diesmal nicht erneut ausgeführt; der Berichtsgenerator selbst lief erfolgreich. Frühere 219/220- und 212/212-Berichte bleiben unveränderte Historie.

**Nicht ausgeführt:** Minecraft-/Gameplay-/GUI-/JEI-/Jade-/Identify-/Multiplayer-/Add-on-/CI-Tests oder Balance-Simulationen. Keine Produktionswelten, Saves oder Testmods verändert. Kein manueller Fall der Core-Regression 1.0 pauschal auf PASS gesetzt. Die Suite umfasst weiterhin 91 Fälle (89 Core, 2 optionale Mod-Fälle), darunter 23 Multiplayer-Fälle.

Nächster Schritt nach eigener Freigabe: aktuelles JAR für UI-01/UI-09/UI-15/UI-16, Seed-Stacks/testseed, Ernte, Journal/F10 und Save-Reload prüfen; danach JEI/Server, Jade/Identify und Multiplayer. Balance-Nachweise bleiben separat offen. Add-ons erst nach erfolgreicher Hauptmod-Prüfung. Erste öffentliche Beta weiterhin nicht freigegeben.
