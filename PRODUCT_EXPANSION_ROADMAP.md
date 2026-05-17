# NearWake — Product Expansion Roadmap

**Last updated:** 2026-05-17
**Applies to:** v1.1 and beyond (v1.0 scope is locked in PLAN.md)

---

## The Problem With v1.0

NearWake v1.0 ships one strong feature cluster: confidence-aware arrival alerts with 3-stage escalation, missed-stop recovery, transfer awareness, and Sleep/Active mode. No competitor does all of this together.

The problem: it is still a **single-use-case app**. Users open it once per commute and close it. There is no reason to open it otherwise. That limits retention, word-of-mouth, and the surface area for sustainable monetization.

The expansion goal: add features that (a) make users open NearWake more than once per trip, (b) directly address gaps in Transit/Citymapper/Moovit, and (c) create a natural freemium surface without ever gating the alert path.

**Hard rule (from PRODUCT_COMPARE_REFERENCE.md §12):**
> Every feature must fit one of the eight pillars. If it doesn't clearly fit, it doesn't ship.

---

## Competitive Gap — What We're Adding

The current feature matrix (from PRODUCT_COMPARE_REFERENCE.md §13) shows NearWake already leads on trust and recovery. These are the remaining gaps:

| Capability | Transit | Citymapper | Moovit | Naplarm | **NearWake v1.0** | **NearWake vNext** |
|-----------|---------|-----------|--------|---------|-------------------|-------------------|
| Scheduled auto-arm | ✓ (basic) | ✓ | ✓ | ✗ | ✗ | **✓ Wave I** |
| Calendar integration | ✗ | ✗ | ✗ | ✗ | ✗ | **✓ Wave J** |
| Home screen widget | ✓ | ✓ | ✓ | ✗ | ✗ | **✓ Wave I** |
| Commute analytics | ✓ (basic) | ✓ | ✓ | ✗ | ✗ | **✓ Wave J** |
| Real-time disruptions | ✓ | ✓ | ✓ | ✗ | ✗ | Deferred |
| Trip profiles / saved settings | ✓ | ✓ | ✓ | ✗ | ✗ | **✓ Wave I** |
| Family / arrival sharing | ✗ | ✓ (heavy) | ✗ | ✗ | ✓ (light) | **✓ Wave I/K** |
| Wear OS | ✓ | ✓ | ✗ | ✗ | ✗ | **✓ Wave K** |
| iOS | ✓ | ✓ | ✓ | ✓ | ✗ | **✓ Wave K** |
| Google Assistant | ✓ | ✗ | ✗ | ✗ | ✗ | **✓ Wave J** |
| Per-route personalization | ✗ | ✗ | ✗ | ✗ | ✗ | **✓ Wave I — unique** |
| Confidence-first UX | ✗ | ✗ | ✗ | ✗ | **✓ unique** | **✓** |
| Bias-early + recovery | ✗ | ✗ | ✗ | ✗ | **✓ unique** | **✓** |
| No ads in alert path | ✗ | ✗ | ✗ | partial | **✓ committed** | **✓** |

---

## Feature Roadmap

---

### Category 1 — Smart Automation
**Pillar 1: Depart on Time**

These reduce the friction of the daily routine: open app → set destination → arm trip. Regular commuters do this every day. It should happen without them thinking about it.

---

#### 1a. Saved Trip Profiles

**Problem today:** Every trip is configured from scratch. A user who commutes to Work every morning re-selects the same destination, the same lead time, the same alert mode — every single day.

**Feature:**
- User creates named trip profiles: "Work", "Home", "Gym", "Mum's house"
- Each profile stores: destination + lead time + alert mode (Active/Sleep) + trigger mode + per-route bias
- HomeScreen shows up to 4 profile buttons below the search bar
- One tap on a profile button → trip is armed immediately, no setup screen required

**User story:**
> "I tap 'Work' on the home screen. Trip is armed. I get on the bus."

**Implementation notes:**
- New `TripProfile` entity in `core:database` (Room migration)
- New `:feature:profiles` screen or extend SavedPlace entity
- `StartTripFromProfileUseCase` in `application:trip`
- HomeScreen gets a `ProfileQuickArmRow` composable below the search bar

**Pillar:** 1 (Depart on time), 3 (Ride awake or asleep — per-profile mode)

---

#### 1b. Per-Route Bias Override

**Problem today:** Some routes are consistently underground (tunnels). Some users always sleep on long intercity trains. The engine uses a global Sleep/Active setting but cannot remember route-specific preferences.

**Feature:**
- On TripSetupScreen: toggle "I always sleep on this route"
- Persisted per destination in `SavedPlace` (new `alwaysUseSleepMode: Boolean` field)
- When this place is selected: automatically switches to Sleep mode + maximum bias multiplier

**User story:**
> "Every time I pick 'Kandy Station', NearWake knows I sleep on that train and uses the strongest alarm."

**Implementation notes:**
- Add `alwaysUseSleepMode: Boolean = false` to `SavedPlaceEntity`
- Room MIGRATION_7_8
- `LoadTripSetupPreviewUseCase` reads this field and pre-fills mode
- TripSetupScreen shows a toggle "I usually sleep on this route" pre-filled from saved state

**Pillar:** 3 (Ride awake or asleep)

---

#### 1c. Scheduled Recurring Trips

**Problem today:** Users who commute at the same time every weekday must open the app and arm it manually each morning.

**Feature:**
- From a trip profile, user enables a schedule: "Arm this trip Monday–Friday at 8:45 AM"
- At the scheduled time, NearWake shows a notification: "Ready to arm trip to Work? — Tap to arm"
- Tapping the notification arms the trip without opening the app
- If the user is already at their destination, the notification is suppressed

**User story:**
> "I set up my Work profile once. Every weekday morning at 8:45, NearWake taps me. I tap back. Trip is armed."

**Implementation notes:**
- `TripProfile` entity stores `scheduledDays: Set<DayOfWeek>` and `scheduledHour: Int` + `scheduledMinute: Int`
- `TripScheduleManager` — uses AlarmManager (same mechanism as `DepartureReminderScheduler`)
- Boot receiver reschedules alarms after device restart
- `ArmTripFromScheduleReceiver` — handles the tap, calls `StartTripFromProfileUseCase`

**Pillar:** 1 (Depart on time)

---

#### 1d. Home Screen Widget

**Problem today:** Checking ETA during a trip requires unlocking the phone, opening the app, navigating to LiveTrip. That is 4–5 steps for a number that should be glanceable.

**Feature — Widget 1: Active ETA Widget**
- Shows when a trip is armed: destination name + ETA countdown (e.g., "Central Station · 12 min")
- Refreshes every 60 seconds
- Tapping opens LiveTrip directly
- Shows "No active trip" when idle with a quick-arm button

**Feature — Widget 2: Quick Arm Buttons Widget**
- Shows up to 4 saved trip profiles as large tappable tiles
- Each tile: profile icon + name + estimated departure time
- Tapping a tile arms that profile immediately

**Implementation notes:**
- Jetpack Glance (`androidx.glance:glance-appwidget`)
- `ActiveTripWidgetReceiver` + `ActiveTripWidgetState` (reads from `ObserveLiveTripUseCase`)
- `QuickArmWidgetReceiver` + tap broadcasts to `StartTripFromProfileUseCase`
- Widget refresh via `GlanceAppWidgetManager.update()` triggered by TripMonitoringService state changes

**Pillar:** 1 (Depart on time), 3 (Ride awake or asleep — glanceable ETA)

---

#### 1e. Google Assistant App Actions

**Feature:**
- "Hey Google, arm my trip to work" → NearWake arms the Work profile
- "Hey Google, how long until my stop?" → Assistant reads the current ETA aloud
- "Hey Google, what's my next departure?" → reads today's departure prediction

**Implementation notes:**
- `actions.xml` in app resources defining `GET_THING` and custom intents
- `AppActionsCallback` wired to `StartTripFromProfileUseCase` and `ObserveLiveTripUseCase`
- Register shortcuts in Google Play Console after launch

**Pillar:** 1 (Depart on time)

---

### Category 2 — Commute Intelligence
**Pillar 1 (cross-cutting retention)**

These make NearWake useful even when no trip is running — daily open reasons beyond just arming a trip.

---

#### 2a. Commute Analytics Dashboard

**Problem today:** Users have no visibility into their commute patterns. Transit and Citymapper surface basic stats. NearWake has all the data in Room but shows none of it.

