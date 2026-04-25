# NearWake UI Modernization Plan

**Status:** Planning complete — ready for implementation
**Last updated:** 2026-04-24

---

## Context

The backend, data layer, navigation, and ViewModels are complete. This pass redesigns the **presentation layer only** — no architecture changes, no ViewModel contract changes, no persistence changes.

The app must feel:
- **Calm** when idle / safe
- **Alive and precise** when monitoring
- **Unmistakable** when alerting

Visual references: Citymapper's clarity, Linear's restraint, Strava's instrument-panel precision. The app is a **trust tool** — every pixel serves status communication.

---

## Hard Constraints

1. Navigation routes must stay intact
2. ViewModel state contracts must not change (UI-only derived `val`s can be added in screen files only)
3. Room entities, DAOs, DataStore keys, repository contracts are frozen
4. No fake/placeholder data — show real empty states
5. Accessibility must not regress (48dp touch targets, 4.5:1 contrast min, AAA on Alert screen)
6. Dark-first — no light theme work this pass
7. No new heavy dependencies — Inter via Google Fonts Compose provider only
8. Build must stay green after every phase

---

## Implementation Order

| Phase | Target | Priority |
|-------|--------|----------|
| 1 | `core:designsystem` — tokens + theme | Critical |
| 2 | `core:ui` — shared components | Critical |
| 3 | `HomeScreen` | Critical |
| 4 | `TripSetupScreen` | Critical |
| 5 | `LiveTripScreen` | Critical |
| 6 | `AlertScreen` + `RecoveryScreen` | Critical |
| 7 | `HistoryScreen`, `TripSummaryScreen` | High |
| 8 | `SettingsScreen`, `DiagnosticsScreen` | High |
| 9 | `OnboardingScreen`, `PermissionsScreen`, `PlaceSearchScreen` | Medium |

Phases 1–6 are product-defining. Phases 7–9 inherit the language established earlier.

---

## Phase 1 — `core:designsystem`

**Files to create/update:** `NearWakeColors.kt`, `NearWakeTypography.kt`, `NearWakeSpacing.kt`, `NearWakeMotion.kt`, `NearWakeTheme.kt`

### Color Tokens (Dark-First)

```kotlin
// Neutral surfaces
val BgBase           = Color(0xFF0A0B0D)
val BgSurface        = Color(0xFF111316)
val BgElevated       = Color(0xFF181B1F)
val BgHighest        = Color(0xFF1F2327)
val BorderSubtle     = Color(0xFF22262B)
val BorderDefault    = Color(0xFF2C3137)

// Text
val TextPrimary      = Color(0xFFF5F6F7)
val TextSecondary    = Color(0xFFA8ADB4)
val TextTertiary     = Color(0xFF6E747C)
val TextDisabled     = Color(0xFF434950)

// Safe / Armed state
val SafeBase         = Color(0xFF22C55E)
val SafeSoft         = Color(0xFF0F2A1A)
val SafeBorder       = Color(0xFF1A4A2E)

// Monitoring state (cool cyan)
val MonitoringBase   = Color(0xFF38BDF8)
val MonitoringSoft   = Color(0xFF0A2A3A)
val MonitoringBorder = Color(0xFF164C66)

// Approaching state (amber)
val ApproachBase     = Color(0xFFF59E0B)
val ApproachSoft     = Color(0xFF2A1F0A)
val ApproachBorder   = Color(0xFF4C3A14)

// Alert state (unmistakable red)
val AlertBase        = Color(0xFFEF4444)
val AlertIntense     = Color(0xFFFF4444)   // Alert screen only
val AlertSoft        = Color(0xFF2A0F0F)
val AlertBorder      = Color(0xFF661818)
```

### Material 3 ColorScheme Mapping

| M3 Role | Token |
|---------|-------|
| `background` | `BgBase` |
| `surface` | `BgSurface` |
| `surfaceVariant` | `BgElevated` |
| `onBackground`, `onSurface` | `TextPrimary` |
| `onSurfaceVariant` | `TextSecondary` |
| `primary` | context via `LocalStateAccent` |
| `outline` | `BorderDefault` |
| `outlineVariant` | `BorderSubtle` |
| `error` | `AlertBase` |

