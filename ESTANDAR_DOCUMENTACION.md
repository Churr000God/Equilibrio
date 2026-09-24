# Estándar de Documentación y Comentarios de Código

> Adaptado a partir del estándar pedagógico de `Clase_01`-`Clase_16` / `ed_proyecto` (Java) para el proyecto **Equilibrio**: app Android multi-módulo en Kotlin con arquitectura limpia (`core-domain`, `core-data`, `core-ui`, `feature-*`). Se conserva la filosofía original — comentar el **por qué**, no el qué — pero se adapta la sintaxis y se eliminan convenciones que no son idiomáticas en Kotlin (cierres de bloque, etiquetado forzado de cada rama) para no ensuciar un código que ya es conciso.

---

## 1. Filosofía general

Los comentarios no describen *qué* hace la línea (eso ya lo dice el código, con nombres explícitos y tipos), describen:

- **Por qué** existe una regla de negocio, y qué pasaría si no estuviera.
- **Qué caso cubre** dentro del dominio (finanzas personales: cuentas, transacciones, tarjetas de crédito, periodos, metas, reportes).
- **Qué rol cumple** la clase/archivo dentro de la arquitectura (¿es un modelo de dominio, un caso de uso, un repositorio, una pantalla Compose, un ViewModel?) y quién la consume.

Reglas base:

- Idioma: español, tono técnico y directo, igual que el código existente del repo.
- KDoc (`/** ... */`) para clases y funciones públicas cuya intención, invariante o efecto secundario no sea obvio solo con el nombre y la firma.
- `//` de una línea para aclarar una decisión puntual dentro del cuerpo de una función (una regla de negocio, un caso límite, una fórmula).
- **Nunca comentar lo obvio sintácticamente** (`val total = a + b //suma a y b` está prohibido). Si el nombre ya lo dice, no se repite en un comentario.
- No se documentan getters/setters de `data class` (son autogenerados por Kotlin) ni propiedades cuyo nombre y tipo ya son autoexplicativos.
- Si una función reproduce una fórmula financiera, una regla de negocio no trivial (periodos de corte, freemium, streaks de ahorro) o una convención de la arquitectura del proyecto, se nombra explícitamente.

**No se usan** (a diferencia del estándar original en Java):
- Comentarios de cierre de bloque (`//Cierre X` / `//cierra if`): los bloques en Kotlin son cortos y el IDE ya resalta el `}` correspondiente.
- Etiquetado obligatorio de cada rama `if/else` con "Caso 1/Caso 2": solo se etiqueta cuando la rama representa una regla de negocio real (ver §4).
- Línea de autoría por archivo: el repo usa control de versiones (`git blame`) para eso.
- Recordatorios de `.equals()` vs `==`: en Kotlin `==` ya invoca `equals()` estructuralmente, no aplica la trampa del Java original.

---

## 2. Encabezado de archivo / clase

Todo archivo cuya intención no sea evidente por su nombre y ubicación en el paquete lleva un KDoc de clase (o de la función principal si es un archivo de top-level functions) explicando:

- **Rol** dentro de la arquitectura: ¿modelo de dominio, regla de validación, caso de uso, repositorio (interfaz o implementación), DAO/entity de Room, mapper, ViewModel, pantalla/composable, DTO de red?
- **Invariante** que mantiene o **restricción de negocio** que aplica.
- **Quién la usa** y, si existe, **con qué otra clase no debe confundirse** (patrón ya usado en el repo, ver `SaveTransaction.kt`: aclara que no aplica a `CreateTransfer`/`PayCreditPeriod`).

```kotlin
package mx.equilibrio.domain.usecase

/**
 * Cierra los periodos vencidos y genera las transacciones recurrentes pendientes
 * a la fecha [today]. Es el único punto de entrada para "avanzar el calendario":
 * ni [GetOrCreatePeriodForDate] ni el resto de los casos de uso deben duplicar
 * esta lógica de cierre.
 */
class SettleDuePeriods @Inject constructor(...) { ... }
```

No hace falta KDoc de clase cuando:
- Es un `data class`/`sealed interface`/`enum class` simple cuyo nombre y campos ya explican el modelo (ver §3).
- Es una función de extensión trivial de una línea (ver `ReportsStrings.kt`: `label()`, `shortLabel()`).

