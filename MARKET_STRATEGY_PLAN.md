# NearWake Market Strategy Plan

Last updated: 2026-09-13

This is the master execution plan connecting three things:

1. **Beat the market** — what current Play Store apps get wrong, and how NearWake solves the real problem better
2. **Sri Lanka plan** — a dedicated plan for Sri Lankan users as the strongest launch market
3. **Pipeline improvements** — how the data pipeline evolves to serve both

Related docs: [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md) (competitor matrix, pillars),
[SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md) (data sources, NTC/Railways contacts),
[PRODUCT_EXPANSION_ROADMAP.md](PRODUCT_EXPANSION_ROADMAP.md) (feature waves).

---

## Section 1 — Beat the Market: Solve the Real Problem Better

### 1.1 What "many apps like this" actually are (Play Store, 2026)

The arrival-alarm category on the Play Store is crowded but often shallow. This working comparison was reviewed on 2026-09-13 and must be revalidated before it is used in public marketing:

| App | What it is | Observed limitation or risk to validate |
|-----|-----------|------------------------------------------|
| Wake Me There (MapFactor) | Location-radius alarm | Public product material describes one perimeter alarm and configurable location-update frequency; some Play reviews report background-alarm and battery concerns |
| Naplarm | Location-based nap alarm | Appears centered on continuous location updates; validate current background and battery behavior on representative devices |
| GPS Alarm (Technomad) | Location reminder | Single-threshold; no staged escalation, no recovery |
| EasySleep | Offline GPS arrival alarm | Adjustable radius only (50m–1km); single stage, no confidence |
| Transit / Moovit / Citymapper | Map-first transit planners | ETA quality and alert-path complexity vary by city, feed quality, and release; benchmark them corridor by corridor rather than treating review anecdotes as universal facts |

**Recurring category risks that NearWake must benchmark explicitly:**

1. **Single-threshold alarm.** One geofence, one alert. If you sleep through it or GPS drifts, you are stranded. No approach warning, no escalation, no recovery.
2. **Fragile background execution.** Continuous GPS can drain batteries, while heavily deferred background work can miss timely delivery. NearWake must prove its own behavior under modern Android battery management on Samsung, Xiaomi, and Oppo devices.
3. **Fake confidence.** They show one number (distance or ETA) with no honesty about signal quality. When they're wrong, the user learns to distrust the whole category.

### 1.2 The real problem, restated

The real problem is not "make an alarm ring at a location." The real problem is:

> **A commuter must be able to hand over responsibility for their stop — and trust that handover completely — even when asleep, offline, on a phone with aggressive battery management, on a route with no schedule data.**

That is a *trust and reliability* problem, not only a geofencing problem. NearWake's differentiation must be demonstrated through delivery evidence, honest state, and recovery behavior.

### 1.3 How NearWake solves it better (and what's already built)

Each category flaw maps to an architectural answer that is already in the codebase — this is our moat, because it cannot be patched into a single-geofence app:

| Category flaw | NearWake answer | Status in code |
|---------------|-----------------|----------------|
| Single-threshold alarm | Three-stage escalation (Approach → Imminent → Arrival) + Recovery state for overshoot | ✅ `TripStateMachine`, `AlertStageEvaluator`, `RecoveryPlanner` |
| Background death | Geofences + Activity Recognition + foreground service while actively monitoring + `TripRecoveryWorker` + boot receiver for process/reboot recovery | ✅ implemented; device kill/Doze proof is still a release gate |
| Battery drain | ~0-battery geofence monitoring; precise GPS only in ≤90s bursts near destination; battery-saver and cellular-only suppression thresholds | ✅ `LocationStrategyOrchestrator`, `TripMonitoringService` |
| Fake confidence | Visible confidence chip (High/Medium/Low) with bias-early rule: uncertain → alert earlier and *say why* | ✅ `Confidence` model, `AlertDecisionEngine` |
| Silent offline failure | "Underground mode" — signal loss is a named, dignified state with its own UI, not a hang | ✅ `NetworkLost`/`NetworkRestored` events, offline bias |
| Missed stop = dead end | First-class Recovery screen: nearest return stop, one-tap re-arm, walk-back | ✅ `RecoveryScreen`, `RearmTripUseCase` |

