package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateRecurringTransactionTest {

    private val bank = Account(id = "bank1", userId = "u1", name = "Débito", type = AccountType.BANK)
    private val card = Account(
        id = "cc1", userId = "u1", name = "Oro", type = AccountType.CREDIT_CARD,
        creditLimitCents = 1000_00, statementDay = 1, dueDay = 10,
    )

    private class Fixture(
        val useCase: CreateRecurringTransaction,
        val recurringRepository: FakeRecurringTransactionRepository,
        val transactionRepository: FakeTransactionRepository,
    )

    private fun fixture(accounts: Map<String, Account> = mapOf("bank1" to bank, "cc1" to card)): Fixture {
        val accountRepository = FakeAccountRepository(accounts)
        val recurringRepository = FakeRecurringTransactionRepository()
        val transactionRepository = FakeTransactionRepository()
        val getAccount = GetAccount(accountRepository)
        val generateDueRecurringTransactions = GenerateDueRecurringTransactions(recurringRepository, transactionRepository, getAccount)
        val useCase = CreateRecurringTransaction(recurringRepository, getAccount, generateDueRecurringTransactions)
        return Fixture(useCase, recurringRepository, transactionRepository)
    }

    private suspend fun create(
        fixture: Fixture,
        accountId: String,
        frequency: RecurrenceFrequency,
        startAt: LocalDate,
        today: LocalDate,
    ) = fixture.useCase(
        id = "s1",
        userId = "u1",
        accountId = accountId,
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        note = null,
        categoryId = null,
        frequency = frequency,
        startAt = startAt,
        today = today,
    )

    @Test
    fun `rechaza cuenta CREDIT_CARD, el mensaje menciona la alternativa`() {
        val fixture = fixture()
        val exception = assertThrows(IllegalArgumentException::class.java) {
            kotlinx.coroutines.runBlocking {
                create(fixture, "cc1", RecurrenceFrequency.MONTHLY, LocalDate(2026, 9, 1), LocalDate(2026, 9, 1))
            }
        }
        assertTrue(exception.message!!.contains("compra a meses"))
    }

    @Test
    fun `CASH o BANK acepta y persiste la serie`() = runTest {
        val fixture = fixture()
        // startAt futuro respecto a today: sin generación, la serie persistida es la devuelta tal cual.
        val series = create(fixture, "bank1", RecurrenceFrequency.WEEKLY, startAt = LocalDate(2026, 9, 8), today = LocalDate(2026, 9, 1))

        assertEquals("bank1", series.accountId)
        assertEquals(series, fixture.recurringRepository.getById("s1"))
    }

    @Test
    fun `MONTHLY fija anchorDay, WEEKLY y BIWEEKLY no`() = runTest {
        val fixture = fixture()

        val monthly = create(fixture, "bank1", RecurrenceFrequency.MONTHLY, LocalDate(2026, 9, 15), LocalDate(2026, 9, 15))
        assertEquals(15, monthly.anchorDay)

        val fixture2 = fixture()
        val weekly = create(fixture2, "bank1", RecurrenceFrequency.WEEKLY, LocalDate(2026, 9, 15), LocalDate(2026, 9, 15))
        assertNull(weekly.anchorDay)

        val fixture3 = fixture()
        val biweekly = create(fixture3, "bank1", RecurrenceFrequency.BIWEEKLY, LocalDate(2026, 9, 15), LocalDate(2026, 9, 15))
        assertNull(biweekly.anchorDay)
    }

    @Test
    fun `startAt igual a today genera una ocurrencia de inmediato`() = runTest {
        val fixture = fixture()

        create(fixture, "bank1", RecurrenceFrequency.MONTHLY, LocalDate(2026, 9, 15), today = LocalDate(2026, 9, 15))

        val generated = fixture.transactionRepository.observeAll().first()
        assertEquals(1, generated.size)
        assertEquals("s1", generated.single().recurringSeriesId)
    }

    @Test
    fun `startAt futuro no genera nada todavia`() = runTest {
        val fixture = fixture()

        val series = create(fixture, "bank1", RecurrenceFrequency.MONTHLY, LocalDate(2026, 10, 15), today = LocalDate(2026, 9, 15))

        assertTrue(fixture.transactionRepository.observeAll().first().isEmpty())
        assertEquals(LocalDate(2026, 10, 15), series.nextOccurrenceAt)
    }
}