---

## 3. Modelos de dominio (`data class`, `sealed interface`, `enum class`)

- Si el nombre de la clase y de cada campo ya son autoexplicativos, **no se documenta** (ejemplo: `ReportsUiState` no necesita KDoc si `month`, `isLoading`, `report`, `canGoForward` se entienden solos).
- Se documenta cuando:
  - Un campo tiene una **unidad o convención no obvia** (p. ej. montos en centavos: `incomeCents`, `leftoverCents` — aclarar una sola vez por archivo que los montos se manejan en centavos para evitar errores de punto flotante).
  - Un valor especial tiene semántica (`null` = "sin categoría", `-1` = "sin límite", etc.).
  - Un `enum class` representa estados de una máquina de estados (`PeriodState`, `GoalStatus`, `TransactionStatus`): se documenta brevemente qué representa cada valor y, si existen, las transiciones válidas.
- Propiedades derivadas (`val isEmpty: Boolean get() = ...`) se documentan si el cálculo no es obvio a simple vista.

```kotlin
enum class TransactionStatus {
    /** Aún no impacta el balance disponible; puede tener fecha futura. */
    PENDING,
    /** Ya impactó el balance; no puede moverse a una fecha futura (ver [requireNoFutureConfirmedDate]). */
    COMPLETED,
}
```

---

## 4. Casos de uso (`core-domain/usecase`)

Cada caso de uso es una regla de negocio aislada. El KDoc de clase explica:

1. Qué hace en una frase.
2. La regla de negocio que protege (qué error evita, qué invariante no puede romperse).
3. Con qué caso de uso "hermano" no debe confundirse cuando el dominio tiene varios caminos parecidos (gasto normal vs. compra con tarjeta, transferencia vs. transacción, etc. — ver `SaveTransaction.kt` como ejemplo ya existente en el repo).

Dentro del cuerpo del método, cada `require`/`check`/rama que aplica una regla de negocio lleva un comentario corto explicando **qué caso de negocio previene**, no describiendo la condición:

```kotlin
require(account == null || account.type != AccountType.CREDIT_CARD) {
    "No se puede guardar un ${transaction.kind} directo contra una cuenta CREDIT_CARD; usá SaveCreditPurchase."
}
```
Aquí el mensaje de la excepción ya documenta el caso — no hace falta comentario adicional. Si el `require`/`if` no trae su propio mensaje descriptivo, se agrega un comentario de una línea antes explicando la regla.

Para lógica con varias ramas mutuamente excluyentes que representan escenarios de negocio distintos (no simple validación), sí se etiqueta el caso, igual que el estándar original:

```kotlin
when {
    matrix.isEmpty -> // Caso sin datos: no hay nada que cruzar todavía
    ...
}
```

---

## 5. Repositorios y capa de datos (`core-data`)

- **Interfaces de repositorio** (`core-domain/repository`): KDoc solo si el contrato tiene una sutileza (p. ej. qué significa que un `Flow` emita `null`, si una escritura es upsert o falla si ya existe, si una consulta filtra automáticamente elementos borrados/archivados).
- **Implementaciones** (`core-data`): se documenta cuando la implementación traduce entre el modelo de dominio y el de persistencia (Room entity, DTO de red) de forma no trivial, o cuando combina/cachea varias fuentes.
- **DAOs de Room**: las queries SQL no triviales (joins, agregaciones, filtros de fecha) llevan un comentario de una línea explicando qué devuelven en términos de negocio, no repitiendo el SQL.
- **Mappers** (`toDomain()`, `toEntity()`): se documentan solo si hay una conversión no obvia (cálculo derivado, valor por defecto, normalización).

---

## 6. ViewModels y estado de UI (`feature-*`)

