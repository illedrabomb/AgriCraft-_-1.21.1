# Lokaler Playtest am 16.11.2026

## Ergebnis

**Playtest mit offenen Befunden abgeschlossen, keine vollständige Abnahme und keine Freigabe.** Echte Client-Interaktionen wurden ausgeführt; daneben gab es ausdrücklich benannte Befehls-/Fixture-Prüfungen. Ein reproduzierbarer Darstellungsfehler wurde korrigiert. Zweiter Fruchtzyklus, fehlende Pflanzenfärbung und ein Fehler in Pams eigenen Daten bleiben offen. Keine Pushes und keine Übernahme nach entwicklung, testing oder master.

Ausgang: `3ce9c03bd9a41f5d35391265bb1e6af133b06df0`, Integrations-/Code-Merge `5790dae5295c9117420d4b682eb6a592d3154712`. Branch weiterhin `integration/neoforge-1211-consolidation`. Minecraft **1.21.1**, NeoForge **21.1.265**, Temurin **21.0.12.1+1**, Gradle **9.2.1**, Mod-Metadaten **0.2.14-alpha**. Korrekturen dieses Playtests nur lokal auf diesem Branch; keine neue Releaseversion.

## Umgebung, Frost und Aussagegrenzen

- Neue Welt `neoforge-port/run/playtest-server/playtest-world`; localhost:25589, RCON localhost:25590; separater Client `run/playtest-client`. Vorherige Welten/Testdaten unverändert.
- Das Gebiet liegt in einem Winterbiom. Der Nutzer wies während des Tests auf gefrorenes Wasser hin; Eis/Schnee waren tatsächlich vorhanden. Bewässerung, Boden und Wachstumszeiten waren anfangs deshalb keine kontrollierten Vergleichsbedingungen. Danach wurde die Bewässerung mit Glowstone gegen Zufrieren geschützt; Zuckerrohr hatte einen eigenen geschützten Wasserplatz. **Frost ist kein als Modfehler gewerteter Befund.** Saison-Provider waren nicht installiert.
- Die Testfläche, Böden, sichere Standfläche, Gegenstände, Blickrichtung und Teleports wurden per RCON vorbereitet. Einmaliger Sturz/Tod bei einem zu niedrig angesetzten Teleport auf verändertem Boden war ein Fixture-Problem; anschließend Steinweg und passende Standhöhe. Keine Aussage über einen Mod-Schadensfehler.
- Echte Spielerinteraktion bedeutet hier: Tastatur-/Inventareingaben im laufenden Minecraft-Client als Spieler Dev, nicht nur ein gestarteter Client oder ein setblock mit Genom. Testbelegung: R = Benutzen, F = Angriff; F war zusätzlich noch als Offhand-Wechsel belegt, was bei den Angriffen einen sichtbaren Handwechsel bewirkte. Kein externes Gameplay-Bot-API wurde verwendet.
- Ein Teil lief in Survival, ein Teil in Creative. Insbesondere erzwingt der isolierte Server bei erneuter Anmeldung Creative. Deshalb sind die Sonderpflanzen-Eingaben nach Clientneustart **Creative-Pflanzaktionen**, keine Verbrauchs-/Survival-Ertragsprüfung. Der spätere Samenabbau und das Crafting wurden ausdrücklich wieder in Survival durchgeführt.
- Wachstum wurde meist mit `randomTickSpeed=1000` beschleunigt und zwischen den Schritten mit 0 angehalten. Das prüft Abläufe, **keine natürliche Wachstumsdauer oder Farmperformance**. Haste/Resistenz dienten der Teststeuerung, nicht einer regulären Spielerbalance.
- Die erste neue Optionsdatei ohne DataVersion wurde von Minecraft falsch migriert und verworfen; nach Übernahme der von Minecraft geschriebenen Version 3955 waren die Testtasten wirksam. Die damaligen Options-Exceptions sind kein Crop-Genetics-Fehler.

## Durchgeführte Prüfungen

Legende: **SPIEL** = tatsächliche Client-Eingabe; **BEFEHL** = RCON-/Weltprüfung; **FIXTURE** = künstlich gesetzte Ausgangsdaten. Ein PASS gilt ausschließlich für den beschriebenen Umfang.

