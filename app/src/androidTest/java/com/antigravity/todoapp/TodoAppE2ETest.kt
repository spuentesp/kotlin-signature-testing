package com.antigravity.todoapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.antigravity.todoapp.presentation.MainActivity
import org.junit.Rule
import org.junit.Test

/**
 * Prueba End-to-End (E2E) completa para la aplicación Todo.
 *
 * EXPLICACIÓN DIDÁCTICA:
 * - Una prueba E2E evalúa la aplicación desde la perspectiva del usuario final (caja negra).
 * - Levanta la Activity real ([MainActivity]) con todas sus dependencias (inyección, repositorio, ViewModel y UI Compose).
 * - Simula el flujo completo:
 *     1. Apertura de la app y visualización de la lista inicial.
 *     2. Creación de una nueva tarea mediante el botón flotante (FAB) y el diálogo.
 *     3. Verificación de que la tarea aparece en la lista.
 *     4. Marcado de la tarea como completada (checkbox).
 *     5. Filtrado por estado ("Completed", "Active") comprobando que la UI reacciona en tiempo real.
 *     6. Eliminación de la tarea y verificación de notificación (Snackbar).
 */
class TodoAppE2ETest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun flujoCompleto_crearTarea_completar_filtrar_y_eliminar() {
        val tituloNuevaTarea = "Aprender Pruebas E2E en Android"
        val descripcionNuevaTarea = "Verificar todo el ciclo de vida de la tarea con Compose Rule"

        // -----------------------------------------------------------------------------------------
        // PASO 1: Verificar que la pantalla inicial cargó con el título
        // -----------------------------------------------------------------------------------------
        composeTestRule
            .onNodeWithText("Modern Todo")
            .assertIsDisplayed()

        // -----------------------------------------------------------------------------------------
        // PASO 2: Abrir el diálogo para agregar una nueva tarea
        // -----------------------------------------------------------------------------------------
        composeTestRule
            .onNodeWithContentDescription("Add Task")
            .performClick()

        // El diálogo de "New Task" debe estar visible
        composeTestRule
            .onNodeWithText("New Task")
            .assertIsDisplayed()

        // -----------------------------------------------------------------------------------------
        // PASO 3: Llenar el formulario del diálogo
        // -----------------------------------------------------------------------------------------
        // Escribir el título en el campo "Title"
        composeTestRule
            .onNodeWithText("Title")
            .performTextInput(tituloNuevaTarea)

        // Escribir la descripción en el campo "Description (Optional)"
        composeTestRule
            .onNodeWithText("Description (Optional)")
            .performTextInput(descripcionNuevaTarea)

        // Seleccionar prioridad Alta ("High")
        composeTestRule
            .onNodeWithText("High")
            .performClick()

        // Confirmar la creación pulsando "Add"
        composeTestRule
            .onNodeWithText("Add")
            .performClick()

        // -----------------------------------------------------------------------------------------
        // PASO 4: Verificar que la tarea fue agregada a la lista
        // -----------------------------------------------------------------------------------------
        composeTestRule
            .onNodeWithText(tituloNuevaTarea)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(descripcionNuevaTarea)
            .assertIsDisplayed()

        // -----------------------------------------------------------------------------------------
        // PASO 5: Marcar la tarea como completada
        // Al hacer click sobre el texto o fila de la tarea se dispara el evento ToggleTodo
        // -----------------------------------------------------------------------------------------
        composeTestRule
            .onNodeWithText(tituloNuevaTarea)
            .performClick()

        // -----------------------------------------------------------------------------------------
        // PASO 6: Probar los filtros reactivos
        // -----------------------------------------------------------------------------------------
        // Filtrar por tareas activas ("Active") -> La tarea completada NO debe aparecer
        composeTestRule
            .onNodeWithText("Active")
            .performClick()

        composeTestRule
            .onNodeWithText(tituloNuevaTarea)
            .assertDoesNotExist()

        // Filtrar por tareas completadas ("Completed") -> La tarea SÍ debe aparecer
        composeTestRule
            .onNodeWithText("Completed")
            .performClick()

        composeTestRule
            .onNodeWithText(tituloNuevaTarea)
            .assertIsDisplayed()

        // Volver al filtro "All"
        composeTestRule
            .onNodeWithText("All")
            .performClick()

        // -----------------------------------------------------------------------------------------
        // PASO 7: Eliminar la tarea recién creada
        // -----------------------------------------------------------------------------------------
        composeTestRule
            .onNodeWithContentDescription("Delete task")
            .performClick()

        // Verificar el mensaje de confirmación en el Snackbar
        composeTestRule
            .onNodeWithText("Task deleted")
            .assertIsDisplayed()
    }
}
