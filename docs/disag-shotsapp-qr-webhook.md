# DISAG ShotsApp QR to JSON (n8n webhook)

Handover document for developing the results webpage. It explains what the webhook does, how to call it, what the returned data means, and what is verified versus assumed.

**Legend:** `[verified]` = confirmed by reading the ShotsApp code or by a real request/response. `[assumed]` = plausible but not confirmed, check before relying on it.

---

## 1. What this is

At the shooting club (no internet), the DISAG scoring system writes a competitor's result into a **QR code**. Scanning that QR code in the DISAG ShotsApp normally sends the code to Disag's server, which decodes and stores it.

The QR code cannot be decoded without Disag's server `[verified]`. The ShotsApp itself does not decode it. It posts the raw QR string to Disag's API and gets the decoded result back. Offline, the app only queues the raw string ("Zwischenspeichern") and sends it later.

This project adds an **n8n webhook** that does that same API call for us, so our webpage can send a QR link and receive the decoded result as clean JSON.

```
Club (offline)                 Home / online
─────────────                  ──────────────────────────────────────────
Scoring device → QR code  →  Webpage  →  n8n webhook  →  Disag API  →  JSON
   (data is IN the code)      POST url     (Bearer token)   decodes       back to
                                                                          webpage
```

Key consequence: **nothing is uploaded from the club.** The QR code is the data carrier. It has to be kept (as image or link) until there is internet, then sent to the webhook.

---

## 2. The QR code format

The QR code contains a URL `[verified]`:

```
https://qr.shotsapp.de/?uuid=<UUID>#<base64-payload>
```

- `uuid`: unique ID of the result (e.g. `4EB58DA6-40B7-4A71-AFF3-A65755137C3F`). It is also returned as `id` / `data.uuid`.
- Everything after `#` is a base64 payload of about 350 bytes. Its beginning is readable: `100_4#2026-10-14 20:26:47Z`, followed by control characters and high-entropy binary. The binary part is encrypted or encoded, and only Disag's server can read it. Do not try to decode it locally.
- **Send the complete link, including the `#...` part, unchanged.** The `#fragment` is never sent in a normal HTTP request line, so the link must go in the request **body**, never as a URL query.
- Opening the link in a browser only redirects to `https://www.shotsapp.de/qr/`, a landing page saying the ShotsApp is required. It shows no results.

---

## 3. The webhook

| | |
|---|---|
| n8n workflow | "Disag ShotsApp QR zu JSON", ID `hpSg9U5F33wPIpU1` |
| n8n editor | `https://n8n.fkreuzer.dev/workflow/hpSg9U5F33wPIpU1` |
| Production URL | `POST https://n8n.fkreuzer.dev/webhook/disag-result` (only works while the workflow is **active**) |
| Test URL | `POST https://n8n.fkreuzer.dev/webhook-test/disag-result` (click "Execute workflow" in n8n first; it listens for one call only) |
| Request body | JSON: `{ "url": "<full QR link>" }` |
| Content-Type | `application/json` |
| Auth header | If Header Auth is enabled on the webhook (recommended): send the header configured in the n8n credential, e.g. `X-Api-Key: <secret>` `[assumed: confirm header name in n8n]` |

The webhook also accepts the link as `body.data` or as query parameter `url`, but use `body.url`.

### Example: curl

```bash
curl -X POST "https://n8n.fkreuzer.dev/webhook/disag-result" \
  -H "Content-Type: application/json" \
  -H "X-Api-Key: YOUR_SECRET" \
  -d '{"url":"https://qr.shotsapp.de/?uuid=...#..."}'
```

### Example: fetch (browser)

```js
const res = await fetch("https://n8n.fkreuzer.dev/webhook/disag-result", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    "X-Api-Key": secret            // typed in by the user; do not hard-code
  },
  body: JSON.stringify({ url: qrLink })
});
if (!res.ok) { /* see error table below */ }
const result = await res.json();
```

### Responses