| Bereich / Ziel | Durchführung und Ergebnis | Status |
|---|---|---|
| Clientstart und Verbindung | SPIEL: Client bis in die lokale Dedicated-Welt geladen, mehrfach nach Neustart verbunden. Kein Client-Crash. | PASS |
| Eigener Kreativ-Tab | SPIEL: Seite 2 geöffnet und Crop Genetics gewählt. Sticks, Analyzer und 16 Startgenome sichtbar; ein eigener Tab, keine doppelte Registrierung. Bild 01. | PASS |
| Normale Saatgut-Tooltips | SPIEL: analysiertes echtes Pam-Saatgut und genetischer Weizensamen im Inventar/Crafting-Feld betrachtet. Pflanzen-ID, Analysehinweis und Anleitung sichtbar. Bild 04/07. | PASS |
| Erweiterte Shift-Anzeige | Shift wurde kurz gedrückt; dauerhaft gehaltene Anzeige aller Genpaare wurde nicht zuverlässig erfasst. NBT-Werte wurden separat gelesen. | BLOCKED / nicht vollständig durchgeführt |
| Sticks platzieren und Weizen pflanzen | SPIEL, Survival: ein Stick verbraucht (64→63), ein Weizensamen verbraucht (32→31). BEFEHL bestätigt wheat_plant, vollständiges 1/1-Genom und OriginalSeed. | PASS |
| Double Crop Sticks | SPIEL, Survival: zweiten Platz mit zwei Sticks aufgebaut, Verbrauch und `cross=true` bestätigt. Später entstand dort zunächst ein Weizenklon. | PASS |
| Analyzer auf gepflanzter Kultur | SPIEL, Survival: Analyzer auf Weizen benutzt, Erfolgsmeldung sichtbar, SeedAnalyzed=1; Genom blieb unverändert. | PASS |
| Wachstum und sichtbare Stufen | SPIEL/BEFEHL: gepflanzter Weizen wächst unter beschleunigten Random Ticks von 0 auf7; reifes Modell sichtbar. Rechtsklickernte setzt auf Stufe4 zurück. Bilder 02/05. | PASS, beschleunigt |
| Weizenernte | SPIEL, Survival: reifen Weizen mit leerer Hand benutzt; zwei Weizen im Inventar, Genom und Analyseflag bleiben am Stamm. | PASS, ein Zyklus |
| Analyzer mit echtem Pam-Samen | SPIEL: Analyzer in Haupthand, echte Artichoke Seed in Offhand benutzt. Vorher ohne Komponenten; danach artichoke_plant-Genom, OriginalSeed und analyzed=1 auf demselben Pam-Item. | PASS |
| Vererbung und Artenmutation | SPIEL-Ausgang: Kartoffel gepflanzt; BEFEHL/FIXTURE: Eltern danach auf dokumentierte unterschiedliche Allele und Fertility10 gesetzt, Kreuzungsplatz per Befehl zurückgesetzt. Unveränderte 0,75-Karottenmutation und echte Welt-Random-Ticks erzeugten carrot_plant mit Growth6/2, Gain3/2, Strength6/4, Fertility10/9, Mutativity1/1. Werte sind mit Elternvererbung und ±1-Mutation vereinbar. | PASS, Fixture-gestützter Weltfall; keine statistische Wahrscheinlichkeitsabnahme |
| Genetischen Samen durch Abbau erhalten | SPIEL, Survival mit Haste: Weizenstamm abgebaut, Drops eingesammelt. Genetischer Samen enthielt exakt die zuvor gesetzten Paare 9/2, 8/3, 7/4, 10/10, 1/1, OriginalSeed und Analyseflag. Das Genom stammt hier aus der vorherigen Eltern-Fixture. | PASS |
| Rückgewinnungsrezept | SPIEL, Survival: tatsächlich erhaltenen genetischen Samen und ein Papier in das 2×2-Crafting-Feld gelegt, Ergebnis angeklickt und ins Inventar übernommen. Ein normales Weizensaatgut, genetischer Samen verbraucht, Papier wieder vorhanden. Das absichtliche Ergebnis ist ein **normales** Saatgut ohne Genom, wie der Tooltip erklärt. Bild07. | PASS |
| Melone/Kürbis pflanzen und erste Frucht | SPIEL, Creative: normale Samen auf vorbereitete leere Sticks gepflanzt. BEFEHL: beschleunigte Ticks; beide Stämme reif, getrennte echte Fruchtblöcke mit passender Parent-Position und UUID. Bild06. | PASS, erster Zyklus |
| Fruchtabbau und Eigentum | SPIEL, Creative: beide Früchte mit Angriffstaste entfernt. Stämme unmittelbar FruitReady=0, ohne Token/Richtung und auf Regrowth4. Creative erzeugt keine regulären Survival-Drops; deshalb keine Ertrags-/Fortune-/Silk-Touch-Abnahme daraus abgeleitet. | PASS, begrenzte Eigentumsprüfung |
| Zweiter Fruchtzyklus | Nach erneutem Wachstum bis7 keine neue Frucht beobachtet, trotz wiederholter beschleunigter Tickfenster, freiem nördlichem Zielplatz und erneut vorbereitetem Dirt-Untergrund. Auch Chunk-Wechsel bestätigte zunächst weiter FruitReady=0. Bilder08/09, RCON-Zeitfolge. Ursache ungeklärt, Winter-/Umgebungsfaktoren nicht vollständig ausgeschlossen. Survival-Fruchtertrag konnte deshalb nicht gültig geprüft werden. | FAIL / ungeklärter Befund |
| Kaktus/Zuckerrohr | SPIEL, Creative: auf vorbereiteten Sticks über Sand gepflanzt; Zuckerrohr neben geschütztem Wasser. Beide unter beschleunigten Ticks 0→7. Kaktusmodell und Zuckerrohrmodell vorhanden. Kein systematischer Negativtest mit vereistem Wasser/Obstruktion, kein Kontakt-Schadenstest. | PASS, positiver Basisfall |
| Bambus/Beeren | SPIEL, Creative: auf vorbereiteten Sticks über Dirt gepflanzt, bis7 gewachsen; erkennbare Pflanzenmodelle. Einzelernte nicht durch Spieler durchgeführt. | PASS, Basisfall |
| Brauner/roter Pilz | SPIEL, Creative: auf Myzel bei Tageslicht gepflanzt, bis7 gewachsen; braunes/rotes Modell sichtbar. Dunkelheit auf anderen Böden nicht geprüft. | PASS, Basisfall |
| Echte Pam-Art pflanzen | SPIEL, Creative: zuvor tatsächlich analysierten Pam-Artischockensamen auf vorbereitete Sticks gesetzt. Die analysierten Item-Komponenten waren zuvor per Befehl von der Offhand in die Hotbar kopiert worden; die Pflanzaktion selbst erfolgte im Client. artichoke_plant, OriginalSeed und Analyseflag bleiben; wächst bis7. Grafik verwendet den generischen Fallback, keine originalgetreue Artischocke. | PASS funktional; Grafikgrenze unten |
| Ressourcen-Reload | SPIEL: F3+T ausgeführt; neue Holz-UVs sichtbar, kein Ressourcen-Crash; 16 Visuals, 0 missing-PNG-Meldungen. | PASS |
| Datapack-Reload/Pam-Vorrang | BEFEHL/FIXTURE: zusätzliche gültige explizite Art beansprucht Artischokensamen/-produkt. Auto-Art weicht zugunsten der expliziten Definition (59 Auto, 54 explizit, keine Konflikt-Diagnose). Geladene ursprüngliche Artischocke behält ihr Genom dormant. | PASS |
| Echte Auto-Kollision | BEFEHL/FIXTURE: konfigurierte zusätzliche Probe-Art beansprucht dieselben echten Items wie die natürliche Auto-Art. Beide Kandidaten als CONFLICT gesperrt; 59 Auto und 2 Konfliktmeldungen, insgesamt112 Arten. Entfernen der Konfiguration stellt 60 Auto/0 Konflikte/113 Arten wieder her. | PASS |
| Chunk entladen/laden | BEFEHL: Spawn-Radius0, Force-Loading entfernt, Spectator-Teleport etwa1000 Blöcke weg. `execute if loaded` bestätigt alten Chunk nicht geladen; Rückkehr lädt ihn wieder. Artischocken-Genom, Analyseflag, Stufe7 und Karottennachkomme erhalten. Kein manueller Fußweg-Test. | PASS, Befehlsprüfung |
| Vollständiger Weltneustart | BEFEHL/SPIEL: save-all, Client geschlossen, Server gestoppt und beides neu gestartet. Nachkomme mit allen Paaren und Pam-Pflanze mit Stufe/Analyse erhalten. Holzmodelle auch mit finalem Code nach Neustart farbig. | PASS |
| Build und Unit-Tests nach Korrektur | Vollständiger normaler Build ohne verpflichtende Pam-Abhängigkeit; 17 Tests erfolgreich. Der neue Holz-UV-Test war vor der Korrektur reproduzierbar rot und danach grün. | PASS |

