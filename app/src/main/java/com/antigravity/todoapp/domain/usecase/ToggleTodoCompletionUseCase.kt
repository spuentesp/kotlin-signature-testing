package com.antigravity.todoapp.domain.usecase

import com.antigravity.todoapp.domain.repository.TodoRepository

/**
 * Use case to toggle the completion status of a Todo item.
 */
class ToggleTodoCompletionUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(id: String) {
        if (id.isNotBlank()) {
            repository.toggleTodoCompletion(id)
        }
    }
}
