# NearWake — UI/UX Redesign Plan (Wave H)

**Status:** ✅ Complete — all 7 screens shipped
**Last updated:** 2026-05-10
**Scope:** Presentation layer only — no ViewModel changes, no architecture changes, no persistence changes

---

## Problem Statement

The current UI suffers from two structural issues that make it feel amateur and exhausting to use:

### 1. Too much text

Every screen has section headers, description text, sub-labels, and explanation rows stacked on top of each other. Users do not read these. They create visual noise, make screens feel heavy, and bury the one signal the user actually needs (the ETA, the destination, the action).

Current examples:
- `PlaceSearchScreen` — every result has a name label, address label, and a full-width "Use this place" button. Three elements per result when one tap should suffice.
- `LiveTripScreen` — 4 chip labels (stage name, confidence percentage, underground text, battery percentage) all visible simultaneously even when most are irrelevant.
- `RecoveryScreen` — 4 `SurfaceCard`s of explanation text before reaching the two action buttons.
- `TripSetupScreen` — 8 scrollable sections; the Arm button is only reachable by scrolling past all of them.

### 2. Oversized, uniform elements

Every element is the same size regardless of its importance:
- Every search result row is ~150dp tall (SurfaceCard + 3 text lines + 56dp button + 12dp padding). Three results fill the entire screen and hide the search field.
- Every card uses 16–20dp uniform padding regardless of content density.
- Every button is 56dp regardless of context — the "View all" history link is the same height as the "Arm trip" CTA.
- `AlertScreen` has a 256dp `PulseRing` and a 160dp circular Walk button — both oversized for their role.

---

## Redesign Goal

**One dominant signal per screen. Communicate everything else through icons + color + size hierarchy — not text.**

---

## Design Principles

| # | Principle | Meaning |
|---|-----------|---------|
| 1 | One primary signal per screen | ETA on LiveTrip. Search field on PlaceSearch. Destination on Home. Everything else is secondary. |
| 2 | Visual over verbal | A `place` icon communicates "saved place" faster than the word "Destination". Replace text labels with icons + color wherever possible. |
| 3 | Progressive disclosure | Hide monitoring mode, signal quality, battery % behind a "Details ›" tap. Show it when the user wants it — not always. |
| 4 | Compact by default, spacious when important | Search result rows = 56dp. Re-arm card = 72dp. Active trip card = 80dp. Not everything is 140dp. |
| 5 | Actions in context | Tap a row to select it. No inline "Use this place" button per result. One tap, not two. |

---

## Material Symbols Icons — Add to Project

**Current state:** Zero custom icons anywhere in the app. All status is communicated through text chips.

**Change:** Add `androidx.compose.material:material-icons-extended` to `app/build.gradle.kts`.

### Icon Map

| Icon | Code Reference | Where Used |
|------|---------------|------------|
| `Icons.Filled.Place` / `LocationOn` | saved places, destination marker | PlaceSearch rows, HomeScreen destination |
| `Icons.Filled.DirectionsTransit` | active trip, route | HomeScreen active trip card, TripSetup route row |
| `Icons.Filled.Schedule` / `Alarm` | ETA, departure time | LiveTrip status strip, DepartureReminder |
| `Icons.Filled.SignalWifiOff` | underground / offline mode | LiveTrip status strip (replaces chip) |
| `Icons.Filled.BatterySaver` | low battery warning | LiveTrip status strip (shown only if < 20%) |
| `Icons.Filled.TransferWithinAStation` | transfer leg | TransferProgressCard |
| `Icons.Filled.CheckCircle` | arrived / trip complete | Recent trips rows (outcome icon) |
| `Icons.Filled.Warning` | approach alert / missed | Recent trips rows (outcome icon), RecoveryScreen |
| `Icons.Filled.DirectionsWalk` | walk finish | WalkFinishScreen, AlertScreen Walk button |
| `Icons.Filled.History` | recent search results | PlaceSearch section header |
| `Icons.Filled.Replay` | re-arm last trip | HomeScreen re-arm card |
| `Icons.Filled.Bolt` | active monitoring mode | TripSetup mode chip, LiveTrip status |
| `Icons.Filled.Bedtime` | sleep mode | TripSetup mode chip, LiveTrip status |
| `Icons.Filled.NotificationsActive` | alert mode active | TripSetup mode chip |
| `Icons.Filled.Tune` | trip options / advanced | TripSetup "Advanced ›" row |
| `Icons.Filled.ExpandMore` / `ExpandLess` | expand/collapse | TripSetup advanced section, LiveTrip details |

