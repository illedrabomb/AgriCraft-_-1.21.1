# Pam's HarvestCraft 2 – automatische Saatgut-Erkennung beim Datapack-Reload

## Lokaler Runtime-Nachweis 16.11.2026

Auf `integration/neoforge-1211-consolidation` wurde die echte NeoForge-1.21.1-JAR von Pam Crops 1.0.9 auf Client und Server geladen. Baseline: 113 aktive Arten (16 Vanilla, 37 explizite Pam, 60 Auto), 34 Mutationen. `/cropgenetics data pam` meldet 53 explizite Pflanzen **aller Mods**, nicht 53 explizite Pam-Arten. Ein echter Artischokensamen wurde mit dem Analyzer versehen und per Spielerinput gepflanzt.

Explizite Item-Claims werden vor Auto-Kandidaten reserviert und reduzieren den Auto-Zaehler ohne CONFLICT-Diagnose. Eine zusaetzlich konfigurierte Probe mit denselben echten Items wie der natuerliche Auto-Kandidat erzeugte dagegen zwei CONFLICT-Diagnosen; beide Auto-Kandidaten blieben inaktiv. Entfernen der Testkonfiguration stellte die Baseline wieder her. Geladene Genomdaten blieben beim zeitweisen Art-Ausfall, Chunk-Wechsel und Neustart erhalten.

Gesamtkompatibilitaet **nicht** fehlerfrei: Pams eigene rabbit_food-Datei enthaelt ungueltige IDs und erzeugt TagLoader-Fehler; grauer generischer Crop-Fallback ohne Laubtoenung bleibt offen. Keine Trees-Integration. Ausfuehrliche Methoden, Artefakt-Hash und Grenzen: [Playtest-Bericht](PLAYTEST_REPORT_2026-11-16.md). Aeltere Angaben "nicht runtime-geprueft" unten beziehen sich auf die vorangegangenen Implementierungsschritte.


**Stand:** 16.11.2026 · **Branch:** `feature/neoforge-1211-foundation`  
**Status:** Implementierung als Java-Quellcode gespeichert; **nicht kompiliert, getestet oder im Spiel validiert**.

## Warum der Automatismus?

Bisher wurde jede Pam's-Pflanze einzeln aus alten AgriPlants-Dateien in moderne Datapacks portiert. Das kann für Originalwerte sinnvoll sein, ist aber für die reine Erkennung registrierter Pam's-Saaten nicht nötig.

Mit `PamCropAutoDiscovery.java` und dem erweiterten `CropDataReloadListener.java` wird jetzt beim serverseitigen Crop-Datapack-Reload automatisch nach weiteren Pam's-HarvestCraft-2-Crops gesucht. **Die bisher manuell portierten 37 Pflanzen behalten ihre Originalregeln und werden nicht überschrieben.**

## Erkennung, Grenzen und Priorität

Die automatische Erkennung läuft ausschließlich, wenn die echte Mod-ID **`pamhc2crops`** geladen ist. Sie betrachtet die tatsächlich existierenden Einträge der **Minecraft-Item-Registry** und erkennt genau dieses konservative ID-Schema:

- Saatgut: `pamhc2crops:<crop>seeditem`
- Zugehöriges Ernteprodukt: `pamhc2crops:<crop>item`
- Interne Pflanzenspezies: `pamhc2crops:<crop>_plant`

Die beiden Item-IDs müssen **beide wirklich registriert und keine AIR-Platzhalter** sein; der `<crop>`-Teil muss einem klar begrenzten, gültigen Namen entsprechen. Somit werden nicht alle beliebigen Items mit dem Namen `seed`, keine Fremdmod-Items und auch keine bloß vermuteten Pam's-Produkte in neue Pflanzen umgewandelt.

Es gelten folgende Regeln:

