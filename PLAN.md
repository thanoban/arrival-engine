# NearWake — Master Product & Implementation Plan

**Package:** `com.nearwake.app`
**Platform:** Android-first (Kotlin + Jetpack Compose)
**Last updated:** 2026-04-24

---

## Strategic Positioning

**Before research:** "NearWake is a focused nap alarm for transit."
**After research:** "NearWake is the Android arrival assurance system — for commuters whether they sleep, read, scroll, work, or zone out during their ride."

This two-audience framing expands the addressable market roughly 5× without diluting the product. A sleeping commuter and a scrolling commuter have the same core failure mode — not noticing the stop — but different alert needs. NearWake serves both with one engine and two modes.

**The twelve-word promise:**
> Wake me before my stop. Tell me if you are not sure.

The second clause is the entire differentiator. No competitor admits uncertainty. NearWake is the first app in this category to make **confidence itself a visible product surface**.

NearWake is a **trust app**, not a safety platform. No SOS, no emergency contacts, no 24/7 monitoring.

---

## The Eight Pillars

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

Pillar 5 is the existing core. Pillars 1–4, 6–8 are new or expanded.

---

## Three-Stage Alert Architecture (Core Engine)

Single-threshold alarms fail. The only safe design is staged escalation.

### Stage A — Approach (far warning)
- **Fires:** 2 stops before destination, OR 500m away, OR 5 min predicted travel remaining (first match)
- **Does:** gentle notification tone, lock-screen update, "Approaching [destination]"
- **Purpose:** user gathers belongings, moves toward door

### Stage B — Imminent (get ready)
- **Fires:** next stop, OR 100–150m away, OR 90 seconds predicted travel remaining
- **Active mode:** stronger tone + haptic pulse, lock-screen emphasis
- **Sleep mode:** audible alarm starts, haptic preamble 30s before audio escalates
- **Purpose:** commitment signal — stand up, reach for bag

### Stage C — Arrival (exit now)
- **Fires:** at stop, OR within 30–50m, OR arrival predicted in under 30 seconds
- **Active mode:** short alert tone, "You have arrived" card
- **Sleep mode:** full unmistakable alarm, max vibration, repeats until dismissed
- **Purpose:** "exit now" signal

### Stage D — Recovery (overshoot)
- **Trigger:** user did not dismiss AND distance from destination increases
- **Does:** Recovery screen opens, shows nearest return stop, one-tap re-arm, "walk back" if < 400m
- **Purpose:** turn the worst failure mode into a managed event

---

## Active Mode vs Sleep Mode

Same engine, different alert personality. Toggle on Live Trip or set default in Settings.

| Aspect | Active Mode (default) | Sleep Mode |
|--------|----------------------|------------|
| Intended state | Awake, distracted | Eyes closed, napping |
| Stage A | Soft notification tone | Soft tone + mild haptic |
| Stage B | Tone + haptic pulse | Haptic preamble + rising audio |
| Stage C | Short confirmation tone | Full alarm until dismissed |
| Screen behavior | Normal | Dims, then wakes for Stage C |
| Overrides silent mode? | No | Yes (user-consented at enable) |
| Lock-screen widget | Visible | Visible + larger, high contrast |
| Bias-early under low confidence | 15% earlier | 25% earlier |
| Auto-suggest | Default | User picks Unmistakable or trip > 30 min |

---

## Confidence Model

Every prediction on every screen shows a confidence level.

| State | Meaning | UI | Engine behavior |
|-------|---------|-----|----------------|
| High | Route-aware, good GPS, consistent motion | Cyan chip | Fire at standard thresholds |
| Medium | One signal weak | Amber chip + banner | Bias 15% earlier |
| Low | Two+ signals weak or underground | Red chip + label | Bias 25% earlier, Stage A fires at Stage B threshold |

**Confidence inputs:**
- GPS accuracy variance over last 60 seconds
- Route snapshot freshness
- Motion signature agreement with expected travel mode
- Signal loss duration (underground detection)
- Route certainty (route-aware vs destination-only)

---

## Pillar-by-Pillar Feature Plan

### Pillar 1 — Depart on Time
- Recurring trip detection from history + saved places
- "Leave in X minutes" notification tied to typical departure
- Manual override ("remind me daily at 07:40 for Home → Work")
- **New modules:** `:domain:commute`, `:data:patterns`, `:feature:departure`

### Pillar 2 — Board Right
- Boarding detection via motion signature (reuses Activity Recognition)
- Direction check 60–90s after arm: compare vehicle heading to route heading
- Soft "are you going the right way?" ping if mismatch
- **Adds:** `BoardingValidator` in `domain:trip`

### Pillar 3 — Ride Awake or Asleep
- Active / Sleep Mode toggle on Live Trip screen
- Lock-screen status widget (destination, ETA, confidence, state color)
- Haptic preamble for Sleep Mode
- **Adds to:** `feature:livetrip`, `data:alerts` gets alert profiles