---

## Design Token Changes

### `core/designsystem/NearWakeSpacing.kt`

Add named card padding constants so all screens use consistent compact/default/large variants instead of ad-hoc values:

```kotlin
// Add to NearWakeSpacing object:
val cardCompact = 8.dp    // search result rows, list rows, compact cards
val cardDefault = 12.dp   // standard cards (was unnamed / inconsistently used)
val cardLarge = 16.dp     // hero cards, elevated destination cards
```

Reduce default inter-card spacing from 16dp → 12dp. This one change reduces total vertical height by ~30dp per screen on average.

### `core/ui/NearWakeButtons.kt`

Add a `NearWakeButtonSize` enum so buttons communicate their importance through height:

```kotlin
enum class NearWakeButtonSize {
    Small,   // 40dp — secondary actions, inline confirmations
    Medium,  // 48dp — standard CTAs (new default for most screens)
    Large    // 56dp — hero CTA only (Arm trip, Dismiss alert)
}
```

Update `NearWakePrimaryButton` to accept an optional `size: NearWakeButtonSize = Medium` parameter. The current 56dp height becomes `Large` — used only for the Arm trip button and Alert dismiss.

### `core/ui/SurfaceCard.kt`

Add a `compact: Boolean = false` parameter. When `compact = true`, padding switches to `cardCompact` (8dp) instead of the default 16dp. Used for search result containers and list-row wrappers.

### Typography Usage Restrictions (no scale changes)

The existing type scale is correct — the problem is overuse of large sizes. Restrict:

| Style | Current overuse | Correct usage |
|-------|----------------|---------------|
| `headlineLarge` (28sp) | Used on destination name, screen titles, hero cards | LiveTrip hero destination + AlertScreen destination only |
| `displayMedium` (48sp) | ETA and other numeric values | ETA countdown number only — nothing else |
| `titleLarge` (18sp) | — | Screen titles, destination name on Home, Alert |
| `titleMedium` (16sp) | Used for card titles, result names | Active trip card destination, section leaders |
| `bodyMedium` (14sp) | Used inconsistently | All secondary text, addresses, row subtitles |
| `labelSmall` (12sp) | Used for timestamps only | Section headers, timestamps, unit labels ("min", "km") |

---

## Screen-by-Screen Redesign

---

### 1. PlaceSearchScreen — Highest Priority

**File:** `feature/places/src/main/kotlin/com/nearwake/feature/places/PlaceSearchScreen.kt`
**New component:** `core/ui/src/main/kotlin/com/nearwake/core/ui/PlaceResultRow.kt`

#### Current layout (per result row)
```
SurfaceCard (12dp padding all sides)
  ├── Text: place name          [titleMedium]
  ├── Text: full address        [bodyMedium]
  └── NearWakePrimaryButton: "Use this place"   [56dp height, full width]
Total height per result: ~150dp
3 results = 450dp — fills the entire screen, search field scrolls off
```

#### Redesigned layout (per result row)
```
Row (clickable, height = 56dp)
  ├── Icon: place / history     [20dp, MonitoringBase or TextTertiary]
  ├── 12dp horizontal spacer
  └── Column
        ├── Text: place name    [titleSmall]
        └── Text: short address [labelSmall, TextSecondary]
HorizontalDivider (between rows)
Total height per result: 56dp
3 results = 168dp — search field stays visible
```

**Interaction changes:**
- Tap the entire row to select — no separate button
- Selected state: 4dp `SafeBase` left border + 4% `SafeBase` background tint
- No `SurfaceCard` wrapper — use a flat `Row` with `clickable` modifier

**Section headers:**
- "SAVED PLACES" / "RESULTS" — `labelSmall`, uppercase, `TextTertiary`, 8dp top padding, no card border
- Icon: `History` for recent searches, `Place` for saved, `LocationOn` for API results

#### Before/After impact
| Metric | Before | After |
|--------|--------|-------|
| Height per result | ~150dp | 56dp |
| Results visible without scrolling | 2–3 | 8–9 |
| Taps to select | 2 (scroll + tap button) | 1 (tap row) |

