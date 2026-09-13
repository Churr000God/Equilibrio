package mx.equilibrio.domain.model

data class Account(
    val id: String,
    val userId: String,
    val name: String,
    val type: AccountType,
)
