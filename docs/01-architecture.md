# 01 - Architecture Overview

This project is built using **Modern Kotlin** and follows **Clean Architecture** combined with **Unidirectional Data Flow (UDF)**.

## Architectural Layers

```
                      ┌─────────────────────────────────┐
                      │       Presentation Layer        │
                      │  (Jetpack Compose + ViewModels) │
                      └──────────────┬──────────────────┘
                                     │ observes / dispatches
                                     ▼
                      ┌─────────────────────────────────┐
                      │          Domain Layer           │
                      │   (Use Cases, Models, Repos)    │
                      └──────────────▲──────────────────┘
                                     │ implements interface
                                     │
                      ┌──────────────┴──────────────────┐
                      │           Data Layer            │
                      │  (Repositories, Storage, Fakes) │
                      └─────────────────────────────────┘
```

### 1. Domain Layer (`com.antigravity.todoapp.domain`)
- **Pure Kotlin**: Absolutely no Android framework dependencies (`android.*`).
- **Models**:
  - `TodoItem`: Immutable data representation of a task.
  - `Priority`: Enum (`LOW`, `MEDIUM`, `HIGH`).
  - `TodoFilter`: Enum defining filter categories (`ALL`, `ACTIVE`, `COMPLETED`, `HIGH_PRIORITY`).
- **Repository Interface**:
  - `TodoRepository`: Defines reactive streams (`Flow<List<TodoItem>>`) and CRUD suspend operations.
- **Use Cases**:
  - `AddTodoUseCase`: Validates titles, length restrictions, and enforces task creation rules.
  - `GetFilteredTodosUseCase`: Transforms the task stream by applying active filter, search query, and domain sorting.
  - `ToggleTodoCompletionUseCase`: Toggles completion status.
  - `DeleteTodoUseCase`: Removes a task.

> **Why this matters for teaching**: Pure Kotlin domain logic executes instantly on the local JVM without emulators or Robolectric, making unit tests fast (milliseconds) and reliable.

---

### 2. Data Layer (`com.antigravity.todoapp.data`)
- **Implementations**:
  - `InMemoryTodoRepository`: Implements `TodoRepository` backed by a thread-safe `MutableStateFlow`.
  - Can easily be replaced or swapped with a Room database or remote REST API without modifying a single line of domain or presentation code.

---

### 3. Presentation Layer (`com.antigravity.todoapp.presentation`)
- **Jetpack Compose with Material 3**:
  - Modern declarative UI with `Scaffold`, `LazyColumn`, `FilterChip`, `AlertDialog`, and `FloatingActionButton`.
- **Unidirectional Data Flow (UDF)**:
  - **State**: `TodoListUiState` (single immutable data class).
  - **Events**: `TodoListEvent` (sealed interface representing all user intentions: `ToggleTodo`, `AddTodo`, `ChangeFilter`, etc.).
  - **ViewModel**: `TodoListViewModel` processes events, executes use cases, and emits updated `StateFlow<TodoListUiState>`.
- **Theme**: Complete Material 3 typography and color palette (`Theme.kt`, `Color.kt`, `Type.kt`).

---

### 4. Dependency Injection (`com.antigravity.todoapp.di`)
- `AppContainer`: Minimalist manual dependency injection container.
- Eliminates heavy annotation processors (like Hilt/KSP) while keeping every component decoupled and testable.
