# Schüsse über Zeit und Wiedergabe (ShotsApp): so funktioniert es

Analyse der DISAG ShotsApp 3.1.2 (React Native, Hermes-Bytecode, dekompiliert). Beschreibt die beiden zeitbezogenen Funktionen der App, damit sie auf der Webseite nachgebaut werden können. Ergänzt `shots-display-integration.md` (Zielscheibe) und `disag-qr-webhook.md` (Datenabruf).

**Legende:** `[verified]` = direkt aus dem App-Code abgelesen oder an den echten Beispieldaten nachgerechnet. `[assumed]` = plausibel, aber nicht bestätigt.

---

## 1. Kurzfassung

Es gibt in der App **zwei** zeitbezogene Ansichten, beide unter *Ergebnis → Statistiken*:

| Ansicht | Screen im Code | Was sie zeigt |
|---|---|---|
| **Detail** → Abschnitt "Schüsse über Zeit" | `ResultsStatisticsDetailScreen` | Liniendiagramm: pro Schuss die Zeit seit dem Vorschuss in Sekunden, mit **Durchschnittslinie** |
| **Video** | `ResultsStatisticsVideoScreen` | Schuss-für-Schuss-Wiedergabe auf der Scheibe mit Play/Pause, Schieberegler und Tempo 0,5x / 1x / 2x / 3x |

Wichtige Erkenntnisse:

1. Die Zeit steckt im Feld `offset` jedes Schusses. Die App behandelt `offset` als **Sekunden** (Achsenbeschriftung "s") `[verified]`. Der erste Schuss hat `0`, die weiteren sind der Abstand zum jeweils vorherigen Schuss `[assumed]`. Die Summe der Werte (1727 s, rund 29 min im Beispiel) passt dazu.
2. Der "Median" der App ist **kein Median, sondern der Mittelwert** `[verified]`: Summe aller Offsets größer 0, geteilt durch die Anzahl **aller** Schüsse (also inklusive des ersten Schusses mit 0). Im Beispiel 43,17 s. Der echte Median wäre 39 s.
3. Die **Wiedergabe läuft nicht in Echtzeit**. Pro Schritt erscheint ein Schuss, im festen Takt von `1500 ms / Tempo`. Die echten Zeitabstände aus `offset` werden dabei nicht verwendet `[verified]`.

---

## 2. Datengrundlage

Es werden dieselben Schüsse benutzt, die auch die Zielscheibe zeigt `[verified]`:

```js
const shots = result.data.results.filter(s => s.valid === 2 && s.flag === 2);   // Reihenfolge = Schussfolge
```

Feldübersicht je Schuss: `{ valid, flag, offset, x, y, value }`. `value` = Ring × 10, `x`/`y` in 1/100 mm (+x rechts, +y oben), `offset` = Zeit (Sekunden).

Echte Beispieldaten (`sample-result.json`, 40 Schuss), `offset` in Schussreihenfolge:

```
0 37 43 40 36 72 50 39 46 54 | 49 41 55 79 36 43 46 37 35 35 | 37 33 34 48 33 49 40 77 36 39 | 52 33 74 36 53 35 32 39 37 37
```

Summe 1727 s, Maximum 79 s, Minimum (ohne ersten Schuss) 32 s.

---

## 3. "Schüsse über Zeit" (Liniendiagramm)

### 3.1 Was die App berechnet `[verified]`

Aus den gefilterten Schüssen (Index `i` ab 0):

| Größe | Berechnung |
|---|---|
| X-Beschriftung | `"S" + (i + 1)`, also S1, S2, ... |
| Y-Wert | `shot.offset` (Sekunden) |
| Punktfarbe | `colorForValue(shot.value)` (siehe 3.4) |
| Maximum `max` | `Math.max(...offsets)` |
| Durchschnitt `avg` | `Σ(offset, nur offset > 0) / Anzahl aller Schüsse` |
| Pixel pro Sekunde `k` | `225 / max` |

