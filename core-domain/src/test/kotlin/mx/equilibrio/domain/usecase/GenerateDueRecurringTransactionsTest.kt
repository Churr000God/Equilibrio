package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import mx.equilibrio.domain.repository.AccountRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import java.util.concurrent.TimeUnit

/**
 * Red de seguridad, NO catch-up: solo materializa la primera ocurrencia de una
 * serie que todavía no generó ninguna. Una vez que una serie tiene al menos
 * una ocurrencia (SCHEDULED o COMPLETED), esta clase no la vuelve a tocar por
 * más vencida que esté — la siguiente se dispara al confirmar la anterior a
 * mano, eso lo cubre [ConfirmTransactionTest].
 */
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

    private fun occurrence(id: String, seriesId: String, occurredAt: LocalDate, status: TransactionStatus) = Transaction(
        id = id,
        userId = "u1",
        accountId = "bank1",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        occurredAt = occurredAt,
        status = status,
        recurringSeriesId = seriesId,
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
    fun `serie sin ninguna ocurrencia genera la primera, SCHEDULED si nextOccurrenceAt es futura`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 20)))

        fixture.useCase(LocalDate(2026, 9, 10))

        val generated = fixture.transactionRepository.observeAll().first().single()
        assertEquals("s1", generated.recurringSeriesId)
        assertEquals(LocalDate(2026, 9, 20), generated.occurredAt)
        assertEquals(TransactionStatus.SCHEDULED, generated.status)
        assertEquals(LocalDate(2026, 9, 27), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `serie sin ninguna ocurrencia genera la primera, COMPLETED si nextOccurrenceAt ya paso`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 1)))

        fixture.useCase(LocalDate(2026, 9, 10))

        val generated = fixture.transactionRepository.observeAll().first().single()
        assertEquals(LocalDate(2026, 9, 1), generated.occurredAt)
        assertEquals(TransactionStatus.COMPLETED, generated.status)
        assertEquals(LocalDate(2026, 9, 8), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `serie con una SCHEDULED ya generada no hace nada, sin importar cuan vencida este`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 10, 1)))
        fixture.transactionRepository.seed(occurrence("o1", "s1", LocalDate(2026, 7, 1), TransactionStatus.SCHEDULED))

        fixture.useCase(LocalDate(2026, 9, 10))

        val transactions = fixture.transactionRepository.observeAll().first()
        assertEquals(1, transactions.size, "no debe auto-confirmar ni generar nada más, aunque o1 esté vencida hace meses")
        assertEquals(TransactionStatus.SCHEDULED, transactions.single().status)
    }

    @Test
    fun `serie con una COMPLETED ya generada no hace nada`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 22)))
        fixture.transactionRepository.seed(occurrence("o1", "s1", LocalDate(2026, 9, 15), TransactionStatus.COMPLETED))

        fixture.useCase(LocalDate(2026, 9, 25))

        val transactions = fixture.transactionRepository.observeAll().first()
        assertEquals(1, transactions.size, "la siguiente se genera al confirmar, no acá")
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
    fun `dos series sin ocurrencia generan cada una la suya de forma independiente`() = runTest {
        val bank2 = Account(id = "bank2", userId = "u1", name = "Cartera", type = AccountType.BANK)
        val fixture = fixture(accountRepository = FakeAccountRepository(mapOf("bank1" to bank, "bank2" to bank2)))
        fixture.recurringRepository.seed(
            series("s1", accountId = "bank1", nextOccurrenceAt = LocalDate(2026, 9, 5)),
            series("s2", accountId = "bank2", nextOccurrenceAt = LocalDate(2026, 9, 12)),
        )

        fixture.useCase(LocalDate(2026, 9, 10))

        val bySeriesId = fixture.transactionRepository.observeAll().first().associateBy { it.recurringSeriesId }
        assertEquals(LocalDate(2026, 9, 5), bySeriesId["s1"]?.occurredAt)
        assertEquals(TransactionStatus.COMPLETED, bySeriesId["s1"]?.status)
        assertEquals(LocalDate(2026, 9, 12), bySeriesId["s2"]?.occurredAt)
        assertEquals(TransactionStatus.SCHEDULED, bySeriesId["s2"]?.status)
    }
}
