# Pam's HarvestCraft 2 – geprüfte AgriPlants-Mutationen

**Stand:** 16.11.2026; Branch `feature/neoforge-1211-foundation`.  
**Status:** Quellcode-Datapacks eingecheckt, **nicht kompiliert, getestet oder im Spiel bestätigt**.

## Originalquelle

Die Originaldefinitionen stammen aus `AgriCraft/AgriPlants`, Commit `9444e75828f66bd40adabb4aeca2544765408f8a`, Ordner `mod_pamhc2crops/mutations/`. Diese Mutationdateien nennen `version: "1.16.4"`, `mods: ["pamhc2crops","agricraft","minecraft"]`, `enabled: true` und `chance: 0.5`. Der Port besitzt die benötigten modernen Arten bereits als optionale Datapacks.

## 4 vollständig referenzierbare Original-Rezepte

| Ergebnis | Eltern A | Eltern B | Originalchance |
|---|---|---|---:|
| `pamhc2crops:oats_plant` | `pamhc2crops:corn_plant` | `pamhc2crops:rice_plant` | 0.5 |
| `pamhc2crops:okra_plant` | `pamhc2crops:bean_plant` | `pamhc2crops:leek_plant` | 0.5 |
| `pamhc2crops:peas_plant` | `pamhc2crops:soybean_plant` | `pamhc2crops:okra_plant` | 0.5 |
| `pamhc2crops:soybean_plant` | `pamhc2crops:bean_plant` | `pamhc2crops:rice_plant` | 0.5 |

Neue Serverdaten: `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/mutations/pamhc2_{oats,okra,peas,soybean}.json`. Die neuen Dateien nennen nur die vom Port unterstützten Felder und `mods: ["pamhc2crops"]`. Das bislang alte Mod-ID-Label `agricraft` ist keine eigene vorhandene Laufzeitmod und wird korrekt nicht als zusätzliche Voraussetzung verwendet.

**Die 0.5 ist die aus dem Original übernommene Mutationsgrundchance**, nicht die Garantie einer Ingame-Mutation: Vererbungs-/Umweltbedingungen, tatsächliche Eltern, Genetik und serverseitige Mod-Registry werden weiterhin geprüft. Der bestehende Crop-Reload deaktiviert Mutationen, wenn `pamhc2crops` fehlt oder eine beteiligte Art wegen fehlender/abweichender Item-ID nicht geladen werden kann.

## Aktualisierung 16.11.2026: 30 Pflanzen und insgesamt 14 Mutationen

Acht fehlende Elternpflanzen wurden aus dem identischen gepinnten AgriPlants-Originaldatensatz portiert: **Gerste, Sellerie, Brokkoli, Süßkartoffel, Radieschen, Frühlingszwiebel, Cantaloupe und Heidelbeere**. Die Originalwerte für Saatgut, Ernteprodukte, Wachstumsstufen, -wahrscheinlichkeiten und Bodenchemie stehen in den neuen `pamhc2_{barley,celery,broccoli,sweetpotato,radish,scallion,cantaloupe,blueberry}.json`. Die NeoForge-Port-Vereinfachungen für Licht, Sonderpflanzung, Seasons und Fluid gelten weiterhin. Damit liegen **30 optionale Pam's-Crops-Pflanzendefinitionen** als Quellcode vor.

Es wurden **zehn weitere Originalmutationen** mit jeweils `chance: 0.5` übernommen; alle Eltern und Ergebnisse sind jetzt als eigene Port-Species vorhanden:

| Neue Ergebnisart | Original-Eltern A + B |
|---|---|
| Mais (`corn`) | Gerste + Roggen |
| Baumwolle (`cotton`) | Gerste + Sojabohne |
| Kohl (`cabbage`) | Brokkoli + Salat |
| Lauch (`leek`) | Frühlingszwiebel + Sellerie |
| Kiwi (`kiwi`) | Cantaloupe + Erdbeere |
| Traube (`grape`) | Heidelbeere + Cantaloupe |
| Salat (`lettuce`) | Sojabohne + Sellerie |
| Erdbeere (`strawberry`) | Radieschen + Sojabohne |
| Brokkoli (`broccoli`) | Salat + Sojabohne |
| Heidelbeere (`blueberry`) | Erdbeere + Sojabohne |