1. Explizite Datapack-Pflanzen haben **immer Vorrang**. Das gilt bewusst auch für deaktivierte, ungültige und wegen fehlender Mods nicht geladene Datapacks. Ihre ID und alle explizit genannten Saatgut-IDs werden reserviert.
2. Nur wenn es keine explizite Art und keinen expliziten Saatgutanspruch gibt, entsteht eine generische Fallback-Spezies. Die vorhandene `PlantRegistryValidator`-Prüfung muss ebenfalls erfolgreich sein.
3. Automatische Einträge werden **vor** der Mutationsvalidierung in denselben Katalog aufgenommen und gemeinsam mit expliziten Pflanzen und Mutationen atomar über `AgriPlantsRegistry.replaceAll` veröffentlicht.
4. Die Erkennung scannt sortierte Item-Registry-IDs und begrenzt neue Einträge pro Reload auf **512**. Die Diagnosen verwenden virtuelle IDs `cropgenetics:auto_pam/<crop>.json` und den Status **WARNING** mit Hinweis auf die generischen Werte. Die Quelle ist kein tatsächliches Datapack-File.
5. Fällt Pam's Crops weg, entstehen beim nächsten Reload keine neuen automatisch generierten Pam's-Species. Bereits gespeicherte Spielwelten und Genomdaten müssen bei Modwechseln gesondert betrachtet werden.

**Derzeitige Standardwerte für automatisch entdeckte Arten** (nur Port-Fallback, ausdrücklich **nicht** aus Original-AgriPlants pro Pflanze belegt): `stages=[2,4,6,8,10,12,14,16]`, `harvestStage=4`, `growthChance=0.75`, `growthBonus=0.025`, `cloneable=true`, `spreadChance=0.1`, ein Ernteprodukt mit Menge `1..5` und Chance `0.9`, standardmäßiges Vanilla-Farmland, Licht `9..15`.

**Nicht behauptet oder automatisch rekonstruiert:** Originale Feuchtigkeits-/Säure-/Nährstoffanforderungen, Jahreszeiten, Wachstumstoleranzen, Render-Texturen, individuelle Drop-Logik, Samen-Pflanzungsüberschreibung, Mod-Kulturpflanzenblöcke oder genetische Mutationsrezepte. Solche Eigenschaften bleiben **expliziten Datapack-Definitionen** vorbehalten und werden nicht aus Item-Namen geraten.

## Nachtrag: konfigurierbare automatische Erkennung (16.11.2026)

Das automatische Ergänzen unbekannter Pam's-Crops ist jetzt per Server-Datapack steuerbar. Mitgelieferter, durch höhere Datapack-Priorität überschreibbarer Pfad:

`data/cropgenetics/cropgenetics/pam_auto_discovery.json`

```json
{
  "enabled": true,
  "max_plants": 512,
  "min_light": 9,
  "growth_chance": 0.75,
  "growth_bonus": 0.025,
  "harvest_maximum": 5,
  "excluded_crops": []
}
```

- `enabled` deaktiviert die **automatische Erkennung**, nicht die explizit definierten Originalpflanzen.
- `max_plants`: höchstens 0..512 automatisch neu eingefügte Arten je Reload; **0** bedeutet keine neuen Arten.
- `excluded_crops`: bis zu 512 eindeutige gültige interne Pam's-Crop-Namen, z. B. `["tomato", "rice"]`. Der Ausschluss gilt nur für **automatisch** entdeckte Arten; bereits vorhandene explizite Datapacks behalten ihre eigenen `enabled`-Regeln.
- `min_light`: 0..15; die Obergrenze bleibt 15.
- `growth_chance` und `growth_bonus`: endliche Zahlen 0..1.
- `harvest_maximum`: 1..64; das Ernteminimum bleibt 1 und die generische Erntechance bleibt 0.9.

Die übrigen Auto-Werte (acht Stufen, harvestStage 4, cloneable=true, spreadChance 0.1, Vanilla-Farmland) bleiben als **Port-Fallback** fest. Sie sind **keine Originalwerte für jede unbekannte Pflanze**. Auch eine geänderte Konfiguration erfindet keine Mutationsrezepte, Jahreszeiten oder Pflanzen-Texturen.

Die Datapack-Datei wird im selben serverseitigen `CropDataReloadListener.prepare` wie die Pflanzen- und Mutationsdaten gelesen. **Fehlt** sie vollständig, gelten die oben angegebenen Defaults. **Ist** sie vorhanden, aber ungültig (falsche Typen, unbekannte Keys, Werte außerhalb der Grenzen, doppelte Ausnahmen), wird die Autodiscovery **für diesen Reload vollständig deaktiviert** und ein `INVALID`-Diagnoseeintrag erzeugt; die expliziten Pflanzendateien werden davon nicht blockiert. Nach erfolgreichem Reload werden Regeln, automatisch ergänzte Arten und Mutationen in einem Katalog veröffentlicht.

