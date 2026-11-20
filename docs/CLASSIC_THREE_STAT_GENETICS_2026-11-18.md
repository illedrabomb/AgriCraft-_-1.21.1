# Vereinfachung der CropGenetics-Genetik – 18.11.2026

## Spielerentscheidung

AgriCraft soll wieder drei eindeutig verständliche Genwerte besitzen:

- **Growth / Wachstum**: 1–10
- **Gain / Ertrag**: 1–10
- **Strength / Stärke / Robustheit**: 1–10

Es gibt **keine versteckten dominanten oder rezessiven Allelpaare** mehr und keine weiteren vererbbaren Fertility-/Mutativity-Werte. Im Shift-Tooltip stehen drei Werte wie `Wachstum: 3 / 10`. Dies bedeutet Wert 3, erlaubtes Maximum 10.

## Genetik und Kreuzungsregeln

- Ein neuer Samen erhält pro sichtbarem Wert zufällig den entsprechenden Wert eines Elternteils.
- Ein Ziel-Feld mit doppelten Crop Sticks (`S`) kann direkt angrenzende reife Eltern (`E`) aus den vier Himmelsrichtungen nutzen: **2, 3 oder 4 Eltern**, beliebige Anordnung (auch über Eck). Bei 2/3/4 Nachbarn gilt der Fruchtbarkeitsfaktor **1,00 / 1,25 / 1,50** auf die Grundchance von 30 %. Die Veränderung einzelner Genwerte nutzt Mutationsfaktor **1 / 2 / 3**. Alle Eltern können Werte vererben.
- **Durchgehende Reihe:** Auch `E E E S` (oder länger) ist erlaubt: mindestens drei direkt aneinanderliegende reife Eltern in derselben Richtung neben dem Ziel, ohne leere Lücke. Maximal vier Eltern gehen in die jeweilige Vererbung ein; weitere Pflanzen dürfen in der Reihe stehen. Die Reihe ist bewusst seltener: Fruchtbarkeitsfaktor **0,50** (= 15 % vor Server-Multiplikatoren), Genwert-Mutationsfaktor **0,50**. Direkt angrenzende 2–4-Eltern-Kreuzungen haben Vorrang vor der Reihe.
- Die bisherigen Konfigurationsnamen `rowCrossEvolution` und `rowEvolutionMutationMultiplier` gelten aus Kompatibilitätsgründen für Genwert-Mutationen bei beiden Kreuzungsarten.
- Klone übernehmen den vollständigen Dreier-Wertesatz des Elternteils.
- Neue Basissamen beginnen mit 1/1/1.
- **Fertility** ist jetzt eine feste Spielmechanik (30 % Grundchance pro Elternpaar und Berechtigungsversuch, bevor der serverseitige `crossFertilityMultiplier` angewendet wird), keine genetische Eigenschaft. Die gleiche Grundchance gilt für einzelne Ausbreitungsversuche vor dem species-spezifischen Spread-Faktor.
- **Mutativity** wird nicht mehr vererbt. Stattdessen gilt eine nachvollziehbare Grundchance von 10 % pro Wert und Kreuzung, skaliert nach Anordnung und durch `rowEvolutionMutationMultiplier`. Ob ein Wert steigt oder fällt ist zufällig.

## Offener Balance-Auftrag BAL-01 (Spielerentscheidung 20.11.2026; **nicht umgesetzt**)

**Priorität:** Hauptmod-Gameplay-Balance, nachrangig gegenüber den bereits vereinbarten Sicherheits-/Testvorbereitungen; vor einer Änderung als **eigenes vollständig abgeschlossenes Arbeitspaket** planen. Keine aktuellen Wahrscheinlichkeiten oder Vererbungsregeln ohne erneute Abstimmung stillschweigend verändern.

**Spielerfeedback:** Der Weg von `1/1/1` zu `10/10/10` erscheint mit dem aktuellen System deutlich zu lang. **Mehr direkt benachbarte Eltern sollen nicht nur die Entstehung beschleunigen, sondern auch die Zahl der für hohe Genwerte erforderlichen erfolgreichen Zucht-/Mutationsschritte spürbar reduzieren.** Die bisherige Mutations-/Unverändert-Quote prüfen; perfekte Seeds sollen anspruchsvoll, aber nicht exzessiv zeitaufwendig sein.