**Feature — new `:feature:analytics` screen:**
- Weekly summary card: trips completed, average duration, on-time %
- Time-of-day heatmap: which hours of day you travel most
- Route reliability table: for each destination — average trip time, min/max, on-time %
- Commute trend chart: average trip time per week for the past 8 weeks
- "Your fastest route to [Work] is Route 138 — consistently 4 min faster than Route 120"

**Data source:** Existing `trips` table in Room. No new data collection required. All analytics are computed client-side from `ObserveTripHistoryUseCase` and `BuildCommuteAnalyticsUseCase` (new use case).

**Implementation notes:**
- `BuildCommuteAnalyticsUseCase` in `application:trip`
- `CommuteAnalyticsViewModel` in `:feature:analytics`
- Chart library: Vico (`com.patrykandpatrick.vico:compose`) — lightweight, Compose-native
- Navigation: new route `analytics` added to `NearWakeNavHost`

**Pillar:** 1 (cross-cutting — turns trip history into depart-better intelligence)

---

#### 2b. Weekly Commute Report Notification

**Feature:**
- Every Monday at 8:00 AM: "Your week: 5 trips, avg 28 min, 4/5 on time. Your fastest day was Thursday."
- Tapping the notification opens the analytics dashboard

**Implementation notes:**
- WorkManager periodic job (`CommuteReportWorker`) scheduled weekly
- `BuildWeeklyCommuteReportUseCase` produces a summary string from trip history
- Notification via `NotificationHelper` (existing)

**Pillar:** 1

---

#### 2c. Route Comparison (from existing CommutePrediction data)

**Feature:**
- After 5+ trips to the same destination on different routes: surfaces a comparison
- "You've taken 2 routes to Work. Route A (via Maradana) averages 26 min. Route B (via Pettah) averages 31 min."
- Shown as a banner in TripSetupScreen when route data is available

**Implementation notes:**
- `CommutePredictionEngine` (already in `:domain:commute`) already groups by destination
- Extend it to group by route within destination and surface comparison if N ≥ 5

**Pillar:** 1

---

### Category 3 — Family Safety
**Pillar 8: Confirm Arrival — biggest unserved market segment**

Transit, Citymapper, Moovit, Naplarm, GPS Alarm — none of them serve the parent/child use case. This is a large emotional market: parents sending children on public transit alone.

---

#### 3a. Arrival Notification to Contact (v1.1 — no backend)

**Feature:**
- After trip completes (Stage C dismissed or walk-finish confirmed):
  - Screen: "Let someone know you arrived?" with contact picker
  - Pre-composed message: "I've arrived at [destination] ✓ — via NearWake"
  - One-tap send via Android share sheet (SMS, WhatsApp, Telegram, any messaging app)
  - Option to save "always notify this contact" for a saved trip profile

**User story:**
> "Every time my daughter arrives at school, NearWake sends me a WhatsApp automatically."

**Implementation notes:**
- Extend `CompanionUseCase` / `feature:companion` post-arrival screen
- `ArrivalMessageComposer` builds the message text
- Android `Intent.ACTION_SEND` share sheet — no backend, uses whatever messaging app the user prefers
- `SavedContact` stored in `TripProfile` entity (optional)

**Pillar:** 8 (Confirm arrival)

---

#### 3b. Trip Share Link — Live Tracking URL (v2.0, requires backend)

**Feature:**
- On trip arm: "Share your trip with a contact?" → generates nearwake.app/track/[token]
- Recipient opens the URL in any browser → sees a live map with the traveller's position
- Map auto-closes when trip ends (token expires)
- No app install required for the recipient

**Implementation notes:**
- Requires backend (Firebase Realtime Database or lightweight server)
- Phone sends encrypted location updates to backend during trip
- Web page reads from backend and renders Google Maps embed
- Token is short-lived (trip duration + 30 min)
- **Defer to Wave K (v2.0 backend wave)**

**Pillar:** 8 (Confirm arrival)

---

#### 3c. Family Account — Parent Dashboard (v2.0, requires backend)

**Feature:**
- Child installs NearWake, parent adds child's device to their Family Plan
- Parent gets push notification: "Kavya has arrived at school"
- Parent can open app and see child's active trip on a map in real time
- Child controls which contacts can see their trips — privacy-first

**Implementation notes:**
- Requires backend + Firebase Cloud Messaging for push
- Requires `READ_CONTACTS`-equivalent permission and family account linking
- Privacy: child can revoke parent access at any time
- **Defer to Wave K (v2.0)**

**Pillar:** 8 (Confirm arrival)

---

### Category 4 — Wearable + Lock Screen
**Pillar 3: Ride Awake or Asleep**

---

#### 4a. Wear OS App (v2.0)

**Feature:**
- Watch face complication showing ETA when trip is armed
- Stage B: strong haptic tap on wrist — more reliable than phone vibrating in pocket
- Stage C: continuous wrist haptic until watch-tap to dismiss
- Watch shows destination + ETA + stage indicator — same ring color system

**Implementation notes:**
- New `:feature:wear` module with Wear OS Compose
- DataLayer API (`ChannelClient`) for phone → watch real-time sync
- `WearEtaService` on the watch reads ETA updates from phone
- **Defer to Wave K (v2.0)**

**Pillar:** 3 (Ride awake or asleep — most reliable alert delivery for sleeping commuters)

---

#### 4b. Home Screen Lock-Screen Widget

**Feature:**
- Active trip ETA visible on lock screen without unlocking
- Same data source as the home screen widget

**Implementation notes:**
- Android 14+ Glance lock-screen widget (`WIDGET_CATEGORY_KEYGUARD`)
- No new data infrastructure needed — reuses widget data pipeline

**Pillar:** 3

---

#### 4c. Do Not Disturb Override (Stage C)

**Feature:**
- Stage C alert fires even if phone is in Do Not Disturb mode
- User must grant `ACCESS_NOTIFICATION_POLICY` permission
- Only Stage C overrides DND — Stage A and B respect it

**Implementation notes:**
- `NotificationHelper` — add `setCategory(NotificationCompat.CATEGORY_ALARM)` to Stage C channel
- `AudioManager` check for DND policy before playing alarm audio
- Settings screen: add "Override Do Not Disturb" toggle with permission request

**Pillar:** 3

---

## Version Wave Map

### Wave I — v1.1 — All Local, No Backend
Build these with the existing architecture. No server required.

| Feature | New modules / files | Pillar |
|---------|-------------------|--------|
| Saved Trip Profiles | `TripProfile` entity, `StartTripFromProfileUseCase`, `:feature:profiles` | 1 + 3 |
| Per-route bias override | `alwaysUseSleepMode` on `SavedPlace`, MIGRATION_7_8 | 3 |
| Scheduled recurring trips | `TripScheduleManager`, `ArmTripFromScheduleReceiver` | 1 |
| Home screen widget — ETA | Glance `ActiveTripWidget` | 1 + 3 |
| Home screen widget — Quick Arm | Glance `QuickArmWidget` | 1 |
| Arrival SMS to contact | Extend `CompanionUseCase`, `ArrivalMessageComposer` | 8 |

### Wave J — v1.5 — New Integrations, No Backend
New data sources and smarter intelligence, still fully local.

| Feature | New modules / files | Pillar |
|---------|-------------------|--------|
| Google Calendar integration | `CalendarEventReader`, `CalendarTripSuggestor` | 1 |
| Commute Analytics dashboard | `BuildCommuteAnalyticsUseCase`, `:feature:analytics` | 1 |
| Weekly commute report | `CommuteReportWorker` | 1 |
| Route comparison | Extend `CommutePredictionEngine` | 1 |
| Google Assistant App Actions | `actions.xml`, `AppActionsCallback` | 1 |
| DND override for Stage C | `NotificationHelper` update, `ACCESS_NOTIFICATION_POLICY` | 3 |

### Wave K — v2.0 — Backend + Platform Expansion
Requires server infrastructure. Build after v1.0 reaches product-market fit.

| Feature | New modules / infrastructure | Pillar |
|---------|------------------------------|--------|
| Wear OS companion app | `:feature:wear`, DataLayer API | 3 |
| Trip share link (live tracking URL) | Backend (Firebase or server), web page | 8 |
| Family account (parent/child) | Backend, FCM push, `:feature:family` | 8 |
| Cloud sync (multi-device) | Backend, sync protocol | Cross-cutting |
| Firebase Remote Config live tuning | `:core:remoteconfig` fully wired | Cross-cutting |
| iOS app | Separate project or KMM shared domain | All |

---

## Monetization Model

**Core principle:** The alert path is free — always. No user who arms a trip is ever blocked from getting alerted. Monetization is on intelligence, automation, and social features.

### Free Tier

