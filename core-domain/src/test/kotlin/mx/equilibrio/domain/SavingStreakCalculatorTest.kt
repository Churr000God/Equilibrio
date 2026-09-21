package mx.equilibrio.domain

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.calc.SavingStreakCalculator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SavingStreakCalculatorTest {

    // 2026-09-16 es miércoles; su semana empieza el lunes 14.
    private val today = LocalDate(2026, 9, 16)

    @Test
    fun `sin abonos la racha es cero`() {
        assertEquals(0, SavingStreakCalculator.weeks(emptyList(), today))
    }

    @Test
    fun `un abono esta semana da racha de 1`() {
        assertEquals(1, SavingStreakCalculator.weeks(listOf(LocalDate(2026, 9, 14)), today))
    }

    @Test
    fun `abonos en semanas consecutivas suman`() {
        val dates = listOf(
            LocalDate(2026, 9, 15), // esta semana
            LocalDate(2026, 9, 9), // semana pasada
            LocalDate(2026, 9, 1), // hace dos
        )
        assertEquals(3, SavingStreakCalculator.weeks(dates, today))
    }

    @Test
    fun `varios abonos en la misma semana cuentan una vez`() {
        val dates = listOf(LocalDate(2026, 9, 14), LocalDate(2026, 9, 15), LocalDate(2026, 9, 16))
        assertEquals(1, SavingStreakCalculator.weeks(dates, today))
    }

    @Test
    fun `sin abono esta semana la racha sigue viva desde la anterior`() {
        val dates = listOf(LocalDate(2026, 9, 13), LocalDate(2026, 9, 6)) // domingos de las dos semanas previas
        assertEquals(2, SavingStreakCalculator.weeks(dates, today))
    }

    @Test
    fun `un hueco de una semana rompe la racha`() {
        val dates = listOf(LocalDate(2026, 9, 15), LocalDate(2026, 9, 1))
        assertEquals(1, SavingStreakCalculator.weeks(dates, today))
    }

    @Test
    fun `abonos de hace mas de una semana sin continuidad dan cero`() {
        assertEquals(0, SavingStreakCalculator.weeks(listOf(LocalDate(2026, 8, 20)), today))
    }
}