## Gefundene Fehler und technische Änderungen

### PT-01 – Schwarze Holzmodelle: behoben und erneut geprüft

Die alte 16×16-Holztextur besitzt nur einen vier Pixel breiten deckenden Streifen. Implizite, aus der Modellgeometrie berechnete UVs trafen transparente schwarze Texel; der feste Renderpfad stellte diese schwarz dar. Explizite UVs `[0,0,4,16]` auf allen Holzflächen von `crop_sticks.json` und `crop_sticks_cross.json` begrenzen die Abtastung auf deckendes Holz. `render_type: minecraft:cutout` setzt den passenden Alpha-Renderpfad. Die vorhandenen angehängten Stängelmodelle verwendeten bereits Cutout.

Eine zunächst untersuchte noOcclusion-Änderung löste den Fehler nicht und wurde vollständig zurückgenommen; **keine Änderung an CropBlocks.java** im Ergebnis. Die UV-Korrektur wurde per F3+T und nach komplettem Client-/Serverneustart visuell bestätigt. Vorher-/Nachherbilder 03/05/09. Regression: `CropStickModelTest.woodFacesNeverSampleTransparentLegacySpritePixels`, prüft die tatsächliche Alpha-Belegung der abgetasteten Sprite-Texel.

### PT-02 – Zweiter Melonen-/Kürbiszyklus: offen

