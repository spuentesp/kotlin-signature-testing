package com.antigravity.todoapp.domain.repository

import com.antigravity.todoapp.domain.model.TodoItem
import kotlinx.coroutines.flow.Flow

/**
 * Domain boundary interface defining repository contracts.
 * Keeps business logic agnostic of data sources (In-Memory, Room, Firebase, Remote REST API).
 */
interface TodoRepository {
    fun getTodos(): Flow<List<TodoItem>>
    suspend fun getTodoById(id: String): TodoItem?
    suspend fun upsertTodo(todo: TodoItem)
    suspend fun deleteTodo(id: String)
    suspend fun toggleTodoCompletion(id: String)
}
