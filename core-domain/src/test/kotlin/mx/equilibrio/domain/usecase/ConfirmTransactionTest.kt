package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * El delegado a [mx.equilibrio.domain.repository.TransactionRepository.confirm]
 * (atomicidad de transferencias, ajuste de fecha futura -> hoy) está cubierto
 * en core-data por TransactionRepositoryImplConfirmTest. Acá se cubre el
 * disparo de la siguiente ocurrencia recurrente al confirmar a mano.
 */
class ConfirmTransactionTest {

    private val bank = Account(id = "bank1", userId = "u1", name = "Débito", type = AccountType.BANK)

    private fun series(id: String, nextOccurrenceAt: LocalDate, isActive: Boolean = true) = RecurringTransaction(
        id = id,
        userId = "u1",
        accountId = "bank1",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        frequency = RecurrenceFrequency.WEEKLY,
        nextOccurrenceAt = nextOccurrenceAt,
        isActive = isActive,
        createdAt = LocalDate(2026, 1, 1),
    )

    private fun occurrence(id: String, seriesId: String?, occurredAt: LocalDate, status: TransactionStatus = TransactionStatus.SCHEDULED) = Transaction(
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
        val confirmTransaction: ConfirmTransaction,
        val transactionRepository: FakeTransactionRepository,
        val recurringRepository: FakeRecurringTransactionRepository,
    )

    private fun fixture(): Fixture {
        val accountRepository = FakeAccountRepository(mapOf("bank1" to bank))
        val transactionRepository = FakeTransactionRepository()
        val recurringRepository = FakeRecurringTransactionRepository()
        val generateDueRecurringTransactions = GenerateDueRecurringTransactions(recurringRepository, transactionRepository, GetAccount(accountRepository))
        val confirmTransaction = ConfirmTransaction(transactionRepository, recurringRepository, generateDueRecurringTransactions)
        return Fixture(confirmTransaction, transactionRepository, recurringRepository)
    }

    @Test
    fun `confirmar una ocurrencia recurrente delega el confirm y genera la siguiente como SCHEDULED`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 15)))
        fixture.transactionRepository.seed(occurrence("o1", "s1", LocalDate(2026, 9, 8)))

        fixture.confirmTransaction("o1", LocalDate(2026, 9, 10))

        assertEquals(listOf("o1"), fixture.transactionRepository.confirmedIds)
        val transactions = fixture.transactionRepository.observeAll().first().sortedBy { it.occurredAt }
        assertEquals(2, transactions.size)
        assertEquals("o1", transactions[0].id)
        assertEquals(LocalDate(2026, 9, 15), transactions[1].occurredAt)
        assertEquals(TransactionStatus.SCHEDULED, transactions[1].status)
        assertEquals("s1", transactions[1].recurringSeriesId)
        assertEquals(LocalDate(2026, 9, 22), fixture.recurringRepository.getById("s1")?.nextOccurrenceAt)
    }

    @Test
    fun `confirmar una transaccion normal sin recurringSeriesId no genera nada extra`() = runTest {
        val fixture = fixture()
        fixture.transactionRepository.seed(occurrence("t1", seriesId = null, occurredAt = LocalDate(2026, 9, 8), status = TransactionStatus.SCHEDULED))

        fixture.confirmTransaction("t1", LocalDate(2026, 9, 10))

        assertEquals(listOf("t1"), fixture.transactionRepository.confirmedIds)
        assertEquals(1, fixture.transactionRepository.observeAll().first().size)
    }

    @Test
    fun `confirmar una ocurrencia de una serie pausada no genera nada`() = runTest {
        val fixture = fixture()
        fixture.recurringRepository.seed(series("s1", nextOccurrenceAt = LocalDate(2026, 9, 15), isActive = false))
        fixture.transactionRepository.seed(occurrence("o1", "s1", LocalDate(2026, 9, 8)))

        fixture.confirmTransaction("o1", LocalDate(2026, 9, 10))

        assertEquals(listOf("o1"), fixture.transactionRepository.confirmedIds)
        assertTrue(fixture.transactionRepository.observeAll().first().size == 1)
    }

    @Test
    fun `confirmar un movimiento con fecha futura falla y no lo confirma`() = runTest {
        val fixture = fixture()
        fixture.transactionRepository.seed(occurrence("o1", seriesId = null, occurredAt = LocalDate(2026, 9, 20)))

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { fixture.confirmTransaction("o1", LocalDate(2026, 9, 10)) }
        }

        assertTrue(fixture.transactionRepository.confirmedIds.isEmpty())
        assertEquals(TransactionStatus.SCHEDULED, fixture.transactionRepository.getById("o1")?.status)
    }

    @Test
    fun `confirmar un movimiento con fecha de hoy si se permite`() = runTest {
        val fixture = fixture()
        fixture.transactionRepository.seed(occurrence("o1", seriesId = null, occurredAt = LocalDate(2026, 9, 10)))

        fixture.confirmTransaction("o1", LocalDate(2026, 9, 10))

        assertEquals(listOf("o1"), fixture.transactionRepository.confirmedIds)
    }
}
