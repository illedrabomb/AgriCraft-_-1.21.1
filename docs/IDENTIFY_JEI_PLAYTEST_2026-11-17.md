# Optionale Informationsmods und Analyse-Sichtbarkeit (17.11.2026)

**Ziel:** Identify – What Am I Looking At? **0.4.0 (NeoForge Minecraft 1.21–1.21.1)** und Just Enough Items **19.64.0.455 (NeoForge Minecraft 1.21.1)** für den ersten manuellen Entwicklungsclient-Playtest verwenden. Beide sind **nur optionale Testhilfen**, keine nötigen Abhängigkeiten und keine Bestandteile des gebauten Mod-JARs.

## Neue optionale Jade-Integration (17.11.2026, ungetesteter Quellcode)

Nach dem Ingame-Befund, dass Identify kein AgriCraft-spezifisches Pflanzenoverlay liefert, wurde das offizielle Erweiterungssystem von **Jade (WAILA-Fork)** geprüft. Jade stellt für NeoForge 1.21.1 die öffentliche Plugin-API `@WailaPlugin`, `IWailaPlugin` und `IBlockComponentProvider` bereit. WTHIT hat ebenfalls eine öffentliche API, aber mit eigenem Registrierungsmodell; dafür gibt es **noch keinen** Adapter. Die vorhandene Identify-Installation wird **nicht** automatisch zu Jade und enthält damit auch nicht die neue Jade-Anzeige.

**Änderungen im Integrationsbranch:**

- `build.gradle` bindet `maven.modrinth:jade:15.10.6+neoforge` nur als `compileOnly` ein. Das veröffentlicht keine Jade-Klassen mit unserer JAR und erfordert Jade nicht beim gewöhnlichen Spielen; der Build muss diese zusätzliche Maven-Abhängigkeit jedoch auflösen.
- `compat/jade/CropGeneticsJadePlugin.java` registriert eine native Block-Tooltip-Zeile für `CropSticksBlock`. Sie zeigt nur übersetzten Pflanzennamen und Wachstumsstufe; **keine unanalysierten Genwerte**. Die Daten stammen aus bereits synchronisiertem `CropSticksBlockEntity`, kein zusätzlicher Netzwerkkanal.
- `CropInfoOverlay` unterdrückt die eigene zusätzliche HUD-Zeile, wenn `jade` geladen ist. **Ohne Jade** bleibt der bisherige Standalone-Fallback erhalten (auch bei Identify oder ohne Look-at-Mod).
- Quelle: https://jademc.readthedocs.io/en/1.x/plugins20/getting-started/ und NeoForge-1.21.1-Jade-Version https://modrinth.com/mod/jade/version/15.10.6%2Bneoforge.

**Nachtest nach gemeinsamem Build:** Jade 15.10.6 NeoForge 1.21.1 als optionale lokale Test-JAR unter `neoforge-port/test-mods` bereitstellen und mit `-PloadTestMods` starten; auf Crop Sticks zielen; Pflanzenart + Stufe **innerhalb des Jade-Panels** prüfen; keine doppelte HUD-Zeile; leere Crop Sticks, nicht analysierte Gene und Jade-Abwesenheit separat testen. Es gibt **noch keinen Build-/Gameplay-PASS** für diese Integration. Bitte keine automatische Jade-Abhängigkeit zur Release-Modliste hinzufügen.

## Neue optionale WTHIT-Integration (17.11.2026, ungetesteter Quellcode)

Zusätzlich zu Jade steht ein WTHIT-Adapter für Minecraft 1.21.1 / NeoForge bereit:

- Nur `compileOnly 'mcp.mobius.waila:wthit-api:neo-12.5.3'` aus dem offiziellen Maven-Repository `https://maven2.bai.lol`. WTHIT und seine Laufzeitabhängigkeit Bad Packets sind **nicht** automatisch im normalen Mod-JAR enthalten. Eine installierte kompatible WTHIT-Version bleibt optional.
- `compat/wthit/CropGeneticsWthitPlugin.java` registriert `IBlockComponentProvider.appendBody` ausschließlich für `CropSticksBlock`. Es zeigt Artname und Wachstumsstufe, **keine Genwerte**.
- `src/main/resources/wthit_plugins.json` aktiviert die clientseitige Plugin-Initialisierung nur bei vorhandener `wthit`-Mod.
- Das selbst gezeichnete `CropInfoOverlay` bleibt sowohl bei Jade als auch bei WTHIT ausgeschaltet, damit keine zwei Anzeigen entstehen; ohne beide bleibt der Fallback bestehen.