| Status | Meaning | Body |
|---|---|---|
| 200 | Decoded result | The result JSON (section 4) |
| 400 | Body missing or `url` does not start with `https://qr.shotsapp.de/` | `{"error":"invalid_input","message":"..."}` |
| 403 | Header Auth enabled and header missing or wrong | n8n default |
| 502 | Disag rejected the request: Bearer token invalid or expired, QR code invalid, or Disag server unreachable/erroring | `{"error":"disag_error","message":"..."}` |

Only a **502** distinguishes "Disag problem" from "our problem". On a 502, check the token first (section 7). During testing Disag once answered `500 {"message":"Server Error"}` for a valid code, which a retry resolved `[verified]`, so retry once before treating it as permanent.

Calls from a browser to a different origin need CORS. n8n answers the preflight automatically. By default all origins are allowed. Restrict "Allowed Origins" in the webhook node options to the webpage's domain.

---

## 4. Response format

Real example (trimmed to 3 of 40 shots) `[verified]`:

```json
{
  "id": "4EB58DA6-40B7-4A71-AFF3-A65755137C3F",
  "discipline_id": "100_4",
  "data": {
    "uuid": "4EB58DA6-40B7-4A71-AFF3-A65755137C3F",
    "disciplineId": "100_4",
    "startTime": "2026-10-14T20:26:47Z",
    "startTimeRaw": "2026-10-14 20:26:47Zlg10",
    "results": [
      { "valid": 2, "offset": 0,  "x": -238, "y": -236, "value": 96,  "flag": 2 },
      { "valid": 2, "offset": 37, "x": -238, "y": 200,  "value": 97,  "flag": 2 },
      { "valid": 2, "offset": 43, "x": 110,  "y": 103,  "value": 104, "flag": 2 }
    ]
  }
}
```

### Fields

| Field | Meaning |
|---|---|
| `id`, `data.uuid` | Unique result ID, the same as the `uuid` in the QR link. Use it as the primary key and for de-duplication. `[verified]` |
| `discipline_id`, `data.disciplineId` | Discipline code, here `100_4`. The exact meaning is not documented. The example has 40 shots on a 10 m air rifle target (Luftgewehr). `[assumed]` |
| `data.startTime` | Start of the session, **UTC** (the `Z`). Cleaned by the workflow into ISO 8601. 2026-10-14T20:26:47Z is 22:26:47 in Germany (CEST). `[verified]` |
| `data.startTimeRaw` | Original string. Disag's `startTime` has stray control characters (``) followed by `lg10`, and the workflow strips the control characters, which leaves `Zlg10`. Possibly `lg` = Luftgewehr and `10` = 10 m. Not used by anything, kept for completeness. `[assumed meaning]` |
| `data.results[]` | One entry per shot, in firing order. 40 entries in the example. `[verified]` |
| `value` | **Ring value × 10.** `104` means 10.4, `96` means 9.6, max in the example is `105`. `[verified]` |
| `x`, `y` | Shot position relative to the target center, in **hundredths of a millimetre** (1 unit = 0.01 mm). `[verified]` through the value/distance fit below. The axis direction is `[assumed]`: x positive = right, y positive = up. Check against a shot whose position is known. |
| `offset` | Looks like the **seconds since the previous shot** (0 for the first shot). The 40 offsets sum to 1727 s, about 29 minutes. `[assumed]` |
| `valid`, `flag` | Both `2` for every shot in the example. Meaning unknown. Treat as opaque, but keep them. `[unknown]` |

### Unit check (why the numbers are trustworthy)

For all 40 shots, `value` against the distance from center `d = sqrt(x² + y²)` fits a straight line with correlation −0.999:

```
value ≈ 109.6 − 0.0402 · d        (d in 1/100 mm)
```

So one tenth of a ring (value step of 1) corresponds to about 24.9 units, which is 0.25 mm of distance, and one whole ring to about 2.49 mm. That matches the 2.5 mm ring spacing of a 10 m air rifle target. To place ring boundaries when drawing a target, use the empirical distance for ring value `k`: `d_k ≈ (10.96 − k) · 248.7` units. The official ISSF target dimensions are the authoritative source if exact rings are needed.

