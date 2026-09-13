# Equilibrio — Plan de implementación, versión inicial

## Context

El equipo tiene el proyecto **Equilibrio** (app Android nativa de finanzas personales, materia Desarrollo de Aplicaciones Móviles) documentado a fondo: propuesta aprobada, manual de identidad completo y un plan técnico de 1418 líneas que cubre las tres fases del producto. Lo que **no** existe todavía es una sola línea de código — el directorio del proyecto solo contiene documentación, mockups y el sistema de diseño.

Esta iteración construye la **primera versión funcional mínima**: registrar ingresos y gastos, verlos en una lista, y ver el saldo acumulado. Sin login, sin red, base de datos local en el dispositivo. El indicador de equilibrio (score 0–100), los tips contextuales y el motor de reglas — que son el diferenciador del producto — quedan explícitamente **fuera** de esta iteración y se construyen encima de esta base después.

El esquema de datos ya está decidido y diagramado en Lucidchart (`Proyecto_desarrollo_apps_moviles`): tres tablas, `users` / `accounts` / `transactions`, con PK tipo UUID y columnas de sincronización presentes desde v1 aunque no se usen hoy.

**Resultado esperado:** una app que compila, instala y permite el ciclo completo alta → lista → editar → eliminar → saldo actualizado, sobre una arquitectura por capas que no requiera reescritura cuando lleguen el indicador, las cuentas múltiples y la nube.

---

## Alcance de esta iteración

### Dentro

| Función | Detalle |
|---|---|
| Registrar ingreso | monto, tipo (Fijo / Variable), fecha, cuenta, nota opcional |
| Registrar gasto | monto, clasificación (Esencial / Recreativo), fecha, cuenta, nota opcional |
| Editar movimiento | reutiliza la pantalla de registro precargada |
| Eliminar movimiento | borrado lógico (`is_deleted = 1`), con diálogo de confirmación |
| Saldo acumulado | todos los ingresos − todos los gastos, mostrado en Inicio |
| Lista de movimientos | ingresos y gastos juntos, orden por fecha descendente |

### Fuera (siguiente iteración, sobre esta misma base)

Indicador de equilibrio (score 0–100, bandas, gauge) · motor de reglas y tips contextuales · categorías (será tabla propia) · cuentas múltiples y tarjetas · metas de ahorro · reportes · alertas · autenticación · cifrado SQLCipher · sincronización.

**Se captura la clasificación desde ya** aunque el indicador no exista todavía: así los movimientos registrados en esta versión ya sirven cuando se agregue el cálculo, sin pedirle al usuario que reclasifique su historial.

---

## Arquitectura

Clean Architecture en tres capas con MVVM y flujo unidireccional, según §3 del plan técnico. Módulos Gradle:

| Módulo | Contenido | Depende de |
|---|---|---|
| `:app` | Application, MainActivity, NavHost raíz, wiring de Hilt | todos |
| `:core-ui` | tokens, tema claro/oscuro, componentes atómicos | — |
| `:core-domain` | modelos, enums, casos de uso, interfaces Repository | — (Kotlin puro) |
| `:core-data` | Room, entidades, DAOs, RepositoryImpl, mappers, DataStore | `:core-domain` |
| `:feature-home` | pantalla Inicio | `:core-domain`, `:core-ui` |
| `:feature-entry` | pantalla Registro rápido | `:core-domain`, `:core-ui` |

**Regla dura:** `:core-domain` no importa nada de Android. `:feature-*` nunca importa `:core-data`.

Configuración: `mx.equilibrio.app`, Kotlin DSL, version catalog (`gradle/libs.versions.toml`), `minSdk 26`, `targetSdk`/`compileSdk 36`.

---

## Trabajo por módulo

### 1. Scaffold del proyecto

Crear la estructura Gradle multi-módulo, el version catalog y `git init` con primer commit. Dependencias: Compose BOM, Material 3, Navigation Compose, Hilt, Room (KSP), DataStore, kotlinx-datetime, JUnit, Turbine, Room-testing, Compose UI Test.

**Verificable:** `./gradlew build` compila los seis módulos vacíos.

### 2. `:core-domain` — modelos y contratos

```kotlin
enum class TransactionKind { INCOME, EXPENSE }

enum class Classification { FIXED, VARIABLE, ESSENTIAL, RECREATIONAL }

fun Classification.isValidFor(kind: TransactionKind): Boolean
```

