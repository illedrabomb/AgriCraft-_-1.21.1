# AgriCraft NeoForge 1.21.1 – Projektstatus und Übergabe

**Stand:** 2026-11-16  
**Aktiver Entwicklungsbranch:** `feature/neoforge-1211-foundation`  
**Codebasis:** `neoforge-port/` (nicht das alte Forge-1.18.2-Rootprojekt)  
**Zielplattform:** Minecraft 1.21.1, NeoForge 21.1.265, Java 21  
**Aktuelle Mod-ID:** `cropgenetics` (vorläufig)  
**Status:** fortgeschrittene Quellcode-Implementierung; **kein validierter spielbarer Release**.

## Regeln für die laufende Dokumentation

1. Nach **jedem abgeschlossenen Entwicklungs-/Änderungsschritt** diese Datei mit Datum, vorgenommenen Änderungen, betroffenen Dateien, offenen Punkten und konkretem nächsten Schritt aktualisieren. Größere Entscheidungen zusätzlich in dauerhaften technischen Dokumenten festhalten.
2. **Implementiert**, **statisch geprüft**, **kompiliert**, **getestet** und **im Spiel bestätigt** konsequent unterscheiden; keine ungeprüften Erfolgsmeldungen.
3. `temporary-research/` ist als vorübergehender Forschungs-/Übergabeordner gedacht. Erkenntnisse vor dessen Löschung in dauerhaft gepflegte Projektunterlagen übernehmen. Den Ordner nicht ohne entsprechende Übernahme löschen.
4. Nutzerentscheidung aus dem vorherigen Chat: **keine Builds, Tests, Minecraft-Starts oder GitHub-Actions-Läufe ohne erneute Freigabe**; unnötige Prototypen vermeiden. Vor einem Release sind Tests dennoch zwingend erforderlich.
5. Folgende Funktionen nicht vergessen: ein späteres **MineColonies-Addon**; mögliche weitere optionale Addons für Bewässerung und Gewächshäuser. Lizenz-/Autorenhinweise aus AgriCraft, AgriCore und verwendeten Assets respektieren.
6. Bei Chatwechsel zuerst **diese Datei**, `neoforge-port/README.md`, die jüngsten Commits sowie relevante Dokumente unter `temporary-research/` lesen und gegen den aktuellen Branch abgleichen. Nicht aus alten Chat-Zusammenfassungen auf erfolgreiche Builds schließen.
7. Die Anweisung, immer zu dokumentieren, ist eine **Arbeitskonvention**, keine automatische Hintergrundaufgabe: Änderungen am Repository werden jeweils während der aktiven Bearbeitung durchgeführt und der Commit wird genannt.

## Aktuell vorhandene Quellcode-Funktionen

- Genom mit dominanten/rezessiven Allelen sowie Growth, Gain, Strength und interner Fertility/Mutativity.
- Genetisches Saatgut mit Data Components, gespeicherter ursprünglicher Saatgut-ID und Analysezustand.
- Crop Sticks / doppelte Crop Sticks: Pflanzung, Random-Tick-Wachstum, Ernte, Kreuzung, Vererbung und Mutationsauswahl.
- Rückwandlung genetischer Samen in ursprüngliche Vanilla-/Mod-Samen über dynamisches Crafting mit wiederverwendbarem Papier; Analyzer für Samen.
- Pflanzen-/Mutations-Datapacks mit gemeinsamem atomarem Reload-Snapshot, Diagnoseberichten, Registry-Referenzprüfung, Limits und Operator-Kommando `/cropgenetics data`.
- Datengesteuerte Client-Pflanzenvisualisierung mit BlockEntityRenderer, Wachstumsstufen und Fallbacks; Client-Resource-Packs werden **nicht automatisch** durch Server-Datapacks synchronisiert.
- Server-Pflanzen für Weizen, Karotten, Kartoffeln, Rote Bete, Netherwarzen, Melonen, Kürbisse, Kornblumen, Mohn, Löwenzahn, Kakteen und Zuckerrohr. Letztere Spezialpflanzen bilden nicht alle Vanilla-Sonderregeln ab.
- Physische Melonen-/Kürbisfrüchte mit Besitz-/Elternzuordnung und Vanilla-Loot-Delegation.
- Jüngste Sicherheitsverbesserungen (2026-11-15): periodische Stage-Reconciliation bereits geladener Pflanzen nach Datapack-Reload, alterskonsistente Rechtsklick-Ernte, Bodenfilter für Mutationen, deterministische Behandlung konkurrierender visueller Arten und sichere Bindung physischer Früchte an benachbarte Stängel.

