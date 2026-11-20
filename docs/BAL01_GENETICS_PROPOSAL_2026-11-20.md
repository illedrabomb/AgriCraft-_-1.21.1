# BAL-01: abstimmbarer Vorschlag für die neue Genetik (20.11.2026)

**Status: Entwurf zur ausdrücklichen Spielerfreigabe, keine Codeänderung und keine Simulation.**
Dieser Vorschlag ersetzt **noch nicht** `BreedingEngine` und ändert weder den Sonntag-Teststand noch die fest implementierten artabhängigen Genobergrenzen (`SpeciesGeneLimits`). Die Werte unten sind vorgeschlagene Balanceparameter, **keine** gemessenen Wahrscheinlichkeiten oder Garantie für das Ziel `10/10/10` in höchstens 70 erfolgreichen Nachkommen.

## Ablauf je erfolgreich erzeugtem Kind

1. **Elternvorlage (nach Spielerfeedback präzisiert):** Einen der zwei bis vier beteiligten Eltern als **vollständiges Genom** wählen. Auswahlgewicht = **Robustheit direkt**, nicht `Robustheit²` (z. B. `1/1/3` hat Gewicht 3, jeder `.../.../1` Gewicht 1). Das hält auch weniger robuste Eltern relevant. Anders als heute werden nicht drei Werte aus unabhängigen Elternrollen gemischt. Eine Mehr-Eltern-Kreuzung ist trotz unveränderter Vorlage keine Ein-Eltern-Klonmechanik.
2. **Grundpunkte:** Vorläufige diskrete Verteilung unten für 0–4 positive Punkte je erfolgreich erzeugtem Kind. Vier direkt angrenzende Eltern erhalten die beste, zwei die langsamste Verteilung. Die schwächere 3+-Reihe verwendet die 2-Eltern-Verteilung. Keine Verbesserung durch mehr als vier Eltern.
3. **Keine doppelte Nullmutation:** Der zusätzliche Aktivierungsfaktor `0,70 + 0,30 × (10 − Robustheit) / 9` aus dem ersten Entwurf **entfällt**. Die Tabellenzeile entscheidet zu **100 %** über 0–4 Grundpunkte; 0 Punkte sind bereits enthalten. Robustheit steuert die Vorlagenwahl (Schritt 1) und die negative Verlustchance (Schritt 4), nicht nochmals separat das Aussetzen positiver Punkte.
4. **Negative Ereignisse:** Für jeden der drei Werte unabhängig eine Verlustchance von `0,025 × (11 − Robustheit)` (bei Vorlagenrobustheit 1: 25 %, bei 10: 2,5 %). Bei Treffer fallen zufällig 1/2/3 Punkte mit vorläufigen Chancen 70/25/5 % weg, höchstens bis zum Mindestwert 1. Nur **tatsächlich** verlorene Punkte zählen.
5. **Rückgewinnung:** Für **jeden** tatsächlich verlorenen Punkt separat eine 40-%-Chance auf **einen** weiteren positiven Poolpunkt. Nicht wiedergewonnene Punkte sind endgültig verloren. Der zusätzliche Pool kann die vier Grundpunkte überschreiten; die vier-Punkte-Grenze gilt nur für den **Grundpool**.
6. **Verteilen:** Alle Grund- und Rückgewinnungspunkte einzeln gleichwahrscheinlich einem der drei Werte zuweisen. **Auch bei vollständiger Elternvorlage und bei gemischten Vorlagen** gelangen pro Wert **höchstens drei positive Punkte gegenüber dem tatsächlich gewählten Vorlagenwert** an, auch wenn mehr zugeteilt werden; der Rest verfällt ohne Neuverteilung. Die Gene bleiben immer zwischen 1 und 10. Die höhere Ausgangsgenetik führt dadurch ganz natürlich zu mehr verlorenen Überläufen. Ein Kind darf natürlich den bereits hohen Wert einer hochentwickelten Vorlage unverändert erben; das zählt nicht als neu gewonnene Punkte.
7. **Artenbegrenzung:** Erst nach diesen Änderungen die bereits implementierte `SpeciesGeneLimits`-Regel anwenden: neue dritte Art pro Wert maximal 4; bei gemischten Elternarten und Ergebnis einer Elternart maximal 5 bzw. eigener höchster Elternwert. Ernte, Genetik-Speicherung und echte Ein-Eltern-Klonierung bleiben unangetastet.

## Vorschlag: Verteilung der 0–4 Grundpunkte

