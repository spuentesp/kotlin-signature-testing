package com.antigravity.todoapp.domain.usecase

import com.antigravity.todoapp.domain.repository.TodoRepository

/**
 * Use case to delete a Todo item by id.
 */
class DeleteTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: String) {
        if (id.isNotBlank()) {
            repository.deleteTodo(id)
        }
    }
}
