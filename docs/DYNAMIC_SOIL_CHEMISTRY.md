# Dynamische Bodenchemie pro Blockposition

Stand: 2026-11-16 | Branch: `feature/neoforge-1211-foundation`  
**Status: im Quellcode implementiert, noch nicht kompiliert, getestet oder im Spiel bestätigt.**

## Architektur und Vorrang

Die bisherige `SoilProfileRegistry` definiert **statische, optionale Datapack-Profile pro Blocktyp**. Der neue `SoilOverrideSavedData` speichert einen **expliziten Bodenwert pro Position und Dimension**. Bei der Abfrage des tragenden Blocks unter einer Pflanze gilt folgende Reihenfolge:

1. Persistierter Override an der **genauen Bodenblockposition** und mit übereinstimmender **Block-ID**.
2. Statisches Datapack-Profil des vorhandenen Blocktyps.
3. Kein Sample: ausdrücklich chemieabhängige Wachstums-/Mutationsbedingungen bleiben serverseitig **unerfüllt**.

Ein gespeicherter Override wird verworfen, sobald beim nächsten Zugriff ein **anderer Blocktyp** an dieser Position erkannt wird; er wird nicht für einen neu gesetzten Blocktyp wiederverwendet. Änderungen des BlockStates **innerhalb desselben Blocktyps** ändern den Override zunächst nicht automatisch. Die dokumentierte AgriCraft-1.18-Strength-Toleranzformel wurde inzwischen umgesetzt; die tatsächliche AgriCore-Runtime bleibt unbestätigt (siehe `docs/AGRICORE_SOIL_PARITY_AND_EVOLUTION.md`).

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

## Ergänzung: Spieler-Bodenpflege (16.11.2026)

Das optionale `SoilTreatment`-Modul und die Item-Interaktion von `CropSticksBlock` erlauben im **ungetesteten Quellcode** eine Änderung vorhandener Bodenchemie: Wassereimer erhöht Feuchtigkeit um eine Stufe, Knochenmehl erhöht Nährstoffe um eine Stufe. Die Werte werden über die hier dokumentierten SavedData-Overrides gespeichert; ohne explizites Ausgangsprofil keine Änderung. Vollständige Semantik, Materialverbrauch, Grenzen und spätere Validierung: `docs/SOIL_TREATMENTS.md`.

## Noch nicht umgesetzt

1. Einfache Regen-Feuchtigkeitserhöhung und Nährstoffverbrauch bei erfolgreicher Wachstumsstufe sind nun über `SoilDynamics` vorhanden. **Verdunstung, Regeneration, echte Bewässerungsnetze, Nachbarblockeffekte und vollständige AgriCore-Physik fehlen.** Alle automatischen Änderungen sind auf explizit profilierte Böden unter bepflanzten Crop Sticks begrenzt.
2. Eigenes Werkzeug/UI zur Bodenbearbeitung, Berechtigungen für Nicht-Administratoren und spätere Integration in MineColonies.
3. **Aktueller Quellstand (Nachabgleich 23.11.):** `SoilOverrideInvalidation` ist bereits als Ereignis-Hook in `CropGeneticsMod` registriert (`BreakEvent`, einzelnes/mehrfaches `EntityPlaceEvent`, `LevelTickEvent.Post`). Bei akzeptierter gewöhnlicher Spieleränderung wird die Position nach dem endgültigen Blockzustand am Tick-Ende bereinigt – **nicht** zwangsläufig im selben Tick-Moment wie das Ereignis. Noch nicht abgedeckt sind insbesondere Vanilla-Befehle, Kolben, Explosionen, Maschinen und gleichartige Blockersetzungen ohne korrespondierendes Place-Ereignis. Die Speicherung und die Event-Semantik sind noch nicht im Spiel geprüft.
4. Die dokumentierten Originalkategorien und `tolerance_factor × Strength` sind übernommen; AgriCore-Bodenphysik, vollständige Originalprofile und World-State-Mechanik bleiben offen.
5. Java-/NeoForge-API-Kompilierung, SavedData-Roundtrip, Permissions, Dimensionen/Chunkgrenzen, Serverneustart und Multiplayer **noch nicht getestet**.

## Nächster Schritt

Die vorhandene Spieler-Break-/Place-Invalidierung nach Freigabe im Spiel prüfen; für nicht abgedeckte Weltänderungen vor einem Ausbau zunächst die gewünschte Core-Beta-Grenze festlegen. Weitere Bewässerungs-/Düngemechanik und vollständige AgriCore-Physik sind kein automatisch freigegebener Beta-Umfang. Kompilierung und Tests nur nach ausdrücklicher Nutzerfreigabe.
