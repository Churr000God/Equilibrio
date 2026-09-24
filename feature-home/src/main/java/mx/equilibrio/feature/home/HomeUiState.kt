package mx.equilibrio.feature.home

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.ui.theme.DomainTone

/** Alcance de fecha de la pantalla de Filtros — MONTH reusa el selector de mes ya existente. */
enum class DateScope { WEEK, MONTH, YEAR, RANGE }

data class TransactionUi(
    val id: String,
    val kind: TransactionKind,
    val classification: Classification?,
    val amountCents: Long,
    val occurredAt: LocalDate,
    val note: String?,
    val goalId: String? = null,
    val installmentPlanId: String? = null,
    val installmentIndex: Int? = null,
    val installmentCount: Int? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val categoryId: String? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val accountId: String = "",
    val accountName: String? = null,
    val recurringSeriesId: String? = null,
) {
    /** Abono espejo de una meta: se gestiona desde Metas, no desde aquí. */
    val isSavings: Boolean get() = goalId != null

    /** Una cuota de una compra a meses: no editable individualmente, solo borrable como plan completo. */
    val isInstallment: Boolean get() = installmentPlanId != null

    /** Una ocurrencia generada por una transacción recurrente: a diferencia de una cuota, SÍ es
     * editable/borrable como cualquier movimiento normal — ver [mx.equilibrio.domain.model.Transaction.recurringSeriesId]. */
    val isRecurringOccurrence: Boolean get() = recurringSeriesId != null

    val isScheduled: Boolean get() = status == TransactionStatus.SCHEDULED

    val tone: DomainTone
        get() = if (isSavings) DomainTone.ESSENTIAL else when (classification) {
            Classification.FIXED -> DomainTone.INCOME_FIXED
            Classification.VARIABLE -> DomainTone.INCOME_VARIABLE
            Classification.ESSENTIAL -> DomainTone.ESSENTIAL
            Classification.RECREATIONAL -> DomainTone.RECREATIONAL
            null -> DomainTone.NEUTRAL
        }

    val label: String
        get() = if (isSavings) {
            "Ahorro"
        } else if (installmentIndex != null && installmentCount != null) {
            "Cuota $installmentIndex/$installmentCount"
        } else when (classification) {
            Classification.FIXED -> "Fijo"
            Classification.VARIABLE -> "Variable"
            Classification.ESSENTIAL -> "Esencial"
            Classification.RECREATIONAL -> "Recreativo"
            null -> "Transferencia"
        }

    /** "Gasto en cuotas" / "Ahorro" / "Gasto recurrente" / "Gasto" / "Ingreso" — para la pantalla de detalle. */
    val typeLabel: String
        get() = when {
            isInstallment -> "Gasto en cuotas"
            isSavings -> "Ahorro"
            isRecurringOccurrence && kind == TransactionKind.EXPENSE -> "Gasto recurrente"
            isRecurringOccurrence -> "Ingreso recurrente"
            kind == TransactionKind.EXPENSE -> "Gasto"
            else -> "Ingreso"
        }

    val isExpense: Boolean get() = kind == TransactionKind.EXPENSE
}

fun Transaction.toUi() = TransactionUi(
    id = id,
    kind = kind,
    classification = classification,
    amountCents = amountCents,
    occurredAt = occurredAt,
    note = note,
    goalId = goalId,
    installmentPlanId = installmentPlanId,
    installmentIndex = installmentIndex,
    installmentCount = installmentCount,
    status = status,
    categoryId = categoryId,
    accountId = accountId,
    recurringSeriesId = recurringSeriesId,
)

/** Igual que [toUi] pero resolviendo nombre/ícono de categoría y nombre de cuenta (lista de Transacciones y detalle). */
fun Transaction.toMovementUi(categoryName: String?, categoryIcon: String?, accountName: String?) = toUi().copy(
    categoryName = categoryName,
    categoryIcon = categoryIcon,
    accountName = accountName,
)

data class AlertUi(
    val id: String,
    val message: String,
)

private fun Alert.toUi() = AlertUi(
    id = id,
    message = when (type) {
        AlertType.BALANCE_DEVIATION -> "Tu gasto recreativo se desvió de tu equilibrio habitual."
        AlertType.GOAL_AT_RISK -> "Una de tus metas está en riesgo."
        AlertType.CARD_DUE -> "Tienes un pago de tarjeta próximo a vencer."
    },
)

fun List<Alert>.toUi(): List<AlertUi> = filter { it.type == AlertType.BALANCE_DEVIATION }.map { it.toUi() }

data class HomeUiState(
    val month: YearMonth,
    val isLoading: Boolean = true,
    val balanceCents: Long = 0,
    val projectedBalanceCents: Long = 0,
    val monthIncomeCents: Long = 0,
    val monthExpenseCents: Long = 0,
    val canGoForward: Boolean = false,
    val transactions: List<TransactionUi> = emptyList(),
    val pendingAlerts: List<AlertUi> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val dateScope: DateScope = DateScope.MONTH,
    val rangeStart: LocalDate? = null,
    val rangeEnd: LocalDate? = null,
    val filterTypes: Set<TransactionKind> = emptySet(),
    val filterAccountIds: Set<String> = emptySet(),
    val filterCategoryIds: Set<String> = emptySet(),
    val filterStatuses: Set<TransactionStatus> = emptySet(),
) {
    val isEmpty: Boolean get() = !isLoading && transactions.isEmpty()

    val hasActiveFilters: Boolean
        get() = dateScope != DateScope.MONTH ||
            filterTypes.isNotEmpty() ||
            filterAccountIds.isNotEmpty() ||
            filterCategoryIds.isNotEmpty() ||
            filterStatuses.isNotEmpty()
}
