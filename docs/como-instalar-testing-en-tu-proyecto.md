# Manual del Estudiante: Cómo Agregar Testing a Tu Proyecto (Forma Moderna y Forma Antigua)

Si eres estudiante y quieres aplicar testing en tu propio proyecto desde cero, esta guía te llevará paso a paso sin asumir ningún conocimiento previo.

---

## 🔍 Paso 0: Identifica qué tipo de proyecto tienes

Antes de copiar código, mira la estructura de tu proyecto en Android Studio (panel izquierdo):

1. **¿Tu archivo de Gradle termina en `.kts` o no?**
   * Si ves `build.gradle.kts`: Tu proyecto usa **Kotlin DSL**.
   * Si ves `build.gradle` (sin `.kts`): Tu proyecto usa **Groovy DSL (Forma Antigua/Tradicional)**.
2. **¿Existe el archivo `gradle/libs.versions.toml`?**
   * **SÍ existe:** Tu proyecto usa el **Version Catalog (Forma Moderna)**.
   * **NO existe:** Tu proyecto usa la **Forma Antigua (Dependencias directas)**.

A continuación tienes las dos opciones. Elige la que coincida con tu proyecto:

---

## 🛠️ Opción A: Forma Moderna (Version Catalog + Kotlin DSL `.kts`)

Esta es la forma estándar recomendada por Google en Android Studio Hedgehog, Iguana, Jellyfish, Koala y versiones superiores.

### Paso A.1: Abre `gradle/libs.versions.toml`
Agrega las versiones en la sección `[versions]`:
```toml
[versions]
junit = "4.13.2"
mockk = "1.13.13"
coroutines = "1.9.0"
turbine = "1.2.0"
```

En el mismo archivo, agrega las librerías en la sección `[libraries]`:
```toml
[libraries]
# Pruebas Unitarias (JVM)
junit = { group = "junit", name = "junit", version.ref = "junit" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }

# Pruebas Instrumentadas / UI (Emulador o Teléfono)
androidx-junit = { group = "androidx.test.ext", name = "junit", version = "1.2.1" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version = "3.6.1" }
```

### Paso A.2: Abre `app/build.gradle.kts`
Busca el bloque `dependencies { ... }` al final del archivo y agrega:
```kotlin
dependencies {
    // -------------------------------------------------------------
    // Pruebas Unitarias (Corren en tu PC, ultra rápidas)
    // -------------------------------------------------------------
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // -------------------------------------------------------------
    // Pruebas en Emulador / UI
    // -------------------------------------------------------------
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
```

### Paso A.3: Sincroniza Gradle
Haz clic en el botón **Sync Now** que aparece arriba a la derecha (o en el icono del elefante).

---

## 📜 Opción B: Forma Antigua / Tradicional (Groovy `build.gradle` o sin Catálogo)

Si estás usando un proyecto más antiguo, un curso universitario anterior o una plantilla clásica de StackOverflow sin `libs.versions.toml`, esta es tu opción.

### Paso B.1: Abre `app/build.gradle` (o `app/build.gradle.kts`)
No necesitas tocar ninguna carpeta `gradle/`. Ve directo a `app/build.gradle` dentro del bloque `dependencies { ... }`:

#### Si tu archivo es Groovy (`app/build.gradle` sin `.kts`):
```groovy
dependencies {
    // PRUEBAS UNITARIAS (Local JVM - Rápidas sin emulador)
    testImplementation 'junit:junit:4.13.2'
    testImplementation 'io.mockk:mockk:1.13.13'
    testImplementation 'org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0'
    testImplementation 'app.cash.turbine:turbine:1.2.0'

    // PRUEBAS INSTRUMENTADAS (Emulador / Dispositivo físico)
    androidTestImplementation 'androidx.test.ext:junit:1.2.1'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.6.1'
}
```

#### Si tu archivo es Kotlin DSL (`app/build.gradle.kts`) pero NO tienes catálogo:
```kotlin
dependencies {
    // PRUEBAS UNITARIAS
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("app.cash.turbine:turbine:1.2.0")

    // PRUEBAS INSTRUMENTADAS
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
```

### Paso B.2: Sincroniza Gradle
Pulsa **Sync Now** para que Android Studio descargue las librerías.

---

## 🧪 PARTE 2: Cómo Crear y Escribir tu Primer Test (Paso a Paso)

Ahora que las librerías están instaladas, vamos a crear una función simple y su prueba correspondiente.

### Paso 1: Crea una clase simple que quieras probar
En `app/src/main/java/tu/paquete/`, crea un archivo llamado `ValidadorUsuario.kt`:

```kotlin
package tu.paquete

class ValidadorUsuario {

    fun esEmailValido(email: String): Boolean {
        val emailLimpio = email.trim()
        return emailLimpio.contains("@") && emailLimpio.contains(".")
    }

    fun esClaveSegura(clave: String): Boolean {
        return clave.length >= 6
    }
}
```

---

### Paso 2: El atajo mágico de Android Studio para crear el Test

