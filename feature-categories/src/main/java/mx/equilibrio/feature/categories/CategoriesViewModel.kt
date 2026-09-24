package mx.equilibrio.feature.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.CategoryType
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject

internal fun today(): LocalDate = Clock.System.todayIn(TimeZone.UTC)

/**
 * Lista de categorías con su gasto/ingreso del mes seleccionado. Combina tres flows (mes,
 * categorías, transacciones) para recalcular totales y proporciones cada vez que cambia
 * cualquiera de ellos.
 */
@HiltViewModel
class CategoriesViewModel @Inject constructor(
    getCategories: GetCategories,
    observeTransactions: ObserveTransactions,
) : ViewModel() {

    private val currentMonth = YearMonth.of(today())
    private val selectedMonth = MutableStateFlow(currentMonth)

    val state: StateFlow<CategoriesUiState> = combine(
        selectedMonth,
        getCategories(),
        observeTransactions(),
    ) { month, categories, transactions ->
        // Solo movimientos ya confirmados del mes; los abonos a metas (goalId != null) no cuentan aquí
        // porque ya se reportan en feature-goals y duplicarían el gasto/ingreso por categoría.
        val monthTransactions = transactions.filter {
            it.status == TransactionStatus.COMPLETED && it.goalId == null && it.occurredAt in month
        }
        val totalsByCategory: Map<String?, Pair<Long, Int>> = monthTransactions
            .filter { it.categoryId != null }
            .groupBy { it.categoryId }
            .mapValues { (_, txs) -> txs.sumOf { it.amountCents } to txs.size }

        val categoryTypeById = categories.associateBy({ it.id }, { it.type })
        val expenseTotal = totalsByCategory.entries
            .filter { (id, _) -> categoryTypeById[id] == CategoryType.EXPENSE }
            .sumOf { it.value.first }
        val incomeTotal = totalsByCategory.entries
            .filter { (id, _) -> categoryTypeById[id] == CategoryType.INCOME }
            .sumOf { it.value.first }

        CategoriesUiState(
            month = month,
            isLoading = false,
            categories = categories
                .map { it.toUi(totalsByCategory[it.id], expenseTotal, incomeTotal) }
                .sortedByDescending { it.amountCents },
            expenseTotalCents = expenseTotal,
            incomeTotalCents = incomeTotal,
            canGoForward = month < currentMonth,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CategoriesUiState(month = currentMonth, isLoading = true),
        )

    fun onEvent(event: CategoriesEvent) {
        when (event) {
            CategoriesEvent.PreviousMonth -> selectedMonth.update { it.previous() }
            CategoriesEvent.NextMonth -> selectedMonth.update { if (it < currentMonth) it.next() else it }
        }
    }

    private fun Category.toUi(agg: Pair<Long, Int>?, expenseTotal: Long, incomeTotal: Long): CategoryUi {
        val amount = agg?.first ?: 0L
        val count = agg?.second ?: 0
        val typeTotal = if (type == CategoryType.EXPENSE) expenseTotal else incomeTotal
        return CategoryUi(
            id = id,
            name = name,
            type = type,
            colorSlot = colorSlot,
            icon = icon,
            amountCents = amount,
            movementsCount = count,
            share = if (typeTotal == 0L) 0f else amount.toFloat() / typeTotal.toFloat(),
        )
    }
}
