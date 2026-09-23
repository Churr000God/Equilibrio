package mx.equilibrio.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.usecase.DeleteInstallmentPlan
import mx.equilibrio.domain.usecase.DeleteTransaction
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject

private data class DeleteState(val isDeleting: Boolean = false, val deleted: Boolean = false)

@HiltViewModel
class TransactionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeTransactions: ObserveTransactions,
    getCategories: GetCategories,
    observeAccounts: ObserveAccounts,
    private val deleteTransaction: DeleteTransaction,
    private val deleteInstallmentPlan: DeleteInstallmentPlan,
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle["transactionId"])

    private val _deleteState = MutableStateFlow(DeleteState())

    val state: StateFlow<TransactionDetailUiState> = combine(
        observeTransactions(),
        getCategories(),
        observeAccounts(),
        _deleteState,
    ) { transactions, categories, accounts, deleteState ->
        val target = transactions.find { it.id == transactionId }
            ?: return@combine TransactionDetailUiState(isLoading = false, found = false, deleted = deleteState.deleted)

        val categoriesById = categories.associateBy(Category::id)
        val accountsById = accounts.associateBy(Account::id)

        val plan = target.installmentPlanId?.let { planId ->
            val planTransactions = transactions.filter { it.installmentPlanId == planId }
            val pending = planTransactions.filter { it.status == TransactionStatus.SCHEDULED }
            InstallmentPlanUi(
                currentIndex = target.installmentIndex ?: 0,
                count = target.installmentCount ?: planTransactions.size,
                remainingCents = pending.sumOf { it.amountCents },
                nextDueDateLabel = pending.minByOrNull { it.occurredAt }?.occurredAt?.longLabel(),
            )
        }

        TransactionDetailUiState(
            isLoading = false,
            found = true,
            id = target.id,
            kind = target.kind,
            status = target.status,
            amountCents = target.amountCents,
            occurredAt = target.occurredAt,
            note = target.note,
            accountName = accountsById[target.accountId]?.name,
            categoryName = target.categoryId?.let { categoriesById[it]?.name },
            categoryIcon = target.categoryId?.let { categoriesById[it]?.icon },
            isSavings = target.isSavings,
            installmentPlanId = target.installmentPlanId,
            installmentIndex = target.installmentIndex,
            installmentCount = target.installmentCount,
            plan = plan,
            isDeleting = deleteState.isDeleting,
            deleted = deleteState.deleted,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TransactionDetailUiState(),
        )

    fun onEvent(event: TransactionDetailEvent) {
        when (event) {
            TransactionDetailEvent.DeleteConfirmed -> delete()
        }
    }

    private fun delete() {
        val current = state.value
        if (!current.found) return
        _deleteState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            if (current.isInstallment) {
                deleteInstallmentPlan(current.installmentPlanId!!)
            } else {
                deleteTransaction(current.id)
            }
            _deleteState.update { it.copy(isDeleting = false, deleted = true) }
        }
    }
}