No crees los archivos de prueba manualmente carpeta por carpeta. Android Studio tiene un atajo automático:

1. Abre tu archivo `ValidadorUsuario.kt`.
2. Coloca el cursor sobre el nombre de la clase `ValidadorUsuario`.
3. Presiona el atajo de teclado:
   * **Windows / Linux:** `Ctrl + Shift + T`
   * **Mac:** `Cmd + Shift + T`
4. Aparecerá un menú emergente. Selecciona **Create New Test...** (Crear nueva prueba).
5. En la ventana emergente:
   * **Testing library:** Elige `JUnit4`.
   * **Class name:** Déjalo en `ValidadorUsuarioTest`.
   * Haz clic en **OK**.
6. Te preguntará en qué carpeta crearlo:
   * Elige la que termina en **`test`** (ej: `app/src/test/java`).
   * *(Ojo: la que termina en `androidTest` es solo para emuladores).*

---

### Paso 3: Escribe tu primer Test con la regla "GIVEN - WHEN - THEN"

Abre el nuevo archivo `ValidadorUsuarioTest.kt` y escribe el siguiente código con explicaciones:

```kotlin
package tu.paquete

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ValidadorUsuarioTest {

    // 1. Declaramos la variable del objeto que vamos a probar
    private lateinit var validador: ValidadorUsuario

    // 2. @Before se ejecuta antes de CADA prueba individual
    @Before
    fun setUp() {
        validador = ValidadorUsuario()
    }

    // 3. Cada método de prueba lleva la anotación @Test
    @Test
    fun emailCorrecto_retornaVerdadero() {
        // GIVEN (Dado): Los datos de entrada
        val correo = "estudiante@universidad.edu"

        // WHEN (Cuando): Ejecutamos la función bajo prueba
        val resultado = validador.esEmailValido(correo)

        // THEN (Entonces): Comprobamos que el resultado sea el esperado
        assertTrue("El email válido debe retornar true", resultado)
    }

    @Test
    fun emailSinArroba_retornaFalso() {
        // GIVEN
        val correoInvalido = "estudianteuniversidad.edu"

        // WHEN
        val resultado = validador.esEmailValido(correoInvalido)

        // THEN
        assertFalse("Un email sin @ debe retornar false", resultado)
    }

    @Test
    fun claveCorta_retornaFalso() {
        // GIVEN: Una contraseña de solo 4 caracteres
        val claveCorta = "1234"

        // WHEN
        val resultado = validador.esClaveSegura(claveCorta)

        // THEN: Esperamos falso porque exigimos mínimo 6 caracteres
        assertFalse(resultado)
    }
}
```

---

### Paso 4: Cómo Ejecutar la Prueba

1. En el margen izquierdo del editor (al lado de `class ValidadorUsuarioTest` o de cada función `@Test`), verás un **pequeño triángulo verde** (▶️).
2. Haz clic en el triángulo verde y selecciona:
   **Run 'ValidadorUsuarioTest'**.
3. En la parte inferior de Android Studio se abrirá la pestaña **Run**:
   * Si todo está correcto, verás un **check verde** (✔️) indicando: `Tests passed: 3 of 3 tests`.
   * Si algo falla, verás una **X roja** (❌) y Android Studio te dirá exactamente en qué línea falló y cuál era el valor esperado vs el obtenido.

---

## ⚡ PARTE 3: Probar Código Asíncrono (Corrutinas / Suspend)

Si tu función tiene la palabra reservada `suspend` (por ejemplo, llama a una API o base de datos):

```kotlin
class RepositorioNotas {
    suspend fun obtenerTotalNotas(): Int {
        // Simula trabajo en segundo plano
        return 10
    }
}
```

En tu archivo de prueba usas **`runTest`** (provisto por `kotlinx.coroutines.test`):

```kotlin
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RepositorioNotasTest {

    @Test
    fun totalNotas_retornaDiez() = runTest {
        val repo = RepositorioNotas()

        // Dentro de runTest puedes llamar a funciones suspend
        val total = repo.obtenerTotalNotas()

        assertEquals(10, total)
    }
}
```

---

## ❓ Preguntas Frecuentes y Errores Típicos de Estudiantes

### 1. "Escribí `@Test` pero sale en rojo"
* **Causa:** No has importado la librería.
* **Solución:** Haz clic sobre `@Test`, presiona `Alt + Enter` (o `Option + Enter` en Mac) y selecciona `import org.junit.Test`.

### 2. "Android Studio dice: Cannot resolve symbol 'testImplementation'"
* **Causa:** Pusiste `testImplementation` fuera del bloque `dependencies { ... }` o escribiste mal el nombre.
* **Solución:** Asegúrate de que esté exactamente dentro de `dependencies { ... }` en el archivo de tu módulo `app`.

### 3. "¿Por qué mi prueba no abre el emulador?"
* **Respuesta:** ¡Porque es una prueba unitaria! Las pruebas en `app/src/test` corren en la máquina virtual Java de tu computadora en fracciones de segundo. Solo las pruebas en `app/src/androidTest` abren el emulador.