### Contextual Primary Color — `LocalStateAccent`

There is no single brand primary. The accent is determined by screen context:

| Screen / context | Accent |
|---|---|
| Home, Trip Setup | `SafeBase` |
| Live Trip — monitoring | `MonitoringBase` |
| Live Trip — approaching | `ApproachBase` |
| Alert (Stage C) | `AlertIntense` |
| Recovery | `ApproachBase` |
| Settings, History, Diagnostics | `MonitoringBase` |

### Typography — Inter (Google Fonts)

```kotlin
object NearWakeType {
    val DisplayXL = TextStyle(fontSize = 64.sp, lineHeight = 68.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
    val DisplayL  = TextStyle(fontSize = 48.sp, lineHeight = 54.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
    val Headline  = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.SemiBold)
    val Title     = TextStyle(fontSize = 18.sp, lineHeight = 24.sp, fontWeight = FontWeight.SemiBold)
    val BodyL     = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal)
    val Body      = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Normal)
    val Label     = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium)
}
```

All numeric data must use tabular figures: `fontFeatureSettings = "tnum"`. Provide `NumericText(text, style)` wrapper.

### Spacing

```kotlin
object Spacing {
    val xxs = 2.dp;  val xs = 4.dp;   val sm = 8.dp
    val md = 12.dp;  val lg = 16.dp;  val xl = 20.dp
    val xxl = 24.dp; val xxxl = 32.dp; val huge = 40.dp
    val hero = 48.dp; val massive = 64.dp
}
```

### Radius

```kotlin
object Radius {
    val sm = 8.dp    // inputs, chips
    val md = 12.dp   // default cards
    val lg = 16.dp   // hero cards
    val xl = 24.dp   // full-bleed surfaces
    // pill: RoundedCornerShape(50)
}
```

### Motion

```kotlin
object Motion {
    val Fast   = 150  // tap, chip select
    val Base   = 220  // card entrance, transitions
    val Slow   = 400  // state color change
    val EasingStandard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val EasingEmphasis = CubicBezierEasing(0.3f, 0f, 0f, 1f)
}
```

### Elevation Rule (No Drop Shadows)

On dark surfaces, drop shadows look muddy. Use `Modifier.nearWakeRaise(level: Int)` — lighter background + 1px border of `BorderSubtle`/`BorderDefault`. No `elevation` parameter.

### Composition Locals

```kotlin
val LocalStateAccent = compositionLocalOf { MonitoringBase }
val LocalSpacing     = compositionLocalOf { Spacing }
val LocalRadius      = compositionLocalOf { Radius }
```

---

## Phase 2 — `core:ui` Shared Components

All components read tokens from `LocalStateAccent`, `LocalSpacing`, `LocalRadius`. No hardcoded values.

| Component | Spec |
|-----------|------|
| `NearWakeScaffold` | 56dp top bar, `BgBase`, no elevation, optional `BorderSubtle` bottom border |
| `PrimaryButton` | Filled, `LocalStateAccent`, 56dp height, `Radius.md` |
| `SecondaryButton` | Outlined, `BorderDefault` border, `TextPrimary` label |
| `TextButton` | Minimal, `TextSecondary` label |
| `SurfaceCard` | `BgSurface` + 1px `BorderSubtle`, `Radius.md`, `Spacing.lg` padding |
| `ElevatedCard` | `BgElevated` + 1px `BorderDefault`, `Radius.lg`, `Spacing.xl` padding |
| `HeroCard(accent)` | `Radius.lg`, `accent.copy(alpha=0.08f)` bg tint + 1px `accent.copy(alpha=0.3f)` border |
| `StateChip(state)` | States: Safe / Monitoring / Approaching / Alert / Neutral — soft bg + border + base text |
| `SelectableChip` | Toggle chip for lead-time / intensity selection |
| `StatusRow` | Horizontal row of small `StateChip`s, 8dp gap |
| `SectionHeader` | `Label`, `TextTertiary`, UPPERCASE, 0.5sp letter spacing |
| `NumericText` | `Text` with `fontFeatureSettings = "tnum"` |
| `PulseRing` | 2–3 concentric rings, 2s infinite cycle, scale 1.0→1.15, alpha 0.4→0.0 |

