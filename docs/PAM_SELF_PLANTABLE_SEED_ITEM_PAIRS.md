# Pam's: Ernte-Item zugleich Saatgut – expliziter Adapter (19.11.2026)

**Status:** Implementierte Konfigurationsmöglichkeit auf `integration/neoforge-1211-consolidation`. Weder mit der NeoForge-1.21.1-JAR noch in Minecraft verifiziert.

## Ausgangspunkt

Im öffentlich einsehbaren älteren Pam's HarvestCraft 2: Crops-Quellcode sind einige Items vom Typ `ItemPamFoodSeed` (z. B. `flaxitem`, `cottonitem`, `agaveitem`). Die tatsächliche 1.21.1-Ausgabe kann abweichen. Ein Datenpaket darf die Item-ID nicht automatisch als Crop-Sticks-Saatgut freigeben, nur weil das Item essbar ist.

Die bestehende allgemeine Namenssuche `<crop>seeditem` → `<crop>item` bleibt unverändert. **Nur ein explizites `item_pairs`-Mapping** darf nun `seed == produce` enthalten. Dadurch lässt sich nach Prüfung der tatsächlichen JAR z. B. Flachs mit dem Ernteprodukt selbst einpflanzen.

## Beispiel – nicht als Standard aktiviert

Datei im **separaten Server-Datapack**:
`data/cropgenetics/cropgenetics/pam_auto_discovery.json`

```json
{
  "enabled": true,
  "item_pairs": {
    "flax": {
      "seed": "pamhc2crops:flaxitem",
      "produce": "pamhc2crops:flaxitem"
    }
  }
}
```

**Nur nach Bestätigung**, dass `pamhc2crops:flaxitem` in der konkret verwendeten NeoForge-1.21.1-JAR tatsächlich existiert und als Pflanzgut fungieren soll. Keine Empfehlung, dieses Beispiel vor der Prüfung zu aktivieren.

## Technische Regeln

- `PamAutoDiscoverySettings.ItemPair` und `RegisteredSeedCropDiscovery.Policy` erlauben identisches Seed- und Produce-Item **nur im vom Administrator geprüften expliziten Paar**. Es gibt weiterhin keine automatische Selbstpflanzer-Suche.
- Die gleiche Item-ID darf nicht einer **anderen** automatisch konfigurierten Pflanzenart als Seed oder Ernteprodukt zugeordnet werden.
- Vorhandene explizite Datapack-Pflanzen oder konkurrierende aktive Saatgut-/Ernte-Claims haben Vorrang. Bei Konflikt wird der Auto-Kandidat übersprungen und diagnostiziert, nicht überschrieben.
- Bei gültiger Item-Registry entstehen genetische Crop-Sticks mit den üblichen generischen Wachstumsregeln; das ersetzt **nicht automatisch** Pam's eigene Wachstumsschritte, native Erntelogik oder besondere Bodenvorgaben.
- Ein normaler Seed mit fremden zusätzlichen Data Components bleibt durch den Schutz in `CropGeneticsSeedRecognition` / `CropSeedSourceSafety` von einer nicht verlustfreien Konvertierung ausgeschlossen; die Seed Chest darf ihn unverändert lagern.
- Ein vorhandenes separates `flaxseeditem` wird bei einem konfigurierten `flax`-Paar **nicht zusätzlich automatisch inferiert**. Für eine Art mit mehreren echten Pflanzgut-IDs sollte später eine explizite `PlantDefinition.seedItems`-Liste benutzt werden; nicht stillschweigend zwei konkurrierende Autokandidaten erstellen.

## Tests nach dem vereinbarten Entwicklungsabschluss

1. Zuerst read-only PowerShell-JAR-Prüfung: `scripts/inspect-optional-crop-jars.ps1`. Vorhandene Modelle sind nur Hinweise; Registry via Laufzeit kontrollieren.
2. Server mit Pam's und freigegebenem `flaxitem`-Mapping neu laden. `/cropgenetics data pam`: ein `AUTO_DISCOVERED`-Eintrag für `pamhc2crops:flax_plant`, keine doppelte Pflanzenart, keine unerwarteten Konflikte.
3. Ein Flachs-Ernteitem als normaler, unmodifizierter Stack auf **einen** Crop Stick setzen: genau ein Item verbraucht; baseline 1/1/1; später genetisches Seed mit `ORIGINAL_SEED=pamhc2crops:flaxitem` zurückerhalten. Bei **zwei** Crop Sticks direkte Samenplatzierung ablehnen.
4. Geändertes/umbenanntes `flaxitem` muss ohne Verlust seiner Zusatzdaten vor dem Pflanzweg abgewiesen werden; reine Seed-Chest-Lagerung darf diese Daten nicht löschen.
5. Mit `flaxseeditem` als zusätzlichem Kandidaten sicherstellen, dass nicht unbemerkt eine zweite `flax_plant` erzeugt wird.
6. Zweites konfiguriertes Paar, das `flaxitem` nutzt, muss beim Datapack-Parsing scheitern bzw. `CONFLICT` melden – niemals einen beliebigen Gewinner wählen.
7. Pam's nicht installiert: kein Hard-Dependency-Crash. Save/Reload, Rückkonvertierung und JEI-Informationsseite nur bei tatsächlich registrierten Items prüfen.
8. Mehrfache Ernte mit Gain 10: je verschiedenem Ernte-Item maximal **ein normaler Stack pro Erntevorgang**, unabhängig von der ursprünglichen Pam-Erntelogik.

## Stand im Repository

- `45052bac`: Pam-Konfigurationsvalidierung für ausdrückliche Selbstpflanzer.
- `5f3ea347`: Entfernen veralteter übergreifender Set-Prüfung.
- `2d5f37c1`: Allgemeine sichere Paar-Discovery akzeptiert explizite gleiche IDs.
- `8a4647b6`: Pam-Config-JUnit für erlaubtes eigenes Paar und verbotene Cross-Crop-Belegung.
- `8ea0a678`: Policy-JUnit für dasselbe und Abgrenzung zur automatischen Suffix-Inferenz.
- **Alle Tests noch nicht ausgeführt.**
