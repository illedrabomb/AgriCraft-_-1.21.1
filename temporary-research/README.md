# Temporäre Entwicklungsrecherche – Handoff
**Status:** Forschungsnotizen; darf nach Übernahme in dauerhafte Dokumentation gelöscht werden.
**Stand:** 2026-11-15
**Ziel:** Neue/modernisierte AgriCraft-basierte Pflanzenzucht für Minecraft 1.21.1 und NeoForge 21.1.265, Java 21.

> Für jeden neuen Chat / jede andere KI: zuerst `docs/NEOFORGE_1.21.1_PLAN.md` und `temporary-research/FINDINGS.md` lesen. Nicht annehmen, dass ein Port bereits kompiliert.

## Wichtig
- Repo ist ein Fork von https://github.com/AgriCraft/AgriCraft – MIT-Lizenz und ursprüngliche Urheber ausdrücklich anerkennen und Lizenzhinweise bewahren.
- Master enthält derzeit alten **Forge 1.18.2**-Code, nicht bereits ein funktionsfähiges NeoForge-Projekt.
- Kein Build/Integrationstest auf NeoForge 21.1.265 durchgeführt.
- Der bestehende Code ist **Referenz für Algorithmen und Daten**, nicht ohne Anpassungen lauffähig.
- Dokumentierte Entscheidungen aus dem Gespräch: Kreuzungen, 1–10-Genetik (Ziel 10/10/10), breitflächige Modpack-Samenunterstützung, Dorf-Züchterhaus und Villager-Handel, Samenanalyse/Saatgutlagerung. Bewässerung (Tank, Kanäle, Sprinkler) und Gewächshaus als **separate optionale Addons**, Weeds nicht als Kernfeature.
- Empfohlene hybride Samenarchitektur ist ein Vorschlag, noch keine endgültig technisch geprüfte Entscheidung.

## Arbeitsablauf
1. Prüfe Originalcode/Fundstellen und baue Tests für die Genetikregeln.
2. Halte Architektur, bestätigte Entscheidungen und offene Fragen in `docs/` fest.
3. Aktualisiere diesen Ordner bei jeder Recherche; lösche ihn erst, wenn alle wichtigen Ergebnisse in der dauerhaften Dokumentation sind.
4. Keine undokumentierten Änderungen an altem Master-Code; arbeite über Branches/PRs.
