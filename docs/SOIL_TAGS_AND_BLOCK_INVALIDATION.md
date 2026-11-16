# Soil-Tags und Blockwechsel-Invalidierung

**Stand:** 2026-11-16  
**Branch:** `feature/neoforge-1211-foundation`  
**Status:** Quellcode und Datapacks gespeichert; **nicht kompiliert, getestet oder im Spiel bestätigt**.

## Profilauflösung: Block-ID vor Block-Tag

Zusätzlich zu `block` akzeptieren Bodenprofile jetzt alternativ `block_tag`. Genau eines der beiden Felder muss vorhanden sein, dazu `humidity`, `acidity` und `nutrients`; `growth_modifier` (0..4) ist weiterhin optional. Beispiel:

```json
{
  "block_tag": "c:sands",
  "humidity": "arid",
  "acidity": "neutral",
  "nutrients": "low",
  "growth_modifier": 0.8
}
```

Der Common-Tag `c:sands` aus NeoForge 1.21.1 ersetzt für modded-Sand die alte, in AgriPlants verwendete `forge:sand`-Zielgruppe, ohne eine exakte Mitgliedschaftsgleichheit zu behaupten. Das neue `sand_tag.json` ist in den Standard-Serverressourcen enthalten; die expliziten Profile für `minecraft:sand` und `minecraft:red_sand` bleiben bestehen. Ein exakter Blocktreffer hat Vorrang vor einem passenden Tag.

`SoilProfileRegistry` veröffentlicht **exakte Blockprofile und Tagprofile in einem einzigen unveränderlichen Snapshot**. Tagmitgliedschaft wird zur Abfragezeit über das aktuelle `BlockState.is(TagKey<Block>)` bestimmt. Dadurch können auch nach einem Datapack-Reload neue Tagmitglieder berücksichtigt werden. Bei einer Mehrdeutigkeit (mehr als ein passender Tag, ohne exakten Blocktreffer) wird **kein Profil** zurückgegeben; es wird weder stillschweigend ein Tag ausgewählt noch werden Profile gemischt.

Doppelte Deklarationen derselben Block-ID oder desselben Tag-IDs über unterschiedliche Dateinamen deaktivieren die betreffende ID/das betreffende Tag. Ungültige Block-IDs und malformed Tag-IDs werden beim Reload abgelehnt. Ein syntaktisch gültiger, zur Laufzeit aber noch ungebundener Tag kann ein Profil erhalten und liefert dann schlicht keine Treffer. Modpacks können die Ressource mit demselben Pfad per Datapack überschreiben.

## Gespeicherte chemische Overrides beim Blockwechsel

`SoilOverrideInvalidation` hängt an NeoForge-1.21.1-Ereignissen:
- `BlockEvent.BreakEvent` für einen Spieler-Breakversuch;
- `BlockEvent.EntityPlaceEvent` für Platzierung;
- `BlockEvent.EntityMultiPlaceEvent` für mehrteilige Platzierung;
- `LevelTickEvent.Post` für die Überprüfung des **tatsächlichen Blocks nach dem Ereignis**, wenn der Eingriff nicht mehr abgebrochen werden kann.

Ein Event ist nur ein **Anlass zur späteren Verifikation**, keine sofortige Löschgenehmigung. Die ursprüngliche `isCanceled()`-Flag wird erst am Ende des Ticks berücksichtigt. Abgebrochene Events bleiben ohne Wirkung. Für Break-Ereignisse wird der alte Blocktyp mit dem aktuellen Blocktyp verglichen; für erfolgreiche Place-Ereignisse muss der neue Blocktyp am Ende des Ticks tatsächlich vorhanden sein. Nur dann wird ein gespeicherter Override an dieser Position entfernt. Der Queue-Schutz begrenzt pro Welt die ausstehenden Änderungen auf 8192; der vorhandene Lazy-Block-ID-Abgleich bei der Bodenprobe bleibt zusätzlich aktiv.

### Bekannte Grenzen

- **Nicht vollständig:** Weltbefehle (z. B. `/setblock`), Pistons, Explosionen, Flüssigkeitsänderungen, manche Maschinen-/Modänderungen und Eingriffe nach dem Level-Tick werden nicht zwingend von den gewählten Events erfasst.
- Ein abgebauter und im selben Tick ersetzter Block gleichen Typs kann ohne begleitendes Place-Ereignis weiterhin schwer erkennbar sein.
- Multi-Place-Cases benutzen derzeit einen finalen Blocktyp-Unterschied pro Snapshot; es gibt keine Erfassung der endgültigen Zielstates aus einem erzwungenen Ersatzpfad.
- Vorgänge mit sehr vielen Changes pro Tick können die Queue-Grenze überschreiten; ein späterer Blocktypwechsel wird weiterhin durch den Lazy-Abgleich erkannt, ein Same-Type-Ersatz aber nicht sicher.
- Die End-of-Tick-Methode reduziert das Risiko des Löschens bei abgebrochenen Events, beweist aber **noch keine vollständige Transaktionssicherheit**. Event-Reihenfolge und Server-Tick-Verhalten müssen im freigegebenen Testdurchlauf überprüft werden.

## Betroffene Dateien

- `data/SoilProfileRegistry.java`: atomare Block-/Tag-Snapshot-Maps und eindeutige Auflösung
- `data/SoilProfileReloadListener.java`: `block` oder `block_tag`
- `content/SoilOverrideInvalidation.java`: verzögerte Ereignisauswertung
- `CropGeneticsMod.java`: Event-Registrierung
- `src/main/resources/data/cropgenetics/cropgenetics/soil_profiles/sand_tag.json`: `c:sands`

Quellcodepfade liegen unter `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/`.

## Nächster Schritt

Bei weiterer Entwicklung die Quellen für `c:sands`-Mitgliedschaften und modded Bodenprofile (besonders Farmer's Delight) weiter erfassen. Für **vollständige** Blockwechsel-Invalidierung ist ein dafür geeigneter zentraler Hook oder ein versionskompatibler World-/Chunk-Block-Update-Adapter statt zahlreicher unvollständiger Einzelereignisse zu prüfen. Vor Merge oder Release die Tests für Event-Cancellation, Same-Type-Replacement, Block-/Tag-Konflikte, Reload, Dimensionen und große Farmen ausdrücklich freigeben.
