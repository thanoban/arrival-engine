# NearWake — Master Product & Implementation Plan

**Package:** `com.nearwake.app`
**Platform:** Android-first (Kotlin + Jetpack Compose)
**Last updated:** 2026-05-10
**App version:** versionCode 1 / versionName "0.1.0"
**DB schema:** Room v7 (migrations 1→7 complete)

---

## Strategic Positioning

**The twelve-word promise:**
> Wake me before my stop. Tell me if you are not sure.

NearWake is the Android arrival assurance system — for commuters whether they sleep, read, scroll, work, or zone out. The confidence model is the entire differentiator: every competitor shows one threshold and hopes for the best. NearWake admits uncertainty, biases early, and tells the user why.

NearWake is a **trust app**, not a safety platform. No SOS, no emergency contacts, no 24/7 monitoring.

---

## Eight Pillars

| # | Pillar | Status |
|---|--------|--------|
| 1 | Depart on time | ✅ Done |
| 2 | Board right | ✅ Done |
| 3 | Ride awake or asleep | ✅ Done |
| 4 | Transfer confidently | ✅ Done |
| 5 | Arrive correctly | ✅ Done |
| 6 | Recover gracefully | ✅ Done |
| 7 | Walk the last mile | ✅ Done |
| 8 | Confirm arrival | ✅ Done |

---

## Module Architecture

**37 modules — Clean Architecture + ports + application layer**

```
:app
├── :application:monitoring     ← StartTripMonitoringUseCase, StopTripMonitoringUseCase
├── :application:trip           ← 21 use cases (see full list below)
├── :ports:monitoring           ← TripMonitoringGateway interface (start/stop)
├── :ports:persistence          ← TripLifecycleStore interface (13 methods)
├── :ports:analytics            ← NearWakeAnalytics interface (7 methods)
├── :core:common
├── :core:database              ← Room v7, schema exported, migrations 1→7
├── :core:datastore             ← UserPreferences (ThemeMode, AlertTriggerMode, lead times)
├── :core:designsystem          ← NearWakeColors (dark+light), NearWakeTheme, tokens
│                                  NearWakeSpacing (cardCompact/cardDefault/cardLarge)
├── :core:network
├── :core:remoteconfig          ← ThresholdConfig, StaticRemoteConfigRepository (v1.0 defaults)
├── :core:testing
├── :core:ui                    ← NearWakeScaffold, SurfaceCard, StateChip, PulseRing,
│                                  PlaceResultRow, NearWakeButtonSize enum
├── :core:benchmark             ← NearWakeBaselineProfileGenerator, NearWakeStartupBenchmark
├── :domain:trip                ← AlertStageEvaluator (ThresholdConfig-injected), TripEngine,
│                                  TransferMonitor, BoardingValidator,
│                                  AlertTriggerMode (TIME/DISTANCE/BOTH)
├── :domain:location
├── :domain:routing             ← RouteSnapshot, Stop, RouteSignalQuality
├── :domain:commute             ← CommutePrediction, CommutePredictionEngine (±30 min)
├── :data:alerts                ← TripMonitoringService, AlertOrchestrator,
│                                  NotificationHelper, DepartureReminderScheduler,
│                                  TripCleanupUseCase, TripRecoveryWorker
│                                  TripMonitoringRuntime (ThresholdConfig-injected)
├── :data:location              ← FusedLocationDataSource, LocationStrategyOrchestrator
├── :data:motion
├── :data:routing
├── :data:analytics             ← DiagnosticsLogger (local Room log, user opt-in)
│                                  SentryNearWakeAnalytics (external, production observability)
├── :data:patterns              ← CommutePredictionRepository
├── :feature:onboarding
├── :feature:permissions
├── :feature:places             ← PlaceSearchViewModel → ObservePlaceSearchUseCase
│                                  compact PlaceResultRow UI (56dp rows, tap-to-select)
├── :feature:tripsetup          ← TripSetupViewModel → LoadTripSetupPreviewUseCase
│                                  sticky Arm button, segmented trigger mode, collapsed advanced
├── :feature:livetrip           ← icon status strip, Details › bottom sheet, compact transfer cards
├── :feature:alerts             ← AlertScreen (streamlined), RecoveryScreen (compact)
├── :feature:history
├── :feature:settings           ← grouped list rows, modal bottom sheet selectors
├── :feature:diagnostics        ← DiagnosticsViewModel → ObserveDiagnosticsUseCase
├── :feature:walkfinish
├── :feature:companion          ← CompanionViewModel → ObserveCompanionUseCase
└── :feature:departure
```

