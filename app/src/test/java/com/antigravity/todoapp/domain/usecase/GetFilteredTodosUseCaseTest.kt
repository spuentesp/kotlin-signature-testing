package com.antigravity.todoapp.domain.usecase

import app.cash.turbine.test
import com.antigravity.todoapp.data.repository.FakeTodoRepository
import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoFilter
import com.antigravity.todoapp.domain.model.TodoItem
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [GetFilteredTodosUseCase].
 * Teaches:
 * - Testing Kotlin Flow transformations using Turbine
 * - Testing filtering conditions (Active, Completed, Priority, Search Query)
 * - Verifying multi-criteria domain sorting
 */
class GetFilteredTodosUseCaseTest {

    private lateinit var repository: FakeTodoRepository
    private lateinit var useCase: GetFilteredTodosUseCase

    private val sampleTodos = listOf(
        TodoItem(
            id = "1",
            title = "Write Unit Tests",
            description = "Use Turbine for Flow testing",
            isCompleted = false,
            priority = Priority.HIGH,
            createdAt = 1000L
        ),
        TodoItem(
            id = "2",
            title = "Learn Keystore signing",
            description = "Debug vs Release certificates",
            isCompleted = true,
            priority = Priority.MEDIUM,
            createdAt = 2000L
        ),
        TodoItem(
            id = "3",
            title = "Review Proguard rules",
            description = "Obfuscation and optimization",
            isCompleted = false,
            priority = Priority.LOW,
            createdAt = 3000L
        )
    )

    @Before
    fun setUp() {
        repository = FakeTodoRepository(initialTodos = sampleTodos)
        useCase = GetFilteredTodosUseCase(repository)
    }

    @Test
    fun `when filter is ALL, returns all todos with active first and sorted by priority`() = runTest {
        useCase(filter = TodoFilter.ALL).test {
            val list = awaitItem()
            assertEquals(3, list.size)
            // Uncompleted items come first, with HIGH before LOW
            assertEquals("1", list[0].id) // Active, HIGH
            assertEquals("3", list[1].id) // Active, LOW
            assertEquals("2", list[2].id) // Completed, MEDIUM
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when filter is ACTIVE, returns only uncompleted todos`() = runTest {
        useCase(filter = TodoFilter.ACTIVE).test {
            val list = awaitItem()
            assertEquals(2, list.size)
            assertTrue(list.all { !it.isCompleted })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when filter is COMPLETED, returns only completed todos`() = runTest {
        useCase(filter = TodoFilter.COMPLETED).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertTrue(list.all { it.isCompleted })
            assertEquals("2", list.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when filter is HIGH_PRIORITY, returns only high priority items`() = runTest {
        useCase(filter = TodoFilter.HIGH_PRIORITY).test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals(Priority.HIGH, list.first().priority)
            assertEquals("1", list.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when searching by query, filters matching title or description case-insensitively`() = runTest {
        useCase(searchQuery = "keystore").test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("2", list.first().id)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