### Pillar 4 — Transfer Confidently
- Multi-leg awareness from route snapshot
- Pre-transfer alert (1 stop or 90s before transfer)
- Transfer window indicator ("3 min to change platforms")
- Per-leg confidence, graceful fallback when route unknown
- **Adds:** `TransferMonitor` in `domain:trip`, per-leg strip in `feature:livetrip`

### Pillar 5 — Arrive Correctly (Core)
- Three-stage alert system
- Confidence chip on all screens
- Bias-early rule under low confidence
- Underground mode with visible chip
- **Hardens existing:** `domain:trip`, `feature:livetrip`

### Pillar 6 — Recover Gracefully
- Recovery screen as first-class destination
- Nearest return stop, one-tap re-arm for return
- "Stop now and walk back" when overshoot < 400m
- **Adds:** `RecoveryPlanner` in `domain:trip`

### Pillar 7 — Walk the Last Mile
- After Stage C dismiss: lightweight walking guidance to pin
- Distance + heading only — no full map, no voice
- "You have arrived" confirmation within 30m
- **New module:** `:feature:walkfinish`

### Pillar 8 — Confirm Arrival
- Optional per-trip arrival confirmation
- One-shot SMS or share-sheet: "I arrived at [place] at 22:14."
- No continuous tracking, no account, no backend
- **New module:** `:feature:companion`

---

## Cross-Cutting Principles (Non-Negotiable)

1. **Confidence-first.** Every prediction shows confidence.
2. **Bias-early.** When in doubt, alert earlier and label why.
3. **Offline-dignified.** Underground is a named mode with its own UI.
4. **No ads, no paywalls in the alert path — ever.**
5. **No account required for core value.**
6. **Battery-honest.** Expose what we spend.
7. **OEM-aware.** Vendor-specific battery guidance in-product.
8. **One-tap re-arm.** Returning commuters never re-enter a trip.
9. **Privacy-respecting.** Local persistence first. No continuous cloud location.
10. **Fail visible, not silent.**

---

## What We Will Not Build

- Full transit route planner
- Live vehicle map
- Ticketing / payments
- Social / community reports
- AR stop finder
- Mandatory accounts
- Continuous cloud location sharing
- Safety platform framing (SOS, emergency contacts, 24/7 monitoring)
- Ads of any kind
- Wear OS (post-1.0)
- Cloud sync (post-1.0, possibly never)

---

## Technical Implementation

### State Machine

```
Idle → Armed → WaitingForMovement → MonitoringLowPower
                                           ↓
                                   MonitoringApproach
                                           ↓
                             Stage A → Stage B → Stage C → Completed
                                                      ↓
                                                  Recovery → Completed
```

All states persist to Room. Extend `TripMonitoringService` to track current stage.

### Monitoring Duty Cycle

| Situation | Location | Motion | Network |
|-----------|----------|--------|---------|
| Armed, no movement | Coarse every 120s | Activity Recognition idle | None |
| Moving, far from destination | Coarse every 60–120s | Active Recognition | Route refresh every 5 min |
| Approach window entered | Precise burst 30–60s (max 90s) | Active | Route refresh on entry |
| Stage B reached | Continuous precise | Active | No new network |
| Stage C reached | Continuous precise until dismiss | Active | No new network |
| Underground / low signal | Fallback to last velocity + motion | Elevated weight | None |

### Confidence Thresholds

- GPS accuracy variance > 50m over last minute → Medium
- GPS accuracy variance > 100m or no fix for 30s → Low
- Route snapshot age > 15 min → Medium
- Motion mode mismatch for 120s → Medium
- No GPS fix for 60s while previously in vehicle → Low + underground chip

### Permission Copy (Research-proven)

1. **Notifications:** "NearWake can't wake you near your stop unless Android is allowed to show alarms."
2. **While-in-use location:** "Location lets NearWake know where your trip is starting and where your destination is."
3. **Background location (screen-off arm only):** "To alert you while your screen is off or you're asleep, NearWake needs background location during active trips only."
4. **OEM battery (degraded only):** "Your phone may delay trip alarms to save battery. Turn off battery restrictions for NearWake only if you want maximum reliability."

### New Modules

```
:domain:commute        # recurring trip / departure inference
:data:patterns         # trip clustering, Room-backed
:feature:departure     # "leave now" surface
:feature:walkfinish    # last-mile walking
:feature:companion     # arrival sharing (SMS / share sheet)
```

### Expanded Existing Modules (Additive Only)

- `domain:trip`: `BoardingValidator`, `TransferMonitor`, `RecoveryPlanner`, `ConfidenceModel`
- `data:alerts`: Active and Sleep alert profiles + Stage A/B/C separation
- `feature:livetrip`: confidence chip, underground chip, per-leg strip, Sleep Mode toggle, lock-screen widget
- `feature:alerts`: RecoveryScreen becomes first-class with real actions
- `feature:settings`: OEM Reliability page, alert personality defaults

**No Room schema, DataStore keys, Hilt modules, or navigation routes changed — additive only.**

---

## Release Waves