| Feature | Limit |
|---------|-------|
| Core arrival alarm — all 3 stages | Unlimited |
| Saved places | 3 |
| Trip history | 30 days |
| Trip profiles | 1 ("Home" preset) |
| Home screen widget (ETA only) | ✅ |
| Arrival SMS (manual, one tap) | ✅ |
| Commute analytics (last 7 days) | ✅ |
| DND override | ✅ |

### Premium — £2.49/month or £14.99/year

| Feature | Included |
|---------|---------|
| Everything in Free | ✅ |
| Saved places | Unlimited |
| Trip history | Unlimited |
| Trip profiles | Unlimited |
| Scheduled recurring trips | ✅ |
| Calendar integration | ✅ |
| Home screen widget — Quick Arm | ✅ |
| Commute analytics — full history | ✅ |
| Weekly commute report | ✅ |
| Google Assistant integration | ✅ |
| Wear OS app (v2.0) | ✅ |
| Arrival auto-message (saved contact) | ✅ |

### Family Plan — £4.99/month (v2.0)

| Feature | Included |
|---------|---------|
| Everything in Premium | ✅ |
| 1 parent + up to 4 children | ✅ |
| Parent arrival push notifications | ✅ |
| Parent live trip map view | ✅ |
| Trip share links | ✅ |

**Ads:** Never. Not in the free tier, not in the paid tier, not in the alert path. This is a non-negotiable product commitment stated publicly in the Play Store listing.

---

## Non-Negotiables (Unchanged from v1.0)

These commitments apply to every feature in every wave:

1. **Alert path always free.** Any user who arms a trip gets all 3 stages regardless of tier.
2. **No ads — ever.** Not in free tier, not as an opt-in.
3. **No account required for core value.** Free tier works fully offline and without registration.
4. **Confidence-first.** Every new prediction surface shows confidence.
5. **Fail visible, not silent.** Any new integration that fails (Calendar read error, network timeout) must show a visible degraded state — not silently proceed.
6. **Privacy-respecting.** Family tracking features are opt-in, child-controlled, and local-first where possible.
7. **Pillar-gated.** No feature ships that does not clearly fit one of the eight pillars.

---

## What We Will Not Build (Expanded)

From PRODUCT_COMPARE_REFERENCE.md §12 plus additions:

- Full transit route planner (Citymapper trap — scope creep with no differentiating advantage)
- Live vehicle map (Transit trap — expensive real-time infrastructure, not our core value)
- Ticketing / payments (liability and compliance overhead not worth it)
- Social / community disruption reports (Waze-style — requires large user base to be useful)
- AR stop finder (novelty feature, no pillar alignment)
- Mandatory accounts (kills frictionless onboarding)
- Continuous cloud location outside of opt-in family features
- Safety/SOS platform — emergency contacts, 24/7 monitoring (liability trap, mission creep)
- Ads of any kind
- Anything that gates the 3-stage alert system behind a paywall

---

## Priority Order Summary

If forced to pick 3 features to build first after v1.0 launch:

1. **Saved Trip Profiles + Scheduled Trips** — eliminates the biggest daily friction for regular commuters. Users who arm the same trip every day see immediate value. Directly threatens Naplarm and GPS Alarm's auto-arm features.

2. **Home Screen Widget (ETA)** — makes NearWake visible on the home screen even when no trip is running. Increases brand presence, reduces "I forgot to arm it" complaints.

3. **Commute Analytics Dashboard** — turns passive trip data into insight. Creates a new reason to open the app when not commuting. Data is already in Room — this is mostly UI work.

These three require zero backend infrastructure and can ship in v1.1.

---

---

# Detailed Development Plan

This section provides sprint-by-sprint, file-by-file implementation instructions for every feature in Wave I and Wave J. Wave K (backend) requires a separate server-side design document once v1.0 reaches product-market fit.

---

## Architecture Rules (Apply to Every Feature Below)

1. **No feature module imports `core:database` directly.** All persistence goes through `ports:persistence` (`TripLifecycleStore`) or a new dedicated port.
2. **Every new use case lives in `application:trip`** (or a new `application:*` module if the domain is truly separate).
3. **Every new ViewModel uses `viewModelScope` only.** No manual coroutine scopes.
4. **Every new Room entity gets a schema export and a numbered migration.** Current schema = v7 (after Wave H token changes). New migrations start at v8.
5. **Every new feature module depends on `application:*` and `ports:*` only** — never on `data:*` or `core:database`.
6. **New Hilt modules go in the relevant `data:*` module** — not in `app`.

---

## Wave I Development Plan — v1.1

---

### Feature I-1: Saved Trip Profiles

**Goal:** Users save named trip configurations (destination + settings). HomeScreen shows them as one-tap arm buttons.

---

#### Step I-1-A: Domain model

**File:** `domain/trip/src/main/kotlin/com/nearwake/domain/trip/model/TripProfile.kt` *(new)*

```kotlin
data class TripProfile(
    val id: String,
    val name: String,                    // "Work", "Home", "Gym"
    val destinationPlaceId: String,
    val destinationName: String,
    val leadTimeMinutes: Int,
    val alertMode: AlertMode,            // ACTIVE / SLEEP
    val triggerMode: AlertTriggerMode,   // TIME / DISTANCE / BOTH
    val alwaysUseSleepMode: Boolean,
    val scheduledDays: Set<DayOfWeek>,   // empty = no schedule
    val scheduledHour: Int,
    val scheduledMinute: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant?,
)
```

No changes to existing domain models.

---

#### Step I-1-B: Database entity + DAO + migration

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/entity/TripProfileEntity.kt` *(new)*

```kotlin
@Entity(tableName = "trip_profiles")
data class TripProfileEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "destination_place_id") val destinationPlaceId: String,
    @ColumnInfo(name = "destination_name") val destinationName: String,
    @ColumnInfo(name = "lead_time_minutes") val leadTimeMinutes: Int,
    @ColumnInfo(name = "alert_mode") val alertMode: String,
    @ColumnInfo(name = "trigger_mode") val triggerMode: String,
    @ColumnInfo(name = "always_sleep_mode") val alwaysSleepMode: Boolean,
    @ColumnInfo(name = "scheduled_days_json") val scheduledDaysJson: String,
    @ColumnInfo(name = "scheduled_hour") val scheduledHour: Int,
    @ColumnInfo(name = "scheduled_minute") val scheduledMinute: Int,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "last_used_at") val lastUsedAt: Instant?,
)
```

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/dao/TripProfileDao.kt` *(new)*

```kotlin
@Dao
interface TripProfileDao {
    @Query("SELECT * FROM trip_profiles ORDER BY last_used_at DESC")
    fun observeProfiles(): Flow<List<TripProfileEntity>>

    @Query("SELECT * FROM trip_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String): TripProfileEntity?

    @Upsert
    suspend fun upsertProfile(profile: TripProfileEntity)

    @Query("DELETE FROM trip_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)

    @Query("UPDATE trip_profiles SET last_used_at = :usedAt WHERE id = :id")
    suspend fun markUsed(id: String, usedAt: Instant)
}
```

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/NearWakeDatabase.kt`
- Add `TripProfileEntity::class` to `@Database(entities = [...])`
- Add `abstract fun tripProfileDao(): TripProfileDao`

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/di/DatabaseModule.kt`
- Add `MIGRATION_7_8` creating the `trip_profiles` table (same SQL as entity schema)
- Add `provideTripProfileDao` provider

**Schema version:** bump to `version = 8`

---

#### Step I-1-C: Port interface extension

**File:** `ports/persistence/src/main/kotlin/com/nearwake/ports/persistence/TripLifecycleStore.kt`

