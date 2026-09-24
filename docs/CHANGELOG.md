# Changelog

Registro de avance funcional de Equilibrio, por sesión de trabajo. Complementa
`PLAN_DESARROLLO.md` (que describe el diseño objetivo) con lo que ya está
construido.

## 2026-09-24 — Freemium, comentarios de código, daltonismo, logo de marca

**Merges de ramas externas** (PR #10, #11, #12, #13 — trabajo de otros
integrantes del equipo, verificado y reconciliado por el orquestador antes de
mergear)
- PR #10 `feature/home-dashboard` (issues #4/#5/#6/#8/#9): cuenta "Efectivo"
  sembrada ya no cuenta para el límite del plan FREE (FREE = Efectivo + 2
  cuentas banco/tarjeta); aviso de límite al abrir "Nueva cuenta" en vez de al
  guardar; `ConfirmTransaction` rechaza fechas futuras en el dominio (no solo
  en la UI) y el detalle de una transacción programada gana un botón
  "Confirmar". **Conflicto real encontrado**: la rama del PR era anterior a
  que este equipo agregara la pestaña "Transacciones" a la barra inferior —
  el auto-merge de git la borró en 3 lugares (`EqBottomNav.kt`,
  `EquilibrioNavHost.kt` x2) SIN marcar conflicto de texto. Se restauró a
  mano tras revisar archivo por archivo contra ambas puntas del merge; el fix
  de overflow de texto que sí traía el PR se conservó.
- PR #11 `feature/daltonismo-fijo`: soporte de daltonismo (paleta alternativa
  en `Color.kt`/`Theme.kt`), con una corrección propia del equipo externo
  (`EquilibrioColors.onGradient` se había perdido en su propio merge —
  commit `2950c32` lo restaura).
- PR #12 `feature/logo-branding`: ícono adaptativo con el logo de marca
  (capa monochrome Android 13+, ícono Play Store), splash animado
  (`AnimatedVectorDrawable`), `EqLogo`/`EqPremiumBadge`/`EqIllustration`
  nuevos en `:core-ui`, `EqEmptyState` acepta ilustración de marca (Inicio,
  Transacciones, Metas, Reportes, Categorías, Búsqueda), Login con logo,
  sello Premium en el aviso de límite FREE.
- PR #13 "Comentado": pasada de comentarios de documentación sobre archivos
  ya existentes (auth, migraciones, DAOs), sin cambios de lógica.

**Verificación**: build completo (`compileDebugKotlin
compileDebugUnitTestKotlin testDebugUnitTest :core-domain:test
:core-data:testDebugUnitTest`) en verde después de cada merge.

## 2026-09-23/24 — Transacciones recurrentes (solo efectivo/débito)

- Plantilla en tabla propia (`recurring_transactions`, migración Room
  v13→v14) + columna `transactions.recurring_series_id`. Deliberadamente
  opuesto a "compras a meses": pausar o borrar la serie NUNCA toca las
  ocurrencias ya generadas (son movimientos normales, editables/borrables
  individualmente).
- Frecuencias: semanal, quincenal, mensual (ancla por día del mes, reusa
  `addMonthsClamped` para no derivar el ancla tras un mes corto).
- **Generación de la siguiente ocurrencia**: se dispara al confirmar
  manualmente la ocurrencia anterior (`ConfirmTransaction`), no por
  auto-confirmación al vencer la fecha — corregido en base a feedback
  explícito del usuario tras una primera implementación con auto-confirm.
  `GenerateDueRecurringTransactions` quedó como red de seguridad: solo
  materializa la primera ocurrencia de una serie que todavía no generó
  ninguna.
- Se agrega desde QuickEntry (gasto/ingreso, no transferencias ni tarjeta);
  el detalle de movimiento muestra la frecuencia y permite "Dejar de
  repetir".

## 2026-09-23 — Saldo actual vs Saldo proyectado

- Antes había dos cálculos de saldo distintos según la pantalla (Inicio vs
  Transacciones), ninguno correcto: uno sumaba transacciones de tarjeta como
  si fueran efectivo (doble conteo al pagar un periodo), el otro excluía
  tarjetas por completo. Unificado en un solo `ObserveBalance`.
- **Saldo actual** = solo efectivo/banco (`COMPLETED` + saldo inicial).
  **Saldo proyectado** = eso, más/menos transacciones `SCHEDULED` de
  efectivo/banco, menos el total a pagar de todas las tarjetas (todos los
  periodos no-`CLOSED`, cuotas futuras incluidas — no el máximo de un solo
  periodo, que es una regla de límite de crédito distinta).
