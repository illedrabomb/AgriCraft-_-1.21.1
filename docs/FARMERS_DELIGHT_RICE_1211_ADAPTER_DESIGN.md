# Farmer's Delight Reis – 1.21.1-Kompatibilitätsentwurf (19.11.2026)

> Status: **Versionsgenaue Quellprüfung und Warnschutz umgesetzt. Kein funktionierender Reisadapter implementiert.** Diese Datei dient als Übergabe an einen Folge-Chat/eine andere AI. Kein JUnit-, Gradle- oder Minecraft-Lauf.

## Verbindliche Quellen

- Hersteller-Branch `vectorwing/FarmersDelight`, `1.21`, `gradle.properties`: `minecraft_version=1.21.1`, `mod_id=farmersdelight`, `neo_version=21.1.219`, `mod_version=1.3.4`. Quelle: https://github.com/vectorwing/FarmersDelight/blob/1.21/gradle.properties
- `RiceBlock.java`: https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/block/RiceBlock.java
- `RicePaniclesBlock.java`: https://github.com/vectorwing/FarmersDelight/blob/1.21/src/main/java/vectorwing/farmersdelight/common/block/RicePaniclesBlock.java
- Änderungsprotokoll: https://github.com/vectorwing/FarmersDelight/blob/1.21/changelog.md
- Technische Aussagen stammen aus diesem **konkreten 1.21.1-Quellbranch**, nicht aus aktuellen 26.x-Wikis. Für die tatsächliche Spieler-JAR ist weiterhin ein SHA-/Registry-Abgleich nötig.

## Tatsächliche Reis-Mechanik

1. Die untere Reispflanze setzt bei `getStateForPlacement` **stehendes Quellwasser am eigenen Blockplatz** voraus. `canSurvive` prüft ebenfalls `FluidTags.WATER` und `fluid.getAmount()==8`. Boden kann unter anderem Dirt sein; einfaches trockenes Farmland reicht nicht.
2. Die untere Pflanze hat `AGE_3`, erzeugt nach Reife im **freien Block darüber** eine eigene `RicePaniclesBlock`. Der obere Teil hat nochmals `AGE_3`, wächst und kann unabhängig vom unteren geerntet werden.
3. `RicePaniclesBlock.mayPlaceOn` benötigt unten den speziellen `RICE_CROP`-Block. Die untere Pflanze merkt, ob sie eine obere Rispe trägt. Oberteil und Unterteil sind **zwei physische Blöcke mit getrenntem Lebenszyklus**.
4. Der veröffentlichte Changelog beschreibt unterschiedliche Ernteergebnisse mit/ohne Messer; der obere Teil kann nachwachsen, ohne den unteren jedes Mal auszureißen. Details der Loot-Tabellen und der konkreten Spieler-JAR müssen noch abgeglichen werden.
5. `ModItems.RICE` dient als Saatgut; ein beliebiges `farmersdelight:rice_seeds` wäre eine unbelegte Erfindung. Keine automatische Item-Zuordnung nach einer hypothetischen Suffixregel.

## Warum unser aktueller Standard-Crop-Stick kein Reisadapter ist

- `CropSticksBlock` ist ein einzelner Block mit `CROSS`, `PLANTED`, `STAGE`, `FRUIT` und `SPECIES`. Er implementiert aktuell **keinen** passenden Fluid-Container/Quellwasserzustand und besitzt keinen physisch getrennten oberen `RicePaniclesBlock`.
- Die Ernte läuft über `CropHarvest.products` (ein Stack je Produkt und Ernte), nicht automatisch über Farmer's-Delight-Messer-/Panicle-Loot. Ein Datapack mit `rice_plant` und `HarvestMode.STANDARD` wäre daher **nur eine explizit vereinfachte Nachbildung**, keine Originalparität.
- `FarmersDelightCropDiscovery` und die JEI-Kandidatenliste schließen Reis bisher zu Recht aus.

## Aktuell ergänzter Warnschutz

- `SpecialCropCompatibilityWarnings.forPlant("farmersdelight:rice_plant")` gibt einen unmissverständlichen Hinweis zu Wasser, zweitem Block und Werkzeug-Ernte aus.
- `CropDataReloadListener` meldet bei explizitem `farmersdelight:rice_plant` künftig `WARNING`, selbst wenn die gewöhnliche Definition technisch gültig ist. Die explizite Datapack-Entscheidung **bleibt aktiv**; es gibt keinen stillschweigenden Override und keine globale Sperre.
- Regressionen in `SpecialCropCompatibilityWarningsTest` prüfen den Warntext und unveränderte Nicht-Reis-Pflanzen.
- **Stand aller Tests: nicht ausgeführt.**

## Geplanter späterer echter Adapter

1. Separate wasserverträgliche Crop-Sticks-Variante oder ein expliziter, versionsgebundener Brückenmechanismus mit konsistentem 2-Block-Eigentum. Ein bloßer `PlantDefinition`-Eintrag reicht nicht.
2. Die genetische Seed-Instanz und ihre Herkunft (`GENOME`, `ORIGINAL_SEED`, `ANALYZED`) bleiben ausschließlich serverautorisiert und dauerhaft speicherbar.
3. Wasserquellenzustand, Boden, angrenzende Chunks, Licht, Blockabbau, Kolben-/Fluidupdates, Bonemeal und Wachstum beider Stufen müssen deterministisch zusammenpassen. Keine fremde Farmer's-Delight-Klasse direkt aus einem immer geladenen Core-Mod-Klasseninitialisierer referenzieren.
4. Oberen Reisbereich separat ernten und ggf. nachwachsen lassen. Tool-/Knife-/Straw-/Panicle-Drops am 1.21.1-Original und der **ein Stack pro Ernteprodukt**-Projektregel ausrichten; mögliche Zielkonflikte explizit entscheiden.
5. Erst nach tatsächlichem JAR- und Laufzeitnachweis in der JEI-Info und Autoerkennung als unterstützt freigeben; dabei serverseitige Datapack-Deaktivierung und fehlende optionale Mod testen.

## Pflicht-Regressionen für später

- Mit Mod abwesend: Start und Datapack-Reload ohne Classloadingfehler, Reis nicht automatisch angeboten.
- Mit Mod vorhanden: Reis korrekt aus Item und Wasserquelle pflanzbar, oberes Element entsteht nur bei freiem Platz, Unterteil bleibt bei Panicle-Ernte erhalten, nachwachsende Rispen und Messer-Drops verhalten sich wie vorgesehen.
- Zwei gleichzeitig betroffene Chunks, Welt-Save/Reload, Mod-/Datapack-Deaktivierung, Client/Server-Sync, Blockabbau unter Wasser, genetische Kreuzung und Originalseed-Erhalt.
- Negativfälle: Fließwasser, trockenes Farmland, fehlendes Wasser, belegter Raum oberhalb, fehlender unterstützender Unterblock, Ernte mit/ohne Knife.
