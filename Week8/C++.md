# Import C++ Code into Android
[Youtube walkthrough](https://www.youtube.com/watch?v=Zwmhp7W6K6E)

## Steps:
1. Define the desired functions in a Kotlin module as `external` functions.
2. create a source C++ file (e.g., `native-lib.cpp`) in the `cpp` directory of your Android project.
3. Write the definitions of the external functions in the C++ file using appropriate function signatures defined by the JNI (Java Native Interface).
4. Create a build script for the C++ code using CMake or ndk-build.
   - click to add a file at the root level -> CMakeLists.txt 
5. Adjust the `build.gradle` file to include the C++ build script.
6. Load the shared C++ library statically to Kotlin using `System.loadLibrary("your-library-name")`.

