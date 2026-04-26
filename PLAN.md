# NearWake — Master Product & Implementation Plan

**Package:** `com.nearwake.app`
**Platform:** Android-first (Kotlin + Jetpack Compose)
**Last updated:** 2026-04-26

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
| 1 | Depart on time | 🟡 In progress — all modules built and passing, Worker + nav wiring pending |
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
| **Wave A — One-tap re-arm** | `HomeViewModel.kt`, `HomeScreen.kt` | Skips setup, reuses last trip settings, starts service directly |
| **Wave C — Per-leg confidence** | `RouteSignalQuality.kt`, `Stop.kt`, `TransferMonitor.kt`, `TransferProgressCard.kt` | Medium/Low signal chip per transfer leg |
| **Wave C — Transfer notifications** | `NotificationHelper.kt` | Transfer uses CHANNEL_APPROACH, boarding warning uses CHANNEL_IMMINENT |
| **Wave E — Diagnostics "why fired"** | `DiagnosticsViewModel.kt`, `DiagnosticsScreen.kt`, `AlertOrchestrator.kt` | Enriched payloads: confidence, stage, distance, ETA per alert |
| **Wave E — CSV export** | `TripExporter.kt`, `HistoryViewModel.kt`, `HistoryScreen.kt` | Export button → share sheet with CSV content |
| **Wave D — domain:commute** | `CommutePrediction.kt`, `CommutePredictionEngine.kt` | Cluster-based prediction from trip history |
| **Wave D — core:database v5** | `CommutePredictionEntity.kt`, `CommutePredictionDao.kt`, `NearWakeDatabase.kt`, `DatabaseModule.kt` | MIGRATION_4_5, new commute_predictions table |
| **Wave D — data:patterns** | `CommutePredictionRepository.kt` | Refresh predictions from TripDao + SavedPlaceDao |
| **Wave D — feature:departure** | `DepartureViewModel.kt`, `DepartureReminderScreen.kt` | Today's predictions, "Start trip" CTA |

### What is in progress (ready to push)

| Item | Status |
|------|--------|
| Wave D modules (`domain:commute`, `data:patterns`, `feature:departure`, `core:database` v5) | Build passes — not yet committed/pushed |
| `DepartureReminderScreen` nav wiring | Not yet wired into NavGraph |
| Departure reminder notification (AlarmManager) | Not yet implemented |

### What is not started yet

| Item | Priority | Notes |
|------|----------|-------|
| **Light/Dark theme** | High | See full plan below — adds `lightColorScheme`, `ThemeMode` DataStore key, Settings toggle |
| **DepartureReminder Worker** | Medium | AlarmManager or WorkManager to fire notification before predicted departure |
| **Google Places Autocomplete** | Medium | Replace 3 hardcoded samples in `PlaceSearchViewModel` with real API |
| **Accessibility audit** | Medium | TalkBack pass, 48dp targets, 4.5:1 contrast in light mode |
| **Lock-screen widget** | Low | AppWidgetProvider + RemoteViews (defer to post-1.0 if notification coverage sufficient) |
| **Field test protocol** | Pre-launch | 30+ real trips, 10+ screen-off, 10+ tunnel/poor signal |
| **Play Store listing** | Pre-launch | Positioning, screenshots, commitments copy |

---

## Light / Dark Mode — Full Implementation Plan

### Decision

The app was built dark-first. Light mode is now a first-class requirement. The approach:

- **Dark mode:** existing color tokens, unchanged
- **Light mode:** a parallel set of background/surface/text tokens, same semantic accent colors
- **Default:** follow system setting (`isSystemInDarkTheme()`)
- **User override:** Light / Dark / System — three-chip selector in Settings → Appearance
- **Persistence:** DataStore key `theme_mode` with values `SYSTEM`, `LIGHT`, `DARK`
- **Theme resolved at:** `MainActivity` (read DataStore → pass `darkTheme: Boolean` to `NearWakeTheme`)

---

### Token Map — Dark vs Light

| Token | Dark | Light |
|-------|------|-------|
| `BgBase` | `#0A0B0D` | `#F8F9FA` |
| `BgSurface` | `#111316` | `#FFFFFF` |
| `BgElevated` | `#181B1F` | `#F1F3F5` |
| `BgHighest` | `#1F2327` | `#E8EAED` |
| `BorderSubtle` | `#22262B` | `#E2E5E9` |
| `BorderDefault` | `#2C3137` | `#CDD1D6` |
| `TextPrimary` | `#F5F6F7` | `#0D0E10` |
| `TextSecondary` | `#A8ADB4` | `#4A5056` |
| `TextTertiary` | `#6E747C` | `#7A8087` |
| `TextDisabled` | `#434950` | `#B0B5BB` |
| `SafeBase` | `#22C55E` | `#22C55E` (same) |
| `SafeSoft` | `#0F2A1A` | `#DCFCE7` |
| `SafeBorder` | `#1A4A2E` | `#86EFAC` |
| `MonitoringBase` | `#38BDF8` | `#0284C7` (darkened for contrast on white) |
| `MonitoringSoft` | `#0A2A3A` | `#E0F2FE` |
| `MonitoringBorder` | `#164C66` | `#7DD3FC` |
| `ApproachBase` | `#F59E0B` | `#D97706` (darkened for contrast) |
| `ApproachSoft` | `#2A1F0A` | `#FEF3C7` |
| `ApproachBorder` | `#4C3A14` | `#FCD34D` |
| `AlertBase` | `#EF4444` | `#DC2626` (darkened for contrast) |
| `AlertIntense` | `#FF4444` | `#DC2626` |
| `AlertSoft` | `#2A0F0F` | `#FEE2E2` |
| `AlertBorder` | `#661818` | `#FCA5A5` |

