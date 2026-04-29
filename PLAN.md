# NearWake — Master Product & Implementation Plan

**Package:** `com.nearwake.app`
**Platform:** Android-first (Kotlin + Jetpack Compose)
**Last updated:** 2026-04-29

---

## Strategic Positioning

**The twelve-word promise:**
> Wake me before my stop. Tell me if you are not sure.

NearWake is the Android arrival assurance system — for commuters whether they sleep, read, scroll, work, or zone out. The confidence model is the entire differentiator: every competitor shows one threshold and hopes for the best. NearWake admits uncertainty, biases early, and tells the user why.

NearWake is a **trust app**, not a safety platform. No SOS, no emergency contacts, no 24/7 monitoring.

---

## Eight Pillars

| # | Pillar | Status |
|---|--------|--------|
| 1 | Depart on time | ✅ Done |
| 2 | Board right | ✅ Done |
| 3 | Ride awake or asleep | ✅ Done |
| 4 | Transfer confidently | ✅ Done |
| 5 | Arrive correctly | ✅ Done |
| 6 | Recover gracefully | ✅ Done |
| 7 | Walk the last mile | ✅ Done |
| 8 | Confirm arrival | ✅ Done |

---

## Current Development Status

### What is complete and pushed

| Area | Files / Modules | Notes |
|------|----------------|-------|
| **UI Modernization — all 9 phases** | `core:designsystem`, `core:ui`, all 11 screens | Dark-first design system, animated accent, PulseRing, all tokens |
| **Battery efficiency** | `TripMonitoringService`, `FusedLocationDataSource`, `TripMonitoringRuntime` | Geofence widens at <20%, screen-off 200m threshold, cellular suppress at <30% |
| **Wave A — Stage A/B notification channels** | `NotificationHelper.kt`, `TripMonitoringService.kt` | CHANNEL_APPROACH (gentle), CHANNEL_IMMINENT (HIGH + vibration 200/100/400ms) |
| **Wave A — Bias-early engine** | `AlertStageEvaluator.kt` | 1.0×/1.15×/1.25× multipliers by confidence + mode |
| **Wave A — OEM Reliability** | `OemReliability.kt`, `SettingsScreen.kt` | Samsung/Xiaomi/OPPO/Pixel vendor-specific guidance |
| **Wave A — One-tap re-arm** | `HomeViewModel.kt`, `HomeScreen.kt`, `:application:trip` | Skips setup, reuses last trip settings, now routed through application use cases |
| **Wave C — Per-leg confidence** | `RouteSignalQuality.kt`, `Stop.kt`, `TransferMonitor.kt`, `TransferProgressCard.kt` | Medium/Low signal chip per transfer leg |
| **Wave C — Transfer notifications** | `NotificationHelper.kt` | Transfer uses CHANNEL_APPROACH, boarding warning uses CHANNEL_IMMINENT |
| **Wave E — Diagnostics "why fired"** | `DiagnosticsViewModel.kt`, `DiagnosticsScreen.kt`, `AlertOrchestrator.kt` | Enriched payloads: confidence, stage, distance, ETA per alert |
| **Wave E — CSV export** | `TripExporter.kt`, `HistoryViewModel.kt`, `HistoryScreen.kt` | Export button → share sheet with CSV content |
| **Wave D — domain:commute** | `CommutePrediction.kt`, `CommutePredictionEngine.kt` | Cluster-based prediction from trip history |
| **Wave D — core:database v5** | `CommutePredictionEntity.kt`, `CommutePredictionDao.kt`, `NearWakeDatabase.kt`, `DatabaseModule.kt` | MIGRATION_4_5, new commute_predictions table |
| **Wave D — data:patterns** | `CommutePredictionRepository.kt` | Refresh predictions from TripDao + SavedPlaceDao |
| **Wave D — feature:departure** | `DepartureViewModel.kt`, `DepartureReminderScreen.kt` | Today's predictions, "Start trip" CTA |
| **Wave D — departure reminder scheduling** | `DepartureReminderScheduler.kt`, `AlertReminderReceiver.kt`, `BootReceiver.kt` | Flexible leave-by reminders, dedicated channel, boot reschedule |
| **Wave E — Google Places search** | `GooglePlacesSearchRepository.kt`, `LocationModule.kt`, `PlaceSearchScreen.kt` | Provider-backed destination search with local fallback |
| **Wave E — persisted theme modes** | `ThemeRepository.kt`, `MainActivity.kt`, `SettingsScreen.kt` | `SYSTEM`, `LIGHT`, `DARK` modes persisted in DataStore |
| **Slice 5 — field-test diagnostics exports** | `DiagnosticsViewModel.kt`, `DiagnosticsScreen.kt` | Event timestamps, permission snapshot, environment snapshot, build/device context |
| **Wave F — architecture hardening slice A** | `:application:monitoring`, `:ports:monitoring` | Start/stop monitoring now flows through a monitoring port |
| **Wave F — architecture hardening slice B** | `:application:trip`, `:ports:persistence`, `TripLifecycleStore` | Trip start and re-arm moved out of screen-level orchestration |
| **Wave F — configurable alert triggers** | `domain:trip`, `core:database`, `core:datastore`, `feature:tripsetup`, `feature:settings`, `feature:history`, `data:alerts` | Users can choose `Time`, `Distance`, or `Both`, with persisted defaults and trip/history/export coverage |