Erste Früchte und tokengebundener Reset funktionieren im beschriebenen Fall. Anschließend erreichten beide Stämme wieder GrowthStage7, blieben aber FruitReady=0. Mehrere kontrollierte Versuche einschließlich neuem Dirt-Zielboden und Chunk-Wechsel sind protokolliert. Kein neuer Fruchtblock wurde per Befehl hineingesetzt, um den Test künstlich bestehen zu lassen. Das ist **kein Nachweis einer Duplikation**, aber ebenso **kein bestandener voller Fruchtzyklus**. Vor Abnahme mit kontrolliertem Klima, vollständig protokollierten Licht-/Bodenbedingungen und Diagnose der Wachstums-/Platzierungsprädikate reproduzieren. Konkrete Ursache noch nicht belastbar bestimmt.

### PT-03 – Graue Pflanzenfärbung: offen

Nicht angehängte Melonen-/Kürbisstängel und der generische Oak-Leaves-Fallback der Pam-Art wirken grau. Der BER setzt weiße Vertexfarben und übernimmt keine Vanilla-Stängel-/Laubtönung. Angefügte Stängel besitzen dagegen den separaten Vanilla-Farbhandler. Keine fehlenden PNGs gemeldet. Sichtbarer Grafikbefund, Bilder08/09 und Pam-Pflanze im Rohprotokoll; kein Datenverlust. Farbhandling bzw. ein farbiger, eindeutig erkennbarer generischer Fallback bedarf Nacharbeit.

### PT-04 – Fehler im echten Pam-1.0.9-Tag: offen, Drittanbieter

`minecraft:tags/item/rabbit_food.json` **in Pams unveränderter JAR** enthält ungültige IDs: `pamhc2crops: blackberryitem`, `minecraft:MELON`, `minecraft:SWEET_BERRIES`, `minecraft:GLOW_BERRIES`. NeoForge/Minecraft meldet bei Start und Reload `Couldn't read tag list minecraft:rabbit_food` / `IllegalStateException: Not a valid resource location`. Die Mod startet trotzdem; Crop Genetics lädt seine Pflanzen fehlerfrei. Die Gesamtintegration ist deswegen **nicht ladefehlerfrei**. Die fremde JAR und globale Rabbit-Food-Tags wurden nicht stillschweigend verändert.

## Reale Pam-Version und Abhängigkeiten

