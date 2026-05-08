# Long-Journey Monitoring Strategy

## Status

This is a product and engineering note only.

It is **not implemented yet**.

## The idea

For very long trips, NearWake should not keep stronger location monitoring active for the full journey.

Instead, the app can use a staged model:

1. The user arms a trip and allows monitoring.
2. If the ETA is very long, such as `5+ hours`, NearWake stores the trip and stays in a low-power state.
3. NearWake wakes back up near a user-approved time window, such as:
   - `4 hours before arrival`
   - `2 hours before arrival`
   - `Auto optimize for battery`
4. NearWake then escalates monitoring in steps:
   - delayed / sleeping mode
   - low-power wake-up
   - balanced location monitoring
   - stronger near-destination monitoring
   - final alert window

This is a better battery strategy than running stronger location collection for the full trip.

## Why this is useful

- reduces battery drain on long journeys
- reduces background pressure from Android
- lowers the risk of keeping high-power monitoring active when it is not needed yet
- gives users more control over when active monitoring begins

## Best product shape

The clean user-facing model is:

- `Start monitoring immediately`
- `Start 4 hours before arrival`
- `Start 2 hours before arrival`
- `Auto optimize for battery`

The most practical automatic rule would be:

- if ETA is greater than `5 hours`, do not start stronger monitoring immediately
- schedule a lower-power wake-up before the destination window
- refresh ETA at wake-up
- decide whether to stay low-power or escalate

## How it should work technically

The professional version is a staged monitoring flow, not a single long-running GPS session.

### Stage 1: delayed state

Persist:

- destination
- trip id
- route snapshot if available
- original ETA
- user preference for delayed wake-up

### Stage 2: wake-up window

Use a delayed trigger to wake the app near the planned time window.

At wake-up:

- re-check ETA if possible
- refresh route context if possible
- decide whether active monitoring should begin now

### Stage 3: balanced monitoring

Once the destination window becomes relevant, move into lower-power active monitoring first.

Examples:

- geofence registration
- balanced location updates
- activity recognition support

### Stage 4: stronger near-arrival monitoring

Closer to the destination:

- increase monitoring confidence
- refresh ETA more aggressively if signal allows
- move into the normal NearWake alert engine flow

## Important limitations

This idea should treat the first ETA as an estimate, not as truth.

Long trips can change because of:

- route changes
- traffic
- rail delays
- the user boarding a different vehicle
- signal loss

So the delayed-start strategy must include a re-check before escalation.

## Android and Play policy realities

This is possible, but it must respect Android background limits and Google Play policy.

### 1. Background location

If NearWake starts or continues location behavior while the app is not visible, that becomes background-location behavior.

For a production release, this is only appropriate when it is genuinely core to the app's main function.

Official references:

- [Android background location](https://developer.android.com/develop/sensors-and-location/location/background)
- [Google Play background location policy](https://support.google.com/googleplay/android-developer/answer/9799150)

### 2. Exact timing is not guaranteed

Android does not guarantee exact wake-up timing for all apps and all alarm types.

That means this feature should usually be designed around:

- inexact delayed wake-up
- WorkManager or a similar deferred execution path
- a re-check and escalation step after wake-up

Official references:

- [Android exact alarms changes](https://developer.android.com/about/versions/14/changes/schedule-exact-alarms)
- [WorkManager overview](https://developer.android.com/topic/libraries/architecture/workmanager)

### 3. Geofencing and staged monitoring fit this better than constant GPS

For longer journeys, a staged model with geofencing and lower-power escalation is more appropriate than a constant high-accuracy GPS session.

Official references:

- [Android geofencing](https://developer.android.com/develop/sensors-and-location/location/geofencing)

## Permissions needed

If this is ever built for production, the app would likely need:

- fine location
- notifications
- background location, if monitoring must continue reliably while the app is not open

The user-facing disclosure must explain:

- why monitoring starts later
- when location is used
- how to disable the feature

## Recommended product rule

The safest version for NearWake would be:

- enable this only with explicit user choice
- use delayed monitoring only for long ETA trips
- wake into a low-power state first
- refresh ETA before escalating
- only switch to stronger monitoring when the destination window becomes relevant

## Conclusion

Yes, this is a valid and realistic idea.

The correct implementation is not "leave location on for five hours."

The correct implementation is:

- delayed wake-up
- low-power staging
- ETA re-check
- gradual monitoring escalation

That keeps the feature aligned with Android limits, Play policy, battery efficiency, and the trust model of NearWake.
