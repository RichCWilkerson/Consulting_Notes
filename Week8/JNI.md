# JNI - Java Native Interface
- A framework that allows **Java/Kotlin code running on the JVM** to call and be called by **native code** (C/C++).
- On Android, JNI is the bridge between the **ART/VM world** (Kotlin/Java) and the **NDK world** (C/C++ libraries).
- Primary goals in Android:
  - Reuse existing C/C++ libraries (crypto, image/audio processing, game engines).
  - Improve performance for **CPU-intensive tasks**.
  - Access low-level system APIs that are only exposed natively (less common today).

---

## NDK - Native Development Kit
- The **NDK** is a set of **tools, headers, and libraries** from Google that let you write parts of your Android app in **C/C++**.
  - TODO: what are headers in this context?
- It includes:
  - **Toolchain**: C/C++ compilers (Clang), linkers, debuggers.
  - **Build tools**: CMake, `ndk-build` for compiling native code into shared libraries.
  - **Platform headers & native libs**: `libc`, `libm`, logging (`liblog`), OpenGL ES, Vulkan, etc.
  - **JNI headers**: APIs to interface with Java/Kotlin code running on ART.
- The NDK is **optional** for most apps:
  - Use it only when you **need native performance** or must **reuse existing C/C++ code**.
- In modern projects it’s most often used with **CMake** to build `.so` shared libraries that your Kotlin/Java code loads with `System.loadLibrary(...)`.

---

## Common JNI/NDK Libraries on Android
- In practice you rarely write *everything* from scratch; you usually wrap or reuse native libraries such as:

- **Crypto / Security**
  - OpenSSL / BoringSSL (via NDK) for encryption, TLS, hashing.
  - Custom in-house C/C++ crypto libraries shared across platforms.

- **Media / Image / Audio Processing**
  - `libjpeg`, `libpng`, `libwebp` for image decoding/encoding.
  - FFmpeg or similar stacks for advanced audio/video processing (transcoding, filters).

- **Game and Graphics Engines**
  - Unity / Unreal / custom C++ engines using OpenGL ES or Vulkan.
  - Physics engines (Box2D, Bullet, etc.).

- **Math / ML / Simulation**
  - Existing numerical libraries (BLAS, LAPACK, Eigen, custom solvers).
  - Native inference runtimes (e.g., TensorFlow Lite’s C API) wrapped for Android.

- **Internal cross‑platform SDKs**
  - Company-wide C++ SDKs (e.g., core business logic, networking, data formats) shared between Android, iOS, desktop.

**Interview angle:**
- Being able to mention **a couple of concrete examples** (e.g., “wrapping an existing C++ crypto library with JNI” or “integrating a C++ image processing engine”) shows you understand *why* teams bring in NDK, not just *how*.

---

## When (and When Not) to Use JNI/NDK

**Good use cases:**
- **Performance-critical code**
  - Heavy math, signal processing, image/video processing, physics engines, codecs.
    - TODO: what are codecs?
- **Reuse of existing native libraries**
  - Your company already has a C++ SDK or shared engine across Android, iOS, desktop.
- **Cross-platform core in C++**
  - Game engines, crypto libraries, simulation engines reused across multiple platforms.

**When *not* to use it:**
- Regular app logic, HTTP calls, DB access, UI logic → Kotlin/Java is more than enough.
- When the bottleneck is **I/O, network, or database**, not CPU.
- When you don’t have strong C/C++ expertise or tooling to maintain native code.

**Interview soundbite:**
> On Android I only reach for the NDK when I truly need it: reusing an existing native library or optimizing hot CPU paths. 
> For normal business logic and networking, Kotlin/Java is safer, easier to debug, and usually fast enough.

---

## Mental Model of JNI on Android

- You have two worlds:
  - **Managed world**: Kotlin/Java, running in ART (Android Runtime).
  - **Native world**: C/C++, compiled into a **.so shared library**.
- JNI provides:
  - A way for **Kotlin/Java to call C/C++** functions (`external` functions).
  - A way for C/C++ to **call back into Java/Kotlin** (via `JNIEnv*` APIs).
- The bridge is based on:
  - A **shared library name** (e.g., `libnative-lib.so` → `System.loadLibrary("native-lib")`).
  - **Function signatures** that follow JNI naming or register with the VM.

---

## Basic Flow: Calling C++ from Kotlin

1. **Declare an external function in Kotlin/Java**
   ```kotlin
   class NativeLib {
       companion object {
           init {
               System.loadLibrary("native-lib")
           }
       }

       external fun stringFromNative(): String
   }
   ```

