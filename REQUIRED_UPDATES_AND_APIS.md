# Required Updates And APIs

This file lists the exact things you still need to update or provide outside the codebase.

It is separate from the architecture/status docs on purpose.
Use this as the operational checklist for machine setup, API setup, and later release setup.

## 1. Things You Must Update Right Now

These are the only things you must set to build and use the app properly on your machine.

### `local.properties`

Update or create:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

The Gradle build reads `MAPS_API_KEY` from `local.properties` for local development. You can also pass it as a Gradle property when needed.

What each value means:

- `sdk.dir`
  - path to your Android SDK
  - required for Android builds
- `MAPS_API_KEY`
  - Google API key used by the current routing integration
  - enables transit route preview, ETA refresh support, and provider-backed destination search

## 2. APIs You Should Enable Now

### Required for current route-aware behavior

- `Directions API`

Why:

- the current `GoogleTransitDataSource` calls the Google Directions transit endpoint
- without this API, route preview will fail even if the key exists

### Required for provider-backed destination search

- `Places API`

Why:

- the current place search repository uses Google Places when `MAPS_API_KEY` is configured
- without this API, the app can still use the local fallback list, but real place search will fail

## 3. APIs Likely Needed Later

These are not strictly required for the current code path, but they are likely part of the later full product path.

- `Maps SDK for Android`

Why:

- the long-term plan includes map-based integrations

## 4. What Happens If You Do Not Add `MAPS_API_KEY`

The app still:

- builds
- installs
- runs
- starts trips

But it falls back to:

- destination-only monitoring
- no live Google route preview
- no trip-scoped cached route details from the provider
- local fallback destination search instead of Google Places search

So this key is recommended, not a hard blocker for all development.

## 5. Things You Do Not Need Right Now

You do not need to update or provide these yet:

- backend `.env`
- custom API server URL
- PostgreSQL credentials
- Firebase project config
- OpenAI API key
- auth provider secrets
- Sentry DSN

Why:

- the app is currently offline-first
- there is no required backend for the present build/use flow
- the current routing logic talks directly to Google Directions using `MAPS_API_KEY`

## 6. Things To Update Later Before Full Release

These are not current build blockers, but they will matter before calling the project fully finished.

### Product/API side

- decide whether to keep direct Google routing or introduce a backend layer
- decide whether Maps SDK UI is part of the final product

### Release/ops side

- final app signing setup
- privacy policy URL
- Play Store listing assets and permission disclosures
- Sentry DSN if crash monitoring is desired
- production API restrictions on the Google key

## 7. Recommended Google Key Setup

For your `MAPS_API_KEY`, you should:

- enable `Directions API`
- enable `Places API`
- restrict the key appropriately in Google Cloud Console
- avoid using an unrestricted production key

Recommended restriction direction:

- Android app restrictions for Android SDK usage where relevant
- API restrictions limited to the services you actually use

## 8. Practical Minimum Setup

If you want the smallest possible setup to continue development:

1. install Android SDK
2. set `sdk.dir`
3. create a Google API key
4. enable `Directions API`
5. enable `Places API`
6. put that key into `local.properties`

That is enough for the current repo to build and use the route-aware and provider-backed place-search parts.

## 9. Short Checklist

- [ ] Android SDK installed
- [ ] `sdk.dir` set in `local.properties`
- [ ] Google API key created
- [ ] `Directions API` enabled
- [ ] `Places API` enabled
- [ ] `MAPS_API_KEY` added to `local.properties`
- [ ] optional future APIs enabled later: `Maps SDK for Android`

## 10. Where This Fits With The Other Docs

- `README.md`
  - project overview and quick start
- `SETUP_AND_STATUS.md`
  - current project condition and what is finished
- `PROJECT_STUDY_GUIDE.md`
  - beginner-friendly architecture explanation
- `REQUIRED_UPDATES_AND_APIS.md`
  - exact external values/services you still need to update or provide
