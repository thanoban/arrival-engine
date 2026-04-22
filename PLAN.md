# NearWake — Arrival Alarm App: Full End-to-End Development Plan

**Working name:** NearWake  
**Package:** `com.nearwake.app`  
**Platform:** Android-first (Kotlin + Jetpack Compose)  
**Status:** Pre-development — scaffold partially created, full plan documented here  
**Last updated:** 2026-04-22

---

## 1. Problem Statement

Users miss their bus stop, train transfer, or destination because they fall asleep, get distracted, or can't rely on real-time transit information. NearWake arms a trip in seconds and monitors in the background with minimal battery impact, alerting before the user misses their stop.

**This is NOT:**
- A live tracking app (no continuous GPS)
- A safety/emergency app
- A social sharing app
- A transit schedule app

**This IS:** An arrival assurance app — set a destination, arm the trip, sleep safely.

---

## 2. Decisions Made

| Decision | Choice | Reason |
|---|---|---|
| Backend | None for MVP | 100% offline-first; alert must fire on-device |
| Route data | Destination-only MVP; Google Transit Phase 2 | Eliminates external data dependency for MVP |
| Maps/Search | Google Maps SDK + Places Autocomplete | Best Android UX, one ecosystem; only called during setup |
| Location during monitoring | Geofences + Activity Recognition (OS-managed) | Zero battery drain; survives process death |
| Precise GPS | Short burst ≤90s only near approach window | Battery-efficient; FLP stopped after decision |
| App name | NearWake (working title) | Easy to rename before launch |
| Architecture | Multi-module, MVVM + Clean, Hilt | Scalable, testable, per spec |
| State persistence | Room DB | Survives process death and OOM kill |

---

## 3. Module Structure

```
:app                         → Application entry point, Hilt, NavGraph
:core:common                 → Extensions, result types, coroutine helpers, constants
:core:database               → Room DB, all DAOs, entities, converters
:core:datastore              → DataStore preferences (UserSettings)
:core:designsystem           → Theme, colors, typography, shared Composables
:core:network                → Retrofit/OkHttp (future backend)
:core:testing                → Fake providers, test fixtures, shared test utils
:core:ui                     → Reusable screen-level Composable building blocks

:domain:trip                 → Trip/TripSession entities, TripEngine state machine, use cases
:domain:location             → Location, LatLng, GeofenceSpec, MotionState interfaces
:domain:routing              → RoutingDataSource interface, RouteSnapshot, Stop, TransferPoint

:data:location               → FusedLocationDataSource, GeofenceDataSource, LocationStrategyOrchestrator
:data:motion                 → ActivityRecognitionDataSource
:data:routing                → NoOpRoutingDataSource (MVP), GoogleTransitDataSource stub
:data:alerts                 → TripMonitoringService, AlertOrchestrator, AlertDecisionEngine
:data:analytics              → DiagnosticsLogger, local event queue

:feature:onboarding          → Welcome screen, value prop carousel
:feature:permissions         → Permission rationale, decline-safe UX, settings-recovery
:feature:places              → Google Places Autocomplete, saved places management
:feature:tripsetup           → Trip config: destination, lead time, alert intensity
:feature:livetrip            → Live monitoring screen: state, mode, battery, ETA
:feature:alerts              → Full-screen alarm, dismissal, snooze
:feature:history             → Past trips list + detail
:feature:settings            → Alert intensity, defaults, monitoring prefs
:feature:diagnostics         → QA/debug screen: engine state, geofence list, log stream
```

---

## 4. Technology Stack

```
Kotlin                     2.0.21
AGP                        8.7.3
Jetpack Compose BOM        2024.12.01
Hilt                       2.52
Room                       2.6.1
DataStore                  1.1.1
WorkManager                2.10.0
Navigation Compose         2.8.5
Lifecycle                  2.8.7
Coroutines                 1.9.0
Google Play Services Location  21.3.0   (FLP + Geofencing + Activity Recognition)
Google Maps SDK            (maps-compose 6.2.1)
Google Places SDK          4.1.0
Retrofit                   2.11.0
OkHttp                     4.12.0
Kotlinx Serialization      1.7.3
Kotlinx DateTime           0.6.1
Timber                     5.0.1
Sentry Android             7.18.0
Coil                       2.7.0
MockK                      1.13.13
Turbine                    1.2.0
Truth                      1.4.4
```

---

## 5. Core Domain Model

### 5.1 Entities

