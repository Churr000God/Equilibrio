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

class ObserveAccountsAvailableTest {

    private val cash = Account(id = "cash", userId = "u1", name = "Cartera", type = AccountType.CASH, balanceCents = 500_00)
    private val bank = Account(id = "bank", userId = "u1", name = "Débito", type = AccountType.BANK, balanceCents = 1_000_00)
    private val card = Account(
        id = "cc", userId = "u1", name = "Oro", type = AccountType.CREDIT_CARD,
        creditLimitCents = 10_000_00, statementDay = 15, dueDay = 25,
    )

    private val accountRepository = FakeAccountRepository(listOf(cash, bank, card).associateBy { it.id })
    private val periodRepository = FakePeriodRepository()
    private val transactionRepository = FakeTransactionRepository()

    private val useCase = ObserveAccountsAvailable(
        ObserveAccounts(accountRepository),
        ObserveAvailableBalances(accountRepository, transactionRepository),
        ObserveCreditAvailable(accountRepository, periodRepository, transactionRepository),
    )

    private fun tx(id: String, accountId: String, kind: TransactionKind, cents: Long, periodId: String? = null) =
        Transaction(
            id = id,
            userId = "u1",
            accountId = accountId,
            kind = kind,
            classification = if (kind == TransactionKind.INCOME) Classification.FIXED else Classification.ESSENTIAL,
            amountCents = cents,
            occurredAt = LocalDate(2026, 9, 1),
            periodId = periodId,
        )

    @Test
    fun `cada tipo usa su propio calculo de disponible`() = runTest {
        periodRepository.seed(
            Period(
                id = "p1", accountId = "cc",
                startAt = LocalDate(2026, 8, 16), endAt = LocalDate(2026, 9, 15), payAt = LocalDate(2026, 9, 25),
                state = PeriodState.OPEN, carriedBalanceCents = 0, amountPaidCents = 0,
            ),
        )
        transactionRepository.seed(
            tx("t1", "cash", TransactionKind.EXPENSE, 100_00),
            tx("t2", "bank", TransactionKind.INCOME, 250_00),
            tx("t3", "cc", TransactionKind.EXPENSE, 2_000_00, periodId = "p1"),
        )

        val byId = useCase(LocalDate(2026, 9, 1)).first().associate { it.account.id to it.availableCents }

        assertEquals(400_00L, byId["cash"])
        assertEquals(1_250_00L, byId["bank"])
        assertEquals(8_000_00L, byId["cc"])
    }

    @Test
    fun `tarjeta sin periodo activo usa el limite completo como disponible`() = runTest {
        val byId = useCase(LocalDate(2026, 9, 1)).first().associate { it.account.id to it.availableCents }

        assertEquals(card.creditLimitCents, byId["cc"])
    }

}
