package com.antigravity.todoapp.domain.usecase

import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoFilter
import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to retrieve todos filtered by completion status, priority, and search query.
 * Applies domain sorting (uncompleted first, highest priority first, newest first).
 */
class GetFilteredTodosUseCase(
    private val repository: TodoRepository
) {
    operator fun invoke(
        filter: TodoFilter = TodoFilter.ALL,
        searchQuery: String = ""
    ): Flow<List<TodoItem>> {
        val query = searchQuery.trim().lowercase()

        return repository.getTodos().map { todos ->
            todos
                .filter { item ->
                    // Filter by status/priority category
                    when (filter) {
                        TodoFilter.ALL -> true
                        TodoFilter.ACTIVE -> !item.isCompleted
                        TodoFilter.COMPLETED -> item.isCompleted
                        TodoFilter.HIGH_PRIORITY -> item.priority == Priority.HIGH
                    }
                }
                .filter { item ->
                    // Filter by search text
                    if (query.isBlank()) {
                        true
                    } else {
                        item.title.lowercase().contains(query) ||
                            item.description.lowercase().contains(query)
                    }
                }
                .sortedWith(
                    compareBy<TodoItem> { it.isCompleted } // Active tasks first
                        .thenByDescending { it.priority.level } // High priority first
                        .thenByDescending { it.createdAt } // Most recent first
                )
        }
    }
}