```kotlin
// ── domain:trip ──────────────────────────────────────────────────────────────

data class Trip(
    val id: String,                         // UUID
    val destination: SavedPlace,
    val alertLeadMinutes: Int,              // 0 = "alert when near"
    val alertIntensity: AlertIntensity,     // GENTLE | STANDARD | LOUD
    val routeSnapshot: RouteSnapshot?,      // null for destination-only (MVP)
    val createdAt: Instant,
    val completedAt: Instant?
)

data class TripSession(
    val tripId: String,
    val state: TripState,
    val monitoringMode: MonitoringMode,
    val lastKnownLat: Double?,
    val lastKnownLng: Double?,
    val lastEtaMinutes: Int?,
    val confidence: Confidence,             // HIGH | DEGRADED | OFFLINE
    val geofenceIds: List<String>,          // registered geofence IDs
    val updatedAt: Instant
)

enum class TripState {
    Idle, Armed, WaitingForMovement,
    MonitoringLowPower, MonitoringApproach,
    Alerting, Recovery, Completed, Cancelled, FailedGracefully
}

enum class MonitoringMode { GEOFENCE_ONLY, BALANCED, PRECISE_BURST }
enum class AlertIntensity { GENTLE, STANDARD, LOUD }
enum class Confidence { HIGH, DEGRADED, OFFLINE }

data class TripRule(
    val alertLeadMinutes: Int,
    val approachRadiusMeters: Float,        // default 1500m
    val destinationRadiusMeters: Float,     // default 300–500m
    val offlineBiasPercent: Int,            // default 20 (alert 20% earlier when offline)
    val preciseBurstMaxSeconds: Int,        // default 90
    val noMotionTimeoutMinutes: Int         // default 15
)

data class AlertDecision(
    val tripId: String,
    val reason: AlertReason,               // ETA_THRESHOLD | GEOFENCE_ENTERED | OVERSHOOT
    val confidence: Confidence,
    val decidedAt: Instant
)

data class AlertEvent(
    val id: String,
    val tripId: String,
    val firedAt: Instant,
    val dismissedAt: Instant?,
    val type: AlertType                    // APPROACH | ARRIVAL | RECOVERY
)

// ── domain:location ──────────────────────────────────────────────────────────

data class SavedPlace(
    val id: String,
    val name: String,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeId: String?                   // Google Place ID for refresh
)

data class LatLng(val lat: Double, val lng: Double)

data class GeofenceSpec(
    val id: String,
    val center: LatLng,
    val radiusMeters: Float,
    val type: GeofenceType                 // APPROACH | DESTINATION
)

data class MotionState(
    val type: MotionType,
    val confidence: Int,                   // 0–100
    val detectedAt: Instant
)

enum class MotionType { IN_VEHICLE, ON_BICYCLE, ON_FOOT, STILL, UNKNOWN }
enum class GeofenceType { APPROACH, DESTINATION }

// ── domain:routing ────────────────────────────────────────────────────────────

interface RoutingDataSource {
    suspend fun fetchRoute(origin: LatLng, destination: LatLng): Result<RouteSnapshot>
    suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int> // minutes
}

data class RouteSnapshot(
    val tripId: String,
    val stops: List<Stop>,
    val transfers: List<TransferPoint>,
    val totalDurationMinutes: Int,
    val fetchedAt: Instant,
    val isStale: Boolean
)

data class Stop(val name: String, val lat: Double, val lng: Double, val order: Int)
data class TransferPoint(val stop: Stop, val lineName: String, val arrivalMinutes: Int)
```

---

## 6. Trip Engine — State Machine

### 6.1 States & Transitions

| From State | Trigger | To State | Side Effect |
|---|---|---|---|
| Idle | User taps Start Trip | Armed | Persist TripSession, register geofences, register activity transitions |
| Armed | Activity: IN_VEHICLE detected | MonitoringLowPower | Start balanced FLP (60s interval) |
| Armed | Approach geofence entered | MonitoringApproach | Start precise burst |
| Armed | 15 min no motion | WaitingForMovement | Show "are you moving?" prompt |
| WaitingForMovement | Motion detected | MonitoringLowPower | Resume |
| WaitingForMovement | User confirms moving | MonitoringLowPower | Resume |
| MonitoringLowPower | ETA ≤ approach threshold | MonitoringApproach | Escalate to precise burst (5s, 90s max) |
| MonitoringLowPower | Approach geofence entered | MonitoringApproach | Escalate |
| MonitoringLowPower | Network lost | MonitoringLowPower | confidence = OFFLINE; bias = early |
| MonitoringApproach | Destination geofence entered | Alerting | Stop all location, fire alert |
| MonitoringApproach | Precise burst confirms < 300m | Alerting | Stop all location, fire alert |
| Alerting | User dismisses alert | Completed | Log success, release all resources |
| Alerting | 5 min no dismissal | Recovery | Fire overshoot detection burst |
| Recovery | User taps "End Trip" | Completed | Release all resources |
| Recovery | User taps "Reroute" | MonitoringLowPower | Restart monitoring to new destination |
| Any | User cancels | Cancelled | Stop all monitoring, clean up geofences |
| Any | App restart + Active session in DB | (restore state) | Re-arm service from persisted state |

### 6.2 Key Engine Rules

1. **Precision gate**: Precise burst only enters when ETA ≤ `alertLeadMinutes + approach_buffer`
2. **Offline bias**: When `confidence == OFFLINE`, alert threshold = `effectiveThreshold * 1.20` (alert earlier)
3. **Geofence sizing**: Approach = 1500m, Destination = max(300m, estimated GPS drift)
4. **Cleanup mandatory**: Every exit path calls `TripCleanupUseCase` → removes geofences, stops FLP, stops service
5. **Process death recovery**: On app start, check Room for `TripSession.state == Active` → restart service

### 6.3 Core Engine Files

```
domain/trip/src/main/kotlin/com/nearwake/domain/trip/
├── model/
│   ├── Trip.kt
│   ├── TripSession.kt
│   ├── TripState.kt
│   ├── TripRule.kt
│   ├── AlertDecision.kt
│   └── AlertEvent.kt
├── engine/
│   ├── TripEngine.kt            ← main orchestrator, takes TripEvent, emits TripState
│   ├── TripStateMachine.kt      ← pure deterministic state transitions
│   ├── ApproachEvaluator.kt     ← given location + ETA → should we escalate?
│   ├── OvershootDetector.kt     ← did user pass destination without alert?
│   └── AlertDecisionEngine.kt   ← fire / hold / recover decision
└── usecase/
    ├── ArmTripUseCase.kt
    ├── CancelTripUseCase.kt
    ├── CompleteTrip UseCase.kt
    ├── ResumeTripUseCase.kt
    ├── EvaluateApproachUseCase.kt
    ├── FireArrivalAlertUseCase.kt
    ├── DetectOvershootUseCase.kt
    ├── EnterRecoveryModeUseCase.kt
    └── ComputeBatterySummaryUseCase.kt
```

---

## 7. Location Strategy

### 7.1 Five-Tier Priority

