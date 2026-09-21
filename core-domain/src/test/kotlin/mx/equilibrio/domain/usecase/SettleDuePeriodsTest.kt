package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Period
import mx.equilibrio.domain.model.PeriodState
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class SettleDuePeriodsTest {

    private val account = Account(
        id = "cc1",
        userId = "u1",
        name = "Tarjeta",
        type = AccountType.CREDIT_CARD,
        creditLimitCents = 10_000_00,
        statementDay = 15,
        dueDay = 25,
    )

    private fun useCase(): Triple<SettleDuePeriods, FakePeriodRepository, FakeTransactionRepository> {
        val accountRepository = FakeAccountRepository(mapOf(account.id to account))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        val getOrCreatePeriodForDate = GetOrCreatePeriodForDate(periodRepository, accountRepository)
        val settleDuePeriods = SettleDuePeriods(periodRepository, transactionRepository, getOrCreatePeriodForDate)
        return Triple(settleDuePeriods, periodRepository, transactionRepository)
    }

    private fun expense(id: String, periodId: String, amountCents: Long, occurredAt: LocalDate) = Transaction(
        id = id,
        userId = "u1",
        accountId = "cc1",
        kind = TransactionKind.EXPENSE,
        classification = null as Classification?,
        amountCents = amountCents,
        occurredAt = occurredAt,
        periodId = periodId,
    )

    @Test
    fun `OPEN pasa a AWAITING_PAYMENT al pasar end_at`() = runTest {
        val (settleDuePeriods, periodRepository, _) = useCase()
        val p1 = Period(
            id = "p1", accountId = "cc1",
            startAt = LocalDate(2026, 1, 16), endAt = LocalDate(2026, 2, 15), payAt = LocalDate(2026, 2, 25),
            state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = 0,
        )
        periodRepository.seed(p1)

        settleDuePeriods("cc1", today = LocalDate(2026, 2, 16))

        assertEquals(PeriodState.AWAITING_PAYMENT, periodRepository.getById("p1")?.state)
    }

    @Test
    fun `AWAITING_PAYMENT pasa a CLOSED al pasar pay_at y arrastra el carryover al siguiente periodo`() = runTest {
        val (settleDuePeriods, periodRepository, transactionRepository) = useCase()
        val p1 = Period(
            id = "p1", accountId = "cc1",
            startAt = LocalDate(2026, 1, 16), endAt = LocalDate(2026, 2, 15), payAt = LocalDate(2026, 2, 25),
            state = PeriodState.AWAITING_PAYMENT, carriedBalanceCents = 1_000_00, amountPaidCents = 200_00,
        )
        periodRepository.seed(p1)
        transactionRepository.seed(expense("t1", "p1", 5_000_00, LocalDate(2026, 1, 20)))

        settleDuePeriods("cc1", today = LocalDate(2026, 2, 26))

        assertEquals(PeriodState.CLOSED, periodRepository.getById("p1")?.state)
        val siguiente = periodRepository.all().single { it.id != "p1" }
        assertEquals(LocalDate(2026, 2, 16), siguiente.startAt)
        assertEquals(PeriodState.OPEN, siguiente.state)
        // finalBalance = carried(1000) + gastado(5000) - pagado(200) = 5800
        assertEquals(5_800_00L, siguiente.carriedBalanceCents)
    }

    @Test
    fun `si el siguiente periodo ya fue pre-creado no se duplica, solo se actualiza su carryover`() = runTest {
        val (settleDuePeriods, periodRepository, transactionRepository) = useCase()
        val p1 = Period(
            id = "p1", accountId = "cc1",
            startAt = LocalDate(2026, 1, 16), endAt = LocalDate(2026, 2, 15), payAt = LocalDate(2026, 2, 25),
            state = PeriodState.AWAITING_PAYMENT, carriedBalanceCents = 1_000_00, amountPaidCents = 200_00,
        )
        // Pre-creado por una compra lejana (GetOrCreatePeriodForDate), todavía con el placeholder en 0.
        val preCreado = Period(
            id = "p2-precreado", accountId = "cc1",
            startAt = LocalDate(2026, 2, 16), endAt = LocalDate(2026, 3, 15), payAt = LocalDate(2026, 3, 25),
            state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = 0,
        )
        periodRepository.seed(p1, preCreado)
        transactionRepository.seed(expense("t1", "p1", 5_000_00, LocalDate(2026, 1, 20)))

        settleDuePeriods("cc1", today = LocalDate(2026, 3, 1))

        assertEquals(2, periodRepository.all().size, "no debe crear un tercer periodo, debe reusar el pre-creado")
        val actualizado = periodRepository.getById("p2-precreado")
        assertNotNull(actualizado)
        assertEquals(5_800_00L, actualizado!!.carriedBalanceCents)
        assertEquals(PeriodState.OPEN, actualizado.state, "no vencido todavía (today <= endAt), no debe transicionar más")
    }

    @Test
    fun `resuelve en una sola llamada varios ciclos vencidos en cascada`() = runTest {
        val (settleDuePeriods, periodRepository, transactionRepository) = useCase()
        val p1 = Period(
            id = "p1", accountId = "cc1",
            startAt = LocalDate(2026, 1, 16), endAt = LocalDate(2026, 2, 15), payAt = LocalDate(2026, 2, 25),
            state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = 0,
        )
        periodRepository.seed(p1)
        transactionRepository.seed(expense("t1", "p1", 3_000_00, LocalDate(2026, 1, 20)))

        // today está muy por delante: p1 (Feb), y los siguientes 2 ciclos (Mar, Abr) que ni
        // siquiera existen todavía, deben quedar todos CLOSED en una sola llamada.
        settleDuePeriods("cc1", today = LocalDate(2026, 5, 1))

        val periodos = periodRepository.all().sortedBy { it.startAt }
        assertEquals(4, periodos.size, "p1 + 3 ciclos encadenados y cerrados en cascada")
        assertEquals(PeriodState.CLOSED, periodos[0].state)
        assertEquals(PeriodState.CLOSED, periodos[1].state)
        assertEquals(PeriodState.CLOSED, periodos[2].state)
        assertEquals(PeriodState.OPEN, periodos[3].state, "el último ciclo (mayo) todavía no vence")

        // Sin gasto nuevo en los ciclos intermedios, el carryover de 3000 viaja sin cambios.
        assertEquals(3_000_00L, periodos[1].carriedBalanceCents)
        assertEquals(3_000_00L, periodos[2].carriedBalanceCents)
        assertEquals(3_000_00L, periodos[3].carriedBalanceCents)
        assertEquals(LocalDate(2026, 4, 16), periodos[3].startAt)
        assertEquals(LocalDate(2026, 5, 15), periodos[3].endAt)
    }
}
