package mx.equilibrio.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.TransactionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SaveInstallmentPurchaseTest {

    private fun account(creditLimitCents: Long) = Account(
        id = "cc1",
        userId = "u1",
        name = "Tarjeta",
        type = AccountType.CREDIT_CARD,
        creditLimitCents = creditLimitCents,
        statementDay = 28,
        dueDay = 5,
    )

    private class Fixture(
        val saveInstallmentPurchase: SaveInstallmentPurchase,
        val transactionRepository: FakeTransactionRepository,
    )

    private fun setUp(creditLimitCents: Long): Fixture {
        val accountRepository = FakeAccountRepository(mapOf("cc1" to account(creditLimitCents)))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        val getOrCreatePeriodForDate = GetOrCreatePeriodForDate(periodRepository, accountRepository)
        val saveInstallmentPurchase = SaveInstallmentPurchase(
            GetAccount(accountRepository),
            getOrCreatePeriodForDate,
            periodRepository,
            transactionRepository,
        )
        return Fixture(saveInstallmentPurchase, transactionRepository)
    }

    @Test
    fun `reparte el total con el resto en la ultima cuota`() = runTest {
        val fixture = setUp(creditLimitCents = 10_000_00)
        val today = LocalDate(2026, 6, 10)

        val installments = fixture.saveInstallmentPurchase(
            planId = "plan1",
            accountId = "cc1",
            classification = null,
            totalAmountCents = 1000,
            installmentCount = 3,
            firstOccurredAt = today,
            note = null,
            categoryId = null,
            userId = "u1",
            today = today,
        )

        assertEquals(listOf(333L, 333L, 334L), installments.map { it.amountCents })
    }

    @Test
    fun `clampea la fecha de cuota al ultimo dia del mes mas corto`() = runTest {
        val fixture = setUp(creditLimitCents = 10_000_00)
        val firstOccurredAt = LocalDate(2026, 1, 31)

        val installments = fixture.saveInstallmentPurchase(
            planId = "plan1",
            accountId = "cc1",
            classification = null,
            totalAmountCents = 300_00,
            installmentCount = 3,
            firstOccurredAt = firstOccurredAt,
            note = null,
            categoryId = null,
            userId = "u1",
            today = firstOccurredAt,
        )

        assertEquals(
            listOf(LocalDate(2026, 1, 31), LocalDate(2026, 2, 28), LocalDate(2026, 3, 31)),
            installments.map { it.occurredAt },
        )
    }

    @Test
    fun `rechaza installmentCount menor a 2`() {
        val fixture = setUp(creditLimitCents = 10_000_00)
        val today = LocalDate(2026, 6, 10)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.saveInstallmentPurchase(
                    planId = "plan1",
                    accountId = "cc1",
                    classification = null,
                    totalAmountCents = 300_00,
                    installmentCount = 1,
                    firstOccurredAt = today,
                    note = null,
                    categoryId = null,
                    userId = "u1",
                    today = today,
                )
            }
        }
    }

    @Test
    fun `rechaza si excede el limite y no persiste ninguna cuota`() {
        val fixture = setUp(creditLimitCents = 900_00)
        val today = LocalDate(2026, 6, 10)

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                fixture.saveInstallmentPurchase(
                    planId = "plan1",
                    accountId = "cc1",
                    classification = null,
                    totalAmountCents = 6_000_00,
                    installmentCount = 6,
                    firstOccurredAt = today,
                    note = null,
                    categoryId = null,
                    userId = "u1",
                    today = today,
                )
            }
        }

        assertTrue(runBlocking { fixture.transactionRepository.observeAll().first() }.isEmpty())
    }

    @Test
    fun `deriva status COMPLETED para cuotas pasadas y SCHEDULED para futuras`() = runTest {
        val fixture = setUp(creditLimitCents = 10_000_00)
        val firstOccurredAt = LocalDate(2026, 4, 10)
        val today = LocalDate(2026, 5, 15)

        val installments = fixture.saveInstallmentPurchase(
            planId = "plan1",
            accountId = "cc1",
            classification = null,
            totalAmountCents = 300_00,
            installmentCount = 3,
            firstOccurredAt = firstOccurredAt,
            note = null,
            categoryId = null,
            userId = "u1",
            today = today,
        )

        assertEquals(
            listOf(TransactionStatus.COMPLETED, TransactionStatus.COMPLETED, TransactionStatus.SCHEDULED),
            installments.map { it.status },
        )
    }
}
