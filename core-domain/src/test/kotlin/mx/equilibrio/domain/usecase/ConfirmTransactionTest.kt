package mx.equilibrio.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * El use case es un delegado simple; la atomicidad real de confirmar ambas
 * patas de una transferencia vive en TransactionRepositoryImpl.confirm (Room
 * transaction) — cubierta en core-data por TransactionRepositoryImplConfirmTest.
 */
class ConfirmTransactionTest {

    @Test
    fun `delega el id recibido al repositorio sin transformarlo`() = runTest {
        val repository = FakeTransactionRepository()
        val confirmTransaction = ConfirmTransaction(repository)

        confirmTransaction("t1")

        assertEquals(listOf("t1"), repository.confirmedIds)
    }
}
