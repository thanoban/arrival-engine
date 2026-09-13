# NearWake Product Compare Reference

Use this file to quickly answer:
- What exact problem are we solving?
- Who is the user?
- What does the app do and not do?
- How do we compare to competitors?
- What are our non-negotiable commitments?

---

## 1. One-Line Product Definition

NearWake is the Android arrival assurance system — for commuters whether they sleep, read, scroll, work, or zone out during their ride.

**The twelve-word promise:**
> Wake me before my stop. Tell me if you are not sure.

---

## 2. The Core Problem

The main problem is not navigation. The main problem is not route planning.

The real problem:
- Users miss stops because they fall asleep
- Users miss stops because they get distracted
- Users cannot depend on themselves to watch the route continuously
- Most transit apps help users plan, but do not specialize in reliably alerting them at the right time

NearWake reduces that failure point. It is a **trust app**, not a safety platform.

---

## 3. User Situations

**Primary users:**
- Bus and train commuters who nap (sleeping commuter)
- Bus and train commuters who are awake but distracted — scrolling, reading, on a call (active commuter)

**Strongest use cases:**
- Daily commuters, night or early-morning travel, long journeys
- Unfamiliar routes, travel in new cities
- Parents whose kids travel alone and want arrival confirmation

**Key insight:** the market thinks "arrival alarm = nap app." That is a small niche. Most commuters are awake but distracted. By serving both, NearWake addresses the full commuter population.

---

## 4. The Eight Pillars of Arrival Assurance

Every feature must fit one of these pillars. Nothing else ships.

| # | Pillar | User question | Competitor gap |
|---|--------|--------------|----------------|
| 1 | Depart on time | "When do I leave?" | Only transit apps attempt this, badly |
| 2 | Board right | "Am I on the right vehicle?" | Nobody solves this |
| 3 | Ride awake or asleep | "Can I relax now?" | Nobody markets dual modes |
| 4 | Transfer confidently | "Do I change here?" | Geofence apps cannot do this |
| 5 | Arrive correctly | "Is this my stop?" | Existing apps, but fragile |
| 6 | Recover gracefully | "I missed it — now what?" | Nobody owns this word |
| 7 | Walk the last mile | "How do I get from stop to door?" | Gap in both clusters |
| 8 | Confirm arrival | "Did I make it?" | Not solved anywhere |

---

## 5. Three-Stage Alert Architecture

Single-threshold alarms fail. NearWake uses staged escalation — the only design that reliably works.

### Stage A — Approach (far warning)
- **Fires:** 2 stops before destination, OR 500m away, OR 5 min predicted travel remaining
- **Does:** gentle notification, lock-screen update "Approaching [destination]"
- **Purpose:** user starts gathering belongings

### Stage B — Imminent (get ready)
- **Fires:** next stop, OR 100–150m away, OR 90 seconds predicted remaining
- **Active mode:** stronger tone + haptic pulse
- **Sleep mode:** haptic preamble 30s, then rising audio
- **Purpose:** stand up, reach for bag

### Stage C — Arrival (exit now)
- **Fires:** at stop, OR within 30–50m, OR < 30 seconds predicted
- **Active mode:** short tone, "You have arrived" card
- **Sleep mode:** full alarm, max vibration, repeats until dismissed
- **Purpose:** "exit now" signal

### Stage D — Recovery (overshoot)
- **Trigger:** user did not dismiss AND distance from destination increases
- **Does:** Recovery screen opens, nearest return stop, one-tap re-arm, "walk back" if < 400m
- **Purpose:** turn the worst failure into a managed event

**Why this beats every competitor:**
- GPS Alarm, Naplarm, Wake Me There, StopAlert: single-stage only — if late, you're stranded
- Transit, Moovit, Citymapper: staged but buried in map-first UI with ads and clutter
- NearWake: three stages + recovery, dedicated UI, honest about confidence

---

## 6. Active Mode vs Sleep Mode

Same engine, different alert personality.

