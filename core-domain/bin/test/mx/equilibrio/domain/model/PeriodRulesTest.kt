package mx.equilibrio.domain.model

import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PeriodRulesTest {

    @Test
    fun `ciclo intermedio ubica start el dia siguiente al corte anterior y end en el corte de este mes`() {
        val bounds = deriveCycleBounds(statementDay = 15, dueDay = 25, referenceDate = LocalDate(2026, 1, 20))

        assertEquals(LocalDate(2026, 1, 16), bounds.startAt)
        assertEquals(LocalDate(2026, 2, 15), bounds.endAt)
        assertEquals(LocalDate(2026, 2, 25), bounds.payAt)
    }

    @Test
    fun `dia 31 se clampea al ultimo dia de abril junio septiembre y noviembre`() {
        for (mes in listOf(4, 6, 9, 11)) {
            val bounds = deriveCycleBounds(statementDay = 31, dueDay = 5, referenceDate = LocalDate(2026, mes, 10))
            assertEquals(LocalDate(2026, mes, 30), bounds.endAt, "mes $mes debe clampear el corte a 30")
        }
    }

    @Test
    fun `dias 29 30 y 31 se clampean a 28 en febrero de un anio no bisiesto`() {
        for (statementDay in listOf(29, 30, 31)) {
            val bounds = deriveCycleBounds(statementDay, dueDay = 5, referenceDate = LocalDate(2026, 2, 10))
            assertEquals(LocalDate(2026, 2, 28), bounds.endAt, "statementDay $statementDay debe clampear a 28")
        }
    }

    @Test
    fun `dias 30 y 31 se clampean a 29 en febrero de un anio bisiesto`() {
        // 2028 es bisiesto: divisible entre 4 y no entre 100.
        for (statementDay in listOf(30, 31)) {
            val bounds = deriveCycleBounds(statementDay, dueDay = 5, referenceDate = LocalDate(2028, 2, 10))
            assertEquals(LocalDate(2028, 2, 29), bounds.endAt, "statementDay $statementDay debe clampear a 29 en bisiesto")
        }
    }

    @Test
    fun `nextCycleBounds no arrastra el clamp de febrero y vuelve al dia 31 en marzo`() {
        val febrero = deriveCycleBounds(statementDay = 31, dueDay = 5, referenceDate = LocalDate(2026, 2, 10))
        assertEquals(LocalDate(2026, 2, 28), febrero.endAt)

        val marzo = nextCycleBounds(febrero)

        assertEquals(LocalDate(2026, 3, 1), marzo.startAt)
        assertEquals(LocalDate(2026, 3, 31), marzo.endAt)
    }

    @Test
    fun `nextCycleBounds encadena el start justo despues del end anterior`() {
        val primero = deriveCycleBounds(statementDay = 15, dueDay = 25, referenceDate = LocalDate(2026, 1, 20))

        val segundo = nextCycleBounds(primero)

        assertEquals(LocalDate(2026, 2, 16), segundo.startAt)
        assertEquals(LocalDate(2026, 3, 15), segundo.endAt)
        assertEquals(LocalDate(2026, 3, 25), segundo.payAt)
    }
}