---

### 2. HomeScreen

**File:** `app/src/main/kotlin/com/nearwake/app/HomeScreen.kt`

#### Active trip card (when monitoring is running)

**Current:** `DestinationHeroCard` — 84dp min height + title + subtitle + button ≈ 180dp total

**Redesigned:**
```
ElevatedCard (height = 80dp, clickable → LiveTrip)
  ├── Icon: DirectionsTransit   [24dp, MonitoringBase]
  ├── 12dp spacer
  ├── Text: destination name    [titleMedium, weight]  (fills remaining width)
  ├── Icon: ChevronRight        [16dp, TextTertiary]
  └── Chip: ETA value           [SafeBase background, labelSmall, "12 min"]
```
No subtitle, no description text. The ETA chip communicates status. Tap → LiveTrip.

#### Re-arm card (last trip available, no active trip)

**Current:** `RearmCard` — same structure as DestinationHeroCard ≈ 180dp

**Redesigned:**
```
SurfaceCard (height = 72dp, clickable → re-arms trip)
  ├── Icon: Replay              [20dp, ApproachBase]
  ├── 12dp spacer
  ├── Text: destination name    [bodyLarge, SemiBold]  (fills remaining width)
  └── Text: "Re-arm →"          [labelLarge, SafeBase]
```
Remove the "Last trip" section header. The `Replay` icon communicates the meaning.

#### Recent trips section

**Current:** Each row = `SurfaceCard` ≈ 100dp with destination + route text + timestamp + chip

**Redesigned:**
```
Row (height = 52dp, divider between rows)
  ├── Icon: CheckCircle (completed) or Warning (missed)  [20dp, colored by outcome]
  ├── 12dp spacer
  ├── Text: destination name    [bodyMedium]  (fills remaining width)
  └── Text: "Mon 14:32"         [labelSmall, TextSecondary, right-aligned]
```
"View all →" becomes a single `TextButton` below the list — not a full-height button.

#### No-trip empty state

**Current:** Multiple text paragraphs explaining how to start

**Redesigned:** One centered card — `DirectionsTransit` icon (48dp, MonitoringBase) + `titleMedium` "Where are you heading?" + `PrimaryButton("Search destination", size=Medium)`. Nothing else.

---

### 3. LiveTripScreen — Information Overload Fix

**File:** `feature/livetrip/src/main/kotlin/com/nearwake/feature/livetrip/LiveTripScreen.kt`
**File:** `feature/livetrip/src/main/kotlin/com/nearwake/feature/livetrip/TransferProgressCard.kt`

#### Hero section

**Current:**
```
PulseRing (256dp diameter)
  ├── ETA number     [displayMedium, 48sp]
  ├── "min"          [bodyMedium — separate line]
  ├── Destination    [headlineLarge, 28sp]
  └── Trust message  [bodyMedium — e.g., "Confident estimate"]
```

**Redesigned:**
```
Text: destination name     [titleMedium, ABOVE the ring]
PulseRing (256dp diameter — keep size)
  ├── ETA number           [displayMedium, 48sp]
  └── "min" inline         [titleSmall, 14sp — same line, right of number, bottom-aligned]
```
- Remove trust message text entirely — ring color already communicates confidence (MonitoringBase=confident, ApproachBase=degraded, AlertBase=imminent)
- Destination moves above the ring in `titleMedium` — cleaner hierarchy

#### Status strip (below ring)

**Current:** 4 `NearWakeStateChip` components in a `FlowRow`:
- `"MONITORING"` stage chip
- `"Confidence: 87%"` chip
- `"Underground"` chip (always visible even when not underground)
- `"Battery: 45%"` chip

**Redesigned:** A single 32dp-tall `Row` with icons only:
```
Row (height = 32dp, spacing = spacing.sm)
  ├── Icon: stage (DirectionsTransit/Warning/CheckCircle)  [colored by stage]
  ├── [Only if DEGRADED or OFFLINE]: dot (amber/red) + labelSmall "Signal degraded"
  ├── [Only if underground]: Icon: SignalWifiOff + labelSmall "Underground"
  └── [Only if battery < 20%]: Icon: BatterySaver + labelSmall battery%
```
No chip borders. Icons in context-appropriate color. The strip is invisible 80% of the time (normal commute = just the stage icon).

