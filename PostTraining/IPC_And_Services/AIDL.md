# AIDL (Android Interface Definition Language) – Deep Dive

## Resources:
[Medium Car Example](https://proandroiddev.com/demystifying-androids-surface-your-secret-weapon-for-high-performance-graphics-7219f9caf0f8)
- has a car app example using AIDL for navigation (mostly narrative, some code)
[Medium Beginner's Guide to AIDL](https://medium.com/@sedakundakitchen/a-beginners-guide-to-aidl-d7b53499baf3)
- has simple code example of AIDL usage
[Medium AIDL for Senior Devs](https://medium.com/android-alchemy/aidl-in-android-must-read-for-senior-android-developers-1d54bc68f72d)
- has a code example with a more detailed walkthrough
[Medium AIDL vs HIDL for HAL](https://medium.com/make-android/aidl-vs-hidl-hal-in-android-a-detailed-comparison-9706166eab92)
- AIDL is preferred for Hardware Abstraction Layers (HALs) since Android 10 (2020)
[Medium AIDL vs HIDL evolution of HAL and JNI](https://medium.com/@anuragsingh7238/hidl-vs-aidl-in-android-understanding-the-evolution-of-hal-interfaces-6a70d75e6fd9)
- more on AOSP and JNI relationship
[Medium COMPREHENSIVE GUIDE to AIDL](https://medium.com/@peternjuguna76/understanding-android-aidl-a-comprehensive-guide-b4d97253b169)
- answers the what, why, when, and how of AIDL with java code examples


[Medium Messenger vs AIDL](https://proandroiddev.com/ipc-techniques-for-android-messenger-3e8555a32167)
- only thing to find on Messenger

## Why AIDL Exists

On Android, apps usually run in separate processes. If you want one app (or a system service) to expose a **rich API** to another app, you need more than Intents:

- Intents → fire‑and‑forget, simple one‑way messages.
- AIDL → **typed, method‑based API across process boundaries**.

Without AIDL, Android developers would need to manually implement and maintain IPC, which would lead to longer development cycles, a higher risk of bugs, and reduced maintainability. 
The loss of automatic code generation would generally mean more boilerplate code, all without the guarantee of consistent communication between different components.

Android uses Hardware Abstraction Layers (HALs) to provide a bridge between the hardware and the Android framework. 
Traditionally, HIDL (Hardware Interface Definition Language) was used for this purpose, but starting with Android 10 (2020), AIDL (Android Interface Definition Language) has become the preferred approach.
- Uses Binder IPC only (no passthrough mode). It can be implemented in C++ or Java (Kotlin now). Supports stable versions to ensure compatibility.
- Recommended for all new HAL implementations. It is easier to update without requiring a vendor partition upgrade.
    - HIDL was only in C++ and was hard to update without a full system update.
    - In Android 11+, HIDL is deprecated and all new HALs must use AIDL.
    - Framework services can directly talk to native HALs over Binder without needing to write JNI manually.


AIDL lets you define an interface like:

```aidl
// IRemoteService.aidl
interface IRemoteService {
    int add(int a, int b);
    void sendMessage(String msg);
}
```

Android generates:

- A **Stub** class (server side) you extend in your Service.
- A **Proxy** class (client side) that implements the same interface and forwards calls over Binder.

Clients call `remote.add(1, 2)` like a normal method; under the hood the call is marshalled over Binder to the service process.

---

## Key Concepts in AIDL

### Typed Interfaces

- **Typed interface** = an interface where each method has a specific signature:
  - Parameter types, return types, `in`/`out`/`inout` direction, and nullability are part of the contract.
- In AIDL, you define allowed types:
  - Primitives, `String`, `CharSequence`, `List`, `Map`, `IBinder`, `Parcelable`, AIDL interfaces, etc.
- This is different from Intents where you just put arbitrary values into a `Bundle`.

> Think of an AIDL file as a **strongly‑typed contract** between processes, similar to a Retrofit interface but for **local IPC** instead of HTTP.

### Bound Services & Process Boundaries

- A **bound service** is a Service that clients can **bind** to and call methods on while they are connected.
- With AIDL, your bound service:
  - Extends `Service`.
  - Exposes an `IBinder` in `onBind()` – typically an implementation of the generated `Stub`.
- "Used across process boundaries" means:
  - Client and service can be in **different apps/processes**.
  - The Binder layer marshals arguments and return values between processes.

### Stubs and Proxies

When you write:

```aidl
interface IRemoteService {
    int add(int a, int b);
}
```

The AIDL compiler generates something like:

- `IRemoteService.Stub` (abstract server‑side stub):
  - Extends `Binder` and implements `IRemoteService`.
  - Has an `onTransact` method that unpacks incoming `Parcel`s and calls your implementation.
- `IRemoteService.Stub.Proxy` (client‑side proxy):
  - Implements `IRemoteService`.
  - Packages method calls into `Parcel`s and sends them over Binder to the remote Stub.

You don’t usually edit Stub/Proxy; you:

- **Server:** extend `Stub` and implement methods.
- **Client:** call `IRemoteService.Stub.asInterface(binder)` to get a Proxy instance.

> In OO terms, Stub is like a base class the server extends; Proxy is a client‑side implementation that forwards calls over Binder.

---

## Who Implements What?

- **Service app (server):**
  - Owns the `.aidl` file.
  - Includes it in its source.
  - Extends the generated `Stub` and implements the methods.
  - Exposes the binder via a bound Service (`onBind`).

- **Client app:**
  - Also includes the **same** `.aidl` file (copied or provided via a library/module).
  - Calls `bindService(...)` to connect to the service.
  - In `onServiceConnected`, calls `IRemoteService.Stub.asInterface(serviceBinder)` to get the Proxy.
  - Calls methods on this Proxy as if it were a local object.

> Both sides share the same `.aidl` definition so the contract is identical. The server owns the implementation; the client just calls it.

---

## AIDL vs Intents vs Retrofit (Analogy)

- **Intents**: loosely typed, fire‑and‑forget messages inside the device.
  - Great for starting Activities/Services, sending broadcasts.
- **AIDL**: strongly typed, method‑based API across processes **on the same device**.
  - Similar to calling methods on a local interface, but marshalling happens via Binder.
- **Retrofit**: strongly typed, method‑based API across **the network**.
  - Similar conceptually to AIDL but for HTTP/Web APIs.

> AIDL is to Binder IPC what Retrofit is to HTTP APIs: both define a typed contract, generate client code, and hide serialization details.

---

## When to Use AIDL

Use AIDL when:

- You have multiple apps/processes that need to **share functionality** (SDKs, carrier services, OEM services).
- You need **bidirectional communication**:
  - Client calls service methods.
  - Service can call back into the client via AIDL callback interfaces.
- You need a **stable, versioned API** for third‑party clients.

Avoid AIDL when:

- Everything is in **one app/process** – a normal interface or repository is simpler.
- You just need **simple fire‑and‑forget** operations – Intents or `PendingIntent` may be enough.

---

## Messenger vs AIDL – What Is “Simpler”?

**Messenger** wraps Binder with a simple message‑based API:

- You send `Message` objects containing `what`, `arg1`, `arg2`, and a `Bundle`.
- All messages go through a single `Handler` in the service.

Use Messenger when:

- You have a **small set of commands** or requests.
- You don’t need a rich, strongly typed interface.
- You’re okay encoding operations as `what` codes + Bundles.

Use full AIDL when:

- You want a **clear, typed set of methods**.
- You have multiple operations, data structures, or callbacks.

Example mental model:

- Messenger: “I send you messages like `MSG_START_DOWNLOAD` with some extras.”
  - TODO: is Messenger used for car apps for things like tire pressure monitoring, or other system services?
- AIDL: “I call `startDownload(url: String, priority: Int)` and get a callback when it’s done.”
  - 

---

## Binder / IBinder – Low Level

- **`IBinder`** is the core interface for a remote object in Android’s Binder IPC.
- **`Binder`** is the base class you extend on the server side.

You usually see `IBinder` in:

- `Service.onBind(intent: Intent): IBinder`.
- `ServiceConnection.onServiceConnected(name: ComponentName, service: IBinder)`.

Examples of where you’d touch Binder directly:

- Writing your own IPC framework or library.
- System/U OEM work:
  - Telephony, media, location, input methods, etc.
  - OEM = **Original Equipment Manufacturer** – device makers like Samsung, Pixel team, etc., who customize the system.

As an app dev, you mostly:

- Return a `Stub` (a `Binder`) from your service.
- Receive an `IBinder` in clients and convert it to a typed interface with `asInterface`.

---

## AIDL Coding Example

Simple example: one app exposes a math service with `add(a, b)`; another app binds and calls it.

if the client and service are in separate applications or modules, a copy of the .aidl file needs to be included in the client's src/main/aidl folder too.

TODO: can we go into more on the Server app vs Client app distinction here? 
TODO: Server app would be what in the context of something like Verizon listening to SMS messages? what are other examples of common client/server app pairs that use AIDL?


**Steps (Server app – exposes the service)**
1. **Create the AIDL file**
   - In `app/src/main/aidl/your/package/name/IRemoteService.aidl`:
   - **You will need to set aidl = true in your build.gradle’s buildFeatures.**
     - TODO: does this need to be in the root? what layer is this typically added to (data/domain/ui/etc)?
   ```aidl
   interface IRemoteService {
       int add(int a, int b);
   }
   ```
   - Make sure the **package** matches your Java/Kotlin package.

2. **Implement the bound Service**
   - This is on the Server app
   - don’t forget to register your service in the AndroidManifest.xml.
   ```kotlin
   // TODO: does this need to be in the root? what layer is this typically added to (data/domain/ui/etc)?
   // YourRemoteService.kt
   class YourRemoteService : Service() {

       // Implementation of the Stub generated from IRemoteService.aidl
       // you want this to be an object so it's a singleton? is this the right way to do it?
       private val binder = object : IRemoteService.Stub() {
           override fun add(a: Int, b: Int): Int {
               return a + b
           }
       }

       // The implemented interface is exposed to clients using onBind() and clients wanting to use it can bind to it.
       override fun onBind(intent: Intent?): IBinder {
           return binder
       }
   // TODO: do we need to unbind anything here?
   }
   ```

3. **Declare the Service in AndroidManifest.xml**
   ```xml
   <!-- `exported = true` is mandatory. It allows applications other than AIDLServer to connect to this service.  -->
   <service
       android:name=".YourRemoteService"
       android:exported="true"  
       android:permission="your.package.name.PERMISSION_USE_REMOTE_SERVICE">
       <!-- Optional: custom permission to restrict who can bind -->
       <!-- intent-filter -> We define a unique action name. The client will use this exact action to find and bind to our service. -->
       <intent-filter>
           <action android:name="your.package.name.REMOTE_SERVICE" />
       </intent-filter>
   </service>
   ```
   - Optional: add a **custom permission** and require it so only trusted clients can bind.

**Steps (Client app – calls the service)**
4. **Add the same AIDL file to the client project**
   - Copy `IRemoteService.aidl` into the **same package path** in the client app (`app/src/main/aidl/your/package/name`).

5. **Bind to the remote service**
   ```kotlin
   class MainActivity : AppCompatActivity() {

       private var remoteService: IRemoteService? = null

       private val conn = object : ServiceConnection {
           override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
               remoteService = IRemoteService.Stub.asInterface(service)
           }

           override fun onServiceDisconnected(name: ComponentName?) {
               remoteService = null
           }
       }

       override fun onStart() {
           super.onStart()
           val intent = Intent("your.package.name.REMOTE_SERVICE").apply {
               setPackage("your.package.name") // server app package
           }
           bindService(intent, conn, BIND_AUTO_CREATE)
       }

       override fun onStop() {
           super.onStop()
           unbindService(conn)
       }

       // TODO: We use this method to call the remote (service?) add function inside the client app? 
       private fun callRemoteAdd() {
           val result = remoteService?.add(1, 2)
           // use result
       }
   }
   ```

6. **(Optional) Add callback AIDL for bidirectional communication**
   - Define another AIDL interface for callbacks (e.g., `IRemoteCallback.aidl`).
   - Pass the callback interface from client to server so the server can call client back.

--- 

## Messenger Coding Example

Simple example: use `Messenger` to send commands (`MSG_ADD`) to a service, and get the result back in a reply `Message`.

- TODO: what are examples of server/client apps that would use Messenger instead of AIDL? what would the server app be and what would the client app be in those cases?

**Steps (Server app – Messenger-based service)**
1. **Define message codes and keys**
   ```kotlin
   // Message codes
   // TODO: do these go into a sealed class? I see we use a `when` statement to handle them.
   const val MSG_ADD = 1 
   const val MSG_RESULT = 2

   const val KEY_A = "KEY_A"
   const val KEY_B = "KEY_B"
   const val KEY_RESULT = "KEY_RESULT"
   ```

2. **Implement the Service with a Handler and Messenger**
    - on the Server app
   ```kotlin
   class MessengerService : Service() {

       private inner class IncomingHandler(looper: Looper) : Handler(looper) {
           override fun handleMessage(msg: Message) {
               when (msg.what) {
                   MSG_ADD -> {
                       val a = msg.data.getInt(KEY_A)
                       val b = msg.data.getInt(KEY_B)
                       val sum = a + b

                       // reply to client
                       val reply = Message.obtain(null, MSG_RESULT)
                       reply.data = Bundle().apply {
                           putInt(KEY_RESULT, sum)
                       }
                       msg.replyTo.send(reply)
                   }
                   else -> super.handleMessage(msg)
               }
           }
       }

       private lateinit var messenger: Messenger

       override fun onCreate() {
           super.onCreate()
           // TODO: what is this handler thread for? where is MessengerServiceThread coming from? apply { start() } starts the thread?
           val handlerThread = HandlerThread("MessengerServiceThread").apply { start() }
           // TODO: messenger requires a Handler to process incoming messages? what is looper here?
           messenger = Messenger(IncomingHandler(handlerThread.looper))
       }

       override fun onBind(intent: Intent?): IBinder {
           return messenger.binder
       }
   // TODO: do we need to stop the handler thread somewhere? or unbind messenger?
   }
   ```

3. **Declare the Service in AndroidManifest.xml**
   ```xml
   <service
       android:name=".MessengerService"
       android:exported="true">
       <intent-filter>
           <action android:name="your.package.name.MESSENGER_SERVICE" />
       </intent-filter>
   </service>
   ```

**Steps (Client app – uses Messenger)**
4. **Bind to the Messenger service**
   ```kotlin
   class MessengerClientActivity : AppCompatActivity() {

       private var serviceMessenger: Messenger? = null
       private var isBound = false

       // Handler to receive replies from the service
       private val replyHandler = object : Handler(Looper.getMainLooper()) {
           override fun handleMessage(msg: Message) {
               if (msg.what == MSG_RESULT) {
                   val result = msg.data.getInt(KEY_RESULT)
                   // use result (e.g., update UI)
               } else {
                   super.handleMessage(msg)
               }
           }
       }

       private val replyMessenger = Messenger(replyHandler)

       private val conn = object : ServiceConnection {
           override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
               serviceMessenger = Messenger(binder)
               isBound = true
           }

           override fun onServiceDisconnected(name: ComponentName?) {
               serviceMessenger = null
               isBound = false
           }
       }

       override fun onStart() {
           super.onStart()
           // TODO: intents are used to find the service by action name? do we need a fallback here if the service isn't found?
           val intent = Intent("your.package.name.MESSENGER_SERVICE").apply {
               setPackage("your.package.name") // server app package
           }
           // TODO: bindService connects to the service? what parameters are needed here?
           // TODO: BIND_AUTO_CREATE creates the service if not already running?
           bindService(intent, conn, BIND_AUTO_CREATE)
       }

       override fun onStop() {
           super.onStop()
           if (isBound) unbindService(conn)
       }

       private fun sendAddRequest(a: Int, b: Int) {
           if (!isBound) return

           // TODO: obtain creates a new Message? what parameters are needed here?
           val msg = Message.obtain(null, MSG_ADD)
           msg.data = Bundle().apply {
               putInt(KEY_A, a)
               putInt(KEY_B, b)
           }
           msg.replyTo = replyMessenger

           serviceMessenger?.send(msg)
       }
   }
   ```

5. **(Optional) Use one-way fire-and-forget messages**
   - If you don’t need a response, you can skip `replyTo` and just send `Message`s with `what` codes.

--- 

## Quick Interview Soundbite for AIDL

> “AIDL is how I define a typed, versioned API across processes on Android. 
> I write an AIDL interface, the tooling generates a stub and a proxy, and then my service app implements the stub while other apps call the proxy as if it were local. 
> I use AIDL when I need more than fire‑and‑forget Intents—like exposing a carrier SDK or system service to third‑party apps—while Messenger is enough for simpler message‑based patterns.”
