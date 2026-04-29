# NearWake Target Production Architecture

This file describes the production-level architecture NearWake is moving toward as the repo hardens from a strong prototype into a scalable modular monolith.

Use it when you want to know:

- what architecture NearWake should converge toward
- which module boundaries are intentional
- what dependency directions are allowed
- what has already been migrated in the current repo

## 1. Core Decision

NearWake should be a **modular monolith**, not a microservice system.

That means:

- one Android application repo
- strict internal module boundaries
- a real application layer for orchestration
- pure domain logic
- thin Android runtime entry points

This is the right fit because most complexity is device-side:

- location
- foreground/background monitoring
- notifications
- battery-aware behavior
- recovery after process death or reboot

## 2. Target Module Shape

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

## 3. Layer Responsibilities

### `app`

Assembly only:

- application startup
- manifest
- top-level navigation
- global dependency wiring

### `application:*`

Workflow orchestration:

- start a trip
- re-arm a previous trip
- start or stop monitoring
- preserve trip-level alert preferences such as `Time`, `Distance`, or `Both`
- later: cancel trip, complete trip, export diagnostics, schedule reminders

### `domain:*`

Business rules only:

- trip engine
- confidence model
- recovery planning
- commute prediction

No Android APIs.
No Room.
No service classes.

### `ports:*`

Interfaces that the inner layers depend on:

- monitoring control
- workflow-oriented persistence
- later: notifications, telemetry, location/session query ports

### `data:*`

Concrete adapters and Android-integrated implementations:

- Room-backed persistence
- Google routing / Places integrations
- geofence and activity-recognition sources
- notification and service runtime infrastructure

### `feature:*`

UI modules only:

- screens
- screen ViewModels
- UI state
- navigation callbacks

Features should trigger application use cases instead of directly orchestrating storage/runtime flows.

## 4. Dependency Direction Rules

```text
feature -> application -> domain + ports
data -> ports + domain + core
app -> feature + application + data + core
domain -> domain only
```

Avoid these directions:

- `feature -> data`
- `feature -> Room DAOs/entities`
- `feature -> TripMonitoringService`
- `domain -> Android`
- `domain -> Room`

## 5. Current Migration Status

Completed architecture-hardening slices:

### Slice A - Monitoring boundary

Added:

- `:ports:monitoring`
- `:application:monitoring`

Result:

- app/features no longer start or stop `TripMonitoringService` directly
- monitoring control now flows through application use cases and a monitoring port

### Slice B - Trip start orchestration

Added:

- `:ports:persistence`
- `:application:trip`

Result:

- trip start and re-arm flows moved out of screen-level orchestration
- `HomeViewModel` and `TripSetupViewModel` now call application use cases
- Room-backed write coordination moved behind `TripLifecycleStore`

## 6. Still To Migrate

Next important architecture slices:

1. move cancel / complete / recovery flows into `:application:trip`
2. pull more feature read-state away from direct DAO/entity assembly
3. reduce `TripMonitoringService` so it focuses on runtime concerns instead of product orchestration
4. add CI guardrails that fail if `feature` modules depend on `data:*`

## 7. Short Summary

NearWake should scale as a **modular Android monolith with hexagonal boundaries**:

- `feature` for UI
- `application` for orchestration
- `domain` for rules
- `ports` for interfaces
- `data` for concrete adapters

That keeps current features intact while making the repo safer for long-term growth.

A good current example is the configurable early-alert work: the trigger mode and distance are modeled in `domain:trip`, persisted through Room/DataStore adapters, surfaced through application-backed trip flows, and then consumed by feature screens and runtime monitoring without turning the UI into the business-logic owner.