#### Secondary details (collapsed by default)

**Current:** `MonitoringStatusCard` always visible — shows monitoring mode, geofence radius, signal quality, 4 more chips.

**Redesigned:** Replace with a single `TextButton("Details ›", style=labelSmall, TextTertiary)` at the bottom of the screen. Tapping opens a `ModalBottomSheet` showing:
- Monitoring mode (Active / Sleep / Balanced)
- Signal quality (HIGH / DEGRADED / OFFLINE)
- Geofence radius in meters
- Last location update timestamp

Removes 2–3 cards from the main scroll path.

#### Transfer progress strip

**Current:** Horizontally scrolling row of cards, each 220dp wide × ~100dp tall:
```
ElevatedCard (220dp × 100dp)
  ├── Text: stop name      [titleMedium]
  ├── Text: arrival time   [bodyMedium]
  ├── Chip: signal quality [NearWakeStateChip]
  └── Chip: transfer mode  [NearWakeStateChip]
```

**Redesigned:** Each card 160dp wide × 64dp tall:
```
SurfaceCard (160dp × 64dp, compact padding)
  ├── Icon: TransferWithinAStation / DirectionsTransit  [20dp]
  ├── 8dp spacer
  ├── Text: stop name      [bodyMedium, single line]
  └── Dot: status          [8dp circle, colored by signal quality]
```
Cards become scannable at a glance. The dot communicates signal quality without a chip label.

---

### 4. TripSetupScreen — Scroll Problem Fix

**File:** `feature/tripsetup/src/main/kotlin/com/nearwake/feature/tripsetup/TripSetupScreen.kt`

#### Arm button — sticky bottom

**Current:** The Arm button is the last item in a `LazyColumn` and is only visible after scrolling past all 8 sections.

**Fix:** Move the Arm button to `Scaffold`'s `bottomBar`:
```kotlin
Scaffold(
    bottomBar = {
        NearWakePrimaryButton(
            text = "Arm trip",
            size = NearWakeButtonSize.Large,
            enabled = uiState.canArm,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.lg, vertical = spacing.md),
        )
    }
)
```
The button is now always visible. Users can tap Arm at any point — no scrolling required.

#### Destination section

**Current:** Large `ElevatedCard` with title, subtitle, description, and change button ≈ 120dp

**Redesigned:**
```
ElevatedCard (height = 72dp, clickable if not selected)
  ├── Icon: Place             [20dp, MonitoringBase if selected, TextTertiary if not]
  ├── 12dp spacer
  ├── Text: destination name or "Select destination"  [bodyLarge]
  └── TextButton: "Change"   [labelSmall, right-aligned — only if place selected]
```

#### Trigger mode selection

**Current:** 3 `NearWakeSelectableChip` with label text, no visual grouping

**Redesigned:** Use `SingleChoiceSegmentedButtonRow` from Material 3:
```kotlin
SingleChoiceSegmentedButtonRow {
    SegmentedButton(selected = mode == TIME,     label = { Text("Time") },     icon = { Icon(Schedule) })
    SegmentedButton(selected = mode == DISTANCE, label = { Text("Distance") }, icon = { Icon(MyLocation) })
    SegmentedButton(selected = mode == BOTH,     label = { Text("Both") },     icon = { Icon(Tune) })
}
```
Height: 40dp. Communicates mutual exclusivity clearly without needing a section header.

#### Lead time chips

**Current:** `SectionHeader("ALERT ME") + FlowRow` of 5 chips, 40dp each

**Redesigned:** Compact chip row, 36dp height chips, horizontal scroll if needed:
```
Row (single line, horizontalScroll)
  Chip: Nearby | Chip: 2 min | Chip: 5 min | Chip: 10 min | Chip: 15 min
```
No section header. Label above is enough: `labelSmall("ALERT BEFORE ARRIVAL")` inline.

#### Advanced options (collapsed by default)