### application:trip — 21 use cases

| Use Case | Role |
|----------|------|
| `StartTripUseCase` | Create trip, save, start monitoring |
| `RearmTripUseCase` | Clone completed trip, fetch fresh route, start monitoring |
| `CancelTripUseCase` | Clear session, stop monitoring |
| `CompleteTripUseCase` | Mark complete, clear session |
| `MarkTripCompletedUseCase` | Stamp completion timestamp (called from service side effect) |
| `UpdateTripAlertModeUseCase` | Toggle ACTIVE / SLEEP mid-trip |
| `ObservePlaceSearchUseCase` | Stream saved places for search |
| `SaveResolvedPlaceUseCase` | Persist resolved Google Place |
| `MarkSavedPlaceUsedUseCase` | Update `lastUsedAt` on place |
| `LoadTripSetupPreviewUseCase` | One-time: destination + route + ETA for setup screen |
| `ObserveHomeDashboardUseCase` | Stream home state (active / rearming / recent trips) |
| `ObserveLiveTripUseCase` | Stream live trip with route, ETA, transfers |
| `ObserveAlertUseCase` | Stream alert presentation |
| `ObserveRecoveryUseCase` | Stream recovery guidance |
| `ObserveWalkFinishUseCase` | Stream walk-to-finish signal |
| `ObserveCompanionUseCase` | Stream arrival confirmation data |
| `ObserveTripSummaryUseCase` | Stream trip summary (post-trip) |
| `ObserveTripHistoryUseCase` | Stream trip history with filter/sort |
| `BuildTripHistoryCsvUseCase` | Export history as CSV string |
| `ObserveDiagnosticsUseCase` | Stream diagnostics events + trip state |
| `ClearDiagnosticsEventsUseCase` | Delete all diagnostics events |

### ports:persistence — TripLifecycleStore (13 methods)

```
Observe flows (3):   observeHomeSnapshot(), observeSavedPlaces(),
                     observeRecentDiagnosticsEvents(limit)
Read snapshots (2):  getSavedPlace(placeId), getTrip(tripId)
Write commands (8):  updateTripAlertMode, completeTrip, clearTripSession,
                     clearDiagnosticsEvents, markPlaceUsed,
                     saveSavedPlace, saveTrip, saveTripSession
```

---

## What Is Complete

### Core product — all 8 pillars
- Bias-early engine — `AlertStageEvaluator` with 1.0×/1.15×/1.25× multipliers by confidence + mode
- `AlertTriggerMode` (TIME / DISTANCE / BOTH) — user-selectable in TripSetupScreen, persisted in DataStore, tested
- APPROACH / IMMINENT notification channels — distinct tones, vibration, heads-up priority
- Sleep / Active mode — different haptic patterns, higher bias in Sleep
- BoardingValidator — bearing check, WRONG_DIRECTION warning notification
- TransferMonitor — per-leg progress strip with `RouteSignalQuality`, CHANNEL_APPROACH notifications
- RecoveryScreen — first-class with real actions (resume / end / re-arm)
- One-tap re-arm — `RearmTripUseCase`, skips setup, fetches fresh route
- Departure predictions — `CommutePredictionEngine` (±30 min window), `DepartureReminderScheduler` (AlarmManager), boot reschedule, `DepartureReminderScreen`
- Walk finish guidance — `ObserveWalkFinishUseCase`, distance + heading
- CSV export — `BuildTripHistoryCsvUseCase`, share sheet in HistoryScreen

### UI — Wave H complete
- All 15 screens modernized — dark-first design system, PulseRing, animated accent
- Light / Dark / System theme — `ThemeMode` DataStore persistence, `LocalNearWakeColors` CompositionLocal, `NearWakeTheme(darkTheme)` fully wired, 3-chip selector in SettingsScreen
- OEM reliability guidance — Samsung / Xiaomi / OPPO / Pixel vendor-specific steps
- Diagnostics screen — "why fired" with confidence, stage, distance, ETA per event
- All 15 NavGraph routes wired including Departure, Companion, WalkFinish
- **Wave H redesign complete** — compact layouts, Material icons, icon-driven status strips:
  - `PlaceResultRow` — 56dp rows, tap-to-select, no inline button
  - `HomeScreen` — 80dp active trip card, 72dp re-arm card, 52dp recent trip rows
  - `TripSetupScreen` — sticky Arm button in Scaffold bottomBar, segmented trigger mode, "Advanced ›" collapse
  - `LiveTripScreen` — 3-icon status strip (32dp), "Details ›" bottom sheet, 160dp transfer cards
  - `AlertScreen` — streamlined, Walk button reduced
  - `RecoveryScreen` — single ElevatedCard, side-by-side action buttons
  - `SettingsScreen` — grouped list rows with modal bottom sheet selectors