| Erfolgreicher Nachwuchs mit | 0 Punkte | 1 Punkt | 2 Punkte | 3 Punkte | 4 Punkte |
| --- | ---: | ---: | ---: | ---: | ---: |
| 2 direkten Eltern | 35 % | 35 % | 20 % | 8 % | 2 % |
| 3 direkten Eltern | 20 % | 30 % | 28 % | 16 % | 6 % |
| 4 direkten Eltern | 10 % | 20 % | 30 % | 25 % | 15 % |
| 3+-Reihe | 35 % | 35 % | 20 % | 8 % | 2 % |

Alle Tabellenzeilen summieren sich auf 100 %. Die Prozentwerte beziehen sich **nur auf ein bereits erfolgreich entstandenes Kind**, nicht auf einen Random-Tick. Bestehende Fruchtbarkeitschancen bleiben in diesem Entwurf unverändert. Die bisherige Config `rowCrossEvolution=false` soll die **gesamte** Genmutation abschalten, nicht die unveränderte Elternvorlage-Auswahl; der vorhandene Mutationsmultiplikator muss bei Implementierung definiert und rückwärtskompatibel berücksichtigt werden (kein unbeabsichtigtes Überlaufen der Wahrscheinlichkeiten).

### Verbindlicher Misch-Eltern-Testfall aus dem Spielerfeedback

**Drei Weizen `10/10/10` + ein Weizen `5/6/7`:** Mit direkter Robustheitsgewichtung erhält der schwächere Elternseed `7 / (10 + 10 + 10 + 7) = 7/37 ≈ 18,9 %` der **Vorlagenwahl**, die drei perfekten zusammen `30/37 ≈ 81,1 %`. Das ist **nicht** die Endwahrscheinlichkeit für ein perfektes Kind: Positive und negative Mutation folgen noch, und bei einer perfekten Vorlage können Werte sinken. Wird `5/6/7` als Vorlage gewählt, darf ein einzelnes Kind nach den positiven Punkten höchstens `8/9/10` erreichen (vor etwaigen negativen Verlusten); **nie `10/10/10` allein durch die vier Grundpunkte**. Ein-Eltern-Klonierung bleibt der separate echte 1:1-Pfad; mit zwei Eltern wird weiterhin gekreuzt. Bei vier niedrigen Eltern soll andererseits die Weiterentwicklung durch häufigere Punkte schneller sein. **Alle Fälle nur statische Erwartungen, nicht simuliert/getestet.**

## Auswahlstrategie und Abnahme erst mit Testfreigabe

- **Vergleichsstrategie v1:** Vier anfängliche identische `1/1/1`-Eltern derselben Art. Nach jedem **erfolgreich entstandenen** Kind die vier Eltern und das Kind nach `(Wachstum + Ertrag + Robustheit)` bewerten; bei gleichem Gesamtwert die höhere Robustheit, dann höheren kleinsten Genwert bevorzugen. Die beste Vierergruppe als nächste Eltern wählen; auf einem unveränderten 4er-Feld beliebig oft ein Kind erzeugen. Das beschreibt eine statistische Zuchtreihe, keinen zusätzlichen Auto-Selektionscode im Spiel.
- **Zu ermitteln, noch nicht geprüft:** Erwartungswert/Median/90. Perzentil der Zahl erfolgreicher Nachkommen bis zum **ersten `10/10/10`-Kind** aus vielen unabhängigen Folgen. Insbesondere Erwartungswert **≤ 70 Kinder** als Nutzerziel; Verteilung der Nachkommen bei 2/3/4 direkten Eltern und Reihe vergleichen. Reproduzierbare Seeds, Randwerte und Konfiguration protokollieren.
- **JUnit-Quelltests erst bei Umsetzung schreiben, nicht ausführen:** Vorlagengewichtung, 0–4-Verteilung als deterministische Grenz-Rolls, +3 je Wert, Verlust bis 1, 40-%-Rückgewinnung pro tatsächlich verlorenem Punkt, keine Punktumverteilung, gekappte 10er-Werte, Stabilität bei hoher Robustheit, Config-Abschaltung, Reihe und unveränderte Ein-Eltern-Klone sowie die bereits implementierte artenabhängige 4er/5er-Grenze.

**Noch vor Implementierung abzustimmen:** Die nun vorgeschlagene **direkte** Robustheitsgewichtung statt des verworfenen Quadrats, Wahrscheinlichkeitsmatrix, Verlustformel, Tie-Breaking der Auswahlstrategie und Verhalten des alten Config-Multiplikators. **Der separate positive Stabilitätsfaktor ist gestrichen.** Erst danach ein vollständiges Code-/Test-/Doku-Paket erstellen. **Gradle/JUnit/Minecraft/CI und statistische Simulation bleiben bis zur ausdrücklichen Freigabe gesperrt.**
