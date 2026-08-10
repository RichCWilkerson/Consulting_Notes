# Biometric Authentication with Android Keystore and a Backend

`BiometricPrompt` does not return a fingerprint, face scan, biometric token, private key, or backend access token. It only confirms that Android authorized a protected cryptographic operation.

For a backend-verifiable biometric step-up flow, the app creates a device key pair, registers the **public** key with the backend, and later uses biometric approval to sign a one-time server challenge with the non-exportable **private** key.

Related:
- [`5_Auth_Sessions.md`](./5_Auth_Sessions.md)
- [`4_Storage_Secrets.md`](./4_Storage_Secrets.md)

---

## What exists where

| Value | Created by | Stored / used by | Sent to backend? |
|---|---|---|---|
| Biometric template | Android system / secure hardware | Never exposed to the app | No |
| Private key | App, through Android Keystore | Android Keystore; non-exportable | Never |
| Public key | App, as part of the key pair | Backend device registration record | Yes, once per key |
| Challenge | Backend | App memory until signed; backend tracks it | Backend sends it to app |
| Signature | App after biometric approval | Backend verifies it | Yes, for every challenge |
| Access / refresh token | Backend | App session storage | Yes, after verification |

The backend does **not** send a key that the app uses to encode or decode data. This pattern uses an asymmetric key pair and a digital signature:

- the app signs with its private key;
- the backend verifies with the corresponding public key.

---

## Flow

### 1. Register a device key (once per device or after key invalidation)

1. User completes the normal login flow.
2. The app creates an EC key pair in Android Keystore.
   - **EC** means *elliptic curve*: a family of modern public-key cryptography algorithms. It gives strong security with relatively small keys and signatures.
3. The app sends the encoded public key and a device key ID to the backend over HTTPS.
4. The backend associates that public key with the authenticated user and device-key record.

The normal authenticated session authorizes this initial registration. The server should allow key replacement only through an authenticated, auditable flow.

### 2. Prove user presence for a sensitive action (every time)

1. The app asks the backend for a random, short-lived, one-time challenge.
2. The app initializes a `Signature` with the Keystore private key.
3. `BiometricPrompt` authorizes use of that signature.
4. The app signs the exact challenge bytes.
5. The app sends the key ID, challenge ID, and signature to the backend.
6. The backend looks up the public key, verifies the signature, consumes the challenge, and then performs the action or returns a normal session token.

Use a new challenge for every operation. A signed value is only meaningful if the backend rejects expired or previously consumed challenges.

---

## Android example

This example uses an EC P-256 key and `SHA256withECDSA`. 
The `setUserAuthenticationParameters` API requires Android 11 (API 30) or later. 
The server must be configured to accept the same algorithm and public-key encoding.

