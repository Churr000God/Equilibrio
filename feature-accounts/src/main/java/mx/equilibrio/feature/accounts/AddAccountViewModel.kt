package mx.equilibrio.feature.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.FreemiumResult
import mx.equilibrio.domain.usecase.CheckFreemiumLimitUseCase
import mx.equilibrio.domain.usecase.ObserveAccounts
import mx.equilibrio.domain.usecase.SaveAccount
import java.util.UUID
import javax.inject.Inject

/**
 * El límite freemium (máx. 2 cuentas BANK/CREDIT_CARD en plan FREE) se valida dos veces: al entrar
 * (ver [init], issue #5) para avisar antes de que el usuario llene el formulario, y otra vez en [save]
 * por si cambia el tipo después de ese aviso inicial.
 */
@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val observeAccounts: ObserveAccounts,
    private val saveAccount: SaveAccount,
    private val checkFreemiumLimit: CheckFreemiumLimitUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AddAccountUiState())
    val state: StateFlow<AddAccountUiState> = _state.asStateFlow()

    init {
        // Avisa del límite FREE al entrar, antes de que el usuario llene el formulario (issue #5).
        viewModelScope.launch {
            if (checkFreemiumLimit(AccountType.BANK) == FreemiumResult.LIMIT_REACHED) {
                _state.update {
                    it.copy(type = AccountType.CASH, nonCashLimitReached = true, freemiumLimitReached = true)
                }
            }
        }
    }

    fun onEvent(event: AddAccountEvent) {
        when (event) {
            is AddAccountEvent.TypeChanged -> _state.update {
                it.copy(
                    type = event.type,
                    creditLimitError = null,
                    statementDayError = null,
                    dueDayError = null,
                    lastDigitsError = null,
                )
            }

            is AddAccountEvent.NameChanged -> _state.update { it.copy(name = event.text, nameError = null) }

            is AddAccountEvent.BalanceChanged -> _state.update {
                it.copy(balanceInput = sanitizeAmountInput(event.raw), balanceError = null)
            }

            is AddAccountEvent.CreditLimitChanged -> _state.update {
                it.copy(creditLimitInput = sanitizeAmountInput(event.raw), creditLimitError = null)
            }

            is AddAccountEvent.StatementDayChanged -> _state.update {
                it.copy(statementDayInput = sanitizeDayInput(event.raw), statementDayError = null)
            }

            is AddAccountEvent.DueDayChanged -> _state.update {
                it.copy(dueDayInput = sanitizeDayInput(event.raw), dueDayError = null)
            }

            is AddAccountEvent.LastDigitsChanged -> _state.update {
                it.copy(lastDigitsInput = event.raw.filter { c -> c.isDigit() }.take(4), lastDigitsError = null)
            }

            is AddAccountEvent.ColorSlotChanged -> _state.update { it.copy(colorSlot = event.slot) }

            AddAccountEvent.SaveClicked -> save()
            AddAccountEvent.FreemiumDialogDismissed -> _state.update { it.copy(freemiumLimitReached = false) }
        }
    }

    private fun save() {
        val current = _state.value
        val invalid = current.validate()
        if (invalid != null) {
            _state.update { invalid }
            return
        }

        _state.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            if (checkFreemiumLimit(current.type) == FreemiumResult.LIMIT_REACHED) {
                _state.update { it.copy(isSaving = false, freemiumLimitReached = true) }
                return@launch
            }

            // Este ViewModel no tiene una fuente de sesión propia: toma el userId de cualquier cuenta existente.
            val ownerId = observeAccounts().first().firstOrNull()?.userId
            if (ownerId == null) {
                _state.update { it.copy(isSaving = false) }
                return@launch
            }

            saveAccount(
                Account(
                    id = UUID.randomUUID().toString(),
                    userId = ownerId,
                    name = current.name.trim(),
                    type = current.type,
                    balanceCents = current.balanceCents ?: 0L,
                    creditLimitCents = current.creditLimitCents.takeIf { current.type == AccountType.CREDIT_CARD },
                    statementDay = current.statementDay.takeIf { current.type == AccountType.CREDIT_CARD },
                    dueDay = current.dueDay.takeIf { current.type == AccountType.CREDIT_CARD },
                    colorSlot = current.colorSlot,
                    lastDigits = current.lastDigits.takeIf { current.type != AccountType.CASH },
                ),
            )
            _state.update { it.copy(isSaving = false, saved = true) }
        }
    }
}

private fun sanitizeAmountInput(raw: String): String {
    val filtered = raw.filterIndexed { index, c -> c.isDigit() || (c == '.' && !raw.take(index).contains('.')) }
    val parts = filtered.split(".")
    return if (parts.size > 1) "${parts[0]}.${parts[1].take(2)}" else filtered
}

private fun sanitizeDayInput(raw: String): String = raw.filter { it.isDigit() }.take(2)
