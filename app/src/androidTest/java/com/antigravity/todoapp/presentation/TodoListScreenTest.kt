package com.antigravity.todoapp.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.antigravity.todoapp.domain.model.Priority
import com.antigravity.todoapp.domain.model.TodoItem
import com.antigravity.todoapp.presentation.theme.TodoAppTheme
import com.antigravity.todoapp.presentation.todo_list.TodoListEvent
import com.antigravity.todoapp.presentation.todo_list.TodoListScreen
import com.antigravity.todoapp.presentation.todo_list.TodoListUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Instrumented UI test using Jetpack Compose Testing APIs.
 * Teaches:
 * - Setting up [createComposeRule]
 * - Verifying Composable rendering and semantics
 * - Simulating user clicks and assertions on event emissions
 */
class TodoListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appTitleAndTodoItemAreDisplayed() {
        val testItem = TodoItem(
            id = "test-1",
            title = "Test Automation Task",
            description = "UI test with Compose Rule",
            priority = Priority.HIGH
        )

        composeTestRule.setContent {
            TodoAppTheme {
                TodoListScreen(
                    state = TodoListUiState(todos = listOf(testItem)),
                    onEvent = {}
                )
            }
        }

        // Verify App title is visible in TopAppBar
        composeTestRule.onNodeWithText("Modern Todo").assertIsDisplayed()

        // Verify Todo item title is displayed
        composeTestRule.onNodeWithText("Test Automation Task").assertIsDisplayed()
    }

    @Test
    fun clickingFabDispatchesOpenAddDialogEvent() {
        var openDialogDispatched = false

        composeTestRule.setContent {
            TodoAppTheme {
                TodoListScreen(
                    state = TodoListUiState(),
                    onEvent = { event ->
                        if (event is TodoListEvent.OpenAddDialog) {
                            openDialogDispatched = true
                        }
                    }
                )
            }
        }

        // Find Floating Action Button and click it
        composeTestRule.onNodeWithContentDescription("Add Task").performClick()

        assertTrue("OpenAddDialog event should have been dispatched", openDialogDispatched)
    }
}
