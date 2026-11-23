# Automatisierte Gameplay- und Kompatibilitätsprüfungen – 23.11.2026

Getesteter Quellcommit: `e0670fae3ee63ec0552625a8750de0d38d57ca78`; Dokumentations-HEAD vor dem Lauf: `48f83db1f9c959fcad053fc7bfbad09421da6f69`. Hauptmod-JAR `cropgenetics-0.2.14-alpha.jar`, 403199 Bytes, SHA-256 `313fb7466b110cdf5e5c255196e387e2f4393049500705a4830b22a93f79a08e`. Windows 11, Temurin 21.0.12.1+1, Gradle 9.2.1, Minecraft 1.21.1, NeoForge 21.1.265.

Der Nutzer hat in diesem Chat ausdrücklich Tests mit den JARs aus `G:\Curse Forge Minecraft\Instances\Playtest- Eiegne Mods\mods` freigegeben. Die CurseForge-Instanz, deren Saves und Mods wurden nur gelesen und nicht verändert. Alle Aktionen liefen in der isolierten Welt `work/gameplay-20261123-e0670fae/server/isolated-core-world`; Server und Clients wurden am Ende gespeichert beziehungsweise beendet, Ports 25589/25599 sind geschlossen.

## Ergebnis

| Fall | Status | Nachweis |
| --- | --- | --- |
| UI-01 | pass | Kombinierter Analyzer/Seed Chest: 64 normale Seeds per Shift-Klick korrekt auf Analyzer-Slot 0 und Chest verteilt; Füllslots 2–8 lehnen Stick ab; genetischer Seed und Journal nur in Slots 0/1; alle Mengen vollständig erhalten und anschließend entnommen. |
| UI-09 | pass | Im Survivalmodus legt Sneak + kontrollierter Rechtsklick genau ein Journal in Slot 1; Hand wird synchron geleert. Ohne Sneak öffnet das Journalbuch und das zweite Journal bleibt erhalten. Der erste Kreativmodus-Rohversuch meldete erwartungsgemäß keinen Handverbrauch und ist kein Fehlernachweis. |
| UI-16 | pass | Alleinstehende 3x9-Seed-Chest nimmt normale und genetische Seeds per Shift-Klick auf, lehnt Stick und Journal serverseitig ab; Fremditems bleiben im Spielerinventar. |
| ER-13 | pass | Melone Gain 1: 6 und 3 Scheiben; Gain 10: 15 und 15 Scheiben. Vier getrennte reife Fruchtzyklen innerhalb der festgelegten Grenzen, keine ganzen Melonenblöcke. |
| ER-12 | partial | Je zwei Kürbiszyklen für Gain 1/2/5/8/10. Einzelbeobachtungen innerhalb 1–4; Gain 1 zweimal genau 1. Der letzte Rohwert 6 ist ein bereits markierter 3er-Itemstack plus neu hineingemergte 3 und kein 6er-Einzeldrop. Zehn Stichproben sind kein statistischer Kurvennachweis. |
| UI-12 Jade | pass für Basiskompatibilität | Jade 15.10.6 lädt das `CropGeneticsJadePlugin`; reife Crop Sticks zeigen Art und Stufe im Jade-Panel. Pflanzen/Ernte und eigene Anzeige funktionieren, keine Genwerte werden offengelegt. |
| UI-13 Identify | pass für Basiskompatibilität | Identify 0.4.0 startet separat, Client verbindet und Crop-Sticks-Interaktion funktioniert. Identify zeigt Block/Mod; die eigene Zeile zeigt `Wheat Crops · Growth stage 7/7`. Keine privaten Gene sichtbar. Eine Identify-spezifische erweiterte Integration ist weiterhin nicht belegt oder vorausgesetzt. |
| UI-11 JEI | partial | JEI 19.64.0.455 und mezz_config 0.7.1 starten separat; `cropgenetics:jei` registriert Rezepte/Kategorie, Serverbeitritt und `/reload` funktionieren ohne neuen Fehler. Inhalte der einzelnen Bedingungsseiten, Cachewechsel, Serverwechsel und alle UI-11a–i-Unterfälle wurden nicht vollständig abgenommen. |

## Mod-JARs

- Jade 15.10.6: `276f1f65a20d04f536989c4f311949d770b52c440c23c2b904a08860f3c93810`
- Identify 0.4.0: `9c29b1489894a7269c5ba9916c3924d552407b869a580005d7c47396dbf5c035`
- JEI 19.64.0.455: `f7a9de3d7ee07eed8d293a1a1a0f6afaac556eb43c103523457bd07ebb551e7b`
- mezz_config 0.7.1: `c42510938584eda0aae134003e367d745fd32ceac1836221cecac4cbc7bfa3a1`
- MCPFabric 0.8.0+1.21.1 (Steuerungsbrücke): `84c4ba2603c612ced619fff90e691de8d8081ac4f12579526b2c99c926558cec`

Die vorhandenen MineColonies-, JadeColonies-, Towntalk- und Watering Cans-JARs wurden inventarisiert, aber nicht gestartet: Sie gehören nicht zum gegenwärtigen Hauptmod-Gate und würden einen eigenen Server-/Client-Modpacklauf erfordern. Pam's HarvestCraft 2 und Farmer's Delight waren im angegebenen Ordner nicht vorhanden.

## Belege und Grenzen

Rohaktionen und Serverantworten liegen unter [evidence](evidence/), Screenshots dort als PNG. Vollständige Prozesslogs der drei getrennten Clientprofile und des Core-Servers liegen unter [logs](logs/); die verwendeten laufgebundenen Helfer unter [harness](harness/). Die beiden `Connection reset`-Einträge auf dem Server entstanden beim absichtlichen Beenden der Jade- und Identify-Clients vor dem jeweils nächsten Profil und sind kein Modabsturz. Keine Quelländerung und keine automatische Reparatur.

Nicht ausgeführt: Multiplayer mit zwei Spielern, MineColonies/add-on-spezifisches Gameplay, vollständige klassische GUI-Abnahme in DE/EN und mehreren Skalierungen, vollständige JEI-Unterfallmatrix, statistische BAL-01/BAL-02-Simulation sowie öffentliche Beta-Abnahme.
