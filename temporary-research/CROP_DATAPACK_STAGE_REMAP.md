# Wachstumsfortschritt nach Datapack-Reload

Stand: 2026-11-16. Änderungen direkt im Branch `feature/neoforge-1211-foundation`.

## Problem
`CropSticksBlockEntity.reconcileLoadedStage(plant)` übernahm beim Wechsel der Anzahl definierter Wachstumsstufen bislang den alten Index und begrenzte ihn höchstens auf die neue Obergrenze. Beispiel: Eine reife Pflanze bei 8 Stufen (Index 7) wurde bei einer neuen Definition mit 16 Stufen auf Index 7 gesetzt und war damit nicht mehr reif.

## Neue Migrationsregel
`CropGrowth.remapStage(age, oldTotal, newTotal)` überträgt den relativen Fortschritt anhand der ganzzahligen Position zwischen Anfang (Index 0) und Ende (Index `total-1`). Das Ergebnis wird auf den nächstgelegenen ganzzahligen neuen Index gerundet. Die Rechnung verwendet `long` für das Produkt und begrenzt den alten Index auf den gültigen Bereich.

- `0 / 7` (8 Stufen) wird zu `0 / 15` (16 Stufen).
- `7 / 7` (reif) wird zu `15 / 15` (weiterhin reif).
- `4 / 7` wird zu `9 / 15` (ungefähr 57 %).
- `15 / 15` (reif) wird bei 8 Stufen zu `7 / 7` (weiterhin reif).
- Bei `oldTotal <= 1` wird die Pflanze in der neuen Definition als reif behandelt; bei `newTotal <= 1` wird Index 0 gesetzt.

Der vorhandene `onLoad`-Hook, der periodische Server-BlockEntity-Ticker und der Random-Tick- bzw. Harvest-Pfad rufen bereits `reconcileLoadedStage` auf. Die Migration greift deshalb auch bei geladenen Chunks nach `/reload`, selbst wenn Random Ticks deaktiviert sind.

## Schutzmaßnahmen
- Genom, Saatgutherkunft und Analyseflag werden nicht verändert.
- Physische Fruchtzuordnung (Token, Richtung und `fruitReady`) wird nicht verändert.
- Wenn das alte `TotalGrowthStages`-NBT-Feld fehlt oder ungültig ist (`0`), kann kein zuverlässiger relativer Fortschritt rekonstruiert werden. In diesem Fall wird der gespeicherte Index wie bisher begrenzt statt eine willkürliche Ausgangsstufenzahl anzunehmen.
- Unterschiede an den `harvestStage`-Werten oder am `HarvestMode` allein lösen keine Stufenmigration aus. Die neue Definition bestimmt die aktuellen Ernte-/Regrow-Regeln, solange die Pflanzenart gleich bleibt.
- Die Datenpublikation durch `AgriPlantsRegistry.replaceAll` bleibt unverändert atomar.

## Offen
- Wechsel eines Vanilla-Gourd-Adapters auf einen anderen Harvest-Modus bei bestehender physischer Frucht erfordert eine gesonderte Schutzregel und Ingame-Prüfung.
- Geänderte Stufenwerte bei *gleicher* Stufenanzahl werden nicht als Fortschrittsänderung interpretiert. Das Modell behandelt diese Array-Werte aktuell nicht als echte numerische Wachstumsschwellen.
- Build, Tests, CI und Minecraft wurden auf ausdrücklichen Wunsch **nicht** ausgeführt.
