# Melone/Kürbis: Gain beeinflusst Erntemenge (Entscheidung 21.11.2026)

**Nutzerentscheidung:** Variante 1: weiterhin **eine sichtbare Frucht** innerhalb der Crop Sticks, aber Gain soll die **Erntemenge** beeinflussen. Die Sicherheitsgrenze von **einem Stack (maximal 64 Items) je Produkt und Ernte** ist kein gewolltes Balance-Ziel, sondern soll später auch erhöhte Config-Drop-Multiplikatoren begrenzen.

## Implementierte Core-Baseline (vor Testfreigabe)

| Gain | 1 Kürbis | 2 Kürbisse | 3 Kürbisse | 4 Kürbisse | Melonenstücke |
| --- | --- | --- | --- | --- | --- |
| 1 | 100 % | 0 % | 0 % | 0 % | 3–7 |
| 2 | 90 % | 10 % | 0 % | 0 % | 4–8 |
| 5 | 60 % | 30 % | 10 % | 0 % | 7–11 |
| 10 | 10 % | 30 % | 30 % | 30 % | 12–16 |

- Kürbis: genau **ein** gleichverteilter Zufallswurf mit verschachtelten Schwellen für mindestens zwei `(Gain−1)/10`, mindestens drei `max(0, Gain−4)/10` und vier `max(0, Gain−7)/10`, Gain jeweils auf 1..10 begrenzt. So bleibt **Gain 2 bei 90 % einem und 10 % zwei Kürbissen**, während ein **10/10/10-Genom auch drei oder vier Kürbisse** ernten kann. Maximal vier **Item-Drops in einem Stack**, nicht vier sichtbare Früchte. Dies korrigiert die erste, zu strikte Maximal-zwei-Annahme aus ST-37 nach Nutzerhinweis.
- Melone: Vanilla-ähnliche **3–7 Melonenscheiben**, plus exakt eine zusätzliche Scheibe je Gain-Punkt oberhalb 1. Die eingebaute Melonendefinition nennt nun `minecraft:melon_slice` statt eines ganzen Melonenblocks.
- Pro Ernte und Frucht liegt alles in **einem** ItemStack. Die Kappung liegt bei 64; die Baseline bleibt deutlich darunter. `GourdHarvestRules` enthält die reine Berechnung, `CropHarvest.products` wird sowohl für Rechtsklick- als auch Blockabbau-Drops aufgerufen.
- **Nicht neu implementiert:** Ein allgemeiner Drop-Multiplikator in der Konfiguration. Der vorhandene `gourds.fruitChanceMultiplier` beeinflusst **nur die Chance der Fruchtbildung**; er verändert keine Dropmenge. Bei einer späteren Drop-Multiplikator-Erweiterung muss erst nach der Multiplikation auf einen Stack gedeckelt werden.
- **Alte Welten:** Historische physische, benachbarte Gourd-Fruchtblöcke verwenden weiterhin Vanilla-Loot-Tabellen; diese Legacy-Route wurde bewusst nicht geändert. Neue interne Früchte verwenden die neue Gain-Regel.

## Offene Nachweise

- `GourdHarvestRulesTest` und `CropHarvestBoundTest` um Gain-Grenzen, exakte Würfelschwellwerte, 3-/4-Kürbis-Fälle und Item-Typ ergänzt, **nicht ausgeführt**.
- Im freigegebenen Spieltest je Gain 1/2/5/10 eine interne Melone und Kürbis ernten; **eine einzelne Ernte beweist keine Prozentchance**. Regelmäßige Ernten beobachten, Stem-Regrowth und Ein-Stack-Grenze prüfen; Rechtsklick und Blockabbau getrennt.
- Das spätere Config-Design (Bereich/Default/Rundung von Drop-Multiplikatoren) bleibt ein eigener Entscheidungs- und Implementierungspunkt; keine stillschweigende Umdeutung der bestehenden Fruchtchance-Config.
