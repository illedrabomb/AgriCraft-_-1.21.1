# Automatisierter AgriCraft-Core-Spieltest – 22.11.2026

Geprüfter Commit: `a2fe5625caf17233efa32be691053bce5f561e36`, Branch `integration/neoforge-1211-consolidation`. Nutzer ausdrücklich abwesend mit Freigabe für Core-Start und automatisierbare Gameplay-Fälle. **Keine vollständige v8-Abnahme. Keine Mod-/Testquellen geändert.**

## Nachgewiesen

- Dedicated Core-Server ohne JEI/Add-ons gestartet. Echter NeoForge-Client ohne JEI mit vorhandener MCPFabric-Steuerung verbunden. Versionen und Artefaktzuordnung in manifest.json.
- Neue isolierte Welt; Stickplatzierung, Kreuzlage, konkrete CROSS-Pflanzablehnung und Itemmengen beobachtet. Wheat und carrot 1/1/1 wachsen per Knochenmehl von Stage 0 auf 7. Keine Abnahme natürlicher Wachstumsgeschwindigkeit.
- Ernte Stage 7->4, erneuter Klick und Klick mit Stick erzeugen keinen zusätzlichen Weizendrop. Survival-Abbau gibt einen genetischen Seed mit original_seed und gleichen 1/1/1-Werten; Analyzer bestätigt diese Werte.
- Wheat 7/4/9, analysierter Analyzer-Seed 1/1/1 und Inventarkomponenten nach save-all flush, regulärem stop, ZIP-Sicherung, Neustart, erneutem Beitritt und reload unverändert.
- Kürbis und Melone je zwei interne Fruchtzyklen, nach Ernte weiter Stage 7. Randomticks im isolierten Stand zweimal für 15 Sekunden auf 512 beschleunigt, danach auf 0 zurückgestellt. Keine Balance-/Zeitmessabnahme.
- Finale Serverläufe regulär gespeichert/gestoppt, Client beendet. Kein bestätigter P0/P1-Defekt, kein Codefix.

## v8-Abdeckung

Alle 86 IDs einzeln in [v8-coverage.json](v8-coverage.json): **6 pass, 2 prior_pass, 8 partial, 1 inconclusive, 36 blocked, 6 not_run, 27 not_authorized**. Nur die sechs pass sind aktuelle vollständig bewertete Gameplay-/Vorbereitungsfälle: TG-01, TG-05, GE-07, ER-01, ER-02, ER-04. Historische TG-02/03 beziehen sich auf den vorangegangenen 212/212-JUnit-/Buildlauf und sind kein neuer JUnit-Lauf.

Multiplayer-/Zuschauerfälle ausdrücklich nicht freigegeben. Spezial-Fixtures, Statistik-/Zuchtharness und optionale UI-Profile fehlen. ER-03/08/12/13/14 und UI-14 noch nicht vollständig durchgeführt; sie sind not_run, kein behaupteter Umgebungsfehler. Teilprüfungen bedeuten keinen vollständigen Pass. NBT-Testfelder aus anderem Chat nicht parallel verändert.

## Offener Befund und Grenzen

- **UI-09, P2-Verdacht, kein bestätigter Modbug:** Journal-Direkteinlage per Steuerungs-API nicht erreicht. Handjournal bleibt erhalten, Analyzer-Journalslot bleibt leer. Sneak-/Interaktionsweg mit normalen Spielereingaben unabhängig prüfen. Keine Reparatur vorgenommen. Der letzte Seed-Belegtversuch verwendete irrtümlich Hotbar 1 mit Melonenscheiben statt des neuen Seeds in Slot 8; dieser Versuch zählt nicht als Seedtest.
- Windows-Fensteraufnahme zeigte Weiß; echte Framebuffer-PNGs über die vorhandene Spielmod lieferten nutzbare Belege. Keine vollständige Tastatur-/Tooltip-/Menü-Abnahme. Chatüberlagerungen schränken Modellabnahme ein; GE-01 deshalb partial.
- Clientseitiges world.getBlock auf Remote-Server liefert no_server: Steuerungsgrenze, kein AgriCraft-Fehler. Serverseitige Nachweise stattdessen mit autorisierten `/data get` und `/execute if block`. Clientvorhersage SUCCESS allein wird nicht als Pass gewertet.
- **Harness-Anläufe:** Erster Start benutzte versehentlich einen neuen relativen Testpfad und Default-Port 25565. Ohne Gameplay gestoppt. Korrigierter absoluter Pfad nutzt ausschließlich 127.0.0.1:25589. Ein weiterer Vorbereitungsneustart zum Setzen des isolierten Dev-Operators erfolgte vor Fixtures per Prozessende. Connection reset/refused und Negative-index-Crashreporthandler-Eintrag im Clientlog gehören zu diesen Neustart-/zu-früh-Beitrittversuchen. Kein Clientprozessabsturz; spätere Beitritte erfolgreich. Erst finale Persistenzläufe regulär gespeichert/gestoppt und gewertet.
- Gradle-Dev-Runs laden Klassen statt einer gepackten Produktionsinstallation. Alle 149 Klassen im JAR wurden byteweise mit geladenen Hauptklassen verglichen: keine Unterschiede. JAR-Hash dokumentiert. Produktionsinstallation bleibt separat offen.

## Umgebung und Belege

Server: nur cropgenetics 0.2.14-alpha, Minecraft 1.21.1, NeoForge 21.1.265. Client zusätzlich MCPFabric 0.8.0+1.21.1 aus vorhandenem geprüftem Downloadordner. Keine Neuinstallation und kein Download. Mineflayer nicht installiert: [Projektbetreuer dokumentieren fehlenden allgemeinen Modsupport](https://github.com/PrismarineJS/mineflayer/issues/2974). Authentifizierte Steuerungsbrücke ausschließlich Loopback 25599; Core-Server Loopback 25589. Beide beendet.

[evidence/](evidence/) enthält komplette Launcherlogs, RPC-Transkript, Ergebnisprotokolle und PNGs. client-logs/server-logs enthalten Minecraft-Logs, harness/ die verwendeten Ablaufhelfer. Konfiguration mit Auth-Token und Weltarchive werden nicht ins Repository aufgenommen. Ursprüngliche Saves/Mods/Konfigurationen nicht verändert. Testinstanz bleibt lokal unter `D:\guthub repositorys\AgriCraft-core-gate-2026-11-22\work\gameplay-20261122`; ungenutzter erster Testpfad unter neoforge-port/work bleibt als Diagnose erhalten.

Nächster Schritt: UI-09 unabhängig reproduzieren und fehlende v8-Abläufe/Spezial-Fixtures vorbereiten. Keine weiteren Starts automatisch freigegeben.