### Wave A — Trust Core (4–5 weeks)
- Confidence chip on Live Trip
- Three-stage alert architecture (Stage A/B/C)
- Bias-early rule + banner
- Underground-mode chip
- Recovery screen as first-class
- OEM Reliability page
- One-tap re-arm on Home

**Exit criteria:** Zero late alerts in internal field tests; confidence model working end-to-end.

### Wave B — Dual Mode + Lock Screen (3–4 weeks)
- Active / Sleep Mode toggle
- Alert personality profiles (stage-by-stage)
- Lock-screen status widget
- Haptic preamble
- Accessibility pass for groggy / low-vision users

**Exit criteria:** Commuter can nap through a 40-minute trip; active-mode commuter gets clean two-stage nudge.

### Wave C — Route + Boarding (4–5 weeks)
- Multi-leg awareness and per-leg strip
- Pre-transfer alerts
- Boarding validation + wrong-direction check
- Per-leg confidence display

**Exit criteria:** Two-transfer commute completes with correct stage alerts on 9 of 10 real field runs.

### Wave D — Journey Extension (3–4 weeks)
- Recurring trip detection
- Departure reminders
- Walk-the-last-mile guidance
- Arrival confirmation (SMS / share sheet)

**Exit criteria:** User arms door-to-door commute in under 5 taps; returning commuters re-arm in 1 tap.

### Wave E — Polish and Public Beta (2–3 weeks)
- Real Google Places Autocomplete
- Trip log CSV export
- "Why this alert fired" diagnostics
- Full field test protocol
- Play listing with positioning and commitments

**Exit criteria:** Under 5% late-alert rate across 30+ real trips; 0% not-fired rate; 4.5+ private beta rating.

### Post-1.0 (Explicitly Deferred)
Wear OS relay, commute analytics, optional backend sync, second-device companion.

---

## Field Test Protocol

No public launch until:

- 30+ real trips across bus, metro, rail, car-passenger
- 10+ trips with screen off / sleeping user simulation
- 10+ trips with poor signal, tunnels, or underground legs
- Recorded per trip: predicted lead, actual lead, OEM device, permission state, battery-saver state, confidence mode at each stage
- Failure classification: late / too early / not fired / background killed / route changed / user confused / trip recovered
- **Pass bar:** predictably conservative — users say "slightly early sometimes", never "disastrously late"

---

## Module Structure

```
:app                          # Application class, Hilt component, NavGraph
:core:common                  # Extensions, result types, coroutine utils, constants
:core:designsystem            # Theme, typography, colors, shared Composables, icons
:core:ui                      # Reusable screen-level Composables
:core:network                 # OkHttp/Retrofit (for future backend)
:core:database                # Room DB, all DAOs, entities, type converters
:core:testing                 # Fake providers, test utilities, shared test fixtures

:domain:trip                  # TripEngine, TripSession, state machine, use cases
:domain:location              # Location/Geofence/Activity models, interfaces
:domain:routing               # RoutingDataSource interface, RouteSnapshot, Stop
:domain:commute               # [Wave D] recurring trip / departure inference

:data:location                # FusedLocationDataSource, GeofenceDataSource impl
:data:motion                  # ActivityRecognitionDataSource impl
:data:routing                 # GoogleTransitDataSource (stub), LocalRouteCache
:data:alerts                  # AlertOrchestrator, NotificationHelper, TripMonitoringService
:data:analytics               # DiagnosticsLogger
:data:patterns                # [Wave D] trip clustering, Room-backed

:feature:onboarding           # Welcome + value prop
:feature:permissions          # Permission rationale, settings-recovery UX
:feature:places               # Place search (Google Places Autocomplete)
:feature:tripsetup            # Trip config: destination, lead time, alert prefs
:feature:livetrip             # Live monitoring: state, mode, battery, ETA
:feature:alerts               # Alert/alarm screen, RecoveryScreen
:feature:history              # Past trips list + trip detail
:feature:settings             # Alert intensity, OEM reliability, preferences
:feature:diagnostics          # QA screen: engine state, geofence list, log stream
:feature:departure            # [Wave D] departure reminders
:feature:walkfinish           # [Wave D] last-mile walking guidance
:feature:companion            # [Wave D] arrival confirmation (SMS/share)
```

---

## Key Design Decisions

| Decision | Choice | Why |
|---|---|---|
| Location during monitoring | Geofences + Activity Recognition only | OS-managed, ~0 battery, survives process death |
| Precise GPS | Short burst ≤90s, only near approach | Battery-efficient; FLP stops after decision |
| Route-aware | Destination-only MVP, interface ready | Removes external data dependency |
| Backend | None for MVP | Eliminates server as single point of alert failure |
| State persistence | Room DB | Survives process death, reboot, OOM kill |
| Offline bias | Alert earlier when degraded | Better to wake slightly early than miss stop |
| Maps rendering | Only on trip setup screen | Zero tile API calls during monitoring |
| Confidence | Visible on all screens | Only app honest about its own uncertainty |
