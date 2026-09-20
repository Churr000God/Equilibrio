package mx.equilibrio.feature.entry

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.ui.components.toCentsOrZero

data class QuickEntryUiState(
    val kind: TransactionKind = TransactionKind.EXPENSE,
    val amountInput: String = "",
    val classification: Classification? = null,
    val accountId: String? = null,
    val occurredAt: LocalDate,
    val note: String = "",
    val amountError: String? = null,
    val dateError: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
) {
    val amountCents: Long
        get() = amountInput.toCentsOrZero()

    val canSave: Boolean
        get() = amountCents > 0 && classification != null && accountId != null && dateError == null && !isSaving
}
