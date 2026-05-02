# NearWake LLM Project Context Prompt

Use this file when you want to hand the NearWake project to another LLM and give it enough context to reason well without re-discovering the whole repo from zero.

This is not the product plan itself.
It is a ready-to-copy context prompt plus a compact project reference.

---

## 1. Best Use

Use this file when you want another LLM to:

- understand what NearWake is
- understand the real problem it solves
- understand the current architecture and module boundaries
- know what is already built vs what is still planned
- know which APIs and external setup matter
- continue development without turning the app into a different product
- review the project with correct product and technical context

If you need deeper product strategy, also read `PLAN.md` and `PRODUCT_COMPARE_REFERENCE.md`.
If you need the machine/API setup checklist, also read `REQUIRED_UPDATES_AND_APIS.md`.

---

## 2. Copy-Paste Prompt For Another LLM

Copy everything in this block when starting a new thread with another LLM:

```text
You are helping on an Android project named NearWake.

NearWake is an Android-first arrival assurance app for bus and train commuters. The core promise is:
"Wake me before my stop. Tell me if you are not sure."

This is not a general navigation app, not a ticketing app, not a live-map planner, and not a social platform. It is a trust app focused on helping commuters avoid missing stops when they are asleep, distracted, reading, scrolling, or otherwise not watching the route continuously.

Product definition:
- NearWake is an offline-first arrival alarm and monitoring system
- it should stay calm when idle, become alive when monitoring, and be unmistakable when alerting
- it must be honest about uncertainty
- it should fail visibly, not silently
- it should be battery-conscious and Android-native

The main problem:
- users miss stops because they nap
- users miss stops because they are distracted
- most transit apps focus on route planning, not trustworthy arrival alerts
- existing single-threshold stop alarms are fragile

NearWake differentiators:
- confidence-first UX
- bias-early behavior when confidence drops
- staged alerts instead of single-threshold alerts
- Active mode vs Sleep mode
- underground / degraded-confidence dignity instead of silent failure
- recovery-first design after overshoot
- transfer-aware monitoring
- wrong-direction validation
- last-mile walk finish guidance
- lightweight arrival confirmation companion flow
- offline-first and no required account for core value

Non-negotiable product commitments:
- confidence must be shown to the user
- when uncertain, alert earlier and label why
- no ads or paywalls in the alert path
- no account required for core value
- privacy-respecting, local-first persistence
- battery-honest behavior
- OEM-specific reliability guidance where relevant
- one-tap re-arm for repeat commutes

Technical stack:
- Kotlin
- Android
- Jetpack Compose
- Material 3
- Hilt
- Room
- DataStore
- WorkManager
- foreground service for active monitoring
- Fused Location Provider
- geofencing
- Activity Recognition
- OkHttp / Retrofit
- modular monolith architecture

Architecture target:
- app = assembly only
- application = orchestration use cases
- domain = pure business rules
- ports = inner-layer interfaces
- data = concrete adapters and Android/runtime integrations
- feature = UI screens and ViewModels

Dependency direction:
- feature -> application -> domain + ports
- data -> ports + domain + core
- app -> feature + application + data + core
- domain should not depend on Android APIs
- feature modules should not own Room/service orchestration logic

Current module layout:
- :app
- :application:monitoring
- :application:trip
- :core:common
- :core:database
- :core:datastore
- :core:designsystem
- :core:network
- :core:testing
- :core:ui
- :domain:commute
- :domain:location
- :domain:routing
- :domain:trip
- :ports:monitoring
- :ports:persistence
- :data:alerts
- :data:analytics
- :data:location
- :data:motion
- :data:patterns
- :data:routing
- :feature:onboarding
- :feature:permissions
- :feature:places
- :feature:tripsetup
- :feature:livetrip
- :feature:alerts
- :feature:history
- :feature:settings
- :feature:diagnostics
- :feature:departure
- :feature:walkfinish
- :feature:companion

Current screen / route map:
- onboarding
- home
- permissions
- places
- trip_setup/{placeId}
- live_trip/{tripId}
- alert/{tripId}
- recovery/{tripId}
- walk_finish/{tripId}
- companion/{tripId}
- history
- departure
- trip_summary/{tripId}
- settings
- diagnostics

What is already built:
- multi-module architecture
- Compose navigation and working screen flow
- Room persistence and DAOs
- DataStore preferences
- Hilt dependency injection
- trip engine and state machine
- route preview and route snapshot cache
- Google transit provider integration
- Google Places-backed search with local fallback
- live trip flow backed by persisted trip/session data
- alert and recovery flows
- history and trip summary
- settings and diagnostics
- appearance/theme selection
- configurable early-alert triggers (Time / Distance / Both)
- departure reminder groundwork
- one-tap re-arm
- lock-screen / foreground monitoring status improvements
- transfer-aware monitoring
- boarding direction validation
- walk-finish flow
- arrival companion flow
- architecture hardening toward application/ports boundaries

What is not fully finished:
- broader end-to-end testing
- more real-device field validation
- final release hardening and store/policy work
- some later plan pillars may still be partial or planned rather than complete

External setup:
- local.properties needs sdk.dir
- local.properties can also contain MAPS_API_KEY
- current Google-backed features use:
  - Directions API
  - Places API
- Maps SDK for Android is only needed if the product uses an embedded Google map view

Current behavior without MAPS_API_KEY:
- app still builds and runs
- place search falls back locally
- destination-only behavior remains available

Current behavior with MAPS_API_KEY:
- Google Places-backed place search works
- Google transit route preview works
- route snapshots can be cached per trip

Important product constraints:
- do not accidentally redesign NearWake into a generic map app
- do not treat roadmap items as already shipped
- separate implemented behavior from planned behavior
- preserve offline-first, trust-first positioning
- preserve modular boundaries
- prefer Android-native reliability over cross-platform convenience
- preserve honest degraded behavior instead of faking certainty

When answering or implementing:
- be explicit about what is current vs planned
- favor source-backed reasoning over product-name assumptions
- keep the user experience calm, high-signal, and non-gimmicky
- preserve existing user changes
- do not remove product honesty features like confidence or recovery wording

Useful repo docs:
- README.md
- PLAN.md
- PRODUCT_COMPARE_REFERENCE.md
- SETUP_AND_STATUS.md
- REQUIRED_UPDATES_AND_APIS.md
- TARGET_PRODUCTION_ARCHITECTURE.md
- UI_MODERNIZATION_PLAN.md
- PROJECT_STUDY_GUIDE.md
- DEVELOPMENT_START.md

Useful verification commands:
- .\gradlew.bat :domain:trip:test
- .\gradlew.bat :core:network:test
- .\gradlew.bat :data:routing:test
- .\gradlew.bat :data:alerts:test
- .\gradlew.bat :app:assembleDebug
- .\gradlew.bat :app:assembleRelease

Approach this project as a production-minded Android modular monolith that solves arrival assurance, not generic navigation.
```

