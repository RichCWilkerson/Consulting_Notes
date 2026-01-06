# Android Components
- Activities
  - Activity Lifecycles
  - starting point of the app
- Services
  - running in Background without UI
  - can execute long running operations
  - need to create a class that extends Service or IntentService
  - started by calling startService() or bindService()
  - can be stopped by calling stopSelf() or stopService()
- Broadcast Receivers
  - handles all the announcements for system or app events
  - Offline, 
- Content Providers
  - access shared content on the current device (e.g. contacts, media, SMS, images, etc)


## Basics
- each of main components needs to be declared in AndroidManifest.xml so it can register with the system (app)
- each component has its own lifecycle and callback methods 
  - if Activity is onPause() or onStop(), the Service can still run in the background
- components can interact with each other using Intents
  - Intent -> messaging object used to request an action from another app component
  - explicit Intent -> specify the component to start by name (e.g. start a specific Activity or Service within your app)
  - implicit Intent -> do not specify the component name, instead declare a general action to perform (e.g. open a web page, send an email, make a call, etc)
- components can be started by other apps or system events (e.g. incoming call, SMS, network change, etc) using Intents


## Service
- onBind() -> return IBinder for clients to communicate with the service
  - it closes the service when all clients unbind
- onStartCommand() -> called when a client starts the service by calling startService()
  - it keeps the service running until it is explicitly stopped
  - START_NOT_STICKY -> if the system kills the service, it won't restart it
  - START_STICKY -> if the system kills the service, it will restart it with a null intent
  - START_REDELIVER_INTENT -> if the system kills the service, it will restart it with the last intent
- onCreate() -> called when the service is first created
  - it is used to initialize the service
- onDestroy() -> called when the service is destroyed
  - it is used to clean up resources

```kotlin
class MyService : Service() {
    // when you want to download a file, play music, sensor data, location updates, etc
    // long running operations that don't need user interaction
    override fun onBind(intent: Intent?): IBinder? {
        // Return null if clients cannot bind to the service
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the service start request
        // Perform long-running operations here
        return START_STICKY // or START_NOT_STICKY, START_REDELIVER_INTENT based on your needs
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize the service
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
    }
}
```

## Content Provider
- is resource available? (Camera, Contacts, Media, etc)
  - PERMISSIONS -> need to request permissions in AndroidManifest.xml and at runtime (for dangerous permissions)
    - need to check if permission is granted before accessing the resource
- ContentManager -> provides access to content providers
- ContentResolver -> communicates with the provider


## Broadcast Receiver
- onReceive() -> called when the BroadcastReceiver is receiving an Intent broadcast (e.g. system event, custom event)
  - it is used to handle the received broadcast
- like when your device says "No Internet Connection" or "Battery Low" or "SMS Received" or "Network Available"
- simple -> it just listens for broadcasts and reacts to them

```kotlin

class MainActivity : AppCompatActivity() {
    private lateinit var networkReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val filter = IntentFilter()
        filter.addAction("com.example.NETWORK_CHANGE")
        registerReceiver(networkReceiver, filter)
        
        // Initialize the BroadcastReceiver
        networkReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Handle the received broadcast
                val isConnected = intent?.getBooleanExtra("isConnected", false) ?: false
                if (isConnected) {
                    Toast.makeText(context, "Network Connected", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Network Disconnected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Register the BroadcastReceiver to listen for network changes
        val filter = IntentFilter("com.example.NETWORK_CHANGE")
        registerReceiver(networkReceiver, filter)
    }

    override fun onPause() {
        super.onPause()
        // Unregister the BroadcastReceiver to avoid memory leaks
        unregisterReceiver(networkReceiver)
    }
}
```