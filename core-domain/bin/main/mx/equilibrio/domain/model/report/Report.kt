package mx.equilibrio.domain.model.report

import mx.equilibrio.domain.model.YearMonth

data class Report(
    val month: YearMonth,
    val comparison: PeriodComparison,
    val trend: List<MonthlyTrendPoint>,
    val categories: List<CategoryShare>,
    val matrix: CrossMatrix,
    val insight: MatrixInsight,
) {
    // Vacío si el mes actual no tuvo ingreso, gasto ni abonos a metas; no considera el mes anterior.
    val isEmpty: Boolean
        get() = comparison.current.incomeCents == 0L &&
            comparison.current.expenseCents == 0L &&
            comparison.current.savingsCents == 0L
}
