# NearWake Privacy Policy Draft

This file is a working draft for a publishable privacy policy.

It is not legal advice.
Replace any placeholder contact details before using it publicly.
Use [privacy-policy.html](privacy-policy.html) if you want the same draft as a simple hostable page file.

## 1. Overview

NearWake is an Android app that helps users monitor trips, receive arrival alerts, and set departure reminders.

NearWake is designed around local-first behavior. The current app flow does not require an account for its core trip-alert features.

## 2. Information NearWake Stores

NearWake may store the following information on the device:

- saved places
- trip records
- trip session state
- route snapshots
- alert events
- diagnostics events, when diagnostics logging is enabled
- commute predictions used for departure reminders
- user preferences such as theme, alert defaults, and monitoring settings

## 3. Location Data

NearWake uses location information during trip monitoring so it can estimate proximity to the destination and decide when to alert the user.

In the current app design, trip-related location context is stored locally on the device as part of trip/session and route-supporting app data. The app is not designed around a continuous cloud location-history account service for its core flow.

## 4. Diagnostics Data

If diagnostics logging is enabled, NearWake stores recent troubleshooting events on the device to help explain missed, late, or degraded monitoring behavior.

Diagnostics logging can be turned off from Settings.

Stored diagnostics can be cleared from the Diagnostics screen. Diagnostics are also removed if the app's local data is cleared by the user.

## 5. Notifications And Permissions

NearWake may request permissions such as:

- notifications
- precise location
- background location
- activity recognition

These permissions support arrival alerts, departure reminders, active trip monitoring, and power-aware monitoring behavior. If some permissions are denied, parts of the app may become less reliable or unavailable.

## 6. Data Sharing

The current NearWake app flow is designed around local persistence first.

The app can share user-initiated exports such as:

- CSV trip-history exports
- Diagnostics text exports from the Diagnostics screen

These exports are only shared when the user chooses to export them.

## 7. Third-Party Services

If the app is configured with a Google API key, NearWake may use Google services for:

- destination search
- transit route preview and routing support

The exact data handling for those services is also governed by the relevant third-party provider terms and policies.

## 8. Retention

NearWake keeps local app data until the user removes it, clears specific in-app data where supported, or uninstalls/clears the app's local storage.

Diagnostics events are intended to be short-lived troubleshooting data and can be cleared manually from the Diagnostics screen.

## 9. User Choices

Users can currently:

- disable diagnostics logging
- clear stored diagnostics events
- control Android permissions through system settings
- remove app data through Android app-management controls

## 10. Children

NearWake is not specifically directed to children.

## 11. Changes To This Policy

This privacy policy may be updated as the product changes.

## 12. Contact

Before public release, replace this section with real contact details such as:

- support email
- company or developer name
- website or policy host URL