```
Tier 1 — Last known location (immediate, 0 battery)
         → used at app start to seed initial state

Tier 2 — Activity Recognition transitions (OS-managed, ~0 battery)
         → wakes engine when user enters/exits vehicle

Tier 3 — Geofence triggers (OS-managed, ~0 battery)
         → primary mechanism for detecting proximity

Tier 4 — Balanced-power FLP (only in MonitoringLowPower)
         → interval: 60s, displacement: 100m
         → used to track progress between geofence checks

Tier 5 — Precise burst (only in MonitoringApproach)
         → PRIORITY_HIGH_ACCURACY, 5s interval, max 90 seconds
         → stopped immediately after alert decision
```

### 7.2 What Gets Registered at Trip Arm

```
┌─ TripArmed ─────────────────────────────────────┐
│                                                  │
│  1. Register APPROACH geofence (1500m radius)    │
│  2. Register DESTINATION geofence (300m radius)  │
│  3. Register ActivityTransition:                 │
│       ENTERING/EXITING IN_VEHICLE               │
│                                                  │
│  ← No FLP yet. Zero active GPS polling.          │
└──────────────────────────────────────────────────┘
```

### 7.3 Implementation Files

```
data/location/src/main/kotlin/com/nearwake/data/location/
├── FusedLocationDataSource.kt          ← wraps FLP; manages callback lifecycle
├── GeofenceDataSource.kt               ← wraps GeofencingClient; registers/removes fences
├── LocationStrategyOrchestrator.kt     ← coordinates 5-tier escalation
├── receivers/
│   ├── GeofenceBroadcastReceiver.kt    ← handles GeofencingEvent → sends to TripEngine
│   └── BootReceiver.kt                 ← restarts monitoring service after device reboot

data/motion/src/main/kotlin/com/nearwake/data/motion/
├── ActivityRecognitionDataSource.kt    ← wraps ActivityRecognitionClient
└── receivers/
    └── ActivityTransitionReceiver.kt   ← handles ActivityTransitionEvent → sends to TripEngine
```

---

## 8. Background Service Architecture

### 8.1 TripMonitoringService (Foreground Service)

```kotlin
// data/alerts — started/stopped by use cases, not bound
class TripMonitoringService : Service() {

    // Notification shown during monitoring:
    // "NearWake active — Monitoring | Low Power"
    // Updated when mode changes (Low Power → Approaching)

    // Handles:
    //   GeofenceBroadcastReceiver triggers
    //   ActivityTransitionReceiver triggers
    //   LocationCallback updates (when FLP is active)

    // On destroy: calls TripCleanupUseCase
}
```

### 8.2 WorkManager Recovery

```kotlin
// Runs on app launch, checks for orphaned active TripSession
class TripRecoveryWorker : CoroutineWorker() {
    // 1. Query Room for TripSession with Active state
    // 2. If found and trip is not expired → restart TripMonitoringService
    // 3. Log recovery event to DiagnosticsLogger
}
```

### 8.3 Alert System

```kotlin
class AlertOrchestrator {
    // On FIRE_ALERT:
    //   1. Show full-screen intent (lock screen compatible)
    //   2. Play sound at configured AlertIntensity
    //   3. Vibrate (pattern based on intensity)
    //   4. Schedule repeat notification every 30s until dismissed
    //   5. Log AlertEvent to Room

    // On DISMISS:
    //   1. Cancel all repeat alarms
    //   2. Stop vibration
    //   3. Transition TripEngine → Completed
    //   4. Update AlertEvent.dismissedAt
}
```

---

## 9. Permissions Flow

```
App first launch
└─ Onboarding screens (value prop)
   └─ Request: POST_NOTIFICATIONS
      └─ Home screen unlocks

First "Start Trip" tap
└─ Check: ACCESS_FINE_LOCATION granted?
   ├─ No → Permission rationale screen
   │         ← Explain: "We need location to detect when you're near your stop"
   │         ← Button: "Grant Location"
   │         ← Button: "Use limited mode" (foreground only)
   └─ Yes → Trip Setup screen

"Start Trip" with background monitoring option
└─ Check: ACCESS_BACKGROUND_LOCATION granted?
   ├─ No → Explain: "Background location lets the app monitor while your screen is off"
   │         → Open Settings deep link
   └─ Yes → Proceed

Alongside foreground location
└─ Request: ACTIVITY_RECOGNITION
   └─ Explain: "Activity detection lets us start monitoring only when you're moving"
```

**Decline-safe behaviour:**
- Background location denied → run in foreground-only mode, show "Limited mode" badge
- Activity recognition denied → skip activity detection tier, start balanced FLP immediately on arm
- No crash, no blocked UI — always gracefully degrade

---

## 10. AndroidManifest Permissions

```xml
<!-- Notifications -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

<!-- Location -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

<!-- Activity recognition -->
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />

<!-- Foreground service -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

<!-- Alarms -->
<uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />

<!-- Boot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- Internet (for Places API + future backend) -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 11. Screen Map & UI

### 11.1 Navigation Graph

```
Onboarding ──────────────────────────────────────────── Home
                                                          │
                     ┌────────────────────────────────────┤
                     │                                    │
                  PlaceSearch                         Settings
                     │                                    │
                 TripSetup                           Diagnostics (QA)
                     │
                  LiveTrip ───────────────── AlertScreen ──── TripSummary
                     │                                             │
                     └──────────────────── RecoveryScreen ────────┘
                                                         