2. **Create the C++ implementation** (`native-lib.cpp`)
   ```cpp
   #include <jni.h>
   #include <string>

   extern "C"
   JNIEXPORT jstring JNICALL
   Java_com_example_app_NativeLib_stringFromNative(
           JNIEnv* env,
           jobject /* this */) {
       std::string hello = "Hello from C++";
       return env->NewStringUTF(hello.c_str());
   }
   ```

   - `extern "C"` → disables C++ name mangling so the VM can find the symbol.
   - `JNIEXPORT` / `JNICALL` → platform-specific calling conventions.
   - Function name pattern: `Java_<package>_<ClassName>_<methodName>` with `_` escaping.

3. **Tell Gradle to build native code** (CMake)
   - `CMakeLists.txt` at module root:
     ```cmake
     cmake_minimum_required(VERSION 3.10.2)

     project("myapplication")

     add_library(
             native-lib
             SHARED
             native-lib.cpp)

     find_library(
             log-lib
             log)

     target_link_libraries(
             native-lib
             ${log-lib})
     ```

   - `build.gradle` (app module):
     ```groovy
     android {
         defaultConfig {
             // ...existing config...
             externalNativeBuild {
                 cmake {
                     cppFlags "-std=c++17"
                 }
             }
            // externalNativeBuild allows specifying CMake or ndk-build
            // cppFlags sets C++ standard version
         }

         externalNativeBuild {
             cmake {
                 path "CMakeLists.txt"
             }
         }
        // path tells Gradle where to find CMakeLists.txt

         ndk {
             abiFilters "armeabi-v7a", "arm64-v8a" // etc.
         }
        // ndk block to specify target ABIs 
           // ABI = Application Binary Interface - CPU architecture
        // abiFilters limits which architectures to build for
     }
     ```

4. **Call it from Kotlin**
   ```kotlin
   val nativeLib = NativeLib()
   textView.text = nativeLib.stringFromNative()
   ```

---

## Step-by-Step: Import Existing C++ Code into an Android App

### 1. Add C++ Support to Your Module
- When creating a project, choose **Include C++ support**; or
- Later: add `externalNativeBuild` + `CMakeLists.txt` manually.

### 2. Place or Create C++ Sources
- In Android Studio, place code under `app/src/main/cpp/`, e.g.:
  - `app/src/main/cpp/native-lib.cpp`
  - `app/src/main/cpp/mylib.cpp`, `mylib.h`.

### 3. Define External Functions in Kotlin/Java
- In a Kotlin file:
  ```kotlin
  class ImageProcessor {
  
  // the companion object loads the native library when the class is first used
      companion object {
          init { System.loadLibrary("image-lib") }
      }

  // external is a keyword indicating this function is implemented in native code (C/C++)
  // applyFilter must match the JNI function name and signature (params and return type) in C++ 
      external fun applyFilter(pixels: IntArray, width: Int, height: Int): IntArray
  }
  ```

### 4. Implement JNI Functions in C++
- Map the Kotlin/Java methods to native functions:
  ```cpp
  extern "C"
  JNIEXPORT jintArray JNICALL
  Java_com_example_app_ImageProcessor_applyFilter(
          JNIEnv* env,
          jobject /* this */,
          jintArray pixels,
          jint width,
          jint height) {

      // 1. Get pointer to array data
      jboolean isCopy;
      jint* data = env->GetIntArrayElements(pixels, &isCopy);
      jsize length = env->GetArrayLength(pixels);

      // 2. Process pixels (example: invert colors)
      for (jsize i = 0; i < length; ++i) {
          data[i] = ~data[i];
      }

      // 3. Create a new jintArray for the result
      jintArray result = env->NewIntArray(length);
      env->SetIntArrayRegion(result, 0, length, data);

      // 4. Release elements
      env->ReleaseIntArrayElements(pixels, data, JNI_ABORT); // we didn’t modify original

      return result;
  }
  ```

### 5. Configure CMake / ndk-build
- Add all C++ files to `add_library` in `CMakeLists.txt`.
- Link third-party native libraries if needed.

### 6. Load the Library
- Typically done once in an `init` block or `Application` class:
  ```kotlin
  init {
      System.loadLibrary("image-lib")
  }
  ```

### 7. Call from Android Code
- Use your wrapper classes (e.g., `ImageProcessor`) from ViewModels or other layers as needed.

---

## Common Patterns with JNI

