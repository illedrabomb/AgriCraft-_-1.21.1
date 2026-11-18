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

## Survival-Rezepte (ab 19.11.2026)

- **Crop Sticks:** vier normale Stöcke als 2×2-Quadrat ergeben **sechs Crop Sticks**, wie im Original. Vorher fehlte dieses Rezept im NeoForge-Port vollständig.
- **Seed Analyzer:** wieder die ursprünglich aufwendigere 3×3-Anordnung aus Stöcken, Glasscheibe, Steinplatte, Holzbrettern und Holzstufe (`SGS / _BS / PWP`; S=Stock, G=Glasscheibe, B=Steinplatte, P=Holzbrett, W=Holzstufe). Im NeoForge-Rezept sind beliebige Vanilla-Bretter und Holzstufen über Tags erlaubt.
- **Seed Chest:** **acht Crop Sticks** um eine normale Truhe (statt Holzbrettern), nach Spielerentscheidung.
- **Seed Journal:** Buch + Crop Sticks (bestehendes Rezept).
- Alle Rezepte liegen unter `data/cropgenetics/recipe/` und sind noch nicht durch einen lokalen Minecraft-Datapack-Reload bestätigt. Für Seed Analyzer und Seed Chest wurden außerdem eigene Block-Loot-Tabellen hinzugefügt, damit die **Blöcke selbst** beim regulären Abbau zurückgegeben werden; die vorhandene BlockEntity-Logik wirft separat ihre Inventarinhalte aus.

## Seed Journal / Entdeckungsbuch

- Ein neues nicht stapelbares `cropgenetics:seed_journal` lässt sich aus **Buch + Crop Sticks** herstellen.
- Der Analyzer besitzt **Feld 0 für Seeds, Feld 1 für das Journal**. Felder 2–8 bleiben gesperrt; eine benachbarte Seed Chest wird weiterhin in drei weiteren Reihen angezeigt.
- Liegen gleichzeitig ein analysierter Seed und das Journal im Analyzer, wird die Pflanzen-ID dedupliziert und **im Journal-Item selbst** als Datenkomponente gespeichert. Das Einlegen in beliebiger Reihenfolge ist möglich. Es werden nur tatsächlich analysierte Pflanzenarten, nicht jede bloß gehaltene Pflanzenart, freigeschaltet.
- Das Journal speichert die Liste dauerhaft und übertragbar. Das Verwenden in der Hand öffnet jetzt eine **eigene blätterbare Buchoberfläche** mit drei einführenden Kapiteln (Erste Schritte, Genetik, Kreuzung) und anschließend den entdeckten Pflanzenarten samt Vanilla-Samenbildern bzw. Gen-Seed-Ersatzbild (sieben Arten pro Seite, Seitenzähler, Zurück-/Weiter-Buttons). Ein Klick auf einen Listeneintrag öffnet eine eigene Detailseite der freigeschalteten Art mit Rücksprung zur Liste. Die Daten stammen aus dem synchronisierten Journal-Item. Eine **Mutationsübersicht** wird jetzt aus den **serverseitig aktiven Mutationsdefinitionen** erstellt und als separater Datenkomponent direkt im Journal gespeichert. Sichtbar sind nur Rezepte, bei denen **beide Elternarten und die Kindart** bereits in diesem Buch entdeckt wurden. Damit erhält der Client keine unentdeckten Pflanzen oder erfundenen Mutationseinträge. Die Übersicht zeigt die Eltern-Kind-Kombination, nicht die situationsabhängige Erfolgswahrscheinlichkeit (Biom, Saison, Boden und globale Cross-Chance beeinflussen diese). Die gespeicherten Hinweise werden beim nächsten Einlegen in den Analyzer aus den aktuellen Datapacks neu berechnet. Das aufwendigere **Original-Layout mit allen Pflanzendetails und spezieller Mutationsgrafik** ist noch offen; die Speicherlogik und die aktuelle Buchoberfläche sind unabhängig von einer externen Mod.
- Ponder wird **ausdrücklich nur als sehr spätes, optionales Tutorial-Addon** geführt. Es ist keine Voraussetzung für das Journal. Siehe `docs/NEOFORGE_1.21.1_PLAN.md`.
- JEI bietet eine Infoseite zum Erhalten genetischer Seeds aus gewöhnlichem registriertem Saatgut und eine Infoseite zu Analyzer, Seed Chest und Journal. Die Seiten sind Erklärungen, **keine erfundenen Crafting-Rezepte**.

