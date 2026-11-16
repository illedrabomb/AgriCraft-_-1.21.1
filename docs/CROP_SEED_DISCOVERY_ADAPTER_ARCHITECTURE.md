# AgriCraft NeoForge 1.21.1 – erweiterbare Saatgut-Erkennung

**Stand:** 16.11.2026 · **Branch:** `feature/neoforge-1211-foundation`  
**Status:** Source-only-Refactoring; **nicht kompiliert, getestet oder im Spiel validiert**.

## Entscheidung und Umfang

Die bestehende Pam's HarvestCraft 2 **Crops**-Erkennung verwendet jetzt einen allgemeinen, aber **nicht global eingeschalteten** Erkennungskern:

- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/RegisteredSeedCropDiscovery.java`: allgemein wiederverwendbare, begrenzt konfigurierbare Erkennung konkreter registrierter Saatgut-/Erntepaare.
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/PamCropAutoDiscovery.java`: **einziger aktuell verdrahteter Adapter**, für `pamhc2crops`.
- `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/data/PamAutoDiscoverySettings.java`: spezifische und unveränderte Server-Datapack-Policy für Pam's Crops; ein Teil ihrer Werte wird in die gemeinsame Engine-Policy übersetzt.
- `CropDataReloadListener.java` ruft weiterhin ausschließlich `PamCropAutoDiscovery.addMissing(...)` auf, **nur wenn `pamhc2crops` geladen ist**. Der Reload, seine Diagnose und das atomare `AgriPlantsRegistry`-Snapshot bleiben strukturell unverändert.

Es wurde **keine weitere Mod freigeschaltet**, keine allgemeine `seed`-Namenssuche über alle Namespaces eingerichtet, kein Fremdmod-Item ohne belegten Namespace übernommen und keine Baumgenetik eingeführt.

## Vertrag der gemeinsamen Erkennung

`RegisteredSeedCropDiscovery.Policy` verlangt ausdrücklich einen geprüften **Mod-Namespace**, ein konkret festgelegtes **Seed-Suffix**, ein **Ernte-Suffix**, ein **Species-Suffix**, einen eindeutigen **virtuellen Diagnosepfad**, eine maximale Anzahl Auto-Arten und begrenzte Fallback-Wachstumswerte. Ein separater Adapter erstellt die `Policy` nach Abgleich mit seiner Mod.

Das derzeit aktive Pam's-Profil setzt:

| Feld | Wert |
|---|---|
| Mod/Item-Namespace | `pamhc2crops` |
| Saatgut-Suffix | `seeditem` |
| Ernte-Suffix | `item` |
| Pflanzen-ID-Suffix | `_plant` |
| Diagnosepfad | `cropgenetics:auto_pam/<crop>.json` |
| Server-Datapack | `data/cropgenetics/cropgenetics/pam_auto_discovery.json` |

Die Engine validiert ausdrücklich konfigurierte `item_pairs` auf ihre Item-Namespaces, Crop-Namen, Eindeutigkeit und nicht überlappende Seed-/Produce-Rollen. Für Standard-Items wird nur im freigegebenen Namespace gesucht. Beide Items müssen in `BuiltInRegistries.ITEM` tatsächlich registriert und nicht `Items.AIR` sein. Vorher geladene explizite Pflanzen und ihre Seed-/Ernteansprüche haben Vorrang. Mehrdeutige Auto-Kandidaten werden mit `CONFLICT` ausgeschlossen; unregistrierte konfigurierbare Paare mit `AUTO_MISSING_ITEM` gemeldet.

Die Engine erstellt dieselben **generischen** Pflanzen wie zuvor: acht Wachstumsstufen, `HarvestMode.STANDARD`, Vanilla-Farmland und die konfigurierte Pam's-Wachstums-/Erntemenge. **Sie leitet keine Mutationen, Saisons, Bodenchemie, Texturen, Spezialblöcke oder nicht vorhandenen Items ab.** Die originalen 37 Pam's-2-Crop-Datapacks sowie 33 originale Mutationdateien bleiben eigenständig, mit Vorrang vor den Fallbacks.

## Voraussetzungen vor einem neuen Mod-Adapter

1. Eine konkret benannte Mod **und** ihre für Minecraft 1.21.1/NeoForge tatsächlich vorhandene Mod-ID und Item-Registrierungen aus belastbaren Quellen überprüfen.
2. Nachweisen, dass ihre Items **Kulturpflanzensaatgut** und **Ernteprodukte** sind – keine Setzlinge, Baumfrüchte, Werkzeugmaterialien oder bloß ähnlich benannte Gegenstände.
3. Das Namensschema und Ausnahmen explizit konfigurieren; keine Wildcard-Erkennung unbekannter Mods aktivieren.
4. Eigenes Server-Datapack/Policy sowie eindeutige Diagnosepfade für die neue Mod schaffen. Die bestehende `PamDiscoveryStats`-Auswertung zählt **nur** den Pfad `auto_pam/`, und darf nicht stillschweigend die anderen Mods als Pam's ausgeben.
5. Umfassende Reload-Priorität, `enabled=false`, fehlende Items, doppelte Seeds/Produkte, Client-Visualisierung, Mutationsreferenzen und Dedicated-Server-Verhalten **erst nach ausdrücklicher Testfreigabe** abnehmen.

## Ausdrücklich ausgeschlossen

**Pam's HarvestCraft 2 Trees, Setzlinge, Obstbäume und eine Baumzuchtintegration.** Dies ist eine verbindliche Nutzerentscheidung. Ein möglicher, unabhängiger Baumgenetik-Mod wäre ein separates Projekt. HarvestCraft 1 bleibt **nur eine historische Crop-ID-Migrationsquelle**; sein `harvestcraft`-Namespace darf nicht als NeoForge-1.21.1-Laufzeitmod angenommen werden.

## Entwicklungsstatus und nächste Schritte

Die Architektur ist nun auf Wiederverwendung vorbereitet, aber **nur Pam's Crops ist verdrahtet**. Keine Anzahl neuer Laufzeitpflanzen kann ohne einen freigegebenen Server-Reload angegeben werden. Vor einer Ausweitung müssen weitere echte Saatgut-Mods anhand von Registry-Belegen ausgewählt werden und die Diagnostik für Mod-spezifische Pfade verallgemeinert werden.

**Keine Builds, Tests, Skripte, Minecraft-Starts, GitHub Actions, Merges oder Branchwechsel durchgeführt.** `entwicklung`, `testing`, `master` bleiben unberührt. Das spätere MineColonies-Addon bleibt geplant.
