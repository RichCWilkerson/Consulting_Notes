# Firebase
- project structure -> these can go into util package
  - push notifications -> can go into the UI package since it deals with UI

## Continue with social auth (Google, Facebook, etc)


## Analytics
- App downloads/uninstalls, app opens, usage of resources (CPU, memory, etc), how long users stay in app, user demographics, user engagement (screens visited, buttons clicked, etc)
- location data, device information (OS version, device model, etc), custom events (in-app purchases, level completions, etc)
- GDPR compliance: inform users about data collection, obtain consent, provide options to opt-out,

### Setup
- Tools -> Firebase -> Analytics
- get the dependencies

```kotlin
// Fragement
private lateinit var firebaseAnalytics: FirebaseAnalytics

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    firebaseAnalytics = Firebase.analytics
    // log event
    binding.button.setOnClickListener {
        firebaseAnalytics.logEvent(
            name = "button_click",
            params = bundleOf( // these are optional, provide more context about the event
                FirebaseAnalytics.Param.ITEM_NAME to "my_button", // to and pair are interchangeable, choose the one you prefer
                Pair(FirebaseAnalytics.Param.CONTENT_TYPE, "button"),
                FirebaseAnalytics.Param.METHOD to "click"
            )
        ) {

        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }
}
```

## Crashlytics
- Tools -> Firebase -> Crashlytics
- need not just dependencies, but also plugins (more access)
- provides insight into why and how your app is crashing 
- Go to Firebase console to see crash reports, stack traces, and other relevant information

## Push Notifications
- Not triggered by the user
- are called runtime permissions
- Promotional messages, reminders, alerts, etc
- firebase handles a lot of the heavy lifting
  - notification wont be sent if app is in foreground

- recent android versions require push notification permissions
  - 2 chances to ask for permission
  - if they say no -> you cannot ask again
  - if they say not now -> you can ask again later (only 1 more time)

- Permissions
  - checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
  - shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
  - rejected / else case

- Setup [Documentation](https://firebase.google.com/docs/cloud-messaging/android/client)
    - Tools -> Firebase -> Cloud Messaging
    - Add Firebase SDK to your app
    - Add a service of type FirebaseMessagingService
      - add service to AndroidManifest.xml 
    - Define the FirebaseMessagingService in your AndroidManifest.xml
      - handle active app use case (firebase takes care of inactive app use case showing notification)
      - Creates FCM token (unique identifier for the device)
        - use it to send notifications to specific devices
        - used to test -> can specify what device to send notification to, if you don't target, then it goes to all devices (not recommended for production)
      - onRefreshToken() -> called when a new token is generated

```xml
<!--Add to AndroidManifest.xml-->

<!-- this isn't everything we need -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>

    <!-- name and exported are required -->
<service
    android:name=".MyFirebaseMessagingService"
    android:exported="true"> 
    <intent-filter>
        <action android:name="com.google.firebase.MESSAGING_EVENT" />
    </intent-filter>
</service>
```

```kotlin
// MyFirebaseMessagingService.kt
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    // called everytime a new token is generated (refresh, new install, etc)
    // ideally when you use a server to send notifications, you call an API to send the token to your server
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // send token to your server if needed
    }
    
    // triggers everytime app gets a notification while user is actively using the app
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Handle FCM messages here.
        Log.d("FCM", "From: ${remoteMessage.from}")
        // Check if message contains a data payload.
        remoteMessage.data.isNotEmpty().let {
            Log.d("FCM", "Message data payload: ${remoteMessage.data}")
            // Handle message within 10 seconds
            handleNow()
        }
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d("FCM", "Message Notification Body: ${it.body}")
            sendNotification(it.body)
        }
    }
    
    private fun sendNotification(messageBody: String?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "default_channel_id"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // replace with your app icon - don't use urls -> use bitmap or vector drawable
            .setContentTitle(messageBody?.notification?.title ?: "FCM Message")
            .setContentText(messageBody?.notification?.body ?: "You have received a new message.")
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since Android Oreo, notification channels are required
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }
    
}


// MainActivity.kt
class MainActivity : AppCompatActivity() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("FCM", "Permission granted")
            } else {
                Log.d("FCM", "Permission denied")
            }
        }

  
  
  
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkAndRequestNotificationPermission()
        fetchFcmToken()
        
        
    }
  // Check if permission is already granted
  private fun checkAndRequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
      Log.d("FCM", "POST_NOTIFICATIONS not required < API 33")
      return
    }
    when {
      ContextCompat.checkSelfPermission(
        this, Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED -> {
        Log.d("FCM", "Permission already granted")
      }
      shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
        // Show rationale UI if needed, then request
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
      else -> {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
      }
    }
  }

  private fun fetchFcmToken() {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
      if (!task.isSuccessful) {
        Log.w("FCM", "Fetching FCM registration token failed", task.exception)
        return@addOnCompleteListener
      }
      val token = task.result
      Log.d("FCM", "FCM Token: $token")
      Toast.makeText(applicationContext, "FCM Token: $token", Toast.LENGTH_SHORT).show()
    }
  }
    
    
    
}
```

- can now go to Firebase console to send a test notification or create a notification campaign
  - compose notification -> message, target (specific device using the token), scheduling, etc
    - can filter by user properties (location, language, app version, etc) of which users will receive the notification
  - Additional options -> notification channel: settings for promotions, posts, invites, messages, etc.
    - can set importance, sound, vibration, lights, etc
    - users can change these settings in app settings
    - can set how long an offer is for