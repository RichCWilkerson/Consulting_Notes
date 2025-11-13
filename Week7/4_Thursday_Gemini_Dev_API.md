# These topics are important, but we don't have time to cover them in depth today.

## Hardware
- Sensors
  - Environmental Sensors
  - Position Sensors
  - Motion Sensors
    - light sensor
    - proximity sensor
    - accelerometer
    - gyroscope
    - magnetometer
    - barometer
    - GPS
  - Can figure out what the user is doing (walking, running, driving, etc.) using these sensors
  - There are ways for us to check if device has these sensors and use them accordingly
- Camera
  - Flash
- Microphone
- Speakers
- Bluetooth


## AI
[Documentation](https://developer.android.com/ai/gemini/developer-api)
[Task](https://developer.android.com/courses/pathways/android-ai-overview)
[Getting Started with Firebase SDK](https://firebase.google.com/docs/vertex-ai/get-started?platform=android)
- Gemini SDK
  - Gemini Developer API
    - Access Gemini models directly from your app
    - Integrate Gemini's capabilities into your app's functionality
- Prompting
  - `Include`: used to specify desired content
  - `Exclude`: used to filter out unwanted content
  - `System`: provides context or instructions to the model
  - `Act as`: defines the role or persona the model should assume
  - [Google AI Studio](https://aistudio.google.com/)
  - is an Integrated Development Environment (IDE) that you can use to prototype and design prompts for your app's use cases.

- Dependencies
```gradle
dependencies {
  // ... other androidx dependencies

  // Import the BoM for the Firebase platform
  implementation(platform("com.google.firebase:firebase-bom:34.4.0"))

  // Add the dependency for the Firebase AI Logic library When using the BoM,
  // you don't specify versions in Firebase library dependencies
  implementation("com.google.firebase:firebase-ai")
}   
```
- initialize 
```kotlin
// Start by instantiating a GenerativeModel and specifying the model name:
val model = Firebase.ai(backend = GenerativeBackend.googleAI())
    .generativeModel("gemini-2.5-flash")
```

- Generate text
```kotlin
scope.launch {
    val response = model.generateContent("Write a story about a magic backpack.")
}
```

- Charles Proxy (paid version of Postman)
  - testing network requests (APIs)

- Gemini SDK and Gemini Mobile 
  - just skim over everything else to know what exists for now
  - we will learn implementation on Gemini SDK and Mobile right now in the code lab