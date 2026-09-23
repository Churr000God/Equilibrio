package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class SaveTransactionTest {

    private val today = LocalDate(2026, 6, 10)
    private val future = LocalDate(2026, 6, 20)
    private val past = LocalDate(2026, 6, 1)

    private val account = Account(id = "cash", userId = "u1", name = "Efectivo", type = AccountType.CASH)

    private fun expense(
        id: String = "t1",
        occurredAt: LocalDate,
        status: TransactionStatus,
    ) = Transaction(
        id = id,
        userId = "u1",
        accountId = "cash",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        occurredAt = occurredAt,
        status = status,
    )

    private fun setUp(vararg seed: Transaction): Pair<SaveTransaction, FakeTransactionRepository> {
        val transactionRepository = FakeTransactionRepository()
        transactionRepository.seed(*seed)
        val accountRepository = FakeAccountRepository(mapOf("cash" to account))
        return SaveTransaction(transactionRepository, GetAccount(accountRepository)) to transactionRepository
    }

    @Test
    fun `rechaza crear directamente como confirmada con fecha futura`() {
        val (saveTransaction, _) = setUp()

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                saveTransaction(expense(occurredAt = future, status = TransactionStatus.COMPLETED), today)
            }
        }
    }

    @Test
    fun `acepta crear como programada con fecha futura`() = runTest {
        val (saveTransaction, repository) = setUp()

        saveTransaction(expense(occurredAt = future, status = TransactionStatus.SCHEDULED), today)

        assertEquals(TransactionStatus.SCHEDULED, repository.getById("t1")?.status)
    }

    @Test
    fun `rechaza editar una transaccion ya confirmada para ponerle fecha futura`() {
        val (saveTransaction, _) = setUp(expense(occurredAt = past, status = TransactionStatus.COMPLETED))

        assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                saveTransaction(expense(occurredAt = future, status = TransactionStatus.SCHEDULED), today)
            }
        }
    }

    @Test
    fun `permite editar una transaccion confirmada manteniendo fecha no futura`() = runTest {
        val (saveTransaction, repository) = setUp(expense(occurredAt = past, status = TransactionStatus.COMPLETED))

        saveTransaction(expense(occurredAt = today, status = TransactionStatus.COMPLETED, id = "t1"), today)

        assertEquals(today, repository.getById("t1")?.occurredAt)
    }

    @Test
    fun `permite editar una transaccion programada a otra fecha futura`() = runTest {
        val (saveTransaction, repository) = setUp(expense(occurredAt = future, status = TransactionStatus.SCHEDULED))

        val otherFuture = LocalDate(2026, 6, 25)
        saveTransaction(expense(occurredAt = otherFuture, status = TransactionStatus.SCHEDULED), today)

        assertEquals(otherFuture, repository.getById("t1")?.occurredAt)
    }
}
