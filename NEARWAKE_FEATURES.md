# NearWake — Complete Feature Catalog

**Last updated:** 2026-05-17
**Architecture:** 37-module Clean Architecture (Kotlin + Jetpack Compose)
**Package:** `com.nearwake.app`

This document is the single source of truth for every feature NearWake has, is partially building, or has planned. Organized by the eight product pillars. Nothing is in this file that is not in one of those pillars.

---

## Status Legend

| Symbol | Meaning |
|--------|---------|
| ✅ | Shipped — in v1.0 codebase, fully working |
| 🟡 | Partial — infrastructure in place, not fully complete |
| 🔴 I | Planned — Wave I (v1.1, no backend required) |
| 🔴 J | Planned — Wave J (v1.5, new integrations, no backend) |
| 🔴 K | Planned — Wave K (v2.0, backend required) |

---

## Pillar 1 — Depart on Time

*"When do I leave?"*

The user should never miss a departure because they forgot to check the time, lost track of their schedule, or had to manually open the app.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| Commute prediction from trip history | ✅ | `:domain:commute` | `CommutePredictionEngine` |
| Leave-by prediction (±30 min window) | ✅ | `:domain:commute` | `CommutePrediction` |
| Departure reminder scheduling | ✅ | `:data:alerts` | `DepartureReminderScheduler` |
| AlarmManager boot reschedule | ✅ | `:data:alerts` | `DepartureReminderBootReceiver` |
| Departure reminder screen | ✅ | `:feature:departure` | `DepartureReminderScreen` |
| Commute prediction persistence | ✅ | `:core:database` | `CommutePredictionEntity`, `CommutePredictionDao` |
| Saved Trip Profiles (1-tap arm) | 🔴 I | `:core:database`, `:feature:profiles` | `TripProfile`, `StartTripFromProfileUseCase` |
| Scheduled recurring trips | 🔴 I | `:data:alerts` | `TripScheduleManager`, `ArmTripFromScheduleReceiver` |
| Home screen widget — Active ETA | 🔴 I | `:feature:widget` | `ActiveTripWidget` (Glance) |
| Home screen widget — Quick Arm | 🔴 I | `:feature:widget` | `QuickArmWidget` (Glance) |
| Google Calendar integration | 🔴 J | `:data:calendar` | `CalendarEventReader`, `CalendarTripSuggestor` |
| Commute analytics dashboard | 🔴 J | `:feature:analytics` | `BuildCommuteAnalyticsUseCase`, `CommuteAnalyticsViewModel` |
| Weekly commute report notification | 🔴 J | `:data:alerts` | `CommuteReportWorker` |
| Route comparison (fastest route surfaces) | 🔴 J | `:domain:commute` | Extend `CommutePredictionEngine` |
| Google Assistant App Actions | 🔴 J | `:app` | `actions.xml`, `AppActionsCallback` |

---

## Pillar 2 — Board Right

*"Am I on the right vehicle?"*

The user arms a trip and boards. The engine verifies they are moving in the correct direction before monitoring begins.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| BoardingValidator — bearing check | ✅ | `:domain:trip` | `BoardingValidator` |
| WRONG_DIRECTION warning notification | ✅ | `:data:alerts` | `NotificationHelper` (BOARDING channel) |
| Direction reversal detection during monitoring | ✅ | `:data:alerts` | `TripMonitoringRuntime` |

---

## Pillar 3 — Ride Awake or Asleep

*"Can I relax now?"*