### 1.4 "Do better" execution priorities

Being architecturally better is not enough — users must *experience* the difference in the first three trips. Priorities, in order:

**P0 — Reliability proof (pre-launch, blocking)**
- Field-test matrix: Samsung + Xiaomi + Oppo devices × screen-off × battery saver × Doze. Use [FIELD_TEST_RUNBOOK.md](FIELD_TEST_RUNBOOK.md) and the CSV log template. Target: **≥99% alert delivery** on the test corridor before any public listing.
- In-app OEM battery-exemption flow (already a differentiator commitment) must be shown *before* the first trip arms, not buried in settings.
- Kill-test automation: `adb shell am kill` + Doze simulation in a nightly device-farm run, asserting the recovery worker restores monitoring.

**P1 — Trust made visible (first release)**
- Post-trip "trust receipt": after every trip, one card — "Alerted you 320m before your stop · Confidence was High · Battery used: 2%." Benchmark whether competitors provide equivalent evidence; the product goal is to convert a working alert into *earned trust* and a habit.
- First-trip onboarding frames the promise honestly: "We alert early when unsure. We tell you when signal is weak."

**P2 — Category-gap features (fast follow)**
- Boarding validation (wrong-direction detection after arming) — validate competitor coverage and position it as a gap only where evidence supports the claim.
- Transfer alerts for multi-leg trips — already scaffolded (`TransferMonitor`, `TransferProgressCard`).
- Departure reminders tied to saved places — closes Pillar 1.

**What we still refuse to build** (aligned with [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md) §12): route planner, public live-vehicle map, ticketing, ads, mandatory accounts, or ambient/indefinite sharing. A future private trip share is allowed only when it is opt-in, recipient-scoped, revocable, and auto-expiring.

---

## Section 2 — Sri Lanka Plan

### 2.1 Why Sri Lanka is the beachhead

- Massive daily bus + rail commuter population; long routes where napping/distraction is the norm, not the edge case.
- Current evidence suggests an arrival-assurance gap; validate local and global competitors on each launch corridor before publishing this claim.
- The founder is local — corridor validation, alias curation, and NTC/Railways relationships are all feasible in person.

### 2.2 The 2026 Lanka Metro Transit landscape

Public procurement material describes a Lanka Metro Transit digital platform with passenger information and operational capabilities. Deployment dates, fleet coverage, app availability, payment, occupancy, and language support must be reconfirmed against official current sources before launch messaging refers to them as live features.

What this means for NearWake:

- **Do not compete on fleet operations, tracking, or payment.** NearWake remains an arrival-assurance product, independent of operator systems.
- **Position as a complementary layer:** where an operator feed exists, it can improve confidence; where it does not, NearWake still provides destination-based assurance.
- **Treat coverage as evidence, not assumption:** document supported fleets and corridors from official feeds and field tests. Offline-first destination monitoring is the fallback, not a claim that no alternative can work.
- **Future integration, not competition:** if LMT exposes live-position data, it becomes a Tier-1 confidence signal for trips on Metro corridors (see pipeline, §3).

### 2.3 What Sri Lankan commuters need that global apps ignore

| Local reality | Product requirement |
|---------------|---------------------|
| Route numbers are the primary mental model ("138", "177", "120") — not street addresses | Route-number-first search and trip setup; "138 to Maharagama" must be a first-class query |
| Landmarks beat addresses ("near Odel", "Pettah", "Pita Kotuwa") | Landmark/alias index with Sinhala, Tamil, English + romanized variants and typo tolerance |
| Trilingual population | UI localization: Sinhala + Tamil + English at launch; verify terminology with native speakers and accessibility testing |
| Buses may skip stops and private-bus timetables can be unreliable | Destination-geofence monitoring remains useful without a schedule feed and can supplement schedule-based products |
| Long intercity routes (Colombo–Kandy, Colombo–Galle, coastal & hill-country rail) with dead zones | Underground/offline mode + bias-early is not a nice-to-have, it is the core value; pre-cache route geometry at setup |
| Budget Android devices (Xiaomi/Samsung A-series/Oppo dominant), aggressive battery killers | OEM exemption flow localized; APK size discipline; test matrix uses locally common devices |
| Data cost sensitivity | Offline-first already; keep seed dataset in the APK so first trip works with zero network |
| Trains: Colombo Fort ↔ suburbs (Panadura, Maharagama corridor, Ragama…) commuter rail is huge | Railways station list as first-class seed data (official station names from Sri Lanka Railways) |