### What is in progress

| Item | Status |
|------|--------|
| Production architecture hardening | In progress — cancel/complete/recovery flows and read-side cleanup still need to move toward the application/ports boundary |
| Field testing and release validation | In progress — repo-side support is in place, but real-device trips and final store assets still remain |

### What is not started yet

| Item | Priority | Notes |
|------|----------|-------|
| **Architecture read-side cleanup** | High | Pull more feature screen state away from direct DAO/entity assembly |
| **Runtime/service slimming** | High | Reduce `TripMonitoringService` responsibility so it owns runtime mechanics more than product workflow |
| **Accessibility audit** | Medium | TalkBack pass, 48dp targets, 4.5:1 contrast in both modes |
| **Lock-screen widget** | Low | AppWidgetProvider + RemoteViews (defer to post-1.0 if notification coverage sufficient) |
| **Manual field-test execution** | Pre-launch | 30+ real trips, 10+ screen-off, 10+ tunnel/poor signal |
| **Final store-submission execution** | Pre-launch | Final screenshots/assets, hosted privacy-policy URL, signing secrets, Play Console submission |

---

## Light / Dark Mode — Implementation Summary

This work is complete.

Delivered behavior:

- the app now supports `SYSTEM`, `LIGHT`, and `DARK` appearance modes
- the user can change the mode from Settings → Appearance
- the selected mode persists through DataStore
- `MainActivity` resolves the chosen mode and applies it through `NearWakeTheme`
- shared design-system colors now support both dark and light surfaces

Acceptance status:

1. Theme selection is available in Settings
2. System preference is respected by default
3. User override persists across app restarts
4. Theme changes apply without restart
5. Debug and release builds pass with the current theme implementation

---

## Wave Checklist

### Wave A — Trust Core ✅ Complete
- [x] Confidence chip on LiveTrip
- [x] Bias-early engine (1.0×/1.15×/1.25×)
- [x] Bias-early banner (biasEarlyMessage in TrustPresentation)
- [x] Underground-mode chip
- [x] Recovery screen as first-class
- [x] OEM Reliability page
- [x] One-tap re-arm on Home
- [x] Stage A (APPROACH) distinct notification channel — gentle, no popup
- [x] Stage B (IMMINENT) stronger tone + heads-up + haptic pulse

### Wave B — Dual Mode + Lock Screen ✅ Engine done / 1 item pending
- [x] Sleep / Active Mode enum + engine behavior
- [x] Haptic preamble patterns (multi-pulse vibration for Sleep mode)
- [x] Confidence-based bias higher in Sleep mode
- [ ] Accessibility audit — TalkBack, 48dp targets, 4.5:1 contrast (both modes)
- [ ] Lock-screen widget (defer if notification coverage sufficient)

### Wave C — Route + Boarding ✅ Complete
- [x] BoardingValidator (direction check, warning notification)
- [x] TransferMonitor (UI strip, CHANNEL_APPROACH notifications)
- [x] Per-leg confidence chip in transfer strip (RouteSignalQuality on Stop)

