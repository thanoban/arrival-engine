# NearWake

NearWake is an Android-first arrival alarm app for buses, trains, and transfers. The app is built around an offline-first monitoring flow: pick a destination, arm a trip quickly, let the background engine monitor quietly, and surface an alert before the user misses the stop.

If you are studying the project from the beginning, read [PROJECT_STUDY_GUIDE.md](PROJECT_STUDY_GUIDE.md) alongside `PLAN.md`.
For the practical current-state checklist, env/API setup, and what is finished vs not finished, read [SETUP_AND_STATUS.md](SETUP_AND_STATUS.md).
For the exact external configuration checklist, read [REQUIRED_UPDATES_AND_APIS.md](REQUIRED_UPDATES_AND_APIS.md).
For product problem, current scope, features, and technology choices in a comparison-friendly format, read [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md).
For the dedicated frontend redesign plan, read [UI_MODERNIZATION_PLAN.md](UI_MODERNIZATION_PLAN.md).

## Current Status

- Multi-module clean architecture is in place across `app`, `core`, `domain`, `data`, and `feature` modules.
- The trip engine, Room database, DataStore, and background monitoring stack are implemented and wired together.
- The app persists selected places, trips, route snapshots, and trip sessions through Room-backed flows.
- The Android toolchain is bootstrapped in-repo with the Gradle wrapper.
- `data:routing` now includes a working Google Transit provider, Room-backed route cache, and shared network wiring in `core:network`.
- Trip setup, live trip, monitoring service recovery, and trip summary screens now surface real persisted route/session data instead of only placeholder values.

## Docs

- [SETUP_AND_STATUS.md](SETUP_AND_STATUS.md): exact current setup, required env/API values, and project condition
- [REQUIRED_UPDATES_AND_APIS.md](REQUIRED_UPDATES_AND_APIS.md): exact API keys, local updates, and later release-time configuration items
- [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md): problem statement, scope, features, weaknesses, and technology choices for competitor comparison
- [UI_MODERNIZATION_PLAN.md](UI_MODERNIZATION_PLAN.md): dedicated plan for the upcoming modern UI/frontend pass
- [PROJECT_STUDY_GUIDE.md](PROJECT_STUDY_GUIDE.md): beginner-friendly architecture and Kotlin/Android explanation
- [corrections.md](corrections.md): correction log and resolved repo issues

## Recent Implemented Slices

- Google transit route fetching and ETA refresh support were added in `data:routing`.
- Route snapshots are cached per trip and shown in trip setup, live trip, and trip summary flows.
- `core:network` now provides shared `OkHttpClient`, shared `Json`, and a `Retrofit.Builder`.
- `TripMonitoringService` now restores and persists `TripSession` state through Room while it runs.
- History/detail UI now shows route summary, last ETA, and confidence information from persisted session data.

## Module Layout

```text
:app
:core:common
:core:database
:core:datastore
:core:designsystem
:core:network
:core:testing
:core:ui

:domain:trip
:domain:location
:domain:routing

:data:location
:data:motion
:data:routing
:data:alerts
:data:analytics

:feature:onboarding
:feature:permissions
:feature:places
:feature:tripsetup
:feature:livetrip
:feature:alerts
:feature:history
:feature:settings
:feature:diagnostics
```

## Prerequisites

- JDK 17
- Android SDK 35
- A valid `sdk.dir` entry in `local.properties`
- Optional but recommended: `MAPS_API_KEY` for current Google transit route preview support

## Setup

1. Copy the template:

   ```powershell
   Copy-Item local.properties.template local.properties
   ```

2. Update `local.properties`:

   ```properties
   sdk.dir=C:\\path\\to\\Android\\Sdk
   MAPS_API_KEY=REPLACE_WITH_YOUR_KEY
   ```

3. Verify the pure Kotlin/domain layer:

   ```powershell
   .\gradlew.bat :domain:trip:test
   ```

4. Verify routing and networking slices:

   ```powershell
   .\gradlew.bat :core:network:test
   .\gradlew.bat :data:routing:test
   .\gradlew.bat :data:alerts:test
   ```

5. Verify the Android app build:

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

6. Verify the release build path:

   ```powershell
   .\gradlew.bat :app:assembleRelease
   ```

## Verified Commands

- `.\gradlew.bat :domain:trip:test`
- `.\gradlew.bat :core:network:test`
- `.\gradlew.bat :data:routing:test`
- `.\gradlew.bat :data:alerts:test`
- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:assembleRelease`

## Notes

- `corrections.md` documents the build and consistency fixes that were applied during stabilization.
- If `MAPS_API_KEY` is present, trip setup can fetch a Google transit preview and cache it against the trip; if not, the app falls back gracefully to destination-only monitoring.
- The current UI is no longer just a shell: destination selection, trip setup, live trip, alert dismissal, diagnostics, settings, and history/summary screens all flow through persisted app data.
- `TripMonitoringService` now uses Room-backed `TripSession` restore/save behavior, which better matches the plan's recovery and process-death requirements.
- `SETUP_AND_STATUS.md` now contains the detailed answer for what you need to provide to build/run the app and what is still unfinished.
- `REQUIRED_UPDATES_AND_APIS.md` now contains the dedicated checklist of what you still need to update outside the codebase.
- `UI_MODERNIZATION_PLAN.md` now contains the screen-by-screen plan for the future visual redesign pass.
- Release builds now enforce HTTPS-only networking and include baseline shrinker rules for Room, Hilt, WorkManager, and Kotlin serialization.