Everything below lead time (distance trigger threshold, alert intensity, power mode) moves into a collapsible section:
```kotlin
var advancedExpanded by rememberSaveable { mutableStateOf(false) }

Row(
    modifier = Modifier.clickable { advancedExpanded = !advancedExpanded },
    verticalAlignment = CenterVertically,
) {
    Icon(Icons.Filled.Tune, tint = TextSecondary)
    Spacer(Modifier.width(8.dp))
    Text("Advanced", style = bodyMedium, color = TextSecondary)
    Spacer(Modifier.weight(1f))
    Icon(if (advancedExpanded) ExpandLess else ExpandMore, tint = TextSecondary)
}

AnimatedVisibility(visible = advancedExpanded) {
    // Distance trigger slider
    // Alert intensity chips
    // Power mode chips
}
```
90% of users will never open this. The screen now shows: destination card → route preview row → trigger mode segmented row → lead time chips → Arm button (sticky). That's it.

#### Route preview row (inline, no card)

**Current:** `ElevatedCard` with route icon, stop count, duration, confidence chip ≈ 80dp

**Redesigned:** Single `Row`, no card border, 40dp tall:
```
Row (height = 40dp)
  ├── Icon: DirectionsTransit  [16dp, MonitoringBase]
  ├── Text: "12 stops · 28 min"  [bodyMedium]
  └── Dot: signal quality     [8dp circle, colored — no chip label]
```

---

### 5. AlertScreen

**File:** `feature/alerts/src/main/kotlin/com/nearwake/feature/alerts/AlertScreen.kt`

#### Current dimensions
- `PulseRing` diameter: 256dp
- "ARRIVING" text: `displayLarge` (48sp)
- Destination: `headlineLarge` (28sp)
- Walk button: 160dp circle

#### Redesigned dimensions

**PulseRing:** Reduce from 256dp → 192dp. The ring is ambient — it supports the signal, it is not the signal itself. 192dp still dominates the screen without eating 40% of it.

**Content inside ring:**
```
Column (centered inside ring)
  ├── Text: ETA number    [displayMedium, 48sp] ← the single dominant signal
  └── Text: "min"         [titleSmall, 14sp, inline / right of number]
```

**Content above ring:**
```
Text: destination name    [titleLarge, 18sp] ← not headlineLarge
```

**Remove "ARRIVING" label entirely.** The full-red background and pulsing ring communicate arrival. The word adds nothing and takes up visual weight that belongs to the ETA number.

**Walk button:** Reduce from 160dp → 128dp circle diameter. Still a large circular touch target (well above 48dp requirement), but proportional to the screen.

**"I missed it" button:** Keep as `TextButton`. No change needed.

#### Revised layout top-to-bottom
```
[full-screen AlertBase background]
  Text: destination name    [titleLarge, white, centered — top third]
  PulseRing (192dp)
    Text: ETA + "min"       [displayMedium + titleSmall, white]
  [spacer]
  CircularButton: Walk      [128dp, white fill, AlertBase text, DirectionsWalk icon above text]
  TextButton: "I missed it" [white 70%]
```

---

### 6. RecoveryScreen

**File:** `feature/alerts/src/main/kotlin/com/nearwake/feature/alerts/RecoveryScreen.kt`

#### Current layout
4 `SurfaceCard`s of explanation text + 2 full-width stacked `NearWakePrimaryButton`s ≈ 500dp total

#### Redesigned layout

**Single `ElevatedCard`:**
```
ElevatedCard
  ├── Icon: Warning            [24dp, ApproachBase]
  ├── Text: "You may have passed your stop"  [titleMedium]
  └── Text: "Last known position: [stop name] · 3 min ago"  [bodyMedium, TextSecondary]
```

**Side-by-side buttons (48dp height):**
```
Row
  ├── OutlinedButton: "Re-arm"   [weight=1f, 48dp, Secondary style]
  └── 12dp spacer
  └── Button: "End trip"         [weight=1f, 48dp, Primary style]
```
Side-by-side layout makes it immediately obvious there are two choices. Stacked buttons imply sequence.

Remove the "walk back note" card. If the user needs it, fold into an expandable "What happened?" row below the action buttons.

---

### 7. SettingsScreen — Endless Scroll Fix

**File:** `feature/settings/src/main/kotlin/com/nearwake/feature/settings/SettingsScreen.kt`

#### Current structure

Multiple `SurfaceCard` sections, each containing `FlowRow` chip groups + description text. Requires significant scrolling to reach sections at the bottom.

