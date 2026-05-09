# Required Updates And APIs

This file lists the exact things you still need to update or provide outside the codebase.

It is separate from the architecture/status docs on purpose.
Use this as the operational checklist for machine setup, API setup, and later release setup.

## 1. Fastest Team Setup

If a new team member is setting up the project for the first time, this is the exact minimum flow:

1. install Android Studio with the Android SDK and Platform-Tools
2. copy `local.properties.template` to `local.properties`
3. create one Google Cloud API key
4. enable `Directions API`
5. enable `Places API`
6. paste that key into `local.properties` as `MAPS_API_KEY`
7. run `.\gradlew.bat :app:assembleDebug`

That is the current local-development integration method for this repo.

## 2. Things You Must Update Right Now

These are the only things you must set to build and use the app properly on your machine.

### `local.properties`

Update or create:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

The Gradle build reads `MAPS_API_KEY` from `local.properties` for local development. You can also pass it as a Gradle property when needed, but `local.properties` is the normal team setup path here.

What each value means:

- `sdk.dir`
  - path to your Android SDK
  - required for Android builds
- `MAPS_API_KEY`
  - Google API key used by the current routing integration
  - enables transit route preview, ETA refresh support, and provider-backed destination search

Important:

- `local.properties` is local machine config
- do not commit a real key into Git
- each teammate should usually keep their own local `local.properties`

## 3. Exact Google Cloud Setup

Use these steps when a teammate asks, "Where exactly do I create and add the API key?"

### Step 1: open Google Cloud

