# Automatische Kreuzung – Implementierungsstand
Stand 2026-11-15 | Branch `feature/neoforge-1211-foundation` | Noch kein Build/Test auf Nutzerwunsch.

## Im GitHub-Fork geänderter Code
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CrossBreeding.java`: neuer Server-Controller für leere Kreuzungsstäbe.
- `.../content/CropSticksBlock.java`: `isRandomlyTicking` tickt jetzt sowohl leere Kreuzungsstäbe als auch bepflanzte Stäbe. Leerer Cross sucht Eltern und erzeugt Nachwuchs, belegter Cross verwendet die bestehende Wachstumskurve.
- `.../content/CropSticksBlockEntity.java`: `acceptCrossOffspring(Genome, PlantDefinition)` übernimmt Nachkommen nur auf leere Cross-Stäbe, setzt Stadium auf 0 und synchronisiert das gespeicherte Genom.
- Datapack-Definitionsdateien: `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/wheat.json`, `potato.json` ergänzt; `carrot.json` und `mutations/carrot.json` waren schon vorhanden.
- Kreativinventar: Genetisches Weizen-, Kartoffel- und Karottensaatgut mit Grundwerten verfügbar.
- Platzhaltermodelle `crop_sticks_cross_young/mid/mature.json` sowie Blockstate-Varianten zeigen auch gekreuzte Nachkommen als wachsende Pflanzen.

## Ablauf
1. Nur serverseitiger Random-Tick eines leeren Kreuzungsblocks.
2. Die **vier horizontalen Nachbarn** werden auf CropSticksBlockEntity, **normale, nicht gekreuzte Stäbe**, vorhandenes Genom, eine registrierte PlantDefinition und Reife geprüft.
3. Jeder geeignete Elternteil wird unabhängig mit Wahrscheinlichkeit `fertility / 10.0` (aus dem dominanten/angezeigten Fertility-Gen) berücksichtigt. Kandidaten werden zufällig gemischt.
4. **Ein Elternteil:** Nur wenn `plant.cloneable()` und `spreadChance` erfolgreich sind, wird dessen gesamtes Genom geklont. Keine Kreuzung/Zahlenmutation in diesem Pfad.
5. **Zwei oder mehr Eltern:** Zwei zufällig erfolgreiche Eltern werden verwendet; die bereits existierende `BreedingEngine.combine` führt Allelvererbung und numerische Mutation aus.
6. Artenmutation: Registrierte `MutationDefinition` mit passenden Elternarten und existierender Kindpflanze werden nach Datapack-Key sortiert geprüft; für jeden Kandidaten `chance` würfeln. Bei Erfolg wird `childPlantId` gewählt. Ohne Erfolg übernimmt der Nachwuchs zufällig eine der Elternarten.
7. Wenn der Ziel-Cross noch frei ist und die Kindart existiert, wird das Genom übernommen. Danach wächst die Pflanze normal und kann über das bestehende System geerntet werden.

## Beispielfall aus dem neuen Datapack
- Reifer Weizen + reife Kartoffel können bei erfolgreicher Elternselektion auf einem benachbarten leeren Doppelstab eine Karotte hervorbringen.
- Die bereits vorhandene Regel `carrot.json` hat `chance=0.75`. **Dies ist nur die Artenmutationschance nach erfolgreicher Elternselektion**, nicht eine globale 75%-Chance pro Tick.
- Die neuen Weizen- und Kartoffel-Pflanzendaten sind **vereinfachte Startdefinitionen, keine vollständige 1:1-Kopie** aller AgriPlants-Boden-, Textur- und Erntebedingungen.
- Genetische Samen für alle drei Arten liegen im Kreativinventar. Normale Vanilla-Saaten können weiterhin nicht automatisch eingesetzt werden.

## Offene Punkte / Risiken
- **Keine Build-, JUnit- oder Ingame-Prüfung durchgeführt**, expliziter Nutzerwunsch. Der Code ist implementiert, aber noch nicht als funktionierend bestätigt.
- Aktuell nur einfache Artenmutationen **ohne Conditions**; beim JSON-Parser werden bedingte Mutationen ausdrücklich abgelehnt. Komplexe AgriPlants-Mutationsbedingungen folgen später.
- Der neue Zuchtcontroller verwendet zufällig zwei erfolgreich ausgewählte Eltern, nicht exakt die Sortier-/Chancenlogik des alten `AgriCrossBreedEngine`. Balancing gegen Original später vergleichen.
- Fertility=1 bedeutet nur 10% Auswahlchance pro Elternteil; 2-Eltern-Kreuzung tritt damit selten auf. Erfolgswahrscheinlichkeiten und Clone-Chance brauchen Balancing-Konfiguration.
- `BreedingEngine.combine` hat eine eigene vereinfachte Zahlenmutation; eine vollständige 1:1-Übernahme aller ursprünglichen Mutatoren ist noch nicht garantiert.
- Noch keine Mutation-Conditions/FORCE/PASS/FORBID, Boden-/Biome-Prüfung, Mod-Spezialadapter, Growth-Tick-Konfiguration, Saatguterkennung für Drittmod-Items oder automatisierte Pflanzen-Speziesrendering.
- Serverseitige Berechtigungen, Savegame-Konsistenz bei Reload, sowie Client-/Dedicated-Server-Stabilität sind noch nicht überprüft.
- `CropSticksBlock.getDrops` und Ernte enthalten unverifizierte NeoForge-API-Details.
- **Originale Lizenz und Credits** müssen beim späteren Packaging/Import unverändert erhalten bleiben.

## Nächste Schritte
1. Sichere Standard-Samenkonvertierung (Vanilla & einfache Mod-Crops) via PlantDefinition.seedItems -> Genome.basic (ohne genetiktragende Seeds zu überschreiben).
2. Ausgebaute, deklarative Soil-/Licht-/Spezialbedingungen und Atlas-/Blockstate-Rendering je Pflanze.
3. Bessere Domänenvalidierung und Datapack-Referenzprüfung bei Reload.
4. Erst auf erneute Freigabe Builds/Tests starten; Workflow ist als manuell gedacht.
