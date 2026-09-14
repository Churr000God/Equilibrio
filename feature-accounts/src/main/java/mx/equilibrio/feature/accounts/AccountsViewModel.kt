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
import mx.equilibrio.domain.usecase.ObserveAccounts
import javax.inject.Inject

@HiltViewModel
class AccountsViewModel @Inject constructor(
    observeAccounts: ObserveAccounts,
) : ViewModel() {

    private val selectedAccountId = MutableStateFlow<String?>(null)

    val state: StateFlow<AccountsUiState> = combine(observeAccounts(), selectedAccountId) { accounts, selectedId ->
        val uiAccounts = accounts.map { it.toUi() }
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

    private fun Account.toUi() = AccountUi(
        id = id,
        name = name,
        type = type,
        balanceCents = balanceCents,
        colorSlot = colorSlot,
        lastDigits = lastDigits,
        creditLimitCents = creditLimitCents,
        dueDay = dueDay,
    )
}