#### Redesigned structure

Replace card-per-section with grouped `LazyColumn` list rows, identical to the Android Settings pattern:

**Section headers:** `labelSmall`, uppercase, `TextTertiary`, 16dp top padding, 8dp bottom padding — no card border.

**Setting row pattern:**
```
Row (height = 52dp, clickable)
  ├── Icon: [context icon]       [20dp, TextSecondary]
  ├── 16dp spacer
  ├── Column
  │     ├── Text: label          [bodyMedium, TextPrimary]
  │     └── Text: current value  [labelSmall, TextSecondary] ← optional
  └── [Trailing control — right-aligned]
        Switch (for toggles)
        or Icon: ChevronRight (for selectors)
        or Text: current value (for chip-group settings)
```

**Chip-group settings** (alert intensity, lead time, trigger mode) → replaced with a tap-to-open `ModalBottomSheet`:
```
SettingRow(
    icon = Icons.Filled.Alarm,
    label = "Lead time",
    value = "5 minutes",
    onClick = { showLeadTimeSheet = true }
)
// ModalBottomSheet with SelectableChip group
```

This reduces the settings screen to a scannable list of ~12 rows. No more FlowRow chip overflows or horizontal scrolling edge cases.

#### Section breakdown

```
ALERTS
  ├── Lead time           → BottomSheet (Nearby / 2 min / 5 min / 10 min / 15 min)
  ├── Alert trigger       → BottomSheet (Time / Distance / Both)
  ├── Alert intensity     → BottomSheet (Gentle / Standard / Unmistakable)
  └── Notifications       → system settings deep-link

MONITORING
  ├── Power mode          → BottomSheet (Balanced / Precise / Battery saver)
  └── Sleep mode default  → Switch

APPEARANCE
  ├── Theme               → BottomSheet (System / Light / Dark) [already exists]

RELIABILITY
  └── OEM battery guide   → chevron → OEM sub-screen [already exists]

PRIVACY
  ├── Diagnostics sharing → Switch
  ├── View diagnostics    → chevron → DiagnosticsScreen
  └── Privacy policy      → external link

ABOUT
  ├── App version         → Text value (non-clickable)
  └── Open source         → chevron
```

---

## Files to Change

| File | Change |
|------|--------|
| `app/build.gradle.kts` | Add `material-icons-extended` dependency |
| `core/designsystem/NearWakeSpacing.kt` | Add `cardCompact`, `cardDefault`, `cardLarge` constants |
| `core/ui/NearWakeButtons.kt` | Add `NearWakeButtonSize` enum; make `Medium` (48dp) the new default |
| `core/ui/SurfaceCard.kt` | Add `compact: Boolean = false` parameter (8dp padding when true) |
| `core/ui/PlaceResultRow.kt` | **New file** — 56dp row composable (icon + name + address, tap-to-select) |
| `feature/places/PlaceSearchScreen.kt` | Replace `SurfaceCard` + button per result with `PlaceResultRow` |
| `app/HomeScreen.kt` | Compact 80dp active trip card, 72dp re-arm card, 52dp recent trip rows |
| `feature/tripsetup/TripSetupScreen.kt` | Sticky Arm button in `Scaffold bottomBar`, segmented trigger row, "Advanced ›" collapse |
| `feature/livetrip/LiveTripScreen.kt` | Icon status strip (32dp), "Details ›" collapse, destination above ring |
| `feature/livetrip/TransferProgressCard.kt` | 160dp × 64dp card, icon + name + status dot |
| `feature/alerts/AlertScreen.kt` | Ring stays 256dp, remove "ARRIVING" label, Walk button 128dp, destination titleLarge |
| `feature/alerts/RecoveryScreen.kt` | Single ElevatedCard, side-by-side 48dp buttons |
| `feature/settings/SettingsScreen.kt` | List rows + `ModalBottomSheet` selectors per setting group |

---

## Implementation Order

Implement in this order so each step builds on a stable foundation.

### Step 1 — Tokens + Components (unblocks everything)
- `NearWakeSpacing` — add `cardCompact`, `cardDefault`, `cardLarge`
- `NearWakeButtons` — add `NearWakeButtonSize`, change default from `Large` → `Medium`
- `SurfaceCard` — add `compact` parameter
- `PlaceResultRow` — new composable in `core/ui`
- Add `material-icons-extended` to `app/build.gradle.kts`
- **Verify:** `:core:designsystem:assemble`, `:core:ui:assemble`

