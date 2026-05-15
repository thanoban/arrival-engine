# NearWake App Signing Setup Guide

This file is the release-signing preparation guide for NearWake.

It does not create a final signing configuration by itself.
Use it to set up the missing secrets and local release-signing values when you are ready to produce a real store build.

## 1. What This Solves

Before a public Play Store release, you need a real signing key and a release configuration that is not hardcoded into the repo.

This guide covers:

- what needs to exist outside git
- where to put local signing values
- how to verify the release build after setup

## 2. Files And Secrets That Should Stay Local

Do not commit these into the repo:

- release keystore file
- keystore password
- key alias password
- Play Console account secrets

Recommended local-only inputs:

- keystore path
- keystore password
- key alias
- key password

## 3. Recommended Local Storage Pattern

Keep release-signing values in a local machine file such as `keystore.properties` at the repo root and do not commit it.
You can start from `keystore.properties.template`.

Suggested format:

```properties
storeFile=C:\\path\\to\\nearwake-release.keystore
storePassword=REPLACE_ME
keyAlias=nearwake
keyPassword=REPLACE_ME
```

If you prefer, you can also keep the values in environment variables on the build machine.

## 4. Keystore Creation Notes

Before final release, create a dedicated signing key for NearWake and store it somewhere backed up and access-controlled.

Typical information you will need to decide:

- keystore file location
- key alias name
- password storage method
- who controls backup and recovery

## 5. Build Integration Expectations

The app module now expects this local file for real release tasks such as `:app:assembleRelease` and `:app:bundleRelease`.

The final release integration should:

- read signing values from a local, untracked source
- fail clearly if release signing is requested without the required values
- avoid committing any secrets or real file paths into git

## 6. Verification After Setup

After local signing values are wired, run:

```powershell
.\gradlew.bat :app:assembleRelease
```

If you later add bundle output for store upload, also verify the release artifact you intend to submit.

## 7. Operational Checklist

- [ ] create the NearWake release keystore
- [ ] store it in a backed-up, access-controlled location
- [ ] choose the final key alias
- [ ] create local signing-value storage such as `keystore.properties`
- [ ] wire the app module to read the local signing values
- [ ] verify `:app:assembleRelease`
- [ ] record who owns the keystore backup and recovery path

## 8. What Still Remains After This Guide

Even after signing is configured, public release still depends on:

- completed field testing
- final store assets
- hosted privacy policy URL
- final Play Console answers and policy review
