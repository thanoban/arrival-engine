# NearWake Project Study Guide

This guide is written for someone who is still learning Kotlin and Android.
The goal is to explain:

- what this project is trying to build
- why the codebase is split into modules
- what each module does
- what the main Android and Kotlin tools are doing
- how the app flows from launch to alert
- what is already built and what is still planned

If you study this file together with `PLAN.md`, the repo will make much more sense.

For the practical machine-setup and API-key view, also read `SETUP_AND_STATUS.md`.

## 1. What This Project Is

NearWake is an Android app that helps a user avoid missing a stop or destination.

The main idea is:

1. The user chooses a destination.
2. The app arms a trip.
3. The app monitors in the background in a battery-aware way.
4. The app alerts before the user misses the destination.

This is not a full maps/navigation app.
It is an arrival alarm app.

That is an important design decision because it shapes the whole architecture:

- we care more about reliable alerting than rich map UI
- we need process death recovery
- we need low-battery background behavior
- we need persisted trip state

## 2. What "Multi-Module Architecture" Means

In a small demo app, everything often sits in one module called `app`.
That becomes messy fast.

This repo uses many Gradle modules so each part has a clear job.

Think of the architecture like a company:

- `app` is the front desk that wires everything together
- `feature:*` modules are the screens the user sees
- `domain:*` modules are the business rules
- `data:*` modules are the real Android implementations
- `core:*` modules are shared tools and infrastructure

This gives us:

- better organization
- faster builds in larger projects
- easier testing
- easier future growth
- cleaner dependency rules

## 3. Why These Layers Exist

### `domain`

The `domain` layer is the brain.
It should be as pure as possible.

This means:

- no Android UI
- no Activity
- no Service
- no database code
- mostly plain Kotlin classes

Why this matters:

- pure Kotlin code is easier to test
- business rules stay stable even if UI changes
- Android details do not leak into core logic

### `data`

The `data` layer is how the app talks to the real world.

Examples:

- GPS / fused location
- geofencing
- activity recognition
- foreground service
- WorkManager
- local persistence coordination

The `domain` layer says what the app needs.
The `data` layer says how Android will actually do it.

### `feature`

The `feature` layer is screen-focused.

Each feature module usually contains:

- screen Composables
- screen ViewModels
- navigation callbacks
- UI state for that screen

This keeps UI code grouped by user workflow rather than by widget type.

### `core`

The `core` layer holds shared building blocks.

Examples:

- database setup
- shared test fakes
- DataStore
- shared theme
- common helpers

### `app`

The `app` module is the assembly layer.
It should be thin.

Its job is:

- application startup
- global navigation
- Hilt app wiring
- top-level manifest and permissions

## 4. Full Module Map

These modules are declared in `settings.gradle.kts`.

### `:app`

Purpose:

- final Android application
- app manifest
- Hilt application setup
- top-level navigation graph
- startup destination logic

Important files:

- `app/src/main/kotlin/com/nearwake/app/NearWakeApp.kt`
- `app/src/main/kotlin/com/nearwake/app/MainActivity.kt`
- `app/src/main/kotlin/com/nearwake/app/NearWakeNavHost.kt`
- `app/src/main/kotlin/com/nearwake/app/NearWakeRoute.kt`

### `:core:common`

Purpose:

- shared Kotlin utilities and common types
- small reusable helpers that many modules need

Why it exists:

- avoids repeating tiny helpers everywhere
- gives `domain` a lightweight shared dependency

### `:core:database`

Purpose:

- Room database
- entities
- DAOs
- database configuration

Why it exists:

- persistence is infrastructure, not UI
- many modules need stored trip/session/history data

### `:core:datastore`

Purpose:

- lightweight key-value app preferences
- things like onboarding completion and settings

Why this is separate from Room:

- Room is for structured relational data
- DataStore is for simple preferences

### `:core:designsystem`

Purpose:

- app theme
- colors
- typography
- shared visual style

Why it exists:

- keeps UI consistent
- stops every feature screen from inventing its own styles

