package mx.equilibrio.feature.accounts

import mx.equilibrio.domain.model.AccountType

sealed interface AddAccountEvent {
    data class TypeChanged(val type: AccountType) : AddAccountEvent
    data class NameChanged(val text: String) : AddAccountEvent
    data class BalanceChanged(val raw: String) : AddAccountEvent
    data class CreditLimitChanged(val raw: String) : AddAccountEvent
    data class StatementDayChanged(val raw: String) : AddAccountEvent
    data class DueDayChanged(val raw: String) : AddAccountEvent
    data class LastDigitsChanged(val raw: String) : AddAccountEvent
    data class ColorSlotChanged(val slot: Int) : AddAccountEvent
    data object SaveClicked : AddAccountEvent
}
