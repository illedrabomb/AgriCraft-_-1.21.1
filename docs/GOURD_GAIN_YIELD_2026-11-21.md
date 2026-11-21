# Melone/Kürbis: Gain beeinflusst Erntemenge (Entscheidung 21.11.2026)

**Nutzerentscheidung:** Variante 1: weiterhin **eine sichtbare Frucht** innerhalb der Crop Sticks, aber Gain soll die **Erntemenge** beeinflussen. Die Sicherheitsgrenze von **einem Stack (maximal 64 Items) je Produkt und Ernte** ist kein gewolltes Balance-Ziel, sondern soll später auch erhöhte Config-Drop-Multiplikatoren begrenzen.

## Implementierte Core-Baseline (vor Testfreigabe)

| Gain | Kürbis: 1 Stück | Kürbis: 2 Stück | Melone: Melonenstücke |
| --- | --- | --- | --- |
| 1 | 100 % | 0 % | 3–7 |
| 2 | 90 % | 10 % | 4–8 |
| 5 | 60 % | 40 % | 7–11 |
| 10 | 10 % | 90 % | 12–16 |

- Kürbis: genau ein Zufallswurf mit `P(zweiter Kürbis) = (Gain - 1) / 10`, auf Gain 1..10 begrenzt. **Ein sichtbarer Kürbis kann also maximal zwei Kürbis-Items liefern** – nicht zwei sichtbare Früchte.
- Melone: Vanilla-ähnliche **3–7 Melonenscheiben**, plus exakt eine zusätzliche Scheibe je Gain-Punkt oberhalb 1. Die eingebaute Melonendefinition nennt nun `minecraft:melon_slice` statt eines ganzen Melonenblocks.
- Pro Ernte und Frucht liegt alles in **einem** ItemStack. Die Kappung liegt bei 64; die Baseline bleibt deutlich darunter. `GourdHarvestRules` enthält die reine Berechnung, `CropHarvest.products` wird sowohl für Rechtsklick- als auch Blockabbau-Drops aufgerufen.
- **Nicht neu implementiert:** Ein allgemeiner Drop-Multiplikator in der Konfiguration. Der vorhandene `gourds.fruitChanceMultiplier` beeinflusst **nur die Chance der Fruchtbildung**; er verändert keine Dropmenge. Bei einer späteren Drop-Multiplikator-Erweiterung muss erst nach der Multiplikation auf einen Stack gedeckelt werden.
- **Alte Welten:** Historische physische, benachbarte Gourd-Fruchtblöcke verwenden weiterhin Vanilla-Loot-Tabellen; diese Legacy-Route wurde bewusst nicht geändert. Neue interne Früchte verwenden die neue Gain-Regel.

## Offene Nachweise

- `GourdHarvestRulesTest` und `CropHarvestBoundTest` um Gain-Grenzen und Item-Typ ergänzt, **nicht ausgeführt**.
- Im freigegebenen Spieltest je Gain 1/2/5/10 eine interne Melone und Kürbis ernten; **eine einzelne Ernte beweist keine Prozentchance**. Regelmäßige Ernten beobachten, Stem-Regrowth und Ein-Stack-Grenze prüfen; Rechtsklick und Blockabbau getrennt.
- Das spätere Config-Design (Bereich/Default/Rundung von Drop-Multiplikatoren) bleibt ein eigener Entscheidungs- und Implementierungspunkt; keine stillschweigende Umdeutung der bestehenden Fruchtchance-Config.
