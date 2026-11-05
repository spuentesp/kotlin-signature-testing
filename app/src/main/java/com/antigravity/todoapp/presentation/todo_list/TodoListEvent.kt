package com.antigravity.todoapp.presentation.todo_list

import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoFilter

/**
 * Sealed interface representing all user intentions and UI events.
 * Enables unidirectional data flow (UDF) and predictable state transitions.
 */
sealed interface TodoListEvent {
    data class ToggleTodo(val id: String) : TodoListEvent
    data class DeleteTodo(val id: String) : TodoListEvent
    data class AddTodo(
        val title: String,
        val description: String = "",
        val priority: Priority = Priority.MEDIUM
    ) : TodoListEvent
    data class ChangeFilter(val filter: TodoFilter) : TodoListEvent
    data class SearchQueryChanged(val query: String) : TodoListEvent
    data object OpenAddDialog : TodoListEvent
    data object DismissAddDialog : TodoListEvent
    data object ClearMessage : TodoListEvent
}
