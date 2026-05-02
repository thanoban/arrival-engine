# NearWake Master Reference

This file is the single-file project reference for NearWake.

Use it when you want one markdown that explains the project from top to bottom without jumping across many separate docs.

It combines:

- what NearWake is
- the problem it solves
- product scope and commitments
- what is already built
- what is still unfinished
- architecture and modules
- current screen flow
- APIs and machine setup
- production constraints
- verification commands
- how another engineer or LLM should approach the repo

If you want the deeper originals, the source docs still exist.
This file is the consolidated version.

---

## 1. Project Identity

### Name

NearWake

### Product type

Android-first arrival assurance app for bus and train commuters.

### Core promise

> Wake me before my stop. Tell me if you are not sure.

### What kind of app this is

NearWake is a trust app.
It is built for commuters who:

- fall asleep during a ride
- stay awake but get distracted
- do not want to monitor every stop manually

It is designed to quietly monitor progress, alert before the user misses the stop, and recover gracefully if something goes wrong.

### What kind of app this is not

NearWake is not:

- a general navigation app
- a live-map transit planner
- a ticketing/payments app
- a social/community report app
- a mandatory-account product
- a cloud tracking platform
- an SOS or emergency safety platform

---

## 2. The Core Problem

The main problem is not route planning.
The main problem is not maps.

The real problem is:

- users miss stops because they sleep
- users miss stops because they get distracted
- most transit apps help with planning, but not with trustworthy arrival alerts
- most stop-alarm apps use fragile single-threshold alerts

NearWake exists to reduce that failure point.

---

## 3. Product Positioning

### The user value

NearWake should let a commuter:

- pick a destination quickly
- arm a trip with low friction
- stop thinking about the route for a while
- receive an alert early enough to act
- understand when the system is uncertain
- recover cleanly if they overshoot

### Product feeling

The app should feel:

- calm when idle
- focused while monitoring
- unmistakable when alerting
- honest when confidence drops

### Non-negotiable commitments

- confidence must be visible
- uncertainty should bias earlier alerts
- degraded behavior must be named, not hidden
- no ads or paywalls in the alert path
- no account required for core value
- local-first persistence
- battery-honest behavior
- OEM reliability guidance where needed
- one-tap re-arm for repeat commutes
- fail visible, not silent

---

## 4. Product Pillars

Every major feature should fit one of these product pillars:

1. Depart on time
2. Board the right vehicle
3. Ride safely whether awake or asleep
4. Handle transfers confidently
5. Arrive correctly
6. Recover gracefully after failure
7. Walk the last mile to the real destination
8. Confirm arrival when needed

These pillars are a better product lens than generic “transport app” thinking.

---

## 5. Main Differentiators

NearWake is meant to stand out through:

- confidence-first UX
- staged alerts instead of a single stop alarm
- Active mode vs Sleep mode
- bias-early behavior when confidence drops
- underground / degraded mode dignity
- recovery-first overshoot handling
- wrong-direction validation
- transfer-aware monitoring
- walk-finish guidance
- lightweight arrival confirmation companion flow
- offline-first approach

---

## 6. Alert Model

NearWake is built around staged alert behavior rather than a single threshold.

### Stage A: Approach

- early awareness
- user starts preparing

### Stage B: Imminent

- get-ready warning
- stronger signal than Stage A

### Stage C: Arrival

- exit-now signal
- the strongest main alert surface

### Stage D: Recovery

- triggered when the user appears to have missed the stop
- should provide a structured next step, not just failure

### Mode distinction

NearWake supports two alert personalities:

- `Active`
  - user is awake but distracted
- `Sleep`
  - user is sleeping or likely to miss low-intensity cues

The engine remains the same, but the alert behavior differs.

---

## 7. Confidence Model

Confidence is part of the product, not just hidden logic.

The user should be able to see whether the system is:

- `High`
- `Medium`
- `Low`

When confidence drops:

- the app should become earlier and more conservative
- the user should be told why behavior changed
- degraded states should be visible instead of pretending certainty

This is central to NearWake’s trust-based positioning.

---

## 8. Current Project Stage

NearWake is no longer a scaffold.
It is a buildable Android app with meaningful real implementation.

### Broad stage label

Strong MVP / late prototype moving toward production hardening.

### What that means

