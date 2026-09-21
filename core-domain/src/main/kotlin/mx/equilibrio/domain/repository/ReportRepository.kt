package mx.equilibrio.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.CategoryShare
import mx.equilibrio.domain.model.report.MonthlyTrendPoint
import mx.equilibrio.domain.model.report.PeriodTotals

/**
 * Consultas agregadas sobre movimientos completados. Ninguna incluye abonos
 * a metas (`goalId != null`) salvo `observeSavingsCents`.
 */
interface ReportRepository {
    fun observeTotals(month: YearMonth): Flow<PeriodTotals>
    fun observeTrend(months: Int, until: YearMonth): Flow<List<MonthlyTrendPoint>>
    fun observeCategoryBreakdown(month: YearMonth): Flow<List<CategoryShare>>
    fun observeSavingsCents(month: YearMonth): Flow<Long>
}
