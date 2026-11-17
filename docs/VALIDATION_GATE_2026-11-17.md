# Technisches Validierungstor vor dem nächsten manuellen Minecraft-Test

## Windows-Testsystem: Gradle-Quelle (bestätigt am 17.11.2026)

Auf dem Testrechner ist das separate Projekt unter folgendem Pfad vorhanden:

```text
D:\guthub repositorys\Minecraft_mods\Lena\villagerfamilytree_1.21.1
```

Dessen Datei `gradle\wrapper\gradle-wrapper.properties` enthält die vom Benutzer geprüfte Zeile:

```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.2.1-bin.zip
```

**Bedeutung:** Der Villager-Wrapper verlangt **Gradle 9.2.1**. Er ist eine bekannte lokale Referenz für die benötigte Version. Das allein bestätigt **nicht**, dass die Distribution bereits entpackt ist oder dass `gradle` im `PATH` liegt. Beim ersten Versuch mit `validate-build.ps1` meldete das Testsystem `Gradle missing on PATH`. Der andere Projektordner und dessen Wrapper-Dateien dürfen bei AgriCraft-Tests nicht geändert werden.

**Vorgehen ohne neue globale Installation:** Ein bereits entpacktes Gradle 9.2.1 zuerst in `$env:USERPROFILE\.gradle\wrapper\dists` suchen. Dann nur im aktuellen PowerShell-Fenster dessen `bin`-Ordner zum `PATH` ergänzen:

```powershell
$gradleBat = Get-ChildItem "$env:USERPROFILE\.gradle\wrapper\dists" -Filter gradle.bat -File -Recurse -ErrorAction SilentlyContinue |
    Where-Object { $_.FullName -match 'gradle-9\.2\.1' } |
    Select-Object -First 1 -ExpandProperty FullName
if ($gradleBat) {
    $env:Path = "$(Split-Path $gradleBat);$env:Path"
    gradle --version
} else {
    Write-Host 'Gradle 9.2.1 nicht im Standard-Cache gefunden; Installation/GRADLE_USER_HOME separat prüfen.'
}
```

Wenn die Ausgabe Gradle 9.2.1 zeigt, im AgriCraft-Unterordner `neoforge-port` das vorbereitete `scripts\validate-build.ps1` starten. Keine Buildfreigabe oder erfolgreicher Lauf wurde allein durch den gefundenen Wrapper bestätigt.


## Bequemer lokaler Einstieg (17.11.2026 nachgereicht)

Im Repository liegt jetzt das PowerShell-Skript `neoforge-port/scripts/validate-build.ps1` (Commit `73febc6`). Es prüft den aktiven Integrationsbranch, zeichnet den vollständigen Commit-SHA und die erkannten Java-/Gradle-Versionen auf, führt **nur** `gradle --no-daemon test build` im richtigen Unterprojekt aus und erstellt ein datiertes Build-Log sowie eine Zusammenfassung unter `neoforge-port/build/validation-logs/`.

Aus dem **Repository-Hauptverzeichnis** in PowerShell aufrufen:

```powershell
powershell -ExecutionPolicy Bypass -File .\neoforge-port\scripts\validate-build.ps1
```

Den tatsächlichen Java-21-/Gradle-9.2.1-Stand in der Zusammenfassung kontrollieren; das Skript installiert keine Toolchains und startet keinen Server/Client. **Das Skript wurde hier weder lokal ausgeführt noch technisch validiert**, weil kein vollständiger Repository-Checkout/keine Gradle-Installation verfügbar ist. Bei Fehlern zuerst Log und Summary aus `validation-logs` übergeben.



**Stand:** 17.11.2026 · **Branch:** `integration/neoforge-1211-consolidation` · **Status:** vorbereitet, **nicht ausgeführt**.

## Warum der Build jetzt der nächste notwendige Schritt ist

Seit dem letzten erfolgreichen lokalen Build und den damals 17 bestandenen Unit-Tests wurden Änderungen an JEI, Client, Pflanzenstufen, Ernte, Frucht-Diagnose und Ressourcen sowie neue Regressionstests committed. Alte PASS-Ergebnisse belegen diese Änderungen **nicht**. Es darf keine Gameplay-Abnahme behauptet werden, bevor die technischen Fehler ausgeschlossen sind.