**Wichtige Grenze:** Mehr Licht-/Ernteoptionen oder ein neues automatisch erkanntes Saatgut bedeuten nicht, dass die Pam's-Crops-1.21.1-JAR erfolgreich validiert wurde. Diese Datapack-Erweiterung ist implementiert, aber unkompiliert und ungetestet. Vor `testing` sind insbesondere der `SimplePreparableReloadListener`-Pfad, Priorität verschiedener Datapacks, `enabled=false`, Ausschlusslisten, ungültige Werte und Restart-Semantik zu prüfen.

## Nachtrag 16.11.2026: Sondernamen mit expliziten Item-Paaren

Für registrierte Pam's-2-Saatgut-/Ernte-Items, die **nicht** zum Standardschema `<crop>seeditem` + `<crop>item` passen, unterstützt die Server-Konfiguration jetzt das zusätzliche optionale Feld `item_pairs`. Die mitgelieferte Default-Datei enthält **`"item_pairs": {}`** und erfindet keine Ausnahmen. Ein Modpack kann bei **nachgewiesenen** Spezialfällen z. B. folgendes Schema ergänzen (**nur demonstrative IDs; kein behauptetes Pam's-Release-Item**):

```json
{
  "enabled": true,
  "item_pairs": {
    "examplecrop": {
      "seed": "pamhc2crops:example_seed",
      "produce": "pamhc2crops:example_food"
    }
  }
}
```

Dies ist eine Einstellung für **die Artidentität** `pamhc2crops:examplecrop_plant`, die zu einem konkret angegebenen `seed`- und `produce`-Item gehört. Es entsteht erst eine Pflanze, wenn **beide IDs tatsächlich in der Server-Item-Registry registriert und nicht AIR** sind. Fehlende konfigurierte Items erzeugen einen **WARNING**-Eintrag und werden übersprungen. **Maximal 128** solche Paare; die Cropnamen sind auf ein überschaubares gültiges Format, die beiden Items auf den Namespace `pamhc2crops:` begrenzt. Doppelt genannte Saatgut-IDs zwischen konfigurierten Paaren machen die Konfiguration ungültig und deaktivieren die automatische Erkennung für den betroffenen Reload; gleichlautendes `seed` und `produce` ist verboten.

**Priorität (höchste zuerst):**

1. Eine **explizite** Crop-Datapack-Art einschließlich deaktivierter, ungültiger oder fehlender-Mod-Definitionen und ihrer Saatgutansprüche blockiert Auto-Erkennung.
2. Die konfigurierten `item_pairs` werden nach Cropnamen sortiert und **vor** den regulär erkannten `seeditem`-Paaren verarbeitet; ein konfiguriertes Paar für eine Art ersetzt den automatischen Namensfallback dieser Art.
3. Erst danach kommen normale Pam's-Saatgut-/Erntepaare aus der Registry. Eine bereits beanspruchte Saatgut-ID wird nicht nochmals für eine neue Spezies verwendet.
4. `excluded_crops` und die Obergrenze `max_plants` gelten **für beide** Erkennungswege.

### Historische Sondernamen sind keine automatischen Runtime-Aliasse

In der ursprünglichen HarvestCraft-1-Registry sind z. B. die Saatgutnamen `harvestcraft:coffeeseeditem`, `harvestcraft:mustardseeditem` und `harvestcraft:teaseeditem` ausdrücklich als Legacy-Ausnahmen belegt. Die eingesehenen **älteren** HarvestCraft-2-Crops-Quellen nennen dagegen `pamhc2crops:coffeebeanseeditem`, `pamhc2crops:mustardseedsseeditem` und `pamhc2crops:tealeafseeditem` – diese folgen dem Standardschema der jeweiligen neuen Crop-Art. Deshalb werden **keine** historischen H1-IDs als moderne Items angenommen und keine Alias-Umleitung eingebaut. Die Existenz der genannten alten H2-Quell-IDs ersetzt **nicht** die noch ausstehende Prüfung der tatsächlichen NeoForge-1.21.1-Registry.