History ──── TripDetail
PermissionsHelp (bottom sheet, accessible from any screen)
```

### 11.2 Screen Designs

**Home Screen**
```
┌─────────────────────────────────────┐
│  NearWake                    ⚙  📋  │
│                                     │
│  ┌──────────────────────────────┐   │
│  │  + Set destination           │   │  ← Primary CTA (large tap target)
│  └──────────────────────────────┘   │
│                                     │
│  Recent places:                     │
│  📍 Central Station                 │
│  📍 Airport Terminal 2              │
│                                     │
│  [Last trip: Completed 2h ago]      │
└─────────────────────────────────────┘
```

**TripSetup Screen**
```
┌─────────────────────────────────────┐
│  ← Trip Setup                       │
│                                     │
│  To: Central Station          [✎]   │
│  ETA: ~35 min                       │
│                                     │
│  Alert me:                          │
│  ○ 5 min before arrival             │
│  ● 10 min before arrival  (default) │
│  ○ 15 min before arrival            │
│  ○ When I'm nearby                  │
│                                     │
│  Alert intensity:                   │
│  [Gentle]  [Standard ✓]  [Loud]     │
│                                     │
│  Background monitoring: [ON]        │
│                                     │
│  ┌──────────────────────────────┐   │
│  │      Start Trip              │   │  ← Large primary button
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
```

**LiveTrip Screen**
```
┌─────────────────────────────────────┐
│  ✕ Cancel Trip           ⏱ 00:12:33 │
│                                     │
│      To: Central Station            │
│                                     │
│  ┌──────────────────────────────┐   │
│  │                              │   │
│  │       ETA: ~22 min           │   │
│  │                              │   │
│  └──────────────────────────────┘   │
│                                     │
│  Status: ● Monitoring · Low Power   │
│  Battery impact: Very Low           │
│  Confidence: Normal                 │
│                                     │
│  Alert: 10 min before · Standard   │
└─────────────────────────────────────┘
```
(Status updates to "● Approaching" when in MonitoringApproach)

**Alert Screen (Full-screen, lock-screen compatible)**
```
┌─────────────────────────────────────┐
│                                     │
│            🔔🔔🔔                   │
│                                     │
│     APPROACHING YOUR STOP           │
│                                     │
│     Central Station                 │
│     ~8 min away                     │
│                                     │
│  ┌──────────────────────────────┐   │
│  │         DISMISS              │   │  ← Big button, accessible when asleep
│  └──────────────────────────────┘   │
│                                     │
│  [Snooze 2 min]    [End Trip]       │
└─────────────────────────────────────┘
```

**Recovery Screen**
```
┌─────────────────────────────────────┐
│  ⚠ You may have missed your stop    │
│                                     │
│  Central Station was 2 min ago      │
│                                     │
│  ┌──────────────────────────────┐   │
│  │      End Trip                │   │
│  └──────────────────────────────┘   │
│                                     │
│  [Open Maps for directions]         │
└─────────────────────────────────────┘
```

---

## 12. Room Database Schema

```
NearWakeDatabase (v1)
├── trips                    ← Trip entity
│     id, destination_id, alert_lead_min, alert_intensity,
│     created_at, completed_at
├── trip_sessions            ← Active/recent TripSession (one per trip)
│     trip_id (FK), state, monitoring_mode, last_known_lat,
│     last_known_lng, last_eta_minutes, confidence,
│     geofence_ids_json, updated_at
├── saved_places             ← User's saved destinations
│     id, name, address, lat, lng, place_id, last_used_at
├── alert_events             ← One per alert fired
│     id, trip_id (FK), fired_at, dismissed_at, type
└── diagnostics_events       ← Local event log for QA
      id, trip_id, event_type, payload_json, recorded_at
```

---

## 13. DataStore — UserPreferences

```kotlin
data class UserPreferences(
    val defaultAlertLeadMinutes: Int = 10,
    val defaultAlertIntensity: AlertIntensity = AlertIntensity.STANDARD,
    val backgroundMonitoringEnabled: Boolean = true,
    val onboardingCompleted: Boolean = false,
    val diagnosticsEnabled: Boolean = false,
    val notificationChannelVersion: Int = 1
)
```

---

## 14. Notification Channels

```kotlin
const val CHANNEL_MONITORING = "nearwake_monitoring"   // Low priority, persistent
const val CHANNEL_ALERT = "nearwake_alert"             // High priority, full-screen intent
const val CHANNEL_RECOVERY = "nearwake_recovery"       // High priority

// MONITORING notification: shown during active trip in foreground service
// ALERT notification: fires when approaching destination (full-screen + lock screen)
// RECOVERY notification: fires if user may have overshot
```

---

## 15. Observability & Diagnostics

Every important engine event is logged locally to the `diagnostics_events` table:

| Event | Logged Data |
|---|---|
| trip_started | tripId, destination, alertLead |
| monitoring_mode_changed | from, to, reason |
| geofence_registered | id, type, radius |
| geofence_fired | id, type, dwell_time |
| motion_transition | from, to, confidence |
| precise_burst_started | reason |
| precise_burst_stopped | duration_ms, reason |
| alert_fired | tripId, type, confidence |
| alert_dismissed | tripId, delay_ms |
| overshoot_detected | tripId, distance_past |
| recovery_started | tripId |
| trip_completed | tripId, duration_min |
| trip_cancelled | tripId, state_at_cancel |
| battery_summary | active_gps_ms, total_ms |
| permission_denied | permission_name |
| process_death_recovered | tripId, state_restored |

**Diagnostics Screen** (feature:diagnostics, dev/QA only):
- Real-time stream of log events
- Current TripSession state
- Registered geofences list
- Monitoring mode indicator
- Simulated geofence fire button (debug builds only)

---

## 16. Phase-by-Phase Development Plan

### Phase 1 — Project Scaffold ✅ PARTIALLY DONE
Status: Root Gradle files, build-logic plugins, version catalog, module build.gradle.kts files created.

Remaining work:
- [ ] Add `local.properties.template` with API key placeholders
- [ ] Create blank `src/main/kotlin/.../` directories with placeholder `AndroidManifest.xml` for each module
- [ ] Create `app/src/main/AndroidManifest.xml` with full permissions

**Estimated time:** 1–2 hours to complete

---

### Phase 2 — Domain + Trip Engine
Files to create:

```
domain/trip/src/main/kotlin/com/nearwake/domain/trip/
├── model/Trip.kt
├── model/TripSession.kt
├── model/TripState.kt
├── model/TripRule.kt
├── model/AlertDecision.kt
├── model/AlertEvent.kt
├── model/MonitoringMode.kt
├── model/Confidence.kt
├── model/AlertIntensity.kt
├── engine/TripStateMachine.kt      ← THE HEART — implement this first
├── engine/TripEngine.kt
├── engine/ApproachEvaluator.kt
├── engine/OvershootDetector.kt
├── engine/AlertDecisionEngine.kt
└── usecase/
    ├── ArmTripUseCase.kt
    ├── CancelTripUseCase.kt
    ├── CompleteTripUseCase.kt
    ├── ResumeTripUseCase.kt
    ├── EvaluateApproachUseCase.kt
    ├── FireArrivalAlertUseCase.kt
    ├── DetectOvershootUseCase.kt
    ├── EnterRecoveryModeUseCase.kt
    └── ComputeBatterySummaryUseCase.kt

