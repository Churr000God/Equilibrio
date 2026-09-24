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
import org.junit.jupiter.api.Test

class PayCreditPeriodTest {

    private val cardAccount = Account(
        id = "cc1", userId = "u1", name = "Tarjeta", type = AccountType.CREDIT_CARD,
        creditLimitCents = 10_000_00, statementDay = 15, dueDay = 25,
    )
    private val sourceAccount = Account(id = "bank1", userId = "u1", name = "Banco", type = AccountType.BANK)

    private val activePeriod = Period(
        id = "p1", accountId = "cc1",
        startAt = LocalDate(2026, 1, 16), endAt = LocalDate(2026, 2, 15), payAt = LocalDate(2026, 2, 25),
        state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = 0,
    )

    private fun useCase(period: Period = activePeriod): Triple<PayCreditPeriod, FakePeriodRepository, FakeTransactionRepository> {
        val accountRepository = FakeAccountRepository(mapOf("cc1" to cardAccount, "bank1" to sourceAccount))
        val periodRepository = FakePeriodRepository()
        periodRepository.seed(period)
        val transactionRepository = FakeTransactionRepository()
        val payCreditPeriod = PayCreditPeriod(GetAccount(accountRepository), periodRepository, transactionRepository)
        return Triple(payCreditPeriod, periodRepository, transactionRepository)
    }

    @Test
    fun `pago parcial crea el egreso en la cuenta origen y suma amountPaidCents sin cerrar el periodo`() = runTest {
        val (payCreditPeriod, periodRepository, _) = useCase()

        val payment = payCreditPeriod("cc1", "bank1", 300_00, LocalDate(2026, 2, 1), LocalDate(2026, 2, 1))

        assertEquals(TransactionKind.EXPENSE, payment.kind)
        assertEquals("bank1", payment.accountId)
        assertEquals(300_00L, payment.amountCents)
        assertEquals(null, payment.periodId, "el pago no es una transacción de tarjeta")

        val periodo = periodRepository.getById("p1")!!
        assertEquals(300_00L, periodo.amountPaidCents)
        assertEquals(PeriodState.OPEN, periodo.state, "pagar no cierra el periodo por sí solo")
    }

    @Test
    fun `pago exacto de la deuda del periodo suma correctamente amountPaidCents`() = runTest {
        val (payCreditPeriod, periodRepository, transactionRepository) = useCase()
        transactionRepository.seed(
            Transaction(
                id = "gasto", userId = "u1", accountId = "cc1", kind = TransactionKind.EXPENSE,
                classification = null as Classification?, amountCents = 1_000_00,
                occurredAt = LocalDate(2026, 1, 20), periodId = "p1",
            ),
        )

        payCreditPeriod("cc1", "bank1", 1_000_00, LocalDate(2026, 2, 1), LocalDate(2026, 2, 1))

        assertEquals(1_000_00L, periodRepository.getById("p1")!!.amountPaidCents)
    }

    @Test
    fun `sobrepago deja carriedBalanceCents negativo al cerrar el periodo`() = runTest {
        val (payCreditPeriod, periodRepository, transactionRepository) = useCase()
        transactionRepository.seed(
            Transaction(
                id = "gasto", userId = "u1", accountId = "cc1", kind = TransactionKind.EXPENSE,
                classification = null as Classification?, amountCents = 1_000_00,
                occurredAt = LocalDate(2026, 1, 20), periodId = "p1",
            ),
        )

        // Deuda real = 1000; paga 1500 (500 de más).
        payCreditPeriod("cc1", "bank1", 1_500_00, LocalDate(2026, 2, 1), LocalDate(2026, 2, 1))
        assertEquals(1_500_00L, periodRepository.getById("p1")!!.amountPaidCents)

        // Cerrar el periodo (today bien pasado su payAt) y verificar el arrastre.
        val accountRepository = FakeAccountRepository(mapOf("cc1" to cardAccount))
        val getOrCreatePeriodForDate = GetOrCreatePeriodForDate(periodRepository, accountRepository)
        val settleDuePeriods = SettleDuePeriods(periodRepository, transactionRepository, getOrCreatePeriodForDate)

        settleDuePeriods("cc1", today = LocalDate(2026, 2, 26))

        val siguiente = periodRepository.all().single { it.id != "p1" }
        // finalBalance = carried(0) + gastado(1000) - pagado(1500) = -500 (saldo a favor)
        assertEquals(-500_00L, siguiente.carriedBalanceCents)
    }
}
