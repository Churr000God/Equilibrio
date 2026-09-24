package mx.equilibrio.domain.model.report

/** Resumen de un mes: lo que entró, lo que salió y lo que se abonó a metas. */
data class PeriodSummary(
    val incomeCents: Long,
    val expenseCents: Long,
    val savingsCents: Long,
) {
    /** Lo que quedó: ingreso menos gasto (los abonos no son gasto). */
    val leftoverCents: Long get() = incomeCents - expenseCents
}

/** Mes actual contra el anterior. `delta*` es null cuando no hay base de comparación. */
data class PeriodComparison(
    val current: PeriodSummary,
    val previous: PeriodSummary,
) {
    val incomeDelta: Float? get() = delta(current.incomeCents, previous.incomeCents)
    val expenseDelta: Float? get() = delta(current.expenseCents, previous.expenseCents)
    val leftoverDelta: Float? get() = delta(current.leftoverCents, previous.leftoverCents)

    private fun delta(now: Long, before: Long): Float? =
        if (before == 0L) null else (now - before).toFloat() / kotlin.math.abs(before)
}
