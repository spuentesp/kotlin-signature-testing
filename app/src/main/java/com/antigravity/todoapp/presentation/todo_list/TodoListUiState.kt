package com.antigravity.todoapp.presentation.todo_list

import com.antigravity.todoapp.domain.model.TodoFilter
import com.antigravity.todoapp.domain.model.TodoItem

/**
 * Immutable UI State representing the single source of truth for the Todo screen.
 */
data class TodoListUiState(
    val todos: List<TodoItem> = emptyList(),
    val filter: TodoFilter = TodoFilter.ALL,
    val searchQuery: String = "",
    val isAddDialogOpen: Boolean = false,
    val isLoading: Boolean = false,
    val userMessage: String? = null
)