Die App nennt die Linie intern "average". In der Oberfläche steht der Wert als Legende neben einem waagerechten Strich: `Number(avg.toFixed(2)) + "s"`, z. B. **43.17s**.

Der Durchschnitt zählt den ersten Schuss (`offset` 0) im Nenner mit. Im Beispiel: `1727 / 40 = 43,175`. Ohne den ersten Schuss wären es `1727 / 39 = 44,28`.

### 3.2 Darstellung `[verified]`

- Liniendiagramm (react-native-chart-kit `LineChart`), horizontal scrollbar.
- Breite `50 px × Anzahl Schüsse`, Höhe 300 px, Y-Achse beginnt bei 0 (`fromZero`), Achsensuffix `s`.
- Eigene Y-Beschriftung statt der Standardachse: 5 Marken bei `round(max × i / 4)` für `i = 0..4`, jeweils mit `s` (z. B. 0s, 20s, 40s, 59s, 79s). Position von oben: `228 − k × Wert`.
- Durchschnittslinie: waagerechte Linie (Dekorator) bei `241 − k × avg` von oben, Farbe Primärfarbe.
- Datenpunkte: Kreise mit Radius 7, gefüllt nach `colorForValue(value)`.
- Dunkler Hintergrund (`darkest`), weiße Linie und Beschriftung.

### 3.3 Weitere Kennzahlen im selben Screen `[verified]`

Der Detail-Screen enthält zusätzlich (hier nur Überblick, nicht zeitbezogen):

- Tabelle mit Kopf `Schusswert, IZ, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, Ø, Ø,X, Teiler`: Anzahl Schüsse je Ringwert, Ø = Summe der ganzen Ringe (`floor(value/10)*10`) / Schusszahl / 10, Ø,X = Summe der Zehntelringe / Schusszahl / 10 (jeweils 2 Nachkommastellen).
- Tortendiagramm mit drei Klassen `>=10`, `>=9`, `<9` (Anzahl Schüsse je Klasse, nach `value >= 100` und `value >= 90`).
- Diagramm "Ringe / Schuss" und Verteilung je Ring.

### 3.4 Punktfarben

Die App färbt per `colorForValue`: `value >= 100` rot (danger), `>= 90` gold (warning), sonst grün (success). Das wirkt verdreht (beste Schüsse rot). Für die Webseite empfohlen: eigene Farbwahl `[choice]`, z. B. 10,0 und höher grün, 9,0 bis 9,9 gelb, darunter rot.

### 3.5 Nachbau (getestet am Beispiel)

```js
// Zeitkennzahlen wie in der App, zusätzlich echter Median
function timeStats(result) {
  const shots = result.data.results.filter(s => s.valid === 2 && s.flag === 2);
  const offsets = shots.map(s => s.offset);
  const n = offsets.length;
  const total = offsets.reduce((a, b) => a + (b > 0 ? b : 0), 0);
  const sorted = [...offsets].sort((a, b) => a - b);
  const median = n ? (n % 2 ? sorted[(n - 1) / 2] : (sorted[n / 2 - 1] + sorted[n / 2]) / 2) : 0;
  let run = 0;
  return {
    n, offsets, total,
    appAverage: n ? total / n : 0,                 // Wert, den die App als Durchschnittslinie zeigt
    median,                                         // echter Median (App hat keinen)
    max: n ? Math.max(...offsets) : 0,
    min: n > 1 ? Math.min(...offsets.slice(1)) : 0, // ohne ersten Schuss (offset 0)
    elapsed: offsets.map(o => (run += o)),          // kumulierte Zeit je Schuss in s
  };
}
```

Ergebnis für `sample-result.json`:

| Kennzahl | Wert |
|---|---|
| Schüsse | 40 |
| Gesamtdauer (Summe) | 1727 s (28 min 47 s) |
| App-Durchschnitt (Linie) | 43,17 s |
| Echter Median | 39 s |
| Maximum / Minimum (ohne ersten Schuss) | 79 s / 32 s |
| Summe je Serie (10er) | 417 / 456 / 426 / 428 s |