### Step 2 — PlaceSearchScreen
- Replace every `SurfaceCard` + `NearWakePrimaryButton` result block with `PlaceResultRow`
- Add tap-to-select behavior (remove the `onUsePlaceClick` button, use row `clickable`)
- Selected state: `SafeBase` left border + background tint
- **Verify:** Search, select a place, confirm it routes to TripSetup

### Step 3 — HomeScreen
- Active trip card → 80dp `ElevatedCard` with ETA chip
- Re-arm card → 72dp `SurfaceCard` with `Replay` icon
- Recent trips → 52dp row list with outcome icons
- Empty state → single centered card
- **Verify:** All 4 home states (no trip, active, re-arm available, recent trips)

### Step 4 — TripSetupScreen
- Move Arm button to `Scaffold bottomBar`
- Replace destination card with 72dp compact version
- Replace trigger mode chips with `SingleChoiceSegmentedButtonRow`
- Reduce lead time chip height to 36dp
- Collapse advanced options behind "Advanced ›" expandable
- **Verify:** Arm button visible without scrolling; Advanced toggle expands/collapses

### Step 5 — LiveTripScreen
- Move destination name above `PulseRing`
- Remove trust message text
- Replace 4 chips with 3-icon status strip (32dp)
- Replace `MonitoringStatusCard` with "Details ›" → `ModalBottomSheet`
- Update `TransferProgressCard` to 160dp × 64dp with icon + name + dot
- **Verify:** All trip states (monitoring, approach, imminent); Details sheet opens; transfer strip scrolls

### Step 6 — AlertScreen + RecoveryScreen
- `AlertScreen`: Remove "ARRIVING" label; reduce Walk button to 128dp
- `RecoveryScreen`: Collapse to single `ElevatedCard` + side-by-side buttons
- **Verify:** Alert fires correctly; both actions on RecoveryScreen work

### Step 7 — SettingsScreen
- Replace card-per-section with grouped list rows
- Add `ModalBottomSheet` for lead time, trigger mode, alert intensity
- **Verify:** All settings persist correctly after selecting via sheet

---

## Acceptance Criteria

- [ ] `PlaceSearchScreen` — 3 search results fit on screen without scrolling, search field remains visible
- [ ] `PlaceSearchScreen` — single tap on a row navigates to TripSetup (no "Use this place" button)
- [ ] `HomeScreen` — active trip card height ≤ 84dp (80dp content + 4dp padding)
- [ ] `HomeScreen` — recent trip rows use outcome icons (`CheckCircle` / `Warning`), no chip labels
- [ ] `TripSetupScreen` — Arm button visible without scrolling on any device ≥ 360dp width
- [ ] `TripSetupScreen` — Advanced section hidden by default; tapping "Advanced ›" reveals it
- [ ] `LiveTripScreen` — status strip height ≤ 32dp; no chip borders visible during normal monitoring
- [ ] `LiveTripScreen` — "Details ›" tap opens bottom sheet with monitoring details
- [ ] `LiveTripScreen` — transfer cards width = 160dp (not 220dp)
- [ ] `AlertScreen` — "ARRIVING" label not visible
- [ ] `AlertScreen` — Walk button diameter = 128dp
- [ ] `RecoveryScreen` — Re-arm and End trip buttons are side-by-side on one row
- [ ] `SettingsScreen` — Lead time, trigger mode, intensity use `ModalBottomSheet` (no FlowRow chips inline)
- [ ] All screens: `./gradlew :app:assembleDebug` passes with no warnings introduced
- [ ] All existing tests pass — none disabled or removed

---

## What Does NOT Change

- No ViewModel contract changes
- No navigation route changes
- No data layer changes
- No Room schema changes
- No Hilt module changes
- `PulseRing` animation stays (ring stays 256dp — only Walk button and chip strip change)
- Light / Dark / System theme toggle stays in SettingsScreen
- OEM reliability sub-screen stays unchanged
- `DiagnosticsScreen` layout stays unchanged (density is a feature there)
- `DepartureReminderScreen` layout stays unchanged
