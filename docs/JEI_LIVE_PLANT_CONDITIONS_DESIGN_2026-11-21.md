# UX-02 / UI-11 – aktuelle Pflanzenbedingungen in JEI

**Status:** Etappen A–C als Quellcode vorbereitet am 21.11.2026; **keine Build-/JUnit-/Minecraft-/CI-Ausführung, JEI-Laufzeitabnahme offen**. Hauptmod und Entwicklungsbranch `integration/neoforge-1211-consolidation`.

## Umsetzungsetappe A – serverseitige Sicht, ohne Netzwerk (21.11.2026)

`PlantConditionsView.from(AgriPlantsRegistry.Snapshot)` stellt nun einen einzelnen,
unveränderlichen, nach Pflanzen-ID sortierten Server-Katalog bereit. Enthalten sind
die gültigen `PlantRequirements`, eindeutige gewöhnliche Seed-Item-IDs sowie die
explizit aus `CropConditions.matchesPlantingSite` abgeglichenen Sonderregeln für
Zuckerrohr, Kaktus und Pilze. Mehrdeutige Seed-Zuordnungen werden nicht als
gültige Einstiegspunkte angeboten. Der Katalog wird bei mehr als 512 Arten
abgelehnt statt still abgeschnitten; das ist **noch kein Netzwerkgrößenlimit**.

Der neue `PlantConditionsViewTest` beschreibt Sortierung, Mehrdeutigkeit,
Sonderregeln, Unveränderlichkeit, Reload-Isolation und Fail-closed-Grenze.
**Nur Testquellcode geschrieben, keine JUnit-/Gradle-/Minecraft-/CI-Ausführung.**

**Nicht enthalten, UI-11 bleibt offen:** Payload/Codec und Byte-Limit,
Login-/Reload-Sync, Logout-Bereinigung, dynamische JEI-Ansicht, DE/EN-Text und
im Spiel bestätigte Lifecycle-Sicherheit. `PlantRequirements` ist zunächst
eine interne Sicht und **darf nicht ungeprüft serialisiert werden**. Bei
einem späteren Wire-Protokoll alle Listen/String-Felder einzeln begrenzen und
bei Übergröße geschlossen abbrechen. Bis zum vollständigen Sync keine
statischen Welt-Regeln in JEI als servergültig bewerben.

## Umsetzungsetappe B – versionierter Core-Sync (21.11.2026)

`PlantConditionsWire` serialisiert die öffentlich sichtbaren Regeln aus Etappe A
mit Formatversion 1 und hartem Limit von **256 KiB**. Die Dekodierung begrenzt
Artenzahl, Listen und Strings, prüft das Versionfeld und validiert anschließend
die Anforderungen über die bereits bestehenden Record-Konstruktoren.
`PlantConditionsPayload` ist ein **nur serverseitig gesendetes**
NeoForge-Play-Payload (`plant_conditions_v1`). Beim `OnDatapackSyncEvent`
(Eintritt oder `/reload`) wird ein einzelnes Abbild an die betroffenen Spieler
gesendet. Übersteigt der Katalog das Budget, wird eine leere
Ungültigkeitsmeldung versandt statt ein unvollständiger Katalog.

`ClientPlantConditions` hält ein optionales unveränderliches Abbild, ersetzt es
atomar bei Empfang und löscht es bei Logout oder ungültigem Paket. **Ein
Client-Login-Event löscht es absichtlich nicht:** der Server darf bereits vor
diesem Event synchronisieren. Vor Eintritt in eine weitere Verbindung entfernt
der Logout-Pfad die vorherigen Regeln. Für ein noch laufendes `/reload` gibt es
keinen gesonderten „Reload beginnt“-Hinweis; die alte Anzeige darf daher **noch
nicht** als live garantiert werden. Die kommende JEI-Anzeige muss diesen
Lifecycle-/Frischevorbehalt berücksichtigen.

`PlantConditionsWireTest` enthält nur **vorbereitete**, nicht ausgeführte
JUnit-Fälle zum Vollständigkeits-Roundtrip und zur Zurückweisung beschädigter,
zu großer oder künftig versionierter Pakete. Keine Gradle-, CI-, JUnit-,
Minecraft- oder Simulationstests durchgeführt. Statische Review-Risiken:
Netzwerk-Event-Reihenfolge, Payload-Registrierung auf dediziertem Server und
der tatsächliche Datapack-Reload im Multiplayer sind nicht laufzeitverifiziert.
**UI-11 weiterhin offen:** In JEI existiert noch keine dynamische Darstellung;
die unveränderlichen statischen `addItemStackInfo`-Seiten werden bewusst nicht
als weltaktuelle Bedingungen ausgegeben.