Die insgesamt **14** Dateien befinden sich unter `data/cropgenetics/cropgenetics/mutations/pamhc2_*.json`. Jede nutzt die originale 50-%-Mutationsgrundchance und den optionalen Mod-Guard `pamhc2crops`. Dies ist **keine bestätigte Erfolgsquote im Spiel** und keine Build-/Testvalidierung.

### Weiterhin absichtlich nicht aktiviert

- Historische `vanilla:`-Artbezeichner (`vanilla:wheat_plant`, `vanilla:carrot_plant`, `vanilla:potato_plant`, `vanilla:sugarcane_plant`, `vanilla:melon_plant`) werden **nicht** ohne explizite und verifizierte ID-Umsetzung auf neue `minecraft:`-Species gemappt. Betroffen sind unter anderem die Originalmutationen für Tomate, Reis, Roggen, Gerste, Sellerie, Süßkartoffel, Frühlingszwiebel und Cantaloupe.
- Zwiebel benötigt zusätzlich `brusselsprout_plant`, Radieschen ebenfalls; weitere Originalmutationen können nicht automatisch ohne vollständige Elternarten übernommen werden.
- Einträge ohne belegte Originalmutationsdatei (wie `flax`, `jute`, `taro`, `kale`, `kenaf`) erhalten keine erfundenen Elternkombinationen.

**Wichtig:** Die frühere Tabelle fehlender Arten in älteren Projektstatus-Einträgen ist historisch; die 8 neu aufgenommenen Species sind inzwischen vorhanden. Die Namenszuordnung von alten zu neuen HarvestCraft-1-Items ist dagegen weiterhin ein nur vorbereiteter Crosswalk, keine Item-/Weltmigration.

## Ziel-Registry-Status

Pam's HarvestCraft 2 Crops hat eine veröffentlichte NeoForge-1.21.1-Version. Die genaue Registry-Identität aller Crop-Items dieser Version wurde bislang **nicht** durch einen direkten Bytecode-/JAR- oder Laufzeitaudit bestätigt. Die Datenpakete validieren Item-IDs später beim normalen Server-Reload; diese Laufzeitvalidierung wurde noch nicht durchgeführt.

## Aktualisierung 16.11.2026: insgesamt 25 Mutationen und 31 Pam's-Crops

`brusselsprout` wurde aus der originalen AgriPlants-Pflanzendatei als neue optionale Crop-Sticks-Spezies importiert. Die beiden bislang gesperrten Originalmutationen **Zwiebel = Sellerie + Rosenkohl** und **Radieschen = Tomate + Rosenkohl** sind damit vollständig referenzierbar und als Datapacks aufgenommen.

Außerdem wurden **neun** weitere Originalmutationen portiert, nachdem sechs historische `vanilla:`-Eltern-IDs mit **exakt geprüften vorhandenen** `minecraft:`-Species abgeglichen worden waren. Alle Originalmutationen behalten Elternkombination und `chance: 0.5`; es wurden keine unbelegten Rezepte ergänzt. Damit sind jetzt **31 optionale Pflanzen** und **25 originale Pam's-2-Mutationsrezepte** als Quellcode vorhanden. Alte Abschnitte dieser Datei, die 14 Mutationen und fehlende Rosenkohl-/Vanilla-Eltern nennen, sind **historische Zwischenstände** und werden durch diesen Nachtrag abgelöst.

Maßgebliche Einzelquellen, Zuordnungstabelle und Grenzen: `docs/PAMS_VANILLA_SPECIES_ALIASES_AND_BRUSSELSPROUT.md`. Die konkreten Item-Registrierungen der Pam's-2-NeoForge-1.21.1-JAR sind weiterhin unbestätigt. **Keine Builds, Tests oder Skripte ausgeführt.**

## Nächste Schritte

1. Die noch fehlenden Originalpflanzen wie `brusselsprout` systematisch prüfen; später weitere Pam's-Arten und Mutationsketten aus Originaldaten ergänzen.
2. Alte `vanilla:`-Artbezeichner gesondert normalisieren, wenn die tatsächlichen IDs sicher bekannt sind.
3. Quelle für die NeoForge-1.21.1-Registrierung von Pam's Crops finden; bis dahin keine Aussage, dass Item-IDs dort erfolgreich aufgelöst wurden.
4. Echte Kompilierung, Datapack-Reload, Pflanzenwachstum und Mutation ausschließlich nach ausdrücklicher Testfreigabe auf `testing`.

**Branchregel:** `entwicklung`, `testing` und `master` sind unverändert.
