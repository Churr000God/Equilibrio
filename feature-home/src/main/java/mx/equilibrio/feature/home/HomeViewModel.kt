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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.usecase.GetCategories
import mx.equilibrio.domain.usecase.GetPendingAlertsUseCase
import mx.equilibrio.domain.usecase.MarkAlertAsReadUseCase
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveBalance
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject
import kotlin.time.Clock

internal fun today() = Clock.System.todayIn(TimeZone.UTC)

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTransactions: ObserveTransactions,
    observeBalance: ObserveBalance,
    getCategories: GetCategories,
    observeAccounts: ObserveAccounts,
    getPendingAlerts: GetPendingAlertsUseCase,
    private val markAlertAsRead: MarkAlertAsReadUseCase,
) : ViewModel() {

    private val currentMonth = YearMonth.of(today())
    private val selectedMonth = MutableStateFlow(currentMonth)

    val state: StateFlow<HomeUiState> = combine(
        selectedMonth,
        observeTransactions(),
        observeBalance(today()),
        combine(getCategories(), observeAccounts(), ::Pair),
        getPendingAlerts(),
    ) { month, transactions, balance, (categories, accounts), alerts ->
        val categoriesById = categories.associateBy { it.id }
        val accountsById = accounts.associateBy { it.id }

        val monthTransactions = transactions
            .filter { it.occurredAt in month }
            .sortedByDescending { it.occurredAt }

        val settled = monthTransactions.filter { it.status == TransactionStatus.COMPLETED && it.goalId == null }
        val incomeCents = settled.filter { it.kind == TransactionKind.INCOME }.sumOf { it.amountCents }
        val expenseCents = settled.filter { it.kind == TransactionKind.EXPENSE }.sumOf { it.amountCents }

        HomeUiState(
            month = month,
            isLoading = false,
            balanceCents = balance.actualCents,
            projectedBalanceCents = balance.projectedCents,
            monthIncomeCents = incomeCents,
            monthExpenseCents = expenseCents,
            canGoForward = month < currentMonth,
            transactions = monthTransactions.map { tx ->
                tx.toMovementUi(
                    categoryName = tx.categoryId?.let { categoriesById[it]?.name },
                    categoryIcon = tx.categoryId?.let { categoriesById[it]?.icon },
                    accountName = accountsById[tx.accountId]?.name,
                )
            },
            pendingAlerts = alerts.toUi(),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(month = currentMonth),
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.PreviousMonth -> selectedMonth.update { it.previous() }
            HomeEvent.NextMonth -> selectedMonth.update { if (it < currentMonth) it.next() else it }
            is HomeEvent.AlertDismissed -> viewModelScope.launch { markAlertAsRead(event.alertId) }
        }
    }
}
