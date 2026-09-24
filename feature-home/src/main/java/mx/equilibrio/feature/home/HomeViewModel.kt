package mx.equilibrio.feature.home

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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.usecase.GenerateDueRecurringTransactions
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.GetPendingAlertsUseCase
import mx.equilibrio.domain.usecase.MarkAlertAsReadUseCase
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveBalance
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject
import kotlin.time.Clock

internal fun today() = Clock.System.todayIn(TimeZone.UTC)

private data class FilterState(
    val dateScope: DateScope = DateScope.MONTH,
    val rangeStart: LocalDate? = null,
    val rangeEnd: LocalDate? = null,
    val types: Set<TransactionKind> = emptySet(),
    val accountIds: Set<String> = emptySet(),
    val categoryIds: Set<String> = emptySet(),
    val statuses: Set<TransactionStatus> = emptySet(),
)

private fun <T> Set<T>.toggled(item: T): Set<T> = if (contains(item)) this - item else this + item

/** Rango de fechas efectivo según el alcance elegido en Filtros. MONTH reusa la navegación de mes ya existente. */
private fun dateRangeFor(scope: DateScope, month: YearMonth, rangeStart: LocalDate?, rangeEnd: LocalDate?): ClosedRange<LocalDate> =
    when (scope) {
        DateScope.WEEK -> {
            val weekStart = today().let { it.minus(it.dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY) }
            weekStart..weekStart.plus(6, DateTimeUnit.DAY)
        }
        DateScope.MONTH -> month.firstDay()..month.lastDay()
        DateScope.YEAR -> LocalDate(month.year, 1, 1)..LocalDate(month.year, 12, 31)
        DateScope.RANGE -> (rangeStart ?: month.firstDay())..(rangeEnd ?: month.lastDay())
    }

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTransactions: ObserveTransactions,
    observeBalance: ObserveBalance,
    getCategories: GetCategories,
    observeAccounts: ObserveAccounts,
    getPendingAlerts: GetPendingAlertsUseCase,
    private val markAlertAsRead: MarkAlertAsReadUseCase,
    private val generateDueRecurringTransactions: GenerateDueRecurringTransactions,
) : ViewModel() {

    private val currentMonth = YearMonth.of(today())
    private val selectedMonth = MutableStateFlow(currentMonth)
    private val _filters = MutableStateFlow(FilterState())

    init {
        viewModelScope.launch { generateDueRecurringTransactions(today()) }
    }

    val state: StateFlow<HomeUiState> = combine(
        combine(selectedMonth, _filters, ::Pair),
        observeTransactions(),
        observeBalance(today()),
        combine(getCategories(), observeAccounts(), ::Pair),
        getPendingAlerts(),
    ) { (month, filters), transactions, balance, (categories, accounts), alerts ->
        val categoriesById = categories.associateBy { it.id }
        val accountsById = accounts.associateBy { it.id }

        val range = dateRangeFor(filters.dateScope, month, filters.rangeStart, filters.rangeEnd)
        val filteredTransactions = transactions
            .filter { it.occurredAt in range }
            .filter { filters.types.isEmpty() || it.kind in filters.types }
            .filter { filters.accountIds.isEmpty() || it.accountId in filters.accountIds }
            .filter { filters.categoryIds.isEmpty() || it.categoryId in filters.categoryIds }
            .filter { filters.statuses.isEmpty() || it.status in filters.statuses }
            .sortedByDescending { it.occurredAt }

        val settled = filteredTransactions.filter { it.status == TransactionStatus.COMPLETED && it.goalId == null }
        val incomeCents = settled.filter { it.kind == TransactionKind.INCOME }.sumOf { it.amountCents }
        val expenseCents = settled.filter { it.kind == TransactionKind.EXPENSE }.sumOf { it.amountCents }

        HomeUiState(
            month = month,
            isLoading = false,
            balanceCents = balance.actualCents,
            projectedBalanceCents = balance.projectedCents,
            monthIncomeCents = incomeCents,
            monthExpenseCents = expenseCents,
            canGoForward = month < maxOf(currentMonth, transactions.maxOfOrNull { YearMonth.of(it.occurredAt) } ?: currentMonth),
            transactions = filteredTransactions.map { tx ->
                tx.toMovementUi(
                    categoryName = tx.categoryId?.let { categoriesById[it]?.name },
                    categoryIcon = tx.categoryId?.let { categoriesById[it]?.icon },
                    accountName = accountsById[tx.accountId]?.name,
                )
            },
            pendingAlerts = alerts.toUi(),
            accounts = accounts,
            categories = categories,
            dateScope = filters.dateScope,
            rangeStart = filters.rangeStart,
            rangeEnd = filters.rangeEnd,
            filterTypes = filters.types,
            filterAccountIds = filters.accountIds,
            filterCategoryIds = filters.categoryIds,
            filterStatuses = filters.statuses,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(month = currentMonth),
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.PreviousMonth -> selectedMonth.update { it.previous() }
            // Sin tope defensivo acá: confía en que el botón "siguiente" ya está deshabilitado
            // cuando canGoForward=false, mismo criterio que PreviousMonth (sin tope tampoco).
            HomeEvent.NextMonth -> selectedMonth.update { it.next() }
            is HomeEvent.AlertDismissed -> viewModelScope.launch { markAlertAsRead(event.alertId) }

            is HomeEvent.DateScopeChanged -> _filters.update { it.copy(dateScope = event.scope) }
            is HomeEvent.RangeStartChanged -> _filters.update { it.copy(rangeStart = event.date) }
            is HomeEvent.RangeEndChanged -> _filters.update { it.copy(rangeEnd = event.date) }
            is HomeEvent.TypeFilterToggled -> _filters.update { it.copy(types = it.types.toggled(event.kind)) }
            is HomeEvent.AccountFilterToggled -> _filters.update { it.copy(accountIds = it.accountIds.toggled(event.accountId)) }
            is HomeEvent.CategoryFilterToggled -> _filters.update { it.copy(categoryIds = it.categoryIds.toggled(event.categoryId)) }
            is HomeEvent.StatusFilterToggled -> _filters.update { it.copy(statuses = it.statuses.toggled(event.status)) }
            HomeEvent.FiltersCleared -> _filters.update { FilterState() }
        }
    }
}