- **Thin JNI layer, thick C++ core**
  - Keep JNI functions small: just marshalling/unmarshalling between Java types and native types.
  - Put business logic in C++ functions/classes that are independent of JNI.

- **Wrapping native handles**
  - Manage long-lived native objects (e.g., `Engine*`) via `jlong` handles returned to Kotlin:
    ```cpp
    JNIEXPORT jlong JNICALL Java_com_example_Engine_nativeCreate(JNIEnv*, jobject) {
        auto* engine = new Engine();
        return reinterpret_cast<jlong>(engine);
    }

    JNIEXPORT void JNICALL Java_com_example_Engine_nativeDestroy(JNIEnv*, jobject, jlong handle) {
        auto* engine = reinterpret_cast<Engine*>(handle);
        delete engine;
    }
    ```
  - Kotlin side stores the `Long` and passes it back for future calls.

- **Exception mapping**
  - Catch C++ exceptions and convert them to Java exceptions using `env->ThrowNew`.

---

## Common Pitfalls (What Senior Devs Watch For)

1. **Memory Leaks**
   - Forgetting to release local references (`DeleteLocalRef`) in long-running native methods.
   - Not calling `Release*ArrayElements()` after `Get*ArrayElements()`.
   - Leaking native heap objects that are never `delete`d.

2. **Crashes from Wrong Signatures / Names**
   - JNI function name doesn’t match the package/class/method exactly → `UnsatisfiedLinkError`.
   - Signature mismatches (e.g., using `jint` instead of `jlong`).

3. **Threading Issues**
   - Using a `JNIEnv*` on a different thread than the one it was obtained from.
   - Forgetting to attach/detach native threads to the JVM (`AttachCurrentThread` / `DetachCurrentThread`).

4. **Performance Traps**
   - Frequent small cross-language calls in hot paths → JNI overhead dominates.
   - Excessive copying of arrays and strings between Java and native side.
   - Calling JNI from the main thread with heavy native work → UI jank.

5. **ABI / Packaging Issues**
   - Not including the right ABIs (`armeabi-v7a`, `arm64-v8a`, `x86`, etc.).
   - `UnsatisfiedLinkError` on some devices because the .so for that ABI is missing.

6. **Security Assumptions**
   - Assuming native code is “more secure.”
     - Attackers can still reverse-engineer native libs.
     - Don’t store secrets in native code expecting them to be perfectly safe.

7. **Poor Error Handling**
   - Letting native crashes bring down the whole process.
   - Not translating native errors into meaningful exceptions or error codes in Kotlin.

---

## Testing & Debugging Native Code

- **Unit testing**
  - Put core logic in plain C++ and test it with C++ unit tests (GoogleTest, Catch2, etc.).
  - Keep JNI layer thin and test with Android instrumentation if needed.

- **Debugging**
  - Use **LLDB** / Android Studio native debugger to set breakpoints in C++.
  - Use `__android_log_print` (`<android/log.h>`) for logging.

- **Profiling**
  - Use Android Studio profiler / `perf` / `systrace` to confirm native code is really a bottleneck.

---

## Interview Questions & Talking Points

1. **What is JNI and why is it used in Android?**
   - Bridge between Java/Kotlin and C/C++.
   - Use for performance-critical code or reusing existing native libraries.

2. **What’s the difference between SDK and NDK on Android?**
   - SDK → Kotlin/Java APIs for app logic and UI.
   - NDK → Toolset to write parts of your app in C/C++ and integrate via JNI.

3. **When would you choose to use the NDK in a typical Android app?**
   - Reusing a C++ engine, crypto libraries, or optimizing CPU-bound tasks.
   - Avoid using it for ordinary business logic.

4. **What are some common pitfalls of using JNI?**
   - Memory leaks, crashes due to bad signatures, threading issues, JNI overhead.

5. **How do you structure your code to minimize JNI complexity?**
   - Thin JNI layer + thick C++ core, clear data marshalling, test C++ separately.

6. **How do you handle errors and exceptions between native and managed code?**
   - Map C++ errors to Java exceptions or error codes; avoid crashing the VM.

---

## Quick Checklist for Using JNI/NDK in an Android Project

- [ ] Do I really need native code for this (performance or reuse)?
- [ ] Is my native core isolated from JNI specifics (thin JNI layer)?
- [ ] Are my JNI method names and signatures correct?
- [ ] Am I properly managing native memory and references?
- [ ] Have I considered threading and which thread calls native code?
- [ ] Do I support the correct ABIs for my target devices?
- [ ] Do I have a clear debugging/testing strategy for the native part?