### 2.4 Corridor-first launch (not "all island")

Per [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md) §13 — launch narrow and deep, expand on evidence:

**Phase SL-1 (launch): Colombo commuter core**
- Rail: Coastal line (Fort–Panadura) + Main line (Fort–Ragama/Gampaha) + KV line
- Bus: 3–5 validated high-volume corridors (e.g., 138 Fort–Maharagama/Homagama, 177, 120) with curated stop/landmark aliases
- Full trilingual UI; alert audio/haptics tuned on real buses (loud environments — validate Sleep-mode escalation volume in the field)

**Phase SL-2: intercity trust**
- Colombo–Kandy, Colombo–Galle (bus + rail), hill-country rail (tourist + local mix)
- Dead-zone map per corridor feeding the confidence model (pre-computed "expect signal loss after X" hints)

**Phase SL-3: island-wide + relationships**
- NTC structured-export relationship (request template already drafted in the data plan)
- Sri Lanka Railways schedule export for departure reminders
- Moderated community alias corrections (Tier 4 of the data plan)

### 2.5 Sri Lanka go-to-market notes

- **Trust language:** market the twelve-word promise in all three languages; "never miss Maharagama again" beats feature lists.
- **Distribution:** university commuter groups, daily-office-commuter Facebook/WhatsApp communities, hostel/boarding communities near rail lines — high nap-rate segments.
- **Trusted-contact use case:** start with an explicit post-arrival Android share action that needs no account. Any future live trip share must be individually initiated, revocable, auto-expiring, and unavailable by default.
- **Pricing:** free core forever (category is free; trust requires zero paywall in the alert path). Monetization deferred — do not solve it before retention is proven.

---

## Section 3 — Pipeline Improvements (per our requirements)

The current planned pipeline (data plan §8) is:

```text
Official PDF / HTML / CSV → importer → normalization → reviewed canonical dataset → packaged seed → search layer
```

That remains correct. These are the upgrades our requirements now demand, in priority order:

### 3.1 Make the pipeline corridor-aware (new)

The launch strategy is corridor-first, so the pipeline must track **coverage quality per corridor**, not just per record:

- Add `corridorId` and `coverageTier` (`validated` / `seeded` / `unverified`) to the canonical schema (extends data plan §9).
- The app surfaces tier honestly: a validated corridor gets standard confidence; an unverified area starts at Medium confidence with the bias-early rule. This extends confidence-first behavior into the data layer; competitor handling of data quality remains a benchmark item.

### 3.2 Add the alias/translation stage (new)

Between normalization and packaging, insert an **alias enrichment stage**:

```text
importer → normalization → alias enrichment (si/ta/en + romanized + typo variants) → review → seed
```

- Owned alias table keyed to canonical stop/landmark IDs (Tier 3 of the data plan is the *content*; this is the *pipeline stage* that carries it).
- Review is a human gate — a wrong alias breaks trust worse than a missing one.

### 3.3 Dead-zone intelligence (new, unique to us)

- During field tests (and later, opt-in diagnostics), log signal-loss segments per corridor → aggregate into a per-corridor dead-zone profile shipped with the seed data.
- Engine effect: entering a validated dead-zone stretch pre-arms offline bias *before* signal drops instead of reacting afterward. Treat uniqueness as a hypothesis until the competitor benchmark is complete.

### 3.4 Seed packaging + update channel