- **Design tokens:** `NearWakeSpacing` + `cardCompact/cardDefault/cardLarge`, `NearWakeButtonSize` enum (Small/Medium/Large), compact `SurfaceCard` variant

### Architecture — fully verified
- **All 15 ViewModels use `viewModelScope`** — no manual scope anywhere
- **`TripMonitoringService.onDestroy()`** — no `runBlocking`, uses `serviceScope.cancel()` cleanly
- **`activeSession` protected by `Mutex`** — no race condition
- **All 13 UiState classes have `errorMessage: String?`** — error slot present everywhere
- **Zero feature modules import `core:database` directly** — all 4 previously-flagged modules (places, tripsetup, diagnostics, companion) now inject use cases only
- **`TripMonitoringService` delegates all persistence** — completion via `MarkTripCompletedUseCase`, cleanup via `TripCleanupUseCase` (geofences, location orchestrator, activity transitions)
- **Zero direct DAO calls in any service or feature ViewModel**
- Room v7, schema exported, migrations 1→7 complete, indices on all queried columns
- **`ThresholdConfig` injected into `AlertStageEvaluator` and `TripMonitoringRuntime`** — all thresholds configurable, no hardcoded constants in engine
- **StrictMode enabled in debug builds** — `NearWakeApp.onCreate()` behind `BuildConfig.DEBUG`
- ProGuard: R8 full mode (minifyEnabled + shrinkResources), Room / Hilt / serialization / WorkManager / Places all kept
- targetSdk 35 / minSdk 26 / compileSdk 35
- MAPS_API_KEY wired via `local.properties` → `manifestPlaceholders`
- Google Places real repository-backed search (not stub)
- DepartureReminderScheduler wired in `DepartureViewModel` and `DepartureReminderBootReceiver`

### Tests — 67+ classes across all layers
- **Domain (18):** AlertStageEvaluatorTest (ThresholdConfig-aware), AlertDecisionEngineTest, CommutePredictionEngineTest, ApproachEvaluatorTest, BoardingValidatorTest, OvershootDetectorTest, RecoveryPlannerTest, TransferMonitorTest, TripStateMachineTest, and more
- **Application (30):** All 21 use cases have tests + FakeNearWakeAnalytics test double
- **Data (12):** TripMonitoringRuntime (ThresholdConfig-aware), DiagnosticsLogger, TripCleanupUseCase, DepartureReminderPlanner, TripMonitoringFeedbackCoordinator, MonitoredTripContextLoader, and more
- **Core (3):** NearWakeHttpClient, UserPreferencesDataStore, StaticRemoteConfigRepositoryTest
- **Feature (4):** OemReliability, TransferProgressBuilder, PermissionsViewModel, DiagnosticsExport
- **Benchmark (2):** NearWakeBaselineProfileGenerator, NearWakeStartupBenchmark

### Documentation — 25 guides in project root
`PLAN.md`, `README.md`, `NEARWAKE_MASTER_REFERENCE.md`, `TARGET_PRODUCTION_ARCHITECTURE.md`,
`FIELD_TEST_RUNBOOK.md`, `REAL_PHONE_TESTING_GUIDE.md`, `APP_SIGNING_SETUP_GUIDE.md`,
`PLAY_STORE_SUBMISSION_RUNBOOK.md`, `PLAY_STORE_LISTING_DRAFT.md`,
`PLAY_CONSOLE_DISCLOSURE_DRAFT.md`, `PRIVACY_POLICY_DRAFT.md`,
`PRIVACY_AND_DISCLOSURE_NOTES.md`, `RELEASE_READINESS_CHECKLIST.md`,
`DEVELOPMENT_START.md`, `SETUP_AND_STATUS.md`, `PROJECT_STUDY_GUIDE.md`,
`UI_MODERNIZATION_PLAN.md`, `UI_UX_REDESIGN_PLAN.md`, `PRODUCT_COMPARE_REFERENCE.md`,
`PRODUCT_EXPANSION_ROADMAP.md`, `LONG_JOURNEY_MONITORING_STRATEGY.md`,
`SRI_LANKA_PRODUCTION_DATA_PLAN.md`, `REQUIRED_UPDATES_AND_APIS.md`,
`LLM_PROJECT_CONTEXT_PROMPT.md`, `corrections.md`

