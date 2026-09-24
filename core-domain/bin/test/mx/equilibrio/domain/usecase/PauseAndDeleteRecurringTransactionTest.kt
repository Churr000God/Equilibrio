package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import mx.equilibrio.domain.model.Classification
import mx.equilibrio.domain.model.RecurrenceFrequency
import mx.equilibrio.domain.model.RecurringTransaction
import mx.equilibrio.domain.model.Transaction
import mx.equilibrio.domain.model.TransactionKind
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

/**
 * Espejo deliberadamente invertido de [DeleteInstallmentPlanTest]: ahí borrar el
 * plan borra en cascada todas sus cuotas, porque son acotadas y nacen juntas.
 * Acá una serie recurrente es una plantilla que sigue generando ocurrencias
 * independientes — el dinero de las ya generadas ya se movió, así que ni
 * pausar ni borrar la plantilla debe tocarlas. Si en algún momento alguien
 * "corrige" esto copiando el patrón de instalments, este test debe fallar.
 */
class PauseAndDeleteRecurringTransactionTest {

    private fun series(id: String) = RecurringTransaction(
        id = id,
        userId = "u1",
        accountId = "bank1",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        frequency = RecurrenceFrequency.MONTHLY,
        anchorDay = 1,
        nextOccurrenceAt = LocalDate(2026, 10, 1),
        createdAt = LocalDate(2026, 7, 1),
    )

    private fun occurrence(id: String, seriesId: String, occurredAt: LocalDate) = Transaction(
        id = id,
        userId = "u1",
        accountId = "bank1",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 100_00,
        occurredAt = occurredAt,
        recurringSeriesId = seriesId,
    )

    private fun unrelated() = Transaction(
        id = "no-relacionada",
        userId = "u1",
        accountId = "bank1",
        kind = TransactionKind.EXPENSE,
        classification = Classification.ESSENTIAL,
        amountCents = 50_00,
        occurredAt = LocalDate(2026, 9, 20),
    )

    @Test
    fun `pausar la serie no toca las ocurrencias ya generadas`() = runTest {
        val recurringRepository = FakeRecurringTransactionRepository()
        val transactionRepository = FakeTransactionRepository()
        recurringRepository.seed(series("s1"))
        transactionRepository.seed(
            occurrence("o1", "s1", LocalDate(2026, 7, 1)),
            occurrence("o2", "s1", LocalDate(2026, 8, 1)),
            occurrence("o3", "s1", LocalDate(2026, 9, 1)),
            unrelated(),
        )
        val pauseRecurringTransaction = PauseRecurringTransaction(recurringRepository)

        pauseRecurringTransaction("s1")

        assertFalse(recurringRepository.getById("s1")!!.isActive)
        assertNotNull(transactionRepository.getById("o1"))
        assertNotNull(transactionRepository.getById("o2"))
        assertNotNull(transactionRepository.getById("o3"))
        assertNotNull(transactionRepository.getById("no-relacionada"))
    }

    @Test
    fun `borrar la plantilla no toca las ocurrencias ya generadas`() = runTest {
        val recurringRepository = FakeRecurringTransactionRepository()
        val transactionRepository = FakeTransactionRepository()
        recurringRepository.seed(series("s1"))
        transactionRepository.seed(
            occurrence("o1", "s1", LocalDate(2026, 7, 1)),
            occurrence("o2", "s1", LocalDate(2026, 8, 1)),
            occurrence("o3", "s1", LocalDate(2026, 9, 1)),
            unrelated(),
        )
        val deleteRecurringTransaction = DeleteRecurringTransaction(recurringRepository)

        deleteRecurringTransaction("s1")

        assertNull(recurringRepository.getById("s1"))
        assertNotNull(transactionRepository.getById("o1"))
        assertNotNull(transactionRepository.getById("o2"))
        assertNotNull(transactionRepository.getById("o3"))
        assertNotNull(transactionRepository.getById("no-relacionada"))
    }
}
