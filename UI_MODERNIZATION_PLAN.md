# NearWake UI Modernization Plan

This file is the dedicated UI planning document for the upcoming frontend redesign pass.

Its purpose is to define:

- the current UI starting point
- the visual direction we want
- what should change
- what must stay stable
- the screen-by-screen redesign plan
- the order of implementation
- the acceptance criteria for a good modern UI pass

This is the plan document to use when giving a future UI redesign prompt.

## 1. Current UI Status

The app already has a functional frontend.

That means:

- screens exist
- navigation exists
- real data is already wired into major flows
- the app is not a blank shell

Current implemented screens:

- onboarding
- home
- permissions
- place search
- trip setup
- live trip
- alert
- recovery
- history
- trip summary
- settings
- diagnostics

Current design-system baseline:

- dark-first palette in `core:designsystem`
- Material 3 setup
- shared card/button/scaffold components in `core:ui`
- consistent but still basic visual presentation

So the next UI work is not “build screens from nothing”.
It is “upgrade the visual system and screen presentation into a stronger, more modern product UI”.

## 2. Current Visual Weaknesses

The current UI is usable, but it is still plain in several ways.

Main weaknesses:

- typography is still generic and safe
- screen hierarchy is functional more than expressive
- cards and layouts are simple and repetitive
- home screen does not yet feel premium or memorable
- route and monitoring data are visible, but not yet presented with strong information design
- motion/animation language is minimal
- onboarding and alerts do not yet have a distinct product identity

So the goal is not to replace functionality.
The goal is to improve:

- clarity
- visual identity
- emotional feel
- modernity
- product confidence

## 3. Non-Negotiable UI Rules

The redesign should preserve these things:

- all current flows must continue to work
- navigation routes must stay intact unless intentionally coordinated
- persisted data behavior must not be broken
- ViewModel-driven state should remain the source of truth
- accessibility should not get worse
- low-battery / monitoring / alert concepts must remain immediately understandable

The redesign should avoid:

- “AI-slop” dashboard styling
- random gradients without meaning
- visual polish that hides important status information
- replacing real data with fake visuals
- breaking screen readability in dark environments

## 4. Product Feeling We Want

NearWake should feel:

- calm when safe
- focused when monitoring
- urgent when approaching
- unmistakable when alerting

The app is not a generic travel planner.
It is an arrival assurance tool.

That means the UI should communicate:

- trust
- readiness
- confidence
- strong state transitions

Good emotional reference points:

- quiet and composed while waiting
- active and precise during monitoring
- high-contrast and impossible-to-ignore during alert state

## 5. Visual Direction

The visual direction should build from the existing dark-first palette, but become more intentional.

Recommended direction:

- retain dark-first atmosphere
- strengthen surface layering
- use clearer state color roles
- improve type scale and spacing rhythm
- create stronger hero sections on key screens
- add subtle but meaningful motion

Color intent:

- green = safe / armed / healthy
- amber = approaching / attention
- red = alert / danger of missing destination
- cool accent = route/system/detail information

Layout intent:

- larger hero status areas
- stronger primary action placement
- fewer “flat stacked forms”
- more visual grouping by user intent

## 6. Screen Priority Order

The redesign should happen in this order:

1. `core:designsystem`
2. `core:ui`
3. `HomeScreen`
4. `TripSetupScreen`
5. `LiveTripScreen`
6. `AlertScreen` and `RecoveryScreen`
7. `HistoryScreen` and `TripSummaryScreen`
8. `SettingsScreen`
9. `DiagnosticsScreen`
10. `OnboardingScreen`
11. `PermissionsScreen`
12. `PlaceSearchScreen`

Why this order:

- system styles must come first
- home/trip/live/alert are the product-defining surfaces
- supporting screens should inherit the new language after that

## 7. Module-Level UI Work

### `core:designsystem`

This is the first place to improve.

Planned work:

- refine color roles
- introduce better surface hierarchy
- improve typography scale
- define reusable spacing rhythm
- define status-specific semantic colors
- define any shared shape/radius language

Likely files:

- `core/designsystem/.../NearWakeColors.kt`
- `core/designsystem/.../NearWakeTheme.kt`

### `core:ui`

This should be upgraded after the theme.

Planned work:

- stronger scaffold header treatment
- better cards with clearer padding and hierarchy
- upgraded primary/secondary button styles
- shared section-header patterns
- reusable status chips / badges / state rows