The user should be able to close their eyes, put away their phone, or zone out — and trust NearWake to wake them up. Both awake (distracted) and asleep commuters are first-class users.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| Active mode alert personality | ✅ | `:domain:trip` | `AlertMode.ACTIVE` |
| Sleep mode alert personality | ✅ | `:domain:trip` | `AlertMode.SLEEP` |
| Sleep mode mid-trip toggle | ✅ | `:application:trip` | `UpdateTripAlertModeUseCase` |
| Alert trigger mode: Time / Distance / Both | ✅ | `:domain:trip` | `AlertTriggerMode` |
| Trigger mode persisted in DataStore | ✅ | `:core:datastore` | `UserPreferencesDataStore` |
| Trigger mode user-selectable in TripSetup | ✅ | `:feature:tripsetup` | `TripSetupScreen` |
| Confidence chip — HIGH / MEDIUM / LOW | ✅ | `:core:ui` | `NearWakeStatus`, `StateChip` |
| Bias-early engine — 1.0× / 1.15× / 1.25× | ✅ | `:domain:trip` | `AlertStageEvaluator` (ThresholdConfig-injected) |
| Bias-early banner ("Alerting earlier") | ✅ | `:application:trip` | `ObserveLiveTripUseCase` → `biasEarlyMessage` |
| Underground-dignified mode (OFFLINE named state) | ✅ | `:domain:routing` | `RouteSignalQuality.OFFLINE` |
| OFFLINE bias — 25% earlier threshold | ✅ | `:core:remoteconfig` | `ThresholdConfig.biasMultiplierOffline` |
| All thresholds configurable via ThresholdConfig | ✅ | `:core:remoteconfig` | `ThresholdConfig`, `StaticRemoteConfigRepository` |
| Per-route bias override (sleep auto-select by destination) | 🔴 I | `:core:database` | `SavedPlaceEntity.alwaysUseSleepMode`, MIGRATION_7_8 |
| Home screen widget — glanceable ETA | 🔴 I | `:feature:widget` | `ActiveTripWidget` (Glance) |
| Lock-screen widget (ETA without unlock) | 🔴 I | `:feature:widget` | Glance `WIDGET_CATEGORY_KEYGUARD` |
| Do Not Disturb override for Stage C | 🔴 J | `:data:alerts` | `NotificationHelper`, `ACCESS_NOTIFICATION_POLICY` |
| Wear OS — wrist ETA + haptic stages | 🔴 K | `:feature:wear` | `WearEtaService`, DataLayer API |

---

## Pillar 4 — Transfer Confidently

*"Do I change here?"*

Multi-leg journeys require per-leg monitoring. The user gets a distinct alert before each transfer point, not just the final destination.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| TransferMonitor — per-leg progress tracking | ✅ | `:domain:trip` | `TransferMonitor` |
| Per-leg RouteSignalQuality chip | ✅ | `:domain:routing` | `RouteSignalQuality` (HIGH / DEGRADED / OFFLINE) |
| Transfer approach notification | ✅ | `:data:alerts` | `NotificationHelper` (CHANNEL_APPROACH) |
| Transfer progress strip in LiveTrip UI | ✅ | `:feature:livetrip` | `TransferProgressCard` |
| Route snapshot cached per trip | ✅ | `:data:routing` | `RouteSnapshotDao`, `RouteSnapshotEntity` |

---

## Pillar 5 — Arrive Correctly

*"Is this my stop?"*

Three-stage escalating alerts with confidence-based timing — the core differentiator from all single-threshold competitors.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| Stage A — APPROACH alert (2 stops / 500m / 5 min) | ✅ | `:domain:trip` | `AlertStageEvaluator` |
| Stage B — IMMINENT alert (next stop / 100m / 90 s) | ✅ | `:domain:trip` | `AlertStageEvaluator` |
| Stage C — ARRIVAL alert (at stop / 30m / <30 s) | ✅ | `:domain:trip` | `AlertStageEvaluator` |
| Distinct notification channels per stage | ✅ | `:data:alerts` | `NotificationHelper` (APPROACH / IMMINENT / ALERT) |
| Stage A — IMPORTANCE_DEFAULT, no popup | ✅ | `:data:alerts` | `NotificationHelper` |
| Stage B — IMPORTANCE_HIGH, heads-up, vibration | ✅ | `:data:alerts` | `NotificationHelper` |
| Stage C (Sleep) — full alarm, max vibration, repeats | ✅ | `:data:alerts` | `AlertOrchestrator` |
| Sleep mode haptic preamble (30 s before Stage C) | ✅ | `:data:alerts` | `AlertOrchestrator` |
| Geofence-based destination detection | ✅ | `:data:alerts` | `TripMonitoringService`, `GeofencingClient` |
| FusedLocationProvider burst GPS (not continuous) | ✅ | `:data:location` | `FusedLocationDataSource` |
| Location strategy orchestration | ✅ | `:data:location` | `LocationStrategyOrchestrator` |
| Activity Recognition transition API | ✅ | `:data:motion` | (`:data:motion` module) |
| Alert screen (Stage C UI) | ✅ | `:feature:alerts` | `AlertScreen`, `AlertViewModel` |

