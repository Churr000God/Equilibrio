package mx.equilibrio.domain.model

data class Transfer(
    val id: String,
    val expenseTransactionId: String,
    val incomeTransactionId: String,
)
