# WorkManager
- WorkManager schedules deferrable background work with guaranteed execution under constraints, even if the app/process is killed or device restarts (with backoff and retries).
- It’s part of Jetpack and typically the best choice for: syncs, backups, log uploads, periodic jobs, file processing when not user‑visible, etc.


## Constraints
- Supported constraints include:
  - Network type: `NOT_REQUIRED`, `CONNECTED`, `UNMETERED`, `NOT_ROAMING`, `TEMPORARILY_UNMETERED` (API level dependent)
  - Charging state: `setRequiresCharging(true)`
  - Battery not low: `setRequiresBatteryNotLow(true)`
  - Storage not low: `setRequiresStorageNotLow(true)`
  - (Device idle is job‑scheduler level; WorkManager support is limited/implementation‑dependent—don’t rely on it for strict idle semantics.)
- Work runs only when all constraints are met. The scheduler picks the best backend (JobScheduler, AlarmManager, etc.).


## Steps
1. Depend on the latest stable: `implementation("androidx.work:work-runtime-ktx:<latest>")` (e.g., 2.9.x).
2. Create a `Worker` or `CoroutineWorker` (prefer `CoroutineWorker` for suspend APIs).
3. Implement `doWork()` and return `Result.success() | failure() | retry()`.
   - define a companion object for any keys needed for input/output data.
4. Optional: set constraints, initial delay, backoff criteria; pass `Data` (max ~10KB) for inputs/outputs.
   - set constrains via `Constraints.Builder()`.
   - set backoff criteria with `setBackoffCriteria(BackoffPolicy.LINEAR|EXPONENTIAL, Duration)`.
   - pass input data to the worker with `setInputData(workDataOf(...))`. or Data.Builder() params.inputData.
5. Enqueue via `WorkManager`: one‑time, chained, or periodic (min 15 minutes window). Use unique work to dedupe.
   - TODO: what is the difference between one-time, chained, and periodic work?
   - One-time: `OneTimeWorkRequestBuilder<YourWorker>().build()`
   - Chained: `WorkManager.beginWith(...).then(...).enqueue()`
   - Periodic: `PeriodicWorkRequestBuilder<YourWorker>(repeatInterval, flexInterval).build()`
   - TODO: what is dedupe?
6. Foreground work: if the task must run immediately and continue under restrictions, provide `ForegroundInfo` (notification) via `setForeground(...)` in `doWork()`.
7. create view model to hold state and UI can observe changes
   - store WorkRequest id(s) in ViewModel to observe status or cancel.
7. Observe status with `WorkManager.getWorkInfoByIdLiveData()` or by unique name; cancel via `WorkManager.cancelWorkById()`.


## Key concepts for interviews
- States: `ENQUEUED → RUNNING → (SUCCEEDED | FAILED | CANCELLED)`; retries obey backoff criteria (linear/exponential, min 10s).
- Unique work: `enqueueUniqueWork(name, ExistingWorkPolicy.KEEP|REPLACE|APPEND)`; APPEND chains new work after existing.
- Chaining: `beginWith().then().enqueue()` with inputs/outputs flowing via `Data`.
- Periodic work: min interval 15 min, flex support; cannot be expedited or chained directly.
- Expedited work (API 31+): `setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK)` for immediate execution without creating an FGS, subject to quota.
- Foreground work vs Foreground Service: Foreground work runs inside WorkManager’s foreground service with your notification; use when the work is user‑important but fits WorkManager semantics.
- Foreground types (Android 12+): provide the service type via `ForegroundInfo` or manifest meta‑data; keep types minimal (e.g., `dataSync`, `mediaPlayback`).
- Data limits: `Data` is ~10KB; pass URIs/paths instead of large payloads.
- Cancellation/cooperation: check `isStopped`/coroutine cancellation; make work idempotent.
- Testing: `WorkManagerTestInitHelper`, `TestWorkerBuilder`, and `InstantTaskExecutorRule` for LiveData.


## Limiting concurrency and duplicates -> don't overwhelm device resources
- Prefer logical serialization with unique work:
    - `enqueueUniqueWork(name, ExistingWorkPolicy.APPEND)` to queue new requests after the current chain finishes.
    - `KEEP` to drop new ones, `REPLACE` to cancel and start fresh.