**Konkreter Zielwert (20.11.2026):** Aus **vier reifen `1/1/1`-Eltern** soll unter einer nachvollziehbaren Strategie, bei der jeweils die **besten erzeugten Nachkommen als neue Eltern** verwendet werden, ein `10/10/10`-Seed **statistisch innerhalb von höchstens 70 erzeugten Kindern** erreichbar sein. Als vorläufige Interpretation den Erwartungswert bzw. typische Versuchsanzahl **≤ 70 Kinder** anstreben, nicht eine mathematisch unmögliche harte Zufallsgarantie behaupten. Vor Implementierung Selektionsregel (welche Eltern ersetzt werden, wie Gleichstand bewertet wird), Mutations-/Vererbungsannahmen und Auswertung (Durchschnitt, Median, obere Perzentile) gemeinsam präzisieren; ohne Simulation keine Erfüllung behaupten.

**Robustheit als Mutationsregler:** Der bestehende Genwert **Strength/Robustheit** soll die Mutationswahrscheinlichkeit beeinflussen: **je niedriger die Robustheit, desto wahrscheinlicher die Mutation**, bei hoher Robustheit entsprechend stabilere Vererbung. Vor Umsetzung klären, ob der Robustheitswert des Kindes oder der Eltern maßgeblich ist und ob die erhöhte Wahrscheinlichkeit positive **und** negative Mutationen gleichermaßen oder gezielt Fortschritt begünstigt. Die einfache Drei-Werte-Genetik beibehalten; keinen neuen versteckten Genwert einführen.

**Vom Spieler konkretisierte geplante Mutationsmechanik (noch kein Code):** Nach der bisherigen unabhängigen Vererbung jedes der drei Werte erhält ein erfolgreicher Kreuzungsnachkomme zufällig **0 bis 4 positive Mutationspunkte** aus einer noch festzulegenden, von Elternanzahl und ggf. Robustheit abhängigen Wahrscheinlichkeitsverteilung. Die Punkte werden zufällig auf Wachstum/Ertrag/Robustheit verteilt. **Pro Wert dürfen in einem Zuchtereignis höchstens +3 ankommen; darüber hinaus zugewiesene Punkte verfallen ausdrücklich**, ebenso Punkte über der Genobergrenze 10. Hohe bestehende Werte sollen dadurch die Verbesserung tatsächlich verlangsamen; keine automatische Neuverteilung von Überläufen auf andere Eigenschaften.

**Negative Mutation und begrenzter Ausgleich:** Ein Wert kann in einem Ereignis **mehr als 1 Punkt verlieren** (Untergrenze 1). **Jeder tatsächlich verlorene Punkt** hat nach Spielerwunsch eine **40-%-Chance**, als zusätzlicher positiver Punkt dem gemeinsamen Pool zugeschlagen zu werden; die übrigen 60 % gehen verloren, sodass das Kind unterm Strich auch schlechter werden kann. Die so gewonnenen Zusatzpunkte unterliegen derselben zufälligen Verteilung und demselben **+3-Limit je Wert** (Extrapunkte können verfallen). **Je höher die Robustheit, desto geringer die Wahrscheinlichkeit für negative Verluste**, damit das Mitentwickeln von Robustheit nützlich bleibt. Noch offen: genaue 0–4-Punkte-Verteilung je Elternzahl, negative Ereigniswahrscheinlichkeit/-höhe und welcher Robustheitswert dafür zählt. Die bisherigen konkreten 10/20/30-%-±1-Regeln bleiben bis zum späteren Balance-Paket aktiv.

**Beispiel der aktuell implementierten Ausgangsvererbung (vor jeder Mutation):** Vier Eltern `4/3/4`, `5/2/3`, `3/2/3`, `4/4/1`. `BreedingEngine.inherit` wählt **für jeden der drei Werte unabhängig und gleichgewichtet einen der vier Eltern**, keinen Mittelwert: Wachstum `3` zu 25 %, `4` zu 50 %, `5` zu 25 %; Ertrag `2` zu 50 %, `3` zu 25 %, `4` zu 25 %; Robustheit `1` zu 25 %, `3` zu 50 %, `4` zu 25 %. Beispielsweise ist der Basisnachkomme `5/4/4` möglich, ohne dass ein Elternteil genau diesen Dreierwert hat. Die spätere Pool-Mutation soll **erst danach** angewendet werden.

