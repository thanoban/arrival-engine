# Corrections & Issues Log

> Apply these fixes when development resumes. Do NOT touch code until ready to implement.

---

## Project Status Snapshot (2026-09-13)

**Overall:** Build system complete. All modules have source files. Core implementation is substantial and buildable.

**Completed:**
- build-logic (all 7 convention plugins)
- app (MainActivity, NavHost, Routes, HomeScreen)
- core/database (DB, 6 entities, 6 DAOs, 3 converters, DI)
- core/datastore (UserPreferences, DI)
- core/designsystem (Theme, Colors)
- core/network (shared OkHttp, Json, Retrofit builder, tests)
- core/testing (5 fakes: Location, Geofence, Activity, Routing, Fixtures)
- core/ui (Card, PrimaryButton, Scaffold)
- domain/location (4 models, 3 repository interfaces)
- domain/routing (RoutingDataSource interface, 3 models, repository interface)
- domain/trip (9 models, 4 engine files, 9 use cases)
- data/alerts (Service, Orchestrator, NotificationHelper, CleanupUseCase, Worker, DI)
- data/analytics (DiagnosticsLogger)
- data/location (FLP, Geofence, 2 repos, EventBus, 2 receivers, DI)
- data/motion (ActivityRecognition, repo, EventBus, receiver, DI)
- data/routing (NoOp fallback, GoogleTransit provider, Room-backed LocalRouteCache, repo, DI, route preview/live summary wiring)
- core/common (dispatchers, result wrapper, extensions, DI)
- feature/alerts, diagnostics, history, livetrip, onboarding, permissions, places, settings, tripsetup (all screens + ViewModels)

**Recent slices now reflected in code:**
- route preview is shown during trip setup and cached per trip
- live trip and trip summary show route/session data from persistence
- monitoring service restores and saves `TripSession` state through Room while active
- location bursts fall back to balanced tracking instead of silently ending monitoring
- ETA refreshes are throttled, stale estimates are rejected, and failures use bounded backoff
- live-trip and arrival screens no longer invent ETA values when routing is unavailable
- alert acknowledgement now records dismissal before monitoring stops
- all tracked correction items in this file are currently resolved

---

## Status Table

| ID | Description | Severity | Status |
|----|-------------|----------|--------|
| C-001 | Dead code in `AndroidApplicationComposeConventionPlugin` | Compile error | ✅ RESOLVED |
| C-002 | JUnit 5 consistency across modules | Medium | ✅ RESOLVED |
| C-003 | `core/testing` JUnit 4 remnant | Low | ✅ RESOLVED |
| C-004 | JUnit engine split pattern | Informational | ✅ RESOLVED |
| C-005 | Duplicate `kotlinx-datetime` in `core/common` | Low | ✅ RESOLVED |
| C-006 | Unnecessary `j2objc-annotations` in `feature/places` | Low | ✅ RESOLVED |
| C-007 | `core/common` has no source files | Medium | ✅ RESOLVED |
| C-008 | `core/network` has no source files | Low | ✅ RESOLVED |
| C-009 | No test source files written yet | Medium | ✅ RESOLVED |
| C-010 | `local.properties.template` missing | Low | ✅ RESOLVED |
| C-011 | Missing `ksp(libs.hilt.work.compiler)` in `app/build.gradle.kts` | Compile Error | ✅ RESOLVED |
| C-012 | `AlertReminderReceiver` directly constructs Hilt-managed `NotificationHelper` | Runtime Crash | ✅ RESOLVED |
| C-013 | Boot recovery must schedule real trip restoration | High | ✅ RESOLVED |
| C-014 | Distance arrivals recorded as ETA decisions | Medium | ✅ RESOLVED |
| C-015 | Transfer UI test depended on an untracked duplicate implementation | High | ✅ RESOLVED |
| C-016 | Live and arrival screens displayed fabricated ETA values | High | ✅ RESOLVED |
| C-017 | Trip creation persisted a fabricated 35-minute ETA | High | ✅ RESOLVED |
| C-018 | Alert acknowledgement never persisted dismissal time | Medium | ✅ RESOLVED |
| C-019 | Precise GPS burst completion could stop monitoring | Critical | ✅ RESOLVED |
| C-020 | ETA refresh and alert output could consume unbounded resources | High | ✅ RESOLVED |

---

## ✅ C-001 — RESOLVED
Dead/broken code removed from `AndroidApplicationComposeConventionPlugin.kt`.

## ✅ C-002 — RESOLVED
All modules on JUnit 5. `AndroidLibraryConventionPlugin` has `useJUnitPlatform()`.

## ✅ C-003 — RESOLVED
`core/testing` uses `api(libs.junit.jupiter)` + `api(libs.junit.jupiter.engine)`.

## ✅ C-004 — RESOLVED
All modules use `testRuntimeOnly` for engine, `testImplementation` for API.

## ✅ C-005 — RESOLVED
`core/common` duplicate `implementation(libs.kotlinx.datetime)` removed. Only `api` declaration remains.

## ✅ C-006 — RESOLVED
`j2objc-annotations` removed from `feature/places` and from `libs.versions.toml`.

---

## ✅ C-007 — RESOLVED

