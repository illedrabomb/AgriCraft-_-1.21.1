# Sonntag Core v5 – statischer Abdeckungs- und Risikoreview

**Stand:** 21.11.2026. Ausgangs-HEAD `69164d18fbc777c08bfc60523be88741e5563c8b` auf `integration/neoforge-1211-consolidation`. **Keine Ausführung und kein Tracker-Import.** Dieser Review bewertet nur Testdefinitionen, Quellstand und Voraussetzungen – er belegt kein Gameplay-Ergebnis.

## Versionsabgleich

- v4: 69 offene Fälle, historisch unverändert aufbewahrt.
- v5: 79 offene Fälle, neue `suiteId`; alle 69 bisherigen Testobjekte sind wortgleich übernommen.
- Neu: `UI-11` (Gesamtabnahme) und `UI-11a`–`UI-11i` (neun einzeln dokumentierte Unterfälle). Die neun Unterfälle sind **nicht** neun bereits bestandene Prüfungen.
- Alte 30-Fälle-/v1-/v2-/v3-/v4-Ergebnisse und andere `suiteId`s dürfen nicht ohne Vergleich zusammengeführt werden. Vor Import bestehende Browserergebnisse exportieren.

## Abdeckung des aktuellen Hauptmod-Codes

| Risikobereich | Verknüpfte v5-Fälle | Statischer Befund / nötiger Beleg |
| --- | --- | --- |
| Basispflanzung, Boden, Licht, Sticklage, Genetik | `GE-01`–`GE-11`, `UI-10` | Normale und abgewiesene Aktionen getrennt; Seedzahlen/Genome und Blockzustand dokumentieren. |
| Artmutation, Fallback, wiederholte Fremdkreuzung | `GE-12`, `GE-12a`–`GE-12f`, `BAL-02` | Vordefiniertes Mutations-Datapack und Weltkopie nötig; ohne Fixture `blocked`. |
| Erntesicherheit, interne Gourd-Früchte, Komponenten, Papier | `ER-01`–`ER-11`, `UI-07` | Itemmengen und fremde Komponenten vor/nach Aktion erfassen; alte physische Früchte nur in Weltkopie. |
| Analyzer/Chest/Journal, private Daten und Multiplayer | `UI-01`–`UI-09`, `MP-01`–`MP-23` | Zwei Spieler für echte Konkurrenzfälle; kein pass aus Einzelspielerbeobachtung. |
| JEI aktive Boden-/Licht-/Sonderregeln | `UI-11`, `UI-11a`, `UI-11b`, `UI-11g` | Server-Datapack statt Defaultliste als Soll; beide Sprachen und artgenaue Seeds. |
| JEI Reload, Cache, zwei Server und Logout | `UI-11c`, `UI-11e`, `UI-11h` | Isolierte A/B/C-Stände und Logs; unmittelbare Frische während Reload bisher **nicht** bewiesen. |
| JEI optional, dedizierter Server und Datenminimierung | `UI-11d`, `UI-11f` | Zwei echte Spieler; Core-Start **ohne JEI**; fremde Journal-/Genomdaten dürfen nicht erscheinen. |
| Versioniertes Netzwerk und Fail-closed-Grenzen | `UI-11i`, `TG-04`, `TG-05` | **Sicheres spezielles Harness erforderlich**, ansonsten `blocked`, keine beliebige Paket-Injektion. |
| Statistische Zuchtbalance | `BAL-01` | Viele unabhängige Zuchtläufe nach **gesonderter** Freigabe; ein Erfolg beweist nicht die 70-Kinder-Erwartung. |

## Noch nicht belegte beziehungsweise bewusst nicht abgedeckte Punkte

