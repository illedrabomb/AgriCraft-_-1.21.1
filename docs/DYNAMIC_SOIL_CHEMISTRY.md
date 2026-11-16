# Dynamische Bodenchemie pro Blockposition

Stand: 2026-11-16 | Branch: `feature/neoforge-1211-foundation`  
**Status: im Quellcode implementiert, noch nicht kompiliert, getestet oder im Spiel bestätigt.**

## Architektur und Vorrang

Die bisherige `SoilProfileRegistry` definiert **statische, optionale Datapack-Profile pro Blocktyp**. Der neue `SoilOverrideSavedData` speichert einen **expliziten Bodenwert pro Position und Dimension**. Bei der Abfrage des tragenden Blocks unter einer Pflanze gilt folgende Reihenfolge:

1. Persistierter Override an der **genauen Bodenblockposition** und mit übereinstimmender **Block-ID**.
2. Statisches Datapack-Profil des vorhandenen Blocktyps.
3. Kein Sample: ausdrücklich chemieabhängige Wachstums-/Mutationsbedingungen bleiben serverseitig **unerfüllt**.

Ein gespeicherter Override wird verworfen, sobald beim nächsten Zugriff ein **anderer Blocktyp** an dieser Position erkannt wird; er wird nicht für einen neu gesetzten Blocktyp wiederverwendet. Änderungen des BlockStates **innerhalb desselben Blocktyps** ändern den Override zunächst nicht automatisch. Die ursprünglichen AgriCraft-`tolerance_factor`-/Strength-Regeln sind weiterhin nicht verifiziert/portiert.

## Speicherung

- NeoForge/Minecraft 1.21.1 `SavedData` mit `SavedData.Factory`, NBT-Serialisierung und `setDirty()`.
- Je Dimension eigene Datei `data/cropgenetics_soil_overrides.dat` im jeweiligen Dimension-Speicherbereich.
- Positionsschlüssel: `BlockPos.asLong()`, Werte: Block-ID sowie kategorische Feuchtigkeit, Säure und Nährstoffe.
- Maximale Anzahl: **65.536 gespeicherte Overrides pro Dimension**; darüber hinaus werden neue Einträge abgelehnt, bestehende können aktualisiert werden.
- Kein erzwungenes Laden fremder Chunks; die Profilabfrage prüft `hasChunkAt`.
- Beim Laden werden beschädigte/ungültige Einträge übersprungen; doppelte NBT-Positionsschlüssel überschreiben den ersten gültigen Eintrag nicht.
- Ein Datapack-Reload verändert **statische Profile**, aber keine gespeicherten Overrides; ein `clear` entfernt einen Override, danach gilt wieder das statische Profil.

## Administratorbefehle

Nur mit **Berechtigungsstufe 2**, über die bestehende `/cropgenetics`-Befehlswurzel. Die Koordinate bezeichnet **den Bodenblock selbst**, nicht die Crop Sticks darüber:

```text
/cropgenetics soil show <x y z>
/cropgenetics soil set <x y z> <humidity> <acidity> <nutrients>
/cropgenetics soil clear <x y z>
```

Beispiel:

```text
/cropgenetics soil set 100 64 100 wet neutral high
/cropgenetics soil show 100 64 100
/cropgenetics soil clear 100 64 100
```

Unterstützte Kategorien entsprechen `docs/SOIL_CHEMISTRY_ARCHITECTURE.md`. Der Befehl weist nicht geladene Positionen, Koordinaten außerhalb der Weltgrenzen und Luftblöcke ab. Ein `show` unterscheidet zwischen gespeichertem Override, statischem Profil und fehlendem Sample.

## Betroffene Dateien

- `data/SoilOverrideSavedData.java` — neu, positions- und dimensionsbasierte Persistenz
- `command/SoilCommand.java` — neu, Administratorbefehle
- `CropGeneticsMod.java` — Override-vor-Profil-Anbindung
- `docs/SOIL_CHEMISTRY_ARCHITECTURE.md` und `docs/PROJECT_STATUS.md` — dauerhafte Dokumentation

Java-Quellen liegen unter `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`.

## Noch nicht umgesetzt

1. **Automatische Bewässerung, Dünger, Verbrauch/Regeneration von Nährstoffen, Wetter oder Nachbarblockeffekte**. Dynamisch bedeutet aktuell *positionsbezogen veränderbar und persistent*, nicht zeitabhängig simuliert.
2. Eigenes Werkzeug/UI zur Bodenbearbeitung, Berechtigungen für Nicht-Administratoren und spätere Integration in MineColonies.
3. Automatisches Löschen bei Blockabbau **im selben Tick**: abweichende Blocktypen werden erst bei der nächsten Abfrage bemerkt; ein Ersatz durch denselben Blocktyp kann weiterhin den alten Wert übernehmen, bis eine gesonderte Blockänderungs-Ereignisbehandlung implementiert wird.
4. Historische AgriCore-Bodenphysik, Strength-Toleranzformeln und Originalwerte: erst quellenbezogen analysieren, nicht durch erfundene Vanilla-Werte ersetzen.
5. Java-/NeoForge-API-Kompilierung, SavedData-Roundtrip, Permissions, Dimensionen/Chunkgrenzen, Serverneustart und Multiplayer **noch nicht getestet**.

## Nächster Schritt

Vor der Releasevalidierung die Blockänderungs-Invalidierung und gegebenenfalls eine spielbare, datengesteuerte Bewässerungs-/Düngemechanik ergänzen; dann bei ausdrücklicher Nutzerfreigabe Kompilierung und kontrollierte Tests.
