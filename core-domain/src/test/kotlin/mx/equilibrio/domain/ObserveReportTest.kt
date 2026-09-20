package mx.equilibrio.domain

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.YearMonth
import mx.equilibrio.domain.model.report.CategoryShare
import mx.equilibrio.domain.model.report.MatrixInsight
import mx.equilibrio.domain.model.report.MonthlyTrendPoint
import mx.equilibrio.domain.model.report.PeriodTotals
import mx.equilibrio.domain.repository.ReportRepository
import mx.equilibrio.domain.usecase.ObserveReport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ObserveReportTest {

    private val september = YearMonth(2026, 9)

    private class FakeReportRepository(
        private val totals: Map<YearMonth, PeriodTotals>,
        private val savings: Map<YearMonth, Long> = emptyMap(),
        private val categories: List<CategoryShare> = emptyList(),
    ) : ReportRepository {
        override fun observeTotals(month: YearMonth): Flow<PeriodTotals> = flowOf(totals[month] ?: PeriodTotals())
        override fun observeTrend(months: Int, until: YearMonth): Flow<List<MonthlyTrendPoint>> = flowOf(
            (months - 1 downTo 0).map { back ->
                val m = until.minusMonths(back)
                val t = totals[m] ?: PeriodTotals()
                MonthlyTrendPoint(m, t.incomeCents, t.expenseCents)
            },
        )
        override fun observeCategoryBreakdown(month: YearMonth): Flow<List<CategoryShare>> = flowOf(categories)
        override fun observeSavingsCents(month: YearMonth): Flow<Long> = flowOf(savings[month] ?: 0L)
    }

    @Test
    fun `combina resumen, tendencia, matriz y categorias`() = runTest {
        val repo = FakeReportRepository(
            totals = mapOf(
                september to PeriodTotals(fixedIncomeCents = 4200_00, essentialExpenseCents = 742_00, recreationalExpenseCents = 85_00),
                september.previous() to PeriodTotals(fixedIncomeCents = 4000_00, essentialExpenseCents = 900_00),
            ),
            savings = mapOf(september to 220_00),
            categories = listOf(CategoryShare(Category.GROCERIES, 742_00, 0.9f), CategoryShare(Category.OUTINGS, 85_00, 0.1f)),
        )

        ObserveReport(repo)(september).test {
            val report = awaitItem()
            assertEquals(4200_00L, report.comparison.current.incomeCents)
            assertEquals(827_00L, report.comparison.current.expenseCents)
            assertEquals(220_00L, report.comparison.current.savingsCents)
            assertEquals(3373_00L, report.comparison.current.leftoverCents)
            assertEquals(0.05f, report.comparison.incomeDelta!!, 0.001f)
            assertEquals(6, report.trend.size)
            assertEquals(september, report.trend.last().month)
            assertEquals(742_00L, report.matrix.fixedEssential)
            assertEquals(85_00L, report.matrix.fixedRecreational)
            assertEquals(MatrixInsight.BALANCED, report.insight)
            assertEquals(Category.GROCERIES, report.categories.first().category)
            awaitComplete()
        }
    }

    @Test
    fun `sin mes anterior los deltas son null`() = runTest {
        val repo = FakeReportRepository(totals = mapOf(september to PeriodTotals(fixedIncomeCents = 100_00)))
        ObserveReport(repo)(september).test {
            val report = awaitItem()
            assertNull(report.comparison.incomeDelta)
            assertNull(report.comparison.expenseDelta)
            awaitComplete()
        }
    }

    @Test
    fun `mes sin movimientos es vacio`() = runTest {
        ObserveReport(FakeReportRepository(totals = emptyMap()))(september).test {
            assertTrue(awaitItem().isEmpty)
            awaitComplete()
        }
    }
}
