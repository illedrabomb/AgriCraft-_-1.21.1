# Genetische Samen: Implementierungsstand und Übergabe
Stand: 2026-11-15 | Branch `feature/neoforge-1211-foundation`

## Benutzerentscheidung
Der Nutzer möchte zunächst **ohne weitere Tests** weiterentwickeln. Deshalb ist `.github/workflows/neoforge-1211.yml` nur noch manuell via `workflow_dispatch` ausführbar. Vorhandene Tests bleiben im Repository, wurden für diesen Schritt nicht gestartet. **Nicht als validiert markieren.**

## Neu implementiert
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/genetics/GenomeCodecs.java`: Codec für `Genome` und `GenePair` sowie netzwerkfähiger `StreamCodec<RegistryFriendlyByteBuf, Genome>`, inklusive aller fünf Gene und der Pflanzen-ID.
- `.../content/CropComponents.java`: registriert das persistente und synchronisierte Data Component `cropgenetics:genome`.
- `.../content/GeneticSeedItem.java`: Item mit Tooltip für Pflanze und drei sichtbare 10/10/10-Werte.
- `.../content/CropItems.java`: registriert `cropgenetics:genetic_seed` und bietet `seedWithGenome(Genome)`, welches den vollständigen Genotyp auf den Stack schreibt.
- `.../CropGeneticsMod.java`: registriert Item + Data Component auf dem NeoForge-Mod-EventBus und fügt dem Kreativinventar ein Karotten-Beispielsaatgut hinzu.
- Item-Modell (vorläufig Vanilla-Saatguttextur) und englische/deutsche Lokalisierung.

## Einschränkungen
- **Kein Test/Build dieser Änderungen ausgeführt.** APIs basieren auf NeoForge 1.21.1-Dokumentation; die neue Java-Codeänderung ist noch unvalidiert.
- Die genetischen Samen sind ItemStacks mit persistentem Genom; **sie können noch nicht auf Crop Sticks wachsen** und besitzen keine benutzerdefinierte Pflanzinteraktion.
- Das Karotten-Beispielsaatgut im Kreativinventar besitzt Standardwerte 1/1/1.
- Es wird noch kein Datapack-PlantDefinition-Lookup zur Erstellung der Kreativinhalte verwendet.
- In `GenomeCodecs` nutzt das Speicherschema Strings und integer Allele. Vor Veröffentlichung Versionierung und Migration von gespeicherten Samentypen prüfen.
- Die drei sichtbaren Werte sind Growth / Gain / Strength; Fertility und Mutativity bleiben genetisch erhalten. Resistance (Weeds) ist bewusst nicht Teil des Kernmodells.

## Nächste Implementierungsaufgaben
1. `CropStickBlock` und `CropStickBlockEntity` mit Genomaufnahme und Datenspeicherung, ohne InfinityLib.
2. Setzen/Entnehmen von Samen, Differenzierung normale Stäbe/Doppelstäbe, Tick, Elternauswahl.
3. PlantDefinition-gestützte Wachstums-/Erntelogik und Seed-Konvertierung.
4. Datapack-/Mutationsregeln in die Zuchtlogik einklinken; Spezial-Adapter für fremde Mods.
5. Erst wenn wieder gewünscht: NeoForge Build, Client und Dedicated Server sowie Regressionstests.

## Lizenz
Fork-Basis AgriCraft, ursprüngliche Mitwirkende und vorhandene Lizenzhinweise bewahren.
