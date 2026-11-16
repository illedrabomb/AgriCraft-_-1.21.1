# Optionale Informationsmods und Analyse-Sichtbarkeit (17.11.2026)

**Ziel:** Identify – What Am I Looking At? **0.4.0 (NeoForge Minecraft 1.21–1.21.1)** und Just Enough Items **19.64.0.455 (NeoForge Minecraft 1.21.1)** für den ersten manuellen Entwicklungsclient-Playtest verwenden. Beide sind **nur optionale Testhilfen**, keine nötigen Abhängigkeiten und keine Bestandteile des gebauten Mod-JARs.

## Technischer Stand

- `neoforge-port/build.gradle`: die bisherigen `-PpamJar=<Pfad>`-Lokaldateioptionen wurden um `-PidentifyJar=<Pfad>` und `-PjeiJar=<Pfad>` ergänzt (beide `runtimeOnly files(...)`). Sie werden **nur bei gesetzter Gradle-Property** in die Runtime aufgenommen. Ohne Pfadangaben lädt ein normaler Build diese Mods **nicht** herunter.
- `GeneticDisclosure.mayShowGenes(Genome, analyzed)` ist die gemeinsame Abfrage für genetische Anzeigewerte. Nur ein existierendes Genom mit `analyzed=true` darf numerische Gene freigeben.
- Die Client-Seite `AnalyzedSeedTooltip` ruft diese Regel für die fünf Genpaare auf; wie bisher zusätzlich nur, wenn Shift gehalten wird. `CropItems.isAnalyzed` nutzt dieselbe Regel. `CropSticksBlockEntity.mayShowGenes()` ist ein bewusst enger Einstiegspunkt für spätere Overlay-Adapter: nicht analysierte Pflanzen dürfen **keine** Genwerte ausgeben.
- **Es ist kein Identify- oder JEI-spezifisches API-Plugin registriert.** Die externen Mods dürfen allgemeine Item-/Blocknamen, Mod-Zugehörigkeit und normale Crafting-Rezepte anzeigen. Eine künftige spezifische Integration muss die zentrale Abfrage respektieren und darf keine `Genome`-Werte für Unanalysiertes direkt aus dem BlockEntity lesen.
- Wichtig: `Genome` liegt bei genetischen Saatgut-Items weiterhin in synchronisierten Minecraft-Item-Data-Components; bei Crop Sticks ist die BlockEntity-Update-NBT weiterhin vollständig. Fremdmods mit freiem Datenzugriff könnten die rohen Felder unabhängig von unseren Tooltips auslesen. **Die Anzeige-Sperre ist keine technische Geheimhaltung der Datenspeicherung. Eine absolute Anti-Leak-Garantie wäre falsch.** Um das zu garantieren, wären separate serverseitige Daten und eine berechtigungsgesteuerte Netzwerk-Projektion nötig. Diese invasive Umstellung ist **nicht** Teil dieser Vorbereitungsänderung.

## Teststart auf lokalem Entwickler-Client

Der vorhandene Root-`gradlew.bat` ist ein alter Wrapper. Für `neoforge-port` die zuvor lokal verwendete **Gradle 9.2.1** mit **Java 21** im Terminal benutzen:

```powershell
cd "C:\Users\tinob\Documents\ChatGPT\AgriCraft-Integration-20261116\neoforge-port"
git status
git branch --show-current
git pull --ff-only origin integration/neoforge-1211-consolidation
java -version
gradle --version
gradle build
gradle runClient '-PidentifyJar=C:\Pfad\identify-neoforge-0.4.0+mc1.21-1.21.1.jar' '-PjeiJar=C:\Pfad\jei-1.21.1-neoforge-19.64.0.455.jar'
```

**Beispielpfade ersetzen.** Der genaue Dateiname der Identify-JAR muss mit der tatsächlichen 1.21–1.21.1-NeoForge-Datei auf dem Rechner übereinstimmen. Nie die Forge- oder Fabric-Version verwenden. Keine JARs in Git committen. `-PpamJar` nur zusätzlich setzen, wenn Pam Crops tatsächlich getestet werden soll. Diese `runtimeOnly`-Optionen gelten für alle Gradle-Runs dieses Aufrufs; für einen Dedicated-Server-Start **Identify und JEI nicht als unnötige lokale Client-Parameter mitgeben**. Welt und Spielstände zuvor sichern; den Test in einer neuen isolierten Welt beginnen.

## Manuelle Sichtbarkeitsmatrix

| Zustand / Oberfläche | Erlaubt | Nicht erlaubt |
| --- | --- | --- |
| Unanalysierter Vanilla-/Pam-Samen (Itemtooltip) | Originalitem, Name, Mod, allgemeine Anleitung | Growth/Gain/Strength/Fertility/Mutativity, dominante oder rezessive Allele |
| Unanalysierter `cropgenetics:genetic_seed` | Artname und Hinweis `Nicht analysiert` | Zahlen und Genpaare, auch bei gehaltenem Shift |
| Analysierter Samen | Analysiert-Hinweis; Genpaare bei Shift | Keine irreführenden Werte eines anderen Genoms |
| Unanalysierte Pflanze auf Crop Sticks | Crop Sticks und eventuell Artname, Modname | Genwerte im Identify-Overlay oder anderen angepassten Panels |
| Analysierte Pflanze auf Crop Sticks | Artname, Analysezustand | Gene nur durch ausdrücklich gebaute, analysegeprüfte Anzeige; Identify erhält im jetzigen Stand keine automatische genetische Anzeige |
| JEI-Rezeptliste und Rezeptansicht | Vanilla-/Mod-Rezepte, Seed Analyzer, Seed-Recovery, Itemnamen | Vollständige Genomwerte aus nicht analysierten Stacks als eigener von Crop Genetics registrierter Zusatz |
| Raw-NBT-/Data-Component-Viewer, Debug-Commands | Aufgabenspezifische Entwicklerdiagnose | **Nicht als geschützte Oberfläche zu bewerten**: solche Tools können die synchronisierten Rohdaten offenlegen |

**Ablauf:** (1) Ohne Analyzer Saatgut-/Pflanzenanzeigen inkl. JEI und Identify prüfen. (2) Seed Analyzer benutzen und Shift-Tooltip erneut prüfen. (3) Crop Sticks mit unanalysiertem Samen bepflanzen, Identify prüfen. (4) Gepflanzten Stamm per Analyzer markieren, Identify erneut prüfen. (5) JEI-Rezeptseite für Analyzer und Papier/Samen-Konvertierung öffnen. (6) Screenshots und ggf. JEI-/Identify-spezifische unerwünschte Genwerte dokumentieren.

## Nachweisgrenze

Diese Änderungen wurden auf GitHub als **Quellcode** gespeichert. **Kein Build, kein Unit-Testlauf, kein Minecraft-Start und keine tatsächliche Identify-/JEI-Laufzeitprüfung**. Eine visuelle/kompatibilitätsbezogene Freigabe ist bis zum Spieltest ausstehend. `entwicklung`, `testing` und `master` blieben unangetastet.

Herstellerseiten:
- Identify 0.4.0 NeoForge 1.21–1.21.1: https://www.curseforge.com/minecraft/mc-mods/identify/files/all
- JEI 19.64.0.455 NeoForge 1.21.1: https://www.curseforge.com/minecraft/mc-mods/jei/files/9205121
