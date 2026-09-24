package mx.equilibrio.feature.accounts

import mx.equilibrio.domain.model.AccountType

/** Slots de color fijos (§eqAccountGradient en EqAccountCard.kt) — no hay más que estos 3. */
const val AccountColorSlotCount = 3

data class AddAccountUiState(
    val type: AccountType = AccountType.BANK,
    val name: String = "",
    val balanceInput: String = "",
    val creditLimitInput: String = "",
    val statementDayInput: String = "",
    val dueDayInput: String = "",
    val lastDigitsInput: String = "",
    val colorSlot: Int = 0,
    val nameError: String? = null,
    val balanceError: String? = null,
    val creditLimitError: String? = null,
    val statementDayError: String? = null,
    val dueDayError: String? = null,
    val lastDigitsError: String? = null,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val freemiumLimitReached: Boolean = false,
    /** El plan FREE ya no admite más cuentas BANK/CREDIT_CARD; solo Efectivo. */
    val nonCashLimitReached: Boolean = false,
) {
    val blockedByFreemium: Boolean get() = nonCashLimitReached && type != AccountType.CASH

    val balanceCents: Long? get() = amountToCentsOrNull(balanceInput)
    val creditLimitCents: Long? get() = amountToCentsOrNull(creditLimitInput)
    val statementDay: Int? get() = statementDayInput.toIntOrNull()
    val dueDay: Int? get() = dueDayInput.toIntOrNull()
    val lastDigits: Int? get() = lastDigitsInput.toIntOrNull()

    val canSave: Boolean
        get() = !isSaving && !blockedByFreemium && validate() == null
}

private fun amountToCentsOrNull(input: String): Long? =
    input.toDoubleOrNull()?.let { (it * 100).toLong() }

private fun dayInRange(value: Int?): Boolean = value != null && value in 1..31

/** Devuelve el estado con los mensajes de error llenos, o null si todo pasa. */
fun AddAccountUiState.validate(): AddAccountUiState? {
    val nameError = if (name.isBlank()) "Ponle un nombre a la cuenta." else null
    val balanceError = if (balanceCents == null) "Agrega un saldo válido." else null
    val creditLimit = creditLimitCents
    val creditLimitError = if (type == AccountType.CREDIT_CARD && (creditLimit == null || creditLimit <= 0)) {
        "Agrega el límite de crédito."
    } else {
        null
    }
    val statementDayError = if (type == AccountType.CREDIT_CARD && !dayInRange(statementDay)) {
        "Día de corte entre 1 y 31."
    } else {
        null
    }
    val dueDayError = if (type == AccountType.CREDIT_CARD && !dayInRange(dueDay)) {
        "Día de pago entre 1 y 31."
    } else {
        null
    }
    val lastDigitsError = if (type != AccountType.CASH && lastDigitsInput.isNotBlank() && lastDigits == null) {
        "Solo dígitos, máximo 4."
    } else {
        null
    }

    val hasError = listOf(nameError, balanceError, creditLimitError, statementDayError, dueDayError, lastDigitsError)
        .any { it != null }
    if (!hasError) return null

    return copy(
        nameError = nameError,
        balanceError = balanceError,
        creditLimitError = creditLimitError,
        statementDayError = statementDayError,
        dueDayError = dueDayError,
        lastDigitsError = lastDigitsError,
    )
}