- the project structure is real
- the app UI exists and is functional
- core persistence is real
- route-aware behavior exists
- background monitoring exists
- architecture hardening is underway
- some product pillars are already implemented meaningfully
- remaining work is increasingly validation, hardening, and completion, not blank-slate setup

---

## 9. What Is Already Implemented

The repo already includes meaningful implementation for:

- multi-module Android architecture
- Gradle wrapper and build logic
- Jetpack Compose navigation and screens
- Room database, entities, DAOs, and persistence flows
- DataStore preferences
- Hilt dependency injection
- trip engine and state management
- background monitoring service
- application-layer monitoring orchestration
- application-layer trip-start orchestration
- route preview and route cache
- Google transit route integration
- Google Places-backed destination search with local fallback
- persisted trips, sessions, and history
- live trip screen backed by persisted session data
- alert and recovery flows
- trip summary and history detail
- settings and appearance mode persistence
- diagnostics and diagnostics export
- configurable early-alert trigger modes:
  - `Time`
  - `Distance`
  - `Both`
- one-tap re-arm
- lock-screen / foreground notification improvements
- transfer-aware trip progress surfaces
- boarding direction validation
- walk-finish flow
- arrival companion flow
- modernized UI system and app-wide visual pass

---

## 10. What Is Not Fully Finished

NearWake is not yet fully complete.

Main remaining gaps include:

- broader end-to-end testing
- more real-device field validation
- final release hardening
- final privacy / disclosure / store submission work
- possible remaining plan pillars that are only partial or still pending

Important rule:

Do not describe the entire product as fully finished unless the current repo state and remaining roadmap items have been checked directly.

---

## 11. Current Screen Flow

The app currently includes these top-level routes:

- `onboarding`
- `home`
- `permissions`
- `places`
- `trip_setup/{placeId}`
- `live_trip/{tripId}`
- `alert/{tripId}`
- `recovery/{tripId}`
- `walk_finish/{tripId}`
- `companion/{tripId}`
- `history`
- `departure`
- `trip_summary/{tripId}`
- `settings`
- `diagnostics`

### Practical meaning

This means the app already contains:

- onboarding
- permission staging
- destination selection
- trip arming
- active monitoring
- alert handling
- recovery handling
- last-mile guidance
- arrival confirmation flow
- history review
- settings and diagnostics

---

## 12. Architecture Direction

NearWake is moving toward a modular Android monolith with hexagonal boundaries.

### Why this shape

The hard part of this product is mostly device-side:

- location
- monitoring
- notifications
- background survival
- battery behavior
- process death recovery

A modular monolith is a better fit than microservices or a backend-heavy architecture for the current product.

---

## 13. Layer Responsibilities

### `:app`

Assembly only:

- app startup
- manifest
- top-level navigation
- dependency assembly

### `:application:*`

Workflow orchestration:

- start trip
- re-arm trip
- start/stop monitoring
- preserve trip-level alert choices

### `:domain:*`

Pure business logic:

- trip engine
- confidence rules
- route logic
- commute logic
- recovery planning

No Android APIs should live here.

### `:ports:*`

Interfaces depended on by inner layers:

- monitoring control
- persistence-oriented workflow interfaces

### `:data:*`

Concrete implementations:

- Room-backed persistence
- Google integrations
- Android runtime integrations
- location, motion, alerts, analytics, patterns

### `:feature:*`

UI modules only:

- screens
- ViewModels
- UI state
- UI-driven events

Features should not become the main owners of persistence or runtime orchestration.

---

## 14. Dependency Rules

### Allowed direction

- `feature -> application -> domain + ports`
- `data -> ports + domain + core`
- `app -> feature + application + data + core`

### Avoid

- `feature -> data`
- `feature -> Room DAOs/entities`
- `feature -> direct foreground service ownership`
- `domain -> Android`
- `domain -> Room`

---

## 15. Module Layout

```text
:app

:application:monitoring
:application:trip

:core:common
:core:database
:core:datastore
:core:designsystem
:core:network
:core:testing
:core:ui

:domain:commute
:domain:location
:domain:routing
:domain:trip

:ports:monitoring
:ports:persistence

:data:alerts
:data:analytics
:data:location
:data:motion
:data:patterns
:data:routing

:feature:onboarding
:feature:permissions
:feature:places
:feature:tripsetup
:feature:livetrip
:feature:alerts
:feature:history
:feature:settings
:feature:diagnostics
:feature:departure
:feature:walkfinish
:feature:companion
```

