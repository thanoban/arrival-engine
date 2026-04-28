# Corrections & Issues Log

> Apply these fixes when development resumes. Do NOT touch code until ready to implement.

---

## Project Status Snapshot (2026-04-23)

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
| C-013 | `BootReceiver` (data/location) not declared in AndroidManifest | Medium | ✅ RESOLVED |

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

**Severity:** Medium — boot recovery silently never fires
**File:** `data/location/src/main/AndroidManifest.xml`

`BootReceiver` is now declared in `data/location/src/main/AndroidManifest.xml`:
```xml
<receiver
    android:name=".receivers.BootReceiver"
    android:enabled="true"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```
`RECEIVE_BOOT_COMPLETED` was already present in the app manifest.

---

## Architecture Notes (observed during scan)

**Event bus pattern added (not in original plan — good call):**
- `data/location/receivers/GeofenceEventBus.kt` — `SharedFlow` bridge between `GeofenceBroadcastReceiver` and `TripMonitoringService`
- `data/motion/receivers/ActivityTransitionEventBus.kt` — same pattern for activity transitions

This is the correct approach: BroadcastReceivers can't inject into Services directly, so a singleton SharedFlow acts as the bridge. No change needed.

**Monitoring boundary updated** — direct screen/app calls to `TripMonitoringService` have now been replaced by `:application:monitoring` + `:ports:monitoring`. This is a stronger long-term boundary than direct service control from UI-facing layers.
