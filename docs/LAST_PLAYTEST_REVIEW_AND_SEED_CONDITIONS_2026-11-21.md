# Letzter Playtest und künftige Pflanzbedingungen-Anzeige – statischer Abgleich

**Stand:** 21.11.2026 · Entwicklungsbranch `integration/neoforge-1211-consolidation` · **kein neuer Build/JUnit-/Minecraft-/CI-Lauf**.

## Quellenlage – keine erfundenen Einzelresultate

- **Jüngster dokumentierter Import:** `mod-test-results-2026-11-18.json`, Suite `agricraft-neoforge-1211-regression-20261117-v2`, **27 Fälle: 25 pass, 1 fail, 1 blocked**. Im Repository liegt bislang **nur die Zusammenfassung** in [`PLAYTEST_FINDINGS_AND_INTERNAL_GOURD_2026-11-18.md`](PLAYTEST_FINDINGS_AND_INTERNAL_GOURD_2026-11-18.md), nicht die vollständige damalige Ergebnis-JSON mit allen 27 Kommentaren.
- Der ältere Export `mod-test-results-2026-11-17.json` und der spätere Nachtrag `(2)` betreffen **andere Ausgangsstände/Suiten**; Details in [`PLAYTEST_FINDINGS_2026-11-17_RENDERING.md`](PLAYTEST_FINDINGS_2026-11-17_RENDERING.md) und [`MANUAL_TEST_TRACKER.md`](MANUAL_TEST_TRACKER.md). Ihre PASS/FAIL dürfen nicht mit der November-18-Suite vermischt werden.
- **Für lückenlosen Einzelreview erforderlich:** Nutzer stellt die Originaldatei `mod-test-results-2026-11-18.json` und ggf. den vollständigen letzten `(2)`-Export erneut bereit. Bis dahin sind Aussagen über die übrigen 25 Einzel-IDs/Kommentare ausdrücklich **nicht verifiziert**. Die alte Suite nicht nachträglich verändern.

## Abgleich tatsächlich dokumentierter Befunde

| Originalfall/Befund | Letzte belegte Beobachtung | Statischer aktueller Code-/Folgestand | Offene Nachprüfung |
| --- | --- | --- | --- |
| `GEN-05` (18.11., **fail**) | Erhöhte `randomTickSpeed` machte den Vergleich unbrauchbar. | `CrossFertilityRules` nutzt feste Basis und Anordnungsfaktoren, `BreedingEngine` neue Punkteregeln. Ein Random-Tick-Vergleich ist nicht gleichbedeutend mit Minutenvergleich. | **`GE-02`, `GE-03`, `GE-11`, `BAL-01`** bei gleicher Config, protokollierten gültigen Ziel-Ticks und getrennten erfolgreichen Kindern. Alter FAIL bleibt historisch bestehen. |
| `HUD-03` (18.11., **blocked**) | Jade/WTHIT nicht installiert. | Optionaler Jade-/WTHIT-Adapter und eigenständiges HUD existieren als Quellcode; keine aktuellen Screenshots/Runtime-Bestätigung. | `UI-05`: Client mit/ohne optionales Overlay separat und ohne Gene-Leak prüfen. Ohne gewünschte Testmods `blocked`, nicht `pass`. |
| `STK-04` / `GEN-01` | Kreuzung mit genetischen Seeds beobachtet, mit normalen Karottensamen kein Ergebnis. | `CropSticksBlock.useItemOn` erzeugt `Genome.basic` für registrierte unveränderte Samen; `CrossBreeding.eligibleParent` verlangt **reife** tatsächlich bepflanzte Eltern. Keine belegte Quellursache allein aus Zufalls-Ausbleiben. | `GE-01` + `GE-02`: Karotte als normale Saat auf **einfachen** Sticks, ausreifen lassen, leeres **doppeltes** Zielfeld und genügend gültige Ticks; Gene/Logs dokumentieren. **Offen.** |
| `AGRI-02` / `AGRI-03` (17.11., blocked) | Sticks eher Platten, X-Foliage bei Weizen **und** Karotte, Stage 0 kaum unterscheidbar, zu kleine/junge und zu große/reife Modelle, dunkler Weizen; Knochenmehl-/Overlay-Abweichung. | Vier-Stäbe-Modell, HASH-Geometrie, Stage-Anzeige und Bonemeal-Schnittstelle später ergänzt, aber das beweist noch kein aktuelles Gameplay-Ergebnis. | `GE-01`, `GE-06`, `UI-05` mit Vergleichsfotos pro Art/Stufe; Farben/Höhen/Overlay getrennt melden. |
| `INT-02` (17.11., fail) | JEI zeigte damals nur eine genetische Seed-Variante. | `CropGeneticsJeiPlugin` registriert bekannte Varianten, dynamische Rezeptbeispiele und wiederverwendbares Papier im Quellcode. | `UI-05`, `UI-07`, `ER-08`: sichtbare bekannte Arten, tatsächliche Crafting-Ergebnisse, Hinweis in DE/EN. Kein JEI-PASS ohne JEI. |
| `VIS-02` / Cross-Layer (17.11., fail) | Zusätzliche Kreuzlage blieb bzw. ließ sich nicht wie erwartet entfernen. | `CropSticksBlock` sperrt Direktpflanzung auf `CROSS`, ermöglicht Abnahme mit leerer Hand und prüft bestätigte Zustandswechsel; Nachkomme soll zweite Lage verbrauchen. | `GE-07`, `GE-08`: Itemzahlen und Kreuzmodell vor/nach jedem Schritt, ggf. Weltkopie für Schutz-Plugin-Fall. |
| `FRU-01` / `FRU-02` / `FRU-04` (17.11., fail) | Erster bzw. zweiter Fruchtzyklus historisch problematisch; insbesondere ausbleibende zweite Melone. | 18.11. auf **interne** Früchte innerhalb Crop Sticks umgebaut; Alt-Save mit physischer Nachbarfrucht gesondert. | `ER-04`, `ER-05`, `ER-06`, `ER-11`: zwei volle Zyklen, Save/Reload und Alt-Save ohne Doppel-Drops. |
| `ITEM-01`, `GEN-02`, `GRO-02` (18.11. kommentiert) | Genpaardarstellung `3/1` als dominante/rezessive Allele; genetische Verbesserung beobachtet; Sticks auf Gras standortabhängig. | `Genome`/`GenePair` und artspezifische `PlantRequirements` im Code; Boden nie pauschal nur Farmland. | `GE-04`, `GE-06`, `ER-07`: bekannte Werte verfolgen; Bodenregel der tatsächlichen Art protokollieren. |