---

## Wave Checklist

### Wave A — Trust Core ✅ Complete
- [x] Confidence chip on LiveTrip
- [x] Bias-early engine (1.0×/1.15×/1.25× by confidence + mode)
- [x] Bias-early banner (biasEarlyMessage in TrustPresentation)
- [x] Underground-mode chip
- [x] Recovery screen as first-class destination
- [x] OEM Reliability page (Samsung/Xiaomi/OPPO/Pixel)
- [x] One-tap re-arm on Home
- [x] Stage A (APPROACH) distinct channel — IMPORTANCE_DEFAULT, no popup
- [x] Stage B (IMMINENT) — IMPORTANCE_HIGH, heads-up, vibration `[0,200,100,400]ms`

### Wave B — Dual Mode ✅ Complete
- [x] Sleep / Active Mode enum + engine behavior
- [x] Haptic preamble patterns (multi-pulse for Sleep mode)
- [x] Confidence-based bias multiplier higher in Sleep mode

### Wave C — Route + Boarding ✅ Complete
- [x] BoardingValidator — bearing check, WRONG_DIRECTION warning notification
- [x] TransferMonitor — per-leg strip, CHANNEL_APPROACH notifications
- [x] Per-leg `RouteSignalQuality` chip in transfer strip (HIGH/DEGRADED/OFFLINE)

### Wave D — Journey Extension ✅ Complete
- [x] `:domain:commute` — CommutePrediction, CommutePredictionEngine (±30 min window)
- [x] `:core:database` v5 — CommutePredictionEntity, DAO, MIGRATION_4_5, schema exported
- [x] `:data:patterns` — CommutePredictionRepository
- [x] `:feature:departure` — DepartureReminderScreen, DepartureViewModel
- [x] DepartureReminderScheduler — AlarmManager scheduling, 10-min window
- [x] DepartureReminderBootReceiver — reschedule on device reboot
- [x] DepartureReminderScreen wired into NavGraph

### Wave E — Polish ✅ Complete
- [x] Enriched diagnostics — confidence, stage, distance, ETA per event
- [x] CSV export — `BuildTripHistoryCsvUseCase`, share sheet in HistoryScreen
- [x] Light / Dark / System theme — DataStore persistence, CompositionLocal, Settings selector
- [x] Google Places-backed destination search
- [x] `AlertTriggerMode` (TIME / DISTANCE / BOTH) — wired in TripSetupScreen, persisted

### Wave F — Production Architecture ✅ Complete
- [x] `:application:monitoring` — `StartTripMonitoringUseCase`, `StopTripMonitoringUseCase`
- [x] `:ports:monitoring` — `TripMonitoringGateway` interface
- [x] `:application:trip` — 21 use cases covering all product operations and observe streams
- [x] `:ports:persistence` — `TripLifecycleStore` interface (13 methods, command pattern)
- [x] All feature ViewModels inject use cases only — zero `core:database` imports in features
- [x] `TripMonitoringService` delegates completion via `MarkTripCompletedUseCase`
- [x] `TripCleanupUseCase` handles geofence removal, location stop, activity unregistration
- [x] All 4 previously-flagged feature modules (places, tripsetup, diagnostics, companion) fully migrated

