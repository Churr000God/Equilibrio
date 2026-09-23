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
import mx.equilibrio.domain.usecase.DeleteInstallmentPlan
import mx.equilibrio.domain.usecase.DeleteTransaction
import mx.equilibrio.domain.usecase.GetPendingAlertsUseCase
import mx.equilibrio.domain.usecase.MarkAlertAsReadUseCase
import mx.equilibrio.domain.usecase.ObserveBalance
import mx.equilibrio.domain.usecase.ObserveCurrentUser
import mx.equilibrio.domain.usecase.ObserveTransactions
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    observeTransactions: ObserveTransactions,
    observeBalance: ObserveBalance,
    getPendingAlerts: GetPendingAlertsUseCase,
    observeCurrentUser: ObserveCurrentUser,
    private val deleteTransaction: DeleteTransaction,
    private val deleteInstallmentPlan: DeleteInstallmentPlan,
    private val markAlertAsRead: MarkAlertAsReadUseCase,
) : ViewModel() {

    private val pendingDeletion = MutableStateFlow<TransactionUi?>(null)

    val state: StateFlow<HomeUiState> = combine(
        observeTransactions(),
        observeBalance(),
        pendingDeletion,
        getPendingAlerts(),
        observeCurrentUser(),
    ) { transactions, balance, pending, alerts, user ->
        HomeUiState(
            isLoading = false,
            balanceCents = balance,
            transactions = transactions.map {
                TransactionUi(
                    id = it.id,
                    kind = it.kind,
                    classification = it.classification,
                    amountCents = it.amountCents,
                    occurredAt = it.occurredAt,
                    note = it.note,
                    goalId = it.goalId,
                    installmentPlanId = it.installmentPlanId,
                    installmentIndex = it.installmentIndex,
                    installmentCount = it.installmentCount,
                )
            },
            pendingDeletion = pending,
            pendingAlerts = alerts.toUi(),
            // RF01 — el saludo usa el nombre de usuario elegido en Perfil, no el nombre de Google.
            greetingName = user?.displayName?.takeIf { it.isNotBlank() },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(isLoading = true),
    )

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.DeleteRequested -> pendingDeletion.update { event.transaction }
            HomeEvent.DeleteCancelled -> pendingDeletion.update { null }
            HomeEvent.DeleteConfirmed -> {
                val target = pendingDeletion.value ?: return
                pendingDeletion.update { null }
                viewModelScope.launch {
                    if (target.isInstallment) {
                        deleteInstallmentPlan(target.installmentPlanId!!)
                    } else {
                        deleteTransaction(target.id)
                    }
                }
            }

            is HomeEvent.AlertDismissed -> viewModelScope.launch { markAlertAsRead(event.alertId) }
        }
    }
}
