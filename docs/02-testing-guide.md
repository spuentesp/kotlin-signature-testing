# 02 - Testing Guide

This application is designed as a foundational codebase for teaching testing in modern Android and Kotlin.

---

## The Testing Pyramid

```
           / \
          /   \     UI & End-to-End Tests (Instrumented, Compose Rule)
         /-----\
        /       \    Integration & ViewModel Tests (Turbine, Coroutine Test)
       /---------\
      /           \   Unit Tests (Domain Use Cases, Models, Logic)
     /-------------\
```

1. **Unit Tests (Local JVM)**:
   - Run in milliseconds on your host machine.
   - Test business rules, validation, algorithms, and state transformations.
2. **ViewModel & Flow Tests (Local JVM)**:
   - Test UI state emissions and user interaction handling using Kotlin Coroutines Test & Turbine.
3. **Instrumented Tests (Android Emulator / Device)**:
   - Test Jetpack Compose layouts, user input, and screen semantics.

---

## 1. Domain Unit Testing (`AddTodoUseCaseTest.kt`)

Use cases represent pure business logic and should be tested exhaustively:
- **Happy path**: Adding a valid task persists it and returns `Result.success`.
- **Validation errors**: Blank title or excessive character lengths return `Result.failure`.
- **Side effects**: Verify whitespace trimming.

```kotlin
@Test
fun `invoke with blank title fails with descriptive error`() = runTest {
    val result = useCase(title = "   ")
    assertTrue(result.isFailure)
    assertEquals("Task title cannot be empty", result.exceptionOrNull()?.message)
}
```

---

## 2. Test Doubles: Fakes vs Mocks (`FakeTodoRepository.kt`)

| Characteristic | Test Fake | Mock (e.g. MockK / Mockito) |
|---|---|---|
| **Definition** | A lightweight working implementation (e.g. in RAM). | A dummy object that stubs calls and records invocations. |
| **Resilience** | High. Tests continue working even when internal calls change. | Low. Can be brittle when refactoring internal methods. |
| **Stateful** | Yes, holds and updates real collections in memory. | No, must explicitly define return values for each method call. |

This repository includes `FakeTodoRepository` which allows testing both Use Cases and ViewModels naturally.

---

## 3. Reactive Flow Testing with Turbine (`GetFilteredTodosUseCaseTest.kt`)

Testing Kotlin `Flow` can be tricky because flows are asynchronous streams. [Turbine](https://github.com/cashapp/turbine) provides a clean testing DSL:

```kotlin
@Test
fun `when filter is ACTIVE, returns only uncompleted todos`() = runTest {
    useCase(filter = TodoFilter.ACTIVE).test {
        val list = awaitItem()
        assertEquals(2, list.size)
        assertTrue(list.all { !it.isCompleted })
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 4. ViewModel Testing (`TodoListViewModelTest.kt`)

ViewModels rely on `viewModelScope`, which uses `Dispatchers.Main` by default. Because `Dispatchers.Main` is part of the Android framework, JVM unit tests will throw `IllegalStateException: Module with the Main dispatcher had failed to initialize` unless a test dispatcher is configured.

We provide `MainDispatcherRule`:
```kotlin
@get:Rule
val mainDispatcherRule = MainDispatcherRule()
```

Then we test user event dispatches and observe state transitions:
```kotlin
@Test
fun `toggle todo changes completion status in uiState`() = runTest {
    viewModel.uiState.test {
        awaitItem() // Skip initial loading/default state
        viewModel.onEvent(TodoListEvent.ToggleTodo("task-1"))
        val updatedState = awaitItem()
        assertTrue(updatedState.todos.first { it.id == "task-1" }.isCompleted)
        cancelAndIgnoreRemainingEvents()
    }
}
```

---

## 5. Compose UI Testing (`TodoListScreenTest.kt`)

Located in `app/src/androidTest/`:
- Uses `createComposeRule()` from `androidx.compose.ui.test.junit4`.
- Verifies screen elements with semantic matchers:
  - `composeTestRule.onNodeWithText("Modern Todo").assertIsDisplayed()`
  - `composeTestRule.onNodeWithContentDescription("Add Task").performClick()`

---

## Running the Tests

To run local unit tests from CLI:
```bash
./gradlew test
```

To run instrumented tests (with connected device or emulator):
```bash
./gradlew connectedAndroidTest
```
