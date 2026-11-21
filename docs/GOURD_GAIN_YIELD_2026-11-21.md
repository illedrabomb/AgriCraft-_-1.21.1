# Melone/Kürbis: Gain beeinflusst Erntemenge (Entscheidung 21.11.2026)

**Nutzerentscheidung:** Variante 1: weiterhin **eine sichtbare Frucht** innerhalb der Crop Sticks, aber Gain soll die **Erntemenge** beeinflussen. Die Sicherheitsgrenze von **einem Stack (maximal 64 Items) je Produkt und Ernte** ist kein gewolltes Balance-Ziel, sondern soll später auch erhöhte Config-Drop-Multiplikatoren begrenzen.

## Implementierte Core-Baseline (vor Testfreigabe)

**Aktuelle Balance nach Nutzerfreigabe** (die älteren Regeln aus ST-37/ST-38 sind überholt):

| Gain | 1 Kürbis | 2 Kürbisse | 3 Kürbisse | 4 Kürbisse |
| --- | --- | --- | --- | --- |
| 1 | 100 % | 0 % | 0 % | 0 % |
| 2 | 81 % | 19 % | 0 % | 0 % |
| 5 | 38 % | 46 % | 16 % | 0 % |
| 8 | 14 % | 39 % | 42 % | 5 % |
| 10 | 10 % | 35 % | 40 % | 15 % |

Zwischen Gain 1/2/5/8/10 werden die **kumulativen Schwellen** für mindestens 2/3/4 Items abschnittsweise linear interpoliert; gerundete Tabellenwerte summieren sich auf 100 %. Gain 1–10 wird begrenzt. Die Melonenscheiben-Kurve bleibt unverändert (Gain 1: 3–7, Gain 2: 4–8, Gain 5: 7–11, Gain 10: 12–16).

- Kürbis: genau **ein** gleichverteilter Zufallswurf mit verschachtelten Schwellen für mindestens zwei/drei/vier Stück, jetzt anhand der **freigegebenen neuen Kurve** interpoliert. Ein sichtbarer Kürbis kann maximal vier Kürbis-Items liefern; vier sind bei Gain 10 nur noch mit **15 %** möglich. Die frühere Gain-2-Regel 90/10 und die erste ST-38-Kurve sind durch die aktuelle Tabelle ersetzt.
- Melone: Vanilla-ähnliche **3–7 Melonenscheiben**, plus exakt eine zusätzliche Scheibe je Gain-Punkt oberhalb 1. Die eingebaute Melonendefinition nennt nun `minecraft:melon_slice` statt eines ganzen Melonenblocks.
- Pro Ernte und Frucht liegt alles in **einem** ItemStack. Die Kappung liegt bei 64; die Baseline bleibt deutlich darunter. `GourdHarvestRules` enthält die reine Berechnung, `CropHarvest.products` wird sowohl für Rechtsklick- als auch Blockabbau-Drops aufgerufen.
- **Jetzt implementiert:** `harvest.dropMultiplier` in der Serverkonfiguration, Standard `1.0`, erlaubt `0.0` bis `64.0`. Die berechnete Erntemenge wird anschließend multipliziert und auf die nächste ganze Zahl gerundet; `0` deaktiviert nur Ernteprodukte (nicht Samen-/Crop-Sticks-Rückgabe). Jede Itemart wird danach auf genau **einen normalen Stack, maximal 64**, begrenzt. Der separate `gourds.fruitChanceMultiplier` beeinflusst weiterhin **nur die Chance der Fruchtbildung**.
- **Alte Welten:** Historische physische, benachbarte Gourd-Fruchtblöcke verwenden weiterhin Vanilla-Loot-Tabellen; diese Legacy-Route wurde bewusst nicht geändert. Neue interne Früchte verwenden die neue Gain-Regel.
- **ST-45, Rohmenge bei Standardpflanzen:** Die Gain-/Datapack-Produktmengen werden erst vollständig pro Itemart aufsummiert, danach mit dem Drop-Faktor multipliziert und **erst zum Schluss** auf die Stackgröße begrenzt. Vorher reduzierte die vorzeitige 64er-Kappung bei Faktoren unter 1 die Auszahlung (z. B. Rohmenge 128 × 0,5 fälschlich 32 statt 64). Ein `long`-Zähler mit Überlaufschutz verhindert dabei int-Überlauf; Kürbis/Melone verwenden weiter denselben finalen Skalierer. JUnit-Quellfälle vorhanden, **nicht ausgeführt**.

## Offene Nachweise

- `GourdHarvestRulesTest` und `CropHarvestBoundTest` um Gain-Grenzen, exakte Stützpunkte/Schwellwerte, monotone Wahrscheinlichkeiten, 3-/4-Kürbis-Fälle und Item-Typ ergänzt, **nicht ausgeführt**.
- Im freigegebenen Spieltest je Gain 1/2/5/10 eine interne Melone und Kürbis ernten; **eine einzelne Ernte beweist keine Prozentchance**. Regelmäßige Ernten beobachten, Stem-Regrowth und Ein-Stack-Grenze prüfen; Rechtsklick und Blockabbau getrennt.
- Im später freigegebenen Lauf `harvest.dropMultiplier` bei `0`, `1`, `2` und einem hohen Wert prüfen: Rechtsklick- und Blockabbau-Ernten, normales Produkt, Melonenscheiben und Kürbis; Samen/Crop Sticks dürfen davon nicht verschwinden. Für effiziente Tests erlaubt `/cropgenetics harvestmultiplier [0..64]` Operatoren einen **temporären Wert nur für den laufenden Server**. `/cropgenetics harvestmultiplier` zeigt den wirksamen Wert und seine Herkunft; `/cropgenetics harvestmultiplier reset` stellt die Serverconfig wieder her. Ein echter Serverneustart entfernt den Override; gespeicherte Config wird nicht geändert. **Nur geschrieben, nicht getestet.**
