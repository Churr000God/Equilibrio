package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class RecurrenceRulesTest {

    @Test
    fun `WEEKLY suma 7 dias fijos`() {
        val next = nextOccurrence(LocalDate(2026, 9, 1), RecurrenceFrequency.WEEKLY, anchorDay = null)

        assertEquals(LocalDate(2026, 9, 8), next)
    }

    @Test
    fun `BIWEEKLY suma 14 dias fijos`() {
        val next = nextOccurrence(LocalDate(2026, 9, 1), RecurrenceFrequency.BIWEEKLY, anchorDay = null)

        assertEquals(LocalDate(2026, 9, 15), next)
    }

    @Test
    fun `ancla 31 no deriva - enero 31 a febrero 28 a marzo 31`() {
        val febrero = nextOccurrence(LocalDate(2026, 1, 31), RecurrenceFrequency.MONTHLY, anchorDay = 31)
        assertEquals(LocalDate(2026, 2, 28), febrero)

        val marzo = nextOccurrence(febrero, RecurrenceFrequency.MONTHLY, anchorDay = 31)
        assertEquals(LocalDate(2026, 3, 31), marzo, "el ancla original (31) debe recuperarse, no arrastrar el clamp de febrero")
    }

    @Test
    fun `ancla 29 en anio bisiesto no clampea febrero`() {
        // 2028 es bisiesto.
        val next = nextOccurrence(LocalDate(2028, 1, 29), RecurrenceFrequency.MONTHLY, anchorDay = 29)

        assertEquals(LocalDate(2028, 2, 29), next)
    }

    @Test
    fun `MONTHLY sin anchorDay explota`() {
        assertThrows(IllegalArgumentException::class.java) {
            nextOccurrence(LocalDate(2026, 9, 1), RecurrenceFrequency.MONTHLY, anchorDay = null)
        }
    }

    @Test
    fun `anchorDayFor devuelve el dia de startAt solo para MONTHLY`() {
        assertEquals(15, anchorDayFor(LocalDate(2026, 9, 15), RecurrenceFrequency.MONTHLY))
        assertNull(anchorDayFor(LocalDate(2026, 9, 15), RecurrenceFrequency.WEEKLY))
        assertNull(anchorDayFor(LocalDate(2026, 9, 15), RecurrenceFrequency.BIWEEKLY))
    }
}