Die verbundene GitHub-Schnittstelle kann Repository-Dateien ändern und Status abfragen, aber keinen neuen Gradle-Prozess/Workflow starten. Für den zuletzt geprüften Quellstand `09f8fad` waren bei der GitHub-Statusabfrage **keine** Commit-Statuses und **keine** zugehörigen Pull-Request-Workflow-Runs vorhanden. Das ist kein Test-PASS.

## Lokale Durchführung (Windows PowerShell)

Vor dem Test den **aktuellen Stand dieses Branches** holen; keine anderen Branches zusammenführen. Den tatsächlich geprüften Commit mit `git rev-parse HEAD` festhalten. Das NeoForge-Unterprojekt hat keinen eigenen `gradlew.bat`-Wrapper; laut früherer lokaler Projektdoku wurden **Java 21** und **Gradle 9.2.1** verwendet.

```powershell
git branch --show-current
git rev-parse HEAD
java -version
gradle --version
cd .\neoforge-port
gradle --no-daemon test build
```

Falls die PowerShell bereits in `neoforge-port` geöffnet wurde, `cd` weglassen. Der Befehl startet **keinen Minecraft-Client und keinen Server**, löst aber die Build-Abhängigkeiten aus dem Gradle-Projekt auf. **Keine** `-PloadTestMods`, `-PpamJar`, `-PidentifyJar` oder `-PjeiJar` angeben: Sie gehören ausschließlich in den späteren manuellen Lauf. Die JEI-API bleibt `compileOnly`; die konkrete Maven-Auflösung muss durch den Build bestätigt werden.

## Nachweise sammeln

- Git-Branch und vollständigen HEAD-SHA vor der Ausführung.
- Versionen von Java und Gradle.
- Gradle-Exitcode, vollständige Fehlermeldungen sowie erstes gescheitertes Task-Ziel, falls vorhanden.
- JUnit-Ergebnisdateien unter `neoforge-port/build/test-results/test/` (XML) und HTML unter `neoforge-port/build/reports/tests/test/`, soweit erzeugt.
- Bei Erfolg: tatsächlich erstellten JAR-Namen aus `neoforge-port/build/libs/` sowie die gemeldete Anzahl bestandener, fehlgeschlagener und übersprungener Tests.

**Reihenfolge bei Fehlern:** (1) JDK/Gradle und Abhängigkeitsauflösung, insbesondere JEI API; (2) Java-Kompilierung inkl. NeoForge-Client-Events; (3) JSON-/Modell-Ressourcen und neue Regressionstests; (4) Packaging und JAR-Metadaten. Keine JARs aus älteren Revisionen als neue Buildresultate ausgeben.

## Gezielt kontrollierte Änderungen

| Schwerpunkt | Vorbereitete Absicherung | Was ein erfolgreicher Build *nicht* beweist |
| --- | --- | --- |
| JEI | `SeedSpeciesCatalogTest`, API-Kompilierung | 16 sichtbare Einträge im echten JEI |
| Crop-Sticks-Eckstäbe | `CropStickGeometryRegressionTest` | tatsächliche Form im Client |
| HASH-Kulturpflanzen | `FieldCropVisualResourcesTest` | erkennbare Unterschiede in Stufe 0 |
| Stage-Reload / Ernte | `CropGrowthStageReloadTest` | echtes Harvest-Verhalten nach `/reload` |
| Melonen/Kürbisse | vorhandene Gourd-Tests und Zähler | zweiter physischer Fruchtzyklus |
| HUD/Identify | Java-Kompilierung | Überlagerung, Position und Übersetzung im Spiel |
| Pam Crops 1.0.9 | kein Fremd-JAR im Build | gültiger `rabbit_food`-Tag der Fremdmod |

## Danach

Erst nach dokumentiertem PASS für **diesen Commit** einen gebündelten manuellen Test in der bestehenden Testwelt starten. PT-02/03/04, Pflanzenfarben/-höhen, Stage-0-Unterschiede, Seed-Icons, JEI und Identify bleiben bis dahin offen. Kein Merge auf `entwicklung`, `testing` oder `master`; keine Release-Aussage und kein fremdes JAR verändern. Das geplante spätere MineColonies-Addon bleibt auf der Roadmap.
