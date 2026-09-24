package mx.equilibrio.feature.accounts

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlin.time.Clock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.CreditAvailability
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.usecase.GetPendingAlertsUseCase
import mx.equilibrio.domain.usecase.MarkAlertAsReadUseCase
import mx.equilibrio.domain.usecase.AccountAvailable
import mx.equilibrio.domain.usecase.ObserveAccountsAvailable
import mx.equilibrio.domain.usecase.ObserveCreditAvailable
import mx.equilibrio.domain.usecase.ObservePeriods
import mx.equilibrio.domain.usecase.ObserveTransactions
import mx.equilibrio.domain.usecase.PayCreditPeriod
import mx.equilibrio.domain.usecase.SettleDuePeriods
import javax.inject.Inject

private fun today() = Clock.System.todayIn(TimeZone.UTC)

/**
 * Combina cuentas, disponibilidad de crédito y alertas pendientes para la pantalla de Cuentas, y además
 * administra el sub-estado de periodos de tarjeta ([cardPeriodsState]) y el diálogo de pago de la cuenta
 * seleccionada. Ese sub-estado vive aparte de [state] porque depende de cuál cuenta está seleccionada,
 * no de la lista completa de cuentas.
 */
@HiltViewModel
class AccountsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    observeAccountsAvailable: ObserveAccountsAvailable,
    observeCreditAvailable: ObserveCreditAvailable,
    getPendingAlerts: GetPendingAlertsUseCase,
    private val markAlertAsRead: MarkAlertAsReadUseCase,
    private val observePeriods: ObservePeriods,
    private val settleDuePeriods: SettleDuePeriods,
    private val observeTransactions: ObserveTransactions,
    private val payCreditPeriod: PayCreditPeriod,
) : ViewModel() {

    /** Arranca en la cuenta pedida por la ruta (p. ej. al tocar una tarjeta en Inicio). */
    private val selectedAccountId = MutableStateFlow(savedStateHandle.get<String>(ARG_ACCOUNT_ID))

    val state: StateFlow<AccountsUiState> = combine(
        observeAccountsAvailable(today()),
        selectedAccountId,
        getPendingAlerts(),
        observeCreditAvailable(today()),
    ) { accounts, selectedId, alerts, creditAvailable ->
        val uiAccounts = accounts.map { it.toUi(creditAvailable) }
        AccountsUiState(
            isLoading = false,
            accounts = uiAccounts,
            selectedAccountId = selectedId?.takeIf { id -> uiAccounts.any { it.id == id } }
                ?: uiAccounts.firstOrNull()?.id,
            pendingAlerts = alerts.toUi(),
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState(isLoading = true),
        )

    private val _cardPeriodsState = MutableStateFlow(CardPeriodsUiState())
    val cardPeriodsState: StateFlow<CardPeriodsUiState> = _cardPeriodsState.asStateFlow()

    init {
        viewModelScope.launch {
            selectedAccountId.filterNotNull().collectLatest { accountId ->
                _cardPeriodsState.value = CardPeriodsUiState(accountId = accountId)
                // Cierra periodos vencidos y genera las recurrencias pendientes de esta tarjeta antes de
                // observarlos, para no mostrar un periodo que ya debería estar cerrado.
                settleDuePeriods(accountId, today())
                combine(observePeriods(accountId), observeTransactions()) { periods, transactions ->
                    periods.sortedByDescending { it.startAt }.map { period ->
                        // Solo gasto (EXPENSE) del periodo; pagos y transacciones de otros periodos no cuentan aquí.
                        val spent = transactions
                            .filter { it.periodId == period.id && it.kind == TransactionKind.EXPENSE }
                            .sumOf { it.amountCents }
                        PeriodUi(
                            id = period.id,
                            state = period.state,
                            startAt = period.startAt,
                            endAt = period.endAt,
                            payAt = period.payAt,
                            carriedBalanceCents = period.carriedBalanceCents,
                            spentCents = spent,
                            amountPaidCents = period.amountPaidCents,
                        )
                    }
                }.collect { periodUis ->
                    _cardPeriodsState.update { it.copy(accountId = accountId, periods = periodUis) }
                }
            }
        }
    }

    /** Se conecta desde el carrusel; alimenta también la sección de periodos de tarjeta. */
    fun onAccountSelected(id: String) {
        selectedAccountId.value = id
    }

    fun onAlertDismissed(alertId: String) {
        viewModelScope.launch { markAlertAsRead(alertId) }
    }

    fun onPayClicked(periodId: String) {
        _cardPeriodsState.update {
            it.copy(
                payTargetPeriodId = periodId,
                paySourceAccountId = null,
                payAmountInput = "",
                payError = null,
                paySaved = false,
            )
        }
    }

    fun onPayDismissed() {
        _cardPeriodsState.update { it.copy(payTargetPeriodId = null, payError = null) }
    }

    fun onPaySourceSelected(accountId: String) {
        _cardPeriodsState.update { it.copy(paySourceAccountId = accountId, payError = null) }
    }

    fun onPayAmountChanged(raw: String) {
        _cardPeriodsState.update { it.copy(payAmountInput = sanitizePayAmountInput(raw), payError = null) }
    }

    fun onPayConfirmed() {
        val current = _cardPeriodsState.value
        val cardAccountId = current.accountId ?: return
        val sourceAccountId = current.paySourceAccountId
        if (sourceAccountId == null) {
            _cardPeriodsState.update { it.copy(payError = "Elige la cuenta de origen del pago.") }
            return
        }
        val amountCents = current.payAmountInput.toDoubleOrNull()?.let { (it * 100).toLong() } ?: 0L
        if (amountCents <= 0) {
            _cardPeriodsState.update { it.copy(payError = "Agrega un monto para pagar.") }
            return
        }

        _cardPeriodsState.update { it.copy(isPaying = true, payError = null) }

        viewModelScope.launch {
            try {
                payCreditPeriod(
                    cardAccountId = cardAccountId,
                    sourceAccountId = sourceAccountId,
                    amountCents = amountCents,
                    occurredAt = today(),
                    today = today(),
                )
                _cardPeriodsState.update {
                    it.copy(isPaying = false, paySaved = true, payTargetPeriodId = null, payAmountInput = "")
                }
            } catch (e: IllegalArgumentException) {
                _cardPeriodsState.update {
                    it.copy(isPaying = false, payError = e.message ?: "No se pudo registrar el pago.")
                }
            }
        }
    }

    private fun AccountAvailable.toUi(creditAvailable: Map<String, CreditAvailability>) = AccountUi(
        id = account.id,
        name = account.name,
        type = account.type,
        balanceCents = availableCents,
        colorSlot = account.colorSlot,
        lastDigits = account.lastDigits,
        creditLimitCents = account.creditLimitCents,
        dueDay = account.dueDay,
        projectedBalanceCents = if (account.type == AccountType.CREDIT_CARD) creditAvailable[account.id]?.projectedCents else null,
    )

    companion object {
        /** Argumento opcional de navegación con la cuenta a preseleccionar. */
        const val ARG_ACCOUNT_ID = "accountId"
    }
}

private fun sanitizePayAmountInput(raw: String): String {
    val filtered = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && !raw.take(index).contains('.')) }
    val parts = filtered.split(".")
    return if (parts.size > 1) "${parts[0]}.${parts[1].take(2)}" else filtered
}