- Configure WorkManager’s executor for global parallelism:
    - Provide a single‑thread or limited thread pool via `Configuration.Builder().setExecutor(...)` in your `Configuration.Provider` Application.
- Rate‑limit in UI/domain layer (disable button, debounce).
- Use tags + queries to detect running work and avoid enqueuing duplicates.


## Additional gotchas
- Min interval for `PeriodicWorkRequest` is ~15 minutes; lower values are rounded up.
- Foreground work requires a visible notification; you can update it and/or publish progress via `setProgress`.
- `Data` is small—use URIs/paths for large inputs.
- `CoroutineWorker` runs on caller dispatcher; use `withContext(Dispatchers.IO)` for I/O.
- Work can be stopped; check `isStopped` and cooperate with cancellation.
- OEM/background restrictions exist; WorkManager retries and persists, but respect user battery optimizations.


## Manifest
- No explicit service registration needed; WorkManager merges its own services.
- For foreground work types, either supply the type in `ForegroundInfo` (preferred) or configure defaults via WorkManager meta‑data (version‑specific).

```xml
<manifest>
    <application>
        <activity >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
<!-- Need to add an intent filter to receive shared images -->
<!-- this intent is not specific to WorkManager but is needed for the example to work -->
<!-- our work manager will process/compress the image received via this intent -->
            <intent-filter>
                <action android:name="android.intent.action.SEND" />
                <category android:name="android.intent.category.DEFAULT" />
                <data android:mimeType="image/*" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```


## Code
```kotlin
class PhotoCompressionWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    // doWork is where the background task is performed and defined
    // there is a limitation of 10 kB for input and output data so we pass the URI of the image to compress as a string
    // for larger data like images we should use URIs or file paths
    override suspend fun doWork(): Result {
        // OPTIONAL: Provide foreground notification early if running as foreground work
        // useful for long-running tasks so user knows something is happening
        setForeground(createForegroundInfo())

        // Get input data passed to the worker
        val uriStr = inputData.getString(KEY_CONTENT_URI) ?: return Result.failure()
        // set size threshold for compression (default 200 KB) and aspect ratio
        // 0L means keep aspect ratio
        val thresholdBytes = inputData.getLong(KEY_COMPRESSION_THRESHOLD, 0L)
        val uri = Uri.parse(uriStr)

        // switch to IO dispatcher for file and bitmap operations
        return withContext(Dispatchers.IO) {
            // Prefer decodeStream to avoid reading whole file into memory
            val original = applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(input)
            } ?: return@withContext Result.failure()

            var quality = 100
            var outputBytes: ByteArray
            do {
                // stream is safer than using readBytes() to avoid OOM for large images (8-16 MB+)
                // check Uris notes for more details on handling large files
                val out = ByteArrayOutputStream()
                // .use will auto-close the stream
                out.use {
                    // how do we interpret the byte array as an image? 
                    // need to convert bitmap to compressed JPEG format
                    original.compress(Bitmap.CompressFormat.JPEG, quality, it)
                    outputBytes = it.toByteArray()
                }
                // Reduce quality by ~10% each iteration (floor to avoid stalling at same value)
                quality = (quality - maxOf(1, (quality * 0.1).roundToInt()))
                // repeat until under threshold (desired size of image) or quality too low (5%)
            } while (outputBytes.size > thresholdBytes && quality > 5)

            /*
            // using readBytes() as an alternative (not recommended for large files)
            val out = appContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            // this is how we handle readBytes being too large -> failure
            val bitmap = BitmapFactory.decodeByteArray(out, 0, out.size) ?: return@withContext Result.failure()
             */
            
            // id is unique work request id to avoid filename collisions
            val outFile = File(applicationContext.cacheDir, "${id}.jpg").apply {
                // write the compressed bytes to the output file
                writeBytes(outputBytes)
            }
            // return the output file path as result data so we can load it later
            Result.success(workDataOf(KEY_RESULT_PATH to outFile.absolutePath))
        }
    }
    companion object {
        const val KEY_CONTENT_URI = "KEY_CONTENT_URI"
        const val KEY_COMPRESSION_THRESHOLD = "KEY_COMPRESSION_THRESHOLD"
        const val KEY_RESULT_PATH = "KEY_RESULT_PATH"
    }

    // OPTIONAL: only if running as foreground work
    private fun createForegroundInfo(): ForegroundInfo {
        val channelId = "photo_compress"
        val nm = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(
                        channelId,
                        "Photo Compression",
                        NotificationManager.IMPORTANCE_LOW
                    ).apply { description = "Foreground work for photo compression" }
                )
            }
        }
        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Compressing image")
            .setContentText("Image compression in progress…")
            .setOngoing(true)
            .build()

        // Provide service type where supported (e.g., data sync)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                1001,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(1001, notification)
        }
    }
}
```