This layer should reduce repeated styling logic in feature screens.

## 8. Screen-By-Screen UI Intent

### Home

Home should feel like the product’s strongest first impression.

Goals:

- create a strong hero section
- make “Set destination” the unmistakable primary action
- show recent activity in a clearer visual structure
- make settings/history/permissions feel secondary but accessible

Desired feeling:

- quick to act
- calm but premium
- immediately understandable

### Trip Setup

Trip setup should feel like arming a mission, not filling a generic form.

Goals:

- emphasize destination and route preview
- make lead time/intensity choices feel deliberate
- clearly show when the app is in route-aware mode vs destination-only fallback
- make the start action feel high-confidence

Desired feeling:

- clear
- controlled
- trustworthy

### Live Trip

This is one of the most important screens in the app.

Goals:

- elevate monitoring status as the visual centerpiece
- show ETA, route summary, confidence, and mode in a stronger information hierarchy
- make the user understand “what the app is doing right now” immediately

Desired feeling:

- alive
- precise
- reassuring

### Alert

Alert UI must be unmistakable and impossible to casually ignore.

Goals:

- high contrast
- strong urgency
- large action targets
- minimal cognitive load

Desired feeling:

- urgent
- unmistakable
- accessible even when tired or groggy

### Recovery

Recovery should feel serious but not chaotic.

Goals:

- communicate that the app detected a problem
- make recovery choices obvious
- keep a strong hierarchy for “end trip” vs future reroute/recovery options

### History And Trip Summary

These screens should feel more like a useful trip record than a plain database view.

Goals:

- stronger list item hierarchy
- clearer status treatments
- better presentation of route summary, ETA, and confidence

### Settings

Settings should feel clean and product-consistent, not like a raw developer form.

Goals:

- better grouping
- clearer preference descriptions
- stronger visual structure for alert and monitoring options

### Diagnostics

Diagnostics can stay more functional, but should still align visually with the rest of the app.

Goals:

- preserve density and usefulness
- improve spacing and readability
- make debug data easier to scan

### Onboarding

Onboarding should feel branded and intentional.

Goals:

- stronger value-prop presentation
- cleaner hierarchy
- more memorable first-run experience

### Permissions

Permissions should feel safe and supportive.

Goals:

- explain “why” clearly
- reduce friction
- distinguish limited mode from full mode cleanly

### Place Search

Even if place search still uses sample/local results for now, the screen should still feel modern.

Goals:

- improve search field presentation
- better result-card hierarchy
- clearer difference between saved places and search results

## 9. Motion And Interaction Plan

The redesign should include light, meaningful motion.

Recommended motion types:

- subtle entrance animation for hero sections
- staggered reveal for grouped content
- state color/mode transitions on live trip
- alert/recovery emphasis transitions where appropriate

Avoid:

- overly decorative motion
- constant looping motion
- animation that slows urgent actions

## 10. Information Design Plan

The app’s UI is not only about beauty.
It is heavily about status communication.

The redesign should improve:

- ETA prominence
- route summary readability
- confidence/state clarity
- alert intensity visibility
- monitoring mode explanation

The user should be able to answer these questions quickly:

- where am I going?
- is the app actively monitoring?
- how close am I?
- is it confident?
- what should I do next?

## 11. What Should Be Documented During UI Work

When the redesign starts, the implementation pass should also update docs if needed.

Things worth documenting:

- final visual direction
- any new design-system tokens
- any reusable shared components added
- any screen ownership/order assumptions

## 12. Acceptance Criteria For A Good UI Pass

The UI redesign will be considered successful if:

- the app still builds and runs
- the key screens feel visually intentional, not generic
- home/trip/live/alert screens feel like one coherent product
- state colors and hierarchy are clearer than before
- dark-mode readability remains strong
- functionality is preserved
- route/session data is presented better, not just restyled superficially

## 13. What The Future Prompt Should Aim For

When giving the later UI redesign prompt, it should ask for:

- a modern, premium-feeling NearWake visual language
- implementation in the real existing screens
- preservation of existing architecture and data flow
- redesign of the most important screens first
- no fake placeholder behavior

Good scope for the first UI pass:

- design system
- core UI primitives
- home
- trip setup
- live trip
- alert/recovery

That would create the biggest product-level improvement first.