domain/location/src/main/kotlin/com/nearwake/domain/location/
├── model/SavedPlace.kt
├── model/LatLng.kt
├── model/GeofenceSpec.kt
├── model/MotionState.kt
├── repository/LocationRepository.kt       ← interface
├── repository/GeofenceRepository.kt       ← interface
└── repository/MotionRepository.kt         ← interface
```

**Start with:** `TripStateMachine.kt` — all other components depend on it being correct.

**Estimated time:** 4–6 hours

---

### Phase 3 — Core Database (Room)

```
core/database/src/main/kotlin/com/nearwake/core/database/
├── NearWakeDatabase.kt                    ← Room DB class, version 1
├── entity/
│   ├── TripEntity.kt
│   ├── TripSessionEntity.kt
│   ├── SavedPlaceEntity.kt
│   ├── AlertEventEntity.kt
│   └── DiagnosticsEventEntity.kt
├── dao/
│   ├── TripDao.kt
│   ├── TripSessionDao.kt
│   ├── SavedPlaceDao.kt
│   ├── AlertEventDao.kt
│   └── DiagnosticsEventDao.kt
├── converter/
│   ├── TripStateConverter.kt
│   ├── InstantConverter.kt
│   └── ListConverter.kt
└── di/
    └── DatabaseModule.kt
```

**Estimated time:** 2–3 hours

---

### Phase 4 — DataStore

```
core/datastore/src/main/kotlin/com/nearwake/core/datastore/
├── UserPreferencesDataStore.kt
├── model/UserPreferences.kt
└── di/DataStoreModule.kt
```

**Estimated time:** 1 hour

---

### Phase 5 — Location Data Layer

```
data/location/src/main/kotlin/com/nearwake/data/location/
├── FusedLocationDataSource.kt
│   └── Wraps FLP. Provides:
│       - getLastKnownLocation(): LatLng?
│       - startBalancedUpdates(interval: Long): Flow<LatLng>
│       - startPreciseBurst(maxDurationMs: Long): Flow<LatLng>
│       - stopUpdates()
│
├── GeofenceDataSource.kt
│   └── Wraps GeofencingClient. Provides:
│       - registerGeofences(specs: List<GeofenceSpec>): Result<Unit>
│       - removeGeofences(ids: List<String>): Result<Unit>
│       - removeAllGeofences(): Result<Unit>
│
├── LocationStrategyOrchestrator.kt
│   └── Implements the 5-tier priority logic
│       - escalate(to: MonitoringMode)
│       - deescalate()
│       - stop()
│
├── LocationRepositoryImpl.kt             ← implements domain:location:LocationRepository
│
├── di/LocationModule.kt
│
└── receivers/
    ├── GeofenceBroadcastReceiver.kt      ← receives from GeofencingClient
    └── BootReceiver.kt                   ← restarts service after reboot

data/motion/src/main/kotlin/com/nearwake/data/motion/
├── ActivityRecognitionDataSource.kt
├── MotionRepositoryImpl.kt
├── di/MotionModule.kt
└── receivers/
    └── ActivityTransitionReceiver.kt
```

**Key implementation notes:**
- `FusedLocationDataSource` must call `removeLocationUpdates()` in every exit path (timeout, cancellation, decision)
- `GeofenceBroadcastReceiver` creates a PendingIntent with `FLAG_UPDATE_CURRENT` and `FLAG_MUTABLE` (API 31+)
- Handle `GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE` — fall back to balanced FLP

**Estimated time:** 4–5 hours

---

### Phase 6 — Background Service + Alert System

```
data/alerts/src/main/kotlin/com/nearwake/data/alerts/
├── TripMonitoringService.kt              ← ForegroundService
│   - startForeground() with MONITORING notification
│   - coordinates LocationStrategyOrchestrator + TripEngine
│   - handles geofence/motion/location events
│   - calls TripCleanupUseCase on destroy
│
├── AlertOrchestrator.kt
│   - fires full-screen intent notification
│   - plays sound (AudioManager + MediaPlayer)
│   - schedules repeating AlarmManager alarm (every 30s)
│   - handles dismiss → cancels alarms, transitions engine
│
├── NotificationHelper.kt
│   - creates all notification channels
│   - builds monitoring notification
│   - builds alert notification (full-screen intent)
│
├── TripCleanupUseCase.kt
│   - removes all geofences
│   - stops FLP
│   - unregisters activity transitions
│   - stops foreground service
│
├── worker/
│   └── TripRecoveryWorker.kt             ← WorkManager, runs on app launch
│
└── di/
    ├── AlertsModule.kt
    └── WorkerModule.kt
