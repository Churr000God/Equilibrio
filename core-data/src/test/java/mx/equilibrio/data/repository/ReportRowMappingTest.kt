package mx.equilibrio.data.repository

import mx.equilibrio.data.local.dao.CategoryRow
import mx.equilibrio.data.local.dao.MonthlyRow
import mx.equilibrio.data.local.dao.PeriodTotalRow
import mx.equilibrio.domain.model.Category
import mx.equilibrio.domain.model.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class ReportRowMappingTest {

    @Test
    fun `filas de totales se reparten por clasificacion`() {
        val totals = listOf(
            PeriodTotalRow("INCOME", "FIXED", 100),
            PeriodTotalRow("INCOME", "VARIABLE", 20),
            PeriodTotalRow("INCOME", null, 5),
            PeriodTotalRow("EXPENSE", "ESSENTIAL", 60),
            PeriodTotalRow("EXPENSE", "RECREATIONAL", 30),
            PeriodTotalRow("EXPENSE", null, 3),
        ).toPeriodTotals()

        assertEquals(100L, totals.fixedIncomeCents)
        assertEquals(20L, totals.variableIncomeCents)
        assertEquals(5L, totals.unclassifiedIncomeCents)
        assertEquals(60L, totals.essentialExpenseCents)
        assertEquals(30L, totals.recreationalExpenseCents)
        assertEquals(3L, totals.unclassifiedExpenseCents)
        assertEquals(125L, totals.incomeCents)
        assertEquals(93L, totals.expenseCents)
    }

    @Test
    fun `tendencia rellena meses sin filas con ceros y respeta el orden de la ventana`() {
        val window = listOf(YearMonth(2026, 7), YearMonth(2026, 8), YearMonth(2026, 9))
        val trend = listOf(
            MonthlyRow("2026-07", "INCOME", 100),
            MonthlyRow("2026-09", "EXPENSE", 40),
            MonthlyRow("2026-09", "INCOME", 200),
        ).toTrend(window)

        assertEquals(window, trend.map { it.month })
        assertEquals(100L, trend[0].incomeCents)
        assertEquals(0L, trend[1].incomeCents + trend[1].expenseCents)
        assertEquals(200L, trend[2].incomeCents)
        assertEquals(40L, trend[2].expenseCents)
    }

    @Test
    fun `categorias calculan proporcion sobre el total`() {
        val shares = listOf(CategoryRow("groceries", 75), CategoryRow(null, 25)).toShares()
        assertEquals(Category.GROCERIES, shares[0].category)
        assertEquals(0.75f, shares[0].share, 0.001f)
        assertNull(shares[1].category)
        assertEquals(0.25f, shares[1].share, 0.001f)
    }

    @Test
    fun `sin categorias no divide entre cero`() {
        assertEquals(emptyList<Any>(), emptyList<CategoryRow>().toShares())
    }
}
