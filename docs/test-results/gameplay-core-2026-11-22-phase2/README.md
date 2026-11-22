# Core-Spieltests Etappe 2 – 22.11.2026

**Ergebnis: 4 pass, 2 partial, 2 fail (P2).** Keine P0/P1-Defekte bestätigt. Kein Mod-/Testquellcode geändert. Der vorherige 212/212-JUnit-Lauf wurde nicht wiederholt.

Getesteter Dokumentationscommit: `4b2e78db09f02db3ee40a66e4bb98a8f7f4acac2`, Integrationsbranch. Hauptmod-Build unverändert gegenüber vorherigem Spieltest: `cropgenetics-0.2.14-alpha.jar`, 385545 Bytes, SHA-256 `9e272b6c9ddf97d8a96dbee64ec27b595caea704bef68c79760f0ad0e41da27a`. Java 21.0.12.1+1 / Gradle 9.2.1 / Minecraft 1.21.1 / NeoForge 21.1.265. Core-Server ohne JEI/Add-ons, ein echter Client mit MCPFabric 0.8.0+1.21.1. Kein Mehrspielerfall und keine neue Installation.

Neue Weltkopie: `work/gameplay-phase2-20261122/server/isolated-core-world`. Jede Serie vorher gespeichert/gesichert, aktive Sicherung mit save-off/save-on; Windows-Sperrdatei session.lock ausgeschlossen. Alte Welt und frühere Belege unverändert. Benutzerfreigabe für neue Tests im Chat erteilt. Gradle-Dev-Runs verwenden unveränderte Klassen, kein neuer gepackter Produktionsstart.

## Einzelergebnisse

| Fall | Status | Tatsächlicher Nachweis |
| --- | --- | --- |
| ER-14 | pass | 12 frische Ernten: Weizen/Kürbis/Melone je Faktor 0/1/2/64. Bei 0 keine Produkte, anschließend beim Abbau jeweils Seed+Stick. Bei 64 je ein Stack 64. Override anschließend nachweislich auf Serverconfig 1.0 zurückgesetzt. |
| ER-03 | pass | Gain-10-Weizen bei Faktor 64 genau ein Produktstapel mit 64. Keine zusätzlichen Mehrfachprodukt-Datapackdefinitionen geprüft. |
| ER-13 | pass | Melone Gain 1: 4/4 Scheiben; Gain 10: 14/15. Je zwei getrennt bestätigte reife Fruchtzyklen, keine ganzen Melonenblöcke. |
| ER-12 | partial | Je zwei Zyklen Gain 1/2/5/8/10. Mengen innerhalb der Grenzen, Gain 1 genau 1. Kein statistischer Prozentnachweis; Drop-Merging schränkt einzelne Rohstapelmessungen ein. |
| UI-09 | fail, P2 | Journal-Direkteinlage über beide vorgesehenen Wege scheitert bei bestätigtem Journal-Hotbar-Slot und sneaking=true; Journalslot bleibt leer, Handmenge 1. Normaler Klick öffnet stattdessen das Buch. Seed-Direkteinlage funktioniert. |
| UI-01 | fail, P2 | Verbundener Analyzer nimmt beim 64er-Shift-Klick Seeds im Journal-/Füllbereich an. Stick lässt sich in Füllslot 2 legen. Server-NBT bestätigt; Gesamtmengen bleiben erhalten. |
| ER-08 | pass | Echte Crafting-Slotklicks mit Papier 1/16/benannt16 und Shift-Klick mit zwei sequenziellen Seeds: Papier samt Name/Menge bleibt, pro Craft ein Originalseed. |
| UI-14 | partial | F10, F9-Umbelegung im Steuerungsmenü, Journalanzeige, Esc, Backspace, F8-Umbelegung und Speicherung über Clientneustart bestätigt. Entf-Injektion als scancode.256, kein physischer Entf-Nachweis; voller DE/EN-Vergleich offen. |

Strukturierte Felder je Fall und Belege: [results.json](results.json), [manifest.json](manifest.json). Zeitgrenzen dort umfassen bewusst die ganze Etappe; genaue Aktionen stehen in timestamped RPC-Logs. Einige ursprüngliche Trial-startUtc-Werte wurden erst bei Ergebnisaufnahme gesetzt, daher nicht als Beginn sämtlicher Aufbauaktionen verwenden. Nur Ergebnisse dieser Etappe bewerten; keine Änderung am v8-Original oder den früheren Resultaten.

## P2-Befunde für gemeinsame Korrektur

