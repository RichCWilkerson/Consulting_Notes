

# Google Play Store

## Build an App
- Finalize your app, test it thoroughly
- Ensure it meets Google Play's quality guidelines
- remove unused resources and code
- optimize images and other assets

## Bundles vs APKs
- bundle has all resources for all device configurations (eg. screen sizes, languages, watch vs phone)
- APK is a single package for a specific device configuration
- when you upload a bundle, Google Play generates and serves optimized APKs for each device configuration
- this reduces the app size for users, as they only download what they need

### Generate Signing Key (.jks)
- In Android Studio, go to Build > Generate Signed Bundle / APK
  - Choose Android App Bundle or APK
  - Create a new key store or use an existing one
  - Fill in the required information (key alias, passwords, etc.)
  - Save the .jks file securely, as you'll need it for future updates (locally)
  - fetch play store certificate for app signing in play store console

- App Integrity (New way of signing apps)
    - Google Play App Signing manages and protects your app's signing key
    - When you enroll in Play App Signing, Google generates a new app signing key for you
    - You can still use your upload key to sign your app before uploading it to the Play Store

- Sign your app with this key before uploading to the Play Store

- Deobfuscation with ProGuard/R8
  - [Documentation](https://developer.android.com/topic/performance/app-optimization/enable-app-optimization#decode-stack-trace)
    - ProGuard and R8 are tools that shrink, optimize, and obfuscate your code
    - They remove unused code and rename classes, fields, and methods to make reverse engineering more difficult
    - To enable ProGuard/R8, add the following to your build.gradle file:
```gradle
buildTypes {
    release {
        // Enables code-related app optimization.
        isMinifyEnabled = true

        // Enables resource shrinking.
        isShrinkResources = true      
          
        proguardFiles(
            // Default file with automatically generated optimization rules.
            getDefaultProguardFile('proguard-android-optimize.txt')
        ), 'proguard-rules.pro'
    }
}
```
#### Google Notes:
- We recommend that you always enable both (minify and shrink) settings. 
- We recommend enabling app optimization ONLY in the FINAL VERSION of your app that you test before publishing—usually your release build
  - —because the optimizations INCREASE the BUILD TIME of your project and can make DEBUGGING HARDER due to the way it modifies code.

- To enable R8 to use its full optimization capabilities, remove the following line from your project's gradle.properties file, if it exists:
  - `android.enableR8.fullMode=false`
  - NOTE that enabling app optimization makes stack traces difficult to understand

## Generated build is ready to be uploaded to the Play Store
- upload as internal test track first for testing (provides sharable link)
  - can use debug builds
- then A/B testing track
- then move to release/production when ready


## Google Play Console