1. **Historisches Originalergebnis vom 18.11. fehlt:** Die 27-Fälle-Auswertung (25 pass, `GEN-05` fail, `HUD-03` blocked) ist nur als Zusammenfassung vorhanden. Die 27 Einzelkommentare/Screenshots können ohne Original-JSON nicht vollständig gegen v5 abgeglichen werden. `GE-05`/`UI-05` und frühe Stage-0-/X-Sticks-Befunde bleiben ausdrücklich Wiederholungspunkte; alte Erfolge werden nicht auf den neuen SHA übertragen.
2. **JEI-19.x-API-Risiko:** `IJeiIngredientInfoRecipe` ist als `@NonExtendable` gekennzeichnet. Statische Implementierung in Etappe C bedeutet weder nachgewiesenen Ladeerfolg noch korrekte Anzeige. `UI-11a/f/h` sind deshalb besonders wichtig; bei Classloader-/JEI-Rezeptfehlern sofort abbrechen.
   **Nachreview ST-32:** Dieser konkrete Quellrisikofall wurde durch eine eigene `IRecipeCategory` behoben. Die echte JEI-JAR-Kompatibilität und die übrigen Laufzeitfälle bleiben offen; [neue Fundmatrix](JEI_LIVE_PLANT_CONDITIONS_DESIGN_2026-11-21.md).
3. **Reload-Zwischenzustand:** Das Sync-Paket enthält keine separate „Reload beginnt“-Benachrichtigung. Der zuletzt bestätigte Snapshot kann bis zum nächsten Serverpaket sichtbar bleiben. `UI-11c/h` misst diese Lücke; die Testanweisung behauptet keine unmittelbare Invalidierung.
4. **Langläufer-/Cache-Risiko:** JEI kann alte versteckte Info-Rezepte behalten, wenn sich Arten-/Seed-Zuordnungen oft ändern. `UI-11h` erfasst Duplikate und Speicher-/Rezeptwachstum; ein kurzer einmaliger Reload reicht nicht als Nachweis.
   **Nachtrag ST-33:** Seit der Lifecycle-Härtung wird bei 4096 unterschiedlichen Signaturen pro Runtime keine weitere neue JEI-Seite registriert; eine DE/EN-Warnung weist auf die reduzierte Anzeige und einen nötigen Client-Neustart hin. `UI-11h` muss diese Grenze und die unterdrückten Varianten ausdrücklich als Ausnahme prüfen. Der v5-JSON-Fall bleibt unverändert historisch; ein späterer neuer Trackerstand kann die Grenzfall-Anleitung detaillierter machen.
5. **JEI-freie Hauptmod:** Obwohl Quellen die JEI-Nutzung bedingt laden, kann nur `UI-11f` plus `TG-04` einen dedizierten Start ohne JEI bestätigen. Keine Add-on-Ladung voraussetzen.
6. **Invasive/automatisierte Sondertests:** `UI-11i`, NBT-/Blockschutz-, GE-12- und BAL-01-Szenarien benötigen eigenständige Vorbereitung und Freigabe. Für den ersten Sonntag-Basistest dürfen sie `blocked` bleiben; kein pass aus fehlender Testumgebung.

## Sichere Ausführungsreihenfolge nach Freigabe

1. `TG-01`–`TG-04`, Core-only ohne Add-ons; zuerst die technischen Stoppsignale und optionales JEI-Profil trennen.
2. F0–F3: sichtbare Pflanzung, Genetik-Basics, Ernte und Analyzer; `UI-10`, dann JEI `UI-11a/b/g` bei passendem Datapack.
3. F4 / dedizierter Server: `MP` und `UI-11d/e/f`, nur mit dokumentierten Umgebungen und Spielerzahl.
4. Weltkopien: `GE-12`, NBT-/Gourd-Sonderfälle sowie `UI-11c/h`; isoliertes `UI-11i` nur mit sicherem Harness.
5. `BAL-01` und statistische Sonderabnahme separat terminieren. Nach jedem Block Ergebnisse exportieren.

**Freigabestatus:** Sonntagstest weiterhin **nicht freigegeben**. v5 ist ein importfähiger **Kandidat**, kein bestätigter Import oder abgeschlossener Testlauf. Vor jedem Lauf exakten gebauten JAR-SHA, Java-/NeoForge-/JEI-Version, Datapacks und Testergebnis belegen.