Das Diagramm kann als SVG oder mit einer Chart-Bibliothek gezeichnet werden. Struktur: X = Schussnummer (1..n), Y = `offset` in Sekunden ab 0, Punkte nach Wert gefärbt, waagerechte Linie bei `appAverage` (optional zusätzlich gestrichelt bei `median`), Beschriftung "Ø 43,2 s". Auf schmalen Bildschirmen horizontal scrollen lassen (50 px pro Schuss wie in der App).

Hinweis zur Aussage: Der erste Schuss hat keinen Vorschuss, sein `offset` 0 ist kein echter Abstand. Für einen "ehrlichen" Median den ersten Schuss weglassen (`offsets.slice(1)`) und das in der Beschriftung angeben. Im Beispiel bleibt der Median dann bei 39 s.

---

## 4. Wiedergabe ("Video")

### 4.1 Ablauf in der App `[verified]`

Zustand: aktueller Index `idx` (Start `-1`, also leere Scheibe), `playing` (Start `false`), Tempo `speed` (Start `1`).

| Element | Verhalten |
|---|---|
| Tempo-Auswahl | `0.5x`, `1x`, `2x`, `3x` |
| Takt | `setInterval(step, 1500 / speed)` ms: 3000 ms, 1500 ms, 750 ms, 500 ms pro Schuss |
| Schritt | `next = idx + 1`. Ist `next < n`: `idx = next`. Sonst: Wiedergabe stoppen (`playing = false`), Intervall löschen. |
| Play/Pause | Auf Pause: Intervall stoppen. Auf Play: ist `idx >= n − 1` (Ende erreicht), erst auf `-1` zurücksetzen (Neustart), dann starten. |
| Schieberegler | Bereich 0 bis n. Wert `v` ergibt `idx = clamp(round(v) − 1, −1, n − 1)`. Beim Ziehen wird die Wiedergabe pausiert. Während der Wiedergabe läuft der Regler auf `idx + 1` mit. |
| Tempo ändern | Intervall wird mit neuem Takt neu gestartet. |

### 4.2 Was auf der Scheibe erscheint `[verified]`

Es ist dieselbe Scheibe wie in der Ergebnisansicht (`ResultImage`), mit `playback = true` und `playbackIndex = idx`:

- Gezeichnet werden die Schüsse `shots.slice(0, idx + 1)`: bei `idx = -1` keine, bei `idx = 0` der erste, usw. **Die Schüsse sammeln sich also an**, bisherige bleiben sichtbar.
- Jeder Schuss: Kreis an `(R + x, R − y)`, Radius `Pellet/2`, Füllung `colorForValue(value)`, Deckkraft 0,8. (Optionale Hervorhebung mit Primärfüllung und dunklem Rand gibt es nur, wenn ein Schuss ausgewählt ist. Die Wiedergabe setzt keine Hervorhebung.)
- Die Wiedergabe verwendet nur die Schussreihenfolge. Die Zeitabstände (`offset`) beeinflussen den Takt nicht.

### 4.3 Nachbau mit der vorhandenen Bibliothek (getestet)

`shotsapp-target.js` kann mit einer gekürzten Schussliste direkt für jeden Schritt rendern:

