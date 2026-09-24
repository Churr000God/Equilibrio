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
import mx.equilibrio.domain.model.TransactionStatus
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

    private fun scheduled(
        id: String,
        accountId: String,
        kind: TransactionKind,
        cents: Long,
        occurredAt: LocalDate,
        periodId: String? = null,
    ) = Transaction(
        id = id,
        userId = "u1",
        accountId = accountId,
        kind = kind,
        classification = if (kind == TransactionKind.INCOME) Classification.FIXED else Classification.ESSENTIAL,
        amountCents = cents,
        occurredAt = occurredAt,
        status = TransactionStatus.SCHEDULED,
        periodId = periodId,
    )

    private fun useCase(
        accountRepository: FakeAccountRepository,
        periodRepository: FakePeriodRepository,
        transactionRepository: FakeTransactionRepository,
    ) = ObserveBalance(
        ObserveAvailableBalances(accountRepository, transactionRepository),
        ObserveCreditAvailable(accountRepository, periodRepository, transactionRepository),
        ObserveScheduledCashFlowCents(accountRepository, transactionRepository),
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

    @Test
    fun `un gasto programado en CASH o BANK baja el proyectado pero no el actual`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank))
        val transactionRepository = FakeTransactionRepository()
        transactionRepository.seed(scheduled("s1", "bank", TransactionKind.EXPENSE, 200_00, LocalDate(2026, 9, 15)))
        val useCase = useCase(accountRepository, FakePeriodRepository(), transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        assertEquals(800_00L, totals.projectedCents)
    }

    @Test
    fun `un ingreso programado en CASH o BANK sube el proyectado`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank))
        val transactionRepository = FakeTransactionRepository()
        transactionRepository.seed(scheduled("s1", "bank", TransactionKind.INCOME, 300_00, LocalDate(2026, 9, 15)))
        val useCase = useCase(accountRepository, FakePeriodRepository(), transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        assertEquals(1_300_00L, totals.projectedCents)
    }

    @Test
    fun `una SCHEDULED en tarjeta no se cuenta dos veces, solo via totalToPayCents`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank, "cc" to card))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        periodRepository.seed(period("p1", LocalDate(2026, 8, 16), LocalDate(2026, 9, 15)))
        transactionRepository.seed(scheduled("s1", "cc", TransactionKind.EXPENSE, 300_00, LocalDate(2026, 9, 10), periodId = "p1"))
        val useCase = useCase(accountRepository, periodRepository, transactionRepository)

        val totals = useCase(LocalDate(2026, 9, 1)).first()

        assertEquals(1_000_00L, totals.actualCents)
        // Si se contara dos veces (acá y en totalToPayCents) daría 400_00, no 700_00.
        assertEquals(700_00L, totals.projectedCents)
    }

    @Test
    fun `confirmar la transaccion programada mueve el monto de proyectado a actual sin cambiar el proyectado`() = runTest {
        val accountRepository = FakeAccountRepository(mapOf("bank" to bank))
        val transactionRepository = FakeTransactionRepository()
        transactionRepository.seed(scheduled("s1", "bank", TransactionKind.EXPENSE, 200_00, LocalDate(2026, 9, 1)))
        val useCase = useCase(accountRepository, FakePeriodRepository(), transactionRepository)

        val before = useCase(LocalDate(2026, 9, 1)).first()
        assertEquals(1_000_00L, before.actualCents)
        assertEquals(800_00L, before.projectedCents)

        transactionRepository.confirm("s1", LocalDate(2026, 9, 1))

        val after = useCase(LocalDate(2026, 9, 1)).first()
        assertEquals(800_00L, after.actualCents)
        assertEquals(800_00L, after.projectedCents, "el proyectado no debe moverse: el actual solo alcanza al valor que ya proyectaba")
    }
}
