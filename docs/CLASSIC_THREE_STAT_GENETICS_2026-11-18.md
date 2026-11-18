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