---

## 5. Computing results

The data has **no totals and no series boundaries**. Compute them in the webpage:

```js
function summarize(result, seriesSize = 10) {
  const shots = result.data.results;
  const v = shots.map(s => s.value);                       // ring × 10

  const total = v.reduce((a, b) => a + b, 0) / 10;         // decimal total
  const totalWhole = v.reduce((a, b) => a + Math.floor(b / 10), 0);

  const series = [];
  for (let i = 0; i < v.length; i += seriesSize) {
    const part = v.slice(i, i + seriesSize);
    series.push({
      decimal: part.reduce((a, b) => a + b, 0) / 10,
      whole: part.reduce((a, b) => a + Math.floor(b / 10), 0),
      shots: part.map(x => x / 10),
    });
  }

  const cx = shots.reduce((a, s) => a + s.x, 0) / shots.length;   // mean point of impact
  const cy = shots.reduce((a, s) => a + s.y, 0) / shots.length;
  const meanRadius = shots.reduce((a, s) => a + Math.hypot(s.x - cx, s.y - cy), 0) / shots.length;

  return { count: shots.length, total, totalWhole, series,
           best: Math.max(...v) / 10, worst: Math.min(...v) / 10,
           average: total / shots.length,
           mpi: { x: cx, y: cy }, meanRadius };           // x, y, meanRadius in 1/100 mm
}
```

Series of 10 is the usual air rifle format, but it is an `[assumed]` split. The data itself does not mark series.

### Expected output for the real example (use as a test)

| | |
|---|---|
| Shots | 40 |
| Decimal total | **380.1** |
| Whole rings | **363** (of 400) |
| Series 1 / 2 / 3 / 4 (decimal) | 92.8 / 99.3 / 91.5 / 96.5 |
| Series 1 / 2 / 3 / 4 (whole) | 87 / 97 / 87 / 92 |
| Best / worst / average shot | 10.5 / 6.7 / 9.5 |
| Mean point of impact (x, y) | (−9, −89) = 0.09 mm left, 0.89 mm low (under the axis assumption) |
| Mean radius around it | 351 (3.5 mm) |
| Largest distance between two shots | 1592 (15.9 mm) |

### Drawing the shots on a target

Convert to millimetres with `mm = units / 100`. Plot with SVG `cx = center + x/100 * scale`, `cy = center − y/100 * scale`, assuming y points up. Check the direction on real data first. Shot (x=777, y=−715, value 6.7) is the lowest score in the example and the farthest from center.

---

## 6. What the workflow does (n8n nodes)

1. **QR-Link empfangen** (Webhook, POST `disag-result`, responds via Respond node)
2. **Eingabe normalisieren** (Set): `qr_url = body.url ?? body.data ?? query.url`, trimmed
3. **Ist ein ShotsApp-QR-Link?** (IF): `qr_url` must start with `https://qr.shotsapp.de/`. If false, **Antwort 400**.
4. **Disag Ergebnis dekodieren** (HTTP Request): `POST https://shotsapp.disag.de/api/results/preview`, multipart/form-data with the single field `data = qr_url`, header `Accept: application/json`, Bearer-token credential. The response is read as **text** because Disag's JSON is invalid (see below). On error, **Antwort 502**.
5. **JSON bereinigen** (Code): removes control characters (` `–``), parses the JSON, rewrites `startTime` to ISO and keeps the original as `startTimeRaw`. Parse failure leads to a 502.
6. **Ergebnis als JSON zurueckgeben** (Respond): status 200 with the JSON.

Disag's raw response contains raw control characters inside `startTime`, so a normal JSON parser rejects it. That is why step 4 reads text and step 5 cleans it `[verified]`.

---

## 7. Disag API facts (taken from the ShotsApp code)

All `[verified]` from the ShotsApp 3.1.2 JavaScript bundle (React Native / Hermes). The API is internal and undocumented.

