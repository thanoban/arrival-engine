# NearWake Release Readiness Checklist

This file is the practical pre-release guide for the current app state.

Pair it with [FIELD_TEST_RUNBOOK.md](FIELD_TEST_RUNBOOK.md) when you are actually running trips on a device.
Use [FIELD_TEST_LOG_TEMPLATE.csv](FIELD_TEST_LOG_TEMPLATE.csv) if you want a simple per-trip test log sheet.
Use [PRIVACY_AND_DISCLOSURE_NOTES.md](PRIVACY_AND_DISCLOSURE_NOTES.md) for the current draft release wording.
Use [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md) for the current publishable-policy draft.
Use [PLAY_CONSOLE_DISCLOSURE_DRAFT.md](PLAY_CONSOLE_DISCLOSURE_DRAFT.md) for the current Play Console disclosure-answer draft.
Use [APP_SIGNING_SETUP_GUIDE.md](APP_SIGNING_SETUP_GUIDE.md) for the local release-signing setup plan.
Use [PLAY_STORE_LISTING_DRAFT.md](PLAY_STORE_LISTING_DRAFT.md) for the current listing copy and screenshot-prep draft.

Use it when you want to answer:

- is the app ready for internal testing, field testing, or store release
- what must still be validated on real devices
- what permission disclosures and privacy notes must be prepared
- what build checks must pass before each release candidate

## 1. Current Stage

As of 2026-04-27, NearWake is:

- build-ready
- feature-complete for the current MVP direction
- not yet proven through the full real-world field-test protocol
- not yet ready for Play Store submission

That means the next work is not basic feature wiring. It is release hardening, evidence gathering, and disclosure prep.

## 2. Commands That Must Pass

Focused debug verification:

```powershell
.\gradlew.bat :feature:permissions:testDebugUnitTest :feature:departure:testDebugUnitTest :data:alerts:test :app:assembleDebug
```

Broad app verification:

```powershell
.\gradlew.bat :domain:trip:test :core:network:test :data:routing:test :data:alerts:test :app:assembleDebug
```

Release build verification:

```powershell
.\gradlew.bat :app:assembleRelease
```

If Gradle is locked or behaves strangely on Windows:

```powershell
.\gradlew.bat --stop
```

Then rerun the failing command.

## 3. Real-Device Field Test Matrix

These checks are still required before calling the app release-ready.

### Core trip tests

- [ ] 10+ normal trips with screen on
- [ ] 10+ trips with screen off or phone locked
- [ ] 5+ low-signal or tunnel scenarios
- [ ] 5+ transfer scenarios
- [ ] 5+ re-armed repeat commute scenarios

For each run, record:

- destination
- whether route preview was available
- whether monitoring started cleanly
- whether arrival alert was early, on time, or late
- whether departure reminder fired
- whether Android killed or interrupted monitoring
- battery impact notes

### Failure-path tests

- [ ] notifications denied
- [ ] precise location denied
- [ ] background location denied
- [ ] activity recognition denied
- [ ] reboot after scheduling departure reminders
- [ ] app process kill during active monitoring

Success means the app fails visibly and with useful guidance, not silently.

## 4. Permission Disclosure Map

Current manifest permissions and the honest product reason for each:

| Permission | Why the app uses it | Release note |
| --- | --- | --- |
| `POST_NOTIFICATIONS` | Arrival alerts, departure reminders, recovery notifications | User-facing and core to the product |
| `ACCESS_COARSE_LOCATION` | fallback location accuracy path | Supportive, not the primary signal |
| `ACCESS_FINE_LOCATION` | trip start accuracy, destination proximity, active monitoring | Core permission |
| `ACCESS_BACKGROUND_LOCATION` | alerts while the phone is locked during an active trip | Must be disclosed carefully |
| `ACTIVITY_RECOGNITION` | power-aware motion detection and monitoring mode changes | Explain battery benefit |
| `FOREGROUND_SERVICE` | long-running active trip monitoring | Operational requirement |
| `FOREGROUND_SERVICE_LOCATION` | foreground trip monitoring with location | Operational requirement |
| `USE_FULL_SCREEN_INTENT` | stronger arrival alert presentation where supported | Validate policy fit before store submission |
| `VIBRATE` | alert urgency | User-facing behavior |
| `WAKE_LOCK` | keep monitoring/alert path alive at critical moments | Operational requirement |
| `RECEIVE_BOOT_COMPLETED` | restore recovery and departure reminder scheduling after reboot | Mention in reliability notes |
| `INTERNET` | Google routing and place search | Only when provider-backed features are used |
| `ACCESS_NETWORK_STATE` | network-aware behavior and graceful fallback | Supportive |

## 5. Privacy And Data Notes

Current local persistence includes:

- saved places
- trips
- trip sessions
- route snapshots
- alert events
- diagnostics events
- commute predictions
- user preferences

Current exported user data path:

- CSV trip export from History
- Diagnostics export from the Diagnostics screen

Current privacy posture to preserve in release material:

- local persistence first
- no account required for core value
- no continuous cloud location history store in the current app path
- diagnostics logging is user-toggleable

Current field-test evidence support already in app:

- diagnostics export with recent event timestamps
- diagnostics export with app version, build type, and device context
- diagnostics export with live permission-readiness state
- diagnostics export with power-saver, battery-optimization, and network context

Before store submission, prepare:

- [ ] privacy policy URL
- [x] plain-language explanation of local trip/history storage
- [x] diagnostics retention explanation
- [x] permission disclosure copy matching the runtime flows

## 6. Battery And Reliability Checks

These statements should only appear in release copy after real-device validation:

- "battery-aware monitoring"
- "works when your phone is locked"
- "reliable overnight or while asleep"
- "departure reminders survive reboot"

Until then, keep the product language honest:

- flexible alarms may vary by a few minutes
- OEM battery settings may still delay alerts
- denied background permissions reduce reliability

## 7. Play Store Prep

Still needed before a public release:

- [ ] final signing configuration
- [ ] app icon and listing screenshots
- [ ] short description and full description
- [ ] privacy policy URL
- [ ] permission disclosure answers in Play Console
- [ ] policy review for full-screen intent usage
- [ ] final Google API key restrictions

Current prep already done:

- [x] working draft for short description and full description
- [x] safe feature claims list and overclaim guardrails
- [x] screenshot checklist for store assets
- [x] signing setup guide draft

## 8. Honest Release Gate

NearWake is ready for a real field-test round when:

- debug and release builds pass
- permission flows are honest
- reminder scheduling survives reboot
- diagnostics can explain failures

NearWake is ready for store submission only when:

- the field-test matrix is completed
- late/missed alert cases are reviewed
- privacy/disclosure material is written
- Play policy-sensitive behaviors are validated