`Transaction` valida el par `(kind, classification)` en su constructor — un movimiento inválido no puede existir en memoria. `classification` es nullable por diseño.

Interfaz de repositorio (la UI y los casos de uso solo conocen esto):

```kotlin
interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeBalanceCents(): Flow<Long>
    suspend fun getById(id: String): Transaction?
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: String)
}
```

Casos de uso: `ObserveTransactions`, `ObserveBalance`, `SaveTransaction`, `DeleteTransaction`, `GetTransaction`.

**Verificable:** tests unitarios de `isValidFor` (los 8 pares: 4 válidos, 4 inválidos) y de la validación del constructor. Sin instrumentación.

### 3. `:core-data` — persistencia

Room con `exportSchema = true`, esquemas versionados en `core-data/schemas/` bajo control de versiones. `fallbackToDestructiveMigration()` prohibido.

Tres entidades espejo del ERD ya aprobado. `TransactionDao` con:

```kotlin
@Query("SELECT * FROM transactions WHERE user_id = :userId AND is_deleted = 0 ORDER BY occurred_at DESC")
fun observeAll(userId: String): Flow<List<TransactionEntity>>

@Query("""
    SELECT COALESCE(SUM(CASE WHEN kind = 'INCOME' THEN amount_cents ELSE -amount_cents END), 0)
    FROM transactions WHERE user_id = :userId AND is_deleted = 0
""")
fun observeBalanceCents(userId: String): Flow<Long>

@Upsert suspend fun upsert(entity: TransactionEntity)

@Query("UPDATE transactions SET is_deleted = 1, sync_state = 'PENDING', updated_at = :now WHERE id = :id")
suspend fun markDeleted(id: String, now: Long)
```

El saldo lo agrega SQLite, no Kotlin — un solo viaje a la base aunque el historial crezca.

`LocalUserProvider` (DataStore): en el primer arranque genera un `userId` UUID y crea la cuenta `CASH` "Efectivo". Idempotente.

Mappers entidad ↔ dominio. `TransactionRepositoryImpl` puebla siempre `updatedAt`, `syncState = PENDING`, `isDeleted = false` en cada escritura.

**Verificable:** tests de DAO con `Room.inMemoryDatabaseBuilder` — upsert, `observeAll` excluyendo borrados, `observeBalanceCents` con mezcla de ingresos y gastos, `markDeleted` desapareciendo de la lista. Test de mapper ida y vuelta.

### 4. `:core-ui` — sistema de diseño

Traducción literal del manual de identidad. Tokens en `Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt`, `Motion.kt`; tema claro/oscuro provisto por `CompositionLocal`, no `isSystemInDarkTheme()` disperso.

Los dos enums cerrados que hacen imposible violar la identidad por tipos:

```kotlin
enum class DomainTone { INCOME_FIXED, INCOME_VARIABLE, ESSENTIAL, RECREATIONAL, NEUTRAL }
enum class FeedbackTone { SUCCESS, WARNING, ERROR, INFO }
```

Ningún componente acepta ambos. `EqAmount` / `EqBadge` / `EqChip` reciben `DomainTone`; `EqSnackbar` / `EqDialog` / `EqInlineValidation` reciben `FeedbackTone`. Ningún `Color` crudo cruza la frontera del módulo.

Componentes de esta iteración: `EqAmount` (cifras tabulares siempre), `EqBadge`, `EqCard`, `EqButton`, `EqTextField`, `EqSegmentedControl`, `EqInlineValidation`, `EqSnackbar`, `EqDialog`, `EqEmptyState`, `EqTopBar`, `EqFab`. Cada uno con `@Preview` en claro y oscuro.

Reglas normativas que el código hace cumplir, no que documenta:
- `EqBadge` con relleno suave usa siempre verde profundo como texto — la combinación que falla contraste AA es imposible de producir.
- Un movimiento sin clasificar recibe insignia neutra, nunca verde ni morada.
- Ninguna curva de animación rebota.
- Área táctil mínima 48×48dp; filas de lista mínimo 56dp.

**Verificable:** previews renderizan en Android Studio en ambos temas.

### 5. `:feature-entry` — Registro rápido

Sirve para alta y para edición: recibe un `transactionId` nullable por navegación.

```kotlin
data class QuickEntryUiState(
    val kind: TransactionKind = TransactionKind.EXPENSE,
    val amountInput: String = "",
    val classification: Classification? = null,
    val accountId: String? = null,
    val occurredAt: LocalDate = today(),
    val note: String = "",
    val amountError: String? = null,
    val dateError: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
)
```

