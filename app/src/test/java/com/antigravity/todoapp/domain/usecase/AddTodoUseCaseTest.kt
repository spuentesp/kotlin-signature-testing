package com.antigravity.todoapp.domain.usecase

import com.antigravity.todoapp.data.repository.FakeTodoRepository
import com.antigravity.todoapp.domain.model.Priority
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [AddTodoUseCase].
 * Teaches:
 * - Testing input validation and domain error handling
 * - Verifying repository side-effects with Fakes
 * - Using kotlinx.coroutines.test.runTest for suspend functions
 */
class AddTodoUseCaseTest {

    private lateinit var repository: FakeTodoRepository
    private lateinit var useCase: AddTodoUseCase

    @Before
    fun setUp() {
        repository = FakeTodoRepository()
        useCase = AddTodoUseCase(repository)
    }

    @Test
    fun `invoke with valid title creates and persists todo`() = runTest {
        // Given
        val title = "Buy groceries"
        val description = "Milk, Eggs, Bread"
        val priority = Priority.HIGH

        // When
        val result = useCase(title = title, description = description, priority = priority)

        // Then
        assertTrue("Result should be success", result.isSuccess)
        val createdTodo = result.getOrNull()
        assertNotNull(createdTodo)
        assertEquals("Buy groceries", createdTodo?.title)
        assertEquals("Milk, Eggs, Bread", createdTodo?.description)
        assertEquals(Priority.HIGH, createdTodo?.priority)

        // Verify repository state
        val savedTodos = repository.getTodos().first()
        assertEquals(1, savedTodos.size)
        assertEquals("Buy groceries", savedTodos.first().title)
    }

    @Test
    fun `invoke with blank title fails with descriptive error`() = runTest {
        // Given
        val blankTitle = "   "

        // When
        val result = useCase(title = blankTitle)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
        assertEquals("Task title cannot be empty", exception?.message)

        // Verify nothing was saved to repository
        val savedTodos = repository.getTodos().first()
        assertTrue("Repository should remain empty", savedTodos.isEmpty())
    }

    @Test
    fun `invoke with title exceeding max length fails`() = runTest {
        // Given
        val longTitle = "A".repeat(AddTodoUseCase.MAX_TITLE_LENGTH + 1)

        // When
        val result = useCase(title = longTitle)

        // Then
        assertTrue("Result should be failure", result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is IllegalArgumentException)
    }

    @Test
    fun `invoke trims whitespace from title and description`() = runTest {
        // Given
        val titleWithSpaces = "   Read Android Documentation   "
        val descWithSpaces = "   Chapter 4   "

        // When
        val result = useCase(title = titleWithSpaces, description = descWithSpaces)

        // Then
        assertTrue(result.isSuccess)
        val todo = result.getOrNull()!!
        assertEquals("Read Android Documentation", todo.title)
        assertEquals("Chapter 4", todo.description)
    }
}