---

# Import C++ Code into Android (Recap)

[Youtube walkthrough](https://www.youtube.com/watch?v=Zwmhp7W6K6E)

## Steps (High-Level Recap):
1. **Define external functions** in a Kotlin/Java class using the `external` keyword.
2. **Create C++ sources** (`native-lib.cpp`, etc.) in `app/src/main/cpp/`.
3. **Implement JNI functions** matching the external declarations.
4. **Create `CMakeLists.txt` or ndk-build script** that builds a shared library.
5. **Configure `build.gradle`** to use `externalNativeBuild` and point to the CMake script.
6. **Load the library** from Kotlin/Java with `System.loadLibrary("your-library-name")`.
7. **Call your native functions** and handle data marshalling + errors carefully.

---

## Basics of C++ for Android Developers

### 1. What Are “Headers” in This Context?
- In C/C++, source is usually split into:
  - **Header files** (`.h` / `.hpp`): declarations.
    - Function signatures, class declarations, type definitions.
    - Think of them like a **public interface** or `.kt` file with only method signatures.
  - **Implementation files** (`.c` / `.cpp`): definitions.
    - Actual function bodies and class method implementations.
- In the NDK:
  - You include Android/NDK headers, e.g. `#include <jni.h>`, `#include <android/log.h>`.
  - Your own library will typically have headers like `mylib.h` that expose functions to be called from JNI.

**Why you care:**
- When you see `#include <jni.h>` at the top of a C++ file, that’s how the compiler learns about the JNI API.
- When you include your own header (e.g., `#include "image_processor.h"`), you’re saying “I’m going to use these functions/classes here.”

---

### 2. C++ Types You’ll Commonly See from Android

You don’t need to master all of C++, but you should be comfortable recognizing these:

- **Primitive types** (rough Java/Kotlin analogies):
  - `int` / `long long` → `Int` / `Long` (but sizes differ per platform; JNI uses `jint`, `jlong` for exact JVM-sized types).
  - `float` / `double` → `Float` / `Double`.
  - `bool` → `Boolean`.
  - `char` → a single byte/character (careful: encoding differs).

- **Standard library types**:
  - `std::string` → roughly like `String`.
  - `std::vector<T>` → dynamic array (like `MutableList<T>` but contiguous in memory).
    - contiguous = stored in a single block of memory
  - `std::unique_ptr<T>`, `std::shared_ptr<T>` → smart pointers for memory ownership.
    - smart pointers = automatically manage memory, avoid leaks -> automatic garbage collection

- **JNI types** (bridging types):
  - `JNIEnv*` → the JNI environment; gives you functions to work with Java objects.
  - `jobject`, `jstring`, `jintArray`, `jbyteArray`, etc. → handles to Java objects on the native side.
  - These are *opaque*; you always manipulate them via `JNIEnv*` methods.

**Why you care:**
- When mapping Kotlin to C++, you’ll often convert:
  - `String` ↔ `jstring` ↔ `std::string`.
  - `IntArray` ↔ `jintArray` ↔ native `jint*` / `std::vector<int>`.

---

### 3. Memory & Ownership (Very High Level)

Unlike the JVM, **C++ doesn’t have garbage collection** by default:

- In C++ you can allocate objects with:
  - **Automatic storage** (stack): `Foo foo;` → destroyed automatically when leaving scope.
  - **Dynamic storage** (heap): `Foo* foo = new Foo();` → you must call `delete foo;` later.

Core idea for you as an Android dev:
- Prefer **RAII / smart pointers** in C++ code:
  - `std::unique_ptr<Foo>` → owns a `Foo`; automatically freed when it goes out of scope.
- Avoid raw `new`/`delete` in JNI glue if you can.

In JNI bridging:
- For **short-lived data** (e.g., process one array, return result):
  - Use local C++ variables and let them go out of scope.
- For **long-lived native objects** (e.g., engine instance):
  - engine instance = a persistent object that maintains state across multiple JNI calls
  - Allocate once (with `new` or smart pointer) and store a handle (`jlong`) in Java/Kotlin.
  - Provide `nativeDestroy()` to clean it up when you’re done.
    - TODO: nativeDestroy goes in the JNI layer written in C++?

You mostly need to **recognize patterns** and not accidentally leak:
- Make sure any `new` / `malloc` has a corresponding `delete` / `free`.
- Make sure JNI arrays/strings you “get” are “released” via the `JNIEnv*` API.

---

### 4. Strings & Arrays Across the Bridge

This is what you’ll do most often.

#### Strings
- On the Kotlin side: `String`.
- On the JNI side: `jstring`.
- On the C++ side: often converted to `std::string`.

Typical pattern:
```cpp
extern "C" // disable name mangling for JNI -> allows Java to find the function
JNIEXPORT void JNICALL // this is the return type and calling convention for JNI functions -> return void
Java_com_example_app_NativeLib_logMessage(
        JNIEnv* env, // parameter giving access to JNI functions
        jobject /* this */, // the calling Java object that invoked this native method (e.g., NativeLib instance)
        jstring message) { // jstring is the JNI representation of a Java String
    // Convert jstring -> std::string
    // declare a const char* to hold the UTF-8 chars from the jstring
    // env->GetStringUTFChars gets the UTF-8 representation of the jstring
    // message is the jstring parameter passed in, nullptr means we don't care about the isCopy flag
    // isCopy flag indicates if the returned chars are a copy or a direct pointer
    const char* chars = env->GetStringUTFChars(message, nullptr);
    // TODO: what is msg here? is it turning the const char* into a std::string?
    std::string msg(chars);

    // ... use msg ...
    // like logging it to Android log
    __android_log_print(ANDROID_LOG_INFO, "NativeLib", "%s", msg.c_str());

    // Release -> free resources associated with the jstring = garbage collection
    env->ReleaseStringUTFChars(message, chars);
}
```

#### Arrays
- Kotlin `IntArray` ↔ JNI `jintArray`.

Pattern:
```cpp
JNIEXPORT void JNICALL
Java_com_example_app_ImageProcessor_nativeProcess(
        JNIEnv* env,
        jobject /* this */,
        jintArray pixels) {

    jboolean isCopy;
    jint* data = env->GetIntArrayElements(pixels, &isCopy);
    // jsize = signed integer type for array lengths/indices
    jsize length = env->GetArrayLength(pixels);

    // Use `data` like a C array of length `length`
    for (jsize i = 0; i < length; ++i) {
        data[i] = ~data[i]; // example: invert colors
    }

    env->ReleaseIntArrayElements(pixels, data, 0); // commit changes back
}
```

As an Android dev, you just need to recognize:
- `GetStringUTFChars` / `ReleaseStringUTFChars` for strings.
- `Get<Primitive>ArrayElements` / `Release<Primitive>ArrayElements` for arrays.
- `GetArrayLength` for sizes.

---

### 5. Error Handling Style

- C++ supports **exceptions**, but many JNI/native codebases avoid them or use them carefully.
- Common patterns you’ll see:
  - Return error codes (`int`, `bool`, enums) instead of throwing.
  - Map native errors to **Java exceptions** using `env->ThrowNew(...)`.

Example of throwing a Java exception from native code:
```cpp
if (something_went_wrong) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
                  "Native processing failed");
    return;
}
```
Example of returning error codes:
```cpp
if (input < 0) {
    return -1; // error code
}
return input * 2; // success
```

Main point: when reading native code, look for how **native failures get surfaced** back to Kotlin.

---

### 6. What You Probably *Don’t* Need (Yet)

For JNI glue + light native integration, you usually **do not** need deep knowledge of:
- Templates and metaprogramming (`template<typename T>`, SFINAE, etc.).
- Advanced C++ features (operator overloading, multiple inheritance, move semantics details).
- STL algorithms (`std::sort`, `std::accumulate`) beyond basic recognition.

If you can:
- Recognize basic types (`int`, `double`, `std::string`, `std::vector`),
- Understand that memory is manual unless using smart pointers,
- Follow how strings/arrays are converted across JNI,
- And read simple function signatures and structs,

…you’re in a good place for **bridging C++ libraries into Android**.

---

### 7. Cheat Sheet for Reading C++ in JNI Context

When you see | Think
-------------|------
`#include <jni.h>` | “This file uses the JNI API.”
`extern "C"` | “Expose this function with a plain C name so Java can find it.”
`JNIEXPORT` / `JNICALL` | “JNI function the VM can call.”
`JNIEnv* env` | “My handle to call into the JVM (create strings, throw exceptions, etc.).”
`jobject`, `jstring`, `jintArray` | “References to Java objects/arrays on the native side.”
`std::string` | “Native string; not the same as `jstring`.”
`std::vector<int>` | “Resizable native array; similar to a `MutableList<Int>`.”
`new` / `delete` | “Manual heap allocation/free; watch for leaks.”
`std::unique_ptr<T>` | “Owns `T`; will auto free when out of scope.”