## Umsetzungsetappe C – dynamische JEI-19.x-Informationsansicht (21.11.2026)

`LivePlantConditionsJei` bindet im optionalen JEI-Plugin
`onRuntimeAvailable`/`onRuntimeUnavailable` an den Client-Regelkatalog.
Für jede aktive Art wird eine Informationsansicht über ihren genetischen
Seed und die eindeutig zugeordneten normalen Seeds angeboten. Bei Sync wird
die vorherige Auswahl ausgeblendet, neu entdeckte Arten/Seed-Zuordnungen werden
registriert und bereits bekannte wieder eingeblendet; die Beschreibung liest
**beim Öffnen** die zuletzt empfangene Serverprojektion. Ein allgemeiner
Status-Eintrag am genetischen Seed erklärt auch ohne Serverdaten ausdrücklich
„nicht synchronisiert“. Die bestehenden, nur allgemein erklärenden
Seed-Umwandlungs- und Analyzer-Seiten bleiben unverändert.

`PlantConditionsText` beschreibt Bodenblock mit lokalisiertem Blocknamen
und technischer ID beziehungsweise `#Boden-Tag`, Lichtbereich, maximale
Strength-Toleranz, Zuckerrohr-/Kaktus-/Pilz-Ausnahmen, Biome, Dimensionen,
Y-Grenzen, Fluid, Saisons und Chemie. Für Tags wird **keine unbewiesene
Blockliste** behauptet. DE/EN-Einträge sind ergänzt; der
`PlantConditionsLocalizationContractTest` ist als **nicht ausgeführter**
JUnit-Quelltest vorbereitet.

**Statischer JEI-API-Befund:** Ziel ist die JEI-19.x-API mit
`IJeiRuntime.getRecipeManager().addRecipes/hideRecipes/unhideRecipes`
und der vorhandenen `RecipeTypes.INFORMATION`-Kategorie. Die
Informationsrezept-Schnittstelle ist durch JEI als `@NonExtendable`
gekennzeichnet; unsere kleine reine Datenimplementierung benötigt deshalb
**zwingend** einen JEI-19.x-Laufzeittest, bevor UI-11 als angenommen gilt.
Wiederholte Datapackwechsel mit neuen Arten-/Seed-Kombinationen können
versteckte JEI-Rezepte im laufenden Runtime-Cache ansammeln; dieses Risiko
ist erst mit der Laufzeitprüfung bzw. einem nötigen eigenen JEI-Rezepttyp
abschließend lösbar. `/reload` hat weiterhin kein separates „beginnt“-Signal;
bis zum neuen Paket ist nur der **zuletzt empfangene**, nicht zwingend bereits
der gerade im Server-Reload entstehende Stand sichtbar.

**UI-11 bleibt zur Laufzeitabnahme offen.** Sonderfall „JEI nicht installiert“
ist im Quellcode durch `ModList.isLoaded("jei")` abgegrenzt, aber nicht
im dedizierten Serverstart bestätigt. Für Sonntag eine neue, getrennte
Tracker-v5-Suite mit UI-11 erstellen; v4 und bisherige Ergebnisse erhalten.

## Statischer Nachreview – eigene JEI-Kategorie statt `@NonExtendable` (21.11.2026)

Der bisherige Etappe-C-Code implementierte `IJeiIngredientInfoRecipe`, obwohl
JEI 19.x diese Informationsrezept-Schnittstelle ausdrücklich als
`@ApiStatus.NonExtendable` markiert. **Dieser konkrete API-Risikofall ist jetzt
im Quellcode beseitigt:** `LivePlantConditionsJei` registriert eine **eigene**
`IRecipeCategory<Info>` mit `RecipeType.create`, eigenem Icon, Ingredient-Slots
und Scrolltext. Die öffentlichen JEI-19.x-Verträge `registerCategories`,
`IRecipeManager.addRecipes/hideRecipes/unhideRecipes`,
`IRecipeLayoutBuilder.addInputSlot` und
`IRecipeExtrasBuilder.addScrollBoxWidget` wurden gegen die öffentliche
1.21.1-API-Quelle gelesen. Kein Eingriff in JEI-interne Klassen.

**Statischer Reviewstatus:**