**Neue Diskussionsvariante: robustheitsgewichtete Startvorlage (20.11.2026):** Der Spieler schlägt vor, **zuerst einen vollständigen Eltern-Genotyp als Vorlage** auszuwählen, wobei Eltern mit höherem Robustheitswert **häufiger** ausgewählt werden. Beispiel `1/1/3`, `2/2/1`, `3/2/1`, `4/1/1`: Die Vorlage `1/1/3` soll am wahrscheinlichsten sein; bei Mutation beginnt der Nachkomme häufiger mit genau diesen drei Werten. Hohe Robustheit macht außerdem die Vorlagenvererbung stabiler und reduziert Mutations-/Verlustwahrscheinlichkeit. **Dies ist noch nicht die bestehende unabhängige Pro-Wert-Vererbung.** Ob die vollständige Vorlage diese ersetzt oder nur eine probabilistische Zusatzoption wird, sowie Gewichtungsfunktion und Bedeutung von „Klon“ bei zwei oder mehr Eltern, sind ausdrücklich **offen**. Die aktuelle echte Ein-Eltern-Klonierung (`cloneable`/`spreadChance`) nicht ungefragt ändern.

### Offener Diskussions- und Testfall BAL-02: Sprung über Artgrenzen

**Ausgangsfall:** Eine Karotte `10/10/10` und eine Kartoffel `1/1/1` sind beide gültige reife Eltern eines leeren gekreuzten Zielfelds. Die heutige `BreedingEngine.combine`-Logik wählt pro Wert zufällig einen Elternwert; damit kann **vor weiterer Mutation** `10/10/10` entstehen (bei exakt zwei Eltern je Wert 50 %, insgesamt **12,5 %**). `CrossBreeding.chooseChildSpecies` kann bei ausbleibender Artenmutation eine der Elternarten auswählen. Wird Kartoffel gewählt und ist der Standort geeignet, kann damit **bereits heute eine `10/10/10`-Kartoffel** entstehen. Ein neuer Kürbis entsteht **nur dann**, wenn eine tatsächlich passende, unter den aktuellen Bedingungen gültige und erfolgreiche Datapack-Mutationsdefinition existiert; das ist keine automatische Folge dieses Karotte/Kartoffel-Paars.

**Begriffe:** Eine misslungene **Artenmutation** mit zwei Eltern führt im heutigen Code zu einem **normalen Zwei-Eltern-Nachkommen einer Elternart** mit rekombinierten Genwerten, **nicht** zu einer garantierten exakten Kopie. **Echte Klonierung** ist der separate Ein-Eltern-Pfad (`cloneable`, `spreadChance`), der derzeit den ganzen Genotyp unverändert übernimmt. Die geplante robustheitsgewichtete vollständige Elternvorlage bei zwei oder mehr Eltern wäre wieder ein dritter, noch offener Mechanismus.

**Spielerwunsch zur Diskussion:** Gegen den direkten Transfer eines perfekten Genoms auf eine vorher schwache Art eine Schranke vorsehen: **je Genwert höchstens fünf Punkte über dem geringsten entsprechenden Wert der beteiligten Eltern**; Überschuss wird abgeschnitten, **nicht** anderswo verteilt. Im Beispiel `10/10/10` + `1/1/1` wären damit alle Werte **höchstens `6/6/6`** (nicht `5/5/5`). Offen: gilt diese Schranke nur für **unterschiedliche Elternarten**, immer für alle Kreuzungen oder abhängig von der gewählten Kindart? Welcher Bezug ist sinnvoll, wenn drei oder vier Eltern und eine neue Mutationsart beteiligt sind? Die Schranke darf das 70-Kinder-Ziel innerhalb einer Art nicht unbemerkt sabotieren.

