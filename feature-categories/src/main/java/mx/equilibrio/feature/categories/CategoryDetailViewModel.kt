package mx.equilibrio.feature.categories

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
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.usecase.DeleteCategory
import mx.equilibrio.domain.usecase.EnsureOtrosCategory
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveCategoryDetail
import javax.inject.Inject

private data class DeleteState(val isDeleting: Boolean = false, val deleted: Boolean = false)

/**
 * Detalle de una categoría (totales del mes, movimientos recientes, presupuesto) con su propio
 * flujo de borrado. [userId] y [categoryType] se guardan al vuelo desde el último valor combinado
 * porque [delete] los necesita fuera del `combine`, para reasignar movimientos a "Otros" sin volver
 * a pedir el detalle completo.
 */
@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeCategoryDetail: ObserveCategoryDetail,
    observeAccounts: ObserveAccounts,
    private val ensureOtrosCategory: EnsureOtrosCategory,
    private val deleteCategory: DeleteCategory,
) : ViewModel() {

    private val categoryId: String = checkNotNull(savedStateHandle["categoryId"])

    private val _deleteState = MutableStateFlow(DeleteState())
    private var userId: String? = null
    private var categoryType: CategoryType? = null

    val state: StateFlow<CategoryDetailUiState> = combine(
        observeCategoryDetail(categoryId, today()),
        observeAccounts(),
        _deleteState,
    ) { detail, accounts, deleteState ->
        val accountsById = accounts.associateBy(Account::id)
        userId = detail.category.userId
        categoryType = detail.category.type
        CategoryDetailUiState(
            isLoading = false,
            id = detail.category.id,
            name = detail.category.name,
            type = detail.category.type,
            colorSlot = detail.category.colorSlot,
            icon = detail.category.icon,
            isSystem = detail.category.isSystem,
            monthTotalCents = detail.monthTotalCents,
            movementsCount = detail.movementsCount,
            averageCents = detail.averageCents,
            monthlyBudgetCents = detail.category.monthlyBudgetCents,
            recentTransactions = detail.recentTransactions.map { tx ->
                RecentTransactionUi(
                    id = tx.id,
                    note = tx.note?.takeIf { it.isNotBlank() } ?: detail.category.name,
                    dateLabel = tx.occurredAt.shortDateLabel(),
                    accountLabel = accountsById[tx.accountId]?.name ?: "",
                    amountCents = tx.amountCents,
                )
            },
            isDeleting = deleteState.isDeleting,
            deleted = deleteState.deleted,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoryDetailUiState(isLoading = true),
        )

    fun onEvent(event: CategoryDetailEvent) {
        when (event) {
            is CategoryDetailEvent.DeleteConfirmed -> delete(event.reassignToOtros)
        }
    }

    private fun delete(reassignToOtros: Boolean) {
        _deleteState.update { it.copy(isDeleting = true) }
        viewModelScope.launch {
            val targetId = if (reassignToOtros) {
                val owner = userId
                val type = categoryType
                if (owner != null && type != null) ensureOtrosCategory(owner, type).id else null
            } else {
                null
            }
            deleteCategory(categoryId, targetId)
            _deleteState.update { it.copy(isDeleting = false, deleted = true) }
        }
    }
}
