# NearWake — Master Product & Implementation Plan

**Package:** `com.nearwake.app`
**Platform:** Android-first (Kotlin + Jetpack Compose)
**Last updated:** 2026-05-10
**App version:** versionCode 1 / versionName "0.1.0"

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

**32 modules — Clean Architecture + ports + application layer**

```
:app
├── :application:monitoring     ← StartTripMonitoringUseCase, StopTripMonitoringUseCase
├── :application:trip           ← 21 use cases (see full list below)
├── :ports:monitoring           ← TripMonitoringGateway interface (start/stop)
├── :ports:persistence          ← TripLifecycleStore interface (13 methods)
├── :ports:analytics            ← NearWakeAnalytics interface (7 methods)
├── :core:common
├── :core:database              ← Room v5, schema exported, migrations 1→5
├── :core:datastore             ← UserPreferences (ThemeMode, AlertTriggerMode, lead times)
├── :core:designsystem          ← NearWakeColors (dark+light), NearWakeTheme, tokens
├── :core:network
├── :core:testing
├── :core:ui                    ← NearWakeScaffold, SurfaceCard, StateChip, PulseRing, etc.
├── :domain:trip                ← AlertStageEvaluator, TripEngine, TransferMonitor,
│                                  BoardingValidator, AlertTriggerMode (TIME/DISTANCE/BOTH)
├── :domain:location
├── :domain:routing             ← RouteSnapshot, Stop, RouteSignalQuality
├── :domain:commute             ← CommutePrediction, CommutePredictionEngine (±30 min)
├── :data:alerts                ← TripMonitoringService, AlertOrchestrator,
│                                  NotificationHelper, DepartureReminderScheduler,
│                                  TripCleanupUseCase, TripRecoveryWorker
├── :data:location              ← FusedLocationDataSource, LocationStrategyOrchestrator
├── :data:motion
├── :data:routing
├── :data:analytics             ← DiagnosticsLogger (local Room log, user opt-in)
│                                  SentryNearWakeAnalytics (external, production observability)
├── :data:patterns              ← CommutePredictionRepository
├── :feature:onboarding
├── :feature:permissions
├── :feature:places             ← PlaceSearchViewModel → ObservePlaceSearchUseCase
├── :feature:tripsetup          ← TripSetupViewModel → LoadTripSetupPreviewUseCase
├── :feature:livetrip
├── :feature:alerts
├── :feature:history
├── :feature:settings
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

### UI
- All 15 screens modernized — dark-first design system, PulseRing, animated accent
- Light / Dark / System theme — `ThemeMode` DataStore persistence, `LocalNearWakeColors` CompositionLocal, `NearWakeTheme(darkTheme)` fully wired, 3-chip selector in SettingsScreen
- OEM reliability guidance — Samsung / Xiaomi / OPPO / Pixel vendor-specific steps
- Diagnostics screen — "why fired" with confidence, stage, distance, ETA per event
- All 15 NavGraph routes wired including Departure, Companion, WalkFinish

### Architecture — fully verified
- **All 15 ViewModels use `viewModelScope`** — no manual scope anywhere
- **`TripMonitoringService.onDestroy()`** — no `runBlocking`, uses `serviceScope.cancel()` cleanly
- **`activeSession` protected by `Mutex`** — no race condition
- **All 13 UiState classes have `errorMessage: String?`** — error slot present everywhere
- **Zero feature modules import `core:database` directly** — all 4 previously-flagged modules (places, tripsetup, diagnostics, companion) now inject use cases only
- **`TripMonitoringService` delegates all persistence** — completion via `MarkTripCompletedUseCase`, cleanup via `TripCleanupUseCase` (geofences, location orchestrator, activity transitions)
- **Zero direct DAO calls in any service or feature ViewModel**
- Room v5, schema exported, migrations 1→5 complete, indices on all queried columns
- ProGuard: R8 full mode (minifyEnabled + shrinkResources), Room / Hilt / serialization / WorkManager / Places all kept
- targetSdk 35 / minSdk 26 / compileSdk 35
- MAPS_API_KEY wired via `local.properties` → `manifestPlaceholders`
- Google Places real repository-backed search (not stub)
- DepartureReminderScheduler wired in `DepartureViewModel` and `DepartureReminderBootReceiver`

### Tests — 66 classes across all layers
- **Domain (18):** AlertStageEvaluatorTest, AlertDecisionEngineTest, CommutePredictionEngineTest, ApproachEvaluatorTest, BoardingValidatorTest, OvershootDetectorTest, RecoveryPlannerTest, TransferMonitorTest, TripStateMachineTest, and more
- **Application (30):** All 21 use cases have tests + FakeNearWakeAnalytics test double
- **Data (12):** TripMonitoringRuntime, DiagnosticsLogger, TripCleanupUseCase, DepartureReminderPlanner, TripMonitoringFeedbackCoordinator, MonitoredTripContextLoader, and more
- **Feature (4):** OemReliability, TransferProgressBuilder, PermissionsViewModel, DiagnosticsExport
- **Core (2):** NearWakeHttpClient, UserPreferencesDataStore

### Documentation — 22 guides in project root
`PLAN.md`, `README.md`, `NEARWAKE_MASTER_REFERENCE.md`, `TARGET_PRODUCTION_ARCHITECTURE.md`,
`FIELD_TEST_RUNBOOK.md`, `REAL_PHONE_TESTING_GUIDE.md`, `APP_SIGNING_SETUP_GUIDE.md`,
`PLAY_STORE_SUBMISSION_RUNBOOK.md`, `PLAY_STORE_LISTING_DRAFT.md`,
`PLAY_CONSOLE_DISCLOSURE_DRAFT.md`, `PRIVACY_POLICY_DRAFT.md`,
`PRIVACY_AND_DISCLOSURE_NOTES.md`, `RELEASE_READINESS_CHECKLIST.md`,
`DEVELOPMENT_START.md`, `SETUP_AND_STATUS.md`, `PROJECT_STUDY_GUIDE.md`,
`UI_MODERNIZATION_PLAN.md`, `PRODUCT_COMPARE_REFERENCE.md`,
`LONG_JOURNEY_MONITORING_STRATEGY.md`, `SRI_LANKA_PRODUCTION_DATA_PLAN.md`,
`REQUIRED_UPDATES_AND_APIS.md`, `LLM_PROJECT_CONTEXT_PROMPT.md`

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
- [ ] **Privacy policy hosted** — HTML ready locally, must be deployed to a public URL ← BLOCKER
- [ ] **Accessibility audit** — zero `contentDescription`, `semantics`, `Role.Button` anywhere yet ← BLOCKER for Play Store
- [ ] `core:remoteconfig` — `ThresholdConfig` + `RemoteConfigRepository`
- [ ] `core:benchmark` — Baseline Profile for 30–40% cold start improvement
- [ ] StrictMode in debug builds — add to `NearWakeApp.onCreate()` behind `BuildConfig.DEBUG`
- [ ] Field testing — follow `FIELD_TEST_RUNBOOK.md` (30+ real trips, 8 scenarios)
- [ ] Release signing — follow `APP_SIGNING_SETUP_GUIDE.md`, rotate MAPS_API_KEY
- [ ] Release AAB — `./gradlew bundleRelease` clean, verify size < 20 MB
- [ ] Play Store listing — follow `PLAY_STORE_SUBMISSION_RUNBOOK.md` + `PLAY_STORE_LISTING_DRAFT.md`

---

## Road to v1.0 — Ordered Execution

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

### Phase 3 — Remote Configuration

- New `:core:remoteconfig` module — add to `settings.gradle.kts`
- `ThresholdConfig` data class with safe defaults matching current hardcoded constants:

| Field | Default | Source |
|-------|---------|--------|
| `approachGeofenceRadiusM` | 1500 | `data/alerts/TripMonitoringRuntime.kt` |
| `approachGeofenceBatterySaverM` | 2250 | `data/alerts/TripMonitoringRuntime.kt` |
| `biasMultiplierDegradedActive` | 1.15 | `domain/trip/engine/AlertStageEvaluator.kt` |
| `biasMultiplierDegradedSleep` | 1.20 | `domain/trip/engine/AlertStageEvaluator.kt` |
| `biasMultiplierOffline` | 1.25 | `domain/trip/engine/AlertStageEvaluator.kt` |
| `minTripsForClustering` | 2 | `domain/commute/CommutePredictionEngine.kt` |

- `RemoteConfigRepository` — v1.0 returns hardcoded defaults; designed for Firebase Remote Config drop-in post-launch
- Inject `ThresholdConfig` into `AlertStageEvaluator` and `TripMonitoringRuntime` via constructor

---

### Phase 4 — Performance

- Add `:core:benchmark` module with `implementation(libs.benchmark.macrobenchmark)`
- Write `NearWakeBaselineProfileGenerator` — launch app, navigate Home, open TripSetup
- Run on API 34 emulator: `./gradlew :core:benchmark:connectedBenchmarkAndroidTest`
- Copy generated `baseline-prof.txt` to `app/src/main/`
- Expected: 30–40% cold start reduction

---

### Phase 5 — Accessibility

- Run Android Studio Accessibility Scanner on all 15 screens
- All icon-only buttons — add `contentDescription`
- `NearWakeStateChip` + `NearWakeSelectableChip` — add `semantics { role = Role.Button }`
- Touch targets — verify ≥ 48dp on chips, back buttons, icon buttons
- Contrast — 4.5:1 on all text in both light and dark modes
- `AlertScreen` — add `liveRegion` so screen reader announces arrival without user interaction
- `LiveTripScreen` — ETA countdown accessible to TalkBack

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
| `domain/trip/engine/AlertStageEvaluator.kt` | Stage transitions + bias multiplier (thresholds hardcoded → Phase 3: ThresholdConfig) |
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
| `core/database/NearWakeDatabase.kt` | Room DB v5, all entities and DAOs |
| `core/database/di/DatabaseModule.kt` | Migrations 1→5, all DAO provisions |
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
| Module structure | ✅ 31 modules, clean separation | — |
| ViewModel lifecycle | ✅ All 15 use `viewModelScope` | — |
| Service thread safety | ✅ `Mutex` guards session mutations | — |
| Service shutdown | ✅ No `runBlocking` in `onDestroy()` | — |
| Error surfaces | ✅ `errorMessage: String?` in all 13 UiState | — |
| Architecture — write path | ✅ start/rearm/cancel/complete behind ports | — |
| Architecture — read path | ✅ All 21 observe use cases through TripLifecycleStore | — |
| Feature DAO isolation | ✅ Zero `core:database` imports in any feature module | — |
| Service delegation | ✅ Completion → `MarkTripCompletedUseCase`; cleanup → `TripCleanupUseCase` | — |
| Room database | ✅ v5, migrations 1→5, indices on all queried columns | — |
| Theme | ✅ Light / Dark / System, DataStore persistence | — |
| Place search | ✅ Real Google Places repository (not stub) | — |
| Departure reminders | ✅ AlarmManager, boot reschedule, UI wired | — |
| NavGraph | ✅ All 15 routes wired | — |
| ProGuard | ✅ R8 full mode, all libraries covered | — |
| SDK levels | ✅ targetSdk 35 / minSdk 26 | — |
| Permissions manifest | ✅ All 13 permissions, `FOREGROUND_SERVICE_LOCATION` | — |
| Test coverage | ✅ 42 test classes across all layers | — |
| AlertTriggerMode | ✅ TIME/DISTANCE/BOTH, wired in UI, persisted, tested | — |
| MAPS_API_KEY | ✅ Wired via local.properties | **Rotate key + add SHA-1 restriction before launch** |
| Crash reporting | ✅ Sentry initialized — DSN from BuildConfig, env-aware, 20% trace sampling | — |
| Behavioral analytics | ✅ `NearWakeAnalytics` + `SentryNearWakeAnalytics` — 6 events + `recordFailure()` at 7 sites | — |
| Privacy policy | 🟡 HTML ready + manifest wired — URL not hosted yet | Host + set `PRIVACY_POLICY_URL` |
| Remote config | 🔴 No `:core:remoteconfig` module | Phase 3 |
| Baseline Profile | 🔴 No `:core:benchmark`, no `baseline-prof.txt` | Phase 4 |
| Accessibility | 🔴 No TalkBack pass done | Phase 5 |
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

- Wear OS relay
- Commute analytics dashboard
- Optional backend / cloud sync
- Second-device companion
- Firebase Remote Config (`:core:remoteconfig` ships with hardcoded defaults in v1.0)
- Lock-screen widget — defer if notification lock-screen coverage is sufficient
- Ads (never)
