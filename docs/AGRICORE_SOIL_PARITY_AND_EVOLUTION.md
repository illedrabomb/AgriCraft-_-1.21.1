# AgriCraft 1.18 Bodenparität: Kategorien, Strength-Toleranz und erste Dynamik

**Datum:** 2026-11-16  
**Branch:** `feature/neoforge-1211-foundation`  
**Status:** Quellcode gespeichert, keine Kompilierung, keine Tests, kein Spielstart.

## Nachgeprüfte Originalquellen

Offizielle AgriCraft-Dokumentation:
- https://agricraft.github.io/docs/customization/1.18/soil
- https://agricraft.github.io/docs/customization/1.18/plant
- https://agricraft.github.io/docs/customization/tolerance-calculator

Die Original-Bodenskalen sind **nicht** die zuvor im NeoForge-Prototyp angenommene verkürzte Auswahl:
- Humidity (0–5): `arid`, `dry`, `damp`, `wet`, `watery`, `flooded`.
- Acidity (0–6): `highly_acidic`, `acidic`, `slightly_acidic`, `neutral`, `slightly_alkaline`, `alkaline`, `highly_alkaline`.
- Nutrients (0–5): `none`, `very_low`, `low`, `medium`, `high`, `very_high`.

`SoilChemistryRule` verwendet jetzt diese Werte samt Originalreihenfolge. Ältere NeoForge-Namen `normal` und `saturated` werden beim Lesen als `damp` bzw. `flooded` akzeptiert, um bisherige Datapacks/NBT-Overrides nicht pauschal ungültig zu machen. **Bei `saturated` ist die Alias-Zuordnung eine bewusste Portierungsmigration, keine behauptete Originaldefinition.** Gespeicherte Daten werden bei einer späteren Speicherung mit dem kanonischen Namen geschrieben.

Die Dokumentation beschreibt als Bedingung:
`conditionValue ± tolerance_factor × plant_strength`.
Für `equal_or_higher` ist die untere Grenze relevant, für `equal_or_lower` die obere. Die Vergleichsrechnung arbeitet mit den angegebenen Ordnungswerten (double-Grenzen) und der **vererbten, tatsächlich exprimierten Strength** (1–10). `tolerance_factor` ist optional und liegt zwischen 0 und 1; ohne Feld gilt 0 (vorherige exakte Vergleichssemantik).

Neues Datapack-Feldbeispiel innerhalb `soil_chemistry`:
```json
{
  "humidity": {
    "category": "damp",
    "comparison": "equal",
    "tolerance_factor": 0.2
  }
}
```
Bei Mutationen heißt das Objekt `cropgenetics_soil_chemistry`. Ein unbekannter oder nicht endlicher Faktor wird abgelehnt. Der Offline-Importer übernimmt nur **ausdrücklich konfigurierte** Regeln samt Faktor; die Legacy-`requirement.soil_humidity`-/`tolerance_factor`-Struktur wird **weiterhin nicht automatisch konvertiert**, damit übrige Legacy-Regeln nicht implizit verlorengehen.

## Erste automatische Bodenveränderungen – bewusst neue Port-Mechaniken

`SoilDynamics` wird bei Random-Ticks **bepflanzter** Crop Sticks verwendet und setzt nur an, wenn der darunterliegende Boden bereits ein explizites statisches Datapackprofil oder einen gespeicherten Override hat:

1. Bei Regen direkt über der Pflanze wird mit Wahrscheinlichkeit 1/16 pro Crop-Random-Tick die **Feuchtigkeit eine Kategorie erhöht**, maximal `flooded`. Die Abfrage geschieht **vor** der Wachstumsprüfung, damit eine Regenänderung einen zuvor ungeeigneten Boden verbessern kann.
2. Bei erfolgreicher Weiterentwicklung um eine Wachstumsstufe wird mit Wahrscheinlichkeit 1/8 die **Nährstoffstufe eine Kategorie vermindert**, minimal `none`. Fehlgeschlagenes Wachstum oder ruhende reife Pflanzen verursachen keine solche Abbuchung.
3. Beide Änderungen schreiben über `SoilOverrideSavedData` dauerhaft einen positionsbezogenen Override. Die Basisprofile werden nicht verändert, und unprofilierte Vanilla-Böden erhalten weiterhin **keine** erfundenen Chemiewerte.
4. Spieler können weiterhin Wassereimer und Knochenmehl für je +1 Feuchtigkeit/Nährstoff verwenden. Die neuen, vollständigen Stufen gelten auch für diese Items.

**Wichtig:** 1/16 und 1/8 sind vorläufige neue Gameplay-Raten, **keine nachgewiesenen AgriCraft-/AgriCore-Raten**. Die Änderungen hängen von Minecrafts Random-Tick-Frequenz ab. Es gibt weder Tick-übergreifende zeitbasierte Hydrologie noch einen vollständigen Wassertransport.

## Fehlende Parität und technische Risiken

- Original-Bodenprofile/Blockzustände, Wachstumsmultiplikatoren, Legacy-Sonderprädikate und exakte AgriCore-Codeimplementierung der Formel bleiben zu prüfen. Die dokumentierte Toleranzformel wurde nachgebildet; daraus folgt **noch keine allgemeine Originaltreue**.
- Stärkeausdruck/MAX-Vererbung, Client/Server-Platzierung, DataPack-Reload sowie Compiler-/NeoForge-API-Signaturen benötigen die später freigegebenen Tests.
- Die Regeneration von Nährstoffen, Düngerausbreitung, Verdunstung, Bewässerungsblöcke, Feuchtigkeit neben Gewässern, Blockzustandswechsel und MineColonies-Integration fehlen.
- Dauerhafte Nährstoffdepletion ist kein harter Wachstumsstopp für Pflanzen **ohne** `soil_chemistry`-Bedingung; sie wirkt erst bei Pflanzen mit entsprechenden konfigurierten Anforderungen.
- Random-Tick-basierte Regenänderung setzt einen platzierten und registrierten Genom-Pflanzenstock voraus. Kein eigener Welt-Hintergrundticker.
- Ein Regen- oder Wachstums-Event kann eine Speicherung verweigern, wenn die Override-Kapazität ausgeschöpft ist. Das wird nicht als erfolgreiches Bodenevent gemeldet.

## Betroffene Quelldateien

- `data/SoilChemistryRule.java`: Originalkategorien + Strength-Toleranz
- `content/SoilChemistryAccess.java`: neue Strength-Abfrage
- `content/CropConditions.java`, `content/CrossBreeding.java`: vererbte Strength bei Wachstum und Mutation
- `data/AgriPlantsParser.java`: `tolerance_factor`
- `content/SoilTreatment.java`: Beschreibung der Stufen
- `content/SoilDynamics.java` (neu): Regen und Nährstoffverbrauch
- `content/CropSticksBlock.java`: Server-Random-Tick-Anschluss
- `tools/migrate_agriplants.py`: kanonische Werte und optionale Faktorvalidierung

## Nächster Schritt

Bei weiterer Entwicklung konkrete originale AgriSoils-Basisprofile und BlockState-Varianten auflisten, danach die automatische Depletion und saisonalen Effekte abstimmbar machen sowie bei gesonderter Freigabe Build, NBT-Roundtrip, Multiplayer und das gesamte Ingame-Wachstum verifizieren.
