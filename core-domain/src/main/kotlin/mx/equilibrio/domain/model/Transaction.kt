package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/**
 * Un movimiento inválido no puede existir en memoria: el constructor rechaza
 * un par (kind, classification) imposible y un monto no positivo.
 */
data class Transaction(
    val id: String,
    val userId: String,
    val accountId: String,
    val kind: TransactionKind,
    val classification: Classification?,
    val amountCents: Long,
    val occurredAt: LocalDate,
    val note: String? = null,
    val categoryId: String? = null,
    val status: TransactionStatus = TransactionStatus.COMPLETED,
) {
    init {
        require(amountCents > 0) { "amountCents debe ser positivo, fue $amountCents" }
        require(classification == null || classification.isValidFor(kind)) {
            "Classification $classification no es válida para $kind"
        }
    }
}