- Selector de mes en Transacciones ya no topa al mes actual: permite
  navegar hasta el mes más lejano que tenga alguna transacción.

## 2026-09-23 — Rediseño de Transacciones (lista, detalle, filtros, búsqueda, editar)

- Mockup de diseño generado con pen.dev (`designs/transacciones.pen`),
  aprobado por el usuario antes de implementar.
- Lista agrupada por día, tabs Todos/Gastos/Ingresos/Programadas, tarjeta de
  saldo con Ingresos/Gastos del mes.
- Pantalla de detalle nueva (antes se entraba directo a editar): plan de
  cuotas con progreso si es una cuota, menú de eliminar, botón editar.
- Pantallas nuevas de Filtros (alcance de fecha, tipo, cuenta, categoría,
  estado, con conteo de resultados en vivo — comparte el mismo ViewModel que
  la lista vía scope del nav graph) y Búsqueda (por nota/categoría/cuenta,
  búsquedas recientes en memoria de sesión).
- Editar movimiento restyleado a filas tipo lista (Descripción, Categoría,
  Cuenta, Fecha); toggle Confirmada deshabilitado con fecha futura.
- Fix de raíz de un bug de superposición visual que afectaba 14 pantallas:
  `Scaffold(topBar=...)` + `calculateTopPadding()` tenía un desajuste de
  medición — reemplazado por `Column` secuencial (topBar primero, contenido
  después), elimina la fuente del bug en vez de compensarla.

## 2026-09-22/23 — Categorías: edición, eliminación, presupuesto y detalle

- Bug de la versión anterior: no se podían editar ni eliminar categorías, y
  las de ingreso/egreso salían mezcladas. Mockup de diseño generado con
  pen.dev (`designs/categorias.pen`), aprobado antes de implementar.
- Tabs Ingreso/Gasto con selector de mes y totales por categoría.
- Presupuesto mensual opcional por categoría (migración Room v12→v13,
  `monthly_budget_cents`).
- Pantalla de detalle de categoría (mes actual, movimientos recientes,
  progreso de presupuesto).
- Borrado con dos opciones: reasignar movimientos a una categoría "Otros"
  (creada bajo demanda si no existe) o eliminarlos en cascada — nunca una
  cascada silenciosa por defecto.

## 2026-09-22 — Fix fecha futura + Compras a meses

- Una transacción `CONFIRMADA` (`COMPLETED`) nunca puede tener fecha futura
  — bloqueado tanto en dominio (`SaveTransaction`) como en la UI (selector
  de fecha al editar).
- Compras a meses (installments): `SaveInstallmentPurchase` genera todas las
  cuotas de una compra por adelantado, vinculadas por `installmentPlanId`,
  validadas de una sola vez contra el límite de crédito proyectado
  (`CreditLimitRules`). No editable individualmente — solo se borra el plan
  completo. Migración Room v11→v12.

## 2026-09-21 (tarde) — Metas y Reportes (PR #3, `feature/metas-reportes`)

**Metas de ahorro (RF08)**
- Nueva tabla `goals` y columna `transactions.goal_id` (migración Room,
  renumerada a v10→v11 al integrar — la rama se había ramificado antes de
  Login/tarjetas de crédito y traía su propia v7→v8, ya ocupada en main por
  `password_hash`). Un abono es un gasto con `goal_id`: sale de la cuenta,
  aparece en Inicio como "Ahorro" y se excluye del indicador y de los
  reportes. El avance de la meta se deriva con `SUM`, nunca se persiste.
- `:feature-goals`: meta destacada con anillo y abono rápido, otras metas,
  metas logradas, racha de semanas consecutivas con abono, editor con plazo
  opcional. La meta se completa sola al alcanzar el objetivo. Reemplaza el
  `GoalsPlaceholderScreen` que traía el banner de alertas RF09 de Login.

**Reportes (RF06)**
- `ReportDao` agrega en SQLite (totales por clasificación, tendencia mensual,
  gasto por categoría). Sin tablas nuevas.
- `:feature-reports`: resumen con variación vs mes anterior, gráfica de 6
  meses, matriz ingreso × gasto con lectura en lenguaje simple
  (`CrossMatrixCalculator`), top categorías. Navegación por mes.

**core-ui**
- `EqProgressRing`, `EqProgressBar`, `EqStatTile`, `EqBarChart`,
  `EqMatrixCell`, `EqChip`; `EqTopBar` acepta `subtitle`; helpers de monto
  compartidos (`AmountInput.kt`); `DomainTone.color/deepColor/softColor`.

## 2026-09-21

