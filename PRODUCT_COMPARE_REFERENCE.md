# NearWake Product Compare Reference

This file is the easiest project reference to use when comparing NearWake against competitor apps.

Use it to answer these questions quickly:

- what exact problem are we solving
- who is the user
- what the app does now
- what the app does not do
- what is already built
- what is still planned
- what technology choices were made
- what parts can still change if the product scope changes

## 1. One-Line Product Definition

NearWake is an offline-first arrival alarm app that helps users avoid missing a bus stop, train stop, transfer, or destination.

## 2. Core Problem We Are Solving

The main problem is not navigation.
The main problem is not route planning.
The main problem is not social location sharing.

The real problem is:

- users miss stops because they fall asleep
- users miss stops because they get distracted
- users cannot depend on themselves to watch the route continuously
- many transit apps help users plan, but do not specialize in reliably waking or warning them at the right time

NearWake is meant to reduce that failure point.

## 3. User Situation

The ideal user is someone who:

- travels by bus or train
- is tired during commuting
- may nap during travel
- wants confidence that they will not miss the stop
- does not want a heavy always-on tracking app

The strongest use cases are:

- bus commuters
- train commuters
- long daily travel
- night or early-morning travel
- transfer-heavy public transit travel

## 4. Product Job To Be Done

The job NearWake is trying to do is:

"Let me set where I need to get off, then quietly monitor in the background and alert me before I miss it."

That breaks into these product responsibilities:

- let the user choose a destination quickly
- let the user arm a trip quickly
- monitor efficiently in the background
- conserve battery as much as possible
- survive app/process death as much as practical
- alert clearly before the user misses the stop

## 5. What NearWake Is

- an arrival assurance app
- a destination monitoring app
- an offline-first Android mobile app
- a background-aware transit helper
- a focused utility product rather than a general transit platform

## 6. What NearWake Is Not

- not a full transit planner replacement
- not a live map-first navigation product
- not a social sharing app
- not a rideshare app
- not a safety/emergency platform
- not a full backend-first SaaS product today

This matters during competitor analysis because many competitors may solve adjacent problems but not the exact same one.

## 7. Current Scope

Current scope is:

- destination selection
- trip arming
- background monitoring
- alerting and recovery
- trip/session persistence
- route preview when Google routing is available
- diagnostics and settings

Current scope is not yet:

- production backend sync
- full account system
- production-grade provider-backed places flow
- full release-store hardening
- polished final end-to-end recovery coverage in every scenario

## 8. Current Feature Status

### Implemented now

- onboarding
- permissions flow
- home screen
- place selection flow
- trip setup
- live trip screen
- alert screen
- recovery screen
- history
- trip summary
- settings
- diagnostics
- Room-backed trip persistence
- DataStore-backed app preferences
- route snapshot cache
- Google transit route preview support
- background monitoring service
- WorkManager recovery path

### Implemented, but still immature

- live monitoring behavior is present, but still needs more hardening and broader scenario coverage
- route-aware behavior exists, but the product still degrades to destination-only mode in several cases
- UI is modernized, but not fully polished
- place search exists, but is not yet a real production Google Places flow

### Planned or likely next

- broader live monitoring hardening
- better end-to-end scenario testing
- richer empty/error/loading states
- real production auth path
- real provider-backed place search
- final release-readiness items

## 9. Current Differentiators

These are the strongest current differentiators to compare against competitors:

- offline-first design
- battery-conscious monitoring strategy
- geofence and activity-recognition-first monitoring
- destination assurance focus instead of generic transit browsing
- process-death/session recovery architecture
- strong modular architecture for future evolution

If a competitor is stronger in maps, route exploration, or real-time schedules, NearWake can still win on focused arrival assurance and background alert reliability.

## 10. Current Weaknesses

These are the main current weaknesses to compare honestly:

- not production-finished yet
- no full backend or cloud sync story yet
- no finalized account/login flow yet
- place search is not fully provider-backed yet
- monitoring still needs broader real-world edge-case validation
- product positioning is clearer than the final business model

## 11. Technology Stack

### Core platform

- Kotlin
- Android
- Jetpack Compose
- Jetpack Navigation
- Hilt

### Persistence and state

- Room
- DataStore

### Background and device capabilities

- WorkManager
- Google Play Services Location
- Geofencing
- Activity Recognition
- Foreground Service

### Networking and external integration

- OkHttp
- Retrofit-ready shared network layer
- Google Directions API integration in the routing module

### Testing and architecture support

- JUnit 5
- MockK
- shared fake/testing modules

## 12. Why These Technology Choices Matter

These choices were made to support the product problem, not just for engineering style.

### Why Room

- trip/session state must survive process death
- history and diagnostics need structured persistence

### Why DataStore

- user preferences are simple key-value settings
- lighter than putting everything in Room

### Why Hilt

- many modules and Android components need dependency injection
- service/repository/data-layer composition needs to stay manageable

### Why Geofencing and Activity Recognition

- they reduce constant GPS usage
- they fit the battery-sensitive nature of the product

### Why Foreground Service

- active monitoring needs a durable runtime path
- Android background limitations require a more explicit monitoring model

### Why Google Directions

- it provides a better route preview and ETA source when available
- but the app is still designed to degrade gracefully if routing is unavailable

## 13. External Dependencies And Operational Inputs

Current required or useful external inputs:

- Android SDK on the development machine
- `sdk.dir` in `local.properties`
- `MAPS_API_KEY` for Google route preview and ETA refresh

Current Google API most relevant now:

- `Directions API`

Likely future Google APIs:

- `Places API`
- `Maps SDK for Android`

## 14. Comparison Questions To Ask Against Competitors

When comparing NearWake to competitors, these are the most useful questions:

- do they solve missed-stop prevention directly, or only route planning generally
- do they support background alerting well
- are they battery-heavy or battery-light
- do they work only with live network data, or can they degrade gracefully
- do they focus on commuters who may sleep or get distracted
- do they support transfers or only final destinations
- is their alerting experience stronger than their planning experience
- do they require a backend/account before delivering value
- is their product broad and generic, or focused and dependable

## 15. Scope Levers That Can Still Change

If competitor analysis changes the direction, these parts can still be adjusted:

- target user segment
- exact alert timing model
- whether route-aware mode stays central or secondary
- whether auth becomes important early
- whether backend sync becomes part of MVP
- whether the app stays transit-only or broadens into general arrival assurance

These parts should be changed carefully:

- offline-first philosophy
- battery-sensitive monitoring approach
- core promise of "wake me before I miss my stop"

## 16. Short Positioning Summary

NearWake should currently be compared as:

- a focused arrival-alarm product
- an offline-first commuter assistant
- a battery-conscious destination monitoring app

It should not be judged mainly as:

- a complete transit planning suite
- a map-heavy navigation platform
- a backend-first mobility product

## 17. Best Use Of This File

Use this file when:

- comparing competitors
- rewriting the scope
- refining the MVP
- preparing a pitch
- deciding what features to keep, cut, or postpone
