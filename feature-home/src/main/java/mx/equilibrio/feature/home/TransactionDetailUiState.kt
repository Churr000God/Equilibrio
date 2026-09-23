package mx.equilibrio.feature.home

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus

data class InstallmentPlanUi(
    val currentIndex: Int,
    val count: Int,
    val remainingCents: Long,
    val nextDueDateLabel: String?,
) {
    val progress: Float get() = if (count == 0) 0f else currentIndex.toFloat() / count.toFloat()
}

data class TransactionDetailUiState(
    val isLoading: Boolean = true,
    val found: Boolean = true,
    val id: String = "",
    val kind: TransactionKind = TransactionKind.EXPENSE,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val amountCents: Long = 0,
    val occurredAt: LocalDate? = null,
    val note: String? = null,
    val accountName: String? = null,
    val categoryName: String? = null,
    val categoryIcon: String? = null,
    val isSavings: Boolean = false,
    val installmentPlanId: String? = null,
    val installmentIndex: Int? = null,
    val installmentCount: Int? = null,
    val plan: InstallmentPlanUi? = null,
    val recurringSeriesId: String? = null,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val isRecurringActive: Boolean = false,
    val isPausingRecurring: Boolean = false,
    val isDeleting: Boolean = false,
    val deleted: Boolean = false,
) {
    val isInstallment: Boolean get() = installmentPlanId != null
    val isRecurringOccurrence: Boolean get() = recurringSeriesId != null
    val isExpense: Boolean get() = kind == TransactionKind.EXPENSE
    val isScheduled: Boolean get() = status == TransactionStatus.SCHEDULED

    val typeLabel: String
        get() = when {
            isInstallment -> "Gasto en cuotas"
            isSavings -> "Ahorro"
            isRecurringOccurrence && isExpense -> "Gasto recurrente"
            isRecurringOccurrence -> "Ingreso recurrente"
            isExpense -> "Gasto"
            else -> "Ingreso"
        }

    val recurrenceLabel: String?
        get() = when (recurrenceFrequency) {
            RecurrenceFrequency.WEEKLY -> "Recurrente · Semanal"
            RecurrenceFrequency.BIWEEKLY -> "Recurrente · Quincenal"
            RecurrenceFrequency.MONTHLY -> "Recurrente · Mensual"
            null -> null
        }

    val statusLabel: String get() = if (isScheduled) "Programada" else "Confirmada"

    val movementsToDeleteCount: Int get() = installmentCount ?: 1
}

sealed interface TransactionDetailEvent {
    data object DeleteConfirmed : TransactionDetailEvent
    data object StopRecurringConfirmed : TransactionDetailEvent
}
