package mx.equilibrio.feature.home

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.ui.theme.DomainTone

data class TransactionUi(
    val id: String,
    val kind: TransactionKind,
    val classification: Classification?,
    val amountCents: Long,
    val occurredAt: LocalDate,
    val note: String?,
    val category: Category? = null,
) {
    /** Abono espejo de una meta: se edita desde Metas, no desde aquí. */
    val isSavings: Boolean get() = category == Category.SAVINGS

    val tone: DomainTone
        get() = if (isSavings) DomainTone.ESSENTIAL else when (classification) {
            Classification.FIXED -> DomainTone.INCOME_FIXED
            Classification.VARIABLE -> DomainTone.INCOME_VARIABLE
            Classification.ESSENTIAL -> DomainTone.ESSENTIAL
            Classification.RECREATIONAL -> DomainTone.RECREATIONAL
            null -> DomainTone.NEUTRAL
        }

    val label: String
        get() = if (isSavings) "Ahorro" else when (classification) {
            Classification.FIXED -> "Fijo"
            Classification.VARIABLE -> "Variable"
            Classification.ESSENTIAL -> "Esencial"
            Classification.RECREATIONAL -> "Recreativo"
            null -> "Sin clasificar"
        }

    val isExpense: Boolean get() = kind == TransactionKind.EXPENSE
}

data class HomeUiState(
    val isLoading: Boolean = true,
    val balanceCents: Long = 0,
    val transactions: List<TransactionUi> = emptyList(),
    val pendingDeletion: TransactionUi? = null,
) {
    val isEmpty: Boolean get() = !isLoading && transactions.isEmpty()
}
