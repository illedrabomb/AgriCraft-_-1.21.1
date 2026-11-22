# Core-Änderung: Genetische Seeds stapeln und testseed-Arten vorschlagen

**22.11.2026, Code vorbereitet, noch nicht gebaut oder ausgeführt.** Bezug: Nutzerfeedback im manuellen v8-Export (GE-01: gleiche Gene derzeit nicht stapelbar; GE-12/ER-03: Arten-IDs im Command schwer zu finden). Frühere 212/212 JUnit- und Build-Belege gelten nur für deren damaligen Commit, **nicht** für diese Änderung.

## Beschlossene Regel

- Gleiche registrierte Itemart `cropgenetics:genetic_seed`, gleiche Pflanzenart und **Growth/Gain/Strength**, gleicher Analysezustand, gleiche `original_seed`-Herkunft und alle sonstigen **identischen Data Components**: bis **64 je Stack**. Die Elternidentität wird im `Genome` gar nicht gespeichert und verhindert das Stapeln nicht.
- Unterschiedliche Gene, Arten, Analysezustände oder Zusatzdaten: Vanillas `ItemStack.isSameItemSameComponents` verhindert das Mergen. **Kein Vergleich nur der sichtbaren Zahlen.**
- `original_seed` bedeutet *ursprünglicher Itemtyp für eine sichere Rückwandlung*, **nicht** Mutter/Vater. Unterschiedliche `original_seed`-Werte dürfen nicht stillschweigend zusammengelegt werden, weil sonst beim Rückwandeln die Herkunft eines Seeds verloren ginge. Gleiche Eltern sind dagegen keinerlei Voraussetzung.
- Der Analyzer bleibt **ein Seed je Slot**, auch wenn das Seed-Item jetzt generell 64 zulässt. Seed Chest und Spielerinventar dürfen identische Seeds stapeln. Journal bleibt unstackbar. Bestehende einzelne Seeds bleiben gültig; der Patch verändert keine gespeicherten Genome.
- Bekannter, **unabhängiger** UI-01-Befund der verbundenen Analyzer-GUI: Menü-Slotfilter sind noch zu reparieren; geänderte Seed-Stapelgrenzen ersetzen diese Reparatur **nicht**.

## Command-Artenvorschläge

`/cropgenetics testseed <species> ...` verwendet den aktiven `AgriPlantsRegistry.snapshot().plants().keySet()` als Brigadier-Vorschläge. Das ist die **tatsächlich geladene** Arten-ID-Liste, nicht die Vanilla-Item-ID-Liste und nicht die historischen Beispielnamen. Suggestions aktualisieren sich nach Datapack-Reload/neu registriertem Command anhand des aktuellen Snapshots. Operatorrechte (Level 2) bleiben Voraussetzung. Leere oder ungültige Datapack-Liste: keine erfundenen Arten vorschlagen.

## Prüfung erst nach neuer Freigabe

1. JUnit: die geänderten Testquellen prüfen gleiche Genome bei verschiedenen fiktiven Eltern, andere Gene/Art, Analyseflag, `original_seed`, fremde `CUSTOM_NAME`-Komponente; Chest 64, Analyzer-Limit 1. **Geschrieben, nicht ausgeführt.**
2. Minecraft: zwei Seeds gleicher Art und gleicher Gene mit identischen Komponenten stapeln im Spielerinventar/Seed Chest bis 64. Unterschiedliche Gene und `original_seed` bleiben getrennt; aus dem Analyzer nur 1. Beim Stapeln/Entstapeln und nach Save/Reload **keine Gene oder Komponenten verlieren**.
3. Command: Als Operator `/cropgenetics testseed <TAB>`, zusätzlich nach Namespace-/Namenspräfix; unbekannte Arten weiterhin korrekt zurückweisen. Aktive Arten mit `/cropgenetics data summary` bzw. gültigem Datapack-Snapshot abgleichen. Nach `/reload` erneut prüfen.
4. Crafting-Rückwandlung mit gestapelten genetischen Seeds: pro Rezept nur ein Seed und kein Papierverlust. Menü-/Shift-Klick-Spezialfälle nicht stillschweigend als bestanden werten.
5. UI-01 (Analyzer + Chest) und UI-09 (Journal-Einlage) aus dem Phase-2-Bericht bleiben separat offen. P0/P1 nur nach tatsächlichem Nachweis.

Keine Simulation, Gradle-/JUnit-/Minecraft-/CI-Ausführung und kein automatischer Run mit dieser Änderung durchgeführt.
