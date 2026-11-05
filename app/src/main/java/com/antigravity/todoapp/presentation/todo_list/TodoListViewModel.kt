package com.antigravity.todoapp.presentation.todo_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoFilter
import com.antigravity.todoapp.domain.usecase.AddTodoUseCase
import com.antigravity.todoapp.domain.usecase.DeleteTodoUseCase
import com.antigravity.todoapp.domain.usecase.GetFilteredTodosUseCase
import com.antigravity.todoapp.domain.usecase.ToggleTodoCompletionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for Todo list UI state and user interactions.
 * Follows Unidirectional Data Flow (UDF) pattern.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModel(
    private val getFilteredTodosUseCase: GetFilteredTodosUseCase,
    private val addTodoUseCase: AddTodoUseCase,
    private val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
    private val deleteTodoUseCase: DeleteTodoUseCase
) : ViewModel() {

    private val _filter = MutableStateFlow(TodoFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isAddDialogOpen = MutableStateFlow(false)
    private val _userMessage = MutableStateFlow<String?>(null)

    // Coordinated atomic flow: combine UI controls and transform with use case
    val uiState: StateFlow<TodoListUiState> = combine(
        _filter,
        _searchQuery,
        _isAddDialogOpen,
        _userMessage
    ) { filter, query, isDialogOpen, message ->
        StateParams(filter, query, isDialogOpen, message)
    }.flatMapLatest { params ->
        getFilteredTodosUseCase(filter = params.filter, searchQuery = params.searchQuery).map { todos ->
            TodoListUiState(
                todos = todos,
                filter = params.filter,
                searchQuery = params.searchQuery,
                isAddDialogOpen = params.isDialogOpen,
                isLoading = false,
                userMessage = params.message
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoListUiState(isLoading = true)
    )

    private data class StateParams(
        val filter: TodoFilter,
        val searchQuery: String,
        val isDialogOpen: Boolean,
        val message: String?
    )

    fun onEvent(event: TodoListEvent) {
        when (event) {
            is TodoListEvent.ToggleTodo -> toggleTodo(event.id)
            is TodoListEvent.DeleteTodo -> deleteTodo(event.id)
            is TodoListEvent.AddTodo -> addTodo(event.title, event.description, event.priority)
            is TodoListEvent.ChangeFilter -> _filter.value = event.filter
            is TodoListEvent.SearchQueryChanged -> _searchQuery.value = event.query
            TodoListEvent.OpenAddDialog -> _isAddDialogOpen.value = true
            TodoListEvent.DismissAddDialog -> _isAddDialogOpen.value = false
            TodoListEvent.ClearMessage -> _userMessage.value = null
        }
    }

    private fun toggleTodo(id: String) {
        viewModelScope.launch {
            toggleTodoCompletionUseCase(id)
        }
    }

    private fun deleteTodo(id: String) {
        viewModelScope.launch {
            deleteTodoUseCase(id)
            _userMessage.value = "Task deleted"
        }
    }

    private fun addTodo(title: String, description: String, priority: Priority) {
        viewModelScope.launch {
            val result = addTodoUseCase(title, description, priority)
            result.onSuccess {
                _isAddDialogOpen.value = false
                _userMessage.value = "Task added successfully"
            }.onFailure { error ->
                _userMessage.value = error.message ?: "Failed to add task"
            }
        }
    }

    companion object {
        fun provideFactory(
            getFilteredTodosUseCase: GetFilteredTodosUseCase,
            addTodoUseCase: AddTodoUseCase,
            toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase,
            deleteTodoUseCase: DeleteTodoUseCase
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return TodoListViewModel(
                    getFilteredTodosUseCase,
                    addTodoUseCase,
                    toggleTodoCompletionUseCase,
                    deleteTodoUseCase
                ) as T
            }
        }
    }
}
