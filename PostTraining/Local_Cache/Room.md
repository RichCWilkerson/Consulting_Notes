# Resources:

# Room Overview


## Steps:
1. Add Room dependencies to your app's `build.gradle` file.
```kotlin
plugins {
    id("androidx.room")
    alias(libs.plugins.ksp)
}
room {
    schemaDirectory("$projectDir/schemas")
}
dependencies {
    implementation("androidx.room:room-runtime:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
}
```




