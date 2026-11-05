package com.antigravity.todoapp.presentation.todo_list

import app.cash.turbine.test
import com.antigravity.todoapp.data.repository.FakeTodoRepository
import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoFilter
import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.domain.usecase.AddTodoUseCase
import com.antigravity.todoapp.domain.usecase.DeleteTodoUseCase
import com.antigravity.todoapp.domain.usecase.GetFilteredTodosUseCase
import com.antigravity.todoapp.domain.usecase.ToggleTodoCompletionUseCase
import com.antigravity.todoapp.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for [TodoListViewModel].
 * Teaches:
 * - Setting up MainDispatcherRule for viewModelScope execution
 * - Testing StateFlow emissions with Turbine
 * - Testing MVI/UDF user event handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeTodoRepository
    private lateinit var viewModel: TodoListViewModel

    private val initialTasks = listOf(
        TodoItem(
            id = "task-1",
            title = "Task One",
            isCompleted = false,
            priority = Priority.MEDIUM
        ),
        TodoItem(
            id = "task-2",
            title = "Task Two",
            isCompleted = true,
            priority = Priority.LOW
        )
    )

    @Before
    fun setUp() {
        repository = FakeTodoRepository(initialTodos = initialTasks)
        val getFilteredTodosUseCase = GetFilteredTodosUseCase(repository)
        val addTodoUseCase = AddTodoUseCase(repository)
        val toggleTodoCompletionUseCase = ToggleTodoCompletionUseCase(repository)
        val deleteTodoUseCase = DeleteTodoUseCase(repository)

        viewModel = TodoListViewModel(
            getFilteredTodosUseCase = getFilteredTodosUseCase,
            addTodoUseCase = addTodoUseCase,
            toggleTodoCompletionUseCase = toggleTodoCompletionUseCase,
            deleteTodoUseCase = deleteTodoUseCase
        )
    }

    @Test
    fun `initial uiState contains loaded items`() = runTest {
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            assertEquals(2, state.todos.size)
            assertEquals(TodoFilter.ALL, state.filter)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `toggle todo changes completion status in uiState`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial emission

            viewModel.onEvent(TodoListEvent.ToggleTodo("task-1"))

            val updatedState = awaitItem()
            val toggledItem = updatedState.todos.find { it.id == "task-1" }
            assertTrue(toggledItem?.isCompleted == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `change filter updates active filter and filtered items`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Initial emission

            viewModel.onEvent(TodoListEvent.ChangeFilter(TodoFilter.COMPLETED))

            val updatedState = expectMostRecentItem()
            assertEquals(TodoFilter.COMPLETED, updatedState.filter)
            assertEquals(1, updatedState.todos.size)
            assertEquals("task-2", updatedState.todos.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `add valid todo updates state and dismisses dialog`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(TodoListEvent.OpenAddDialog)
            val dialogOpenState = awaitItem()
            assertTrue(dialogOpenState.isAddDialogOpen)

            viewModel.onEvent(
                TodoListEvent.AddTodo(
                    title = "New Integration Task",
                    description = "Verify test coverage",
                    priority = Priority.HIGH
                )
            )

            val finalState = expectMostRecentItem()
            assertFalse(finalState.isAddDialogOpen)
            assertEquals("Task added successfully", finalState.userMessage)
            assertTrue(finalState.todos.any { it.title == "New Integration Task" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `delete todo removes item and emits notification message`() = runTest {
        viewModel.uiState.test {
            awaitItem()

            viewModel.onEvent(TodoListEvent.DeleteTodo("task-1"))

            val stateAfterDelete = expectMostRecentItem()
            assertEquals("Task deleted", stateAfterDelete.userMessage)
            assertEquals(1, stateAfterDelete.todos.size)
            assertFalse(stateAfterDelete.todos.any { it.id == "task-1" })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