### Wave G — Launch Readiness 🟡 In Progress
- [x] **Sentry initialization** — `NearWakeApp.onCreate()` calls `SentryAndroid.init()`, DSN from `BuildConfig.SENTRY_DSN`, env-aware, tracesSampleRate 0.2
- [x] **Behavioral analytics** — `:ports:analytics` (`NearWakeAnalytics` interface, 7 methods), `SentryNearWakeAnalytics` impl, Hilt-wired, all 6 events + `recordFailure()` at 7 error sites
- [x] **Privacy policy manifest metadata** — `privacy-policy.html` created, `<meta-data PRIVACY_POLICY_URL>` in AndroidManifest, `configuredPrivacyPolicyUrl()` in build config
- [x] **`core:remoteconfig`** — `ThresholdConfig` + `StaticRemoteConfigRepository`; injected into `AlertStageEvaluator` and `TripMonitoringRuntime`; `StaticRemoteConfigRepositoryTest` passes
- [x] **`core:benchmark`** — `NearWakeBaselineProfileGenerator` + `NearWakeStartupBenchmark`; `baseline-prof.txt` in `app/src/main/`
- [x] **StrictMode in debug builds** — `NearWakeApp.onCreate()` behind `BuildConfig.DEBUG`
- [x] **Accessibility (partial)** — `AlertScreen` + `LiveTripScreen` + `NearWakeStatus` updated with `contentDescription` and `semantics`; full TalkBack pass still needed
- [ ] **Privacy policy hosted** — HTML ready locally, must be deployed to a public URL ← BLOCKER
- [ ] **Accessibility audit — full pass** — remaining screens need `contentDescription`, `Role.Button` semantics, contrast check ← BLOCKER for Play Store
- [ ] Field testing — follow `FIELD_TEST_RUNBOOK.md` (30+ real trips, 8 scenarios)
- [ ] Release signing — follow `APP_SIGNING_SETUP_GUIDE.md`, rotate MAPS_API_KEY
- [ ] Release AAB — `./gradlew bundleRelease` clean, verify size < 20 MB
- [ ] Play Store listing — follow `PLAY_STORE_SUBMISSION_RUNBOOK.md` + `PLAY_STORE_LISTING_DRAFT.md`

### Wave H — UI/UX Polish ✅ Complete

**Goal:** One dominant signal per screen. Visual elements over text. Professional information hierarchy.

- [x] `core/ui/NearWakeButtons.kt` — `NearWakeButtonSize` enum (Small/Medium/Large); Medium=48dp new default
- [x] `core/designsystem/NearWakeSpacing.kt` — `cardCompact=8dp`, `cardDefault=12dp`, `cardLarge=16dp`
- [x] `core/ui/SurfaceCard.kt` — compact padding variant
- [x] `core/ui/PlaceResultRow.kt` — new 56dp row composable (icon + name + address, tap-to-select)
- [x] `feature/places/PlaceSearchScreen.kt` — compact 56dp rows, tap-to-select, no inline button
- [x] `app/HomeScreen.kt` — 80dp active trip card, 72dp re-arm card, 52dp recent trip rows with outcome icons
- [x] `feature/tripsetup/TripSetupScreen.kt` — sticky Arm button in Scaffold bottomBar, segmented trigger mode, "Advanced ›" collapse
- [x] `feature/livetrip/LiveTripScreen.kt` — 3-icon status strip (32dp), "Details ›" bottom sheet, destination above ring
- [x] `feature/livetrip/TransferProgressCard.kt` — 160dp width, 64dp height, icon+name+status dot
- [x] `feature/alerts/AlertScreen.kt` — streamlined, Walk button reduced, accessibility semantics added
- [x] `feature/alerts/RecoveryScreen.kt` — single ElevatedCard, side-by-side 48dp buttons
- [x] `feature/settings/SettingsScreen.kt` — grouped list rows (52dp), modal bottom sheet selectors

---

## Road to v1.0 — Ordered Execution

### Phase 0 — UI/UX Redesign ✅ Complete

All 7 screens redesigned (Wave H). Design tokens added. `PlaceResultRow` composable shipped. See **Wave H** checklist above for full detail and `UI_UX_REDESIGN_PLAN.md` for the original spec.

---

### Phase 1 — Observability ✅ Complete

**Sentry initialization** — done in `NearWakeApp.onCreate()`. DSN from `BuildConfig.SENTRY_DSN` (read from `local.properties`). Environment-aware. tracesSampleRate = 0.2.

**Behavioral analytics** — done. `:ports:analytics` (`NearWakeAnalytics` interface) + `SentryNearWakeAnalytics` impl in `data:analytics`. All 6 events wired + `recordFailure()` at 7 error sites. `FakeNearWakeAnalytics` test double in `application:trip` tests.

| Event | Callsite |
|-------|---------|
| `trip_armed` | `StartTripUseCase`, `RearmTripUseCase` |
| `alert_stage_advanced` | `TripMonitoringFeedbackCoordinator` |
| `alert_fired` | `TripMonitoringService` |
| `trip_completed` | `CompleteTripUseCase` |
| `rearm_tapped` | `HomeViewModel` |
| `transfer_missed` | `TripMonitoringFeedbackCoordinator` |

---

### Phase 2 — Privacy Policy 🟡 Partial

