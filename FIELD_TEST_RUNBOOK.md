# NearWake Field Test Runbook

This file turns the remaining real-device validation work into a repeatable runbook.

Use it when you are actively testing trips on a device.

## 1. Goal

Prove whether NearWake is reliable enough for a release candidate under real travel conditions.

This runbook focuses on:

- alert timing
- background survival
- reminder behavior
- permission and battery edge cases
- whether failures are visible and diagnosable

## 2. Before Each Test Session

Checklist:

- [ ] install the latest debug or release candidate build
- [ ] confirm `MAPS_API_KEY` behavior is as expected for the test device
- [ ] confirm notifications are enabled
- [ ] confirm precise location is enabled
- [ ] confirm background location state
- [ ] confirm activity recognition state
- [ ] note OEM battery mode or battery saver state
- [ ] clear old expectations from previous trips

Record this once per session:

- device model
- Android version
- app build type
- app version/commit
- OEM battery mode
- network condition baseline
- export a diagnostics snapshot after any failed or suspicious run
- compare diagnostics event timestamps against the real alert/reminder timing when investigating issues
- use the Diagnostics screen export to capture app version, build type, and device context with each report
- use the Diagnostics screen export to capture the live permission readiness state that was active during the run
- use the Diagnostics screen export to capture battery-saver, battery-optimization, and network context for the run
- use the Diagnostics screen export to capture the app's git revision so reports can be matched to the exact build
- refresh the Diagnostics screen after permission or battery-setting changes so the exported snapshot matches the live device state

## 3. Trip Scenario Matrix

Minimum scenario coverage:

| Scenario | Minimum runs | Notes |
| --- | --- | --- |
| Normal commute, screen on | 10 | Baseline timing |
| Phone locked, screen off | 10 | Core reliability path |
| Poor signal or tunnel | 5 | Offline-dignified behavior |
| Transfer journey | 5 | Transition handling |
| Re-armed familiar commute | 5 | Reuse and repeatability |
| Departure reminder day | 5 | Flexible alarm validation |

## 4. Per-Trip Log Template

Copy this block for each real trip:

```text
Trip ID / label:
Date:
Device:
Android version:
Build / commit:
Destination:
Scenario type:
Route preview available: Yes / No
Departure reminder expected: Yes / No
Departure reminder fired: Early / On time / Late / Missed / N/A
Monitoring started cleanly: Yes / No
Phone state during trip: Screen on / Locked / Mixed
Signal conditions: Normal / Weak / Tunnel / Mixed
Transfer involved: Yes / No
Arrival alert result: Early / On time / Late / Missed
Failure visible to user: Yes / No
Diagnostics captured: Yes / No
Battery notes:
Tester notes:
```

## 5. Required Failure Tests

Run these intentionally:

- [ ] notifications denied
- [ ] precise location denied
- [ ] background location denied
- [ ] activity recognition denied
- [ ] reboot after a departure reminder is scheduled
- [ ] process kill during active monitoring
- [ ] battery saver enabled

Pass condition:

- the app explains the degraded state clearly
- the app does not silently pretend monitoring is healthy
- diagnostics or visible UI gives enough context to understand what happened
- the Diagnostics screen export is usable for sharing the latest service state and event trail

## 6. Severity Rules

Treat these as release blockers:

- missed arrival alert during a normal supported trip
- missed departure reminder after a reboot in an expected supported path
- monitoring silently stops while UI still implies coverage
- denied permissions path still claims the app is fully ready

Treat these as warning-level issues:

- alert noticeably early but still safe
- reminder delayed by a few minutes under flexible alarm conditions
- route preview missing while destination-only fallback still works

## 7. After Each Session

Summarize:

- total trips run
- on-time alerts
- early alerts
- late alerts
- missed alerts
- reminder success rate
- permission-path failures
- battery concerns
- top 3 issues found

If a blocking issue appears, record:

- exact scenario
- whether it is reproducible
- whether diagnostics explained it
- suspected module or screen

## 8. Exit Criteria

A field-test round is healthy enough to move toward release only when:

- no unexplained missed alerts remain
- permission-denied paths are honest
- departure reminders behave acceptably on supported devices
- diagnostics are useful for every meaningful failure
- battery impact is acceptable for the monitored trip window
