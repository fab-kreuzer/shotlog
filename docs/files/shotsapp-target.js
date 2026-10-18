/*
 * shotsapp-target.js
 * Draws a DISAG ShotsApp result (target disk + shots) as an SVG string.
 * No dependencies, no network access, works offline.
 *
 * The geometry follows the DISAG ShotsApp 3.1.2 drawing code exactly:
 *   - coordinate unit = 1/100 mm (unitsPerMM = 100)
 *   - SVG viewBox = 0 0 2R 2R, R = disk.biggestdistance, target centre = (R, R)
 *   - a shot is drawn at cx = R + x, cy = R - y   (so +x = right, +y = UP)
 *   - shot circle radius = disk.shotsizedisplay / 2  (pellet radius)
 *   - the app only draws shots with flag === 2 && valid === 2
 *
 * Usage:
 *   const svg = ShotsAppTarget.render(result, { size: 600, showGroup: true });
 *   container.innerHTML = svg;
 *
 * `result` is the JSON returned by the n8n webhook (or Disag's /results/preview).
 */
(function (root) {
  'use strict';

  // Standard target disks, copied from the app (type "default").
  // coords = outer radius of ring 1..10 (+ final 0), in 1/100 mm.
  var DISKS = {
    lg:      { name: 'Luftgewehr',                 coords: [2275, 2025, 1775, 1525, 1275, 1025, 775, 525, 275, 25, 0],            blackuntil: 1525,  countsfrom: 0, countsuntil: 8, R: 2275,  pellet: 450, fontsize: 150 },
    lga:     { name: 'Luftgewehr Aufgelegt',       coords: [2275, 2025, 1775, 1525, 1275, 1025, 775, 525, 275, 25, 0],            blackuntil: 1525,  countsfrom: 0, countsuntil: 8, R: 2275,  pellet: 450, fontsize: 150 },
    lp:      { name: 'Luftpistole',                coords: [7775, 6975, 6175, 5375, 4575, 3775, 2975, 2175, 1375, 575, 250],      blackuntil: 2975,  countsfrom: 0, countsuntil: 8, R: 7775,  pellet: 450, fontsize: 300 },
    lpa:     { name: 'Luftpistole Aufgelegt',      coords: [7775, 6975, 6175, 5375, 4575, 3775, 2975, 2175, 1375, 575, 250],      blackuntil: 2975,  countsfrom: 0, countsuntil: 8, R: 7775,  pellet: 450, fontsize: 300 },
    spopip:  { name: 'Sportpistole Praezision',    coords: [25000, 22500, 20000, 17500, 15000, 12500, 10000, 7500, 5000, 2500, 1250], blackuntil: 10000, countsfrom: 0, countsuntil: 9, R: 25000, pellet: 600, fontsize: 1000 },
    fp:      { name: 'Freie Pistole',              coords: [25000, 22500, 20000, 17500, 15000, 12500, 10000, 7500, 5000, 2500, 1250], blackuntil: 10000, countsfrom: 0, countsuntil: 9, R: 25000, pellet: 600, fontsize: 1000 },
    kk:      { name: 'Kleinkalibergewehr',         coords: [7720, 6920, 6120, 5320, 4520, 3720, 2920, 2120, 1320, 502, 250],      blackuntil: 5620,  countsfrom: 0, countsuntil: 8, R: 7720,  pellet: 560, fontsize: 300 },
    kka:     { name: 'KK Aufgelegt',               coords: [7720, 6920, 6120, 5320, 4520, 3720, 2920, 2120, 1320, 502, 250],      blackuntil: 5620,  countsfrom: 0, countsuntil: 8, R: 7720,  pellet: 560, fontsize: 300 },
    kkb:     { name: 'KK Biathlon',                coords: [7720, 6920, 6120, 5320, 4520, 3720, 2920, 2120, 1320, 502, 250],      blackuntil: 5620,  countsfrom: 0, countsuntil: 8, R: 7720,  pellet: 560, fontsize: 300 },
    zs:      { name: 'Zimmerstutzen',              coords: [4250, 3800, 3350, 2900, 2450, 2000, 1550, 1100, 650, 200, 0],         blackuntil: 2000,  countsfrom: 0, countsuntil: 8, R: 4250,  pellet: 465, fontsize: 300 },
    zsa:     { name: 'Zimmerstutzen Aufgelegt',    coords: [4250, 3800, 3350, 2900, 2450, 2000, 1550, 1100, 650, 200, 0],         blackuntil: 2000,  countsfrom: 0, countsuntil: 8, R: 4250,  pellet: 465, fontsize: 300 },
    kyffh:   { name: 'Kyffhaeuser LG',             coords: [4150, 3700, 3250, 2800, 2350, 1900, 1450, 1000, 550, 100, 0],         blackuntil: 1450,  countsfrom: 0, countsuntil: 9, R: 4250,  pellet: 450, fontsize: 300 },
    lpduell: { name: 'LP Duell',                   coords: [7800, 7800, 7800, 7800, 7800, 6500, 5200, 3900, 2600, 1300, 475],     blackuntil: 7800,  countsfrom: 0, countsuntil: 9, R: 7800,  pellet: 450, fontsize: 500 }
  };

  // Ring "X" marker the app uses for the mean point of impact (ShotsMiddle).
  var CENTER_ICON_PATH = 'm4.07.708 1.678 1.684 4.884 4.88L17.195.708l3.362 3.362-1.684 1.677-4.88 4.884 6.563 6.563-3.36 3.361-6.563-6.562-4.885 4.885-1.678 1.677-3.362-3.362 1.684-1.677 4.88-4.884-4.886-4.885L.71 4.07z';

  // The app's own palette.
  var THEME = { primary: '#06B1CB', dark: '#0E3D4E', success: '#2ECC71', warning: '#FFD700', danger: '#E74C3C' };

  /**
   * Disag packs extra fields into data.startTime, separated by the control
   * character U+001F:  "<timestamp>\x1f<modeTitle>\x1f<title>\x1f<diskType>\x1f<seriesShots>"
   * Example: "2026-10-14 20:26:47Z\x1f\x1f\x1flg\x1f10"  ->  disk "lg", 10 shots per series.
   */
  function parseStartTime(raw) {
    var parts = String(raw || '').split('\u001f');
    var out = { timestamp: (parts[0] || '').trim(), modeTitle: '', title: '', diskType: '', seriesShots: null };
    if (parts.length > 4) {
      out.modeTitle = (parts[1] || '').trim();
      out.title = (parts[2] || '').trim();
      out.diskType = (parts[3] || '').trim();
      var n = parseInt((parts[4] || '').trim(), 10);
      out.seriesShots = isNaN(n) ? null : n;
    }
    return out;
  }

  /** Reads disk type / series length from a webhook result (new or raw shape). */
  function describeResult(result) {
    var d = (result && result.data) || {};
    var meta = d.meta || {};
    var fromRaw = parseStartTime(d.startTimeRaw || d.startTime);
    return {
      diskType: (meta.diskType || fromRaw.diskType || '').toLowerCase(),
      seriesShots: meta.seriesShots || fromRaw.seriesShots || 10,
      modeTitle: meta.modeTitle || fromRaw.modeTitle,
      title: meta.title || fromRaw.title,
      timestamp: fromRaw.timestamp
    };
  }

  /** The shots the app draws: flag === 2 and valid === 2. */
  function usableShots(result) {
    var list = (result && result.data && result.data.results) || [];
    return list.filter(function (s) { return s.flag === 2 && s.valid === 2; });
  }

  // ---- smallest enclosing circle (the app's makeCircle; Nayuki's algorithm) ----
  function makeCircle(points) {
    var shuffled = points.slice();
    for (var i = shuffled.length - 1; i > 0; i--) {
      var j = Math.floor(Math.random() * (i + 1));
      var t = shuffled[i]; shuffled[i] = shuffled[j]; shuffled[j] = t;
    }
    var c = null;
    for (var k = 0; k < shuffled.length; k++) {
      var p = shuffled[k];
      if (c === null || !inCircle(c, p)) c = onePoint(shuffled.slice(0, k + 1), p);
    }
    return c;
  }
  function onePoint(points, p) {
    var c = { x: p.x, y: p.y, r: 0 };
    for (var i = 0; i < points.length; i++) {
      var q = points[i];
      if (!inCircle(c, q)) c = (c.r === 0) ? diameter(p, q) : twoPoints(points.slice(0, i + 1), p, q);
    }
    return c;
  }
  function twoPoints(points, p, q) {
    var circ = diameter(p, q), left = null, right = null;
    var px = p.x, py = p.y, qx = q.x, qy = q.y;
    for (var i = 0; i < points.length; i++) {
      var r = points[i];
      var cross = cross2(px, py, qx, qy, r.x, r.y);
      var c = circumcircle(p, q, r);
      if (c === null) continue;
      else if (cross > 0 && (left === null || cross2(px, py, qx, qy, c.x, c.y) > cross2(px, py, qx, qy, left.x, left.y))) left = c;
      else if (cross < 0 && (right === null || cross2(px, py, qx, qy, c.x, c.y) < cross2(px, py, qx, qy, right.x, right.y))) right = c;
    }
    if (left === null && right === null) return circ;
    if (left === null) return right;
    if (right === null) return left;
    return left.r <= right.r ? left : right;
  }
  function diameter(a, b) {
    var cx = (a.x + b.x) / 2, cy = (a.y + b.y) / 2;
    return { x: cx, y: cy, r: Math.max(Math.hypot(cx - a.x, cy - a.y), Math.hypot(cx - b.x, cy - b.y)) };
  }
  function circumcircle(a, b, c) {
    var ox = (Math.min(a.x, b.x, c.x) + Math.max(a.x, b.x, c.x)) / 2;
    var oy = (Math.min(a.y, b.y, c.y) + Math.max(a.y, b.y, c.y)) / 2;
    var ax = a.x - ox, ay = a.y - oy, bx = b.x - ox, by = b.y - oy, cx = c.x - ox, cy = c.y - oy;
    var d = (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by)) * 2;
    if (d === 0) return null;
    var x = ox + ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d;
    var y = oy + ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d;
    var ra = Math.hypot(x - a.x, y - a.y), rb = Math.hypot(x - b.x, y - b.y), rc = Math.hypot(x - c.x, y - c.y);
    return { x: x, y: y, r: Math.max(ra, rb, rc) };
  }
  function cross2(x0, y0, x1, y1, x2, y2) { return (x1 - x0) * (y2 - y0) - (y1 - y0) * (x2 - x0); }
  function inCircle(c, p) { return c !== null && Math.hypot(p.x - c.x, p.y - c.y) <= c.r * (1 + 1e-14); }

  // ---- ring value from distance (verified against the sample: all 40 shots match) ----
  // value (in tenths of a ring) = floor(110.03 - d/25), d = distance from centre in 1/100 mm, max 109.
  function valueFromDistance(d) { return Math.min(109, Math.floor(110.03 - d / 25)); }

  function esc(s) { return String(s).replace(/[&<>"]/g, function (c) { return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;' }[c]; }); }
  function n(v) { return Math.round(v * 100) / 100; }

  /** The app's own shot colours. NOTE: this mapping is taken literally from the app code; it paints
   *  value >= 10.0 red and < 9.0 green, which looks inverted. Use 'plain' or your own function instead. */
  function appColor(value) { return value >= 100 ? THEME.danger : (value >= 90 ? THEME.warning : THEME.success); }

  /**
   * @param {object} result  webhook JSON
   * @param {object} [opts]
   *   size        px width/height of the SVG (default 600)
   *   diskType    override disk key, e.g. 'lg' (default: read from the result, fallback 'lg')
   *   showNumbers ring numbers 1..8 (default true)
   *   showGroup   grey smallest-enclosing circle around all shots (default false)
   *   showMean    X marker at the mean point of impact (default false)
   *   shotNumbers print the shot number inside each shot (default false, not in the app)
   *   shotColor   'plain' (default, app primary), 'app' (the app's red/gold/green) or function(value)->css colour
   *   highlight   index (into the drawn shots) of one shot to emphasise, like the app does
   * @returns {string} SVG markup
   */
  function render(result, opts) {
    opts = opts || {};
    var info = describeResult(result);
    var key = (opts.diskType || info.diskType || 'lg').toLowerCase();
    var disk = DISKS[key] || DISKS.lg;
    var R = disk.R, size = opts.size || 600;
    var shots = usableShots(result);
    var out = [];

    out.push('<svg xmlns="http://www.w3.org/2000/svg" width="' + size + '" height="' + size + '" viewBox="0 0 ' + (2 * R) + ' ' + (2 * R) + '" role="img" aria-label="' + esc(disk.name) + ', ' + shots.length + ' shots">');

    // 1) paper and black aiming mark
    out.push('<circle cx="' + R + '" cy="' + R + '" r="' + R + '" fill="#fff"/>');
    if (disk.blackuntil) out.push('<circle cx="' + R + '" cy="' + R + '" r="' + disk.blackuntil + '" fill="#000"/>');

    // 2) ring lines: black outside the black mark, white inside; no line on the black mark's own edge
    var lineW = (2 * R) / 300;
    for (var i = 0; i < disk.coords.length; i++) {
      var r = disk.coords[i];
      var stroke = r > disk.blackuntil ? '#000' : '#fff';
      var sw = r === disk.blackuntil ? 0 : lineW;
      out.push('<circle cx="' + R + '" cy="' + R + '" r="' + r + '" fill="none" stroke="' + stroke + '" stroke-width="' + n(sw) + '"/>');
    }

    // 3) ring numbers: digit = ring index + 1, at the band mid-radius, top/right/bottom/left
    if (opts.showNumbers !== false) {
      for (var idx = disk.countsfrom; idx < disk.countsuntil && idx + 1 < disk.coords.length; idx++) {
        var mid = (disk.coords[idx] + disk.coords[idx + 1]) / 2;
        var fill = disk.coords[idx] > disk.blackuntil ? '#000' : '#fff';
        var pos = [[R, R - mid], [R + mid, R], [R, R + mid], [R - mid, R]];
        for (var p = 0; p < 4; p++) {
          out.push('<text x="' + n(pos[p][0]) + '" y="' + n(pos[p][1]) + '" fill="' + fill + '" font-size="' + disk.fontsize + '" font-weight="bold" font-family="Arial, Helvetica, sans-serif" text-anchor="middle" dominant-baseline="central">' + (idx + 1) + '</text>');
        }
      }
    }

    // 4) grouping circle (ShotsArea): smallest enclosing circle of the shot centres + pellet radius
    if (opts.showGroup && shots.length > 0) {
      var c = makeCircle(shots.map(function (s) { return { x: s.x, y: s.y }; }));
      out.push('<circle cx="' + n(R + c.x) + '" cy="' + n(R - c.y) + '" r="' + n(c.r + disk.pellet / 2) + '" fill="#C8C8C8" fill-opacity="0.4" stroke="' + THEME.primary + '" stroke-width="25"/>');
    }

    // 5) shots (Shots)
    var colorFn = typeof opts.shotColor === 'function' ? opts.shotColor
      : opts.shotColor === 'app' ? appColor
      : function () { return THEME.primary; };
    shots.forEach(function (s, k) {
      var cx = n(R + s.x), cy = n(R - s.y), rr = disk.pellet / 2;
      if (opts.highlight === k) {
        out.push('<circle cx="' + cx + '" cy="' + cy + '" r="' + rr + '" fill="' + THEME.primary + '" fill-opacity="1" stroke="' + THEME.dark + '" stroke-width="35"/>');
      } else {
        out.push('<circle cx="' + cx + '" cy="' + cy + '" r="' + rr + '" fill="' + colorFn(s.value) + '" fill-opacity="0.8"/>');
      }
      if (opts.shotNumbers) {
        out.push('<text x="' + cx + '" y="' + cy + '" font-size="' + n(rr * 1.1) + '" fill="#fff" stroke="#000" stroke-width="' + n(rr * 0.05) + '" font-weight="bold" font-family="Arial, Helvetica, sans-serif" text-anchor="middle" dominant-baseline="central">' + (k + 1) + '</text>');
      }
    });

    // 6) mean point of impact (ShotsMiddle): X marker, 300 units wide (scaled with the disk)
    if (opts.showMean && shots.length > 0) {
      var mx = shots.reduce(function (a, s) { return a + s.x; }, 0) / shots.length;
      var my = shots.reduce(function (a, s) { return a + s.y; }, 0) / shots.length;
      var k2 = R / 2275, wUnits = 300 * k2, sc = wUnits / 21.265;
      out.push('<g transform="translate(' + n(R + mx - 150 * k2) + ' ' + n(R - my - 150 * k2) + ') scale(' + n(sc * 1000) / 1000 + ')"><path d="' + CENTER_ICON_PATH + '" fill="#1B1B1B" fill-rule="nonzero" stroke="#FFFFFF" stroke-width="1"/></g>');
    }

    out.push('</svg>');
    return out.join('');
  }

  /** Totals and series, computed the way a results page needs them. */
  function summarize(result, seriesSize) {
    var info = describeResult(result);
    var size = seriesSize || info.seriesShots || 10;
    var shots = usableShots(result);
    var v = shots.map(function (s) { return s.value; });
    var sum = function (a) { return a.reduce(function (x, y) { return x + y; }, 0); };
    var series = [];
    for (var i = 0; i < v.length; i += size) {
      var part = v.slice(i, i + size);
      series.push({ decimal: sum(part) / 10, whole: sum(part.map(function (x) { return Math.floor(x / 10); })), shots: part.map(function (x) { return x / 10; }) });
    }
    var mx = shots.length ? sum(shots.map(function (s) { return s.x; })) / shots.length : 0;
    var my = shots.length ? sum(shots.map(function (s) { return s.y; })) / shots.length : 0;
    return {
      count: shots.length,
      decimalTotal: sum(v) / 10,
      wholeTotal: sum(v.map(function (x) { return Math.floor(x / 10); })),
      series: series,
      best: v.length ? Math.max.apply(null, v) / 10 : null,
      worst: v.length ? Math.min.apply(null, v) / 10 : null,
      meanPoint: { x_mm: mx / 100, y_mm: my / 100 },           // +x right, +y up
      meanRadius_mm: shots.length ? sum(shots.map(function (s) { return Math.hypot(s.x - mx, s.y - my); })) / shots.length / 100 : 0
    };
  }

  root.ShotsAppTarget = {
    DISKS: DISKS, THEME: THEME,
    render: render, summarize: summarize,
    parseStartTime: parseStartTime, describeResult: describeResult, usableShots: usableShots,
    makeCircle: makeCircle, valueFromDistance: valueFromDistance
  };
  if (typeof module !== 'undefined' && module.exports) module.exports = root.ShotsAppTarget;
})(typeof window !== 'undefined' ? window : this);