Add 4 methods:
```kotlin
fun observeTripProfiles(): Flow<List<TripProfile>>
suspend fun getTripProfile(id: String): TripProfile?
suspend fun saveTripProfile(profile: TripProfile)
suspend fun deleteTripProfile(id: String)
suspend fun markTripProfileUsed(id: String)
```

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/persistence/RoomTripLifecycleStore.kt`
- Implement the 5 new methods using `TripProfileDao`
- Add `TripProfileEntity ↔ TripProfile` mapper functions in `TripProfileMapper.kt` *(new in `core/database`)*

---

#### Step I-1-D: Use cases

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/ObserveTripProfilesUseCase.kt` *(new)*
```kotlin
class ObserveTripProfilesUseCase @Inject constructor(
    private val store: TripLifecycleStore,
) {
    operator fun invoke(): Flow<List<TripProfile>> = store.observeTripProfiles()
}
```

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/SaveTripProfileUseCase.kt` *(new)*
- Validates name not empty, destination not blank
- Calls `store.saveTripProfile()`

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/DeleteTripProfileUseCase.kt` *(new)*

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/StartTripFromProfileUseCase.kt` *(new)*
- Loads profile from store
- Resolves destination `SavedPlace`
- Calls `StartTripUseCase` with profile settings pre-filled
- Calls `store.markTripProfileUsed(profile.id)`

---

#### Step I-1-E: Feature screen

**New module:** `:feature:profiles`
- `build.gradle.kts`: depends on `application:trip`, `core:ui`, `core:designsystem`
- `TripProfilesScreen.kt` — list of saved profiles with edit/delete swipe actions
- `EditTripProfileScreen.kt` — form: name + destination picker + settings (same UI pattern as TripSetupScreen but saves to profile instead of arming)
- `TripProfilesViewModel.kt` — `ObserveTripProfilesUseCase`, `SaveTripProfileUseCase`, `DeleteTripProfileUseCase`

**HomeScreen changes** (`app/src/main/kotlin/com/nearwake/app/HomeScreen.kt`):
- Read profiles from `HomeViewModel` (inject `ObserveTripProfilesUseCase`)
- Show up to 4 profiles as `ProfileQuickArmRow` below the search bar
- Tapping a row calls `StartTripFromProfileUseCase` and navigates to LiveTrip

**Navigation:** Add routes `profiles`, `edit_profile/{profileId}` to `NearWakeNavHost.kt`

---

#### Step I-1-F: Tests

- `SaveTripProfileUseCaseTest` — validates empty name rejection, saves correctly
- `StartTripFromProfileUseCaseTest` — profile loads → trip arms with correct settings
- `TripProfileDaoTest` — observe, upsert, delete, markUsed

---

### Feature I-2: Per-Route Bias Override

**Goal:** When a user always sleeps on a specific route, NearWake remembers and auto-applies Sleep mode + max bias for that destination.

---

#### Step I-2-A: Schema change

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/entity/SavedPlaceEntity.kt`
- Add: `@ColumnInfo(name = "always_sleep_mode") val alwaysSleepMode: Boolean = false`

**File:** `core/database/src/main/kotlin/com/nearwake/core/database/di/DatabaseModule.kt`
- Add `MIGRATION_8_9` (if after I-1) or fold into `MIGRATION_7_8`:
  ```sql
  ALTER TABLE saved_places ADD COLUMN always_sleep_mode INTEGER NOT NULL DEFAULT 0
  ```

---

#### Step I-2-B: Use case

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/UpdatePlaceSleepPreferenceUseCase.kt` *(new)*
```kotlin
suspend operator fun invoke(placeId: String, alwaysSleepMode: Boolean)
```

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/LoadTripSetupPreviewUseCase.kt`
- Return value: add `suggestedAlertMode: AlertMode` field
- Logic: if `savedPlace.alwaysSleepMode == true` → return `AlertMode.SLEEP`

---

#### Step I-2-C: TripSetupScreen

**File:** `feature/tripsetup/src/main/kotlin/com/nearwake/feature/tripsetup/TripSetupScreen.kt`
- In the Advanced section (already collapsed): add toggle "I usually sleep on this route"
- Pre-filled from `LoadTripSetupPreviewUseCase.suggestedAlertMode`
- On toggle change: call `UpdatePlaceSleepPreferenceUseCase`

---

### Feature I-3: Scheduled Recurring Trips

**Goal:** Trip profiles can have a weekly schedule. At the scheduled time, a notification fires and one tap arms the trip.

---

#### Step I-3-A: Schedule manager

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/TripScheduleManager.kt` *(new)*

```kotlin
@Singleton
class TripScheduleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val alarmManager: AlarmManager,
) {
    fun schedule(profile: TripProfile) {
        // For each day in profile.scheduledDays, compute next occurrence
        // Set an exact AlarmManager alarm with a PendingIntent to ArmTripFromScheduleReceiver
    }

    fun cancel(profileId: String) {
        // Cancel all alarms for this profileId
    }

    fun rescheduleAll(profiles: List<TripProfile>) {
        // Called from boot receiver — reschedule all profiles that have schedules
    }
}
```

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/ArmTripFromScheduleReceiver.kt` *(new)*
- `BroadcastReceiver` — receives alarm intent with `profileId` extra
- Starts a `CoroutineScope`, calls `StartTripFromProfileUseCase`
- Posts a notification "Arming trip to [destination]..." with a cancel action

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/DepartureReminderBootReceiver.kt` *(extend existing)*
- Already handles departure reminders — extend to also call `TripScheduleManager.rescheduleAll()`

**AndroidManifest.xml:**
- Register `ArmTripFromScheduleReceiver` with `RECEIVE_BOOT_COMPLETED` and `SCHEDULE_EXACT_ALARM` permissions

---

#### Step I-3-B: Wire schedule management

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/SaveTripProfileUseCase.kt`
- After saving profile: if `scheduledDays` is not empty, call `TripScheduleManager.schedule(profile)`

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/DeleteTripProfileUseCase.kt`
- Before deleting: call `TripScheduleManager.cancel(profileId)`

---

#### Step I-3-C: EditTripProfileScreen additions

**File:** `feature/profiles/src/main/kotlin/com/nearwake/feature/profiles/EditTripProfileScreen.kt`
- Add day-of-week toggle row (M T W T F S S)
- Add time picker (HH:MM)
- Both visible only when "Schedule this trip" switch is ON

---

### Feature I-4: Home Screen Widgets

**Goal:** Two Glance widgets — one showing active ETA, one showing quick-arm profile buttons.

---

#### Step I-4-A: Glance dependency

**File:** `app/build.gradle.kts`
```kotlin
implementation(libs.glance.appwidget)
implementation(libs.glance.material3)
```

**File:** `gradle/libs.versions.toml`
```toml
glance-appwidget = { group = "androidx.glance", name = "glance-appwidget", version = "1.1.0" }
glance-material3 = { group = "androidx.glance", name = "glance-material3", version = "1.1.0" }
```

---

#### Step I-4-B: Active ETA Widget

**File:** `app/src/main/kotlin/com/nearwake/app/widget/ActiveTripWidget.kt` *(new)*

```kotlin
class ActiveTripWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val state = getAppWidgetState(context, ActiveTripWidgetState.serializer(), id)
        provideContent {
            when {
                state.isActive -> ActiveEtaContent(state.destinationName, state.etaMinutes)
                else -> IdleContent()
            }
        }
    }
}
```

State updated by `TripMonitoringService` — every time ETA changes, calls:
```kotlin
ActiveTripWidget().updateAll(context)
```

**File:** `app/src/main/res/xml/active_trip_widget_info.xml`
- `minWidth = 180dp`, `minHeight = 80dp`, `resizeMode = horizontal|vertical`
- `updatePeriodMillis = 0` (manual updates only — we push from service)

**AndroidManifest.xml:**
- Register `ActiveTripWidgetReceiver` with `APPWIDGET_UPDATE` action

---

#### Step I-4-C: Quick Arm Widget

**File:** `app/src/main/kotlin/com/nearwake/app/widget/QuickArmWidget.kt` *(new)*
- Shows up to 4 `TripProfile` names as large tappable buttons
- Each button sends `ACTION_ARM_PROFILE` broadcast with `profileId` extra
- `QuickArmWidget` reads profiles from `TripLifecycleStore` via a coroutine in `provideGlance`

**File:** `app/src/main/res/xml/quick_arm_widget_info.xml`
- `minWidth = 180dp`, `minHeight = 160dp`

---

### Feature I-5: Arrival SMS to Contact

**Goal:** After trip completion, one-tap to send an arrival message via any installed messaging app.

---

#### Step I-5-A: Arrival message composer

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/BuildArrivalMessageUseCase.kt` *(new)*
```kotlin
fun invoke(destinationName: String, contactName: String?): String {
    val recipient = contactName ?: "someone"
    return "Hi $recipient — I've arrived at $destinationName. (Sent via NearWake)"
}
```

---

#### Step I-5-B: CompanionScreen update

**File:** `feature/companion/src/main/kotlin/com/nearwake/feature/companion/CompanionScreen.kt`
- Add "Let someone know" section below arrival confirmation
- Shows contact picker button + text preview
- On tap: fires `Intent.ACTION_SEND` with `type = "text/plain"` and composed message
- If profile has a saved contact: pre-fills the contact name in the message

---

---

## Wave J Development Plan — v1.5

---

### Feature J-1: Google Calendar Integration

**Goal:** NearWake reads the user's calendar, finds events with location addresses, and suggests arming a trip before those events.

---