---

## Pillar 6 — Recover Gracefully

*"I missed it — now what?"*

Missing a stop is not a failure state — it is a managed event. The Recovery screen is a first-class destination, not an error screen.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| OvershootDetector — distance increase after Stage C | ✅ | `:domain:trip` | `OvershootDetector` |
| RecoveryPlanner — nearest return stop + walk-back | ✅ | `:domain:trip` | `RecoveryPlanner` |
| Recovery screen — first-class destination | ✅ | `:feature:alerts` | `RecoveryScreen`, `RecoveryViewModel` |
| Recovery actions — resume / end / re-arm | ✅ | `:feature:alerts` | `RecoveryScreen` |
| One-tap re-arm (no setup screen) | ✅ | `:application:trip` | `RearmTripUseCase` |
| TripRecoveryWorker — restore after process death | ✅ | `:data:alerts` | `TripRecoveryWorker` (WorkManager) |
| WorkManager + ForegroundService (survives process death) | ✅ | `:data:alerts` | `TripMonitoringService` |
| Session persistence across restarts | ✅ | `:ports:persistence` | `TripLifecycleStore` → `TripSessionEntity` |

---

## Pillar 7 — Walk the Last Mile

*"How do I get from stop to door?"*

After the trip ends, the user still needs to reach the final destination. NearWake provides a lightweight walk-to-finish guidance signal.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| WalkFinish screen — distance + heading to destination | ✅ | `:feature:walkfinish` | `WalkFinishScreen`, `WalkFinishViewModel` |
| ObserveWalkFinishUseCase | ✅ | `:application:trip` | `ObserveWalkFinishUseCase` |

---

## Pillar 8 — Confirm Arrival

*"Did I make it?"*

The journey is not complete until the user (or their family) knows they arrived. NearWake closes the loop.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| Trip summary screen (post-arrival) | ✅ | `:feature:history` | `TripSummaryScreen`, `TripSummaryUiState` |
| ObserveTripSummaryUseCase | ✅ | `:application:trip` | `ObserveTripSummaryUseCase` |
| Companion screen — arrival confirmation flow | ✅ | `:feature:companion` | `CompanionScreen`, `CompanionViewModel` |
| ObserveCompanionUseCase | ✅ | `:application:trip` | `ObserveCompanionUseCase` |
| Trip history — full session log | ✅ | `:feature:history` | `HistoryScreen`, `ObserveTripHistoryUseCase` |
| CSV export — share trip history for field evidence | ✅ | `:application:trip` | `BuildTripHistoryCsvUseCase` |
| Arrival notification to contact — 1-tap SMS/WhatsApp | 🔴 I | `:feature:companion` | `ArrivalMessageComposer`, `Intent.ACTION_SEND` |
| Trip share link — live tracking URL (browser, no install) | 🔴 K | `:feature:share` + backend | Firebase RTDB + web page |
| Family account — parent sees child's active trip | 🔴 K | `:feature:family` + backend | Firestore, `FamilyTripStatusPusher`, FCM |
| Family push notification — "Kavya has arrived" | 🔴 K | Backend | `onTripCompleted` Cloud Function + FCM |

---

## Cross-Cutting — Trust, Transparency, and Infrastructure

These features span all pillars. They are what make NearWake different from every competitor in tone and character.