**Festgelegte Spielerentscheidung vom 20.11.2026 – frühere +5-Regel als Entwurf überholt:** Bei **artübergreifender Kreuzung** soll der erzeugte Seed **grundsätzlich bei `5/5/5` je Wert gedeckelt** werden, **außer ein Elternteil derselben Kindart besitzt bereits einen höheren Wert**. So darf eine starke fremde Art eine schwache Art nicht direkt auf hohe Gene ziehen, selbst wenn dieselben zwei Arten wiederholt gekreuzt werden. Regelentwurf je Attribut für eine bereits beteiligte Kindart: `Limit = max(5, höchster entsprechender Genwert der Eltern DIESER Art)`; für das Kind `min(10, Limit, berechneterWert)`. Für eine **neu mutierte dritte Art ohne gleichartige Eltern** gilt dagegen die nun **festgelegte Obergrenze `4/4/4`**, selbst wenn alle Eltern `10/10/10` besitzen. **Weder `4/4/4` noch `5/5/5` sind garantierte Startwerte oder Untergrenzen:** Genetikvererbung, Punktverluste und -verschiebungen können deutlich schwächere Kinder erzeugen. **Die Obergrenze wird nach Vererbung und nach allen Mutations-/Bonus-Punkten angewendet**; abgeschnittene Punkte verfallen ohne Umverteilung. **Gleichartige Eltern untereinander** sollen ihre hohen Werte weiterentwickeln können, also keine künstliche 5er-Decke in der normalen Zucht derselben Art.

**Konkrete erwartete Beispiele für die spätere Designfreigabe:**

| Eltern bei gemischter Artenkreuzung | Ergebnisart | Geplanter höchster Genwert |
| --- | --- | --- |
| Karotte `10/10/10` + Kartoffel `1/1/1` | Kartoffel | `5/5/5` |
| Dieselben | Karotte | `10/10/10` |
| Kartoffel `5/5/5` + Karotte `10/10/10` | Kartoffel | Weiter `5/5/5` (kein schrittweises Umgehen durch Wiederholung) |
| Kartoffel `7/6/8` + Karotte `10/10/10` | Kartoffel | `7/6/8` (eigene bereits erzüchtete Werte bleiben erlaubt) |
| Karotte `10/10/10` + Kartoffel `10/10/10` | neue, gültig mutierte Art, z. B. Kürbis | höchstens `4/4/4`, je nach Genetik/Verlust auch niedriger |

**Zusätzliche verbindliche Prüfpunkte für BAL-02 (später, nicht jetzt):** (a) wiederholt Kartoffel `5/5/5` mit fremder perfekter Karotte kreuzen und sicherstellen, dass kein 6er-Erschleichen möglich ist; (b) bereits in der Kartoffellinie selbst erreichte `7/6/8` nicht herabsetzen, sofern das Ergebnis diese Werte gar nicht überschreitet; (c) Mutation zu dritter Art unabhängig von deren Elternperfektion auf maximal `4/4/4` deckeln und ausdrücklich auch schwächere Nachkommen nach Punktverlust/-verschiebung zulassen; (d) die Begrenzung nach der positiven Punkteverteilung anwenden; (e) reine Kartoffel×Kartoffel-Zucht und echten Ein-Eltern-Klon ohne artübergreifenden Deckel prüfen; (f) 3/4 gemischte Eltern mit zwei Eltern derselben Ergebnisart und unterschiedlichen Genwerten. **Arten-Deckel inzwischen in `SpeciesGeneLimits.apply` und `CrossBreeding.attempt` umgesetzt; neue JUnit-Testquelle geschrieben, nicht ausgeführt. BAL-01-Vererbungs-/Mutationsreform weiterhin offen.**

**Verbindliche spätere Testmatrix, jetzt nur TODO:** (1) Kartoffel-Kind **ohne** Artenmutation, (2) Karotte-Kind ohne Artenmutation, (3) gültige Mutation zu einer dritten Art (z. B. Kürbis **nur mit passender Datapack-Regel**), (4) ungültige/fehlgeschlagene Mutationsbedingung mit Fallback auf Elternarten, (5) Ein-Eltern-Klonpfad ohne fremde Gene, (6) drei/vier gemischte Eltern einschließlich stark unterschiedlicher Robustheit. Pro Fall Eltern-/Kindart und alle drei Werte vor/nach Mutationspunkten und Clamp dokumentieren; die **aktualisierte 5er-Deckelung pro Kindart** bzw. die **feste 4er-Obergrenze für neue Arten** ist maßgeblich, nicht der historische +5-Entwurf. Fälle unterhalb der Grenzwerte durch Punktverlust/-verschiebung mitprüfen. **Arten-Schranke inzwischen im Code eingebunden; JUnit/Minecraft noch nicht ausgeführt. Der Sonntag-Baseline-Test muss die neue Deckelung berücksichtigen, BAL-01-Mutationsreform bleibt ausgenommen. Keine Simulation/JUnit/Minecraft ohne Freigabe.**