**Grenzen:** `item_pairs` ergänzt nur die generische Fallback-Species. Es erstellt keine Originalmutationen, keine korrekten Spezial-Bodenregeln, keine Pam's-1-Migration, keine Client-Texturen und ausdrücklich keine Baum-/Trees-Integration. Die 37 bereits expliziten Crop-Sticks-Arten und 31 originalen Mutationsdefinitionen werden nicht verändert oder überschrieben.

**Validierungsstand:** Die Java-Dateien und Datapacks sind gespeichert, aber **nicht kompiliert/getestet**. Ein später genehmigter Lauf muss insbesondere konfigurierte Paare mit fehlenden Items, doppelte Seed-IDs, `excluded_crops`, explizite `enabled=false`-Datapacks, Restart und `/reload` überprüfen.

## Verbindliche Crops-only-Abgrenzung (16.11.2026)

Der Nutzer hat entschieden: **Pam's HarvestCraft 2 Trees wird nicht in AgriCraft integriert.** Bäume, Setzlinge, Baumfrüchte und Fruchtbaumwachstum sind nicht Teil der Crop-Sticks-/Saatgut-Kompatibilität. Die frühere Idee eines Baumadapters ist gestrichen.

Der Scanner nutzt weiterhin ausschließlich die Mod-ID `pamhc2crops`, niemals `pamhc2trees` oder eine andere Mod-ID, und benötigt für eine Art nachweislich ein Saatgut-/Ernte-Item-Paar. `item_pairs` akzeptiert ebenfalls nur ausdrücklich konfigurierte Items aus `pamhc2crops:`. Damit gibt es **keinen globalen Scanner für alle Items mit »seed« im Namen** und keine automatische Erkennung von Setzlingen. Die konkrete Eignung automatisch erkannter Items für Crop Sticks muss bei später freigegebenen Tests bestätigt werden.

Der gleiche konservative Grundsatz gilt für spätere Saatgut-Mods: Erweiterung nur über geprüfte, bewusst gewählte Mod-IDs und Item-Schemata sowie klare Nutzerfreigabe. Diese Ausweitung wird **hier nicht als bereits implementiert behauptet**.

## Nachtrag 16.11.2026: Konflikte zwischen Saatgut- und Ernte-IDs

Die automatische Pam's-Crops-Erkennung schützt jetzt **auch Ernteprodukt-IDs**, nicht nur Saatgutnamen oder Species-Namen. Ein Registry-Eintrag kann in ungewöhnlich konfigurierten Modpacks ansonsten mehreren Auto-Species zugeordnet werden und das Ergebnis von der Iterationsreihenfolge abhängen lassen.

- `PamAutoDiscoverySettings` verwirft die komplette manuelle `item_pairs`-Konfiguration bei **doppelten Seed-IDs**, **doppelten Produce-IDs** und **Seed-/Produce-Überlappungen zwischen mehreren Paaren**. Die bestehende fail-closed-Konfigurationsbehandlung verhindert, dass solche fehlerhaften Einstellungen neue Arten erzeugen.
- `CropDataReloadListener` sammelt vor der Auto-Erkennung die in **allen expliziten Pflanzen-Datapacks** belegten Species-, Saatgut- und nun auch **Ernteprodukt-IDs**. Das schließt deaktivierte und wegen anderer Prüfungen inaktive Definitionen ein, soweit ihre JSON-Felder lesbar sind.
- `PamCropAutoDiscovery` reserviert auch alle Ernteprodukt-IDs der bereits erfolgreich geladenen Pflanzen. Ein Fallback darf weder eine reservierte Saatgut- noch eine reservierte Produkt-ID erneut beanspruchen. Explizite Definitionen bleiben maßgeblich.
- Vor der Veröffentlichung zählt der Scanner Seed- und Produce-Ansprüche **aller noch zulässigen Auto-Kandidaten**. Wenn mehrere Kandidaten ein Item in der gleichen Rolle beanspruchen oder ein Item bei einer Art als Saat und bei einer anderen als Ernte auftauchen soll, werden **alle betroffenen Kandidaten** mit `CONFLICT` übersprungen. Ein alphabetisch früherer Crop gewinnt ausdrücklich nicht.
- Die Konfliktprüfung ignoriert bereits ausgeschlossene, explizit definierte und reservierte Kandidaten, damit diese nicht nachträglich andere gültige Fallbacks unnötig blockieren. Sortierung und `max_plants` bleiben unverändert.

