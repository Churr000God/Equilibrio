package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

class GenerateDueRecurringTransactionsTest {

    private val bank = Account(id = "bank1", userId = "u1", name = "Débito", type = AccountType.BANK)

    private fun series(
        id: String,
        accountId: String = "bank1",
        frequency: RecurrenceFrequency = RecurrenceFrequency.WEEKLY,
        anchorDay: Int? = null,
        nextOccurrenceAt: LocalDate,
        isActive: Boolean = true,
    ) = RecurringTransaction(
        id = id,
        userId = "u1",
        accountId = accountId,
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        frequency = frequency,
        anchorDay = anchorDay,
        nextOccurrenceAt = nextOccurrenceAt,
        isActive = isActive,
        createdAt = LocalDate(2026, 1, 1),
    )

    private class Fixture(
        val useCase: GenerateDueRecurringTransactions,
        val recurringRepository: FakeRecurringTransactionRepository,
        val transactionRepository: FakeTransactionRepository,
    )

    private fun fixture(accountRepository: AccountRepository = FakeAccountRepository(mapOf("bank1" to bank))): Fixture {
        val recurringRepository = FakeRecurringTransactionRepository()
        val transactionRepository = FakeTransactionRepository()
        val useCase = GenerateDueRecurringTransactions(recurringRepository, transactionRepository, GetAccount(accountRepository))
        return Fixture(useCase, recurringRepository, transactionRepository)
    }

    @Test
    fun `nada vencido no genera nada`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 20)))

        fixture.useCase(LocalDate(2026, 9, 10))

        assertTrue(fixture.transactionRepository.observeAll().first().isEmpty())
        assertEquals(LocalDate(2026, 9, 20), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `una vencida genera una transaccion COMPLETED con recurringSeriesId y avanza la serie`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 1)))

        fixture.useCase(LocalDate(2026, 9, 5))

        val generated = fixture.transactionRepository.observeAll().first().single()
        assertEquals("s1", generated.recurringSeriesId)
        assertEquals(LocalDate(2026, 9, 1), generated.occurredAt)
        assertEquals(TransactionStatus.COMPLETED, generated.status)
        assertEquals(LocalDate(2026, 9, 8), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `catch-up de 3 ciclos atrasados en una sola llamada`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 1)))

        fixture.useCase(LocalDate(2026, 9, 21))

        val generated = fixture.transactionRepository.observeAll().first().sortedBy { it.occurredAt }
        assertEquals(3, generated.size)
        assertEquals(LocalDate(2026, 9, 1), generated[0].occurredAt)
        assertEquals(LocalDate(2026, 9, 8), generated[1].occurredAt)
        assertEquals(LocalDate(2026, 9, 15), generated[2].occurredAt)
        assertEquals(LocalDate(2026, 9, 22), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `catch-up con ancla 31 preserva el no-drift`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(
            series("s1", frequency = RecurrenceFrequency.MONTHLY, anchorDay = 31, nextOccurrenceAt = LocalDate(2026, 1, 31)),
        )

        fixture.useCase(LocalDate(2026, 4, 1))

        val generated = fixture.transactionRepository.observeAll().first().sortedBy { it.occurredAt }
        assertEquals(3, generated.size)
        assertEquals(LocalDate(2026, 1, 31), generated[0].occurredAt)
        assertEquals(LocalDate(2026, 2, 28), generated[1].occurredAt)
        assertEquals(LocalDate(2026, 3, 31), generated[2].occurredAt, "el ancla 31 debe recuperarse en marzo, no quedar en 28")
        assertEquals(LocalDate(2026, 4, 30), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `serie inactiva no genera`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 1), isActive = false))

        fixture.useCase(LocalDate(2026, 9, 10))

        assertTrue(fixture.transactionRepository.observeAll().first().isEmpty())
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    fun `cuenta inexistente desactiva la serie y no cuelga`() = runTest {
        val fixture = fixture(accountRepository = FakeAccountRepository(emptyMap()))
        fixture.recurringRepository.seed(series("s1", accountId = "no-existe", nextOccurrenceAt = LocalDate(2026, 9, 1)))

        fixture.useCase(LocalDate(2026, 9, 10))

        assertTrue(fixture.transactionRepository.observeAll().first().isEmpty())
        assertFalse(fixture.recurringRepository.getById("s1")!!.isActive)
    }

    @Test
    fun `cuenta CREDIT_CARD desactiva la serie`() = runTest {
        val card = Account(id = "cc1", userId = "u1", name = "Oro", type = AccountType.CREDIT_CARD, creditLimitCents = 1000_00, statementDay = 1, dueDay = 10)
        val fixture = fixture(accountRepository = FakeAccountRepository(mapOf("cc1" to card)))
        fixture.recurringRepository.seed(series("s1", accountId = "cc1", nextOccurrenceAt = LocalDate(2026, 9, 1)))

        fixture.useCase(LocalDate(2026, 9, 10))

        assertTrue(fixture.transactionRepository.observeAll().first().isEmpty())
        assertFalse(fixture.recurringRepository.getById("s1")!!.isActive)
    }

    @Test
    fun `dos series vencidas se procesan en orden cronologico`() = runTest {
        val bank2 = Account(id = "bank2", userId = "u1", name = "Cartera", type = AccountType.BANK)
        val accessOrder = mutableListOf<String>()
        val recordingRepository = object : AccountRepository {
            private val delegate = FakeAccountRepository(mapOf("bank1" to bank, "bank2" to bank2))
            override fun observeAll() = delegate.observeAll()
            override suspend fun count() = delegate.count()
            override suspend fun getById(id: String): Account? {
                accessOrder += id
                return delegate.getById(id)
            }
            override suspend fun upsert(account: Account) = delegate.upsert(account)
            override suspend fun delete(id: String) = delegate.delete(id)
        }
        val fixture = fixture(accountRepository = recordingRepository)
        fixture.recurringRepository.seed(
            series(
                "later", accountId = "bank2", frequency = RecurrenceFrequency.MONTHLY, anchorDay = 5,
                nextOccurrenceAt = LocalDate(2026, 9, 5),
            ),
            series(
                "earlier", accountId = "bank1", frequency = RecurrenceFrequency.MONTHLY, anchorDay = 1,
                nextOccurrenceAt = LocalDate(2026, 9, 1),
            ),
        )

        fixture.useCase(LocalDate(2026, 9, 10))

        assertEquals(listOf("bank1", "bank2"), accessOrder)
    }
}
