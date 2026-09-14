package mx.equilibrio.feature.accounts

import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.ui.theme.DomainTone

data class AccountUi(
    val id: String,
    val name: String,
    val type: AccountType,
    val balanceCents: Long,
    val colorSlot: Int = 0,
    val lastDigits: Int? = null,
    val creditLimitCents: Long? = null,
    val dueDay: Int? = null,
) {
    val typeLabel: String
        get() = when (type) {
            AccountType.CASH -> "Efectivo"
            AccountType.BANK -> "Banco"
            AccountType.CREDIT_CARD -> "Tarjeta de crédito"
        }

    val tone: DomainTone
        get() = if (balanceCents < 0) DomainTone.RECREATIONAL else DomainTone.ESSENTIAL

    /** "•••• 4821", o "****" si aún no hay dígitos que mostrar. */
    val maskedDigits: String
        get() = lastDigits?.let { "•••• " + it.toString().padStart(4, '0').takeLast(4) } ?: "****"
}

data class AccountsUiState(
    val isLoading: Boolean = true,
    val accounts: List<AccountUi> = emptyList(),
    val selectedAccountId: String? = null,
) {
    val isEmpty: Boolean get() = !isLoading && accounts.isEmpty()
}
