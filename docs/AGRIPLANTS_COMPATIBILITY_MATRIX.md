# AgriPlants 1.18.2 → NeoForge 1.21.1: Kompatibilitätsmatrix

**Stand:** 2026-11-16. **Quelle:** `AgriCraft/AgriPlants`, gepinnter Commit `9444e75828f66bd40adabb4aeca2544765408f8a`.  
**Ziel:** `neoforge-port/`, Branch `feature/neoforge-1211-foundation`.  
**Prüfart:** Statischer Vergleich ausgewählter Original-JSONs mit Port-README und Importer; **kein vollständiges Katalogaudit, keine Tests, keine Imports ausgeführt.**

## Untersuchte Arten

| Legacy-Quelle | Im Port | Wichtige Originalmerkmale | Migration / Differenz |
|---|---|---|---|
| `minecraft/plants/wheat_plant.json` | Quellcode-Pflanze laut README vorhanden | `hash`-Geometrie, Vanilla-Frames, `overridePlanting=true`, strukturgebundene Dorf-Gewächshaus-Erzeugung, Bodenchemie, saisonale Regeln | Pflanze vorhanden; Legacy-Sonderregeln nicht als vollständig portiert bestätigt |
| `minecraft/plants/beet_root_plant.json` | Datensatz + Visuals vorhanden | `hash`-Geometrie, `overridePlanting=true`, Dorf-Gewächshäuser, komplexe Umweltbedingungen | Vereinfachte Boden-/Lichtbedingungen im Manifest; Override und Strukturerzeugung nun explizit als ausgelassen freigegeben |
| `minecraft/plants/nether_wart_plant.json` | Datensatz + Visuals vorhanden | `hash`-Geometrie, `overridePlanting=true`, komplexe Umweltbedingungen | Vereinfachte Soul-Sand-/Licht-Bedingungen; Override nun explizit als ausgelassen freigegeben |
| `minecraft/plants/melon_plant.json` | Physische Melonenfrucht im Port laut README vorhanden | `gourd`, zwei Texturebenen in letzter Stufe, Original-`agricraft:`-Texturen, `overridePlanting=true`, Strukturerzeugung | Angepasster eigener Frucht-Lebenszyklus statt identischer Legacy-Semantik; Assets/Render und Loot noch ungeprüft |
| `minecraft/plants/cactus_plant.json` | Vereinfachte Kaktus-Pflanze laut README vorhanden | `hash`, `agricraft:`-Texturen, `agricraft:thorns`-Callback, Sommer-/Bodenchemie-Regeln, `overridePlanting=true`, Strukturerzeugung | **Nicht 1:1 portiert**; der Importer lehnt Callback ab und fordert für fehlende Texture-Assets Adapter/Freigabe |
| `minecraft/plants/sugar_cane_plant.json` | Vereinfachtes Zuckerrohr laut README vorhanden | Der konkret angefragte Quellpfad wurde nicht gefunden; möglicherweise anderer Dateiname | **Originaldefinition noch nicht verifiziert**, keine Behauptung über genaue Regeln |

Alle Statusangaben zu bestehenden Port-Arten stammen aus `neoforge-port/README.md` und ersetzen keine vollständige Einzeldatei-Validierung.

## Statisch bestätigte Lücken / nächste Adapter

1. **Priorität hoch – Legacy-Rules nicht stillschweigend verwerfen:** `overridePlanting` und nichtleere `structure_generation` benötigen ab diesem Arbeitsschritt eigene Manifest-Freigaben. Nichtstandardmäßige `grass_drop_chance`, `seed_drop_chance` oder `seed_drop_bonus` führen bis zur Implementierung eines Adapters zum Abbruch.
2. **Priorität hoch – Sonderpflanzen:** `agricraft:thorns` für Kaktus sowie Melonen-/Kürbis-Gourd-Regeln/Mehrlagen-Visuals benötigen Abgleich mit den bereits angepassten NeoForge-Implementierungen.
3. **Priorität mittel – Ökologie:** Originalbedingungen für Feuchtigkeit, Säure, Nährstoffe, Jahreszeiten, Biome, Dimensionen und Fluide sind nicht in gleicher Semantik implementiert. Manifest-Freigaben zur Vereinfachung sind kein Ersatz für einen späteren Adapter.
4. **Priorität mittel – Dorf-/Greenhouse-Integration:** Legacy-`structure_generation` für Dorf-Gewächshäuser ist kein äquivalentes Verhalten der zukünftigen MineColonies-Integration; als eigenes optionales Feature planen.
5. **Priorität mittel – Darstellung:** `agricraft:plant/*`-Texturen müssen explizit mit Lizenz-/Assetprüfung verfügbar sein oder durch bestätigte Vanilla-/Port-Ressourcen ersetzt werden.
6. **Katalogvollständigkeit:** Weitere originale Unterordner, modabhängige Arten und Mutationen systematisch erfassen, bevor „AgriPlants komplett portiert“ behauptet wird.

## Import-Sicherheitsregel

`neoforge-port/tools/migrate_agriplants.py` führt einen Batch-Preflight durch und überschreibt keine existierenden Output-Dateien. Das mitgelieferte Manifest für Rote Bete und Netherwarze genehmigt nun ausdrücklich den Verzicht auf deren Legacy-`overridePlanting`-Semantik sowie – für Rote Bete – die Legacy-Strukturerzeugung. Die manuell portierten Ziel-Dateien liegen bereits im Resource-Baum; einen Abgleich/Import später in **ein separates leeres Output-Verzeichnis** leiten. Das Tool wurde nicht ausgeführt und ist nicht end-to-end-validiert.

## Quelle / Belege

- `https://github.com/AgriCraft/AgriPlants/tree/9444e75828f66bd40adabb4aeca2544765408f8a`
- Einzeldateien unter `minecraft/plants/{wheat,beet_root,nether_wart,melon,cactus}_plant.json`
- `temporary-research/AGRIPLANTS_MIGRATION_BATCH_01.md`
- `neoforge-port/README.md`

**Nächster Schritt:** Legacy-Katalog/Mutationen systematisch erfassen und den ersten echten Sonderregel-Adapter priorisieren; weiterhin ohne Builds und Tests bis zur Freigabe.