- El `UiState` (`data class`) sigue la regla de §3: se documenta solo lo que no es obvio (p. ej. un flag que combina dos condiciones, como `ReportsUiState.isEmpty`).
- Los eventos (`sealed interface XxxEvent`) generalmente no necesitan documentación si los nombres son claros (`PreviousMonth`, `NextMonth`).
- El `ViewModel` lleva KDoc de clase solo si orquesta algo no evidente (combina varios flows, aplica debounce, cachea, tiene un efecto secundario que no se ve en la firma). Si es un simple `map`/`flatMapLatest` de un único caso de uso (como `ReportsViewModel`), no hace falta.
- Dentro de `onEvent`, comentar solo la lógica que no sea un `update` directo (p. ej. por qué `NextMonth` no avanza si ya se llegó al mes actual).

---

## 7. Pantallas Compose (`core-ui`, `feature-*`)

- Los `@Composable` públicos que representan una pantalla completa llevan KDoc solo si reciben parámetros cuyo efecto no es obvio (p. ej. `focusSection` en `ReportsScreen`: qué pasa si viene `null`, cuándo se usa).
- Los `@Composable` privados (sub-bloques de una pantalla: tarjetas, filas, headers) no requieren KDoc; alcanza con nombres descriptivos (`MatrixCard`, `SummaryTiles`).
- Comentar inline cuando:
  - Un modifier o efecto (`LaunchedEffect`, `clipToBounds()`, `derivedStateOf`) resuelve un problema visual o de UX concreto que no se entiende con solo leer la línea (el repo ya hace esto: ver comentarios de `LaunchedEffect(focusSection, ...)` y `clipToBounds()` en `ReportsScreen.kt` — mantener ese estilo).
  - Un valor mágico (`MAX_CATEGORIES = 5`, paddings específicos con una razón de diseño) tiene una razón de producto detrás.
- Textos de copy/tono de UI (mensajes vacíos, textos de ayuda): si el proyecto tiene una guía de tono (como el `§12` referenciado en `ReportsStrings.kt`), se cita esa sección en vez de repetir la regla.

---

## 8. Fórmulas y cálculos numéricos

Toda fórmula financiera no trivial (deltas de periodo, participación porcentual en una categoría, cálculo de racha de ahorro, matriz cruzada esencial/recreativo) lleva un comentario inmediatamente antes explicando en palabras qué representa cada término, igual que el estándar original:

```kotlin
// % del total que representa esta categoría; category.cents puede ser 0 si el mes no tuvo gasto ahí
val share = if (total == 0L) 0f else category.cents.toFloat() / total
```

---

## 9. Estructura recomendada de un archivo nuevo

```kotlin
package mx.equilibrio.domain.usecase

/**
 * Qué hace en una frase + qué regla de negocio protege + con qué caso de uso
 * hermano no confundirse (si aplica).
 */
class MiCasoDeUso @Inject constructor(
    private val repository: MiRepository,
) {
    suspend operator fun invoke(param: Tipo) {
        // Regla de negocio 1: por qué se valida esto
        require(condicion) { "Mensaje descriptivo del caso que previene" }

        // Fórmula/cálculo no trivial, si lo hay
        val resultado = ...

        repository.upsert(resultado)
    }
}
```

---

## 10. Checklist rápido antes de dar por terminado un archivo

- [ ] La clase/archivo tiene KDoc si su rol, invariante o relación con clases "hermanas" no es obvio por el nombre.
- [ ] No hay KDoc/comentarios redundantes en modelos cuyo nombre ya lo dice todo.
- [ ] Cada `require`/`check`/validación de negocio sin mensaje descriptivo propio tiene un comentario de una línea explicando qué caso previene.
- [ ] Ramas de negocio con varios escenarios mutuamente excluyentes están etiquetadas por el caso que cubren (no cada `if` trivial).
- [ ] Fórmulas y cálculos numéricos no triviales explican qué representa cada término.
- [ ] Unidades o convenciones no obvias (centavos, `null` con semántica especial) están aclaradas donde aparecen por primera vez en el archivo.
- [ ] Comentarios de UI (`LaunchedEffect`, modifiers no triviales, valores mágicos) explican el problema de UX/producto que resuelven.
- [ ] Sin comentarios que solo repitan la sintaxis o el nombre de la variable/función.
- [ ] Sin `//Cierre X`, sin etiquetado forzado `Caso 1/Caso 2` en validaciones simples, sin línea de autoría (no aplica a este proyecto).
