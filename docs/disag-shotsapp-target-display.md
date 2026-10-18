# Shots display: Zielscheibe und Ergebnisse auf der Webseite einbauen

Übergabedokument für den Code-Agenten. Ziel: ein Schützenergebnis (JSON vom n8n-Webhook, siehe `disag-qr-webhook.md`) als Zielscheibe mit Schüssen plus Ergebnis-Tabelle auf der Webseite anzeigen.

**Legende:** `[verified]` = aus dem ShotsApp-Code (v3.1.2) abgeleitet oder an echten Daten geprüft. `[choice]` = eigene Entscheidung, frei änderbar.

---

## 1. Was geliefert wird

| Datei | Zweck |
|---|---|
| `shotsapp-target.js` | Abhängigkeitsfreie Bibliothek, global `ShotsAppTarget`. Erzeugt die Scheibe als SVG-String und berechnet die Summen. Läuft im Browser und in Node (`module.exports`). |
| `target-demo.html` | Eigenständige Demo mit eingebetteter Bibliothek und Beispieldaten. Zeigt Ebenen-Schalter, Serien-Tabelle, Hell/Dunkel. Als Vorlage für den Einbau benutzen. |
| `sample-result.json` | Echtes Ergebnis (40 Schuss, Luftgewehr) in der Form, die der Webhook zurückgibt. Zum Testen ohne Netz. |

Die Bibliothek bildet die Scheibenzeichnung der DISAG ShotsApp nach (gleiche Geometrie, aus dem App-Code übernommen). Nichts davon braucht Internet oder eine CDN.

---

## 2. Eingabedaten

Eingabe ist die JSON-Antwort des Webhooks `[verified]`:

```json
{
  "id": "4EB58DA6-...",
  "discipline_id": "100_4",
  "data": {
    "uuid": "4EB58DA6-...",
    "startTime": "2026-10-14T20:26:47Z",
    "startTimeRaw": "2026-10-14 20:26:47Z\u001f\u001f\u001flg\u001f10",
    "meta": { "modeTitle": "", "title": "", "diskType": "lg", "seriesShots": 10 },
    "results": [
      { "valid": 2, "offset": 0, "x": -238, "y": -236, "value": 96, "flag": 2 }
    ]
  }
}
```

Bedeutung der Schussfelder `[verified]`:

| Feld | Bedeutung |
|---|---|
| `value` | Ringwert × 10 (`104` = 10,4). |
| `x`, `y` | Position relativ zur Scheibenmitte in **1/100 mm**. **+x = rechts, +y = oben.** |
| `flag`, `valid` | Die App zeichnet und zählt nur Schüsse mit `flag === 2 && valid === 2`. Bibliothek filtert genauso (`usableShots`). |
| `offset` | Vermutlich Sekunden seit dem vorherigen Schuss `[assumed]`, für die Anzeige nicht nötig. |

`data.meta.diskType` wählt die Scheibe (`lg` = Luftgewehr 10 m), `data.meta.seriesShots` ist die Serienlänge (10). Fehlt `meta` (alter Webhook-Stand), liest die Bibliothek beides aus `startTimeRaw`. Fehlt auch das, gelten `lg` und 10.

---

## 3. API der Bibliothek

```html
<script src="shotsapp-target.js"></script>
<div id="target"></div>
<script>
  document.getElementById('target').innerHTML =
    ShotsAppTarget.render(result, { size: 600, showNumbers: true });
  const sum = ShotsAppTarget.summarize(result);
</script>
```

### `ShotsAppTarget.render(result, opts)` → SVG-String