### `:core:network`

Purpose:

- shared HTTP/network support

Current state:

- shared `OkHttpClient`
- shared `Json`
- shared `Retrofit.Builder`
- used by routing

Why keep it now:

- route-aware work already plugs into it
- future backend/API work can reuse the same foundation

### `:core:testing`

Purpose:

- fake implementations for tests
- reusable fixtures

Examples:

- fake location source
- fake routing source
- trip engine fixtures

Why it matters:

- tests become easier to write
- fake objects can be reused across modules

### `:core:ui`

Purpose:

- reusable UI pieces shared by features

Why it exists:

- stops screen modules from duplicating common UI patterns

### `:domain:trip`

Purpose:

- trip models
- trip state machine
- trip engine
- alerting/recovery decisions

This is one of the most important modules in the whole repo.

Why:

- this is the main business behavior of the app
- "when should we monitor / alert / recover / stop?" lives here

### `:domain:location`

Purpose:

- location-related models and contracts

Examples:

- `LatLng`
- `SavedPlace`
- `GeofenceSpec`
- `MotionState`

Why it exists:

- location concepts are important enough to model clearly

### `:domain:routing`

Purpose:

- route models and routing interfaces

Current state:

- route models and contracts are real and already used by feature flows
- route snapshots are cached per trip and shown in multiple screens

Why it exists:

- keeps future route-aware work separate from Android implementation details

### `:data:location`

Purpose:

- Android location implementation
- geofences
- fused location orchestration

This module connects domain ideas like "approach zone" to Android location APIs.

### `:data:motion`

Purpose:

- Android activity recognition implementation

Why it matters:

- the app wants to know if the user is moving or in a vehicle
- this helps save battery

### `:data:routing`

Purpose:

- routing source implementations

Current state:

- includes a Google Transit data source
- includes a Room-backed local route cache
- provides graceful destination-only fallback when an API key or route data is unavailable

### `:data:alerts`

Purpose:

- foreground monitoring service
- recovery worker
- alert orchestration
- cleanup logic

This is another very important module.

Why:

- it handles the real background runtime behavior of the app

### `:data:analytics`

Purpose:

- diagnostics logging
- local event tracking

Why it exists:

- helps debugging
- helps understand what happened in a trip session

### `:feature:onboarding`

Purpose:

- first-run introduction
- user education and onboarding completion

### `:feature:permissions`

Purpose:

- explain and request permissions safely

Why separate:

- permissions are part of the user experience, not just a technical detail

### `:feature:places`

Purpose:

- place search / selection
- saved places

### `:feature:tripsetup`

Purpose:

- turn a selected place into an armed trip
- choose trip options

### `:feature:livetrip`

Purpose:

- show current active trip session
- show monitoring state

### `:feature:alerts`

Purpose:

- alert UI
- recovery UI after missed/overshoot behavior

### `:feature:history`

Purpose:

- past trips
- trip summary screens

### `:feature:settings`

Purpose:

- user preferences

### `:feature:diagnostics`

Purpose:

- debug/QA information

## 5. Dependency Direction Rules

This is one of the most important architecture ideas.

Not every module is allowed to depend on every other module.

The intended direction is:

- `feature:*` depends on `core:*` and `domain:*`
- `data:*` depends on `core:*` and `domain:*`
- `domain:*` stays as pure as possible
- `app` can depend on everything because it assembles the final application

Why these rules exist:

- UI should not tightly control low-level infrastructure
- business rules should not know Android details
- feature screens should be replaceable without breaking data internals

## 6. The Main Runtime Flow

This is the big picture of how the app behaves.

### Step 1: App starts

The app starts in `NearWakeApp` and `MainActivity`.

Important idea:

- `NearWakeApp` is the application class
- `MainActivity` hosts Compose UI
- `NearWakeAppViewModel` chooses where the app starts

The startup destination depends on onboarding state.

If onboarding is complete:

- start at `Home`

If onboarding is not complete:

- start at `Onboarding`