- Seed dataset ships **inside the APK** (first trip works offline, zero network).
- Delta updates via existing `core:remoteconfig` / app-sync path — versioned payloads, `sourceUpdatedAt` from the canonical schema; the app never blocks on a data update.

### 3.5 Source upgrades (sequence, from the data plan)

1. **Now:** NTC permit PDF + route map pages → importer (bootstrap); Sri Lanka Railways public station/schedule pages; OSM/Overpass for coordinates only.
2. **Next:** send the drafted NTC + Railways structured-export requests (data plan §5–7) — the pipeline's importer swaps without touching the app.
3. **Later:** LMT-GO / Lanka Metro data if accessible → live-position confidence signal on Metro corridors (Tier 1.5 between official seeds and OSM).
4. **Later:** moderated community corrections (Tier 4) — only after launch, only through the review gate.

### 3.6 Pipeline placement in the codebase

- The importer/normalizer/enricher lives **outside the app** (separate `tools/` or repo — Python or Kotlin scripts; not shipped).
- The app consumes only the packaged seed via the existing interfaces: search merges local index → Google Places fallback (data plan §10) behind `RoutingDataSource`-style interfaces, so no feature-layer rewrite when sources upgrade.
- New Room entities for routes/stops/aliases go in `core:database` as **additive** schema (respecting the frozen-entities constraint — additions only, no changes to existing tables).

---

## Section 4 — One App, Every Country: Reconciling Global Reach with Local Depth

**The question:** if we publish to the global Play Store, users outside Sri Lanka can't use the Sri Lanka data plan (route numbers, landmarks, trilingual). Does that break the product for them?

**The answer: no — because the Sri Lanka plan is a layer, not the app.**

### 4.1 The core engine is location-agnostic

NearWake's MVP is destination-only geofencing (see [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md) §14). Where Google Places is available and supported, a user can choose a destination, set a lead time, and use staged alerts, visible confidence, offline fallback, and missed-stop recovery with no local transit dataset. Coverage, API availability, and legal requirements still need per-country validation.

A user outside Sri Lanka keeps the supported location-based core where required platform services are available. What they do not get is the local convenience layer: route-number search, landmark aliases, trilingual UI, and dead-zone prediction. The engine is reusable across eligible markets; each local pack is additive.

### 4.2 One binary, region-adaptive

```
User opens app
  -> detect region (SIM MCC / locale / user picker — never GPS pre-onboarding; allow override)
      -> Sri Lanka:  local route+landmark index -> Google Places fallback -> merged  (+ si/ta/en UI, dead-zone profiles)
      -> elsewhere:  Google Places search only -> same engine, standard confidence
```

- Search fallback is already specified (data plan §10): local-first where a pack exists, Places everywhere else.
- **Data packs are modular:** ship the SL pack in the base APK for the SL launch; deliver other regions on-demand (Play asset/feature delivery or `core:remoteconfig`) so no user carries data they can't use and the APK stays small.
- Users can override detected region (diaspora, tourists, expats).

### 4.3 The upside: Sri Lanka is the template, not a one-off

The SL plan is the **repeatable localization playbook**. India, Bangladesh, Kenya, Philippines, Indonesia, and Egypt are candidate markets with hypotheses around route-number usage, landmark-first navigation, timetable quality, budget devices, and dead zones. Validate those hypotheses before investment. Each approved market becomes a data pack from the same pipeline, not an app rewrite.

The fully worked, reusable per-country template — a prioritization score, effort tiers (L0 core-only → L2 full pack), a fill-in-the-blanks plan, and the sequenced expansion loop — lives in [COUNTRY_EXPANSION_PLAYBOOK.md](COUNTRY_EXPANSION_PLAYBOOK.md).

### 4.4 The real risk and the launch-sequencing decision

The genuine risk is **shared global ratings**: Play Store ratings are worldwide. A poor experience in an untuned market tanks the star rating for everyone.

**Recommendation: global-capable architecture, Sri-Lanka-first launch sequencing.**