**Verification:** `:core:ui` compiles. Write `NearWakeUiPreviews.kt` showing each component dark.

---

## Phase 3 — `HomeScreen`

**Accent:** `SafeBase`

**Layout top to bottom:**
1. Top bar — app wordmark left (`Label`), History + Settings icons right. No title.
2. Hero greeting — `Headline` greeting (time-of-day aware optional) + `StateChip` system health. If permissions degraded: `StateChip("Permissions needed", Approaching)` → tap to permissions flow.
3. Destination hero card — `HeroCard(SafeBase)`, min 96dp, left: location pin in `SafeBase`, center: "Set destination" `Title`, right: chevron. Tap → Place Search.
4. **One-tap re-arm** — if last trip exists: `PrimaryButton("Re-arm [destination]")` below hero card.
5. Recent trips — `SectionHeader("RECENT TRIPS")` + max 5 `SurfaceCard` rows (destination `Title`, route `Body`, timestamp + outcome `StateChip`). "View all" text button → History.
6. Empty state — single `SurfaceCard` with `Body` "Your trips will appear here after your first arrival."

**Entrance motion:** greeting fades + translates 8dp up (`Motion.Base`), destination card 60ms later, trips stagger 40ms each.

---

## Phase 4 — `TripSetupScreen`

**Accent:** `SafeBase`

**Layout:**
1. Top bar — "Set up trip", back arrow.
2. Destination section — `ElevatedCard` if place selected (name `Title`, address `Body`, "Change" text button); `SurfaceCard` CTA if not.
3. Route preview — If `routeSnapshot` exists: `ElevatedCard` with `SectionHeader("ROUTE")`, horizontal leg chips, `NumericText` total duration, `StateChip("Route-aware monitoring", Monitoring)`. If not: `SurfaceCard` with `StateChip("Destination-only mode", Approaching)` + `Body` explanation.
4. Lead time — `SectionHeader("ALERT ME")` + `SelectableChip` row: 2 min / 5 min / 10 min / 15 min.
5. Alert intensity — `SectionHeader("ALERT STYLE")` + 3 `SelectableChip`s: Gentle / Standard / Unmistakable. Descriptor below selected chip.
6. Arm Trip — sticky bottom `PrimaryButton("Arm trip")`, disabled until destination set.

**Motion:** sections fade in top-to-bottom, 40ms stagger. Chip selection animates `Motion.Fast`.

---

## Phase 5 — `LiveTripScreen` (The Centerpiece)

**Accent:** animates `MonitoringBase → ApproachBase → AlertBase` over `Motion.Slow`

**Layout:**
1. Minimal top bar — "Stop trip" text button right only. No title.
2. Hero ETA block (top 50% of screen):
   - `PulseRing(LocalStateAccent, 280.dp)` as background layer
   - `NumericText` ETA in `DisplayXL` centered
   - `Title` "min" label below
   - `Headline` destination name
   - `Body` secondary distance (e.g., "≈ 2.3 km")
3. Route summary strip — `ElevatedCard` full-width, horizontal leg chips (icon + `Label` mode + `NumericText` duration). If no route: `StateChip("Destination-only", Approaching)`.
4. Status row — 4 chips:
   - Monitoring state: Monitoring / Approaching / Alert
   - Power mode: Balanced / Low power / Precise
   - Confidence: High / Medium / Low (amber or red if medium/low)
   - Underground chip (only when signal loss detected)
5. **Active / Sleep Mode toggle** — below status row or inline.
6. Bottom — `SecondaryButton("Stop trip")`.

**State transitions:** entire screen `LocalStateAccent` tweens on state change. Background gains 3% accent tint. Pulse ring cross-fades. ETA stays white — only accent elements change color.

---

## Phase 6 — Alert Screens

### Stage A — Approach (notification only, no new screen)
- Persistent notification: soft tone, "Approaching [destination] — 2 stops away"
- Lock-screen card: destination, ETA, confidence chip

