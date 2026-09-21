package mx.equilibrio.domain

import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.YearMonth
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class YearMonthTest {

    @Test
    fun `primer y ultimo dia de febrero bisiesto`() {
        val feb = YearMonth(2028, 2)
        assertEquals(LocalDate(2028, 2, 1), feb.firstDay())
        assertEquals(LocalDate(2028, 2, 29), feb.lastDay())
    }

    @Test
    fun `previous y next cruzan el anio`() {
        assertEquals(YearMonth(2025, 12), YearMonth(2026, 1).previous())
        assertEquals(YearMonth(2027, 1), YearMonth(2026, 12).next())
    }

    @Test
    fun `minusMonths resta varios meses`() {
        assertEquals(YearMonth(2026, 3), YearMonth(2026, 9).minusMonths(6))
    }

    @Test
    fun `contains verifica fecha dentro del mes`() {
        val sep = YearMonth(2026, 9)
        assertTrue(LocalDate(2026, 9, 30) in sep)
        assertFalse(LocalDate(2026, 10, 1) in sep)
    }

    @Test
    fun `mes fuera de rango lanza`() {
        assertThrows(IllegalArgumentException::class.java) { YearMonth(2026, 13) }
    }

    @Test
    fun `es comparable`() {
        assertTrue(YearMonth(2026, 1) < YearMonth(2026, 2))
        assertTrue(YearMonth(2025, 12) < YearMonth(2026, 1))
    }
}