**Done:** `privacy-policy.html` created. `AndroidManifest.xml` has `<meta-data android:name="com.nearwake.PRIVACY_POLICY_URL" android:value="${PRIVACY_POLICY_URL}"/>`. Build config reads URL from `local.properties`.

**Remaining:** Host `privacy-policy.html` at a stable public URL and set `PRIVACY_POLICY_URL=https://yourhost/privacy` in `local.properties`. Required for Play Store data safety form submission — location apps cannot submit without it.

---

### Phase 3 — Remote Configuration ✅ Complete

`:core:remoteconfig` module shipped. `ThresholdConfig` data class with all engine constants. `StaticRemoteConfigRepository` returns hardcoded defaults for v1.0 — designed as a drop-in for Firebase Remote Config post-launch. `ThresholdConfig` injected into `AlertStageEvaluator` and `TripMonitoringRuntime` via constructor. `StaticRemoteConfigRepositoryTest` passes.

| Field | Default | Injected into |
|-------|---------|---------------|
| `approachGeofenceRadiusM` | 1500 | `TripMonitoringRuntime` |
| `approachGeofenceBatterySaverM` | 2250 | `TripMonitoringRuntime` |
| `biasMultiplierDegradedActive` | 1.15 | `AlertStageEvaluator` |
| `biasMultiplierDegradedSleep` | 1.20 | `AlertStageEvaluator` |
| `biasMultiplierOffline` | 1.25 | `AlertStageEvaluator` |
| `minTripsForClustering` | 2 | `CommutePredictionEngine` |

---

### Phase 4 — Performance ✅ Complete

`:core:benchmark` module shipped. `NearWakeBaselineProfileGenerator` navigates app → Home → TripSetup. `NearWakeStartupBenchmark` measures cold start. `baseline-prof.txt` generated and committed to `app/src/main/`. Expected 30–40% cold start improvement on production builds.

---

### Phase 5 — Accessibility 🟡 Partial

**Done:** `AlertScreen` + `LiveTripScreen` + `NearWakeStatus` updated with `contentDescription`, `semantics`, and `liveRegion`. AlertScreen now announces arrival to TalkBack without user interaction.

**Remaining:**
- Full TalkBack pass on all 13 remaining screens
- `NearWakeStateChip` + `NearWakeSelectableChip` — add `semantics { role = Role.Button }`
- Icon-only buttons on HomeScreen, TripSetupScreen, HistoryScreen — add `contentDescription`
- Touch targets — verify ≥ 48dp on all chips after Wave H compact redesign
- Contrast — 4.5:1 check in both light and dark modes

---

### Phase 6 — Field Testing

Follow `FIELD_TEST_RUNBOOK.md` (already written). Summary:

- [ ] 10 short trips (< 15 min) — verify APPROACH fires in time
- [ ] 10 medium trips (15–45 min) — screen on and off
- [ ] 10 long trips (45+ min) — verify battery management, service survives
- [ ] 10 screen-off trips — Sleep mode, phone in pocket, IMMINENT must fire before stop
- [ ] 10 tunnel / poor-signal trips — verify OFFLINE bias, no false ARRIVAL
- [ ] 5 multi-transfer trips — APPROACH notification per transfer leg
- [ ] 3 OEM battery-saver trips — Samsung + Xiaomi + OPPO foreground service survival
- [ ] 5 re-arm trips — session starts without setup screen

**Gate:** Sentry and analytics must be active before field testing so every alert event is measurable.

---

### Phase 7 — Release Engineering

- **Rotate MAPS_API_KEY** — current key in `local.properties` is unrestricted. Add Android app restriction in Google Cloud Console with SHA-1 fingerprint of release keystore.
- Follow `APP_SIGNING_SETUP_GUIDE.md` — generate keystore, populate `keystore.properties`
- `./gradlew bundleRelease` — clean build, zero ProGuard warnings
- Install release APK on physical device: `adb install --no-incremental app-release.apk`
- Verify size < 20 MB in Play Console pre-launch report

---

### Phase 8 — Play Store Submission

Follow `PLAY_STORE_SUBMISSION_RUNBOOK.md` and `PLAY_STORE_LISTING_DRAFT.md` (both already written).

