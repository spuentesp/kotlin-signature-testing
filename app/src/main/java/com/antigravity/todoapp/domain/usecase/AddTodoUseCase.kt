package com.antigravity.todoapp.domain.usecase

import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.domain.repository.TodoRepository

/**
 * Use case to validate and add a new Todo item.
 * Encapsulates domain business rules for task creation.
 */
class AddTodoUseCase(
    private val repository: TodoRepository
) {
    suspend operator fun invoke(
        title: String,
        description: String = "",
        priority: Priority = Priority.MEDIUM
    ): Result<TodoItem> {
        val trimmedTitle = title.trim()

        if (trimmedTitle.isBlank()) {
            return Result.failure(IllegalArgumentException("Task title cannot be empty"))
        }

        if (trimmedTitle.length > MAX_TITLE_LENGTH) {
            return Result.failure(
                IllegalArgumentException("Task title cannot exceed $MAX_TITLE_LENGTH characters")
            )
        }

        val item = TodoItem(
            title = trimmedTitle,
            description = description.trim(),
            priority = priority
        )

        repository.upsertTodo(item)
        return Result.success(item)
    }

    companion object {
        const val MAX_TITLE_LENGTH = 100
    }
}
