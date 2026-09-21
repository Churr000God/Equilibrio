package mx.equilibrio.domain

import mx.equilibrio.domain.calc.CrossMatrixCalculator
import mx.equilibrio.domain.model.report.MatrixInsight
import mx.equilibrio.domain.model.report.PeriodTotals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CrossMatrixCalculatorTest {

    @Test
    fun `sin datos devuelve matriz vacia y NO_DATA`() {
        val totals = PeriodTotals()
        assertEquals(0L, CrossMatrixCalculator.compute(totals).fixedEssential)
        assertEquals(MatrixInsight.NO_DATA, CrossMatrixCalculator.insight(totals))
    }

    @Test
    fun `solo gasto sin ingreso da NO_INCOME`() {
        val totals = PeriodTotals(essentialExpenseCents = 500_00)
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(0L, m.fixedEssential + m.variableEssential)
        assertEquals(MatrixInsight.NO_INCOME, CrossMatrixCalculator.insight(totals))
    }

    @Test
    fun `fijo cubre esencial y recreativo cuando alcanza`() {
        val totals = PeriodTotals(fixedIncomeCents = 1000_00, essentialExpenseCents = 600_00, recreationalExpenseCents = 200_00)
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(600_00L, m.fixedEssential)
        assertEquals(200_00L, m.fixedRecreational)
        assertEquals(0L, m.variableEssential)
        assertEquals(0L, m.variableRecreational)
        assertEquals(MatrixInsight.BALANCED, CrossMatrixCalculator.insight(totals))
    }

    @Test
    fun `variable paga esencial descubierto cuando fijo no alcanza`() {
        val totals = PeriodTotals(fixedIncomeCents = 400_00, variableIncomeCents = 600_00, essentialExpenseCents = 700_00, recreationalExpenseCents = 100_00)
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(400_00L, m.fixedEssential)
        assertEquals(0L, m.fixedRecreational)
        assertEquals(300_00L, m.variableEssential)
        assertEquals(100_00L, m.variableRecreational)
        assertEquals(MatrixInsight.ESSENTIAL_UNCOVERED, CrossMatrixCalculator.insight(totals))
    }

    @Test
    fun `recreativo sale del variable cuando el fijo se agota en esencial`() {
        // Mockup: fijo 12,880 / variable 5,520 / esencial 8,080 / recreativo 4,880
        val totals = PeriodTotals(
            fixedIncomeCents = 12_880_00,
            variableIncomeCents = 5_520_00,
            essentialExpenseCents = 8_080_00,
            recreationalExpenseCents = 4_880_00,
        )
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(8_080_00L, m.fixedEssential)
        assertEquals(4_800_00L, m.fixedRecreational)
        assertEquals(0L, m.variableEssential)
        assertEquals(80_00L, m.variableRecreational)
        assertEquals(18_400_00L, m.incomeCents)
        assertEquals(0.439f, m.shareOf(m.fixedEssential), 0.001f)
    }

    @Test
    fun `recreativo mayormente variable da RECREATIONAL_FROM_VARIABLE`() {
        val totals = PeriodTotals(fixedIncomeCents = 1000_00, variableIncomeCents = 800_00, essentialExpenseCents = 950_00, recreationalExpenseCents = 500_00)
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(50_00L, m.fixedRecreational)
        assertEquals(450_00L, m.variableRecreational)
        assertEquals(MatrixInsight.RECREATIONAL_FROM_VARIABLE, CrossMatrixCalculator.insight(totals))
    }

    @Test
    fun `gasto mayor que ingreso da OVERSPENT y celdas no exceden ingreso`() {
        val totals = PeriodTotals(fixedIncomeCents = 500_00, essentialExpenseCents = 400_00, recreationalExpenseCents = 400_00)
        val m = CrossMatrixCalculator.compute(totals)
        assertEquals(400_00L, m.fixedEssential)
        assertEquals(100_00L, m.fixedRecreational)
        assertEquals(500_00L, m.fixedEssential + m.fixedRecreational + m.variableEssential + m.variableRecreational)
        assertEquals(MatrixInsight.OVERSPENT, CrossMatrixCalculator.insight(totals))
    }
}