```kotlin
// file: SomeActivity.kt
class SomeActivity : AppCompatActivity() {

    private lateinit var workManager: WorkManager
    private val viewModel: SomeViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_some)

        // Initialize WorkManager
        workManager = WorkManager.getInstance(this)
        setContent {
            WorkManagerTheme{
                // UI observes view model state
                val workerResult by rememberUpdatedState(
                    newValue = viewModel.workId?.let { workManager.getWorkInfoByIdLiveData(it) }
                )
                // or prefer using observeAsState in Compose
                val workResultFlow = viewModel.workId?.let { id ->
                    // need dependency: `implementation("androidx.compose.runtime:runtime-livedata:<latest>")`
                    workManager.getWorkInfoByIdLiveData(id).observeAsState().value
                }
                
                // Launch effect to react to work state changes
                // whenever the worker completes, this will be triggered
                // LaunchedEffect is a side-effect in Compose that runs when the key changes
                // it's a coroutine scope tied to the composable lifecycle
                // so it will be cancelled if the composable leaves the composition
                LaunchedEffect(key1 = workerResult?.outputData) {
                    if (workerResult?.outputData != null) {
                        val resultPath = workerResult?.outputData?.getString(PhotoCompressionWorker.KEY_RESULT_PATH)
                        resultPath?.let { path ->
                            // load the compressed image from file path
                            val bitmap = BitmapFactory.decodeFile(path)
                            viewModel.updateCompressedBitmap(bitmap)
                        }
                    }
                }
            }
        }
    }
    
    // handle new intents (e.g., when activity is already running)
    // only makes sense if activity launch mode is singleTop or singleTask
    // otherwise a new activity would be created -> cause onCreate to be called again -> old activity destroyed
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        } ?: return

        viewModel.updateUncompressedUri(uri)

        // define work request - conditions to run the worker
        // since we want our worker to run every time an image is sent to the activity
        // we create a new OneTimeWorkRequest each time
        // if user sends multiple images quickly, each will create its own work request, 
        // WorkManager handles system resources and scheduling -> limits being flooded with too many parallel tasks
        // e.g. limits number of concurrent workers based on system resources -> use setMaxSchedulerLimit() in Configuration.Provider if needed
        val request = OneTimeWorkRequestBuilder<PhotoCompressionWorker>()
            .setInputData(
                workDataOf(
                    PhotoCompressionWorker.KEY_CONTENT_URI to uri.toString(),
                    PhotoCompressionWorker.KEY_COMPRESSION_THRESHOLD to 20 * 1024L // 20 KB
                )
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresCharging(false)
                    // NOTE: storage not low can be triggered on emulators with small storage
                    .setRequiresStorageNotLow(true)
                    .build()
            )
            .build()
    }
}
```

## ViewModel
```kotlin
class SomeViewModel(application: Application) : AndroidViewModel(application) {

    var uncompressedUri: Uri? by mutableStateOf(null)
        private set
    
    var compressedBitmap: Bitmap? by mutableStateOf(null)
        private set
    
    var workId: UUID? by mutableStateOf(null)
        private set

    fun updateUncompressedUri(uri: Uri?) {
        uncompressedUri = uri
    }

    fun updateCompressedBitmap(bitmap: Bitmap?) {
        compressedBitmap = bitmap
    }

    fun updateWorkId(uuid: UUID?) {
        workId = uuid
    }
}

```

