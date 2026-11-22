# Codex-Live-Gate Etappe 2 – AgriCraft Core (vorbereitet, nicht ausgeführt)

**Stand:** 22.11.2026. Basis: [Spieltestbericht](test-results/gameplay-core-2026-11-22/README.md), [v8-Abdeckung](test-results/gameplay-core-2026-11-22/v8-coverage.json), [v8-Testdefinition](test-tracker/AgriCraft-Core-Sonntag-2026-11-22-v8.json). **Keine erneute Start-/Testfreigabe aus diesem Dokument ableiten.** Aktuelle GitHub-SHA, JAR-SHA und Freigabe vor dem Lauf neu festhalten. Keine GUI-Portierung in diesem Paket.

## Übergabe an lokalen Codex

1. `AGENTS.md`, `docs/AI_START_HERE.md` und das Core-Gate lesen. Aktuellen Integrationsbranch und sauberen Arbeitsbaum prüfen; keine ungesicherten lokalen Änderungen ersetzen. Für **diese neue Etappe** die ausdrückliche Nutzerfreigabe protokollieren, bevor Minecraft/Gradle/Skripte gestartet werden. Vorhandene, durch den früheren Lauf beendete Server-/Client-Prozesse **nicht** als noch aktiv annehmen.
2. Bestehende Windows-Testinstanz `D:\\guthub repositorys\\AgriCraft-core-gate-2026-11-22\\work\\gameplay-20261122` nur nach tatsächlicher Pfadprüfung als Vorlage verwenden. **Pro Etappe eine neue disposable Welt bzw. Weltkopie**, eigenen Ausgabeordner und Serverbindung **127.0.0.1:25589** erstellen. Die Steuerungsbrücke (MCPFabric 0.8.0+1.21.1) war auf **127.0.0.1:25599** erreichbar, muss aber neu validiert werden. Auth-Token nur lokal aus dem vorhandenen `mcpfabric.config.json`, **nie ins Git**.
3. Die vorhandenen, bereits verwendeten Hilfen unter `docs/test-results/gameplay-core-2026-11-22/harness/` sind **Belege des vorherigen Laufs**, keine garantiert portablen Starter. `bridge.py` erwartet eine lokale Konfiguration unter `harness/client/config/` und schreibt Screenshots/Transkript relativ zum eigenen Verzeichnis. Deshalb die frühere Bridge nur in einen **neuen lokalen Arbeits-/Ergebnisordner kopieren und Pfade kontrollieren**, keine committed Belege überschreiben. Kein automatischer Start von `placement.py` auf bestehender Welt: es enthält `/fill` und `/setblock`.
4. Vor jeder einzelnen Versuchsserie: `/save-all flush`, Sicherung der isolierten Welt, `/gamerule randomTickSpeed 0` (nur in Kopie), getrennte markierte Testflächen, freie Inventarplätze und **Inventar-Snapshot vor/nach**. `/data get block` bzw. `/data get entity` für serverseitige Belege verwenden; `world.getBlock` der Client-Brücke lieferte zuvor `no_server` und ist kein Serverbeleg. `interact.useItem` mit `SUCCESS` allein ist kein erfolgreicher Inventar-/Block-Nachweis. Screenshot über `vision.screenshot` aus dem Spiel-Framebuffer, nicht über die weiße Windows-Fensteraufnahme.
5. Ergebnisse als separaten Lauf unter `docs/test-results/gameplay-core-<Datum>-phase2/` mit `manifest.json`, strukturierten Einzelergebnissen, Logs und **redigierten** Screenshots dokumentieren. Bestehendes v8-Original und frühere Ergebnisdateien unverändert lassen; nur neue Beobachtungen einstufen. `pass` nur bei allen v8-Erwartungen, sonst `partial`, `inconclusive`, `blocked` oder `fail`; kein stilles Hochstufen. Bei Duplikation/Itemverlust oder Persistenzschaden sofort stoppen und P0/P1-Triage anwenden.

## Runde A – Core-Ernte (priorisiert, reproduzierbare Ergebnisse)

| ID | Aufbau und Aktionen | Unverzichtbarer Nachweis |
| --- | --- | --- |
| **ER-14** | Je eine frische reife Standardpflanze sowie nach Möglichkeit Kürbis/Melone auf **getrennten Feldern** vorbereiten. Als Operator nacheinander `/cropgenetics harvestmultiplier 0`, `1`, `2`, `64` setzen und den Wert jeweils mit `/cropgenetics harvestmultiplier` auslesen. **Keine Wiederernte desselben noch unreifen Zyklus** als unabhängigen Versuch zählen. Am Ende `/cropgenetics harvestmultiplier reset` und Wert nachlesen. | Vor-/Nachher-Drops nach Item-ID und Menge, tatsächlicher Gain, Fruchtzustand und Multiplikator; bei 0 keine Produkte, Seeds/Sticks beim Abbau erhalten; je Produkt maximal ein Stack und höchstens 64. Bei fehlender Fruchtreife nur betreffende Unterfälle `blocked`. |
| **ER-03** | Hoher Gain, ggf. mehrere unterschiedliche Datapack-Produkte; einmalige legitime Ernte. Testen, dass gleiche Item-ID nicht in zwei Produktstapel aufgespalten wird. | Item-IDs, Stackzahlen, Screenshot und serverseitige Nachweise. **Keine absichtliche Änderung eines aktiven produktiven Datapacks.** |
| **ER-13** | `/cropgenetics harvestmultiplier reset`; getrennte Testseeds `/cropgenetics testseed minecraft:melon_plant 1 1 1` und `/cropgenetics testseed minecraft:melon_plant 1 10 1`. Reife **interne** Frucht in mindestens zwei Zyklen pro Gain ernten. | Nur Melonenscheiben, keine ganzen Melonenblöcke; Gain 1: 3–7, Gain 10: 12–16, je ein Stack. Fruchtbereitschaft und neuer Zyklus getrennt bestätigen. |
| **ER-12** | Fünf eigene Kürbisfelder: `/cropgenetics testseed minecraft:pumpkin_plant 1`, `2`, `5`, `8`, `10`; Multiplikator auf 1 zurücksetzen. Nur gültige reife Zyklen zählen. | Je Ernte 1–4 Kürbis-Items, Gain 1 genau 1; Verteilung für Gain 2/10 **nur mit protokollierter Stichprobe** diskutieren. Ein Durchlauf ist **kein** Prozentbeleg; ohne hinreichende Wiederholungen `partial`. |

