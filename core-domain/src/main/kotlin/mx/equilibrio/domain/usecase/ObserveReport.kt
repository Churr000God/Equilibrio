package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import mx.equilibrio.domain.calc.CrossMatrixCalculator
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.PeriodComparison
import mx.equilibrio.domain.model.report.PeriodSummary
import mx.equilibrio.domain.model.report.PeriodTotals
import mx.equilibrio.domain.model.report.Report
import mx.equilibrio.domain.repository.ReportRepository
import javax.inject.Inject

class ObserveReport @Inject constructor(
    private val repository: ReportRepository,
) {
    operator fun invoke(month: YearMonth, trendMonths: Int = TREND_MONTHS): Flow<Report> = combine(
        repository.observeTotals(month),
        summaryOf(month),
        summaryOf(month.previous()),
        repository.observeTrend(trendMonths, month),
        repository.observeCategoryBreakdown(month),
    ) { totals, current, previous, trend, categories ->
        Report(
            month = month,
            comparison = PeriodComparison(current = current, previous = previous),
            trend = trend,
            categories = categories,
            matrix = CrossMatrixCalculator.compute(totals),
            insight = CrossMatrixCalculator.insight(totals),
        )
    }

    private fun summaryOf(month: YearMonth): Flow<PeriodSummary> = combine(
        repository.observeTotals(month),
        repository.observeSavingsCents(month),
    ) { totals: PeriodTotals, savings: Long ->
        PeriodSummary(
            incomeCents = totals.incomeCents,
            expenseCents = totals.expenseCents,
            savingsCents = savings,
        )
    }

    companion object {
        const val TREND_MONTHS = 6
    }
}
