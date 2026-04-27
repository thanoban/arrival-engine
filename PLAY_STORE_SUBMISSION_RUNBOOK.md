# NearWake Play Store Submission Runbook

This file is the final handoff runbook for preparing a real Play Store submission.

It does not replace field testing or the Play Console itself.
Use it to move through the remaining manual steps in a clean order once the repo-side work is done.

## 1. Goal

Get NearWake from "repo is ready for field testing and release prep" to "real submission package is ready."

## 2. Inputs You Should Have Before Starting

You should already have:

- a buildable repo
- passing debug verification
- passing release verification
- the field-test runbook and log template
- draft listing copy
- draft privacy policy text
- draft Play Console disclosure answers

Useful repo files:

- [FIELD_TEST_RUNBOOK.md](FIELD_TEST_RUNBOOK.md)
- [FIELD_TEST_LOG_TEMPLATE.csv](FIELD_TEST_LOG_TEMPLATE.csv)
- [PLAY_STORE_LISTING_DRAFT.md](PLAY_STORE_LISTING_DRAFT.md)
- [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md)
- [privacy-policy.html](privacy-policy.html)
- [PLAY_CONSOLE_DISCLOSURE_DRAFT.md](PLAY_CONSOLE_DISCLOSURE_DRAFT.md)
- [APP_SIGNING_SETUP_GUIDE.md](APP_SIGNING_SETUP_GUIDE.md)

## 3. Recommended Sequence

1. finish the real-device field-test matrix
2. review late, early, and missed alert cases
3. freeze the user-facing wording to match what was actually validated
4. prepare final screenshots and icon assets
5. host the privacy policy page and obtain the final URL
6. complete local release-signing setup
7. run the release build again with the final signing path
8. copy the final listing and disclosure answers into Play Console
9. re-check policy-sensitive items before pressing submit

## 4. Field-Test Closeout

Before submission, confirm:

- the field-test matrix is filled out
- meaningful failures have diagnostics exports attached
- reminder timing behavior is understood on target devices
- permission-denied paths are honest
- battery and OEM limitations are reflected in the release wording

## 5. Final Wording Freeze

Before you lock store copy, make sure these all agree with each other:

- [PLAY_STORE_LISTING_DRAFT.md](PLAY_STORE_LISTING_DRAFT.md)
- [PRIVACY_AND_DISCLOSURE_NOTES.md](PRIVACY_AND_DISCLOSURE_NOTES.md)
- [PLAY_CONSOLE_DISCLOSURE_DRAFT.md](PLAY_CONSOLE_DISCLOSURE_DRAFT.md)
- what the tested build actually does

Do not keep claims that were not validated in field testing.

## 6. Privacy Policy Hosting

You still need a real hosted privacy-policy URL for submission.

Use these files as the source:

- [PRIVACY_POLICY_DRAFT.md](PRIVACY_POLICY_DRAFT.md)
- [privacy-policy.html](privacy-policy.html)

The exact hosting method is up to you, but before submission the result should be:

- publicly reachable
- stable
- aligned with the shipped app behavior

## 7. Signing And Release Artifact

Use [APP_SIGNING_SETUP_GUIDE.md](APP_SIGNING_SETUP_GUIDE.md) to prepare the local signing inputs.

Then verify again:

```powershell
.\gradlew.bat :app:assembleRelease
```

If you later switch to a bundle-based upload path, verify that exact artifact before submission.

## 8. Play Console Entry Pass

When filling Play Console:

- paste the final short description and full description
- attach final screenshots and icon assets
- add the hosted privacy policy URL
- answer the permission and data-safety forms using the draft disclosures as the starting point
- review any full-screen intent or background-location policy prompts carefully

## 9. Final Pre-Submit Checklist

- [ ] field-test matrix complete
- [ ] release wording matches validated behavior
- [ ] privacy policy hosted and reachable
- [ ] release signing configured
- [ ] release build artifact verified
- [ ] final screenshots and icon assets ready
- [ ] Play Console disclosure answers pasted and reviewed
- [ ] policy-sensitive permission behavior re-checked

## 10. Honest Stop Rule

Do not submit just because all repo documents exist.

Stop and re-check if any of these are still unresolved:

- unexplained missed alerts
- unclear battery or OEM reliability behavior
- privacy policy not yet hosted
- signing path not finalized
- policy-sensitive wording still uncertain
