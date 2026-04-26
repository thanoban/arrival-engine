# NearWake Privacy And Disclosure Notes

This file is a working draft for release preparation.

It is not legal advice and it is not a final Play Console submission.
Use it to keep the product copy aligned with what the current app actually does.

## 1. Current Privacy Posture

NearWake currently follows these product rules:

- local persistence first
- no account required for core value
- no ads in the alert path
- no always-on cloud location history in the current app flow
- diagnostics logging is user-toggleable

## 2. Data Currently Stored On Device

The current app persists these categories locally:

- saved places
- trips
- trip sessions
- route snapshots
- alert events
- diagnostics events
- commute predictions
- user preferences

The app also supports:

- CSV export of trip history from the History flow

## 3. Data The Release Copy Should Describe Clearly

Plain-language draft:

> NearWake stores your saved places, trip history, trip session state, and alert-related diagnostics on your device so arrival alerts, reminders, and recent trip context can keep working between app launches.

Plain-language draft for diagnostics:

> If diagnostics logging is enabled, NearWake keeps recent troubleshooting events on the device to help explain missed or degraded monitoring behavior.

## 4. Permissions Disclosure Draft

Use copy like this in release material and in-product explanations.

### Notifications

Draft:

> NearWake uses notifications for arrival alerts, departure reminders, and recovery warnings. Without notification access, the app cannot reliably wake you at the critical moment.

### Precise location

Draft:

> NearWake uses precise location during trip monitoring so it can estimate when you are approaching your destination and decide when to alert you.

### Background location

Draft:

> NearWake uses background location only while an active trip is being monitored, so alerts can still work when the phone is locked or the screen is off.

### Activity recognition

Draft:

> NearWake uses activity recognition to adjust monitoring intensity more intelligently and reduce unnecessary battery use when you are not moving.

### Boot completed

Draft:

> NearWake uses restart-after-boot handling to restore scheduled reminder and recovery behavior after the device restarts.

## 5. Product Claims That Are Safe Right Now

These statements match the current codebase direction:

- local persistence first
- no account required for the main trip-alert workflow
- reminders and alerts depend on Android permissions and device settings
- flexible reminders may vary by a few minutes
- some OEM battery modes can still reduce reliability

## 6. Product Claims That Still Need Validation

Do not overstate these until the field-test runbook is completed:

- "reliable overnight"
- "works every time when locked"
- "battery-light on all devices"
- "departure reminders always survive reboot"
- "store-ready"

## 7. Final Release Prep Still Needed

- [ ] privacy policy URL
- [ ] Play Console permission disclosure answers
- [ ] final wording review against the shipped build
- [ ] confirm whether diagnostics are included in release builds exactly as described
- [ ] policy review for full-screen intent behavior