| Feature | Status | Module | Key Class |
|---------|--------|--------|-----------|
| Confidence chip on every prediction screen | ✅ | `:core:ui` | `NearWakeStatus`, `StateChip` |
| Bias-early banner — "Alerting earlier" | ✅ | `:application:trip` | `ObserveLiveTripUseCase` |
| Fail visible — degraded permissions surfaced to user | ✅ | `:feature:permissions` | `PermissionsScreen`, `PermissionsViewModel` |
| Runtime permission staging (course → fine → background → notification) | ✅ | `:feature:permissions` | `PermissionsScreen` |
| OEM reliability guidance (Samsung / Xiaomi / OPPO / Pixel) | ✅ | `:feature:settings` | `OemReliabilityScreen` |
| Diagnostics screen — per-event confidence, stage, distance, ETA | ✅ | `:feature:diagnostics` | `DiagnosticsScreen`, `DiagnosticsViewModel` |
| Diagnostics CSV export | ✅ | `:application:trip` | `BuildTripHistoryCsvUseCase` |
| Battery-honest design — geofences first, GPS in bursts | ✅ | `:data:location` | `FusedLocationDataSource`, `LocationStrategyOrchestrator` |
| Dark-first design system | ✅ | `:core:designsystem` | `NearWakeColors`, `NearWakeTheme`, `LocalNearWakeColors` |
| Deep Transit Navy palette — #060C18 → #1A2D42 backgrounds; Slate text scale | ✅ | `:core:designsystem` | `NearWakeColors` (BgBase/BgSurface/BgElevated/BgHighest) |
| BrandBase (#4A7FFF) transit blue accent — non-semantic screens | ✅ | `:core:designsystem` | `NearWakeColors.BrandBase`, `NearWakeColorRoles.brandBase` |
| SafeBase green — semantic reservation for arrival/safe states only | ✅ | All screens | WalkFinish, Companion, TripSummary only |
| Light / Dark / System theme selection | ✅ | `:feature:settings` | `SettingsScreen`, `ThemeMode` DataStore |
| Compact UI tokens — 56dp search rows, 80dp trip cards | ✅ | `:core:designsystem` | `NearWakeSpacing`, `cardCompact/cardDefault/cardLarge` |
| Icon-driven status strips — 3-icon strip, 32dp | ✅ | `:feature:livetrip` | `LiveTripScreen` |
| NearWakeButtonSize enum (Small / Medium / Large) | ✅ | `:core:ui` | `NearWakeButtonSize` |
| PlaceResultRow composable — 56dp tap-to-select | ✅ | `:core:ui` | `PlaceResultRow` |
| QuickActionsStrip — 5-icon scrollable row (Search, History, Departure, Settings, Permissions) | ✅ | `:app` | `HomeScreen.QuickActionsStrip` |
| AlertModeTabRow — section-tab Active/Sleep selector with 2dp underline indicator | ✅ | `:feature:livetrip` | `LiveTripScreen.AlertModeTabRow` |
| Filled ActiveTripCard — MonitoringBase-tinted stat card with circle icon | ✅ | `:app` | `HomeScreen.ActiveTripCard` |
| Accessibility semantics — all 16 screens | ✅ | All feature modules | `contentDescription`, `mergeDescendants`, `stateDescription`, `Role.Tab` |
| Sentry crash and non-fatal reporting | ✅ | `:data:analytics`, `:app` | `SentryNearWakeAnalytics`, `NearWakeApp.initSentry()` |
| Behavioral analytics — 6 events + recordFailure() | ✅ | `:ports:analytics`, `:data:analytics` | `NearWakeAnalytics`, `SentryNearWakeAnalytics` |
| Baseline Profile — cold start optimization | ✅ | `:core:benchmark` | `NearWakeBaselineProfileGenerator` |
| Startup benchmark | ✅ | `:core:benchmark` | `NearWakeStartupBenchmark` |
| StrictMode in debug builds | ✅ | `:app` | `NearWakeApp.enableStrictModeIfDebug()` |
| ThresholdConfig — all engine constants injectable | ✅ | `:core:remoteconfig` | `ThresholdConfig` |
| StaticRemoteConfigRepository — v1.0 defaults, Firebase drop-in | ✅ | `:core:remoteconfig` | `StaticRemoteConfigRepository` |
| ProGuard / R8 full mode — minify + shrink | ✅ | `:app` build config | `build.gradle.kts` |
| No account required for core value | ✅ | All | Offline-first, no auth layer |
| No ads — in code, in build config, in Play listing | ✅ | — | Non-negotiable commitment |
| Privacy policy manifest metadata | ✅ | `:app` | `AndroidManifest.xml`, `PRIVACY_POLICY_URL` |
| Firebase Remote Config live tuning (post-launch) | 🔴 K | `:core:remoteconfig` | Drop-in for `StaticRemoteConfigRepository` |
| Cloud sync — trip history across devices | 🔴 K | Backend | Firebase Realtime Database |
| iOS app — KMM shared domain layer | 🔴 K | KMM | Share `:domain:*`, `:application:trip` |

---

## Module-to-Pillar Map

Every module in the 37-module architecture maps to one or more pillars:

| Module | Pillar(s) | Role |
|--------|-----------|------|
| `:app` | All | NavHost, Application class, Hilt entry point |
| `:application:monitoring` | 3, 5, 6 | StartTripMonitoringUseCase, StopTripMonitoringUseCase |
| `:application:trip` | All | 21 use cases — all product operations |
| `:ports:monitoring` | 3, 5, 6 | TripMonitoringGateway contract |
| `:ports:persistence` | All | TripLifecycleStore contract (13 methods) |
| `:ports:analytics` | Cross | NearWakeAnalytics contract (7 methods) |
| `:core:common` | Cross | Shared utilities |
| `:core:database` | All | Room v7 — 7 entities, 7 DAOs, migrations 1→7 |
| `:core:datastore` | 3, Cross | UserPreferences, ThemeMode, AlertTriggerMode |
| `:core:designsystem` | Cross | NearWakeColors, NearWakeTheme, NearWakeSpacing |
| `:core:network` | 1, 4, 5 | OkHttp + Retrofit for routing |
| `:core:remoteconfig` | 3, 5 | ThresholdConfig — all tunable engine constants |
| `:core:testing` | Cross | Shared test utilities |
| `:core:ui` | Cross | NearWakeScaffold, SurfaceCard, PlaceResultRow, NearWakeButtonSize |
| `:core:benchmark` | Cross | Baseline Profile, startup benchmark |
| `:domain:trip` | 2, 3, 4, 5, 6 | AlertStageEvaluator, TripEngine, TransferMonitor, BoardingValidator |
| `:domain:location` | 3, 5 | Location domain models |
| `:domain:routing` | 4, 5 | RouteSnapshot, Stop, RouteSignalQuality |
| `:domain:commute` | 1 | CommutePredictionEngine, CommutePrediction |
| `:data:alerts` | 3, 5, 6 | TripMonitoringService, AlertOrchestrator, NotificationHelper, DepartureReminderScheduler |
| `:data:location` | 3, 5 | FusedLocationDataSource, LocationStrategyOrchestrator |
| `:data:motion` | 3 | Activity Recognition transition |
| `:data:routing` | 4, 5 | Google Directions, route cache |
| `:data:analytics` | Cross | DiagnosticsLogger, SentryNearWakeAnalytics |
| `:data:patterns` | 1 | CommutePredictionRepository |
| `:feature:onboarding` | Cross | First-run walkthrough |
| `:feature:permissions` | Cross | Staged permission requests, OEM guidance |
| `:feature:places` | 1 | Google Places search + local fallback |
| `:feature:tripsetup` | 1, 3 | Trip configuration, trigger mode, route preview |
| `:feature:livetrip` | 3, 4, 5 | Live monitoring UI, transfer strip, ETA strip |
| `:feature:alerts` | 5, 6 | AlertScreen (Stage C), RecoveryScreen |
| `:feature:history` | 8 | Trip history, trip summary |
| `:feature:settings` | Cross | Appearance, alert defaults, OEM reliability |
| `:feature:diagnostics` | Cross | Diagnostics events, CSV export |
| `:feature:walkfinish` | 7 | Distance + heading to destination |
| `:feature:companion` | 8 | Arrival confirmation, contact notification |
| `:feature:departure` | 1 | Departure predictions, leave-by screen |

---

## Delivery Summary

| Wave | Release | Backend | Features | Status |
|------|---------|---------|----------|--------|
| A–H | v1.0 | None | All 8 pillars, core alert engine, 15 screens, 37 modules | ✅ Shipped |
| I | v1.1 | None | Trip profiles, scheduled trips, widgets, arrival SMS, per-route bias | 🔴 Planned |
| J | v1.5 | None | Calendar integration, commute analytics, assistant, DND override | 🔴 Planned |
| K | v2.0 | Firebase | Wear OS, family tracking, trip share link, cloud sync, iOS (KMM) | 🔴 Planned |

**Non-negotiables across all waves:**
- Alert path is never gated by tier
- No ads, ever
- No mandatory account for core value
- Confidence visible on every prediction
- Bias-early when uncertain
- Fail visible, not silent
