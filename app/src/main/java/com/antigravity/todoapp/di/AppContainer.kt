package com.antigravity.todoapp.di

import com.antigravity.todoapp.data.repository.InMemoryTodoRepository
import com.antigravity.todoapp.domain.repository.TodoRepository
import com.antigravity.todoapp.domain.usecase.AddTodoUseCase
import com.antigravity.todoapp.domain.usecase.DeleteTodoUseCase
import com.antigravity.todoapp.domain.usecase.GetFilteredTodosUseCase
import com.antigravity.todoapp.domain.usecase.ToggleTodoCompletionUseCase

/**
 * Dependency container providing application-wide singletons and use cases.
 * Simplifies manual dependency injection and facilitates test swapping.
 */
interface AppContainer {
    val todoRepository: TodoRepository
    val getFilteredTodosUseCase: GetFilteredTodosUseCase
    val addTodoUseCase: AddTodoUseCase
    val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase
    val deleteTodoUseCase: DeleteTodoUseCase
}

class DefaultAppContainer : AppContainer {
    override val todoRepository: TodoRepository by lazy {
        InMemoryTodoRepository()
    }

    override val getFilteredTodosUseCase: GetFilteredTodosUseCase by lazy {
        GetFilteredTodosUseCase(todoRepository)
    }

    override val addTodoUseCase: AddTodoUseCase by lazy {
        AddTodoUseCase(todoRepository)
    }

    override val toggleTodoCompletionUseCase: ToggleTodoCompletionUseCase by lazy {
        ToggleTodoCompletionUseCase(todoRepository)
    }

    override val deleteTodoUseCase: DeleteTodoUseCase by lazy {
        DeleteTodoUseCase(todoRepository)
    }
}
