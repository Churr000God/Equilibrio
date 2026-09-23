package mx.equilibrio.feature.home

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Alert
import mx.equilibrio.domain.model.AlertType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.ui.theme.DomainTone

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
) {
    /** Abono espejo de una meta: se gestiona desde Metas, no desde aquí. */
    val isSavings: Boolean get() = goalId != null

    /** Una cuota de una compra a meses: no editable, solo borrable como plan completo. */
    val isInstallment: Boolean get() = installmentPlanId != null

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
    val isLoading: Boolean = true,
    val balanceCents: Long = 0,
    val transactions: List<TransactionUi> = emptyList(),
    val pendingDeletion: TransactionUi? = null,
    val pendingAlerts: List<AlertUi> = emptyList(),
    val greetingName: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && transactions.isEmpty()
}