```

**Critical details:**
- `TripMonitoringService` declared in AndroidManifest with `foregroundServiceType="location"`
- Full-screen intent requires `USE_FULL_SCREEN_INTENT` permission + notification channel with HIGH importance
- On Android 14+: must declare `foregroundServiceType="location"` for background location access
- Recovery: after 5 min of unanswered alert, do 1 short precise burst → if > 500m past destination → Recovery state

**Estimated time:** 4–5 hours

---

### Phase 7 — Design System + UI

**Design System (`core/designsystem`):**
```kotlin
// Colors — dark-first (user may be in dark environment or tunnel)
object NearWakeColors {
    val BackgroundDark = Color(0xFF0D0D0D)
    val SurfaceDark = Color(0xFF1A1A1A)
    val Primary = Color(0xFF4CAF50)         // Green = safe, monitoring
    val OnApproach = Color(0xFFFFC107)      // Amber = approaching
    val Alert = Color(0xFFF44336)           // Red = alert firing
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB3B3B3)
}

// Typography — large, readable (user may be groggy)
// Primary CTA buttons: 56dp height minimum (accessible while tired)
```

**Screen files:**

```
feature/onboarding/   → OnboardingScreen.kt, OnboardingViewModel.kt
feature/permissions/  → PermissionsScreen.kt, PermissionsViewModel.kt
                        PermissionsHelpSheet.kt
feature/places/       → PlaceSearchScreen.kt, PlaceSearchViewModel.kt
                        SavedPlacesScreen.kt
feature/tripsetup/    → TripSetupScreen.kt, TripSetupViewModel.kt
feature/livetrip/     → LiveTripScreen.kt, LiveTripViewModel.kt
                        MonitoringStatusCard.kt
                        BatteryStatusCard.kt
feature/alerts/       → AlertScreen.kt, AlertViewModel.kt
                        RecoveryScreen.kt, RecoveryViewModel.kt
feature/history/      → HistoryScreen.kt, HistoryViewModel.kt
                        TripSummaryScreen.kt
feature/settings/     → SettingsScreen.kt, SettingsViewModel.kt
feature/diagnostics/  → DiagnosticsScreen.kt, DiagnosticsViewModel.kt
```

**Estimated time:** 8–12 hours (largest phase)

---

### Phase 8 — Route-Aware Stub (Architecture Ready)

```
domain/routing/src/main/kotlin/com/nearwake/domain/routing/
├── RoutingDataSource.kt        ← interface (already defined in design)
├── model/RouteSnapshot.kt
├── model/Stop.kt
├── model/TransferPoint.kt
└── repository/RoutingRepository.kt   ← interface

data/routing/src/main/kotlin/com/nearwake/data/routing/
├── NoOpRoutingDataSource.kt    ← MVP: returns null route, destination-only mode
├── GoogleTransitDataSource.kt  ← STUB: TODO markers for Phase 2 implementation
│                                  Uses Google Directions API with transit mode
├── LocalRouteCache.kt          ← Room-backed cache of RouteSnapshot
└── di/RoutingModule.kt
```

Google Directions API call (Phase 2 implementation):
```
GET https://maps.googleapis.com/maps/api/directions/json
  ?origin={lat},{lng}
  &destination={lat},{lng}
  &mode=transit
  &key={API_KEY}
```

**Estimated time:** 2 hours (stub only)

---

### Phase 9 — Tests

**Priority order (highest value first):**

1. **TripStateMachineTest** — 100% coverage of state transitions
   - Test every trigger from every state
   - Test process death recovery sequence
   - Test offline bias calculation

2. **ApproachEvaluatorTest** — escalation logic
   - Test ETAthreshold with various lead times
   - Test offline bias (20% earlier)
   - Test geofence trigger paths

3. **LocationStrategyOrchestratorTest** — tier escalation
   - Test escalate/de-escalate sequences
   - Test cleanup on cancel

4. **AlertDecisionEngineTest** — fire/hold/recover
   - Test overshoot detection
   - Test confidence levels

5. **TripRepositoryTest** — DB persistence
   - Test session save/restore after process death
   - Test session cleanup after completion

6. **Fake Providers:**
   ```
   core/testing/src/main/kotlin/com/nearwake/core/testing/
   ├── FakeLocationSource.kt         ← emits controllable LatLng updates
   ├── FakeGeofenceClient.kt         ← manually trigger geofence events
   ├── FakeActivityRecognition.kt    ← manually emit motion transitions
   ├── FakeRoutingDataSource.kt      ← returns preset RouteSnapshot
   └── TripEngineTestFixtures.kt     ← common test trip objects
   ```

7. **Critical scenario tests:**
   - User sleeps with screen off → geofence fires → alert rings (no FLP needed)
   - App killed mid-trip → relaunch → session restored → monitoring resumes
   - Background location denied → limited mode → foreground-only monitoring works
   - Weak network mid-trip → confidence drops to OFFLINE → alert biases earlier
   - User overshoots → recovery → recovery screen shown
   - Transfer alert boundary (Phase 2)

**Estimated time:** 6–8 hours

---

### Phase 10 — Polish, README, Launch Checklist

**README contents:**
- What NearWake is (2 sentences)
- Architecture diagram (module graph)
- Setup: clone → add API key → build
- Google Cloud setup steps
- Running tests
- Building release APK
- Emulator configuration for testing (fake GPS route)
- Play Store permission policy notes

**Launch checklist:**
- [ ] `local.properties` template committed (no actual keys)
- [ ] Proguard rules for Retrofit, Hilt, Room, Serialization
- [ ] Network security config (HTTPS only)
- [ ] App signing config documented
- [ ] Privacy policy URL in-app
- [ ] Play Store listing: permission rationale in description
- [ ] Target SDK 35 (Android 15)
- [ ] Test on: Pixel 6 (API 33), Samsung Galaxy (API 34), API 26 minimum
- [ ] Battery optimization whitelist prompt for OEM devices (Samsung, Xiaomi, Huawei)
- [ ] Sentry DSN configured

**Play Store Permission Notes:**
- `ACCESS_BACKGROUND_LOCATION` requires a dedicated declaration in the Play Console + policy attestation
- `FOREGROUND_SERVICE_LOCATION` must match the `foregroundServiceType` in the manifest
- `USE_FULL_SCREEN_INTENT` is restricted on Android 14+ — requires Play approval for non-alarm/communication apps. NearWake qualifies as "alarm" use case.
- Activity Recognition requires purpose disclosure in the app store listing

---

## 17. Future Improvements (Post-MVP)

### Route-Aware Mode (Phase 2)
- Integrate Google Directions API with transit mode
- Show stop sequence in LiveTrip screen
- Alert before transfer ("Get ready to transfer in 3 min")
- Alert 1/2 stops before destination

### Backend (Phase 3)
- Node.js + Hono API
- PostgreSQL for trip history sync
- Auth (anonymous device ID → optional Google sign-in)
- Remote config (adjust approach thresholds, disable features)
- Analytics ingestion (trip success rate, alert effectiveness)
- Support/debug endpoint

### Product Improvements
- Repeat/favorite trips ("My commute")
- Widget for quick trip start
- Wear OS companion
- Custom alert sounds
- Google Maps integration deep link for navigation during recovery
- ETA source: Google Directions real-time (replaces rough distance estimate)
- Disruption alerts (when route is affected by delays)

---

## 18. Environment Setup

### Prerequisites
- Android Studio Hedgehog or later
- JDK 17+
- Android SDK 35
- Google Cloud project with:
  - Maps SDK for Android enabled
  - Places API enabled
  - (Phase 2) Directions API enabled

### Setup Steps
```bash
# 1. Clone repo
git clone <repo-url>
cd LocationTracker

