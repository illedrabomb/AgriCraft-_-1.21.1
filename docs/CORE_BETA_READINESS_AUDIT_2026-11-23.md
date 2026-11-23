# Core-Beta-Bereitschaft – Restarbeiten und Build-Einstieg (23.11.2026)

**Statischer Abgleich**, keine Gradle-/JUnit-/Minecraft-/CI-Ausführung oder Simulation. Grundlage: GitHub-Branch `integration/neoforge-1211-consolidation`, vor diesem Dokumentenpaket HEAD `eb69f3973090827c9c9e82232732ca7e8c8376a3`. Dieser SHA ist nicht automatisch der nächste Build-HEAD. Hauptmod `neoforge-port/` zuerst; Add-ons bleiben bis nach dem Core-Gate getrennt.

## 1. Build-Weg: zwei verschiedene Gradle-Projekte

- Das **Repository-Root** enthält `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` und `gradle/wrapper/gradle-wrapper.properties` mit **Gradle 7.4**. Das Root-`settings.gradle` benennt das **historische** Projekt `AgriCraft` samt `AgriCore`/`InfinityLib`. Dieser Wrapper ist **kein nachgewiesener NeoForge-1.21.1-Buildweg**.
- Das eigenständige **NeoForge-Projekt** beginnt erst in `neoforge-port/`: `build.gradle`, `settings.gradle`, Java-Toolchain **21**, NeoForge **21.1.265**, eigenständige optionale Gradle-Subprojekte. **Dort gibt es aktuell weder `gradlew` noch `gradlew.bat`**. Historische Testanweisungen, die im Unterprojekt `.\gradlew.bat` aufrufen, wurden in der Hauptmod-Gate-Anleitung korrigiert.
- Die vorhandenen `scripts/core-test-gate.ps1` und `scripts/core-test-gate.sh` erwarten nach ausdrücklicher Freigabe einen **passenden lokalen Wrapper im Unterprojekt oder `gradle` aus `PATH`**. Der belegte technische Core-Lauf vom **22.11.2026** verwendete lokal **Gradle 9.2.1**, Java 21, ergab **212/212 JUnit**, `:test :build` Exitcode 0 auf dem **älteren** Commit `5ec9c3c7`. Dessen JAR-Task war UP-TO-DATE; das beweist keinen frischen JAR-Build des aktuellen Branches.
- **DOC-41, zusätzlicher Vorlauf-Quellschutz (23.11.):** Beide Core-Starter fragen bei einem später **separat freigegebenen** Lauf vor den Tasks `gradle --version` ab und stoppen unter **8.5** (Java-21-Laufzeitminimum). Der Bericht enthält die erkannte Gradle-Version. Zwei zusätzliche Parser-Quelltests wurden geschrieben, aber nicht ausgeführt; der frühere Forge-7.4-Wrapper wird damit auch bei irrtümlicher Auswahl als Umgebungsmangel erkannt.
- Vor einem neuen Build den echten Checkout/HEAD, Java 21, Gradle-Verfügbarkeit/-Version und Dependency-Zugriff auf **der tatsächlich verbundenen Maschine** prüfen. Die vom Nutzer erwähnte Windows-PC-Installation ist damit nicht automatisch erreichbar oder verifiziert. **Kein Wrapper-Upgrade, Download oder Lauf ohne Auftrag/Freigabe.**
- **Nach erneuter ausdrücklicher Codex-Core-Gate-Freigabe**: im Ordner `neoforge-port/` den vorbereiteten Starter (`scripts/core-test-gate.ps1 -ApproveCoreTests` bzw. `bash scripts/core-test-gate.sh --approve-core-tests`) nutzen, SHA/Logs/XML/JAR-Hash auswerten; **nicht** den Root-Forge-Wrapper. Persönlicher Minecraft-Test benötigt eine **eigene** Freigabe.

## 2. Hauptmod – jetzt Code vorhanden, aber noch zu bestätigen