**Grenzen der Source-Prüfung:** Wenn ein explizites JSON noch vor dem Parsing nicht gelesen werden kann, lässt sich daraus keine verlässliche Species-/Seed-/Produce-ID ableiten. Der defekte Datapack-Eintrag wird als `INVALID` protokolliert. Für solche Fälle ist noch ein gesonderter Fail-closed-Ansatz auf Dateipfad-/Pack-Ebene zu prüfen. Die hier implementierten Schutzregeln sind **Quellcodeänderungen**, nicht durch Builds, Unit-Tests oder einen Server-Reload bestätigt.

**Beispielkonflikte:** Zwei `item_pairs` mit identischem `produce` invalidieren die Einstellungen; eine konfigurierbare Spezialpflanze, deren Saatgut bereits von einer anderen Auto-Art beansprucht wird, deaktiviert beide betroffenen Auto-Arten; ein definiertes Crop-Datapack mit demselben Ernte-Item verhindert den Auto-Fallback.

**Pam's Trees bleibt ausdrücklich ausgeschlossen.** Es wird ausschließlich `pamhc2crops` betrachtet, keine Setzlings- oder Baumregistrierung.

## Nachtrag 16.11.2026: beschädigte Pflanzen-Datapacks deaktivieren den Auto-Fallback

Die frühere offene Sicherheitsfrage zu **nicht lesbarem JSON** ist jetzt mit einem bewusst konservativen **Fail-closed-Verfahren** im `CropDataReloadListener` behandelt:

1. Fehler beim tatsächlichen Lesen oder Parsen einer Ressource unter `cropgenetics/plants/*.json` werden weiterhin als `INVALID` dokumentiert. Zusätzlich wird für denselben Reload die automatische Pam's-Crops-Erkennung deaktiviert. Ohne lesbaren Datensatz sind die dort eventuell reservierten Arten-/Saatgut-/Produktnamen nicht verlässlich bestimmbar.
2. Auch bei formal lesbarem JSON müssen die **`id`-Artidentität** und gegebenenfalls vorhandene `seed_items`-/`products.products`-Elemente ausreichend auswertbar sein, um Ansprüche auf Art-, Saatgut- und Ernte-IDs zu reservieren. Fehlende `id`, ungültige Typen oder nicht auswertbare Item-`object`-Felder verhindern den Auto-Fallback. Gültige ausdrücklich angegebene Item-Ansprüche aus anderen Dateien werden wie bisher gesammelt.
3. Die Blockierung erhält eine eigene `WARNING`-Diagnose an `cropgenetics:cropgenetics/pam_auto_discovery.json`. **Gültige explizite Pflanzen und Mutationen werden trotzdem weitergeladen**; der gesamte Datapack-Reload wird nicht verworfen.
4. Nach Korrektur der defekten Datei kann die automatische Erkennung bei einem späteren regulären Reload wieder arbeiten. Ein defekter Eintrag darf **nicht** dadurch umgangen werden, dass die Automatik eine ähnliche Art neu herstellt.

Die besondere Behandlung gilt **nur für den automatischen Pam's-Crops-Fallback** und verändert weder die HarvestCraft-1-Legacy-Migration noch die ursprünglichen 37 expliziten Pam's-2-Pflanzen und 33 Originalmutationsdateien. **Pam's Trees bleibt ausgeschlossen.**

**Offene Abnahme:** Es wurden weder Unit-Tests noch Java-Kompilierung, Dedicated-Server-Reload oder Minecraft-Starts ausgeführt. Zu prüfen sind später insbesondere beschädigtes JSON, JSON-Typfehler, `enabled=false`, Datapack-Prioritäten, Sonderpaar-Konfigurationen und die Reaktivierung nach Reparatur.

## Nachtrag 16.11.2026: Registry-basierte Konfliktprüfung präzisiert

Bei einer erneuten **Quellcodeprüfung ohne Ausführung** wurden zwei weitere Probleme mit dem Auto-Fallback identifiziert und im `PamCropAutoDiscovery.java` korrigiert:

