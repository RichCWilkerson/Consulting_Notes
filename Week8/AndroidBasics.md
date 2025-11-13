[Phillip Lackner Playlist](https://www.youtube.com/watch?v=SJw3Nu_h8kk&list=PLQkwcJG4YTCSVDhww92llY3CAnc_vUhsm&index=1)

# Activities
- not just a screen container
  - is it currently active? in the background? 
  - entry point to the app
- onDestroy is called:
  - configuration change (e.g. rotation)
  - user navigates away
  - system kills the activity to reclaim resources

# Tasks, Back Stack and Launch Modes
[Article](https://medium.com/huawei-developers/android-launch-modes-explained-8b32ae01f204) with good examples
- Task: collection of screens/activities on the back stack
  - click on a link from your app to instagram (different app) -> new task created with different back stack
  - composable that are defined by a NavController are added onto the NavController back stack, not the Android task back stack
    - NavController is part of the Activity
- Back Stack: LIFO stack of activities
- Launch Modes: set behavior when new activity is pushed onto the back stack
  - **Standard** (default): new instance every time
  - **SingleTop**: if instance is on top of back stack, reuse it
    - useful if user leaves app and then comes back, we can send them to where they left off instead of creating a new instance
  - **SingleTask**: if instance exists anywhere in back stack, pop everything above it and reuse it
    - use when: want to avoid multiple instances of the same activity in back stack (e.g., main/home activity)
  - **SingleInstance**: like SingleTask but no other activities can be started in the same task
    - this means the backstack will only ever have one activity 
      - you can have multiple tasks with different back stacks
    - useful for activities that should be unique across the system (e.g. phone dialer, payment gateway)
  - **SingleInstancePerTask**: like SingleInstance but can start other activities in a new task
    - rare use case 

# View Models and Configuration Changes
- by inheriting from ViewModel class, data is retained across configuration changes, otherwise it is lost
  - view model is no longer tied to activity lifecycle

- to initialize a view model inside a composable, you must use the dependency: `implementation "androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1"`

```kotlin
class MainActivity : ComponentActivity() {
    private val viewModel = myViewModel() // this will be recreated on configuration change, instead use:
    private val viewModel by viewModels<myViewModel>() // this will retain the same instance across configuration changes
}

// two ways to get view model instance inside a composable:
// 1. pass it as a parameter
// 2. use the viewModel() function from androidx.lifecycle.viewmodel.compose
@Composable
fun MyScreen(viewModel: myViewModel = viewModel()) {
    // this will retain the data across configuration changes because we are using the dependency above
    // the problem with this approach, is if we have parameters in the view model constructor, we cannot use this method out of the box
    // we need to create a ViewModelFactory to pass parameters to the view model constructor
    val viewModel: myViewModel = viewModel() // this will get the existing view model instance or create a new one if it doesn't exist
    // Factory example:
    val viewModel: myViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return myViewModel(param1, param2) as T
            }
        }
    )
}

class myViewModel: ViewModel() {
    var count by mutableStateOf(0)
        private set 
    // above is a short way of writing:
    private var _count by mutableStateOf(0)
    val count: Int = _count
        
}
```

# Context
- in android it is nothing more than an instance of a class
- a bridge between your android app and the android system (e.g., phone hardware, resources, etc.)
- provides your app to operate within the whole android ecosystem
  - need images, strings, themes, access to system services (e.g., location, camera, etc.)
  - database access, preferences, file system access
- middleman between app and system
- Context is a superclass of Activity and Application classes
  - Context has the same lifecycle as the application or activity it is associated with
  - this can lead to memory leaks if not handled properly (meaning holding a reference to a context longer than its lifecycle -> resources not being freed up)
    - if you save the context from an activity inside a view model, the view model will outlive the activity and the context will not be freed up when the activity is destroyed
    - to avoid this, use application context instead of activity context when saving context in a view model
      - application context has the same lifecycle as the application, so it will not lead to memory leaks
      - mostly prefer not to use context in view models, but if you have to, use application context
- why use activity context?
  - temporary permissions (e.g., camera, location)
  - UI related tasks (e.g., showing a dialog, toast, snackbar)
- why use application context?
  - long lived operations (e.g., database access, network calls)
  - accessing resources (e.g., strings, images)
  - system services (e.g., location, connectivity)

```kotlin
class MyApp: Application() {
    override fun onCreate() {
        super.onCreate()
        this // refers to application context
        val context = applicationContext // also refers to application context
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this // refers to activity context
        val context = this.applicationContext // refers to application context the activity is associated with
        
        // example of permission
        ActivityCompat.requestPermissions(
            this, // activity context -> not application context
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }
}

```

# Resources and Qualifiers
- resources: external files and static content (e.g., images, strings, layouts, colors, dimensions)
  - non-code assets that your app uses
  - found under the `res` directory in an Android project
- qualifiers: suffixes added to resource directories to specify different versions of resources for different device configurations (e.g., screen size, orientation, language)
  - v# qualifier: specifies the minimum API level required for the resource to be used
    - e.g., `values-v21` means the resources in this directory will only be used on devices running API level 21 (Lollipop) or higher
  - can also base qualifiers on:
    - screen size (e.g., `layout-sw600dp` for devices with a minimum width of 600dp)
    - orientation (e.g., `layout-land` for landscape mode)
    - language (e.g., `values-fr` for French language resources)
    - dark mode (e.g., `values-night` for dark theme resources)
  - can combine multiple qualifiers (e.g., `icon-v21-night` for icons used on API level 21+ in dark mode)
- must be accessed via context or activity instance

- mipmap -> icon for app launcher

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        resources // access to resources of the app
        applicationContext.resources // access to resources via application context if outside activity
        
        // with qualifiers, you don't need to use if or when statements to check for device configuration
        // android will automatically select based on qualifiers and users device settings
        // just ensure you name the resources the same across different qualifier directories
        val myDrawable = resources.getDrawable(R.drawable.my_image, null) // get drawable resource
    }
}

