# Hauptmod – statischer Pre-Gate-Review (21.11.2026)

**Ausgangspunkt:** Branch `integration/neoforge-1211-consolidation`, GitHub-HEAD `8816bf67c0631f90df66d7f47982098b889195ec`. Review von Hauptmod-Registrierung, optionaler JEI-Anzeige, Pflanzenregel-Netzwerk und unmittelbar verbundenen Datenpfaden. **Keine Build-, Gradle-, JUnit-, CI-, Minecraft-, Multiplayer- oder Simulationsausführung.** Dieser Bericht ist **kein technisches Gate-Pass**.

## Review-Umfang und Quellbefunde

| Priorität | Pfad / Methode | Beobachtung, Risiko und Umgang |
| --- | --- | --- |
| **HOCH → Quellkorrektur** | `compat/jei/LivePlantConditionsJei.Info.description` | Bereits geöffnete JEI-Seiten konnten nach geänderter Seed-Zuordnung die **neuen Bedingungen der alten Art** anzeigen, obwohl der frühere Seed nicht mehr dieser Art zugeordnet war. Beschreibung verifiziert jetzt die vollständige Art-/Seed-Signatur der zuletzt empfangenen Regeln; bei abweichender Signatur wird DE/EN-„veraltet“ angezeigt. Runtime weiterhin nicht geprüft. |
| MITTEL, offen | `network/PlantConditionsSync.register` und `CropGeneticsMod` | Mod-Bus-Payload-Registrierung, Client-Handler-Referenz sowie `OnDatapackSyncEvent` sind statisch nachverfolgt. Dedizierter Serverstart und Play-Phasen-Reihenfolge **ohne Build/Lauf nicht bestätigt** (`TG-04`, `UI-11d/f`). |
| MITTEL, offen | `client/ClientPlantConditions.receive` | Atomare Snapshot-Ersetzung und Logout-Bereinigung im Quellcode; **vor** einem eintreffenden neuen Paket kann während `/reload` der letzte Serverstand sichtbar bleiben. UI sagt explizit „zuletzt empfangen“ (`UI-11c/e`). |
| MITTEL, begrenzt | `compat/jei/LivePlantConditionsJei.CACHE` | JEI 19.x bietet Ein-/Ausblenden, aber kein geprüftes Runtime-Entfernen. Maximal 4096 historische Signaturen und sichtbare Warnung bei zusätzlicher neuer Zuordnung. Keine unbeschränkte Registrierung; nach Grenzerreichen reduzierte JEI-Abdeckung (`UI-11h`). |
| MITTEL, offen | `data/PlantConditionsWire` / `network/PlantConditionsPayload` | Nur öffentliche `PlantRequirements`, Seed-IDs und Standort-Regeltyp; 256 KiB und Listen-/Stringgrenzen, Versionsprüfung und ungültige Daten „fail closed“. **Nicht** per JUnit oder Netzwerk ausgeführt (`UI-11i`). |
| NIEDRIG, beobachtet | `compat/jei/PlantConditionsText` | Keine behauptete vollständige Blockliste für Tags, separate Wasser-/Pilz-/Kaktus-Sonderregeln, DE/EN-Schlüssel; Titel und veraltete Seitenmeldung in Quelltest aufgenommen. Tatsächliche Übersetzungs-/JEI-Anzeige noch offen. |

## Datenintegrität und optionale Abhängigkeiten

- Keine Änderungen an `ItemStack`-Mengen, fremden Data Components, Genome-Serialisierung, Journal-/Analyzer-/Chest-NBT, Ernte- oder Crafting-Restzutaten in diesem Paket.
- Die öffentliche Serverprojektion enthält weder individuelle Genome noch Journalentdeckungen oder Inventare. JEI erzeugt lediglich ein Basissaatgut zur Anzeige; **keine Spieler-Stacks werden verändert**.
- JEI-API-Aufrufe befinden sich ausschließlich in `compat/jei`; die Client-Brücke ruft sie nur auf, wenn `ModList.isLoaded("jei")` gilt. Ein dedizierter Server **ohne JEI** muss trotzdem im späteren `TG-04`/`UI-11f` starten, statisch nicht beweisbar.
- Keine Änderung des Netzwerkformats, keine Migration von gespeicherten Daten und kein Add-on-Code. Pam/Farmer's/MineColonies bleiben außerhalb dieses Gates.

## Verbindliche offene Nachweise

1. **Technisch:** `TG-02` JUnit, `TG-03` Build und `TG-04` Client/dedizierter Server erst nach ausdrücklicher Freigabe. Bei Classloader-/Payloadfehler Gate stoppen, Logs mit tatsächlichem JAR-SHA sichern.
2. **JEI:** `UI-11a/c/e/f/h/i` mit realer JEI-19.x-JAR, geänderten Datapacks, Serverwechsel, ohne JEI sowie wiederholten Reloads. Veraltete bereits geöffnete Seite muss statt falscher Regeln einen Hinweis anzeigen.
3. **Historischer Playtest:** Originales 27-Fälle-JSON vom 18.11. fehlt weiter; `GEN-05`/`HUD-03` können nur anhand der Zusammenfassung nachverfolgt werden. Alte Resultate nicht auf den neuen Branch übertragen.
4. **Statistische Balance:** `BAL-01` bleibt eine gesonderte statistische Abnahme, keine Implikation der statischen Quellprüfung.

**Entscheidung:** Keine offene **nachgewiesene** kritische Datenverluststelle in den hier geprüften neuen JEI-/Sync-Diffs. Das heißt **nicht**, dass die gesamte Hauptmod fehlerfrei oder baubereit bestätigt wäre. Test-Gate bleibt gesperrt; nächster Schritt ist die ausdrückliche Freigabe einzelner technischer Prüfungen oder eine gezielte weitere Quellkorrektur bei neuem Befund.