```kotlin
import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.PrivateKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec
import java.util.Base64

// "AndroidKeyStore" is the name of Android's secure key-storage provider.
// The alias is this app's local label for one specific key pair. It is not the key itself.
// Think of a keystore as a cabinet that can hold many keys. The alias is the label on one
// drawer, such as "biometric-signing-key-v1". Later, the app gives Android this label to
// retrieve that exact key; the label is local to this app/device and is not sent to the backend.
private const val ANDROID_KEY_STORE = "AndroidKeyStore"
private const val KEY_ALIAS = "biometric-signing-key-v1"

// @RequiresApi is an Android annotation
// It documents and lets tooling check that this function requires API 30 (Android 11, code name R)
// because setUserAuthenticationParameters was added in that version.
// It is not itself a runtime `if (SDK_INT >= 30)` check. Callers still need that runtime check
// when the app supports older Android versions. API numbers and code-name constants refer to the
// same platform release: API 30 is R (Android 11), and API 33 is TIRAMISU (Android 13).
//
// A KeyPair contains two mathematically related keys:
// - private: stays in Keystore and creates signatures;
//   A KeyPair on its own is just a Java object; it is not automatically stored anywhere.
//   This particular pair is stored in Android Keystore because the generator uses the
//   "AndroidKeyStore" provider. Keystore can hold many entries, including key pairs,
//   symmetric AES keys, and certificate chains.
// - public: can be shared with the backend, which verifies those signatures.
//   They are created together by the algorithm's mathematics. The public key is derived from
//   the private key, and a signature made with the private key can be verified only with its
//   matching public key. Neither key has to "look up" the other at signing time.
//
// A generator creates the pair. The specification tells Android how to create and protect it:
// its alias, allowed operations, cryptographic algorithm, and biometric requirement.
// - Alias: lets this app retrieve the correct key from a keystore containing many keys.
// - Allowed operations: prevents accidental or malicious use for a different purpose.
// - Cryptographic algorithm: defines the key type and what the backend must use to verify it.
// - Biometric requirement: makes Android refuse private-key use until the user authenticates.
@RequiresApi(Build.VERSION_CODES.R)
fun createBiometricSigningKey(): KeyPair {
    // KeyPairGenerator is the Java security API that creates public/private key pairs.
    // KEY_ALGORITHM_EC selects elliptic-curve keys. The second argument asks Android to
    // store the private key in Android Keystore rather than returning exportable key material.
    // "Exportable key material" means raw key bytes that code could copy, save, or transmit,
    // such as a private key's `encoded` value. Android Keystore keeps this private key
    // non-exportable: the app can ask it to sign, but cannot read the private-key bytes.
    // RSA is another common asymmetric algorithm, but its keys and signatures are larger.
        // larger keys and signatures are slower to compute and transmit, so EC is preferred when supported.
    // getInstance returns a configured generator object; do not assume it is a singleton or
    // thread-safe. Create, configure, and use it within one operation as this function does.
    val generator = KeyPairGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_EC,
        // This string is the registered name of a platform security provider, not an arbitrary
        // app-defined value. Android recognizes "AndroidKeyStore" and routes key operations to it.
        // The constant simply avoids repeating or misspelling that registered provider name.
        ANDROID_KEY_STORE,
    )

    val spec = KeyGenParameterSpec.Builder(
        KEY_ALIAS,
        // SIGN allows this private key to create signatures; VERIFY permits verification with
        // the matching key. Other common purposes are ENCRYPT and DECRYPT. A key should be given
        // only the purposes it truly needs; this signing key does not need encryption/decryption.
        KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY,
    )
        // The ECGenParameterSpec("secp256r1") tells Android to create a P-256 elliptic curve key.
        .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
        // SHA-256 is the hash function used in the signature algorithm. It is not a password or PIN.
        .setDigests(KeyProperties.DIGEST_SHA256)
        // Require a strong biometric approval for every private-key use.
        .setUserAuthenticationParameters(
            // The timeout is 0, which means the user must authenticate for every operation.
            // A non-zero value (seconds) creates a short window after authentication in which
            // the key can be reused without another prompt. That improves convenience but means
            // more than one operation may be approved by a single biometric check.
            0,
            // Strong biometrics meet Android's higher security bar. Device credential means the
            // device PIN, pattern, or password and may be chosen for accessibility, availability,
            // or product-policy reasons. BIOMETRIC_WEAK is available for lower-risk prompt UX,
            // but it cannot be used to authorize this Keystore key; this API accepts strong
            // biometrics and/or device credentials. Choose the authenticator based on the risk
            // of the action, and design any credential fallback explicitly.
            KeyProperties.AUTH_BIOMETRIC_STRONG,
        )
        // If a person adds or removes a fingerprint/face profile in Android Settings, invalidate
        // this key. The app must then make and register a new key pair before signing again.
        .setInvalidatedByBiometricEnrollment(true)
        .build()

    generator.initialize(spec)
    return generator.generateKeyPair()
}

fun loadPrivateKey(): PrivateKey {
    // Yes: retrieve the entry with this alias. One keystore can have many keys, so a well-named
    // alias identifies the key for this feature and version. Store the alias in app code or
    // configuration; do not guess it or select an arbitrary key.
    // `apply` is a Kotlin scope function: it runs `load(null)` on the new KeyStore, then returns
    // that same KeyStore object. Java's general KeyStore API can load a file with a password, but
    // AndroidKeyStore is system-managed rather than an app-owned file, so it uses null here.
    val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
    return keyStore.getKey(KEY_ALIAS, null) as PrivateKey
}

// Base64 is not encryption. It is a text-safe way to put raw public-key bytes into JSON.
// Raw bytes may contain non-printing values, quotes, or invalid UTF-8, which can be corrupted or
// rejected by JSON, HTTP tooling, and logs. Base64 converts every group of bytes into ordinary
// ASCII characters. Sending a JSON list of numbers is possible but larger and less conventional;
// treating arbitrary bytes as a String risks changing them.
// That is OK: public keys are meant to be shared. HTTPS/TLS encrypts the network request.
fun encodedPublicKey(keyPair: KeyPair): String =
    Base64.getEncoder().encodeToString(keyPair.public.encoded)

// A server challenge is random binary data, so ByteArray preserves its exact values. A String is
// text and requires a character encoding such as UTF-8; use a String only if the server defines
// an exact text-to-bytes conversion. Signature is a Java object representing one in-progress
// signing operation: it holds the selected algorithm, references the Keystore private key, and
// receives the challenge bytes. It lives in app memory, not in Keystore.
// After biometric success, signature.sign() produces the actual signature bytes. This example
// Base64-encodes them, sends them to the backend, and does not persist them locally.
fun signatureFor(challenge: ByteArray): Signature =
    // This must match the EC key, its allowed SHA-256 digest, and the algorithm the backend uses
    // to verify the registered public key. "SHA256withECDSA" hashes the challenge with SHA-256,
    // then signs that hash with ECDSA.
    Signature.getInstance("SHA256withECDSA").apply {
        // This prepares the signing operation. BiometricPrompt authorizes its use.
        initSign(loadPrivateKey())
        update(challenge)
    }
```