- [ ] Google Play Console developer account ($25 one-time)
- [ ] App name: `NearWake — Transit Alarm`
- [ ] Short description (80 chars): `Wake up before your stop. Smart transit alarm for commuters.`
- [ ] Screenshots — 8 phone screens (Home, LiveTrip monitoring, LiveTrip APPROACH, Alert, Departure, Settings Appearance, Settings OEM, Diagnostics)
- [ ] Feature graphic — 1024×500 px
- [ ] Content rating — IARC questionnaire ("Everyone")
- [ ] Data safety form — follow `PLAY_CONSOLE_DISCLOSURE_DRAFT.md`
- [ ] Release track: Internal → Closed beta → Open beta → Production 20% → 50% → 100%

---

## Key File Map

| File | Purpose |
|------|---------|
| `domain/trip/engine/AlertStageEvaluator.kt` | Stage transitions + bias multiplier — `ThresholdConfig`-injected (no hardcoded constants) |
| `domain/trip/engine/TripEngine.kt` | Core state machine |
| `domain/trip/engine/TransferMonitor.kt` | Transfer checkpoints + RouteSignalQuality passthrough |
| `domain/trip/engine/BoardingValidator.kt` | Bearing-based direction check |
| `domain/trip/model/AlertTriggerMode.kt` | TIME / DISTANCE / BOTH — user-selectable trigger strategy |
| `domain/routing/model/Stop.kt` | Stop with `signalQuality: RouteSignalQuality` |
| `domain/commute/CommutePredictionEngine.kt` | Cluster trip history → CommutePrediction (±30 min window) |
| `application/trip/` | 21 use cases — all product operations and observe streams |
| `application/monitoring/` | StartTripMonitoringUseCase, StopTripMonitoringUseCase |
| `ports/persistence/TripLifecycleStore.kt` | Persistence contract (13 methods) |
| `ports/monitoring/TripMonitoringGateway.kt` | Monitoring contract (start/stop) |
| `data/alerts/TripMonitoringService.kt` | Foreground service — location, geofences, engine events |
| `data/alerts/AlertOrchestrator.kt` | Stage C alert: sound + vibration + diagnostics payload |
| `data/alerts/NotificationHelper.kt` | All notification builders (APPROACH, IMMINENT, ALERT, transfer, departure) |
| `data/alerts/DepartureReminderScheduler.kt` | AlarmManager scheduling — 10-min window, per prediction |
| `data/alerts/TripCleanupUseCase.kt` | Geofence removal + location stop + activity unregistration |
| `data/alerts/TripRecoveryWorker.kt` | WorkManager worker — restore monitoring after process death |
| `data/analytics/DiagnosticsLogger.kt` | Local Room diagnostic event log (NOT behavioral analytics — see Phase 1) |
| `data/patterns/CommutePredictionRepository.kt` | Refresh commute predictions from trip history |
| `core/database/NearWakeDatabase.kt` | Room DB v7, all entities and DAOs |
| `core/database/di/DatabaseModule.kt` | Migrations 1→7, all DAO provisions |
| `core/remoteconfig/ThresholdConfig.kt` | All tunable engine thresholds — injected via Hilt |
| `core/remoteconfig/StaticRemoteConfigRepository.kt` | v1.0 defaults; swap for Firebase post-launch |
| `core/benchmark/NearWakeBaselineProfileGenerator.kt` | Generates baseline-prof.txt for cold start |
| `core/ui/PlaceResultRow.kt` | 56dp place result row — icon + name + address, tap-to-select |
| `core/designsystem/NearWakeColors.kt` | Dark + light color token sets, `LocalNearWakeColors` |
| `core/designsystem/NearWakeTheme.kt` | Theme composition — resolves dark/light per ThemeMode |
| `core/datastore/` | UserPreferences, ThemeMode, UserPreferencesDataStore |
| `feature/livetrip/LiveTripScreen.kt` | Trip monitoring UI — accent, chips, transfer strip |
| `feature/departure/DepartureReminderScreen.kt` | Today's predicted departures + start trip CTA |
| `feature/history/HistoryScreen.kt` | Trip history + Export button |
| `feature/settings/SettingsScreen.kt` | Settings + OEM reliability + Appearance (theme selector) |
| `app/HomeScreen.kt` | Home — active trip card, re-arm card, recent trips |
| `app/NearWakeNavHost.kt` | All 15 routes |
| `app/NearWakeApp.kt` | Application class — WorkManager config, **Sentry init goes here** |

---

## Production Readiness — Verified State

