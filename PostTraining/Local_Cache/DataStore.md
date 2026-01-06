[Youtube](https://www.youtube.com/watch?v=XMaQNN9YpKk)

# DataStore (vs SharedPreferences)
A modern, coroutine-based key-value and small-object storage API that replaces most SharedPreferences use cases.

## What DataStore is
- Jetpack library for **small, persistent configuration/state**, not a full database.
- Uses **Kotlin coroutines + Flow** for asynchronous, reactive I/O (no main-thread blocking).
- Two flavors:
  - **Preferences DataStore** – untyped key–value pairs (similar to SharedPreferences but suspend/Flow-based).
  - **Proto DataStore** – strongly typed data using **Protocol Buffers** schemas.

---

## Why choose DataStore over SharedPreferences
- **Non-blocking I/O**
  - SharedPreferences does disk I/O on the calling thread; easy to accidentally block the main thread.
  - DataStore performs I/O off the main thread and exposes data as `Flow`, reducing ANR risk.

- **Reactive data model**
  - Exposes values as `Flow<T>` so UI can automatically react to changes (ideal for Compose / MVVM / MVI).
  - Encourages unidirectional data flow: repository → ViewModel → UI.

- **Type safety (Proto)**
  - `.proto` schema generates Kotlin classes; fields are strongly typed (no stringly-typed keys).
  - Safer refactors: reusing field numbers preserves backward compatibility.

- **Built-in migration & corruption handling**
  - `SharedPreferencesMigration` for one-time migration from existing prefs.
  - `CorruptionHandler` / `ReplaceFileCorruptionHandler` to recover from file corruption without crashing.

- **Better testability & architecture fit**
  - Suspend/Flow APIs are easy to test with `runTest` and fake implementations.
  - No direct Android dependency in most calling code, fits clean layering and DI.

---

## When to use DataStore
Use DataStore for **small, configuration-like data**:
- User preferences: theme, language, notification toggles.
- App-level flags: onboarding complete, biometric enabled, feature flags.
- Lightweight, structured settings objects (best with Proto DataStore).
- Scenarios where **reactive updates** to config/preferences are helpful.
  - e.g., UI automatically updates when a preference changes.

```kotlin
// Structured settings example
data class UserSettings(
    val darkModeEnabled: Boolean,
    val fontSize: Int,
    val notificationsEnabled: Boolean
)
```

### When *not* to use DataStore
Avoid DataStore when data:
- Is **large or growing** (lists of entities, logs, timelines, feeds).
- Needs rich queries (filtering, sorting, joins).
- Is binary (images, PDFs, audio) or many KB/MB.

For those cases, prefer:
- **Room / SQLDelight** – structured, queryable data.
- **Files/MediaStore** – large binary content.
- **Backend APIs / cloud** – cross-device or server-owned data.

---

## Preferences vs Proto DataStore
### Preferences DataStore
- Key–value store similar to SharedPreferences.
- Uses typed keys (`booleanPreferencesKey`, `stringPreferencesKey`, etc.).
- Great as a **drop-in replacement** when you don't need a strict schema.
  - for simple flags/settings.

### Proto DataStore
- Stores a single strongly typed object defined in a `.proto` file.
- Benefits:
  - Strong contracts between versions; schema is the source of truth.
  - Easy **schema evolution**: add optional fields with new field numbers.
  - Clear separation between model (`.proto`) and usage (repositories/ViewModels).

```proto
syntax = "proto3";

message UserSettings {
  bool dark_mode_enabled = 1;
  int32 font_size = 2;
  bool notifications_enabled = 3;
}
```

---

## Advantages
- Non-blocking, coroutine-based API.
- Reactive streams of data via `Flow`.
- Type safety and schema evolution with Proto.
- Built-in migration from SharedPreferences.
- Corruption handling hooks.
- Easier testing and cleaner layering than direct SharedPreferences usage.

---

## Disadvantages / Common pitfalls
- **Learning curve** – requires familiarity with coroutines & Flow.
- **Proto setup overhead** – defining `.proto` files and Gradle/protoc configuration.
- **Not a database** – inefficient for large or highly structured data.
- **Async debugging** – need to reason about scopes, cancellation, and collectors.

---

## Senior-level practices & interview notes
### Clean Architecture integration
- Wrap DataStore in a **repository or local data source**:
  - Repositories expose `Flow`/suspend APIs; callers don't depend on DataStore directly.
  - Enables swapping implementations (e.g., in-memory for tests, different storage on TV/Wear).

### Migrations from SharedPreferences
- Use `SharedPreferencesMigration` when creating DataStore:
  - Migrate keys once, then deprecate/remove SharedPreferences usage.
  - Add tests around upgrade paths to avoid data loss.

### Error handling & corruption
- Implement `CorruptionHandler` to reset to safe defaults or recover data.
- Log corruption events (no PII) to monitoring for observability.

### Security & encryption
- DataStore by itself is **not encrypted**.
- For sensitive values (tokens, PII):
  - Use **Jetpack Security + Keystore** and a custom `Serializer` that encrypts/decrypts bytes.
  - Tink (Google’s cryptography library)
  - Custom encryption transformer functions (e.g., Transformations.map or custom Serializer encrypt/decrypt steps)

### Testing strategies
- Use `kotlinx.coroutines.test.runTest` to unit-test repositories that wrap DataStore.
- Prefer injecting `DataStore` or an abstraction; provide in-memory or temporary-file instances in tests.
- For Proto DataStore, test both:
  - Default values when file is missing or empty.
  - Migration behavior when schema changes or when SharedPreferencesMigration runs.


---

# Philipp Lackner
Dependencies:
- `androidx-datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "androidxDataStore" }`
  - access to Preferences DataStore
- `kotlinx-serialization-json = { module = "org.jetbrains.kotlinx:kotlinx-serialization-json", version.ref = "kotlinxSerialization" }`
  - for JSON serialization if needed

Plugins:
- `jetbrains-kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }`
  - for Kotlin serialization support


## Steps
1. Add dependencies and plugins to `build.gradle`
2. Create a Crypto object for encryption/decryption
3. Create a Serializer for DataStore that uses Crypto to encrypt/decrypt data
4. Create the DataStore instance using the custom Serializer


## Check datastore in Android Studio
1. left side -> click on "..." (More tool windows)
2. Select Device Explorer
3. Search for app package name
4. Navigate to files/datastore/<name of datastore file>


## KMP 
- works but needs some adjustments
- DataStore is the same
- Encryption is platform-specific, so need to implement Crypto separately for Android and iOS, Windows, Web, etc.

```kotlin   
// Crypto.kt - currently in UI layer
object Crypto {
    // name of the key in the Android Keystore so we can retrieve it later
    // keys need to be kept safe and not hardcoded in production apps
    private const val KEY_ALIAS = "secret"

    // AES is a popular symmetric encryption algorithm used for secure data storage
    private const val ALGORITHM = KeyProperties.KEY_ALGORITHM_AES
    private const val BLOCK_MODE = KeyProperties.BLOCK_MODE_CBC
    private const val PADDING = KeyProperties.ENCRYPTION_PADDING_PKCS7

    // bundles algorithm, block mode, and padding into a single transformation string for Cipher
    private const val TRANSFORMATION = "$ALGORITHM/$BLOCK_MODE/$PADDING"

    // contains the information on how we want to encrypt/decrypt our data
    private val cipher = Cipher.getInstance(TRANSFORMATION)

    // need to access the Android Keystore to store/retrieve encryption keys
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null) // null is just saying we are not going to configure it with any parameters
    }

    private fun getKey(): SecretKey {
        val existingKey = keyStore
            .getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
        // if key already exists, return it; otherwise, create a new one
        return existingKey?.secretKey ?: createKey()
    }

    // need to generate a secret key for encryption/decryption
    private fun createKey(): SecretKey {
        return KeyGenerator
            .getInstance(ALGORITHM)
            .apply {
                init(
                    KeyGenParameterSpec.Builder(
                        KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(BLOCK_MODE)
                        .setEncryptionPaddings(PADDING)
                        // if we create the same key multiple times, it should be different each time
                        // everytime you encrypt the string "Hello", the output should be different
                        .setRandomizedEncryptionRequired(true)
                        // set to true for biometric auth - typical for banking apps, extra level of security
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }
    }

    fun encrypt(bytes: ByteArray) {
        cipher.init(Cipher.ENCRYPT_MODE, getKey())
        val initializationVector = cipher.iv
        val encrypted = cipher.doFinal(bytes)
        return initializationVector + encrypted // prepend IV for use in decryption
    }

    fun decrypt(bytes: ByteArray): ByteArray {
        val iv = bytes.copyOfRange(0, cipher.blockSize)
        val data = bytes.copyOfRange(cipher.blockSize, bytes.size)
        cipher.init(Cipher.DECRYPT_MODE, getKey(), IvParameterSpec(iv))
        return cipher.doFinal(data)
    }

}

// UserPreferencesSerializer.kt
@Serializable
data class UserPreferences(
    val authToken: String = null,

    )

object UserPreferencesSerializer : Serializer<UserPreferences> {

    // used to get the default UserPreferences when no data is stored yet
    override val defaultValue: UserPreferences 
        get() = UserPreferences()
    
    // read the data from the InputStream, decrypt it, and deserialize it into UserPreferences
    override suspend fun readFrom(input: InputStream): UserPreferences {
        val encryptedBytes = withContext(Dispatchers.IO) {
            input.use {
                it.readBytes()
            }
        }
        val encryptedDecodedBase64 = Base64.getDecoder().decode(encryptedBytes)
        val decryptedBytes = Crypto.decrypt(encryptedDecodedBase64)
        val decodedJsonString = decryptedBytes.decodeToString()
        return Json.decodeFromString(decodedJsonString)
    }
    
    // serialize the UserPreferences into bytes, encrypt them, and write to OutputStream
    override suspend fun writeTo(t: UserPreferences, output: OutputStream) {
        // write to output stream to save to DataStore
        // need to convert UserPreferences to an object that can be easily serialized and stored in DataStore
        // use kotlinx.serialization to convert to JSON bytes
        // t is the UserPreferences object passed in
        val json = Json.encodeToString(t)
        // these are the bytes we want to encrypt and store
        val bytes = json.toByteArray() 
        val encryptedBytes = Crypto.encrypt(bytes)
        // in order to avoid certain crashes, we now save as base64 string
        val encryptedByteBase64 = Base64.getEncoder().encode(encryptedBytes)
        withContext(Dispatchers.IO) {
            // .use to automatically close the stream after writing
            output.use {
                it.write(encryptedByteBase64)
            }
        }
    }
}

// MainActivity.kt or wherever you create the DataStore instance

private val Context.dataStore by dataStore(
    fileName = "user_prefs",
    serializer = UserPreferencesSerializer
)

// generate a random 
private const val SECRET_AUTH_TOKEN = (1..1000 ).map {
    (('a'..'z') + ('A'..'Z') + ('0'..'9')).random()
}.joinToString(separator = "")

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        SetContent {
            MyAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Row {
                        val scope = rememberCoroutineScope()
                        var text by remember { mutableStateOf("") }
                        Button(
                            onClick = {
                                scope.launch {
                                    // save the auth token to DataStore
                                    dataStore.updateData {
                                        UserPreferences(
                                            authToken = SECRET_AUTH_TOKEN
                                        )
                                    }
                                }
                            }
                        ) {
                            Text(text = "Encrypt Auth Token")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    // read the auth token from DataStore
                                    text = dataStore.data.first().authToken ?: "No Auth Token Found"
                                }
                            }
                        ) {
                            Text(text = "Decrypt Auth Token")
                        }
                        Text(text = text)
                    }
                }
            }
        }
    }
}
```



