#### Step J-1-A: Calendar reader

**File:** `data/calendar/src/main/kotlin/com/nearwake/data/calendar/CalendarEventReader.kt` *(new module `:data:calendar`)*

```kotlin
class CalendarEventReader @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    suspend fun getUpcomingEventsWithLocation(withinHours: Int = 4): List<CalendarEvent> {
        // ContentResolver query on CalendarContract.Events
        // Filter: dtstart within next `withinHours` hours, location != null/blank
        // Returns List<CalendarEvent(title, locationAddress, startTimeMs)>
    }
}
```

`CalendarEvent` data class:
```kotlin
data class CalendarEvent(
    val id: Long,
    val title: String,
    val locationAddress: String,
    val startTimeMs: Long,
    val suggestedLeaveByMs: Long,   // startTimeMs - averageCommuteMs - 15 min buffer
)
```

**Required permission:** `READ_CALENDAR` — add to AndroidManifest and PermissionsScreen

---

#### Step J-1-B: CalendarTripSuggestor

**File:** `data/calendar/src/main/kotlin/com/nearwake/data/calendar/CalendarTripSuggestor.kt` *(new)*
- Runs as a `WorkManager` periodic job every 30 min when calendar permission is granted
- For each upcoming event: resolves location via Google Places geocoding → gets `placeId`
- Posts a notification 90 min before event: "Meeting at [Title] at 2:00 PM — leave by 1:15 PM?"
- Notification actions: "Set up trip" (opens TripSetupScreen pre-filled) or "Dismiss"

**Required permission:** calendar access requires `READ_CALENDAR` manifest permission + runtime request.

---

#### Step J-1-C: CalendarModule

**File:** `data/calendar/src/main/kotlin/com/nearwake/data/calendar/di/CalendarModule.kt` *(new)*
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object CalendarModule {
    @Provides @Singleton
    fun provideCalendarEventReader(@ApplicationContext ctx: Context) = CalendarEventReader(ctx)
}
```

---

#### Step J-1-D: PermissionsScreen update

**File:** `feature/permissions/src/main/kotlin/com/nearwake/feature/permissions/PermissionsScreen.kt`
- Add optional "Calendar" permission card — labeled "Smart departure suggestions (optional)"
- Not required for core flow — only surfaced if user taps "Enable smart suggestions" in Settings

**File:** `feature/settings/src/main/kotlin/com/nearwake/feature/settings/SettingsScreen.kt`
- Add row: "Smart departure suggestions — reads calendar for upcoming events" + toggle
- Tapping toggle → requests `READ_CALENDAR` if not granted

---

### Feature J-2: Commute Analytics Dashboard

**Goal:** New screen showing personal commute patterns derived from existing trip history in Room.

---

#### Step J-2-A: Analytics use case

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/BuildCommuteAnalyticsUseCase.kt` *(new)*

Input: `List<Trip>` from `ObserveTripHistoryUseCase`

Output: `CommuteAnalytics` data class:
```kotlin
data class CommuteAnalytics(
    val totalTrips: Int,
    val onTimePercentage: Float,          // trips completed before or at scheduled arrival
    val averageDurationMinutes: Float,
    val longestTripMinutes: Int,
    val shortestTripMinutes: Int,
    val busiestDayOfWeek: DayOfWeek,
    val busiestHourOfDay: Int,
    val topDestinations: List<DestinationStat>,
    val weeklyTrend: List<WeeklyTripSummary>,   // last 8 weeks
)

data class DestinationStat(
    val destinationName: String,
    val tripCount: Int,
    val avgDurationMinutes: Float,
    val onTimePercentage: Float,
)

data class WeeklyTripSummary(
    val weekStart: LocalDate,
    val tripCount: Int,
    val avgDurationMinutes: Float,
)
```

All computation is pure — no database calls, just transforms the input list.

---

#### Step J-2-B: Feature module

**New module:** `:feature:analytics`
- `build.gradle.kts`: depends on `application:trip`, `core:ui`, `core:designsystem`, Vico chart library

**File:** `feature/analytics/src/main/kotlin/com/nearwake/feature/analytics/AnalyticsScreen.kt` *(new)*

Screen layout:
```
SectionHeader: THIS WEEK
  SurfaceCard: 3 stat chips (X trips · Y min avg · Z% on time)

SectionHeader: TOP DESTINATIONS
  For each destination: Row (place icon + name + avg time + on-time %)

SectionHeader: COMMUTE TREND (last 8 weeks)
  LineChart (Vico): x = week, y = avg duration minutes
  Tap a point to see that week's trips

SectionHeader: BUSIEST TIME
  Heatmap: 7 columns (days) × 24 rows (hours) with dot intensity
```

**File:** `feature/analytics/src/main/kotlin/com/nearwake/feature/analytics/AnalyticsViewModel.kt` *(new)*
```kotlin
@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val observeTripHistory: ObserveTripHistoryUseCase,
    private val buildAnalytics: BuildCommuteAnalyticsUseCase,
) : ViewModel() {
    val analytics: StateFlow<CommuteAnalytics?> = observeTripHistory()
        .map { trips -> buildAnalytics(trips) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
}
```

**Navigation:** Add route `analytics` to `NearWakeNavHost`. Add Analytics icon to HomeScreen top bar (history icon or chart icon).

**Chart library:** Vico (`com.patrykandpatrick.vico:compose-m3` — 200KB, Compose-native, no heavyweight dependencies)

---

#### Step J-2-C: Tests

- `BuildCommuteAnalyticsUseCaseTest` — various trip list inputs → verify percentages, trend, top destinations
- Edge cases: no trips, single trip, all trips to same destination

---

### Feature J-3: Weekly Commute Report Notification

---

#### Step J-3-A: Worker

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/CommuteReportWorker.kt` *(new)*

```kotlin
@HiltWorker
class CommuteReportWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val observeTripHistory: ObserveTripHistoryUseCase,
    private val buildAnalytics: BuildCommuteAnalyticsUseCase,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val trips = observeTripHistory().first()
        val analytics = buildAnalytics(trips.takeLast(35)) // last 5 weeks
        val thisWeek = analytics.weeklyTrend.lastOrNull() ?: return Result.success()
        notificationHelper.postCommuteReport(
            tripCount = thisWeek.tripCount,
            avgMinutes = thisWeek.avgDurationMinutes.toInt(),
            onTimePct = analytics.onTimePercentage,
        )
        return Result.success()
    }
}
```

**File:** `app/src/main/kotlin/com/nearwake/app/NearWakeApp.kt`
- On app start, enqueue `CommuteReportWorker` as a `PeriodicWorkRequest` with `MONDAY` constraint:
  ```kotlin
  WorkManager.getInstance(this).enqueueUniquePeriodicWork(
      "commute_report",
      ExistingPeriodicWorkPolicy.KEEP,
      PeriodicWorkRequestBuilder<CommuteReportWorker>(7, TimeUnit.DAYS)
          .setInitialDelay(nextMondayDelay(), TimeUnit.MILLISECONDS)
          .build()
  )
  ```

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/NotificationHelper.kt`
- Add `postCommuteReport(tripCount, avgMinutes, onTimePct)` method
- Uses a new `CHANNEL_WEEKLY_REPORT` notification channel (importance = DEFAULT, no sound)

---

### Feature J-4: Google Assistant App Actions

---

#### Step J-4-A: Actions definition

**File:** `app/src/main/res/xml/shortcuts.xml` *(new or extend existing)*

```xml
<shortcuts xmlns:android="http://schemas.android.com/apk/res/android">
  <shortcut
      android:shortcutId="arm_work_trip"
      android:enabled="true"
      android:shortcutShortLabel="@string/shortcut_arm_work">
    <intent
        android:action="com.nearwake.ACTION_ARM_PROFILE"
        android:targetPackage="com.nearwake.app"
        android:targetClass="com.nearwake.app.MainActivity">
      <extra android:name="profile_name" android:value="Work"/>
    </intent>
  </shortcut>
</shortcuts>
```

**File:** `app/src/main/res/xml/actions.xml` *(new)*
- Defines `GET_THING` intent for ETA query
- Defines custom `arm_trip` intent with `profile_name` parameter

---

#### Step J-4-B: MainActivity intent handling

**File:** `app/src/main/kotlin/com/nearwake/app/MainActivity.kt`
- In `onCreate` / `onNewIntent`: check for `ACTION_ARM_PROFILE` intent
- If found: lookup profile by name, call `StartTripFromProfileUseCase`, navigate to LiveTrip
- If `ACTION_QUERY_ETA`: read current ETA from `ObserveLiveTripUseCase`, reply via `AppActionsCallback`

