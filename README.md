# NearWake

NearWake is an Android-first arrival alarm app for buses, trains, and transfers. The current codebase follows a multi-module clean architecture with an offline-first MVP focus: arm a trip quickly, monitor quietly in the background, and alert before the user misses the stop.

## Current Status

- Project scaffold, Gradle convention plugins, and module graph are in place.
- Core domain models and the trip engine state machine are implemented.
- Room database and DataStore foundations are implemented.
- Location, motion, alert-service, and routing stub layers are present as architecture-ready foundations.
- Design system, navigation shell, and primary feature screen scaffolds are present.
- Gradle wrapper is included.

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
- Android SDK with API 35 installed
- A valid `sdk.dir` in `local.properties`
- Optional: Google Maps / Places API key for later map and places integration

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

3. Run a JVM-only verification task:

   ```powershell
   .\gradlew.bat :domain:trip:test
   ```

4. Run an Android build once the SDK path is valid:

   ```powershell
   .\gradlew.bat :app:assembleDebug
   ```

## Verification Status

- Verified:
  - `.\gradlew.bat :domain:trip:test`
- Not yet verified on this machine:
  - `.\gradlew.bat :app:assembleDebug`

The remaining blocker for a full Android build in this environment is a missing Android SDK installation / `local.properties` SDK path.

## Notes

- `corrections.md` is included as a running fixes log that was applied during build stabilization.
- The current routing layer is intentionally stubbed for MVP destination-only mode.
- The alert, service, and UI layers are implemented as foundations and will benefit from a full Android compile pass once the SDK is available locally.
