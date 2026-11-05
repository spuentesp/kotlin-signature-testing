# Guía Didáctica: Pruebas y Firma de Aplicaciones en Android con Kotlin Moderno

Esta guía explica en detalle los tres pilares solicitados para el proyecto:
1. **Prueba Unitaria (Unit Test)**
2. **Prueba de Extremo a Extremo (E2E Test)**
3. **Firma de la Aplicación y Certificados (App Signature & Keystores)**

---

## 1. La Prueba Unitaria (`AddTodoUseCaseTest.kt`)

### ¿Qué es una prueba unitaria?
Una prueba unitaria evalúa la unidad mínima de código verificable (una función, clase o caso de uso) de manera completamente **aislada** de componentes externos (bases de datos reales, red, emuladores o frameworks de Android).

### ¿Por qué probar el Caso de Uso (`AddTodoUseCase`)?
En Clean Architecture, la capa de Dominio contiene las **reglas de negocio puras**. Al no depender de `android.*`, estas pruebas se ejecutan directamente en la JVM de tu computadora en milisegundos.

### Anatomía del Test: El patrón AAA (Arrange - Act - Assert)

Veamos el caso de prueba para validar que no se permitan títulos vacíos:

```kotlin
@Test
fun `invoke with blank title fails with descriptive error`() = runTest {
    // 1. GIVEN / ARRANGE (Preparar el escenario)
    val blankTitle = "   "

    // 2. WHEN / ACT (Ejecutar la acción bajo prueba)
    val result = useCase(title = blankTitle)

    // 3. THEN / ASSERT (Verificar que los resultados cumplen la expectativa)
    assertTrue("El resultado debe ser fallido", result.isFailure)
    val exception = result.exceptionOrNull()
    assertTrue(exception is IllegalArgumentException)
    assertEquals("Task title cannot be empty", exception?.message)

    // Verificar efecto colateral: que el repositorio no haya guardado nada
    val savedTodos = repository.getTodos().first()
    assertTrue("El repositorio debe continuar vacío", savedTodos.isEmpty())
}
```

### Conceptos clave que enseña:
1. **`runTest`**: Bloque provisto por `kotlinx-coroutines-test` para ejecutar funciones suspendidas (`suspend`) de forma determinista, saltando retrasos virtuales sin esperas reales.
2. **`FakeTodoRepository`**: En lugar de usar una base de datos pesada o un mock complejo, usamos un "Fake" (falso) que guarda los datos en memoria en un `MutableStateFlow`. Es rápido y no se rompe con refactorizaciones menores.
3. **Manejo de Errores con `Result<T>`**: No dejamos que la app se caiga con un crash; encapsulamos el error de validación en un `Result.failure`.

---

## 2. La Prueba End-to-End (E2E) (`TodoAppE2ETest.kt`)

### ¿Qué es una prueba E2E?
Una prueba End-to-End (E2E o instrumental) evalúa la aplicación en su totalidad desde la perspectiva del **usuario final**. 
Se ejecuta en un emulador o dispositivo Android real levantando la `MainActivity`.

### El Flujo de Usuario Simulado:
1. **Lanzamiento**: La app inicia y muestra la barra superior "Modern Todo".
2. **Interacción**: El usuario presiona el botón flotante (+) para abrir el diálogo.
3. **Formulario**: Escribe el título "Aprender Pruebas E2E en Android", descripción y selecciona prioridad "High".
4. **Persistencia y Reactividad**: Presiona "Add", el diálogo se cierra y la nueva tarea aparece en la lista.
5. **Cambio de Estado**: Hace clic sobre la tarea para marcarla como completada (aparece tachada).
6. **Filtros en tiempo real**: Pulsa el chip "Active" (la tarea desaparece) y luego "Completed" (la tarea vuelve a ser visible).
7. **Eliminación**: Presiona el icono de borrar y se comprueba la aparición del mensaje en el Snackbar.

### Código clave en Jetpack Compose:
```kotlin
@get:Rule
val composeTestRule = createAndroidComposeRule<MainActivity>()

@Test
fun flujoCompleto_crearTarea_completar_filtrar_y_eliminar() {
    // Encontrar componentes por texto o descripción de accesibilidad:
    composeTestRule
        .onNodeWithContentDescription("Add Task")
        .performClick()

    // Escribir texto en campos OutlinedTextField:
    composeTestRule
        .onNodeWithText("Title")
        .performTextInput("Aprender Pruebas E2E")

    // Validar visibilidad:
    composeTestRule
        .onNodeWithText("Aprender Pruebas E2E")
        .assertIsDisplayed()
}
```

---

## 3. Certificados y Firma de la Aplicación (App Signature)

### ¿Por qué toda app Android debe firmarse?
1. **Identidad del Desarrollador**: Demuestra criptográficamente quién construyó la app.
2. **Integridad del Binario**: Garantiza que nadie alteró el código del APK tras su compilación.
3. **Actualizaciones Seguras**: Android solo permite actualizar una app si el nuevo APK tiene el **mismo nombre de paquete (applicationId) y está firmado con la misma clave privada**.

---

### El Certificado Educativo incluido en el Repo

Hemos generado un certificado de demostración listo para usar:
- **Ubicación**: [`keystore/debug-demo.keystore`](../keystore/debug-demo.keystore)
- **Configuración Gradle**: [`keystore.properties`](../keystore.properties)
- **Tipo de almacén**: PKCS12 (estándar moderno de la industria)
- **Algoritmo de clave**: RSA 2048-bit
- **Validez**: 10,000 días (hasta el año 2054)

#### Credenciales configuradas:
| Propiedad | Valor |
|---|---|
| `storeFile` | `keystore/debug-demo.keystore` |
| `storePassword` | `demo1234` |
| `keyAlias` | `demo_key` |
| `keyPassword` | `demo1234` |

#### Huellas Digitales del Certificado (Fingerprints):
- **SHA-1**: `C1:5C:7C:16:54:0A:AB:42:A7:9E:14:53:34:0C:4A:57:E8:3B:03:79`
- **SHA-256**: `E1:77:B6:62:69:43:24:40:AA:3E:4F:02:EC:D0:05:F2:26:E1:6B:D6:0A:D4:9E:0D:C7:67:82:AE:DF:3D:75:23`

> **Nota Didáctica**: La huella **SHA-256** es la que se copia y pega en la consola de **Google Cloud / Firebase / Google Sign-In** para autorizar que la app consuma esos servicios de manera segura.

---

### Cómo compilar el APK Release firmado

Gracias a la configuración en [`app/build.gradle.kts`](../app/build.gradle.kts), simplemente ejecuta:
```bash
./gradlew assembleRelease
```
Gradle leerá automáticamente `keystore.properties`, firmará el APK con los esquemas **v1, v2, v3 y v4**, y generará el archivo en:
```
app/build/outputs/apk/release/app-release.apk
```

Para verificar que el APK fue firmado correctamente, ejecuta:
```bash
./scripts/verify-signature.sh keystore/debug-demo.keystore
```
