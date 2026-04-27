# NearWake Play Console Disclosure Draft

This file is a working draft for Play Console permission and policy answers.

It is not a submitted form.
Use it to keep store answers aligned with the current app behavior before final submission.

## 1. Current Scope

NearWake is an arrival-alert and departure-reminder Android app.

The current product direction is:

- local-first trip monitoring
- no account required for the core trip-alert flow
- route-aware behavior when Google services are configured
- destination-only fallback when provider-backed routing is unavailable

## 2. Permissions To Explain Clearly

| Permission | Current reason | Draft Play Console explanation |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | arrival alerts, reminders, recovery warnings | NearWake uses notifications to deliver arrival alerts and departure reminders at the time the user depends on them. |
| `ACCESS_FINE_LOCATION` | active trip monitoring and destination proximity | NearWake uses precise location during active trip monitoring to estimate proximity to the destination and decide when to alert the user. |
| `ACCESS_BACKGROUND_LOCATION` | trip monitoring while locked or off-screen | NearWake uses background location only while an active trip is being monitored so arrival alerts can still work when the phone is locked or the screen is off. |
| `ACTIVITY_RECOGNITION` | power-aware monitoring behavior | NearWake uses activity recognition to adjust monitoring behavior more intelligently and reduce unnecessary battery use when the user is not moving. |
| `RECEIVE_BOOT_COMPLETED` | restore reminder/recovery behavior after reboot | NearWake uses restart-after-boot handling to restore scheduled reminder and recovery behavior after the device restarts. |
| `USE_FULL_SCREEN_INTENT` | stronger alert presentation where policy allows | NearWake may request stronger alert presentation for urgent arrival alerts, but this should be validated against Play policy before submission. |

## 3. Background Location Declaration Draft

Use wording along these lines if Play Console asks why background location is needed:

> NearWake monitors active trips so it can alert the user before they miss their destination. Background location is used only while a trip is actively being monitored, because the core use case includes the phone being locked, in a pocket, or with the screen off during travel.

Do not claim:

- always-on location history
- passive tracking outside active trips
- advertising, profiling, or unrelated analytics use

## 4. Notifications And Alarm Behavior Draft

Suggested explanation:

> NearWake uses notifications for arrival alerts, departure reminders, and recovery warnings. Some reminders use flexible Android alarm windows rather than exact-alarm guarantees, so timing can vary slightly depending on device state and battery behavior.

## 5. Local Data And Sharing Draft

Suggested plain-language answers:

- core trip data is stored locally on the device
- diagnostics logging is optional and can be turned off
- diagnostics exports and history exports are only shared when the user chooses to export them
- the app is not built around a continuous cloud location-history account service in the current flow

## 6. Data Safety Draft Notes

Before submission, review the actual Play Data safety form carefully.

Current repo-aligned answers should stay close to these ideas:

- trip-related app data is stored locally for app functionality
- user-initiated exports are optional
- no account is required for the core product path
- permission use should be described as functional and user-facing, not marketing-related

## 7. Policy-Sensitive Open Item

Still validate before final submission:

- whether the current `USE_FULL_SCREEN_INTENT` behavior and wording fit the latest Play policy expectations

## 8. Submission Checklist For This Draft

- [ ] paste final answers into Play Console
- [ ] review wording against the exact shipped build
- [ ] confirm screenshots and listing copy match the same behavior
- [ ] re-check full-screen intent policy fit before submission
