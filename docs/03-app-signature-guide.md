# 03 - Android App Signature Guide

App signing is one of the most critical security pillars in the Android ecosystem. An Android device will **refuse** to install or update any APK that is not cryptographically signed.

---

## 1. Why App Signing Exists

1. **Developer Authenticity & Identity**: Proves who authored the application.
2. **Binary Integrity**: Ensures that the APK has not been tampered with or modified since it was built.
3. **Application Updates**: The Android OS only permits an update to replace an existing app if both the installed APK and the new APK share the **identical signature and package name**.
4. **App Sandbox & Permissions**: Shared user IDs (`sharedUserId`) or custom signature-level permissions require matching certificate signatures.

---

## 2. Android Signature Schemes Explained

Over the years, Android evolved through multiple signature schemes to improve security and verification speed:

| Scheme | Introduced | How it works | Characteristics |
|---|---|---|---|
| **v1 (JAR Signing)** | Android 1.0 | Signs individual files inside the ZIP container. Signatures stored in `META-INF/`. | Slow verification; vulnerable to zip metadata manipulation. |
| **v2 (APK Signature)** | Android 7.0 (API 24) | Signs the entire APK binary file (excluding the signature block itself). | Fast verification; completely protects ZIP file integrity. |
| **v3 (Key Rotation)** | Android 9.0 (API 28) | Adds key rotation proof-of-rotation lineage. | Allows developers to rotate compromised or legacy signing keys. |
| **v4 (Streaming)** | Android 11 (API 30) | Stores signature in an external `.apk.idsig` file. | Enables incremental streaming installation over ADB or Google Play. |

In this project's `app/build.gradle.kts`:
```kotlin
create("release") {
    // ...
    enableV1Signing = true
    enableV2Signing = true
    enableV3Signing = true
    enableV4Signing = true
}
```

---

## 3. Keystore Anatomy

A Keystore (typically `.keystore`, `.jks`, or `.p12`) is an encrypted container holding cryptographic keys:

- **Store Type**: Standard format is **PKCS12** (`-storetype PKCS12`).
- **Store Password**: Protects the entire keystore container.
- **Key Alias**: Unique name identifying a specific key pair inside the keystore.
- **Key Password**: Protects the private key for that specific alias.
- **Validity**: Must be valid beyond the expected lifetime of your app (typically 25+ years or 10,000+ days).

---

## 4. Debug vs. Release Keystores

| Aspect | Debug Keystore | Release Keystore |
|---|---|---|
| **Location** | Default path: `~/.android/debug.keystore` | Kept private, stored securely off version control |
| **Password** | Default: `android` | Secret, strong password |
| **Alias** | Default: `androiddebugkey` | Custom (e.g. `todoapp_key`) |
| **Distribution** | Never distribute to users or Play Store | Required for production releases |
| **Permissions** | Some APIs (like Google Pay) disable debug keys | Allowed for production integrations |

---

## 5. Safe Credential Management (The Cardinal Rules)

> [!CAUTION]
> **NEVER commit your production keystore or credentials to Git!**
> If your private key is leaked, malicious actors can distribute compromised updates to your users. If your key is lost without Google Play App Signing, you can never update your app again!

### Recommended Local Setup
1. Copy `keystore.properties.example` to `keystore.properties`:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
2. Note that `keystore.properties` and `*.jks` / `*.keystore` are already included in `.gitignore`.
3. Provide your real values in `keystore.properties`.

### Recommended CI/CD Setup (GitHub Actions / GitLab CI)
In automated build pipelines, store credentials in environment secrets:
- `KEYSTORE_FILE`: Path to downloaded secret or base64-decoded keystore.
- `KEYSTORE_PASSWORD`: Keystore master password.
- `KEY_ALIAS`: Alias name.
- `KEY_PASSWORD`: Key password.

Our `app/build.gradle.kts` automatically checks `keystore.properties` first, then falls back to environment variables.

---

## 6. Hands-On Exercises & Helper Scripts

### Exercise 1: Generate a Keystore
Run the included helper script:
```bash
./scripts/generate-keystore.sh release.keystore todoapp_key 10000
```
This runs:
```bash
keytool -genkeypair \
    -v \
    -keystore release.keystore \
    -alias todoapp_key \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storetype PKCS12
```

### Exercise 2: Extract Certificate Fingerprints (SHA-256 / SHA-1)
Certificate fingerprints are required when registering apps on Google Cloud Console, Firebase, Google Sign-In, or Google Maps:
```bash
keytool -list -v -keystore release.keystore -alias todoapp_key
```

### Exercise 3: Inspect & Verify Signatures
Run the verification helper:
```bash
# To inspect a keystore:
./scripts/verify-signature.sh release.keystore

# To verify an APK:
./scripts/verify-signature.sh app/build/outputs/apk/release/app-release.apk
```
Or directly with Android SDK tools:
```bash
apksigner verify --verbose --print-certs app-release.apk
```

---

## 7. Google Play App Signing

When publishing to Google Play Store:
1. **Upload Key**: You generate this key and use it to sign your App Bundle (`.aab`) before uploading.
2. **App Signing Key**: Google securely stores the actual master signing key in Google's cloud infrastructure. Google verifies your upload key, strips it, and signs the distributed APKs with the master signing key.
3. **Advantage**: If you lose your upload key, Google Play Developer Support can reset it for you without losing the ability to update your app.
