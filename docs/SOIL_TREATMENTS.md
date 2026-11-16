# Bodenpflege mit Wassereimer und Knochenmehl

Stand: 2026-11-16 · Branch: `feature/neoforge-1211-foundation`  
**Status: Java-Quellcode gespeichert, nicht kompiliert, nicht getestet, nicht ingame bestätigt.**

## Ziel und Verhalten

Die bestehende Bodenchemie ist positionsbezogen persistent. Mit `SoilTreatment` wurde nun eine **bewusst einfache, eigenständige NeoForge-Mechanik** implementiert, um zwei Chemiewerte im Spiel zu ändern. Dies behauptet **keine 1:1-Übernahme** historischer AgriCore-Bewässerungs-, Feuchtigkeits- und Nährstoffformeln.

Ein Spieler soll den Crop-Sticks-Block mit einem der folgenden Vanilla-Items benutzen können. Die Handlung bezieht sich auf den **Bodenblock direkt unter den Crop Sticks**:

| Item | Veränderung des Bodenprofils | Maximalwert | Verbrauch bei erfolgreicher Änderung |
|---|---|---|---|
| `minecraft:water_bucket` | Feuchtigkeit um genau eine qualitative Stufe erhöhen | `saturated` | Wassereimer wird im verwendeten Hand-Slot zu leerem Eimer; Creative unverändert |
| `minecraft:bone_meal` | Nährstoffe um genau eine qualitative Stufe erhöhen | `high` | 1 Knochenmehl; Creative unverändert |

Reihenfolge Feuchtigkeit: `arid` → `dry` → `normal` → `wet` → `saturated`.  
Reihenfolge Nährstoffe: `low` → `medium` → `high`.

`SoilTreatment.apply(ServerLevel, BlockPos, Treatment)` verarbeitet ausschließlich geladene, innerhalb der Weltgrenzen liegende Positionen. Es verwendet zunächst einen gespeicherten Positions-Override, sonst ein explizites **statisches Datapack-Profil** des Bodenblocktyps. **Ohne eines dieser Profile wird nichts verändert**: Vanilla-Farmland bekommt nicht ungefragt erfundene Chemiewerte.

Die Operation schreibt bei erfolgreicher tatsächlicher Änderung über `SoilOverrideSavedData.put` einen neuen persistenten positionsbezogenen Override. Säuregehalt und alle nicht betroffenen Eigenschaften bleiben erhalten. Ist der Wert bereits auf Maximum oder wurde das Speicherlimit (65.536 Overrides pro Dimension) erreicht, wird keine Änderung gespeichert und das Item soll nicht verbraucht werden.

## Technische Integration

- Neue Klasse: `neoforge-port/src/main/java/dev/illedrabomb/cropgenetics/content/SoilTreatment.java`
- `CropSticksBlock.useItemOn` erkennt `Items.WATER_BUCKET` und `Items.BONE_MEAL`, bevor die bisherige Seed-/Crop-Stick-Interaktion weiterläuft.
- Nur der Server verändert das `SavedData` und verbraucht Items. Die Clientseite sendet für die beiden Eingaben zunächst einen Erfolg; endgültig entscheidet der Server. Bei einer nicht änderbaren Bodenposition fällt die serverseitige Interaktion auf das normale Block-/Item-Verhalten zurück.
- Das gespeicherte Profil wird bereits vom existierenden `SoilChemistryAccess.Provider` ausgelesen. Ein erfolgreicher Eingriff kann damit sofort eine chemisch bedingte Pflanzen-Wachstums-/Mutationsregel erfüllen.

## Grenzen / offene Entscheidungen

1. Das System enthält **keine automatische Feuchtigkeitsabnahme**, Regen-, Wetter-, Pfützen- oder Nachbarwasser-Erkennung, keine automatische Nährstoffentnahme bei Ernte und keine Bodenausbreitung. Die Werte werden nur durch explizite Behandlung oder bestehende Administratorbefehle verändert.
2. Die Behandlung funktioniert nur mit **im Datapack ausdrücklich definierten** Bodenprofilen oder zuvor gespeicherten Overrides. Es werden **noch keine Standard-Bodenprofile mitgeliefert**.
3. Die Wirkung von Wassereimer und Knochenmehl am Crop-Sticks-Block sowie die exakten NeoForge-/Minecraft-Interaktionsprioritäten sind **nicht kompiliert oder im Spiel geprüft**. Insbesondere Item-Use-Präzedenz, Rückfallverhalten und Creative-/Offhand-Verbrauch müssen vor Release validiert werden.
4. Die ursprünglichen AgriCraft-/AgriCore-`tolerance_factor`-/Strength-Formeln bleiben offen. Ebenso die Invalidierung bei Ersatz durch denselben Blocktyp.
5. Spätere Mods (u. a. Bewässerung, Gewächshäuser und das geplante MineColonies-Addon) können die gemeinsame `SoilOverrideSavedData`-Datenebene nutzen, sollten die Item-/Gameplay-Regeln aber nicht stillschweigend umgehen.

## Nächster Schritt

Die Original-AgriCore-Regeln zur chemischen Toleranz und tatsächlichen Bodenfeuchtigkeit nachzeichnen. Anschließend bei Freigabe Build und Ingame-Verbrauch prüfen, dann eine kontrollierte Witterungs-/Verbrauchssimulation konzipieren.