| Layer | State | Action needed |
|-------|-------|---------------|
| Module structure | ✅ 37 modules, clean separation | — |
| ViewModel lifecycle | ✅ All 15 use `viewModelScope` | — |
| Service thread safety | ✅ `Mutex` guards session mutations | — |
| Service shutdown | ✅ No `runBlocking` in `onDestroy()` | — |
| Error surfaces | ✅ `errorMessage: String?` in all 13 UiState | — |
| Architecture — write path | ✅ start/rearm/cancel/complete behind ports | — |
| Architecture — read path | ✅ All 21 observe use cases through TripLifecycleStore | — |
| Feature DAO isolation | ✅ Zero `core:database` imports in any feature module | — |
| Service delegation | ✅ Completion → `MarkTripCompletedUseCase`; cleanup → `TripCleanupUseCase` | — |
| Room database | ✅ v7, migrations 1→7, indices on all queried columns | — |
| Theme | ✅ Light / Dark / System, DataStore persistence | — |
| Place search | ✅ Real Google Places repository (not stub) | — |
| Departure reminders | ✅ AlarmManager, boot reschedule, UI wired | — |
| NavGraph | ✅ All 15 routes wired | — |
| ProGuard | ✅ R8 full mode, all libraries covered | — |
| SDK levels | ✅ targetSdk 35 / minSdk 26 | — |
| Permissions manifest | ✅ All 13 permissions, `FOREGROUND_SERVICE_LOCATION` | — |
| Test coverage | ✅ 67+ test classes across all layers | — |
| AlertTriggerMode | ✅ TIME/DISTANCE/BOTH, wired in UI, persisted, tested | — |
| MAPS_API_KEY | ✅ Wired via local.properties | **Rotate key + add SHA-1 restriction before launch** |
| Crash reporting | ✅ Sentry initialized — DSN from BuildConfig, env-aware, 20% trace sampling | — |
| Behavioral analytics | ✅ `NearWakeAnalytics` + `SentryNearWakeAnalytics` — 6 events + `recordFailure()` at 7 sites | — |
| Privacy policy | 🟡 HTML ready + manifest wired — URL not hosted yet | Host + set `PRIVACY_POLICY_URL` |
| Remote config | ✅ `:core:remoteconfig` — `ThresholdConfig` injected into engine | Firebase drop-in post-launch |
| Baseline Profile | ✅ `:core:benchmark` — `baseline-prof.txt` committed to `app/src/main/` | — |
| StrictMode | ✅ Enabled in debug builds via `BuildConfig.DEBUG` | — |
| UI/UX redesign | ✅ Wave H complete — all 7 screens, compact tokens, Material icons | — |
| Accessibility | 🟡 AlertScreen + LiveTripScreen done — full TalkBack pass remaining | Phase 5 remaining |
| Field testing | 🔴 `FIELD_TEST_RUNBOOK.md` written, trips not done | Phase 6 |
| Release signing | 🔴 `APP_SIGNING_SETUP_GUIDE.md` written, keystore not generated | Phase 7 |
| Play Store | 🔴 `PLAY_STORE_SUBMISSION_RUNBOOK.md` written, not submitted | Phase 8 |

---

## Non-Negotiable Principles

1. Confidence-first — every prediction shows confidence
2. Bias-early — when in doubt, alert earlier and label why
3. Offline-dignified — underground is a named mode with its own UI
4. No ads, no paywalls in the alert path — ever
5. No account required for core value
6. Battery-honest — expose what we spend
7. OEM-aware — vendor-specific battery guidance
8. One-tap re-arm — returning commuters never re-enter a trip
9. Privacy-respecting — local persistence first, no continuous cloud location
10. Fail visible, not silent — degraded permissions shown to user

---

## Post-1.0 Deferred

See `PRODUCT_EXPANSION_ROADMAP.md` for the full feature roadmap with step-by-step dev plans.

**Wave I (v1.1 — all local):** Saved trip profiles, scheduled recurring trips, home screen widgets, per-route bias override, arrival SMS to contact
**Wave J (v1.5 — new integrations):** Google Calendar integration, commute analytics dashboard, weekly report notification, Google Assistant App Actions, DND override for Stage C
**Wave K (v2.0 — backend required):** Wear OS app, trip share link, family account (parent/child), cloud sync, iOS (KMM)

- Firebase Remote Config live tuning — `:core:remoteconfig` ships with static defaults; Firebase drop-in designed for post-launch
- Lock-screen widget — Glance `widgetCategory=keyguard` (Wave I extension of home widget)
- Ads (never)
