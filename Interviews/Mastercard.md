Current Address:
750 Fort Worth Avenue • Dallas TX 75208
Phone:
(214) 329-1138
Email:
richardchristianwilkerson@gmail.com


## Pitch:
Hi, my name is Christian like the religion, and I’m currently a Lead Android Developer at Neiman Marcus with over 13 years of experience in mobile application development across industries like FINANCE, LUXURY RETAIL, AUTOMOTIVE, and TRAVEL.

At Neiman Marcus, I was brought in to modernize and scale the app.
I:
- Re‑architected the app into Clean MVVM Architecture with feature‑based Gradle modules, which sped up builds and made releases more predictable.
- Led the move to Jetpack Compose, starting with a hybrid XML+Compose approach and then fully composing new features, which cut UI development time and improved design parity with Figma.
- Improved performance and stability by profiling with Android Studio and Firebase, then introducing lazy loading, Coil for images, and better background initialization.
- Hardened security with SSL pinning, token-based auth, and biometrics, and set up CI with GitHub Actions for automated testing.
- led and developed a KMM module for sign-up and login as a PoC to evaluate cross-platform code sharing for iOS and Android.

Before that, at Ally Bank, I worked on the "One Ally" ecosystem, bringing banking, auto, investing, and mortgage into a single app.
There I:
- Implemented secure login and authentication flows combining biometrics with MFA, ensuring compliance with PCI-DSS, FDIC, GFCR, and CFPB.
- Built modular, Kotlin-based features for snapshot, fund transfer, and bill pay using MVVM, Coroutines, Retrofit, and Room with Jetpack Compose.
- Also developed the mobile check deposit feature using CameraX, image processing, and secure upload.

I really enjoy collaborating with other engineers to build useful and engaging mobile experiences that solve real user problems.
As my current project wraps up, I’m now looking for my next challenge, and I believe [Client/Company Name]
would be a fantastic place to continue growing my career and contribute.




# Interview Questions
1. Experience writing SDKs? Describe one you wrote.