**Bewertung:** Der bisherige Quellenstand erlaubt eine **gezielte Risiko-/Nachtestliste**, aber **keinen vollständigen 27-von-27-Abgleich** des letzten Ergebnisexports. Alle oben als „Quellcode vorhanden“ bezeichneten Fixes bleiben für den gemeinsamen Test **unbestätigt**.

## Neu angefragt: Pflanzablehnung verständlich anzeigen (`UX-01`, Quellcode ergänzt)

**Spielerverhalten:** Beim Rechtsklick mit einem passenden Seed auf Crop Sticks, wenn das Pflanzen serverseitig scheitert, eine **einmalige kompakte Aktionsleisten-Meldung** (kein Chat-Spam) mit dem **konkreten ersten Ablehnungsgrund** anzeigen. Beispiel: „Karotte: benötigt Ackerboden“, „Pilz: zu hell auf diesem Boden“, „Zuckerrohr: Wasser neben dem Boden fehlt“, „Kreuzungs-Sticks: obere Lage zuerst entfernen“. Bei bereits bepflanzten Sticks und nicht erkanntem/unzulässigem Seed eigene Ursachen statt irreführender Bodenmeldung.

**Technische Leitplanken:**

1. **Server entscheidet** anhand derselben Regeln wie `CropSticksBlock.useItemOn` / `CropConditions.canGrow`; keine unabhängige, abweichende Client-Schattenlogik. Aktionsleisten-Nachricht nur an den handelnden Spieler, keine Welt-Zuschauer-Daten.
2. Prüfreihenfolge: vorhandener Zielzustand (`CROSS`, `PLANTED`, BE) → gültige/zuordenbare Saatgutart und fremde Data Components → konkreter Standort/Boden → artspezifische Zusatzregel → Biome/Dimension/Höhe/Fluid/Saison/Chemie → Licht mit **Strength-Toleranz**. Ursachen dürfen keine unanalysierten Genwerte verraten.
3. Für bedingte Nährstoff-/Chemie-/Saisonregeln nicht „Wasser fehlt“ raten. Bei unklarer externer Datenquelle sachlich „Standortbedingungen nicht erfüllt“ melden; optional Diagnosehinweis nur mit berechtigtem Entwicklerbefehl.
4. Nichts am Saatgut verbrauchen und kein Pflanzenwachstum/Genom ändern, nur weil die Anzeige neu ist. Lokalisierung DE/EN, höchstens ein Hinweis pro bewusster Interaktion.

