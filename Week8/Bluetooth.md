# Bluetooth on Android – Senior Android Dev Notes

## 1. Big Picture

Bluetooth on Android = **short‑range wireless communication** between your app/device and nearby devices:

- **Classic Bluetooth** (BR/EDR)
  - Higher throughput, designed for **continuous streams** (audio, serial data).
  - Used by: headphones, audio systems, some older OBD dongles, serial adapters.
- **Bluetooth Low Energy (BLE)**
  - Optimized for **low power, small bursts of data**.
  - Used by: sensors, beacons, smart locks, medical devices, vehicle telematics, routers with BLE provisioning.

As an Android dev, your main responsibilities:
- Discover devices.
- Connect and pair when needed.
- Exchange data (read/write) in a safe, user‑respectful way.
- Respect **permissions, privacy, power, and background limits**.

Use cases similar to what you mentioned:
- **Vehicles**: OBD dongle, telematics box, or head unit that exposes data over Classic or BLE.
- **Routers (e.g., Starlink)**: initial provisioning / configuration via BLE (SSID/password, firmware status, etc.).

---

## 2. Key Bluetooth Concepts (Android‑Relevant)

### 2.1 Adapter, Devices, and Profiles

- **BluetoothAdapter**
  - Entry point to the local Bluetooth radio.
  - From API 31+ you usually access it via `BluetoothManager`.

- **BluetoothDevice**
  - Represents a **remote device** (phone, car, router, sensor).
  - Identified by a **MAC address** (or randomized address for privacy in BLE).

- **Profiles** (Classic only)
  - Define **use cases** and protocols built on top of Bluetooth:
    - A2DP: audio streaming.
    - HFP: hands‑free calling.
    - HID: keyboards/mice.
    - SPP: Serial Port Profile (like a virtual COM port).
      - TODO: used for what? 
  - In many app use‑cases (vehicles/routers) you won’t directly manage profiles, you’ll handle streams or use BLE.

### 2.2 BLE GATT

Most modern IoT / router / vehicle accessories use **BLE GATT**:

- **GATT (Generic Attribute Profile)**
  - Defines how data is structured and accessed in BLE.
  - Communication is organized as:
    - **Services** → groups of functionality (e.g., Device Info Service, Battery Service, Custom Vehicle Service).
    - **Characteristics** → individual data points (speed, temperature, VIN, Wi‑Fi SSID, etc.).
    - **Descriptors** → metadata for characteristics (e.g., Client Characteristic Configuration for notifications).

- Typical operations:
  - Discover services/characteristics.
  - **Read** characteristic.
  - **Write** characteristic (with/without response).
  - **Subscribe to notifications/indications** to get async updates.

---

## 3. Permissions, Capabilities, and Requirements

### 3.1 Manifest Permissions (Modern Android)

Android 12+ split Bluetooth permissions:
> NOTE: Permissions go in `AndroidManifest.xml` and must be requested at runtime.
> `<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />`

- **`BLUETOOTH_SCAN`**
  - Required to discover/scan for devices.
  - Often needs **approximate or precise location** if you want to see non‑your‑own devices, because scanning can reveal location.
  - TODO: so do you need location permission too? what is required here?

- **`BLUETOOTH_CONNECT`**
  - Required to connect to already known / paired devices, or manage connections.

- **`BLUETOOTH_ADVERTISE`**
  - Required if your app acts as a BLE **peripheral** and advertises.
    - peripheral in this context = device that other devices connect to (e.g., a sensor broadcasting data).
    - advertiser = device that scans and connects to peripherals.

- **`BLUETOOTH` / `BLUETOOTH_ADMIN`** (legacy)
  - Pre‑Android 12; now largely replaced by above runtime permissions.

**Best practice:**
- Request only what you need, and only at the time you need it.
- Clearly explain **why**: e.g., “We scan for nearby vehicle modules so you can connect and view diagnostics.”

### 3.2 Other Requirements

- **Location**: on some versions, BLE scanning requires location permission & location being ON.
- **Bluetooth state**: make sure Bluetooth is enabled or prompt the user to enable.
- **Hardware features**:
  - `uses-feature android:name="android.hardware.bluetooth_le"` for BLE.

---

## 4. Classic Bluetooth vs BLE – When to Use What

### Classic Bluetooth
- Good for:
  - **Audio streaming**, continuous data streams, virtual serial ports.
  - Older accessories (legacy OBD readers, some car head units).
- Dev experience:
  - Use `BluetoothSocket` for RFCOMM (like TCP over Bluetooth).
  - Pairing often needed up front.

### BLE (Bluetooth Low Energy)
- Good for:
  - **Sensors and control** use cases: periodic telemetry from a vehicle, controlling a router, reading/writing configs.
  - Battery‑powered devices.
