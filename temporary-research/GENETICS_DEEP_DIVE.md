# Genetik-Deep-Dive: geprüfter Istzustand und Portierungsentscheidungen
**Stand:** 2026-11-15. **Status:** Code-Leserecherche, keine Kompilierung/Tests. **Referenz:** `master` des Forks.

## Primäre Quellstellen
Die folgenden Dateien liegen unter `src/main/java/com/infinityraider/agricraft/`:
- `impl/v1/stats/AgriStatRegistry.java`
- `api/v1/stat/IAgriStatRegistry.java`
- `api/v1/stat/IAgriStatsMap.java`
- `impl/v1/genetics/GeneStat.java`
- `impl/v1/genetics/AgriGenePair.java`
- `impl/v1/genetics/GeneSpecies.java`
- `impl/v1/genetics/AgriGenome.java`
- `impl/v1/genetics/AgriCrossBreedEngine.java`
- `impl/v1/genetics/AgriMutationHandler.java`
- `impl/v1/genetics/AgriMutationRegistry.java`
- `impl/v1/genetics/Mutation.java`

## Gesichert: sechs Standard-Statistiken, nicht nur drei
`AgriStatRegistry` registriert `gain`, `growth`, `strength`, `resistance`, `fertility`, `mutativity`. Bedeutung laut `IAgriStatRegistry`:
- **gain:** Ernteertrag
- **growth:** Wachstumsgeschwindigkeit
- **strength:** Wahrscheinlichkeit, ungünstige Wachstumsbedingungen zu überwinden
- **fertility:** Fortpflanzungs-/Verbreitungschance
- **resistance:** Widerstand gegen Unkraut
- **mutativity:** Einfluss auf positive Stat-Mutationen

**Achtung:** Der im Gespräch zunächst benutzte Name „Widerstandsfähigkeit“ für den dritten sichtbaren Wert ist mehrdeutig: Im originalen 10/10/10-Kern sollte **Strength** der dritte Hauptwert sein; **Resistance** steht im Original explizit für Weeds. Bei deutscher UI besser „Robustheit“ für Strength und „Unkrautresistenz“ für Resistance verwenden.

**Vorschlag, noch keine finale Implementierungsentscheidung:** sichtbare Hauptwerte `growth/gain/strength`; `fertility` und `mutativity` intern bewahren, weil sie die Kreuzungs- und Mutationswahrscheinlichkeit beeinflussen; `resistance` nur als optionale Erweiterung für ein späteres Weeds-Addon. Vor Entfernen eines Werts korrekte Initialisierung, Balance und alte Daten-Kompatibilität prüfen.

## Gesichert: Allel-Paare
`GeneStat`: numerische Gene haben zwei Allele. Das höhere numerische Allel ist dominant, das andere rezessiv; Werte werden auf das konfigurierte `min..max` begrenzt. Der angezeigte Trait wird konfigurierbar als `MIN`, `MEAN` (Ganzzahldivision) oder `MAX` aus beiden Allelen ermittelt; Default-Fallback ist `MAX`. Man darf nicht fälschlich annehmen, dass „10/10/10 sichtbar“ notwendigerweise alle sechs Allele jeweils 10 sind.

`GeneSpecies` verwendet das Pflanzenart-Gen neben numerischen Genen. Mutationen können somit Art oder Statistik betreffen.

`AgriGenome` speichert die Gene-Paare bislang in NBT als Liste mit Gene-ID, dominantem und rezessivem Allel. Dieses Schema fachlich dokumentieren und in 1.21.1 auf Data Components/geeignete Codec-basierte Persistierung abstimmen; Save-Migration ist gesondert zu planen.

## Gesichert: Zucht/Mutationen
`AgriCrossBreedEngine` prüft geeignete ausgewachsene Nachbarn, optional Fruchtbarkeit, sortiert Kandidaten nach Fertility, würfelt nach Fertility. Ein Kandidat: Klon (falls Pflanze Klonen erlaubt und Spread-Chance passt); mindestens zwei: Kombination der beiden ersten erfolgreichen Eltern; pro Gen wird von jedem Elternteil zufällig eines der beiden Allele ausgewählt und ein Mutator aufgerufen. Das bedeutet, dass Klonen und Kreuzung verschiedene Pfade sind.

`AgriMutationHandler.AgriStatMutator` verändert je nach Mutativity-Zufallswürfen die numerischen Allele um +1/-1 oder nicht; Grenzen kommen aus `GeneStat.getAllele()`. Kommentare zu Wahrscheinlichkeiten nicht ungetestet als garantierte tatsächliche Verteilung übernehmen; durch feste Seeds/Simulation oder Tests beweisen.

`AgriMutationHandler.AgriPlantMutator` prüft registrierte Artenmutationen, Conditions `FORBID/PASS/FORCE` und Erfolgschance, ansonsten Elterngene. Zufallsgesteuerter Comparator in der Auswahl sollte auf konsistentes Sortierverhalten geprüft werden.

## Prüfhinweise / mögliche Fehler, noch NICHT validiert
- In `AgriMutationRegistry.add(String id,double chance,String childId,List<String> parentIds)` wird die Kindpflanze augenscheinlich mit `get(id)` statt `get(childId)` nachgeschlagen. Vermutlich Fehler oder unerwartete API-Konvention; mit Tests prüfen, bevor Logik übernommen wird.
- `Mutation.equals` vergleicht Kind/Eltern, `hashCode` nutzt nur `id`. Falls gleiche Mutation mit unterschiedlicher ID als gleich gilt, könnte der Hash-Vertrag verletzt sein. Prüfen.
- `AgriMutationRegistry` berechnet Mutations-Komplexität als Summe der Elternkomplexitäten +1 und hält Zyklen im rekursiven Update teilweise ab. Bei neuer Mutation-Registry besser deterministische Tests und Kreislaufprüfung.
- Der Unterschied zwischen „sichtbarem Trait = MAX“ und echter Mendel-Vererbung führt zu interessanten Genetik-Effekten; nicht durch triviale Mittelwertlogik ersetzen.
- Initialisierungsreihenfolge von `AgriStatRegistry`/`AgriGeneRegistry` beim Neuentwurf entkoppeln; alte Registrierungen greifen ineinander.

## Konkrete portierbare Testfälle (noch zu implementieren)
1. `GeneStat` aus `(3,8)`: dominant 8, rezessiv 3; MIN=3, MEAN=5, MAX=8.
2. Grenzwerte für +1 bei Maximalallel bzw. -1 bei Minimalallel werden geklemmt.
3. Zucht mit einem Elternteil respektiert Clone-Config; zwei Elternteile vererben Allele beider Eltern.
4. Ausgefallene/FORBid-Artenmutation lässt Elternart bestehen; FORCE übersteuert Chance.
5. `fertility` wirkt auf Elternauswahl, `mutativity` auf Werte-Mutation.
6. Sichtbare 10/10/10-Werte dürfen nicht stillschweigend 10/10/10 in allen rezessiven Allelen bedeuten.
7. Registrierung einer Artenmutation findet die korrekte Kindart anhand von `childId`.
8. Genom serialisieren, laden und erneut serialisieren ergibt äquivalente Genpaare.

## Nächster sinnvoller Schritt
Analyse des kompletten `TileEntityCrop`-Wachstums-/Erntesystems und der Pflanzen-JSON-Schemas, dazu eine Abhängigkeits-Matrix der InfinityLib-/AgriCore-Aufrufe. Danach durch reine Java-Tests den übernehmbaren Genetik-Kern isolieren.