**UI-01 – bestätigte falsche Slotannahme.** Bei 64 Seeds liegen zunächst neun einzelne Seeds im Analyzer und 55 in der Chest. Nach dem Füllslotversuch: acht Seeds im Analyzer, 55 in der Chest, einer beim Spieler, Stick in Analyzer-Slot 2. Die 64 Seeds und übrigen Itemmengen sind erhalten. Fehlerhafte Journal-/Füllbelegung durch serverseitiges `/data get block` bestätigt, kein bloßer Client-Ghost.

Fundstelle: `SeedAnalyzerCombinedMenu.java:56` und `:80`. Die Overrides rufen `super.mayPlace(stack)` auf. Im tatsächlich verwendeten Minecraft-1.21.1-Quellstand gibt Slot.mayPlace immer true zurück; der Filter von `SeedAnalyzerBlockEntity.canPlaceItem` wird auf diesem Menüpfad nicht angewendet. Vorschlag: die jeweiligen Container-Slotfilter ausdrücklich berücksichtigen und den gesamten Shift-/Cursorpfad erneut prüfen. **Nicht umgesetzt.** Weitere Einlage/Entnahme-Unterfälle nach dem Befund nicht als bestanden markieren.

**UI-09 – reproduzierter Journal-Dispatchfehler über die beiden API-Wege.** Bestätigte Auswahl Slot 1 mit Journal und sneaking=true. useItem öffnet das Journal, explizites useItemOn legt ebenfalls nichts ein. Keine Itemverluste. Native Maus-/Hardwareprüfung nicht zusätzlich durchgeführt. Der statische Pfad passt zum Befund: `SeedAnalyzerBlock.java:89` erwartet Sneak im Block-useItemOn; Minecraft ServerPlayerGameMode überspringt diesen Blockpfad beim Secondary-Use mit gehaltenem Item, sofern kein Sneak-Bypass greift. Der Journal-Itempfad hat keine entsprechende Ausnahme. Vor einer Korrektur den normalen Dispatch gemeinsam prüfen. **Kein Codefix.**

## Mess- und Eingabegrenzen

- Beim zweiten Gain-10-Kürbis meldete die Filtermessung keine neue Item-Entity. Serie sofort angehalten und gespeichert. Ungefiltertes Server-NBT zeigt einen markierten Kürbisstapel mit 6 statt zuvor 3: neuer Ertrag 3 war in den bereits markierten Stapel gemergt. Kein Verlust belegt. Roh-fail bleibt erhalten; der nachfolgende Audit korrigiert die Interpretation zu partial. Bei weiteren Stichproben UUID-/Gesamtbestandsdifferenzen oder abgeholte Erträge verwenden. Einzelne zusammengeführte Rohstapel sind kein Verteilungsbeleg.
- Früher Vorbereitungsaufruf kam vor abgeschlossenem Beitritt: no_client_player, noch keine Aktion. Eine Sicherung scheiterte zuerst an session.lock; danach Ausschluss nur dieser Sperrdatei, neue erfolgreiche ZIPs. Beide Harnessfehler mit Logs erhalten, keine Modfehler.
- Beim Tastentest wurde Entf von der Windows-Eingabe als unbekannter Scancode verarbeitet. Backspace hob die Bindung korrekt auf. Daraus kein belegter Entf-Modfehler abgeleitet.
- Windows-UI nur für echte Tasten genutzt; Slot-/Crafting-Aktionen über vorhandene Container-RPCs. Screenshots stammen aus dem Spiel-Framebuffer. Bilder enthalten nur Testspieler/-daten, keine Zugangsdaten. Auth-Konfiguration und Weltbackups nicht in Git.

## Abschluss

Override 1.0 aus Serverconfig, randomTickSpeed 0 und isolierte Clientbelegung F10 wiederhergestellt. Server regulär gespeichert/gestoppt, beide Clientläufe regulär beendet; Ports 25589/25599 geschlossen. Bekannter Fehlerzustand der verbundenen Analyzer-Fixture bleibt in der gesicherten Belegwelt. Keine zusätzlichen Starts, keine Add-on-/Multiplayer-Abnahme, keine vollständige v8-Abnahme.

Vollständige Laufbelege unter evidence/, Minecraft-Logs unter client-logs/ und server-logs/, Ablaufhelfer unter harness/. Die Helfer sind laufgebundene Reproduktionsbelege und dürfen nicht blind auf bestehende Welten ausgeführt werden. Nächster gemeinsamer Schritt: P2-Funde UI-01/UI-09 besprechen und erst nach vereinbarter Korrektur gezielt erneut prüfen.