The public key is X.509-encoded by `keyPair.public.encoded`. 
It is safe to send to the backend; it cannot sign challenges.

### Registering the public key

```kotlin
data class DeviceKeyRegistration(
    val keyId: String,
    val publicKeyBase64: String,
    val algorithm: String,
)

val keyPair = createBiometricSigningKey()

val registration = DeviceKeyRegistration(
    keyId = "server-issued-or-app-generated-key-id",
    publicKeyBase64 = encodedPublicKey(keyPair),
    algorithm = "ES256",
)

// POST /devices/keys, authenticated with the user's normal access token.
// The backend stores the public key, key ID, user association, and key status.
```

Do not use a stable hardware identifier as `keyId`. 
For example, an IMEI, device serial number, MAC address, or other identifier intended to remain tied to the physical device. Android restricts access to many of these identifiers, and apps should not use them as an easy substitute for a device key ID.
A random server-issued or app-generated identifier is preferable.
A random ID is only a lookup label for this registered key. It can be replaced, revoked, or deleted without revealing a cross-app/device identifier. A stable hardware ID can enable tracking across accounts or services, creates privacy and compliance risk, and remains sensitive if your database is breached.


### Signing a server challenge after biometric approval

```kotlin
fun requestBiometricSignature(
    context: Context,
    challenge: ByteArray,
    onSigned: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val prompt = BiometricPrompt(
        context as androidx.fragment.app.FragmentActivity,
        ContextCompat.getMainExecutor(context),
        // This is an anonymous object that extends AuthenticationCallback.
        // It is not a higher-order function. Android holds this callback object, then calls
        // its methods later after the user succeeds, cancels, or encounters an error.
        // onSigned and onError above *are* higher-order-function parameters: callers pass
        // functions that this function invokes with the result.
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(
                result: BiometricPrompt.AuthenticationResult,
            ) {
                val signature = result.cryptoObject?.signature
                    ?: return onError("No authorized signature was returned.")

                val signatureBase64 = Base64.getEncoder()
                    .encodeToString(signature.sign())

                // Send this signature, not the private key and not biometric data.
                onSigned(signatureBase64)
            }

            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                onError(errString.toString())
            }
        },
    )

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Confirm with biometrics")
        .setSubtitle("Approve this sensitive action")
        // A CryptoObject-backed operation requires a strong biometric authenticator.
        .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
        .build()

    prompt.authenticate(
        promptInfo,
        BiometricPrompt.CryptoObject(signatureFor(challenge)),
    )
}
```

In production, pass a `FragmentActivity` or `Fragment` directly instead of casting `Context`.
`BiometricPrompt` needs an Activity or Fragment so Android can attach the prompt to the screen lifecycle. A generic `Context` might be an application context or another type; the cast would then crash at runtime. Accepting `FragmentActivity` or `Fragment` in the function signature makes the requirement explicit and type-safe.
Before showing the prompt, check `BiometricManager.canAuthenticate(BIOMETRIC_STRONG)` and provide a secure fallback when biometrics are unavailable.
A fallback might be a device PIN/pattern/password, a passkey, a normal account reauthentication, or another MFA method. The correct choice depends on the action's risk and product policy. Do not silently skip the required verification merely because biometrics are unavailable.

### Sending the verification request

```kotlin
data class ChallengeSignatureRequest(
    val keyId: String,
    val challengeId: String,
    val signatureBase64: String,
)

// POST /sensitive-actions/verify
// {
//   "keyId": "...",
//   "challengeId": "...",
//   "signatureBase64": "..."
// }
```