---

## 3. One-Page Human Summary

### Product

NearWake is an Android-first arrival assurance app for transit commuters.
The strongest user stories are:

- a commuter who sleeps on the ride
- a commuter who is awake but distracted
- a parent who wants arrival confirmation after a child reaches the stop

The center of the product is not route planning.
It is trust.

### Core promise

- warn me before I miss my stop
- keep monitoring quietly in the background
- tell me when you are less sure
- recover gracefully if I overshoot

### What NearWake is not

- not a full transit planner
- not a live vehicle map product
- not ticketing
- not a social network
- not a mandatory-account system
- not a safety/SOS platform

---

## 4. Architecture Summary

NearWake is moving toward a modular Android monolith with hexagonal boundaries.

### Layers

`app`
- app startup
- manifests
- top-level assembly
- navigation wiring

`application:*`
- workflow orchestration
- start trip
- re-arm trip
- start/stop monitoring
- preserve trip-level alert choices

`domain:*`
- pure business rules
- trip engine
- confidence model
- route logic
- recovery planning
- commute prediction logic

`ports:*`
- interfaces the inner layers depend on
- monitoring control
- persistence-oriented workflow abstractions

`data:*`
- Room-backed implementations
- Google routing and place search adapters
- location, motion, notifications, and Android runtime integrations

`feature:*`
- Compose screens
- ViewModels
- UI state
- navigation callbacks

### Dependency rules

Allowed:

- `feature -> application -> domain + ports`
- `data -> ports + domain + core`
- `app -> feature + application + data + core`

Avoid:

- `feature -> data`
- `feature -> DAOs/entities`
- `feature -> foreground service orchestration`
- `domain -> Android`

---

## 5. Current Module Inventory

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