| Aspect | Active Mode (default) | Sleep Mode |
|--------|----------------------|------------|
| Intended state | Awake, distracted | Eyes closed, napping |
| Stage A | Soft notification tone | Soft tone + mild haptic |
| Stage B | Tone + haptic pulse | Haptic preamble + rising audio |
| Stage C | Short confirmation tone | Full alarm until dismissed |
| Overrides silent mode? | No | Yes (user-consented) |
| Bias-early under low confidence | 15% earlier | 25% earlier |

---

## 7. Confidence Model (The Visible Differentiator)

Every prediction on every screen shows a confidence level. No competitor does this.

| State | UI | Engine behavior |
|-------|-----|----------------|
| High | Cyan chip | Fire at standard thresholds |
| Medium | Amber chip + "Alerting earlier" banner | Bias 15% earlier |
| Low | Red chip + "Underground mode" label | Bias 25% earlier, Stage A at Stage B threshold |

**Why this wins:** Transit reviews say "DO NOT TRUST ARRIVAL TIME." Moovit says "very inaccurate." These apps show confident ETAs that are wrong. NearWake shows honest confidence that is occasionally conservative — and users learn to trust it.

---

## 8. What NearWake Is

- An offline-first arrival assurance app
- A trust tool: calm when idle, alive when monitoring, unmistakable when alerting
- An honest system: confidence is visible, degraded modes are named, failures are surfaced
- Battery-conscious: OS-managed geofences, Activity Recognition, precise GPS only in short bursts

---

## 9. What NearWake Is Not

- Not a live-map navigator
- Not a transit route planner (no schedule lookups, no trip planning)
- Not a ticketing app
- Not a social platform
- Not a cloud tracking service
- Not a safety/SOS platform — no emergency contacts, no 24/7 monitoring
- Not a lifestyle app — every pixel serves status communication

---

## 10. Current Differentiators

Features no competitor currently has:

1. **Confidence-first UX** — every prediction shows confidence level with labeled fallback behavior
2. **Three-stage alert system** — Approach / Imminent / Arrival, distinct from single-threshold competitors
3. **Dual Active/Sleep modes** — same engine, two alert personalities per user state
4. **Bias-early rule** — when uncertain, alert earlier and tell the user why
5. **Underground-dignified mode** — signal loss is a named, visible mode, not a silent failure
6. **Missed-stop recovery** — first-class Recovery screen with re-arm and walk-back
7. **Boarding validation** — wrong-direction check after trip arms
8. **Transfer awareness** — pre-transfer alerts for multi-leg journeys
9. **OEM-specific reliability guidance** — Samsung, Xiaomi, Oppo, etc. battery exemption steps shown in-product
10. **No-ads commitment** — stated publicly, in the Play listing, enforced in product
11. **Configurable engine thresholds** — all bias multipliers and geofence radii in `ThresholdConfig`; no hardcoded constants in the alert engine
12. **Icon-driven, text-minimal UI** — compact information hierarchy; one dominant signal per screen; 56dp place search rows; status communicated through icons + color, not chip labels

---

## 11. Cross-Cutting Commitments (Non-Negotiable)

These are product commitments that go on the Play Store listing:

1. **Confidence-first.** Every prediction shows confidence.
2. **Bias-early.** When in doubt, alert earlier and label why.
3. **Offline-dignified.** Underground is a named mode with its own UI.
4. **No ads, no paywalls in the alert path — ever.**
5. **No account required for core value.**
6. **Battery-honest.** Expose what we spend.
7. **OEM-aware.** Vendor-specific battery guidance in-product.
8. **One-tap re-arm.** Returning commuters never re-enter a trip.
9. **Privacy-respecting.** Local persistence first. No ambient, public, or indefinite cloud location.
10. **Fail visible, not silent.**

---

## 12. What We Will Not Build

- Full transit route planner (Citymapper trap)
- Live vehicle map (Transit / Moovit trap)
- Ticketing / payments
- Social / community reports (Waze-style)
- AR stop finder
- Mandatory accounts
- Ambient, public, or indefinite cloud location sharing. A future trip-scoped share may be explicit, recipient-scoped, revocable, and auto-expiring.
- Safety platform — SOS, emergency contacts, 24/7 monitoring (liability trap)
- Ads of any kind
- Wear OS (post-1.0)
- Cloud sync (post-1.0, possibly never)

