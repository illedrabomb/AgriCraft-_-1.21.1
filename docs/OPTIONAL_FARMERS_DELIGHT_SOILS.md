# Optionale Farmer's-Delight-Bodenprofile

**Stand:** 16.11.2026  
**Branch:** `feature/neoforge-1211-foundation`  
**Status:** Quellcode und Datapack-Ressourcen erstellt; **keine Kompilierung, keine Tests und kein Minecraft-Start**.

## Quelle und Datensatz

Original: `AgriCraft/AgriPlants`, festgehaltener Commit
`9444e75828f66bd40adabb4aeca2544765408f8a`.

| Originaldatei | Block | Feuchte | Säure | Nährstoffe | growth_modifier |
|---|---|---|---|---|---|
| `mod_farmersdelight/soils/rich_soil_soil.json` | `farmersdelight:rich_soil` | `damp` | `neutral` | `very_high` | 1.8 |
| `mod_farmersdelight/soils/rich_soil_farmland_soil.json` | `farmersdelight:rich_soil_farmland` | `wet` | `slightly_acidic` | `very_high` | 1.8 |

Die Originaldateien verlangen die Mod-ID `farmersdelight`. Das Runtime-Verhalten für **NeoForge 1.21.1** und die tatsächliche Verfügbarkeit dieser beiden Block-IDs in einer passenden Farmer's-Delight-Version sind **noch nicht im Spiel verifiziert**.

## Optionale Integration ohne Pflichtabhängigkeit

Zwei neue Dateien unter `neoforge-port/src/main/resources/data/cropgenetics/cropgenetics/soil_profiles/`:

- `farmersdelight_rich_soil.json`
- `farmersdelight_rich_soil_farmland.json`

Beide deklarieren `required_mod: "farmersdelight"` und die jeweiligen Originalwerte. Der bisherige `SoilProfileReloadListener` akzeptiert diesen **optional** konfigurierten Mod-Prädikatsfilter. `CropGeneticsMod` reicht `ModList.get().isLoaded` als Prüfer ein.

Wenn die Mod **fehlt**, wird das entsprechende Bodenprofil übersprungen, bevor die Block-ID geprüft wird. Eine Pflichtabhängigkeit im NeoForge-Mod-Metadatenfile ist hierfür **nicht** erforderlich; der Kern verweist auch nicht direkt auf Farmer's-Delight-Java-Klassen.

Wenn die Mod vorhanden ist, aber die erwartete Block-ID nicht registriert ist, meldet der Loader das Profil als ungültig und verwendet es nicht. Das hilft auch bei einer späteren Umbenennung oder Portierung der Farmer's-Delight-Blöcke. Bei fehlender Mod werden keine Dummy-/Fallback-Blocks erfunden.

Andere Soil-Profile werden nach denselben Regeln wie bisher geprüft. `required_mod` ist ein einzelner Mod-ID-String mit gültiger Syntax; Sammel-Dependencies können später hinzugefügt werden.

**Grenze:** Die bloße Existenz eines Bodenprofils erlaubt noch keine Pflanzung. Eine Pflanze muss den Bodenblock oder das Boden-Tag zusätzlich in ihren `soil_block`/`soil_tag`-Anforderungen zulassen. Das bisherige Modell kennt auch keinen Original-`growth_modifier` für Pflanzen, die keine Crop Sticks benutzen.

## Blockwechsel – geprüfte Grenze

Die bereits implementierte `SoilOverrideInvalidation` berücksichtigt nur die vorhandenen NeoForge-Break-/EntityPlace-/MultiPlace-Ereignisse und den Level-Tick-Nachlauf. Bei unveränderten Block-IDs bleibt der Lazy-Abgleich unzureichend für manche Same-Type-Replacements. Command-, Piston-, Explosion- und Maschinenaktionen sind nicht vollständig abgedeckt. Wir ersetzen diesen unvollständigen Mechanismus **nicht** durch unbestätigte zusätzliche Eventsignaturen: Bevor die Invalidierung erweitert wird, muss die Ziel-API für NeoForge 21.1.265 und ihr tatsächlicher Zeitpunkt bei Blockwechseln im Source-JAR geprüft werden, und später braucht es eine ausdrücklich freigegebene Spielvalidierung.

## Weiteres Vorgehen

1. Farmer's-Delight-1.21.1-Version und Registries prüfen; mögliche `rich_soil_farmland`-Tags und Pflanzungs-Kompatibilität gesondert modellieren.
2. Optionale Boden-Kompatibilität mit zusätzlichen Mods per Datapack prüfen, ohne Zwangsabhängigkeiten.
3. Geeigneten Blockänderungs-Hook über die tatsächlich verwendete NeoForge-API belegen, statt auf Vermutungen zu bauen.
4. Erst auf ausdrückliche Freigabe kompilieren und Spieleraktionen, Server-Reload, Datapack-Priorität, fehlende Mod und Spawn/Abbau testen.