### Wave D — Journey Extension ✅ Complete
- [x] `:domain:commute` — CommutePrediction, CommutePredictionEngine
- [x] `:core:database` v5 — CommutePredictionEntity, DAO, MIGRATION_4_5
- [x] `:data:patterns` — CommutePredictionRepository
- [x] `:feature:departure` — DepartureReminderScreen, DepartureViewModel
- [x] **Room KSP build failure fixed** — schema v5 generated, full build passes
- [x] Departure reminder notification scheduling — "Leave by HH:MM for Destination"
- [x] Departure screen wired into NavGraph and reachable in-app

### Wave E — Polish and Public Beta 🟡 Manual validation remaining
- [x] Enriched diagnostics "why fired" — confidence, stage, distance, ETA per event
- [x] CSV trip log export with share sheet
- [x] Light / dark / system theme support
- [x] Google Places-backed search with local fallback
- [x] Field-test protocol and runbook
- [x] Play Store listing draft and release-prep docs
- [ ] Real-device field testing
- [ ] Final screenshots/assets and store submission execution

### Wave F — Production Architecture Hardening 🟡 In progress
- [x] `:application:monitoring` + `:ports:monitoring`
- [x] `:application:trip` + `:ports:persistence`
- [x] monitoring start/stop moved behind a monitoring gateway
- [x] trip start and one-tap re-arm moved into application use cases
- [x] early-alert trigger preferences now flow through domain rules, persistence, settings, runtime, history, and export
- [ ] cancel / complete / recovery workflow migration
- [ ] read-side cleanup away from direct DAO/entity assembly
- [ ] slimmer runtime/service responsibilities

---

## Key File Map

| File | Purpose |
|------|---------|
| `domain/trip/engine/AlertStageEvaluator.kt` | Stage transitions + bias multiplier |
| `domain/trip/engine/TripEngine.kt` | Core state machine |
| `domain/trip/engine/TransferMonitor.kt` | Transfer checkpoints + RouteSignalQuality passthrough |
| `domain/trip/engine/BoardingValidator.kt` | Direction check on boarding |
| `domain/routing/model/Stop.kt` | Stop with `signalQuality: RouteSignalQuality` |
| `domain/routing/model/RouteSignalQuality.kt` | Per-leg signal quality enum (HIGH/DEGRADED/OFFLINE) |
| `domain/commute/CommutePredictionEngine.kt` | Cluster trip history → CommutePrediction list |
| `data/alerts/TripMonitoringService.kt` | Foreground service — location + geofence loop |
| `data/alerts/AlertOrchestrator.kt` | Stage C alert with sound + vibration + diagnostics payload |
| `data/alerts/NotificationHelper.kt` | CHANNEL_APPROACH, CHANNEL_IMMINENT, CHANNEL_ALERT builders |
| `data/patterns/CommutePredictionRepository.kt` | Refresh predictions from trip history |
| `core/database/NearWakeDatabase.kt` | Room DB v5 with CommutePredictionEntity |
| `core/database/di/DatabaseModule.kt` | Migrations 1→5, CommutePredictionDao provision |
| `core/designsystem/NearWakeColors.kt` | Color tokens for dark and light appearance modes |
| `core/designsystem/NearWakeTheme.kt` | Theme composition and runtime appearance resolution |
| `feature/livetrip/LiveTripScreen.kt` | Trip monitoring UI with animated accent |
| `feature/departure/DepartureReminderScreen.kt` | Today's predicted departures |
| `feature/history/HistoryScreen.kt` | History list + Export button |
| `feature/settings/SettingsScreen.kt` | Settings, OEM reliability guidance, and Appearance controls |
| `app/HomeScreen.kt` | Home with one-tap re-arm |

---

## Non-Negotiable Principles

1. Confidence-first — every prediction shows confidence
2. Bias-early — when in doubt, alert earlier and label why
3. Offline-dignified — underground is a named mode with its own UI
4. No ads, no paywalls in the alert path — ever
5. No account required for core value
6. Battery-honest — expose what we spend
7. OEM-aware — vendor-specific battery guidance
8. One-tap re-arm — returning commuters never re-enter a trip
9. Privacy-respecting — local persistence first, no continuous cloud location
10. Fail visible, not silent — degraded permissions shown to user

## Post-1.0 Deferred

- Wear OS relay
- Commute analytics dashboard
- Optional backend / cloud sync
- Second-device companion
- Cloud sync
- Ads (never)