**Präzisierung:** Bei vier Eltern gilt derzeit pro **Genwert eines erfolgreich erzeugten Nachkommens** ungefähr 30 % Mutationschance (davon 15 % `+1`, 15 % `−1`) und 70 % **keine Mutation dieses Wertes**. Das ist **keine** 70-%-Chance auf einen vollständigen 1:1-Klon: Auch ohne Mutation können Werte von verschiedenen Eltern stammen. Der Ein-Eltern-Klonpfad ist eine gesonderte Mechanik.

**[Konkreter BAL-01-Regelvorschlag zur Freigabe](BAL01_GENETICS_PROPOSAL_2026-11-20.md) (20.11.2026; keine Implementierung).** **Später gemeinsam entscheiden:** Exakte Wahrscheinlichkeiten für den 0–4-Punkte-Pool und Mehrpunktverluste, Robustheitsbezug sowie die konkrete Beste-Kinder-Auslese, um die statistische Zielvorgabe zu erreichen. Die Spielerentscheidung **Punkteüberschuss verfällt** nicht durch automatische Umverteilung aufweichen. Negative Mutationen, Auslese/Vererbung und eine faire Zieldauer für `10/10/10` berücksichtigen. Eine **analytische Abschätzung bzw. erst nach ausdrücklicher Freigabe ausführbare Simulation** verschiedener Zuchtstrategien als Entscheidungsgrundlage vorbereiten. Der Sonntag-Core-Test darf die gegenwärtige Baseline beobachten; BAL-01 darf dabei **nicht** als erledigt markiert werden.

**Akzeptanz für das spätere Paket:** Vorher-/Nachher-Regeln mit 2/3/4 direkten Eltern und 3+-Reihe nachvollziehbar vergleichen, insbesondere **vier `1/1/1`-Eltern, Beste-Kinder-Auslese und Ziel ≤ 70 erzeugte Kinder bis `10/10/10`** nach klar vereinbarter statistischer Kennzahl. Einfluss niedriger/hoher Robustheit auf Mutationschance und Rückschritte zeigen. Gameplay-Regeln ggf. samt Config, JUnit-Quellfällen und manuellem Nachtest aktualisieren. **Keine Tests/Simulationen ohne Freigabe ausführen.**

## Migration und Rückwärtskompatibilität

- Das neue NBT-/Datenkomponenten-Format speichert `plant`, `growth`, `gain`, `strength` als einfache Werte.
- `GenomeCodecs.GENOME` akzeptiert zusätzlich alte Werte mit `{dominant,recessive}` für alle fünf bisherigen Gene. Bei der Konvertierung bleibt pro sichtbarem Merkmal der **höhere (vorher dominante/angezeigte) Wert** erhalten. Versteckte rezessive Allele, Fertility und Mutativity werden verworfen.
- Bereits bestehende Samen und platzierte Pflanzen werden beim nächsten Lesen in das neue Genom übersetzt. Die neue Version darf nicht mit einem alten, weiter laufenden Client gemischt werden (geändertes Netzwerk-Streamformat).
- **Wichtig:** Vor einem Test mit wertvollen alten Welten eine Sicherung anlegen. Die Rückmigration in ältere Modversionen ist nicht vorgesehen.

## Vor dem nächsten kombinierten Gameplay-Test offen

- Lokaler Java-21-Gradle-9.2.1-Build und vollständige JUnit-Suite müssen die neue Migration und die parallele Melonen-/Kürbis-Umstellung bestätigen.
- Neue Einzelwerte und Alt-Samen vor/nach einem Save-Reload vergleichen; der frühere dominante Wert muss erhalten bleiben.
- gerade Reihe (mindestens 3 Eltern) und zwei, drei und vier direkt angrenzende Eltern separat prüfen: Ein neuer Samen kann Werte von jedem Elternteil übernehmen; die Chancen steigen mit der Anzahl.
- Crossbreeding normaler Karottensamen gesondert im gleichen Setup und über angemessene Random-Tick-Zeit testen; im bisherigen Code gab es keine artbedingte Sperre.
- Fehlende HUD-Integration mit Jade/WTHIT bleibt optional/blockiert, solange diese Mods nicht installiert sind.
