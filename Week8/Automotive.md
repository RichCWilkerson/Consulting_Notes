# Resources:
[Basics 1 year ago](https://www.youtube.com/watch?v=sripkRMgww0&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

[Auto Architecture](https://www.youtube.com/watch?v=KFf4IH4CQ48&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

[Introduction](https://www.youtube.com/watch?v=KVM5njlZ4sM&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

[Long](https://www.youtube.com/watch?v=nNfX4sA2y4o&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

[Android Automotive OS](https://www.youtube.com/watch?v=H7fx3pnaHp4&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

[Build for Automotive](https://www.youtube.com/watch?v=5XyujJ7G47c&pp=ygUSYW5kcm9pZCBhdXRvbW90aXZl)

---

# Android Automotive

## 1. Big Picture – What is Android in the Car?

There are **two related but different** things you’ll see in job descriptions:

1. **Android Auto** (phone projection)
   - Your **phone** runs the app.
   - UI is **projected** to the car’s head unit.
   - Apps are built as **projection apps** (media, messaging, navigation) using specific APIs and templates.
   - OEM head unit is basically a “dumb screen” plus input events.

2. **Android Automotive OS (AAOS)**
   - Android is the **OS running directly in the car** (on the IVI – In-Vehicle Infotainment system).
   - Your app runs **on the car hardware**, not on the phone.
   - Can have deeper integration with vehicle data (speed, climate, battery, doors, etc.) via **Car APIs / Vehicle HAL**.
   - There are two flavors:
     - **AOSP-based AAOS**: OEM can ship without Google apps/services.
     - **Google Automotive Services (GAS)**: includes Play Store, Google Maps, Assistant, etc.

As an Android dev:
- Android Auto = you mostly build **projection apps** with constraints.
- AAOS = you build **native in-car apps or system apps**, often with extra responsibilities (safety, HMI guidelines, vehicle integration).

---

## 2. Core Concepts & Architecture

### 2.1 High-level architecture (AAOS)

- **Linux kernel + Android framework**: similar to phones, but customized for automotive.
- **Vehicle HAL (Hardware Abstraction Layer)**:
  - Interface between Android and the car’s ECUs/buses (e.g., CAN, LIN, FlexRay).
  - Provides standardized signals: speed, gear, fuel/battery, doors, HVAC states.
- **Car Service / Car APIs**:
  - System service on top of Vehicle HAL.
  - Exposes `Car` and related APIs to apps (`CarInfoManager`, `CarSensorManager`, `CarPropertyManager`, etc.).
- **System UI / HMI**:
  - Launchers, status bars, navigation bars tailored for driver/passenger use.
- **Apps**:
  - System apps (e.g., settings, HVAC UI) and third-party apps (media, navigation, communication).

You don’t usually touch the HAL directly as an app dev – you talk to **Car APIs** or OEM-provided SDKs.

---

## 3. Technologies & What You Need to Know

### 3.1 Android Automotive OS (AAOS)
- Full-blown Android OS in the car.
- As an app dev, you mostly care about:
  - App lifecycle on AAOS.
  - Car-specific permissions and restrictions.
  - HMI guidelines and templates.
  - How to access car properties in a safe/limited way.

### 3.2 Android Auto (Projection)
- Apps run on the **phone**, UI drawn in the car.
- Strict UX templates for safety (no arbitrary UI).
- App categories: Navigation, Media, Messaging, EV Charging, Parking, etc.
- You work with **Car App Library** and **templates** rather than free-form Compose layouts.

### 3.3 Vehicle HAL (Hardware Abstraction Layer)
- Low-level interface for OEMs / system integrators.
- Defines properties (e.g., vehicle speed, gear, temperature) and events.
- App developers generally don’t implement VHAL; they **consume** data exposed via higher-level Car APIs.

### 3.4 Car APIs
- Exposed via `android.car` (or OEM-specific) packages.
- Examples:
  - `CarInfoManager`: static info like make, model, fuel type.
  - `CarSensorManager`: sensor-like properties.
  - `CarPropertyManager`: generic car properties (HVAC, doors, windows) depending on OEM.
- Require **special permissions** and often **system / privileged** status for write access.

### 3.5 Media & Navigation Services
- Common AAOS / Android Auto apps:
  - **Media**: music, podcasts, audiobooks.
  - **Navigation**: turn-by-turn directions.
- Use standard Android frameworks:
  - `MediaBrowserServiceCompat` / `MediaSession` / ExoPlayer.
  - `CarAppService` and navigation templates for Android Auto / AAOS.
- Must follow **driver distraction rules**: very limited animation & interactions while driving.

### 3.6 Google Play Services for Automotive
- Available on GAS-based AAOS systems.
- Provides:
  - Play Store for Automotive.
  - Google Maps, Assistant integration.
  - Some APIs may be gated or limited.

### 3.7 HMI (Human-Machine Interface) Guidelines
- Safety-critical: you must follow guidelines for:
  - Text size, contrast, touch target sizes.
  - Limited interactions while driving.
  - No long text entry, minimal scrolling, simplified lists.
- Google and OEMs publish design docs for Automotive / Android Auto.
- As a senior dev, you’re expected to advocate for **driver-safe UI** and push back on risky designs.

### 3.8 AOSP for Automotive
- Open-source AAOS reference implementation.
- OEMs start from it and customize system UI, services, and HAL.
- For dev roles closer to platform, you might:
  - Work on AOSP system apps.
  - Extend CarService.
  - Integrate new vehicle properties.

### 3.9 IPC (Binder, AIDL)
- Same as regular Android but used more heavily between:
  - CarService ↔ system apps.
  - OEM services ↔ apps.
- If you work near the platform, you should be comfortable with AIDL interfaces and Binder.

### 3.10 Android SDK for Automotive (Car App Library)
- For Automotive / Android Auto apps (especially media/nav/messaging):
  - Uses **templates** rather than free-form UIs.
  - You implement screens by returning template objects (lists, maps, panes) from a `CarAppService`.
  - System renders them appropriately for the vehicle.
- Good to know at a high level; details depend on app category.

### 3.11 Automotive-specific Testing Frameworks
- Similar to phones but with:
  - **Emulators / head unit simulators**.
  - OEM test harnesses.
  - UI automation tools integrated with the IVI environment.
- For senior roles, you should know:
  - How to run your app in an AAOS emulator.
  - How to run instrumentation/UI tests in an automotive environment.

### 3.12 Telematics & Connectivity (CAN, OBD-II)
- Underneath, vehicle networks like CAN/LIN carry data.
- Apps usually **do not talk to CAN directly**; they use:
  - OEM SDKs.
  - Vehicle HAL → Car APIs.
  - Or Bluetooth/Wi‑Fi to an external dongle (OBD-II) for aftermarket solutions.
- For senior interviews, you just need to know **where your app sits** in this stack.

---

## 4. AAOS vs Android Auto – What’s expected from a Senior Android Dev?

### If the role is more **Android Auto / projection app**:
- Strong knowledge of:
  - Car App Library (templates for media/nav).
  - Constraints: no arbitrary UI, driver distraction limits.
  - Handling different head units / screen sizes / input methods (touch, knob, D‑pad).
- You’re mostly building a **flavor** of your mobile app tailored for the car.

### If the role is more **AAOS / in-car app**:
- Strong Android fundamentals + knowledge of:
  - How Car APIs expose vehicle data.
  - How to design safe, glanceable UIs for drivers.
  - Working with OEM/integration teams on permissions and APIs.
- Possibly some exposure to:
  - AOSP for Automotive customs.
  - Binder/AIDL.
  - System apps and privileged permissions.

Often, job descriptions mention both, so being able to **articulate the difference** is key.

---

## 5. Best Practices (for Automotive Apps)

1. **Safety first**
   - Minimize driver distraction: short interactions, big touch targets, limited input while moving.
   - Avoid long text entry or complex flows while the vehicle is in motion.
   - Respect HMI and driver distraction guidelines from Google and OEM.

2. **Use templates when required**
   - For Android Auto and many AAOS contexts, use **Car App Library templates** instead of custom UIs.
   - Let the system handle layout and theming.

3. **Design for multiple screen sizes and input methods**
   - Cars can have:
     - Small, low-res displays or large, ultra-wide ones.
     - Touch screens, rotary knobs, D‑pads, steering wheel controls.
   - Don’t rely solely on touch; support focus navigation and hardware buttons where needed.

4. **Offline and connectivity-aware**
   - Connectivity in cars can be spotty.
   - Cache data where appropriate and handle network loss gracefully.

5. **Permissions & privacy**
   - Vehicle data is sensitive (location, driving behavior).
   - Request only what you need; be clear about why.
   - Follow policies for vehicle data, similar to location/privacy on mobile.

6. **Performance and startup time**
   - AAOS apps should start quickly; UI should be responsive.
   - Avoid heavy work on main thread; use coroutines/WorkManager.

7. **Work closely with OEMs and other teams**
   - Automotive projects often involve:
     - OEM partners.
     - Embedded teams.
     - Backend and mapping providers.
   - Communication and documentation matter as much as code.

---

## 6. Common Pitfalls

1. **Treating Automotive like a regular phone UI**
   - Overly dense UI, small touch targets, too much text.
   - Not considering driver distraction rules.

2. **Ignoring template constraints (Android Auto)**
   - Trying to hack around templates or simulate custom UI → will fail review.

3. **Overusing vehicle data**
   - Pulling more signals than needed or updating too frequently.
   - Can raise privacy/performance concerns.

4. **Not handling different OEM implementations**
   - Some Car APIs or features may vary by OEM.
   - Need robust fallback logic and feature detection.

5. **Assuming constant network access**
   - Many cars have poor connectivity; need robust offline/path-degraded behavior.

6. **Not testing on real hardware / realistic emulators**
   - Some issues only appear with actual head units or OEM images.

---

## 7. Interview Questions (and how to think about them)

1. **What’s the difference between Android Auto and Android Automotive OS?**
   - Hint: projection vs native OS, phone vs in-car, templates vs full system.

2. **How would you design a media app for Android Auto?**
   - Hint: Car App Library templates, MediaSession, driver distraction limits.

3. **How do apps access vehicle data on AAOS?**
   - Hint: Vehicle HAL → CarService → Car APIs; usually not directly to CAN.

4. **What are some UX considerations when building apps for cars?**
   - Hint: safety, limited interaction, large touch targets, minimal typing.

5. **How would you handle connectivity issues in a navigation or media app in a car?**
   - Hint: caching, graceful offline states, clear user feedback.

6. **What’s your approach to testing automotive apps?**
   - Hint: AAOS emulator / head unit, instrumentation/UI tests, real hardware where possible, different screen sizes/input methods.

7. **How would you collaborate with OEM or platform teams on an automotive project?**
   - Hint: shared contracts for Car APIs, regular syncs, clear documentation, handling different variants.

---

## 8. What You Need to Learn Next (as an Android dev new to Automotive)

If you’re just starting:

1. **Concepts**
   - Be able to explain Android Auto vs AAOS.
   - Understand Vehicle HAL at a high level and how Car APIs sit on top.

2. **Hands-on**
   - Run the Android Automotive emulator from Android Studio.
   - Build a simple media or navigation sample using the Car App Library.

3. **Design & safety**
   - Read the official Android Auto / Automotive design guidelines.
   - Practice turning a phone UI into a safe, simplified car UI.

4. **Deeper topics (for senior roles)**
   - AIDL/Binder basics.
   - How OEM customizations might affect your app.
   - Observability and logging in an automotive environment.

Use this file as your roadmap: start with concepts, then do a small prototype (media or nav), then dive into OEM- and AAOS-specific details as needed for the role you’re targeting.