---

### Feature J-5: DND Override for Stage C

---

#### Step J-5-A: Permission + channel

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/NotificationHelper.kt`
- Stage C channel: add `setCategory(NotificationCompat.CATEGORY_ALARM)`
- This makes the notification break through DND by default on Android 8+

**File:** `feature/settings/src/main/kotlin/com/nearwake/feature/settings/SettingsScreen.kt`
- Add row: "Override Do Not Disturb for Stage C alarm"
- Toggle — when turned ON: request `ACCESS_NOTIFICATION_POLICY`
- When granted: `NotificationManager.setInterruptionFilter(INTERRUPTION_FILTER_NONE)` briefly during Stage C, then restore

**File:** `core/datastore/src/main/kotlin/com/nearwake/core/datastore/UserPreferences.kt`
- Add `overrideDnd: Boolean` preference (default `false`)

---

### Feature I-6: Lock-Screen Widget

**Goal:** Active trip ETA visible on the lock screen without the user needing to unlock their phone.

---

#### Step I-6-A: Dependency

Lock-screen widgets on Android 14+ use the same Glance infrastructure as home screen widgets. No extra dependency — already added in I-4-A.

---

#### Step I-6-B: Widget declaration

**File:** `app/src/main/res/xml/active_trip_widget_info.xml` *(extend from I-4-B)*

Add the `widgetCategory` attribute so the widget appears in both the home screen picker and the lock screen picker:

```xml
<appwidget-provider
    ...
    android:widgetCategory="home_screen|keyguard"
    android:initialKeyguardLayout="@layout/active_trip_widget_lock" />
```

**File:** `app/src/main/res/layout/active_trip_widget_lock.xml` *(new)*
- A simpler RemoteViews layout (lock-screen only supports a subset of views)
- Large ETA number + destination name — two TextViews, no clickable elements (lock-screen restriction)
- Dark background with 60% opacity — legible over any wallpaper

---

#### Step I-6-C: Glance content for keyguard

**File:** `app/src/main/kotlin/com/nearwake/app/widget/ActiveTripWidget.kt` *(extend from I-4-B)*

```kotlin
override suspend fun provideGlance(context: Context, id: GlanceId) {
    val state = getAppWidgetState(context, ActiveTripWidgetState.serializer(), id)
    val isLockScreen = GlanceAppWidgetManager(context)
        .getAppWidgetSizes(this, id).any { it.isKeyguard }

    provideContent {
        when {
            isLockScreen -> LockScreenEtaContent(state)
            state.isActive -> ActiveEtaContent(state)
            else -> IdleContent()
        }
    }
}
```

`LockScreenEtaContent`:
- `Text(state.etaMinutes.toString(), style = displayMedium, color = Color.White)`
- `Text("min · ${state.destinationName}", style = titleSmall, color = Color.White.copy(alpha = 0.8f))`
- No `clickable` modifier — lock-screen widgets cannot open activities directly on Android 12+

---

#### Step I-6-D: AndroidManifest

```xml
<receiver android:name=".widget.ActiveTripWidgetReceiver"
    android:exported="true">
  <intent-filter>
    <action android:name="android.appwidget.action.APPWIDGET_UPDATE"/>
  </intent-filter>
  <!-- existing home_screen category already declared -->
</receiver>
```

No additional permissions needed beyond what I-4 already declared.

---

#### Step I-6-E: Tests

- Manual test only: install widget on lock screen on API 34+ device, verify ETA updates when trip is running
- Verify no `SecurityException` on lock-screen widget registration on API < 28 (fallback: widget only appears in home screen picker)

---

---

### Feature J-6: Route Comparison

**Goal:** After a user has taken 5+ trips to the same destination via different routes, surface a comparison showing which route is faster and more reliable.

---

#### Step J-6-A: Extend CommutePredictionEngine

**File:** `domain/commute/src/main/kotlin/com/nearwake/domain/commute/CommutePredictionEngine.kt` *(extend)*

Current behavior: groups trips by destination → builds a `CommutePrediction` per destination.

New behavior: within each destination group, further sub-group by `routeId` (first stop ID of the route snapshot). If a destination has 2+ route sub-groups each with ≥ 3 trips, produce a `RouteComparisonResult`.

```kotlin
data class RouteComparisonResult(
    val destinationId: String,
    val destinationName: String,
    val routes: List<RouteOption>,
)

data class RouteOption(
    val routeLabel: String,          // e.g. "via Maradana" — derived from first intermediate stop
    val avgDurationMinutes: Float,
    val tripCount: Int,
    val onTimePercentage: Float,     // trips where arrival was ≤ 2 min of predicted
    val isFastest: Boolean,
)
```

**Threshold:** minimum 3 trips per route, minimum 2 distinct routes to show comparison.

---

#### Step J-6-B: Use case

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/ObserveRouteComparisonUseCase.kt` *(new)*

```kotlin
class ObserveRouteComparisonUseCase @Inject constructor(
    private val store: TripLifecycleStore,
    private val engine: CommutePredictionEngine,
) {
    operator fun invoke(destinationId: String): Flow<RouteComparisonResult?> =
        store.observeTripHistoryForDestination(destinationId)
            .map { trips -> engine.buildRouteComparison(trips) }
}
```

Add `observeTripHistoryForDestination(destinationId: String): Flow<List<Trip>>` to `TripLifecycleStore` + implement in `RoomTripLifecycleStore` using an existing DAO query filtered by destination.

---

#### Step J-6-C: TripSetupScreen banner

**File:** `feature/tripsetup/src/main/kotlin/com/nearwake/feature/tripsetup/TripSetupViewModel.kt`
- Collect `ObserveRouteComparisonUseCase` for the selected destination
- Expose `routeComparison: RouteComparisonResult?` in `TripSetupUiState`

**File:** `feature/tripsetup/src/main/kotlin/com/nearwake/feature/tripsetup/TripSetupScreen.kt`
- If `routeComparison` is not null and has 2+ options: show an inline `RouteComparisonBanner` composable below the route preview row
- Banner: `"Route A averages 26 min · Route B averages 31 min — Route A is usually faster"`
- Height: 48dp, no card — just an informational `Row` with a `bolt` icon and `bodyMedium` text

---

#### Step J-6-D: Analytics screen card

**File:** `feature/analytics/src/main/kotlin/com/nearwake/feature/analytics/AnalyticsScreen.kt`
- Add section `ROUTE COMPARISONS` below top destinations
- For each destination with a comparison: expandable row showing the route table
- Column headers: Route | Avg time | Trips | On-time %

---

#### Step J-6-E: Tests

**File:** `domain/commute/src/test/kotlin/com/nearwake/domain/commute/CommutePredictionEngineTest.kt` *(extend)*
- Test: 6 trips to Work via Route A, 4 trips via Route B → `RouteComparisonResult` produced correctly
- Test: only 1 route available → `RouteComparisonResult` is null (no comparison)
- Test: fewer than 3 trips per route → comparison suppressed

---

---

## Wave K Development Plan — v2.0

Wave K requires a Firebase backend. Build after v1.0 proves product-market fit (PMF). Target PMF signal: 500 daily active users with ≥ 3 trips each per week.

---

### K-0: Backend Infrastructure Setup

**Firebase project setup:**
1. Create Firebase project `nearwake-prod` in Firebase Console
2. Enable: Authentication, Firestore, Realtime Database, Cloud Functions, Cloud Messaging (FCM), Hosting
3. Download `google-services.json` → add to `app/` folder
4. Add to `app/build.gradle.kts`:
   ```kotlin
   implementation(platform(libs.firebase.bom))
   implementation(libs.firebase.auth)
   implementation(libs.firebase.firestore)
   implementation(libs.firebase.database)
   implementation(libs.firebase.functions)
   implementation(libs.firebase.messaging)
   ```
5. Add to `gradle/libs.versions.toml`:
   ```toml
   firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version = "33.x.x" }
   ```

**Firestore security rules** — written before any user data is stored:
- A user can only read/write their own documents
- Family members can read (not write) child trip status within their family group
- Trip share tokens are readable by anyone, writable only by the token creator

**Firebase emulator suite** — used in all local development and CI:
```bash
firebase emulators:start --only auth,firestore,database,functions
```

---

### K-1: Anonymous Auth + Account Upgrade

All Wave K features require a user identity. But requiring registration kills onboarding.

**Strategy:** Sign in anonymously on first launch. Offer email/phone upgrade when accessing family features.

**File:** `core/auth/src/main/kotlin/com/nearwake/core/auth/NearWakeAuthManager.kt` *(new module `:core:auth`)*