| Priorität | Fundstelle | Befund / Maßnahme | Status |
| --- | --- | --- | --- |
| HOCH | `LivePlantConditionsJei.Info` (zuvor) | Abhängigkeit von JEIs nicht erweiterbarer Informationsrezept-Schnittstelle | Durch eigene `IRecipeCategory` als Quelländerung beseitigt; **kein Build**. |
| MITTEL | `LivePlantConditionsJei.CACHE` | Verschiedene Arten-/Seed-Signaturen über viele Datapack-Reloads bleiben als ausgeblendete JEI-Rezepte im Runtime-Manager. JEI 19.x bietet in der geprüften `IRecipeManager`-API kein `removeRecipes`; `hideRecipes` entfernt sie nicht aus dem Speicher. | Bekanntes Langläufer-Risiko; `UI-11h` bleibt Pflichtnachweis, bei deutlichem Wachstum eigene Strategie vor Gameplayfreigabe erforderlich. |
| MITTEL | `ClientPlantConditions.receive` / `PlantConditionsSync` | Zwischen Reload-Beginn und Serverpaket kann die zuletzt empfangene Version noch sichtbar sein. Keine Frische-Garantie vor Paketeingang. | Ehrlich als „zuletzt empfangene Serverregeln“ ausgewiesen; `UI-11c/e` müssen die Reihenfolge belegen. |
| MITTEL | `PlantConditionsSync.register` / `ClientPlantConditions` | Dedicated-Server-Classloading und NeoForge-Play-Payload-Reihenfolge ohne Runtimebefund. | `TG-04`, `UI-11d/f` offen. |

Das Modul ist **nicht** automatisch freigegeben: Die Kompatibilität mit der
tatsächlich eingesetzten JEI-JAR, deren Rezeptindex und der Wechsel zwischen
Servern bleiben unbewiesen. `PlantConditionsLocalizationContractTest` enthält
zusätzlich den DE/EN-Kategorietitel, wurde aber nicht ausgeführt. Kein
Gradle/JUnit/CI/Minecraft/Simulation gestartet.

## Nutzeranforderung

Für jede in dieser Welt tatsächlich unterstützte Pflanze soll JEI verständlich erklären, auf welchem Boden und unter welchen Umweltbedingungen der Seed wächst: z. B. Ackerboden und Licht, Myzel/Pilze, Zuckerrohr mit Wasser neben dem Boden, gegebenenfalls Block-Tags, Biome, Dimension, Höhe, Jahreszeit und Bodenchemie. Die Anzeige muss nach einem Server-Datapackwechsel aktuell bleiben; JEI ist optional.

## Bestätigter Quellbefund

1. `CropConditions.canGrow` berechnet Wachstum aus den **serverseitigen** `PlantDefinition.requirements()` einschließlich soil_block/soil_tag, Pflanzen-Sonderregeln, Biomen/Dimensionen, Umgebung, Saison, Chemie, Licht und Strength-Toleranz sowie Soil-Modifier. Der Server trifft die tatsächliche Pflanzentscheidung.
2. `CropGeneticsJeiPlugin.registerRecipes` nutzt derzeit `SeedSpeciesCatalog.DEFAULT_SPECIES` plus optionale Item-Links. **Daraus folgt nicht**, dass genau diese Arten auf dem verbundenen Server aktuell aktiv sind. Eigene Datapack-Pflanzen und Overrides können abweichen.
3. Ein bestehender Netzwerk-Kanal für die allgemeine Pflanzenanforderungs-Liste wurde im Hauptmod nicht gefunden. Die vorhandenen `Genome`-StreamCodecs und Blockentity-Update-Pakete betreffen andere Daten; sie dürfen nicht als globaler Pflanzenregel-Snapshot wiederverwendet werden.
4. JEI-`IRecipeRegistration.addItemStackInfo` erzeugt Informationsansichten beim Registrieren. Aus dem aktuellen Quellcode folgt **kein Nachweis**, dass diese Texte nach `/reload` automatisch neu erstellt werden. JEI-Reload-Verhalten ist daher eine eigene Integrationsprüfung.

## Technisch saubere Umsetzung – in einem eigenen Codepaket