## Inventarsicherheit

- Die gemeinsame Inventar-Basisklasse erstellt beim Einlagern eine **Kopie** des übergebenen ItemStacks und begrenzt nur diese Kopie auf die erlaubte Slot-Größe. Dadurch verändert der Analyzer beim Einlagern von z. B. normalen 64er-Seedstapeln **nicht versehentlich den ursprünglichen Stack des Aufrufers**. Die eigentliche Entnahme aus dem Spielerinventar bleibt Aufgabe des Minecraft-Menüs bzw. der Rechtsklick-Interaktion.
- Genetische Samen bleiben weiterhin maximal auf einen Gegenstand pro Slot begrenzt; bestehende Komponenten werden beim Kopieren und Speichern erhalten.

## Testfälle für die nächste gemeinsame Testphase (noch NICHT durchgeführt)

1. Genetische Samen unterschiedlicher Werte aus Kreuzungen aufsammeln: sie müssen jeweils einzeln liegen und ihre individuellen Gene auch nach Inventar-/Save-Reload behalten.
2. Normale 64er-Samenstapel in Analyzer-Slot übernehmen: nur ein Seed darf eingelagert werden; übrige Seeds dürfen nicht verlorengehen. Danach unanalysierter Samen: mit Shift keine Werte; nach Einlegen in Analyzer zeigt derselbe Stack die drei gespeicherten Werte und behält sie nach Herausnehmen.
3. Journal in Feld 1 einlegen, erst danach einen Seed analysieren; Reihenfolge umkehren und doppelte Analysen wiederholen: jede Pflanzenart darf nur einmal im Buch erscheinen. Journal entnehmen und per Rechtsklick die Buchoberfläche mit drei Anleitungsseiten, Entdeckungsseiten und Mutationsübersicht prüfen; jede entdeckte Pflanzenart anklicken, Detailansicht öffnen und zur Liste zurückkehren. Mutationseinträge erst nach Analyse aller drei beteiligten Arten erwarten. Analyzer mit Seed in der Hand rechtsklicken: genau ein Seed wird aufgenommen und analysiert; andere Items werden nicht eingelagert. Danach Analyzer mit leerer Hand öffnen: erstes Feld verwendbar, acht Leerfelder der ersten Reihe nicht als Speicher benutzbar; Modell und Blickrichtung bei allen vier Orientierungen kontrollieren.
4. Seed Chest direkt daneben platzieren: Analyzer zeigt die 27 Chest-Slots darunter; Inventar bleibt nach Welt-Neuladen bestehen.
5. Chest ohne Analyzer öffnen und Samen aufnehmen/entnehmen. Beim Abbau müssen **die Truhe selbst sowie ihre Inhalte** zurückkommen, ohne Duplikate. Dasselbe für den Analyzer mit Seed und Journal kontrollieren.
6. Chest an einer nicht angrenzenden Position soll nicht in Analyzer auftauchen.
7. Mehrere Analyzer-/Chest-Blöcke, Speichern/Neuladen und gleichzeitige Spielerzugriffe auf sichere Synchronisation prüfen.
8. Durchgehende Reihe mit mindestens drei reifen Eltern hat die geringste Chance; zwei direkt angrenzende Eltern sind erfolgreicher, drei und vier direkte Eltern erhöhen sie weiter. Vier Nachbarn liefern den höchsten Erfolgs-/Mutationsbonus. Zufälligkeit erfordert wiederholte Versuche.

## Offene Qualitätsgrenzen

Die Änderungen wurden über GitHub vorgenommen und **noch nicht mit Java 21/Gradle 9.2.1 gebaut oder im Spiel verifiziert**, weil zuerst alle Änderungen gesammelt werden sollen. Insbesondere GUI-Slot-Synchronisation, BlockEntity-Speicherung und das interne Melonen-/Kürbis-Rendering gehören ins gemeinsame Build-/Regression-Gate.
