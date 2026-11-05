package com.antigravity.todoapp.presentation.todo_list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.antigravity.todoapp.presentation.todo_list.components.AddTodoDialog
import com.antigravity.todoapp.presentation.todo_list.components.FilterBar
import com.antigravity.todoapp.presentation.todo_list.components.TodoItemRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoListScreen(
    state: TodoListUiState,
    onEvent: (TodoListEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            onEvent(TodoListEvent.ClearMessage)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Modern Todo") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onEvent(TodoListEvent.OpenAddDialog) }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { onEvent(TodoListEvent.SearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search tasks…") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            // Category & Priority Filters
            FilterBar(
                selectedFilter = state.filter,
                onFilterSelected = { onEvent(TodoListEvent.ChangeFilter(it)) }
            )

            // Todo List Items
            if (state.todos.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (state.searchQuery.isNotBlank()) "No matching tasks found" else "No tasks yet! Tap + to add one.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = state.todos,
                        key = { it.id }
                    ) { item ->
                        TodoItemRow(
                            todo = item,
                            onToggle = { onEvent(TodoListEvent.ToggleTodo(item.id)) },
                            onDelete = { onEvent(TodoListEvent.DeleteTodo(item.id)) }
                        )
                    }
                }
            }
        }

        if (state.isAddDialogOpen) {
            AddTodoDialog(
                onDismiss = { onEvent(TodoListEvent.DismissAddDialog) },
                onConfirm = { title, desc, priority ->
                    onEvent(TodoListEvent.AddTodo(title, desc, priority))
                }
            )
        }
    }
}