### Step 2: Navigation is handled by `NearWakeNavHost`

The app uses Navigation Compose.

`NearWakeRoute.kt` defines route names like:

- `onboarding`
- `home`
- `places`
- `trip_setup/{placeId}`
- `live_trip/{tripId}`
- `alert/{tripId}`

`NearWakeNavHost.kt` connects routes to Composable screens.

### Step 3: User selects a destination

The `places` feature lets the user choose a place.

That chosen place feeds the `tripsetup` feature.

### Step 4: Trip is created and monitoring begins

The trip setup flow stores trip data and starts monitoring.

If a last known location is available and routing succeeds:

- the app fetches a transit preview
- caches the route snapshot against the new trip
- seeds the first ETA shown in live trip/history

If routing is unavailable:

- the app still starts in destination-only mode without blocking the user

The app then moves to the live trip screen.

### Step 5: Background monitoring happens

This is where multiple pieces work together:

- `domain:trip` decides state transitions
- `data:location` handles location strategy
- `data:motion` handles activity recognition
- `data:alerts` runs the foreground service

### Step 6: Alert fires or recovery flow happens

If the user is near the destination:

- alert screen can be shown

If something goes wrong or the trip needs recovery:

- recovery flow can take over

### Step 7: Cleanup

Cleanup matters a lot in Android background work.

The app must stop:

- active location updates
- geofences when no longer needed
- activity transition registrations when appropriate
- foreground service when the trip ends

That is why cleanup code is a real use case, not just an afterthought.

## 7. How the Background Side Works

This project is not just UI.
It is mostly about reliable background behavior.

### Foreground service

`TripMonitoringService` is used because Android background work is restricted.

Why a foreground service:

- monitoring must remain reliable
- the user must know the app is actively doing background work
- Android gives foreground services more permission to keep running

Current implementation note:

- the service now restores a `TripSession` from Room on start
- it persists updated `TripSession` state back to Room as geofence and motion events move the trip through the engine
- that lets the live trip and history detail screens read real monitoring state instead of only startup defaults

### WorkManager recovery

`TripRecoveryWorker` exists so the app can recover from:

- process death
- app restart cases
- orphaned active trip sessions

This is important because Android can kill your process at any time.

### Boot recovery

There is also boot-time recovery wiring so the app can re-check active work after device reboot.

This matters because a user might set a trip, reboot the phone, and still expect the app to recover properly.

## 8. Kotlin Basics You Need For This Repo

If Kotlin is new to you, focus on these ideas first.

### `val` and `var`

- `val` means read-only reference
- `var` means mutable reference

Example:

```kotlin
val name = "NearWake"
var count = 0
```

### Data classes

Used for simple data containers.

Example:

```kotlin
data class SavedPlace(
    val id: String,
    val name: String,
)
```

Why used heavily here:

- domain models are mostly data

### `object`

An `object` is a singleton.

Used when you want one shared instance.

Example in this repo:

- route objects inside `NearWakeRoute`
- scheduler/helper style objects in some places

### `sealed class`

A sealed class is useful when you want a controlled family of types.

In this repo it is used for route definitions.

Why useful:

- all valid route types are known in one place

### Functions

Basic syntax:

```kotlin
fun createRoute(id: String): String = "trip/$id"
```

### Nullable types

`String?` means the value can be null.

Why common here:

- some trip fields are not always known yet
- Android state often arrives gradually

### Constructors

Primary constructor values are often defined directly in the class header.

Example:

```kotlin
class Example(
    private val repo: MyRepo,
)
```

### `suspend`

A `suspend` function is used for coroutines.

Why:

- database calls
- background work
- async operations

Example:

```kotlin
suspend fun loadTrip(id: String): Trip
```

### Coroutines

Coroutines are Kotlin’s lightweight async system.

Why this app uses them:

- database work
- background coordination
- flows of state
- WorkManager and service logic

### `Flow`

`Flow` is a stream of values over time.

Why useful here:

- settings can update over time
- database observation can emit new values
- UI can react automatically

### Extension functions / properties