- Dev experience:
  - Work with **GATT**: services, characteristics, descriptors.
  - Data is usually small binary blobs.

**For vehicles / routers:**
- Newer devices likely expose **BLE services**.
- You’ll discover specific services (defined by the vendor) and then read/write characteristics to:
  - Get telemetry (speed, voltage, temp).
  - Configure device (Wi‑Fi SSID/password, operating mode).

**Interview answer framing:**
> For streaming audio or high‑throughput continuous data I’d use Classic Bluetooth. 
> For modern IoT‑style devices where power and small control messages matter, I’d use BLE and talk to the device via GATT services/characteristics.

---

## 5. Basic BLE Flow on Android

### 5.1 High‑Level Steps

1. Get `BluetoothManager` / `BluetoothAdapter`.
2. Check & request required permissions.
3. Start **BLE scan** to find devices.
4. Filter for your target device (by name, service UUID, manufacturer data, etc.).
5. Connect to the device → get a `BluetoothGatt` instance.
6. Discover services.
7. Read/write characteristics, subscribe to notifications.
8. Close GATT when done.

### 5.2 Code Sketch (Conceptual)

```kotlin
// BluetoothVehicleClient.kt
class BluetoothVehicleClient(
    private val context: Context,
    private val serviceUuid: UUID,
    private val charUuid: UUID,
) {

    private val bluetoothManager: BluetoothManager =
        context.getSystemService(BluetoothManager::class.java)

    private val adapter: BluetoothAdapter? = bluetoothManager.adapter

    private var scanner: BluetoothLeScanner? = null
    private var currentGatt: BluetoothGatt? = null

    private val _data = MutableStateFlow<ByteArray?>(null)
    val data: StateFlow<ByteArray?> = _data

    fun startScan() {
        val adapter = adapter ?: return
        if (!adapter.isEnabled) return

        scanner = adapter.bluetoothLeScanner
        scanner?.startScan(
            listOf(
                ScanFilter.Builder()
                    .setServiceUuid(ParcelUuid(serviceUuid))
                    .build()
            ),
            ScanSettings.Builder().build(),
            scanCallback
        )
    }

    fun stopScan() {
        scanner?.stopScan(scanCallback)
    }

    fun disconnect() {
        currentGatt?.close()
        currentGatt = null
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            stopScan()
            connectToDevice(device)
        }
    }

    private fun connectToDevice(device: BluetoothDevice) {
        currentGatt = device.connectGatt(
            context,
            /* autoConnect = */ false,
            gattCallback
        )
    }

    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                gatt.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                gatt.close()
                if (currentGatt === gatt) currentGatt = null
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            val service = gatt.getService(serviceUuid) ?: return
            val characteristic = service.getCharacteristic(charUuid) ?: return
            gatt.readCharacteristic(characteristic)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (characteristic.uuid == charUuid && status == BluetoothGatt.GATT_SUCCESS) {
                _data.value = characteristic.value
            }
        }
    }
}
```

```kotlin
// Activity.kt 
class VehicleActivity : ComponentActivity() {

    // declare the bluetooth client 
    private lateinit var client: BluetoothVehicleClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Bluetooth client with service/char UUIDs
        // serviceUuid and charUuid would be defined by the vehicle module/router
        client = BluetoothVehicleClient(
            context = this,
            serviceUuid = UUID.fromString("0000xxxx-0000-1000-8000-00805f9b34fb"),
            charUuid = UUID.fromString("0000yyyy-0000-1000-8000-00805f9b34fb")
        )

        setConent {
            VehicleScreen(
                onPermissionGranted = {
                    // get permission inside composable, then start scan
                    // we pass in the callback to start scan after permission is granted
                    client.startScan()
                }
            )
        }
        
        // use a lifecycle scope to collect data updates which will also handle lifecycle properly -> no leaks
        lifecycleScope.launch {
            client.data.collectLatest { bytes ->
                // Update UI with parsed data
                // type of data depends on characteristic definition 
                // e.g., parse bytes to speed, temperature, etc.
                // update a ViewModel or state holder accordingly to any UI composables can observe
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        client.disconnect()
    }
}
```


You don’t need to memorize this; just understand the **sequence**: scan → connect → discover services → interact with characteristics.

```kotlin
// VehicleScreen.kt
@Composable
fun VehicleScreen(
    onPermissionsGranted: () -> Unit
) {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            onPermissionsGranted()
        } else {
            // show error UI
        }
    }
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        )
    }

    // rest of your UI
}
```


---

## 6. Working with Vehicles & Routers – Design Considerations

### 6.1 Vehicles (OBD / Telematics / Head Units)