Reglas de comportamiento:
1. `KindChanged` resetea `classification` a `null` — el par inválido no es representable.
2. `canSave = amount > 0 && classification != null && accountId != null`. Deshabilitado es el estado por defecto.
3. Ninguna clasificación viene preseleccionada. El usuario debe tocar.
4. El monto se parsea a **centavos** (`Long`), nunca a `Double`. `"240.50"` → `24050L`.
5. Fecha por defecto hoy, editable al pasado, **futuro bloqueado** con validación inline.
6. Al guardar: persiste y vuelve a Inicio.

**Verificable:** UI tests — Guardar deshabilitado sin monto; cambiar Gasto→Ingreso resetea clasificación; fecha futura muestra validación; guardar persiste y navega.

### 6. `:feature-home` — Inicio

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val balanceCents: Long = 0,
    val transactions: List<TransactionUi> = emptyList(),
    val isEmpty: Boolean = false,
    val pendingDeletion: TransactionUi? = null,
)
```

Composición: `EqTopBar` → tarjeta de saldo (`EqAmount` grande, tabular) → `LazyColumn` de movimientos con `key` estable → `EqFab`.

Cada fila: insignia de clasificación (neutra si `null`), nota o etiqueta del tipo, fecha, monto con signo y tono de dominio. Tap abre edición; long-press o icono abre confirmación de borrado.

Estados no felices: skeleton con la forma de las tarjetas reales durante la carga (no spinner); estado vacío con texto e invitación a registrar el primer movimiento — **nunca un cero**.

El saldo y la lista se recomponen solos: el `Flow` del DAO emite tras cada escritura. No hay recálculo manual.

**Verificable:** UI tests — estado vacío sin datos; el saldo refleja ingresos menos gastos; eliminar pide confirmación y la fila desaparece.

### 7. Navegación y cierre

`NavHost` con dos destinos type-safe: `Home` y `QuickEntry(transactionId: String?)`. Hilt conectado punta a punta. Strings en `strings.xml`, ninguno literal en composables, tono sin exclamaciones y sin culpar al usuario.

---

## Orden de ejecución

La lógica de dominio y los datos van **antes** que la UI, con sus tests en verde, porque son la base sobre la que se monta el indicador después.

1. Scaffold + version catalog → compila vacío
2. `:core-domain` modelos y contratos → tests en verde
3. `:core-data` Room, DAOs, repositorio → tests de DAO en verde
4. `:core-ui` tokens y tema → previews de la paleta
5. `:core-ui` componentes → previews de cada componente
6. `:feature-entry` → guarda y persiste
7. `:feature-home` → saldo y lista reactivos
8. Navegación, edición, borrado → flujo completo
9. Estados vacío / carga / confirmación → pulido

---

## Verificación end-to-end

Antes de dar la iteración por cerrada:

```
./gradlew build                    # los seis módulos compilan
./gradlew testDebugUnitTest        # dominio y datos en verde
./gradlew connectedDebugAndroidTest # UI tests en emulador API 26
```

Recorrido manual en el emulador:

1. Primer arranque → Inicio muestra estado vacío, sin cero.
2. Agregar ingreso Fijo de $5,000 → saldo muestra $5,000.00, la fila aparece con insignia verde.
3. Agregar gasto Recreativo de $500 → saldo baja a $4,500.00 sin tocar nada, fila con insignia morada.
4. Editar el gasto a $800 → saldo pasa a $4,200.00.
5. Eliminar el gasto → pide confirmación, la fila desaparece, saldo vuelve a $5,000.00.
6. Cerrar y reabrir la app → los datos siguen ahí.
7. Intentar guardar sin monto → botón deshabilitado, validación inline visible.
8. Intentar fecha futura → validación inline, no guarda.
9. Alternar Gasto/Ingreso → la clasificación se limpia, el toggle cambia sus opciones.
10. Modo oscuro del sistema → los acentos se aclaran manteniendo su tono, nada ilegible.

Revisión visual contra el manual: ningún color de estado rellena una insignia o un monto; ningún color de dominio aparece en un mensaje del sistema; los movimientos sin clasificar muestran insignia neutra; todos los montos usan cifras tabulares; ninguna animación rebota; todo elemento tocable mide al menos 48×48dp.
