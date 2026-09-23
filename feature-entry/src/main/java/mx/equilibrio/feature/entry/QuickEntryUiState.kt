package mx.equilibrio.feature.entry

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus

/** Snapshot de los campos editables al cargar una transacción existente, para detectar cambios reales del usuario. */
data class EntrySnapshot(
    val amountInput: String,
    val classification: Classification?,
    val categoryId: String?,
    val accountId: String?,
    val occurredAt: LocalDate,
    val note: String,
)

data class QuickEntryUiState(
    val mode: EntryMode = EntryMode.EXPENSE,
    val kind: TransactionKind = TransactionKind.EXPENSE,
    val amountInput: String = "",
    val classification: Classification? = null,
    val accountId: String? = null,
    val categoryId: String? = null,
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val originAccountId: String? = null,
    val destinationAccountId: String? = null,
    val occurredAt: LocalDate,
    val note: String = "",
    val amountError: String? = null,
    val dateError: String? = null,
    val transferError: String? = null,
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val newCategoryName: String = "",
    val newCategoryNameError: String? = null,
    val newCategoryColorSlot: Int = 0,
    val isSavingCategory: Boolean = false,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
    val viewTab: EntryViewTab = EntryViewTab.EDIT,
    val isConfirming: Boolean = false,
    val confirmError: String? = null,
    val loadedSnapshot: EntrySnapshot? = null,
    val creditPeriod: Period? = null,
    val creditAvailable: Map<String, Long> = emptyMap(),
    val creditLimitError: String? = null,
) {
    val amountCents: Long
        get() = amountInput.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L

    val canSave: Boolean
        get() = when (mode) {
            EntryMode.TRANSFER -> amountCents > 0 &&
                originAccountId != null &&
                destinationAccountId != null &&
                originAccountId != destinationAccountId &&
                !isSaving
            EntryMode.CREDIT_PURCHASE -> amountCents > 0 && accountId != null && !isSaving
            EntryMode.EXPENSE, EntryMode.INCOME -> amountCents > 0 &&
                classification != null &&
                accountId != null &&
                !isSaving
        }

    val hasUnsavedInput: Boolean
        get() {
            val snapshot = loadedSnapshot
            return if (snapshot != null) {
                EntrySnapshot(amountInput, classification, categoryId, accountId, occurredAt, note) != snapshot
            } else {
                amountInput.isNotBlank() ||
                    note.isNotBlank() ||
                    classification != null ||
                    originAccountId != null ||
                    destinationAccountId != null
            }
        }
}
