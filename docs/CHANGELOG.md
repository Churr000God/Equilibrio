# Changelog

Registro de avance funcional de Equilibrio, por sesión de trabajo. Complementa
`PLAN_DESARROLLO.md` (que describe el diseño objetivo) con lo que ya está
construido.

## 2026-09-20

**Metas de ahorro (RF08)**
- Nueva tabla `goals` y columna `transactions.goal_id` (Room, migración v7→v8).
  Un abono es un gasto con `goal_id`: sale de la cuenta, aparece en Inicio como
  "Ahorro" y se excluye del indicador y de los reportes. El avance de la meta
  se deriva con `SUM`, nunca se persiste.
- `:feature-goals`: meta destacada con anillo y abono rápido, otras metas,
  metas logradas, racha de semanas consecutivas con abono, editor con plazo
  opcional. La meta se completa sola al alcanzar el objetivo.

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
