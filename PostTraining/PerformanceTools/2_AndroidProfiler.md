# Resources:
- [Android Studio Profiler - Youtube](https://www.youtube.com/watch?v=CQc-QDTmCoQ&t=5s&pp=ygUbcGhpbGlwcCBsYWNrbmVyIHBlcmZvcm1hbmNl)
  - shows visual examples for CPU, Memory, Network, and Energy profilers
---
- [Medium - Mastering Android Profiling](https://medium.com/@pinankhpatel/mastering-android-profiling-a-complete-guide-to-battery-memory-ui-and-overall-app-performance-0f8bc4175aab)
  - This was the foundation of my notes - my notes expand on this article with more examples and details
- [Medium - Why Performance Matters](https://medium.com/@anandgaur2207/android-performance-analysis-optimizations-cfb1f8b46d95)
    - Requires upgrading Medium to paid account – skim the free portion for framing, my notes here are the main reference.
---
- [Developers - Docs](https://developer.android.com/studio/profile)
- [Developers - App Performance Guide](https://developer.android.com/topic/performance/overview)
- [Developers - Use Profiler](https://developer.android.com/studio/profile#build-and-run)
- [Developers - Memory Performance Management](https://developer.android.com/topic/performance/memory)
---
other tools:
Flipper: Database, network, layout inspector. https://fbflipper.com/


# Performance
## Why Performance Matters
- User Experience: Fast and responsive apps lead to higher user satisfaction and retention.
  - slow apps get 1 star reviews
  - 53% of users abandon apps that take longer than 3 seconds to load
  - Google uses performance as a ranking factor in search results
- Android tracks battery usage per app. Apps consuming too much battery get:
  - Restricted 
  - Flagged in Battery Optimization 
  - Killed frequently 
  - Uninstalled by users
- If your app uses too much memory:
  - The system triggers GC frequently 
  - User sees UI stutters 
  - App faces ANRs 
  - App crashes with OutOfMemoryError
- Most 1-star reviews mention:
  - Crashes
  - Slowness
  - Lag
  - Battery drain

> Performance = startup, responsiveness, smooth UI, memory stability, efficient network usage, and reasonable battery/energy usage. The tools below help you measure and improve each of those.


# Performance Checklist before Release
### CPU
[] No long operations on main thread
[] Efficient algorithms

### Memory
[] No memory leaks
[] Large objects released
[] Caches size-limited

### Battery
[] No unnecessary GPS access
[] No constant background tasks
[] No redundant network calls

### UI
[] 60 FPS or more
[] Smooth transitions
[] Optimized layout hierarchy


# Android Profiler Overview
Android Studio’s Profiler gives real-time data on:
1. CPU usage 
2. Memory usage 
3. Energy (Battery)
4. Network activity 
5. UI rendering frames

## Open Android Profiler
In Android Studio:
1. Run your app on a device or emulator
2. Go to View > Tool Windows > Profiler
3. Select your device and app process
4. You’ll see 4 main tabs: CPU, Memory, Energy, Network

---

## CPU Profiler
Helps you detect:
- Functions taking too long (e.g., heavy loops, JSON parsing, large DB queries)
- Blocking tasks on main thread 
- Coroutine or RxJava mis-configuration 
- Infinite loops 
- Thread mismanagement

Modes:
- Sampled: Low overhead, less detail, general performance overview
- Instrumented: High detail, most accurate, shows every method call
- Trace System Calls: use for Thread scheduling, doze, frame drops
    - Doze is when Android puts the device into a low-power state to save battery when not in use

---

Use CPU Profiler:
1. Open Profiler 
2. Click the CPU timeline 
3. Press Record 
4. Interact with your app (scroll, load data, navigate)
5. Stop recording

Shows:
- Thread list 
- Call stacks 
- Time spent in each method

---

Example:
- Suppose loadUsers() is taking too long
```kotlin
fun loadUsers() {
    val users = api.getUsers() // network call on main thread! ❌
    recyclerView.adapter = UserAdapter(users)
}

// Fix by moving network call to background thread
suspend fun loadUsers() = withContext(Dispatchers.IO) {
    api.getUsers()
}
// or better yet, use viewModelScope
fun loadUsers() {
    viewModelScope.launch {
        val users = withContext(Dispatchers.IO) {
            api.getUsers()
        }
        recyclerView.adapter = UserAdapter(users)
    }
}
```

What you'll see in CPU Profiler before and after:
Before fix:
- Main thread blocked
- Network call on main thread
- Frame rendering janky

After fix:
- Main thread no longer blocked
- Network calls moved to worker threads
- Frame rendering improves


---


## Memory Profiler
Memory Profiler shows:
- Heap size 
- Java/Kotlin object allocations 
- Native memory usage 
- Garbage collection events

Steps:
1. Open Profiler → Memory 
2. Press Record Memory 
3. Perform navigation (where leak might be)
4. Trigger GC manually - click "Garbage Collect" button in Memory Profiler
5. If memory does NOT drop → possible leak

---

Common Memory Leaks:
- Activity Leak: Due to static references to Activity/Context
- Fragment Leak: ViewBinding references not cleared in Fragments
- Coroutine Leak: Coroutines running longer than lifecycle (GlobalScope or CoroutineScope that was not cancelled)
- Context Leak: Long-lived objects/singletons holding onto Context
- Adapter Leak: Using outer variables incorrectly 
- Listener Leak: Not unregistering listeners/callbacks (e.g., BroadcastReceiver, EventBus)

Examples:

1. Activity Leak
```kotlin
// Legacy-style problem (still possible in older codebases)
object SessionManager {
    var currentActivity: Activity? = null
}
// Fix: Avoid keeping a strong reference to an Activity; if you must track it, use a WeakReference
object SessionManager {
    var currentActivity: WeakReference<Activity>? = null
}
```

2. Fragment Leak
```kotlin
private var binding: FragmentHomeBinding? = null
// Fix: Clear binding in onDestroyView
private var _binding: FragmentHomeBinding? = null
private val binding = _binding!!

override fun onDestroyView() {
    _binding = null
}
```

3. Coroutine Leak
```kotlin
GlobalScope.launch {
    // long running task
}
// Fix: Use viewModelScope or lifecycleScope
viewModelScope.launch {
    // long running task
}
// or cancel in onDestroy
override fun onDestroy() {
    job.cancel()
}
```

4. Context Leak
```kotlin
// Bad: singleton holding arbitrary Context reference (could be an Activity)
class MySingleton private constructor(context: Context) {
    private val contextRef = context
    companion object {
        // ...
    }
}

// Fix: always store applicationContext, never an Activity
class MySingleton private constructor(context: Context) {
    private val appContext = context.applicationContext
    companion object {
        // ...
    }
}
```

5. Adapter Leak
```kotlin
class MyAdapter(private val activity: Activity) : RecyclerView.Adapter<MyViewHolder>() {
    // ...
}
// Fix: Use Context instead of Activity to reduce risk of accidentally leaking a UI component
class MyAdapter(private val context: Context) : RecyclerView.Adapter<MyViewHolder>() {
    // ...
}
```

6. Listener Leak
```kotlin
override fun onStart() {
    super.onStart()
    LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver, intentFilter)
}  
// Fix: Unregister in onStop
override fun onStop() {
    super.onStop()
    LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
}
```


---


## Energy Profiler
helps detect:
- Wake locks 
- Excessive location checks 
- Unnecessary background tasks 
- Expensive CPU operations 
- High network activity 
- Sensor misuse

---

Common Battery Drains:
- Location: running requestLocationUpdates() without interval or stopping updates
- Network: Polling/Calling APIs too frequently
- CPU: Big loops or heavy computations 
- Alarms: Using setExact() too often
- Services: Long-running foreground services without user need
- Wake Locks: Holding wake locks longer than necessary
  - Wake locks keep the CPU on even when the screen is off, draining battery
- Sensors: Keeping sensors (accelerometer, gyroscope) active when not needed
- Animations: Excessive or complex animations running continuously
- Background Work: Frequent or unnecessary background tasks (WorkManager, JobScheduler)


Examples:

1. Location Updates
```kotlin
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    0,
    0f,
    listener
)
// Fix: Set interval and distance
locationManager.requestLocationUpdates(
    LocationManager.GPS_PROVIDER,
    10_000, // 10 seconds - how often to check
    50f,    // 50 meters - minimum distance change  
    listener
)
```

2. Unnecessary background work
```kotlin
// Bad: periodic work too frequent
WorkManager.getInstance(context).enqueue(
    PeriodicWorkRequestBuilder<SyncWork>(15, TimeUnit.MINUTES).build()
)

// Better: use constraints and sensible intervals
val request = PeriodicWorkRequestBuilder<SyncWork>(
    6, TimeUnit.HOURS
).setConstraints(
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .build()
).build()
WorkManager.getInstance(context).enqueue(request)
```

---


## Network Profiler
shows:
- Total bytes sent & received 
- Upload/download timelines 
- REST API call durations (and other HTTP/HTTPS calls – GraphQL/Protobuf/etc. all appear as network requests)
- Slow endpoints 
- Over-fetching data

steps:
1. Go to View > Tool Windows > App Inspection
   - this is also where database and background task inspector are located
2. Interact with app (load data, images, etc.)
3. Analyze requests:
   - Look for long durations
   - Large payloads
   - Frequent calls

analyze:
- can click on a network request to see details like headers, payload, response (JSON body), response type, authorization, timing breakdown, status code, etc.

---

Common Network Issues:
- Large Payloads: Downloading/uploading large JSON/XML/images/videos
- Unoptimized Images: Not using proper formats (e.g., WebP, AVIF)
- No Caching: Not leveraging HTTP caching headers, or local caching with libraries like Coil, Room, SQLDelight, etc.
- Excessive Calls: Making too many API calls in a short time (e.g., infinite scroll without pagination)
- Synchronous Calls: Blocking main thread with network calls
- Redundant Calls: Fetching the same data multiple times unnecessarily
- Multiple Calls for Same Screen: Making separate API calls for data that could be combined into a single call


Examples:
1. Multiple API Calls / Redundant Calls
```kotlin
// Bad: separate endpoints for data that always appear together
suspend fun loadHome() {
    val user = api.getUser()
    val posts = api.getPosts()
    val comments = api.getComments()
}

// Better: combine into a single endpoint or backend aggregator
@GET("/home")
suspend fun getHomeData(): HomeResponse

suspend fun loadHome() {
    val home = api.getHomeData()
    // home.user, home.posts, home.comments
}
```

2. Synchronous Call on Main Thread
```kotlin
fun fetchData() {
    // Bad: synchronous network call on main thread
    val response = api.getDataBlocking() // blocks UI
}

// Fix: use coroutines or enqueue async calls
fun fetchData() {
    viewModelScope.launch {
        val data = withContext(Dispatchers.IO) { api.getData() }
        // update UI with data
    }
}
```

3. Excessive Calls (no batching)
```kotlin
// Bad: calling API for each item
items.forEach { item ->
    api.updateItem(item.id)
}

// Better: batch update
@POST("/items/updateBatch")
suspend fun updateItems(@Body ids: List<String>)

suspend fun updateAll(items: List<Item>) {
    val ids = items.map { it.id }
    api.updateItems(ids)
}
```

---


## UI Rendering & Jank Detection
Android devices must draw each frame within 16ms for 60 FPS.

Profiler (System Trace) shows:
- Frame drops 
- Layout passes 
- GPU usage 
- Slow compositing

steps:
1. Open Profiler → CPU
2. Click "System Trace"
3. Look for "Choreographer" or frame timeline events taking >16ms

---

Common UI Performance Issues:
- Heavy work on Main Thread: JSON parsing, DB queries, complex calculations, loop iterations
- Deep Nested Layouts: Multiple nested LinearLayouts, RelativeLayouts
- Inefficient RecyclerView: No view recycling, complex item layouts, using notifyDataSetChanged() instead of DiffUtil
- Complex Animations: Without hardware acceleration or too many simultaneous animations
- Compose-specific: over-recomposition due to unstable parameters, state stored too low in the tree, or passing large objects instead of primitive/immutable state.
  - Note: Android Profiler’s system trace still works for Compose – frames/jank show up the same way – but for composition-specific issues, prefer **Layout Inspector** and **Compose recomposition tools**.


---


## Simulating Performance Conditions
Android Studio lets you simulate:
- Slow network 
- High CPU load 
- Low memory 
- Background restrictions 
- App being killed

Use these to reproduce real‑world conditions and then watch the CPU/Memory/Network/Energy graphs while the app runs.

---


## Using adb Commands for Profiling

`adb` (Android Debug Bridge) is a command-line tool that lets you talk to devices/emulators. You can use `adb shell` commands to dump profiling and performance data outside of Android Studio.

Examples:
1. Check memory usage: 
```bash
adb shell dumpsys meminfo your.package.name
```
2. Check battery consumption: 
```bash
adb shell dumpsys batterystats your.package.name
```
3. Check UI rendering stats (frame data): 
```bash
adb shell dumpsys gfxinfo your.package.name
```

These are useful when:
- You’re debugging on a CI device or remote device without full Studio access.
- You want to capture a snapshot you can paste into tickets/docs.

---

## Common Pitfalls with Android Profiler

- **Profiling only in Debug builds**
  - Debug builds have different performance characteristics (no R8, extra logging). Always validate with a **release‑like build** when you care about numbers.

- **Chasing micro-optimizations without a symptom**
  - Don’t start profiling randomly; start from a **clear problem** (slow screen, jank, OOM) and use the Profiler to validate/falsify hypotheses.

- **Misreading short profiling sessions**
  - Very short runs can show misleading spikes. Run actions **several times** to see stable patterns.

- **Forgetting about lifecycle**
  - Measuring only first run in a session can hide leaks that appear after many navigations. Use the Memory Profiler to watch **multiple open/close cycles**.

- **Leaving heavy instrumentation on all the time**
  - Instrumented CPU profiling adds overhead. Use **Sampled** mode first; save **Instrumented** for targeted deep dives.

- **Assuming all slowness is CPU**
  - Some issues are **I/O bound (disk/network)** or due to **GC/allocations**. Correlate CPU, Memory, Network, and Energy timelines.

- **Ignoring production data**
  - Lab profiling is necessary but not sufficient. Use Firebase Performance / Crashlytics to see how issues show up **in the wild**.

---

## Interview-Oriented Questions & Answers

### 1. How do you use Android Studio Profiler to diagnose a performance issue?

**High-Level**
- Start from a symptom (slow screen, jank, OOM), pick the relevant Profiler tab, reproduce the issue, then correlate spikes with code paths.

**Talking Points**
- CPU tab for **janky UI / long operations**.
- Memory tab for **leaks and OOMs** (heap growth, GC events, heap dumps).
- Network tab for **slow or excessive API calls**.
- Energy tab for **battery drain**.

**Succinct Answer**
> I start with the symptom—slow startup, janky scroll, or high memory—and open the corresponding Profiler tab. 
> I reproduce the behavior while recording, then look for spikes in CPU, memory, or network, and drill down into threads and call stacks to find the hot paths. 
> Once I have a hypothesis, I fix the code and rerun the same scenario to confirm the graphs and timings actually improved.

---

### 2. How do you detect and fix memory leaks using Android Profiler?

**High-Level**
- Watch heap usage while navigating; take heap dumps; confirm memory drops after GC.

**Talking Points**
- Use Memory Profiler to:
  - Record while opening/closing screens.
  - Trigger GC and see if heap shrinks.
  - Inspect retained objects to see which Activities/Fragments or ViewModels are held.
- Combine with LeakCanary for automatic leak detection in debug builds.

**Succinct Answer**
> I use the Memory Profiler to record while I navigate through the app, trigger a manual GC, and see whether memory returns to a baseline. 
> If it doesn’t, I take a heap dump and inspect which Activities or Fragments are still retained. 
> That usually points to things like view bindings not cleared, long‑lived singletons holding a Context, or coroutines outliving their scope. 
> I fix the root cause, then rerun the same navigation to confirm that memory drops after GC and LeakCanary reports are clean.

---

### 3. How do you use the Network Profiler to improve performance?

**High-Level**
- Identify **slow, large, or redundant** network calls and optimize payloads, caching, and batching.

**Talking Points**
- Look at duration, size, and frequency per endpoint.
- Spot:
  - Large JSON or image payloads.
  - Repeated calls for the same data.
  - Multiple calls that could be combined.
- Fixes:
  - Add pagination or batching.
  - Introduce caching (HTTP or local).
  - Reduce payload size and fields.

**Succinct Answer**
> In the Network Profiler I look for endpoints that are either slow, very large, or called too often. 
> I inspect the payloads and timings, then reduce data size, add caching, or batch requests where possible. 
> After that I rerun the same flows to verify that the number of calls and the total transferred bytes went down and the screen loads faster.

---

### 4. How do you find and fix jank using Profiler and System Trace?

**High-Level**
- Use System Trace to see frame timelines and identify work on the main thread during frames that exceed 16ms.

**Talking Points**
- Capture System Trace while reproducing the jank.
- Look at **Choreographer / frame length** tracks.
- Find:
  - Long CPU slices on main thread.
  - Disk or network operations on main thread.
  - GC happening during frames.
- Move heavy work off the main thread or break it into smaller chunks.

**Succinct Answer**
> I record a System Trace while reproducing the jank and then look at the frame timeline to see which frames missed the 16ms deadline. 
> From there I drill into the main thread’s call stack to see if it’s doing heavy work like parsing, DB queries, or layout during those frames. 
> The fix is usually to move that work off the main thread or restructure the UI to do less per frame, then re‑record the trace to confirm that frames are now within budget.

---

### 5. How do you combine Profiler data with production monitoring tools?

**High-Level**
- Use Profiler for **deep, local investigation**; use APM (Firebase Performance, Crashlytics) for **real‑world telemetry**.

**Talking Points**
- APM gives:
  - Startup times, slow/frozen frames, network latency per region/device.
  - Crash and ANR hotspots.
- Profiler gives:
  - Detailed local view of threads, allocations, and call stacks.
- Workflow:
  - Discover issues in production metrics.
  - Reproduce locally with Profiler/System Trace.
  - Fix, then watch production metrics to confirm improvement.

**Succinct Answer**
> I treat the Android Profiler as my microscope and Firebase Performance or Crashlytics as my observatory. 
> Production tools tell me which screens or flows are slow or crashing for real users; 
> then I reproduce those locally with the Profiler and System Trace to understand exactly what the code is doing. 
> After I optimize, I monitor those same production metrics to make sure startup times, frame rates, or crash rates actually improved.
