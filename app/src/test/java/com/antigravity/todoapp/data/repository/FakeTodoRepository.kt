package com.antigravity.todoapp.data.repository

import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Test Fake implementation of [TodoRepository].
 *
 * EDUCATIONAL NOTE (Fakes vs Mocks):
 * - A Fake has working business logic (e.g., in-memory list) but is lightweight and runs entirely in RAM.
 * - A Mock (e.g. MockK) records interactions and stubs method responses without actual behavior.
 * Using Fakes often leads to faster, more resilient tests that don't break when internal implementation details change.
 */
class FakeTodoRepository(
    initialTodos: List<TodoItem> = emptyList()
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
}
