# GE-12: Artenmutation, Standortfilter und Elternart-Fallback

**Status 20.11.2026: statischer Review und Testvorbereitung, NICHT AUSGEFÜHRT.** Hauptmod-only, Entwicklungsbranch `integration/neoforge-1211-consolidation`. Diese Fälle ergänzen `GE-02`/`GE-04` und den Balancefall `BAL-02`; sie ersetzen weder die Gesamtfreigabe noch die Beobachtung im Spiel.

## Statisch abgeglichene Reihenfolge im Code

1. `CrossBreeding.attempt` sammelt 2–4 berechtigte Eltern und erzeugt nach erfolgreicher Fruchtbarkeitsrolle die Genwerte in `BreedingEngine`.
2. `chooseChildSpecies` betrachtet passende Datapack-Mutationsdefinitionen in stabiler ID-Reihenfolge. Pro Kandidat müssen Elternpaar, Biome/Dimension, Umgebung, Saison, **Bodenchemie mit gedeckelter Strength** und **Wachstumsfähigkeit mit gedeckeltem Kindgenom** stimmen. Erst danach wird dessen Artenmutations-Chance gewürfelt.
3. Scheitert jede gültige Mutation, werden Elternarten zufällig angeordnet. Nur eine Elternart, die mit den **endgültig gedeckelten** Werten am Ziel wachsen kann, wird gewählt. Sind alle ungeeignet, verwirft der Aufrufer den Versuch ohne Kind.
4. Nach der Artenwahl setzt `SpeciesGeneLimits.apply` den Gen-Deckel erneut um. Eine neue Art erhält maximal `4/4/4` auch bei ausschließlich gleichartigen Eltern. Eine Elternart im gemischten Kreuz erhält maximal 5 bzw. den höchsten tatsächlich vorhandenen Elternwert **dieser Art** je Eigenschaft. Reine gleichartige Nachkommen haben keinen zusätzlichen Deckel.

## Vorbereitete, noch offene Fälle

| ID | Vorbereitung in **kopierter Testwelt** | Erwartetes Ergebnis |
| --- | --- | --- |
| GE-12a | Zwei unterschiedliche Elternarten und eine **tatsächlich passende** aktivierte Datapack-Mutation zur dritten Art; Zielbedingungen erfüllen. Für die isolierte Artenchance im Test-Datapack vorübergehend `chance=1` verwenden. | Entsteht ein Kind, gehört es zur Zielart und besitzt je Wert höchstens 4; es darf auch schwächer sein. Kein Rückfall auf eine Elternart, solange diese gültige Mutation erfolgreich bleibt. |
| GE-12b | Dieselben Eltern, aber gültige Mutationsdefinition mit `chance=0`; Zielbedingungen der beiden Elternarten für den Ergebnisstand möglichst beide erlauben. | Nach erfolgreichem Fruchtbarkeitswurf entsteht eine **Elternart**, keine dritte Art; Genwerte folgen der 5er-/Eigeneltern-Regel. Eine fehlgeschlagene Artenmutation ist **keine garantierte exakte Genom-Kopie**. |
| GE-12c | Datapack-Zielart benötigt durch Licht-/Chemiebedingungen einen Strength-Wert **über 4**; Mutation wäre mit dem ungekürzten Erbgenom möglich, mit `4/4/4` aber nicht. | Die Mutation wird **vor der Chancenrolle** als ungeeignet aussortiert. Ein geeigneter Elternart-Fallback darf entstehen; niemals eine am Standort unmöglich wachsende Zielart. |
| GE-12d | Alle beteiligten Eltern besitzen dieselbe Art mit hohen Genwerten, aber die Datapack-Mutation ergibt eine **andere** Art. Nur durchführen, falls die Test-Mutationsdefinition dieses Elternpaar ausdrücklich zulässt. | Auch hier maximal `4/4/4` für die neue Art (`ST-24`); normale Nachkommen derselben Elternart bleiben ungekürzt. |
| GE-12e | Alle Mutationsergebnisse und alle Elternart-Fallbacks sind für die **Kind-Gene** am Standort ungeeignet. | Kein unsichtbarer Genomrest, keine versehentliche Bepflanzung und kein fälschlich verbrauchtes Item. Ziel bleibt leere, gekreuzte Crop Sticks. |
| GE-12f | Drei/vier gemischte Eltern mit wiederholter `5/5/5`-Kartoffel und perfekter Karotte; dieselbe Zielart mehrfach erzwingen bzw. dokumentieren, sobald sie zufällig entsteht. | Kartoffeln werden nicht durch wiederholte Fremdkreuzung auf 6+ angehoben; höhere, **bereits unter Kartoffel-Eltern vorhandene** Werte bleiben einzeln erlaubt. |

**Voraussetzungen und Abbruchregeln:** Zuerst in den aktuellen Datapack-Definitionen nach gültigen Elternpaaren und einer Zielpflanze suchen; **Karotte + Kartoffel → Kürbis ist nur ein Beispiel und kein behauptetes Standardrezept**. Falls kein passender Fall existiert, einen eigenen Test-Datapack **nur in einer Weltkopie** anlegen; niemals produktive Saves oder bestehende Datapacks überschreiben. `chance=1` garantiert **nur die Artenmutation, nachdem** ein zulässiges Kind entstanden ist; weder Fruchtbarkeit noch ein Kind pro Tick sind garantiert. Bei fehlenden geeigneten Pflanzen-/Boden-/Saisonbedingungen als `blocked` dokumentieren statt Ergebnisse zu erfinden.

**Nachweise pro Fall:** exakte HEAD-SHA, Modliste, Test-Datapack und dessen SHA/Backup, Elternarten und Vorlagen-Genwerte, Boden/Licht/Chemie/Saison, Server-Config, Anzahl gültiger Ziel-Random-Ticks, tatsächliche Kindart und Gene, Screenshots/Log und Ergebnis `pass/fail/blocked`. Für seltene Zufallszweige mit `chance=0` oder gemischten Eltern mehrere Beobachtungen protokollieren; ein einzelner Versuch beweist keine Wahrscheinlichkeit.

**Gesondert offen:** Das statistische Ziel „vier `1/1/1`-Eltern → `10/10/10` innerhalb durchschnittlich 70 erfolgreicher Kinder“ gehört zu `BAL-01`, nicht zu diesem Art-Fallback-Test. **Keine Gradle-, JUnit-, Minecraft-, CI- oder Simulationsausführung ohne ausdrückliche Freigabe.**
