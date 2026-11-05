# Modern Kotlin Todo App (Testing & App Signature Educational Base)

A modern, production-grade Android Todo application built with **Kotlin 2.0+**, **Jetpack Compose (Material 3)**, and **Clean Architecture**.

This repository is specifically structured as an educational playground and teaching foundation for two critical Android topics:
1. **Testing**: Unit tests, Coroutines & Flow testing with Turbine, ViewModel testing, and Compose UI testing.
2. **App Signature**: Android signing schemes (v1, v2, v3, v4), Keystores (PKCS12/JKS), certificate fingerprints (SHA-256/SHA-1), secure Gradle `signingConfigs`, and CI/CD secret management.

---

## 🏗️ Architecture Overview

The app follows **Clean Architecture** with **Unidirectional Data Flow (UDF)**:

```
app/src/main/java/com/antigravity/todoapp/
├── TodoApplication.kt                 # Base Application with DI container
├── di/
│   └── AppContainer.kt                # Manual Dependency Injection
├── domain/
│   ├── model/                         # Pure Kotlin domain entities
│   │   ├── Priority.kt                # Priority enum (Low, Medium, High)
│   │   ├── TodoFilter.kt              # Filter enum (All, Active, Completed, High Priority)
│   │   └── TodoItem.kt                # Task entity
│   ├── repository/
│   │   └── TodoRepository.kt          # Repository interface contract
│   └── usecase/                       # Domain business logic (100% JVM unit-testable)
│       ├── AddTodoUseCase.kt          # Input validation & task creation
│       ├── DeleteTodoUseCase.kt       # Task removal
│       ├── GetFilteredTodosUseCase.kt # Search, filtering & sorting logic
│       └── ToggleTodoCompletionUseCase.kt
├── data/
│   └── repository/
│       └── InMemoryTodoRepository.kt  # Thread-safe StateFlow repository implementation
└── presentation/
    ├── MainActivity.kt                # Edge-to-edge Compose activity
    ├── theme/                         # Material 3 Theme, Typography, Colors
    └── todo_list/
        ├── TodoListUiState.kt         # Immutable single source of truth UI state
        ├── TodoListEvent.kt           # Sealed interface representing user actions
        ├── TodoListViewModel.kt       # ViewModel coordinating Use Cases & StateFlow
        ├── TodoListScreen.kt          # Jetpack Compose UI screen
        └── components/
            ├── AddTodoDialog.kt       # Task input dialog with validation feedback
            ├── FilterBar.kt           # Filter chips
            └── TodoItemRow.kt         # Task row with checkbox, priority dot & delete
```

---

## 🧪 Part 1: Teaching Testing

Full testing documentation is available in [docs/02-testing-guide.md](docs/02-testing-guide.md).

### What's included:
- **Use Case Unit Testing** (`AddTodoUseCaseTest.kt`): Validating business logic, input constraints, whitespace trimming, and error results.
- **Flow & Stream Testing** (`GetFilteredTodosUseCaseTest.kt`): Testing reactive Kotlin flows using [Turbine](https://github.com/cashapp/turbine).
- **Test Doubles (Fakes vs. Mocks)** (`FakeTodoRepository.kt`): Demonstrates how to create maintainable, resilient fakes for repository testing.
- **ViewModel Testing** (`TodoListViewModelTest.kt`): Demonstrates `MainDispatcherRule`, `viewModelScope` execution, and verifying state transitions upon receiving UI events.
- **Instrumented Compose UI Testing** (`TodoListScreenTest.kt`): Uses `createComposeRule()` to test rendering, semantics, and user interactions on Android.

### Running Tests:
```bash
# Run JVM Unit & ViewModel tests:
./gradlew test

# Run Android Instrumented UI tests (device/emulator):
./gradlew connectedAndroidTest
```

---

## 🔐 Part 2: Teaching App Signature

Full app signature documentation is available in [docs/03-app-signature-guide.md](docs/03-app-signature-guide.md).

### Key Features:
- **Secure `signingConfigs`**: Configured in `app/build.gradle.kts` to safely read credentials from `keystore.properties` or environment variables with fallback to debug.
- **Signature Schemes**: Covers v1 (JAR), v2 (APK Signature Scheme), v3 (Key rotation), and v4 (Streaming installation).
- **Helper Scripts**:
  - `scripts/generate-keystore.sh`: Generates PKCS12 release keystores and outputs SHA-256 / SHA-1 fingerprints for Google OAuth / Firebase.
  - `scripts/verify-signature.sh`: Inspects keystores with `keytool` and verifies APK signatures with `apksigner`.
- **Credential Protection**: Pre-configured `.gitignore` prevents accidentally leaking keystores or passwords to version control.

### Quick Start for Release Signing:
1. Generate a keystore:
   ```bash
   ./scripts/generate-keystore.sh release.keystore todoapp_key 10000
   ```
2. Copy sample configuration:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
3. Edit `keystore.properties` with your passwords.
4. Build the signed release APK:
   ```bash
   ./gradlew assembleRelease
   ```
5. Verify the generated APK signature:
   ```bash
   ./scripts/verify-signature.sh app/build/outputs/apk/release/app-release.apk
   ```

---

## 📚 Educational Documentation

- [01 - Architecture Overview](docs/01-architecture.md)
- [02 - Testing Guide](docs/02-testing-guide.md)
- [03 - App Signature Guide](docs/03-app-signature-guide.md)

---

## 🛠️ Tech Stack & Dependencies

- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose (BOM 2024.10.01) + Material 3 (1.3.1)
- **Architecture**: Clean Architecture + MVI/UDF + AndroidX Lifecycle 2.8.7
- **Coroutines**: `kotlinx.coroutines` 1.9.0
- **Testing**: JUnit 4, MockK 1.13.13, Coroutines Test 1.9.0, Turbine 1.2.0, Espresso 3.6.1, Compose UI Test
- **Build System**: Gradle 8.11.1 with Version Catalog (`gradle/libs.versions.toml`)
