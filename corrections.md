# Corrections & Issues Log

> Apply these fixes when development resumes. Do NOT touch code until ready to implement.

---

## Project Status Snapshot (2026-04-23)

**Overall:** Build system complete. All modules have source files. Core implementation done.

**Completed:**
- build-logic (all 7 convention plugins)
- app (MainActivity, NavHost, Routes, HomeScreen)
- core/database (DB, 6 entities, 6 DAOs, 3 converters, DI)
- core/datastore (UserPreferences, DI)
- core/designsystem (Theme, Colors)
- core/testing (5 fakes: Location, Geofence, Activity, Routing, Fixtures)
- core/ui (Card, PrimaryButton, Scaffold)
- domain/location (4 models, 3 repository interfaces)
- domain/routing (RoutingDataSource interface, 3 models, repository interface)
- domain/trip (9 models, 4 engine files, 9 use cases)
- data/alerts (Service, Orchestrator, NotificationHelper, CleanupUseCase, Worker, DI)
- data/analytics (DiagnosticsLogger)
- data/location (FLP, Geofence, 2 repos, EventBus, 2 receivers, DI)
- data/motion (ActivityRecognition, repo, EventBus, receiver, DI)
- data/routing (NoOp fallback, GoogleTransit provider, Room-backed LocalRouteCache, repo, DI)
- core/common (dispatchers, result wrapper, extensions, DI)
- feature/alerts, diagnostics, history, livetrip, onboarding, permissions, places, settings, tripsetup (all screens + ViewModels)

**Still intentionally thin / future-facing:**
- `core/network` source files (manifest exists, no Kotlin implementation yet)

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
| C-008 | `core/network` has no source files | Low | Pending |
| C-009 | No test source files written yet | Medium | ✅ RESOLVED |
| C-010 | `local.properties.template` missing | Low | ✅ RESOLVED |

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

## C-008 — `core/network` has no source files

**Severity:** Low — no feature currently depends on it (backend is deferred), but it's declared

**What's needed when backend is added:**
```
core/network/src/main/kotlin/com/nearwake/core/network/
├── NearWakeHttpClient.kt    ← OkHttpClient factory (logging, timeouts)
└── di/
    └── NetworkModule.kt     ← Hilt module providing OkHttpClient + Retrofit
```
Safe to leave empty until backend work begins.

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

## Architecture Notes (observed during scan)

**Event bus pattern added (not in original plan — good call):**
- `data/location/receivers/GeofenceEventBus.kt` — `SharedFlow` bridge between `GeofenceBroadcastReceiver` and `TripMonitoringService`
- `data/motion/receivers/ActivityTransitionEventBus.kt` — same pattern for activity transitions

This is the correct approach: BroadcastReceivers can't inject into Services directly, so a singleton SharedFlow acts as the bridge. No change needed.

**`TripMonitoringStarter.kt` added in `data/alerts`** — helper to start/stop the foreground service. Clean separation. No change needed.