## Runde B – Inventar und UI (ohne neue Analyzer-GUI)

| ID | Aufbau und Aktionen | Unverzichtbarer Nachweis |
| --- | --- | --- |
| **UI-09** | Leeren Analyzer, Seed und Journal getrennt prüfen: Seed mit normalem Rechtsklick; Journal nur mit **Sneak + Journal in Hand**. Sowohl `interact.useItem` als auch `interact.placeBlock` können anders dispatchen: tatsächlichen Server-Blockentity-Slot und Handmenge **nach jedem** Versuch lesen. Belegte Slots ebenfalls prüfen. | Hand- und Slotkomponenten vorher/nachher; keine Itemverluste, keine Phantom-Erfolge. Frühere Journalversuche waren wegen Steuerungs-/Hotbar-Fehler **inconclusive**, also zuerst Eingabe und ausgewählten Hotbar-Slot bestätigen. |
| **UI-01** | Analyzer **neben** Seed Chest in der isolierten Welt aufbauen. 64 normale Seeds mit Shift-Klick in der verbundenen GUI bewegen; je einen genetischen Seed und Journal in die Analyzer-Slots legen/entnehmen, übrige Chest-Slots getrennt prüfen. | Analyzer-Slot 0/1 jeweils maximal 1, sieben Füllslots nicht belegbar; Chest 27 Slots, normale Stapel bis Itemlimit; identische Gesamtmengen und Item-Komponenten vor/nach. GUI-Screenshot nur als Bedienbeleg, nicht als Beweis für unveränderte Komponenten. |
| **ER-08** | In separater Test-Crafting-Umgebung je einen genetischen Seed mit 1, 16 und benanntem Papier kombinieren, danach Shift-Klick bei mehreren Seeds prüfen. | Papier bleibt samt Name/Komponenten und Menge erhalten; je Craft genau ein Originalseed; bei erstem Duplikations-/Verlusthinweis abbrechen. Wenn Brücke echte Menü-Slotklicks nicht anbietet, **blocked** und manuellen Schritt nennen; keine Crafting-Erfolge durch bloße Commands ersetzen. |
| **UI-14** | F10 schaltet AgriCraft-HUD am Crop ein/aus; dann Taste in Minecraft-Steuerung ändern und die aktuelle Belegung im Journal (Anleitungsseite 4) gegenprüfen, Klick-Umbelegung/Esc/Delete und Clientneustart prüfen. | Framebuffer vor/nach, gespeicherte Taste und keine Leaks verdeckter Gene. Falls MCPFabric keine Tasten-/GUI-Widgets steuern kann, gezielt `blocked`, nicht per Annahme `pass`. |

## Nicht in Etappe 2 hineinschieben

- **GE-02/03/04/05/11, BAL-01:** brauchen validierte Elternfelder, Wiederholungs-/Tick-Harness und getrennte statistische Bewertung; die vorhandene Einzelpflanzenprobe reicht nicht.
- **GE-08/09/10/12, ER-06/09/10:** erst mit geprüften Spezial-NBT-/Datapack-Fixtures in Wegwerf-Weltkopie.
- **UI-11/12/13:** JEI/Jade/Identify jeweils mit eigenem bestätigtem Modprofil; den alten clientseitigen Core-only-Lauf nicht als Kompatibilitätsprüfung ausgeben.
- **Multiplayer/MP-Fälle:** separate Nutzerfreigabe erforderlich. Keine zweite Session oder erfundene Spielersimulation starten.
- **Original-Analyzer-GUI:** ausdrücklich auf nach vollständigem Testgate/Beta-Entscheid verschoben.

## Codex-Abschlussbericht pro Fall

`{id, testedCommit, jarSha256, worldCopy, profile, startUtc, endUtc, actions, observedItemCounts, serverProof, screenshots, status, failureOrBlocker, rollback}`

Mindestens einmal vor/nach jeder Serie die aktive Testwelt und Modliste festhalten; bei flüchtigem Override den Resetwert belegen. **Keine automatischen P2/P3-Codefixes**. P0/P1 nur bei eindeutig reproduziertem kritischem Defekt und gemäß `docs/CODEX_TEST_TRIAGE_POLICY_2026-11-22.md`, sonst Logs im Chat zur gemeinsamen Reparatur.

**Ausführungsfreigabe:** Dieses Dokument autorisiert keinen neuen Lauf. Lokaler Codex holt für Etappe 2 vor dem Start die ausdrückliche Freigabe ein.