[Offizielle CurseForge-Datei 8064883](https://www.curseforge.com/minecraft/mc-mods/pams-harvestcraft-2-crops/files/8064883): `pamhc2crops-NEOFORGE-1.21.1-1.0.9.jar`, modId pamhc2crops, Version1.0.9. SHA256 `ebdb8aaf2bda8d4a46fb655af57b546f285a1ff15411bba691c18420064b84b5`. Aus der tatsächlichen JAR-Metadatei: erforderliches NeoForge >=21.1.0, Minecraft >=1.21.1 und <1.22; kein verpflichtendes Food Core/Trees. Beide laufenden Modlisten bestätigen Pam. **Trees wurde nicht installiert.** Die JAR bleibt ausschließlich im ignorierten lokalen run-Verzeichnis und wird nicht mit dem Projekt veröffentlicht.

Baseline mit Pam: **113 Arten =16 Vanilla +37 explizite Pam +60 automatisch erkannte Pam**, **34 Mutationen**, 147 Diagnoseeinträge; keine ungültigen Crop-Genetics-Definitionen, keine fehlenden registrierten Itempaare. Die Angabe „Explicit plants loaded:53“ zählt alle Mods, nicht nur Pam. Wiederhergestellter Baselinezustand nach Konflikttests und Neustart erneut bestätigt.

Testskript-Korrektur: Ein expliziter Claim wird absichtlich vor der Auto-Kandidatenbildung reserviert und zählt deshalb nicht als Auto-CONFLICT. Eine ursprüngliche Harness-Erwartung „1 Konflikt“ war falsch; nach Quellabgleich wurde der echte Registry-Kollisionstest mit zusätzlichem konfiguriertem Alias durchgeführt und lieferte korrekt zwei gesperrte Kandidaten. Rohdurchlauf und erfolgreicher Nachtest bleiben getrennt erhalten.

## Geänderte Dateien und Belege

- `neoforge-port/build.gradle`: opt-in `-Pplaytest` nutzt getrennte Laufverzeichnisse und lokale Quickplay-Adresse; `-PpamJar=<lokale JAR>` hängt eine lokale Testabhängigkeit an. Normaler Build lädt/bündelt Pam nicht.
- `neoforge-port/src/main/resources/assets/cropgenetics/models/block/crop_sticks.json` und `crop_sticks_cross.json`: Holz-UVs und Cutout.
- `neoforge-port/src/test/java/dev/illedrabomb/cropgenetics/client/CropStickModelTest.java`: reale Sprite-/UV-Regression.
- Status, README, Pam-Runtime-Dokumentation und dieser Bericht aktualisiert.

Versionierte Belege: `docs/playtest-evidence/01-creative-tab.png` bis `09-final-restart-models.png`, `admin.jsonl`, Pam-Reload-Rohdurchlauf und Nachtest, Neustartprotokoll, `summary.txt` mit Build-/JUnit-/Logauszügen. Die vollständigen lokalen Logs heißen `neoforge-port/playtest-*.log`; die neue Welt und Fixture-Dateien bleiben unter run/ erhalten. Kein Produktionsspielstand angefasst.

## Nicht vollständig geprüfte Bereiche / nächste Entscheidung

Kein Langzeit-/Lasttest, keine natürliche Wachstumsdauer, kein vollständiger Survival-Erntezyklus sämtlicher Sonderpflanzen, keine verlässliche gehaltene Shift-Anzeige, kein neuer Client-Sichttest feiner Altersänderungen innerhalb derselben BlockState-Stufe, keine statistische Mutationsverteilung, kein großer Mehrspieler-/Chunkgrenzen-/Explosion-/Piston-/Fortune-/Silk-Touch-Test und keine Saison-Provider-Integration. Die früheren Server-/Unit-Prüfungen werden nicht als Ersatz für diese ausstehenden Spielerprüfungen ausgegeben.

**Empfehlung: Integrationsbranch beibehalten und noch nicht freigeben.** Zuerst PT-02 reproduzieren/klären, PT-03 korrigieren und für PT-04 eine dokumentierte Pam-Version-/Kompatibilitätsentscheidung treffen; dann gezielt erneut testen. Dieser Auftrag endet mit lokalem Bericht und lokaler Korrektur. Keine Freigabeautomation, keine Actions, keine Branch-Übernahme und kein Push.
