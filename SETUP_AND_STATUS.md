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
If you want the Sri Lanka-specific transport-data sourcing and production-data plan, read [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md).
If you want the post-v1.0 competitive feature roadmap (Wave I/J/K), read [PRODUCT_EXPANSION_ROADMAP.md](PRODUCT_EXPANSION_ROADMAP.md).

---

## 1. Current Project Condition

The project is a fully-functional Android app with production-grade architecture across 37 modules.

All core screens are shipped. The UI/UX redesign pass (Wave H) is complete. Architecture hardening (Wave G) is substantially complete. The remaining work before Play Store submission is field testing, hosted privacy policy, release signing, and a full accessibility TalkBack pass.

### What is implemented

**Architecture**
- 37-module multi-module project across `:app`, `:application:*`, `:ports:*`, `:core:*`, `:domain:*`, `:data:*`, and `:feature:*`
- Room v7 database with migrations 1→7
- Hilt dependency injection throughout
- DataStore for preferences
- WorkManager + ForegroundService for background monitoring (survives process death)
- `ThresholdConfig` — all alert bias multipliers and geofence radii are injected constants; no hardcoded values in the alert engine
- `StaticRemoteConfigRepository` in `:core:remoteconfig` — v1.0 hardcoded defaults with a Firebase drop-in interface for post-launch
- StrictMode enabled in debug builds (`BuildConfig.DEBUG` guard in `NearWakeApp.onCreate()`)
- Baseline Profile generator + startup benchmark in `:core:benchmark`

**Features — all screens shipped**
- Onboarding and permissions flow
- Live permission status with staged runtime permission requests
- Home screen
- Place selection with Google Places search and local fallback
- Trip setup with `Time` / `Distance` / `Both` trigger mode selection
- Live trip screen
- Three-stage alert system (Approach / Imminent / Arrival)
- Sleep mode and Active mode alert personalities
- Missed-stop recovery screen with one-tap re-arm
- Trip history and trip summary
- Settings screen with configurable alert trigger defaults
- Appearance / theme selection (light / dark / system)
- Departure reminder scheduling from learned commute predictions
- Diagnostics screen with CSV export for field-test evidence
- Route preview and route cache per trip
- Monitoring session persistence

**Infrastructure**
- Gradle wrapper + convention plugins
- Sentry crash reporting (activated when `SENTRY_DSN` is set)
- 67+ unit test classes across domain, data, and application layers
- `StaticRemoteConfigRepositoryTest`, benchmark classes, and alert engine tests included

**UI/UX (Wave H — complete)**
- Compact layouts: 56dp place search rows, 72dp destination cards, 80dp active trip cards
- `NearWakeButtonSize` enum (`Small` / `Medium` / `Large`) replacing ad-hoc sizing
- Design token additions: `cardCompact`, `cardDefault`, `cardLarge`
- `PlaceResultRow` composable replacing inline search result rows
- `SingleChoiceSegmentedButtonRow` for trigger mode in TripSetupScreen
- Sticky Arm button in `Scaffold bottomBar` on TripSetupScreen
- Material Icons Extended for icon-driven status communication
- Accessibility: AlertScreen and LiveTripScreen have content descriptions; full TalkBack pass remaining

---

## 2. What Is Complete

Every screen and flow listed in PLAN.md Wave A through Wave H is shipped:

- Wave A — project structure and Hilt wiring
- Wave B — Room entities and DAOs
- Wave C — domain layer (trip engine, alert evaluator, commute predictor)
- Wave D — data layer (routing, alerts, places, monitoring persistence)
- Wave E — application use cases (monitoring, trip creation, departure reminders)
- Wave F — all feature screens end-to-end
- Wave G — architecture hardening (ThresholdConfig, remote config interface, benchmark, StrictMode)
- Wave H — UI/UX redesign pass (compact layouts, icon-driven UI, design tokens, sticky Arm button)

---

## 3. What Is Not Yet Done

These are the remaining gaps before Play Store submission:

| Item | Status | Notes |
|------|--------|-------|
| Full accessibility TalkBack pass | 🟡 Partial | AlertScreen + LiveTripScreen done; remaining screens need content descriptions |
| Real-device field testing | 🔴 Not started | 30+ trips on physical Android hardware |
| Hosted privacy policy URL | 🔴 Blocker | Required for Play Store listing |
| Release keystore generation | 🔴 Not done | Rotate `MAPS_API_KEY` to release-restricted key first |
| Play Store submission | 🔴 Not started | Depends on policy URL + signed AAB |
| OEM battery exemption screens | 🟡 In progress | Content exists; Samsung/Xiaomi/Oppo guidance integration |