```

# Intents and Intent Filters
- **Intent**: messaging object used to request an action from another ANDROID COMPONENT (e.g., start an activity, start a service, deliver a broadcast)
  - **explicit intent**: specifies the target component by name (e.g., starting an activity within the same app)
  - **implicit intent**: does not specify the target component, instead declares a general action to perform (e.g., opening a website, sharing content)
- **Intent Filters**: declarations in the app's manifest file that specify the types of intents the app can handle
  - allows the app to respond to implicit intents from other apps
  - defined using `<intent-filter>` tags in the AndroidManifest.xml file

- **Manifest file** = summarizes the functionality of the app to the android system
  - declares components (activities, services, broadcast receivers, content providers)
  - permissions required by the app
  - hardware and software features used by the app
  - app metadata (e.g., app name, icon, theme)

- **Permissions**: required for certain actions that may affect user privacy or device security (e.g., accessing camera, location, contacts)
  - must be declared in the manifest file
  - some permissions require runtime approval from the user (e.g., location access)
  - Google Play Store enforces permission policies to protect user data
  - an app shouldn't request more permissions than necessary for its core functionality

```kotlin
class MainActivity : ComponentActivity() {
    
    private val viewModel by viewModels<ImageViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
      // EXPLICIT INTENT EXAMPLE
        // first param is context (can be this activity or application context)
        // second param is the target activity class
        val explicitIntent = Intent(this, SecondActivity::class.java)
        startActivity(explicitIntent)
        // Intent(this, SecondActivity::class.java).also { startActivity(it) } // alternative way
        
      // IMPICIT INTENT EXAMPLE
        // param is the action to be performed
        // now any app that can handle this action can respond, user can select which app to use
        val implicitIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("https://www.example.com")
        }
        // Check if there is an app that can handle this intent
        if (implicitIntent.resolveActivity(packageManager) != null) {
            startActivity(implicitIntent)
        }
      
      // EXPLICIT
        // what if we want to open youtube app specifically?
        // in terminal use: `adb shell` -> `pm list packages | grep youtube` to find package name
        Intent(Intent.ACTION_MAIN).also {
            // check if youtube app is installed
//            it.component = ComponentName("com.google.android.youtube", "com.google.android.youtube.HomeActivity")
            it.`package` = "com.google.android.youtube"
//            if (it.resolveActivity(packageManager) != null) {
//                startActivity(it)
//            }
          // or
            try {
                startActivity(it)
            } catch (e: ActivityNotFoundException) {
                // youtube app not installed, handle gracefully
            }
        }
      
      // IMPLICIT
      // just want an app that can: open a pdf, email, browser, etc.
      // SEND is a common action for sharing content or emailing
      // to be clear, this is just opening an email app, not sending an email directly
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
          // what about email name, subject, body, attachments, etc.?
          // use an array, because you can send to multiple recipients
            putExtra(Intent.EXTRA_EMAIL, arrayOf("test@test.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Check this out!")
            putExtra(Intent.EXTRA_TEXT, "Here's some interesting content.")
        }
      
        // Check if there is an app that can handle this intent
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }
  
