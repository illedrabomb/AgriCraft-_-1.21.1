# Seed Analyzer / Seed Chest – manuelle Regression (19.11.2026)

> **Noch nicht ausgeführt.** Diese Testfälle erst beim vereinbarten gemeinsamen Minecraft-Test nach dem gesamten Entwicklungspaket durchführen. Geeignet für externe Tester ohne Java-Kenntnisse.

## Ziel und Vorbereitung

- Minecraft 1.21.1, passender NeoForge-Build, aktueller `integration/neoforge-1211-consolidation`-Mod-Build.
- Neue Testwelt mit Cheats und Creative Inventory; für aussagekräftige Entnahme-Tests auf Survival wechseln.
- Bereitstellen: Crop Sticks, ein Seed Analyzer, eine Seed Chest, ein Seed Journal, Weizensamen, einen genetisch verbesserten Seed mit sichtbaren Growth/Gain/Strength-Werten, ggf. Pam's und Farmer's-Delight-Seeds.
- Für alle Tests notieren: Build/Commit, Mod-Versionen, Singleplayer oder Server, Soll/Ist, Screenshot (bei Datenabweichung Vorher/Nachher), Erfolg/Fehlschlag.
- Bei Save/Reload erst Welt regulär verlassen und neu betreten; keinen Datenordner direkt manipulieren.
- **Nicht** von einem bloßen erfolgreichen Java-Build auf getestete Spielmechanik schließen.

## Einzelschritte

### SC-01 – Ursprünglicher Saatgutstapel bleibt im Inventar erhalten

**Warum:** Der Analyzer-Slot fasst nur 1 Seed. Beim direkten Rechtsklick mit 64 normalen Samen dürfen die 63 übrigen nicht verschwinden.

1. Im Survival einen Stack aus 64 Weizensamen halten.
2. Mit dem Stack auf den leeren Analyzer rechtsklicken.
3. Inventar und Analyzer-Inhalt kontrollieren.

**Soll:** Analyzer enthält 1 Weizensamen; 63 bleiben beim Spieler (oder 64 im Creative-Modus). Es entsteht kein unbemerkt verlorener Stack.

### SC-02 – Analyse schreibt nur den sichtbaren Zustand, kein neues Zufallsgenom

**Warum:** Der Analyzer soll gespeicherte Gene aufdecken, nicht neu würfeln.

1. Einen zuvor ungeprüften genetischen Seed mit bekannten, nichtstandardmäßigen Genwerten in den Analyzer legen.
2. Genwerte vor/nach Analyse vergleichen (ggf. mittels eigenem Debug-/Test-Samen).
3. Seed wieder entnehmen.

**Soll:** Die Growth-/Gain-/Strength-Zahlen sind unverändert. Nur „analysiert“ wird gesetzt.

### SC-03 – Veränderte normale Seeds nicht heimlich konvertieren

**Warum:** Zusätzliche Komponenten anderer Mods dürfen nicht verloren gehen.

1. Einen normalen, aber z. B. umbenannten oder mit Fremddaten versehenen Seed vorbereiten.
2. Versuchen, ihn direkt in den Analyzer einzulegen.
3. Bei fehlgeschlagener Aktion GUI-Inhalt und Spielerinventar prüfen.

**Soll:** Der Server akzeptiert diesen Stack nicht als analysierbare normale Saat. Item und Zusatzdaten bleiben beim Spieler unverändert. Die Seed Chest darf ihn hingegen lagern, falls seine Seed-ID registriert ist.

### SC-04 – Analyzer + danebenstehende Seed Chest

**Warum:** Die gemeinsame GUI zeigt den Analyzer (9 Slots) und die Seed Chest (27 Slots), ohne Items doppelt zu speichern.

1. Seed Chest horizontal direkt neben den Analyzer stellen.
2. Analyzer öffnen; Ansicht auf vier Reihen prüfen.
3. Mindestens drei unterschiedliche, unmodifizierte Seeds in die Seed Chest legen.
4. Seed mit bekannten Genen in den Analyzer-Slot legen.
5. GUI schließen und erneut öffnen.

**Soll:** Alle Items sind genau einmal vorhanden. Die benachbarte Chest zeigt 27 Speicherplätze; der Analyzer-Slot und Journal-Slot bleiben getrennt.

### SC-05 – Welt speichern und neu laden

**Warum:** Saatgut-Genome, Analysezustand und der Journal-Forschungsstand müssen persistent bleiben.

1. Verbesserte genetische Seeds (z. B. verschiedene Growth/Gain/Strength-Werte) in die Seed Chest legen.
2. Einen analysierten Seed im Analyzer und das Journal im Journal-Slot belassen.
3. Welt regulär verlassen und neu laden.
4. Items entnehmen, Tooltips vergleichen und Journal ansehen.