The challenge value itself need not be returned when the backend retains it by `challengeId`;
the backend can use `challengeId` to load the original bytes it generated and verify the signature against those bytes. Sending the same challenge back adds no information.
returning it is acceptable only if the backend compares it to the original server-issued value.
This can be useful when a backend is stateless or when the client must echo the challenge for protocol reasons. The backend must compare it to its original value (or a securely signed server-issued representation) before verification. It must never verify a signature against an arbitrary client-supplied challenge, because then the signed proof would not be bound to the server's intended one-time action.

---

## Backend responsibilities

On registration, store:

- authenticated user ID;
- key ID;
- public key and algorithm;
- created time, last-used time, and active/revoked status;
- optional key-attestation evidence, if your risk model needs it.

On verification:

1. Load the active public key for the authenticated user and key ID.
2. Confirm the challenge exists, belongs to that user/action, has not expired, and is unused.
3. Verify the ECDSA signature against the exact original challenge bytes.
   - **ECDSA** is the *Elliptic Curve Digital Signature Algorithm*. It uses the EC key pair created above: the device signs with the private key, and the backend verifies with the public key.
   - **RSA** is an older, widely supported public-key algorithm. RSA and ECDSA can both sign data, but ECDSA typically uses smaller keys and produces smaller signatures for comparable security. They are not interchangeable: the backend must verify with the algorithm used when the key was registered.

> Cryptography operates on **bytes**: raw numeric values such as `[12, 245, 7]`. 
> A **String** is human-readable text such as `"approve payment"`. 
> Text must be converted to bytes using a known character encoding (usually UTF-8), and different conversions can produce different bytes. 
> A server challenge is normally random binary data, so the app signs those exact bytes. 
> If it arrives in JSON, it is usually Base64-encoded for transport and decoded back to the original bytes before signing.

4. Mark the challenge consumed atomically.
   - This is **not automatic**. The backend stores each issued challenge with a status such as `unused`, then changes it to `consumed` only after a successful verification. “Atomically” means verification and consumption are performed as one database-safe operation, so two simultaneous requests cannot both use the same challenge. Any later use is rejected as a replay.
5. Authorize the specific action or issue the normal short-lived access token.

Do not treat a valid signature as permanent authentication. It only proves that the registered device key signed this particular challenge after the configured local authentication requirement.

---

## Important boundaries and failure cases

- **No biometric data leaves the device.** The app receives a success/failure callback, not a biometric value.
- **Private keys never leave Keystore.** Do not serialize them, log them, or send them to an API.
- **This is not encryption.** Signature verification proves possession of the protected private key.
  - Use TLS for transport encryption and separate encryption keys when data-at-rest encryption is needed.
  - **Data at rest** is data that remains on the device after the app closes: for example, a refresh token, an offline database containing PII/PHI, or downloaded documents. Encrypt it when it must be persisted and exposure would harm the user or business. Do not persist sensitive data when it can be fetched again; then there is nothing local to encrypt. This signing private key is already protected by Keystore and should never be copied into app storage.
- **Biometric enrollment can invalidate the key.** With `setInvalidatedByBiometricEnrollment(true)`, handle `KeyPermanentlyInvalidatedException` by deleting the old key, requiring normal account authentication, and registering a new public key.
  - Yes. This refers to a device-level change unrelated to your app, such as adding or removing a fingerprint or face profile in Android Settings. With this option enabled, Android intentionally makes the old private key unusable because the set of people who can unlock it changed. Registration is normally one time **per key**, not forever: the user does not necessarily need to be logged out, but the app cannot use the old key for challenge signing. For a sensitive flow, require normal account authentication, create a new key pair, and register its new public key.
- **Server verification is mandatory.** A local biometric success callback alone must not authorize a backend-sensitive operation.
  - **Local verification** means Android has accepted the fingerprint/face and calls `onAuthenticationSucceeded`. That result exists only inside the app, which can be tampered with on a compromised device. **Server verification** means the backend receives the signature and proves, using the stored public key and its one-time challenge, that the device key actually signed this request. The server must make the final authorization decision because it owns the protected data and action.
- **Challenge scope matters.** Bind challenges to the intended user, action, amount/resource, and expiry so a signature for one action cannot authorize another.
  - Example: for a $500 transfer to account `A`, the backend creates a challenge tied to user `U`, action `transfer`, amount `500`, recipient `A`, and a 60-second expiry. Even if the signature is intercepted, it cannot be reused for a $5,000 transfer, a different recipient, or an account-settings change because the backend verifies the stored scope before authorizing.
- **Device credential is a separate choice.** CryptoObject-backed signing is designed for strong biometric approval. If product requirements allow PIN/pattern/password fallback, define whether that fallback can authorize the same backend action and implement it as a separately reviewed flow.