Typical patterns:
- Device exposes:
  - **Classic SPP**: you open an RFCOMM socket and speak a custom or standard protocol (e.g., OBD‑II). 
    - SPP (Serial Port Profile) = virtual serial port over Bluetooth.
    - RFCOMM (Radio Frequency Communication) = is like TCP over Bluetooth.
    - OBD-II (On-Board Diagnostics) = standard for vehicle diagnostics.
  - Or **BLE GATT**: services for telemetry, diagnostics, configuration.
    - GATT (Generic Attribute Profile) = defines how data is structured and accessed in BLE.

Considerations:
- **Safety & UX**
  - Avoid complex flows while driving; minimal interactions.
  - Handle connection loss gracefully (e.g., when the car is turned off).
- **Power**
  - Vehicle modules may power down when ignition is off; reconnection logic must handle this.
- **Security**
  - Sometimes pairing with a PIN or secure handshake.
  - Ensure that only authorized users can send control commands (e.g., start engine, unlock doors).

### 6.2 Routers / Network Devices (e.g., Starlink‑like)

Use cases:
- Initial **onboarding/provisioning** of a router/AP via BLE:
    - AP (Access Point) = device that provides network access (e.g., Wi-Fi router).
  - App discovers the router via BLE.
  - App sends Wi‑Fi SSID/password and maybe user account info.
    - SSID (Service Set Identifier) = Wi-Fi network name.
  - Router connects to the internet and then may expose HTTP/gRPC APIs over LAN.

Considerations:
- **Ephemeral connection**
  - BLE used only during provisioning; after that, communication switches to Wi‑Fi.
- **Security**
  - Use secure pairing or pre‑provisioned keys.
    - secure pairing = process of establishing a trusted connection between devices (e.g., using PINs or passkeys).
  - Don’t send Wi‑Fi credentials in plain text without at least some secure channel.
    - TODO: how to do this over BLE?
- **Reliability**
  - Provisioning should be robust to dropped connections; use retries and clear progress states.
    - TODO: what does provisioning mean here exactly? what does robustness mean in this context?
    - TODO: what are progress states?

---

## 7. Background, Power, and OS Limitations

Modern Android is strict about background work and power usage:

- **Scanning limits**
  - Background BLE scans are rate‑limited and may be batched.
  - Foreground service is often required for long‑running scans.

- **Connections**
  - Long‑lived connections in the background can be killed; design for reconnect.

- **Best practices**
  - Use foreground services and notifications for critical Bluetooth interactions (e.g., continuous telemetry while app appears active to user).
  - Stop scanning/close GATT when not needed.
  - Batch operations where possible.

---

## 8. Data Modeling & Protocol Design

When you control both the mobile app and the accessory (vehicle module/router):

- **Define clear GATT services/characteristics**
  - E.g., `VehicleService` with `SPEED`, `RPM`, `VIN` characteristics.
  - `ConfigService` with `WIFI_SSID`, `WIFI_PASSWORD`, `FIRMWARE_VERSION`.
    - TODO: `ConfigService` is used where? 

- **Binary vs text**
  - BLE characteristics carry **bytes**.
  - You choose encoding:
    - JSON (simple but bigger and slower).
    - Packed binary (compact, more complex to parse).
      - TODO: is this decided on the car side? like the API/service?
      - TODO: are there libraries to use for Packed binary? like Gson for JSON? what makes it more complex to parse?

- **Versioning**
  - Include version fields so app and device can negotiate capabilities.
    - TODO: what are version fields exactly? where are they included?
    - TODO: how does negotiation work in this context?

- **Error handling**
  - Define error codes and status characteristics or notification messages.
    - TODO: are these status codes similar to HTTP status codes? how are they defined?

This is similar to designing a REST/gRPC API, just more constrained.
- TODO: what are the constraints exactly? how is it similar to REST/gRPC API design?
- TODO: are there libraries like Retrofit/Ktor for designing these protocols?

---

## 9. Security & Privacy

Key concerns:
- **Pairing & bonding**
  - Pairing establishes a trusted relationship; bonded devices can reconnect more easily and securely.
  - TODO: what is bonding exactly? how is it different from pairing?
  - TODO: is this like the remembered devices for bluetooth on my phone?
- **Authentication & authorization**
  - Don’t rely solely on pairing if the action is sensitive (e.g., vehicle control, router admin).
    - TODO: is bonding the other option? Are we saying it should be a hard connection (wired)?
  - Consider an app‑level auth layer (tokens, user login) on top of Bluetooth.
    - TODO: like OAuth or JWT tokens? does this need to be set up on the car/router side too?
