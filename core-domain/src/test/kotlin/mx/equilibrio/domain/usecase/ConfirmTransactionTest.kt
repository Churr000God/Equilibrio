package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlinx.datetime.LocalDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * El use case es un delegado simple; la atomicidad real de confirmar ambas
 * patas de una transferencia, y el ajuste de fecha futura -> hoy, viven en
 * TransactionRepositoryImpl.confirm (Room transaction) — cubierta en
 * core-data por TransactionRepositoryImplConfirmTest.
 */
class ConfirmTransactionTest {

    @Test
    fun `delega el id y la fecha recibidos al repositorio sin transformarlos`() = runTest {
        val repository = FakeTransactionRepository()
        val today = LocalDate(2026, 6, 10)
        val confirmTransaction = ConfirmTransaction(repository)

        confirmTransaction("t1", today)

        assertEquals(listOf("t1"), repository.confirmedIds)
    }
}