**Referenzen:** Offizielles Plugin-Setup https://github.com/badasintended/wthit/blob/dev/master/docs/plugin/getting_started.md ; kompatible Version https://modrinth.com/mod/wthit/version/neo-12.5.3. Die aktuell publizierten 20.x-API-Javadoc-Seiten bestätigen die Form von `IClientRegistrar`/`appendBody`, sind aber kein Ersatz für einen tatsächlichen Build gegen die gewählte 12.5.3-API.

**Nachtest:** Nach gemeinsamem Gradle-Build WTHIT NeoForge 12.5.3 + kompatibles Bad Packets lokal in `test-mods` laden, auf bepflanzte Crop Sticks zeigen, Art/Stufe im **WTHIT-Panel** prüfen, keine zweite Standalone-HUD-Zeile, keine Genwerte an unanalysierten Pflanzen. Jade und WTHIT zunächst getrennt testen; nicht beide gleichzeitig erzwingen. **Aktuell kein Build-/Client-PASS.**

## JEI-Anzeigerezepte für dynamische Saatgut-Rückgewinnung (17.11.2026, ungetesteter Quellcode)

**Ursache der fehlenden Anzeige:** Das echte Rezept `cropgenetics:original_seed_recovery` ist ein `CustomRecipe`/`SimpleCraftingRecipeSerializer`: Ein genetischer Seed plus Papier ergeben das ursprüngliche Vanilla-/Mod-Saatgut; das Papier wird als verbleibender Gegenstand zurückgegeben. Weil der Output vom jeweiligen `ORIGINAL_SEED`-Dateneintrag abhängt, ist er kein festes JSON-Rezeptergebnis und war in JEI bisher nicht aussagekräftig dargestellt.

**Quellcode-Ergänzung:** `CropGeneticsJeiPlugin.registerRecipes` registriert **nur in JEI** ein konkret benanntes Shapeless-Crafting-Anzeigebeispiel je bekannter Vanilla-Art, **sofern** `CropItems.recoverOriginalSeed` für den Beispielstack einen zulässigen Original-Seed liefert. Vanilla-Source-IDs werden auf dem Client explizit mitgeführt, da der serverseitige Pflanzenkatalog dort fehlen kann. Die Beispiele haben eigene `cropgenetics:jei_recover_...`-IDs und sind **keine neuen echten Crafting-Rezepte**. Die Implementierung des echten Rezeptes sowie Input-, Output- und Paper-Rest-Logik wurden nicht verändert.

**Darstellungsgrenze:** JEIs gewöhnliche Shapeless-Rezeptgrafik zeigt beide Zutaten, modelliert aber die Besonderheit „Papier wird zurückgegeben“ nicht als eigenen Rest-Item-Effekt. Der Nutzerhinweis wurde bewusst aus dem normalen genetischen Seed-Tooltip entfernt; ein zusätzlicher JEI-spezifischer Rest-Item-Hinweis oder eigene Rezeptkategorie wäre eine mögliche weitere Verbesserung. In besonderen Datapacks können Beispielrezepte von aktivierten oder gesperrten Rezepten abweichen; deshalb beim Nachtest unbedingt die tatsächliche Rückwandlung im Craftingfeld kontrollieren.

**Nachtest:** JEI 19.64.0.455 laden, mit mindestens Weizen-/Karotten-/Melonen-Gen-Saatgut JEI-Rezeptansicht öffnen, angezeigte Inputs/Outputs auf korrekte Arten prüfen. Echte Herstellung durchführen: genau ein genetischer Seed muss verbraucht werden, ein normaler Ursprungsseed entstehen und das Papier erhalten bleiben. Die 16 JEI-Seedvarianten separat prüfen. **Kein Build oder Minecraft-Test wurde für diesen Stand ausgeführt.**

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