Semantic accent colors (SafeBase/MonitoringBase/ApproachBase/AlertBase) are used directly on both light and dark surfaces. The light-mode variants of the "active" colors (Monitoring/Approach/Alert) are slightly darkened to maintain 4.5:1 contrast on white.

---

### Architecture Changes

**1. `NearWakeColors.kt`** — split into `NearWakeDarkColors` and `NearWakeLightColors` objects, plus a `NearWakeColors` resolved instance that references whichever is active via `CompositionLocal`.

**2. `LocalNearWakeColors` CompositionLocal** — provides the correct color set. Components read colors from here instead of hardcoding `NearWakeColors.X`. Existing references continue to work because `NearWakeColors` stays as the accessor.

**3. `NearWakeTheme.kt`** — `NearWakeTheme(darkTheme: Boolean)` already has this parameter (currently ignored). Wire it: if `darkTheme = true` use dark color scheme + dark tokens, else use light.

**4. `ThemeMode.kt`** (new, in `core:datastore`) — enum `SYSTEM`, `LIGHT`, `DARK` + DataStore key.

**5. `ThemeRepository.kt`** (new, in `core:datastore`) — `observeThemeMode(): Flow<ThemeMode>`, `setThemeMode(mode)`.

**6. `MainActivity.kt`** — collect `ThemeRepository.observeThemeMode()`, resolve `darkTheme: Boolean` (`DARK` → true, `LIGHT` → false, `SYSTEM` → `isSystemInDarkTheme()`), pass to `NearWakeTheme`.

**7. `SettingsScreen.kt`** — add "Appearance" section with three `NearWakeSelectableChip`s: System / Light / Dark. Write to `ThemeRepository` on tap.

**8. `SettingsViewModel.kt`** — inject `ThemeRepository`, expose `themeMode: ThemeMode` in state, add `setThemeMode(mode)`.

---

### Files to Change

| File | Change |
|------|--------|
| `core/designsystem/NearWakeColors.kt` | Add `NearWakeLightColors`, `NearWakeDarkColors`, `LocalNearWakeColors` CompositionLocal |
| `core/designsystem/NearWakeTheme.kt` | Wire `darkTheme` param to choose color set + M3 color scheme |
| `core/datastore/ThemeMode.kt` | New enum + DataStore key constant |
| `core/datastore/ThemeRepository.kt` | New repository: observe + set theme mode |
| `app/src/main/.../MainActivity.kt` | Collect theme mode, resolve darkTheme, pass to NearWakeTheme |
| `feature/settings/SettingsViewModel.kt` | Inject ThemeRepository, expose themeMode in state |
| `feature/settings/SettingsScreen.kt` | Add "Appearance" section with System/Light/Dark chip row |

---

### What Does NOT Change

- Navigation routes — unchanged
- ViewModel state contracts — unchanged
- Room schema — unchanged
- All screens — they inherit theme via `MaterialTheme` automatically
- Alert screen — `AlertIntense` used directly as background; in light mode it's `#DC2626` (still unmistakable red)
- Accent animation on LiveTripScreen — unchanged, semantic colors work on both backgrounds

---

### Acceptance Criteria for Light/Dark

1. All screens visually correct in light mode — no dark text on dark bg, no light text on light bg
2. 4.5:1 contrast ratio on all text in both modes (AAA on Alert screen)
3. System preference respected by default
4. User override persists across app restarts
5. Theme switch in Settings applies immediately without restart
6. No hardcoded `NearWakeColors.BgBase` etc. — all background/surface/text tokens resolved via `LocalNearWakeColors`
7. Debug and release builds pass

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

### Wave D — Journey Extension 🟡 In progress
- [x] `:domain:commute` — CommutePrediction, CommutePredictionEngine
- [x] `:core:database` v5 — CommutePredictionEntity, DAO, MIGRATION_4_5
- [x] `:data:patterns` — CommutePredictionRepository
- [x] `:feature:departure` — DepartureReminderScreen, DepartureViewModel
- [x] **Room KSP build failure fixed** — schema v5 generated, full build passes
- [ ] Departure reminder notification (AlarmManager) — "Leave by HH:MM for Destination"
- [ ] Wire DepartureReminderScreen into NavGraph

### Wave E — Polish and Public Beta 🟡 Partial
- [x] Enriched diagnostics "why fired" — confidence, stage, distance, ETA per event
- [x] CSV trip log export with share sheet
- [ ] **Light / Dark Mode** — see full plan above
- [ ] Google Places Autocomplete (replace stub in PlaceSearchViewModel)
- [ ] Full field test protocol (30+ real trips)
- [ ] Play Store listing

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
| `core/designsystem/NearWakeColors.kt` | Color tokens — dark + **light variants needed** |
| `core/designsystem/NearWakeTheme.kt` | Theme composition — **wire darkTheme param** |
| `feature/livetrip/LiveTripScreen.kt` | Trip monitoring UI with animated accent |
| `feature/departure/DepartureReminderScreen.kt` | Today's predicted departures |
| `feature/history/HistoryScreen.kt` | History list + Export button |
| `feature/settings/SettingsScreen.kt` | Settings + OEM reliability + **Appearance section needed** |
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