| Thema | Quellstand | Offener Beleg / Risiko |
| --- | --- | --- |
| Genetische Samen und `testseed`-Tabvorschläge | 64 gleiche Stack-Komponenten, Herkunft der Eltern nicht als eigener Stack-Teiler | `GE-01`, `GE-13`, `ER-15` im aktuellen JAR/Client |
| Original-Analyzer-GUI, Standalone-/verbundene Seed Chest und Sneak-Journal | Eigene Menüs/klassischer Screen, serverseitig gefilterte Slots und Shift-Klick, Journal-Dispatch-Korrektur | `UI-01`, `UI-09`, `UI-15`, `UI-16`; alte P2-Fehler sind **quelladressiert, nicht als PASS belegt** |
| Kürbis-/Melonen-Ertrag, Gain, Multiplikator 0..64, Stapelgrenzen | Quellimplementierung nach Nutzerentscheidung | `ER-12`–`ER-15`; ein Stack je Produkt und tatsächliches Item-Maximum |
| Journal-F10-Belegung/-Editor | Quellimplementierung, früherer begrenzter Teilnachweis | `UI-14`: aktuelle Tastenanzeige, Klick-Umbelegung, Esc/Entf/Backspace, DE/EN |
| Normale Karottensamen beim Kreuzen | `Genome.basic`-Quellpfad vorhanden, **keine belegte Codeursache** aus altem `STK-04/GEN-01` | `GE-01`/`GE-02` reproduzierbar mit reifen Eltern und leerem Doppelstick |
| Genetik und Artgrenzen | BAL-01/02 Quellregeln implementiert | Statistische 70-Kinder-Zielvorgabe und Artmutationsfälle noch offen |
| JEI-Serverbedingungen | Etappen A–C mit versioniertem Sync und dynamischer JEI-Kategorie vorhanden | `UI-11` samt Unterfällen: Reload, Cache, Serverwechsel, Dedicated Server |
| Jade und WTHIT | Eigene optionale Quell-Plugins vorhanden | `UI-12`/Gameplay, keine runtimebestätigte Anzeige; für Identify ist keine entsprechende spezifische Core-Integration nachgewiesen |
| Bodenchemie | `SoilOverrideInvalidation` behandelt gewöhnliche akzeptierte Spieler-Break-/Place-Events am Tick-Ende | Befehle, Kolben, Maschinen, Explosionen und bestimmte gleichartige Ersetzungen noch nicht vollständig abgedeckt; kein bewiesener neuer Datenverlust |
| Build/Gate | **Kein** technischer Lauf auf dem aktuellen HEAD | Neuer autorisierter SHA-gebundener Core-Build, JUnit, JAR-Inhalt, GUI-/MP-Lauf nötig |

Für den nächsten gebündelten manuellen Durchlauf gilt unverändert **[Core-Regression 1.0](test-tracker/AgriCraft-Core-Regression-2026-11-22-v1.0.json)** mit **91 Definitionen**: 89 Core-Fälle inklusive 23 Multiplayer-Fällen, zwei optionale Mod-Fälle erst nach Core-Freigabe. Historische v8-Belege bleiben getrennt, **keine neuen Tests vorweg als bestanden markieren**.

## 3. Nach Core-Gate – bewusst getrennte oder ungeklärte Wünsche

- **Pam's HarvestCraft 2 Crops**: vorhandenes eigenständiges Add-on zuerst nach dem Hauptmod testen; Version, Registry, Discovery und konkretes Gameplay sind nicht allein durch Quellcode bestätigt.
- **MineColonies**: eigenes Add-on danach; derzeit kein Core-Beta-Pflichtumfang.
- **Farmer's Delight**: Add-on-Quellstand vorhanden; natives Reis-/Spezialernteverhalten ausdrücklich später. **Pam's HarvestCraft 1** bleibt Legacy-Migrationsplanung, kein direktes NeoForge-1.21.1-Laufzeitmodul.
- **Identify**: eigener Integrationsadapter ist nicht als fertig belegt; zunächst `UI-13` als Koexistenz-/Datenschutztest, bei Bedarf später Featureentscheidung.
- **Enderperlen-Samen**: früher vom Nutzer **erfragt**, aber **nicht als beschlossene Core-Beta-Pflanzenart** festgelegt. Ein Ernte-Test mit `minecraft:ender_pearl` als begrenztem 16er-Produkt ist **keine** Implementierung einer eigenen Enderperlen-Pflanze.
- **Komplette AgriCore-Bodenphysik, Bewässerungsnetze, Saisonadapter**: teilweise vorbereitet, nicht automatisch Teil des ersten Beta-Gates.
- **CodeQL**: v4-Workflowkorrektur nur im Integrationsbranch; `master`/403-Folge und tatsächlicher CI-Lauf erst nach eigener Genehmigung bewerten.
- **Erste Public Beta**: Modversion derzeit `0.2.14-alpha`; eine Beta-Version/-JAR und Release-Nachweise sind noch nicht erstellt. **Suite-Titel „Core-Regression 1.0“ ist keine Modversion.**

## 4. Entscheidung zum nächsten Paket

Jetzt keinen breiten Funktionsausbau nur aufgrund historischer TODOs erzwingen. **Direkt vor dem neuen Build**: exakten NeoForge-Gradle-Startweg und lokalen Commit prüfen, alte `STK-04/GEN-01`-Reproduktion als Nachtest beibehalten, GUI/Inventar/BAL/JEI im bestehenden Tracker vollständig abdecken. P0/P1 nur mit kausalem Nachweis minimal beheben; P2/P3 gesammelt zurückmelden. Persönlicher Test und optionaler Mod-Support sind separat freizugeben.

**Grenze dieses Dokumentenpakets:** ausschließlich Dokumentation und statischer Quellenabgleich, keine Code-/Testfalländerung, keine Tests, keine Simulation, kein Commit außerhalb des Integrationsbranches.