// INCOMING INTENT HANDLING EXAMPLE
  // handling incoming intents if the activity is already running (singleTop mode)
  override fun onNewIntent(intent: Intent?) {
      super.onNewIntent(intent)
      // this is called when the activity is launched in singleTop mode and it is already running
      intent?.let {
          if (it.action == Intent.ACTION_SEND) {
              val imageUri: Uri? = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    it.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION") // this will always say deprecated on newer versions, but we need it for older versions
                    it.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
              }
              // handle the received image URI
              viewModel.setImageUri(imageUri)
          }
      }
  }
}

// ViewModel example for handling incoming intents
// we use a view model here to retain the image URI across configuration changes
class ImageViewModel: ViewModel() {
    var imageUri: Uri? by mutableStateOf(null)
        private set
    fun setImageUri(uri: Uri) {
        this.imageUri = uri
    }
}
```

- Permission example in AndroidManifest.xml:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.allnotes">
    <uses-permission android:name="android.permission.CAMERA" />
    <application 
      android:label="@string/app_name"
      android:icon="@mipmap/ic_launcher">
      <activity 
        android:name=".MainActivity"
        android:exported="true"
        android:launchMode="singleTop">
<!-- singleTop here is for the step of sharing image data,
 normally the action would open a new instance of our app
   this way with singleTop, if our app is already open, we will use that instance -->
        <intent-filter>
<!-- this intent filter is used to tell android that this is how we want our app opened when selected -->
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
        <intent-filter>
<!-- this intent filter is used to tell android that our app can handle SEND actions with image data -->
<!-- like sharing an image/meme on our app (social media) -->
            <action android:name="android.intent.action.SEND" />
            <category android:name="android.intent.category.DEFAULT" />
<!-- can specify types of images like `image/jpeg` -->
            <data android:mimeType="image/*" />
        </intent-filter>
        </activity>
    </application>
<!-- queries is where we say what actions our app can perform -->
    <queries>
        <intent>
<!-- SEND is what we used above to send an email, we tell google here that our app uses this action -->
            <action android:name="android.intent.action.SEND" />
<!-- data is what kind of data we will be using -->
            <data android:mimeType="text/plain" />
        </intent>
    </queries>

</manifest>
```

- What if you want your app to appear as an option for certain actions/intents from other apps?
  - e.g., user clicks on a link in a browser, and you want your app to appear as an option to open that link
  - you need to define an intent filter in your manifest file for the activity that can handle that action


# Broadcasts and Broadcast Receivers
- **Broadcasts**: system-wide messages that can be sent by the system or other apps to notify about events (e.g., battery low, network connectivity change, incoming call)
- **Broadcast Receivers**: components that listen for broadcasts and respond to them (e.g., show a notification, start an activity)
  - registered in the manifest file or dynamically at runtime
- useful for decoupling components and enabling communication between different parts of the app or different apps

- Example: music app that pauses playback when a phone call is received
  - broadcast receiver listens for the `PHONE_STATE` broadcast
  - when a call is received, the receiver pauses the music playback

- Static vs Dynamic Broadcast Receivers:
  - **Static**: registered in the manifest file, always listening for broadcasts even when the app is not running
    - useful for system-wide events that need to be handled even when the app is not active (e.g., boot completed, airplane mode changed)
    - NOTE: starting from Android 8.0 (API level 26), MOST implicit broadcasts can no longer be registered statically in the manifest to improve performance and battery life. 
      - However, some broadcasts like `BOOT_COMPLETED` and `AIRPLANE_MODE_CHANGED` are still allowed.
    - NOTE: you do not register or unregister static receivers in code, the system handles that based on the manifest file
  - **Dynamic**: registered at runtime in the activity or service, only listening for broadcasts while the app is running
    - useful for events that are only relevant while the app is active (e.g., network connectivity change while the app is in use)