- Build the region-adaptive layer now — it is additive, fits existing interfaces, and does not touch the frozen core.
- Use Play Console country targeting to **launch and market in Sri Lanka first**. Prove ≥99% field-tested delivery (Section 1, P0) + retention.
- Then flip on availability market-by-market, each with its own pack. Global reach arrives *when the core is proven*, not before.
- Store listings are localized per country in Play Console; SL gets Sinhala/Tamil/English listings, other markets get English (or their locale) with the universal "never miss your stop" promise.

### 4.5 Code implications (all additive)

- Region detection + region-scoped data-pack selection: new, but sits above the existing `RoutingDataSource` / search interfaces — no feature-layer rewrite.
- New route/stop/alias Room entities: additive schema in `core:database` (respects frozen-entities constraint — additions only).
- Pack delivery: reuse `core:remoteconfig` / app-sync; packs are versioned payloads, app never blocks on a pack update.
- Outside SL the app simply never loads a local pack — the same code path, Google Places only.

---

## Section 5 — Development Plan (Strategy → Engineering)

The existing roadmaps cover **product features**: [PLAN.md](PLAN.md) (Waves A–H, done) and [PRODUCT_EXPANSION_ROADMAP.md](PRODUCT_EXPANSION_ROADMAP.md) (Waves I/J/K — v1.1/v1.5/v2.0 feature specs). This section covers the **new engineering the market strategy requires** and is not in those docs: the reliability proof, the region-adaptive/localization engine, and the offline data pipeline.

### 5.0 Architecture rules (unchanged, apply to every workstream)

- **Additive schema only** — new Room entities/columns, never changes to frozen tables.
- **Interface-driven** — new capability sits behind existing interfaces (`RoutingDataSource`-style); feature layer untouched.
- **Offline-first, no backend** for MVP — packs ship in-APK or via `core:remoteconfig`; the app never blocks on network.
- **Same code path for L0** — a country with no pack runs Google-Places-only through the identical path.

### 5.1 Workstreams

Each workstream (WS) lists intent → target modules → deliverable → gate.

**WS-0 — Reliability Proof** · *blocks public launch* · (Section 1 P0)
- Automate the OEM kill/Doze/battery-saver matrix (`adb shell am kill`, `dumpsys deviceidle force-idle`) on Samsung + Xiaomi + Oppo; assert `TripRecoveryWorker` + boot receiver restore monitoring.
- Surface the OEM battery-exemption flow **before the first trip arms**, not in settings.
- Modules: `data:alerts`, `data:location`, `feature:permissions`; new instrumentation tests + a diagnostics-log field dashboard.
- **Gate:** ≥99% alert delivery on the Sri Lanka launch corridor.

**WS-1 — Region-Adaptive Foundation** · (Section 4)
- `RegionResolver` (SIM MCC → locale → user picker, overridable; no GPS pre-onboarding).
- `RegionalDataPack` abstraction + pack registry; search flow `local index → Places fallback → merged`.
- Modules: `core:common` (resolver), `domain:routing`/`data:routing` (pack interface), `feature:places` (merged search), `core:datastore` (region preference).
- **Gate:** outside a pack region, behavior is byte-for-byte the current Places-only path; unit tests for resolver precedence + override.

**WS-2 — Localization / i18n Engine** · (Section 2 trilingual, Section 4 L1)
- Externalize hardcoded strings → per-language resources (si/ta/en); locale-aware alert copy.
- Multi-script + romanization + typo-tolerant search matching.
- Modules: `core:ui`/`core:designsystem` strings, `feature:places` matcher.
- **Gate:** UI renders fully in all three languages; search matches across scripts + common typos (test corpus).

**WS-3 — Data Pipeline (offline tooling, not shipped)** · (Section 3)
- New `tools/` project (Python or standalone Kotlin): `importer → normalizer → alias-enrichment → dead-zone → seed-packager`.
- Canonical schema per [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md) §9 + `corridorId` + `coverageTier`.
- Output: versioned seed pack (JSON/proto) — an artifact, not part of the APK build.
- **Gate:** reproducible pack build from raw source → validated canonical output; human review step enforced for aliases.