- Google Cloud Console: [https://console.cloud.google.com/](https://console.cloud.google.com/)
- APIs & Services Library: [https://console.cloud.google.com/apis/library](https://console.cloud.google.com/apis/library)
- Credentials page: [https://console.cloud.google.com/apis/credentials](https://console.cloud.google.com/apis/credentials)

### Step 2: create or select the project

Use the project that owns the NearWake Google API key.

Click path:

1. open the project picker at the top of Google Cloud Console
2. select the NearWake Google Cloud project
3. wait until the project switch completes before enabling APIs or creating credentials

### Step 3: enable the required APIs

Enable:

- `Directions API`
- `Places API`

Optional later:

- `Maps SDK for Android`

Click path:

1. open `APIs & Services`
2. open `Library`
3. search for `Directions API`
4. open it and click `Enable`
5. go back to `Library`
6. search for `Places API`
7. open it and click `Enable`
8. only if you later need map UI, search for `Maps SDK for Android` and enable it too

### Step 4: create one API key

On the Credentials page:

1. click `Create credentials`
2. choose `API key`
3. copy the created key value

Click path:

1. open `APIs & Services`
2. open `Credentials`
3. click `Create credentials`
4. click `API key`
5. Google will show the created key in a popup
6. copy that value immediately
7. save it into a temporary note only if needed
8. then move it into `local.properties`

Current repo method:

- one `MAPS_API_KEY`
- one key used by both the current Directions and Places integrations

### Step 5: add the key locally

Create or update:

```text
local.properties
```

If the file does not exist yet:

1. copy `local.properties.template`
2. rename the copy to `local.properties`

with:

```properties
sdk.dir=C:\\Users\\<your-user>\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=YOUR_GOOGLE_API_KEY
```

Practical edit method:

1. open the repo root
2. open `local.properties` in Android Studio or any text editor
3. keep the `sdk.dir` line
4. replace only the `MAPS_API_KEY` value
5. save the file

Example:

```properties
sdk.dir=C:\\Users\\thano\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=PASTE_THE_CREATED_GOOGLE_KEY_HERE
```

Do not:

- add quotes around the key
- add spaces around `=`
- commit the real key to Git
- paste the key into markdown or source code files

### Step 6: build and verify

Run:

```powershell
.\gradlew.bat :app:assembleDebug --no-daemon --max-workers=1 --console=plain
```

If that succeeds, the local key is wired into the app build correctly.

## 4. Android Key Restriction Setup

For local Android testing in this repo, the current debug and QA app identities are:

- package: `com.nearwake.app.debug`
- package: `com.nearwake.app.qa`

Current debug/QA SHA-1:

```text
59:B9:4C:7F:D5:57:FF:A0:BA:81:0C:99:F1:C5:8F:07:CA:3C:CF:FE
```

These values were verified from:

```powershell
.\gradlew.bat :app:signingReport --no-daemon --max-workers=1 --console=plain
```

Recommended local restriction setup in Google Cloud:

1. open the API key
2. under `Application restrictions`, choose `Android apps`
3. add:
   - `com.nearwake.app.debug` + SHA-1 above
   - `com.nearwake.app.qa` + SHA-1 above
4. under `API restrictions`, allow only:
   - `Directions API`
   - `Places API`
   - `Maps SDK for Android` only if you later enable/use it

Click path:

1. open `APIs & Services`
2. open `Credentials`
3. click the API key you created
4. scroll to `Application restrictions`
5. choose `Android apps`
6. click `Add`
7. enter `com.nearwake.app.debug`
8. paste the debug SHA-1 above
9. click `Add` again
10. enter `com.nearwake.app.qa`
11. paste the same SHA-1 above
12. scroll to `API restrictions`
13. choose `Restrict key`
14. select `Directions API`
15. select `Places API`
16. optionally select `Maps SDK for Android` only if you enabled it
17. click `Save`
18. wait a few minutes for the restriction change to propagate

Important release note:

- `com.nearwake.app` release signing is **not configured yet**
- release must use its own real release keystore SHA-1 later
- do not reuse the debug fingerprint as the final production release identity

## 5. APIs You Should Enable Now

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

## 6. APIs Likely Needed Later

These are not strictly required for the current code path, but they are likely part of the later full product path.

- `Maps SDK for Android`

Why:

- the long-term plan includes map-based integrations

## 7. What Happens If You Do Not Add `MAPS_API_KEY`

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

## 8. Things You Do Not Need Right Now

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

## 9. Things To Update Later Before Full Release

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

For the full release-prep checklist, field-test matrix, and permission disclosure map, use [RELEASE_READINESS_CHECKLIST.md](RELEASE_READINESS_CHECKLIST.md).
For local release-signing preparation, use [APP_SIGNING_SETUP_GUIDE.md](APP_SIGNING_SETUP_GUIDE.md).
For the Sri Lanka transport-data production path, official-source links, and contact checklist, use [SRI_LANKA_PRODUCTION_DATA_PLAN.md](SRI_LANKA_PRODUCTION_DATA_PLAN.md).

## 10. Recommended Google Key Setup

For your `MAPS_API_KEY`, you should:

- enable `Directions API`
- enable `Places API`
- restrict the key appropriately in Google Cloud Console
- avoid using an unrestricted production key

Recommended restriction direction:

- Android app restrictions for debug/QA local builds where relevant
- API restrictions limited to the services you actually use

Official references:

- Google Maps Platform security best practices: [https://developers.google.com/maps/api-security-best-practices](https://developers.google.com/maps/api-security-best-practices)
- Places SDK for Android setup: [https://developers.google.com/maps/documentation/places/android-sdk/get-api-key](https://developers.google.com/maps/documentation/places/android-sdk/get-api-key)
- Maps SDK for Android setup: [https://developers.google.com/maps/documentation/android-sdk/get-api-key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)

## 11. Practical Minimum Setup

If you want the smallest possible setup to continue development:

1. install Android SDK
2. set `sdk.dir`
3. create a Google API key
4. enable `Directions API`
5. enable `Places API`
6. put that key into `local.properties`

That is enough for the current repo to build and use the route-aware and provider-backed place-search parts.

## 12. New Team Member Checklist

Use this exact checklist for onboarding a new developer to the Google API setup:

- [ ] Android Studio installed
- [ ] Android SDK installed
- [ ] `local.properties` created from `local.properties.template`
- [ ] Google Cloud project selected
- [ ] `Directions API` enabled
- [ ] `Places API` enabled
- [ ] one API key created
- [ ] API key added to `local.properties` as `MAPS_API_KEY`
- [ ] Android app restrictions added for `com.nearwake.app.debug`
- [ ] Android app restrictions added for `com.nearwake.app.qa`
- [ ] correct debug/QA SHA-1 entered
- [ ] `.\gradlew.bat :app:assembleDebug` passes

## 13. Short Checklist

- [ ] Android SDK installed
- [ ] `sdk.dir` set in `local.properties`
- [ ] Google API key created
- [ ] `Directions API` enabled
- [ ] `Places API` enabled
- [ ] `MAPS_API_KEY` added to `local.properties`
- [ ] optional future APIs enabled later: `Maps SDK for Android`

## 14. Where This Fits With The Other Docs

- `README.md`
  - project overview and quick start
- `SETUP_AND_STATUS.md`
  - current project condition and what is finished
- `PROJECT_STUDY_GUIDE.md`
  - beginner-friendly architecture explanation
- `REQUIRED_UPDATES_AND_APIS.md`
  - exact external values/services you still need to update or provide
- `RELEASE_READINESS_CHECKLIST.md`
  - pre-release validation, field testing, and disclosure prep
- `SRI_LANKA_PRODUCTION_DATA_PLAN.md`
  - Sri Lanka transport-data sources, official contact path, and production data strategy