```kotlin
class MainActivity: ComponentActivity() {
    
// INITIALIZE DYNAMIC RECEIVERS # 1
    // we want these declared outside of onCreate so we can unregister in onDestroy
  private val airplaneModeReceiver = AirPlaneModeReceiver()
  private val testReceiver = TestReceiver()
  
    
  override fun onCreate(savedInstanceState: Bundle?) {
      super.onCreate(savedInstanceState)
    
// REGISTER DYNAMIC RECEIVERS # 2
      // Registering the BroadcastReceiver dynamically (only while the app is running and the user changes airplane mode)
      // we need to use Static receivers to listen for broadcasts when the app is not running
      // remember to unregister in onDestroy to avoid memory leaks 
      registerReceiver(airplaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
      registerReceiver(testReceiver, IntentFilter("TEST_ACTION"))
    
// SEND A BROADCAST
    // imagine we have two apps:
    // first app sends a broadcast when button is clicked
      Button (
          onClick = {
              // this is a custom broadcast
              sendBroadcast(Intent("TEST_ACTION"))
          }) {
                  Text("Send Broadcast")
              }
  }
  
// UNREGISTER DYNAMIC RECEIVERS # 3
  override fun onDestroy() {
      super.onDestroy()
      // Unregistering the BroadcastReceiver
      unregisterReceiver(airplaneModeReceiver)
      unregisterReceiver(testReceiver)
  }
}

// CUSTOM BROADCAST RECEIVER
// ui/receivers/TestReceiver.kt
class TestReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == "TEST_ACTION") {
          // Handle the received broadcast with a domain layer
          TestActionHandler.handle(context?.applicationContext)
      }
  }
}

// CUSTOM BROADCAST HANDLER
// domain/receivers/TestActionHandler.kt
object TestActionHandler {
  fun handle(context: Context?) {
    // domain logic here (use cases, repositories, WorkManager, etc.)
    // run off the main thread if doing IO
    CoroutineScope(Dispatchers.IO).launch {
      // call use case / repository
      // Example: MyUseCase.execute(params)
    }
  }
}


// AIR PLANE MODE BROADCAST RECEIVER
class AirPlaneModeReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context?, intent: Intent?) {
      if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
          val isAirplaneModeOn = intent.getBooleanExtra("state", false)
        // other way:
        /*
            val isAirplaneModeOn = Settings.Global.getInt(
                context?.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON
            ) != 0
         */
          if (isAirplaneModeOn) {
              // Airplane mode is ON, handle accordingly
          } else {
              // Airplane mode is OFF, handle accordingly
          }
      }
  }
}
```

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.allnotes">
    <application>
        <activity android:name=".MainActivity" android:exported="true">
        </activity>
      
        <!-- STATIC BROADCAST RECEIVER REGISTRATION -->
        <receiver android:name=".AirPlaneModeReceiver">
            <intent-filter>
                <action android:name="android.intent.action.AIRPLANE_MODE_CHANGED" />
            </intent-filter>
        </receiver>
    </application>
</manifest>
```

# Foreground Services
- **Services**: components that run in the background to perform long-running operations without a user interface 
  - user doesn't know when a service is running
  - e.g., downloading files, tracking location, syncing data, playing music
  - work manager handles most background tasks now instead of services, now services are used mainly for foreground tasks
- **Foreground Services**: services that perform tasks that are noticeable to the user and require ongoing attention 
  - e.g., music playback, fitness tracking
  - must display a persistent notification to inform the user that the service is running
  - have higher priority than background services, less likely to be killed by the system
  - 

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
          Actions.Start.toString() -> start()
          Actions.Stop.toString() -> stopSelf()
      }
      return super.onStartCommand(intent, flags, startId)
  }
  
  // need to ensure the app is visible for the user
  private fun start() {
      startForeground()
  }
  
  enum class Actions {
      Start, Stop
  }
}
```