**Login con correo/contraseña (RF01) — merge de rama `Login`**
- `LoginScreen`/`LoginViewModel`/`LoginUiState` (nuevo, `:app`): tabs "Iniciar
  sesión"/"Registrarse", validación de email/contraseña, accesible desde
  Perfil (no es pantalla de arranque, no bloquea el uso de la app). Reutiliza
  `GoogleAuthClient` existente para el botón de Google.
- `PasswordHasher` (nuevo, `:core-data`): hash/verificación con Argon2id
  (`argon2kt`). `RegisterWithPasswordUseCase`/`SignInWithPasswordUseCase`
  (nuevo, `:core-domain`).
- Nueva columna `users.password_hash` (migración Room v7→v8).
- Alertas (RF09): banner `GOAL_AT_RISK` conectado en Cuentas y Metas
  (`GoalsPlaceholderScreen`/`ViewModel`, nuevo) — el esquema/DAO/casos de uso
  ya existían de una sesión anterior.
- La rama `Login` se había ramificado antes de que main incorporara
  transferencias/estado de transacción (sesión 2026-09-19) y reconstruyó esa
  misma feature en paralelo; el merge resolvió tomando la migración 7→8
  (password_hash) y las adiciones propias de Login sobre lo ya existente en
  main, sin duplicar transfers/status.

**Pendiente (seguimiento del merge, no bloqueante)**
- Índice UNIQUE en `users.email` (riesgo de registro duplicado por carrera).
- Cobertura de tests: PasswordHasher, casos de uso de auth, transferencias, y
  migraciones Room.
- Timing side-channel y rate-limiting en login (hallazgos de seguridad).
- Accesibilidad de `EqColorSlotPicker` y loading state en `AccountsScreen`.

## 2026-09-19

**Categorías — creación con color**
- `:feature-categories` / `:feature-entry`: selector de color (`EqColorSlotPicker`,
  nuevo en `:core-ui`, reusado por ambos) al crear una categoría.
- El formulario de movimientos (QuickEntry) permite crear una categoría nueva
  sin salir del formulario (sub-pestaña "Nueva categoría"), además de asignar
  categoría a un movimiento existente.

**Transferencias entre cuentas**
- Nueva tabla `transfers` (Room, migración v5→v6) que liga un par de
  transacciones (egreso + ingreso) generadas automáticamente al transferir
  entre cuentas.
- Restringido a cuentas de efectivo (`CASH`) y débito (`BANK`) — nunca tarjetas
  de crédito, ni como origen ni como destino.
- Borrado en cascada: eliminar cualquiera de las dos transacciones de una
  transferencia elimina (soft-delete) la otra y el registro de `transfers`.
- QuickEntry suma una tercera opción "Transferencia" junto a Ingreso/Gasto.

**Disponible calculado por cuenta**
- Cuentas de efectivo y débito ya no muestran el saldo inicial estático:
  `ObserveAvailableBalances` (nuevo, `:core-domain`) calcula el disponible como
  saldo inicial + neto de movimientos `COMPLETED` de esa cuenta. Tarjetas de
  crédito no se tocan (fuera de alcance por ahora).

**Estado de transacción — movimientos programados a futuro**
- Nueva columna `status` (`COMPLETED` / `SCHEDULED`) en `transactions`
  (migración v6→v7). Se calcula automáticamente por fecha al guardar
  (`deriveTransactionStatus`): fecha futura → `SCHEDULED`, hoy o anterior →
  `COMPLETED`. Nunca se recalcula sola con el paso del tiempo — pasar a
  `COMPLETED` requiere confirmación explícita del usuario.
- `ConfirmTransaction` (nuevo caso de uso) confirma una transacción programada;
  si es parte de una transferencia, confirma las dos patas juntas.
- El disponible por cuenta y el balance global de Inicio solo cuentan
  transacciones `COMPLETED` — lo programado a futuro no está disponible hoy.
- QuickEntry: al abrir un movimiento existente arranca en una sub-pestaña
  "Ver" (resumen de solo lectura + badge "Programada" + botón "Confirmar" si
  aplica) con un segmented control para pasar a "Editar". El selector de fecha
  ahora es un campo con borde e ícono de calendario (antes un botón de texto
  poco visible) y ya no rechaza fechas futuras.
- Movimientos generados por una transferencia se etiquetan "Transferencia" en
  vez de "Sin clasificar" en la lista de Inicio.

**Pendiente (fuera de alcance de esta sesión)**
- Estados/transacciones específicas de tarjeta de crédito.
- Editar los montos/cuentas de una transferencia ya creada como par (hoy solo
  se puede editar cada pata como transacción individual, o borrar el par).
