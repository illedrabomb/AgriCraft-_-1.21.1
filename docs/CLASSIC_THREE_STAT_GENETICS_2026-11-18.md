# Vereinfachung der CropGenetics-Genetik – 18.11.2026

## Spielerentscheidung

AgriCraft soll wieder drei eindeutig verständliche Genwerte besitzen:

- **Growth / Wachstum**: 1–10
- **Gain / Ertrag**: 1–10
- **Strength / Stärke / Robustheit**: 1–10

Es gibt **keine versteckten dominanten oder rezessiven Allelpaare** mehr und keine weiteren vererbbaren Fertility-/Mutativity-Werte. Im Shift-Tooltip stehen drei Werte wie `Wachstum: 3 / 10`. Dies bedeutet Wert 3, erlaubtes Maximum 10.

## Genetik und Kreuzungsregeln

- Ein neuer Samen erhält pro sichtbarem Wert zufällig den entsprechenden Wert eines Elternteils.
- Eine Kreuzung nutzt **zwei, drei oder vier** geeignete reife Elternpflanzen um den Ziel-Crop. Die gerade Zweierreihe ist die kleinste Kombination und hat die geringste Wahrscheinlichkeit. Mit drei und vier Eltern können die drei Stat-Werte von jedem Elternteil geerbt werden; die Chance auf ±1-Änderungen steigt (1× / 2× / 3×). Grenzwerte 1..10. Die alten Konfigurationsschlüssel `rowCrossEvolution` und `rowEvolutionMutationMultiplier` gelten aus Kompatibilitätsgründen jetzt für alle Mehr-Eltern-Kreuzungen.
- Klone übernehmen den vollständigen Dreier-Wertesatz des Elternteils.
- Neue Basissamen beginnen mit 1/1/1.
- **Fertility** ist jetzt eine feste Spielmechanik (30 % Grundchance pro Elternpaar und Berechtigungsversuch, bevor der serverseitige `crossFertilityMultiplier` angewendet wird), keine genetische Eigenschaft. Die gleiche Grundchance gilt für einzelne Ausbreitungsversuche vor dem species-spezifischen Spread-Faktor.
- **Mutativity** wird nicht mehr vererbt. Stattdessen gilt eine nachvollziehbare Grundchance von 10 % pro Wert und Kreuzung, skaliert durch `rowEvolutionMutationMultiplier` (nur in erlaubten geraden Reihen). Ob ein Wert steigt oder fällt ist zufällig.

## Migration und Rückwärtskompatibilität

- Das neue NBT-/Datenkomponenten-Format speichert `plant`, `growth`, `gain`, `strength` als einfache Werte.
- `GenomeCodecs.GENOME` akzeptiert zusätzlich alte Werte mit `{dominant,recessive}` für alle fünf bisherigen Gene. Bei der Konvertierung bleibt pro sichtbarem Merkmal der **höhere (vorher dominante/angezeigte) Wert** erhalten. Versteckte rezessive Allele, Fertility und Mutativity werden verworfen.
- Bereits bestehende Samen und platzierte Pflanzen werden beim nächsten Lesen in das neue Genom übersetzt. Die neue Version darf nicht mit einem alten, weiter laufenden Client gemischt werden (geändertes Netzwerk-Streamformat).
- **Wichtig:** Vor einem Test mit wertvollen alten Welten eine Sicherung anlegen. Die Rückmigration in ältere Modversionen ist nicht vorgesehen.

## Vor dem nächsten kombinierten Gameplay-Test offen

- Lokaler Java-21-Gradle-9.2.1-Build und vollständige JUnit-Suite müssen die neue Migration und die parallele Melonen-/Kürbis-Umstellung bestätigen.
- Neue Einzelwerte und Alt-Samen vor/nach einem Save-Reload vergleichen; der frühere dominante Wert muss erhalten bleiben.
- Zweierreihe, drei und vier geeignete Eltern separat prüfen: Ein neuer Samen kann Werte von jedem Elternteil übernehmen; die Chancen steigen mit der Anzahl.
- Crossbreeding normaler Karottensamen gesondert im gleichen Setup und über angemessene Random-Tick-Zeit testen; im bisherigen Code gab es keine artbedingte Sperre.
- Fehlende HUD-Integration mit Jade/WTHIT bleibt optional/blockiert, solange diese Mods nicht installiert sind.
