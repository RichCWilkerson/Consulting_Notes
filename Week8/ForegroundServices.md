# Foreground Services

## Quick Purpose
Use a foreground service for user-noticeable, ongoing work that must continue even if the UI goes to background (music playback, active workout tracking, navigation turn-by-turn). Prefer WorkManager / JobScheduler for deferrable, non user‑visible tasks.


## Core Concepts
- **Service**: Component with no UI that can run even when activity not visible. Do not assume it runs forever—system may kill.
  - prefer WorkManager
- **Foreground Service (FGS)**: A started service promoted via `startForeground(id, notification)` with an ongoing notification (must be shown within 5 seconds of starting on Android 12+). Higher priority, less likely to be killed.
  - e.g. exercise tracker like running with GPS
- **Notification Channel** (API 26+): Required for posting notifications; user controls importance and can turn them on or off
  - e.g. turn off all notifications except direct messages
- **Foreground Service Types** (manifest `android:foregroundServiceType`): Declare specific resource usage (e.g., `location`, `mediaPlayback`, `camera`, `microphone`, `dataSync`, `connectedDevice`). Android 14 tightened policy; use the smallest applicable set.


## Common Misconceptions (Corrections)
- You do NOT always "require two permissions". Base requirement: `<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>`. Additional specific permissions only if using special types (e.g., location, camera) or posting notifications (runtime permission `POST_NOTIFICATIONS` on API 33+). 
- Returning `super.onStartCommand` is ambiguous; explicitly return a start mode: `START_STICKY`, `START_NOT_STICKY`, or `START_REDELIVER_INTENT` based on desired restart semantics.
- Use `ContextCompat.startForegroundService(context, intent)` when starting an FGS from a context that might be in background (Android 8.0+ restrictions). Then call `startForeground(...)` quickly (≤5s) in `onStartCommand`.
- Call `stopForeground(true)` (to remove the notification) before `stopSelf()` when you are done.
- Heavy work must NOT run on the main thread; create your own coroutine scope or use another threading mechanism. A Service does not have a built-in lifecycle scope like Activities/Fragments.
  - cancel coroutines in `onDestroy` to avoid leaks.


## Interview Talking Points
1. Lifecycle: `onCreate`, `onStartCommand`, `onDestroy`
2. Best Practices: Minimal work in service class (delegate logic), keep notification informative & ongoing, choose low/medium importance channel to avoid noise. 
3. Alternatives: Media playback—consider MediaSession + `foregroundServiceType="mediaPlayback"`. Location—prefer `FusedLocationProviderClient` with good batching. Long downloads—use `DownloadManager` or WorkManager.
4. Security/Privacy: Minimally request permissions (e.g., location precise vs approximate). Explain why an ongoing notification is required (transparency to user).


## Typical Steps 
1. Create service class extending `Service`.
2. Decide if started or bound; foreground services are usually started (`startService` / `ContextCompat.startForegroundService`).
   - e.g., music player is started; music controller UI could bind to it for controls.
3. In `onStartCommand`, handle actions and promote to foreground (`startForeground`). Return an explicit start mode (`START_STICKY`, etc.).
4. Build notification + channel (importance LOW or DEFAULT; set `setOngoing(true)` and a content intent to reopen UI).
   - typically done in Application class on app start
5. Declare service + base permission in manifest:
   - (OPTIONAL) `android:foregroundServiceType`. specific permissions if needed (e.g., location, camera). 
6. Provide UI controls to start/stop.
7. Offload work to background (coroutines/executors) and clean up on `onDestroy`.


## Start Modes Cheat Sheet
- `START_STICKY`: System recreates service after kill; intent null; good for ongoing indefinite tasks (music).
- `START_NOT_STICKY`: System does not recreate; good for short tasks that can be abandoned.
- `START_REDELIVER_INTENT`: System restarts and re-delivers last intent; good for tasks needing completion.


## Troubleshooting / Edge Cases
- Notification not appearing → channel importance may be NONE or user disabled; cannot run FGS without visible notification.
- Service killed unexpectedly → verify start mode, battery optimizations, OEM kill policies.
- Multiple starts → make `startForegroundWork()` idempotent (check if already running).

## Code (Improved Example GPT)
```kotlin
class RunningService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null // Not a bound service

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.name -> startForegroundWork()
            Actions.STOP.name -> stopForegroundService()
        }
        return START_STICKY // choose based on your use-case
    }

    private fun startForegroundWork() {
        val notification = buildNotification()
        // Must be called quickly after service start
        startForeground(NOTIF_ID, notification)
        // Launch long-running work off main thread
        scope.launch {
            // Simulated ongoing task; replace with real logic
            while (isActive) {
                delay(5_000)
                // do periodic work (e.g., location sample, progress update)
            }
        }
    }

    private fun buildNotification(): Notification {
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Running Service")
            .setContentText("Foreground service active")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent)
            .build()
    }

    private fun stopForegroundService() {
        scope.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE) // removes notification
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    enum class Actions { START, STOP }

    companion object {
        private const val CHANNEL_ID = "running_channel"
        private const val NOTIF_ID = 1
    }
}
```

