package com.antigravity.todoapp.data.repository

import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Thread-safe in-memory repository implementation.
 * Pre-populated with realistic initial data for immediate use and testing.
 */
class InMemoryTodoRepository(
    initialTodos: List<TodoItem> = defaultInitialTodos()
) : TodoRepository {

    private val _todosFlow = MutableStateFlow(initialTodos)

    override fun getTodos(): Flow<List<TodoItem>> = _todosFlow.asStateFlow()

    override suspend fun getTodoById(id: String): TodoItem? {
        return _todosFlow.value.find { it.id == id }
    }

    override suspend fun upsertTodo(todo: TodoItem) {
        _todosFlow.update { currentList ->
            val index = currentList.indexOfFirst { it.id == todo.id }
            if (index >= 0) {
                currentList.toMutableList().apply { set(index, todo) }
            } else {
                listOf(todo) + currentList
            }
        }
    }

    override suspend fun deleteTodo(id: String) {
        _todosFlow.update { currentList ->
            currentList.filterNot { it.id == id }
        }
    }

    override suspend fun toggleTodoCompletion(id: String) {
        _todosFlow.update { currentList ->
            currentList.map { item ->
                if (item.id == id) {
                    item.copy(isCompleted = !item.isCompleted)
                } else {
                    item
                }
            }
        }
    }

    companion object {
        private fun defaultInitialTodos(): List<TodoItem> = listOf(
            TodoItem(
                id = "seed-1",
                title = "Learn Android App Signing",
                description = "Understand keystores, v1/v2/v3 signing schemes, and SHA-256 fingerprints",
                isCompleted = false,
                priority = Priority.HIGH,
                createdAt = System.currentTimeMillis() - 3600000
            ),
            TodoItem(
                id = "seed-2",
                title = "Master Unit & UI Testing",
                description = "Write tests for Use Cases with JUnit, ViewModels with Turbine, and Compose UI",
                isCompleted = false,
                priority = Priority.HIGH,
                createdAt = System.currentTimeMillis() - 7200000
            ),
            TodoItem(
                id = "seed-3",
                title = "Configure release signingConfigs",
                description = "Add keystore.properties and secure CI/CD signing secrets",
                isCompleted = true,
                priority = Priority.MEDIUM,
                createdAt = System.currentTimeMillis() - 10800000
            )
        )
    }
}
