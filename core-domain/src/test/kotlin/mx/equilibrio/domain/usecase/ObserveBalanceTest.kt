package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
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

class ObserveBalanceTest {

    private val bank = Account(id = "bank", userId = "u1", name = "Débito", type = AccountType.BANK, balanceCents = 1_000_00)
    private val card = Account(
        id = "cc", userId = "u1", name = "Oro", type = AccountType.CREDIT_CARD,
        creditLimitCents = 10_000_00, statementDay = 15, dueDay = 25,
    )

    private fun period(id: String, startAt: LocalDate, endAt: LocalDate, amountPaidCents: Long = 0) = Period(
        id = id, accountId = "cc",
        startAt = startAt, endAt = endAt, payAt = endAt,
        state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = amountPaidCents,
    )

    private fun charge(id: String, accountId: String, periodId: String, cents: Long) = Transaction(
        id = id,
        userId = "u1",
        accountId = accountId,
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = cents,
        occurredAt = LocalDate(2026, 9, 1),
        periodId = periodId,
    )

    private fun useCase(
        accountRepository: FakeAccountRepository,
        periodRepository: FakePeriodRepository,
        transactionRepository: FakeTransactionRepository,
    ) = ObserveBalance(
        ObserveAvailableBalances(accountRepository, transactionRepository),
        ObserveCreditAvailable(accountRepository, periodRepository, transactionRepository),
    )

    @Test
    fun `sin tarjetas actual y proyectado son iguales al saldo`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank))
        val useCase = useCase(accountRepository, FakePeriodRepository(), FakeTransactionRepository())

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        assertEquals(1_000_00L, totals.projectedCents)
    }

    @Test
    fun `una compra de tarjeta no baja el actual, solo el proyectado`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank, "cc" to card))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        periodRepository.seed(period("p1", LocalDate(2026, 8, 16), LocalDate(2026, 9, 15)))
        transactionRepository.seed(charge("c1", "cc", "p1", 300_00))
        val useCase = useCase(accountRepository, periodRepository, transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        assertEquals(700_00L, totals.projectedCents)
    }

    @Test
    fun `pagar un periodo no descuenta dos veces`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank, "cc" to card))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        periodRepository.seed(period("p1", LocalDate(2026, 8, 16), LocalDate(2026, 9, 15)))
        transactionRepository.seed(charge("c1", "cc", "p1", 300_00))
        val payCreditPeriod = PayCreditPeriod(GetAccount(accountRepository), periodRepository, transactionRepository)
        payCreditPeriod(
            cardAccountId = "cc",
            sourceAccountId = "bank",
            amountCents = 300_00,
            occurredAt = LocalDate(2026, 9, 1),
            today = LocalDate(2026, 9, 1),
        )
        val useCase = useCase(accountRepository, periodRepository, transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(700_00L, totals.actualCents)
        assertEquals(700_00L, totals.projectedCents)
    }

    @Test
    fun `cuotas a meses descuentan el total de todas las cuotas del proyectado`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank, "cc" to card))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        val periods = (0 until 3).map { i ->
            period("p$i", LocalDate(2026, i + 9, 1), LocalDate(2026, i + 9, 28))
        }
        periodRepository.seed(*periods.toTypedArray())
        transactionRepository.seed(
            *periods.mapIndexed { i, p -> charge("c$i", "cc", p.id, 200_00) }.toTypedArray(),
        )
        val useCase = useCase(accountRepository, periodRepository, transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        assertEquals(400_00L, totals.projectedCents)
    }
}
