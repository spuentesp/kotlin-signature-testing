# Guía Paso a Paso para Estudiantes: Instalación de Dependencias, Pruebas y Firma

¡Bienvenido! Esta guía está pensada para que cualquier estudiante, incluso si nunca antes ha configurado pruebas o firmado una aplicación en Android, entienda exactamente qué hace cada herramienta y cómo funciona cada archivo.

---

## 1. ¿Cómo funcionan las Dependencias en Android Moderno?

En Android moderno (Gradle 8+ y Kotlin 2+), las dependencias ya no se escriben como textos sueltos en `build.gradle`. Se utiliza una herramienta llamada **Version Catalog** (Catálogo de Versiones), ubicado en:
📁 `gradle/libs.versions.toml`

### ¿Por qué se hace así?
* **Centralización**: Todas las versiones están en un solo lugar. Si quieres actualizar una librería, solo cambias un número.
* **Seguridad y Consistencia**: Evita conflictos cuando varios módulos del proyecto usan versiones diferentes de la misma librería.
* **Autocompletado**: Android Studio te autocompleta los nombres de las librerías en Kotlin DSL (`libs.junit`, `libs.mockk`, etc.).

---

## 2. Paso a Paso: Cómo Agregar una Nueva Dependencia de Testing

Imagina que quieres agregar una nueva librería de pruebas (por ejemplo, `Turbine` o `MockK`). Estos son los **3 pasos exactos**:

### Paso 1: Declarar la versión en `gradle/libs.versions.toml`
Abre el archivo `gradle/libs.versions.toml` y busca la sección `[versions]`:
```toml
[versions]
junit = "4.13.2"
mockk = "1.13.13"
turbine = "1.2.0"
coroutines = "1.9.0"
```

### Paso 2: Definir el paquete y módulo en `[libraries]`
En el mismo archivo `gradle/libs.versions.toml`, ve a la sección `[libraries]`:
```toml
[libraries]
# Nombre corto que usarás en el código = { grupo, nombre_artefacto, versión_referenciada }
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

### Paso 3: Aplicar la dependencia en `app/build.gradle.kts`
Abre el archivo `app/build.gradle.kts` y busca el bloque `dependencies { ... }`:

```kotlin
dependencies {
    // -------------------------------------------------------------
    // PRUEBAS UNITARIAS (Se ejecutan en tu PC sin emulador)
    // Usamos 'testImplementation'
    // -------------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // -------------------------------------------------------------
    // PRUEBAS INSTRUMENTADAS / UI / E2E (Requieren emulador o teléfono)
    // Usamos 'androidTestImplementation'
    // -------------------------------------------------------------
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
```

### Paso 4: Sincronizar Gradle en Android Studio
Haz clic en el botón con forma de elefante azul (**Sync Project with Gradle Files**) o en el aviso superior que dice **Sync Now**.
Gradle descargará las librerías automáticamente desde `mavenCentral()` o `google()`.

---

## 3. ¿Qué significan `testImplementation` vs `androidTestImplementation`?

Es muy común que los estudiantes se confundan entre estos dos alcances:

| Tipo | Alcance de Gradle | Dónde se guardan los archivos | Dónde se ejecutan | ¿Requiere Emulador o Celular? | Velocidad |
|---|---|---|---|---|---|
| **Prueba Unitaria** | `testImplementation` | `app/src/test/java/` | En la JVM de tu computador | ❌ No | ⚡ Ultrarrápido (Milisegundos) |
| **Prueba Instrumentada / E2E** | `androidTestImplementation` | `app/src/androidTest/java/` | En el sistema operativo Android | ✅ Sí (Emulador o Teléfono USB) | 🐢 Más lenta (Segundos/Minutos) |

---

## 4. Cómo Abrir y Ejecutar el Proyecto en Android Studio

1. **Abrir el Proyecto**:
   * Abre Android Studio.
   * Selecciona `File -> Open...` (o `Open` en la pantalla de bienvenida).
   * Navega hasta la carpeta `kotlin-signature-testing` y selecciona `OK`.
   * Espera a que termine la sincronización de Gradle (barra de progreso abajo a la derecha).

2. **Ejecutar las Pruebas Unitarias**:
   * En el explorador de archivos (panel izquierdo), expande:
     `app -> src -> test -> java -> com.antigravity.todoapp`.
   * Haz clic derecho sobre la carpeta `usecase` o sobre cualquier archivo (ej: `AddTodoUseCaseTest.kt`).
   * Selecciona **Run 'AddTodoUseCaseTest'** (icono verde de Play ▶️).
   * Verás la consola inferior mostrar todos los checks verdes ✅.

3. **Ejecutar la Prueba E2E**:
   * Inicia un emulador de Android desde el **Device Manager** de Android Studio.
   * En el explorador, expande:
     `app -> src -> androidTest -> java -> com.antigravity.todoapp`.
   * Haz clic derecho sobre `TodoAppE2ETest.kt` y selecciona **Run 'TodoAppE2ETest'**.
   * Verás al emulador abrir la app, escribir automáticamente en los campos, presionar botones y verificar la UI sin que toques nada.

4. **Generar el APK Release Firmado**:
   * Abre la pestaña inferior **Terminal** en Android Studio.
   * Ejecuta:
     ```bash
     ./gradlew assembleRelease
     ```
   * Encontrarás tu APK firmado listo para instalar en:
     `app/build/outputs/apk/release/app-release.apk`
