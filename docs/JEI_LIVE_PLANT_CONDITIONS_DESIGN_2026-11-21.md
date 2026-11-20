# UX-02 / UI-11 – aktuelle Pflanzenbedingungen in JEI

**Status:** Technischer Machbarkeits-/Sicherheitsreview am 21.11.2026, **keine Implementierung oder Testausführung**. Hauptmod und Entwicklungsbranch `integration/neoforge-1211-consolidation`.

## Nutzeranforderung

Für jede in dieser Welt tatsächlich unterstützte Pflanze soll JEI verständlich erklären, auf welchem Boden und unter welchen Umweltbedingungen der Seed wächst: z. B. Ackerboden und Licht, Myzel/Pilze, Zuckerrohr mit Wasser neben dem Boden, gegebenenfalls Block-Tags, Biome, Dimension, Höhe, Jahreszeit und Bodenchemie. Die Anzeige muss nach einem Server-Datapackwechsel aktuell bleiben; JEI ist optional.

## Bestätigter Quellbefund

1. `CropConditions.canGrow` berechnet Wachstum aus den **serverseitigen** `PlantDefinition.requirements()` einschließlich soil_block/soil_tag, Pflanzen-Sonderregeln, Biomen/Dimensionen, Umgebung, Saison, Chemie, Licht und Strength-Toleranz sowie Soil-Modifier. Der Server trifft die tatsächliche Pflanzentscheidung.
2. `CropGeneticsJeiPlugin.registerRecipes` nutzt derzeit `SeedSpeciesCatalog.DEFAULT_SPECIES` plus optionale Item-Links. **Daraus folgt nicht**, dass genau diese Arten auf dem verbundenen Server aktuell aktiv sind. Eigene Datapack-Pflanzen und Overrides können abweichen.
3. Ein bestehender Netzwerk-Kanal für die allgemeine Pflanzenanforderungs-Liste wurde im Hauptmod nicht gefunden. Die vorhandenen `Genome`-StreamCodecs und Blockentity-Update-Pakete betreffen andere Daten; sie dürfen nicht als globaler Pflanzenregel-Snapshot wiederverwendet werden.
4. JEI-`IRecipeRegistration.addItemStackInfo` erzeugt Informationsansichten beim Registrieren. Aus dem aktuellen Quellcode folgt **kein Nachweis**, dass diese Texte nach `/reload` automatisch neu erstellt werden. JEI-Reload-Verhalten ist daher eine eigene Integrationsprüfung.

## Technisch saubere Umsetzung – in einem eigenen Codepaket

**A. Serverprojektion (ohne Genetik):** Aus dem aktuellen `AgriPlantsRegistry.Snapshot` eine sortierte, auf öffentliche Anzeigeinformationen reduzierte Liste erstellen. Nur Pflanzen-ID, passende Seed-Item-IDs, Bodenregel (ein Block oder Tag), min/max-Licht und Toleranz, artspezifische Nachbarregeln, Biome/Dimensionen, Höhen-/Fluid-/Saison-/Chemie-Anforderungen und ggf. eine klar bezeichnete Versionskennung übertragen. **Keine** Seed-Genome, Journal-Entdeckungen, individuelle Stärke, Chunk-/Blockentity-Inventare oder sensible Spielerstände.

**B. NeoForge-1.21.1-Verbindung:** Versioniertes `CustomPacketPayload` mit `StreamCodec` über `RegisterPayloadHandlersEvent` registrieren; bei Login bzw. `OnDatapackSyncEvent` nur an tatsächlich verbundene Spieler senden. Die Dokumentation zu [1.21.1-Payloads](https://docs.neoforged.net/docs/1.21.1/networking/payload/) beschreibt diese Registrierung und `PacketDistributor.sendToPlayer`. Größenlimit, maximale Anzahl Arten/IDs und Strings begrenzen; keinen Client-Thread mit serverseitigem Registryzugriff belasten.

**C. Clientzustand:** Snapshot atomar ersetzen, bei Logout vollständig leeren. Während keine gültigen Serverregeln vorliegen **keine** behaupteten aktiven Anforderungen anzeigen; explizit „Serverbedingungen werden geladen“ bzw. „nicht synchronisiert“. Bei Serverwechsel nicht den alten Snapshot weiterverwenden.

**D. JEI:** Pro aktiver Pflanzenart einen klaren Bedingungs-/Infobereich bereitstellen. Die technische Umsetzung muss anhand der **tatsächlich verwendeten JEI-1.21.1-API** klären, wie Informationen nach Login oder `/reload` aktualisiert werden; statisch registrierte `addItemStackInfo`-Seiten dürfen keine unaktualisierbaren Welt-Regeln vortäuschen. Bei fehlendem JEI hat die Hauptmod weder Ladefehler noch eine Pflichtabhängigkeit.

**E. Darstellung:** Bodenblock/Tag mit lesbarem Namen und technischer ID, Lichtbereich inkl. Strength-Abweichung, Zusatzbedingung „Wasser neben dem Stützboden“ **nur für Zuckerrohr**, Pilz-Helligkeitsausnahme nur für erlaubte Substrate. Keine generelle Aussage „alle Ackerpflanzen brauchen Wasser daneben“. Unbekannte Tag-Inhalte oder Chemieanbieter als solche ausweisen, statt Beispiele als vollständige Liste zu behaupten. DE/EN.

## Vorbereitetes späteres Testgate `UI-11` (weiterhin offen)

| Teilfall | Konkreter Nachweis |
| --- | --- |
| Standardpflanze | Bodenblock/-Tag, Lichtanforderung und Seed-Zuordnung stimmen mit aktivem Datapack überein. |
| Zuckerrohr und Pilze | Wasser-/Myzel-/Licht-Sonderregeln korrekt, ohne sie auf andere Pflanzen zu übertragen. |
| Geändertes Datapack | `/reload` ändert JEI-Anzeige ohne Client-Neustart bzw. zeigt während unbestätigtem Zustand ehrlich „nicht synchronisiert“. |
| Dedizierter Server | Zwei Spieler sehen dieselben öffentlichen Regeln, aber keine privaten Genome oder Journal-Inhalte. |
| Serverwechsel / Logout | Alter Server-Snapshot wird vollständig verworfen; keine falschen Werte in der nächsten Welt. |
| Fehlendes JEI | Core-Spielstart und Seed-Pflanzung weiterhin ohne JEI möglich. |
| Erweiterungen | Unbekannte Mod-ID, nicht registrierter Seed und späteres optionales Add-on erzeugen keine irreführende Unterstützungsmeldung. |

**Review-Entscheidung vor dem gemeinsamen Test:** Keine statischen Pseudo-Weltinformationen in JEI veröffentlichen. Das erforderliche Paket greift in **Netzwerk-/Login-/Reload- und JEI-Lifecycle** ein und ist deshalb als zusammenhängende Hauptmod-Erweiterung mit eigenem NBT-/Netzwerk-Review und eigenen Testquellen durchzuführen. Ohne ausdrücklich freigegebene Gradle-/JUnit-/Minecraft-Tests bleibt die technische Runtime-Akzeptanz unbewiesen. Bis dahin `UI-11` als **offen, nicht implementiert** führen.