1. Vor der Kandidaten-Konfliktzählung wird nun mit `registeredPair` kontrolliert, ob **beide Items tatsächlich in `BuiltInRegistries.ITEM` existieren und nicht AIR sind**. Ein nur konfiguriertes, aber fehlendes Item-Paar darf keine andere, tatsächlich registrierte Crop-Art blockieren. Die normale WARNING-Diagnose für fehlende konfigurierte Paare bleibt erhalten.
2. Die reservierten Identitäten expliziter Datapacks werden **rollenübergreifend** geschützt. Eine automatische Saat darf auch **kein explizit reserviertes Ernteprodukt** sein, und das Auto-Ernteprodukt darf **kein explizit reservierter Seed** sein. Die bestehende Konfliktprüfung zwischen Auto-Kandidaten bleibt bestehen.

Die Schutzregeln beeinflussen die **generisch automatisch ergänzten** Species, nicht die ursprünglichen expliziten Pflanzen oder ihre Mutationstabellen. Sie ändern nichts an den aus historischen AgriPlants-Daten stammenden Wachstums-/Chemieanforderungen.

**Quellenstatus der Pam's-2-1.21.1-Registry:** Die offizielle CurseForge-Dateiliste bestätigt `pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar` vom 09.05.2026 (https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops/files/all). Der auf CurseForge verlinkte öffentliche Source-Repository `MatrexsVigil/phc2crops` zeigt historische Item-Registrierungen, aber keine separat belegte aktuelle 1.21.1-1.0.9-Registry. **Die aktuellen Item-IDs sind weiterhin nicht statisch gegen genau diese Version bestätigt.** Die Erkennung nutzt darum ausschließlich Item-IDs, die in der tatsächlichen geladenen Registry existieren. Es wurden keine externen JAR-Dateien, Assets oder fremder Quellcode kopiert.

**Keine Builds, Tests, Minecraft-Starts, Importer, Shell-Skripte oder GitHub Actions ausgeführt.** Das Verhalten ist im Source implementiert, aber noch nicht runtime-validiert. **Pam's Trees bleibt ausgeschlossen.**

## Nachtrag 16.11.2026: getrennte Laufzeitdiagnose und Admin-Befehl

Nach der nächsten **genehmigten** Spiel-/Server-Ausführung zeigt `/cropgenetics data pam` eine **nur lesende** Übersicht des letzten serverseitigen Crop-Datapack-Reloads. Der bereits existierende Befehl `/cropgenetics data summary` erhält eine kurze Pam's-Zeile. Für Einzelheiten bleiben `/cropgenetics data errors [page]` und `/cropgenetics data all [page]` verfügbar.

Die Zahlen werden aus den bereits gemeinsam veröffentlichten, **unveränderlichen** `CropReloadReport.Entry`-Datensätzen berechnet und nicht aus veralteten Dateien, historischen Zählungen oder einem erneuten Item-Scan. Neue, explizite Statuswerte ermöglichen die Unterscheidung:
- **`AUTO_DISCOVERED`**: erfolgreiche neue Fallback-Pflanze (virtueller Diagnosepfad `cropgenetics:auto_pam/<crop>.json`); im detaillierten `all` sichtbar, nicht als Fehler in `errors` oder als Warn-Spam im Serverlog
- **`AUTO_MISSING_ITEM`**: konfiguriertes Paar, dessen Saatgut-/Ernte-Item nicht beide in der aktuellen Item-Registry gefunden wurden
- **`CONFLICT`** unter dem virtuellen `auto_pam/`-Pfad: mehrdeutige Auto-Kandidaten
- **Sonstige Auto-Diagnosen**: etwa die konfigurierte Maximalanzahl oder ein fehlgeschlagener Registrierungsvalidator
- **Auto-Erkennung ausgesetzt:** Fehler der `pam_auto_discovery.json`-Konfiguration oder der Schutz gegen beschädigte explizite Pflanzenansprüche
- **Explizit geladene Pflanzen:** Definitionen mit `LOADED` oder `WARNING` und realem Pfad `cropgenetics/plants/`; **dies ist derzeit eine Zahl aller geladenen expliziten Pflanzen der Mod, nicht ausschließlich Pam's-Crops**

