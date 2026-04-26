# NearWake Setup And Status

This file is the practical project-status and setup reference for the current repo state.

Use it when you want to know:

- what is already finished
- what still remains
- what keys or environment values you must provide
- what APIs need to be enabled
- what works even without keys
- how to verify the project on your machine

If you want the product-side reference for competitor comparison, scope decisions, and technology choices, read [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md).

## 1. Current Project Condition

The project is no longer a scaffold.
It is a buildable Android app with real architecture and multiple working flows.

What is already implemented:

- multi-module architecture across `app`, `core`, `domain`, `data`, and `feature`
- Compose navigation and screen flow
- Room database and DAOs
- DataStore preferences
- trip engine/state machine
- background monitoring service
- WorkManager trip recovery
- route preview and route cache
- Google transit routing integration
- Google Places-backed destination search with local fallback
- persisted light/dark/system appearance mode
- flexible leave-by reminders from learned commute predictions
- persisted trip/session/history flows
- diagnostics and settings screens backed by real data

What this means in practice:

- the repo builds
- the frontend exists
- the backend is not required for the current app
- the app can run in a degraded destination-only mode without a Google routing key

## 2. What Is Finished

These areas are meaningfully developed:

- onboarding
- permissions flow
- home screen
- place selection flow
- trip setup
- live trip
- alerts and recovery screens
- history and trip summary
- settings
- appearance/theme selection
- departure reminder scheduling
- diagnostics
- route cache and route summaries
- monitoring session persistence

These infrastructure areas are also in place:

- Gradle wrapper
- convention plugins
- Hilt dependency injection
- Room persistence
- DataStore
- shared network module
- test coverage for major domain/data slices

## 3. What Is Still Not Finished

These are the main remaining gaps before calling the project fully finished:

- deeper live ETA refresh behavior during an active monitored trip
- broader end-to-end scenario testing
- richer UI polish and edge-state refinement
- real-device validation for flexible reminder delivery timing
- release/store readiness items such as privacy/distribution/final hardening

So the app is substantial and usable for development, but not fully production-finished.

## 3.1 Problem And Scope Reference

If you are comparing this project with competitor apps, use [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md) for:

- the exact problem NearWake is trying to solve
- what the product is and is not
- current scope versus future scope
- implemented features versus planned work
- the technology stack and why those choices were made

## 4. What You Must Provide

Right now, the repo only requires a very small amount of machine-specific setup.

### Required now

- `sdk.dir`

This is required for Android builds.

### Optional but strongly recommended now

- `MAPS_API_KEY`

This enables Google Places destination search and Google transit route preview / ETA refresh.

Without it:

- the app still builds
- the app still runs
- place search falls back to local sample results
- the app falls back to destination-only monitoring

With it:

- trip setup can fetch a route preview
- route snapshots can be cached per trip
- live trip and history flows can show richer route context
- place search can use Google Places instead of the local fallback list

## 5. Exact File To Update

Create or update `local.properties` at the project root:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

You can start from:

```properties
sdk.dir=/path/to/android/sdk
MAPS_API_KEY=REPLACE_WITH_YOUR_KEY
```

which already exists in `local.properties.template`.

`MAPS_API_KEY` can also be passed as a Gradle property, but `local.properties` is the normal local-development path.

## 6. Which APIs To Enable

### Needed now

- `Directions API`
- `Places API`

Why:

- `Directions API` is used by the current transit routing code.
- `Places API` is used by the provider-backed destination search path.

### Likely needed later

- `Maps SDK for Android`

Important current note:

- the app can still run without `Places API` by using the local fallback list

So if you only want routing previews, `Directions API` is the most important one. If you also want real place search, enable `Places API`.

## 7. What Is Not Needed Right Now

You do not need to provide these to keep development moving:

- backend URL
- server database connection
- Firebase configuration
- OpenAI API key
- auth secrets
- Node `.env`
- Sentry DSN

The app is currently offline-first and does not depend on a custom backend to build or run.

## 8. Current Behavior With And Without Keys

### If `MAPS_API_KEY` is set

- routing module uses the Google transit provider
- place search uses Google Places
- trip setup can show route preview
- route snapshots are cached against trip ids
- live trip and trip summary can show route details

### If `MAPS_API_KEY` is missing

- routing gracefully falls back
- place search uses the local fallback list
- trip setup shows destination-only behavior
- the app does not block the user from starting a trip
- the app remains buildable

## 9. Verification Commands

These are the useful verification commands for the current repo:

```powershell
.\gradlew.bat :domain:trip:test
.\gradlew.bat :core:network:test
.\gradlew.bat :data:routing:test
.\gradlew.bat :data:alerts:test
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

## 10. Recommended Next Inputs From You

If you want development to continue smoothly, the most useful thing you can provide is:

1. a valid `sdk.dir`
2. a `MAPS_API_KEY`
3. that key with `Directions API` and `Places API` enabled

That is enough for the current routing-backed and provider-backed place search behavior.

## 11. Short Answer

To build the app on your machine:

- set `sdk.dir`

To unlock the current route-aware behavior:

- add `MAPS_API_KEY`
- enable `Directions API` and `Places API`

To fully finish the whole product:

- more development is still needed, but no backend secret set is blocking the current repo right now.