- Base URL: `https://shotsapp.disag.de/api/`
- **Login** (only needed to obtain a token): `POST /api/token`, multipart/form-data with `email`, `password`, `device_name`. The response body is the token (a Laravel Sanctum token). Each call creates a new token.
- **Decode / preview**: `POST /api/results/preview`, multipart/form-data with field `data` (the full QR string), headers `Authorization: Bearer <token>`. This is what the webhook calls.
- **Save**: `POST /api/results` with the same body **stores the result in the Disag account**, like scanning in the app. **The webhook does not use it.** Do not add it unless saving to the Disag account is wanted.
- The app marks a result as "already scanned" locally. Whether the server rejects duplicates is unknown.
- Older QR codes (`qr.disag.eu/qr.php?v=2...`) use a different path via an S3 bucket. Not supported by this workflow.

Bearer token: kept as a credential in n8n (never in the page or in this repo). If it expires or is revoked, every call returns 502. Create a new one with the login call and update the credential.

---

## 8. Security

- A webhook URL is public. Without Header Auth, anyone who knows the URL can use the Disag account to decode QR codes.
- Enable **Header Auth** on the webhook node with a long random secret. Do not hard-code the secret in public page source, because anyone viewing the source can read it. Prefer typing it into the page once and storing it in the browser's local storage, or let a server-side script of the page owner call the webhook.
- Set "Allowed Origins" on the webhook to the page's domain. This only stops other websites' scripts, not curl.
- For stronger protection put authentication in front of n8n (e.g. Cloudflare Access or a reverse-proxy login for `/webhook/disag-result`).
- Never commit the Bearer token, the webhook secret, or the Disag password.
- This is an unofficial use of an undocumented API. It can change or be blocked, and Disag's terms may not allow it. Asking Disag for an official export is the safe long-term route.

---

## 9. Offline workflow at the club

The QR code can only be decoded online, but it can be **captured offline**:

1. At the club, scan or photograph the QR code with the webpage (or just copy the link).
2. A QR reader library that is bundled with the page (not loaded from a CDN) reads the image in the browser and yields the link string. This works without internet.
3. Store the link in a local queue (IndexedDB or local storage) together with a timestamp.
4. Once online, send each queued link to the webhook, store the JSON result, and mark the queue entry as done (use `id` / `uuid` to avoid duplicates).
5. Compute the summary (section 5) and publish it.

Keep the original QR image or link until the result has been successfully decoded and stored. There is no other way to get the data back.

---

## 10. Troubleshooting

| Symptom | Likely cause |
|---|---|
| Nothing happens, request hangs | Production URL used while the workflow is inactive, test URL used without clicking "Execute workflow", or an n8n error output is not connected (the 502 response must be wired from "Disag Ergebnis dekodieren"). |
| 400 | `url` missing, or the link does not start with `https://qr.shotsapp.de/`. Send JSON, not form data. |
| 403 | Header Auth is on and the header is missing or has the wrong value or name. |
| 502 | Expired or invalid Bearer token, invalid QR code, or Disag server error. Retry once, then check the token (section 7). |
| `JSON.parse` fails in the page | The page is not using this webhook's response, or is calling Disag directly. Only the webhook returns cleaned JSON. |
| Link breaks | The link was shortened, the `#...` part was cut off, or it was sent as a query string. Send it complete, in the JSON body. |
| n8n will not save changes | The workflow is open in the n8n editor and being edited. Close it and retry. |

---

## 11. Verified vs. assumed (summary)

| Verified | Assumed or unknown |
|---|---|
| QR content is `https://qr.shotsapp.de/?uuid=...#payload` | Meaning of `discipline_id` `100_4` and of `lg10` |
| Only Disag's server can decode it; the app posts it to `/api/results/preview` | Direction of the x/y axes |
| `value` = ring × 10 | `offset` = seconds since the previous shot |
| `x`, `y` in 1/100 mm (value/distance fit, r = −0.999) | Series of 10 shots (not in the data) |
| `startTime` is UTC and has control characters in Disag's response | Meaning of `valid` and `flag` (both 2) |
| Workflow status codes 200, 400, 502 | Whether Disag rejects duplicate scans server-side |