| Option | Standard | Wirkung |
|---|---|---|
| `size` | 600 | Breite und Höhe in px. Für responsive Layout per CSS `svg { width:100%; height:auto }` überschreiben. |
| `diskType` | aus `meta` | Scheibe erzwingen (`lg`, `lga`, `lp`, `lpa`, `kk`, `kka`, `kkb`, `fp`, `zs`, `zsa`, `spopip`, `kyffh`, `lpduell`). |
| `showNumbers` | `true` | Ringzahlen 1 bis 8 oben, rechts, unten, links. |
| `showGroup` | `false` | Streukreis: kleinster Kreis um alle Schüsse. |
| `showMean` | `false` | Kreuz auf dem mittleren Treffpunkt. |
| `shotNumbers` | `false` | Schussnummer in jedem Treffer. |
| `shotColor` | `'plain'` | `'plain'` = eine Farbe (#06B1CB), `'app'` = Farben der App, oder `function(value) -> css-Farbe` (`value` = Ring × 10). |
| `highlight` | – | Index eines Schusses (in der gezeichneten Liste), der wie in der App hervorgehoben wird. |

### `ShotsAppTarget.summarize(result, seriesSize?)`

```js
{
  count: 40,
  decimalTotal: 380.1,       // Summe der Zehntelringe
  wholeTotal: 363,           // Summe der ganzen Ringe
  series: [{ decimal: 92.8, whole: 87, shots: [9.6, 9.7, ...] }, ...],
  best: 10.5, worst: 6.7,
  meanPoint: { x_mm: -0.09, y_mm: -0.89 },   // +x rechts, +y oben
  meanRadius_mm: 3.51                         // mittlerer Abstand zum mittleren Treffpunkt
}
```

Erwartete Werte für `sample-result.json` (als Test verwenden): 40 Schuss, **380,1** Ringe, **363** ganze Ringe, Serien 92,8 / 99,3 / 91,5 / 96,5 (ganz 87 / 97 / 87 / 92), bester Schuss 10,5, schlechtester 6,7, mittlerer Treffpunkt −0,09 / −0,89 mm, mittlerer Abstand 3,51 mm.

### Weitere Exporte

`parseStartTime(raw)`, `describeResult(result)`, `usableShots(result)`, `makeCircle(points)`, `valueFromDistance(d)`, `DISKS`, `THEME`.

---

## 4. Geometrie (falls neu implementiert werden soll) `[verified]`

Alle Maße in 1/100 mm. Luftgewehr (`lg`):

- Scheibenradius **R = 2275** (22,75 mm). SVG-viewBox: `0 0 2R 2R` (= `0 0 4550 4550`).
- Ringradien: `[2275, 2025, 1775, 1525, 1275, 1025, 775, 525, 275, 25]`, Ring 1 außen bis Ring 10 innen. Schwarzer Spiegel bis Radius **1525**.
- Schuss-Mittelpunkt: `cx = R + x`, `cy = R − y` (SVG-y zeigt nach unten, daher das Minus).
- Schuss-Radius: **225** (Diabolo 4,5 mm, Radius = Pellet/2).
- Zeichenreihenfolge: weißer Kreis R → schwarzer Kreis 1525 → Ringlinien → Ringzahlen → (Streukreis) → Schüsse → (mittlerer Treffpunkt).
- Ringlinien: Strichbreite `2R/300`, schwarz außerhalb des Spiegels, weiß innerhalb. Auf dem Spiegelrand selbst keine Linie.
- Ringzahlen 1 bis 8 stehen in der Mitte des jeweiligen Rings (`(r_i + r_{i+1}) / 2`), schwarz im weißen Bereich, weiß im schwarzen. Schriftgröße 150.
- Ringwert eines Schusses aus dem Abstand d zur Mitte: `floor(110,03 − d/25)`, Maximum 109 (also 10,9). Bei allen 40 Beispielschüssen bestätigt. In der Praxis den `value` aus den Daten verwenden, die Formel ist nur eine Gegenprobe.
- Streukreis: kleinster umschließender Kreis der Schussmittelpunkte, Radius + 225, Füllung #C8C8C8 mit 0,4 Deckkraft, Rand #06B1CB.
- Hervorgehobener Schuss: Füllung #06B1CB deckend, Rand #0E3D4E, Breite 35.

---

## 5. Einbau in die Webseite

Empfohlene Struktur:

1. **Datenhaltung:** Ergebnis-JSON (vom Webhook) pro `uuid` speichern. `uuid` ist der Primärschlüssel und verhindert doppelte Einträge. Die Darstellung rechnet immer aus dem gespeicherten JSON, es werden keine fertigen Summen gespeichert.
2. **Ergebnisansicht** (pro Ergebnis):
   - Titel: Disziplin und Anzahl Schuss (`summarize().count`), Datum aus `data.startTime` (UTC!) umgerechnet nach `Europe/Berlin`, z. B. `new Date(startTime).toLocaleString('de-DE', { timeZone: 'Europe/Berlin' })`.
   - Gesamt: `decimalTotal` groß (deutsches Komma, eine Nachkommastelle), `wholeTotal` klein darunter.
   - Serien-Tabelle aus `series` (Zehntel und ganz je Serie, optional die Einzelschüsse).
   - Scheibe: `render(result, { size, showNumbers: true })`.
   - Schalter für Streukreis, mittlere Treffpunkt und Schussnummern (siehe `target-demo.html`).
   - Kennzahlen: bester und schlechtester Schuss, mittlerer Treffpunkt links/rechts bzw. hoch/tief, mittlerer Abstand.
3. **Mittlerer Treffpunkt in Worten:** `x_mm < 0` = links, `> 0` = rechts. `y_mm < 0` = tief, `> 0` = hoch.
4. **Ergebnisliste:** Übersicht pro Ergebnis mit Datum, Gesamt und kleinem `render(..., { size: 120, showNumbers: false })` als Vorschau.
5. **Interaktion (optional):** Beim Hover oder Tippen auf eine Tabellenzeile den Schuss mit `highlight: index` neu rendern. `render` liefert einen String, also einfach `innerHTML` ersetzen.

Das SVG enthält keine externen Ressourcen. Es kann direkt inline eingefügt, als `.svg` gespeichert oder per Canvas als PNG exportiert werden.

---

## 6. Gestaltungsregeln

- **Formatierung:** deutsches Dezimalkomma (`380,1`). `Intl.NumberFormat('de-DE', { minimumFractionDigits: 1, maximumFractionDigits: 1 })`.
- **Farben `[choice]`:** Standard ist eine einfarbige Darstellung. Die App färbt ab 10,0 rot, ab 9,0 gold, sonst grün, was verdreht wirkt. Falls Farbcodierung gewünscht ist, eine eigene Funktion übergeben, z. B. `shotColor: v => v >= 100 ? '#2ECC71' : v >= 90 ? '#06B1CB' : '#E74C3C'`.
- **Lesbarkeit:** Die Scheibe ist quadratisch. Auf Handys auf Containerbreite skalieren (`width:100%; height:auto; max-width:600px`). Ringzahlen sind ab etwa 300 px Breite lesbar, darunter `showNumbers: false`.
- **Dark Mode:** Scheibe bleibt weiß und schwarz (Papieroptik). Nur Umfeld und Tabelle an das Theme anpassen.
- **Barrierefreiheit:** Das SVG trägt `role="img"` und ein `aria-label` mit Scheibenname und Schusszahl. Die Serien-Tabelle liefert die Werte als Text.

---

## 7. Grenzen und Annahmen

- Geprüft ist nur die Scheibe `lg` mit echten Daten. Die übrigen Scheibentypen stammen aus dem App-Code (16 Konfigurationen), wurden aber nicht an echten Ergebnissen gegengeprüft. Unbekannter `diskType` fällt auf `lg` zurück.
- Ringzahlen zeichnet die Bibliothek als normalen SVG-Text, die App benutzt eigene 7-Segment-Zeichen. Aussehen daher leicht anders, Positionen sind gleich.
- Die Bedeutung von `discipline_id` (`100_4`) ist nicht bekannt. Die Scheibe wird aus `diskType` bestimmt, nicht daraus.
- Ein Ergebnis mit weniger oder mehr als 40 Schuss funktioniert, die Serien werden alle `seriesShots` Schuss geschnitten, die letzte kann kürzer sein.
- Der Disag-Webhook ist inoffiziell. Das Datenformat kann sich mit App-Updates ändern. `shotsapp-target.js` liest nur `value`, `x`, `y`, `flag`, `valid` und `meta`/`startTimeRaw`.

---

## 8. Abnahmetest

1. `sample-result.json` laden und anzeigen. Gesamt muss **380,1** sein, ganze Ringe **363**, vier Serien 92,8 / 99,3 / 91,5 / 96,5.
2. Der schlechteste Schuss (6,7) liegt rechts unten, weit außerhalb der Gruppe. Das bestätigt die Achsenrichtung (+x rechts, +y oben).
3. Die Gruppe liegt fast mittig, mittlerer Treffpunkt −0,09 / −0,89 mm.
4. Ringzahl-Schalter, Streukreis, mittlerer Treffpunkt, Schussnummern an und aus schalten, ohne Konsolenfehler.
5. Mit Browser-Breite 360 px prüfen, kein horizontales Scrollen.