```kotlin
@Singleton
class NearWakeAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
) {
    val currentUser: FirebaseUser? get() = firebaseAuth.currentUser

    suspend fun ensureSignedIn(): FirebaseUser {
        return currentUser ?: firebaseAuth.signInAnonymously().await().user!!
    }

    suspend fun upgradeToEmail(email: String, password: String): FirebaseUser {
        val credential = EmailAuthProvider.getCredential(email, password)
        return firebaseAuth.currentUser!!.linkWithCredential(credential).await().user!!
    }
}
```

**Rule:** Anonymous users have full access to all features. The upgrade prompt only appears when setting up a family group (K-4). Never block core features behind account creation.

---

### K-2: Wear OS App

**Goal:** ETA visible on wrist. Stage B + Stage C haptic on wrist — more reliable than phone in pocket.

---

#### Step K-2-A: New Wear OS module

**New module:** `:feature:wear` in `settings.gradle.kts`

```kotlin
// feature/wear/build.gradle.kts
plugins {
    id("com.android.application")   // Wear OS module is a separate APK
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
}
android {
    defaultConfig {
        minSdk = 26   // Wear OS 3.0+
        targetSdk = 35
    }
}
dependencies {
    implementation(libs.wear.compose.material)
    implementation(libs.wear.compose.foundation)
    implementation(libs.wearable.data.layer)
    implementation(libs.play.services.wearable)
}
```

---

#### Step K-2-B: Phone → Watch data bridge

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/WearBridgeService.kt` *(new — on phone)*

```kotlin
@AndroidEntryPoint
class WearBridgeService : WearableListenerService() {
    @Inject lateinit var observeLiveTrip: ObserveLiveTripUseCase

    override fun onCreate() {
        super.onCreate()
        serviceScope.launch {
            observeLiveTrip().collect { tripState ->
                sendEtaToWatch(tripState.etaMinutes, tripState.destinationName, tripState.stage)
            }
        }
    }

    private suspend fun sendEtaToWatch(etaMin: Int, destination: String, stage: AlertStage) {
        val dataMap = DataMapItem.fromDataItem(putDataMapRequest("/trip/eta").apply {
            dataMap.putInt("eta_minutes", etaMin)
            dataMap.putString("destination", destination)
            dataMap.putString("stage", stage.name)
            dataMap.putLong("timestamp", System.currentTimeMillis())
        }.asPutDataRequest()).dataMap
        Wearable.getDataClient(this).putDataItem(dataMap.asPutDataRequest()).await()
    }
}
```

**File:** `AndroidManifest.xml` (phone)
```xml
<service android:name=".data.alerts.WearBridgeService"
    android:exported="true">
  <intent-filter>
    <action android:name="com.google.android.gms.wearable.DATA_CHANGED"/>
  </intent-filter>
</service>
```

---

#### Step K-2-C: Watch app screens

**File:** `feature/wear/src/main/kotlin/com/nearwake/wear/WearMainActivity.kt` *(new)*
- Wear OS Activity using `WearApp` Composable

**File:** `feature/wear/src/main/kotlin/com/nearwake/wear/EtaScreen.kt` *(new)*

Screen layout (round watch face optimized):
```
[PulseRing — 120dp, stage-colored]
  Text: ETA number     [displayMedium, white]
  Text: "min"          [labelSmall, white 70%]
Text: destination      [labelSmall, white 80%, below ring]
```

**File:** `feature/wear/src/main/kotlin/com/nearwake/wear/WearDataListener.kt` *(new)*

```kotlin
class WearDataListener : WearableListenerService() {
    override fun onDataChanged(events: DataEventBuffer) {
        for (event in events) {
            if (event.dataItem.uri.path == "/trip/eta") {
                val dataMap = DataMapItem.fromDataItem(event.dataItem).dataMap
                WearEtaStore.update(
                    etaMinutes = dataMap.getInt("eta_minutes"),
                    destination = dataMap.getString("destination"),
                    stage = AlertStage.valueOf(dataMap.getString("stage")),
                )
            }
        }
    }
}
```

---

#### Step K-2-D: Watch alert haptics

**File:** `feature/wear/src/main/kotlin/com/nearwake/wear/WearAlertManager.kt` *(new)*

```kotlin
object WearAlertManager {
    fun fireStageB(context: Context) {
        val vibrator = context.getSystemService(Vibrator::class.java)
        // 3 strong pulses — impossible to sleep through
        vibrator.vibrate(VibrationEffect.createWaveform(
            longArrayOf(0, 200, 100, 200, 100, 400), -1
        ))
    }

    fun fireStageCContinuous(context: Context) {
        // Continuous until dismissed via watch-tap
        val vibrator = context.getSystemService(Vibrator::class.java)
        vibrator.vibrate(VibrationEffect.createWaveform(
            longArrayOf(0, 500, 200), 0  // repeat = 0 means loop
        ))
    }

    fun cancel(context: Context) {
        context.getSystemService(Vibrator::class.java).cancel()
    }
}
```

`WearDataListener` calls `WearAlertManager.fireStageB()` when stage changes to IMMINENT, and `fireStageCContinuous()` when stage is ALERT. Dismissed by tapping the watch dismiss button which sends `ACTION_WATCH_DISMISS` back to phone via DataLayer.

---

#### Step K-2-E: Watch complications

**File:** `feature/wear/src/main/kotlin/com/nearwake/wear/EtaComplicationService.kt` *(new)*
- Implements `SuspendingComplicationDataSourceService`
- Provides a `SHORT_TEXT` complication: "12 min" (ETA) when trip is active, "NearWake" when idle
- Tapping complication opens `WearMainActivity`

---

### K-3: Trip Share Link

**Goal:** Traveller shares a link. Recipient watches trip progress in a browser — no app install needed.

---

#### Step K-3-A: Cloud Function — create share token

**File:** `firebase/functions/src/createTripShare.ts` *(new — TypeScript Cloud Function)*

```typescript
export const createTripShare = functions.https.onCall(async (data, context) => {
    if (!context.auth) throw new functions.https.HttpsError('unauthenticated', 'Must be signed in');

    const token = generateShortToken();  // 8 random alphanumeric chars
    const expiresAt = Date.now() + (24 * 60 * 60 * 1000);  // 24h max

    await admin.database().ref(`shares/${token}`).set({
        tripId: data.tripId,
        ownerId: context.auth.uid,
        createdAt: admin.database.ServerValue.TIMESTAMP,
        expiresAt,
        lastLocation: null,
        ended: false,
    });

    return { token, shareUrl: `https://nearwake.app/track/${token}` };
});
```

---

#### Step K-3-B: Phone — location push during trip

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/TripShareRepository.kt` *(new)*

```kotlin
@Singleton
class TripShareRepository @Inject constructor(
    private val database: FirebaseDatabase,
    private val auth: NearWakeAuthManager,
) {
    private var activeToken: String? = null

    suspend fun createShare(tripId: String): String {
        auth.ensureSignedIn()
        val result = FirebaseFunctions.getInstance()
            .getHttpsCallable("createTripShare")
            .call(mapOf("tripId" to tripId)).await()
        activeToken = result.getData<Map<String, String>>()?.get("token")
        return activeToken ?: throw IllegalStateException("No token returned")
    }

    fun pushLocationUpdate(lat: Double, lng: Double, etaMinutes: Int) {
        val token = activeToken ?: return
        database.reference.child("shares/$token/lastLocation").setValue(mapOf(
            "lat" to lat,
            "lng" to lng,
            "eta" to etaMinutes,
            "ts" to ServerValue.TIMESTAMP,
        ))
    }

    fun markTripEnded() {
        val token = activeToken ?: return
        database.reference.child("shares/$token/ended").setValue(true)
        activeToken = null
    }
}
```

`TripMonitoringService` calls `pushLocationUpdate()` every 30 seconds when a share is active. On trip completion, calls `markTripEnded()`.

---

#### Step K-3-C: Web page

**File:** `firebase/hosting/public/track/index.html` *(new)*

Single-page web app (no framework needed — vanilla JS):
- Reads `token` from URL path: `nearwake.app/track/TOKEN`
- Subscribes to `database.ref("shares/TOKEN/lastLocation")` via Firebase JS SDK
- Renders Google Maps embed centered on last location
- Shows ETA countdown from `lastLocation.eta`
- When `ended === true`: shows "Arrived safely ✓" card and stops updating

---

#### Step K-3-D: TripSetupScreen sharing option

