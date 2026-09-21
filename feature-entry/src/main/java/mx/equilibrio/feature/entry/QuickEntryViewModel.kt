package mx.equilibrio.feature.entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.deriveTransactionStatus
import mx.equilibrio.domain.usecase.CheckBalanceDeviationAlertUseCase
import mx.equilibrio.domain.usecase.ConfirmTransaction
import mx.equilibrio.domain.usecase.CreateTransfer
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.GetOrCreatePeriodForDate
import mx.equilibrio.domain.usecase.GetTransaction
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveCreditAvailable
import mx.equilibrio.domain.usecase.SaveCategory
import mx.equilibrio.domain.usecase.SaveCreditPurchase
import mx.equilibrio.domain.usecase.SaveTransaction
import java.util.UUID
import javax.inject.Inject

fun today() = Clock.System.todayIn(TimeZone.UTC)

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeAccounts: ObserveAccounts,
    private val getTransaction: GetTransaction,
    private val saveTransaction: SaveTransaction,
    private val getCategories: GetCategories,
    private val saveCategory: SaveCategory,
    private val createTransfer: CreateTransfer,
    private val confirmTransaction: ConfirmTransaction,
    private val checkBalanceDeviation: CheckBalanceDeviationAlertUseCase,
    private val saveCreditPurchaseUseCase: SaveCreditPurchase,
    private val observeCreditAvailableUseCase: ObserveCreditAvailable,
    private val getOrCreatePeriodForDate: GetOrCreatePeriodForDate,
) : ViewModel() {

    private val editingId: String? = savedStateHandle["transactionId"]

    private val _state = MutableStateFlow(QuickEntryUiState(occurredAt = today()))
    val state: StateFlow<QuickEntryUiState> = _state.asStateFlow()

    private var userId: String? = null

    init {
        viewModelScope.launch {
            val accounts = observeAccounts().first()
            val defaultAccountId = accounts.firstOrNull()?.id
            userId = accounts.firstOrNull()?.userId

            val existing = editingId?.let { getTransaction(it) }
            if (existing != null) {
                userId = existing.userId
                val amountInput = formatAmountInput(existing.amountCents)
                val note = existing.note.orEmpty()
                _state.update {
                    it.copy(
                        mode = if (existing.kind == TransactionKind.EXPENSE) EntryMode.EXPENSE else EntryMode.INCOME,
                        kind = existing.kind,
                        amountInput = amountInput,
                        classification = existing.classification,
                        accountId = existing.accountId,
                        categoryId = existing.categoryId,
                        occurredAt = existing.occurredAt,
                        note = note,
                        isEditing = true,
                        status = existing.status,
                        viewTab = EntryViewTab.VIEW,
                        loadedSnapshot = EntrySnapshot(
                            amountInput = amountInput,
                            classification = existing.classification,
                            categoryId = existing.categoryId,
                            accountId = existing.accountId,
                            occurredAt = existing.occurredAt,
                            note = note,
                        ),
                    )
                }
            } else {
                _state.update { it.copy(accountId = defaultAccountId) }
            }
        }

        viewModelScope.launch {
            getCategories().collect { categories ->
                _state.update { it.copy(categories = categories) }
            }
        }

        viewModelScope.launch {
            observeAccounts().collect { accounts ->
                _state.update { it.copy(accounts = accounts) }
            }
        }

        viewModelScope.launch {
            observeCreditAvailableUseCase().collect { available ->
                _state.update { it.copy(creditAvailable = available) }
            }
        }
    }

    fun onEvent(event: QuickEntryEvent) {
        when (event) {
            is QuickEntryEvent.EntryModeChanged -> {
                _state.update {
                    val newKind = when (event.mode) {
                        EntryMode.EXPENSE -> TransactionKind.EXPENSE
                        EntryMode.INCOME -> TransactionKind.INCOME
                        EntryMode.TRANSFER -> it.kind
                        EntryMode.CREDIT_PURCHASE -> TransactionKind.EXPENSE
                    }
                    val newAccountId = if (event.mode == EntryMode.CREDIT_PURCHASE) {
                        it.accounts.firstOrNull { account -> account.type == AccountType.CREDIT_CARD }?.id
                    } else {
                        it.accountId
                    }
                    it.copy(
                        mode = event.mode,
                        kind = newKind,
                        accountId = newAccountId,
                        classification = null,
                        categoryId = null,
                        isCreatingCategory = false,
                        transferError = null,
                        creditLimitError = null,
                        creditPeriod = if (event.mode == EntryMode.CREDIT_PURCHASE) it.creditPeriod else null,
                    )
                }
                refreshCreditPeriodIfNeeded()
            }

            is QuickEntryEvent.AmountChanged -> _state.update {
                it.copy(amountInput = sanitizeAmountInput(event.raw), amountError = null, creditLimitError = null)
            }

            is QuickEntryEvent.ClassificationChanged -> _state.update {
                it.copy(classification = event.classification)
            }

            is QuickEntryEvent.DateChanged -> {
                _state.update { it.copy(occurredAt = event.date) }
                refreshCreditPeriodIfNeeded()
            }

            is QuickEntryEvent.CreditAccountSelected -> {
                _state.update { it.copy(accountId = event.accountId, creditLimitError = null) }
                refreshCreditPeriodIfNeeded()
            }

            is QuickEntryEvent.NoteChanged -> _state.update { it.copy(note = event.text) }

            is QuickEntryEvent.CategorySelected -> _state.update { it.copy(categoryId = event.categoryId) }

            is QuickEntryEvent.CreateCategoryTabToggled -> _state.update {
                it.copy(
                    isCreatingCategory = event.show,
                    newCategoryName = "",
                    newCategoryNameError = null,
                    newCategoryColorSlot = 0,
                )
            }

            is QuickEntryEvent.NewCategoryNameChanged -> _state.update {
                it.copy(newCategoryName = event.text, newCategoryNameError = null)
            }

            is QuickEntryEvent.NewCategoryColorSlotChanged -> _state.update {
                it.copy(newCategoryColorSlot = event.slot)
            }

            QuickEntryEvent.SaveCategoryClicked -> saveNewCategory()

            is QuickEntryEvent.OriginAccountSelected -> _state.update {
                it.copy(originAccountId = event.accountId, transferError = null)
            }

            is QuickEntryEvent.DestinationAccountSelected -> _state.update {
                it.copy(destinationAccountId = event.accountId, transferError = null)
            }

            is QuickEntryEvent.ViewTabChanged -> _state.update { it.copy(viewTab = event.tab) }

            QuickEntryEvent.ConfirmClicked -> confirmScheduledTransaction()

            QuickEntryEvent.SaveClicked -> save()
        }
    }

    private fun saveNewCategory() {
        val current = _state.value
        val name = current.newCategoryName.trim()
        if (name.isBlank()) {
            _state.update { it.copy(newCategoryNameError = "Ponle un nombre a la categoría.") }
            return
        }
        val ownerId = userId ?: return

        _state.update { it.copy(isSavingCategory = true) }

        viewModelScope.launch {
            val type = if (current.kind == TransactionKind.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
            val nextSortOrder = current.categories.count { it.type == type }
            val newCategory = Category(
                id = UUID.randomUUID().toString(),
                userId = ownerId,
                name = name,
                type = type,
                colorSlot = current.newCategoryColorSlot,
                icon = "",
                sortOrder = nextSortOrder,
            )
            saveCategory(newCategory)
            _state.update {
                it.copy(
                    isSavingCategory = false,
                    isCreatingCategory = false,
                    newCategoryName = "",
                    newCategoryColorSlot = 0,
                    categoryId = newCategory.id,
                )
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.amountCents <= 0) {
            _state.update { it.copy(amountError = "Agrega un monto para guardar.") }
            return
        }
        if (!current.canSave) return

        when (current.mode) {
            EntryMode.TRANSFER -> saveTransfer(current)
            EntryMode.CREDIT_PURCHASE -> saveCreditPurchase(current)
            EntryMode.EXPENSE, EntryMode.INCOME -> saveIncomeOrExpense(current)
        }
    }

    private fun refreshCreditPeriodIfNeeded() {
        val current = _state.value
        if (current.mode != EntryMode.CREDIT_PURCHASE) return
        val accountId = current.accountId ?: return

        viewModelScope.launch {
            val period = getOrCreatePeriodForDate(accountId, current.occurredAt)
            _state.update { it.copy(creditPeriod = period) }
        }
    }

    private fun saveCreditPurchase(current: QuickEntryUiState) {
        val accountId = current.accountId ?: return
        val ownerId = userId ?: return

        _state.update { it.copy(isSaving = true, creditLimitError = null) }

        viewModelScope.launch {
            try {
                saveCreditPurchaseUseCase(
                    id = editingId ?: UUID.randomUUID().toString(),
                    accountId = accountId,
                    classification = current.classification,
                    amountCents = current.amountCents,
                    occurredAt = current.occurredAt,
                    note = current.note.ifBlank { null },
                    categoryId = current.categoryId,
                    userId = ownerId,
                    today = today(),
                )
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: IllegalArgumentException) {
                _state.update {
                    it.copy(isSaving = false, creditLimitError = e.message ?: "No se pudo registrar la compra.")
                }
            }
        }
    }

    private fun saveIncomeOrExpense(current: QuickEntryUiState) {
        val accountId = current.accountId ?: return
        val classification = current.classification ?: return
        val ownerId = userId ?: return

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            saveTransaction(
                Transaction(
                    id = editingId ?: UUID.randomUUID().toString(),
                    userId = ownerId,
                    accountId = accountId,
                    kind = current.kind,
                    classification = classification,
                    amountCents = current.amountCents,
                    occurredAt = current.occurredAt,
                    note = current.note.ifBlank { null },
                    categoryId = current.categoryId,
                    status = deriveTransactionStatus(current.occurredAt, today()),
                ),
            )
            checkBalanceDeviation()
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }

    private fun confirmScheduledTransaction() {
        val id = editingId ?: return

        _state.update { it.copy(isConfirming = true) }

        viewModelScope.launch {
            confirmTransaction(id)
            _state.update { it.copy(isConfirming = false, status = TransactionStatus.COMPLETED) }
        }
    }

    private fun saveTransfer(current: QuickEntryUiState) {
        val originId = current.originAccountId ?: return
        val destinationId = current.destinationAccountId ?: return
        val ownerId = current.accounts.firstOrNull { it.id == originId }?.userId ?: userId ?: return

        _state.update { it.copy(isSaving = true, transferError = null) }

        viewModelScope.launch {
            try {
                createTransfer(
                    originAccountId = originId,
                    destinationAccountId = destinationId,
                    amountCents = current.amountCents,
                    occurredAt = current.occurredAt,
                    note = current.note.ifBlank { null },
                    userId = ownerId,
                    expenseTransactionId = UUID.randomUUID().toString(),
                    incomeTransactionId = UUID.randomUUID().toString(),
                    today = today(),
                )
                _state.update { it.copy(isSaving = false, saved = true) }
            } catch (e: IllegalArgumentException) {
                _state.update {
                    it.copy(isSaving = false, transferError = e.message ?: "No se pudo crear la transferencia.")
                }
            }
        }
    }
}

private fun sanitizeAmountInput(raw: String): String {
    val filtered = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && !raw.take(index).contains('.')) }
    val parts = filtered.split(".")
    return if (parts.size > 1) "${parts[0]}.${parts[1].take(2)}" else filtered
}

private fun formatAmountInput(cents: Long): String {
    val whole = cents / 100
    val fraction = cents % 100
    return if (fraction == 0L) whole.toString() else "$whole.${fraction.toString().padStart(2, '0')}"
}