If a feature request does not clearly fit one of the eight pillars, it does not ship.

---

## 13. Feature Matrix vs Competitors

| Capability | Transit | Citymapper | Moovit | Naplarm | GPS Alarm | Wake Me There | StopAlert | **NearWake** |
|-----------|---------|-----------|--------|---------|-----------|---------------|-----------|------------|
| Destination alarm | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | **✓** |
| Route-aware stops | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Transfer alerts | ✓ | ✓ | ✓ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Three-stage alerts | partial | partial | partial | ✗ | ✗ | ✗ | ✗ | **✓** |
| Sleep / Active mode distinction | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Confidence shown to user | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Bias-early under uncertainty | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Underground-dignified mode | partial | partial | partial | ✗ | ✗ | ✗ | ✗ | **✓** |
| Missed-stop recovery | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | ✗ | **✓** |
| Walk the last mile | ✗ | ✓ (heavy) | ✓ (heavy) | ✗ | ✗ | ✗ | ✗ | **✓ (light)** |
| Departure reminder | ✓ | ✓ | ✓ | ✗ | partial | ✗ | ✗ | **✓** |
| Arrival confirmation to contact | ✗ | ✓ (heavy) | ✗ | ✗ | ✗ | ✗ | ✗ | **✓ (light)** |
| OEM battery guidance in-product | ✗ | ✗ | ✗ | ✗ | partial | ✗ | ✗ | **✓ vendor-specific** |
| No ads in alert path | ✗ | partial | ✗ | partial | ✗ | ? | ? | **✓ committed** |
| Works without account | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | ✓ | **✓** |
| Process-death recovery | partial | ? | partial | ✗ | partial | ? | ✗ | **✓** |

NearWake does not beat Transit on map features or Moovit on ticketing. It beats all of them on trust, transparency, and recovery — which is what the review evidence says users actually want.

---

## 14. Technology Choices (Frozen for MVP)

| Layer | Choice | Constraint |
|-------|--------|-----------|
| Language | Kotlin | Android-only MVP |
| UI | Jetpack Compose + Material 3 | Dark-first, no light theme yet |
| DI | Hilt | All modules use `@HiltViewModel`, `@HiltAndroidApp` |
| Database | Room | Entities frozen — no schema changes |
| State | DataStore | Keys frozen |
| Background | WorkManager + ForegroundService | Survives process death |
| Location | FusedLocationProvider (bursts only) + GeofencingClient | Never continuous GPS |
| Motion | Activity Recognition Transition API | OS-managed |
| Network | OkHttp + Retrofit (core:network) | Backend deferred |
| Maps | Google Maps SDK + Places Autocomplete | Setup only, not during monitoring |

---

## 15. What Parts Can Still Change

- UI / design system (modernization pass planned)
- Alert personality profiles (Active vs Sleep mode details)
- Geofence radius tuning (300–500m destination, 1.5km approach)
- Confidence threshold calibration (from field test data)
- Permission copy wording
- New feature modules (Pillars 1–4, 6–8) — all additive, no breaking changes

---

## 16. Sri Lanka-First Data Direction

If NearWake focuses on Sri Lanka as its strongest launch market, the search and data layers should become more local instead of depending only on generic global place search.

The recommended direction is:

- public official transport documents now
- normalized NearWake-owned schema
- route-number and landmark-first search
- official structured exports later when available

Use [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md) for:

- why PDF is enough to start but not ideal forever
- which official sources to use first
- what to ask NTC and Sri Lanka Railways for
- how to structure the production data pipeline

For the consolidated execution strategy — beating current Play Store apps, the Sri Lanka launch plan (including the 2026 Lanka Metro Transit / LMT-GO landscape), and pipeline upgrades — see [MARKET_STRATEGY_PLAN.md](MARKET_STRATEGY_PLAN.md).
