package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate

/**
 * Plantilla de una transacción recurrente (solo CASH/BANK). A diferencia de un
 * plan de cuotas, no genera nada por adelantado: [nextOccurrenceAt] avanza uno
 * a la vez, vía [GenerateDueRecurringTransactions][mx.equilibrio.domain.usecase.GenerateDueRecurringTransactions].
 */
data class RecurringTransaction(
    val id: String,
    val userId: String,
    val accountId: String,
    val kind: TransactionKind,
    val classification: Classification,
    val amountCents: Long,
    val note: String? = null,
    val categoryId: String? = null,
    val frequency: RecurrenceFrequency,
    /** Solo para [RecurrenceFrequency.MONTHLY]: el día-del-mes ancla, ver [addMonthsClamped]. */
    val anchorDay: Int? = null,
    val nextOccurrenceAt: LocalDate,
    val isActive: Boolean = true,
    val createdAt: LocalDate,
) {
    init {
        require(amountCents > 0) { "amountCents debe ser positivo, fue $amountCents" }
        require(classification.isValidFor(kind)) {
            "Classification $classification no es válida para $kind"
        }
        require((frequency == RecurrenceFrequency.MONTHLY) == (anchorDay != null)) {
            "anchorDay es obligatorio si y solo si frequency es MONTHLY"
        }
        require(anchorDay == null || anchorDay in 1..31) {
            "anchorDay debe estar en 1..31, fue $anchorDay"
        }
    }
}
