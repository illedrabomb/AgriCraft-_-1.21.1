# Schutz bei HarvestMode-Wechsel durch Datapack-Reload

## Aktualisierung: lokale Konsolidierung 16.11.2026

Die Integration behaelt diese Eigentums-Migration und kombiniert sie mit dem strengeren STANDARD-Ernteguard aus entwicklung (keine Frucht-, Richtungs- oder Token-Metadaten). Das fruehere pauschale Verbot eines Moduswechsels wird damit ersetzt. Ein begrenzter Dedicated-Test bestaetigt Fruchteigentum nach STANDARD-Reload, unterdrueckten zusaetzlichen Stamm-Loot sowie Regrowth nach Entfernung der eigenen Frucht. Aktives Test-Datapack durch anschliessenden STANDARD-Loot bestaetigt. Vollstaendige Chunkgrenzen-/Gameplay-Abnahme steht aus. Details: [Integrationsbericht](../docs/INTEGRATION_REPORT_2026-11-16.md). Die folgenden Nicht-Test-Aussagen beziehen sich auf die damalige Feature-Implementierung vor dieser Konsolidierung.


Datum: 2026-11-16. Branch `feature/neoforge-1211-foundation`.

## Problem

Eine Pflanze mit `GOURD`-Erntemodus kann bereits einen echten, getrennten Fruchtblock besitzen, während ein Datapack-Reload die gleiche Pflanzenart auf `STANDARD` umstellt. Vorher war `reconcilePhysicalFruit` an den aktuellen `GOURD`-Modus und Reife gebunden. Zugleich ließ `readyForHarvest` jeden reifen `STANDARD`-Stängel unabhängig von `fruitReady` abernten. Daraus ergab sich eine mögliche doppelte Ernte: Fruchtblockdrops und zusätzliche Standard-Stängeldrops.

## Änderungen

- `GourdFruits.fruitBlock(plant)` verlangt weiterhin `GOURD` für **neue** physische Früchte.
- `GourdFruits.matchesOwned(..., speciesId, token)` erkennt eine **existierende** Melonen-/Kürbisfrucht dagegen anhand ihrer Pflanzenart, Position und des gemeinsamen Eigentums-Tokens unabhängig vom aktuellen Erntemodus. Das erzeugt keine Früchte und verändert deren Loot nicht.
- `CropSticksBlockEntity.readyForHarvest` verweigert `STANDARD`-Stängeldrops, solange `fruitReady` gesetzt ist.
- `reconcilePhysicalFruit` funktioniert nun auch nach Wechsel auf `STANDARD` und auch bei nicht reifem Stängel. Bei geladenem Nachbarchunk mit weiterhin zugeordneter Frucht bleibt die Zuordnung erhalten; bei entferntem oder fremdem Fruchtblock wird die alte Zuordnung gelöscht und die Regrowth-Phase gemäß der **aktuellen** Pflanzendefinition gestartet.
- `onOwnedFruitRemoved` reagiert auch bei `STANDARD` auf die Entfernung der exakt eigenen Frucht (Position und Token müssen passen).
- Der periodische BlockEntity-Ticker kontrolliert `fruitReady` weiterhin unabhängig von Random Ticks. Das vermeidet dauerhafte Erntesperren nach Entfernung der Frucht bei `randomTickSpeed=0`. Der Random-Tick-Pfad beachtet ebenfalls beide Erntemodi.

## Sicherheitsgrenzen

- Bei **nicht geladenem** Nachbarchunk wird die Fruchtzuordnung nicht entfernt. Währenddessen bleibt die normale Stängelernte gesperrt; das ist absichtlich konservativ, weil ein Entfernen der Zuordnung zu doppeltem Loot führen könnte.
- Bei deaktivierter/fehlender Pflanzenart greift die Pflege erst wieder, wenn die Art erneut im aktiven Registry-Snapshot verfügbar ist.
- Das Genom, das Eigentums-Token einer vorhandenen physischen Frucht, Saatgutherkunft und Analysemarkierung werden nicht überschrieben.
- Ein Wechsel von `STANDARD` zu `GOURD` erlaubt nach Reife die normale physische Fruchterzeugung und sperrt Standard-Stängelernte.
- Bei einem `STANDARD`-Stängel, der nach /reload noch eine physische Frucht hält, bleibt der Stängel gesperrt, bis diese Frucht entfernt oder als nicht mehr vorhanden bestätigt wurde.

## Offen

- Das Verhalten von bereits zerstörten Stängeln, deren Frucht im benachbarten geladenen/ungeladenen Chunk verbleibt, ist separat zu prüfen. Die Frucht behält bei ihrer Zerstörung die normalen Weltblock-Drops.
- Es wurde **kein Build, kein Test, kein CI und kein Minecraft-Start** durchgeführt. Insbesondere Chunkgrenzen, Reloads und BlockEntity-Ticker bedürfen später einer praktischen Prüfung.
