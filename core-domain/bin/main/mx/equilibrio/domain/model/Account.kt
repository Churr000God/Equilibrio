package mx.equilibrio.domain.model

data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: AccountType,
    val balanceCents: Long = 0,
    // creditLimitCents/statementDay/dueDay solo aplican a AccountType.CREDIT_CARD; en CASH/BANK viajan null.
    val creditLimitCents: Long? = null,
    val statementDay: Int? = null,
    val dueDay: Int? = null,
    val colorSlot: Int = 0,
    val lastDigits: Int? = null,
)
