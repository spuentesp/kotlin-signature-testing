import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

// -------------------------------------------------------------------------------------------------
// APP SIGNATURE CONFIGURATION
//
// In Android, release builds must be signed with a cryptographic private key.
// Never commit production keystores (*.jks) or credentials into version control!
// This configuration loads credentials from:
//   1. Local 'keystore.properties' file (for local builds)
//   2. Environment variables (for CI/CD pipelines like GitHub Actions)
// -------------------------------------------------------------------------------------------------
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        load(FileInputStream(keystorePropertiesFile))
    }
}

fun getKeystoreProperty(key: String, envVar: String): String? {
    return keystoreProperties.getProperty(key) ?: System.getenv(envVar)
}

android {
    namespace = "com.antigravity.todoapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.antigravity.todoapp"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        // Default debug signing configuration:
        // Automatically created by Android Studio / Gradle with default androiddebugkey
        getByName("debug") {
            // Android uses default ~/.android/debug.keystore if not specified
        }

        // Production release signing configuration:
        create("release") {
            val storeFilePath = getKeystoreProperty("storeFile", "KEYSTORE_FILE")
            val keystoreTarget = if (storeFilePath != null) {
                val f = file(storeFilePath)
                if (f.exists()) f else rootProject.file(storeFilePath)
            } else null

            if (keystoreTarget != null && keystoreTarget.exists()) {
                storeFile = keystoreTarget
                storePassword = getKeystoreProperty("storePassword", "KEYSTORE_PASSWORD")
                keyAlias = getKeystoreProperty("keyAlias", "KEY_ALIAS")
                keyPassword = getKeystoreProperty("keyPassword", "KEY_PASSWORD")

                // Signature Scheme flags (v1: JAR, v2: APK Signature Scheme v2, v3: Key rotation, v4: Streaming)
                enableV1Signing = true
                enableV2Signing = true
                enableV3Signing = true
                enableV4Signing = true
            } else {
                println("⚠️ [Signing] Release keystore not configured or file not found. Falling back to debug signing config for release build type.")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Use release signing config if keystore is present, otherwise fallback to debug config
            val releaseConfig = signingConfigs.getByName("release")
            signingConfig = if (releaseConfig.storeFile != null && releaseConfig.storeFile!!.exists()) {
                releaseConfig
            } else {
                signingConfigs.getByName("debug")
            }
        }

        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // AndroidX & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Tooling & Debugging
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // ---------------------------------------------------------------------------------------------
    // UNIT TESTING DEPENDENCIES (Runs on JVM - Fast)
    // ---------------------------------------------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // ---------------------------------------------------------------------------------------------
    // INSTRUMENTED TESTING DEPENDENCIES (Runs on Android Device/Emulator)
    // ---------------------------------------------------------------------------------------------
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
