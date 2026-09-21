package mx.equilibrio.domain.model.report

import mx.equilibrio.domain.model.YearMonth

data class MonthlyTrendPoint(
    val month: YearMonth,
    val incomeCents: Long,
    val expenseCents: Long,
)
