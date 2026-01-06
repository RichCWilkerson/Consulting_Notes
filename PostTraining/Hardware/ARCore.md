Led the development of personalized product discovery, AR-based virtual try-on experiences, and exclusive member-only content, significantly increasing user interaction and sales conversions.

Koin and Ktor


# Resources:
- [Official Home Page Documentation](https://developers.google.com/ar)
- [Android Documentation](https://developers.google.com/ar/develop/java/quickstart)
- [Medium](https://ssharanyab.medium.com/build-your-first-augmented-reality-ar-app-in-android-c8eee36ba277)
- [Medium](https://medium.com/@sidcasm/image-tracking-using-arcore-in-android-f95daf3eee10)
- [Medium](https://medium.com/@DraganaBabic/embarking-on-the-augmented-adventure-unraveling-the-magic-of-arcore-in-android-app-development-c9e1dbeaa965)

# ARCore
## Overview

Can be written in Java or Kotlin.
[SDK github](https://github.com/google-ar/arcore-android-sdk)

currently only XML based layouts, no Compose support yet.

ARCore provides three major capabilities:
1. Motion tracking, which shows positions relative to the world 
2. Anchors, which ensures tracking of an object’s position over time 
3. Environmental understanding, which detects the size and location of all types of surfaces 
4. Depth understanding, which measures the distance between surfaces from a given point 
5. Light estimation, which provides information about the average intensity and color correction of the environment


## How ARCore “Understands” the Real World
1. Plane Detection
> ARCore continuously scans the camera feed to detect flat surfaces — horizontal (floors, tables) and vertical (walls). 
> These planes act as valid locations where virtual objects can be placed.

2. Anchors
> An anchor is a fixed point in the real world. 
> Once you place an object on a surface, ARCore attaches it to an anchor so it stays stable even when the device moves.

3. Frames & Tracking
> Every camera update produces a Frame, which contains information about detected planes, camera pose, and lighting conditions. 
> Your AR scene updates based on these frames.


## Dependencies
Add the ARCore dependency to your app-level build.gradle file:
```gradle
dependencies {
 implementation "com.google.ar:core:1.43.0"
 implementation "com.google.ar.sceneform:core:1.17.1"
}
```

## Setup
1. Best to create it's own feature module and expose a single entry point to the app module.
   - use a dedicated Activity for strong isolation (memory, immersive UI, easier teardown, simpler permission handling)
2. Request and Add ARCore permissions to AndroidManifest.xml:
   - Need to request CAMERA permission from user at runtime as well.
```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-feature android:name="android.hardware.camera.ar" android:required="true" />
```
3. Utilize lifecycle callbacks to manage AR session:
   - onCreate() -> initialize AR session
   - onResume() -> resume AR session
   - onPause() -> pause AR session
   - onDestroy() -> teardown AR session
4. Keep AR dependencies behind an interface and use DI (Koin) to inject into the Activity.
5. Create a model.glb file for 3D object to render in AR.
   - Place model.glb in assets directory `app/src/main/assets/model.glb`
     - ARCore load 3D models from assets folder at runtime.



## Example:
```kotlin
// ArActivity.kt

class ArActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Use a dedicated layout containing a fragment container (or include ArFragment directly)
        setContentView(R.layout.activity_ar)

        // Show the AR fragment (id = container_ar)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container_ar, CustomArFragment())
                .commitNow()
        }

        // Configure window / immersive flags here if needed
    }

    override fun onStart() {
        super.onStart()
        // Initialize AR session here
    }
    
    override fun onResume() {
        super.onResume()
        // Ensure camera permission and AR session lifecycle handled here
    }
    
    override fun onPause() {
        super.onPause()
        // Pause AR session here
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Teardown AR session here
    }
}

// HandleModel.kt
// create a function to place 3D model on detected plane
/*
// How it works:
The user taps a detected surface
An anchor is created at that point
A 3D model is attached to the anchor
The object stays fixed in space as the user moves around
 */
private fun placeObject(hitResult: HitResult) {
    ModelRenderable.builder()
        .setSource(this, Uri.parse("model.glb"))
        .setIsFilamentGltf(true)
        .build()
        .thenAccept { renderable ->
            val anchor = hitResult.createAnchor()
            val anchorNode = AnchorNode(anchor)
            anchorNode.setParent(arFragment.arSceneView.scene)

            val node = TransformableNode(arFragment.transformationSystem)
            node.renderable = renderable
            node.setParent(anchorNode)
            node.select()
        }
}
```

## UI
To use ARCore, you typically create an Activity that hosts an ArFragment. 
The ArFragment manages the AR session and provides a surface for rendering AR content.

You can use Compose, but you'll host that inside a ComposeView using AndroidView or a regular FragmentContainerView.

```kotlin
@Composable
fun ArScreen() {
    AndroidView(
        factory = { context ->
            FrameLayout(context).apply {
                id = View.generateViewId()

                // Attach ArFragment to this container
                val fragment = CustomArFragment()
                (context as AppCompatActivity).supportFragmentManager
                    .beginTransaction()
                    .replace(id, fragment)
                    .commitNow()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
```
