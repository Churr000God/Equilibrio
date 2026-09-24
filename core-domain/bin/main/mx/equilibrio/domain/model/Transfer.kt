package mx.equilibrio.domain.model

/**
 * Une el par de transacciones (gasto en la cuenta origen + ingreso en la
 * destino) que juntas representan una transferencia entre cuentas propias;
 * no existe un `TransactionKind` de transferencia separado.
 */
data class Transfer(
    val id: String,
    val expenseTransactionId: String,
    val incomeTransactionId: String,
)
