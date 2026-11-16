# Datapack-Reload: Konflikte und transaktionale Veröffentlichung
Datum: 2026-11-15 | Branch: `feature/neoforge-1211-foundation`

**Nur statische Prüfung und GitHub-Änderungen. Kein Build, kein Test, keine CI-Ausführung oder Spielstart.**

## Gefundene Probleme und Korrekturen

### Kandidaten vor vollständiger Prüfung in den Pflanzenkatalog eingefügt
Vorher setzte der `CropDataReloadListener` eine Pflanze bereits in die lokale Candidate-Map, bevor die optionalen Boden-Tag-Diagnosen vollständig berechnet waren. Eine unerwartete Exception in der Tag-Diagnose konnte anschließend einen `INVALID`-Eintrag erzeugen, während die betroffene Pflanze im veröffentlichten Snapshot **doch** aktiv blieb.

**Korrektur:** Registry-Referenzen und Tag-Diagnosen werden vollständig berechnet, bevor die lokale Pflanzenmap verändert wird. Fehlerhafte Definitionen werden nicht übernommen.

### Doppelte Pflanzen-IDs erst nach Inhaltsvalidierung erkannt
Vorher griff die Duplikat-Erkennung nur bei zwei bereits vollständig validierten Pflanzen. Ein zweiter Datensatz mit derselben `id`, aber einer fehlerhaften Registrierungsreferenz, konnte als `INVALID` übersprungen werden, während die erste Definition aktiv blieb. Das ließ einen widersprüchlichen Pflanzenanspruch unbemerkt zu.

**Korrektur:** Eine separate Vorprüfung sammelt alle Ressourcen, die aktiviert sind, deren `mods`-Abhängigkeiten erfüllt sind und deren `id`-Metadaten gelesen werden können. Beanspruchen mehrere Ressourcen dieselbe Pflanzen-ID, werden sie alle als `CONFLICT` gekennzeichnet und die Art wird deaktiviert. Die Meldung nennt die beteiligten ResourceLocations. Erst danach erfolgen die detaillierte JSON-/Registry- und Tag-Validierung.

**Grenze:** Ein vollständig unlesbares JSON, fehlende oder nicht lesbare `id` und deaktivierte oder wegen fehlender Mod übersprungene Dateien gelten nicht als ID-Anspruch. Solche Dateien erhalten ihren jeweiligen `INVALID`, `DISABLED` oder `MISSING_MOD`-Eintrag. Ein identischer Ressourcenpfad wird bereits durch Minecrafts Resource-Pack-Priorität auf einen sichtbaren Eintrag reduziert.

### Registry warf Mutationen still aus dem Snapshot
`AgriPlantsRegistry.replaceAll` filterte Mutationseinträge mit nicht existierenden Pflanzen ein zweites Mal. Damit hätte ein Diagnosebericht `LOADED` für eine Mutation anzeigen können, obwohl die Registry sie aus dem publizierten Snapshot entfernt.

**Korrektur:** `replaceAll` erstellt immutable Kopien der Eingaben und validiert die Referenzintegrität aller Mutationen **ohne stille Filterung**. Bei einer Inkonsistenz wirft die Methode eine `IllegalArgumentException`, **bevor** die neue `Snapshot`-Referenz gesetzt wird. Der vorherige vollständige Snapshot bleibt damit gültig. Unter normalen Bedingungen filtert der gemeinsame Reload-Listener fehlerhafte Mutationen bereits vor dem Aufruf.

## Garantierter Rahmen

- Die beiden Datapack-Maps und der Diagnosebericht werden als unveränderliche Objekte über eine `volatile`-Snapshot-Referenz gemeinsam veröffentlicht.
- Vorher berechnete Kandidaten bleiben lokal und ersetzen den aktiven Snapshot erst nach Abschluss der Integritätsprüfung.
- `CrossBreeding` erfasst weiterhin einen Snapshot zu Beginn eines Züchtungsversuchs.
- Fehler einzelner Datendateien werden weiterhin im Report gesammelt und führen zu deren Überspringen.
- Schwerwiegende unerwartete Fehler während `apply` können den Reload abbrechen. Dieser Code garantiert nur, dass **seine** Registry-Referenz in diesem Fall nicht teilweise ausgetauscht wird; er garantiert keine Transaktion über den gesamten Minecraft-Datapack-Reload.
- **Unverifiziert:** Java-/NeoForge-Kompilierung sowie Verhalten mit modded Registries, Server-/Client-Reload und verschiedenen Fehlerdateien. Es wurden absichtlich keine Builds/Tests gestartet.

## Nächster Schritt

Vor weiterem AgriPlants-Massenimport sind statische Quellcodeprüfungen der übrigen Reload- und Registry-Aufrufe sinnvoll. Bei ausdrücklicher Freigabe: Build/Tests und Dedicated-Server-Datapack-Szenarien mit Duplikaten und ungültigen Dateien.
