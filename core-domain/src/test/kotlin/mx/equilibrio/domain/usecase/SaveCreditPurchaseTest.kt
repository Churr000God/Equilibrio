package mx.equilibrio.domain.usecase

import kotlinx.coroutines.runBlocking
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

class SaveCreditPurchaseTest {

    private val occurredAt = LocalDate(2026, 6, 10)

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
        val saveCreditPurchase: SaveCreditPurchase,
        val periodRepository: FakePeriodRepository,
        val transactionRepository: FakeTransactionRepository,
    )

    /** [carriedBalanceCents] y [existingSpentCents] simulan un periodo con historia previa. */
    private suspend fun setUp(
        creditLimitCents: Long,
        carriedBalanceCents: Long = 0,
        existingSpentCents: Long = 0,
    ): Fixture {
        val accountRepository = FakeAccountRepository(mapOf("cc1" to account(creditLimitCents)))
        val periodRepository = FakePeriodRepository()
        val transactionRepository = FakeTransactionRepository()
        val getOrCreatePeriodForDate = GetOrCreatePeriodForDate(periodRepository, accountRepository)
        val saveCreditPurchase = SaveCreditPurchase(GetAccount(accountRepository), getOrCreatePeriodForDate, transactionRepository)

        if (carriedBalanceCents != 0L || existingSpentCents != 0L) {
            val period = getOrCreatePeriodForDate("cc1", occurredAt)
            periodRepository.upsert(period.copy(carriedBalanceCents = carriedBalanceCents))
            if (existingSpentCents != 0L) {
                transactionRepository.upsert(
                    Transaction(
                        id = "previo",
                        userId = "u1",
                        accountId = "cc1",
                        kind = TransactionKind.EXPENSE,
                        classification = null as Classification?,
                        amountCents = existingSpentCents,
                        occurredAt = occurredAt,
                        periodId = period.id,
                    ),
                )
            }
        }
        return Fixture(saveCreditPurchase, periodRepository, transactionRepository)
    }

    private suspend fun purchase(fixture: Fixture, amountCents: Long) = fixture.saveCreditPurchase(
        id = "compra",
        accountId = "cc1",
        classification = null,
        amountCents = amountCents,
        occurredAt = occurredAt,
        note = null,
        categoryId = null,
        userId = "u1",
        today = occurredAt,
    )

    private fun expectRejected(creditLimitCents: Long, amountCents: Long, carriedBalanceCents: Long = 0, existingSpentCents: Long = 0) {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val fixture = setUp(creditLimitCents, carriedBalanceCents, existingSpentCents)
                purchase(fixture, amountCents)
            }
        }
    }

    @Test
    fun `rechaza una compra que excede el disponible sin carryover previo`() {
        expectRejected(creditLimitCents = 1_000_00, amountCents = 1_000_01)
    }

    @Test
    fun `rechaza una compra que excede el disponible con carryover previo`() {
        // disponible = 1000 - (500 + 0 - 0) = 500
        expectRejected(creditLimitCents = 1_000_00, amountCents = 500_01, carriedBalanceCents = 500_00)
    }

    @Test
    fun `el gasto ya existente en el periodo tambien reduce el disponible`() {
        // disponible = 1000 - (0 + 700 - 0) = 300
        expectRejected(creditLimitCents = 1_000_00, amountCents = 300_01, existingSpentCents = 700_00)
    }

    @Test
    fun `acepta una compra justo en el limite disponible`() = runTest {
        val fixture = setUp(creditLimitCents = 1_000_00, carriedBalanceCents = 200_00)

        // disponible = 1000 - (200 + 0 - 0) = 800
        val transaction = purchase(fixture, 800_00)

        assertEquals(800_00L, transaction.amountCents)
        assertEquals(TransactionKind.EXPENSE, transaction.kind)
    }

    @Test
    fun `asigna el periodId correcto a la transaccion guardada`() = runTest {
        val fixture = setUp(creditLimitCents = 1_000_00)

        val transaction = purchase(fixture, 100_00)

        val periodoEsperado = fixture.periodRepository.all().single()
        assertEquals(periodoEsperado.id, transaction.periodId)
    }

    @Test
    fun `rechaza editar una compra ya confirmada para ponerle fecha futura`() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                val fixture = setUp(creditLimitCents = 1_000_00)
                fixture.transactionRepository.upsert(
                    Transaction(
                        id = "compra",
                        userId = "u1",
                        accountId = "cc1",
                        kind = TransactionKind.EXPENSE,
                        classification = null as Classification?,
                        amountCents = 100_00,
                        occurredAt = occurredAt,
                        status = TransactionStatus.COMPLETED,
                    ),
                )

                fixture.saveCreditPurchase(
                    id = "compra",
                    accountId = "cc1",
                    classification = null,
                    amountCents = 100_00,
                    occurredAt = LocalDate(2026, 7, 1),
                    note = null,
                    categoryId = null,
                    userId = "u1",
                    today = occurredAt,
                )
            }
        }
    }
}