# 2. Create local.properties
cp local.properties.template local.properties

# 3. Add your API key to local.properties
echo "MAPS_API_KEY=your_key_here" >> local.properties

# 4. Build
./gradlew assembleDebug

# 5. Run tests
./gradlew test

# 6. Install on device
./gradlew installDebug
```

### local.properties.template (to commit)
```
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=REPLACE_WITH_YOUR_KEY
```

---

## 19. File Creation Priority Order

When resuming development, create files in this order:

1. `app/src/main/AndroidManifest.xml` — permissions + service declarations
2. `domain/trip/model/*.kt` — all domain models (no dependencies)
3. `domain/location/model/*.kt` — location models
4. `domain/trip/engine/TripStateMachine.kt` — **critical path**
5. `domain/trip/engine/TripEngine.kt`
6. `domain/trip/usecase/*.kt` — use cases
7. `core/database/entity/*.kt` — Room entities
8. `core/database/dao/*.kt` — DAOs
9. `core/database/NearWakeDatabase.kt`
10. `core/datastore/*.kt`
11. `data/location/*.kt` — FLP + geofencing
12. `data/motion/*.kt` — activity recognition
13. `data/alerts/TripMonitoringService.kt`
14. `data/alerts/AlertOrchestrator.kt`
15. `core/designsystem/*.kt` — theme
16. `app/src/main/kotlin/com/nearwake/app/NearWakeApplication.kt`
17. `app/src/main/kotlin/com/nearwake/app/MainActivity.kt`
18. `app/src/main/kotlin/com/nearwake/app/navigation/NavGraph.kt`
19. Feature screens (onboarding → permissions → home → tripsetup → livetrip → alerts)
20. Tests

---

## 20. Scalability Architecture

### 20.1 Module Scalability — Adding New Features

The multi-module structure is designed so new features are **zero-impact additions**. Each new feature:
- Gets its own `:feature:xxx` module
- Depends only on `:core:*` and `:domain:*` (never on other features)
- Registers its screen in the shared `NavGraph.kt` in `:app`
- Is completely removable without touching other modules

**Examples of scalable additions:**
```
:feature:widgets           → Glance widget for quick trip start
:feature:wearos            → Wear OS companion screen
:feature:commutemode       → Recurring/saved commute routes
:feature:disruptions       → Transit disruption alerts
:feature:sharing           → Share ETA with a contact (not tracking)
```

### 20.2 Data Source Scalability

The `RoutingDataSource` interface is the extension point for all route/transit providers:

```kotlin
// domain:routing — interface defined once, multiple implementations
interface RoutingDataSource {
    suspend fun fetchRoute(origin: LatLng, destination: LatLng): Result<RouteSnapshot>
    suspend fun refreshEta(tripId: String, currentLocation: LatLng): Result<Int>
    val priority: Int      // higher = preferred when multiple sources are available
    val name: String
}

// Current implementations (data:routing):
// NoOpRoutingDataSource         → MVP, destination-only
// GoogleTransitDataSource       → Phase 2, Google Directions API
// GtfsRoutingDataSource         → Phase 3, local GTFS feed support
// RealTimeTransitDataSource     → Phase 4, agency GTFS-RT feeds

// RoutingRepository picks the highest-priority available source
class RoutingRepository @Inject constructor(
    private val sources: Set<@JvmSuppressWildcards RoutingDataSource>
) {
    fun getBestSource() = sources.maxByOrNull { it.priority }
}
```

### 20.3 Alert Source Scalability

```kotlin
// Alert channels are pluggable — easy to add:
// - SMS alert to emergency contact
// - Smartwatch vibration
// - External BLE device
// - Google Home announcement
interface AlertChannel {
    suspend fun fire(event: AlertDecision): Result<Unit>
    suspend fun dismiss(tripId: String)
}

// data:alerts registers channels via Hilt multibindings:
@Module @InstallIn(SingletonComponent::class)
object AlertChannelModule {
    @IntoSet @Provides fun notificationChannel(...): AlertChannel = NotificationAlertChannel(...)
    @IntoSet @Provides fun vibratorChannel(...): AlertChannel = VibratorAlertChannel(...)
    // Add new channels here without touching AlertOrchestrator
}
```

### 20.4 Backend Scalability (When Added)

The offline-first design means the backend is purely additive:

```
MVP (now): 100% on-device
│
Phase 3 — Sync tier:
│   ├── POST /trips/sync      ← upload completed trip summaries
│   ├── GET  /config          ← remote feature flags + threshold config
│   └── POST /events/batch    ← analytics batch upload
│
Phase 4 — Auth tier:
│   ├── POST /auth/anonymous  ← device-based anonymous ID
│   ├── POST /auth/google     ← optional Google sign-in for sync
│   └── GET  /places/saved    ← cross-device saved places
│
Phase 5 — Transit data tier:
│   ├── GET  /routes/search   ← cached GTFS stop data by region
│   ├── GET  /disruptions     ← real-time alerts for active routes
│   └── GET  /eta/{tripId}    ← server-side ETA from transit APIs
│
Backend stack (when added):
├── Node.js + Hono (lightweight, fast startup)
├── PostgreSQL (trip history, user data)
├── Redis (ETA cache, session state)
├── Kafka/SQS (analytics event queue — optional at scale)
└── Deployed on: Railway / Fly.io / GCP Cloud Run
```

**The Android app always falls back to on-device mode** if the backend is unreachable. The `RoutingRepository` and `AlertOrchestrator` check connectivity before any network call and degrade gracefully.

### 20.5 Multi-Region / Multi-Provider Scalability

```
Phase 2: Google Transit (works globally, one API key)
Phase 3: City-level GTFS (NYC MTA, London TfL, etc.)
Phase 4: Real-time GTFS-RT (vehicle positions, live delays)
Phase 5: Aggregator tier (combine multiple sources, pick best)

// Location sources are equally pluggable:
// Current: FusedLocationProvider (Android standard)
// Future:  Could add HERE SDK, Mapbox Location Engine
//          without changing TripEngine or UI
```

### 20.6 Scalable Project Structure (Full View)

```
LocationTracker/
├── build-logic/                     ← Convention plugins (add new ones here)
│   └── convention/src/main/kotlin/
│       ├── AndroidApplicationConventionPlugin.kt
│       ├── AndroidLibraryConventionPlugin.kt
│       ├── AndroidLibraryComposeConventionPlugin.kt
│       ├── AndroidHiltConventionPlugin.kt
│       ├── AndroidFeatureConventionPlugin.kt   ← apply to every :feature:* module
│       └── KotlinLibraryConventionPlugin.kt
│
├── gradle/
│   └── libs.versions.toml           ← Single source of truth for all versions
│
├── app/                             ← Thin shell: NavGraph + Hilt + Application
│
├── core/                            ← Shared infrastructure, no business logic
│   ├── common/                      → Extensions, result wrappers, dispatchers
│   ├── database/                    → Room (add new entities/DAOs here)
│   ├── datastore/                   → UserPreferences (add new prefs here)
│   ├── designsystem/                → Theme, typography, shared components
│   ├── network/                     → HTTP client config (add interceptors here)
│   ├── testing/                     → Fakes + fixtures (add new fakes per domain)
│   └── ui/                          → Shared Composable building blocks
│
├── domain/                          ← Pure Kotlin, no Android, fully testable
│   ├── trip/                        → TripEngine, use cases (core business logic)
│   ├── location/                    → Location/Geofence interfaces
│   └── routing/                     → RoutingDataSource interface, route models
│
├── data/                            ← Android implementations of domain interfaces
│   ├── location/                    → FLP + Geofencing
│   ├── motion/                      → Activity recognition
│   ├── routing/                     → Transit API adapters (add new ones here)
│   ├── alerts/                      → Service + alert channels (add new channels here)
│   └── analytics/                   → Diagnostics + future remote sync
│
└── feature/                         ← One module per screen group (add new modules here)
    ├── onboarding/
    ├── permissions/
    ├── places/
    ├── tripsetup/
    ├── livetrip/
    ├── alerts/
    ├── history/
    ├── settings/
    └── diagnostics/
    # ↓ Future features (add here — zero impact on existing modules)
    # ├── widgets/
    # ├── wearos/
    # ├── commutemode/
    # └── disruptions/
```

### 20.7 Dependency Graph (Rules That Keep It Scalable)

```
Allowed dependencies (→ means "can depend on"):

:feature:*      → :core:*, :domain:*, (never :data:*, never other :feature:*)
:data:*         → :core:*, :domain:*, :data:analytics (never :feature:*)
:domain:*       → :core:common only (pure Kotlin, Android-free)
:core:database  → :domain:* (for Room entities mirroring domain models)
:core:*         → :core:common (no circular deps)
:app            → everything (assembly only, no business logic)

This means:
- Adding a new :feature:x → zero risk to existing features
- Adding a new :data:y    → zero risk to features or other data modules
- Changing :domain:trip   → only :feature:livetrip, :feature:tripsetup etc. need updating
- :domain:trip has no Android dependencies → runs in pure JVM tests (fast)
```

---

## 21. Estimated Total Development Time

| Phase | Hours |
|---|---|
| Scaffold (remaining) | 1–2 |
| Domain + engine | 4–6 |
| Database | 2–3 |
| DataStore | 1 |
| Location data layer | 4–5 |
| Background service + alerts | 4–5 |
| Design system + UI (7 screens) | 8–12 |
| Route-aware stub | 2 |
| Tests | 6–8 |
| README + polish | 2–3 |
| **Total** | **34–47 hours** |

This is approximately 1–2 focused development weeks for an experienced Android engineer.
