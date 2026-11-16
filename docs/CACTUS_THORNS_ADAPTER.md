# Cactus-Thorns-Adapter – AgriCraft NeoForge 1.21.1

Stand: 2026-11-16 | Branch: `feature/neoforge-1211-foundation`  
**Status: Quellcode implementiert, weder kompiliert noch getestet.**

## Funktion

Der alte AgriPlants-Kaktus hat den Callback `agricraft:thorns`. Die Portierung besitzt jetzt eine **explizite, datengesteuerte Annäherung** über `cropgenetics_contact_thorns: true` im Server-Pflanzendatensatz. Die Standarddefinition `data/cropgenetics/cropgenetics/plants/cactus.json` aktiviert ihn.

`CropSticksBlock.entityInside` prüft serverseitig:
1. Ist der Crop-Sticks-Block tatsächlich bepflanzt?
2. Existiert eine CropSticksBlockEntity mit Genome?
3. Ist die Art im aktuellen serverseitigen Pflanzenkatalog vorhanden und `contactThorns` aktiv?

Nur dann wird über `entity.hurt(level.damageSources().cactus(), 1.0F)` der normale Vanilla-Kaktus-Kontaktschaden ausgelöst. Leere Sticks, ein noch nicht bepflanztes Kreuzungsfeld sowie entfernte/unbekannte Pflanzen-IDs lösen keinen Kontakt-Effekt aus. Minecraft-eigene Schadenimmunitäten und Cooldowns sind nicht durch spezielle Mod-Logik ersetzt.

Die `PlantDefinition` ist um das unveränderliche Feld `contactThorns` erweitert. Ein zusätzlicher Konstruktor mit der bisherigen Parameterliste setzt standardmäßig `false`, um vorhandene Aufrufstellen und ältere Fixtures nicht vorschnell zu brechen. `AgriPlantsParser` liest die optionale Eigenschaft `cropgenetics_contact_thorns`; fehlt sie, gilt `false`.

## Legacy-Import

`tools/migrate_agriplants.py` erkennt **ausschließlich** die genaue Callback-Liste `["agricraft:thorns"]` mit einer expliziten Manifestfreigabe `"adapt_thorns": true`. Dann wird `"cropgenetics_contact_thorns": true` ausgegeben. Andere Callback-Kombinationen bleiben nicht unterstützt und werden abgelehnt. Der direkte Import einer unveränderten Legacy-JSON mit `callbacks` bleibt im `AgriPlantsParser` absichtlich untersagt; zuerst kontrolliert konvertieren.

Die vollständige Kaktusdefinition wird dadurch **noch nicht** unverändert migrierbar: Legacy-Texturen, Struktur-Generierung, Pflanz-Override sowie Bodenchemie/Jahreszeiten benötigen eigene Migrationen oder explizite Vereinfachungen. Die neue Mechanik setzt nur den Dornenkontakt um.

## Betroffene Dateien

- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/PlantDefinition.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/AgriPlantsParser.java`
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/CropSticksBlock.java`
- `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/plants/cactus.json`
- `neoforge-port/tools/migrate_agriplants.py`

## Noch offene Validierung

- Die Override-Signatur wurde anhand der NeoForge-1.21.1-Javadocs statisch abgeglichen; **keine Kompilierung** erfolgt.
- Ingame prüfen, ob `entityInside` bei den Crop-Sticks-Kollisions-/Bewegungszuständen genau wie erwartet auslöst; Creative Mode, Rüstungs-/Schadensmodifikationen, Multiplayer, andere Mod-Entities und Performance.
- Datapack-Reload: Ein-/Ausschalten der Dornenfunktion bei bereits gepflanztem Kaktus.
- Weitere Originalmechaniken: Wachstum, Bodenbedingungen, `thorns`-Triggersemantik gegenüber 1.18.2, Sondertexturen und Spezialdrops.

**Nächster Arbeitsschritt:** Erweiterte, klar modellierte Umweltbedingungen und komplexere Mutationsprädikate als optionale serverseitige Regeln ergänzen. Weiterhin keine Tests ohne Nutzerfreigabe.