**Soll:** Anzahl, Herkunft, Genwerte, Analyseflag und Journal-Fortschritt sind unverändert.

### SC-06 – Volles Spielerinventar bei Entnahme

**Warum:** Der Sneak-Leerhand-Entnahmeweg muss Items notfalls in die Welt droppen, nicht löschen.

1. Analyzer mit einem Seed und Journal befüllen.
2. Spielerinventar mit beliebigen Items vollständig füllen.
3. Mit **beiden Händen leer** schleichen und rechtsklicken.
4. Danach noch einmal wiederholen.

**Soll:** Zuerst Seed, danach Journal werden herausgenommen; wenn kein Inventarplatz frei ist, werden sie als sichtbare Item-Entities gedroppt. Keine Duplikate und kein Verlust.

### SC-07 – Zwei Crop Sticks verhindern direktes Einpflanzen

**Warum:** Doppelte Sticks reservieren das Feld für Kreuzungen.

1. Zwei Crop Sticks auf demselben Feld setzen.
2. Versuchen, normalen und analysierten Seed direkt einzupflanzen.
3. Den oberen Stick mit Sneak/Leerhand wie im Gameplay vorgesehen entfernen, danach Seed einsetzen.

**Soll:** Vor Entfernen des oberen Sticks wird kein Seed verbraucht und keine Pflanze eingesetzt; danach gelingt reguläre Pflanzung.

### SC-08 – Genetischer Seed nach Pflanzung und Abbau

**Warum:** Der ausgerissene Seed muss genau das Ausgangsgenom und die analysierte Herkunft tragen.

1. Analysierten genetischen Seed mit erhöhten Werten und dokumentierter Herkunft wählen.
2. In einfache Crop Sticks pflanzen; bei Bedarf bis Reife wachsen lassen.
3. Crop Sticks abbauen und erhaltenen genetischen Seed prüfen.

**Soll:** Die Genwerte, das Analyseflag und die vermerkte ursprüngliche Seed-ID stimmen; keine unerwartete Rücksetzung auf 1/1/1.

### SC-09 – Optionale Mod vorübergehend entfernen

**Warum:** Bereits gepflanzte Gene dürfen nicht allein wegen fehlender Pflanzenregistrierung verlorengehen.

1. In einer separaten Sicherungskopie mit installierter optionaler Mod einen ihrer Seeds pflanzen und dokumentieren.
2. Die Mod nur in dieser **Sicherungskopie** testweise entfernen, Welt erneut laden.
3. Gespeicherte Pflanze/Seed-Daten kontrollieren; danach Mod wieder einsetzen.

**Soll:** Kein Absturz und keine automatische Löschung des gespeicherten Genoms; bei fehlendem Eintrag verhält sich die Pflanze ggf. inaktiv. Diesen Test erst durchführen, wenn die konkrete optionale Mod tatsächlich verifiziert wurde.

## Auswertung

- **Erfolg:** alle relevanten Soll-Ergebnisse beobachtet und belegt.
- **Fehler:** erwartete und tatsächliche Zahlen/Items inklusive Screenshot sowie `latest.log` beifügen.
- **Blockiert:** Build startet nicht, benötigte Mod-/Seed-Art nicht verfügbar oder Testwelt nicht reproduzierbar. Nicht als Erfolg zählen.


### SC-10 – Verdeckte Genwerte nicht an andere Spieler übertragen

**Warum:** Unanalysierte Crop-Sticks-Pflanzen dürfen in normalen Welt-Update-Paketen ihre echten Genwerte nicht an umstehende Clients offenlegen.

1. Auf einem Server mit zwei Spielern einen unanalysierten genetischen Seed mit dokumentierten Werten außerhalb von 1/1/1 (z. B. 8/9/10) pflanzen.
2. Spieler B betrachtet den Block mit einer verfügbaren Overlay-Mod (z. B. Jade/Identify), ohne eine Analyse auszuführen.
3. Danach den Block analysieren, erneut ansehen, abbauen und den zurückgegebenen Seed vergleichen.

**Soll:** Vor Analyse sind echte individuelle Werte nicht öffentlich sichtbar und werden im gewöhnlichen Block-Update durch 1/1/1-Platzhalter ersetzt. Nach Analyse dürfen sie erscheinen. Der vom Server herausgegebene Seed trägt nach Abbau weiterhin die **echten** 8/9/10 und seine ursprüngliche Seed-ID. Der Netzwerk-Fix darf die Welt-Speicherung nicht neutralisieren.

**Status:** Nicht ausgeführt. Hinweis: Dieser Test prüft öffentliches Block-Sync-Verhalten, keine per-Spieler-spezifische Geheimhaltung nach global erfolgter Analyse.
