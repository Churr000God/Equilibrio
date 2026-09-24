package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.PeriodState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GetOrCreatePeriodForDateTest {

    private val account = Account(
        id = "cc1",
        userId = "u1",
        name = "Tarjeta",
        type = AccountType.CREDIT_CARD,
        creditLimitCents = 1_000_00,
        statementDay = 15,
        dueDay = 25,
    )

    private fun useCase(): Pair<GetOrCreatePeriodForDate, FakePeriodRepository> {
        val accountRepository = FakeAccountRepository(mapOf(account.id to account))
        val periodRepository = FakePeriodRepository()
        return GetOrCreatePeriodForDate(periodRepository, accountRepository) to periodRepository
    }

    @Test
    fun `crea el primer periodo de una cuenta sin periodos previos`() = runTest {
        val (getOrCreatePeriodForDate, periodRepository) = useCase()

        val period = getOrCreatePeriodForDate("cc1", LocalDate(2026, 1, 20))

        assertEquals(LocalDate(2026, 1, 16), period.startAt)
        assertEquals(LocalDate(2026, 2, 15), period.endAt)
        assertEquals(PeriodState.OPEN, period.state)
        assertEquals(0L, period.carriedBalanceCents)
        assertEquals(1, periodRepository.all().size)
    }

    @Test
    fun `encuentra un periodo existente sin crear uno nuevo`() = runTest {
        val (getOrCreatePeriodForDate, periodRepository) = useCase()
        val original = getOrCreatePeriodForDate("cc1", LocalDate(2026, 1, 20))

        val encontrado = getOrCreatePeriodForDate("cc1", LocalDate(2026, 1, 25))

        assertEquals(original.id, encontrado.id)
        assertEquals(1, periodRepository.all().size)
    }

    @Test
    fun `crea en cascada los periodos intermedios para una fecha 2 ciclos adelante`() = runTest {
        val (getOrCreatePeriodForDate, periodRepository) = useCase()
        val primero = getOrCreatePeriodForDate("cc1", LocalDate(2026, 1, 20))
        assertEquals(LocalDate(2026, 1, 16), primero.startAt)
        assertEquals(LocalDate(2026, 2, 15), primero.endAt)

        // Compra programada dentro del tercer ciclo (2 periodos adelante del primero).
        val lejano = getOrCreatePeriodForDate("cc1", LocalDate(2026, 4, 10))

        assertEquals(LocalDate(2026, 3, 16), lejano.startAt)
        assertEquals(LocalDate(2026, 4, 15), lejano.endAt)
        assertEquals(0L, lejano.carriedBalanceCents, "los periodos intermedios placeholder arrancan en 0")
        assertEquals(3, periodRepository.all().size, "debieron crearse 2 periodos intermedios además del primero")
        assertTrue(periodRepository.all().all { it.state == PeriodState.OPEN })
    }
}
