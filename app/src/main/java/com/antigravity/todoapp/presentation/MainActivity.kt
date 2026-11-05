package com.antigravity.todoapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.antigravity.todoapp.TodoApplication
import com.antigravity.todoapp.presentation.theme.TodoAppTheme
import com.antigravity.todoapp.presentation.todo_list.TodoListScreen
import com.antigravity.todoapp.presentation.todo_list.TodoListViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: TodoListViewModel by viewModels {
        val container = (application as TodoApplication).container
        TodoListViewModel.provideFactory(
            getFilteredTodosUseCase = container.getFilteredTodosUseCase,
            addTodoUseCase = container.addTodoUseCase,
            toggleTodoCompletionUseCase = container.toggleTodoCompletionUseCase,
            deleteTodoUseCase = container.deleteTodoUseCase
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TodoAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val state by viewModel.uiState.collectAsStateWithLifecycle()
                    TodoListScreen(
                        state = state,
                        onEvent = viewModel::onEvent
                    )
                }
            }
        }
    }
}
