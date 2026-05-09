# NearWake Real Phone Testing Guide

This guide explains how to test NearWake on a real Android phone from a Windows laptop.

Use this guide when you want to:

- install the app on your own phone
- run the latest debug build quickly
- repeat testing without rebuilding your whole setup each time
- let Codex help with live verification after the phone is connected

This guide is phone-first.

If you want trip-by-trip validation scenarios after setup, use [FIELD_TEST_RUNBOOK.md](FIELD_TEST_RUNBOOK.md).

## 1. What you need

On the laptop:

- Android Studio installed
- Android SDK installed
- Android platform-tools installed
- the NearWake project opened or available locally

On the phone:

- Android phone
- USB data cable
- enough battery for setup and testing

You do **not** need:

- a Windows `.exe` for the app
- an Android emulator
- to recreate the setup every time

## 2. One-time laptop setup

### Install Android Studio

Install Android Studio on the laptop.

During setup, make sure these are available:

- Android SDK
- Android SDK Platform-Tools
- Android Build-Tools

### Verify `adb`

Open PowerShell and run:

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe version
```

If that prints an `adb` version, the laptop side is ready.

## 3. One-time phone setup

### Step 1: enable Developer Options

On the phone:

1. Open `Settings`
2. Open `About phone`
3. Find `Build number`
4. Tap `Build number` 7 times
5. Enter your PIN or password if asked

You should see a message that Developer Options are enabled.

### Step 2: enable USB debugging

On the phone:

1. Go back to `Settings`
2. Open `Developer options`
3. Turn on `USB debugging`

If the phone shows a warning, accept it.

### Step 3: prepare USB mode

When you connect the phone later, use:

- `File transfer`

if Android asks what USB mode to use.

## 4. First connection

### Step 1: connect the phone

Use a USB cable that supports data transfer, not only charging.

Plug the phone into the laptop.

### Step 2: approve the laptop

The phone should show:

- `Allow USB debugging?`

Tap:

- `Allow`

If available, also tick:

- `Always allow from this computer`

### Step 3: verify the phone from PowerShell

Run:

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
```

Expected result:

```text
List of devices attached
R58Nxxxxxxx    device
```

If you see `unauthorized`:

- unlock the phone
- check for the USB debugging popup
- tap `Allow`
- run the command again

If you see nothing:

- reconnect the cable
- switch USB mode to `File transfer`
- try another USB port
- try another cable if needed

## 5. Open the project in Android Studio

1. Open Android Studio
2. Choose `Open`
3. Select:

```text
D:\PROJECTS\Startup\LocationTracker
```

4. Wait for Gradle sync to finish

If Android Studio asks to trust or import Gradle settings, allow it.

## 6. Install the app on the phone

You have two easy ways.

### Option A: Android Studio Run button

This is the easiest repeated workflow.

1. Make sure the phone is connected
2. In Android Studio, pick your phone from the device selector
3. Click the green `Run` button

Android Studio will:

- build the app
- install it on the phone
- launch it

### Option B: PowerShell command

From the repo folder:

```powershell
cd D:\PROJECTS\Startup\LocationTracker
.\gradlew.bat :app:installDebug --no-daemon --max-workers=1 --console=plain
```

This installs the current debug build to the connected phone.

## 7. Install the APK manually if needed

If you already have a built APK, you can install it directly with `adb`.

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r D:\PROJECTS\Startup\LocationTracker\app\build\outputs\apk\debug\app-debug.apk
```

The `-r` flag means:

- reinstall or update the app if it is already present

## 8. Normal repeat workflow after the first setup

After the one-time setup, testing becomes much easier.

Usually you only need:

1. connect the phone
2. unlock it
3. make sure `USB debugging` is still enabled
4. run `adb devices` if you want to confirm the connection
5. click `Run` in Android Studio or run `:app:installDebug`

That is the normal fast loop.

You do **not** need to:

- reinstall Android Studio
- reinstall the Android SDK
- install an emulator
- install a Windows `.exe`

## 9. What to do before each NearWake test session

Before opening the app:

- turn on phone location
- allow notifications
- keep the phone unlocked during first launch
- allow location permissions when asked
- optionally open Google Maps once to help the phone get a fresh location fix

This is especially useful if you want to test:

- place search
- route preview
- trip setup

## 10. Suggested first NearWake test flow

After the app is installed:

1. open NearWake on the phone
2. complete onboarding if shown
3. go to permissions
4. allow notifications
5. allow location
6. open place search
7. type a real place
8. confirm results appear
9. choose one result
10. confirm trip setup opens
11. check whether route preview appears

Important:

If route preview does not appear, it may mean the phone does not yet have a usable last known location.

That is why opening Google Maps once can help before testing the flow.

## 11. How to let Codex help with live testing

Once the phone is connected, you can tell Codex one short message like:

```text
phone connected
```

Then Codex can help with:

- checking `adb devices`
- building the latest app
- installing it
- launching it
- guiding a screen-by-screen test pass
- debugging failures if something crashes or does not respond

## 12. Useful commands

### Check connected devices

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe devices
```

### Install the latest debug app

```powershell
cd D:\PROJECTS\Startup\LocationTracker
.\gradlew.bat :app:installDebug --no-daemon --max-workers=1 --console=plain
```

### Build the debug APK

```powershell
cd D:\PROJECTS\Startup\LocationTracker
.\gradlew.bat :app:assembleDebug --no-daemon --max-workers=1 --console=plain
```

### Launch the app manually

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe shell am start -n com.nearwake.app.debug/com.nearwake.app.MainActivity
```

### Reinstall the APK directly

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe install -r D:\PROJECTS\Startup\LocationTracker\app\build\outputs\apk\debug\app-debug.apk
```

## 13. Troubleshooting

### Problem: `adb devices` shows nothing

Try:

- unlock the phone
- reconnect the cable
- switch USB mode to `File transfer`
- try another USB port
- try another cable

### Problem: device shows `unauthorized`

Try:

- look at the phone screen
- accept the USB debugging popup
- run `adb devices` again

### Problem: install fails

Try:

```powershell
C:\Users\thano\AppData\Local\Android\Sdk\platform-tools\adb.exe uninstall com.nearwake.app.debug
```

Then install again:

```powershell
cd D:\PROJECTS\Startup\LocationTracker
.\gradlew.bat :app:installDebug --no-daemon --max-workers=1 --console=plain
```

### Problem: route preview does not show

Check:

- phone location is on
- permissions are granted
- the phone has a recent location fix
- `MAPS_API_KEY` is configured locally

### Problem: app opens but place search or route data looks wrong

Check:

- network is available
- the phone has Google Play services
- the API setup in `local.properties` is correct for the current build machine

## 14. Best practice for NearWake

For this app, test on a real phone whenever possible.

That is the best way to validate:

- permissions
- notifications
- location
- background survival
- battery behavior

The emulator is useful for quick UI work, but the real phone is the better truth for NearWake.
