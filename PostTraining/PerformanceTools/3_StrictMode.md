# Resources:
- [Medium - Watchdog](https://medium.com/@sandeepkella23/strictmode-your-android-apps-watchdog-4c97be188d57)
- [Medium - Coroutine ANRs](https://blog.stackademic.com/strictmode-coroutines-catch-android-anrs-before-users-do-0ee72765302b)
- [Medium - Performance](https://medium.com/@aniketindulkar/enhance-android-app-performance-a-comprehensive-guide-to-using-strictmode-870fbc18cb82)
- [Medium - Overview](https://medium.com/@rishabhsx/strictmode-androids-most-brutal-truth-teller-18efe22d5117)
  - foundation for much of this doc
- [Developer Docs](https://developer.android.com/reference/android/os/StrictMode)


# StrictMode in Android

## Overview

StrictMode is a developer tool baked into the Android SDK.
It’s a runtime auditor for your code.
Only enable StrictMode in debug builds — it is not intended for production.

It detects dangerous or expensive operations immediately that could lead to:
- Frame drops (jank)
- UI freezes
- ANRs (Application Not Responding)
- Memory leaks

Shown in the form of:
- logs, crashes, or visual cues

What it detects:
- Disk Reads/Writes
- Accessing files, SharedPreferences, or databases on the main thread
- Network on Main Thread
- Any synchronous HTTP or socket call blocking the UI thread 
- Leaked Resources 
- Cursors, input streams, SQLite connections not closed
- Slow Calls
- Use of reflection or expensive APIs that block threads
- Custom Violations
- Your own logic, via callbacks in the StrictMode config


Place this inside your Application.onCreate():
- This configuration enables all detection types, logs violations to Logcat, 
- (optionally) crashes the app when one occurs — forcing the developer to fix it immediately.
```kotlin
if (BuildConfig.DEBUG) {
    // THREAD POLICY: Detect violations on the main (UI) thread
    StrictMode.setThreadPolicy(
        StrictMode.ThreadPolicy.Builder()
            .detectAll() // Enables detection for all thread-related issues:
                         // - Disk reads
                         // - Disk writes
                         // - Network access
                         // - Custom slow calls
                         // - Resource mismatches (e.g. incorrect close)
                         // - Unbuffered IO
            .penaltyLog() // Log the violation to Logcat (recommended default)
            .penaltyDeath() // Crash the app immediately on violation (useful in dev)
            .penaltyDialog() // Show a system dialog alerting the developer
            .penaltyFlashScreen() // Flash the screen when a violation occurs (for visual feedback)
            .penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                // Custom listener (optional): Log or report violations to analytics
                Log.w("StrictMode", "Thread violation: ${violation.violation}")
            }
            .build()
    )

    // VM POLICY: Detect issues related to memory/resource leaks or unsafe operations
    StrictMode.setVmPolicy(
        StrictMode.VmPolicy.Builder()
            .detectActivityLeaks() // Detect when an Activity is not garbage collected
            .detectLeakedClosableObjects() // Detect unclosed Closeable objects (streams, files)
            .detectLeakedRegistrationObjects() // Detect leaked broadcast receivers or listeners
            .detectLeakedSqlLiteObjects() // Detect SQLite DB objects that were not closed
            .detectCleartextNetwork() // Warn if app uses HTTP instead of HTTPS
            .detectContentUriWithoutPermission() // Detect if a content URI is exposed without permission
            .penaltyLog() // Log violations to Logcat
            .penaltyDeath() // Crash app on violation (recommended only in dev)
            .penaltyListener(Executors.newSingleThreadExecutor()) { violation ->
                // Optional custom violation reporting (e.g. send to Firebase or internal logs)
                Log.w("StrictMode", "VM violation: ${violation.violation}")
            }
            .build()
    )
}
```


## Examples

1. SharedPreferences Read on Main Thread
```kotlin
val token = context.getSharedPreferences("session", MODE_PRIVATE)
    .getString("auth_token", null)
```
Log: StrictModeDiskReadViolation: policy=31 violation=3
Fix: Move the SharedPreferences read to a coroutine with Dispatchers.IO

2. Bitmap Decode in Adapter Binding
```kotlin
val bitmap = BitmapFactory.decodeFile(imagePath)
imageView.setImageBitmap(bitmap)
```
Log: StrictModeDiskReadViolation: policy=31 violation=3
Fix: Use Coil or Glide to load the image off-thread with proper caching.


## Best Practices
1. Combine StrictMode with CPU Profiler
   - Detects bad behavior
   - Shows timing and thread usageLogs exact violations
   - Visualizes main thread blocks
   - Prevents jank earlyAnalyzes jank after it happens 
   - Turn on StrictMode and then use CPU Profiler to analyze what impact that violation had on performance. 
     - Together, they give you both immediate feedback and deep insight.
2. Enable StrictMode only in debug builds. 
3. Use penaltyDeath() during development to enforce discipline. 
4. Run your app with StrictMode enabled during QA cycles to catch missed violations. 
5. Combine with Trace.beginSection() in code for more granular CPU profiling. 
6. Educate your team to keep it on throughout local development. 
7. Add custom callbacks to log violations in your internal tools.

