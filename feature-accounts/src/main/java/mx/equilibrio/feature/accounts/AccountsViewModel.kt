package mx.equilibrio.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.ObserveAvailableBalances
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    observeAccounts: ObserveAccounts,
    observeAvailableBalances: ObserveAvailableBalances,
) : ViewModel() {

    private val selectedAccountId = MutableStateFlow<String?>(null)

    val state: StateFlow<AccountsUiState> = combine(
        observeAccounts(),
        selectedAccountId,
        observeAvailableBalances(),
    ) { accounts, selectedId, availableBalances ->
        val uiAccounts = accounts.map { it.toUi(availableBalances) }
        AccountsUiState(
            isLoading = false,
            accounts = uiAccounts,
            selectedAccountId = selectedId?.takeIf { id -> uiAccounts.any { it.id == id } }
                ?: uiAccounts.firstOrNull()?.id,
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AccountsUiState(isLoading = true),
        )

    /** Se conecta desde el carrusel; la sección de detalle que depende de esto llega en una tarea aparte. */
    fun onAccountSelected(id: String) {
        selectedAccountId.value = id
    }

    private fun Account.toUi(availableBalances: Map<String, Long>) = AccountUi(
        id = id,
        name = name,
        type = type,
        balanceCents = if (type == AccountType.CASH || type == AccountType.BANK) {
            availableBalances[id] ?: balanceCents
        } else {
            balanceCents
        },
        colorSlot = colorSlot,
        lastDigits = lastDigits,
        creditLimitCents = creditLimitCents,
        dueDay = dueDay,
    )
}