**Quellen:** `neoforge-port/README.md`, `temporary-research/CORE_GAMEPLAY_FIVE_WORKSTREAMS_2026_11_15.md`, Commits bis `fe111987fb50759aa9607fc5b68f79430c124441` (Zeitpunkt der Bestandsaufnahme). Diese Zusammenfassung ist kein vollständiges Codeaudit.

## Verifizierungsstatus und Einschränkungen

- Frühere Grundlagen-Kompilierung mit Java 21/NeoForge gelang laut Projekt-README auf GitHub Actions. Ein Test-Compile scheiterte später zunächst an fehlendem Gson im Test-Classpath; die Abhängigkeit wurde danach ergänzt.
- Spätere Änderungen an Genetik, Gameplay und Rendering sowie die fünf Arbeitsbereiche vom 15.11.2026 wurden **nicht neu kompiliert oder im Spiel getestet**.
- GitHub-Actions-Workflow ist nach Nutzerwunsch auf **manuellen Start** gestellt.
- Leistung des BlockEntity-Tickers bei großen Farmen, Clientdarstellung, Client/Server-Kompatibilität, Atlas-Sprites sowie Fortune/Silk-Touch-Verhalten der physischen Früchte bleiben unbestätigt.
- Der offene Draft-PR #2 enthält die aktive Entwicklung, während die Master-README noch den alten Forge-Stand zeigt. Nie die Root-`build.gradle` mit `neoforge-port/build.gradle` verwechseln.

## Offene Arbeiten, vorgeschlagene Priorität

1. **AgriPlants-Datenmigration:** `tools/migrate_agriplants.py` existiert, wurde laut README aber nicht ausgeführt. Vorhandene Definitionen, Mutationen, besondere Drops, Boden-/Klima-Prädikate, Sonderpflanzen sowie Referenzvalidierung vervollständigen. AgriPlants muss nicht zwingend als separate Laufzeit-Mod portiert werden; die alten Daten und Regeln benötigen stattdessen eine geprüfte Migration/Adapter.
2. **Gameplay-Abdeckung:** vererbte Eigenschaften, Zucht, Ertrag, Umweltregeln und Sonderfälle systematisch vervollständigen, ohne vorzeitige Testfreigaben zu unterstellen.
3. **Bedienung/Genetik-UI:** Analyzer-/Inspektions- und mögliche Aufbewahrungsoberflächen erweitern.
4. **Pflanzenzüchter-Villager („Breeder Villager“):** bislang **nur geplant**, nicht als implementiert darstellen. Gemeint ist ein eigener pflanzenzüchtender Dorfbewohnerberuf, **nicht** Villager-Vermehrung. Mögliche Funktionen: Handel mit genetischem Saatgut, Analyse oder aktive Pflanzenkreuzung. Umfang noch nicht festgelegt. Zuchtlogik wiederverwendbar für künftiges MineColonies-Addon entwickeln.
5. **Integrationen/Addons:** optionale Bewässerung, Gewächshaus und vor allem später MineColonies; möglichst über klar abgegrenzte Schnittstellen.
6. **Validierung vor Release:** Build, Client, Dedicated Server, Multiplayer, Welt-/Datapack-Migration, Rendering, Loot, Performance und Gameplay. Erst nach Zustimmung des Nutzers starten.

## Nächster konkreter Entwicklungsschritt

Die vorhandene `tools/migrate_agriplants.py` und bereits migrierte Pflanzen gegen den Legacy-AgriPlants-Katalog **statisch** prüfen, eine Matrix „vorhanden / teilweise / fehlt“ erstellen und die fehlenden Definitionen und Adapter schrittweise implementieren. Ergebnisse und neue Commits hier protokollieren. Keine Builds/Tests automatisch starten.

## Übergabe beim nächsten Chat

> Wir arbeiten an `illedrabomb/AgriCraft-_-1.21.1`, Branch `feature/neoforge-1211-foundation`, neuer NeoForge-Code in `neoforge-port/`. Bitte zuerst `docs/PROJECT_STATUS.md`, `neoforge-port/README.md`, jüngste Commits und relevante `temporary-research/`-Dokumente lesen. Den bestätigten Ist-Stand von ungeprüften Implementierungen trennen. Nach **jedem Entwicklungsschritt** die Projektstatus-Datei aktualisieren, Änderungen committen und konkrete nächste Aufgaben aufführen. Keine Tests/Builds ohne Freigabe. MineColonies-Addon für später einplanen.

## Änderungsprotokoll

- **2026-11-16:** Dauerhafte Status-/Übergabedatei erstellt; Stand aus Branch-README, Forschungsdokument vom 15.11. und GitHub-Commits zusammengetragen; Begriff „Breeder Villager“ als geplanter Pflanzenzüchter klargestellt; Dokumentationskonvention und Weitergabe an neue Chats aufgenommen. **Keine Codeänderung und keine Testläufe.**
