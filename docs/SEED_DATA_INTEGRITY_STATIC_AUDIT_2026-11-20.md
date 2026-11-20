# Hauptmod: statisches Seed-/Inventar-/NBT-Audit (20.11.2026)

**Ausgangsstand:** `5254fbba823299147ae8d878ec08d171537b3ca1`, Branch `integration/neoforge-1211-consolidation`. **Keine** Gradle-, JUnit-, CI-, Minecraft- oder Multiplayer-Ausführung. Bewertung nach [`CODE_REVIEW_NBT_GUIDELINES.md`](CODE_REVIEW_NBT_GUIDELINES.md), nur relevanter Hauptmod-Quellcode. Alle Aussagen sind statische Codebefunde, keine Laufzeitgarantie.

## Geprüfte Datenwege

| Weg | Statischer Befund | Noch zu beweisen |
| --- | --- | --- |
| `CropComponents` / `CropItems` | Genome, Analysestatus und OriginalSeed besitzen Persistenz- und Netzwerk-Codecs; `recoverOriginalSeed` verweigert zusätzliche fremde Component-Patches und uneindeutige Herkunft. Bewusste explizite Rückwandlung erzeugt ein gewöhnliches Seed-Item ohne Genetik. | `ER-07`, `CR-08`–`CR-11`: Save/Reload, entfernte optionale Mods, tatsächliche Item-Komponenten. |
| `OriginalSeedRecoveryRecipe` | `getRemainingItems` kopiert exakt **ein** Papier mit Komponenten; genetischer Seed hat keinen Rest. Rezept akzeptiert nur je einen gültigen Seed und Papier. | `ER-08` / `CR-12`: Bleibt ein einzelnes Blatt tatsächlich im Gitter; 16er-Stack, benannte Komponenten und Shift-Klick ohne Duplikation. |
| `SeedInventoryBlockEntity` | `setItem` erstellt eine Kopie und begrenzt die Menge auf das Minimum aus Container- und Itemlimit; `saveAdditional/loadAdditional` ruft `super` auf und leert vor Einlesen alter NBT-Slotlisten die aktuellen Slots. `removeItem` und `clearContent` setzen `setChanged`. | `MP-11`, `MP-12`, `MP-15`, `MP-16`, `MP-19`: Gesamtmengen, Gene und leere Slots nach Menüaktionen und Reload. |
| `SeedAnalyzerCombinedMenu` | Die neun Analyzer-Slots sind auf 1, die 27 Chest-Slots auf 64 begrenzt. `quickMoveStack` und Slotannahme prüfen die Gültigkeit beider Blockentities; Entfernung eines Blocks sperrt weiteren Transfer. | `MP-04`–`MP-09`, `MP-15`–`MP-17`, `MP-21`: Rennen mit zwei Spielern sowie reale Menü-/Cursor-Verarbeitung. |
| `SeedAnalyzerBlockEntity` / `SeedChestBlockEntity` | Welt-Zuschauer sehen beim Analyzer nur Platzhalter und Belegungsflags, bei der Chest keine Inventarstacks; volle Inventare bleiben im serverseitigen Save-/Menüweg. Analyse und Journal-Abgleich erfolgen serverseitig. | `MP-01`–`MP-03`, `MP-18`, `MP-20`, `MP-22`, `MP-23`: Watcher-Pakete, Save/Reload und clientseitige Prognose. |
| `SeedJournalItem` | Entdeckungen und Mutationstriple sind Data Components des einzelnen Journal-Stacks. Gespeicherte IDs werden auf gültige ResourceLocations geprüft; bekannte Mutationen werden aus dem aktuellen Snapshot abgeleitet. | `UI-03`, `UI-04`, `UI-08`: Buchübergabe, Reload und Journal-Anzeige. |

## Verbleibender Befund: direkte Container-Setter sind keine verlustfreie Bulk-Einlage

| Feld | Ergebnis |
| --- | --- |
| **Priorität** | **MITTEL** – Integrations-/Automationsrisiko, kein nachgewiesener Spieler-Menüfehler. |
| **Fundstelle** | `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/SeedInventoryBlockEntity.java`, Methode `setItem`. |
| **Ursache** | Das öffentliche `Container.setItem(slot, stack)` kopiert einen übergroßen `ItemStack` und setzt seinen Count auf das Slotlimit, ohne den abgeschnittenen Rest zurückzugeben. `setItem` hat keinen Rückgabewert. |
| **Mögliche Auswirkung** | Ein anderer Mod-/Automationspfad, der direkt `setItem(0, 64erSeedStack)` statt regulärer Slot-Transferlogik benutzt, kann 63 Seeds verlieren. Der normale kombinierte Menüpfad besitzt separate Slotlimits; ob alle externen APIs sie beachten, ist nicht untersucht. |
| **Entscheidung** | **Keine spekulative Setter-Änderung** vor dem gemeinsamen Gate: unbedingtes Werfen einer Exception oder Entfernen des Slotlimits könnte bestehende Vanilla-Menüabläufe beschädigen. Externe Automationsunterstützung ist bislang nicht zugesagt. Bei beobachtetem Problem den konkreten Transferpfad nachweisen und verlustfreie Split-/Rückgabe-API gezielt ergänzen. |
| **Nachweis/Test** | Statische Quellinspektion; `MP-15`/`MP-16` überprüfen den normalen Spielermenüweg. Separater externer Hopper-/Mod-Automationsfall nur, wenn diese Unterstützung ausdrücklich in den Umfang aufgenommen wird. **Kein Test bestanden.** |

## Abschluss und Restrisiko

Keine neue, eindeutig belegte Datenverluststelle im dokumentierten **regulären Spielerpfad** gefunden; deshalb keine unnötige Quelländerung. Das bedeutet **nicht**, dass Menü-, NBT- oder Shift-Klick-Verhalten bereits bewiesen ist. Besonders Papier-Reststacks, konkurrierende Spieler und die beschriebenen SetItem-Grenzen bleiben bis zum gemeinsamen Live-Test bzw. gezielter Erweiterung offen. Die geprüften Ausgangspfade können durch spätere Commits erneut geändert werden; bei Abweichungen auch dieses Audit gegen den tatsächlichen Git-Diff prüfen.
