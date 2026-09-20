# Equilibrio — Plan de Integración y Desarrollo

**Aplicación Android nativa de finanzas personales**
Desarrollo de Aplicaciones Móviles · Documento técnico de integración
Fecha: 12 de septiembre de 2026

**Fuentes normativas de este documento**

| Documento | Aporta |
|---|---|
| `Equilibrio.pdf` | Propósito, usuario objetivo, diferenciador, alcance, RF01–RF10, RNF01–RNF08, modelo de negocio, riesgos, 5 pantallas |
| `Equilibrio — Manual de identidad.pdf` | Sistema visual completo (17 secciones): principio rector, paleta, tokens semánticos, accesibilidad, modo oscuro, tipografía, espaciado, radios, movimiento, iconografía, errores, alertas, tono de voz, estados, tokens Compose |

**Equipo:** Diego Hermilo Guillén García (AL07018240) · Santiago Gutiérrez Rivera (AL03094306) · Christopher Eduardo Soto Betancourt (AL07098518) · Abraham Radahi Bautista Triana (AL07124721)

---

## Índice

1. [Propósito y principio rector](#1-propósito-y-principio-rector)
2. [Alcance por fases](#2-alcance-por-fases)
3. [Arquitectura](#3-arquitectura)
4. [Modelo de datos](#4-modelo-de-datos)
5. [Algoritmo del indicador de equilibrio](#5-algoritmo-del-indicador-de-equilibrio)
6. [Motor de tips y alertas](#6-motor-de-tips-y-alertas)
7. [Requisitos funcionales](#7-requisitos-funcionales)
8. [Requisitos no funcionales](#8-requisitos-no-funcionales)
9. [Design system en Compose](#9-design-system-en-compose)
10. [Especificación de las 2 pantallas de Fase 1](#10-especificación-de-las-2-pantallas-de-fase-1)
11. [Accesibilidad](#11-accesibilidad)
12. [Tono de voz aplicado a strings](#12-tono-de-voz-aplicado-a-strings)
13. [Plan de pruebas](#13-plan-de-pruebas)
14. [Configuración del proyecto en Android Studio](#14-configuración-del-proyecto-en-android-studio)
15. [Riesgos y mitigación](#15-riesgos-y-mitigación)
16. [Matriz de trazabilidad](#16-matriz-de-trazabilidad)

---

## 1. Propósito y principio rector

### 1.1 Qué es Equilibrio

Equilibrio es una aplicación móvil de finanzas personales cuyo núcleo **no es el registro de movimientos**, sino el cruce entre dos clasificaciones que ninguna herramienta accesible combina hoy:

```
        TIPO DE INGRESO  ×  TIPO DE GASTO
        ───────────────     ─────────────
        Fijo                Esencial
        Variable            Recreativo
```

Cada ingreso se clasifica como **fijo** o **variable**; cada gasto, como **esencial** o **recreativo**. El sistema cruza ambas clasificaciones en un **indicador de equilibrio financiero**: un solo número que resume qué tan sano es el balance entre cómo entra el dinero y cómo sale. De ese indicador se derivan consejos contextuales inmediatos y reportes concisos.

### 1.2 El problema

Las herramientas generales registran ingresos y gastos como una lista plana. Muestran cuánto entró y cuánto salió, pero **no relacionan de dónde viene el dinero con en qué se va**. Un ingreso variable financiando gasto recreativo no es la misma situación financiera que un ingreso fijo cubriendo gasto esencial, y en un historial convencional ambas se ven idénticas.

Consecuencia: la señal de riesgo llega tarde. La persona nota el desequilibrio cuando ya comprometió el mes, no cuando todavía podía corregirlo.

### 1.3 Usuario objetivo

Persona joven —estudiante o profesional en inicio de carrera— **sin experiencia previa administrando sus finanzas**, que combina un ingreso fijo (mesada, sueldo, beca) con ingresos variables (trabajos independientes, propinas, ventas ocasionales). No debe exigírsele vocabulario financiero previo ni configuración compleja.

**Implicación de producto:** la educación ocurre *dentro del flujo de uso*, nunca en un módulo separado de lecciones. Cada registro puede generar un tip específico a esa transacción.

### 1.4 Diferenciador

El valor no está en la cantidad de funciones sino en el cruce que ninguna app accesible ofrece: **tipo de ingreso × tipo de gasto, resuelto en un indicador único y acompañado de un consejo en el instante del registro**. Mint, Fintonic o YNAB acumulan historial; Equilibrio lo interpreta en el momento en que ocurre.

### 1.5 Principio rector — restricción de arquitectura, no de estilo

> **El color comunica la clasificación antes que el texto.**
> Verde es ingreso y gasto esencial. Morado es gasto recreativo y crédito.

Esta regla del manual de identidad (§01) se eleva en este plan a **invariante de código**, no a recomendación visual. De ella se derivan tres restricciones duras que la implementación debe hacer imposibles de violar:

| # | Invariante | Cómo se hace cumplir en código |
|---|---|---|
| **I1** | Los colores de dominio (verde/morado) **nunca** aparecen en un mensaje del sistema | Los composables de feedback (`EqBanner`, `EqSnackbar`, `EqInlineError`, `EqDialog`) solo aceptan un parámetro de tipo `FeedbackTone`, enum cerrado que no expone colores de dominio |
| **I2** | Los colores de estado (error/advertencia/info) **nunca** rellenan un chip, insignia, monto o segmento de gráfica | `EqBadge`, `EqAmount`, `EqChip` y `EqChartSegment` solo aceptan `DomainTone`, enum cerrado sin colores de estado |
| **I3** | Un movimiento **nunca** recibe insignia verde o morada hasta que el usuario lo clasificó | `Transaction.classification` es de tipo `Classification?`; `null` renderiza insignia neutra. No existe valor por defecto |

Un `Color` crudo nunca se pasa a un componente de la capa de UI: los componentes reciben tokens tipados. Un lint rule de detekt/ktlint prohíbe `Color(0x...)` fuera del módulo `:core-ui`.

---

## 2. Alcance por fases

### 2.1 Fase 1 — Entrega actual

**Objetivo:** demostrar el diferenciador completo end-to-end con la mínima superficie posible.

- **2 pantallas:** Inicio y Registro rápido.
- **Persistencia:** Room, 100 % local en el dispositivo.
- **Sin autenticación:** usuario local único con `userId` fijo generado en el primer arranque.
- **Sin red:** la app no realiza ninguna llamada HTTP.
- El indicador de equilibrio, la clasificación cruzada y los tips contextuales están **completos y funcionales**, porque son el diferenciador.

### 2.2 Fase 2 — Resto del alcance del semestre

- Cuentas y tarjetas (RF04) + pantalla de gestión.
- Metas de ahorro (RF08) + pantalla de metas.
- Reportes (RF06) + pantalla de reportes.
- Alertas del producto (RF09).
- Autenticación local (RF01).
- Límites del plan gratuito (RF10).

### 2.3 Fase 3 — Trabajo futuro

- Autenticación remota real y cuentas en la nube.
- Sincronización bidireccional local-first.
- Mascota de la aplicación (alertas ilustradas en gratuito, asistente conversacional en Pro).
- Reportes históricos comparativos extensos.
- Multi-moneda.
- Conexión bancaria automática.

### 2.4 Qué queda fuera de Fase 1 y por qué

| Fuera de Fase 1 | Razón | Cómo se evita que bloquee después |
|---|---|---|
| Autenticación | Añadiría una tercera pantalla sin aportar al diferenciador | El esquema ya lleva `userId` en toda entidad; activar auth es poblar ese campo con un id real, no migrar |
| Sincronización en nube | Requiere backend, que no existe aún | `Repository` es una interfaz de dominio; hoy la implementa solo `LocalDataSource`. Añadir `RemoteDataSource` no toca la UI ni el dominio |
| Cuentas y tarjetas | RF04 es Fase 2 | La entidad `Account` **sí se crea en Fase 1** con una cuenta por defecto ("Efectivo"), para que `Transaction.accountId` nunca sea nulo y no haya migración al llegar RF04 |
| Reportes y metas | Fase 2 | Las entidades `Goal` y las consultas agregadas se definen ya en el esquema v1 |
| Límite freemium | Fase 2 | `PlanTier` se modela desde v1 con valor `FREE` fijo |

> **Decisión clave:** el *esquema de datos* de Fase 1 ya contempla las tres fases. Solo la *superficie de UI* y los *casos de uso* crecen por fase. Esto evita migraciones destructivas de Room.

### 2.5 Estado de avance de Fase 2 (actualizado 2026-09-19)

| RF04 — Cuentas y tarjetas | Estado |
|---|---|
| Alta/gestión de cuentas, categorías | Hecho |
| Disponible calculado desde movimientos (efectivo/débito) | Hecho |
| Transferencias entre cuentas (efectivo/débito, nunca crédito) | Hecho |
| Movimientos programados a futuro (estado COMPLETED/SCHEDULED) | Hecho |
| Estados/transacciones propias de tarjeta de crédito | Pendiente |
| RF01 — Autenticación local | En progreso (login con Google, perfil) |
| RF08 — Metas de ahorro | Pendiente |
| RF06 — Reportes | Pendiente |
| RF09 — Alertas del producto | Modelo de datos hecho, UI pendiente |
| RF10 — Límites del plan gratuito | Modelo de datos hecho, UI pendiente |

Detalle sesión a sesión en `CHANGELOG.md`.

---

## 3. Arquitectura

### 3.1 Vista general

Clean Architecture en tres capas (cumple **RNF06 — Mantenibilidad: arquitectura organizada por capas de datos, lógica de negocio y presentación**), con MVVM y flujo de datos unidireccional (UDF) en la capa de presentación.

```
┌──────────────────────────────────────────────────────────────┐
│  PRESENTACIÓN            :feature-home  :feature-entry       │
│  Compose · ViewModel · UiState · UiEvent · Navigation        │
│  Depende de: dominio                                          │
└───────────────────────────┬──────────────────────────────────┘
                            │ (modelos de dominio, no entidades Room)
┌───────────────────────────▼──────────────────────────────────┐
│  DOMINIO                 :core-domain                        │
│  Modelos puros · UseCases · interfaces Repository            │
│  Kotlin puro. Cero dependencias de Android, Room o red.      │
└───────────────────────────┬──────────────────────────────────┘
                            │ (implementa las interfaces)
┌───────────────────────────▼──────────────────────────────────┐
│  DATOS                   :core-data                          │
│  RepositoryImpl · LocalDataSource (Room) · Mappers           │
│                          [ RemoteDataSource — Fase 3 ]       │
└──────────────────────────────────────────────────────────────┘

              :core-ui  — design system, sin lógica
```

### 3.2 Módulos Gradle

| Módulo | Contenido | Depende de |
|---|---|---|
| `:app` | `Application`, `MainActivity`, grafo de navegación raíz, wiring de Hilt | todos |
| `:core-ui` | Tokens (`Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt`, `Motion.kt`), tema, componentes atómicos (`EqCard`, `EqBadge`, `EqAmount`, `EqGauge`, `EqButton`, `EqTextField`, `EqChip`, `EqBanner`, `EqSnackbar`) | — |
| `:core-domain` | Modelos, `UseCase`s, interfaces `Repository`, `BalanceCalculator`, `RuleEngine` | — |
| `:core-data` | Room (`EquilibrioDatabase`, entidades, DAOs), `RepositoryImpl`, mappers, `DataStore` de preferencias | `:core-domain` |
| `:feature-home` | Pantalla Inicio | `:core-domain`, `:core-ui` |
| `:feature-entry` | Pantalla Registro rápido | `:core-domain`, `:core-ui` |
| `:feature-accounts` | Pantalla Cuentas (carrusel de tarjetas + alta de cuenta) — adelantada de Fase 2, implementada ya | `:core-domain`, `:core-ui` |
| `:feature-categories` | Pantalla Categorías (listado + alta) — adelantada de Fase 2, implementada ya | `:core-domain`, `:core-ui` |
| *(Fase 2)* `:feature-goals`, `:feature-reports` | | igual patrón |

**Regla de dependencia:** `:core-domain` no depende de nada. `:feature-*` nunca importa `:core-data`. Se verifica con una tarea Gradle de comprobación de dependencias en CI.

### 3.3 Stack técnico

| Área | Elección | Justificación |
|---|---|---|
| Lenguaje | **Kotlin** | RNF07 lo exige explícitamente |
| UI | **Jetpack Compose** + Material 3 | El manual entrega tokens ya en formato Compose (§17) |
| Inyección | **Hilt** | Estándar de Android, soporta módulos multi-Gradle |
| Asincronía | **Coroutines + Flow** | El indicador debe recalcularse reactivamente ante cada movimiento |
| Persistencia | **Room** | RNF03 pide respuesta casi en tiempo real con datos locales |
| Preferencias | **DataStore (Proto)** | Tema claro/oscuro, `userId` local, plan vigente |
| Navegación | **Navigation Compose** con rutas type-safe | 2 destinos ahora, 5+ después |
| Fechas | **kotlinx-datetime** | Cálculo de periodos sin depender del desugaring de `java.time` en API bajas |
| Dinero | **`Long` en centavos** + formateo con `NumberFormat` | Nunca `Double` para dinero |
| Tests | JUnit5, Turbine, Room in-memory, Compose UI Test | §13 |

### 3.4 El punto de extensión a la nube

Este es el requisito arquitectónico central pedido: **base local hoy, nube después, sin reescritura**.

```kotlin
// :core-domain — la UI y los UseCases solo conocen esto
interface TransactionRepository {
    fun observeByPeriod(period: Period): Flow<List<Transaction>>
    suspend fun upsert(transaction: Transaction)
    suspend fun delete(id: TransactionId)
}
```

```kotlin
// :core-data — Fase 1
class TransactionRepositoryImpl(
    private val local: TransactionLocalDataSource,
    // Fase 3: private val remote: TransactionRemoteDataSource,
    // Fase 3: private val syncScheduler: SyncScheduler,
) : TransactionRepository {

    override fun observeByPeriod(period: Period): Flow<List<Transaction>> =
        local.observeByPeriod(period).map { it.map(TransactionEntity::toDomain) }

    override suspend fun upsert(transaction: Transaction) {
        // local-first: la escritura local es la que confirma al usuario
        local.upsert(transaction.toEntity(syncState = SyncState.PENDING))
        // Fase 3: syncScheduler.enqueue(transaction.id)
    }

    override suspend fun delete(id: TransactionId) {
        // borrado lógico, no físico — indispensable para sincronizar borrados
        local.markDeleted(id, updatedAt = Clock.System.now())
        // Fase 3: syncScheduler.enqueue(id)
    }
}
```

**Cuatro decisiones tomadas hoy que hacen posible la nube mañana sin migración destructiva:**

1. **PK = UUID (`String`), no autoincrement.** Dos dispositivos offline pueden crear registros sin colisionar. Un `Long` autoincremental haría imposible la fusión.
2. **`userId` en toda entidad desde v1.** Hoy vale un UUID local fijo. Al llegar la auth real, se reasigna una vez; el esquema no cambia.
3. **`updatedAt: Instant` en toda entidad.** Base para resolución de conflictos *last-write-wins* por campo o por registro.
4. **`syncState: SyncState` (`SYNCED` / `PENDING` / `CONFLICT`) y `isDeleted: Boolean`.** En Fase 1 todo queda `PENDING` y nadie lo lee — pero la columna existe. En Fase 3, `WorkManager` recorre lo pendiente. Un borrado físico en Fase 1 sería un borrado que la nube nunca conocería.

> Costo de estas cuatro decisiones en Fase 1: cuatro columnas que no se usan. Costo de omitirlas: una migración de Room con reasignación de todas las claves primarias y pérdida de historial. Se toman ahora.

**Estrategia de sincronización prevista (Fase 3, documentada aquí para que el esquema la soporte):** local-first con `WorkManager`. El usuario escribe siempre local; un worker con backoff exponencial empuja lo `PENDING` y baja cambios remotos por `updatedAt > lastSyncAt`. Conflicto = ambos lados modificados desde el último sync → gana el `updatedAt` mayor, y el perdedor se conserva en una tabla `conflict_log` para no perder datos financieros en silencio.

### 3.5 Flujo de datos unidireccional en presentación

```
Usuario ──evento──▶ ViewModel ──llama──▶ UseCase ──▶ Repository ──▶ Room
                        ▲                                            │
                        └────────── Flow<Domain> ────────────────────┘
                        │
                   StateFlow<UiState>
                        │
                        ▼
                    Composable
```

- El `Composable` es una función de `UiState` y no guarda estado de negocio.
- El `ViewModel` expone un único `StateFlow<UiState>` y recibe un único `onEvent(UiEvent)`.
- El recálculo del indicador **no se dispara manualmente**: `observeByPeriod` emite, el `UseCase` recombina, la UI se recompone. Esto satisface RNF03 sin trabajo adicional.

---

## 4. Modelo de datos

### 4.1 Enumeraciones de dominio

```kotlin
enum class TransactionKind { INCOME, EXPENSE }

/** El cruce que define el producto. */
enum class Classification {
    FIXED,      // ingreso fijo      — verde
    VARIABLE,   // ingreso variable  — verde medio
    ESSENTIAL,  // gasto esencial    — verde
    RECREATIONAL // gasto recreativo — morado
}

enum class AccountType { BANK, CREDIT_CARD, CASH }
enum class SyncState { SYNCED, PENDING, CONFLICT }
enum class PlanTier { FREE, PRO }
enum class GoalStatus { ACTIVE, COMPLETED, ARCHIVED }
```

`Classification` es un único enum y no dos, porque la validez del par `(kind, classification)` es una regla de dominio verificable:

```kotlin
fun Classification.isValidFor(kind: TransactionKind): Boolean = when (kind) {
    TransactionKind.INCOME  -> this == Classification.FIXED || this == Classification.VARIABLE
    TransactionKind.EXPENSE -> this == Classification.ESSENTIAL || this == Classification.RECREATIONAL
}
```

Esta función se invoca en el constructor del modelo de dominio: un `Transaction` inválido no puede existir en memoria.

### 4.2 Entidades Room (esquema v1)

Todas las entidades incluyen el bloque común de sincronización: `id: String` (UUID, PK), `userId: String`, `updatedAt: Long` (epoch millis), `syncState: SyncState`, `isDeleted: Boolean`.

#### `users`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | UUID local en Fase 1 |
| `displayName` | TEXT | Editable, opcional |
| `plan` | TEXT | `FREE` fijo en Fase 1 y 2 hasta RF10 |
| `passwordHash` | TEXT? | `null` en Fase 1. Argon2id al activar RF01 |
| `createdAt`, `updatedAt`, `syncState`, `isDeleted` | | bloque común |

#### `accounts`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | |
| `userId` | TEXT FK → users | índice |
| `name` | TEXT | |
| `type` | TEXT | `BANK` / `CREDIT_CARD` / `CASH` |
| `balanceCents` | INTEGER | Saldo en centavos |
| `creditLimitCents` | INTEGER? | Solo `CREDIT_CARD` |
| `statementDay` | INTEGER? | Día de corte, 1–31 — alimenta la alerta de RF09 |
| `dueDay` | INTEGER? | Día de pago |
| `colorSlot` | INTEGER | Índice de variante visual (0–2), no un color crudo — el catálogo de gradientes vive hardcodeado en `:core-ui`/`AccountCard.kt`, no en esquema |
| `lastDigits` | INTEGER? | Últimos 4 dígitos de la tarjeta/cuenta. Nullable: sin dato, la UI muestra "****". Agregado en migración 1→2 |
| bloque común | | |

> En Fase 1 se crea automáticamente una cuenta `CASH` llamada "Efectivo" en el primer arranque, para que `Transaction.accountId` nunca sea nulo.
> El alta manual de cuentas (RF04) se adelantó: el formulario tiene 3 variantes según `type` — `CREDIT_CARD` pide `creditLimitCents`/`statementDay`/`dueDay`/`lastDigits`, `BANK` pide `lastDigits` sin los campos de crédito, `CASH` no pide ninguno de los dos.

#### `transactions`

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | |
| `userId` | TEXT FK | índice |
| `accountId` | TEXT FK → accounts | índice |
| `kind` | TEXT | `INCOME` / `EXPENSE` |
| `classification` | TEXT? | **Nullable por diseño** — invariante I3 |
| `amountCents` | INTEGER | Siempre positivo; el signo lo da `kind` |
| `occurredAt` | INTEGER | epoch millis — índice compuesto con `userId` |
| `note` | TEXT? | |
| `categoryId` | TEXT? FK → categories | Renombrado desde `categoryKey` en migración 2→3: pasó de string suelto a FK real. Nullable — un movimiento sin categoría existe |
| `receiptUri` | TEXT? | Comprobante opcional (RF03) |
| bloque común | | |

#### `categories` *(agregado en migración 2→3, adelantado de Fase 2)*

| Columna | Tipo | Notas |
|---|---|---|
| `id` | TEXT PK | UUID |
| `userId` | TEXT FK → users | índice |
| `name` | TEXT | |
| `type` | TEXT | `INCOME` / `EXPENSE` |
| `colorSlot` | INTEGER | Mismo mecanismo que `accounts.colorSlot` |
| `icon` | TEXT | Key de ícono (ej. "food", "transport"), mapeado a `Icons.*` en `:core-ui`, no una imagen |
| `isSystem` | INTEGER (bool) | Categoría sembrada por la app vs. creada por el usuario — protege las default de borrado accidental |
| `sortOrder` | INTEGER | Orden manual en listas/pickers |
| bloque común | | |

Índices: `(userId, occurredAt DESC)` para la lista de movimientos recientes y para el agregado por periodo; `(userId, kind, classification, occurredAt)` para el cálculo del indicador.

#### `goals` *(esquema creado en v1, UI en Fase 2)*

`id`, `userId`, `name`, `targetCents`, `savedCents`, `deadline`, `status`, bloque común.

#### `tips`

`id`, `userId`, `ruleId`, `transactionId?`, `messageKey`, `paramsJson`, `createdAt`, `dismissedAt?`, bloque común.
Se persisten para no repetir el mismo tip en el mismo periodo y para poder mostrar "el tip del día" de forma estable.

#### `alerts` *(esquema en v1, UI en Fase 2)*

`id`, `userId`, `type`, `severity`, `messageKey`, `paramsJson`, `createdAt`, `readAt?`, bloque común.

#### `conflict_log` *(esquema en v1, uso en Fase 3)*

`id`, `entityType`, `entityId`, `localJson`, `remoteJson`, `resolvedAs`, `createdAt`.

### 4.3 DAOs y consultas agregadas

```kotlin
@Dao
interface TransactionDao {

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND isDeleted = 0
          AND occurredAt BETWEEN :from AND :to
        ORDER BY occurredAt DESC
    """)
    fun observeByPeriod(userId: String, from: Long, to: Long): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions
        WHERE userId = :userId AND isDeleted = 0
        ORDER BY occurredAt DESC LIMIT :limit
    """)
    fun observeRecent(userId: String, limit: Int): Flow<List<TransactionEntity>>

    /** Agregado que alimenta el indicador — un solo viaje a SQLite. */
    @Query("""
        SELECT kind, classification, SUM(amountCents) AS totalCents, COUNT(*) AS count
        FROM transactions
        WHERE userId = :userId AND isDeleted = 0
          AND occurredAt BETWEEN :from AND :to
        GROUP BY kind, classification
    """)
    fun observePeriodTotals(userId: String, from: Long, to: Long): Flow<List<PeriodTotalRow>>

    @Upsert suspend fun upsert(entity: TransactionEntity)

    @Query("UPDATE transactions SET isDeleted = 1, syncState = 'PENDING', updatedAt = :now WHERE id = :id")
    suspend fun markDeleted(id: String, now: Long)
}
```

El indicador **no se calcula recorriendo la lista en Kotlin**: SQLite agrega y devuelve como máximo 4 filas (`INCOME/FIXED`, `INCOME/VARIABLE`, `EXPENSE/ESSENTIAL`, `EXPENSE/RECREATIONAL`) más las no clasificadas. Esto es lo que sostiene RNF03 aunque el historial crezca.

### 4.4 Migraciones

- `exportSchema = true`, esquemas versionados en `:core-data/schemas/` y **bajo control de versiones**.
- Toda migración se escribe a mano (`Migration(n, n+1)`) en `Migrations.kt`. `fallbackToDestructiveMigration()` está **prohibido** en release: borraría datos financieros del usuario.
- Aplicadas hasta ahora: `MIGRATION_1_2` (agrega `accounts.lastDigits`, aditiva simple) y `MIGRATION_2_3` (crea `categories` y recrea `transactions` completa para agregar la FK real `categoryId` — SQLite no soporta agregar constraint FK con `ALTER`/`RENAME COLUMN` directo, así que el patrón es: tabla nueva con el esquema final → copiar datos → drop de la vieja → rename).
- Pendiente: no hay todavía un test con `MigrationTestHelper` corriendo estas migraciones contra una DB con datos reales — se validaron a mano contra el `createSql` exportado por Room. Se recomienda agregarlo antes de la primera migración destructiva real.

---

## 5. Algoritmo del indicador de equilibrio

### 5.1 Qué debe medir

RF05 pide *"calcular y mostrar el indicador de equilibrio financiero cruzando tipo de ingreso y tipo de gasto"*. La formulación traduce a números la tesis del producto: **el ingreso fijo debería sostener el gasto esencial; el gasto recreativo debería vivir dentro de lo que sobra.**

Notación sobre el periodo vigente (mes calendario en curso):

| Símbolo | Definición |
|---|---|
| `IF` | Suma de ingresos clasificados `FIXED` |
| `IV` | Suma de ingresos clasificados `VARIABLE` |
| `GE` | Suma de gastos clasificados `ESSENTIAL` |
| `GR` | Suma de gastos clasificados `RECREATIONAL` |
| `I`  | `IF + IV` |
| `G`  | `GE + GR` |

Los movimientos **sin clasificar se excluyen del cálculo** (invariante I3) y su existencia se comunica aparte en la UI.

### 5.2 Los cuatro subíndices

Cada subíndice devuelve `0.0..1.0`.

**C1 — Cobertura esencial (peso 0.40).** ¿El ingreso fijo cubre lo indispensable?

```
C1 = clamp(IF / GE, 0, 1)          si GE > 0
C1 = 1.0                            si GE == 0 y IF > 0
```

**C2 — Independencia del ingreso variable (peso 0.20).** Del gasto esencial que el ingreso fijo no cubrió, ¿cuánto está financiando el ingreso variable? Financiar lo indispensable con dinero incierto es la situación de riesgo que el producto existe para detectar.

```
descubierto = max(GE - IF, 0)
C2 = 1.0                                    si descubierto == 0
C2 = clamp(1 - (descubierto / max(IV, 1)), 0, 1)   en otro caso
```

**C3 — Disciplina recreativa (peso 0.25).** Objetivo: el gasto recreativo no supera el 30 % del ingreso total.

```
ratio = GR / I                      (si I == 0 → C3 = 0)
C3 = 1.0                            si ratio <= 0.30
C3 = clamp(1 - (ratio - 0.30) / 0.30, 0, 1)   si ratio > 0.30
```

Es decir: 30 % da puntaje pleno, 60 % da cero, y entre ambos decrece linealmente.

**C4 — Capacidad de ahorro (peso 0.15).** Objetivo: quedarse con al menos el 20 % del ingreso.

```
tasa = (I - G) / I                  (si I == 0 → C4 = 0)
C4 = clamp(tasa / 0.20, 0, 1)
```

### 5.3 Puntaje y bandas

```
score = round(100 × (0.40·C1 + 0.20·C2 + 0.25·C3 + 0.15·C4))
```

| Rango | Banda | Etiqueta en UI | Color del gauge |
|---|---|---|---|
| 80–100 | `IN_CONTROL` | "En control" | Verde `#2F7D5B` |
| 60–79 | `STABLE` | "Estable" | Verde `#2F7D5B` |
| 40–59 | `WATCH` | "Con atención" | Verde medio `#7EAE99` |
| 0–39 | `OFF_BALANCE` | "Desbalanceado" | Morado `#6C4CE0` |

> El gauge **conserva siempre su verde/morado propio** aunque el estado sea malo (manual §12). Un indicador desbalanceado **no se pinta de rojo**: rojo es color de estado del sistema, y el indicador es un dato de dominio. Esta es la aplicación más visible de la invariante I2.

### 5.4 Casos borde

| Caso | Comportamiento |
|---|---|
| Sin ningún movimiento en el periodo | `BalanceIndicator.Empty` — la UI muestra estado vacío con CTA a Registro rápido, **nunca "0 / Desbalanceado"** |
| Solo ingresos, sin gastos | `C1 = C3 = C4 = 1`, `C2 = 1` → 100. Correcto: no hay desequilibrio que señalar |
| Solo gastos, sin ingresos (`I == 0`) | `C1 = 0`, `C2 = 0`, `C3 = 0`, `C4 = 0` → 0, banda `OFF_BALANCE`, con tip específico "Registra de dónde viene tu dinero para leer tu equilibrio" |
| Sin ingreso fijo (`IF == 0`, `IV > 0`) | `C1 = 0`; el puntaje cae fuerte por diseño. El tip explica que todo su gasto esencial depende de ingreso incierto |
| Mes parcial (día 3 del mes) | El cálculo **no se prorratea**. En su lugar, si han transcurrido menos de 7 días del periodo, la UI muestra la etiqueta "Lectura preliminar" junto al gauge |
| Menos de 3 movimientos en el periodo | Igual: etiqueta "Lectura preliminar". El número se muestra, pero no se emiten alertas de desvío (RF09) |
| División por cero | Toda división está guardada por `max(x, 1)` o por rama explícita. Sin excepciones en runtime |

### 5.5 Ubicación en el código

```kotlin
// :core-domain/balance/BalanceCalculator.kt — Kotlin puro, sin Android
class BalanceCalculator {
    fun compute(totals: PeriodTotals, elapsedDays: Int, movementCount: Int): BalanceIndicator
}

data class BalanceIndicator(
    val score: Int,                 // 0..100
    val band: BalanceBand,
    val breakdown: Breakdown,       // IF, IV, GE, GR en centavos
    val subscores: Subscores,       // C1..C4, para el desglose de Reportes en Fase 2
    val isPreliminary: Boolean,
    val unclassifiedCount: Int,
)
```

Es una clase sin dependencias, determinista y sin estado — el objetivo es que sea **exhaustivamente testeable sin instrumentación** (§13.1). Los pesos y umbrales viven en un `BalanceConfig` con valores por defecto, para poder calibrarlos con datos reales sin tocar la lógica.

---

## 6. Motor de tips y alertas

### 6.1 Principio

El manual y la propuesta coinciden: *"cada registro de gasto puede generar un tip específico a esa transacción, no una lección genérica programada"*. Por tanto el motor **no es un carrusel de contenido**: es un conjunto de reglas evaluadas contra el estado financiero real inmediatamente después de cada escritura.

### 6.2 Diseño

```kotlin
// :core-domain/rules/
fun interface Rule {
    fun evaluate(ctx: FinancialContext): RuleResult?
}

data class FinancialContext(
    val indicator: BalanceIndicator,
    val previousIndicator: BalanceIndicator?,
    val lastTransaction: Transaction?,
    val period: Period,
    val accounts: List<Account>,
    val goals: List<Goal>,
    val alreadyFiredThisPeriod: Set<RuleId>,
)

data class RuleResult(
    val ruleId: RuleId,
    val severity: Severity,     // determina el componente, ver §6.4
    val messageKey: String,     // clave de strings.xml — nunca texto literal
    val params: Map<String, String>,
    val cooldown: Cooldown,     // ONCE_PER_PERIOD | ONCE_PER_DAY | EVERY_TIME
)
```

`RuleEngine` recibe la lista de reglas ordenada por prioridad, las evalúa todas, filtra por cooldown y devuelve **como máximo una** para mostrar tras un registro. Añadir una regla es añadir un objeto a una lista; no se toca la UI ni el ViewModel.

### 6.3 Catálogo de reglas — Fase 1 (RF07)

| ID | Condición | Mensaje (tono §13) | Cooldown |
|---|---|---|---|
| `R01_ESSENTIAL_UNCOVERED` | `GE > IF` | "Tu gasto esencial supera tu ingreso fijo este mes. Un ajuste de {monto} lo vuelve a cubrir." | 1×/periodo |
| `R02_RECREATIONAL_HIGH` | `GR / I > 0.30` | "Llevas {pct} % en gasto recreativo. Tu ritmo cómodo está cerca del 30 %." | 1×/día |
| `R03_VARIABLE_FUNDING_ESSENTIAL` | `descubierto > 0 && IV > 0` | "Parte de tu gasto esencial se está pagando con ingreso variable. Ese ingreso no siempre llega igual." | 1×/periodo |
| `R04_NO_FIXED_INCOME` | `IF == 0 && GE > 0` | "Aún no registras ingreso fijo. Sin él no se puede leer qué tan sostenible es tu mes." | 1×/periodo |
| `R05_FIRST_RECREATIONAL` | Primer gasto `RECREATIONAL` del periodo | "Primer gasto recreativo del mes. Te queda {monto} antes de tocar tu ritmo cómodo." | 1×/periodo |
| `R06_BAND_DROP` | `band` bajó respecto al registro anterior | "Tu equilibrio bajó a \"{banda}\". Revisa tus últimos gastos recreativos." | cada vez |
| `R07_BAND_RISE` | `band` subió | "Tu equilibrio subió a \"{banda}\". Lo que hiciste este mes está funcionando." | cada vez |
| `R08_UNCLASSIFIED` | `unclassifiedCount > 0` | "Tienes {n} movimientos sin clasificar. Sin eso tu equilibrio queda incompleto." | 1×/día |
| `R09_HEALTHY_SAVING` | `tasa >= 0.20` | "Vas ahorrando {pct} % de lo que entra este mes." | 1×/periodo |
| `R10_LARGE_EXPENSE` | Gasto individual `> 0.15 × I` | "Este gasto equivale al {pct} % de tu ingreso del mes." | cada vez |

### 6.4 Alertas del producto — Fase 2 (RF09)

| Tipo | Condición | Severidad | Nota del manual |
|---|---|---|---|
| Desvío del equilibrio | `band` cae por debajo de `IN_CONTROL` | Advertencia | "El gauge conserva su verde/morado propio" |
| Meta en riesgo | Ritmo de abono < requerido para el plazo | Informativa | Coaching, nunca juicio: "Va más lento de lo planeado" |
| Tarjeta por vencer | Faltan ≤ 3 días para corte o pago | Informativa a > 3 días, **escala a advertencia en los últimos 3** | |

### 6.5 Severidad → componente (tabla de decisión del manual §11)

Esta tabla es normativa. El motor devuelve una `Severity` y la capa de UI la resuelve a un componente sin decidir nada por su cuenta:

| Severidad | ¿Requiere acción? | Componente |
|---|---|---|
| Baja, recuperable al instante | Sí, inmediata | **Validación inline** |
| Destructiva e irreversible | Sí, antes de continuar | **Diálogo bloqueante** |
| Informativa, reversible | Opcional (deshacer) | **Snackbar** |
| Bloquea función central | No inmediata, visible | **Estado de pantalla completa** |
| Condición vigente | No | **Banner persistente** |
| Sin conexión — no es error | No | **Indicador de estado sutil** |

```kotlin
@Composable
fun FeedbackHost(result: RuleResult) = when (result.severity) {
    Severity.INLINE     -> EqInlineValidation(result)
    Severity.BLOCKING   -> EqDialog(result)
    Severity.REVERSIBLE -> EqSnackbar(result)
    Severity.FULLSCREEN -> EqFullScreenState(result)
    Severity.STANDING   -> EqBanner(result)
    Severity.AMBIENT    -> EqSubtleIndicator(result)
}
```

Un `when` exhaustivo sobre un enum: añadir una severidad sin darle componente no compila.

---

## 7. Requisitos funcionales

Definen el comportamiento que el sistema debe ejecutar para cumplir su propósito. Se transcriben íntegros de `Equilibrio.pdf` §07 y se les asigna fase, pantalla y criterios de aceptación verificables.

---

### RF01 — Registro y autenticación de usuarios

> *El sistema debe permitir registrar y autenticar usuarios.*

**Fase:** 2 · **Pantalla:** Login / Registro · **Módulo:** `:feature-auth`

En Fase 1 se sustituye por un `userId` local único generado en el primer arranque y persistido en DataStore. Toda entidad ya lo referencia.

**Criterios de aceptación**
- Dado un usuario nuevo, cuando se registra con correo y contraseña válidos, entonces se crea su registro y queda con sesión iniciada.
- Dado un usuario existente, cuando introduce una contraseña incorrecta, entonces se muestra validación inline y **no** se revela si el correo existe.
- La contraseña se almacena hasheada con Argon2id; en ninguna circunstancia en claro (RNF01).
- Al activar RF01, los datos creados bajo el `userId` local se reasignan al usuario real sin pérdida.

---

### RF02 — Registro de ingreso

> *El sistema debe permitir registrar un ingreso con monto, tipo (fijo/variable), fecha y cuenta destino.*

**Fase: 1** · **Pantalla: Registro rápido** · **Módulo:** `:feature-entry`

**Criterios de aceptación**
- Dado el modo Ingreso, cuando se introduce monto > 0, tipo, fecha y cuenta, entonces al guardar el ingreso aparece en Inicio sin recarga manual.
- Un ingreso solo acepta clasificación `FIXED` o `VARIABLE`; el toggle no ofrece otras opciones.
- Con monto vacío o cero, el botón Guardar está deshabilitado y se muestra validación inline: "Agrega un monto para guardar."
- La fecha por defecto es hoy y es editable hacia el pasado; **no se aceptan fechas futuras**.
- El saldo de la cuenta destino se incrementa en el mismo monto, en la misma transacción de base de datos.

---

### RF03 — Registro de gasto

> *El sistema debe permitir registrar un gasto con monto, categoría (esencial/recreativo), fecha, cuenta o tarjeta de origen y comprobante opcional.*

**Fase: 1** *(comprobante en Fase 2)* · **Pantalla: Registro rápido** · **Módulo:** `:feature-entry`

**Criterios de aceptación**
- El cruce esencial/recreativo **se captura en el mismo instante del registro**, en la misma pantalla, sin paso posterior.
- Un gasto solo acepta `ESSENTIAL` o `RECREATIONAL`.
- Al guardar, el motor de reglas se evalúa y, si procede, se muestra un tip contextual referido a esa transacción.
- El saldo de la cuenta origen se decrementa; si es tarjeta de crédito, se incrementa el uso de línea.
- El comprobante opcional (Fase 2) se guarda como URI con permiso persistente; su ausencia nunca bloquea el guardado.

---

### RF04 — Gestión de cuentas y tarjetas

> *El sistema debe permitir dar de alta, editar y consultar el saldo de múltiples cuentas bancarias y tarjetas de crédito.*

**Fase:** 2 · **Pantalla:** Cuentas y tarjetas · **Módulo:** `:feature-accounts`

En Fase 1 existe la entidad y una cuenta "Efectivo" por defecto, sin UI de gestión.

**Criterios de aceptación**
- Alta, edición y baja lógica de cuentas y tarjetas.
- Una tarjeta muestra uso de línea de crédito como porcentaje y fechas de corte y pago.
- El borrado de una cuenta con movimientos asociados pide confirmación mediante **diálogo bloqueante** (acción destructiva e irreversible, §6.5).

---

### RF05 — Indicador de equilibrio financiero

> *El sistema debe calcular y mostrar el indicador de equilibrio financiero cruzando tipo de ingreso y tipo de gasto.*

**Fase: 1** · **Pantalla: Inicio** · **Módulo:** `:core-domain` + `:feature-home`

**Criterios de aceptación**
- El indicador se calcula según §5 y se muestra como número único más banda textual.
- El desglose muestra ingreso fijo / variable y gasto esencial / recreativo, cada uno con su color de dominio.
- El indicador se actualiza en el mismo frame en que se guarda un movimiento, sin acción del usuario.
- Sin movimientos en el periodo, se muestra el estado vacío, **no** un cero.
- El gauge **nunca** usa colores de estado (rojo/ámbar/azul), en ninguna banda.
- Los movimientos sin clasificar se excluyen del cálculo y su número se comunica explícitamente.

---

### RF06 — Panel de reportes

> *El sistema debe presentar un panel de reportes con distribución de gasto por categoría y tendencia en el tiempo.*

**Fase:** 2 · **Pantalla:** Reportes · **Módulo:** `:feature-reports`

**Criterios de aceptación**
- Resumen de ingresos, gastos y ahorro con variación respecto al periodo anterior.
- Tendencia de seis meses.
- Matriz del cruce ingreso × gasto **en lenguaje simple**, sin jerga financiera.
- Los segmentos de gráfica usan exclusivamente colores de dominio (invariante I2).

---

### RF07 — Tips contextuales

> *El sistema debe generar tips contextuales basados en reglas sobre el comportamiento financiero registrado.*

**Fase: 1** · **Pantallas: Inicio y Registro rápido** · **Módulo:** `:core-domain/rules`

**Criterios de aceptación**
- Tras guardar un movimiento, si alguna regla del catálogo §6.3 se cumple, se muestra su tip.
- Un tip nunca es genérico: siempre referencia un monto, porcentaje o conteo real del usuario.
- Se respeta el cooldown: la misma regla no se repite dentro de su ventana.
- Inicio muestra el tip vigente del día.
- Todos los textos cumplen el tono de §12: sin exclamaciones, sin culpa, con acción concreta.

---

### RF08 — Metas de ahorro

> *El sistema debe permitir crear metas de ahorro con monto objetivo y plazo, y mostrar su avance.*

**Fase:** 2 · **Pantalla:** Metas · **Módulo:** `:feature-goals`

**Criterios de aceptación**
- Alta de meta con nombre, monto objetivo y plazo.
- Meta destacada con anillo de avance y abono rápido; metas secundarias en lista; racha de ahorro.
- El avance se recalcula al registrar un abono.

---

### RF09 — Alertas

> *El sistema debe emitir alertas cuando el indicador de equilibrio se desvía, una meta está en riesgo o una tarjeta está por vencer.*

**Fase:** 2 · **Módulo:** `:core-domain/rules` + notificaciones

**Criterios de aceptación**
- Las tres condiciones de §6.4 generan alerta.
- La alerta de tarjeta es informativa a más de 3 días y **escala a advertencia en los últimos 3**.
- Cada alerta se renderiza con el componente que le corresponde por severidad (§6.5).
- Las alertas usan colores de estado, **nunca** verde ni morado.

---

### RF10 — Límite del plan gratuito

> *El sistema debe limitar, en el plan gratuito, el número combinado de cuentas bancarias y tarjetas de crédito registrables.*

**Fase:** 2 · **Módulo:** `:core-domain`

**Criterios de aceptación**
- En plan `FREE`, el tope combinado de cuentas + tarjetas es **2**.
- Al intentar crear la tercera, se muestra el estado de límite alcanzado con el texto del manual: "Ya tienes 2 cuentas y tarjetas en tu plan gratuito. Pásate a Pro."
- El indicador de equilibrio **permanece gratuito en todos los planes** — es el diferenciador y restringirlo impediría descubrir el valor central.

#### Referencia — corte freemium (modelo de negocio, `Equilibrio.pdf` §09)

| Función | Gratis | Pro |
|---|---|---|
| Cuentas y tarjetas (tope combinado) | Hasta 2 elementos | Ilimitadas |
| Metas de ahorro | 1 activa | Ilimitadas |
| Indicador de equilibrio financiero | Sí | Sí |
| Reportes | Periodo actual | Con histórico |
| Tips contextuales | Básicos | Avanzados y personalizados |
| Exportación de reportes | No | PDF y Excel |
| Publicidad | Sí | No |
| Mascota (trabajo futuro) | Alertas ilustradas | Asistente conversacional |

---

## 8. Requisitos no funcionales

Establecen las cualidades del sistema que condicionan cómo se cumplen los requisitos funcionales. Cada uno se traduce a decisión técnica concreta y a método de verificación.

---

### RNF01 — Seguridad

> *Los datos financieros se cifran en tránsito (TLS) y en reposo; las contraseñas se almacenan con hash.*

| Aspecto | Fase 1 | Fase 3 |
|---|---|---|
| En reposo | **SQLCipher** sobre Room; la clave se genera en el primer arranque y se guarda en **Android Keystore** (`MasterKey` con `AES256_GCM`) | igual |
| Preferencias | `EncryptedSharedPreferences` / DataStore cifrado para `userId` y clave de BD | igual |
| En tránsito | **No aplica: la app no realiza llamadas de red en Fase 1** | TLS 1.3 obligatorio + `NetworkSecurityConfig` con `cleartextTrafficPermitted="false"` y certificate pinning |
| Contraseñas | No aplica (sin auth) | **Argon2id**, nunca MD5/SHA1/SHA256 a secas |
| Otros | `android:allowBackup="false"`, `FLAG_SECURE` en pantallas con montos, sin logs de datos financieros en release (ProGuard elimina `Log.d`) | igual |

**Verificación:** inspeccionar el `.db` con `sqlite3` y confirmar que no es legible; revisión de `AndroidManifest`; búsqueda de literales sensibles en el APK release.

> **Estado en Fase 1:** cumplido en reposo; el requisito de tránsito queda **no aplicable** por ausencia de red, y se cierra en Fase 3. Esta parcialidad es consecuencia de la decisión de fases y se declara explícitamente para que no se lea como cumplimiento completo.

---

### RNF02 — Usabilidad

> *El registro de un gasto o ingreso se completa en pocos toques, con interfaz pensada para móvil.*

**Meta medible: registrar un gasto en ≤ 4 toques** desde Inicio.

```
1. FAB "Agregar"           → abre Registro rápido, modo Gasto por defecto
2. Teclado numérico        → monto (el campo recibe foco automáticamente)
3. Toggle                  → Esencial / Recreativo
4. Guardar
```

Fecha (hoy) y cuenta (última usada) vienen precargadas y solo se tocan si hay que cambiarlas.

**Verificación:** UI test que cuenta interacciones desde Inicio hasta persistencia confirmada.

---

### RNF03 — Rendimiento

> *El indicador de equilibrio y los reportes responden en tiempo casi real con datos locales.*

| Presupuesto | Objetivo |
|---|---|
| Recálculo del indicador tras guardar | **< 100 ms** hasta recomposición |
| Arranque en frío hasta Inicio interactivo | **< 1.5 s** en gama media |
| Scroll de movimientos | Sin frames perdidos (jank < 1 %) |

Cómo se logra: agregación en SQLite (no en Kotlin, §4.3), índices sobre `(userId, occurredAt)`, `Flow` con `distinctUntilChanged`, `LazyColumn` con `key` estable, Baseline Profile.

**Verificación:** Macrobenchmark para arranque y scroll; test unitario de `BalanceCalculator` con 10 000 movimientos sintéticos.

---

### RNF04 — Disponibilidad

> *La aplicación permite registrar movimientos sin conexión y sincronizarlos al recuperarla.*

| Fase | Estado |
|---|---|
| **Fase 1** | La app es **100 % offline por diseño**: la parte de "registrar sin conexión" está cumplida de forma total y trivial. La parte de "sincronizarlos al recuperarla" **no aplica todavía** porque no hay destino remoto |
| **Fase 3** | `WorkManager` con `NetworkType.CONNECTED`, backoff exponencial, cola de pendientes leída de `syncState = PENDING`, resolución de conflictos last-write-wins con `conflict_log` |

Las columnas `syncState`, `updatedAt`, `isDeleted` y el uso de UUID como PK (§3.4) existen desde el esquema v1 **precisamente** para que este RNF se complete sin migración destructiva. El banner "Sin conexión · tus cambios se guardan y se sincronizan después" está especificado en el manual y se implementa en Fase 3.

**Verificación (Fase 3):** test de integración en modo avión → escritura → reconexión → confirmar `SYNCED`.

---

### RNF05 — Privacidad

> *El usuario puede borrar su cuenta y sus datos desde la aplicación.*

**Fase: 1 (parcial) / 2 (completo)**

- Ajustes → "Borrar todos mis datos" → **diálogo bloqueante** (acción destructiva e irreversible, §6.5) → borrado real de la base, no lógico, más limpieza de DataStore y de comprobantes en almacenamiento.
- Se distingue del borrado lógico de sincronización: el borrado a petición del usuario es físico y total.
- Sin analítica de terceros ni telemetría de datos financieros.
- En Fase 3, el borrado se propaga al servidor antes de considerarse completo.

**Verificación:** test que ejecuta el borrado y confirma que la base queda vacía y DataStore limpio.

---

### RNF06 — Mantenibilidad

> *Arquitectura organizada por capas de datos, lógica de negocio y presentación.*

Cumplido por la arquitectura de §3: tres capas, módulos Gradle con dependencias unidireccionales, `:core-domain` en Kotlin puro sin dependencias de Android.

**Verificación:** tarea Gradle de comprobación de dependencias que falla si `:feature-*` importa `:core-data` o si `:core-domain` importa Android. Convenciones de estilo con ktlint + detekt en CI.

---

### RNF07 — Compatibilidad

> *Desarrollo en Android nativo con Kotlin; versión mínima de SDK a definir en implementación.*

**Definición propuesta:**

| Parámetro | Valor | Justificación |
|---|---|---|
| `minSdk` | **26** (Android 8.0) | Cubre >95 % de dispositivos activos; habilita `java.time` sin desugaring, Keystore con `StrongBox` y notificaciones con canales. Bajar a 24 obligaría a desugaring y a rutas alternas en cifrado, sin ganancia real de audiencia |
| `targetSdk` / `compileSdk` | **36** (Android 16) | Exigido por Play Store para publicación |
| Orientación | Vertical; `WindowSizeClass` previsto para tablet en Fase 3 | |
| Idioma | Español (MX) como base; strings externalizados para i18n futura | |

---

### RNF08 — Identidad visual

> *Interfaz minimalista y limpia, paleta verde-morado, que proyecte calma y control del día a día.*

Cumplido mediante la implementación literal del manual de identidad en `:core-ui` (§9), incluyendo las tres invariantes de §1.5, la escala 4pt, los radios amplios, la barra de navegación capsular flotante y la ausencia total de curvas con rebote (§9.6).

**Verificación:** checklist visual de §13.4 y tests de screenshot comparando contra referencias aprobadas.

---

## 9. Design system en Compose

Traducción literal del manual de identidad. Todo vive en `:core-ui`.

### 9.1 `Color.kt` — paleta

Tomado de §02 y §17 del manual, con los ratios de contraste ya calculados en el propio manual con la fórmula de luminancia relativa WCAG.

```kotlin
// ── Fijos (definidos en la propuesta original) ──────────────────────────────
val Green       = Color(0xFF2F7D5B)  // ingreso, esencial          AA 4.99:1
val GreenDeep   = Color(0xFF1D4F3A)  // verde profundo             AA 9.41:1
val Purple      = Color(0xFF6C4CE0)  // recreativo, crédito        AA 5.60:1
val Background  = Color(0xFFF5F7F3)  // fondo de la app

// ── Derivados ───────────────────────────────────────────────────────────────
val GreenMid    = Color(0xFF7EAE99)  // ingreso variable
val GreenSoft   = Color(0xFFE3EFE7)  // relleno de insignias
val PurpleMid   = Color(0xFFA490EC)  // morado medio
val PurpleSoft  = Color(0xFFEDE9FB)  // tarjeta de tip

// ── Neutros — teñidos hacia el verde de marca, nunca gris puro ─────────────
val Ink         = Color(0xFF1C2420)  // texto primario
val InkMuted    = Color(0xFF5B675E)  // texto secundario
val InkFaint    = Color(0xFF8A948A)  // solo timestamps — falla AA cuerpo
val Border      = Color(0xFFE3E8E3)
```

> Los neutros se tiñen levemente hacia el verde de marca en vez de usar gris puro, para que texto y bordes queden en la misma familia tonal que el color dominante.

### 9.2 Tokens semánticos — la separación dominio / estado

Esta es la regla dura del manual §03 y la base de las invariantes I1 e I2.

```kotlin
/** Colores de DOMINIO — clasifican un dato. Nunca en un mensaje del sistema. */
enum class DomainTone { INCOME_FIXED, INCOME_VARIABLE, ESSENTIAL, RECREATIONAL, CREDIT, NEUTRAL }

/** Colores de ESTADO — describen la confianza del sistema.
 *  Nunca rellenan chip, insignia, monto ni segmento de gráfica. */
enum class FeedbackTone { SUCCESS, WARNING, ERROR, INFO }
```

| Token | Valor | Significado |
|---|---|---|
| `semantic.ingreso` | `#2F7D5B` | Montos positivos, insignias de ingreso |
| `semantic.gasto-recreativo` | `#6C4CE0` | Montos de gasto no esencial, crédito |
| `semantic.error` | `#B3492E` | **Terracota — nunca rojo puro**, para no competir con el morado |
| `semantic.advertencia` | `#9A6400` | Ámbar — tono ausente en el resto de la paleta |
| `semantic.informacion` | `#2E5FB3` | Azul — estados neutros del sistema |

Los dos enums son tipos distintos y ningún componente acepta ambos. Un banner de error no puede pintarse de verde porque `EqBanner` no tiene forma de recibir un `DomainTone`.

### 9.3 Modo oscuro (§05)

Cada acento **se aclara y desatura levemente manteniendo su tono** — nunca se invierte a un color más oscuro.

| Claro | Oscuro |
|---|---|
| `#2F7D5B` | `#5FBE94` |
| `#6C4CE0` | `#A392F5` |
| `#F5F7F3` | `#14181A` |

Implementado como dos `EquilibrioColorScheme` provistos por `CompositionLocal`, no como `isSystemInDarkTheme()` disperso por los composables. Preferencia de tema persistida en DataStore con tres valores: sistema / claro / oscuro.

### 9.4 `Type.kt` — tipografía (§06)

**Plus Jakarta Sans** para cifras y títulos; **Inter** para cuerpo y UI. Ambas se empaquetan como recursos locales (`res/font/`), no se descargan.

| Estilo | Familia | Peso | Tamaño |
|---|---|---|---|
| `displayCifra` | Plus Jakarta Sans | 700 | 40sp |
| `h1` | Plus Jakarta Sans | 700 | 24sp |
| `h2` | Plus Jakarta Sans | 600 | 18sp |
| `body` | Inter | 400 | 15sp |
| `label` | Inter | 600 | 13sp |
| `caption` | Inter | 500 | 11sp |

**Todo monto usa cifras tabulares.** No es opcional: sin ellas los montos de una lista bailan al recomponerse.

```kotlin
val TabularFigures = TextStyle(
    fontFeatureSettings = "tnum",
    fontFamily = PlusJakartaSans,
)
```

El componente `EqAmount` aplica esto siempre; ningún monto se renderiza con un `Text` crudo.

### 9.5 `Spacing.kt` y `Shape.kt` (§07, §08)

Escala 4pt:

```kotlin
object Spacing {
    val xs = 4.dp; val sm = 8.dp; val md = 12.dp
    val base = 16.dp   // margen de pantalla
    val lg = 20.dp     // padding de tarjeta
    val xl = 24.dp     // entre tarjetas
    val xxl = 32.dp    // entre secciones
    val safe = 48.dp   // safe area
}

object Radii {
    val sm = 12.dp; val md = 20.dp; val lg = 24.dp; val pill = 999.dp
}
```

- Fila de lista: **mínimo 56dp de alto**.
- Área táctil: **mínimo 48×48dp en todo elemento tocable**.
- Elevación: tres niveles, y **las sombras usan siempre el color de tinta a baja opacidad — nunca negro puro ni sombras coloreadas**.

### 9.6 `Motion.kt` (§09)

> **Ninguna curva con rebote en todo el sistema.** El trabajo de esta app es bajar la ansiedad, no generar urgencia.

```kotlin
object Motion {
    val standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val emphasized = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    const val fast = 150      // toggles, chips
    const val normal = 250    // transiciones de pantalla
    const val gaugeFill = 700 // llenado del indicador
}
```

`Spring` con `dampingRatio < 1f` está prohibido; detekt lo marca. El llenado del gauge (`motion.gauge-fill`, 700 ms) es la única animación larga del sistema y es la que comunica el valor central.

### 9.7 Iconografía (§10)

**Material Symbols Rounded.** `FILL 0` en reposo, `FILL 1` en estado activo — el estado se codifica **también por forma, no solo por color**. `wght 400` · `GRAD 0` en claro, `+200` en oscuro · `opsz 20–24` según densidad.

Mapeo icono → función (del manual): inicio, agregar, cuentas, metas, tip, reportes, notificaciones, buscar, editar, eliminar, comida, transporte, súper, salidas, suscripciones, música, ahorro, racha, tendencia, delta +, delta − (morado), cuenta bancaria, crédito, plan Pro, sesión/seguridad, fecha/corte, sin conexión, sin resultados, advertencia, información.

**Regla de insignias:** sólida / suave / **neutra — sin clasificar**. Un movimiento nunca recibe insignia verde o morada hasta que el usuario lo clasificó; un color por defecto clasificaría en silencio, antes que el texto (invariante I3).

### 9.8 Componentes de `:core-ui`

| Componente | Recibe | Nunca recibe |
|---|---|---|
| `EqAmount` | `Long` centavos, `DomainTone`, signo | `FeedbackTone`, `Color` |
| `EqBadge` | texto, `DomainTone` (incluye `NEUTRAL`) | `FeedbackTone` |
| `EqChip` | texto, icono, seleccionado, `DomainTone` | `FeedbackTone` |
| `EqCard` | contenido, elevación 1–3 | color de sombra personalizado |
| `EqGauge` | `BalanceIndicator` | cualquier color de estado |
| `EqButton` | primario / secundario / **destructivo** | — |
| `EqTextField` | valor, estado default/focus/error | — |
| `EqBanner`, `EqSnackbar`, `EqDialog`, `EqInlineValidation` | `FeedbackTone` | `DomainTone` |
| `EqBottomBar` | destinos | — barra capsular flotante |

---

## 10. Especificación de las 2 pantallas de Fase 1

### 10.1 Navegación

```
NavHost(startDestination = Home)
  ├── Home            (feature-home)
  └── QuickEntry      (feature-entry)   ← modal bottom sheet a pantalla completa

Home ──FAB "Agregar"──▶ QuickEntry
QuickEntry ──Guardar / Cancelar──▶ Home
```

La barra de navegación capsular flotante del manual se dibuja ya en Fase 1 con los 5 destinos previstos; los tres no implementados (Cuentas, Metas, Reportes) muestran un estado de pantalla completa "Disponible pronto" en lugar de estar ocultos, para que la estructura del producto sea legible desde la primera entrega.

---

### 10.2 Pantalla 01 — Inicio

**Propósito (mockup §11 de la propuesta):** *"Saldo disponible, indicador de equilibrio con desglose de ingreso fijo/variable y gasto esencial/recreativo, tip contextual del día y movimientos recientes."*

#### Jerarquía de composables

```
HomeScreen(state, onEvent)
├── EqTopBar                    saludo + acceso a ajustes
├── LazyColumn(space.24 entre tarjetas)
│   ├── BalanceHeaderCard
│   │   ├── Text "Saldo disponible"        label
│   │   └── EqAmount(total)                displayCifra 40sp, tabular
│   ├── EquilibriumCard                    ← el corazón de la pantalla
│   │   ├── EqGauge(indicator)             animación gaugeFill 700ms
│   │   ├── Text(band.label)               h2 — "En control"
│   │   ├── BreakdownRow(income)
│   │   │   ├── EqBadge("Fijo", INCOME_FIXED)      + EqAmount
│   │   │   └── EqBadge("Variable", INCOME_VARIABLE) + EqAmount
│   │   ├── BreakdownRow(expense)
│   │   │   ├── EqBadge("Esencial", ESSENTIAL)     + EqAmount
│   │   │   └── EqBadge("Recreativo", RECREATIONAL) + EqAmount
│   │   └── UnclassifiedHint?               si unclassifiedCount > 0
│   ├── TipCard                             fondo PurpleSoft, icono tip
│   └── RecentTransactionsSection
│       └── TransactionRow × n              mínimo 56dp de alto
│           ├── CategoryIcon
│           ├── Column(title, "Recreativo · hoy")
│           └── EqAmount(±monto, tone)
├── EqFab("Agregar")
└── EqBottomBar
```

#### Estado y eventos

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val availableBalanceCents: Long = 0,
    val indicator: BalanceIndicator? = null,
    val tipOfTheDay: TipUi? = null,
    val recent: List<TransactionUi> = emptyList(),
    val isEmpty: Boolean = false,
    val banner: FeedbackUi? = null,
)

sealed interface HomeEvent {
    data object AddClicked : HomeEvent
    data class TransactionClicked(val id: String) : HomeEvent
    data class ClassifyRequested(val id: String) : HomeEvent
    data object TipDismissed : HomeEvent
}
```

#### Estados no felices

| Estado | Render |
|---|---|
| Cargando | **Skeleton** con la forma de las tarjetas reales, no spinner |
| Sin movimientos | Ilustración + "Aún no registras movimientos" + "Registra tu primer ingreso o gasto para ver tu equilibrio." + botón "Agregar movimiento" |
| Lectura preliminar | Etiqueta junto al gauge: "Lectura preliminar" |
| Movimientos sin clasificar | Insignia **neutra** en la fila + hint bajo el gauge: "Tienes {n} movimientos sin clasificar." |

#### La prueba visual de la pantalla

El manual (§01) demuestra el principio rector con dos columnas idénticas, una con color y otra sin él, y concluye: *"la segunda columna obliga a leer el texto para entender qué es cada movimiento. La primera se entiende de un vistazo — ese vistazo es el producto."*

**Criterio de aceptación visual de Inicio:** en escala de grises la pantalla sigue siendo *usable*, pero deja de ser *instantánea*. Si en color tampoco es instantánea, la pantalla está mal implementada.

---

### 10.3 Pantalla 02 — Registro rápido

**Propósito (mockup §11):** *"Alterna entre gasto e ingreso; el cruce esencial/recreativo se captura en el mismo instante del registro, no después."*

#### Jerarquía de composables

```
QuickEntryScreen(state, onEvent)
├── EqTopBar(cerrar, título "Nuevo movimiento")
├── KindSegmentedControl                 [ Gasto | Ingreso ]  ← define el toggle de abajo
├── AmountField                          displayCifra, teclado numérico, foco automático
│   └── EqInlineValidation?              "Agrega un monto para guardar."
├── ClassificationToggle                 ← EL COMPONENTE DIFERENCIADOR
│   │   modo Gasto   → [ ✓ Esencial | Recreativo ]
│   │   modo Ingreso → [ ✓ Fijo     | Variable   ]
│   └── HelperText                       explica el par en lenguaje simple
├── CategoryChipRow                      comida, transporte, súper, salidas, suscripciones…
├── AccountSelector                      precargado con la última cuenta usada
├── DateField                            hoy por defecto; futuro bloqueado
├── NoteField                            opcional
└── EqButton("Guardar gasto" / "Guardar ingreso")   primario, ancho completo
```

#### Estado y eventos

```kotlin
data class QuickEntryUiState(
    val kind: TransactionKind = TransactionKind.EXPENSE,
    val amountInput: String = "",
    val classification: Classification? = null,
    val categoryKey: String? = null,
    val accountId: String? = null,
    val occurredAt: LocalDate = today(),
    val note: String = "",
    val amountError: String? = null,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
)

sealed interface QuickEntryEvent {
    data class KindChanged(val kind: TransactionKind) : QuickEntryEvent
    data class AmountChanged(val raw: String) : QuickEntryEvent
    data class ClassificationChanged(val c: Classification) : QuickEntryEvent
    data class CategoryChanged(val key: String) : QuickEntryEvent
    data class AccountChanged(val id: String) : QuickEntryEvent
    data class DateChanged(val date: LocalDate) : QuickEntryEvent
    data class NoteChanged(val text: String) : QuickEntryEvent
    data object SaveClicked : QuickEntryEvent
}
```

#### Reglas de comportamiento

1. **`KindChanged` resetea `classification` a `null`.** Cambiar de Gasto a Ingreso con `ESSENTIAL` seleccionado produciría un par inválido. El reset es obligatorio y está cubierto por test.
2. **`canSave = amount > 0 && classification != null && accountId != null`.** El botón deshabilitado es el estado por defecto.
3. **Clasificación por defecto: ninguna.** Preseleccionar "Esencial" clasificaría en silencio (invariante I3). El usuario debe tocar.
4. **El monto se parsea a centavos**, nunca a `Double`. Entrada `"240.50"` → `24050L`.
5. Al guardar: persistir → evaluar reglas → volver a Inicio → mostrar el tip resultante como **snackbar** (informativo, reversible) con acción "Deshacer".
6. Cancelar con datos introducidos → **diálogo bloqueante** de confirmación de descarte.

#### Estados no felices

| Estado | Render |
|---|---|
| Monto vacío o cero | Validación inline: "Agrega un monto para guardar." · botón deshabilitado |
| Fecha futura | Validación inline: "Elige una fecha de hoy o anterior." |
| Guardando | Botón con progreso, campos deshabilitados |
| Fallo al guardar | Snackbar con "Reintentar" · **los datos del formulario no se pierden** |

---

## 11. Accesibilidad

Del manual §04, cuyos ratios ya están calculados con la fórmula de luminancia relativa WCAG. AA de cuerpo exige ≥ 4.5:1; AA de texto grande, ≥ 3:1.

| Combinación | Ratio | Resultado |
|---|---|---|
| Verde sobre blanco | 4.99:1 | Pasa AA cuerpo |
| Morado sobre blanco | 5.60:1 | Pasa AA cuerpo |
| Verde profundo sobre verde suave | 7.96:1 | Pasa |
| Verde sobre verde suave | 4.22:1 | **Falla AA cuerpo** |
| Ink-faint sobre blanco | 3.14:1 | **Falla AA cuerpo — solo texto grande** |

### Las dos fallas y su corrección — normativas

1. **Ink-faint (3.14 / 2.91)** se restringe a **metadatos no esenciales**, nunca por debajo de **16sp** y **nunca como único portador de información**. Un timestamp puede usarlo; un monto o una clasificación, jamás.
2. **Verde sobre verde suave (4.22)** se corrige usando **siempre verde profundo** (`#1D4F3A`) como color de texto/icono dentro de una insignia con relleno suave. `EqBadge` aplica esta sustitución internamente: es imposible producir la combinación que falla.

### Resto de requisitos

- Área táctil mínima **48×48dp** en todo elemento tocable; filas de lista mínimo **56dp**.
- `contentDescription` en toda iconografía portadora de significado; `null` explícito en la decorativa.
- **La clasificación nunca depende solo del color** para lectores de pantalla: la insignia lleva texto ("Recreativo") y el icono cambia de forma (`FILL 0` → `FILL 1`).
- Soporte de escalado de fuente hasta **200 %** sin recortes: sin alturas fijas en contenedores de texto.
- Orden de foco lógico y `semantics` con `heading()` en los títulos de sección.
- Verificación con TalkBack y con Accessibility Scanner antes de cada entrega.

---

## 12. Tono de voz aplicado a strings

Fórmula del manual §13: **qué pasó (sin culpa) + qué puedes hacer (concreto)**. Segunda persona informal. **Sin signos de exclamación.**

| No | Sí |
|---|---|
| "Error: el campo monto es obligatorio." | "Agrega un monto para guardar." |
| "¡Advertencia! Tu meta está en riesgo de fallar." | "Va más lento de lo planeado. Un abono de $220 esta semana la vuelve a encarrilar." |
| "Error crítico de sincronización." | "Algunos movimientos no se sincronizaron. Toca para reintentar." |
| "Has alcanzado el límite de tu plan." | "Ya tienes 2 cuentas y tarjetas en tu plan gratuito. Pásate a Pro." |

### Reglas de implementación

- **Todos** los textos visibles viven en `strings.xml`. Ningún literal en composables — permite revisar el tono en un solo archivo y prepara la i18n.
- Nomenclatura: `home_empty_title`, `entry_error_amount_required`, `tip_r02_recreational_high`.
- Los tips y alertas referencian `messageKey` + `params`, nunca texto construido por concatenación (rompe el orden gramatical al traducir).
- Revisión de tono en el checklist de PR: si un string tiene `!` o culpa al usuario, no pasa.

---

## 13. Plan de pruebas

### 13.1 Unit tests — `:core-domain` (prioridad máxima)

`BalanceCalculator` es la pieza que justifica el producto y no depende de Android; debe ser la más probada.

| Caso | Verifica |
|---|---|
| `IF=5000, GE=3000, GR=500, IV=0` | Escenario sano → banda `IN_CONTROL` |
| `IF=0, IV=4000, GE=3500` | Ingreso variable financiando esencial → `C2` bajo, banda degradada |
| `GR/I = 0.30` exacto | `C3 == 1.0` (frontera inclusiva) |
| `GR/I = 0.60` | `C3 == 0.0` |
| `GR/I = 0.45` | `C3 == 0.5` (linealidad) |
| Todo en cero | `BalanceIndicator.Empty`, nunca `score = 0` |
| `I == 0, G > 0` | Sin excepción; `score = 0`, banda `OFF_BALANCE` |
| `GE == 0, IF > 0` | `C1 == 1.0` |
| Movimientos sin clasificar | Excluidos del cálculo; `unclassifiedCount` correcto |
| 10 000 movimientos | Cálculo < 50 ms (RNF03) |
| Property-based (kotest) | `score` siempre en `0..100` para cualquier entrada |

`RuleEngine`: una regla dispara con su condición; no dispara sin ella; el cooldown se respeta; con varias reglas cumplidas se devuelve solo la de mayor prioridad.

`Classification.isValidFor`: los 8 pares posibles, 4 válidos y 4 inválidos.

### 13.2 Tests de datos — `:core-data`

- DAOs con `Room.inMemoryDatabaseBuilder`: upsert, `observeByPeriod` con límites del rango, `observePeriodTotals` agrupando correctamente, `markDeleted` excluyendo del observe.
- Mappers entidad ↔ dominio, ida y vuelta sin pérdida.
- Migraciones con `MigrationTestHelper` desde v1 (obligatorio en cuanto exista v2).
- Verificación de que `syncState`, `updatedAt`, `isDeleted` y UUID se pueblan en toda escritura — es lo que sostiene la Fase 3.

### 13.3 UI tests — Compose

**Inicio:** muestra estado vacío sin datos; muestra el gauge con la banda correcta; una insignia neutra aparece para movimientos sin clasificar; el skeleton se muestra durante la carga.

**Registro rápido:** el botón Guardar está deshabilitado sin monto; cambiar de Gasto a Ingreso resetea la clasificación; guardar persiste y navega a Inicio; el flujo completo se completa en ≤ 4 toques (RNF02); una fecha futura muestra validación inline.

**End-to-end:** Inicio vacío → agregar gasto recreativo de $500 con ingreso fijo previo de $5 000 → el indicador de Inicio refleja el nuevo valor sin acción manual.

### 13.4 Checklist manual de verificación visual

Contra el manual de identidad, antes de cada entrega:

- [ ] Ningún color de estado (rojo/ámbar/azul) rellena chip, insignia, monto o segmento de gráfica
- [ ] Ningún color de dominio (verde/morado) aparece en banner, snackbar, diálogo o validación
- [ ] El gauge conserva verde/morado en todas las bandas, incluida `OFF_BALANCE`
- [ ] Movimientos sin clasificar muestran insignia neutra, nunca verde ni morada
- [ ] Todos los montos usan cifras tabulares
- [ ] Ninguna animación rebota
- [ ] Las sombras usan color de tinta a baja opacidad, no negro
- [ ] Ningún texto usa ink-faint por debajo de 16sp ni como único portador de información
- [ ] Todo elemento tocable mide ≥ 48×48dp
- [ ] Modo oscuro: los acentos se aclararon manteniendo su tono
- [ ] Ningún string lleva signo de exclamación ni culpa al usuario
- [ ] Escala de fuente al 200 %: sin recortes

### 13.5 CI

`./gradlew ktlintCheck detekt testDebugUnitTest` en cada push. UI tests en emulador API 26 y API 36 antes de cada entrega.

---

## 14. Configuración del proyecto en Android Studio

### 14.1 Creación

1. **New Project → Empty Activity (Compose)**
2. Name: `Equilibrio` · Package: `mx.equilibrio.app` · Language: **Kotlin** · Build: **Kotlin DSL** (`build.gradle.kts`)
3. `minSdk 26`, `targetSdk 36`, `compileSdk 36`
4. Activar **Gradle Version Catalog** (`gradle/libs.versions.toml`)

### 14.2 Estructura de carpetas

```
equilibrio/
├── app/
├── core-ui/           theme/  components/  tokens/
├── core-domain/       model/  usecase/  repository/  balance/  rules/
├── core-data/         local/{entity,dao,converter}/  repository/  mapper/  di/
│   └── schemas/       ← esquemas Room exportados, versionados en git
├── feature-home/
├── feature-entry/
├── gradle/libs.versions.toml
├── PLAN_DESARROLLO.md
├── Equilibrio.pdf
└── Equilibrio — Manual de identidad.pdf
```

### 14.3 Dependencias principales

Compose BOM · Material 3 · Navigation Compose · Hilt + hilt-navigation-compose · Room (runtime, ktx, compiler vía KSP) · DataStore Proto · kotlinx-datetime · kotlinx-serialization · SQLCipher (`net.zetetic:android-database-sqlcipher`) + `androidx.security:security-crypto` · WorkManager *(declarada, sin uso hasta Fase 3)* · Coil (comprobantes, Fase 2).
Test: JUnit5 · kotest-property · Turbine · MockK · `androidx.room:room-testing` · `compose-ui-test-junit4` · `androidx.benchmark:benchmark-macro-junit4`.

### 14.4 Recursos previos al código

- Descargar y colocar **Plus Jakarta Sans** e **Inter** en `core-ui/src/main/res/font/` (pesos 400, 500, 600, 700).
- Añadir la dependencia de **Material Symbols Rounded** o exportar los ~30 iconos del mapeo de §9.7 como `ImageVector`.

### 14.5 Orden de implementación sugerido para Fase 1

| # | Paso | Entregable verificable |
|---|---|---|
| 1 | Módulos Gradle y version catalog | El proyecto compila vacío |
| 2 | `:core-ui` — tokens, tema claro/oscuro, tipografías | Preview con la paleta y la escala tipográfica |
| 3 | `:core-ui` — componentes atómicos | Previews de `EqAmount`, `EqBadge`, `EqGauge`, `EqButton`, `EqTextField` |
| 4 | `:core-domain` — modelos, enums, `BalanceCalculator` | **Tests de §13.1 en verde** |
| 5 | `:core-domain` — `RuleEngine` y catálogo de reglas | Tests de reglas en verde |
| 6 | `:core-data` — Room, entidades, DAOs, cifrado | Tests de DAO en verde |
| 7 | `:core-data` — repositorios y mappers | Test de ida y vuelta |
| 8 | `:feature-entry` — Registro rápido | Guarda y persiste |
| 9 | `:feature-home` — Inicio | Muestra el indicador reactivo |
| 10 | Navegación, FAB, barra capsular | Flujo completo navegable |
| 11 | Estados vacío / carga / error | Checklist §13.4 |
| 12 | Accesibilidad y pulido | TalkBack + Accessibility Scanner |

> Los pasos 4 y 5 van **antes** de la UI a propósito: el diferenciador es lógica de dominio, y debe estar probado antes de dibujar nada.

---

## 15. Riesgos y mitigación

### 15.1 Riesgos de la propuesta original

| Riesgo | Mitigación |
|---|---|
| Competencia consolidada (Mint, Fintonic, YNAB) | No se compite en cantidad de funciones, sino en el indicador cruzado, ausente en esas plataformas |
| Baja conversión al plan Pro | El nivel gratuito se diseñó como canal de adquisición, no como producto sacrificado. El indicador se mantiene gratuito deliberadamente |
| Alcance excesivo para el plazo del semestre | Delimitación explícita del entregable y registro formal de funciones diferidas (§2) |
| Sensibilidad del dato financiero | Cifrado en reposo desde Fase 1 y borrado a petición del usuario, incorporados desde el diseño (RNF01, RNF05) |

### 15.2 Riesgos introducidos por la estrategia de fases

| Riesgo | Probabilidad | Impacto | Mitigación |
|---|---|---|---|
| **Deuda de sincronización:** el esquema local no soporta la nube y obliga a migración destructiva | Media | **Alto** — pérdida de datos financieros | Las cuatro decisiones de §3.4 (UUID, `userId`, `updatedAt`, `syncState` + borrado lógico) están en el esquema v1. Se aceptan cuatro columnas sin uso hoy a cambio de eliminar el riesgo |
| **`fallbackToDestructiveMigration` en producción** | Media | **Alto** | Prohibido por convención; revisión obligatoria en PR y `exportSchema = true` versionado |
| Un `:feature-*` accede a Room directamente y rompe las capas | Media | Medio | Tarea Gradle de comprobación de dependencias que falla el build |
| Deriva de la identidad visual: alguien pinta un error de rojo dentro de una insignia | **Alta** | Medio | Los enums cerrados `DomainTone` / `FeedbackTone` lo hacen imposible por tipos, no por disciplina. Lint prohíbe `Color(0x...)` fuera de `:core-ui` |
| El indicador queda mal calibrado y da lecturas contraintuitivas | Media | Alto — es el producto | Pesos y umbrales en `BalanceConfig`, ajustables sin tocar la lógica; batería de escenarios realistas en §13.1 |
| Ausencia de auth en Fase 1 se vuelve permanente | Baja | Medio | `userId` presente en todas las entidades desde v1: activar RF01 es poblar el campo, no rediseñar |
| Fuente de pago (Plus Jakarta Sans / Inter) no disponible | Baja | Bajo | Ambas son SIL Open Font License; se empaquetan localmente |

---

## 16. Matriz de trazabilidad

| Req. | Fase | Módulo | Pantalla | Verificación |
|---|---|---|---|---|
| RF01 | 2 | `:feature-auth` | Login | UI test de registro y login; test de hash |
| RF02 | **1** | `:feature-entry`, `:core-data` | Registro rápido | UI test de guardado de ingreso + test de DAO |
| RF03 | **1** | `:feature-entry`, `:core-data` | Registro rápido | UI test de guardado de gasto clasificado |
| RF04 | 2 | `:feature-accounts` | Cuentas y tarjetas | UI test CRUD + diálogo de borrado |
| RF05 | **1** | `:core-domain/balance`, `:feature-home` | Inicio | **Unit tests §13.1** + UI test del gauge |
| RF06 | 2 | `:feature-reports` | Reportes | UI test de agregados |
| RF07 | **1** | `:core-domain/rules` | Inicio, Registro rápido | Unit tests de reglas + UI test de snackbar |
| RF08 | 2 | `:feature-goals` | Metas | UI test de avance |
| RF09 | 2 | `:core-domain/rules` | todas | Unit tests de condiciones + escalado de tarjeta |
| RF10 | 2 | `:core-domain` | Cuentas | Unit test del tope de 2 elementos |
| RNF01 | **1** (reposo) / 3 (tránsito) | `:core-data` | — | Inspección del `.db` cifrado; revisión de manifest |
| RNF02 | **1** | `:feature-entry` | Registro rápido | UI test que cuenta ≤ 4 toques |
| RNF03 | **1** | `:core-data`, `:core-domain` | Inicio | Macrobenchmark + test con 10 000 registros |
| RNF04 | **1** (offline) / 3 (sync) | `:core-data` | — | Test en modo avión (Fase 3) |
| RNF05 | **1** parcial / 2 | `:core-data` | Ajustes | Test de borrado total |
| RNF06 | **1** | todos | — | Tarea Gradle de dependencias + detekt |
| RNF07 | **1** | `:app` | — | Build en API 26 y API 36 |
| RNF08 | **1** | `:core-ui` | ambas | Checklist visual §13.4 + screenshot tests |

---

## Resumen ejecutivo

**Qué se construye ahora:** dos pantallas —Inicio y Registro rápido— sobre Room local, sin autenticación y sin red, que entregan el diferenciador completo del producto: clasificar cada movimiento por el cruce ingreso fijo/variable × gasto esencial/recreativo, resolverlo en un indicador único y devolver un consejo en el instante del registro.

**Qué se decide ahora aunque se use después:** el esquema de datos completo de las tres fases (UUID como PK, `userId`, `updatedAt`, `syncState`, borrado lógico), la separación en tres capas con `Repository` como interfaz de dominio, y el design system íntegro con las reglas de identidad codificadas como tipos que hacen imposible violarlas.

**Por qué así:** el costo de esas decisiones hoy son cuatro columnas sin uso y un enum extra. El costo de omitirlas es una migración destructiva sobre datos financieros el día que llegue la nube.
