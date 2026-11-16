# Nachbearbeitung des Playtests – PT-02, PT-03, PT-04

**Datum:** 16.11.2026  
**Repository:** `illedrabomb/AgriCraft-_-1.21.1`  
**Branch:** `integration/neoforge-1211-consolidation`  
**Basis vor Nachbearbeitung:** `ec8e15775391f69e5270820706601181043b02f4`  
**Status:** Quellcode-Änderungen und gezielte Diagnose. **Kein neuer lokaler Build, kein Minecraft-Start und kein neuer Gameplay-Abnahmetest.** Eine Produktivfreigabe ist nicht erfolgt.

## PT-03: Graue Stängel und generisches Pam-Laub

**Quellursache:** `CropSticksRenderer` zeichnet Pflanzenflächen direkt mit atlasbasierten Vertices und hat bisher alle Vertexfarben mit `setColor(255,255,255,255)` auf Weiß gesetzt. Vanilla-Blockmodell-Farbhandler werden bei dieser BER-Zeichenstrecke nicht automatisch angewandt.

**Änderung (`CropSticksRenderer.java`, Commit `8b7e2b1`):** Bei Vanilla-`melon_stem`-/`pumpkin_stem`-Sprites wird jetzt die zugehörige Vanilla-Stängelfärbung anhand des erreichten Wachstumsalters aus `Minecraft.getInstance().getBlockColors()` ermittelt und als Vertex-RGB verwendet. Für den üblichen `minecraft:block/oak_leaves`-Fallback wird der Vanilla-Oak-Leaves-Farbhandler verwendet. Alle anderen Texturen bleiben bewusst weiß/unverändert, um mod-spezifische Farben nicht zu erraten. Der bereits separat registrierte Farbhandler der angehängten Stängel bleibt unverändert.

**Abnahme noch nötig:** Client-Neustart, F3+T, Darstellung von unreifen/reifen Melonen- und Kürbisstängeln, angeschlagenen/angehängten Stängeln und automatisch erkannter Pam-Art in mehreren Biomen. Die Korrektur ist **implementiert, aber nicht visuell verifiziert**.

## PT-02: Zweiter physischer Fruchtzyklus

**Befund:** Der Playtestbericht belegt einwandfrei erste Früchte, deren tokengebundene Entfernung und den Reset auf Stufe 4. Nach erneutem Erreichen von Stufe 7 blieb `FruitReady=0`; die unmittelbare Ursache ist bislang **nicht bestätigt**. Die bestehende `CropSticksBlock.randomTick`-Logik lässt grundsätzlich weitere Fruchtversuche zu, sofern `growthFactor > 0`, `HarvestMode.GOURD`, `fruitReady=false`, der Random-Wurf erfolgreich ist und mindestens ein zulässiger, freier Nachbarblock vorhanden ist. Die alte Testwelt lag in einem Frostbiom und nutzte beschleunigte Random Ticks. Aus den vorhandenen NBT-Zeitreihen allein lässt sich der exakte Ausfallgrund nicht ableiten.

**Änderung (`GourdFruits.java`, Commit `e8268ee`):** Die Prüfung auf wirklich freie Zielblöcke, geladenen Ziel-/Bodenchunk und zulässigen Boden wurde in eine gemeinsame, rein lesende Funktion `placeable` extrahiert. Sowohl die echte Platzierung als auch die Diagnose verwenden jetzt denselben Prädikat-Pfad; **keine Änderung an Fruchtchance, Spielbalance, Block-Ownership, Token, Ertrag, Reset oder tatsächlicher Platzierungsfreigabe**.

**Neuer Operator-Befehl (`CropDataCommand.java`, Commit `ee703ec`):**

```
/cropgenetics data gourd <x> <y> <z>
```

Dieser Befehl zeigt für eine geladene, bepflanzte Crop-Sticks-Position:

- Pflanzen-ID, `HarvestMode`, genaue Wachstumsstufe und `FruitReady`
- Aktuellen `growthFactor`, Lichtstufe und berechnete Fruchtchance **pro tatsächlichem Random-Tick-Versuch**
- Für Nord/Süd/West/Ost: ob der Nachbarblock frei ist, welcher Boden darunter liegt, ob die Position als Fruchtziel geeignet ist und ob ein Nachbarchunk fehlt

Er führt **keine Random-Würfe, Blockänderungen, Chunk-Loads oder Fruchtneuerzeugung** aus.

**Gezielte Abnahme nötig:** Auf derselben isolierten Testwelt die Werte bei erster reifer Frucht (vor Abbau), unmittelbar nach Fruchtabbau, nach erneutem Reifwerden und während wiederholter Tickfenster dokumentieren. Dabei Licht, Uhrzeit, Boden, `growthFactor`, Fruchtmode, Random-Tick-Einstellungen, sämtliche Nachbarn und Bodenprofile vergleichen. Werden `growthFactor > 0` und mindestens ein freies zulässiges Ziel bestätigt, aber auch unter kontrollierten vielen Tickversuchen keine zweite Frucht gesetzt, dann den konkreten `tryPlace`-/Bind-/Token-Fehler im Dedicated Server weiter eingrenzen. Erst nach gesichertem Befund eine Spiellogik-Korrektur vornehmen.

**PT-02 ist weiterhin offen; ein Diagnosetool ist keine Gameplay-Fehlerbehebung.**

## PT-04: Ungültige rabbit_food-IDs der offiziellen Pam's-Crops-1.0.9-JAR

Der Playtest meldete in der **unveränderten Fremd-JAR** für `minecraft:tags/item/rabbit_food.json` ungültige Item-IDs (u.a. Leerzeichen nach `pamhc2crops:` und großgeschriebene Vanilla-IDs). Der Fehler tritt vor bzw. unabhängig von Crop Genetics' Pflanzenauflösung auf; beim Test wurden trotzdem 113 Arten und 34 Mutationen geladen.

**Entscheidung:** Nicht ungefragt die fremde JAR modifizieren und **keine** automatische Überschreibung des globalen `minecraft:rabbit_food`-Tags in unseren Standarddatapacks einführen. Das könnte andere Mods/Datapacks und Rabbit-Futterregeln unbemerkt beeinflussen; zudem wäre ohne echten Server-Nachtest nicht gesichert, dass der beschädigte Fremdtag damit sicher überschrieben werden kann. Stattdessen den Befund und die betroffene exakte JAR-Version dokumentieren, auf einen möglichen Upstream-Fix verweisen und gegebenenfalls einen **separat aktivierbaren Kompatibilitätsdatapack** nach einer gezielten Ressourcen-Prioritätsprüfung entwerfen. Kein Blind-Patch und keine Abnahme als fehlerfreie Drittanbieterintegration.

## Erlaubte Änderungen / Branch-Grenzen

Diese Nachbearbeitung enthält nur GitHub-Quellcodeänderungen auf `integration/neoforge-1211-consolidation`, keine Builds, Tests, Minecraft-, Server-, Client- oder Skriptläufe und keine Merge-/Release-Aktionen. `entwicklung`, `testing` und `master` bleiben unverändert. Weiterhin ausdrücklich ausgeschlossen sind **Pam's HarvestCraft 2 Trees, Setzlinge und Obstbaumgenetik**. Das geplante MineColonies-Addon bleibt auf der späteren Roadmap.
