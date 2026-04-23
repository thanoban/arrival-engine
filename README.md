# NearWake

NearWake is an Android-first arrival alarm app for buses, trains, and transfers. The app is built around an offline-first monitoring flow: pick a destination, arm a trip quickly, let the background engine monitor quietly, and surface an alert before the user misses the stop.

## Current Status

- Multi-module clean architecture is in place across `app`, `core`, `domain`, `data`, and `feature` modules.
- The trip engine, Room database, DataStore, and background monitoring foundations are implemented.
- The app now persists selected places and trip sessions through Room-backed feature flows.
- The Android toolchain is bootstrapped in-repo with the Gradle wrapper.
- Routing remains intentionally stubbed for MVP destination-only mode.

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
- Optional: Google Maps / Places API key for future map and search integrations

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

4. Verify the Android app build:

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

5. Verify the release build path:

   ```powershell
   .\gradlew.bat :app:assembleRelease
   ```

## Verified Commands

- `.\gradlew.bat :domain:trip:test`
- `.\gradlew.bat :app:assembleDebug`
- `.\gradlew.bat :app:assembleRelease`

## Notes

- `corrections.md` documents the build and consistency fixes that were applied during stabilization.
- `data:routing` currently provides cache and stub implementations only; Google transit integration is a later phase.
- The current UI is no longer just a shell: destination selection, trip setup, live trip, alert dismissal, and history/summary screens now flow through persisted Room data.
- Release builds now enforce HTTPS-only networking and include baseline shrinker rules for Room, Hilt, WorkManager, and Kotlin serialization.
