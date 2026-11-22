# Klassische Seed-Analyzer-GUI – NeoForge 1.21.1

**Quellportierung vorbereitet, nicht gebaut oder im Spiel getestet (22.11.2026).** Nutzerentscheidung: klassische GUI bereits vor einer möglichen ersten Beta fertigstellen; zusammen mit den aktuellen Analyzer-/Journal-Korrekturen später **gebündelt** testen.

## Portierung

- Original-Atlas `src/main/resources/assets/agricraft/textures/gui/seed_analyzer_gui.png` unverändert unter `neoforge-port/src/main/resources/assets/cropgenetics/textures/gui/seed_analyzer_gui.png` wiederverwendet (256x256). Keine neue erfundene Grafik.
- Eigene `CropMenus`-Typen für alleinstehenden Analyzer und Analyzer mit Chest statt generischer Truhenmenüs. Dadurch bleiben Vanilla-Truhen unverändert.
- `ClassicSeedAnalyzerScreen` verwendet Original-Panel **186x186**, Original-Lupe, Journal-Slot und DNA-Sprite. Seed-Slot bei **(26,38)**, Journal bei **(26,71)**, Spielerinventar bei **(13,104)**. Die sieben intern bestehenden Füllslots sind nicht klickbar sichtbar (außerhalb Bildschirm).
- Bei Chest-Anschluss rechts zusätzlich ein 27-Slot-Feld mit eigenen Slotkoordinaten; kein Überschreiben der Original-Analyzer-Fläche. Der Titel ist auf die Panelbreite gekürzt.
- Die drei aktuellen Werte **Growth/Gain/Strength** und Pflanze werden aus dem **an den Menübetrachter synchronisierten** Seed-ItemStack angezeigt, aber nur bei `CropItems.isAnalyzed`. Der BlockEntity-Display-Paketweg verrät keine versteckten Werte. Keine neue Netzwerk-NBT und keine automatische Analyse vor dem Serverzustand.
- Serverseitige Slots, `canPlaceItem`, Shift-Klick, Mengen und Komponenten bleiben im Menu-Backend; auf dem Client werden Dummy-Container für synchronisierte Slots verwendet. Normales Journal-Rechtsklick öffnet weiter das Buch, Sneak+Journal trifft den separaten Einlegepfad aus `UI-09`.
- **Keine Warteanimation** – Analyse bleibt unmittelbar beim serverseitigen Einlegen.

## Nachbesserung ST-60 – Texte und Artenanzeige (ungetestet)

- Die Genwerte im Originalpanel verwenden jetzt eigene kurze DE/EN-Schlüssel: „Wachstum/Ertrag/Robustheit“ bzw. „Growth/Gain/Strength“. Diese Kurztexte sind auf den schmalen DNA-Bereich ausgelegt; große GUI-Schrift und beide Sprachen bleiben optisch zu prüfen.
- Die Artenbezeichnung verwendet die bereits im Hauptmod benutzte übersetzte Anzeige `CropSpeciesNames.label`, statt die technische Datapack-ID in die 88-Pixel-Zeile zu pressen.
- Die JEI-Analysehilfe in DE/EN beschreibt nun korrekt die aktuell implementierte Stapelregel: gleiches Item mit gleichen Data Components (einschließlich Art, Genen, Analyse und Originalseed) kann bis 64 stapeln. Elternidentitäten werden nicht als Datenkomponente gespeichert. Analysierte und nicht analysierte Seeds bleiben wegen unterschiedlicher Komponenten getrennt.
- `ClassicSeedAnalyzerGuiResourcesTest` prüft zusätzlich die Existenz der DE/EN-GUI-Schlüssel und die neue JEI-Aussage. **Nur Testquelle, nicht ausgeführt.**
- **Statischer Review:** keine Änderungen am Menü-/ItemStack-/NBT-/Sync-Pfad; keine neue Geheimdatenfreigabe. Verbleibendes Risiko: exakte Schriftbreite, Originalgrafik und Klickbereich erst im Spiel belegbar.

## Gebündelter Abnahmelauf (noch nicht freigegeben)

1. Gradle/JUnit/Build für neuen GitHub-HEAD und optische GUI-Prüfung getrennt protokollieren. `ClassicSeedAnalyzerGuiResourcesTest` prüft die vorhandene Original-Atlasgröße, ist **geschrieben, nicht gelaufen**.
2. Core-Client auf dediziertem Server: klassisches Panel mit Lupe/DNA, Seed-/Journal-Positionen, lesbare lokalisierte drei Genwerte nach Analyse, übersetzte Artennamen, vorher keine Gene, korrekte Itemtooltips. Beide Sprachen einzeln prüfen.
3. Seed Chest rechts: 27 Slots, klickbare Koordinaten, 64 gleiche genetische Seeds, 64 normale Seeds, jeweils kompatible/inkompatible Slots, Shift-Klick, Cursorplatzierung; Mengen und Data Components vor/nach sichern. Wiederholte Öffnung ohne Screen-Registrierungsfehler.
4. Journal per Sneak+Rechtsklick, Entnahme, Buchaufruf ohne Sneak, Save/Reload, Datapack-Reload; F10/Journalseite separat.
5. 854x480 und größere Fensterauflösungen: Skalierung, Tooltipüberlagerung, kleine Schrift, überlange Artennamen und Titel. **UX optisch unbestätigt**, ohne Screenshot nicht als abgenommen markieren.
6. Ohne JEI, mit JEI, Jade, Identify jeweils nur in **freigegebenen separaten Profilen**. Multiplayer erst nach gesonderter Freigabe.

**Offene Risiken:** NeoForge-1.21.1-Menütyp-/Screen-API, tatsächliche GUI-Klicks, Client-Slot-Sync und überlappende Modtexte können erst im später freigegebenen Build/Spieltest bestätigt werden. P0/P1 nach belegtem Befund, keine unbewiesene Erfolgsmeldung.