`core/common` now contains real Kotlin sources:
```
core/common/src/main/kotlin/com/nearwake/core/common/
├── AppDispatchers.kt
├── Result.kt
├── Extensions.kt
└── di/
    └── DispatchersModule.kt
```

`core/common/build.gradle.kts` also now applies the Hilt convention so dispatcher bindings are injectable.

---

## ✅ C-008 — RESOLVED

`core/network` now contains real Kotlin sources:
```
core/network/src/main/kotlin/com/nearwake/core/network/
├── NearWakeHttpClient.kt
└── di/
    └── NetworkModule.kt
```

It now provides shared `OkHttpClient`, `Json`, and `Retrofit.Builder` instances for modules that need network access. `data:routing` consumes that shared wiring instead of creating its own duplicate client/json providers.

---

## ✅ C-009 — RESOLVED

Test sources now exist in the repo, including:
- `domain/trip/src/test/.../TripStateMachineTest.kt`
- `domain/trip/src/test/.../ApproachEvaluatorTest.kt`
- `domain/trip/src/test/.../AlertDecisionEngineTest.kt`
- `data/location/src/test/.../LocationStrategyOrchestratorTest.kt`
- `data/routing/src/test/.../RoutingStubsTest.kt`
- `data/alerts/src/test/...`

---

## ✅ C-010 — RESOLVED

`local.properties.template` exists at project root and documents both `sdk.dir` and `MAPS_API_KEY`.

---

## ✅ C-011 — RESOLVED

**Severity:** Compile Error
**File:** `app/build.gradle.kts`

`app/build.gradle.kts` now includes:
```kotlin
ksp(libs.hilt.work.compiler)
```

This keeps the app module aligned with the Hilt WorkManager setup already used by the repo.

---

## ✅ C-012 — RESOLVED

**Severity:** Runtime Crash
**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/AlertReminderReceiver.kt`

`AlertReminderReceiver` now uses Hilt field injection:
```kotlin
@AndroidEntryPoint
class AlertReminderReceiver : BroadcastReceiver() {
    @Inject lateinit var notificationHelper: NotificationHelper
}
```

---

## ✅ C-013 — RESOLVED

**Severity:** High — boot recovery must restore active monitoring, not only log
**Files:** `data/alerts/src/main/AndroidManifest.xml`, `data/alerts/.../TripRecoveryBootReceiver.kt`

The production receiver is `TripRecoveryBootReceiver`, declared by `data:alerts`:
```xml
<receiver
    android:name=".worker.TripRecoveryBootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```
It enqueues `TripRecoveryWorker`, which reloads the active Room session and restarts monitoring. The duplicate `data/location` receiver was removed because it only wrote a log line and could falsely imply recovery had occurred. `RECEIVE_BOOT_COMPLETED` remains in the app manifest.

---

## ✅ C-014 — RESOLVED

Distance-radius alerts now persist `AlertReason.DISTANCE_THRESHOLD` instead of incorrectly reporting `ETA_THRESHOLD`. `AlertDecisionEngineTest` protects the diagnostic reason.

## ✅ C-015 — RESOLVED

`TransferProgressBuilder.kt` is tracked and limited to mapping application presentation models into UI state. Transfer timing remains owned by `ObserveLiveTripUseCase` and `TransferMonitor`, avoiding duplicate business logic across layers.

## ✅ C-016 — RESOLVED

Missing ETA is represented as nullable data. Live trip shows `ETA unavailable`; the arrival screen shows `EXIT NOW` without converting missing data to `0` or a placeholder countdown. Application tests cover both available and unavailable estimates.

## ✅ C-017 — RESOLVED

`StartTripUseCase` and `RearmTripUseCase` persist `null` when no route ETA exists instead of a fabricated 35-minute estimate. `TripCreationEtaTest` covers both creation paths.

## ✅ C-018 — RESOLVED

`AcknowledgeTripAlertUseCase` carries the trip ID through `TripMonitoringGateway`. The Android gateway now calls `AlertOrchestrator.dismissAlert()` before stopping the service, preserving acknowledgement timestamps for trust metrics.

## ✅ C-019 — RESOLVED

Precise location bursts transition back to balanced tracking after their bounded duration. Monitoring decisions run through a conflated channel so changing location modes cannot cancel the consumer handling the triggering sample. Cleanup attempts every resource release independently.

## ✅ C-020 — RESOLVED

`EtaRefreshPolicy` limits route requests, rejects stale results, isolates trip responses, and applies bounded retry backoff. Alert sound is asynchronously prepared and bounded to two minutes; vibration and reminder alarms no longer repeat indefinitely.

---

## Architecture Notes (observed during scan)

**Event bus pattern added (not in original plan — good call):**
- `data/location/receivers/GeofenceEventBus.kt` — `SharedFlow` bridge between `GeofenceBroadcastReceiver` and `TripMonitoringService`
- `data/motion/receivers/ActivityTransitionEventBus.kt` — same pattern for activity transitions

This is the correct approach: BroadcastReceivers can't inject into Services directly, so a singleton SharedFlow acts as the bridge. No change needed.

**Monitoring boundary updated** — direct screen/app calls to `TripMonitoringService` have now been replaced by `:application:monitoring` + `:ports:monitoring`. This is a stronger long-term boundary than direct service control from UI-facing layers.