---

## 16. Technology Stack

### Language and platform

- Kotlin
- Android

### UI

- Jetpack Compose
- Material 3

### DI and app structure

- Hilt

### Persistence

- Room
- DataStore

### Background/runtime

- WorkManager
- foreground service
- Fused Location Provider
- geofencing
- Activity Recognition

### Networking

- OkHttp
- Retrofit
- shared network wiring in `:core:network`

### Project style

- modular monolith
- Android-native reliability first

---

## 17. Why Kotlin / Native Android Was The Right Choice

For NearWake, Kotlin is a stronger fit than a cross-platform-first stack because the product depends heavily on Android-native behaviors:

- foreground services
- geofences
- activity recognition
- lock-screen notifications
- device-specific battery restrictions
- background monitoring
- WorkManager
- Android permissions and settings guidance

The hardest problem here is monitoring reliability, not UI reuse.

---

## 18. External Setup

### Required local file

`local.properties`

Typical contents:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

### Required now

- `sdk.dir`

### Optional but strongly recommended now

- `MAPS_API_KEY`

---

## 19. Google APIs

### Needed now

- `Directions API`
- `Places API`

### Likely needed only if the product uses an embedded map UI

- `Maps SDK for Android`

### Important current code reality

- transit routing currently uses the legacy Google Directions endpoint
- place search currently uses Google Places SDK for Android

### Restriction direction

For Android app keys, use Android app restrictions with the correct package name and signing SHA values.

---

## 20. Behavior With And Without `MAPS_API_KEY`

### If `MAPS_API_KEY` is present

- Google Places-backed destination search can work
- Google transit route preview can work
- route snapshots can be cached per trip
- route-rich screens can show more detailed context

### If `MAPS_API_KEY` is missing

- the app still builds
- the app still runs
- local fallback search remains available
- destination-only monitoring remains possible

This key improves product richness, but it is not a full build blocker for the current repo.

---

## 21. Production Constraints

When continuing development, do not accidentally reshape the product into something else.

Important constraints:

- do not turn NearWake into a generic map-heavy transit app
- do not hide uncertainty behind fake precision
- do not let roadmap items read like already-shipped functionality
- preserve trust-first behavior
- preserve offline-first behavior
- preserve local-first persistence where possible
- avoid gimmicky UI that weakens the calm/high-signal character
- prefer Android-native reliability over portability-driven compromises

---

## 22. Guidance For Another Engineer Or LLM

If another engineer or LLM works on this repo, it should:

- inspect the actual repo before making claims
- separate current behavior from planned behavior
- preserve modular boundaries
- preserve the trust app identity
- preserve confidence and recovery surfaces
- avoid product drift toward generic navigation
- ground API/setup advice in the real code paths
- preserve honest degraded behavior instead of pretending certainty

Good default files to read alongside this one:

- `README.md`
- `PLAN.md`
- `PRODUCT_COMPARE_REFERENCE.md`
- `SETUP_AND_STATUS.md`
- `REQUIRED_UPDATES_AND_APIS.md`
- `SRI_LANKA_PRODUCTION_DATA_PLAN.md`
- `TARGET_PRODUCTION_ARCHITECTURE.md`
- `UI_MODERNIZATION_PLAN.md`
- `PROJECT_STUDY_GUIDE.md`
- `LLM_PROJECT_CONTEXT_PROMPT.md`

---

## 23. Verification Commands

Useful commands for the current repo:

```powershell
.\gradlew.bat :domain:trip:test
.\gradlew.bat :core:network:test
.\gradlew.bat :data:routing:test
.\gradlew.bat :data:alerts:test
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

---

## 24. Short Practical Summary

NearWake is an Android-native arrival assurance system for commuters.

It is already a substantial, buildable app with:

- real modular architecture
- real persistence
- real monitoring logic
- real route-aware features
- real UI flows

It is not yet fully finished, but it is well past the setup stage.

The key idea to protect during all future work is simple:

NearWake is not trying to be the best transit planner.
It is trying to be the most trustworthy app for not missing your stop.