**A. Serverprojektion (ohne Genetik):** Aus dem aktuellen `AgriPlantsRegistry.Snapshot` eine sortierte, auf öffentliche Anzeigeinformationen reduzierte Liste erstellen. Nur Pflanzen-ID, passende Seed-Item-IDs, Bodenregel (ein Block oder Tag), min/max-Licht und Toleranz, artspezifische Nachbarregeln, Biome/Dimensionen, Höhen-/Fluid-/Saison-/Chemie-Anforderungen und ggf. eine klar bezeichnete Versionskennung übertragen. **Keine** Seed-Genome, Journal-Entdeckungen, individuelle Stärke, Chunk-/Blockentity-Inventare oder sensible Spielerstände.

**B. NeoForge-1.21.1-Verbindung:** Versioniertes `CustomPacketPayload` mit `StreamCodec` über `RegisterPayloadHandlersEvent` registrieren; bei Login bzw. `OnDatapackSyncEvent` nur an tatsächlich verbundene Spieler senden. Die Dokumentation zu [1.21.1-Payloads](https://docs.neoforged.net/docs/1.21.1/networking/payload/) beschreibt diese Registrierung und `PacketDistributor.sendToPlayer`. Größenlimit, maximale Anzahl Arten/IDs und Strings begrenzen; keinen Client-Thread mit serverseitigem Registryzugriff belasten.

**C. Clientzustand:** Snapshot atomar ersetzen, bei Logout vollständig leeren. Während keine gültigen Serverregeln vorliegen **keine** behaupteten aktiven Anforderungen anzeigen; explizit „Serverbedingungen werden geladen“ bzw. „nicht synchronisiert“. Bei Serverwechsel nicht den alten Snapshot weiterverwenden.

**D. JEI:** Pro aktiver Pflanzenart einen klaren Bedingungs-/Infobereich bereitstellen. Die technische Umsetzung muss anhand der **tatsächlich verwendeten JEI-1.21.1-API** klären, wie Informationen nach Login oder `/reload` aktualisiert werden; statisch registrierte `addItemStackInfo`-Seiten dürfen keine unaktualisierbaren Welt-Regeln vortäuschen. Bei fehlendem JEI hat die Hauptmod weder Ladefehler noch eine Pflichtabhängigkeit.

**E. Darstellung:** Bodenblock/Tag mit lesbarem Namen und technischer ID, Lichtbereich inkl. Strength-Abweichung, Zusatzbedingung „Wasser neben dem Stützboden“ **nur für Zuckerrohr**, Pilz-Helligkeitsausnahme nur für erlaubte Substrate. Keine generelle Aussage „alle Ackerpflanzen brauchen Wasser daneben“. Unbekannte Tag-Inhalte oder Chemieanbieter als solche ausweisen, statt Beispiele als vollständige Liste zu behaupten. DE/EN.

## Vorbereitetes späteres Testgate `UI-11` (weiterhin offen)

| Teilfall | Konkreter Nachweis |
| --- | --- |
| Standardpflanze | Bodenblock/-Tag, Lichtanforderung und Seed-Zuordnung stimmen mit aktivem Datapack überein. |
| Zuckerrohr und Pilze | Wasser-/Myzel-/Licht-Sonderregeln korrekt, ohne sie auf andere Pflanzen zu übertragen. |
| Geändertes Datapack | `/reload` ändert JEI-Anzeige ohne Client-Neustart bzw. zeigt während unbestätigtem Zustand ehrlich „nicht synchronisiert“. |
| Dedizierter Server | Zwei Spieler sehen dieselben öffentlichen Regeln, aber keine privaten Genome oder Journal-Inhalte. |
| Serverwechsel / Logout | Alter Server-Snapshot wird vollständig verworfen; keine falschen Werte in der nächsten Welt. |
| Fehlendes JEI | Core-Spielstart und Seed-Pflanzung weiterhin ohne JEI möglich. |
| Erweiterungen | Unbekannte Mod-ID, nicht registrierter Seed und späteres optionales Add-on erzeugen keine irreführende Unterstützungsmeldung. |

**Review-Entscheidung vor dem gemeinsamen Test:** Keine statischen Pseudo-Weltinformationen in JEI veröffentlichen. Das erforderliche Paket greift in **Netzwerk-/Login-/Reload- und JEI-Lifecycle** ein und ist deshalb als zusammenhängende Hauptmod-Erweiterung mit eigenem NBT-/Netzwerk-Review und eigenen Testquellen durchzuführen. Ohne ausdrücklich freigegebene Gradle-/JUnit-/Minecraft-Tests bleibt die technische Runtime-Akzeptanz unbewiesen. Bis dahin `UI-11` als **offen, nicht implementiert** führen.
