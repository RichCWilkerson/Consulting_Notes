# Local Data Security and Encryption

## Short answer

Android's app sandbox is the baseline that prevents ordinary apps from reading another app's private database. Database encryption is an additional defense for cases such as copied files, forensic extraction, root access, or backup mistakes. Android Keystore protects cryptographic **keys**; a cipher such as SQLCipher protects database **bytes**. Encryption does not protect data after a compromised process can ask the unlocked app to decrypt it.

The decision should come from a threat model, not simply from whether encryption is technically possible.

---

## Is support or oppose sensitive?

Yes. It is a direct political preference tied at least to a device and potentially later to an account. A favorite can also be sensitive because it reveals political interest even when the user has not chosen support or oppose.

Sensitivity does not automatically mean "add SQLCipher today." It means deliberately decide:

- Whether the value needs to be stored at all.
- Whether it leaves the device.
- Whether it is included in backup or device transfer.
- Whether it is logged or sent to analytics.
- How long it is retained and how deletion works.
- Which attacker and failure scenarios encryption is expected to address.

For fictional prototype values, excluding backups and avoiding logs may be proportional. Before real values, the threat model and encryption decision should be completed.

---

## Threat-model questions

1. **What are we protecting?** Public civic records, explicit positions, favorites, account IDs, district data, tokens, or raw addresses?
2. **From whom?** Another normal app, a person holding an unlocked phone, a rooted-device attacker, malware in the process, a backup operator, or a backend administrator?
3. **When is it exposed?** At rest, in memory, in transit, in logs, in backups, or on the server?
4. **What is the impact?** Embarrassment, profiling, discrimination, physical safety, account takeover, or regulatory exposure?
5. **What operational behavior is required?** Multiple devices, restore, logout, key rotation, offline access, and account deletion all affect the solution.

---

## Protection layers

### Android app sandbox

Files in internal app storage are private to the app under normal Android operation. This blocks other ordinary apps and is the default baseline for most application data.

It does not promise protection against every rooted device, platform vulnerability, debugging configuration, exported file, or copied backup.

### Backup and device-transfer exclusion

Excluding a database prevents Android backup/transfer paths from creating another copy with different access and retention behavior. This is especially useful for a replaceable cache.

The tradeoff is that local-only user state is lost after reinstall or device replacement. Once account synchronization exists, restore may come from the authenticated backend instead.

### Android Keystore

Keystore creates or imports cryptographic keys and can keep key material non-exportable; some devices can protect keys in hardware. Keystore is not a general database. Normally it protects a small key that is used by another cipher to protect application data.

Important lifecycle questions include:

- What happens if the key is invalidated or lost?
- Is authentication required every time the key is used?
- How is a key rotated?
- Does logout delete the key and therefore make remaining ciphertext unrecoverable?
- What happens when a database is restored without its device-bound key?

### Whole-database encryption

SQLCipher encrypts SQLite pages, which covers table data plus much of the database's index and structural content. Room can integrate with SQLCipher on Android through an open-helper factory when those versions and database configuration are compatible.

Costs include dependency compatibility, startup/IO overhead, migration from plaintext, secure passphrase handling, key rotation, recovery testing, and platform-specific implementations. A hard-coded passphrase in source code is not meaningful protection because it can be recovered from the app.

### Field-level encryption

The app can encrypt only selected values such as a position. This may keep public civic data easy to query while protecting user state.

The tradeoff is application complexity: encrypted fields cannot be indexed or filtered normally, associated metadata may still reveal behavior, mapping becomes more complex, and every platform needs equivalent authenticated-encryption and key lifecycle behavior. Prefer well-reviewed authenticated encryption APIs; do not invent a cipher.

---

## What encryption does not solve

- Authorization on a backend.
- TLS for data in transit.
- Sensitive values copied into logs, analytics, URLs, or crash reports.
- Screenshots or an unlocked user interface.
- Malware executing with the app's privileges.
- Retention and deletion of server copies.
- Whether the product should collect the data in the first place.

This is why minimization and access control are more fundamental than adding a cipher alone.

---

## A practical decision for a prototype

For deterministic fictional data:

1. Store the data in internal app storage.
2. Exclude the database from cloud backup and device-to-device transfer.
3. Never log or analyze position/favorite values.
4. Permit destructive schema migration only while every user value is disposable.
5. Before real users, define the threat model, retention, deletion, account synchronization, and key recovery requirements.
6. Then compare whole-database encryption with narrowly encrypted user-state fields using the actual Room/KMP versions.

Deferring a dependency is acceptable. Deferring the decision until after sensitive production data exists is not.

## Official resources

- [Android security best practices for storing private data](https://developer.android.com/privacy-and-security/security-tips)
- [Android Keystore system](https://developer.android.com/privacy-and-security/keystore)
- [SQLCipher for Android](https://github.com/sqlcipher/sqlcipher-android)
- [Android backup controls](https://developer.android.com/identity/data/autobackup)