### Application Channel Setup
```kotlin
class RunningApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // O is for Oreo (8.0) -> Notification Channels introduced
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Running Service Channel",
                NotificationManager.IMPORTANCE_LOW // low to avoid noise, still visible
            ).apply { description = "Channel for running foreground service" }
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
    companion object { const val CHANNEL_ID = "running_channel" }
}
```

### Starting / Stopping (Activity or ViewModel)
```kotlin
fun startRunningService(context: Context) {
    val intent = Intent(context, RunningService::class.java).apply { action = RunningService.Actions.START.name }
    ContextCompat.startForegroundService(context, intent)
}

fun stopRunningService(context: Context) {
    val intent = Intent(context, RunningService::class.java).apply { action = RunningService.Actions.STOP.name }
    context.startService(intent) // still uses startService to deliver STOP action
}
```

---

## Original (Simplified) Reference Lackner Example
```kotlin
class RunningService: Service() {
    
    // Binder provides the ability to have a single active instance of a service, 
    // and multiple components can connect to that instance
    
    // required override for Services 
  override fun onBind(p0: Intent?): IBinder? {
      return null // we don't want anything to bind to our service
  }
  // NEED TO IMPLEMENT HOW THE SERVICE STARTS AND ENDS
  // onStartCommand is triggered when any other android components sends an intent to this service
  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
      when(intent?.action) {
          Actions.START.toString() -> start()
          Actions.STOP.toString() -> stopSelf() // stopSelf is a predefined function to stop the service
      }
      return super.onStartCommand(intent, flags, startId)
  }
  
  // need to ensure the app is visible for the user
  private fun start() {
      // remember that services also inherit from Context, so we can use 'this' to refer to the context
      // by assigning to this context, it is now tying the notification to this services lifecycle
      // notification channel is a requirement for notifications on android 8.0+
      // channels are used to group notifications and allow users to customize notification settings for each channel
      // e.g., sound, vibration, importance, messages, price drop, etc.
      val notification = NotificationCompat.Builder(this, "running_channel")
          .setContentTitle("Running Service")
          .setContentText("The service is running in the foreground") // description of notification
          .setSmallIcon(R.drawable.ic_launcher_foreground) // required to provide an icon
          .build()
      startForeground(1, notification)
  }
  
  enum class Actions {
      START, STOP
  }
}

// Application class
class RunningApp: Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        // Notification channels are required for notifications on Android 8.0+
        // .O for Oreo version 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                id = "running_channel",
                name = "Running Service Channel", // name of the channel visible to users
                importance = NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for Running Service notifications"
            }
            // need to get an android system service to create the notification channel
            // our app is not allowed to create notification channels directly, Android provides this system service to manage notification channels
            // we cast this to NotificationManager because it can return different types of system services
            // if we didn't cast it, we would get a generic Object type that we can't use
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
// MainActivity.kt
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // super simple permission request for POST_NOTIFICATIONS -> see Permission notes for more details
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                activity = this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                requestCode = 0
            )
        }
        setContent {
            ForegroundServicesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = {
                            val startIntent = Intent(this@MainActivity, RunningService::class.java).apply {
                                action = RunningService.Actions.START.toString()
                            }
                            startService(startIntent)
                            // or 
                            Intent(this@MainActivity, RunningService::class.java).also {
                                it.action = RunningService.Actions.START.toString()
                                startService(it)
                            }
                        }) {
                            Text(text = "Start Service")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val stopIntent = Intent(this@MainActivity, RunningService::class.java).apply {
                                action = RunningService.Actions.STOP.toString()
                            }
                            startService(stopIntent)
                        }) {
                            Text(text = "Stop Service")
                        }
                    }
                }
            }
        }
    }
}
```

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.foregroundservices">

<!-- this is not a dangerous permission -> don't need to ask for permission from user for this -->
<!-- this just lets the user see what permissions the app has on google play -->
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<!-- this is a sensitive permission -> requires user to say ok to -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    
    <application
        android:name=".RunningApp">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <service
            android:name=".RunningService"
            android:exported="false" />
<!-- there are different kinds of foregroundServices -> these are required for certain types of foreground services e.g. camera, location, etc. -->
<!-- can determine what kind of intents it can receive by using intent-filter inside <service> -->
    </application>
</manifest>
```