Kotlin lets you add functions to existing types.

Example idea:

```kotlin
val TripSession.isActive: Boolean
    get() = !state.isTerminal
```

This keeps logic readable.

## 9. Android / Jetpack Tools Used Here

These are the main "built-in" frameworks and libraries you should understand.

### Jetpack Compose

Compose is Android’s modern UI toolkit.

Why used here:

- screen code is written in Kotlin
- UI is built from Composable functions
- navigation integrates nicely

Examples:

- `OnboardingScreen`
- `PermissionsScreen`
- `LiveTripScreen`

### Navigation Compose

This handles moving between screens.

Why used:

- cleaner route-based navigation
- supports arguments like `tripId`

### Hilt

Hilt is dependency injection.

Simple meaning:

- instead of manually creating every object yourself,
- Hilt builds and provides dependencies for you

Why useful:

- less wiring boilerplate
- easier testing
- better lifecycle-aware object creation

Examples:

- ViewModels getting repositories or DataStore
- workers receiving dependencies
- service dependencies being injected

### Room

Room is the local SQL database layer.

Why used:

- store trips
- store sessions
- store cached route snapshots
- store diagnostics/history
- survive process death

Important Room concepts:

- Entity = table model
- DAO = database access object
- Database = holder of DAOs

### DataStore

DataStore stores small preference-style data.

Why used:

- onboarding completed flag
- settings/preferences

### WorkManager

WorkManager runs scheduled or deferrable background work.

Why used here:

- recovery logic
- resilient startup/background checks

### Play Services Location

This gives modern Android location features.

Used for:

- fused location provider
- geofencing

### Activity Recognition

This helps infer whether the user is:

- in vehicle
- walking
- still

Why important:

- battery savings
- smarter trip monitoring

### Timber

Timber is a logging library.

Why used:

- cleaner logs for debugging

### kotlinx.serialization

Used to serialize structured data.

Why useful:

- structured payload logging
- data conversion

### kotlinx-datetime

Used for date/time values like timestamps.

Why useful:

- trip/session timing
- alert/recovery timestamps

### KSP

KSP is Kotlin Symbol Processing.

Why it exists here:

- Hilt code generation
- Room code generation

Without KSP, generated code for those frameworks would not be produced.

## 10. Repo "Built-Ins" at Build Time

Besides Android libraries, this repo also has its own build logic.

That lives in:

- `build-logic/convention`

These convention plugins reduce repeated Gradle code.

### `AndroidApplicationConventionPlugin`

Sets common Android app configuration.

### `AndroidApplicationComposeConventionPlugin`

Adds Compose support for application modules.

### `AndroidLibraryConventionPlugin`

Sets common Android library config.

### `AndroidLibraryComposeConventionPlugin`

Adds Compose setup for Android library modules.

### `AndroidFeatureConventionPlugin`

Applies standard dependencies and config to feature modules.

That is why feature modules do not each repeat the same navigation and Compose dependencies.

### `AndroidHiltConventionPlugin`

Adds Hilt and KSP setup.

### `KotlinLibraryConventionPlugin`

Used for pure JVM Kotlin modules like domain modules.

This is why domain tests use JUnit 5 and JVM 17 cleanly.

## 11. Why Services, Workers, and ViewModels Are Different

This is a very common beginner confusion.

### ViewModel

A ViewModel is for UI-related state and logic.

Use it when:

- a screen needs state
- a screen triggers actions
- you want logic to survive configuration changes

### Service

A Service is for longer-lived background behavior.

Use it when:

- work should continue outside the screen
- the app must keep monitoring in background

### Worker

A Worker is for scheduled/deferred work.

Use it when:

- Android should restart/check work later
- you want recovery/resilient background execution

In this repo:

- ViewModels drive screens
- `TripMonitoringService` handles active monitoring
- `TripRecoveryWorker` handles restart/recovery checks

## 12. What Is Already Built

At a high level, the repo already has these major pieces:

- multi-module project structure
- Gradle wrapper and convention plugins
- domain trip engine and tests
- Room database foundations
- DataStore preferences
- location/motion/alerts foundations
- shared network foundation in `core:network`
- Google transit route fetching and Room-backed route cache
- persisted trip flow across UI screens
- route preview wiring in trip setup
- route summary wiring in live trip and trip history detail
- onboarding persistence
- settings and diagnostics backed by stored data
- WorkManager-based trip recovery wiring
- monitoring service session persistence through Room
- release build hardening

This means the project is no longer just a scaffold.
It already has real architecture and working flows.

## 13. What Is Still Not Finished

Important future work still remaining from the broader plan includes:

- deeper live ETA refresh logic during active monitoring
- broader end-to-end scenario testing
- more UI/design polish
- more production hardening and store-launch prep
- privacy policy / distribution / final release prep

So the repo is substantial, but not feature-complete.

## 13.1 What You Need To Provide To Run It

At the moment, you do not need a backend secret set or server environment.

The main things you need are:

- Android SDK path via `sdk.dir`
- optionally `MAPS_API_KEY`

Why `sdk.dir` matters:

- Gradle cannot build the Android app without the Android SDK path

Why `MAPS_API_KEY` matters:

- current route preview uses Google Directions transit calls
- without the key, the app falls back to destination-only mode
- with the key, trip setup/live/history can show richer route data

What API should be enabled first:

- `Directions API`

What is not fully wired yet:

- real Google Places-backed search flow
- map-heavy UI

So one Google key with `Directions API` enabled is the most useful current addition.

## 14. Beginner Reading Order

If you are learning Kotlin and this repo at the same time, do not jump randomly.

Study in this order:

1. `settings.gradle.kts`
2. `PLAN.md`
3. `PROJECT_STUDY_GUIDE.md`
4. `app/src/main/kotlin/com/nearwake/app/NearWakeRoute.kt`
5. `app/src/main/kotlin/com/nearwake/app/NearWakeNavHost.kt`
6. one simple feature screen such as onboarding or permissions
7. `domain:trip` models
8. `TripStateMachine`
9. `TripEngine`
10. `core:database` entities and DAOs
11. `data:alerts` and `data:location`

Why this order helps:

- you first learn the shape of the app
- then the screen flow
- then the business logic
- then persistence
- then the harder Android background machinery

## 15. If You Are New To Kotlin, Study These Concepts In Order

1. variables: `val`, `var`
2. functions
3. classes and constructors
4. data classes
5. nullable types
6. collections
7. `object`
8. `sealed class`
9. coroutines and `suspend`
10. Flow
11. annotations like `@Inject`, `@Composable`, `@HiltViewModel`

## 16. Why This Project Is Good For Learning

This repo is useful for study because it shows multiple important real-world ideas in one place:

- clean architecture
- Compose UI
- dependency injection
- local persistence
- background Android work
- testing pure logic separately from Android details

It is more complex than a beginner toy app, which is good for learning architecture.
But because it is multi-module, it also gives you a clean structure to follow.

## 17. Practical Advice For You While Studying

- Start by understanding what each module is responsible for.
- Do not try to memorize every file.
- When you open a file, first ask: "Is this UI, domain, data, or infrastructure?"
- When you see a class, ask: "What job does this class own?"
- When you see a dependency, ask: "Why is this class receiving this instead of creating it directly?"
- When you see `suspend`, think: "This is probably async/background work."
- When you see `@Inject`, think: "Hilt is constructing this."
- When you see `Flow`, think: "This value can change over time."

## 18. Short Summary

NearWake is an arrival-alarm Android app.

Its architecture is split intentionally:

- `app` assembles
- `feature` shows screens
- `domain` holds rules
- `data` talks to Android systems
- `core` provides shared tools

The most important learning areas in this repo are:

- Kotlin basics
- Compose navigation
- Hilt injection
- Room persistence
- background service + WorkManager recovery
- trip state machine thinking

Read this guide with `PLAN.md` and the app/navigation files first, then move deeper into the domain and data modules.