- **Data protection**
  - Be cautious with PII or credentials sent over Bluetooth.
  - Use encryption provided by BLE pairing or an application‑level crypto protocol.
    - TODO: what are some crypto protocols suitable for this context?
- **Privacy**
  - Scanning can reveal nearby devices; comply with Android’s permission model and be transparent with users.
    - TODO: what are some best practices for transparency here?

Interview‑friendly line:
> For sensitive actions over Bluetooth—like configuring a router or sending commands to a vehicle—I wouldn’t rely exclusively on pairing. 
> I’d combine BLE security with app‑level authentication and clear UX so the user knows what’s happening.

---

## 10. Common Pitfalls for Android Bluetooth

- **Not handling permissions correctly**
  - Forgetting `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` on Android 12+.
  - Assuming old `BLUETOOTH_ADMIN` is enough.

- **Ignoring location requirement for BLE scanning**
  - Scans fail or return nothing; app looks “broken”.
  - Need to request location permission and ensure location services are ON.

- **Doing heavy work on the main thread**
  - Callbacks doing parsing/work on main → UI jank.
  - use coroutines or background threads.

- **Not cleaning up**
  - Leaving GATT connections open.
  - Forgetting to stop scans.

- **Poor reconnection strategy**
  - App silently fails when the device goes out of range or powers off.

- **Hardcoding MAC addresses**
  - Users may have multiple devices, or devices may randomize addresses.
  - Better: filter by advertised name or service UUID.

- **Assuming all devices behave correctly**
  - Many BLE firmwares are buggy; timeouts and retries are essential.

---

## 11. Testing Bluetooth Features

- **Real devices**
  - There’s no full emulator for Bluetooth; always validate on actual hardware.

- **Fake peripherals**
  - Use dedicated hardware simulators or other phones acting as BLE peripherals.

- **Logging & tools**
  - Log GATT operations and responses.
  - Use tools like nRF Connect or LightBlue on another phone to inspect services/characteristics.

- **Automated tests**
  - Abstract your Bluetooth layer behind interfaces; unit test logic that parses/produces byte arrays.
  - Use fakes/mocks for `BluetoothGatt` in instrumentation tests.

---

## 12. Interview Questions & Succinct Answers

**1. Explain Classic Bluetooth vs BLE on Android.**
> Classic Bluetooth is optimized for continuous, higher‑throughput streams like audio; 
> you typically work with sockets and profiles like A2DP or SPP. 
> BLE is optimized for low‑power, small messages and uses GATT services and characteristics. 
> For sensors and provisioning devices like routers or vehicle modules I’d choose BLE; for streaming audio I’d use Classic.

**2. How do you connect to a BLE device and read data?**
> I start with `BluetoothAdapter` and a BLE scan using `BluetoothLeScanner`, filter for my target device, then call `connectGatt` to get a `BluetoothGatt`. 
> In the `BluetoothGattCallback`, once connected I call `discoverServices()`, then look up the service and characteristic by UUID and use `readCharacteristic` or `writeCharacteristic`, and enable notifications if I need streaming updates.

**3. What permissions are needed for Bluetooth on Android 12+?**
> For discovery I request `BLUETOOTH_SCAN` and usually location because scanning can infer location. 
> For connecting to devices I need `BLUETOOTH_CONNECT`. If my app advertises BLE, I also need `BLUETOOTH_ADVERTISE`. 
> I only request them when needed and explain the reason to the user.

**4. How would you design BLE communication with a vehicle module or router?**
> I’d define one or more GATT services—for example a telemetry service and a config service—with well‑documented characteristics. 
> The app discovers the device, connects, and then reads telemetry characteristics or writes configuration like Wi‑Fi credentials. 
> I’d version the protocol, handle retries/timeouts, and layer app‑level authentication or pairing to secure sensitive actions.

**5. What are common pitfalls with Bluetooth on Android and how do you avoid them?**
> Permissions and OS version differences are a big one, so I gate behavior based on API level and handle runtime permissions explicitly. 
> Another is not closing GATT or stopping scans, which wastes battery. I also build robust reconnection logic because devices go out of range or power off. 
> Finally, I test against real hardware and expect flaky behavior from some BLE firmwares.

---

## 13. Quick Mental Checklist When Starting a Bluetooth Feature

- [ ] Do I know whether the device uses **Classic** or **BLE**?
- [ ] Do I know the **service/characteristic UUIDs** or profile I need to talk to?
- [ ] Have I declared and requested the right **permissions** for the target API levels?
- [ ] Do I have a plan for **connection lifecycle** (connect, disconnect, reconnect)?
- [ ] How will I handle **errors, timeouts, and retries**?
- [ ] What are the **security** implications (credentials, vehicle control, etc.)?
- [ ] How will I **test** this on real hardware and simulate edge cases?

