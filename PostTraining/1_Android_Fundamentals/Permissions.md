# Resources:

[Youtube](https://www.youtube.com/watch?v=D3JCtaK8LSU&t=2s&pp=ygUbcGhpbGlwcCBsYWNrbmVyIHBlcm1pc3Npb25z)


# Permissions

## Overview
- Permissions control access to **sensitive user data** (contacts, location, camera, SMS) and **powerful hardware/APIs** (Bluetooth, notifications, background activity).
- Modern Android (10–14) heavily emphasizes **user control and privacy**:
  - Runtime permission prompts.
  - One-time and while-in-use permissions.
  - Background access split from foreground access.
  - Scoped storage and restricted APIs.
- As a senior Android dev you should:
  - Know **which permissions** your features really need.
  - Understand the **runtime vs manifest** model.
  - Design flows that **respect user choice** and degrade gracefully.
  - Be aware of **Play Store policies** around sensitive permissions.

---

## Types of Permissions

### 1. Normal permissions
- Low-risk, automatically granted at install time (no runtime dialog).
- Examples:
  - `INTERNET`
  - `ACCESS_NETWORK_STATE`
  - `BLUETOOTH`
- User is **not** explicitly prompted.

### 2. Dangerous / runtime permissions
- Access to user data or critical hardware; require **runtime request**.
- Grouped into permission groups (e.g., `android.permission-group.CAMERA`).
- Examples:
  - `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`
  - `READ_CONTACTS`, `READ_EXTERNAL_STORAGE` (legacy)
  - `CAMERA`, `RECORD_AUDIO`
- Flow:
  - Declare in `AndroidManifest.xml`.
  - At runtime, call `requestPermissions()` / `ActivityResultContracts.RequestPermission`.
  - System shows a **runtime dialog**; user can allow/deny.

### 3. Special / privileged permissions
- Require extra steps: **special settings screens**, OEM/system only, or Play policy approval.
- Examples:
  - `SYSTEM_ALERT_WINDOW` (draw over other apps).
  - `REQUEST_INSTALL_PACKAGES`.
  - `MANAGE_EXTERNAL_STORAGE` (super-scoped storage / “All files access”).
  - Notification policy access.
- Typically obtained via **Settings screens** rather than simple runtime requests.

### 4. Background vs foreground permissions
- For sensitive areas like **location** and **Bluetooth**, Android distinguishes:
  - *Foreground/while-in-use*: e.g., `ACCESS_FINE_LOCATION` when app is visible.
  - *Background*: e.g., `ACCESS_BACKGROUND_LOCATION` to keep accessing when app is not visible.
- Background access often requires **two-step flows** and strong justifications in Play Console.

### 5. Newer Android 12+ specific permissions
- Split and more granular:
  - Bluetooth: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`.
  - Nearby devices: `NEARBY_WIFI_DEVICES` (Android 13).
  - Precise vs approximate location (user can choose).
  - Notifications: `POST_NOTIFICATIONS` (Android 13+).

---

## Setting Permissions

### 1. Declare in `AndroidManifest.xml`
- Always declare what you need in the manifest:
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```
- For some features you also declare `uses-feature`:
```xml
<uses-feature android:name="android.hardware.camera.any" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />
```
- TODO: `uses-feature` is specifically for?


### 2. Request at runtime (Activity/Fragment)
- Classic pattern (pre-Activity Result APIs):
```kotlin
if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
    != PackageManager.PERMISSION_GRANTED
) {
    ActivityCompat.requestPermissions(
        this,
        arrayOf(Manifest.permission.CAMERA),
        REQUEST_CAMERA
    )
} else {
    openCamera()
}
```

- Recommended modern approach (Activity Result APIs):
```kotlin
private val cameraPermissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { granted ->
    if (granted) openCamera() else showCameraDeniedUi()
}

fun onCameraButtonClicked() {
    // TODO: what does this do? launch with Manifest file?
    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
}
```

TODO: it doesn't need to go here, but i was thinking about where permission functions live. I would assume it's best to create a directory in the domain layer or create a feature-module to house all permissions to share across whichever features need it to reduce duplication if it comes up. 

### 3. Request in Jetpack Compose
- Use `rememberLauncherForActivityResult` inside a composable and delegate the actual action back to a ViewModel or callback:
```kotlin
@Composable
fun CameraButton(onCameraReady: () -> Unit) {
    val context = LocalContext.current

    // TODO: ActivityResultContracts is a class i create? and RequestPermission is a function i create? 
    // TODO: what would those classes or functions look like?
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onCameraReady() else {
            // show denied UI (e.g., Snackbar)
        }
    }

    Button(onClick = {
        // TODO: explain each of these lines, what is ContextCompat, etc.?
        if (ContextCompat.checkSelfPermission(
                context, // TODO: is this app or activity context?
                Manifest.permission.CAMERA // we are passing in the permission we want to check in the Manifest file? or is this checking if the permission is already granted? are granted permissions stored on device or do we need to keep track?
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onCameraReady()
        } else {
            // TODO: does this automatically launch a ui to ask for the permission? or do i need to design it?
            launcher.launch(Manifest.permission.CAMERA)
        }
    }) {
        Text("Open Camera")
    }
}
```

### 4. Explaining rationale
- If user denies a permission but you need it:
  - Show a **rationale** before re-requesting: explain why you need it.
  - Use `shouldShowRequestPermissionRationale()` to detect when to show rationale.
- If user selects “Don’t ask again” / permanently denies:
  - Now if a user wants the functionality you need to guide them to **Settings** with a clear explanation.
    - cannot use ask permission again

---

## Common Pitfalls

1. **Requesting too many permissions at once**
   - Asking for camera + location + contacts on first launch scares users.
   - Better: **just-in-time** requests tied to a user action.

2. **Requesting permissions you don’t actually need**
   - Causes Play Store review issues and erodes user trust.
   - Example: asking for location when you only need coarse region for analytics.

3. **Ignoring API level differences**
   - Android 12+ Bluetooth permissions vs older `BLUETOOTH_ADMIN`.
   - Notification permission (`POST_NOTIFICATIONS`) only on Android 13+.

4. **Not handling denial gracefully**
   - App crashes or shows blank screens when permission is denied.
   - Good UX: explain limited functionality and offer alternatives.

5. **Background access overuse**
   - Background location or Bluetooth scanning can trigger Play policy rejections.
   - Use only when necessary and document carefully.

6. **Not checking permission before sensitive calls**
   - Calling camera, location, or sensors without checking → security exceptions.

7. **Not testing “don’t ask again” flows**
   - Many apps only test first-request happy path.
   - You must also test permanent denial and Settings redirection.

---

## Best Practices

1. **Least privilege**
   - Request the **minimal set** of permissions needed for a feature.
   - Prefer approximate location over precise if that’s enough.

2. **Just-in-time requests**
   - Ask for permission at the moment the user tries to use a feature.
   - Example: request camera permission when they tap “Scan card”, not on app launch.

3. **Clear user communication**
   - Explain **why** you need the permission, in user language.
   - Tie it to value: “We need location to show nearby stores and same-day delivery options.”

4. **Graceful degradation**
   - If user denies permission, app should still be usable in a limited way.
   - Example: if location denied, let them search by ZIP code.

5. **Handle “don’t ask again” states**
   - Detect permanent denial and show a “Go to Settings” flow with explanation.

6. **Respect privacy and policies**
   - Don’t log or send more data than necessary.
   - Follow Play Store and OS guidelines for sensitive permissions.

7. **Test across API levels**
   - Emulators/devices with Android 10, 11, 12, 13+ to verify behavior.
   - Test rotation, process death, and restore flows while permissions are in different states.

8. **Separate concern in code**
   - Keep permission handling logic close to UI layer but not buried in business logic.
   - Example: ViewModel exposes “needsPermission” state, Activity/Composable triggers the actual request.

---

## Interview Questions

1. **What’s the difference between normal and dangerous permissions in Android?**
> Normal are auto-granted at install
> Dangerous require runtime user consent.

2. **How do you request a dangerous permission at runtime?**
> Declare in manifest, check with `checkSelfPermission`, and request via `ActivityResultContracts.RequestPermission` or `requestPermissions`. 
> Handle the callback result.

3. **How do permissions differ across Android versions (e.g., location, Bluetooth, notifications)?**
> Android 10+ split background vs foreground location.
> Android 12+ split Bluetooth.
> Android 13 added `POST_NOTIFICATIONS` and `NEARBY_WIFI_DEVICES`.

4. **How would you design a good UX around a permission that’s critical for a feature?**
> Explain why, request just-in-time, show rationale on denial, offer limited functionality if denied, provide a Settings redirect for “don’t ask again.”

5. **How do you handle when a user selects “Don’t ask again” on a permission dialog?**
> Detect with `shouldShowRequestPermissionRationale` returning false after denial.
> then show an in-app explanation and navigate them to system Settings.

6. **How would you test permission flows in an Android app?**
> Instrumentation tests that simulate grant/deny, configuration changes, and “don’t ask again”.
> Verify UI and behavior; test across API levels.

7. **In a multi-feature app, how do you avoid over-requesting permissions?**
> Least privilege, per-feature analysis, just-in-time requests, and possibly feature flags/remote config to control rollout of permission-gated features.