Die Zahl der **aktiven Species aller Mods** wird separat angezeigt. Auf diese Weise wird die Zahl aus originalen, manuell portierten Pam's-Datapacks nicht mit den zur Laufzeit tatsächlich erfolgreich nachgeladenen Auto-Arten verwechselt. Im Repository vorhandene Dateien müssen nicht aktiv sein, beispielsweise bei fehlender `pamhc2crops`-Mod oder unbestätigten Item-IDs.

**Wichtige Grenzen:** Der Befehl startet **keinen** Reload, scannt **keine** Items erneut und verändert **keine** Spielewelt. Die virtualisierten `auto_pam/`-Ressourcen sind **keine echten JSON-Dateien**. Außerdem gibt es noch keine historisierte Reload-Statistik über mehrere Sitzungen; angezeigt wird der letzte Snapshot. Die aktuelle NeoForge-1.21.1-JAR-Item-Registry ist weiterhin nicht unabhängig auditiert.

**Stand:** Java-Quellcode und Dokumentation gespeichert. **Nicht kompiliert, nicht getestet und nicht im Spiel ausgeführt.** Bei später genehmigten Prüfungen müssen insbesondere Befehlssyntax, Berechtigungslevel 2, unterschiedliche Fehlertypen, `/reload`-Aktualisierung und Server-Logausgaben verifiziert werden. Pam's Trees bleibt ausgeschlossen.

## HarvestCraft 1 – klare Grenze

**HarvestCraft 1** ist keine offizielle NeoForge-1.21.1-Laufzeitmod. Sie wird durch diese Laufzeit-Erkennung **nicht** automatisch geladen, und es wird kein fiktiver `harvestcraft`-Registry-Eintrag erzeugt. Die Legacy-1→2-ID-Zuordnungen verbleiben im eigenständigen, nur als Daten vorliegenden Crosswalk. Eine spätere Item-/Weltmigration benötigt getrennte, ausdrücklich freigegebene Logik.

## Originalmutationen

Die **31 zuvor portierten AgriPlants-Mutationen** bleiben explizite Datapack-Einträge. Der Mutations-Reload prüft jetzt auch die gegebenenfalls neu erkannten Fallback-Species als aktive Eltern oder Kinder. Dadurch können weitere **bereits deklarierte** Rezepte gültig werden, wenn die echten Saat-/Ernte-Items vorhanden sind.

Die Automatik **erfindet keine Mutationen** aus der Ähnlichkeit zweier Pflanzen-IDs und lädt auch nicht heimlich Mutationsdateien, die gar nicht mitgeliefert werden. Das verhindert unbeabsichtigte Zuchtwege.

## Betrieb und offene Prüfaufgaben

- Der zusätzliche Scanner läuft nur bei Datapack-Reload; die Sortierung, die Reservierung expliziter IDs und der feste Grenzwert sollen stabile Ergebnisse gewährleisten.
- Welche weiteren Fallback-Arten tatsächlich erkannt werden, hängt **ausschließlich von den registrierten Items der installierten Pam's-Version** ab. Deshalb lässt sich ohne einen später ausdrücklich genehmigten Mod-/Datapack-Reload-Test **keine konkrete Gesamtzahl automatisch erkannter Arten** nennen.
- Im späteren Testbranch müssen die NeoForge-21.1.265-Signaturen, Datapack-Priorität und `enabled=false`-Vorrang, nicht vorhandene Seed-/Produkt-Paare, Warnmeldungen, Server-Restart und Crop-Sticks-Gameplay geprüft werden.
- Einige Pam's-Pflanzen können andere Benennungen oder Sonderanbauverhalten haben; diese werden absichtlich **übersprungen**, bis ihr Schema mit Originalquellen verifiziert wurde.
- Wenn ein Betreiber automatisch entdeckten Arten originale Bedingungen geben möchte, legt er unter `data/<namespace>/cropgenetics/plants/*.json` eine explizite Spezies-ID mit den richtigen Werten an. Damit überstimmt er die generische Erkennung.

**Keine Builds, Tests, CI-Läufe, Minecraft-Starts oder Importer-/Prüfskript-Ausführung ohne ausdrückliche Nutzerfreigabe.** Änderungen ausschließlich auf `feature/neoforge-1211-foundation`; `entwicklung`, `testing` und `master` bleiben unverändert.