2. How do you establish a Bluetooth connection between a mobile device and a peripheral device using BLE?
- Get the right permissions and pre‑checks:
    - Android < 12: `ACCESS_FINE_LOCATION` for scanning.
    - Android ≥ 12: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` (and use `neverForLocation` if not doing location).
    - Check that Bluetooth is enabled and, if required, that location services are on.

- Scan for peripherals:
    - Use `BluetoothLeScanner` with `ScanFilter` (e.g., by service UUID) and `ScanSettings` to limit noise and battery impact.
    - Surface discovered devices to the user or auto‑select by identifier.

- Connect and discover services:
    - Call `connectGatt(...)` to connect as a GATT client.
    - In `BluetoothGattCallback.onConnectionStateChange`, on `STATE_CONNECTED` call `discoverServices()`.
    - In `onServicesDiscovered`, obtain the required `BluetoothGattService` and `BluetoothGattCharacteristic` instances by UUID.

- Interact with characteristics:
    - For reads/writes, call `readCharacteristic` / `writeCharacteristic` and handle results in `onCharacteristicRead` / `onCharacteristicWrite`.
    - For streaming updates, enable notifications/indications via `setCharacteristicNotification` and write the CCC descriptor.

- Manage reliability:
    - Queue GATT operations (one at a time) and handle timeouts.
    - Handle `onConnectionStateChange` for `STATE_DISCONNECTED` by applying a reconnect/backoff strategy when appropriate.
    - Request a larger MTU with `requestMtu()` if you need to send larger payloads.

- Clean up:
    - When the session is finished, stop notifications, call `disconnect()`, then `close()` on `BluetoothGatt` to free resources.



# Job Description
Job Description – SDK Developer (Bluetooth/BLE)
Location: St. Louis, MO || Hybrid
Client: Mastercard

Role Overview

We are seeking an experienced SDK Developer with strong expertise in building end-to-end software development kits and deep knowledge of Bluetooth and Bluetooth Low Energy (BLE) frameworks. 
The ideal candidate will design, develop, and optimize SDK solutions that integrate seamlessly with our product ecosystem while ensuring high performance, security, and reliability.

Key Responsibilities

Design, develop, and maintain end-to-end SDKs, including architecture, implementation, versioning, and release processes.
Build robust, scalable, and reusable SDK components for mobile and connected devices.
Integrate and optimize Bluetooth/BLE communication within applications and device platforms.
Work closely with cross-functional teams (mobile, firmware, QA, product) to ensure seamless integration.
Troubleshoot and resolve issues related to connectivity, performance, and compatibility.
Contribute to technical documentation, coding standards, and best practices.
Participate in code reviews and provide constructive feedback.
Stay updated with emerging technologies, Bluetooth advancements, and SDK frameworks.
Required Skills & Qualifications

Strong hands-on experience in end-to-end SDK development.
Solid understanding of Bluetooth and BLE frameworks, protocols, and communication flows.
Experience with mobile platforms (iOS, Android) or embedded systems is a plus.
Proficiency in programming languages such as Java, Kotlin, Swift, Objective-C, or C/C++.
Strong problem-solving skills and ability to work in a fast-paced environment.
Experience working with APIs, connectivity modules, and device communication layers.
Good understanding of software architecture, design patterns, and debugging tools.
Preferred Qualifications
Experience with IoT ecosystems or connected consumer products.
Knowledge of firmware communication interfaces.
Experience with performance tuning and low-level debugging.
Familiarity with version control systems (Git) and CI/CD pipelines.


## Technology
Bluetooth and NFC are required for the majority of positions



## Interviews

### Round 1

[Justin](http://s3-storage-explorer.s3-website.ap-south-1.amazonaws.com/?video=Android%2FInterviews%2F2025-12-23_R1_Justin_Mastercard.webm&bucket=storage-solution)



## Honda
At Honda, I would treat media playback as a Classic Bluetooth use case based on A2DP (audio) and AVRCP (controls):
Profiles and roles
Head unit / IVI acts as A2DP Sink and AVRCP Controller.
Phone acts as A2DP Source and AVRCP Target.
Connection & routing
Discover and pair the phone using Classic BT, then automatically reconnect known devices when the car starts.
Monitor profile events (A2DP connected/disconnected) from the platform to decide when to:
Route media audio to the car.
Update UI state (PLAYING, PAUSED, DISCONNECTED).

Playback control
Use AVRCP to:
Send play/pause/next/previous commands from steering wheel or head unit.
Read metadata (track title, artist, album, artwork) and sync it with the media UI.
Integrate with Android’s MediaSession/AudioManager so:
Audio focus is respected (navigation prompts, calls, alerts).
Transitions between local sources (FM, USB) and Bluetooth are smooth.
Edge cases
Handle profile switch timing issues (e.g., switching between BT audio and phone call).
Apply a small state machine with debouncing so audio isn’t torn down during fast reconnects or device switches.



I start by doing the platform-specific prechecks: on Android that means runtime permissions for scanning (ACCESS_FINE_LOCATION < 12, BLUETOOTH_SCAN/BLUETOOTH_CONNECT ≥ 12) and confirming that Bluetooth (and, if required, location services) are enabled.
Then I scan using BluetoothLeScanner with ScanFilters on known service UUIDs so I only see relevant peripherals and keep battery impact low. In an SDK, I usually hide this behind a method like discoverDevices() and expose a clean callback/Flow of discovered devices.

Connecting and discovering services
Once the user or host app picks a device, I call connectGatt(...) and treat it as the start of a connection state machine: DISCONNECTED → CONNECTING → DISCOVERING_SERVICES → READY.
In BluetoothGattCallback.onConnectionStateChange, after STATE_CONNECTED, I immediately call discoverServices().
When onServicesDiscovered fires, I validate that the required services and characteristics are present by UUID. If the device doesn’t match the expected GATT profile, I fail fast with a clear error instead of letting callers hit null characteristics later.

Interacting with characteristics
For request/response style operations (e.g., read configuration, write a command), I use readCharacteristic / writeCharacteristic and handle results in onCharacteristicRead / onCharacteristicWrite.
For streaming data (e.g., sensor values, vehicle telemetry), I enable notifications or indications:
Call setCharacteristicNotification(...).
Write the Client Characteristic Configuration descriptor (CCC) with the right value for notify/indicate.
In an SDK, I don’t expose raw GATT calls. Instead, I model domain APIs like:
fun readDeviceInfo(): DeviceInfo
fun startTelemetry(onSample: (TelemetrySample) -> Unit)

Internally those map to specific characteristic reads/writes and notifications, but the host app just sees high-level methods and callbacks or Flows.
Operation queueing and reliability
One of the big BLE pitfalls is issuing multiple GATT operations in parallel. I avoid that by implementing a serial operation queue inside the SDK: one read/write/descriptor write at a time, with timeouts and retries.
I also normalize error handling: connection drops, timeouts, GATT error codes, and MTU negotiation failures are all surfaced as typed errors so the host app can make consistent decisions (e.g., show “device disconnected” vs “operation failed, please retry”).
For throughput, if I need to send larger payloads, I negotiate a higher MTU with requestMtu() and then chunk data accordingly, still respecting the single-operation-at-a-time rule.
Connection lifecycle and reconnection
I treat onConnectionStateChange as the single source of truth for connection state. On STATE_DISCONNECTED, I:
Clean up the operation queue.
Optionally trigger a reconnect with backoff if the product requires it.
Emit a clear state change to the host app (Connected, Ready, Disconnected, Failed(reason)).
This is also where I handle edge cases like bonding/pairing prompts, or devices that briefly drop during firmware updates.
Security and performance considerations
From an SDK perspective, I’m careful about:
Only requesting permissions that are actually needed, and using flags like neverForLocation where appropriate.
Minimizing scan time and using filters so we don’t drain the battery.
Avoiding busy-loop polling in favor of notifications wherever possible.
Providing structured logging hooks (e.g., tag, device ID, operation, latency, result) so integrators can debug field issues without touching the BLE internals.