**WS-4 — Pack Delivery + Storage** · (Section 3.4, 4.2)
- Additive Room entities (routes/stops/aliases) in `core:database`.
- Pack loader: in-APK seed for the beachhead; on-demand download for other regions via `core:remoteconfig`; versioning + staleness + non-blocking update.
- `coverageTier` → confidence hook; known dead-zone stretch pre-arms offline bias *before* signal drops.
- **Gate:** first trip works offline from in-APK seed; delta update applies without blocking a trip; schema migration test green.

**WS-5 — Sri Lanka L2 Pack (first real instance)** · (Section 2 + Playbook)
- Run WS-3 pipeline for SL: NTC + Railways import, Colombo-corridor + rail aliases, dead-zone profiles from WS-0 field tests. Ship in base APK.
- This is the end-to-end proof that WS-1…WS-4 work.
- **Gate:** route-number + landmark search returns correct SL results offline; corridor tiers drive confidence honestly.

**WS-6 — Trust Receipt** · (Section 1 P1)
- Post-trip card: "Alerted 320m before your stop · Confidence High · Battery 2%" from existing diagnostics data.
- Modules: `feature:history` (or new small feature), reads diagnostics logs.
- **Gate:** card renders after every completed trip with accurate figures.

### 5.2 Sequencing

```
WS-0  ██████████ (parallel, blocks launch — start now, run continuously)
WS-1  ->  WS-2
      ->  WS-4  ->  WS-5
WS-3  ---------->  WS-5      (pipeline must exist before the SL pack)
WS-6  ██ (independent, any time)
```

Critical path to Sri Lanka v1.0: **WS-1 → WS-4 → WS-5**, with **WS-3** feeding WS-5 and **WS-0** gating the release.

### 5.3 Standard verification (per workstream, before merge)

```bash
./gradlew.bat compileDebugKotlin        # compiles
./gradlew.bat testDebugUnitTest         # all unit tests green
./gradlew.bat assembleDebug             # APK builds
# + new: instrumentation kill/Doze suite for WS-0; pipeline reproducibility check for WS-3
```

### 5.4 Milestones

| Milestone | Contents |
|-----------|----------|
| **v1.0 — Sri Lanka launch** | WS-0 (gate passed), WS-1, WS-2, WS-3, WS-4, WS-5, WS-6 |
| **v1.1+** | Existing feature Wave I (PRODUCT_EXPANSION_ROADMAP) + first *second* country taken L0 → L2 via [COUNTRY_EXPANSION_PLAYBOOK.md](COUNTRY_EXPANSION_PLAYBOOK.md) |
| **v1.5 / v2.0** | Wave J / Wave K as already specified; each new market = a data pack, not a rewrite |

---

## Summary: the one-paragraph strategy

NearWake is designed around the *trust handover*: staged alerts, visible confidence, dignified offline behavior, and recovery when things go wrong. Those advantages become defensible only after the ≥99% field-delivery gate is met on representative devices and corridors. Sri Lanka is the beachhead because route-number and landmark usage, variable timetables, dead zones, and budget phones reward an offline-first approach. The data pipeline evolves from bootstrap sources to reviewed official feeds while adding corridor-level coverage evidence, trilingual aliases, and dead-zone intelligence.

---

## Evidence Register

Review these sources before each competitor or launch-claim update. Record the review date and preserve screenshots or exported evidence in the release research folder; app-store text and deployment status can change without notice.

- MapFactor Wake Me There product page: https://www.mapfactor.com/wake-me-there-app/
- Google Play listing and current user feedback: https://play.google.com/store/apps/details?id=com.mapfactor.wakemethere
- Lanka Metro Transit procurement implementation manual: https://lankametro.lk/docs/VOLUMME1-BIDDING-DOCUMENT-%20METRO%20BUS%20DIGITAL%20PLATFORM%20%28MBDP%29.pdf
- Sri Lanka Ministry of Transport functional requirements: https://www.transport.gov.lk/web/images/pdf/VOLUMME2-Section_VII-Functional_Requirements_Schedule_FRS.pdf

Claims without a dated primary source are hypotheses for validation, not approved marketing copy.
