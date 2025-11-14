# URIs (Uniform Resource Identifiers) in Android

## Overview
- A Uri identifies a resource. In Android, resources can be app resources, app/private files, media via MediaStore, documents via Storage Access Framework (SAF), or remote resources.
- General structure: `scheme://authority/path?query#fragment` (some are opaque like `mailto:` or `tel:`).
- Parse/build: `Uri.parse(String)`, `Uri.fromFile(File)`, or `Uri.Builder()`.
- URIs are NOT file paths. Many URIs (especially `content://`) point to provider-managed data requiring the ContentResolver and permissions.

## Common Android URI Schemes
- `content://` → Data served by a ContentProvider (MediaStore, Contacts, Documents, FileProvider, custom providers). Access via `ContentResolver`.
- `file://` → Raw file path URI. Since Android 7.0, sharing `file://` with other apps causes `FileUriExposedException`. Use `FileProvider` to convert app files to `content://` for sharing.
- `android.resource://` → App or other app resources (drawables, raw, etc.). Usable with `ContentResolver.openInputStream`.
- `data:` → Inline data (e.g., base64). Useful for tiny payloads only.
- `http://` and `https://` → Network resources; don’t use `ContentResolver` to read these; use an HTTP client.

## Persistence and Permissions
- `content://` from SAF (e.g., ACTION_OPEN_DOCUMENT) can be persisted across reboots with `takePersistableUriPermission` (read/write flags returned in the result).
- `content://` from ACTION_GET_CONTENT (or Compose `GetContent`) is typically a temporary grant (no persistable flag). To keep long-term, either:
  - copy the bytes into your app’s private storage -> `file://`
  - or prefer `OpenDocument` so you can persist the grant.
- MediaStore URIs can often be re-queried later if the item remains, but don’t assume permanence; the item can be deleted or reindexed.
- On Android 13+ you don’t need storage permissions when using the system Photo Picker. Otherwise follow scoped storage rules and request granular media permissions (`READ_MEDIA_IMAGES/VIDEO/AUDIO`) on 33+, `READ_EXTERNAL_STORAGE` on 29–32.

## Handy APIs
- `contentResolver.openInputStream(uri)` / `openOutputStream(uri)`
- `contentResolver.getType(uri)` to get MIME type
- `DocumentsContract` helpers for SAF
- `ContentResolver.takePersistableUriPermission(uri, flags)` and `persistedUriPermissions`
- `Uri.encode`, `Uri.decode`, `uri.getQueryParameter(name)`, `uri.pathSegments`

## Interview Talking Points
- Scoped storage + SAF + Photo Picker: how they changed media/file access, and when to use each.
- Persistable URI permissions: `OpenDocument` vs `GetContent`, how to persist and revoke.
- `FileProvider`: why it exists and how to configure it for sharing files with other apps.
- Security: temporary vs persistable grants, `FLAG_GRANT_READ_URI_PERMISSION`, and minimizing permissions.
- MIME types and how clients/providers agree on formats.

## Gotchas and Best Practices
- Always close streams: wrap in `use {}`.
- Don’t share `file://` with other apps—use `FileProvider` to get a `content://`.
- For long-term access to user-selected documents, prefer `OpenDocument` and call `takePersistableUriPermission`.
  - apps like instagram do not do this, they just upload immediately and discard the uri.
  - use case -> doc or photo editor saving back to original location.
- For user media selection on 33+, prefer the Photo Picker; no storage permission needed.
- Use `contentResolver.getType(uri)` before assuming a format.
  - When you can’t assume a format:
    - content:// URIs: providers may return null or generic MIME (e.g., application/octet-stream), or mislabel types. File extensions can be missing or spoofed.
    - Transcoding sources: camera/Photos might return HEIC/WEBP even if you expected JPEG.
    - Cloud/backed‑by‑stream providers (Drive, email, ZIP viewers) often hide real file types.
    - FileProvider and custom providers may omit getType, or return directory MIME for trees.
    - Never trust filename/extension alone; validate MIME and, if critical, sniff magic bytes.
- Large reads: stream/chunk instead of `readBytes()` to avoid OOM on big files.
  - Prefer streaming unless size is known and comfortably small. A pragmatic bound is 8–16 MB for readBytes().
  - Get size -> decide by scheme:
    - for file:// use File.length(); 
    - for content:// 
      - first OpenableColumns.SIZE; 
      - then (if still unknown, OpenableColumns.SIZE is -1 or null) try: 
        - AssetFileDescriptor.length; 
        - OR DocumentFile.length(); 
        - if still unknown, treat as unknown size and stream carefully.
    - for MediaStore you can include its size column in the initial query (it often maps to the same underlying column);
- Watching changes: register a `ContentObserver` on provider URIs if you need to react to updates.

## Code Examples

### 1) Resource URI (android.resource)
```kotlin
// Build a resource URI and read bytes via ContentResolver
val resUri = Uri.parse("android.resource://$packageName/${R.drawable.image_name}")
val resBytes = contentResolver.openInputStream(resUri)?.use { it.readBytes() }

// Alternatively, use Resources APIs when you control the resource
val drawableStream = resources.openRawResource(R.drawable.image_name)
val bytes = drawableStream.use { it.readBytes() }
```

### 2) File URI and FileProvider (share safely)
```kotlin
// Create a private file
val file = File(filesDir, "example.txt").apply {
    writeText("Hello, Uris!")
}

// If staying inside your app, you can use Uri.fromFile(file)
val internalFileUri: Uri = Uri.fromFile(file)

// To share with other apps, convert to a content URI via FileProvider
val shareUri: Uri = FileProvider.getUriForFile(
    this,
    "${applicationContext.packageName}.fileprovider", // authority from manifest
    file
)

// Share via intent with a temporary read grant
val share = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_STREAM, shareUri)
    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
}
startActivity(Intent.createChooser(share, "Share file"))
```

#### FileProvider security configuration
The `file_paths.xml` file defines which directories/files in your app's internal or external storage can be shared via the `FileProvider`.
This is a security measure to prevent exposing arbitrary files on the device.
By specifying paths in this XML file, you control what parts of your app's storage are accessible through the `FileProvider`.

```xml
<!-- AndroidManifest.xml -->
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>

<!-- res/xml/file_paths.xml -->
<paths>
    <files-path name="files" path="."/>
</paths>
```

### 3) Picking content: GetContent vs OpenDocument
```kotlin
// GetContent: temporary access, no persistable grants
val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
    uri?.let {
        val mime = contentResolver.getType(it)
        val bytes = contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
        // For long-term use, copy to app storage 
    }
}
// pickImage.launch("image/*")

// OpenDocument: persistable permission possible
val openDoc = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
    uri?.let {
        // Persist permission for future access
        val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        contentResolver.takePersistableUriPermission(it, flags)
    }
}
// openDoc.launch(arrayOf("image/*"))
```

### 4) Photo Picker (API 33+)
```kotlin
// No storage permission required; returns content URIs
val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
    uri?.let {
        val bytes = contentResolver.openInputStream(it)?.use { s -> s.readBytes() }
    }
}
// picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
```

### 5) Data URI (small inline data)
```kotlin
val dataUri = Uri.parse("data:text/plain;charset=utf-8,Hello%2C%20World!")
val text = contentResolver.openInputStream(dataUri)?.use { it.reader().readText() }
```

### 6) Working with query params and builders
```kotlin
val uri = Uri.Builder()
    .scheme("https")
    .authority("api.example.com")
    .appendPath("search")
    .appendQueryParameter("q", "cats & dogs")
    .build()
val q = uri.getQueryParameter("q") // "cats & dogs"
```