```js
function resultUpTo(result, idx) {                  // idx = -1: leere Scheibe
  const c = JSON.parse(JSON.stringify(result));
  c.data.results = ShotsAppTarget.usableShots(result).slice(0, idx + 1);
  return c;
}

const n = ShotsAppTarget.usableShots(result).length;
let idx = -1, timer = null, speed = 1;

function draw() {
  target.innerHTML = ShotsAppTarget.render(resultUpTo(result, idx), {
    size: 600, showNumbers: true, shotColor: 'app'   // 'app' = Farben wie die App, oder eigene Funktion
  });
  slider.value = idx + 1;                            // slider: min 0, max n
  label.textContent = idx + 1 + ' / ' + n;
}
function stop()  { clearInterval(timer); timer = null; playBtn.textContent = 'Play'; }
function start() {
  stop();
  if (idx >= n - 1) { idx = -1; draw(); }            // am Ende: neu beginnen
  playBtn.textContent = 'Pause';
  timer = setInterval(() => {
    if (idx + 1 >= n) return stop();
    idx++; draw();
    if (idx >= n - 1) stop();
  }, 1500 / speed);
}
slider.oninput = () => { stop(); idx = Math.max(-1, Math.min(n - 1, Math.round(slider.value) - 1)); draw(); };
playBtn.onclick = () => (timer ? stop() : start());
speedSelect.onchange = () => { speed = +speedSelect.value; if (timer) start(); };   // Optionen 0.5, 1, 2, 3
draw();
```

Anmerkung zur Bibliothek: `shotColor: 'app'` bildet die App-Farben nach. Standard ist eine einzige Farbe.

### 4.4 Optionale Erweiterung `[choice]`

Die App spielt gleichmäßig ab. Wer die echte Schussfolge zeitgetreu abspielen will (Schuss erscheint nach `offset` Sekunden, ggf. beschleunigt), nutzt die kumulierte Zeit `elapsed` aus `timeStats()` und setzt pro Schuss einen `setTimeout` mit `elapsed[i] * 1000 / speed`. Das ist eine **Erweiterung gegenüber der App**, nicht deren Verhalten.

---

## 5. Einbau-Empfehlung

1. Im Ergebnis-Screen zwei Reiter oder Abschnitte: "Zeit" (Diagramm + Kennzahlen) und "Wiedergabe".
2. Kennzahlen als Kacheln: Gesamtdauer (`min:s`), Ø pro Schuss (App-Wert), Median, schnellster und langsamster Abstand, Dauer je Serie.
3. Beschriftung klarstellen: "Ø" = Summe / Schusszahl wie in der App, "Median" = echter Median. Beide nur anzeigen, wenn die Abweichung zur Erklärung nötig ist.
4. Zeiten als `mm:ss` formatieren, deutsche Dezimalkommas.
5. Daten kommen aus demselben JSON (`data.results`). Es wird nichts Zusätzliches vom Webhook benötigt.

---

## 6. Grenzen und Annahmen

- `offset` als "Sekunden seit dem Vorschuss" ist durch Plausibilität belegt (Einheit "s" in der App, erster Wert 0, Summe passend zu einem 40-Schuss-Wettkampf), nicht durch eine Dokumentation von DISAG `[assumed]`. Ob `offset` bei anderen Geräten oder Disziplinen eine andere Einheit hat (das Gerät kennt `timeInHundredths`), wurde nicht geprüft.
- Nicht bekannt ist, ob Pausen (z. B. Serienwechsel) im Offset enthalten sind. Im Beispiel gibt es an den Serienübergängen keine auffälligen Sprünge.
- Das Aussehen der App-Diagramme (Farben, Maße) ist hier nur beschrieben. Der Nachbau darf frei gestaltet werden.
- Die App ist eine inoffizielle Quelle: Verhalten kann sich mit App-Updates ändern.

---

## 7. Abnahmetest mit `sample-result.json`

1. Diagramm hat 40 Punkte, S1 bei 0 s, Maximum 79 s bei S14, Durchschnittslinie bei 43,17 s.
2. Median 39 s, Gesamtdauer 1727 s.
3. Wiedergabe: Start leer, Schritt 1 zeigt einen Schuss, Schritt 40 zeigt alle 40. Tempo 3x ergibt 500 ms pro Schritt. Schieberegler auf 0 ergibt leere Scheibe.
4. Nach dem letzten Schuss stoppt die Wiedergabe. Erneut Play beginnt bei leerer Scheibe.
