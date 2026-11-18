# Playtest und interne Melonen-/Kürbisfrucht – 18.11.2026

## Testbefund 18.11.2026

Import: `mod-test-results-2026-11-18.json`, Suite `agricraft-neoforge-1211-regression-20261117-v2` (27 Fälle): 25 pass, 1 fail (`GEN-05` bei erhöhter `randomTickSpeed` nicht vergleichbar), 1 blocked (`HUD-03`, Jade/WTHIT nicht installiert).

Spielerwunsch: Melonen und Kürbisse sollen als Frucht **innerhalb** der Crop Sticks erscheinen und durch Rechtsklick geerntet werden; Mutterpflanze bleibt reif für weitere Fruchtzyklen.

## Implementierter Wechsel

- Neue Fruchtzyklen setzen `CropSticksBlockEntity.fruitReady` ohne externe Fruchtposition/Token und belegen keine Nachbarblöcke.
- Der Crop-Stick-BER rendert ein verkleinertes Vanilla-Melonen-/Kürbismodell innerhalb der Sticks. Multipart-Modelle für seitlich geneigte Stängel wurden entfernt.
- Leerer Rechtsklick erntet exakt eine Frucht über `CropHarvest.products` und setzt `fruitReady` zurück, ohne den Stängel auf Stufe 0 zurückzusetzen.
- Blockabbau mit vorhandener interner Frucht berücksichtigt deren Drop.
- Bestehende Weltstände mit externer physischer Frucht nutzen weiterhin ihre alte Besitzerzuordnung bis zu deren Ernte/Abbau. Danach entstehen neue Früchte intern.
- Alte diagnostische Zähler und externe Fruchtblöcke bleiben vorerst erhalten, um bestehende Speicherstände nicht zu verlieren; die Nachbarblock-Positionsdiagnose ist für *neue* interne Früchte nicht mehr maßgebend.

## Nächste Verifizierung (noch nicht bestätigt)

1. `test build` mit Java 21 und lokalem Gradle 9.2.1 auf Windows erfolgreich.
2. Frische Melone/Kürbis auf Crop Sticks erzeugen: kein externer Nachbarblock, internes Modell sichtbar.
3. Leerer Rechtsklick gibt genau einen Ernte-Drop, Stängel bleibt, nächste Frucht entsteht wieder intern.
4. Nach Spielstand-Neuladen bleibt eine bereits gewachsene interne Frucht sichtbar und einmalig erntbar.
5. Altes Save mit physischem Nachbarfruchtblock lädt ohne Duplikate; nach Ernte wächst nächste Frucht intern.
6. Bei Blockabbau mit Frucht keinen doppelten Drop.

## Weitere Befunde aus derselben Suite

- `STK-04`, `GEN-01`: Kreuzung gelang mit genetischen Saatgutvarianten; normales Karotten-Saatgut zeigte kein Kreuzungsergebnis. Das ist als Reproduktionsfall offen, nicht durch die Fruchtänderung gelöst.
- `GEN-02`: verbesserte Allele werden über mehrere Generationen beobachtet; zufallsbedingte Dauer war teilweise lang.
- `ITEM-01`: Darstellung `3/1` bedeutet zwei vererbbare Allele (dominant/rezessiv), **nicht** Wert/Maximum. Allelgrenzen jeweils 1..10. `GenePair` sortiert intern höheres Allel nach links; ein beobachtetes Gegenteil benötigt einen konkreten Screenshot/Stack zur Analyse.
- `GRO-02`: Crop Sticks können auf Wiese platziert werden, abhängig vom Pflanzen-/Bodenprofil. Beim Nachtest als spezifizierte Bodenregel prüfen.
