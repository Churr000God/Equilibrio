# Changelog

Registro de avance funcional de Equilibrio, por sesión de trabajo. Complementa
`PLAN_DESARROLLO.md` (que describe el diseño objetivo) con lo que ya está
construido.

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
