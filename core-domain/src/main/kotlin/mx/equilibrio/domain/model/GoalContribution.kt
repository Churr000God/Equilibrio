package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/** Un abono a una meta. Siempre tiene un movimiento espejo (`transactionId`). */
data class GoalContribution(
    val id: String,
    val goalId: String,
    val transactionId: String,
    val amountCents: Long,
    val occurredAt: LocalDate,
) {
    init {
        require(amountCents > 0) { "amountCents debe ser positivo, fue $amountCents" }
    }
}
