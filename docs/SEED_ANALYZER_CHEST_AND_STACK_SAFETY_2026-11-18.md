# Seed Analyzer, Seed Chest und sichere Samen – 18.11.2026

## Anforderung

- Der Seed Analyzer ist **ein platzierbarer Block**, kein Handwerkzeug. Orientierung an `src/main/java/com/infinityraider/agricraft/content/core/BlockSeedAnalyzer.java` und `TileEntitySeedAnalyzer.java` der alten AgriCraft-Version.
- Eine **Seed Chest** ist als eigenständiger 27-Slot-Block erhältlich. Steht sie direkt auf einer der vier horizontalen Nachbarpositionen des Analyzers, erscheinen ihre 27 Plätze **im selben geöffneten Inventarfenster** unter dem Analyzer-Samenfeld.
- Analysieren darf keinen Genwert verändern. Bereits beim Erzeugen eines genetischen Samens wird dessen individueller `Genome`-Datenkomponent gespeichert, aber vor Analyse nicht preisgegeben.
- Zur zusätzlichen Absicherung sind genetische Samen nicht stapelbar. Normale Vanilla-Samen ohne Genom bleiben normale Stapel und erhalten erst beim Pflanzen oder Analyse ihr anfängliches 1/1/1-Genom.

## Technischer Stand im Integrations-Branch

- `cropgenetics:seed_analyzer` wurde vom Handitem zum platzierten Blockitem mit BlockEntity und einem persistenten Analyseslot umgestellt.
- `cropgenetics:seed_chest` ist als eigener Block mit persistenter 27-Slot-Inventory hinzugekommen. Das Analyzer-Menü verwendet ohne Chest ein Vanilla-1-Reihen-Inventar, mit Chest ein Vanilla-4-Reihen-Inventar (Analyzerslot oben links, übrige Felder der ersten Reihe gesperrt, 27 Chest-Slots darunter).
- Nur Saatgut (bereits genetisch oder eindeutig als Samen registriert) darf in den Analyzer-Slot. Rechtsklick mit einem Samen auf den leeren platzierten Analyzer legt direkt **genau einen** Samen ein; dabei bleiben seine Datenkomponenten erhalten. Per leerer Hand öffnet sich das Inventar. Die Analyse erfolgt serverseitig, sobald der Samen im Analyzer-Slot liegt. Es wird nur die `ANALYZED`-Markierung gesetzt; der `GENOME`-Wert wird nicht geändert.
- Bestehende Handitem-Rezepte liefern nun einen platzierbaren Analyzer mit derselben Registry-ID; ein neues Rezept erzeugt die Seed Chest. Die veraltete Analyse von stehenden Pflanzen durch Rechtsklick mit dem Analyzer-Item wurde entfernt, da es kein Handwerkzeug mehr ist.
- Das **originale AgriCraft-Blockmodell** mit schräger Glasplatte und die beiden originalen Texturen wurden ins CropGenetics-Namespace übernommen. Der Block hat horizontale Ausrichtung. Die ursprüngliche dynamische Kamerafahrt, das Journal und die Spezial-GUI sind dagegen **noch nicht vollständig portiert**; die Bedienung läuft vorerst über eine zuverlässige Vanilla-Containeransicht.

## Testfälle für die nächste gemeinsame Testphase (noch NICHT durchgeführt)

1. Genetische Samen unterschiedlicher Werte aus Kreuzungen aufsammeln: sie müssen jeweils einzeln liegen und ihre individuellen Gene auch nach Inventar-/Save-Reload behalten.
2. Unanalysierter Samen: mit Shift keine Werte; nach Einlegen in Analyzer zeigt derselbe Stack die drei gespeicherten Werte und behält sie nach Herausnehmen.
3. Analyzer mit Seed in der Hand rechtsklicken: genau ein Seed wird aufgenommen und analysiert; andere Items werden nicht eingelagert. Danach Analyzer mit leerer Hand öffnen: erstes Feld verwendbar, acht Leerfelder der ersten Reihe nicht als Speicher benutzbar; Modell und Blickrichtung bei allen vier Orientierungen kontrollieren.
4. Seed Chest direkt daneben platzieren: Analyzer zeigt die 27 Chest-Slots darunter; Inventar bleibt nach Welt-Neuladen bestehen.
5. Chest ohne Analyzer öffnen und Samen aufnehmen/entnehmen. Beim Blockabbau müssen Inhalte als Items fallen, ohne Duplikate.
6. Chest an einer nicht angrenzenden Position soll nicht in Analyzer auftauchen.
7. Mehrere Analyzer-/Chest-Blöcke, Speichern/Neuladen und gleichzeitige Spielerzugriffe auf sichere Synchronisation prüfen.
8. Durchgehende Reihe mit mindestens drei reifen Eltern hat die geringste Chance; zwei direkt angrenzende Eltern sind erfolgreicher, drei und vier direkte Eltern erhöhen sie weiter. Vier Nachbarn liefern den höchsten Erfolgs-/Mutationsbonus. Zufälligkeit erfordert wiederholte Versuche.

## Offene Qualitätsgrenzen

Die Änderungen wurden über GitHub vorgenommen und **noch nicht mit Java 21/Gradle 9.2.1 gebaut oder im Spiel verifiziert**, weil zuerst alle Änderungen gesammelt werden sollen. Insbesondere GUI-Slot-Synchronisation, BlockEntity-Speicherung und das interne Melonen-/Kürbis-Rendering gehören ins gemeinsame Build-/Regression-Gate.
