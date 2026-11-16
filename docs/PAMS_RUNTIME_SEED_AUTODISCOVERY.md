# Pam's HarvestCraft 2 – automatische Saatgut-Erkennung beim Datapack-Reload

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