**Post-1.0 features** (Wave I, J, K) are planned in `PRODUCT_EXPANSION_ROADMAP.md` and are not part of this release.

---

## 3.1 Problem And Scope Reference

Use [PRODUCT_COMPARE_REFERENCE.md](PRODUCT_COMPARE_REFERENCE.md) for:

- the exact problem NearWake is trying to solve
- what the product is and is not
- current scope versus future scope
- the competitor feature matrix
- the technology stack and why those choices were made

---

## 4. What You Must Provide

### Required now

- `sdk.dir`

This is required for Android builds.

### Optional but strongly recommended

- `MAPS_API_KEY`

Enables Google Places destination search and Google Directions transit route preview / ETA refresh.

Without it:
- the app still builds and runs
- place search falls back to local sample results
- the app falls back to destination-only monitoring

With it:
- trip setup fetches a real route preview
- route snapshots are cached per trip id
- live trip and history screens show richer route context
- place search uses Google Places

### Needed for launch-readiness / field testing

- `SENTRY_DSN` — enables Sentry crash and non-fatal reporting
- `PRIVACY_POLICY_URL` — exposed via Android manifest metadata; required for Play Store disclosure

---

## 5. Exact File To Update

Create or update `local.properties` at the project root:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
SENTRY_DSN=
PRIVACY_POLICY_URL=
```

A template already exists at `local.properties.template`.

`MAPS_API_KEY` can also be passed as a Gradle property, but `local.properties` is the standard local-development path.

---

## 6. Which APIs To Enable

### Needed now

- `Directions API` — used by the Google transit routing provider
- `Places API` — used by the provider-backed destination search

### Likely needed for later waves

- `Maps SDK for Android` — needed if any live-map UI is ever added (not planned for v1.0)

The app runs without `Places API` by using the local fallback list. If you only want route previews, `Directions API` is the only critical one.

---

## 7. What Is Not Needed Right Now

You do not need any of these to build or run the app locally:

- backend URL
- Firebase configuration (`google-services.json`)
- server database connection
- OpenAI API key
- auth secrets
- Node `.env`

The app is offline-first. Firebase is planned for post-1.0 Wave K (family tracking, cloud sync, Wear OS). You only need `SENTRY_DSN` once you want real observability during field testing.

---

## 8. Current Behavior With And Without Keys

### If `MAPS_API_KEY` is set

- routing module uses the Google Directions transit provider
- place search uses Google Places Autocomplete
- trip setup shows route preview with stop list
- route snapshots are cached against trip ids
- live trip and trip summary show route details

### If `MAPS_API_KEY` is missing

- routing gracefully falls back (destination-only monitoring)
- place search uses the local fallback list
- trip setup shows destination-only behavior
- the app does not block the user from starting a trip
- the app remains fully buildable

---

## 9. Verification Commands

```powershell
.\gradlew.bat :domain:trip:test
.\gradlew.bat :domain:location:test
.\gradlew.bat :core:network:test
.\gradlew.bat :core:remoteconfig:test
.\gradlew.bat :data:routing:test
.\gradlew.bat :data:alerts:test
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:assembleRelease
```

To run all module tests in one pass:

```powershell
.\gradlew.bat test
```

---

## 10. Recommended Next Inputs

To keep development moving toward release:

1. Set `sdk.dir`
2. Add `MAPS_API_KEY` with `Directions API` and `Places API` enabled
3. Add `SENTRY_DSN` before field testing begins
4. Host a `PRIVACY_POLICY_URL` before Play Store submission
5. Generate a release keystore and rotate `MAPS_API_KEY` to a release-key-restricted credential

---

## 11. Short Answer

To build the app on your machine:

```
sdk.dir=<path-to-android-sdk>
```

To unlock route-aware behavior:

```
MAPS_API_KEY=<your-key>
```
Enable: `Directions API`, `Places API`.

To submit to Play Store:

- host a privacy policy
- generate a release keystore
- sign the release AAB
- complete the full TalkBack accessibility pass
- complete 30+ trip field tests on physical devices
