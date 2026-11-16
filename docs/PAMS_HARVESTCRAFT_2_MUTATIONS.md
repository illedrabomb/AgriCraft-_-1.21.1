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

## Absichtlich nicht aktiviert

Folgende geprüfte Originalmutationen zu bereits importierten H2-Arten sind noch **nicht** aktivierbar, weil mindestens eine Voraussetzung noch nicht als moderne Port-Art vorhanden ist:

| Ergebnis | Noch fehlende Original-Eltern bzw. anderer Adapter |
|---|---|
| Tomate | `sweetpotato_plant` und Legacy-`vanilla:carrot_plant` |
| Mais | `barley_plant` |
| Reis | Legacy-`vanilla:sugarcane_plant` |
| Roggen | `barley_plant` und Legacy-`vanilla:wheat_plant` |
| Zwiebel | `celery_plant`, `brusselsprout_plant` |
| Kohl | `broccoli_plant` |
| Baumwolle | `barley_plant` |
| Lauch | `scallion_plant`, `celery_plant` |
| Kiwi | `cantaloupe_plant` |
| Traube | `blueberry_plant`, `cantaloupe_plant` |
| Salat | `celery_plant` |
| Erdbeere | `radish_plant` |
| Weitere Mutationen | Noch vollständig zu inventarisieren; teils noch nicht portierte Species |

Nicht in den ursprünglichen Mutationsdateien gefundene Ziele (z. B. `flax`, `jute`, `taro`, `kale`, `kenaf`) bekommen **keine erfundenen Rezepte**. Ältere Vanilla-IDs wie `vanilla:carrot_plant` sind ausdrücklich **nicht** mit `minecraft:carrot_plant` gleichgesetzt, bevor eine entsprechende Adapterregel sauber dokumentiert und implementiert wurde.

## Ziel-Registry-Status

Pam's HarvestCraft 2 Crops hat eine veröffentlichte NeoForge-1.21.1-Version. Die genaue Registry-Identität aller Crop-Items dieser Version wurde bislang **nicht** durch einen direkten Bytecode-/JAR- oder Laufzeitaudit bestätigt. Die Datenpakete validieren Item-IDs später beim normalen Server-Reload; diese Laufzeitvalidierung wurde noch nicht durchgeführt.

## Nächste Schritte

1. Zusätzliche originale Pam's-Arten wie `barley`, `celery`, `broccoli`, `radish`, `sweetpotato`, `scallion`, `cantaloupe`, `blueberry` einzeln auf ihre Originalanforderungen prüfen und portierbar machen, dann entsprechende Originalmutationen freigeben.
2. Alte `vanilla:`-Artbezeichner gesondert normalisieren, wenn die tatsächlichen IDs sicher bekannt sind.
3. Quelle für die NeoForge-1.21.1-Registrierung von Pam's Crops finden; bis dahin keine Aussage, dass Item-IDs dort erfolgreich aufgelöst wurden.
4. Echte Kompilierung, Datapack-Reload, Pflanzenwachstum und Mutation ausschließlich nach ausdrücklicher Testfreigabe auf `testing`.

**Branchregel:** `entwicklung`, `testing` und `master` sind unverändert.
