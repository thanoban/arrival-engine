# NearWake Development Start

This is the active development-start tracker for the NearWake Android app.

Use this file when beginning a new implementation thread. It captures the current build-verified state, the first development slices to work on, and the commands that should be run before each slice is considered finished.

## Current Verified Baseline

Workspace:

```text
D:\PROJECTS\Startup\LocationTracker
```

Project:

```text
NearWake
Android app
Package: com.nearwake.app
```

Verified on 2026-04-26 with:

```powershell
.\gradlew.bat :domain:trip:test :core:network:test :data:routing:test :data:alerts:test :app:assembleDebug
```

Result:

```text
BUILD SUCCESSFUL
```

## Development Rules For This Repo

- Use `.\gradlew.bat`, not global `gradle`.
- Keep each finished part small and independently verifiable.
- Commit and push each finished part separately.
- Do not include generated/editor folders in commits.
- Preserve the existing module structure: `app`, `core`, `domain`, `data`, and `feature`.
- Add production features through the correct layer, not as screen-only shortcuts.
- Keep docs honest: separate implemented, partial, and planned work.

Known generated/editor folders currently visible in git status:

```text
.vscode/
build-logic/convention/bin/
domain/location/bin/
domain/routing/bin/
domain/trip/bin/
```

Do not stage these unless a future task explicitly makes them intentional project files.

## Current Product State

NearWake is already beyond scaffold stage. The app currently has:

- Compose app shell and navigation.
- Onboarding and startup gating.
- Home, permissions, place search, trip setup, live trip, alert, recovery, walk finish, companion, history, settings, and diagnostics screens.
- Room database with persisted places, trips, trip sessions, route snapshots, alert events, diagnostics events, and commute predictions.
- DataStore preferences.
- Foreground trip monitoring service.
- WorkManager recovery worker.
- Geofence and activity-recognition monitoring foundations.
- Alert stage evaluation with confidence and bias-early behavior.
- Google Directions transit routing when `MAPS_API_KEY` is configured.
- Destination-only fallback when routing is unavailable.
- CSV trip export from history.
- Persisted light, dark, and system appearance modes.
- Flexible leave-by reminders from learned commute predictions.

## Main Open Development Slices

### Slice 1 - Wire Departure Reminders Into The App

Status:

```text
Done
```

Already present:

- `:domain:commute`
- `:data:patterns`
- `:feature:departure`
- Room database version 5 with `commute_predictions`
- `DepartureReminderScreen`
- `DepartureViewModel`

Completed:

- Add `:feature:departure` dependency to `:app`.
- Add a `Departure` route to `NearWakeRoute`.
- Wire `DepartureReminderScreen` into `NearWakeNavHost`.
- Add a visible entry point from Home or Settings.
- Ensure the "Start trip" CTA navigates to the existing trip setup flow.

Remaining departure work now belongs to Slice 4:

- Add reminder notification scheduling.
- Add any focused tests needed for scheduling behavior.

Verification for this slice:

```powershell
.\gradlew.bat :domain:commute:test :data:patterns:test :feature:departure:testDebugUnitTest :app:assembleDebug
```

If a module has no tests yet, `assembleDebug` must still pass.

### Slice 2 - Replace Sample Place Search With Real Provider Search

Status:

```text
Done
```

Completed:

- Added a proper place-search data path.
- Keep saved places persisted through `SavedPlaceDao`.
- Use Google Places behind a domain repository so the screen is not tied directly to SDK calls.
- Keep destination-only fallback behavior if provider search fails.

Implemented structure:

```text
domain/location
data/location
feature/places
```

External setup likely needed:

- Google API key.
- Places API enabled.
- Key restrictions appropriate for Android usage.

Verification for this slice:

```powershell
.\gradlew.bat :feature:places:testDebugUnitTest :app:assembleDebug
```

### Slice 3 - Implement Light / Dark Theme Support

Status:

```text
Done
```

Completed:

- Added light color roles and a light Material color scheme.
- Added persisted theme mode: `SYSTEM`, `LIGHT`, `DARK`.
- Resolved the actual theme in `MainActivity`.
- Added an Appearance section in Settings.
- Updated shared UI components to use semantic theme colors.
- Added focused DataStore coverage for theme mode persistence.

Verification for this slice:

```powershell
.\gradlew.bat :core:designsystem:testDebugUnitTest :core:datastore:testDebugUnitTest :feature:settings:testDebugUnitTest :app:assembleDebug
```

If a module has no tests yet, `assembleDebug` must still pass.

### Slice 4 - Add Departure Reminder Notification Scheduling

Status:

```text
Done
```

Completed:

- Scheduled "Leave by HH:MM for Destination" reminders from learned commute predictions.
- Used flexible `AlarmManager` windows for clock-time reminders without exact-alarm permission.
- Kept battery behavior honest and visible in the Departure screen.
- Added a dedicated departure notification channel.
- Added a persisted departure-reminder enabled flag.
- Added boot rescheduling when reminders are enabled.
- Added focused planner and DataStore tests.

Verification for this slice:

```powershell
.\gradlew.bat :core:datastore:testDebugUnitTest :data:patterns:test :data:alerts:test :feature:departure:testDebugUnitTest :app:assembleDebug
```

### Slice 5 - Field Test And Release Hardening

Status:

```text
In progress
```

Target:

- Run real trips with screen on, screen off, tunnels or low signal, and transfers.
- Confirm notification behavior on target Android versions.
- Check permission flows.
- Check battery impact.
- Prepare privacy and Play Store disclosure material.

Completed so far inside this slice:

- Replaced placeholder permission status with live Android permission checks.
- Added staged permission requests for notifications, precise location, activity recognition, and background location.
- Added a direct app-settings path for denied or settings-only cases.
- Added focused permission-readiness unit coverage.
- Added a dedicated release-readiness checklist covering field tests, permission disclosures, privacy notes, and release gates.
- Added a dedicated field-test runbook with a per-trip logging template and blocker criteria.
- Added a privacy and permission-disclosure draft aligned to the current app behavior.
- Added a Play Store listing draft covering safe release copy, screenshot prep, and claims to avoid before field testing.

Still remaining in this slice:

- Real trip field testing across the target device scenarios.
- Notification timing validation on real devices and Android versions.
- Final device-validated release wording and store assets.

## Recommended First Implementation Order

1. Run field testing and release hardening.

This order keeps work useful immediately while reducing risk. Departure reminders are reachable and scheduled, destination search has a provider-backed path with local fallback, and appearance mode is configurable from Settings.

## Standard Verification Set

Use this after broad app-level changes:

```powershell
.\gradlew.bat :domain:trip:test :core:network:test :data:routing:test :data:alerts:test :app:assembleDebug
```

Use this before claiming release-build readiness:

```powershell
.\gradlew.bat :app:assembleRelease
```

Use this if Gradle or Windows file locks behave strangely:

```powershell
.\gradlew.bat --stop
```

Then rerun the focused verification command.

## Done Criteria For Each Development Part

A part is done only when:

- The intended app behavior is reachable from navigation or documented as intentionally internal.
- The implementation follows the module boundaries.
- Persisted data and external API requirements are documented.
- Focused verification passes.
- Git status is reviewed.
- Only relevant source/docs files are staged.
- The part is committed and pushed separately.
