package mx.equilibrio.domain.model.report

/**
 * Totales de un periodo por clasificación, en centavos. Excluye movimientos
 * sin clasificar y abonos a metas.
 */
data class PeriodTotals(
    val fixedIncomeCents: Long = 0,
    val variableIncomeCents: Long = 0,
    val essentialExpenseCents: Long = 0,
    val recreationalExpenseCents: Long = 0,
    val unclassifiedIncomeCents: Long = 0,
    val unclassifiedExpenseCents: Long = 0,
) {
    val incomeCents: Long get() = fixedIncomeCents + variableIncomeCents + unclassifiedIncomeCents
    val expenseCents: Long get() = essentialExpenseCents + recreationalExpenseCents + unclassifiedExpenseCents
    val isEmpty: Boolean get() = incomeCents == 0L && expenseCents == 0L
}