:feature:alerts
:feature:companion
:feature:departure
:feature:diagnostics
:feature:history
:feature:livetrip
:feature:onboarding
:feature:permissions
:feature:places
:feature:settings
:feature:tripsetup
:feature:walkfinish
```

---

## 6. Current Screen Flow

The app route map currently includes:

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

This means the app already contains:

- onboarding and permission staging
- destination selection
- trip arming
- active monitoring
- staged alert/recovery surfaces
- final walk guidance
- optional arrival confirmation
- history and diagnostics

---

## 7. Current Implemented Capabilities

These are implemented in meaningful form:

- multi-module project structure
- Room persistence
- DataStore preferences
- Hilt DI
- Compose UI/navigation
- route-aware trip setup
- route snapshot cache
- live trip state from persisted trip/session data
- Google Directions-backed route fetch
- Google Places-backed place search with fallback
- configurable alert triggers:
  - `Time`
  - `Distance`
  - `Both`
- one-tap re-arm
- transfer-aware progress and alerts
- wrong-direction validation
- recovery planning
- walk-finish flow
- companion/arrival share flow
- theme selection and settings persistence
- diagnostics export support
- architecture hardening through application and port modules

---

## 8. Current Gaps

The project is substantial and buildable, but not completely finished.

Main remaining gaps:

- broader end-to-end testing
- more real-device field validation
- final release hardening
- store/privacy/disclosure/publication steps
- some later roadmap pillars may still be partial or planned

Another LLM should not claim the app is "fully finished" unless it has checked the current repo state and validated the remaining roadmap items.

---

## 9. External APIs And Setup

### Required machine setup

`local.properties`

```properties
sdk.dir=C:\\Users\\<user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

### Important Google APIs

Needed now:

- `Directions API`
- `Places API`

Optional later:

- `Maps SDK for Android`

### Current real usage

- transit routing currently goes through the legacy Google Directions endpoint
- place search currently uses Google Places SDK for Android

### Degraded behavior without key

- app still builds
- app still runs
- local fallback search remains available
- destination-only monitoring remains possible

---

## 10. Practical Guidance For Another LLM

If another LLM is asked to work on this repo, it should:

- inspect the real repo before making architectural claims
- separate shipped behavior from planned behavior
- preserve the offline-first trust-app identity
- avoid turning NearWake into a generic transit map app
- keep confidence, recovery, and bias-early behavior visible
- prefer Android-native reliability choices
- respect modular boundaries when adding features
- ground API/setup advice in the current code paths, not in generic Maps assumptions

Good default reference docs:

- [README.md](/d:/PROJECTS/Startup/LocationTracker/README.md)
- [PLAN.md](/d:/PROJECTS/Startup/LocationTracker/PLAN.md)
- [PRODUCT_COMPARE_REFERENCE.md](/d:/PROJECTS/Startup/LocationTracker/PRODUCT_COMPARE_REFERENCE.md)
- [SETUP_AND_STATUS.md](/d:/PROJECTS/Startup/LocationTracker/SETUP_AND_STATUS.md)
- [REQUIRED_UPDATES_AND_APIS.md](/d:/PROJECTS/Startup/LocationTracker/REQUIRED_UPDATES_AND_APIS.md)
- [TARGET_PRODUCTION_ARCHITECTURE.md](/d:/PROJECTS/Startup/LocationTracker/TARGET_PRODUCTION_ARCHITECTURE.md)
- [UI_MODERNIZATION_PLAN.md](/d:/PROJECTS/Startup/LocationTracker/UI_MODERNIZATION_PLAN.md)
- [PROJECT_STUDY_GUIDE.md](/d:/PROJECTS/Startup/LocationTracker/PROJECT_STUDY_GUIDE.md)

---

## 11. Quick Verification Commands

```powershell
.\gradlew.bat :domain:trip:test
.\gradlew.bat :core:network:test
.\gradlew.bat :data:routing:test
.\gradlew.bat :data:alerts:test
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

---

## 12. Short Final Note

If you only send one file to another LLM, send this one.

If the task is strategic, also send:

- `PLAN.md`
- `PRODUCT_COMPARE_REFERENCE.md`

If the task is technical/build-oriented, also send:

- `SETUP_AND_STATUS.md`
- `REQUIRED_UPDATES_AND_APIS.md`
- `TARGET_PRODUCTION_ARCHITECTURE.md`
