package mx.equilibrio.domain.usecase

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Account
import mx.equilibrio.domain.model.AccountType
import mx.equilibrio.domain.model.TransactionKind
import mx.equilibrio.domain.model.TransactionStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreateTransferTest {

    private val today = LocalDate(2026, 9, 21)

    private fun account(id: String, type: AccountType) =
        Account(id = id, userId = "u1", name = id, type = type)

    private fun useCase(vararg accounts: Account): Pair<CreateTransfer, FakeTransferRepository> {
        val accountRepository = FakeAccountRepository(accounts.associateBy { it.id })
        val transferRepository = FakeTransferRepository()
        val createTransfer = CreateTransfer(GetAccount(accountRepository), transferRepository)
        return createTransfer to transferRepository
    }

    private fun expectThrows(block: suspend () -> Unit): IllegalArgumentException =
        assertThrows(IllegalArgumentException::class.java) { runBlocking { block() } }

    @Test
    fun `misma cuenta origen y destino lanza`() {
        val cash = account("cash", AccountType.CASH)
        val (createTransfer, _) = useCase(cash)

        expectThrows {
            createTransfer(
                originAccountId = "cash",
                destinationAccountId = "cash",
                amountCents = 1_00,
                occurredAt = today,
                note = null,
                userId = "u1",
                expenseTransactionId = "e1",
                incomeTransactionId = "i1",
                today = today,
            )
        }
    }

    @Test
    fun `cuenta origen inexistente lanza`() {
        val bank = account("bank", AccountType.BANK)
        val (createTransfer, _) = useCase(bank)

        expectThrows {
            createTransfer(
                originAccountId = "no-existe",
                destinationAccountId = "bank",
                amountCents = 1_00,
                occurredAt = today,
                note = null,
                userId = "u1",
                expenseTransactionId = "e1",
                incomeTransactionId = "i1",
                today = today,
            )
        }
    }

    @Test
    fun `cuenta origen CREDIT_CARD se rechaza`() {
        val creditCard = account("cc", AccountType.CREDIT_CARD)
        val bank = account("bank", AccountType.BANK)
        val (createTransfer, _) = useCase(creditCard, bank)

        val error = expectThrows {
            createTransfer(
                originAccountId = "cc",
                destinationAccountId = "bank",
                amountCents = 1_00,
                occurredAt = today,
                note = null,
                userId = "u1",
                expenseTransactionId = "e1",
                incomeTransactionId = "i1",
                today = today,
            )
        }
        assertTrue(error.message!!.contains("origen"), "mensaje debe mencionar la cuenta origen")
    }

    @Test
    fun `cuenta destino CREDIT_CARD se rechaza`() {
        val cash = account("cash", AccountType.CASH)
        val creditCard = account("cc", AccountType.CREDIT_CARD)
        val (createTransfer, _) = useCase(cash, creditCard)

        val error = expectThrows {
            createTransfer(
                originAccountId = "cash",
                destinationAccountId = "cc",
                amountCents = 1_00,
                occurredAt = today,
                note = null,
                userId = "u1",
                expenseTransactionId = "e1",
                incomeTransactionId = "i1",
                today = today,
            )
        }
        assertTrue(error.message!!.contains("destino"), "mensaje debe mencionar la cuenta destino")
    }

    @Test
    fun `CASH a BANK crea egreso e ingreso COMPLETED con el mismo monto`() = runTest {
        val cash = account("cash", AccountType.CASH)
        val bank = account("bank", AccountType.BANK)
        val (createTransfer, transferRepository) = useCase(cash, bank)

        createTransfer(
            originAccountId = "cash",
            destinationAccountId = "bank",
            amountCents = 500_00,
            occurredAt = today,
            note = "traspaso",
            userId = "u1",
            expenseTransactionId = "e1",
            incomeTransactionId = "i1",
            today = today,
        )

        val (expense, income) = transferRepository.lastCreateArgs!!
        assertEquals(TransactionKind.EXPENSE, expense.kind)
        assertEquals("cash", expense.accountId)
        assertEquals(TransactionKind.INCOME, income.kind)
        assertEquals("bank", income.accountId)
        assertEquals(500_00L, expense.amountCents)
        assertEquals(500_00L, income.amountCents)
        assertEquals(TransactionStatus.COMPLETED, expense.status)
        assertEquals(TransactionStatus.COMPLETED, income.status)
    }

    @Test
    fun `fecha futura deriva status SCHEDULED en ambas patas`() = runTest {
        val cash = account("cash", AccountType.CASH)
        val bank = account("bank", AccountType.BANK)
        val (createTransfer, transferRepository) = useCase(cash, bank)
        val future = LocalDate(2026, 12, 25)

        createTransfer(
            originAccountId = "cash",
            destinationAccountId = "bank",
            amountCents = 100_00,
            occurredAt = future,
            note = null,
            userId = "u1",
            expenseTransactionId = "e1",
            incomeTransactionId = "i1",
            today = today,
        )

        val (expense, income) = transferRepository.lastCreateArgs!!
        assertEquals(TransactionStatus.SCHEDULED, expense.status)
        assertEquals(TransactionStatus.SCHEDULED, income.status)
    }
}