### Stage B — Imminent (notification + optional screen wake)
- **Active mode:** full-width notification, stronger tone + haptic pulse, lock-screen emphasis
- **Sleep mode:** screen wakes, haptic preamble 30s, audio rises progressively

### Stage C — `AlertScreen`

**Accent:** `AlertIntense`

- Full-screen `AlertIntense` (#FF4444) background
- Slow pulsing darker overlay: 2s cycle, 0→8% black amplitude — presence without distraction
- Transparent / red system bars (hide if policy allows)
- **Top half empty** — no nav clutter
- Center: `DisplayL` "ARRIVING" (or "YOUR STOP" if sub-minute) — white, bold, centered
- Below: `Headline` destination name — white 90%
- Below: `NumericText` ETA or distance — white 80%
- Bottom 25%: 160dp circular dismiss button — white fill, `AlertIntense` text, `Title` "Dismiss"
- Below dismiss: `TextButton` "Snooze 60s" — white 70%
- **No nav chrome. No back button. No menu. Only Dismiss and Snooze.**
- **Accessibility:** AAA contrast (white on #FF4444 passes), TalkBack immediate announcement on open, reduced-motion disables pulse, content description includes destination name

### `RecoveryScreen` (Stage D — first-class screen)

**Accent:** `ApproachBase`

1. Top bar — "Trip recovery", **no back arrow** (decision screen, not dismissable)
2. `HeroCard(ApproachBase)`:
   - `StateChip("Monitoring interrupted", Approaching)`
   - `Title`: description of what happened (from ViewModel — e.g., "Location signal was lost")
   - `Body` secondary: "Last known position: 2 min ago"
3. `ElevatedCard`: destination name, route summary, last ETA (from persisted `TripSession`)
4. Action stack (bottom):
   - `PrimaryButton("End trip", accent = AlertBase)` — red, marks finality
   - `SecondaryButton("Resume monitoring")` — only if ViewModel says resumable
   - Nearest return stop + `SecondaryButton("Re-arm return trip")`
   - "Stop now and walk back" note if overshoot < 400m
   - `TextButton("View diagnostics")`

---

## Phase 7 — `HistoryScreen` + `TripSummaryScreen`

### HistoryScreen
- Top bar: "History", back arrow
- `SectionHeader` date groups: TODAY / YESTERDAY / THIS WEEK / EARLIER
- Each row: `SurfaceCard`:
  - `Title` destination name
  - `Body` secondary route summary or "Destination-only"
  - Right: timestamp `Body` tertiary + outcome `StateChip` (Arrived=Safe, Missed=Alert, Ended=Neutral)
- Tap → Trip Summary

### TripSummaryScreen
- Top bar: destination name as title
- `HeroCard` (accent by outcome): outcome label `DisplayL`, `NumericText` total duration, date + time range
- `ElevatedCard`: full route with leg chips
- `SurfaceCard`: stats — first ETA, final ETA, confidence over trip, power mode used, alert stages fired (all from persisted data)
- `TextButton("View session diagnostics")`

---

## Phase 8 — `SettingsScreen` + `DiagnosticsScreen`

### SettingsScreen
- `SectionHeader` groups: ALERTS / MONITORING / PRIVACY / ABOUT
- Each group in `SurfaceCard`, `Spacing.xxxl` between groups
- Row pattern: `Title` label + `Body` descriptor + trailing control (switch / chevron / chip)
- Sections:
  - **ALERTS:** default intensity, lead time, sound, vibration, Active vs Sleep default
  - **MONITORING:** default power mode, location frequency
  - **PRIVACY:** data retention, diagnostics sharing
  - **RELIABILITY:** OEM battery sub-page (see below)
  - **ABOUT:** version, licenses

### OEM Reliability Sub-page
- Detects OEM (Samsung / Xiaomi / Oppo / Vivo / Huawei / OnePlus)
- Shows vendor-specific battery exemption steps with screenshots/instructions
- `StateChip` showing current reliability status (Optimal / Degraded)

### DiagnosticsScreen
- Dense power-user layout — density is the feature
- `SectionHeader` groups: SERVICES / LOCATION / PERMISSIONS / LAST SESSION
- `Label` mono keys, `NumericText` values, `StateChip` health badges
- "Why this alert fired" expandable log per stage
- Optional "Share" action in top bar

---

## Phase 9 — `OnboardingScreen`, `PermissionsScreen`, `PlaceSearchScreen`

### OnboardingScreen
- Horizontal pager, 3–4 pages
- Each page: 120dp Canvas icon (no external illustration) + `Headline` + `BodyL`
- Pages:
  1. "Never miss your stop again" — value prop
  2. "Works offline" — reliability
  3. "Privacy first" — data framing
  4. "Ready?" — CTA
- Bottom: dot indicators (accent-colored) + `PrimaryButton` "Continue" / "Get started" on final
- Skip `TextButton` top-right

### PermissionsScreen
- `Body` intro: "NearWake needs a few permissions to monitor your trips reliably."
- 3 `SurfaceCard`s:
  - **Location** — essential — `StateChip` status (Granted / Needed / Limited)
  - **Notifications** — essential — same chip
  - **Background activity** — recommended — softer framing
- Each card: expandable "Why?" row (`Body` secondary) + request button
- `PrimaryButton("Continue")` — disabled until essentials granted; "Continue with limited mode" fallback label
- Uses research-proven permission copy from PLAN.md §Permission Copy

### PlaceSearchScreen
- Search field in top bar position — not a title, prominent input, `BgElevated`, `Radius.sm`, `BodyL`
- `SectionHeader("SAVED PLACES")` — if any saved places exist
- `SectionHeader("SEARCH RESULTS")` — appears when query has 2+ characters
- Each result row: location pin icon + `Title` name + `Body` secondary address + tap to select
- Empty state: `Body` secondary "Start typing to search places"

### Walk-the-Last-Mile Screen (new — after Stage C dismiss, `feature:walkfinish`)
- Distance ring + compass heading indicator to destination pin
- `NumericText` distance + `Body` direction label
- "You have arrived" confirmation card at 30m — tap to dismiss

### Departure Reminder Screen (new — `feature:departure`)
- Recurring trip card with `SectionHeader("LEAVE BY")`
- `NumericText` countdown + `Title` route
- `PrimaryButton("Start trip now")`

### Arrival Confirmation Sheet (new — `feature:companion`)
- Bottom sheet appearing after trip reaches Completed state
- `Body` "Share your arrival with a contact?"
- Contact picker + share intent (SMS / share sheet)
- Privacy note below

---

## Verification Commands (Run After Each Phase)

```bash
./gradlew.bat :core:designsystem:assemble
./gradlew.bat :core:ui:assemble
./gradlew.bat :domain:trip:test
./gradlew.bat :core:network:test
./gradlew.bat :data:routing:test
./gradlew.bat :data:alerts:test
./gradlew.bat :app:assembleDebug
./gradlew.bat :app:assembleRelease
```

Also render Compose previews for every modified screen and component. Fix preview failures before moving to next phase.

---

## Acceptance Criteria

1. Debug and release builds pass
2. All existing tests pass — none disabled or removed
3. All screens consume tokens from `core:designsystem` — zero hardcoded colors/sizes/radii in feature modules
4. All screens reuse components from `core:ui` — no local style recreation
5. Home → Trip Setup → Live Trip → Alert feel like one coherent product
6. Live Trip accent animates smoothly: Monitoring → Approaching → Alert
7. Alert screen is AAA contrast, dismiss ≥ 120dp (160dp target), no nav chrome
8. All numeric data uses tabular figures (`tnum`)
9. No fake data anywhere — real persisted data or real empty states
10. ViewModel contracts unchanged, navigation routes unchanged
11. No new heavy dependencies

---

## Anti-Patterns to Avoid

- Decorative gradients without state meaning
- Drop shadows on dark surfaces (use border + elevated bg instead)
- Hardcoded `primary` color — always resolve via `LocalStateAccent`
- Emoji as UI icons (use Material icons or Canvas shapes)
- Looping animations other than the pulse ring
- Placeholder data in previews to "make them look nicer"
- Centered body text everywhere (center only headlines and hero content)
- Touch targets under 48dp
- Color-coded status with no text label (always color + chip label)
- Light-theme work during this pass
