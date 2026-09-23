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
    val periodId: String? = null,
    /** Si no es null, este movimiento es el abono espejo de una meta. */
    val goalId: String? = null,
    /** Las 3 propiedades de cuota son todo-o-nada: ver validación en [init]. */
    val installmentPlanId: String? = null,
    val installmentIndex: Int? = null,
    val installmentCount: Int? = null,
) {
    init {
        require(amountCents > 0) { "amountCents debe ser positivo, fue $amountCents" }
        require(classification == null || classification.isValidFor(kind)) {
            "Classification $classification no es válida para $kind"
        }
        require(goalId == null || kind == TransactionKind.EXPENSE) {
            "Un abono a meta siempre es un gasto"
        }
        val installmentFields = listOf(installmentPlanId != null, installmentIndex != null, installmentCount != null)
        require(installmentFields.all { it } || installmentFields.none { it }) {
            "installmentPlanId/installmentIndex/installmentCount son todo-o-nada"
        }
        if (installmentCount != null && installmentIndex != null) {
            require(kind == TransactionKind.EXPENSE) { "Una cuota siempre es un gasto" }
            require(installmentCount >= 2) { "installmentCount debe ser >= 2, fue $installmentCount" }
            require(installmentIndex in 1..installmentCount) {
                "installmentIndex debe estar en 1..$installmentCount, fue $installmentIndex"
            }
        }
    }

    /** Abono a una meta: no se clasifica ni cuenta como gasto en reportes. */
    val isSavings: Boolean get() = goalId != null

    /** Una cuota de una compra a meses. */
    val isInstallment: Boolean get() = installmentPlanId != null
}
