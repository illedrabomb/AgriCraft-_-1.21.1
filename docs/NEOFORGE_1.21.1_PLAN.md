# NeoForge 1.21.1 – Projektplan und Herkunft

Stand: 2026-11-15. Status: Planung, noch keine Portierung oder Builds nachgewiesen.

## Herkunft und Lizenz
Dieses Repository ist ein Fork von [AgriCraft/AgriCraft](https://github.com/AgriCraft/AgriCraft), ursprünglich von InfinityRaider und weiteren Mitwirkenden entwickelt. Die Grundlage ist der Minecraft-1.18.2-Forge-Quellcode (AgriCraft 3.1.6). Ziel ist eine modernisierte, erweiterte Version für Minecraft **1.21.1** und **NeoForge 21.1.265**. Die ursprünglichen Mitwirkenden und die MIT-Lizenz müssen in README, Credits und bei übernommenen Code-/Assetteilen anerkannt werden. Bestehende Copyright- und Lizenzhinweise bleiben erhalten. Eine neue Implementierung darf ihre Herkunft nicht verschleiern.

## Beschlossener Funktionsumfang (Hauptmod)
- Crop Sticks und doppelte Stäbe zum Kreuzen
- Drei genetische Werte 1–10: Wachstum, Ertrag, Widerstandsfähigkeit; Ziel 10/10/10
- Genetische Vererbung, Verbesserungen und Pflanzenmutationen
- Vanilla-Pflanzen und möglichst breite Kompatibilität mit Pflanzen/Samen anderer Mods; automatisch erkannte Standardpflanzen plus Adapter für Spezialfälle
- Datapack-basierte Pflanzendefinitionen und Integrationsregeln
- Analyse von Saatgut und Anzeige der Genetik, Saatgutaufbewahrung
- Dorf-Züchterhaus als Saatgutquelle, Pflanzenzüchter-Villager und Saatguthandel
- Vereinfachte Bodenbedingungen; bestehende Spezialanforderungen von Mod-Pflanzen respektieren
- Multiplayer-Unterstützung und konfigurierbare Balance

**Vorgeschlagen:** Hybrid-Saatgutsystem: normale Samen bleiben verwendbar und können in genetische Zuchtsamen für die Crop Sticks überführt werden. Noch technisch zu validieren (insbesondere Crop-Block- und Ernteverhalten).

## Separate, optional installierbare Addons (nicht Teil der Hauptmod)
- Bewässerung: Wasserspeicher/Tank, Kanäle und Sprinkler
- Erweitertes Gewächshaussystem
- Unkraut (Weeds) gehört nicht zum Hauptumfang; nur bei künftigem Bedarf als optionales Addon

## Wiederverwendung versus Neubau
**Primär wiederverwenden oder portieren:** Genetik-/Vererbungsregeln, Kreuzungs- und Mutationslogik, Datendefinitionen, passende Texturen/Modelle, Crop-Stick-Mechaniken, Saatgutkonzepte, ggf. Weltstrukturen und Lootdaten. Jede Komponente wird auf API-/Versionskompatibilität geprüft.

**Neu schreiben beziehungsweise grundlegend anpassen:** NeoForge-Registrierung und Mod-Einstieg, Netzwerkprotokolle, Datenspeicherung (Data Components), Menüs/Rendering wo API-gebunden, die ehemalige InfinityLib-Infrastruktur, plattformgerechter Integrationsmanager, Adapter und Tests.

Das bisherige Repository hat ca. 375 Java-Dateien unter `src/main/java`, verweist in `build.gradle` auf Forge `1.18.2-40.2.4`, ForgeGradle 5.1, Java 17 sowie die Submodule AgriCore und InfinityLib. Ein bloßes Anheben der Versionsnummer genügt nicht. Es gibt **keine verifizierte Prozentzahl** des direkt kompilierbar wiederverwendbaren Codes. Die vorhandenen Algorithmen, Daten und Assets senken dennoch den Entwicklungsaufwand.

## Entwicklungsphasen
1. Inventar der Core-Algorithmen, Assets, Abhängigkeiten und Lizenzen; automatisierte Referenztests auf Basis des alten Codes erstellen.
2. Sauberes NeoForge-21.1.265-Projekt für Java 21, mit getrenntem modularen Core und Adapter-Schnittstellen.
3. Genetische Saatgutdaten, Crop Sticks, Wachstum, Vererbung und 10/10/10 für Vanilla-Pflanzen.
4. Mutation, Pflanzendatensätze, Datapacks und kompatibler Standard-Pflanzenscanner; Spezialadapter pro Mod.
5. Züchterhaus, Villager-Handel, Analyse, Aufbewahrung, UI und Multiplayer.
6. Spieltests (Client/Server), Balancing, Save-Kompatibilität, CI und Release-Build.
7. Bewässerungs- und Gewächshaus-Addons unabhängig entwickeln.

## Integrationsprinzip
Keine Zusicherung, dass *alle* Mod-Samen ohne Anpassungen kompatibel sind: eigene Pflanzenblöcke, Sonderwachstum, Custom Harvest oder nichtstandardisierte Samen verlangen Adapter und echte Modpack-Tests. Ziel ist maximale Abdeckung ohne bestehende Ernte-/Bodenregeln anderer Mods zu beschädigen.

## Dokumentationspflege
Feature-Änderungen in dieser Datei und bei Bedarf in `README.md` / `docs/CREDITS.md` nachvollziehbar festhalten. Für Entscheidungen separate Einträge mit Datum, Motivation und Auswirkungen ergänzen. Die Dokumentation wird mit den jeweiligen Codeänderungen aktualisiert; dies geschieht nicht automatisch zwischen Unterhaltungen.