**File:** `feature/tripsetup/src/main/kotlin/com/nearwake/feature/tripsetup/TripSetupScreen.kt`
- At the bottom (above Arm button): toggle "Share trip with someone"
- When enabled: on arm → call `TripShareRepository.createShare()`, post notification with share URL
- Share URL can be copied or sent via share sheet

---

### K-4: Family Account — Parent/Child Tracking

**Goal:** Parent gets a push notification when their child's trip completes. Parent can watch the trip live.

---

#### Step K-4-A: Family data model (Firestore)

```
families/{familyId}
  ├── name: string
  ├── ownerId: string          (parent's Firebase UID)
  ├── members: [
  │     { uid, displayName, role: "parent"|"child", canShareTrips: bool }
  │   ]
  └── inviteCodes: { code → expiresAt }

userProfiles/{uid}
  ├── displayName: string
  ├── familyId: string?
  └── fcmToken: string         (updated on every app launch)

activeTripStatus/{uid}
  ├── isActive: bool
  ├── destinationName: string
  ├── etaMinutes: int
  ├── lastLat: double
  ├── lastLng: double
  ├── stage: string
  └── updatedAt: timestamp
```

---

#### Step K-4-B: Family setup screen

**New module:** `:feature:family`

**File:** `feature/family/src/main/kotlin/com/nearwake/feature/family/FamilySetupScreen.kt` *(new)*
- "Create family group" → prompts email upgrade from anonymous auth
- Generates a 6-character invite code (stored in Firestore with 24h expiry)
- "Join with code" → child enters code → added to family group
- Shows current family members list with remove option

---

#### Step K-4-C: Trip status push to Firestore (child side)

**File:** `data/alerts/src/main/kotlin/com/nearwake/data/alerts/FamilyTripStatusPusher.kt` *(new)*

```kotlin
@Singleton
class FamilyTripStatusPusher @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: NearWakeAuthManager,
) {
    fun pushStatus(etaMinutes: Int, destination: String, lat: Double, lng: Double, stage: AlertStage) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("activeTripStatus").document(uid).set(mapOf(
            "isActive" to true,
            "destinationName" to destination,
            "etaMinutes" to etaMinutes,
            "lastLat" to lat,
            "lastLng" to lng,
            "stage" to stage.name,
            "updatedAt" to FieldValue.serverTimestamp(),
        ), SetOptions.merge())
    }

    fun clearStatus() {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("activeTripStatus").document(uid)
            .update("isActive", false)
    }
}
```

`TripMonitoringService` calls `pushStatus()` every 30 sec when family sharing is enabled. Calls `clearStatus()` on trip completion.

---

#### Step K-4-D: Parent arrival push notification (Cloud Function)

**File:** `firebase/functions/src/onTripCompleted.ts` *(new)*

```typescript
export const onTripCompleted = functions.firestore
    .document('activeTripStatus/{uid}')
    .onUpdate(async (change, context) => {
        const before = change.before.data();
        const after = change.after.data();

        // Detect trip completion: was active, now not active
        if (!before.isActive || after.isActive) return;

        const uid = context.params.uid;
        const familyQuery = await admin.firestore()
            .collection('families')
            .where('members', 'array-contains', { uid, role: 'child' })
            .get();

        for (const familyDoc of familyQuery.docs) {
            const family = familyDoc.data();
            const parents = family.members.filter(m => m.role === 'parent');
            for (const parent of parents) {
                const parentProfile = await admin.firestore()
                    .collection('userProfiles').doc(parent.uid).get();
                const fcmToken = parentProfile.data()?.fcmToken;
                if (!fcmToken) continue;

                await admin.messaging().send({
                    token: fcmToken,
                    notification: {
                        title: `${before.destinationName}`,
                        body: `${family.memberDisplayName(uid)} has arrived safely ✓`,
                    },
                    data: { type: 'CHILD_ARRIVED', uid },
                });
            }
        }
    });
```

---

#### Step K-4-E: Parent live map screen

**File:** `feature/family/src/main/kotlin/com/nearwake/feature/family/FamilyDashboardScreen.kt` *(new)*
- Lists all family members with their current status (active trip / idle)
- Tapping a member with an active trip → opens `ChildTripMapScreen`

**File:** `feature/family/src/main/kotlin/com/nearwake/feature/family/ChildTripMapScreen.kt` *(new)*
- Google Maps Compose, marker at `lastLat/lastLng` from Firestore
- Real-time updates via `firestore.collection("activeTripStatus").document(uid).addSnapshotListener`
- Shows destination name + ETA chip + stage indicator
- Map updates every Firestore snapshot (no extra polling needed — Firestore push)

---

### K-5: Cloud Sync (Trip History Across Devices)

**Goal:** User switches phones or uses a tablet — trip history follows them.

---

#### Step K-5-A: Sync architecture

**Strategy:** Phone is authoritative (Room is the source of truth). Firestore is a sync mirror. On first launch on new device, pull from Firestore. On each trip completion, push to Firestore.

**Firestore schema:**
```
userTrips/{uid}/trips/{tripId}
  ├── destinationName: string
  ├── destinationId: string
  ├── startedAt: timestamp
  ├── completedAt: timestamp?
  ├── durationMinutes: int
  ├── outcome: "COMPLETED" | "MISSED" | "CANCELLED"
  └── routeLabel: string?
```

---

#### Step K-5-B: TripSyncRepository

**File:** `data/sync/src/main/kotlin/com/nearwake/data/sync/TripSyncRepository.kt` *(new module `:data:sync`)*

```kotlin
@Singleton
class TripSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: NearWakeAuthManager,
    private val store: TripLifecycleStore,
) {
    suspend fun pushTrip(trip: Trip) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("userTrips/$uid/trips")
            .document(trip.id)
            .set(trip.toSyncMap())
            .await()
    }

    suspend fun pullAllTrips(): List<Trip> {
        val uid = auth.currentUser?.uid ?: return emptyList()
        return firestore.collection("userTrips/$uid/trips")
            .get().await()
            .documents.mapNotNull { it.toTrip() }
    }
}
```

`CompleteTripUseCase` calls `TripSyncRepository.pushTrip()` after local save.

---

#### Step K-5-C: First-launch sync

**File:** `application/trip/src/main/kotlin/com/nearwake/application/trip/SyncTripHistoryUseCase.kt` *(new)*
- Called once on first launch after sign-in
- Pulls all trips from Firestore
- Merges with local Room history (skip duplicates by `tripId`)
- Updates Room with any remote trips that are missing locally

---

### K-6: iOS App (KMM)

**Goal:** Share the domain layer (confidence engine, use cases) with iOS while keeping native UI on both platforms.

---

#### Step K-6-A: KMM module restructure

Convert `domain:*` and `application:trip` modules to KMM (Kotlin Multiplatform Mobile):

```
domain/trip/         → shared Kotlin (JVM + iOS targets)
domain/routing/      → shared Kotlin
domain/location/     → shared Kotlin
domain/commute/      → shared Kotlin
application/trip/    → shared Kotlin (interfaces + pure business logic only)
ports/persistence/   → shared Kotlin (interface only)
ports/monitoring/    → shared Kotlin (interface only)
```

**Platform-specific implementations stay in Android modules:**
- `data:alerts` → Android only (ForegroundService, AlarmManager)
- `core:database` → Android only (Room)
- All feature modules → Android only (Compose)

---

#### Step K-6-B: iOS project

Separate Xcode project in `/ios/` directory.
- SwiftUI for all screens
- KMM shared framework imported as `NearWakeShared.xcframework`
- iOS-specific implementations: `CoreLocationDataSource`, `UNUserNotificationCenter` alerts, `BackgroundTaskScheduler`

**Shared KMM entrypoints that iOS calls:**
```kotlin
// In application:trip (shared KMM)
class StartTripUseCaseIosAdapter(store: TripLifecycleStore): StartTripUseCase(store)
```

iOS `LocationMonitoringService.swift` implements `TripMonitoringGateway` protocol (generated from KMM interface) and calls `AlertStageEvaluator` from KMM for ETA decisions.

---

---

## Build Verification After Each Feature

Run after every completed feature (not just at the end):

```bash
# After any Room schema change:
./gradlew :core:database:kspDebugKotlin

# After any use case change:
./gradlew :application:trip:test

# After any feature module change:
./gradlew :feature:<module>:assembleDebug

# Full build before merge:
./gradlew :app:assembleDebug :app:assembleRelease

# Full test suite:
./gradlew test
```

**Schema export verification:** After each Room migration, check that `core/database/schemas/com.nearwake.core.database.NearWakeDatabase/<version>.json` was generated and committed.

**Regression tests:** All 66 existing test classes must pass after every change. Never disable a test to make a build green.
