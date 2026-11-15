# Adminbefehl zur Crop-Datapack-Diagnose
Stand: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

## Umgesetzt
Der Server registriert über `net.neoforged.neoforge.event.RegisterCommandsEvent` einen lesenden Brigadier-Befehl `cropgenetics`. Der Handler liegt in `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/command/CropDataCommand.java`; `CropGeneticsMod` registriert ihn auf dem `NeoForge.EVENT_BUS`.

**Berechtigung:** `CommandSourceStack.hasPermission(2)`. Der Befehl ist damit für ausreichend berechtigte Spieler und die Serverkonsole verfügbar; normale Spieler ohne die Berechtigung bekommen ihn nicht angeboten.

### Befehle
| Befehl | Ausgabe |
|---|---|
| `/cropgenetics data` | Zusammenfassung des aktuellen Reports |
| `/cropgenetics data summary` | Dieselbe Zusammenfassung explizit |
| `/cropgenetics data errors` | Erste Seite aller Einträge außer `LOADED` |
| `/cropgenetics data errors 2` | Zweite Seite der Probleme |
| `/cropgenetics data all` | Erste Seite aller Ressourcen inklusive erfolgreich geladener |
| `/cropgenetics data all 3` | Dritte Seite aller Ressourcen |

Die Ausgabe ist auf **acht Zeilen pro Seite** begrenzt. Die Seitenzahlen beginnen bei 1; zu hohe Seitenzahlen melden den gültigen Bereich. Die Detailzeilen werden auf maximal 180 Zeichen gekürzt, damit fehlerhafte JSON-Dateien keine überlange Chat-Ausgabe erzeugen. Ausführliche Probleme verbleiben im Server-Log.

Der Command-Handler erfasst pro Aufruf **einen** `AgriPlantsRegistry.Snapshot`, damit Pflanzenzahl, Mutationszahl und Diagnose aus demselben Datenstand stammen. Er verändert keine Daten, führt keine Mutationen aus und löst keine `/reload`-Aktion aus. Vor dem ersten Report erscheint ein entsprechender Hinweis.

### Nutzung in der Konsole
In der Serverkonsole Befehle wie `cropgenetics data` oder `cropgenetics data errors 2` ohne führenden Slash eingeben. Die Ausgabe erscheint am ausführenden CommandSourceStack und enthält keine sensiblen Weltkoordinaten oder Spielerinformationen.

## API
NeoForge 1.21.1: `RegisterCommandsEvent` wird auf `NeoForge.EVENT_BUS` ausgelöst und stellt `getDispatcher()` bereit; der Command-Handler registriert einen `Commands.literal(...)`-Baum. Als Textausgabe wird `CommandSourceStack.sendSuccess(() -> Component.literal(...), false)` verwendet.
Dokumentation: https://lexxie.dev/neoforge/1.21.1/net/neoforged/neoforge/event/RegisterCommandsEvent.html

## Noch offen
- **Kein Build, keine Tests, kein Spielstart, keine CI-Ausführung** auf Wunsch des Nutzers. Registrierung, Chat-Nachrichten und Berechtigungen sind Quellcode-Änderungen, nicht Ingame-bestätigt.
- Die Befehle arbeiten ausschließlich mit dem letzten **im Arbeitsspeicher gespeicherten** Reload-Report. Ein Server-Neustart erzeugt erst mit dem normalen Datapack-Ladeablauf einen neuen Report.
- Der Command zeigt nur vorhandene Fehler; fehlende Item-/Model-Assets und externe Resource-Pack-Konflikte werden nicht eigens durchsucht.
- Bei einem extrem großen Modpack werden die Report-Einträge im Speicher gehalten, aber die Chat-Ausgabe paginiert.

## Empfohlener nächster Schritt
Die Datapack-Validierung um fehlende Item-Registrierungen, Block-IDs und Client-Asset-Referenzen erweitern. Dann bei ausdrücklicher Testfreigabe den Command auf einem Dedicated Server mit unterschiedlichen Operatorberechtigungen ausprobieren.