**Akzeptanzfälle (`UI-10`, jetzt in Tracker-v4):** normales Weizensaatgut auf unzulässigem Boden, Pilz auf Myzel vs. zu heller sonstiger Erde, Zuckerrohr mit/ohne angrenzendes Wasser, Samen auf doppelter Kreuzlage, belegter Stick, unerkannte oder manipulierte Saat, Strength-abhängige Lichttoleranz. In jedem Fall Meldung, Itemzahl und weiterhin unverändertes Genom prüfen.

**Nachtrag im Quellcode:** `CropPlantingFeedback` prüft diese Gründe im Serverpfad; `CropSticksBlock.useItemOn` sendet eine Aktionsleisten-Nachricht nach abgelehnter Pflanzung, ohne den Itemverbrauch zu verändern. DE/EN-Nachrichtenschlüssel und ein Lokalisierungs-JUnit-Quellfall sind ergänzt. [Neue Tracker-Suite v4](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v4.json) führt `UI-10`. **Nicht gebaut oder im Spiel überprüft**. Der erste Ablehnungsgrund ist absichtlich kompakt; komplexe Datapack-Saison-/Chemiebedingungen erhalten eine sachliche Sammelmeldung.

## Neu angefragt: JEI-Informationsseite für Pflanzenstandorte (`UX-02`)

**Anzeige pro tatsächlich registrierter Pflanzenart:** erlaubter Bodenblock oder Boden-Tag (mit lesbaren Blockbeispielen und ID-Fallback), Lichtbereich, gegebenenfalls Wasser-/Nachbarregel, Biome/Dimension/Y/Fluid/Saison/Bodenchemie und Strength-Toleranz. Ein schlichtes Beispiel darf „Weizen: Ackerboden, Licht 9–15“ zeigen. **Nicht verallgemeinern:** Wasser ist nicht für jede Ackerpflanze ein harter Nachbarblockzwang; das zusätzliche Wasser neben dem Boden betrifft im aktuellen Code ausdrücklich Zuckerrohr. Pilze können auf Myzel/Podsol/Nylium die Dunkelheits-Sonderregel umgehen, sofern die Datapack-Anforderungen es zulassen.

**Wichtige technische Grenze:** Der `AgriPlantsRegistry`-Snapshot ist serverautoritativ, während `CropGeneticsJeiPlugin.registerRecipes` clientseitig mit bekannten Standardarten arbeitet. Eine genaue JEI-Seite für **Datapack- und später optional registrierte Pflanzen** benötigt eine schmale, versionsgebundene Client-Projektion der angezeigten `PlantRequirements` und eine Aktualisierung nach Login/Datapack-Reload. Solange diese nicht existiert, keine Standardangaben als aktuelle Serverregeln ausgeben; besser „Serverbedingungen nicht synchronisiert“ als falsche Informationen. Keine privaten Seed-Genwerte übertragen.

**Akzeptanzfälle (`UI-11`, zukünftige Tracker-Suite):** Vanilla-Farmland, Myzel/Pilz-Sonderregel, Zuckerrohr-Wasser, eigener Boden-Tag/Datapack-Override, unbekannter Mod-Block, Wechsel nach `/reload`, JEI nicht installiert und dedizierter Server ohne JEI. JEI bleibt **optional**; ohne JEI soll die Hauptmod funktionieren.

## Nächster zusammenhängender Schritt

1. **Original-Ergebnisexport nachreichen**, damit der 27-Fälle-Review ohne erfundene Einzelbefunde vervollständigt wird. Bis dahin bekannte Fehler gezielt als offene Nachtests führen.
2. `UX-01`-Quellpaket ist vorbereitet und bleibt bis zum autorisierten Hauptmod-Test unter `UI-10` unbestätigt.
3. Danach `UX-02` separat implementieren; wegen Netzwerk-/Datapack-Synchronisation **größeres** UI-/JEI-Paket mit NBT-/Netzwerk-Review, nicht als beiläufige Tooltipp-Ergänzung behandeln.

**Testfreigabe weiterhin ausstehend.** Weder der Review noch die UX-Anforderung ist ein bestandener Spieltest.
