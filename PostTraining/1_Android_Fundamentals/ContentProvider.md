# Resources:
[Phillip Lackner - Youtube]() TODO: find the youtube video

# Content Providers
Content Providers in Android are a fundamental component that allows applications to share data with other applications securely. They act as an intermediary between the data source and the application, providing a standard interface for accessing and manipulating data.


## Key Concepts
- **Data Sharing**: Content Providers enable data sharing between different applications. This is particularly useful for sharing data like contacts, media files, or any custom data.
- **URI Structure**: Content Providers use URIs (Uniform Resource Identifiers) to identify the data they manage. The URI typically includes the authority (the Content Provider's unique identifier) and the path to the specific data.
- **CRUD Operations**: Content Providers support standard CRUD (Create, Read, Update, Delete) operations through methods like `insert()`, `query()`, `update()`, and `delete()`.
- **ContentResolver**: Applications use the `ContentResolver` class to interact with Content Providers. It provides methods to perform operations on the data exposed by Content Providers.
  - can query, insert, update, delete data via ContentProvider URIs.
  - requires proper permissions if the ContentProvider is protected.
  - requires:
    1. content URIs to specify which data to operate on, e.g. MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    2. optional selection criteria, e.g. SQL-like WHERE clause
    3. optional ContentValues for insert/update operations


## Permissions
- define permissions in AndroidManifest.xml to control access to Content Providers.
- use `<uses-permission>` to request permissions for accessing protected data.
- some Content Providers require runtime permissions (e.g., accessing contacts or media files).
- ensure to handle permission requests and checks in your application code.
- **NOTE**: CHECK Android documentation for specific permissions required for different Content Providers, as they may vary based on android version and data sensitivity.

## Example Use Cases
- **Contacts**: The Android Contacts Content Provider allows applications to access and manage the user's contacts.
- **Media Store**: The Media Store Content Provider provides access to media files like images, videos, and audio on the device.
- **Custom Data**: Developers can create their own Content Providers to share custom data between applications.
- **Calendar**: The Calendar Content Provider allows apps to read and write calendar events.


## Consume a Content Provider
1. Determine content you want to use (e.g., images from MediaStore, calendar events, contacts).
2. Request necessary permissions in AndroidManifest.xml and at runtime.
3. Use `ContentResolver` to perform CRUD operations on the Content Provider's data.
    - query():
      - content URI to specify data source
      - projection: columns to retrieve
      - selection: SQL-like WHERE clause (optional)
      - selectionArgs: arguments for selection (optional)
      - sortOrder: SQL-like ORDER BY clause (optional)
    - insert():
      - content URI to specify data source
      - ContentValues with data to insert
    - update():
      - content URI to specify data source
      - ContentValues with updated data
      - selection and selectionArgs to specify which rows to update
    - delete():
      - content URI to specify data source
      - selection and selectionArgs to specify which rows to delete
4. NOTE:
   - Always call context?.contentResolver?.notifyChange(uri, null) after insert/update/delete.
   - In query, set cursor.setNotificationUri(context?.contentResolver, uri) so observers are notified.
   - When sharing URIs via intents, add Intent.FLAG_GRANT_READ_URI_PERMISSION (and optionally persistable grants via takePersistableUriPermission if needed).
   - Use applyBatch with ContentProviderOperation for atomic multi‑step updates; bulkInsert for bulk writes.
     - As a client (consumer): Use ContentResolver.applyBatch(authority, ops) to perform multiple inserts/updates/deletes atomically where supported (e.g., ContactsContract).
   - Do not do heavy work on the main thread when consuming a Cursor.
     - ContentResolver.query/insert/update/delete are blocking. Call them off the main thread, typically on Dispatchers.IO.


## Code
```kotlin
class MainActivity : AppCompatActivity() {
    
    private val viewModel: ImageViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
            0
        )
        
        // Example: Querying images from MediaStore Content Provider 
        // each of these is like a column in a database table
        // we define here what columns we want to retrieve
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATE_ADDED, // fallback to DATE_TAKEN if needed
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT
        )
        
        val millisYesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
        }.timeInMillis
        // ? for parameterized query to prevent SQL injection
        val selection = "${MediaStore.Images.Media.DATE_TAKEN} >= ?" // SQL-like WHERE clause (optional)
        // in order of the ? in selection
        val selectionArgs = arrayOf(millisYesterday.toString()) 
        
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC" // sort by date added descending (optional)
        
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor -> 
            // NOTE: always use .use {} to auto-close the cursor
            // cursor is used to iterate over a large dataset efficiently
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            // ... get other column indices similarly from projection
            
            // now we want to iterate over the results and convert each row into an object
            val images = mutableListOf<ImageItem>()
            // will continue to be true if there is a next row (item in db)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id // id is to specify which image we want
                )
                images.add(ImageItem(id, name, contentUri))
            }
            viewModel.updateImages(images)
        }
        
        setContent {
            ContentProviderTheme {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(viewModel.images) { image ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // display image using Coil library
                            AsyncImage(
                                model = image.uri,
                                contentDescription = image.name,
                                modifier = Modifier
                                    .size(200.dp)
                                    .padding(8.dp),
                                contentScale = ContentScale.Crop
                            )
                            Text(
                                text = image.name,
                                modifier = Modifier.padding(8.dp),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

data class ImageItem(
    val id: Long,
    val name: String,
    val uri: Uri
)

// viewmodel
class ImageViewModel: ViewModel() {
    var images by mutableStateOf(emptyList<ImageItem>())
        private set
    
    fun updateImages(newImages: List<ImageItem>) {
        images = newImages
    }
}
```



## Create our Own Custom Content Provider
1. Create a class that extends `ContentProvider`.
2. Implement the required methods: `onCreate()`, `query()`, `insert()`, `update()`, `delete()`, and `getType()`.
3. Declare the Content Provider in the `AndroidManifest.xml` with the appropriate authority and permissions.
   - `<provider android:name=".MyContentProvider" android:authorities="com.example.myapp.provider" android:exported="false" />`
     - if exporting to other apps, don't set exported to true, instead use `android:readPermission/android:writePermission` or `android:grantUriPermissions` with `<path-permission>`.
4. Use the `ContentResolver` to interact with your Content Provider from other applications.
5. NOTE:
    - Use a unique authority (usually your applicationId + .provider).
      - purpose = uniquely identify your Content Provider in "content://" URIs.
      - e.g., "content://com.example.myapp.provider" must be unique across all apps on the device to properly route requests to your provider.
      - add to AndroidManifest.xml when declaring your Content Provider. `android:authorities="${applicationId}provider"`
    - Prefer a contract class to centralize URIs, columns, and MIME types so both provider and clients share the same definitions.
      - provider = the Content Provider implementation
      - clients = apps using the Content Provider
    - applyBatch and bulkInsert can be implemented for efficiency.
      - Override applyBatch (and often bulkInsert) to wrap operations in a single DB transaction and return ContentProviderResult[].

```kotlin
class MyContentProvider: ContentProvider() {
    
    override fun onCreate(): Boolean {
        // TODO
        // initialize your database or data source here
        return true
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        // TODO
        // handle query requests from clients
        // return a Cursor object with the requested data
        return null
    }
    
    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        // TODO
        // handle insert requests from clients
        // return the URI of the newly inserted item
        return null
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // TODO
        // handle update requests from clients
        // return the number of rows updated
        return 0
    }
    
    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        // TODO
        // handle delete requests from clients
        // return the number of rows deleted
        return 0
    }
    
    // getType should return vnd.android.cursor.dir/vnd.<authority>.<path> for collections 
    // and vnd.android.cursor.item/vnd.<authority>.<path> for single items.
    override fun getType(uri: Uri): String? {
        // TODO
        // return the MIME type of data for the given URI
        return null
    }
}

// Example of completed Content Provider:
class MyContentProvider : ContentProvider() {

    private val ITEMS = 1
    private val ITEM_ID = 2

    private val matcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(MyItemsContract.AUTHORITY, MyItemsContract.PATH_ITEMS, ITEMS)
        addURI(MyItemsContract.AUTHORITY, "${MyItemsContract.PATH_ITEMS}/#", ITEM_ID)
    }

    // In‑memory store for demo; replace with SQLite/Room backing store
    private val data = linkedMapOf<Long, ContentValues>()
    private var nextId = 1L

    override fun onCreate(): Boolean = true

    override fun getType(uri: Uri): String? = when (matcher.match(uri)) {
        ITEMS -> MyItemsContract.MIME_DIR
        ITEM_ID -> MyItemsContract.MIME_ITEM
        else -> null
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val columns = projection ?: arrayOf(
            MyItemsContract.Columns._ID,
            MyItemsContract.Columns.TITLE,
            MyItemsContract.Columns.CREATED_AT
        )
        val cursor = MatrixCursor(columns)

        when (matcher.match(uri)) {
            ITEMS -> {
                data.values.forEach { row ->
                    cursor.addRow(columns.map { col -> row[col] })
                }
            }
            ITEM_ID -> {
                val id = ContentUris.parseId(uri)
                data[id]?.let { row ->
                    cursor.addRow(columns.map { col -> row[col] })
                }
            }
            else -> return null
        }

        context?.contentResolver?.let { cr ->
            cursor.setNotificationUri(cr, uri)
        }
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (matcher.match(uri) != ITEMS || values == null) return null
        val id = nextId++
        val row = ContentValues(values).apply {
            put(MyItemsContract.Columns._ID, id)
        }
        data[id] = row
        val inserted = ContentUris.withAppendedId(MyItemsContract.CONTENT_URI, id)
        context?.contentResolver?.notifyChange(inserted, null)
        return inserted
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (values == null) return 0
        return when (matcher.match(uri)) {
            ITEM_ID -> {
                val id = ContentUris.parseId(uri)
                val row = data[id] ?: return 0
                row.putAll(values)
                context?.contentResolver?.notifyChange(uri, null)
                1
            }
            else -> 0
        }
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return when (matcher.match(uri)) {
            ITEM_ID -> {
                val id = ContentUris.parseId(uri)
                val removed = if (data.remove(id) != null) 1 else 0
                if (removed > 0) context?.contentResolver?.notifyChange(uri, null)
                removed
            }
            else -> 0
        }
    }
}
```















